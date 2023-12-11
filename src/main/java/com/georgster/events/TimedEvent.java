package com.georgster.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import com.georgster.settings.UserSettings;
import com.georgster.util.DateTimed;
import com.georgster.util.SoapUtility;

/**
 * A {@link DateTimed} event which has a future date and a time. The {@link TimedEvent} will handle
 * all logic involving dates and times including, but not limited to:
 * <ul>
 * <li>Standardizing date and time strings based on general inputs</li>
 * <li>Adjusting dates and times to ensure they are not in the past</li>
 * </ul>
 */
public abstract class TimedEvent extends DateTimed {

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
            this.date = LocalDate.now(ZoneId.of("America/New_York")).toString();
        } else {
            this.date = getCorrectDate(SoapUtility.timeConverter(time));
            setTime(time);
        }
    }

    /**
     * Creates a TimedEvent based on a time input. If the time is {@code 99:99},
     * this event will have no associated time and the date is set to today, otherwise,
     * the time is standardized. If the time is not in the past, the date is set to today, otherwise
     * the date is set for the next day.
     * <p>
     * This constructor will also adjust the time to the user's timezone.
     * 
     * @param time The describing time String.
     * @param settings The user's settings.
     * @throws IllegalArgumentException is the time input is invalid.
     */
    protected TimedEvent(String time, UserSettings settings) throws IllegalArgumentException {
        if (time.equals("99:99")) {
            this.time = time;
            this.date = LocalDate.now(ZoneId.of("America/New_York")).toString();
        } else {
            this.date = getCorrectDate(SoapUtility.timeConverter(time));
            setTime(time, settings);
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
     * Creates a TimedEvent based on a date and time input.
     * <p>
     * This constructor will also adjust the time to the user's timezone.
     * 
     * @param time The describing time String.
     * @param date The describing date String.
     * @param settings The user's settings.
     * @throws IllegalArgumentException is the date or time input is invalid.
     */
    protected TimedEvent(String time, String date, UserSettings settings) throws IllegalArgumentException {
        setDate(date);
        setTime(time, settings);
    }

    /**
     * Returns how many seconds until this event's date and time is equal to the current date and time.
     * If this event has no time, this method fails.
     * 
     * @return the number of seconds until this event's date and time is equal to the current date and time.
     */
    @Override
    public long until() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("America/New_York"));
        String eventDateTimeString = date + "T" + time + ":00";
        long until = (now.until(LocalDateTime.parse(eventDateTimeString), ChronoUnit.SECONDS));
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
        if (LocalTime.now(ZoneId.of("America/New_York")).isAfter(localTime)) {
            return LocalDate.now(ZoneId.of("America/New_York")).plusDays(1).toString();
        } else {
            return LocalDate.now(ZoneId.of("America/New_York")).toString();
        }
    }

    /**
     * Sets the time for the event. The time will be standardized, therefore any time standard
     * accepted by {@link SoapUtility#timeConverter(String)} will be accepted.
     * 
     * @param time The new time.
     * @throws IllegalArgumentException If the time string is in an invalid format.
     */
    @Override
    public void setTime(String time) throws IllegalArgumentException {
        this.time = SoapUtility.timeConverter(time);
        if (isToday()) {
            this.date = getCorrectDate(this.time); //Makes sure date time is not in the past
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime(String time, UserSettings settings) throws IllegalArgumentException {
       // EST timezone
       ZoneId estId = ZoneId.of("America/New_York");
       ZoneId userZoneId = ZoneId.of(settings.getTimezoneSetting().currentOption());
   
       String standardizedTime = SoapUtility.timeConverter(time);
       LocalTime timeObj = LocalTime.parse(standardizedTime);
   
       // Assuming 'date' is a valid LocalDate instance you have defined earlier
       LocalDateTime userDateTime = LocalDateTime.of(LocalDate.parse(date), timeObj);
   
       // Create the date-time in the user's timezone
       ZonedDateTime userZoneDateTime = ZonedDateTime.of(userDateTime, userZoneId);

       // Convert it to EST
       ZonedDateTime estDateTime = userZoneDateTime.withZoneSameInstant(estId);

       this.time = estDateTime.toLocalTime().toString();

        if (isToday()) {
            this.date = getCorrectDate(this.time); //Makes sure date time is not in the past
        }
    }
}
