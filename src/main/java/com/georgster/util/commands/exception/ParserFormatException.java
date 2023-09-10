package com.georgster.util.commands.exception;

import java.util.Arrays;

import com.georgster.util.commands.CommandParser;

/**
 * A {@link CommandParserException} thrown when a configuration for a {@link CommandParser CommandParser's} pattern or rules is in an invalid format.
 */
public class ParserFormatException extends CommandParserException {
    /**
     * Creates a new {@link ParserFormatException} based on the given invalid {@code attemptedInput}.
     * 
     * @param attemptedInput The invalid input configuration.
     */
    public ParserFormatException(String... attemptedInput) {
        super("Parser configuration: " + Arrays.toString(attemptedInput) + " has invalid formatting. Please refer to CommandParser docs for correct formatting");
    }

    /**
     * Creates a new {@link ParserFormatException} with the provided {@code message}.
     * 
     * @param message The exception message.
     */
    public ParserFormatException(String message) {
        super(message);
    }
}
