package com.georgster;

import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.permissions.PermissibleAction;
import com.georgster.control.util.ClientContext;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A {@link Command} is the primary definition for a SOAP Bot Command.
 * <p>
 * {@link #execute(CommandExecutionEvent)} defines the entry-point for logic
 * when a user interacts with a Command through Discord and obtains
 * a {@link CommandExecutionEvent}, which will be created upon the firing
 * of a valid Discord4J {@code ApplicationCommandInteractionEvent} or a
 * {@code MessageCreateEvent}.
 * <p>
 * A {@link Command} is instantiated by its associated {@code CommandRegistry}
 * upon execution and, thus, a {@link Command} object only exists for as long as
 * {@link #execute(CommandExecutionEvent)} is running. A {@link Command} is <b>REQUIRED</b>
 * to have either an empty Constructor, OR a constructor with a {@link ClientContext} as
 * its only parameter.
 * <p>
 * A {@link Command} should generally <b>NOT</b> have any subcommands or any potential arguments,
 * though custom subcommand logic can be created by getting the formatted message from the {@code DiscordEvent}
 * in the {@link CommandExecutionEvent} upon execution. {@link Command Commands} which require subcommands
 * should implement {@link ParseableCommand} to define a {@code CommandParser} to parse any arguments.
 * @see ParseableCommand
 */
public interface Command {
    /**
     * Executes this {@link Command} from the given {@link CommandExecutionEvent}.
     * <p>
     * Upon execution, a {@link Command} may assume the executing User has valid Permissions for this Command,
     * and the Command has been correctly parsed for arguments (if it is a {@link ParseableCommand}). If any
     * unhandled exceptions are thrown during execution, the {@link CommandExecutionEvent} will log the exception
     * and send the Command's {@link #help()} message to the user.
     * 
     * @param event The {@link CommandExecutionEvent} that triggered the command's execution.
     */
    public void execute(CommandExecutionEvent event);

    /**
     * Returns a list of all aliases for a {@link Command}, which are how {@link Command Commands} are identified.
     * The first alias in the list is the will be used as the "primary" alias for the command. All aliases
     * are case-insensitive.
     * <p>
     * <i>For example: If a {@link Command} has the aliases "ping" and "pong", then the {@link Command} can be
     * executed with either "!ping" or "!pong." If "ping" is the primary alias and this {@link Command}
     * has a valid {@link #getCommandApplicationInformation() ApplicationCommandRequest}, /ping can also be used.</i>
     * 
     * @return A list of all aliases for a {@link Command}.
     */
    public List<String> getAliases();

    /**
     * Returns the {@link ApplicationCommandRequest} for a {@link Command}, which is used to
     * register the command with Discord, enabling slash commands. If this {@link Command} does not have a valid
     * {@link ApplicationCommandRequest}, then this method should return null and slash
     * commands will not be registered for this {@link Command}.
     * 
     * @return The {@link ApplicationCommandRequest} for a {@link Command}.
     */
    default ApplicationCommandRequest getCommandApplicationInformation() {
        return null;
    }

    /**
     * Returns the {@link PermissibleAction} required to execute an action within {@link Command}.
     * 
     * @param args The parsed arguments passed to the {@link Command}.
     * @return The {@link PermissibleAction} required to execute an action within {@link Command}.
     */
    default PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.DEFAULT;
    }

    /**
     * Provides information about usage for a {@link Command}.
     * <p>
     * A {@link #help()} message should be formatted as follows:
     * <ul>
     * <li>Each line should be separated by a newline character ({@code \n}).
     * <li>The first line should be the list of aliases for this {@link Command}.
     * <li>Each subsequent line should be a description of different ways to use this {@link Command}.
     * <li>Any additional information should be included at the end of the message, wrapped in stars (*).
     * </ul>
     * 
     * @return A string containing information on how to use this {@link Command}.
     */
    public String help();

    /**
     * Returns true the reply from a {@code ApplicationCommandInteractionEvent} should be deferred
     * because this command may take more than three seconds to compute a response, false otherwise.
     * <p>
     * <b>NOTE:</b> Only a single response needs to be sent within three seconds of {@link #execute(CommandExecutionEvent)}
     * being called. If a {@link Command} may take longer than three seconds execute, but a single interaction (i.e a sent message)
     * can be created to within three seconds, then a {@link Command} should not defer its reply.
     * 
     * @return If the reply should be deferred, false otherwise.
     */
    default boolean shouldDefer() {
        return false;
    }
}
