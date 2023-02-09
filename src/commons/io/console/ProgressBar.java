/*
 * File:    ProgressBar.java
 * Package: commons.io.console
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.io.console;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import commons.math.number.BoundUtility;
import commons.object.string.StringUtility;
import commons.time.DateTimeUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A progress bar for the console.
 */
public class ProgressBar {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProgressBar.class);
    
    
    //Constants
    
    /**
     * The default width of the progress bar in characters.
     */
    public static final int DEFAULT_PROGRESS_BAR_WIDTH = 32;
    
    /**
     * The default value of the flag indicating whether to automatically print the progress bar after an update.
     */
    public static final boolean DEFAULT_AUTO_PRINT = true;
    
    /**
     * The minimum number of milliseconds that must pass before an update can occur.
     */
    public static final long MINIMUM_UPDATE_DELAY = 200;
    
    /**
     * The number of previous updates to use for calculating the rolling average speed.
     */
    public static final int ROLLING_AVERAGE_UPDATE_COUNT = 5;
    
    /**
     * The default value of the flag to show the percentage in the progress bar.
     */
    public static final boolean DEFAULT_SHOW_PERCENTAGE = true;
    
    /**
     * The default value of the flag to show the bar in the progress bar.
     */
    public static final boolean DEFAULT_SHOW_BAR = true;
    
    /**
     * The default value of the flag to show the ratio in the progress bar.
     */
    public static final boolean DEFAULT_SHOW_RATIO = true;
    
    /**
     * The default value of the flag to show the speed in the progress bar.
     */
    public static final boolean DEFAULT_SHOW_SPEED = true;
    
    /**
     * The default value of the flag to show the time remaining in the progress bar.
     */
    public static final boolean DEFAULT_SHOW_TIME_REMAINING = true;
    
    /**
     * The default value of the flag to comma-separate the numbers in the progress bar.
     */
    public static final boolean DEFAULT_USE_COMMAS = true;
    
    /**
     * The default color for the base text of the progress bar.
     */
    public static final Console.ConsoleEffect DEFAULT_COLOR_BASE = Console.ConsoleEffect.GREEN;
    
    /**
     * The default color for the "good" text of the progress bar.
     */
    public static final Console.ConsoleEffect DEFAULT_COLOR_GOOD = Console.ConsoleEffect.CYAN;
    
    /**
     * The default color for the "bad" text of the progress bar.
     */
    public static final Console.ConsoleEffect DEFAULT_COLOR_BAD = Console.ConsoleEffect.RED;
    
    /**
     * The decimal format to use when printing comma-separated integers.
     */
    public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");
    
    /**
     * The decimal format to use when printing comma-separated decimals.
     */
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.0");
    
    /**
     * A string printed at the end of the progress bar to help it display in some terminals.
     */
    public static final String ENDCAP = Console.ConsoleEffect.BLACK.apply(" ");
    
    
    //Fields
    
    /**
     * The title to display for the progress bar.
     */
    private String title;
    
    /**
     * The total progress of the progress bar.
     */
    private long total;
    
    /**
     * The current progress of the progress bar.
     */
    private long progress = 0L;
    
    /**
     * The currently completed progress of the progress bar.
     */
    private long current = 0L;
    
    /**
     * The completed progress of the progress bar at the time of the last update.
     */
    private long previous = 0L;
    
    /**
     * The initial progress of the progress bar.
     */
    private long initialProgress = 0L;
    
    /**
     * The last couple progress values of the progress bar for calculating the rolling average speed.
     */
    private final List<Long> rollingProgress = new ArrayList<>();
    
    /**
     * The initial duration of the progress bar in seconds.
     */
    private long initialDuration = 0L;
    
    /**
     * The time of the current update of the progress bar.
     */
    private long currentUpdate = 0L;
    
    /**
     * The time of the previous update of the progress bar.
     */
    private long previousUpdate = 0L;
    
    /**
     * The time the progress bar was updated for the first time.
     */
    private long firstUpdate = 0L;
    
    /**
     * The last couple times the progress bar was updated for calculating the rolling average speed.
     */
    private final List<Long> rollingUpdate = new ArrayList<>();
    
    /**
     * The width of the bar in the progress bar.
     */
    private int width;
    
    /**
     * The units of the progress bar.
     */
    private String units;
    
    /**
     * A flag indicating whether to automatically print the progress bar after an update.
     */
    private boolean autoPrint;
    
    /**
     * A flag indicating whether to comma-separate the current and total progress in the progress bar.
     */
    private boolean useCommas = DEFAULT_USE_COMMAS;
    
    /**
     * The indent size of the bar in the progress bar.
     */
    private int indent = 0;
    
    /**
     * A flag indicating whether the progress bar has not been printed yet.
     */
    private final AtomicBoolean firstPrint = new AtomicBoolean(true);
    
    /**
     * A flag indicating whether to show the percentage in the progress bar.
     */
    private boolean showPercentage = DEFAULT_SHOW_PERCENTAGE;
    
    /**
     * A flag indicating whether to show the bar in the progress bar.
     */
    private boolean showBar = DEFAULT_SHOW_BAR;
    
    /**
     * A flag indicating whether to show the ratio in the progress bar.
     */
    private boolean showRatio = DEFAULT_SHOW_RATIO;
    
    /**
     * A flag indicating whether to show the speed in the progress bar.
     */
    private boolean showSpeed = DEFAULT_SHOW_SPEED;
    
    /**
     * A flag indicating whether to show the time remaining in the progress bar.
     */
    private boolean showTimeRemaining = DEFAULT_SHOW_TIME_REMAINING;
    
    /**
     * The color for the base text of the progress bar.
     */
    private Console.ConsoleEffect baseColor = DEFAULT_COLOR_BASE;
    
    /**
     * The color for the "good" text of the progress bar.
     */
    private Console.ConsoleEffect goodColor = DEFAULT_COLOR_GOOD;
    
    /**
     * The color for the "bad" text of the progress bar.
     */
    private Console.ConsoleEffect badColor = DEFAULT_COLOR_BAD;
    
    /**
     * The current progress bar.
     */
    private String progressBar = "";
    
    /**
     * A flag indicating whether there was an update to the progress bar.
     */
    private final AtomicBoolean update = new AtomicBoolean(false);
    
    /**
     * A flag indicating whether the progress bar has completed.
     */
    private final AtomicBoolean completed = new AtomicBoolean(false);
    
    /**
     * A flag indicating whether the progress bar has failed.
     */
    private final AtomicBoolean failed = new AtomicBoolean(false);
    
    
    //Constructors
    
    /**
     * Creates a new ProgressBar object.
     *
     * @param title     The title to display for the progress bar.
     * @param total     The total size of the progress bar.
     * @param width     The with of the bar in the progress bar.
     * @param units     The units of the progress bar.
     * @param autoPrint Whether to automatically print the progress bar after an update.
     */
    public ProgressBar(String title, long total, int width, String units, boolean autoPrint) {
        this.title = title;
        this.total = total;
        this.width = width;
        this.units = units;
        this.autoPrint = autoPrint;
    }
    
    /**
     * Creates a new ProgressBar object.
     *
     * @param title The title to display for the progress bar.
     * @param total The total size of the progress bar.
     * @param width The with of the bar in the progress bar.
     * @param units The units of the progress bar.
     * @see #ProgressBar(String, long, int, String, boolean)
     */
    public ProgressBar(String title, long total, int width, String units) {
        this(title, total, width, units, DEFAULT_AUTO_PRINT);
    }
    
    /**
     * Creates a new ProgressBar object.
     *
     * @param title The title to display for the progress bar.
     * @param total The total size of the progress bar.
     * @param units The units of the progress bar.
     * @see #ProgressBar(String, long, int, String)
     */
    public ProgressBar(String title, long total, String units) {
        this(title, total, DEFAULT_PROGRESS_BAR_WIDTH, units);
    }
    
    /**
     * Creates a new ProgressBar object.
     *
     * @param title The title to display for the progress bar.
     * @param total The total size of the progress bar.
     * @param width The with of the bar in the progress bar.
     * @see #ProgressBar(String, long, int, String)
     */
    public ProgressBar(String title, long total, int width) {
        this(title, total, width, "");
    }
    
    /**
     * Creates a new ProgressBar object.
     *
     * @param title The title to display for the progress bar.
     * @param total The total size of the progress bar.
     * @see #ProgressBar(String, long, int, String)
     */
    public ProgressBar(String title, long total) {
        this(title, total, DEFAULT_PROGRESS_BAR_WIDTH, "");
    }
    
    
    //Methods
    
    /**
     * Builds the progress bar.
     *
     * @return The progress bar.
     * @see #buildPercentageString()
     * @see #buildBarString()
     * @see #buildRatioString()
     * @see #buildSpeedString()
     * @see #buildTimeRemainingString()
     */
    public String get() {
        return !update.get() ? progressBar :
               (progressBar = Stream.of(
                               (showPercentage ? buildPercentageString() : ""),
                               (showBar ? buildBarString() : ""),
                               (showRatio ? buildRatioString() : ""),
                               ((showSpeed && !isDone()) ? buildSpeedString() : ""),
                               (showTimeRemaining ? ("- " + buildTimeRemainingString()) : ""))
                       .filter(e -> !e.isEmpty())
                       .collect(Collectors.joining(" "))
                       .replaceAll("^(?:-\\s+)?", StringUtility.spaces(indent)));
    }
    
    /**
     * Builds the printable progress bar.<br>
     * This must be displayed with print(), not println().
     *
     * @return The printable progress bar.
     * @see #get()
     */
    public String getPrintable() {
        final int oldLength = StringUtility.removeConsoleEscapeCharacters(progressBar).length();
        final String bar = get();
        final int newLength = StringUtility.removeConsoleEscapeCharacters(bar).length();
        
        return '\r' + bar + ' ' +
                StringUtility.spaces(oldLength - newLength - 1) + ENDCAP;
    }
    
    /**
     * Updates the progress bar.<br>
     * If the time since the last update is less than the minimum update delay then the update will not take place until called again after the delay.
     *
     * @param newProgress The new progress of the progress bar.
     * @param autoPrint   Whether to automatically print the progress bar after an update.
     * @return Whether the progress bar was updated.
     */
    private synchronized boolean update(long newProgress, boolean autoPrint) {
        if (progressComplete() || isFailed()) {
            return false;
        }
        
        firstUpdate = (firstUpdate == 0) ? System.nanoTime() : firstUpdate;
        progress = BoundUtility.truncate(newProgress, 0L, total);
        
        final boolean needsUpdate = (Math.abs(System.nanoTime() - currentUpdate) >= TimeUnit.MILLISECONDS.toNanos(MINIMUM_UPDATE_DELAY));
        if (needsUpdate || (progress == total)) {
            previous = current;
            current = progress;
            
            previousUpdate = currentUpdate;
            currentUpdate = System.nanoTime();
            
            rollingProgress.add(current);
            rollingUpdate.add(currentUpdate);
            if (rollingProgress.size() > ROLLING_AVERAGE_UPDATE_COUNT) {
                rollingProgress.remove(0);
            }
            if (rollingUpdate.size() > ROLLING_AVERAGE_UPDATE_COUNT) {
                rollingUpdate.remove(0);
            }
            
            update.set(true);
        }
        
        if (update.get() && autoPrint) {
            print();
            return true;
        }
        return update.get();
    }
    
    /**
     * Updates the progress bar.<br>
     * If the time since the last update is less than the minimum update delay then the update will not take place until called again after the delay.
     *
     * @param newProgress The new progress of the progress bar.
     * @return Whether the progress bar was updated.
     * @see #update(long, boolean)
     */
    public synchronized boolean update(long newProgress) {
        return update(newProgress, autoPrint);
    }
    
    /**
     * Adds one to the current progress.
     *
     * @return Whether the progress bar was updated.
     * @see #update(long)
     */
    public synchronized boolean addOne() {
        return update(progress + 1);
    }
    
    /**
     * Processes the log data passed into it and updates the progress bar accordingly.<br>
     * It is expected that this method be overridden in subclasses for specific use cases.
     *
     * @param log     The log data.
     * @param isError Whether the passed log is an error log.
     * @return Whether the progress bar was updated.
     */
    public synchronized boolean processLog(String log, boolean isError) {
        return false;
    }
    
    /**
     * Processes the log data passed into it and updates the progress bar accordingly.<br>
     * It is expected that this method be overridden in subclasses for specific use cases.
     *
     * @param log The log data.
     * @return Whether the progress bar was updated.
     * @see #processLog(String, boolean)
     */
    public synchronized boolean processLog(String log) {
        return processLog(log, false);
    }
    
    /**
     * Prints the progress bar to the console.
     *
     * @param extras  Any extras to print at the end of the progress bar.
     * @param newLine Whether to move to a new line after printing.
     * @see #getPrintable()
     */
    private synchronized void print(String extras, boolean newLine) {
        final String bar = getPrintable()
                .replace(" ", (StringUtility.isNullOrBlank(extras) ? " " : extras)) +
                (newLine ? System.lineSeparator() : "");
        
        if (firstPrint.get() && !title.isEmpty()) {
            System.out.println(buildTitleString());
        }
        System.out.print(bar);
        System.out.flush();
        System.err.flush();
        
        firstPrint.set(false);
        update.set(false);
    }
    
    /**
     * Prints the progress bar to the console.
     *
     * @see #print(String, boolean)
     */
    public synchronized void print() {
        print(null, false);
    }
    
    /**
     * Prints the completed progress bar to the console.
     *
     * @param printTime      Whether to print the final time after the progress bar.
     * @param additionalInfo Additional info to print at the end of the progress bar.
     * @see #update(long, boolean)
     * @see #print(String, boolean)
     */
    private synchronized void finish(boolean printTime, String additionalInfo) {
        final long duration = getTotalDuration();
        final String durationStamp = DateTimeUtility.durationToDurationString(duration, false, false, true);
        final String extras = String.join("",
                (!printTime ? "" : (" (" + durationStamp + ')')),
                (additionalInfo.isEmpty() ? "" : (" - " + additionalInfo)));
        
        update.set(true);
        print(extras, true);
    }
    
    /**
     * Calculates the ratio of the progress bar.
     *
     * @return The ratio of the progress bar.
     */
    public synchronized double getRatio() {
        return ((total <= 0) || (current >= total)) ? 1 :
               (current < 0) ? 0 :
               ((double) current / total);
    }
    
    /**
     * Calculates the percentage of the progress bar.
     *
     * @return The percentage of the progress bar.
     * @see #getRatio()
     */
    public synchronized int getPercentage() {
        return (int) (getRatio() * 100);
    }
    
    /**
     * Calculates the last recorded speed of the progress bar.
     *
     * @return The last recorded speed of the progress bar in units per second.
     */
    public synchronized double getLastSpeed() {
        if ((current < 0) || (previous < 0) || (previousUpdate <= 0) || (currentUpdate <= 0)) {
            return 0.0;
        }
        
        final double recentTime = (double) Math.max((currentUpdate - previousUpdate), 0) / TimeUnit.SECONDS.toNanos(1);
        final long recentProgress = Math.max((current - previous), 0);
        
        return ((recentTime == 0) || (recentProgress == 0)) ? 0.0 :
               (recentProgress / recentTime);
    }
    
    /**
     * Calculates the average speed of the progress bar.
     *
     * @return The average speed of the progress bar in units per second.
     */
    public synchronized double getAverageSpeed() {
        if ((current <= 0) || (firstUpdate < 0) || (currentUpdate <= 0)) {
            return 0.0;
        }
        
        final double totalTime = (double) Math.max((currentUpdate - firstUpdate), 0) / TimeUnit.SECONDS.toNanos(1);
        
        return (totalTime == 0) ? 0 :
               (current / totalTime);
    }
    
    /**
     * Calculates the rolling average speed of the progress bar for the last 5 updates.
     *
     * @return The rolling average speed of the progress bar in units per second.
     */
    public synchronized double getRollingAverageSpeed() {
        if ((rollingProgress.size() != ROLLING_AVERAGE_UPDATE_COUNT) || (rollingUpdate.size() != ROLLING_AVERAGE_UPDATE_COUNT)) {
            return 0.0;
        }
        
        final double windowTime = (double) Math.max((rollingUpdate.get(ROLLING_AVERAGE_UPDATE_COUNT - 1) - rollingUpdate.get(0)), 0) / TimeUnit.SECONDS.toNanos(1);
        final long windowProgress = Math.max((rollingProgress.get(ROLLING_AVERAGE_UPDATE_COUNT - 1) - rollingProgress.get(0)), 0);
        
        return ((windowTime == 0) || (windowProgress == 0)) ? 0.0 :
               (windowProgress / windowTime);
    }
    
    /**
     * Calculates the total duration of the progress bar.
     *
     * @param nanos Whether to return the total duration in nanoseconds; otherwise in milliseconds.
     * @return The total duration of the progress bar.
     */
    public synchronized long getTotalDuration(boolean nanos) {
        if ((currentUpdate <= 0) || (firstUpdate < 0)) {
            return 0L;
        }
        
        final long totalTime = Math.max((currentUpdate - firstUpdate), 0);
        final long initialTime = (Math.max(initialDuration, 0) * TimeUnit.SECONDS.toNanos(1));
        final long totalDuration = (totalTime + initialTime);
        
        return nanos ? totalDuration : TimeUnit.NANOSECONDS.toMillis(totalDuration);
    }
    
    /**
     * Calculates the total duration of the progress bar.
     *
     * @return The total duration of the progress bar in milliseconds.
     * @see #getTotalDuration(boolean)
     */
    public synchronized long getTotalDuration() {
        return getTotalDuration(false);
    }
    
    /**
     * Estimates the estimated time remaining of the progress bar.
     *
     * @param nanos Whether to return the time remaining in nanoseconds; otherwise in milliseconds.
     * @return The estimated time remaining of the progress bar.
     */
    public synchronized long getTimeRemaining(boolean nanos) {
        if ((current <= 0) || (currentUpdate <= 0) || (firstUpdate < 0)) {
            return Long.MAX_VALUE;
        }
        
        final long remainingProgress = Math.max((total - current), 0);
        final long totalProgress = Math.max((current - Math.max(initialProgress, 0)), 0);
        final long totalTime = Math.max((currentUpdate - firstUpdate), 0);
        final long timeRemaining = ((totalProgress == 0) || (totalTime == 0)) ? Long.MAX_VALUE :
                                   (long) (((double) remainingProgress / totalProgress) * totalTime);
        
        return nanos ? timeRemaining : TimeUnit.NANOSECONDS.toMillis(timeRemaining);
    }
    
    /**
     * Estimates the estimated time remaining of the progress bar.
     *
     * @return The estimated time remaining of the progress bar in milliseconds.
     * @see #getTimeRemaining(boolean)
     */
    public synchronized long getTimeRemaining() {
        return getTimeRemaining(false);
    }
    
    /**
     * Returns the current status color of the progress bar.
     *
     * @return The current status color of the progress bar.
     */
    public synchronized Console.ConsoleEffect getStatusColor() {
        return isFailed() ? badColor :
               isCompleted() ? goodColor :
               baseColor;
    }
    
    /**
     * Determines if the progress bar is done.
     *
     * @return Whether the progress bar is done.
     */
    private synchronized boolean isDone() {
        return progressComplete() || isCompleted() || isFailed();
    }
    
    /**
     * Determines if the progress bar's progress is complete.
     *
     * @return Whether the progress bar's progress is complete.
     */
    public synchronized boolean progressComplete() {
        return (current >= total);
    }
    
    /**
     * Determines if the progress bar has been completed.
     *
     * @return Whether the progress bar has been completed.
     */
    public synchronized boolean isCompleted() {
        return completed.get();
    }
    
    /**
     * Completes the progress bar.
     *
     * @param printTime      Whether to print the final time after the progress bar.
     * @param additionalInfo Additional info to print at the end of the progress bar.
     * @see #finish(boolean, String)
     */
    public synchronized void complete(boolean printTime, String additionalInfo) {
        if (completed.getAndSet(true) || failed.get()) {
            return;
        }
        
        update(total, false);
        finish(printTime, additionalInfo);
    }
    
    /**
     * Completes the progress bar.
     *
     * @param printTime Whether to print the final time after the progress bar.
     * @see #complete(boolean, String)
     */
    public synchronized void complete(boolean printTime) {
        complete(printTime, "");
    }
    
    /**
     * Completes the progress bar.
     *
     * @see #complete(boolean)
     */
    public synchronized void complete() {
        complete(true);
    }
    
    /**
     * Determines if the progress bar has been failed.
     *
     * @return Whether the progress bar has been failed.
     */
    public synchronized boolean isFailed() {
        return failed.get();
    }
    
    /**
     * Fails the progress bar.
     *
     * @param printTime      Whether to print the final time after the progress bar.
     * @param additionalInfo Additional info to print at the end of the progress bar.
     * @see #finish(boolean, String)
     */
    public synchronized void fail(boolean printTime, String additionalInfo) {
        if (completed.getAndSet(true) || failed.getAndSet(true)) {
            return;
        }
        
        finish(printTime, additionalInfo);
    }
    
    /**
     * Fails the progress bar.
     *
     * @param printTime Whether to print the final time after the progress bar.
     * @see #fail(boolean, String)
     */
    public synchronized void fail(boolean printTime) {
        fail(printTime, "");
    }
    
    /**
     * Fails the progress bar.
     *
     * @see #fail(boolean)
     */
    public synchronized void fail() {
        fail(true);
    }
    
    /**
     * Builds the title string for the progress bar.
     *
     * @return The title string.
     * @see #getTitle()
     */
    public String buildTitleString() {
        return StringUtility.isNullOrBlank(title) ? "" :
               goodColor.apply(title + ": ");
    }
    
    /**
     * Builds the percentage string for the progress bar.
     *
     * @return The percentage string.
     * @see #getPercentage()
     */
    public String buildPercentageString() {
        final int percentage = getPercentage();
        final String percentageString = StringUtility.padLeft(String.valueOf(percentage), 3);
        
        return getStatusColor().apply(percentageString) + '%';
    }
    
    /**
     * Builds the progress bar string for the progress bar.
     *
     * @return The progress bar string.
     * @see #getRatio()
     */
    public String buildBarString() {
        final double ratio = getRatio();
        final int completed = Math.max((int) ((double) width * ratio), 0);
        final int remaining = Math.max((width - completed - 1), 0);
        final String barString = StringUtility.fillStringOfLength('=', completed) +
                (progressComplete() ? "" : (isDone() ? ' ' : '>')) +
                StringUtility.spaces(remaining);
        
        return '[' + getStatusColor().apply(barString) + ']';
    }
    
    /**
     * Builds the ratio string for the progress bar.
     *
     * @return The ratio string.
     */
    public String buildRatioString() {
        final long currentProgress = Math.max(Math.min(current, total), 0);
        final String currentProgressString = useCommas ? INTEGER_FORMAT.format(currentProgress) : String.valueOf(currentProgress);
        final String totalProgressString = useCommas ? INTEGER_FORMAT.format(total) : String.valueOf(total);
        final String ratioString = StringUtility.padLeft(currentProgressString, totalProgressString.length());
        
        return getStatusColor().apply(ratioString) + units + '/' +
                goodColor.apply(totalProgressString) + units;
    }
    
    /**
     * Builds the speed string for the progress bar.
     *
     * @return The speed string.
     */
    public String buildSpeedString() {
        final double rollingAverageSpeed = getRollingAverageSpeed();
        final String speedString = useCommas ? DECIMAL_FORMAT.format(rollingAverageSpeed) : String.format("%.1f", rollingAverageSpeed);
        
        return isDone() ? "" :
               ("at " + speedString + units + "/s");
    }
    
    /**
     * Builds the time remaining string for the progress bar.
     *
     * @return The time remaining string.
     * @see #getTimeRemaining()
     */
    public String buildTimeRemainingString() {
        final long timeRemaining = getTimeRemaining();
        final String timeRemainingString = DateTimeUtility.durationToDurationStamp(timeRemaining, false, false);
        
        return isFailed() ? badColor.apply("Failed") :
               isCompleted() ? goodColor.apply("Complete") :
               (timeRemaining == Long.MAX_VALUE) ? "ETA: --:--:--" :
               ("ETA: " + timeRemainingString);
    }
    
    
    //Getters
    
    /**
     * Returns the title of the progress bar.
     *
     * @return The title of the progress bar.
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Returns the total progress of the progress bar.
     *
     * @return The total progress of the progress bar.
     */
    public long getTotal() {
        return total;
    }
    
    /**
     * Returns the progress of the progress bar.
     *
     * @return The progress of the progress bar.
     */
    public long getProgress() {
        return progress;
    }
    
    /**
     * Returns the currently completed progress of the progress bar.
     *
     * @return The currently completed progress of the progress bar.
     */
    public long getCurrent() {
        return current;
    }
    
    /**
     * Returns the completed progress of the progress bar at the time of the last update.
     *
     * @return The completed progress of the progress bar at the time of the last update.
     */
    public long getPrevious() {
        return previous;
    }
    
    /**
     * Returns the initial progress of the progress bar.
     *
     * @return The initial progress of the progress bar.
     */
    public long getInitialProgress() {
        return initialProgress;
    }
    
    /**
     * Returns the initial duration of the progress bar in seconds.
     *
     * @return The initial duration of the progress bar in seconds.
     */
    public long getInitialDuration() {
        return initialDuration;
    }
    
    /**
     * Returns the time of the current update of the progress bar.
     *
     * @return The time of the current update of the progress bar.
     */
    public long getCurrentUpdate() {
        return currentUpdate;
    }
    
    /**
     * Returns the time of the previous update of the progress bar.
     *
     * @return The time of the previous update of the progress bar.
     */
    public long getPreviousUpdate() {
        return previousUpdate;
    }
    
    /**
     * Returns the time the progress bar was updated for the firstUpdate time.
     *
     * @return The time the progress bar was updated for the firstUpdate time.
     */
    public long getFirstUpdate() {
        return firstUpdate;
    }
    
    /**
     * Returns the width of the bar in the progress bar.
     *
     * @return The width of the bar in the progress bar.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Returns the units of the progress bar.
     *
     * @return The units of the progress bar.
     */
    public String getUnits() {
        return units;
    }
    
    /**
     * Returns the flag indicating whether to automatically print the progress bar after an update.
     *
     * @return The flag indicating whether to automatically print the progress bar after an update.
     */
    public boolean getAutoPrint() {
        return autoPrint;
    }
    
    /**
     * Returns the flag indicating whether to comma-separate the current and total progress in the progress bar.
     *
     * @return The flag indicating whether to comma-separate the current and total progress in the progress bar.
     */
    public boolean getUseCommas() {
        return useCommas;
    }
    
    /**
     * Returns the indent size of the progress bar.
     *
     * @return The indent size of the progress bar.
     */
    public int getIndent() {
        return indent;
    }
    
    /**
     * Returns the flag indicating whether to show the percentage in the progress bar.
     *
     * @return The flag indicating whether to show the percentage in the progress bar.
     */
    public boolean getShowPercentage() {
        return showPercentage;
    }
    
    /**
     * Returns the flag indicating whether to show the bar in the progress bar.
     *
     * @return The flag indicating whether to show the bar in the progress bar.
     */
    public boolean getShowBar() {
        return showBar;
    }
    
    /**
     * Returns the flag indicating whether to show the ratio in the progress bar.
     *
     * @return The flag indicating whether to show the ratio in the progress bar.
     */
    public boolean getShowRatio() {
        return showRatio;
    }
    
    /**
     * Returns the flag indicating whether to show the speed in the progress bar.
     *
     * @return The flag indicating whether to show the speed in the progress bar.
     */
    public boolean getShowSpeed() {
        return showSpeed;
    }
    
    /**
     * Returns the flag indicating whether to show the time remaining in the progress bar.
     *
     * @return The flag indicating whether to show the time remaining in the progress bar.
     */
    public boolean getShowTimeRemaining() {
        return showTimeRemaining;
    }
    
    /**
     * Returns the color for the base text of the progress bar.
     *
     * @return The color for the base text of the progress bar.
     */
    public Console.ConsoleEffect getBaseColor() {
        return baseColor;
    }
    
    /**
     * Returns the color for the "good" text of the progress bar.
     *
     * @return The color for the "good" text of the progress bar.
     */
    public Console.ConsoleEffect getGoodColor() {
        return goodColor;
    }
    
    /**
     * Returns the color for the "bad" text of the progress bar.
     *
     * @return The color for the "bad" text of the progress bar.
     */
    public Console.ConsoleEffect getBadColor() {
        return badColor;
    }
    
    
    //Setters
    
    /**
     * Updates the title of the progress bar.<br>
     * If the title has already been printed then the title will not be changed.
     *
     * @param title The new title of the progress bar.
     * @return Whether the title of the progress bar was updated.
     */
    public synchronized boolean updateTitle(String title) {
        if (firstPrint.get()) {
            this.title = Optional.ofNullable(title).orElse("");
            return true;
        }
        return false;
    }
    
    /**
     * Updates the total progress of the progress bar.
     *
     * @param total The new total progress of the progress bar.
     */
    public synchronized void updateTotal(long total) {
        this.total = total;
    }
    
    /**
     * Updates the units of the progress bar and scales the progress.
     *
     * @param units The new units of the progress bar.
     * @param scale The amount to scale the current and total progress by.
     */
    public synchronized void updateUnits(String units, double scale) {
        this.units = Optional.ofNullable(units).orElse("");
        
        this.total *= scale;
        this.progress *= scale;
        this.current *= scale;
        this.previous *= scale;
        this.initialProgress *= scale;
        IntStream.range(0, this.rollingProgress.size()).boxed().forEach(i ->
                this.rollingProgress.set(i, (long) (this.rollingProgress.get(i) * scale)));
    }
    
    /**
     * Updates the units of the progress bar.
     *
     * @param units The new units of the progress bar.
     * @see #updateUnits(String, double)
     */
    public synchronized void updateUnits(String units) {
        updateUnits(units, 1.0);
    }
    
    /**
     * Defines the initial progress of the progress bar.<br>
     * If the initial progress has already been defined the initial progress will not be changed.
     *
     * @param initialProgress The initial progress of the progress bar.
     * @return Whether the initial progress was defined.
     */
    public synchronized boolean defineInitialProgress(long initialProgress) {
        if (this.initialProgress == 0) {
            this.initialProgress = initialProgress;
            return true;
        }
        return false;
    }
    
    /**
     * Sets the initial duration of the progress bar in seconds.<br>
     * If the initial duration has already been defined the initial duration will not be changed.
     *
     * @param initialDuration The initial duration of the progress bar in seconds.
     * @return Whether the initial duration was defined.
     */
    public synchronized boolean defineInitialDuration(long initialDuration) {
        if (this.initialDuration == 0) {
            this.initialDuration = initialDuration;
            return true;
        }
        return false;
    }
    
    /**
     * Sets the flag indicating whether to automatically print the progress bar after an update.
     *
     * @param autoPrint The flag indicating whether to automatically print the progress bar after an update.
     */
    public void setAutoPrint(boolean autoPrint) {
        this.autoPrint = autoPrint;
    }
    
    /**
     * Sets the flag indicating whether to comma-separate the current and total progress in the progress bar.
     *
     * @param useCommas The flag indicating whether to comma-separate the current and total progress in the progress bar.
     */
    public void setUseCommas(boolean useCommas) {
        this.useCommas = useCommas;
    }
    
    /**
     * Sets the indent size of the progress bar.
     *
     * @param indent The indent size of the progress bar.
     */
    public void setIndent(int indent) {
        this.indent = indent;
    }
    
    /**
     * Sets the flag indicating whether to show the percentage in the progress bar.
     *
     * @param showPercentage The flag indicating whether to show the percentage in the progress bar.
     */
    public void setShowPercentage(boolean showPercentage) {
        this.showPercentage = showPercentage;
    }
    
    /**
     * Sets the flag indicating whether to show the bar in the progress bar.
     *
     * @param showBar The flag indicating whether to show the bar in the progress bar.
     */
    public void setShowBar(boolean showBar) {
        this.showBar = showBar;
    }
    
    /**
     * Sets the flag indicating whether to show the ratio in the progress bar.
     *
     * @param showRatio The flag indicating whether to show the ratio in the progress bar.
     */
    public void setShowRatio(boolean showRatio) {
        this.showRatio = showRatio;
    }
    
    /**
     * Sets the flag indicating whether to show the speed in the progress bar.
     *
     * @param showSpeed The flag indicating whether to show the speed in the progress bar.
     */
    public void setShowSpeed(boolean showSpeed) {
        this.showSpeed = showSpeed;
    }
    
    /**
     * Sets the flag indicating whether to show the time remaining in the progress bar.
     *
     * @param showTimeRemaining The flag indicating whether to show the time remaining in the progress bar.
     */
    public void setShowTimeRemaining(boolean showTimeRemaining) {
        this.showTimeRemaining = showTimeRemaining;
    }
    
    /**
     * Sets the color for the base text of the progress bar.
     *
     * @param baseColor The color for the base text of the progress bar.
     */
    public void setBaseColor(Console.ConsoleEffect baseColor) {
        this.baseColor = Optional.ofNullable(baseColor).orElse(this.baseColor);
    }
    
    /**
     * Sets the color for the "good" text of the progress bar.
     *
     * @param goodColor The color for the "good" text of the progress bar.
     */
    public void setGoodColor(Console.ConsoleEffect goodColor) {
        this.goodColor = Optional.ofNullable(goodColor).orElse(this.goodColor);
    }
    
    /**
     * Sets the color for the "bad" text of the progress bar.
     *
     * @param badColor The color for the "bad" text of the progress bar.
     */
    public void setBadColor(Console.ConsoleEffect badColor) {
        this.badColor = Optional.ofNullable(badColor).orElse(this.badColor);
    }
    
    /**
     * Sets the colors to print the progress bar in.
     *
     * @param baseColor The color for the base text of the progress bar.
     * @param goodColor The color for the "good" text of the progress bar.
     * @param badColor  The color for the "bad" text of the progress bar.
     * @see #setBaseColor(Console.ConsoleEffect)
     * @see #setGoodColor(Console.ConsoleEffect)
     * @see #setBadColor(Console.ConsoleEffect)
     */
    public void setColors(Console.ConsoleEffect baseColor, Console.ConsoleEffect goodColor, Console.ConsoleEffect badColor) {
        setBaseColor(baseColor);
        setGoodColor(goodColor);
        setBadColor(badColor);
    }
    
}
