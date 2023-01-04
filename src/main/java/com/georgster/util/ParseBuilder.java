package com.georgster.util;

/**
 * Builds a CommandParser object.
 */
public class ParseBuilder {
    private CommandParser parser; // The CommandParser object to be built.

    /**
     * Begins building a CommandParser. Since every CommandParser requires a pattern,
     * one must be provided. 
     * @param pattern The pattern for the CommandParser to use
     */
    public ParseBuilder(String pattern) {
        parser = new CommandParser(pattern);
    }

    /**
     * Adds identifiers to the CommandParser.
     * 
     * @param identifiers The identifiers to add
     * @return The active ParseBuilder
     */
    public ParseBuilder withIdentifiers(String... identifiers) {
        parser.addIdentifiers(identifiers);
        return this;
    }

    /**
     * Adds rules to the CommandParser.
     * 
     * @param rules The rules to add
     * @return The active ParseBuilder
     */
    public ParseBuilder withRules(String rules) {
        parser.setRules(rules);
        return this;
    }

    /**
     * Builds the CommandParser.
     * 
     * @return The built CommandParser
     */
    public CommandParser build() {
        return parser;
    }

}
