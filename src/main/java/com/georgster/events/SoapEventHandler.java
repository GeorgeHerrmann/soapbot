package com.georgster.events;
import com.georgster.events.reserve.ReserveEvent;
import com.georgster.profile.ProfileHandler;
import com.georgster.util.GuildManager;

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
     * Schedules a {@code SoapEvent} to be fulfilled for the Guild the {@code GuildManager} is managing.
     * If the event is a {@code ReserveEvent} the active channel is set to the channel the event was reserved in.
     * This method will wait and block the calling thread until the event has been
     * cancelled, is no longer valid, or the event's {@code fulfilled()} condition has been met and will
     * call {@code onFulfill()} on the event if the event is valid.
     * 
     * @param event the event to be scheduled
     * @param manager the GuildManager managing the guild the event is being scheduled for
     */
    public static void scheduleEvent(SoapEvent event, GuildManager manager) {
        if (manager.getProfileHandler().eventExists(event.getIdentifier())) {
            if (event.getType() == SoapEventType.RESERVE) {
                manager.setActiveChannel(manager.getTextChannel(((ReserveEvent) event).getChannel()));
            }
            if (validateSoapEvent(event, manager)) {
                event.onFulfill(manager);
            } else {
                manager.sendText("Event " + event.getIdentifier() + " has been cancelled");
            }
        }
    }

    
    /**
     * Validates a {@code SoapEvent} by waiting until the event has been cancelled, is no longer validm
     * or the event's {@code fulfilled()} condition has been met.
     * 
     * @param event the event to be validated
     * @param manager the GuildManager managing the guild the event is being scheduled for
     * @return true if the event is valid, false otherwise
     */
    private static boolean validateSoapEvent(SoapEvent event, GuildManager manager) {
        ProfileHandler handler = manager.getProfileHandler();
        try {
            /* Will continue to wait until the event has been removed/cancelled, or the fulfillment condition has been met */
            while (handler.eventExists(event.getIdentifier()) && !handler.pullEvent(event.getIdentifier()).fulfilled()) {
                Thread.sleep(2000);
            }
            if (handler.eventExists(event.getIdentifier())) { //If it was removed we return false
                return false;
            }
        } catch (Exception e) { //If we are interrupted we return false
            Thread.currentThread().interrupt();
            return false;
        }
        return true; //Otherwise we return true
    }


}
