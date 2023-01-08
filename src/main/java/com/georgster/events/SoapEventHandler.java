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
     * 
     * @param event
     * @param channel
     * @param id
     */
    public static void scheduleEvent(SoapEvent event, GuildManager manager) {
        if (manager.getHandler().eventExists(event.getIdentifier())) {
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

    
    private static boolean validateSoapEvent(SoapEvent event, GuildManager manager) {
        ProfileHandler handler = manager.getHandler();
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
