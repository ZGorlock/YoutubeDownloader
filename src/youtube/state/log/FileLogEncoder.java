/*
 * File:    FileLogEncoder.java
 * Package: youtube.state.log
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.state.log;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Pattern;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encodes file logging.
 */
public class FileLogEncoder extends PatternLayoutEncoder {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileLogEncoder.class);
    
    
    //Constants
    
    /**
     * The default log pattern.
     */
    public static final String DEFAULT_PATTERN = "%d{HH:mm:ss.SSS} %-5level %32logger{32} - %message%n";
    
    /**
     * A regex pattern matching one or more leading indentations.
     */
    public static final Pattern LEADING_INDENT_PATTERN = Pattern.compile("^(?:\u001B\\[\\d+m +\u001B\\[0m)+");
    
    
    //Fields
    
    /**
     * The log pattern encoder.
     */
    private final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    
    
    //Methods
    
    /**
     * Starts the encoder.
     */
    @Override
    public void start() {
        encoder.setContext(context);
        encoder.setPattern(getPattern());
        encoder.start();
        
        super.start();
    }
    
    /**
     * Encodes an event.
     *
     * @param event The event to encode.
     */
    @Override
    public byte[] encode(ILoggingEvent event) {
        try {
            Field field = LoggingEvent.class.getDeclaredField("formattedMessage");
            field.setAccessible(true);
            field.set(event, Optional.ofNullable(event)
                    .map(ILoggingEvent::getFormattedMessage)
                    .map(e -> e.replaceAll(LEADING_INDENT_PATTERN.pattern(), ""))
                    .map(e -> e.replace(" ", " "))
                    .map(StringUtility::removeConsoleEscapeCharacters)
                    .orElse(""));
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        
        return encoder.encode(event);
    }
    
    
    //Getters
    
    /**
     * Returns the log pattern.
     *
     * @return The log pattern
     */
    @Override
    public String getPattern() {
        return Optional.ofNullable(super.getPattern()).orElse(DEFAULT_PATTERN);
    }
    
}
