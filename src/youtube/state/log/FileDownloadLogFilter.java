/*
 * File:    FileDownloadLogFilter.java
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
 * Filters file logging to the download log file.
 */
public class FileDownloadLogFilter extends Filter<ILoggingEvent> {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileDownloadLogFilter.class);
    
    
    //Methods
    
    /**
     * Decides whether to allow an event through the filter or not.
     *
     * @param event The event to filter.
     * @return The filter reply to the event.
     */
    @Override
    public FilterReply decide(ILoggingEvent event) {
        return (LogUtils.Config.allowFileLogging && LogUtils.Config.writeDownloadLog) ?
               FilterReply.NEUTRAL : FilterReply.DENY;
    }
    
}
