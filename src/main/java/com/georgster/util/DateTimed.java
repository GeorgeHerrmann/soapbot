package com.georgster.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import com.georgster.control.manager.Manageable;

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
    protected String date;
    protected String time;

    /**
     * Creates a DateTimed for the current moment (EST).
     */
    protected DateTimed() {
        this.date = LocalDate.now(ZoneId.of("-05:00")).toString();
        this.time = LocalTime.now(ZoneId.of("-05:00")).toString();
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
     * Returns how long, in seconds, from now (EST) until this {@link DateTimed} date and time.
     * If this {@link DateTimed} is in the past, the result will be negative.
     * 
     * @return How long, in seconds, from now (EST) until this {@link DateTimed} date and time.
     */
    public long until() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("-05:00"));
        String eventDateTimeString = date + "T" + time + ":00";
        return (now.until(LocalDateTime.parse(eventDateTimeString), ChronoUnit.SECONDS)) - 3600;
    }

    /**
     * Returns true if this {@link DateTimed DateTimed's} date is today, false otherwise.
     * 
     * @return True if this {@link DateTimed DateTimed's} date is today, false otherwise.
     */
    public boolean isToday() {
        LocalDate now = LocalDate.now(ZoneId.of("-05:00"));
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
     * Returns a formatted String representing the Time of this {@link DateTimed}.
     * 
     * @return A formatted time String.
     */
    public String getFormattedTime() {
        return SoapUtility.convertToAmPm(time);
    }

    /**
     * Returns a standardized String representing the date and time using the ISO 8601 format.
     * 
     * @return A String with the date and time using the ISO 8601 format.
     */
    public String getDateTime() {
        return date + "T" + time + ":00";
    }
}
