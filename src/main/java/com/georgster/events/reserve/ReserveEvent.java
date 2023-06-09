package com.georgster.events.reserve;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.georgster.api.ActionWriter;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.SoapUtility;

/**
 * A ReserveEvent object is created when a user uses the !reserve command
 * with a new identifier. Every ReserveEvent object has an identifier, number,
 * of people needed to start the event, number of people reserved, a time,
 * the name of the channel the event was reserved in, and a list of reserved users.
 * 
 * An event that is Timeless has no associated time, while an event that is
 * Unlimited has no associated number of people needed to start the event. A ReserveEvent
 * cannot be both Timeless and Unlimited.
 */
public class ReserveEvent implements SoapEvent {
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private String identifier;
    private int numPeople;
    private int reserved;
    private String time;
    private String date;
    private String channel;
    private List<String> reservedUsers;
    
    /**
     * Constructs a ReserveEvent object with an identifier, number of people, number of people
     * reserved, time, channel name, and a list of reserved users. This constructor is used when a ReserveEvent
     * is pulled from the database.
     * 
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param reserved the number of people that have reserved for the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     * @param reservedUsers a list of users that have reserved for the event
     */
    public ReserveEvent(String identifier, int numPeople, int reserved, String time, String channel, List<String> reservedUsers, String date) {
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.reserved = reserved;
        this.time = time;
        this.channel = channel;
        this.reservedUsers = reservedUsers;
        this.date = date;
    }

    /**
     * Constructs a ReserveEvent object with an identifier, number of people, time, and channel name.
     * This constructor is used when a ReserveEvent that is neither Unlimited nor Timeless is created.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     */
    public ReserveEvent(String identifier, int numPeople, String time, String channel) {
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.time = time;
        this.reserved = 0; //We will always start with one person reserved
        this.channel = channel;
        reservedUsers = new ArrayList<>(); //We will create a new list for reserved users
        this.date = getCorrectDate(time);
    }

    /**
     * Constructs a ReserveEvent object with an identifier, number of people, time, date, and channel name.
     * This constructor is used when a ReserveEvent that is neither Unlimited nor Timeless is created.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     */
    public ReserveEvent(String identifier, int numPeople, String time, String channel, String date) {
        this.date = date;
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.time = time;
        this.reserved = 0; //We will always start with one person reserved
        this.channel = channel;
        reservedUsers = new ArrayList<>(); //We will create a new list for reserved users
    }

    /**
     * Constructs a ReserveEvent object with an identifier number of people and channel name.
     * This constructor is used when a ReserveEvent that is Timeless is created.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param channel the name of the channel the event was reserved in
     */
    public ReserveEvent(String identifier, int numPeople, String channel) {
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.reserved = 0;
        this.time = "99:99"; //A Timeless event will always be at 99:99
        this.channel = channel;
        reservedUsers = new ArrayList<>();
        this.date = LocalDate.now(ZoneId.of("-05:00")).toString();
    }

    /**
     * Constructs a ReserveEvent object with an identifier, time and channel name.
     * This constructor is used when a ReserveEvent that is Unlimited is created.
     * 
     * @param identifier the name of the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     */
    public ReserveEvent(String identifier, String time, String channel) {
        this.identifier = identifier;
        this.time = time;
        this.numPeople = 9999; //An Unlimited event will always have 9999 people needed to start
        this.reserved = 0;
        this.channel = channel;
        reservedUsers = new ArrayList<>();
        this.date = getCorrectDate(time);
    }

    /**
     * Constructs a ReserveEvent object with an identifier, time, date and channel name.
     * This constructor is used when a ReserveEvent that is Unlimited is created.
     * 
     * @param identifier the name of the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     */
    public ReserveEvent(String identifier, String time, String channel, String date) {
        this.date = date;
        this.identifier = identifier;
        this.time = time;
        this.numPeople = 9999; //An Unlimited event will always have 9999 people needed to start
        this.reserved = 0;
        this.channel = channel;
        reservedUsers = new ArrayList<>();
    }

