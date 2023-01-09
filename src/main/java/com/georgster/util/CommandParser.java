package com.georgster.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A CommandParser parses a command and returns a list of arguments.
 * A pattern is a string that defines the arguments of a command, and
 * must be in the format <num1>|<req> <num2>|<req> ... <numn>|<req> where
 * num is the number of arguments that arg takes and req is whether or not
 * the argument is required. The parser will prioritize required arguments,
 * and will return the first set of arguments that match the pattern.
 * 
 * The "num" or number of arguments can be defined by putting a number 
 * for the number of arguments, or by putting a V for a varying number of arguments.
 * 
 * The "req" or required can be defined by putting a R for required, or an O for optional.
 * 
 * A pattern that has a varying number of arguments may not always return the desired
 * output unless the parser is given identifiers and/or rules. Identifiers are strings
 * that are used to identify arguments, and rules are strings that define how the parser
 * should parse the command. The parser will return the first set of arguments that match
 * the pattern, and the identifiers and rules will be used to determine how argument
 * are "grouped" for the returning list. Rules and identifiers are optional, but may help
 * the parser return the desired output.
 * 
 * For example, a valid pattern would be "V|R 1|R" which means that the command must 
 * start with a varying number of required arguments, followed by a required argument.
 * A parse("Testing the command parser") with this pattern would
 * return ["Testing the command", "parser"].
 * 
 * Another example would be "V|R 1|O 1|R". Without specific argument identifiers, this
 * pattern would return ["Testing the command", "parser"]. With "parser" being the 
 * only argument identifier, this pattern would still return the same result. However,
 * with "command" and "parser" being the argument identifiers, this pattern would return
 * ["Testing the", "command", "parser"].
 * 
 * @author George Herrmann
 */
public class CommandParser {
    private String pattern; //A string that defines the arguments of a command
    private List<String> rulesList; //A list of the rules split by spaces
    private List<String> patternList; //A list of the pattern split by spaces
    private List<String> identifiers; //A list of the identifiers
    private List<String> arguments; //A list of the parsed arguments

