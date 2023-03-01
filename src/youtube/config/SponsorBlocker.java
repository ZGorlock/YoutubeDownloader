/*
 * File:    SponsorBlocker.java
 * Package: youtube.config
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.config;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.base.ConfigData;
import youtube.util.ExecutableUtils;

/**
 * Handles configurations and commands for using SponsorBlock with yt-dlp.
 */
public class SponsorBlocker {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(SponsorBlocker.class);
    
    
    //Static Fields
    
    /**
     * The global SponsorBlock Config.
     */
    public static SponsorBlockConfig globalConfig = null;
    
    /**
     * A flag indicating whether the global SponsorBlock Config has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Static Methods
    
    /**
     * Returns the SponsorBlock command for a SponsorBlock Config.
     *
     * @param config The SponsorBlock Config.
     * @return The SponsorBlock command for the SponsorBlock Config, or an empty string if the selected Executable is not yt-dlp.
     */
    public static String getCommand(SponsorBlockConfig config) {
        final boolean configValid = (config != null) && config.isEnabled();
        final boolean globalConfigValid = (globalConfig != null) && globalConfig.isEnabled();
        
        if ((!configValid && !globalConfigValid) || ExecutableUtils.executable.isDeprecated()) {
            return "";
        }
        
        final boolean useGlobalConfig = globalConfigValid && (!configValid || (globalConfig.isForceGlobally() && !config.isOverrideGlobal()));
        final SponsorBlockConfig conf = useGlobalConfig ? globalConfig : config;
        
        return !conf.isActive() ? "" :
               conf.getCategories().stream().collect(Collectors.joining(",", "--sponsorblock-remove ", ""));
    }
    
    /**
     * Loads a SponsorBlock Config.
     *
     * @param configData The json data of the SponsorBlock Config.
     * @return The SponsorBlock Config.
     */
    public static SponsorBlockConfig loadConfig(Map<String, Object> configData) {
        return new SponsorBlockConfig(configData);
    }
    
    /**
     * Loads a Channel SponsorBlock Config.
     *
     * @param configData The json data of the Channel SponsorBlock Config.
     * @return The SponsorBlock Config.
     */
    public static SponsorBlockConfig loadChannelConfig(Map<String, Object> configData) {
        final SponsorBlockConfig config = loadConfig(configData);
        config.type = SponsorBlockConfig.Type.CHANNEL;
        return config;
    }
    
    /**
     * Loads the global SponsorBlock Config.
     *
     * @param configData The json data of the global SponsorBlock Config.
     */
    public static void loadGlobalConfig(Map<String, Object> configData) {
        if (loaded.compareAndSet(false, true)) {
            globalConfig = loadConfig(configData);
            globalConfig.type = SponsorBlockConfig.Type.GLOBAL;
        }
    }
    
    
    //Inner Classes
    
    /**
     * Defines a SponsorBlock Config.
     */
    public static class SponsorBlockConfig extends ConfigData {
        
        //Constants
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config is enabled or not.
         */
        public static final boolean DEFAULT_ENABLED = true;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should be forced globally or not.
         */
        public static final boolean DEFAULT_FORCE_GLOBALLY = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should override a globally forced SponsorBlock Config or not.
         */
        public static final boolean DEFAULT_OVERRIDE_GLOBAL = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip all segments or not.
         */
        public static final boolean DEFAULT_SKIP_ALL = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip 'sponsor' segments or not.
         */
        public static final boolean DEFAULT_SKIP_SPONSOR = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip 'intro' segments or not.
         */
        public static final boolean DEFAULT_SKIP_INTRO = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip 'outro' segments or not.
         */
        public static final boolean DEFAULT_SKIP_OUTRO = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip 'self promo' segments or not.
         */
        public static final boolean DEFAULT_SKIP_SELF_PROMO = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip 'preview' segments or not.
         */
        public static final boolean DEFAULT_SKIP_PREVIEW = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip 'interaction' segments or not.
         */
        public static final boolean DEFAULT_SKIP_INTERACTION = false;
        
        /**
         * The default value of the flag indicating whether a SponsorBlock Config should skip 'music off topic' segments or not.
         */
        public static final boolean DEFAULT_SKIP_MUSIC_OFF_TOPIC = false;
        
        
        //Enums
        
        /**
         * An enumeration of SponsorBlock Config Types.
         */
        public enum Type {
            GLOBAL,
            CHANNEL
        }
        