    /**
     * Returns the correct Date String based on the time of the event.
     * If the time of the event has already passed, the date will be set to the next day.
     * Otherwise, the date will be set to the current day.
     * <p><b>Note:</b> This method should be used when a time, but not a date, is specified.</p>
     * 
     * @param time the time of the event
     * @return the correct date of the event
     */
    private String getCorrectDate(String time) {
        LocalTime localTime = LocalTime.parse(time);
        if (LocalTime.now(ZoneId.of("-05:00")).plusHours(1).isAfter(localTime)) {
            return LocalDate.now(ZoneId.of("-05:00")).plusDays(1).toString();
        } else {
            return LocalDate.now(ZoneId.of("-05:00")).toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean fulfilled() {
        if (!isTimeless()) {
            LocalDateTime now = LocalDateTime.now(ZoneId.of("-05:00"));
            String eventDateTimeString = date + "T" + time + ":00";
            long until = (now.until(LocalDateTime.parse(eventDateTimeString), ChronoUnit.SECONDS)) - 3600;
            if (until < 0 && Math.abs(until) > 60) {
                until = Math.abs(until);
            }
            return until <= 0;
        } else {
            return reserved >= numPeople;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onFulfill(GuildInteractionHandler handler) {
        ActionWriter.writeAction("Starting event " + identifier);
        StringBuilder response = new StringBuilder("Event " + identifier + " has started!\n" +
        "- " + reserved + "/" + numPeople + " reserved with the following people:");
        for (String name : reservedUsers) { //We add the names of the people who reserved to the event
            response.append("\n\t- " + handler.getMember(name).getMention());
        }
        handler.sendText(response.toString());
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier for this event.
     * 
     * @param identifier the new identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Adds a person to the number of people that have reserved for the event.
     */
    public void addReserved(String user) {
        reserved++;
        reservedUsers.add(user);
    }

    /**
     * Removes a person from the number of people that have reserved for the event.
     */
    public void removeReserved(String user) {
        reserved--;
        reservedUsers.remove(user);
    }

    /**
     * Returns the number of people that have reserved for the event.
     * 
     * @return the number of people that have reserved for the event
     */
    public int getReserved() {
        return reserved;
    }

    /**
     * Returns the maximum amount of people who can reserve to this event.
     * If the event is timeless, this event will pop once the maximum number
     * of reservees has been reached.
     * 
     * @return the number of people needed to start the event
     */
    public int getNumPeople() {
        return numPeople;
    }

    /**
     * Sets the new maximum number of people for the event.
     * 
     * @param newNumPeople The new maximum number of people for the event.
     * @throws IllegalArgumentException If the new max is less than the number of people already reserved.
     */
    public void setNumPeople(int newNumPeople) throws IllegalArgumentException {
        if (newNumPeople < reserved) {
            throw new IllegalArgumentException("Maximum number of reservees can not be less than current number of reservees");
        } else {
            numPeople = newNumPeople;
        }
    }

    /**
     * Returns the number of people that are available to reserve for the event.
     * 
     * @return the number of people that are available to reserve for the event
     */
    public int getAvailable() {
        return numPeople - reserved;
    }

    /**
     * Returns the time the event will start.
     * 
     * @return the time the event will start
     */
    public String getTime() {
        return time;
    }

    /**
     * {@inheritDoc}
     */
    public String getOwner() {
        return reservedUsers.get(0);
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
     * Returns whether this reserve event's date is today's date.
     * 
     * @return True if the date is today's date, false otherwise.
     */
    public boolean isToday() {
        LocalDate now = LocalDate.now(ZoneId.of("-05:00"));
        LocalDate eventDate = LocalDate.parse(date);
        return (now.getYear() == eventDate.getYear() && now.getDayOfYear() == (eventDate.getDayOfYear() - 1));
    }

    /**
     * Returns the date the event will start.
     * 
     * @return the date the event will start
     */
    public String getDate() {
        return date;
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
     * Returns the channel the event was created in.
     * 
     * @return the channel the event was created in
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Returns the list of users that have reserved for the event.
     * 
     * @return the list of users that have reserved for the event
     */
    public List<String> getReservedUsers() {
        return reservedUsers;
    }

    /**
     * Returns the type of the event.
     * 
     * @return the type of the event
     */
    public SoapEventType getType() {
        return TYPE;
    }

    /**
     * Returns true if the event is full, false otherwise.
     * 
     * @return true if the event is full, false otherwise
     */
    public boolean isFull() {
        return reserved >= numPeople;
    }

    /**
     * Returns true if the event has no time, false otherwise.
     * 
     * @return true if the event has no time, false otherwise
     */
    public boolean isTimeless() {
        return time.equals("99:99");
    }

    /**
     * Returns true if the event has no player cap, false otherwise.
     * 
     * @return true if the event has no player cap, false otherwise
     */
    public boolean isUnlimited() {
        return numPeople == 9999;
    }

    /**
     * Returns true if the user has already reserved for the event, false otherwise.
     * 
     * @param user the user to check
     * @return true if the user has already reserved for the event, false otherwise
     */
    public boolean alreadyReserved(String user) {
        return reservedUsers.contains(user);
    }

    /**
     * {@inheritDoc}
     */
    public boolean same(SoapEvent compare) {

        if (!(compare instanceof ReserveEvent)) {
            return false;
        }
        ReserveEvent event = (ReserveEvent) compare;
        return identifier.equals(event.getIdentifier())
        && time.equals(event.getTime())
        && channel.equals(event.getChannel())
        && numPeople == event.getNumPeople()
        && reserved == event.getReserved()
        && reservedUsers.equals(event.getReservedUsers())
        && TYPE == event.getType();
    }

}
