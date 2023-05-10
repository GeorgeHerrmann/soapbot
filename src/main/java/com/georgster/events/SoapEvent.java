package com.georgster.events;

import com.georgster.control.manager.Manageable;
import com.georgster.util.GuildInteractionHandler;

/**
 * A SOAPEvent is an object that holds information that will be used
 * to schedule and fulfill said event. A SOAPEvent is scheduled upon
 * creation, and is fulfilled when the conditions to fulfill the event are met.
 */
public interface SoapEvent extends Manageable {

    /**
     * Returns the type of the event. The type of the event is used to determine
     * how the SoapEventHandler will handle the event. Every SOAPEvent must
     * declare its type from the SoapEventType enum.
     * 
     * @return the type of the event
     */
    public SoapEventType getType();

    /**
     * Returns whether or not the event is the same as the event being compared.
     * 
     * @param compare the event being compared to
     * @return true if the events are the same, false otherwise
     */
    public boolean same(SoapEvent compare);

    /**
     * Returns whether or not the conditions to fulfill the event have been met.
     * 
     * @return true if the conditions to fulfill the event have been met, false otherwise
     */
    public boolean fulfilled();

    /**
     * Represents the actions that will be taken when the event is fulfilled.
     * 
     * @param handler the GuildInteractionHandler that will be used to fulfill the event
     */
    public void onFulfill(GuildInteractionHandler handler);

    /**
     * Returns the channel that the event will be fulfilled in.
     * 
     * @return the channel that the event will be fulfilled in
     */
    public String getChannel();
}
