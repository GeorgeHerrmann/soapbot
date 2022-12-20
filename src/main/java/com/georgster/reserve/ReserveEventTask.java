package com.georgster.reserve;

import java.util.TimerTask;

import com.georgster.api.ActionWriter;
import com.georgster.profile.ProfileHandler;
import com.georgster.util.SoapGeneralHandler;

import discord4j.core.object.entity.channel.MessageChannel;

/**
 * The task associated with a ReserveEvent object once the event has started.
 */
public class ReserveEventTask extends TimerTask {
    
    private ReserveEvent event;
    private MessageChannel channel;
    private String id;

    /**
     * Constructs a ReserveEventTask object with a ReserveEvent, MessageChannel, and id.
     * @param event the ReserveEvent object
     * @param channel the MessageChannel object
     * @param id the id of the {@code Guild} associated with the event
     */
    public ReserveEventTask(ReserveEvent event, MessageChannel channel, String id) {
        this.event = event;
        this.channel = channel;
        this.id = id;
    }

    /**
     * Runs the task, which is to send a message in the channel that the event has started.
     */
    @Override
    public void run() {
        if (ProfileHandler.eventExists(id, event.getIdentifier())) {
            ActionWriter.writeAction("Starting event " + event.getIdentifier());
            StringBuilder response = new StringBuilder("Event " + event.getIdentifier() + " has started!\n" +
            "\t- " + ProfileHandler.pullEvent(id, event.getIdentifier()).getReserved() + "/" + event.getNumPeople() + " reserved with the following people:");
            for (String name : ProfileHandler.pullEvent(id, event.getIdentifier()).getReservedUsers()) {
                response.append("\n\t\t- " + name);
            }
            SoapGeneralHandler.sendTextMessageInChannel(response.toString(), channel);
            ProfileHandler.removeEvent(id, ProfileHandler.pullEvent(id, event.getIdentifier()));
        }
    }

}
