package com.georgster;

import com.georgster.util.commands.CommandParser;

/**
 * A {@link Command} with arguments that can be parsed by a {@link CommandParser}.
 * <p>
 * A {@link ParseableCommand} follows all properties of a {@link Command}, but
 * also must additionaly define a {@link CommandParser} to parse arguments.
 * 
 * @see Command
 */
public interface ParseableCommand extends Command {

    /**
     * Returns the {@link CommandParser} used to parse the arguments of this command.
     * 
     * @return The {@link CommandParser} used to parse the arguments of this command.
     */
    public CommandParser getCommandParser();

}
