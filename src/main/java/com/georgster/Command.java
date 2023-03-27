package com.georgster;

import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A SoapBot Command will be executed on any firing of a 
 * MessageCreateEvent (a user typing a message in a channel the bot can see) or
 * a ChatInputInteractionEvent (a user using a slash command in a channel the bot can see),
 * given a defined command in the CommandRegistry was used.
 * All commands have an associated execute function, which is the primary entry point for a command's logic.
 */
public interface Command {
    /**
     * Executes the command from the given {@code CommandExecutionEvent}.
     * 
     * @param event The {@code CommandExecutionEvent} that triggered the command's execution.
     */
    public void execute(CommandExecutionEvent event);

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
    default ApplicationCommandRequest getCommandApplicationInformation() {
        return null;
    }

    /**
     * Returns the {@code PermissibleAction} required to execute an action within {@code Command}.
     * 
     * @param args The parsed arguments passed to the {@code Command}.
     * @return The {@code PermissibleAction} required to execute an action within {@code Command}.
     */
    default PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.DEFAULT;
    }

    /**
     * Provides information about usage for a {@code Command}.
     * 
     * @return A string containing information on how to use this {@code Command}.
     */
    public String help();
}
