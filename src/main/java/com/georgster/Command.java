package com.georgster;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * A SoapBot Command will be executed on any firing of a 
 * MessageCreateEvent (a user typing a message in a channel the bot can see),
 * given a defined command was at the beginning of the message. All commands have an associated execute function,
 * which defines what SoapBot should do after the command was given.
 */
public interface Command {
    /**
     * Executes the associated command after the {@code MessageCreateEvent was fired}.
     * Each implementation of a Command must implement this method in order to define the bot's
     * actions following a command input.
     * @param event The event of the message a user typed to active a {@code Command}.
     */
    void execute(MessageCreateEvent event);

    /**
     * Provides information about usage for a {@code Command}.
     * 
     * @return A string containing information on how to use this {@code Command}.
     */
    String help();
}
