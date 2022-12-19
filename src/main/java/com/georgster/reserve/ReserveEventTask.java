package com.georgster.reserve;

import java.util.TimerTask;

import com.georgster.profile.ProfileHandler;

import discord4j.core.object.entity.channel.MessageChannel;

public class ReserveEventTask extends TimerTask {
    
    private ReserveEvent event;
    private MessageChannel channel;
    private String id;

    public ReserveEventTask(ReserveEvent event, MessageChannel channel, String id) {
        this.event = event;
        this.channel = channel;
        this.id = id;
    }

    @Override
    public void run() {
        StringBuilder response = new StringBuilder("Event " + event.getIdentifier() + " has started!\n" +
        "\t- " + ProfileHandler.pullEvent(id, event.getIdentifier()).getReserved() + "/" + event.getNumPeople() + " reserved with the following people:");
        for (String name : ProfileHandler.pullEvent(id, event.getIdentifier()).getReservedUsers()) {
            response.append("\n\t\t- " + name);
        }
       channel.createMessage(response.toString()).block();
       ProfileHandler.removeEvent(id, ProfileHandler.pullEvent(id, event.getIdentifier()));
    }

}
