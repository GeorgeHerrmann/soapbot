package com.georgster.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.georgster.control.manager.Manageable;
import com.georgster.settings.TimezoneOption;
import com.georgster.settings.UserSettings;

/**
 * An object which is associated with a date and a time.
 * <p>
 * The date and time may have no guarantees about being in the past, present or future.
 * <p>
 * Though this object is not {@link Manageable}, both the Date and Time
 * are stored as Strings, and can therefore be easily serialized and
 * deserialized.
 */
public abstract class DateTimed {
    /** The String-based representation of the date */
    protected String date;
    /** The String-based representation of the time */
    protected String time;

    /**
     * Creates a DateTimed for the current moment (EST).
     */
    protected DateTimed() {
        this.date = LocalDate.now(ZoneId.of("America/New_York")).toString();
        this.time = LocalTime.now(ZoneId.of("America/New_York")).toString();
    }

    /**
     * Creates a DateTimed for the provided {@link LocalDateTime}.
     * 
     * @param dateTime The LocalDateTime to represent this DateTimed.
     */
    protected DateTimed(LocalDateTime dateTime) {
        this.date = dateTime.toLocalDate().toString();
        this.time = dateTime.toLocalTime().toString();
    }

    /**
     * Creates a DateTimed based on the provided date and time Strings.
     * <p>
     * <b>NOTE:</b> This method provides no guarantee on the format validity of
     * the input Strings, as it is intended to be used for more unique implementations.
     * If {@link DateTimed} methods need to be used, ensure the Strings 
     * follow {@link LocalDate} and {@link LocalTime} {@code parse(String)} standards.
     * 
     * @param date The formatted date String.
     * @param time The formatted time String
     */
    protected DateTimed(String date, String time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Returns the {@link LocalDateTime} representation of this {@link DateTimed}.
     * 
     * @return The {@link LocalDateTime} representation of this {@link DateTimed}.
     */
    public LocalDateTime getLocalDateTime() {
        try {
            return LocalDateTime.parse(date + "T" + time + ":00");
        } catch (Exception e) {
            return LocalDateTime.parse(date + "T" + time);
        }
    }

    /**
     * Returns how long, in seconds, from now (EST) until this {@link DateTimed} date and time.
     * If this {@link DateTimed} is in the past, the result will be negative.
     * 
     * @return How long, in seconds, from now (EST) until this {@link DateTimed} date and time.
     */
    public long until() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/New_York"));
        String eventDateTimeString = date + "T" + time + ":00";
        return (now.until(LocalDateTime.parse(eventDateTimeString), ChronoUnit.SECONDS));
    }

    /**
     * Returns true if this {@link DateTimed DateTimed's} date is today, false otherwise.
     * 
     * @return True if this {@link DateTimed DateTimed's} date is today, false otherwise.
     */
    public boolean isToday() {
        LocalDate now = LocalDate.now(ZoneId.of("America/New_York"));
        LocalDate eventDate = LocalDate.parse(date);
        return (now.getYear() == eventDate.getYear() && now.getDayOfYear() == (eventDate.getDayOfYear()));
    }

    /**
     * Sets the time for this {@link DateTimed}. The time will be standardized, therefore any time standard
     * accepted by {@link SoapUtility#timeConverter(String)} will be accepted.
     * 
     * @param time The new time.
     * @throws IllegalArgumentException If the time string is in an invalid format.
     */
    public void setTime(String time) throws IllegalArgumentException {
        this.time = SoapUtility.timeConverter(time);
    }

    /**
     * Sets the time for this {@link DateTimed} based on the given {@link UserSettings}.
     * The time will be standardized, therefore any time standard
     * accepted by {@link SoapUtility#timeConverter(String)} will be accepted.
     * 
     * @param time The new time.
     * @param settings The {@link UserSettings} to use for formatting.
     * @throws IllegalArgumentException If the time string is in an invalid format.
     */
    public void setTime(String time, UserSettings settings) throws IllegalArgumentException {
        // EST timezone
        ZoneId estId = ZoneId.of("America/New_York");
        ZoneId userZoneId = ZoneId.of(TimezoneOption.getJavaTimeString(settings.getTimezoneSetting()));
    
        String standardizedTime = SoapUtility.timeConverter(time);
        LocalTime timeObj = LocalTime.parse(standardizedTime);
    
        // Assuming 'date' is a valid LocalDate instance you have defined earlier
        LocalDateTime userDateTime = LocalDateTime.of(LocalDate.parse(date), timeObj);
    
        // Create the date-time in the user's timezone
        ZonedDateTime userZoneDateTime = ZonedDateTime.of(userDateTime, userZoneId);

        // Convert it to EST
        ZonedDateTime estDateTime = userZoneDateTime.withZoneSameInstant(estId);

        this.time = estDateTime.toLocalTime().toString();
    }

