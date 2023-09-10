package com.georgster.util.commands.exception;

import com.georgster.util.commands.CommandParser;

/**
 * A {@link CommandParserException} thrown when an input String for a {@link CommandParser}
 * does not match the parser's pattern and or rules when parsing the input.
 * <p>
 * <b>Note:</b> This exception can only be thrown if the {@link CommandParser} has required arguments.
 * If the parser has no required arguments, any input can match the pattern and rules, unless the pattern or rules are invalid,
 * in which case a {@link ParserFormatException} is thrown.
 */
public class ParseInputException extends CommandParserException {
    /**
     * Constructs a {@link ParseInputException} for the invalid {@code attemptedInput} for the configuration of the {@code parser}.
     * 
     * @param attemptedInput The invalid input String.
     * @param parser The {@link CommandParser} for which the configuration did not match the {@code attemptedInput}.
     */
    public ParseInputException(String attemptedInput, CommandParser parser) {
        super ("Input " + attemptedInput + " does not match pattern " + parser.getPattern() + " and or rules " + parser.getRules().toString());
    }
}
