package com.georgster.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import com.georgster.util.SoapUtility;

/**
 * An event which has a future date and a time. The {@code TimedEvent} will handle
 * all logic involving dates and times including, but not limited to:
 * <ul>
 * <li>Standardizing date and time strings based on general inputs</li>
 * <li>Adjusting dates and times to ensure they are not in the past</li>
 * </ul>
 */
public abstract class TimedEvent {
    protected String date;
    protected String time;

    /**
     * Creates a TimedEvent based on a time input. If the time is {@code 99:99},
     * this event will have no associated time and the date is set to today, otherwise,
     * the time is standardized. If the time is not in the past, the date is set to today, otherwise
     * the date is set for the next day.
     * 
     * @param time The describing time String.
     * @throws IllegalArgumentException is the time input is invalid.
     */
    protected TimedEvent(String time) throws IllegalArgumentException {
        if (time.equals("99:99")) {
            this.time = time;
            this.date = LocalDate.now(ZoneId.of("-05:00")).toString();
        } else {
            this.date = getCorrectDate(SoapUtility.timeConverter(time));
            setTime(time);
        }
    }

    /**
     * Creates a TimedEvent based on a date and time input.
     * 
     * @param time The describing time String.
     * @param date The describing date String.
     * @throws IIllegalArgumentException is the date or time input is invalid.
     */
    protected TimedEvent(String time, String date) throws IllegalArgumentException {
        setDate(date);
        setTime(time);
    }

    /**
     * Returns whether or not this event's date is for today.
     * 
     * @return true if the event's date is for today, false otherwise.
     */
    public boolean isToday() {
        LocalDate now = LocalDate.now(ZoneId.of("-05:00"));
        LocalDate eventDate = LocalDate.parse(date);
        return (now.getYear() == eventDate.getYear() && now.getDayOfYear() == (eventDate.getDayOfYear()));
    }

    /**
     * Returns how many seconds until this event's date and time is equal to the current date and time.
     * If this event has no time, this method fails.
     * 
     * @return the number of seconds until this event's date and time is equal to the current date and time.
     */
    public long until() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("-05:00"));
        String eventDateTimeString = date + "T" + time + ":00";
        long until = (now.until(LocalDateTime.parse(eventDateTimeString), ChronoUnit.SECONDS)) - 3600;
        if (until < 0 && Math.abs(until) > 60) {
            until = Math.abs(until);
        }
        return until;
    }

    /**
     * Returns the correct date for a String based on a time String input to ensure
     * the event's date time is not in the past.
     * 
     * @param time The standardized time input.
     * @return The future-bound date input.
     */
    protected String getCorrectDate(String time) {
        LocalTime localTime = LocalTime.parse(time);
        if (LocalTime.now(ZoneId.of("-05:00")).plusHours(1).isAfter(localTime)) {
            return LocalDate.now(ZoneId.of("-05:00")).plusDays(1).toString();
        } else {
            return LocalDate.now(ZoneId.of("-05:00")).toString();
        }
    }

    /**
     * Sets the time for the event. The time will be standardized, therefore any time standard
     * accepted by {@link SoapUtility#timeConverter(String)} will be accepted.
     * 
     * @param time The new time.
     * @throws IllegalArgumentException If the time string is in an invalid format.
     */
    public void setTime(String time) throws IllegalArgumentException {
        this.time = SoapUtility.timeConverter(time);
        if (isToday()) {
            this.date = getCorrectDate(this.time); //Makes sure date time is not in the past
        }
    }

    /**
     * Sets the date for the event. The date will be standardized, therefore any date standard
     * accepted by {@link SoapUtility#convertDate(String)} will be accepted.
     * 
     * @param date The new date.
     * @throws IllegalArgumentException If the date string is in an invalid format.
     */
    public void setDate(String date) throws IllegalArgumentException {
        this.date = SoapUtility.convertDate(date);
    }

    /**
     * Sets this event's date and time based on a String describing a timer based time.
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
     * Returns the standardized Date of this event.
     * 
     * @return The date of this event.
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns the standardized Time of this event.
     * 
     * @return The time of this event.
     */
    public String getTime() {
        return time;
    }

    /**
     * Returns a formatted String representing the Date of this event.
     * 
     * @return A formatted date String.
     */
    public String getFormattedDate() {
        return SoapUtility.formatDate(date);
    }

    /**
     * Returns a formatted String representing the Time of this event.
     * 
     * @return A formatted time String.
     */
    public String getFormattedTime() {
        return SoapUtility.convertToAmPm(time);
    }

    /**
     * Returns a standardized String representing the date and time using the ISO 8601 format.
     * 
     * @return A String with this event's date and time using the ISO 8601 format.
     */
    public String getDateTime() {
        return date + "T" + time + ":00";
    }
}