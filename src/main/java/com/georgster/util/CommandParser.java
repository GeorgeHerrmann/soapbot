package com.georgster.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A CommandParser parses a command and returns a list of arguments.
 */
public class CommandParser {
    private String pattern;
    private Map<String, String> patternMap;

    /**
     * Creates a new CommandParser with the given pattern.
     * A pattern is a string that defines the arguments of a command, and
     * must be in the format <num1>|<req> <num2>|<req> ... <numn>|<req> where
     * num is the number of arguments that arg takes and req is whether or not
     * the argument is required. 
     * 
     * The "num" or number of arguments can be defined by putting a number 
     * for the number of arguments, or by putting a V for a varying number of arguments.
     * 
     * The "req" or required can be defined by putting a R for required, or an O for optional.
     * 
     * A pattern cannot have a varying number of arguments and an optional argument.
     * 
     * For example, a valid
     * pattern would be "V|R 1|R V|R" which means that the command must 
     * start with a varying number of required arguments, followed by an optional
     * argument, followed by a required argument.
     * A parse("Testing the command parser") with this pattern would
     * return [""]
     * @param pattern
     */
    public CommandParser(String pattern) {
        this.pattern = pattern;
        patternMap = new HashMap<>();
        try {
            List<String> patternList = List.of(pattern.split(" ")); //Get each argument of the pattern
            for (String s : patternList) {
                patternMap.put(s.substring(0, s.indexOf("|")), s.substring(s.indexOf("|") + 1)); //Add each argument to the map
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Pattern must be in the format <num1>|<req> <num2>|<req> ... <numn>|<req> where num is the number of arguments that arg takes and req is whether or not the argument is required.");
        }
    }

    public List<String> parse(String input) {
        List<String> command = List.of(input.split(" ")); //Get each argument of the command
        command.remove(0); //Remove the command name
        
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }


}
