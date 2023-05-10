package com.georgster.events;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.util.GuildInteractionHandler;

/**
 * Utility class for handling reserve events.
 */
public class SoapEventHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private SoapEventHandler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Schedules a {@code SoapEvent} to be fulfilled for the Guild the event originated from.
     * If the event is a {@code ReserveEvent} the active channel is set to the channel the event was reserved in.
     * This method will wait and block the calling thread until the event has been
     * cancelled, is no longer valid, or the event's {@code fulfilled()} condition has been met and will
     * call {@code onFulfill()} on the event if the event is valid.
     * 
     * @param event the event to be scheduled
     * @param eventManager the SoapEventManager managing the guild the event is being scheduled for
     */
    public static void scheduleEvent(SoapEvent event, SoapEventManager eventManager) {
        GuildInteractionHandler handler = new GuildInteractionHandler(eventManager.getGuild());
        if (eventManager.exists(event.getIdentifier())) {
            handler.setActiveChannel(handler.getTextChannel(event.getChannel()));
            if (validateSoapEvent(event, eventManager)) {
                event.onFulfill(handler);
            } else {
                handler.sendText("Event " + event.getIdentifier() + " has been cancelled");
            }
            if (eventManager.exists(event.getIdentifier())) {
                eventManager.remove(event);
            }
        }
    }

    
    /**
     * Validates a {@code SoapEvent} by waiting until the event has been cancelled, is no longer valid
     * or the event's {@code fulfilled()} condition has been met.
     * 
     * @param event the event to be validated
     * @param eventManager the SoapEventManager managing the guild the event is being scheduled for
     * @return true if the event is valid, false otherwise
     */
    private static boolean validateSoapEvent(SoapEvent event, SoapEventManager eventManager) {
        try {
            /* Will continue to wait until the event has been removed/cancelled, or the fulfillment condition has been met */
            while (eventManager.exists(event.getIdentifier()) && !eventManager.get(event.getIdentifier()).fulfilled()) {
                Thread.sleep(2000);
            }
            if (!eventManager.exists(event.getIdentifier())) { //If it was removed we return false
                return false;
            }
        } catch (Exception e) { //If we are interrupted we return false
            Thread.currentThread().interrupt();
            return false;
        }
        return true; //Otherwise we return true
    }


}
