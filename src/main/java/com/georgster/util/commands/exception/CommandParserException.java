package com.georgster.util.commands.exception;

import com.georgster.util.commands.NewCommandParser;

/**
 * The super class for all {@link RuntimeException RuntimeExceptions} thrown by a {@link NewCommandParser}.
 */
public abstract class CommandParserException extends RuntimeException {

    /**
     * Creates a new {@link CommandParserException} with the provided message.
     * 
     * @param message The message for why the {@link NewCommandParser} threw an exception.
     */
    protected CommandParserException(String message) {
        super(message);
    }
}
