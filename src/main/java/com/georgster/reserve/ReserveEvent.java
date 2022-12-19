package com.georgster.reserve;

import java.util.ArrayList;
import java.util.List;

/**
 * A ReserveEvent object is created when a user uses the !reserve command
 * with a new identifier. The identifier is the name of the event, and the
 * numPeople is the number of people that are needed to start the event.
 */
public class ReserveEvent {
    private String identifier;
    private int numPeople;
    private int numReserved;
    private String time;
    private String channel;
    private List<String> reservedUsers;
    
    /**
     * Constructs a ReserveEvent object with an identifier, number of people, number of people reserved, and time.
     * 
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param numReserved the number of people that have reserved for the event
     * @param time the time the event will start
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
     * Constructs a ReserveEvent object with an identifier, number of people, and time.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param time the time the event will start
     */
    public ReserveEvent(String identifier, int numPeople, String time, String channel) {
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.time = time;
        this.numReserved = 1;
        this.channel = channel;
        reservedUsers = new ArrayList<>();
    }

    /**
     * Constructs a ReserveEvent object with an identifier and number of people.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     */
    public ReserveEvent(String identifier, int numPeople, String channel) {
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.numReserved = 1;
        this.time = "00:00";
        this.channel = channel;
        reservedUsers = new ArrayList<>();
    }

    /**
     * Constructs a ReserveEvent object with an identifier and time.
     * 
     * @param identifier the name of the event
     * @param time the time the event will start
     */
    public ReserveEvent(String identifier, String time, String channel) {
        this.identifier = identifier;
        this.time = time;
        this.numPeople = 9999;
        this.numReserved = 1;
        this.channel = channel;
        reservedUsers = new ArrayList<>();
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
    public void addReserved() {
        numReserved++;
    }

    /**
     * Adds a person to the number of people that have reserved for the event.
     * 
     * @param user the user to add
     */
    public void addReservedUser(String user) {
        reservedUsers.add(user);
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

    public List<String> getReservedUsers() {
        return reservedUsers;
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
        return time.equals("00:00");
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

}
