package com.georgster.reserve;

import java.util.TimerTask;

import discord4j.core.object.entity.channel.MessageChannel;

public class ReserveEventTask extends TimerTask {
    
    private ReserveEvent event;
    private MessageChannel channel;

    public ReserveEventTask(ReserveEvent event, MessageChannel channel) {
        this.event = event;
        this.channel = channel;
    }

    @Override
    public void run() {
       channel.createMessage("Event " + event.getIdentifier() + " has started!\n" +
       "\t- " + event.getReserved() + "/" + event.getNumPeople() + " people reserved to this event").block();
    }

}
