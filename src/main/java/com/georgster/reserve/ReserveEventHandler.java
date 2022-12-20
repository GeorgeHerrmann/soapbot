package com.georgster.reserve;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;

import com.georgster.profile.ProfileHandler;
import com.georgster.util.SoapGeneralHandler;

import discord4j.core.object.entity.channel.MessageChannel;

/**
 * Utility class for handling reserve events.
 */
public class ReserveEventHandler {

    /**
     * Private constructor to prevent instantiation.
     */
    private ReserveEventHandler() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Schedules a reserve event to be sent to the channel at the time specified in
     * the event.
     * 
     * @param event the event to be scheduled
     * @param channel the channel to send the event to
     * @param id the id of the {@code Guild} the event is being scheduled for
     */
    public static void scheduleEvent(ReserveEvent event, MessageChannel channel, String id) {
        if (event.isTimeless()) {
            if (validateTimelessEvent(event, id)) {
                ReserveEventTask task = new ReserveEventTask(event, channel, id);
                task.run();
            }
        } else {
            LocalTime eventTime = LocalTime.parse(event.getTime());
            long seconds = (LocalTime.now().until(eventTime, ChronoUnit.SECONDS)) * 1000;
            try {
                new Timer().schedule(new ReserveEventTask(event, channel, id), seconds);
            } catch (IllegalArgumentException e) {
                SoapGeneralHandler.sendTextMessageInChannel("The time specified for the event is in the past, this event will not be scheduled", channel);
                ProfileHandler.removeEvent(id, event);
            }
        }
    }

    /**
     * Validates that a timeless event is still valid. If the event is not valid, it
     * is cancelled. A valid event is one that still exists in the server's events.json
     * and still needs to reach the number of people needed to start the event.
     * 
     * @param event the event to be validated
     * @param id the id of the {@code Guild} the event is being validated for
     * @return true if the event is valid, false otherwise
     */
    private static boolean validateTimelessEvent(ReserveEvent event, String id) {
        try {
            while (ProfileHandler.eventExists(id, event.getIdentifier()) && !ProfileHandler.pullEvent(id, event.getIdentifier()).isFull()) {
                Thread.sleep(1000);
            }
            if (!ProfileHandler.eventExists(id, event.getIdentifier())) {
                return false;
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }


}