    /**
     * Creates a new CommandParser with the given pattern.
     * A pattern is a string that defines the arguments of a command, and
     * must be in the format <num1>|<req> <num2>|<req> ... <numn>|<req> where
     * num is the number of arguments that arg takes and req is whether or not
     * the argument is required.
     * 
     * @param pattern The pattern to use for parsing commands.
     * @throws IllegalArgumentException If the pattern is not in the correct format.
     */
    public CommandParser(String pattern) throws IllegalArgumentException {
        this.pattern = pattern;
        identifiers = new ArrayList<>();
        rulesList = new ArrayList<>();
        arguments = new ArrayList<>();
        try {
            this.patternList = new ArrayList<>(List.of(pattern.split(" ")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Pattern must be in the format <num1>|<req> <num2>|<req> ... <numn>|<req> where num is the number of arguments that arg takes and req is whether or not the argument is required.");
        }
    }

    /**
     * Adds identifiers to the parser. Identifiers are hard-defined inputs for non-variable arguments.
     * Identifiers assist the parser in stopping at the correct argument when parsing for variable arguments.
     * Identifiers may help the parser if there is only one variable argument, but is really only necessary
     * if there are multiple variable arguments.
     * 
     * NOTE: Adding rules and/or identifiers is only necessary if you have a varying amount of 
     * arguments, otherwise the parser will automatically correctly parse the command.
     * @param identifiers The identifiers to use for parsing commands.
     */
    public void addIdentifiers(String... identifiers) {
        for (String s : identifiers) {
            this.identifiers.add(s);
        }
    }

    /**
     * Clears all identifiers from the parser.
     */
    public void clearIdentifiers() {
        identifiers.clear();
    }

    /**
     * Adds rules to the parser. Rules are strings that define the format for the arguments
     * of a command. If a rule is set using this method, a rule for every argument in the
     * pattern must be set. For example: if the pattern is "V|R 1|R", then two rules must be set.
     * For optional arguments, the parser will not throw an exception if the argument does not
     * match the rule, but will simply ignore it. However, if the argument is required and
     * does not match the rule, the parser will throw an exception. Multiple rules for one argument 
     * can be specified with a | for 'or' and a & for 'and', however you can only pair 'and' for E rules (as E rules are mutually exclusive)
     * because there is no reason you would want, for example, an argument to be the last argument OR a number,
     * since that means anything that is a number is already valid.
     * 
     * Valid rules are as follows:
     * - "N" if this argument must contain a number
     * - "!N" if this argument must not contain a number
     * - "S" if this argument must contain a string
     * - "!S" if this argument must not contain a string
     * - "I" if this argument must be an identifier
     * - "T" if this argument must contain a time
     * - "X" if this argument has no rules
     * - "E1" if this argument must be the last argument
     * - "E2" if this argument must be the second to last argument
     * 
     * For example: the reserve command is in the format V|R 1|O 1|O. The varying
     * argument has no rules, however both the optional arguments must contain a number.
     * Therefore, the rules would be "X N N". With these rules in place, a parse() of
     * "reserve testing parser 2" would return ["testing parser", "2"]. If no rules
     * were set, the parse() would return "testing" "parser" "2".
     * 
     * NOTE: Adding rules and/or identifiers is only necessary if you have a varying amount of 
     * arguments, otherwise the parser will automatically correctly parse the command.
     * @param rules The rules to use for parsing commands.
     * @throws IllegalArgumentException If the number of rules does not match the number of arguments in the pattern, or if the rules are not in the correct format.
     */
    public void setRules(String rules)  throws IllegalArgumentException {
        rulesList = new ArrayList<>();
        String[] split = rules.split(" ");
        if (split.length != patternList.size()) {
            throw new IllegalArgumentException("The number of rules must match the number of arguments in the pattern.");
        } else {
            for (String s : split) {
                if (!s.contains("N") && !s.contains("!N") && !s.contains("S") && !s.contains("!S") && !s.contains("X") && !s.contains("T") && !s.contains("E") && !s.contains("I")) {
                    throw new IllegalArgumentException("Rules must be in the format N, !N, S, !S, I, X, T, E1, or E2.");
                } else {
                    rulesList.add(s);
                }
            }
        }
    }


    /**
     * Simply parses the input by splitting it by spaces and
     * returns the result.
     * 
     * @param input The input to parse.
     * @return A list of arguments split by spaces.
     */
    public static List<String> parseGeneric(String input) {
        String[] split = input.substring(1).split(" ");
        return new ArrayList<>(Arrays.asList(split));
    }
    /**
     * Parses the given input and returns a list of arguments that match the pattern of
     * this CommandParser. If the input does not match the pattern, an IllegalArgumentException
     * is thrown. If this parser has rules and/or identifiers set, they are used to help
     * accurately parse the input, otherwise the parser will indiscriminately parse in this order:
     * 
     * 1. All required arguments, with varying arguments being treated as one argument.
     * 2. All optional arguments, with varying arguments being treated as one argument.
     * 3. All varying arguments, with the parser filling in the rest of the arguments left to right.
     * @param input The input to parse.
     * @return A list of arguments that match the pattern and rules of this CommandParser.
     * @throws IllegalArgumentException If the input does not match the pattern and rules of this CommandParser.
     */
    public List<String> parse(String input) throws IllegalArgumentException {
        List<String> command = new ArrayList<>(Arrays.asList(input.split(" "))); //Get each argument of the command
        List<String> args = new ArrayList<>();

        if (command.size() == 1) {
            arguments = args;
            return args;
        } else {
            command.remove(0); //Remove the command itself
        }
        
        List<Integer> added = new ArrayList<>();
        if (rulesList.isEmpty()) {
            for (int i = 0; i < patternList.size(); i++) {
                rulesList.add("X");
            }
        }
        int loc = 0;
        int holder = 0;
        /*
         * First examine only required arguments, varying arguments get one spot here 
         * otherwise if its a number it gets that many spots. Loc is the
         * index in command that is being examined. Holder is the index in args that
         * the argument is being added to. Added is a list of the indexes in command that
         * have been filled. If there is not enough room for required arguments, or no arguments
         * satisfy the rule for the argument we throw an IllegalArgumentException.
         */
        for (String s : patternList) { //The list of patterns determines how long our output will be
            String[] split = s.split("|");
            if (split[2].equals("R")) { //We only want to examine required arguments first
                /*
                 * We will continue moving loc until we find an argument that matches the rule
                 */
                while (!matchesRule(command.get(loc), rulesList.get(holder), command) || added.contains(loc)) {
                    loc++;
                    if (loc >= command.size()) { //If we run out of room, no arguments satisfy the rule or pattern
                        throw new IllegalArgumentException("The command does not match the pattern.");
                    }
                }
                if (split[0].equals("V")) { //Varying arguments, for now, get one spot
                    args.add(command.get(loc));
                    holder++; //Holder is maintained throughout each "section" of the parse
                    added.add(loc);
                    loc = 0; //We want to examine each argument on every pass
                } else {
                    StringBuilder sb = new StringBuilder();
                    int num = Integer.parseInt(split[0]);
                    if (loc + num > command.size()) { //If there is not enough room for the number of arguments, we throw an exception
                        throw new IllegalArgumentException("The command does not match the pattern.");
                    }
                    for (int i = 0; i < num; i++) {
                        sb.append(command.get(loc) + " ");
                        added.add(loc);
                        loc ++;
                    }
                    args.add(sb.toString().trim());
                    holder ++;
                    loc = 0;
                }
            }
        }
        /* Required arguments done */
        loc = 0;
        holder = 0;
        
        /*
         * Now examine optional arguments, if any exist. If there is not enough room for optional arguments,
         * this step is effectively skipped. Instead of throwing an exception is no arguments satisfy the rule for
         * the part of the pattern, we just move on to the next part of the pattern. Since we have already filled
         * some spots for the required arguments, we may need to shift elements in args to make room for optional
         * arguments.
         */
        for (String s : patternList) { //We loop through again, this time examining optional arguments
            String[] split = s.split("|");
            if (split[2].equals("O")) {
                /* We move loc until we have found an argument that matches the rule, or until we hit a new argument */
                while (loc < command.size() && (!matchesRule(command.get(loc), rulesList.get(holder), command) || added.contains(loc))) {
                    loc++;
                }
                if (loc < command.size()) { //If we have not run out of room, we have found an argument that matches the rule
                    if (split[0].equals("V")) { //Varying arguments still get one spot
                        StringBuilder sb = new StringBuilder();
                        args.add(holder, ""); //We add a new argument at the spot
                        /*
                         * In order to ensure that the arguments are in the correct order, and that we are able to
                         * move elements around later on. We follow this shifting process:
                         * - adjuster1 keeps track of any multiple word arguments that we have passed,
                         *  so that we can shift the rest of the arguments to the right by the correct amount.
                         * - adjuster2 is the same, however it will be used to shift the rest of the arguments
                         *  past the optional argument to the right by the correct amount.
                         * - If we need to shift elements to the right, we do so by calling shiftElementsRight
                         *  on each element beyond the optional argument, and append the "lost" element to
                         *  the end of the optional argument we are adding in.
                         */
                        int adjuster1 = 0; //It starts at zero
                        for (int x = 0; x < holder; x++) { //We want to keep note of all the arguments we have passed
                            if (args.get(x).split(" ").length > 1) { //Holder will already reflect any single-word arguments
                                adjuster1 += args.get(x).split(" ").length - 1; //So we only need to adjust for multi-word arguments
                            }
                        }
                        int adjuster2 = adjuster1 == 0 ? adjuster1 : adjuster1 - 1; //Adjuster2 keeps track of arguments beyond the optional argument we're adding in
                        if (holder < args.size() - 1) { //If we are not at the end, we're going to shift the rest of the args to the right
                            for (int j = 0; j < (args.size() - holder) - 1; j++) { //We need to shift the rest of the arguments beyond the index of holder
                                adjuster2 += args.get(holder + j + 1).split(" ").length - 1; //Adjuster2 will keep track of multi-word arguments beyond holder's index
                                args.set(holder + j + 1, shiftElementsRight(args.get(holder + j + 1), command.subList((holder + j + 1 + adjuster2), command.size())));
                            }
                        }
                        if (holder >= args.size() - 1) { //We simply want to add what's at loc if we're already at the end
                            sb.append(command.get(loc) + " ");
                        } else { //Otherwise shiftElementsRight has already added it in, so we need to add what was lost by shifting
                            sb.append(command.get(holder + adjuster1) + " ");
                        }
                        added.add(loc); //Add our new argument to the list of added arguments
                        args.set(holder, sb.toString().trim());
                        holder ++;
                        loc = 0;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        int num = Integer.parseInt(split[0]);
                        if (loc + num > command.size()) {
                            break;
                        }
                        if (loc < command.size()) {
                            args.add(holder, "");
                            int adjuster1 = num - 1;
                            for (int x = 0; x < holder; x++) {
                                if (args.get(x).split(" ").length > 1) {
                                    adjuster1 += args.get(x).split(" ").length - 1;
                                }
                            }
                            int adjuster2 = adjuster1 == 0 ? adjuster1 : adjuster1 - 1;
                            for (int i = 0; i < num; i++) { //This process is identical to the one above, except we are adding in multiple arguments
                                if (holder < args.size() - 1) { //If we are not at the end, we're going to shift the rest of the args to the right
                                    for (int j = 0; j < (args.size() - holder) - 1; j++) {
                                        adjuster2 += args.get(holder + j + 1).split(" ").length - 1;
                                        args.set(holder + j + 1, shiftElementsRight(args.get(holder + j + 1), command.subList((holder + j + 1 + adjuster2), command.size())));
                                    }
                                }
                                if (holder >= args.size() - 1) {
                                    sb.append(command.get(loc) + " ");
                                } else {
                                    if (num > 1) {
                                        sb.append(command.get((holder + adjuster1 - 1) + i) + " ");
                                    } else {
                                        sb.append(command.get(holder + adjuster1) + " ");
                                    }
                                }
                                added.add(loc);
                                loc ++;
                            }
                            args.set(holder, sb.toString().trim());
                            holder ++;
                            loc = 0;
                        }
                    }
                }
            } else {
                holder ++;
            }
        }
        loc = 0;
        holder = 0;

        
        /*
         * Now examine the varying arguments. If examined is less than the size of command
         * then we have space for varying arguments, otherwise we skip this step.
         * For each varying argument, we add arguments to args, shifting the args to the right
         * if necessary. We do this until we reach the end of command, or we reach an identifier
         * from the identifiers list.
         */
        for (String s : patternList) { //One final pass through the pattern list
            String[] split = s.split("|");
            if (split[0].equals("V")) { //Only examine varying arguments
                /*
                 * We want to find the first argument that matches the rule for the varying argument,
                 * and that has not already been added to args (since we've already examined) the rules
                 * for other arguments.
                 */
                while (loc < command.size() && (!matchesRule(command.get(loc), rulesList.get(holder), command) || added.contains(loc))) {
                    loc++;
                    if (loc >= command.size()) {
                        break;
                    }
                }
                /*
                 * Identifiers are used to "stop" the varying arguments. We want to loop through the entire
                 * command now, adding unused arguments to args and shifting if necessary. The shifting process
                 * is extremely similar as before, but since we're not adding in new arguments to our args list,
                 * we adjust the numbers a bit since we're simply modifying the existing arguments.
                 */
                while (loc < command.size() && !identifiers.contains(command.get(loc)) && !added.contains(loc)) {
                    StringBuilder sb = new StringBuilder();
                    int adjuster1 = 0; //Now adjuster keeps track of all the arguments that have been added to args
                    for (int x = 0; x <= holder; x++) {
                        adjuster1 += args.get(x).split(" ").length;
                    }
                    int adjuster2 = adjuster1 == 0 ? adjuster1 : adjuster1 - 1; //Makes sure adjuster2 doesn't go negative
                    if (holder < args.size() - 1) { //If we are not at the end, we're going to shift the rest of the args to the right
                        for (int j = 0; j < (args.size() - holder) - 1; j++) { //We shift just as before
                            adjuster2 += args.get(holder + j + 1).split(" ").length - 1;
                            args.set(holder + j + 1, shiftElementsRight(args.get(holder + j + 1), command.subList((holder + j + adjuster2 + 1), command.size())));
                        }
                    }
                    if (holder >= args.size() - 1) {
                        sb.append(command.get(loc) + " ");
                    } else {
                        sb.append(command.get(holder + adjuster1) + " ");
                    }
                    added.add(loc);
                    args.set(holder, args.get(holder) + " " + sb.toString().trim());
                    loc ++;
                }
            }
            holder ++; //Since we're not going to add any new arguments, we're always going to increment holder
        }
        
        arguments = args; //If we have made it here, the input command was valid and the parser gave us a valid output
        return args;
        
    }
    /**
     * Shifts all elements (separated by spaces) in one to the elements in follows to the right by one.
     * For example: shiftElementsRight("Hello world", ["world", "testing", "this"]) would return
     * "world testing this." Another example would be shiftElementsRight("hello", ["world"]) would
     * return "world".
     * 
     * @param one The string to shift the elements of
     * @param follows The list of elements following the string
     * @return The string with the elements shifted to the right
     */
    private String shiftElementsRight(String one, List<String> follows) {
        if (one == null || one.equals("") || follows == null || follows.isEmpty()) { //Effectively if parser reached end of command
            return one;
        } else if (one.split(" ").length == 1) { //If there is only one element in one, we just want the next element in follows
            try {
                return follows.get(1);
            } catch (IndexOutOfBoundsException e) { //Again, if the parser reached the end of the command
                return follows.get(0);
            }
        }
        String[] split = one.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) { //Go through each element in one
            if (i == follows.size()) break; //If one has more elements than follows, this ensures we don't go out of bounds
            sb.append(follows.get(i)).append(" "); //We maintain number of elements in one, adding the next element in follows
        }
        return sb.toString().trim();
    }

    /**
     * Checks if the given argument matches the given rule.
     * 
     * Valid rules are as follows:
     * - "N" if this argument must be a number
     * - "!N" if this argument must not be a number
     * - "S" if this argument must contain a string
     * - "!S" if this argument must not contain a string
     * - "T" if this argument must contain a time
     * - "X" if this argument has no rules
     * - "I" if this argument must be an identifier
     * - "E1" if this argument must is the last argument
     * - "E2" if this argument must be the second to last argument
     * 
     * Multiple rules for one argument can be specified with a | for or
     * and a & for and, however you can only pair 'and' for E rules (as E rules are mutually exclusive)
     * because there is no reason you would want, for example, an argument to be the last argument OR a number,
     * since that means anything that is a number is already valid.
     * 
     * @param arg The argument to check
     * @param rule The rule to check against
     * @return True if the argument matches the rule, false otherwise
     */
    private boolean matchesRule(String arg, String rule, List<String> command) {
        if (rule.contains("X")) return true; //X rules are always valid
        boolean returns = false; //Returns is used to keep track of whether or not the argument matches the rule
        boolean and = true; //And is used to keep track of whether or not we are using an 'and' rule

        if (rule.contains("E")) { //For "ending" arguments
            if (rule.contains("&")) and = false; //If we are using an 'and' rule, 
            String[] split = rule.split("&");
            for (String s : split) {
                if (s.contains("E")) {
                    int num = Integer.parseInt(s.substring(1));
                    if (num == 1) {
                        returns = arg.equals(command.get(command.size() - 1));
                    } else if (num == 2) {
                        returns = arg.equals(command.get(command.size() - 2));
                    }
                    if (!returns) {
                        return false; //E rules are mutually exclusive, so if we fail here, we can return false
                    }
                }
            }
        }
        /* For each rule, we only want to examine if returns isn't already true or if we are using an 'and' rule */
        if (rule.contains("N") && (!returns || !and)) { //If the arg must be a number
            try {
                Integer.parseInt(arg);
                returns = !rule.contains("!");
                if (returns) and = true;
            } catch (NumberFormatException e) {
                returns = rule.contains("!");
                if (returns) and = true;
            }
        }
        if (rule.contains("S") && (!returns || !and)) { //If the arg must have a letter
            for (int i = 0; i < arg.length(); i++) {
                if (Character.isLetter(arg.charAt(i))) {
                    returns = !rule.contains("!");
                    if (returns) and = true;
                }
            }
        }
        if (rule.contains("T") && (!returns || !and)) { //If the arg must be in a time format the timeConverter accepts
            try {
                SoapHandler.timeConverter(arg);
                returns = true;
                if (returns) and = true;
            } catch (IllegalArgumentException e) {
                returns = false;
                if (returns) and = true;
            }
        }
        if (rule.contains("I") && (!returns || !and)) { //If the arg must be an identifier
            if (identifiers.contains(arg)) {
                returns = true;
            } else {
                returns = false;
            }
            if (returns) and = true;
        }
        return (returns && and); //Both returns and and must be true for the argument to match the rule
    }

    /**
     * Returns this parser's pattern
     * 
     * @return The pattern of this parser
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets this parser's pattern
     * 
     * @param pattern The new pattern for this parser
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Returns the list of arguments parsed by this parser
     * 
     * @return The list of arguments parsed by this parser
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public List<String> getArguments() throws NullPointerException {
        if (!arguments.isEmpty()) {
            return arguments;
        } else {
            throw new NullPointerException("No command has been parsed by this parser yet.");
        }
    }

    /**
     * Returns the first argument in the parsed command that matches the given rule.
     * 
     * @param rule The rule to match against
     * @return The first argument in the parsed command that matches the given rule, or null if no argument matches the rule
     */
    public String getMatchingRule(String rule) {
        List<String> args = getArguments(); //Ensures that the arguments are parsed
        for (String s : args) {
            if (matchesRule(s, rule, args)) {
                return s;
            }
        }
        return "";
    }

    /**
     * Returns all arguments in the parsed command that match the given rule.
     * 
     * @param rule The rule to match against
     * @return All arguments in the parsed command that match the given rule, or an empty list if no arguments match the rule
     */
    public List<String> getMatchingRules(String rule) {
        List<String> args = getArguments(); //Ensures that the arguments are parsed
        List<String> returns = new ArrayList<>();
        for (String s : args) {
            if (matchesRule(s, rule, args)) {
                returns.add(s);
            }
        }
        return returns;
    }

    /**
     * Returns a list of all required arguments in the parsed command.
     * 
     * @return A list of all required arguments in the parsed command
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public List<String> getRequired() throws NullPointerException {
        List<String> args = getArguments(); //Ensures that the arguments are parsed
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
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public List<String> getOptional() throws NullPointerException {
        List<String> args = getArguments(); //Ensures that the arguments are parsed
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
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public List<String> getVariable() throws NullPointerException {
        List<String> args = getArguments(); //Ensures that the arguments are parsed
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
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public List<String> getOfSize(int size, boolean variable) throws NullPointerException {
        List<String> args = getArguments(); //Ensures that the arguments are parsed
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
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public String get(int index) throws IndexOutOfBoundsException, NullPointerException {
        return getArguments().get(index); //Ensures that the arguments are parsed
    }

    /**
     * Returns the number of arguments in the parsed command.
     * 
     * @return The number of arguments in the parsed command
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public int numArgs() throws NullPointerException {
        return getArguments().size(); //Ensures that the arguments are parsed
    }

    /**
     * Returns the arguments at the given index range in the parsed command.
     * 
     * @param index1 The index of the first argument to return (inclusive)
     * @param index2 The index of the last argument to return (exclusive)
     * @return The arguments at the given index range in the parsed command
     * @throws IndexOutOfBoundsException If the index is out of range
     * @throws NullPointerException If no command has been parsed by this parser yet
     */
    public List<String> getCombined(int index1, int index2) throws IndexOutOfBoundsException, NullPointerException {
        List<String> args = getArguments(); //Ensures that the arguments are parsed
        return args.subList(index1, index2);
    }

    /**
     * Inserts an argument at the given index in this parser's arguments list.
     * 
     * @param arg
     * @param index
     * @throws IndexOutOfBoundsException If the index is out of range
     */
    public void insert(String arg, int index) throws IndexOutOfBoundsException {
        arguments.add(index, arg);
    }
}
