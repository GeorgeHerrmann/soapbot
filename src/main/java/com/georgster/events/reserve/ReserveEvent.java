package com.georgster.events.reserve;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.georgster.api.ActionWriter;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.util.GuildManager;

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
    private String identifier;
    private int numPeople;
    private int numReserved;
    private String time;
    private String channel;
    private List<String> reservedUsers;
    private SoapEventType type = SoapEventType.RESERVE;
    
    /**
     * Constructs a ReserveEvent object with an identifier, number of people, number of people
     * reserved, time, channel name, and a list of reserved users. This constructor is used when a ReserveEvent
     * is pulled from the database.
     * 
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param numReserved the number of people that have reserved for the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     * @param reservedUsers a list of users that have reserved for the event
     */
    public ReserveEvent(String identifier, int numPeople, int numReserved, String time, String channel, List<String> reservedUsers) {
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.numReserved = numReserved;
        this.time = time;
        this.channel = channel;
        this.reservedUsers = reservedUsers;
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
        this.numReserved = 0; //We will always start with one person reserved
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
        this.numReserved = 0;
        this.time = "99:99"; //A Timeless event will always be at 99:99
        this.channel = channel;
        reservedUsers = new ArrayList<>();
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
        this.numReserved = 0;
        this.channel = channel;
        reservedUsers = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    public boolean fulfilled() {
        long until = LocalTime.now(ZoneId.of("-05:00")).until(LocalTime.parse(time), ChronoUnit.SECONDS);
        if (until < 0 && Math.abs(until) > 60) {
            until = Math.abs(until);
        }
        return isTimeless() ? numReserved >= numPeople : until <= 0;
    }

    /**
     * {@inheritDoc}
     */
    public void onFulfill(GuildManager manager) {
        ActionWriter.writeAction("Starting event " + identifier);
        StringBuilder response = new StringBuilder("Event " + identifier + " has started!\n" +
        "\t- " + numReserved + "/" + numPeople + " reserved with the following people:");
        for (String name : reservedUsers) { //We add the names of the people who reserved to the event
            response.append("\n\t\t- " + manager.getMember(name).getMention());
        }
        manager.sendText(response.toString());
    }

    /**
     * Returns the identifier of the event.
     * 
     * @return the identifier of the event
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Adds a person to the number of people that have reserved for the event.
     */
    public void addReserved(String user) {
        numReserved++;
        reservedUsers.add(user);
    }

    /**
     * Removes a person from the number of people that have reserved for the event.
     */
    public void removeReserved(String user) {
        numReserved--;
        reservedUsers.remove(user);
    }

    /**
     * Returns the number of people that have reserved for the event.
     * 
     * @return the number of people that have reserved for the event
     */
    public int getReserved() {
        return numReserved;
    }

    /**
     * Returns the number of people needed to start the event.
     * 
     * @return the number of people needed to start the event
     */
    public int getNumPeople() {
        return numPeople;
    }

    /**
     * Returns the number of people that are available to reserve for the event.
     * 
     * @return the number of people that are available to reserve for the event
     */
    public int getAvailable() {
        return numPeople - numReserved;
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
        return type;
    }

    /**
     * Returns true if the event is full, false otherwise.
     * 
     * @return true if the event is full, false otherwise
     */
    public boolean isFull() {
        return numReserved >= numPeople;
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
        && numReserved == event.getReserved()
        && reservedUsers.equals(event.getReservedUsers())
        && type == event.getType();
    }

}
