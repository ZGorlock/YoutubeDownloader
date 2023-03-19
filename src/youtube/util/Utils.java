/*
 * File:    Utils.java
 * Package: youtube.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.config.Color;
import youtube.config.Configurator;

/**
 * Provides utility methods for the Youtube Downloader.
 */
public final class Utils {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    
    
    //Constants
    
    /**
     * The title of the project.
     */
    public static final String PROJECT_TITLE = "YoutubeDownloader";
    
    
    //Static Methods
    
    /**
     * Performs startup operations.
     *
     * @param program The current active program.
     * @return Whether startup was successful or not.
     */
    public static boolean startup(Configurator.Program program) {
        logger.debug(Color.log("Initializing..."));
        logger.trace(LogUtils.NEWLINE);
        
        Configurator.loadSettings(program);
        LogUtils.initLogging();
        Color.initColors();
        FileUtils.initFilesystem();
        
        return WebUtils.checkInternet() && ExecutableUtils.checkExe();
    }
    
}