        /**
         * An enumeration of SponsorBlock Categories.
         */
        public enum Category {
            
            //Values
            
            ALL("all"),
            SPONSOR("sponsor"),
            INTRO("intro"),
            OUTRO("outro"),
            SELF_PROMO("selfpromo"),
            PREVIEW("preview"),
            INTERACTION("interaction"),
            MUSIC_OFF_TOPIC("music_offtopic");
            
            
            //Fields
            
            /**
             * The key of the SponsorBlock Category.
             */
            public final String key;
            
            
            //Constructors
            
            /**
             * Constructs a SponsorBlock Category.
             *
             * @param key The key of the SponsorBlock Category.
             */
            Category(String key) {
                this.key = key;
            }
            
            
            //Getters
            
            /**
             * Returns the key of the SponsorBlock Category.
             *
             * @return The key of the SponsorBlock Category.
             */
            public String getKey() {
                return key;
            }
            
        }
        
        
        //Fields
        
        /**
         * The type of the SponsorBlock Config.
         */
        public Type type;
        
        /**
         * A flag indicating whether SponsorBlock is enabled.
         */
        public boolean enabled;
        
        /**
         * A flag indicating whether this global SponsorBlock configuration should be applied globally and take precedence over standard Channel SponsorBlock configurations.
         */
        public boolean forceGlobally;
        
        /**
         * A flag indicating whether this Channel SponsorBlock configuration should override a forced Global SponsorBlock configuration.
         */
        public boolean overrideGlobal;
        
        /**
         * A flag indicating whether SponsorBlock should skip all segments.
         */
        public boolean skipAll;
        
        /**
         * A flag indicating whether SponsorBlock should skip 'sponsor' segments.
         */
        public boolean skipSponsor;
        
        /**
         * A flag indicating whether SponsorBlock should skip 'intro' segments.
         */
        public boolean skipIntro;
        
        /**
         * A flag indicating whether SponsorBlock should skip 'outro' segments.
         */
        public boolean skipOutro;
        
        /**
         * A flag indicating whether SponsorBlock should skip 'self promo' segments.
         */
        public boolean skipSelfPromo;
        
        /**
         * A flag indicating whether SponsorBlock should skip 'preview' segments.
         */
        public boolean skipPreview;
        
        /**
         * A flag indicating whether SponsorBlock should skip 'interaction' segments.
         */
        public boolean skipInteraction;
        
        /**
         * A flag indicating whether SponsorBlock should skip 'music off topic' segments.
         */
        public boolean skipMusicOffTopic;
        
        
        //Constructors
        
        /**
         * Creates a SponsorBlock Config.
         *
         * @param configData The json data of the SponsorBlock Config.
         */
        public SponsorBlockConfig(Map<String, Object> configData) {
            super(configData);
            
            this.enabled = parseBoolean("enabled").orElse(SponsorBlockConfig.DEFAULT_ENABLED);
            this.forceGlobally = parseBoolean("forceGlobally").orElse(SponsorBlockConfig.DEFAULT_FORCE_GLOBALLY);
            this.overrideGlobal = parseBoolean("overrideGlobal").orElse(SponsorBlockConfig.DEFAULT_OVERRIDE_GLOBAL);
            this.skipAll = parseBoolean("skipAll").orElse(SponsorBlockConfig.DEFAULT_SKIP_ALL);
            this.skipSponsor = parseBoolean("skipSponsor").orElse(SponsorBlockConfig.DEFAULT_SKIP_SPONSOR);
            this.skipIntro = parseBoolean("skipIntro").orElse(SponsorBlockConfig.DEFAULT_SKIP_INTRO);
            this.skipOutro = parseBoolean("skipOutro").orElse(SponsorBlockConfig.DEFAULT_SKIP_OUTRO);
            this.skipSelfPromo = parseBoolean("skipSelfPromo").orElse(SponsorBlockConfig.DEFAULT_SKIP_SELF_PROMO);
            this.skipPreview = parseBoolean("skipPreview").orElse(SponsorBlockConfig.DEFAULT_SKIP_PREVIEW);
            this.skipInteraction = parseBoolean("skipInteraction").orElse(SponsorBlockConfig.DEFAULT_SKIP_INTERACTION);
            this.skipMusicOffTopic = parseBoolean("skipMusicOffTopic").orElse(SponsorBlockConfig.DEFAULT_SKIP_MUSIC_OFF_TOPIC);
        }
        
