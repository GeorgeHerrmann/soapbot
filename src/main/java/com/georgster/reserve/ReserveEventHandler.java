package com.georgster.reserve;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;

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
     */
    protected static void scheduleEvent(ReserveEvent event, MessageChannel channel) {
        LocalTime eventTime = LocalTime.parse(event.getTime());
        long seconds = (LocalTime.now().until(eventTime, ChronoUnit.SECONDS)) * 1000;
        new Timer().schedule(new ReserveEventTask(event, channel), seconds);
    }


}
