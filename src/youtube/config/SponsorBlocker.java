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
        final boolean configValid = (config != null) && config.enabled;
        final boolean globalConfigValid = (globalConfig != null) && globalConfig.enabled;
        
        if ((!configValid && !globalConfigValid) ||
                (ExecutableUtils.EXECUTABLE != ExecutableUtils.Executable.YT_DLP)) {
            return "";
        }
        
        final boolean useGlobalConfig = globalConfigValid && (!configValid || (globalConfig.forceGlobally && !config.overrideGlobal));
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
        
        
        //Fields
        
        /**
         * The type of the SponsorBlock Config.
         */
        public Type type;
        
        /**
         * A flag indicating whether or not SponsorBlock is enabled.
         */
        public boolean enabled;
        
        /**
         * A flag indicating whether or not this global SponsorBlock configuration should be applied globally and take precedence over standard Channel SponsorBlock configurations.
         */
        public boolean forceGlobally;
        
        /**
         * A flag indicating whether or not this Channel SponsorBlock configuration should override a forced Global SponsorBlock configuration.
         */
        public boolean overrideGlobal;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip all segments.
         */
        public boolean skipAll;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip 'sponsor' segments.
         */
        public boolean skipSponsor;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip 'intro' segments.
         */
        public boolean skipIntro;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip 'outro' segments.
         */
        public boolean skipOutro;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip 'self promo' segments.
         */
        public boolean skipSelfPromo;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip 'preview' segments.
         */
        public boolean skipPreview;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip 'interaction' segments.
         */
        public boolean skipInteraction;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip 'music off topic' segments.
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
            return enabled && (skipAll || skipSponsor || skipIntro || skipOutro ||
                    skipSelfPromo || skipPreview || skipInteraction || skipMusicOffTopic);
        }
        
        /**
         * Returns the list of categories enabled in the SponsorBlock Config.
         *
         * @return The list of categories enabled in the SponsorBlock Config.
         */
        public List<String> getCategories() {
            return skipAll ? List.of("all") :
                   Stream.of(
                           (skipSponsor ? "sponsor" : null),
                           (skipIntro ? "intro" : null),
                           (skipOutro ? "outro" : null),
                           (skipSelfPromo ? "selfpromo" : null),
                           (skipPreview ? "preview" : null),
                           (skipInteraction ? "interaction" : null),
                           (skipMusicOffTopic ? "music_offtopic" : null)
                   ).filter(Objects::nonNull).collect(Collectors.toList());
            
        }
        
    }
    
}
