package com.georgster;

import com.georgster.util.commands.CommandParser;

public interface ParseableCommand extends Command {

    /**
     * Returns the CommandParser used to parse the arguments of this command.
     * 
     * @return the CommandParser used to parse the arguments of this command.
     */
    public CommandParser getCommandParser();

}
