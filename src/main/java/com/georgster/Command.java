package com.georgster;

import java.util.List;

import com.georgster.control.util.CommandPipeline;
import com.georgster.util.GuildManager;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A SoapBot Command will be executed on any firing of a 
 * MessageCreateEvent (a user typing a message in a channel the bot can see) or
 * a ChatInputInteractionEvent (a user using a slash command in a channel the bot can see),
 * given a defined command in the CommandRegistry was used.
 * All commands have an associated execute function, which defines what SoapBot should do after the command was given.
 */
public interface Command {
    /**
     * Executes the associated command after a {@code Event} was fired.
     * Each implementation of a Command must implement this method in order to define the bot's
     * actions following a command input, and gets a {@code GuildManager} to manage
     * information about the guild the command was executed in.
     * @param pipeline The {@code CommandPipeline} containing data from the {@code Event}.
     */
    void execute(CommandPipeline pipeline, GuildManager manager);

    /**
     * Returns a list of all aliases for a {@code Command}. The first alias in the list is the
     * will be used as the "primary" alias for the command.
     * 
     * @return A list of all aliases for a {@code Command}.
     */
    public List<String> getAliases();

    /**
     * Returns the {@code ApplicationCommandRequest} for a {@code Command}, which is used to
     * register the command with Discord.
     * 
     * @return The {@code ApplicationCommandRequest} for a {@code Command}.
     */
    public ApplicationCommandRequest getCommandApplicationInformation();

    /**
     * Returns whether or not a {@code Command} needs the {@code SoapClientManager's}
     * {@code EventDispatcher} upon execution.
     * 
     * @return {@code true} if the {@code Command} needs the {@code EventDispatcher}, {@code false} otherwise.
     */
    public boolean needsDispatcher();

    /**
     * Provides information about usage for a {@code Command}.
     * 
     * @return A string containing information on how to use this {@code Command}.
     */
    String help();
}
