/*
 * File:    DateTimeUtility.java
 * Package: commons.time
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.time;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.math.string.NumberNameUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional date-time functionality.
 */
public final class DateTimeUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtility.class);
    
    
    //Constants
    
    /**
     * The minimum value of a year value.
     */
    public static final int MINIMUM_YEAR = 1;
    
    /**
     * The maximum value of a year value.
     */
    public static final int MAXIMUM_YEAR = Integer.MAX_VALUE - 1;
    
    /**
     * The minimum value of a month index.
     */
    public static final int MINIMUM_MONTH = 1;
    
    /**
     * The maximum value of a month index.
     */
    public static final int MAXIMUM_MONTH = 12;
    
    /**
     * The minimum value of a day value.
     */
    public static final int MINIMUM_DAY = 1;
    
    /**
     * The maximum value of a day value.
     */
    public static final int MAXIMUM_DAY = 31;
    
    /**
     * The minimum value of a hour value.
     */
    public static final int MINIMUM_HOUR = 0;
    
    /**
     * The maximum value of a hour value.
     */
    public static final int MAXIMUM_HOUR = 23;
    
    /**
     * The minimum value of a minute value.
     */
    public static final int MINIMUM_MINUTE = 0;
    
    /**
     * The maximum value of a minute value.
     */
    public static final int MAXIMUM_MINUTE = 59;
    
    /**
     * The minimum value of a second value.
     */
    public static final int MINIMUM_SECOND = 0;
    
    /**
     * The maximum value of a second value.
     */
    public static final int MAXIMUM_SECOND = 59;
    
    /**
     * The regex pattern of a date.
     */
    public static final Pattern DATE_PATTERN = Pattern.compile("(?<year>\\d+)-(?<month>\\d{2})-(?<day>\\d{2})");
    
    /**
     * The regex pattern of a time.
     */
    public static final Pattern TIME_PATTERN = Pattern.compile("(?<hour>\\d{2}):(?<minute>\\d{2})(:(?<second>\\d{2}))?");
    
    
    //Enums
    
    /**
     * An enumerations of Months.
     */
    public enum Month {
        
        //Values
        
        JANUARY,
        FEBRUARY,
        MARCH,
        APRIL,
        MAY,
        JUNE,
        JULY,
        AUGUST,
        SEPTEMBER,
        OCTOBER,
        NOVEMBER,
        DECEMBER
        
    }
    
    /**
     * An enumeration of Weekdays.
     */
    public enum Weekday {
        
        //Values
        
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
        
    }
    
    
    //Static Methods
    
    /**
     * Converts a date to a formatted date string.
     *
     * @param date A date string in the format 'yyyy-MM-dd'.
     * @return A formatted date string.
     * @see #monthToMonthString(int)
     * @see #dayToDayOfMonthString(int)
     */
    public static String dateToDateString(String date) {
        Matcher dateMatcher = DATE_PATTERN.matcher(date);
        
        if (dateMatcher.matches()) {
            int year = Integer.parseInt(dateMatcher.group("year"));
            int month = Integer.parseInt(dateMatcher.group("month"));
            int day = Integer.parseInt(dateMatcher.group("day"));
            
            if (!validDate(month, day, year)) {
                return date;
            }
            return monthToMonthString(month) + ' ' + dayToDayOfMonthString(day) + ", " + year;
            
        } else {
            return date;
        }
    }
    
    /**
     * Converts a date to a formatted date string.
     *
     * @param date A date.
     * @return A formatted date string.
     * @see #dateToDateString(String)
     */
    public static String dateToDateString(Date date) {
        return dateToDateString(new SimpleDateFormat("yyyy-MM-dd").format(date));
    }
    
    /**
     * Converts a month index to a month string.
     *
     * @param month The index of a month.
     * @return The month string.
     * @see #validMonth(int)
     * @see StringUtility#toTitleCase(String)
     */
    public static String monthToMonthString(int month) {
        if (!validMonth(month)) {
            return "";
        }
        
        return StringUtility.toTitleCase(Month.values()[month - 1].name().toLowerCase());
    }
    
    /**
     * Converts a day index to a day of month string.
     *
     * @param day The index of a day.
     * @return The day of month string.
     * @see #validDay(int)
     * @see NumberNameUtility#reciprocalAppendix(Number)
     */
    public static String dayToDayOfMonthString(int day) {
        if (!validDay(day)) {
            return "";
        }
        
        return day + NumberNameUtility.reciprocalAppendix(day);
    }
    
    /**
     * Converts a time to a formatted time string.
     *
     * @param time           A time string in the format 'HH:mm' or 'HH:mm:ss'.
     * @param includeSeconds A flag indicating whether to include seconds or not.
     * @return A formatted time string.
     */
    public static String timeToTimeString(String time, boolean includeSeconds) {
        Matcher timeMatcher = TIME_PATTERN.matcher(time);
        
        if (timeMatcher.matches()) {
            int hour = Integer.parseInt(timeMatcher.group("hour"));
            int minute = Integer.parseInt(timeMatcher.group("minute"));
            int second = (StringUtility.numberOfOccurrences(time, ":") > 1) ? Integer.parseInt(timeMatcher.group("second")) : 0;
            
            if (!validTime(hour, minute, second)) {
                return time;
            }
            return (((hour % 12) == 0) ? 12 : (hour % 12)) + ":" +
                    StringUtility.padZero(minute, 2) +
                    (includeSeconds ? (':' + StringUtility.padZero(second, 2)) : "") +
                    (((hour % 12) == hour) ? " AM" : " PM");
            
        } else {
            return time;
        }
    }
    
    /**
     * Converts a time to a formatted time string.
     *
     * @param time           A time.
     * @param includeSeconds A flag indicating whether to include seconds or not.
     * @return A formatted time string.
     * @see #timeToTimeString(String, boolean)
     */
    public static String timeToTimeString(Date time, boolean includeSeconds) {
        return timeToTimeString(new SimpleDateFormat("HH:mm:ss").format(time), includeSeconds);
    }
    
    /**
     * Converts a time to a formatted time string.
     *
     * @param time A time string in the format 'HH-mm-ss'.
     * @return A formatted time string.
     * @see #timeToTimeString(String, boolean)
     */
    public static String timeToTimeString(String time) {
        return timeToTimeString(time, false);
    }
    
    /**
     * Converts a time to a formatted time string.
     *
     * @param time A time.
     * @return A formatted time string.
     * @see #timeToTimeString(String)
     */
    public static String timeToTimeString(Date time) {
        return timeToTimeString(new SimpleDateFormat("HH:mm:ss").format(time));
    }
    
    /**
     * Converts a time to a formatted military time string.
     *
     * @param time           A time string in the format 'HH:mm' or 'HH:mm:ss'.
     * @param includeSeconds A flag indicating whether to include seconds or not.
     * @return A formatted military time string.
     */
    public static String timeToMilitaryTimeString(String time, boolean includeSeconds) {
        Matcher timeMatcher = TIME_PATTERN.matcher(time);
        
        if (timeMatcher.matches()) {
            int hour = Integer.parseInt(timeMatcher.group("hour"));
            int minute = Integer.parseInt(timeMatcher.group("minute"));
            int second = (StringUtility.numberOfOccurrences(time, ":") > 1) ? Integer.parseInt(timeMatcher.group("second")) : 0;
            
            if (!validTime(hour, minute, second)) {
                return time;
            }
            return StringUtility.padZero(hour, 2) + ":" +
                    StringUtility.padZero(minute, 2) +
                    (includeSeconds ? (':' + StringUtility.padZero(second, 2)) : "");
            
        } else {
            return time;
        }
    }
    
    /**
     * Converts a time to a formatted military time string.
     *
     * @param time           A time.
     * @param includeSeconds A flag indicating whether to include seconds or not.
     * @return A formatted military time string.
     * @see #timeToMilitaryTimeString(String, boolean)
     */
    public static String timeToMilitaryTimeString(Date time, boolean includeSeconds) {
        return timeToMilitaryTimeString(new SimpleDateFormat("HH:mm:ss").format(time), includeSeconds);
    }
    
    /**
     * Converts a time to a formatted military time string.
     *
     * @param time A time string in the format 'HH-mm-ss'.
     * @return A formatted military time string.
     * @see #timeToMilitaryTimeString(String, boolean)
     */
    public static String timeToMilitaryTimeString(String time) {
        return timeToMilitaryTimeString(time, false);
    }
    
    /**
     * Converts a time to a formatted military time string.
     *
     * @param time A time.
     * @return A formatted military time string.
     * @see #timeToMilitaryTimeString(String)
     */
    public static String timeToMilitaryTimeString(Date time) {
        return timeToMilitaryTimeString(new SimpleDateFormat("HH:mm:ss").format(time));
    }
    
    /**
     * Converts a time to a formatted military hours string.
     *
     * @param time A time string in the format 'HH:mm' or 'HH:mm:ss'.
     * @return A formatted military hours string.
     */
    public static String timeToMilitaryHoursString(String time) {
        Matcher timeMatcher = TIME_PATTERN.matcher(time);
        
        if (timeMatcher.matches()) {
            int hour = Integer.parseInt(timeMatcher.group("hour"));
            int minute = Integer.parseInt(timeMatcher.group("minute"));
            
            if (!validTime(hour, minute, 0)) {
                return time;
            }
            return "" + StringUtility.padZero(hour, 2) +
                    StringUtility.padZero(minute, 2) + " hours";
            
        } else {
            return time;
        }
    }
    
    /**
     * Converts a time to a formatted military hours string.
     *
     * @param time A time.
     * @return A formatted military hours string.
     * @see #timeToMilitaryHoursString(String)
     */
    public static String timeToMilitaryHoursString(Date time) {
        return timeToMilitaryHoursString(new SimpleDateFormat("HH:mm").format(time));
    }
    
    /**
     * Determines the day of the week for a date.
     *
     * @param month The month.
     * @param day   The day.
     * @param year  The year.
     * @return The day of the week on that date.
     */
    public static Weekday dayOfWeek(int month, int day, int year) {
        if (validDate(month, day, year)) {
            int[] t = new int[] {0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4};
            int y = year - ((month < 3) ? 1 : 0);
            int f = (y + (y / 4) - (y / 100) + (y / 400) + t[month - 1] + day);
            int w = (f % 7) + ((f < 0) ? 7 : 0);
            
            switch (w) {
                case 0:
                    return Weekday.SUNDAY;
                case 1:
                    return Weekday.MONDAY;
                case 2:
                    return Weekday.TUESDAY;
                case 3:
                    return Weekday.WEDNESDAY;
                case 4:
                    return Weekday.THURSDAY;
                case 5:
                    return Weekday.FRIDAY;
                case 6:
                    return Weekday.SATURDAY;
                default:
                    return null;
            }
        } else {
            return null;
        }
    }
    
    /**
     * Determines the day of the week for a date.
     *
     * @param date A date string in the format 'yyyy-MM-dd'.
     * @return The day of the week on that date.
     * @see #dayOfWeek(int, int, int)
     */
    public static Weekday dayOfWeek(String date) {
        Matcher dateMatcher = DATE_PATTERN.matcher(date);
        
        if (dateMatcher.matches()) {
            int year = Integer.parseInt(dateMatcher.group("year"));
            int month = Integer.parseInt(dateMatcher.group("month"));
            int day = Integer.parseInt(dateMatcher.group("day"));
            
            return dayOfWeek(month, day, year);
            
        } else {
            return null;
        }
    }
    
    /**
     * Determines the day of the week for a date.
     *
     * @param date A date.
     * @return The day of the week on that date.
     * @see #dayOfWeek(String)
     */
    public static Weekday dayOfWeek(Date date) {
        return dayOfWeek(new SimpleDateFormat("yyyy-MM-dd").format(date));
    }
    
    /**
     * Determines if a year is a leap year.
     *
     * @param year The year.
     * @return Whether the year is a leap year or not.
     * @see #validYear(int)
     */
    public static boolean isLeapYear(int year) {
        return validYear(year) &&
                ((((year % 4) == 0) && ((year % 100) != 0)) || ((year % 400) == 0));
    }
    
    /**
     * Converts a length in milliseconds to a duration string.
     *
     * @param duration         The duration in milliseconds.
     * @param abbreviate       Whether or not to skip time units with zero value.
     * @param showMilliseconds Whether or not to include milliseconds in the duration string.
     * @param abbreviateUnits  Whether or not to abbreviate time units.
     * @return The duration string.
     */
    public static String durationToDurationString(long duration, boolean abbreviate, boolean showMilliseconds, boolean abbreviateUnits) {
        boolean isNegative = duration < 0;
        duration = Math.abs(duration);
        int milliseconds = (int) (duration % 1000);
        duration = duration / 1000;
        int seconds = (int) (duration % 60);
        duration = duration / 60;
        int minutes = (int) (duration % 60);
        duration = duration / 60;
        int hours = (int) (duration % 24);
        duration = duration / 24;
        int days = (int) (duration);
        
        StringBuilder durationString = new StringBuilder();
        durationString.append(((!abbreviate && (durationString.length() > 0)) || (days > 0)) ? (days + (abbreviateUnits ? "d " : (" Day" + ((days == 1) ? "" : "s") + " "))) : "");
        durationString.append(((!abbreviate && (durationString.length() > 0)) || (hours > 0)) ? (hours + (abbreviateUnits ? "h " : (" Hour" + ((hours == 1) ? "" : "s") + " "))) : "");
        durationString.append(((!abbreviate && (durationString.length() > 0)) || (minutes > 0)) ? (minutes + (abbreviateUnits ? "m " : (" Minute" + ((minutes == 1) ? "" : "s") + " "))) : "");
        durationString.append(((!abbreviate && (durationString.length() > 0)) || (seconds > 0)) ? (seconds + (abbreviateUnits ? "s " : (" Second" + ((seconds == 1) ? "" : "s") + " "))) : "");
        durationString.append(showMilliseconds ? (((!abbreviate && (durationString.length() > 0)) || (milliseconds > 0)) ? (milliseconds + (abbreviateUnits ? "ms " : (" Millisecond" + ((milliseconds == 1) ? "" : "s") + " "))) : "") : "");
        durationString.insert(0, ((isNegative && (durationString.length() > 0)) ? (abbreviateUnits ? "- " : "Negative ") : ""));
        
        if (durationString.toString().isBlank()) {
            return "0" + (showMilliseconds ? (abbreviateUnits ? "ms" : " Milliseconds") : (abbreviateUnits ? "s" : " Seconds"));
        }
        return StringUtility.trim(durationString.toString());
    }
    
    /**
     * Converts a length in milliseconds to a duration string.
     *
     * @param duration         The duration in milliseconds.
     * @param abbreviate       Whether or not to skip time units with zero value.
     * @param showMilliseconds Whether or not to include milliseconds in the duration string.
     * @return The duration string.
     * @see #durationToDurationString(long, boolean, boolean, boolean)
     */
    public static String durationToDurationString(long duration, boolean abbreviate, boolean showMilliseconds) {
        return durationToDurationString(duration, abbreviate, showMilliseconds, false);
    }
    
    /**
     * Converts a length in milliseconds to a duration string.
     *
     * @param duration   The duration in milliseconds.
     * @param abbreviate Whether or not to skip time units with zero value.
     * @return The duration string.
     * @see #durationToDurationString(long, boolean, boolean)
     */
    public static String durationToDurationString(long duration, boolean abbreviate) {
        return durationToDurationString(duration, abbreviate, true);
    }
    
    /**
     * Converts a length in milliseconds to a duration string.
     *
     * @param duration The duration in milliseconds.
     * @return The duration string.
     * @see #durationToDurationString(long, boolean)
     */
    public static String durationToDurationString(long duration) {
        return durationToDurationString(duration, true);
    }
    
    /**
     * Converts a length in milliseconds to a duration stamp.
     *
     * @param duration         The duration in milliseconds.
     * @param abbreviate       Whether or not to omit leading and trailing zeros.
     * @param showMilliseconds Whether or not to include milliseconds in the duration stamp.
     * @return The duration stamp.
     */
    public static String durationToDurationStamp(long duration, boolean abbreviate, boolean showMilliseconds) {
        boolean isNegative = duration < 0;
        duration = Math.abs(duration);
        int milliseconds = (int) (duration % 1000);
        duration = duration / 1000;
        int seconds = (int) (duration % 60);
        duration = duration / 60;
        int minutes = (int) (duration % 60);
        duration = duration / 60;
        int hours = (int) (duration);
        
        StringBuilder durationStamp = new StringBuilder();
        durationStamp.append((!abbreviate || (hours > 0)) ? (((!abbreviate || (durationStamp.length() > 0)) ? StringUtility.padZero(hours, 2) : hours) + ":") : "");
        durationStamp.append((!abbreviate || (minutes > 0) || (durationStamp.length() > 0)) ? (((!abbreviate || (durationStamp.length() > 0)) ? StringUtility.padZero(minutes, 2) : minutes) + ":") : "");
        durationStamp.append((!abbreviate || (seconds > 0) || (durationStamp.length() > 0)) ? (((!abbreviate || (durationStamp.length() > 0)) ? StringUtility.padZero(seconds, 2) : seconds) + "") : "0");
        durationStamp.append(showMilliseconds ? (((!abbreviate || (milliseconds > 0)) ? "." : "") + (!abbreviate ? StringUtility.padZero(milliseconds, 3) : StringUtility.padZero(milliseconds, 3).replaceAll("0+$", ""))) : "");
        durationStamp.insert(0, (isNegative ? "-" : ""));
        
        return durationStamp.toString();
    }
    
    /**
     * Converts a length in milliseconds to a duration stamp.
     *
     * @param duration   The duration in milliseconds.
     * @param abbreviate Whether or not to omit leading and trailing zeros.
     * @return The duration stamp.
     * @see #durationToDurationStamp(long, boolean, boolean)
     */
    public static String durationToDurationStamp(long duration, boolean abbreviate) {
        return durationToDurationStamp(duration, abbreviate, true);
    }
    
    /**
     * Converts a length in milliseconds to a duration stamp.
     *
     * @param duration The duration in milliseconds.
     * @return The duration stamp.
     * @see #durationToDurationStamp(long, boolean)
     */
    public static String durationToDurationStamp(long duration) {
        return durationToDurationStamp(duration, false);
    }
    
    /**
     * Converts a duration stamp to a length in milliseconds.
     *
     * @param durationStamp The duration stamp.
     * @return The length of the duration stamp in milliseconds.
     */
    public static long durationStampToDuration(String durationStamp) {
        boolean isNegative = durationStamp.startsWith("-");
        durationStamp = durationStamp.replaceAll("^-", "").replaceAll("(?<=\\.\\d{3}).+$", "");
        int[] unitValues = new int[4];
        
        List<String> units = StringUtility.tokenize(durationStamp, ":", true);
        Collections.reverse(units);
        if (units.size() > 0) {
            if (units.get(0).contains(".")) {
                unitValues[0] = Integer.parseInt(StringUtility.padRight(units.get(0).substring(units.get(0).indexOf('.') + 1), 3, '0'));
                unitValues[1] = Integer.parseInt(units.get(0).substring(0, units.get(0).indexOf('.')));
            } else {
                unitValues[1] = Integer.parseInt(units.get(0));
            }
        }
        if (units.size() > 1) {
            unitValues[2] = Integer.parseInt(units.get(1));
        }
        if (units.size() > 2) {
            unitValues[3] = Integer.parseInt(units.get(2));
        }
        
        long duration = unitValues[0] +
                (unitValues[1] * 1000L) +
                (unitValues[2] * 60 * 1000L) +
                (unitValues[3] * 60 * 60 * 1000L);
        duration *= (isNegative ? -1 : 1);
        return duration;
    }
    
    /**
     * Determines if the date specified is a valid date.
     *
     * @param month The month.
     * @param day   The day.
     * @param year  The year.
     * @return Whether the date specified is a valid date or not.
     * @see #validYear(int)
     * @see #validMonth(int)
     * @see #validDay(int)
     */
    public static boolean validDate(int month, int day, int year) {
        if (validYear(year) && validMonth(month) && validDay(day)) {
            switch (Month.values()[month - 1]) {
                case JANUARY:
                case MARCH:
                case MAY:
                case JULY:
                case AUGUST:
                case OCTOBER:
                case DECEMBER:
                    return day <= 31;
                case APRIL:
                case JUNE:
                case SEPTEMBER:
                case NOVEMBER:
                    return day <= 30;
                case FEBRUARY:
                    return day <= (28 + (isLeapYear(year) ? 1 : 0));
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
    
    /**
     * Determines if the date specified is a valid date.
     *
     * @param date A date string in the format 'yyyy-MM-dd'.
     * @return Whether the date specified is a valid date or not.
     * @see #validDate(int, int, int)
     */
    public static boolean validDate(String date) {
        Matcher dateMatcher = DATE_PATTERN.matcher(date);
        
        if (dateMatcher.matches()) {
            int year = Integer.parseInt(dateMatcher.group("year"));
            int month = Integer.parseInt(dateMatcher.group("month"));
            int day = Integer.parseInt(dateMatcher.group("day"));
            
            return validDate(month, day, year);
            
        } else {
            return false;
        }
    }
    
    /**
     * Determines if the time specified is a valid time.
     *
     * @param hour   The month.
     * @param minute The day.
     * @param second The year.
     * @return Whether the time specified is a valid time or not.
     * @see #validHour(int)
     * @see #validMinute(int)
     * @see #validSecond(int)
     */
    public static boolean validTime(int hour, int minute, int second) {
        return validHour(hour) && validMinute(minute) && validSecond(second);
    }
    
    /**
     * Determines if the time specified is a valid time.
     *
     * @param time A date string in the format 'yyyy-MM-dd'.
     * @return Whether the time specified is a valid time or not.
     * @see #validTime(int, int, int)
     */
    public static boolean validTime(String time) {
        Matcher timeMatcher = TIME_PATTERN.matcher(time);
        
        if (timeMatcher.matches()) {
            int hour = Integer.parseInt(timeMatcher.group("hour"));
            int minute = Integer.parseInt(timeMatcher.group("minute"));
            int second = (StringUtility.numberOfOccurrences(time, ":") > 1) ? Integer.parseInt(timeMatcher.group("second")) : 0;
            
            return validTime(hour, minute, second);
            
        } else {
            return false;
        }
    }
    
    /**
     * Determines if a year is valid.
     *
     * @param year The year.
     * @return Whether the year value is valid or not.
     */
    public static boolean validYear(int year) {
        return !((year < MINIMUM_YEAR) || (year > MAXIMUM_YEAR));
    }
    
    /**
     * Determines if a month is valid.
     *
     * @param month The month.
     * @return Whether the month value is valid or not.
     */
    public static boolean validMonth(int month) {
        return !((month < MINIMUM_MONTH) || (month > MAXIMUM_MONTH));
    }
    
    /**
     * Determines if a day is valid.
     *
     * @param day The day.
     * @return Whether the day value is valid or not.
     */
    public static boolean validDay(int day) {
        return !((day < MINIMUM_DAY) || (day > MAXIMUM_DAY));
    }
    
    /**
     * Determines if a hour is valid.
     *
     * @param hour The hour.
     * @return Whether the hour value is valid or not.
     */
    public static boolean validHour(int hour) {
        return !((hour < MINIMUM_HOUR) || (hour > MAXIMUM_HOUR));
    }
    
    /**
     * Determines if a minute is valid.
     *
     * @param minute The minute.
     * @return Whether the minute value is valid or not.
     */
    public static boolean validMinute(int minute) {
        return !((minute < MINIMUM_MINUTE) || (minute > MAXIMUM_MINUTE));
    }
    
    /**
     * Determines if a second is valid.
     *
     * @param second The second.
     * @return Whether the second value is valid or not.
     */
    public static boolean validSecond(int second) {
        return !((second < MINIMUM_SECOND) || (second > MAXIMUM_SECOND));
    }
    
}
