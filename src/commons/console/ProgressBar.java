/*
 * File:    ProgressBar.java
 * Package: commons.console
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.console;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import commons.math.BoundUtility;
import commons.string.StringUtility;
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
     * The default value of the flag indicating whether or not to automatically print the progress bar after an update.
     */
    public static final boolean DEFAULT_PROGRESS_BAR_AUTO_PRINT = true;
    
    /**
     * The minimum number of milliseconds that must pass before an update can occur.
     */
    public static final long PROGRESS_BAR_MINIMUM_UPDATE_DELAY = 200;
    
    /**
     * The number of previous updates to use for calculating the rolling average speed.
     */
    public static final int ROLLING_AVERAGE_UPDATE_COUNT = 5;
    
    /**
     * The default value of the flag to show the percentage in the progress bar or not.
     */
    public static final boolean DEFAULT_SHOW_PERCENTAGE = true;
    
    /**
     * The default value of the flag to show the bar in the progress bar or not.
     */
    public static final boolean DEFAULT_SHOW_BAR = true;
    
    /**
     * The default value of the flag to show the ratio in the progress bar or not.
     */
    public static final boolean DEFAULT_SHOW_RATIO = true;
    
    /**
     * The default value of the flag to show the speed in the progress bar or not.
     */
    public static final boolean DEFAULT_SHOW_SPEED = true;
    
    /**
     * The default value of the flag to show the time remaining in the progress bar or not.
     */
    public static final boolean DEFAULT_SHOW_TIME_REMAINING = true;
    
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
    private long progress = 0;
    
    /**
     * The currently completed progress of the progress bar.
     */
    private long current = 0;
    
    /**
     * The completed progress of the progress bar at the time of the last update.
     */
    private long previous = 0;
    
    /**
     * The initial progress of the progress bar.
     */
    private long initialProgress = 0;
    
    /**
     * The last couple progress values of the progress bar for calculating the rolling average speed.
     */
    private final List<Long> rollingProgress = new ArrayList<>();
    
    /**
     * The initial duration of the progress bar in seconds.
     */
    private long initialDuration = 0;
    
    /**
     * The time of the current update of the progress bar.
     */
    private long currentUpdate = 0;
    
    /**
     * The time of the previous update of the progress bar.
     */
    private long previousUpdate = 0;
    
    /**
     * The time the progress bar was updated for the firstUpdate time.
     */
    private long firstUpdate = 0;
    
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
     * A flag indicating whether or not to automatically print the progress bar after an update.
     */
    private boolean autoPrint;
    
    /**
     * A flag indicating whether the progress bar has not been printed yet or not.
     */
    private AtomicBoolean firstPrint = new AtomicBoolean(true);
    
    /**
     * A flag indicating whether to show the percentage in the progress bar or not.
     */
    private boolean showPercentage = DEFAULT_SHOW_PERCENTAGE;
    
    /**
     * A flag indicating whether to show the bar in the progress bar or not.
     */
    private boolean showBar = DEFAULT_SHOW_BAR;
    
    /**
     * A flag indicating whether to show the ratio in the progress bar or not.
     */
    private boolean showRatio = DEFAULT_SHOW_RATIO;
    
    /**
     * A flag indicating whether to show the speed in the progress bar or not.
     */
    private boolean showSpeed = DEFAULT_SHOW_SPEED;
    
    /**
     * A flag indicating whether to show the time remaining in the progress bar or not.
     */
    private boolean showTimeRemaining = DEFAULT_SHOW_TIME_REMAINING;
    
    /**
     * The current progress bar.
     */
    private String progressBar = "";
    
    /**
     * A flag indicating whether there was an update to the progress bar or not.
     */
    private AtomicBoolean update = new AtomicBoolean(false);
    
    /**
     * A flag indicating whether the progress bar has failed or not.
     */
    private AtomicBoolean failed = new AtomicBoolean(false);
    
    
    //Constructors
    
    /**
     * Creates a new ProgressBar object.
     *
     * @param title     The title to display for the progress bar.
     * @param total     The total size of the progress bar.
     * @param width     The with of the bar in the progress bar.
     * @param units     The units of the progress bar.
     * @param autoPrint Whether or not to automatically print the progress bar after an update.
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
        this(title, total, width, units, DEFAULT_PROGRESS_BAR_AUTO_PRINT);
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
    
    /**
     * Private constructor for a new ProgressBar object.
     */
    private ProgressBar() {
    }
    
    
    //Methods
    
    /**
     * Builds the progress bar.
     *
     * @return The progress bar.
     * @see #getPercentageString()
     * @see #getBarString()
     * @see #getRatioString()
     * @see #getSpeedString()
     * @see #getTimeRemainingString()
     */
    @SuppressWarnings("HardcodedLineSeparator")
    public String get() {
        if (update.get()) {
            StringBuilder progressBarBuilder = new StringBuilder();
            
            if (getShowPercentage()) {
                progressBarBuilder.append(getPercentageString());
            }
            if (getShowBar()) {
                progressBarBuilder.append((progressBarBuilder.length() == 0) ? "" : ' ');
                progressBarBuilder.append(getBarString());
            }
            if (getShowRatio()) {
                progressBarBuilder.append((progressBarBuilder.length() == 0) ? "" : ' ');
                progressBarBuilder.append(getRatioString());
            }
            if (getShowSpeed() && !isComplete() && !isFailed()) {
                progressBarBuilder.append((progressBarBuilder.length() == 0) ? "" : ' ');
                progressBarBuilder.append(getSpeedString());
            }
            if (getShowTimeRemaining()) {
                progressBarBuilder.append((progressBarBuilder.length() == 0) ? "" : " - ");
                progressBarBuilder.append(getTimeRemainingString());
            }
            
            progressBar = progressBarBuilder.toString();
        }
        
        return progressBar;
    }
    
    /**
     * Builds the printable progress bar.<br>
     * This must be displayed with print(), not println().
     *
     * @return The printable progress bar.
     * @see #get()
     */
    public String getPrintable() {
        int oldLength = StringUtility.removeConsoleEscapeCharacters(progressBar).length();
        String bar = get();
        int newLength = StringUtility.removeConsoleEscapeCharacters(bar).length();
        return '\r' + bar + ' ' + StringUtility.spaces(Math.max((oldLength - newLength - 1), 0)) + ENDCAP;
    }
    
    /**
     * Updates the progress bar.<br>
     * If the time between updates is less than PROGRESS_BAR_MINIMUM_UPDATE_DELAY then the update will not take place until called again after the delay.
     *
     * @param newProgress The new progress of the progress bar.
     * @param autoPrint   Whether or not to automatically print the progress bar after an update.
     * @return Whether the progress bar was updated or not.
     */
    private synchronized boolean update(long newProgress, boolean autoPrint) {
        if (isComplete() || isFailed()) {
            return false;
        }
        
        firstUpdate = (firstUpdate == 0) ? System.nanoTime() : firstUpdate;
        progress = BoundUtility.truncateNum(newProgress, 0, total).longValue();
        
        boolean needsUpdate = (Math.abs(System.nanoTime() - currentUpdate) >= TimeUnit.MILLISECONDS.toNanos(PROGRESS_BAR_MINIMUM_UPDATE_DELAY));
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
     * If the time between updates is less than PROGRESS_BAR_MINIMUM_UPDATE_DELAY then the update will not take place until called again after the delay.
     *
     * @param newProgress The new progress of the progress bar.
     * @return Whether the progress bar was updated or not.
     * @see #update(long, boolean)
     */
    public synchronized boolean update(long newProgress) {
        return update(newProgress, autoPrint);
    }
    
    /**
     * Adds one to the current progress.
     *
     * @return Whether the progress bar was updated or not.
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
     * @param isError Whether the passed log is an error log or not.
     * @return Whether the progress bar was updated or not.
     */
    public synchronized boolean processLog(String log, boolean isError) {
        return false;
    }
    
    /**
     * Processes the log data passed into it and updates the progress bar accordingly.<br>
     * It is expected that this method be overridden in subclasses for specific use cases.
     *
     * @param log The log data.
     * @return Whether the progress bar was updated or not.
     * @see #processLog(String, boolean)
     */
    public synchronized boolean processLog(String log) {
        return processLog(log, false);
    }
    
    /**
     * Prints the progress bar to the console.
     *
     * @see #getPrintable()
     */
    public synchronized void print() {
        String bar = getPrintable();
        bar = bar.replace(" ", " ");
        
        if (firstPrint.get() && !title.isEmpty()) {
            System.out.println(getTitleString());
        }
        System.out.print(bar);
        System.out.flush();
        System.err.flush();
        
        firstPrint.set(false);
        update.set(false);
    }
    
    /**
     * Calculates the ratio of the progress bar.
     *
     * @return The ratio of the progress bar.
     */
    public synchronized double getRatio() {
        return ((total <= 0) || (current >= total)) ? 1 :
               ((current < 0) ? 0 :
                ((double) current / total));
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
        double recentTime = (double) Math.max((currentUpdate - previousUpdate), 0) / TimeUnit.SECONDS.toNanos(1);
        long recentProgress = Math.max((current - previous), 0);
        
        return ((recentTime == 0) || (recentProgress == 0) || (current < 0) || (previous < 0) || (previousUpdate <= 0) || (currentUpdate <= 0)) ? 0 :
               (recentProgress / recentTime);
    }
    
    /**
     * Calculates the average speed of the progress bar.
     *
     * @return The average speed of the progress bar in units per second.
     */
    public synchronized double getAverageSpeed() {
        double totalTime = (double) Math.max((currentUpdate - firstUpdate), 0) / TimeUnit.SECONDS.toNanos(1);
        
        return ((totalTime == 0) || (current <= 0) || (firstUpdate < 0) || (currentUpdate <= 0)) ? 0 :
               (current / totalTime);
    }
    
    /**
     * Calculates the rolling average speed of the progress bar for the last 5 updates.
     *
     * @return The rolling average speed of the progress bar in units per second.
     */
    public synchronized double getRollingAverageSpeed() {
        if ((rollingProgress.size() != ROLLING_AVERAGE_UPDATE_COUNT) || (rollingUpdate.size() != ROLLING_AVERAGE_UPDATE_COUNT)) {
            return 0;
        }
        
        double windowTime = (double) Math.max((rollingUpdate.get(ROLLING_AVERAGE_UPDATE_COUNT - 1) - rollingUpdate.get(0)), 0) / TimeUnit.SECONDS.toNanos(1);
        long windowProgress = Math.max((rollingProgress.get(ROLLING_AVERAGE_UPDATE_COUNT - 1) - rollingProgress.get(0)), 0);
        
        return ((windowTime == 0) || (windowProgress == 0)) ? 0 :
               (windowProgress / windowTime);
    }
    
    /**
     * Calculates the total duration of the progress bar.
     *
     * @return The total duration of the progress bar in nanoseconds.
     */
    public synchronized long getTotalDuration() {
        long totalDuration = Math.max((currentUpdate - firstUpdate), 0) +
                (Math.max(initialDuration, 0) * TimeUnit.SECONDS.toNanos(1));
        
        return ((currentUpdate <= 0) || (firstUpdate < 0)) ? 0 :
               totalDuration;
    }
    
    /**
     * Estimates the time remaining in seconds.
     *
     * @return The estimated time remaining in seconds.
     */
    public synchronized long getTimeRemaining() {
        long remainingProgress = Math.max((total - current), 0);
        long totalProgress = Math.max((current - Math.max(initialProgress, 0)), 0);
        long totalTime = Math.max((currentUpdate - firstUpdate), 0);
        
        return ((totalProgress == 0) || (totalTime == 0) || (current <= 0) || (currentUpdate <= 0) || (firstUpdate < 0)) ? Long.MAX_VALUE :
               TimeUnit.NANOSECONDS.toSeconds((long) (((double) remainingProgress / totalProgress) * totalTime));
    }
    
    /**
     * Determines if the progress bar is complete or not.
     *
     * @return Whether the progress bar is complete or not.
     */
    public synchronized boolean isComplete() {
        return (current >= total);
    }
    
    /**
     * Completes the progress bar.
     *
     * @param printTime      Whether or not to print the final time after the progress bar.
     * @param additionalInfo Additional info to print at the end of the progress bar.
     * @see #getPrintable()
     */
    public synchronized void complete(boolean printTime, String additionalInfo) {
        update(total, false);
        String completeProgressBar = getPrintable();
        
        String extras = "";
        if (printTime) {
            long duration = TimeUnit.NANOSECONDS.toMillis(getTotalDuration());
            String durationString = DateTimeUtility.durationToDurationString(duration, false, false, true);
            extras += " (" + durationString + ')';
        }
        if (!additionalInfo.isEmpty()) {
            extras += " - " + additionalInfo;
        }
        completeProgressBar = completeProgressBar.replace(" ", (extras.isEmpty() ? " " : extras));
        
        if (firstPrint.get() && !title.isEmpty()) {
            System.out.println(getTitleString());
        }
        System.out.println(completeProgressBar);
        System.out.flush();
        System.err.flush();
        
        firstPrint.set(false);
        update.set(false);
    }
    
    /**
     * Completes the progress bar.
     *
     * @param printTime Whether or not to print the final time after the progress bar.
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
     * Determines if the progress bar has failed.
     *
     * @return Whether the progress bar has failed or not.
     */
    public synchronized boolean isFailed() {
        return failed.get();
    }
    
    /**
     * Fails the progress bar.
     *
     * @param printTime      Whether or not to print the final time after the progress bar.
     * @param additionalInfo Additional info to print at the end of the progress bar.
     * @see #getPrintable()
     */
    public synchronized void fail(boolean printTime, String additionalInfo) {
        failed.set(true);
        update.set(true);
        String failedProgressBar = getPrintable();
        
        String extras = "";
        if (printTime) {
            long duration = TimeUnit.NANOSECONDS.toMillis(getTotalDuration());
            String durationString = DateTimeUtility.durationToDurationString(duration, false, false, true);
            extras += " (" + durationString + ')';
        }
        if (!additionalInfo.isEmpty()) {
            extras += " - " + additionalInfo;
        }
        failedProgressBar = failedProgressBar.replace(" ", (extras.isEmpty() ? " " : extras));
        
        if (firstPrint.get() && !title.isEmpty()) {
            System.out.println(getTitleString());
        }
        System.out.println(failedProgressBar);
        System.out.flush();
        System.err.flush();
        
        firstPrint.set(false);
        update.set(false);
    }
    
    /**
     * Fails the progress bar.
     *
     * @param printTime Whether or not to print the final time after the progress bar.
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
    public String getTitleString() {
        return Console.ConsoleEffect.CYAN.apply(getTitle() + ": ");
    }
    
    /**
     * Builds the percentage string for the progress bar.
     *
     * @return The percentage string.
     * @see #getPercentage()
     */
    public String getPercentageString() {
        int percentage = getPercentage();
        String percentageString = StringUtility.padLeft(String.valueOf(percentage), 3);
        Console.ConsoleEffect color = (isFailed() ? Console.ConsoleEffect.RED :
                                       (isComplete() ? Console.ConsoleEffect.CYAN :
                                        Console.ConsoleEffect.GREEN));
        
        return color.apply(percentageString) + '%';
    }
    
    /**
     * Builds the progress bar string for the progress bar.
     *
     * @return The progress bar string.
     * @see #getRatio()
     */
    public String getBarString() {
        double ratio = getRatio();
        int completed = Math.max((int) ((double) width * ratio), 0);
        int remaining = Math.max((width - completed - 1), 0);
        Console.ConsoleEffect color = (isFailed() ? Console.ConsoleEffect.RED :
                                       (isComplete() ? Console.ConsoleEffect.CYAN :
                                        Console.ConsoleEffect.GREEN));
        
        String bar = "=".repeat(completed) + (isComplete() ? "" : (isFailed() ? ' ' : '>')) + " ".repeat(remaining);
        return '[' + color.apply(bar) + ']';
    }
    
    /**
     * Builds the ratio string for the progress bar.
     *
     * @return The ratio string.
     */
    public String getRatioString() {
        String formattedCurrent = StringUtility.padLeft(String.valueOf(Math.max(Math.min(current, total), 0)), String.valueOf(total).length());
        Console.ConsoleEffect color = (isFailed() ? Console.ConsoleEffect.RED :
                                       (isComplete() ? Console.ConsoleEffect.CYAN :
                                        Console.ConsoleEffect.GREEN));
        
        return color.apply(formattedCurrent) + units + '/' +
                Console.ConsoleEffect.CYAN.apply(String.valueOf(total)) + units;
    }
    
    /**
     * Builds the speed string for the progress bar.
     *
     * @return The speed string.
     */
    public String getSpeedString() {
        double rollingAverageSpeed = getRollingAverageSpeed();
        String formattedSpeed = String.format("%.1f", rollingAverageSpeed);
        
        return (isFailed() || isComplete()) ? "" :
               ("at " + formattedSpeed + units + "/s");
    }
    
    /**
     * Builds the time remaining string for the progress bar.
     *
     * @return The time remaining string.
     * @see #getTimeRemaining()
     */
    public String getTimeRemainingString() {
        long time = getTimeRemaining();
        String durationStamp = DateTimeUtility.durationToDurationStamp(TimeUnit.SECONDS.toMillis(time), false, false);
        
        return isFailed() ? Console.ConsoleEffect.RED.apply("Failed") :
               (isComplete() ? Console.ConsoleEffect.CYAN.apply("Complete") :
                ((time == Long.MAX_VALUE) ? "ETA: --:--:--" :
                 ("ETA: " + durationStamp)));
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
     * Returns the flag indicating whether or not to automatically print the progress bar after an update.
     *
     * @return The flag indicating whether or not to automatically print the progress bar after an update.
     */
    public boolean getAutoPrint() {
        return autoPrint;
    }
    
    /**
     * Returns the flag indicating whether to show the percentage in the progress bar or not.
     *
     * @return The flag indicating whether to show the percentage in the progress bar or not.
     */
    public boolean getShowPercentage() {
        return showPercentage;
    }
    
    /**
     * Returns the flag indicating whether to show the bar in the progress bar or not.
     *
     * @return The flag indicating whether to show the bar in the progress bar or not.
     */
    public boolean getShowBar() {
        return showBar;
    }
    
    /**
     * Returns the flag indicating whether to show the ratio in the progress bar or not.
     *
     * @return The flag indicating whether to show the ratio in the progress bar or not.
     */
    public boolean getShowRatio() {
        return showRatio;
    }
    
    /**
     * Returns the flag indicating whether to show the speed in the progress bar or not.
     *
     * @return The flag indicating whether to show the speed in the progress bar or not.
     */
    public boolean getShowSpeed() {
        return showSpeed;
    }
    
    /**
     * Returns the flag indicating whether to show the time remaining in the progress bar or not.
     *
     * @return The flag indicating whether to show the time remaining in the progress bar or not.
     */
    public boolean getShowTimeRemaining() {
        return showTimeRemaining;
    }
    
    
    //Setters
    
    /**
     * Updates the title of the progress bar.<br>
     * If the title has already been printed then the title will not be changed.
     *
     * @param title The new title of the progress bar.
     * @return Whether the title of the progress bar was updated or not.
     */
    public synchronized boolean updateTitle(String title) {
        if (firstPrint.get()) {
            this.title = title;
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
        this.units = units;
        
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
     * @return Whether the initial progress was defined or not.
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
     * @return Whether the initial duration was defined or not.
     */
    public synchronized boolean defineInitialDuration(long initialDuration) {
        if (this.initialDuration == 0) {
            this.initialDuration = initialDuration;
            return true;
        }
        return false;
    }
    
    /**
     * Sets the flag indicating whether or not to automatically print the progress bar after an update.
     *
     * @param autoPrint The flag indicating whether or not to automatically print the progress bar after an update.
     */
    public void setAutoPrint(boolean autoPrint) {
        this.autoPrint = autoPrint;
    }
    
    /**
     * Sets the flag indicating whether to show the percentage in the progress bar or not.
     *
     * @param showPercentage The flag indicating whether to show the percentage in the progress bar or not.
     */
    public void setShowPercentage(boolean showPercentage) {
        this.showPercentage = showPercentage;
    }
    
    /**
     * Sets the flag indicating whether to show the bar in the progress bar or not.
     *
     * @param showBar The flag indicating whether to show the bar in the progress bar or not.
     */
    public void setShowBar(boolean showBar) {
        this.showBar = showBar;
    }
    
    /**
     * Sets the flag indicating whether to show the ratio in the progress bar or not.
     *
     * @param showRatio The flag indicating whether to show the ratio in the progress bar or not.
     */
    public void setShowRatio(boolean showRatio) {
        this.showRatio = showRatio;
    }
    
    /**
     * Sets the flag indicating whether to show the speed in the progress bar or not.
     *
     * @param showSpeed The flag indicating whether to show the speed in the progress bar or not.
     */
    public void setShowSpeed(boolean showSpeed) {
        this.showSpeed = showSpeed;
    }
    
    /**
     * Sets the flag indicating whether to show the time remaining in the progress bar or not.
     *
     * @param showTimeRemaining The flag indicating whether to show the time remaining in the progress bar or not.
     */
    public void setShowTimeRemaining(boolean showTimeRemaining) {
        this.showTimeRemaining = showTimeRemaining;
    }
    
}
