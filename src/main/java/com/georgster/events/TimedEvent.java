package com.georgster.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import com.georgster.util.SoapUtility;

public abstract class TimedEvent {
    protected String date;
    protected String time;

    protected TimedEvent(String time) {
        if (time.equals("99:99")) {
            this.time = time;
            this.date = LocalDate.now(ZoneId.of("-05:00")).toString();
        } else {
            this.date = getCorrectDate(time);
            setTime(time);
        }
    }

    protected TimedEvent(String time, String date) {
        setDate(date);
        setTime(time);
    }

    public boolean isToday() {
        LocalDate now = LocalDate.now(ZoneId.of("-05:00"));
        LocalDate eventDate = LocalDate.parse(date);
        return (now.getYear() == eventDate.getYear() && now.getDayOfYear() == (eventDate.getDayOfYear()));
    }

    public long until() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("-05:00"));
        String eventDateTimeString = date + "T" + time + ":00";
        long until = (now.until(LocalDateTime.parse(eventDateTimeString), ChronoUnit.SECONDS)) - 3600;
        if (until < 0 && Math.abs(until) > 60) {
            until = Math.abs(until);
        }
        return until;
    }

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

    public void setDateTime(String increment) throws IllegalArgumentException {
        String[] dateTimeString = SoapUtility.calculateFutureDateTime(increment).split("T");
        this.date = dateTimeString[0];
        this.time = dateTimeString[1].substring(0, 5);
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getFormattedDate() {
        return SoapUtility.formatDate(date);
    }

    public String getFormattedTime() {
        return SoapUtility.convertToAmPm(time);
    }

    public String getDateTime() {
        return date + "T" + time + ":00";
    }
}
