/*
 * File:    SponsorBlocker.java
 * Package: youtube.util
 * Author:  Zachary Gill
 */

package youtube.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;

/**
 * Handles configurations and commands for using SponsorBlock with yt-dlp.
 */
public class SponsorBlocker {
    
    //Static Fields
    
    /**
     * The global SponsorBlock configuration.
     */
    public static SponsorBlockConfig globalConfig = null;
    
    /**
     * A flag indicating whether the global SponsorBlock configuration has been loaded yet or not.
     */
    private static final AtomicBoolean loaded = new AtomicBoolean(false);
    
    
    //Functions
    
    /**
     * Returns the SponsorBlock command for a SponsorBlock configuration.
     *
     * @param config The SponsorBlock configuration.
     * @return SponsorBlock command, or an empty string if the selected Executable is not yt-dlp.
     */
    public static String getCommand(SponsorBlockConfig config) {
        boolean configValid = (config != null) && config.enabled;
        boolean globalConfigValid = (globalConfig != null) && globalConfig.enabled;
        if ((ExecutableUtils.EXECUTABLE != ExecutableUtils.Executable.YT_DLP) ||
                (!configValid && !globalConfigValid)) {
            return "";
        }
        
        SponsorBlockConfig conf = config;
        if (globalConfigValid) {
            if (!configValid) {
                conf = globalConfig;
            } else {
                if (globalConfig.forceGlobally) {
                    conf = globalConfig;
                    if (config.overrideGlobal) {
                        conf = config;
                    }
                }
            }
        }
        
        if (!conf.isActive()) {
            return "";
        }
        
        List<String> categories = new ArrayList<>();
        if (conf.skipAll) {
            categories.add("all");
        } else {
            categories.add(conf.skipSponsor ? "sponsor" : "");
            categories.add(conf.skipIntro ? "intro" : "");
            categories.add(conf.skipOutro ? "outro" : "");
            categories.add(conf.skipSelfPromo ? "selfpromo" : "");
            categories.add(conf.skipInteraction ? "interaction" : "");
            categories.add(conf.skipPreview ? "preview" : "");
            categories.add(conf.skipMusicOffTopic ? "music_offtopic" : "");
        }
        
        return categories.stream().filter(e -> !e.isEmpty())
                .collect(Collectors.joining(",", "--sponsorblock-remove ", " "));
    }
    
    /**
     * Loads a SponsorBlock configuration from a JSON settings configuration.
     *
     * @param conf The JSON settings configuration.
     * @return The SponsorBlock config, or null if the settings configuration does not contain SponsorBlock settings.
     */
    @SuppressWarnings("unchecked")
    public static SponsorBlockConfig loadConfig(JSONObject conf) {
        SponsorBlockConfig config = new SponsorBlockConfig();
        
        config.enabled = (conf.containsKey("enabled") && (boolean) conf.getOrDefault("enabled", true));
        config.forceGlobally = (conf.containsKey("forceGlobally") && (boolean) conf.getOrDefault("forceGlobally", false));
        config.overrideGlobal = (conf.containsKey("overrideGlobal") && (boolean) conf.getOrDefault("overrideGlobal", false));
        config.skipAll = (conf.containsKey("skipAll") && (boolean) conf.getOrDefault("skipAll", false));
        config.skipSponsor = (conf.containsKey("skipSponsor") && (boolean) conf.getOrDefault("skipSponsor", false));
        config.skipIntro = (conf.containsKey("skipIntro") && (boolean) conf.getOrDefault("skipIntro", false));
        config.skipOutro = (conf.containsKey("skipOutro") && (boolean) conf.getOrDefault("skipOutro", false));
        config.skipSelfPromo = (conf.containsKey("skipSelfPromo") && (boolean) conf.getOrDefault("skipSelfPromo", false));
        config.skipInteraction = (conf.containsKey("skipInteraction") && (boolean) conf.getOrDefault("skipInteraction", false));
        config.skipPreview = (conf.containsKey("skipPreview") && (boolean) conf.getOrDefault("skipPreview", false));
        config.skipMusicOffTopic = (conf.containsKey("skipMusicOffTopic") && (boolean) conf.getOrDefault("skipMusicOffTopic", false));
        
        return config;
    }
    
    /**
     * Loads the global SponsorBlock configuration from a JSON settings configuration.
     *
     * @param conf The JSON settings configuration.
     */
    public static void loadGlobalConfig(JSONObject conf) {
        if (loaded.compareAndSet(false, true)) {
            globalConfig = loadConfig(conf);
            globalConfig.type = SponsorBlockConfig.Type.GLOBAL;
        }
    }
    
    
    //Inner Classes
    
    /**
     * Holds a configuration for SponsorBlock.
     */
    public static class SponsorBlockConfig {
        
        //Enums
        
        /**
         * An enumeration of SponsorBlock configuration types.
         */
        public enum Type {
            GLOBAL,
            CHANNEL
        }
        
        //Fields
        
        /**
         * The type of the SponsorBlock config.
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
         * A flag indicating whether or not SponsorBlock should skip sponsor segments.
         */
        public boolean skipSponsor;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip intro segments.
         */
        public boolean skipIntro;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip outro segments.
         */
        public boolean skipOutro;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip self promo segments.
         */
        public boolean skipSelfPromo;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip interaction segments.
         */
        public boolean skipInteraction;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip preview segments.
         */
        public boolean skipPreview;
        
        /**
         * A flag indicating whether or not SponsorBlock should skip music off topic segments.
         */
        public boolean skipMusicOffTopic;
        
        
        //Methods
        
        /**
         * Returns whether this SponsorBlock Config is active and has enabled categories.
         *
         * @return Whether this SponsorBlock Config is active and has enabled categories.
         */
        public boolean isActive() {
            return enabled && (skipAll || skipSponsor || skipIntro || skipOutro ||
                    skipSelfPromo || skipInteraction || skipPreview || skipMusicOffTopic);
        }
        
    }
    
}
