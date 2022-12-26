package com.georgster.events;
import com.georgster.profile.ProfileHandler;
import com.georgster.util.SoapHandler;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;

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
    public static void scheduleEvent(SoapEvent event, MessageChannel channel, String id) {
        if (ProfileHandler.eventExists(id, event.getIdentifier())) {
            if (validateSoapEvent(event, id)) {
                event.onFulfill(((TextChannel) channel).getGuild().block());
            } else {
                SoapHandler.sendTextMessageInChannel("Event " + event.getIdentifier() + " has been cancelled", channel);
            }
        }
    }

    
    private static boolean validateSoapEvent(SoapEvent event, String id) {
        try {
            /* Will continue to wait until the event has been removed/cancelled, or the fulfillment condition has been met */
            while (ProfileHandler.eventExists(id, event.getIdentifier()) && !ProfileHandler.pullEvent(id, event.getIdentifier()).fulfilled()) {
                Thread.sleep(2000);
            }
            if (!ProfileHandler.eventExists(id, event.getIdentifier())) { //If it was removed we return false
                return false;
            }
        } catch (Exception e) { //If we are interrupted we return false
            Thread.currentThread().interrupt();
            return false;
        }
        return true; //Otherwise we return true
    }


}
