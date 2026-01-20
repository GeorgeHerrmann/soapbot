package com.georgster.util.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * The resulting arguments parsed by a {@link CommandParser}. Arguments can be retrieved with {@link #getArguments()}.
 */
public class ParsedArguments {
    private String originalMessage;
    private List<String> args;
    private CommandParser parser;

    /**
     * Creates a new ParsedArguments from the provided args and parser.
     * 
     * @param args The arguments after parsing.
     * @param parser The CommandParser which parsed the arguments.
     */
    protected ParsedArguments(List<String> args, CommandParser parser, String originalMessage) {
        this.args = args;
        this.parser = parser;
        this.originalMessage = originalMessage;
    }

    /**
     * Returns the arguments parsed by the {@link CommandParser} as a List.
     * 
     * @return The arguments parsed by the {@link CommandParser} as a List.
     */
    public List<String> getArguments() {
        return args;
    }

    /**
     * Returns a list of all required arguments in the parsed command.
     * 
     * @return A list of all required arguments in the parsed command
     */
    public List<String> getRequired() {
        List<String> patternList = parser.getPattern();
        int adjuster = 0; //First adjuster will contain the number of required arguments
        for (String s : patternList) {
            if (s.contains("R")) {
                adjuster++;
            }
        }
        adjuster = args.size() - adjuster; //Represents how many optional arguments there are
        List<String> returns = new ArrayList<>();
        /* 
         * Since the parser will fill in optional arguments from left to right,
         * we only need to account for the number of optional arguments adjuster has.
         */
        for (int i = 0; i < args.size(); i++) {
            if (patternList.get(i).contains("O") && adjuster > 0) {
                adjuster--;
            } else {
                returns.add(args.get(i));
            }
        }
        return returns;
    }

    /**
     * Returns a list of all optional arguments in the parsed command.
     * 
     * @return A list of all optional arguments in the parsed command
     */
    public List<String> getOptional() {
        List<String> patternList = parser.getPattern();
        int adjuster = 0; //First adjuster will contain the number of required arguments
        for (String s : patternList) {
            if (s.contains("R")) {
                adjuster++;
            }
        }
        adjuster = args.size() - adjuster; //Represents how many optional arguments there are
        List<String> returns = new ArrayList<>();
        /* 
         * Since the parser will fill in optional arguments from left to right,
         * we only need to account for the number of optional arguments adjuster has.
         */
        for (int i = 0; i < args.size(); i++) {
            if (patternList.get(i).contains("O") && adjuster > 0) {
                returns.add(args.get(i));
                adjuster--;
            }
        }
        return returns;
    }

    /**
     * Returns a list of all variable arguments in the parsed command.
     * 
     * @return A list of all variable arguments in the parsed command
     */
    public List<String> getVariable() {
        List<String> patternList = parser.getPattern();
        List<String> returns = new ArrayList<>();
        String temp;
        int adjuster = 0; //First adjuster will contain the number of required arguments
        for (int i = 0; i < args.size(); i++) { //Finds all required arguments that are variables first
            temp = patternList.get(i);
            if (temp.contains("R")) {
                if (temp.contains("V")) {
                    returns.add(args.get(i));
                }
                adjuster++;
            }
        }
        adjuster = args.size() - adjuster; //Represents how many optional arguments there are
        for (int i = 0; i < args.size(); i++) { //Finds all optional arguments that are variables
            temp = patternList.get(i);
            if (temp.contains("O") && temp.contains("V") && adjuster > 0) { //Only added if there is room
                returns.add(args.get(i));
                adjuster --;
            }
        }
        return returns;
    }

    /**
     * Returns a list of all arguments in the parsed command that are of the given size.
     * 
     * @param size The size of the arguments to return
     * @param variable Whether or not to include variable arguments, true to include, false to exclude
     * @return A list of all arguments in the parsed command that are of the given size
     */
    public List<String> getOfSize(int size, boolean variable) {
        List<String> patternList = parser.getPattern();
        List<String> returns = new ArrayList<>();
        String temp;
        for (int i = 0; i < args.size(); i++) {
            temp = args.get(i);
            if (temp.length() == size) {
                if (!variable) {
                    if (!patternList.get(i).contains("V")) {
                        returns.add(temp);
                    }
                } else {
                    returns.add(temp);
                }
            }
        }
        return returns;
    }

    /**
     * Returns the argument at the given index in the parsed command.
     * 
     * @param index The index of the argument to return
     * @return The argument at the given index in the parsed command
     * @throws IndexOutOfBoundsException If the index is out of range
     */
    public String get(int index) throws IndexOutOfBoundsException {
        return args.get(index); //Ensures that the arguments are parsed
    }

    /**
     * Returns the number of arguments in the parsed command.
     * 
     * @return The number of arguments in the parsed command
     */
    public int size() {
        return args.size(); //Ensures that the arguments are parsed
    }

    /**
     * Returns the arguments at the given index range in the parsed command.
     * 
     * @param index1 The index of the first argument to return (inclusive)
     * @param index2 The index of the last argument to return (exclusive)
     * @return The arguments at the given index range in the parsed command
     * @throws IndexOutOfBoundsException If the index is out of range
     */
    public List<String> getCombined(int index1, int index2) throws IndexOutOfBoundsException {
        return args.subList(index1, index2);
    }

    /**
     * Returns the unformatted message from the input of the Command Parse.
     * 
     * @return The unformatted message.
     */
    public String getOriginalMessage() {
        return originalMessage;
    }

    
}
