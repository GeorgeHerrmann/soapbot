package com.georgster;

import java.util.List;

import com.georgster.util.GuildManager;

import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * A SoapBot Command will be executed on any firing of a 
 * MessageCreateEvent (a user typing a message in a channel the bot can see),
 * given a defined command in the CommandRegistry was at the beginning of the message.
 * All commands have an associated execute function, which defines what SoapBot should do after the command was given.
 */
public interface Command {
    /**
     * Executes the associated command after the {@code MessageCreateEvent} was fired.
     * Each implementation of a Command must implement this method in order to define the bot's
     * actions following a command input, and gets a {@code GuildManager} to manage
     * information about the guild the command was executed in.
     * @param event The event of the message a user typed to active a {@code Command}.
     */
    void execute(MessageCreateEvent event, GuildManager manager);

    /**
     * Returns a list of all aliases for a {@code Command}. The first alias in the list is the
     * will be used as the "primary" alias for the command.
     * 
     * @return A list of all aliases for a {@code Command}.
     */
    public List<String> getAliases();

    /**
     * Provides information about usage for a {@code Command}.
     * 
     * @return A string containing information on how to use this {@code Command}.
     */
    String help();
}
