/*
 * File:    FileLogFilter.java
 * Package: youtube.state.log
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import youtube.util.LogUtils;

/**
 * Filters file logging to the main log file.
 */
public class FileLogFilter extends Filter<ILoggingEvent> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileLogFilter.class);
    
    
    //Methods
    
    /**
     * Decides whether to allow an event through the filter or not.
     *
     * @param event The event to filter.
     * @return The filter reply to the event.
     */
    @Override
    public FilterReply decide(ILoggingEvent event) {
        return (LogUtils.Config.allowFileLogging && LogUtils.Config.writeMainLog) ?
               FilterReply.NEUTRAL : FilterReply.DENY;
    }
    
}
