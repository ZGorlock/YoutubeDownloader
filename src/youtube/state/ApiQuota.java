/*
 * File:    ApiQuota.java
 * Package: youtube.state
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Filesystem;
import commons.access.Project;
import commons.lambda.function.checked.CheckedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.util.FileUtils;
import youtube.util.LogUtils;
import youtube.util.PathUtils;

/**
 * Manages the API Quota.
 */
public class ApiQuota {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ApiQuota.class);
    
    
    //Constants
    
    /**
     * The file containing the API Quota data.
     */
    public static final File API_QUOTA_FILE = new File(Project.DATA_DIR, FileUtils.setFormat("apiQuota", FileUtils.LIST_FILE_FORMAT));
    
    /**
     * The separator used in a API Quota Entry.
     */
    public static final String SEPARATOR = "|";
    
    /**
     * The daily quota provided by the Youtube Data API.
     */
    public static final Integer DAILY_QUOTA = 10000;
    
    /**
     * The time zone used by the Youtube Data API to reset the daily quota.
     */
    public static final ZoneId QUOTA_TIMEZONE = ZoneId.of(ZoneId.SHORT_IDS.get("PST"));
    
    
    //Enums
    
    /**
     * An enumeration of API Quota Costs.
     */
    public enum QuotaCost {
        
        //Values
        
        ACTIVITIES_LIST(1),
        
        CAPTIONS_LIST(50),
        CAPTIONS_INSERT(400),
        CAPTIONS_UPDATE(450),
        CAPTIONS_DELETE(50),
        
        CHANNEL_BANNERS_INSERT(50),
        
        CHANNELS_LIST(1),
        CHANNELS_UPDATE(50),
        
        CHANNEL_SECTIONS_LIST(1),
        CHANNEL_SECTIONS_INSERT(50),
        CHANNEL_SECTIONS_UPDATE(50),
        CHANNEL_SECTIONS_DELETE(50),
        
        COMMENTS_LIST(1),
        COMMENTS_INSERT(50),
        COMMENTS_UPDATE(50),
        COMMENTS_MARK_AS_SPAM(50),
        COMMENTS_SET_MODERATION_STATUS(50),
        COMMENTS_DELETE(50),
        
        COMMENT_THREADS_LIST(1),
        COMMENT_THREADS_INSERT(50),
        COMMENT_THREADS_UPDATE(50),
        
        GUIDE_CATEGORIES_LIST(1),
        
        I18N_LANGUAGES_LIST(1),
        
        I18N_REGIONS_LIST(1),
        
        MEMBERS_LIST(1),
        
        MEMBERSHIP_LEVELS_LIST(1),
        
        PLAYLIST_ITEMS_LIST(1),
        PLAYLIST_ITEMS_INSERT(50),
        PLAYLIST_ITEMS_UPDATE(50),
        PLAYLIST_ITEMS_DELETE(50),
        
        PLAYLISTS_LIST(1),
        PLAYLISTS_INSERT(50),
        PLAYLISTS_UPDATE(50),
        PLAYLISTS_DELETE(50),
        
        SEARCH_LIST(100),
        
        SUBSCRIPTIONS_LIST(1),
        SUBSCRIPTIONS_INSERT(50),
        SUBSCRIPTIONS_DELETE(50),
        
        THUMBNAILS_SET(50),
        
        VIDEO_ABUSE_REPORT_REASONS_LIST(1),
        
        VIDEO_CATEGORIES_LIST(1),
        
        VIDEOS_LIST(1),
        VIDEOS_INSERT(1600),
        VIDEOS_UPDATE(50),
        VIDEOS_RATE(50),
        VIDEOS_GET_RATING(1),
        VIDEOS_REPORT_ABUSE(50),
        VIDEOS_DELETE(50),
        
        WATERMARKS_SET(50),
        WATERMARKS_UNSET(50);
        
        
        //Fields
        
        /**
         * The value of the Quota Cost.
         */
        public final int value;
        
        
        //Constructors
        
        /**
         * Constructs a Quota Cost.
         *
         * @param value The value of the Quota Cost.
         */
        QuotaCost(int value) {
            this.value = value;
        }
        
        
        //Getters
        
        /**
         * Returns the value of the Quota Cost.
         *
         * @return The value of the Quota Cost.
         */
        public int getValue() {
            return value;
        }
        
    }
    
    
    //Static Fields
    
    /**
     * The map containing the API Key Quota data.
     */
    private static final Map<String, KeyQuota> quota = new LinkedHashMap<>();
    
    /**
     * The default API key hash provided during initialization.
     */
    private static final AtomicReference<String> defaultKeyHash = new AtomicReference<>(null);
    
    /**
     * A flag indicating whether the API Quota has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Initializes the API Quota.
     *
     * @param keyHash The default API key hash provided during initialization..
     * @return Whether the API Quota was successfully initialized.
     */
    public static boolean initQuota(String keyHash) {
        if (loaded.compareAndSet(false, true)) {
            logger.trace(LogUtils.NEWLINE);
            logger.debug(Color.log("Initializing API Quota..."));
            
            defaultKeyHash.set(keyHash);
            
            loadQuota();
            
            return true;
        }
        return false;
    }
    
    /**
     * Initializes the API Quota.
     *
     * @return Whether the API Quota was successfully initialized.
     */
    public static boolean initQuota() {
        return initQuota(null);
    }
    
    /**
     * Loads the API Quota.
     *
     * @throws RuntimeException When the API Quota could not be loaded.
     */
    private static void loadQuota() {
        if (!loaded.get()) {
            logger.warn(Color.bad("The API Quota has not been initialized"));
            return;
        }
        
        logger.debug(Color.log("Loading API Quota..."));
        
        Optional.of(API_QUOTA_FILE)
                .filter(file -> (file.exists() || Filesystem.createFile(file)))
                .ifPresentOrElse(ApiQuota::readQuota, () -> {
                    logger.warn(Color.bad("Could not load or create API Quota file: ") + Color.quoteFilePath(API_QUOTA_FILE));
                    throw new RuntimeException(new IOException("Error reading: " + PathUtils.path(API_QUOTA_FILE)));
                });
    }
    
    /**
     * Reads the API Quota from a file.
     *
     * @param file The file.
     */
    private static synchronized void readQuota(File file) {
        quota.clear();
        Optional.ofNullable(file)
                .filter(File::exists).map(Filesystem::readLines)
                .stream().flatMap(Collection::stream)
                .map((CheckedFunction<String, KeyQuota>) KeyQuota::parse)
                .filter(Objects::nonNull).filter(KeyQuota::isValid)
                .forEachOrdered(entry -> quota.put(entry.getKeyHash(), entry));
    }
    
    /**
     * Reads the API Quota from a file.
     */
    private static synchronized void readQuota() {
        readQuota(API_QUOTA_FILE);
    }
    
    /**
     * Saves the API Quota.
     *
     * @throws RuntimeException When the API Quota could not be saved.
     */
    private static void saveQuota() {
        if (!loaded.get()) {
            logger.warn(Color.bad("The API Quota has not been initialized"));
            return;
        }
        
        logger.debug(Color.log("Saving API Quota..."));
        
        Optional.of(API_QUOTA_FILE)
                .filter(file -> (file.exists() || Filesystem.createFile(file)))
                .ifPresentOrElse(ApiQuota::writeQuota, () -> {
                    logger.error(Color.bad("Could not save or create API Quota file: ") + Color.quoteFilePath(API_QUOTA_FILE));
                    throw new RuntimeException(new IOException("Error writing: " + PathUtils.path(API_QUOTA_FILE)));
                });
    }
    
    /**
     * Writes the API Quota to a file.
     *
     * @param file The file.
     */
    private static synchronized void writeQuota(File file) {
        Filesystem.writeLines(file, quota.values().stream()
                .map(KeyQuota::format)
                .collect(Collectors.toList()));
    }
    
    /**
     * Writes the API Quota to a file.
     */
    private static synchronized void writeQuota() {
        writeQuota(API_QUOTA_FILE);
    }
    
    /**
     * Fetches an API Key Quota.
     *
     * @param keyHash The hash of the API key.
     * @return The API Key Quota.
     */
    public static synchronized KeyQuota getKeyQuota(String keyHash) {
        return quota.computeIfAbsent(keyHash, KeyQuota::new);
    }
    
    /**
     * Returns the usage of the quota.
     *
     * @param keyHash The hash of the API key.
     * @return The quota usage.
     */
    public static synchronized int getQuotaUsage(String keyHash) {
        return getKeyQuota(keyHash).getUsage();
    }
    
    /**
     * Returns the usage of the quota.
     *
     * @return The quota usage.
     */
    public static synchronized int getQuotaUsage() {
        return getQuotaUsage(defaultKeyHash.get());
    }
    
    /**
     * Returns the remaining usage of the quota.
     *
     * @param keyHash The hash of the API key.
     * @return The quota remaining.
     */
    public static synchronized int getQuotaRemaining(String keyHash) {
        return (DAILY_QUOTA - getQuotaUsage(keyHash));
    }
    
    /**
     * Returns the remaining usage of the quota.
     *
     * @return The quota remaining.
     */
    public static synchronized int getQuotaRemaining() {
        return getQuotaRemaining(defaultKeyHash.get());
    }
    
    /**
     * Registers an API call against the quota.
     *
     * @param keyHash The hash of the API key used.
     * @param cost    The cost of the API call.
     */
    public static synchronized void registerApiCall(String keyHash, int cost) {
        getKeyQuota(keyHash).update(cost);
        writeQuota();
    }
    
    /**
     * Registers an API call against the quota.
     *
     * @param cost The cost of the API call.
     */
    public static synchronized void registerApiCall(int cost) {
        registerApiCall(defaultKeyHash.get(), cost);
    }
    
    /**
     * Registers an API call against the quota.
     *
     * @param keyHash The hash of the API key used.
     * @param cost    The Quota Cost of the API call.
     */
    public static synchronized void registerApiCall(String keyHash, QuotaCost cost) {
        registerApiCall(keyHash, cost.getValue());
    }
    
    /**
     * Registers an API call against the quota.
     *
     * @param cost The Quota Cost of the API call.
     */
    public static synchronized void registerApiCall(QuotaCost cost) {
        registerApiCall(defaultKeyHash.get(), cost);
    }
    
    /**
     * Returns the current quota date, according to the Youtube Data API.
     *
     * @return The current quota date.
     */
    public static LocalDate getApiDate() {
        return ZonedDateTime.ofInstant(Instant.now(), QUOTA_TIMEZONE).toLocalDate();
    }
    
    
    //Inner Classes
    
    /**
     * Defines an API Key Quota.
     */
    public static class KeyQuota {
        
        //Enums
        
        /**
         * An enumeration of the Parts of a API Key Quota entry.
         */
        private enum Part {
            KEY_HASH,
            QUOTA_USAGE,
            QUOTA_DATE
        }
        
        
        //Fields
        
        /**
         * The hash of the API key.
         */
        public String keyHash;
        
        /**
         * The quota usage.
         */
        public Integer usage;
        
        /**
         * The quota date.
         */
        public LocalDate date;
        
        
        //Constructors
        
        /**
         * Creates an API Key Quota.
         *
         * @param keyHash The hash of the API key.
         * @param usage   The quota usage.
         * @param date    The quota date.
         */
        private KeyQuota(String keyHash, Integer usage, LocalDate date) {
            this.keyHash = keyHash;
            this.usage = usage;
            this.date = date;
        }
        
        /**
         * Creates an API Key Quota.
         *
         * @param keyHash The hash of the API key.
         * @param usage   The quota usage.
         */
        private KeyQuota(String keyHash, Integer usage) {
            this(keyHash, usage, getApiDate());
        }
        
        /**
         * Creates an API Key Quota.
         *
         * @param keyHash The hash of the API key.
         */
        private KeyQuota(String keyHash) {
            this(keyHash, 0);
        }
        
        /**
         * Creates an API Key Quota.
         *
         * @param keyHash The hash of the API key.
         * @param usage   The quota usage.
         * @param date    The quota date.
         */
        private KeyQuota(String keyHash, String usage, String date) {
            this(keyHash, Integer.parseInt(usage), LocalDate.parse(date));
        }
        
        
        //Methods
        
        /**
         * Determines whether the API Key Quota is valid.
         *
         * @return Whether the API Key Quota is valid.
         */
        public boolean isValid() {
            return (getKeyHash() != null) && (getUsage() != null) &&
                    (getDate() != null) && getDate().equals(getApiDate());
        }
        
        /**
         * Formats the API Key Quota.
         *
         * @return A line of the API Quota file.
         */
        private String format() {
            return String.join(SEPARATOR,
                    getKeyHash(), String.valueOf(getUsage()), String.valueOf(getDate()));
        }
        
        /**
         * Updates the API Key Quota.
         *
         * @param cost The cost to add to the quota usage.
         * @return The current API Key Quota usage.
         */
        public int update(int cost) {
            final LocalDate apiDate = getApiDate();
            if (!apiDate.isEqual(getDate())) {
                date = apiDate;
                usage = 0;
            }
            
            return (usage += cost);
        }
        
        
        //Getters
        
        /**
         * Returns the hash of the API key.
         *
         * @return The hash of the API key.
         */
        public String getKeyHash() {
            return keyHash;
        }
        
        /**
         * Returns the quota usage.
         *
         * @return The quota usage.
         */
        public Integer getUsage() {
            return usage;
        }
        
        /**
         * Returns the quota date.
         *
         * @return The quota date.
         */
        public LocalDate getDate() {
            return date;
        }
        
        
        //Static Methods
        
        /**
         * Parses a line from an API Quota file.
         *
         * @param apiQuotaLine The line from the API Quota file.
         * @return The API Key Quota.
         * @throws ParseException When the API Quota line is not valid.
         */
        private static KeyQuota parse(String apiQuotaLine) throws ParseException {
            return Optional.ofNullable(apiQuotaLine)
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(Pattern.quote(SEPARATOR) + "+"))
                    .filter(lineParts -> (lineParts.length == KeyQuota.Part.values().length))
                    .map(lineParts -> new KeyQuota(lineParts[0], lineParts[1], lineParts[2]))
                    .orElseThrow(() -> {
                        logger.warn(Color.bad("Unable to parse API Quota line: ") + Color.quoted(Color.base(apiQuotaLine)));
                        return new ParseException(apiQuotaLine, 0);
                    });
        }
        
    }
    
}
