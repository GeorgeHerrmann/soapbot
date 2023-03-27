package com.georgster;

import com.georgster.util.commands.CommandParser;

/**
 * A Command that can be parsed by a {@code CommandParser}.
 */
public interface ParseableCommand extends Command {

    /**
     * Returns the CommandParser used to parse the arguments of this command.
     * 
     * @return the CommandParser used to parse the arguments of this command.
     */
    public CommandParser getCommandParser();

}
