package com.georgster.events.reserve;
import java.util.ArrayList;
import java.util.List;

import com.georgster.api.ActionWriter;
import com.georgster.events.SoapEvent;
import com.georgster.events.SoapEventType;
import com.georgster.events.TimedEvent;
import com.georgster.settings.UserSettings;
import com.georgster.util.SoapUtility;
import com.georgster.util.handler.GuildInteractionHandler;

/**
 * A ReserveEvent object is created when a user uses the !reserve command
 * with a new identifier. Every ReserveEvent object has an identifier, number,
 * of people needed to start the event, number of people reserved, a time,
 * the name of the channel the event was reserved in, and a list of reserved users.
 * <p>
 * An event that is Timeless has no associated time, while an event that is
 * Unlimited has no associated number of people needed to start the event. A ReserveEvent
 * cannot be both Timeless and Unlimited.
 */
public class ReserveEvent extends TimedEvent implements SoapEvent {
    private static final SoapEventType TYPE = SoapEventType.RESERVE;

    private String identifier;
    private int numPeople;
    private int reserved;
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
        super(time, date);

        this.identifier = identifier;
        this.numPeople = numPeople;
        this.reserved = reserved;
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
    public ReserveEvent(String identifier, int numPeople, String time, String channel, UserSettings settings) {
        super(time, settings);

        this.identifier = identifier;
        this.numPeople = numPeople;
        this.reserved = 0; //We will always start with one person reserved
        this.channel = channel;
        reservedUsers = new ArrayList<>(); //We will create a new list for reserved users
    }

    /**
     * Constructs a ReserveEvent object with an identifier, number of people, time, date, and channel name.
     * This constructor is used when a ReserveEvent that is neither Unlimited nor Timeless is created.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     */
    public ReserveEvent(String identifier, int numPeople, String time, String channel, String date, UserSettings settings) {
        super(time, date, settings);

        this.identifier = identifier;
        this.numPeople = numPeople;
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
        super("99:99");

        this.identifier = identifier;
        this.numPeople = numPeople;
        this.reserved = 0;
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
    public ReserveEvent(String identifier, String time, String channel, UserSettings settings) {
        super(time, settings);

        this.identifier = identifier;
        this.numPeople = 9999; //An Unlimited event will always have 9999 people needed to start
        this.reserved = 0;
        this.channel = channel;
        reservedUsers = new ArrayList<>();
    }

    /**
     * Constructs a ReserveEvent object with an identifier, time, date and channel name.
     * This constructor is used when a ReserveEvent that is Unlimited is created.
     * 
     * @param identifier the name of the event
     * @param time the time the event will start
     * @param channel the name of the channel the event was reserved in
     */
    public ReserveEvent(String identifier, String time, String channel, String date, UserSettings settings) {
        super(time, date, settings);

        this.identifier = identifier;
        this.numPeople = 9999; //An Unlimited event will always have 9999 people needed to start
        this.reserved = 0;
        this.channel = channel;
        reservedUsers = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    public boolean fulfilled() {
        if (!isTimeless()) {
            return until() <= 0;
        } else {
            return reserved >= numPeople;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void onFulfill(GuildInteractionHandler handler) {
        ActionWriter.writeAction("Starting event " + identifier);
        StringBuilder response = new StringBuilder("**" + reserved + "/" + numPeople + "** reserved with the following people:");
        for (String name : reservedUsers) { //We add the names of the people who reserved to the event
            response.append("\n- " + handler.getMemberById(name).getMention());
        }
        handler.sendMessage(response.toString(), "Event " + identifier + " has started!");
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
     * {@inheritDoc}
     */
    public String getOwner() {
        return reservedUsers.get(0);
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

    /**
     * Returns a String describing this {@code ReserveEvent}.
     * 
     * @return A String describing this {@code ReserveEvent}.
     */
    @Override
    public String toString() {
        StringBuilder response = new StringBuilder();
        response.append("Event: " + getIdentifier() + "\n");
        response.append("- Reserved: " + getReserved() + "\n");
        if (isUnlimited()) {
            response.append("\t- This event has no limit on the amount of people that can reserve to it\n");
        } else {
            response.append("- Needed: " + getNumPeople() + "\n");
        }
        if (isTimeless()) {
            response.append("- This event has no associated time\n");
            response.append("\t- This event will pop once the needed number of people have reserved to it");
        } else {
            response.append("- Time: " + SoapUtility.convertToAmPm(getTime()) + "\n");
            response.append("\t- This event will pop at " + SoapUtility.convertToAmPm(getTime()));
        }
        response.append("\nScheduled for: " + SoapUtility.formatDate(getDate()));
        response.append("\nReserved users:\n");
        getReservedUsers().forEach(user -> response.append("- " + user + "\n"));
        return response.toString();
    }

}
