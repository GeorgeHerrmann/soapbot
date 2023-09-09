package com.georgster.util.commands;

import java.util.ArrayList;
import java.util.List;

import com.georgster.util.SoapUtility;
import com.georgster.util.commands.exception.CommandParserException;
import com.georgster.util.commands.exception.ParseInputException;
import com.georgster.util.commands.exception.ParserFormatException;

public class NewCommandParser {
    private static final List<String> AVAILABLE_RULES = List.of("X", "N", "T", "D", "Z", "C", "S", "F", "L", "M", "I", "<", ">");

    private boolean autoFormat; //Whether or not the parser should automatically format the arguments
    private List<String> rulesList; //A list of the rules split by spaces
    private List<String> patternList; //A list of the pattern split by spaces
    private List<String> identifiers; //A list of the identifiers

    protected class IntermediateParse {
        private String inputString;
        private List<String> currentArgs;
        private List<String> inputWords;

        protected IntermediateParse(String input) {
            this.inputString = input;
            this.currentArgs = new ArrayList<>();
            this.inputWords = List.of(input.split(" "));
        }

        protected void assign(String arg, int index) {
            currentArgs.set(index, arg);
        }

        protected List<String> getCurrentArgs() {
            return currentArgs;
        }

        protected List<String> getInputWords() {
            return inputWords;
        }

        protected boolean isAssigned(String word) {
            return currentArgs.contains(word);
        }

        protected String getInputString() {
            return inputString;
        }

        protected String getCombined(int start, int end) {
            StringBuilder output = new StringBuilder();
            for (int i = start; i < end; i++) {
                output.append(inputWords.get(i));
            }
            return output.toString();
        }
    }

    public NewCommandParser(String... pattern) throws CommandParserException {
        this.patternList = new ArrayList<>();

        validatePattern(pattern);
        for (String argDescriptor : pattern) {
            this.patternList.add(argDescriptor.toUpperCase());
        }

        this.rulesList = new ArrayList<>();
        this.identifiers = new ArrayList<>();
        this.autoFormat = true;
    }

    /**
     * Validates a {@link NewCommandParser} pattern, throwing a {@link ParserFormatException} with a descriptive message if invalid.
     * 
     * @param pattern The pattern to validate.
     * @throws ParserFormatException A {@link CommandParserException} with a descriptive message if the pattern is in an invalid format.
     */
    private void validatePattern(String... pattern) throws ParserFormatException {
        if (pattern.length < 1) {
            throw new ParserFormatException("Pattern length must be greater than 1");
        }

        for (String argDescriptor : pattern) {
            if (argDescriptor.length() > 2) {
                throw new ParserFormatException("Argument Descriptor '" + argDescriptor + "'' may not have more than one count descriptor and one requirement descriptor.");
            }
            try {
                String argCount = argDescriptor.substring(0, 1);
                String requirementStatus = argDescriptor.substring(1, 2);

                if (!((argCount.equalsIgnoreCase("V") || Integer.parseInt(argCount) > 0) && (requirementStatus.equalsIgnoreCase("O") || requirementStatus.equalsIgnoreCase("R")))) {
                    throw new ParserFormatException(pattern);
                }
            } catch (Exception e) {
                throw new ParserFormatException(pattern);
            }
        }
    }

    /**
     * Validates a {@link NewCommandParser} rules, throwing a {@link ParserFormatException} with a descriptive message if invalid.
     * 
     * @param rules The rules to validate.
     * @throws ParserFormatException A {@link CommandParserException} with a descriptive message if the rules are in an invalid format.
     */
    private void validateRules(String... rules) throws ParserFormatException {
        if (rules.length < 1 || rules.length != patternList.size()) {
            throw new ParserFormatException("Rules must be provided for all arguments for the pattern for this parser. This parser has " + patternList.size() + " patterned arguments, with " + rules.length + " rules provided.");
        }

        for (String rule : rules) {
            try {
                if (!AVAILABLE_RULES.contains(rule)) {
                    throw new ParserFormatException("Rule " + rule + "is not a valid rule. Available rules are: " + AVAILABLE_RULES.toString() + "."); 
                }
            } catch (Exception e) {
                throw new ParserFormatException(rules);
            }
        }
    }

    /**
     * Sets the argument rules for this {@link NewCommandParser}.
     * <p>
     * Valid rules are:
     * <ul>
     * <li>"X" - No Rule</li>
     * <li>"N" - Must be a number</li>
     * <li>"T" - Must be a time</li>
     * <li>"D" - Must be a date</li>
     * <li>"Z" - Must be a future time increment</li>
     * <li>"C" - Must contain a character</li>
     * <li>"S" - Must be only characters</li>
     * <li>"F" - Must be the first argument</li>
     * <li>"L" - Must be the last argument</li>
     * <li>"M" - Must be in the middle (not first nor last)</li>
     * <li>"I" - Must be an identifier</li>
     * <li>"<" - Must come directly before an identifier</li>
     * <li>">" - Must come directly after an identifier</li>
     * </ul>
     * 
     * @param rules The rules for this parser.
     * @throws CommandParserException If the rules are in an invalid format or an invalid rule is input.
     */
    public void setRules(String... rules) throws CommandParserException {
        validateRules(rules);

        rulesList = new ArrayList<>();

        for (String rule : rules) {
            this.rulesList.add(rule.toUpperCase());
        }
    }

