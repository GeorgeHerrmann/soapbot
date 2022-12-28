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
 * A pattern that has a varying number of arguments and an optional argument may
 * not always return the correct list of arguments, unless specific argument identifiers
 * are set using the addIdentifiers() method.
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
    private String pattern;
    private List<String> rulesList;
    private List<String> patternList;
    private List<String> identifiers;

    /**
     * Creates a new CommandParser with the given pattern.
     * A pattern is a string that defines the arguments of a command, and
     * must be in the format <num1>|<req> <num2>|<req> ... <numn>|<req> where
     * num is the number of arguments that arg takes and req is whether or not
     * the argument is required.
     * 
     * @param pattern The pattern to use for parsing commands.
     */
    public CommandParser(String pattern) throws IllegalArgumentException {
        this.pattern = pattern;
        identifiers = new ArrayList<>();
        rulesList = new ArrayList<>();
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
     * NOTE: Adding rules and/or identifiersis only necessary if you have a varying amount of 
     * arguments, otherwise the parser will automatically correctly parse the command.
     * @param identifiers The identifiers to use for parsing commands.
     */
    public void addIdentifiers(String... identifiers) {
        for (String s : identifiers) {
            this.identifiers.add(s);
        }
    }

    /**
     * Adds rules to the parser. Rules are strings that define the format for the arguments
     * of a command. If a rule is set using this method, a rule for every argument in the
     * pattern must be set. For example: if the pattern is "V|R 1|R", then two rules must be set.
     * For optional arguments, the parser will not throw an exception if the argument does not
     * match the rule, but will simply ignore it. However, if the argument is required and
     * does not match the rule, the parser will throw an exception. Multiple rules for
     * one argument can be specified with a |.
     * 
     * Valid rules are as follows:
     * - "N" if this argument must contain a number
     * - "!N" if this argument must not contain a number
     * - "S" if this argument must contain a string
     * - "!S" if this argument must not contain a string
     * - "T" if this argument must contain a time
     * - "X" if this argument has no rules
     * 
     * For example: the reserve command is in the format V|R 1|O 1|O. The varying
     * argument has no rules, however both the optional argument must contain a number.
     * Therefore, the rules would be "X N N". With these rules in place, a parse() of
     * "reserve testing parser 2" would return ["testing parser", "2"]. If no rules
     * were set, the parse() would return "testing" "parser" "2".
     * 
     * NOTE: Adding rules and/or identifiersis only necessary if you have a varying amount of 
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
                if (!s.equals("N") && !s.equals("!N") && !s.equals("S") && !s.equals("!S") && !s.equals("X")) {
                    throw new IllegalArgumentException("Rules must be in the format N, !N, S, !S, or X.");
                } else {
                    rulesList.add(s);
                }
            }
        }
    }

    /**
     * Parses the given input and returns a list of arguments that match the pattern of
     * this CommandParser. If the input does not match the pattern, an IllegalArgumentException
     * is thrown. If this parser has rules and/or identifiers set, they are used to help
     * accurately parse the input, otherwise the parser will parse in this order:
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
        command.remove(0);
        List<String> args = new ArrayList<>();
        if (rulesList.isEmpty()) {
            for (int i = 0; i < patternList.size(); i++) {
                rulesList.add("X");
            }
        }
        int examined = 0;
        int loc = 0;
        int holder = 0;
        /*
         * First examine only required arguments, varying arguments get one spot here 
         * If its a number it gets that many spots, if its a V it gets one spot. Examined
         * is the number of arguments in command that have been examined. Loc is the
         * index in command that is being examined. Holder is the index in args that
         * the argument is being added to. If there is not enough room for required arguments
         * we throw an IllegalArgumentException.
         */
        for (String s : patternList) {
            String[] split = s.split("|");
            if (split[2].equals("R")) {
                if (split[0].equals("V")) {
                    args.add(holder, command.get(loc));
                    examined ++;
                    holder ++;
                    loc ++;
                } else {
                    int num = Integer.parseInt(split[0]);
                    if (command.size() - examined < num) {
                        throw new IllegalArgumentException("Not enough arguments for required arguments.");
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < num; i++) {
                        sb.append(command.get(loc + i)).append(" ");
                    }
                    args.add(holder, sb.toString().trim());
                    examined += num;
                    loc += num;
                    holder ++;
                }
            }
        }
        loc = 0;
        holder = 0;
        /*
         * Now examine optional arguments. If examined is less than the size of command
         * then we have space for optional arguments, otherwise we skip this step.
         * If the argument is a number, we add that many arguments to args.
         * If the argument is a V, we add one argument to args. If the argument in command
         * we are examining matches the current spot in args, we must shift the rest of args
         * to the right by one, and add the argument to the current spot. If the argument
         * does not match the current spot in args, we add it to the next spot in args.
         */
        for (String s : patternList) {
            if (examined < command.size()) {
                String[] split = s.split("|");
                if (split[2].equals("O")) {
                    if (split[0].equals("V")) {
                        try {
                            if (loc < command.size()) {
                                int x = loc;
                                for (int i = holder; i < args.size(); i++) {
                                    args.set(i, shiftElementsRight(args.get(i), command.subList(x + 1, command.size())));
                                    x++;
                                }
                                args.add(holder, command.get(loc));
                            } else {
                                args.add(holder, command.get(loc));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            args.add(holder, command.get(loc));
                        }
                        holder ++;
                        loc ++;
                        examined ++;
                    } else {
                        int num = Integer.parseInt(split[0]);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < num; i++) {
                            sb.append(command.get(loc + i)).append(" ");
                        }
                        try {
                            if (loc < command.size()) {
                                int x = loc;
                                for (int i = holder; i < args.size(); i++) {
                                    args.set(i, shiftElementsRight(args.get(i), command.subList(x + 1, command.size())));
                                    x++;
                                }
                                args.add(holder, command.get(loc));
                            } else {
                                args.add(holder, sb.toString().trim());
                            }
                        } catch (IndexOutOfBoundsException e) {
                            args.add(holder, sb.toString().trim());
                        }
                        holder ++;
                        loc += num;
                        examined += num;
                    }
                } else {
                    loc += args.get(holder).split(" ").length; //We must take into account the required argument already examined
                    holder ++;
                }
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
        for (String s : patternList) {
            if (examined < command.size()) {
                String[] split = s.split("|");
                if (split[0].equals("V")) {
                        while (examined < command.size() && !identifiers.contains(command.get(loc))) {
                            int x = loc;
                            List<String> temp = Arrays.asList(args.get(holder).split(" "));
                            String leftover = temp.get(0);
                            for (int i = holder; i < args.size(); i++) { //Shift all following elements to the right
                                temp = Arrays.asList(args.get(i).split(" "));
                                if (temp.size() > 1) {
                                    temp = new ArrayList<>(temp.subList(1, temp.size()));
                                    temp.addAll(command.subList(x + 1, command.size()));
                                    args.set(i, shiftElementsRight(args.get(i), temp));
                                } else {
                                    args.set(i, shiftElementsRight(args.get(i), command.subList(x + 1, command.size())));
                                }
                                x++;
                            }
                            temp = Arrays.asList(args.get(holder).split(" "));
                            if (temp.size() > 1) {
                                args.set(holder, leftover + " " + args.get(holder)); //Add the new element to the current spot
                            } else {
                                args.set(holder, command.get(loc) + " " + args.get(holder)); //Add the new element to the current spot
                            }
                            loc ++;
                            examined++;
                        }
                        holder ++;
                } else {
                    loc += args.get(holder).split(" ").length; //We must take into account the required argument already examined
                    holder ++;
                }
            }
        }
        return args;
        
    }
    /**
     * Shifts all elements (separated by spaces) in one to the elements in follows to the right by one.
     * For example: shiftElementsRight("Hello world", ["world", "testing", "this"]) would return
     * "world testing this." Another example would be shiftElementsRight("hello", ["world"]) would
     * return "world". This method should only be called from parse if there are extra spots available in args,
     * as it will shift all elements in one to the right without checking if there is enough room.
     * 
     * @param one The string to shift the elements of
     * @param follows The list of elements following the string
     * @return The string with the elements shifted to the right
     */
    private String shiftElementsRight(String one, List<String> follows) {
        if (one == null || one.equals("") || follows == null || follows.isEmpty()) {
            return one;
        }
        String[] split = one.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            if (i == follows.size()) break;
            sb.append(follows.get(i)).append(" ");
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
     * Multiple rules for
     * one argument can be specified with a |.
     * 
     * @param arg The argument to check
     * @param rule The rule to check against
     * @return True if the argument matches the rule, false otherwise
     */
    private boolean matchesRule(String arg, String rule) {
        if (rule.contains("X")) return true;
        boolean returns = false;
        if (rule.contains("N") && !returns) {
            try {
                Integer.parseInt(arg);
                returns = !rule.contains("!");
            } catch (NumberFormatException e) {
                returns = rule.contains("!");
            }
        } if (rule.contains("S") && !returns) {
            for (int i = 0; i < arg.length(); i++) {
                if (Character.isLetter(arg.charAt(i))) {
                    returns = !rule.contains("!");
                }
            }
        } if (rule.contains("T") && !returns) {
            try {
                SoapHandler.timeConverter(arg);
                returns = true;
            } catch (IllegalArgumentException e) {
                returns = false;
            }
        }
        return returns;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }


}
