package com.georgster.events;

import com.georgster.util.GuildManager;

/**
 * A SOAPEvent is an object that holds information that will be used
 * to schedule and fulfill said event. A SOAPEvent is scheduled upon
 * creation, and is fulfilled when the event when the conditions to
 * fulfill the event are met.
 */
public interface SoapEvent {

    /**
     * Returns the identifier of the event. Every SOAPEvent must have a unique identifier.
     * How this identifier is generated is up to the implementation of the SOAPEvent.
     * 
     * @return the identifier of the event
     */
    public String getIdentifier();

    /**
     * Returns the type of the event. The type of the event is used to determine
     * how the SoapEventHandler will handle the event. Every SOAPEvent must
     * declare its type from the SoapEventType enum.
     * 
     * @return the type of the event
     */
    public SoapEventType getType();

    /**
     * Returns whether or not the conditions to fulfill the event have been met.
     * 
     * @return true if the conditions to fulfill the event have been met, false otherwise
     */
    public boolean fulfilled();

    /**
     * Represents the actions that will be taken when the event is fulfilled.
     */
    public void onFulfill(GuildManager manager);
}