        /**
         * Creates an empty SponsorBlock Config.
         */
        public SponsorBlockConfig() {
            super();
        }
        
        
        //Methods
        
        /**
         * Returns whether this SponsorBlock Config is active and has enabled categories.
         *
         * @return Whether this SponsorBlock Config is active and has enabled categories.
         */
        public boolean isActive() {
            return isEnabled() && (isSkipAll() || isSkipSponsor() || isSkipIntro() || isSkipOutro() ||
                    isSkipSelfPromo() || isSkipPreview() || isSkipInteraction() || isSkipMusicOffTopic());
        }
        
        /**
         * Returns the list of categories enabled in the SponsorBlock Config.
         *
         * @return The list of categories enabled in the SponsorBlock Config.
         */
        public List<String> getCategories() {
            return isSkipAll() ? List.of(Category.ALL.getKey()) :
                   Stream.of(
                           (isSkipSponsor() ? Category.SPONSOR : null),
                           (isSkipIntro() ? Category.INTRO : null),
                           (isSkipOutro() ? Category.OUTRO : null),
                           (isSkipSelfPromo() ? Category.SELF_PROMO : null),
                           (isSkipPreview() ? Category.PREVIEW : null),
                           (isSkipInteraction() ? Category.INTERACTION : null),
                           (isSkipMusicOffTopic() ? Category.MUSIC_OFF_TOPIC : null)
                   ).filter(Objects::nonNull).map(Category::getKey).collect(Collectors.toList());
        }
        
        
        //Getters
        
        /**
         * Returns the type of the SponsorBlock Config.
         *
         * @return The type of the SponsorBlock Config.
         */
        public Type getType() {
            return type;
        }
        
        /**
         * Returns whether SponsorBlock is enabled.
         *
         * @return Whether SponsorBlock is enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }
        
        /**
         * Returns whether this global SponsorBlock configuration should be applied globally and take precedence over standard Channel SponsorBlock configurations.
         *
         * @return Whether this global SponsorBlock configuration should be applied globally and take precedence over standard Channel SponsorBlock configurations.
         */
        public boolean isForceGlobally() {
            return forceGlobally;
        }
        
        /**
         * Returns whether this Channel SponsorBlock configuration should override a forced Global SponsorBlock configuration.
         *
         * @return Whether this Channel SponsorBlock configuration should override a forced Global SponsorBlock configuration.
         */
        public boolean isOverrideGlobal() {
            return overrideGlobal;
        }
        
        /**
         * Returns whether SponsorBlock should skip all segments.
         *
         * @return Whether SponsorBlock should skip all segments.
         */
        public boolean isSkipAll() {
            return skipAll;
        }
        
        /**
         * Returns whether SponsorBlock should skip 'sponsor' segments.
         *
         * @return Whether SponsorBlock should skip 'sponsor' segments.
         */
        public boolean isSkipSponsor() {
            return skipSponsor;
        }
        
        /**
         * Returns whether SponsorBlock should skip 'intro' segments.
         *
         * @return Whether SponsorBlock should skip 'intro' segments.
         */
        public boolean isSkipIntro() {
            return skipIntro;
        }
        
        /**
         * Returns whether SponsorBlock should skip 'outro' segments.
         *
         * @return Whether SponsorBlock should skip 'outro' segments.
         */
        public boolean isSkipOutro() {
            return skipOutro;
        }
        
        /**
         * Returns whether SponsorBlock should skip 'self promo' segments.
         *
         * @return Whether SponsorBlock should skip 'self promo' segments.
         */
        public boolean isSkipSelfPromo() {
            return skipSelfPromo;
        }
        
        /**
         * Returns whether SponsorBlock should skip 'preview' segments.
         *
         * @return Whether SponsorBlock should skip 'preview' segments.
         */
        public boolean isSkipPreview() {
            return skipPreview;
        }
        
        /**
         * Returns whether SponsorBlock should skip 'interaction' segments.
         *
         * @return Whether SponsorBlock should skip 'interaction' segments.
         */
        public boolean isSkipInteraction() {
            return skipInteraction;
        }
        
        /**
         * Returns whether SponsorBlock should skip 'music off topic' segments.
         *
         * @return Whether SponsorBlock should skip 'music off topic' segments.
         */
        public boolean isSkipMusicOffTopic() {
            return skipMusicOffTopic;
        }
        
    }
    
}
