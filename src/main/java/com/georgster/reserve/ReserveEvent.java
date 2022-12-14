package com.georgster.reserve;


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
    
    /**
     * Constructs a ReserveEvent object with an identifier, number of people, and time.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     * @param time the time the event will start
     */
    public ReserveEvent(String identifier, int numPeople, String time) {
        this.identifier = identifier;
        this.numPeople = numPeople;
        this.time = time;
        numReserved = 1;
    }

    /**
     * Constructs a ReserveEvent object with an identifier and number of people.
     * @param identifier the name of the event
     * @param numPeople the number of people needed to start the event
     */
    public ReserveEvent(String identifier, int numPeople) {
        this.identifier = identifier;
        this.numPeople = numPeople;
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
     * Returns true if the event is full, false otherwise.
     * 
     * @return true if the event is full, false otherwise
     */
    public boolean isFull() {
        return numReserved >= numPeople;
    }

}