    /**
     * Sets the date for this {@link DateTimed}. The date will be standardized, therefore any date standard
     * accepted by {@link SoapUtility#convertDate(String)} will be accepted.
     * 
     * @param date The new date.
     * @throws IllegalArgumentException If the date string is in an invalid format.
     */
    public void setDate(String date) throws IllegalArgumentException {
        this.date = SoapUtility.convertDate(date);
    }

    /**
     * Sets this {@link DateTimed DateTimed's} date and time based on a String describing a future timer based time.
     * <p>
     * For example:
     * <ul>
     * <li>5 days</li>
     * <li>1 hour</li>
     * <li>15 minutes</li>
     * </ul>
     * 
     * @param increment The descriptive String.
     * @throws IllegalArgumentException If the increment String is in a format not accepted by {@link SoapUtility#calculateFutureDateTime(String)}.
     */
    public void setDateTime(String increment) throws IllegalArgumentException {
        String[] dateTimeString = SoapUtility.calculateFutureDateTime(increment).split("T");
        this.date = dateTimeString[0];
        this.time = dateTimeString[1].substring(0, 5);
    }

     /**
     * Returns the standardized Date.
     * 
     * @return The date.
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns the standardized Time.
     * 
     * @return The time.
     */
    public String getTime() {
        return time;
    }

    /**
     * Returns a formatted String representing the Date of this {@link DateTimed}.
     * 
     * @return A formatted date String.
     */
    public String getFormattedDate() {
        return SoapUtility.formatDate(date);
    }

    /**
     * Returns a formatted String representing the Date of this {@link DateTimed} based on the given {@link UserSettings}.
     * 
     * @param settings The {@link UserSettings} to use for formatting.
     * @return A formatted date String.
     */
    public String getFormattedDate(UserSettings settings) {
        if (time.equals("99:99")) {
            return getFormattedDate();
        }
        LocalDate dateObj = LocalDate.parse(this.date);
        LocalTime timeObj = LocalTime.parse(this.time);
        LocalDateTime dateTime = LocalDateTime.of(dateObj, timeObj);

        ZoneId estZoneId = ZoneId.of("America/New_York");
        ZoneId userZoneId = ZoneId.of(settings.getTimezoneSetting().currentOption());

        ZonedDateTime estDateTime = ZonedDateTime.of(dateTime, estZoneId);
        ZonedDateTime targetDateTime = estDateTime.withZoneSameInstant(userZoneId);

        return SoapUtility.formatDate(targetDateTime.toLocalDate().toString());
    }

    /**
     * Returns a formatted String representing the Time of this {@link DateTimed}.
     * 
     * @return A formatted time String.
     */
    public String getFormattedTime() {
        return SoapUtility.convertToAmPm(time);
    }

    /**
     * Returns a formatted String representing the Time of this {@link DateTimed} based on the given {@link UserSettings}.
     * 
     * @param settings The {@link UserSettings} to use for formatting.
     * @return A formatted time String.
     */
    public String getFormattedTime(UserSettings settings) {
        if (time.equals("99:99")) {
            return "No Time";
        }
        LocalDate dateObj = LocalDate.parse(this.date);
        LocalTime timeObj = LocalTime.parse(this.time);
        LocalDateTime dateTime = LocalDateTime.of(dateObj, timeObj);

        ZoneId estZoneId = ZoneId.of("America/New_York");
        ZoneId userZoneId = ZoneId.of(settings.getTimezoneSetting().currentOption());

        ZonedDateTime estDateTime = ZonedDateTime.of(dateTime, estZoneId);
        ZonedDateTime targetDateTime = estDateTime.withZoneSameInstant(userZoneId);

        return SoapUtility.convertToAmPm(targetDateTime.toLocalTime().toString());
    }

    /**
     * Returns a standardized String representing the date and time using the ISO 8601 format.
     * <p>
     * This method manually adds the seconds field to the time String. If the time String
     * already has a seconds field, use {@link #getRawDateTime()} instead.
     * 
     * @return A String with the date and time using the ISO 8601 format.
     * @see {@link #getRawDateTime()} for a version without the seconds field.
     */
    public String getDateTime() {
        return date + "T" + time + ":00";
    }

    /**
     * Returns a standardized String representing the date and time using the ISO 8601 format.
     * <p>
     * This method does not add the seconds field to the time String. If the time String
     * does not have a seconds field, use {@link #getDateTime()} instead.
     * <p>
     * This method includes the time zone ("Z") at the end of the String.
     * 
     * @return A String with the date and time using the ISO 8601 format.
     * @see {@link #getDateTime()} for a version with the seconds field.
     */
    public String getRawDateTime() {
        return date + "T" + time + "Z";
    }
}
