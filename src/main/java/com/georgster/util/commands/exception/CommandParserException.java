package com.georgster.util.commands.exception;

import com.georgster.util.commands.CommandParser;

/**
 * The super class for all {@link RuntimeException RuntimeExceptions} thrown by a {@link CommandParser}.
 */
public abstract class CommandParserException extends RuntimeException {

    /**
     * Creates a new {@link CommandParserException} with the provided message.
     * 
     * @param message The message for why the {@link CommandParser} threw an exception.
     */
    protected CommandParserException(String message) {
        super(message);
    }
}