    private boolean matchesRule(String arg, String rule, IntermediateParse currentParse) {
        if (rule.contains("X")) {
            return true;
        }

        List<String> all = currentParse.getInputWords();

        if (rule.contains("N")) {
            try {
                Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (rule.contains("T")) {
            try {
                SoapUtility.timeConverter(arg);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        if (rule.contains("D")) {
            try {
                SoapUtility.convertDate(arg);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        if (rule.contains("Z")) {
            try {
                SoapUtility.calculateFutureDateTime(arg);
            } catch (Exception e) {
                return false;
            }
        }

        if (rule.contains("C")) {
            boolean passed = false;
            for (int i = 0; i < arg.length(); i++) {
                if (Character.isLetter(arg.charAt(i))) {
                    passed = true;
                    break;
                }
            }
            if (!passed) {
                return false;
            }
        }

        if (rule.contains("S")) {
            for (int i = 0; i < arg.length(); i++) {
                if (!Character.isLetter(arg.charAt(i))) {
                    return false;
                }
            }
        }

        if (rule.contains("F")) {
            if (all.indexOf(arg.split(" ")[0]) != 0) {
                return false;
            }
        }

        if (rule.contains("L")) {
            if (all.indexOf(arg.split(" ")[0]) != (all.size() - 1)) {
                return false;
            }
        }

        if (rule.contains("M")) {
            int argIndex = all.indexOf(arg.split(" ")[0]);
            if (argIndex == 0 || argIndex == (all.size() - 1)) {
                return false;
            }
        }

        if (rule.contains("I")) {
            if (!identifiers.contains(arg)) {
                return false;
            }
        }

        if (rule.contains("<")) {
            try {
                String[] argWords = arg.split(" ");
                int argIndex = all.indexOf(argWords[0]);
                int increment = argWords.length;
                if (!identifiers.contains(all.get(argIndex + increment))) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (rule.contains(">")) {
            try {
                int argIndex = all.indexOf(arg.split(" ")[0]);
                if (!identifiers.contains(all.get(argIndex - 1))) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    public void addIdentifiers(String... identifiers) {
        for (String identifier : identifiers) {
            this.identifiers.add(identifier);
        }
    }

    private String getPatternCount(String argPattern) {
        return argPattern.substring(0, 1);
    }

    private String getPatternRequired(String argPattern) {
        return argPattern.substring(1, 2);
    }

    /**
     * Parses the given input string based on this parser's pattern, rules and identifiers.
     * The first arguments which match this parser's configuration will be returned.
     * <p>
     * <b>Note:</b> If this parser's pattern has no required arguments, and {@code input} is
     * empty, the resulting {@link ParsedArguments} will have an empty argument List, but no
     * {@link CommandParserException} will be thrown, as no pattern or rules were violated.
     * <p>
     * If this parser's pattern has required arguments or rules which are violated during parsing,
     * a {@link CommandParserException} will be thrown.
     * 
     * @param input
     * @return
     */
    public ParsedArguments parse(String input) throws CommandParserException {
        if (!patternList.contains("R") && input.isEmpty()) {
            return new ParsedArguments(new ArrayList<>());
        }

        if (autoFormat) {
            input = input.toLowerCase().trim();
        }
    }

    private IntermediateParse parseRequired(IntermediateParse currentParse) {
        List<String> current = currentParse.getCurrentArgs();
        List<String> all = currentParse.getInputWords();

        for (int j = 0; j < patternList.size(); j++) {
            String argPattern = patternList.get(j);
            String associatedRule = rulesList.get(j);

            String count = getPatternCount(argPattern);
            String required = getPatternRequired(argPattern);

            if (required.equals("R")) {
                for (int i = 0; i < all.size(); i++) {
                    int requiredSpaces = 0;
                    if (count.equals("V")) {
                        requiredSpaces = 1;
                    } else {
                        requiredSpaces = Integer.parseInt(count);
                    }

                    try {
                        String checkedArg = currentParse.getCombined(i, i + requiredSpaces);
                        if (matchesRule(checkedArg, associatedRule, currentParse)) {
                            currentParse.assign(checkedArg, j);
                        } else if (i == (all.size() - 1)) {
                            throw new ParseInputException(currentParse.getInputString(), this);
                        }
                    } catch (Exception e) {
                        throw new ParseInputException(currentParse.getInputString(), this);
                    }
                }
            }
        }
    }

    private IntermediateParse fillRequiredVariableArgs(IntermediateParse currentParse) {

    }

    private IntermediateParse parseOptional(IntermediateParse currentParse) {

    }

    private IntermediateParse fillAllVariableArgs(IntermediateParse currentParse) {

    }

    private IntermediateParse reorderIfNecessary(IntermediateParse currentParse) {

    }

    private boolean matchesRules(IntermediateParse currentParse) {

    }

    private boolean checkForIdentifiers(IntermediateParse currentParse) {

    }

    public String getPattern() {
        return pattern;
    }

    public List<String> getRules() {
        return rulesList;
    }
}
