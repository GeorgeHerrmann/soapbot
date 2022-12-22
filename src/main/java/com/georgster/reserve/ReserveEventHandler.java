package com.georgster.reserve;

import java.time.LocalTime;
import java.time.ZoneId;
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
     * the event, or a timeless event to be sent immediately if the event is full.
     * 
     * @param event the event to be scheduled
     * @param channel the channel to send the event to
     * @param id the id of the {@code Guild} the event is being scheduled for
     */
    public static void scheduleEvent(ReserveEvent event, MessageChannel channel, String id) {
        if (event.isTimeless()) { //If the event has no associated time
            if (validateTimelessEvent(event, id)) { //Will wait and listen until the event is full or cancelled
                ReserveEventTask task = new ReserveEventTask(event, channel, id); //Creates a new task associated with the completion of the event
                task.run(); //Then runs the task immediately
            }
        } else {
            LocalTime eventTime = LocalTime.parse(event.getTime()); //We keep the time as a string in the event, so we need to parse it to a LocalTime object
            long seconds = (LocalTime.now(ZoneId.of("-05:00")).until(eventTime, ChronoUnit.SECONDS)) * 1000; //How many seconds until the event is scheduled to start
            try {
                new Timer().schedule(new ReserveEventTask(event, channel, id), seconds); //Attempts to schedule that event in a Timer
            } catch (IllegalArgumentException e) { //The Timer will throw this exception if the time specified is in the past
                SoapGeneralHandler.sendTextMessageInChannel("The time specified for the event is in the past, this event will not be scheduled", channel);
                ProfileHandler.removeEvent(id, event); //To ensure negative time events are not scheduled, we remove them from the server's events.json
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
            /* Will continue to wait until the event has been removed/cancelled, or is full */
            while (ProfileHandler.eventExists(id, event.getIdentifier()) && !ProfileHandler.pullEvent(id, event.getIdentifier()).isFull()) {
                Thread.sleep(1000);
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
