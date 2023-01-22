package com.georgster.util.commands;

import com.georgster.util.GuildManager;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

public class CommandWizard {

    /*private final GuildManager manager;
    private final EventDispatcher dispatcher;
    private Message message;
    private Flux<MessageCreateEvent> caller;
    private Disposable canceller;

    public CommandWizard(GuildManager manager) {
        this.manager = manager;
        this.dispatcher = manager.getEventDispatcher();
        caller = dispatcher.on(MessageCreateEvent.class);
        caller.subscribe(event -> this.message = event.getMessage());
    }

    public boolean ended(String end) {
        return message.getContent().equals(end);
    }

    public Message step(String step) {
        Message previous = message;
        while (!previous.getContent().equals(message.getContent())) {
            
        }

    }*/


}
