package com.georgster.util.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.georgster.util.commands.exception.CommandParserException;
import com.georgster.util.commands.exception.ParseInputException;
import com.georgster.util.commands.exception.ParserFormatException;
import com.georgster.ParseableCommand;

/**
 * Parses a String-Based input and formats a {@link ParsedArguments} for a {@link ParseableCommand}.
 * <p>
 * Based on a required pattern and optional rules and/or identifiers, a {@link CommandParser} will break
 * an input up into a {@link List} of "arguments". The pattern dictates the structure for how the arguments are divided,
 * while the rules and identifiers assist in the accuracy of each argument. 
 * <p>
 * <h3>Pattern:</h3>
 * <p>
 * The Provided pattern is an sequence of argument definers in the following format:
 * <p>
 * <i>NUMWORDSREQUIRED</i>
 * <p>
 * Where NUMWORDS is the number of individual words that argument will make up, and REQUIRED is whether or not that argument is required to parse.
 * These are represented by the following values:
 * <ul>
 * <li>NUMWORDS: Either "V" for a variable amount of words, or a number 1-9 for a static amount of words</li>
 * <li>REQUIRED: "R" for required, "O" for optional
 * </ul>
 * Some example patterns are:
 * <ul>
 * <li>"1R", "VO" - A single required word followed by a varying amount of words</li>
 * <li>"V|R", "1O", "2R" - A varying required amount of words following a single optional word and two required words</li>
 * <li>"1O" - A single optional argument (means the parser will not throw exception on empty parse inputs)</li>
 * </ul>
 * With our example pattern of "1R", "VO", an input of "testing the command parser" would result in "testing", "the command parser".
 * The same input with the pattern "1O" would only return "testing".
 * <p>
 * <h3>Rules:</h3>
 * <p>
 * Rules are ways to specify how each "argument" should be before it is assigned. See {@link #setRules(String...)} for available rules.
 * <p>
 * If rules are assigned, rules must be assigned for every argument in the parser's pattern. If the parser should format three arguments (example: "1R", "2O", "VO"),
 * three rules must be provided.
 * <p>
 * An example rule would look like "FT" meaning this argument must be first and a time, or "D" simply meaning it must be a date.
 * Rules can be combined for "and" functionality, meaning illogical rule combinations should be avoided.
 * <p>
 * Rules generally assist the parser when a pattern has variable arguments or when a specific order for arguments is desired.
 * Consider a pattern of "VR", "1O", "1O" and rules of "X", "T", "D". An input of "future event name" would simply return "future event name"
 * since the rules for the optional arguments were not met (there's no time nor date present). However, an input of "future event 9pm dec15" would
 * return "future event", "9pm", "dec15".
 * <p>
 * <h3>Identifiers</h3>
 * Identifiers assist the command parser for knowing when to stop including words for variable arguments ("V" in pattern).
 * For example, a pattern of "VR", "VR" and an input of "testing the command parser" would return "testing the command", "parser",
 * but with the identifier "command", it would now return "testing the", "command parser".
 * <p>
 * If no variable arguments are present, identifiers are not necessary.
 * @see {@link #setRules(String...)}
 */
public class CommandParser {
    private static final List<String> AVAILABLE_RULES = List.of("X", "N", "T", "D", "Z", "C", "S", "F", "L", "M", "I", "<", ">");

    private boolean autoFormat; //Whether or not the parser should automatically format the arguments
    private List<String> rulesList; //A list of the rules split by spaces
    private List<String> patternList; //A list of the pattern split by spaces
    private List<String> identifiers; //A list of the identifiers

    /**
     * A context for an ongoing {@link CommandParser#parse(String)}.
     */
    protected class ParseContext {
        private String inputString;
        private List<String> currentArgs;
        private List<String> inputWords;

        /**
         * Creates a ParseContext from the provided input String.
         * 
         * @param input The string given to parse.
         */
        protected ParseContext(String input) {
            this.inputString = input;
            this.currentArgs = new ArrayList<>();
            this.inputWords = new ArrayList<>(List.of(inputString.split(" ")));
            for (int i = 0; i < patternList.size(); i++) {
                currentArgs.add(" "); // Ensures there are elements at each index when assigning to prevent oob
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
         * Assigns the {@code arg} to the argument at the provided {@code index}.
         * 
         * @param arg The argument to assign.
         * @param index The index to assign to.
         */
        protected void assign(String arg, int index) {
            currentArgs.set(index, arg);
        }

        /**
         * Returns the currently assigned arguments in this context.
         * 
         * @return The currently assigned arguments in this context.
         */
        protected List<String> getCurrentArgs() {
            return currentArgs;
        }

        /**
         * Returns the list of input "words" in this context.
         * 
         * @return The input "words" in this context.
         */
        protected List<String> getInputWords() {
            return inputWords;
        }

        /**
         * Returns if the {@code word} is part of an assigned argument in any way.
         * 
         * @param word The word to check.
         * @return True if the word is part of an assigned argument, false otherwise.
         */
        protected boolean isPartOfAssigned(String word) {
            for (String arg : currentArgs) {
                if (arg.contains(word)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns if the provided {@code word} is assigned to an argument.
         * 
         * @return True if the provided {@code word} is assigned to an argument, false otherwise.
         */
        protected boolean isAssigned(String word) {
            return currentArgs.contains(word);
        }

        /**
         * Returns if the argument at the provided index has been assigned.
         * 
         * @param index The index to check for.
         * @return True if it has been assigned, false otherwise.
         */
        protected boolean isFilled(int index) {
            return !currentArgs.get(index).equals(" ");
        }

        /**
         * Returns the input string for this context.
         * 
         * @return The input String for this context.
         */
        protected String getInputString() {
            return inputString;
        }

        /**
         * Resets and unassigns the argument at the provided index.
         * 
         * @param index The index to reset.
         */
        protected void reset(int index) {
            currentArgs.set(index, " ");
        }

        /**
         * Finishes and cleans up the arguments in this context.
         */
        protected void finish() {
            currentArgs.removeIf(arg -> arg.equals(" "));
        }

        /**
         * Returns how many "words" are in the arguments from the starting to the ending index.
         * 
         * @param startIndex The index to start counting from (inclusive).
         * @param endIndex The index to stop counting at (exclusive).
         * @return The number of "words" in arguments in the index range.
         */
        protected int getWordCountBetween(int startIndex, int endIndex) {
            int count = 0;
            for (int i = startIndex; i < endIndex; i++) {
                count += currentArgs.get(i).split(" ").length;
            }
            return count;
        }

        /**
         * Returns a String composed of the "words" in the input words list from the starting to ending index.
         * @param startIndex The index to start adding from (inclusive).
         * @param endIndex The index to stop adding at (exclusive).
         * @return A String of the words in the range combined (separated by spaces).
         */
        protected String getCombined(int start, int end) {
            StringBuilder output = new StringBuilder();
            for (int i = start; i < end; i++) {
                output.append(inputWords.get(i) + " ");
            }
            return output.toString().trim();
        }
    }

    /**
     * Creates a new {@link CommandParser} from the provided pattern.
     * 
     * @param pattern The pattern for the parser to use when parsing.
     * @throws CommandParserException If the pattern is in an invalid format.
     * @see {@link CommandParser} For pattern rules
     */
    public CommandParser(String... pattern) throws CommandParserException {
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
     * Validates a {@link CommandParser} pattern, throwing a {@link ParserFormatException} with a descriptive message if invalid.
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
     * Validates a {@link CommandParser} rules, throwing a {@link ParserFormatException} with a descriptive message if invalid.
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
     * Sets the argument rules for this {@link CommandParser}.
     * <p>
     * Rules can be combined for "and" functionality. Combined rules should simply all be together in a single String.
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

    /**
     * Returns if the {@code arg} matches the {@code rule} for given the provided {@link ParseContext}.
     * 
     * @param arg The arg to verify rules for.
     * @param rule The rule to verify.
     * @param currentParse The current parse context.
     * @return True if the rule matches, false otherwise
     */
    private boolean matchesRule(String arg, String rule, ParseContext currentParse) {
        if (rule.contains("X")) {
            return true;
        }

        if (rule.contains("N") && !ParserRulesAdvice.isNumber(arg)) {
            return false;
        }

        if (rule.contains("T") && !ParserRulesAdvice.isTime(arg)) {
            return false;
        }

        if (rule.contains("D") && !ParserRulesAdvice.isDate(arg)) {
            return false;
        }

        if (rule.contains("Z") && !ParserRulesAdvice.isFutureTimeIncrement(arg)) {
            return false;
        }

        if (rule.contains("C") && !ParserRulesAdvice.hasCharacter(arg)) {
            return false;
        }

        if (rule.contains("S") && !ParserRulesAdvice.isOnlyCharacters(arg)) {
            return false;
        }

        if (rule.contains("F") && !ParserRulesAdvice.isFirst(arg, currentParse)) {
            return false;
        }

        if (rule.contains("L") && !ParserRulesAdvice.isLast(arg, currentParse)) {
            return false;
        }

        if (rule.contains("M") && !ParserRulesAdvice.isMiddle(arg, currentParse)) {
            return false;
        }

        if (rule.contains("I") && !isIdentifier(arg)) {
            return false;
        }

        if (rule.contains("<") && !ParserRulesAdvice.isBeforeIdentifier(arg, currentParse, identifiers)) {
            return false;
        }

        if (rule.contains(">") && !ParserRulesAdvice.isAfterIdentifier(arg, currentParse, identifiers)) {
            return false;
        }

        return true;
    }

    /**
     * Adds identifiers to this parser.
     * 
     * @param identifiers The identifiers to add.
     * @see {@link CommandParser} for identifier information.
     */
    public void addIdentifiers(String... identifiers) {
        for (String identifier : identifiers) {
            this.identifiers.add(identifier);
        }
    }

    /**
     * Returns the extracted String representing the "count" part of an argument pattern.
     * 
     * @param argPattern The pattern to extract from.
     * @return The "count" part of an argument pattern.
     */
    private String getPatternCount(String argPattern) {
        return argPattern.substring(0, 1);
    }

    /**
     * Returns the extracted String representing the "required" part of an argument pattern.
     * 
     * @param argPattern The pattern to extract from.
     * @return The "required" part of an argument pattern.
     */
    private String getPatternRequired(String argPattern) {
        return argPattern.substring(1, 2);
    }

    /**
     * Returns true if this parser has at least one required argument in its pattern, false otherwise.
     * 
     * @return True if this parser has at least one required argument in its pattern, false otherwise.
     */
    private boolean hasRequiredArg() {
        for (String pattern : patternList) {
            if (pattern.contains("R")) {
                return true;
            }
        }
        return false;
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
     * @param input The String-based input to parse.
     * @return A {@link ParsedArguments} containing the output and various utility for it.
     */
    public ParsedArguments parse(String input) throws CommandParserException {
        if (input.contains(" ")) {
            input = input.substring(input.indexOf(' ') + 1);
        } else {
            input = "";
        }

        if (input.isEmpty()) {
            if (!hasRequiredArg()) {
                return new ParsedArguments(new ArrayList<>(), this);
            } else {
                throw new ParseInputException(input, this);
            }
        }

        if (autoFormat) {
            input = input.toLowerCase().trim();
        }

        if (rulesList.isEmpty()) {
            patternList.forEach(s -> rulesList.add("X"));
        }

        ParseContext currentParse = new ParseContext(input);

        parseRequired(currentParse);
        parseOptional(currentParse);
        fillAllVariableArgs(currentParse);

        currentParse.finish();

        return new ParsedArguments(currentParse.getCurrentArgs(), this);
    }

    /**
     * Parses the required arguments of the parser and returns the resulting
     * {@link ParseContext}. If some required arguments cannot be assigned, a
     * {@link CommandParserException} is thrown.
     * 
     * @param currentParse The current parse conrtext.
     * @return The resulting parse context.
     * @throws CommandParserException if a required argument couldn't be parsed.
     */
    private ParseContext parseRequired(ParseContext currentParse) throws CommandParserException {
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
                            if (currentParse.isAssigned(checkedArg)) {
                                if (reassign(currentParse.getCurrentArgs().indexOf(checkedArg), currentParse)) {
                                    break;
                                } else {
                                    throw new ParseInputException(currentParse.getInputString(), this);
                                }
                            } else {
                                currentParse.assign(checkedArg, j);
                                break;
                            }
                        } else if (i == (all.size() - 1)) {
                            throw new ParseInputException(currentParse.getInputString(), this);
                        }
                    } catch (Exception e) {
                        throw new ParseInputException(currentParse.getInputString(), this);
                    }
                }
            }
        }
        return currentParse;
    }

    /**
     * Attempts to reassign the argument at the provided index to a new argument index, if possible
     * based on the given {@link ParseContext}, returning true if successful and false otherwise.
     * 
     * @param index The index to reassign.
     * @param currentParse The context of the current parse.
     * @return True if successful and false otherwise.
     */
    private boolean reassign(int index, ParseContext currentParse) {
        String currentArg = currentParse.getCurrentArgs().get(index);
        int requiredSpaces = currentArg.split(" ").length;

        for (int i = 0; i < patternList.size(); i++) {
            String associatedRule = rulesList.get(i);
            String count = getPatternCount(patternList.get(i));
            int ruleCount = 0;

            if (count.equals("V")) {
                ruleCount = 1;
            } else {
                ruleCount = Integer.parseInt(count);
            }

            if (ruleCount == requiredSpaces && i != index && matchesRule(currentArg, associatedRule, currentParse)) {
                if (currentParse.isFilled(i)) {
                    return reassign(i, currentParse);
                } else {
                    currentParse.assign(currentArg, i);
                    currentParse.reset(index);
                    return fill(index, currentArg.split(" ").length, currentParse);
                }
            }
        }

        return false;
    }

    /**
     * Attempts to fill the argument at the provided index with a different argument, if possible
     * based on the given {@link ParseContext}, returning true if successful and false otherwise.
     * 
     * @param index The index to fill.
     * @param currentParse The context of the current parse.
     * @return True if successful and false otherwise.
     */
    private boolean fill(int index, int requiredSpaces, ParseContext currentParse) {
        List<String> all = currentParse.getInputWords();

        for (int i = 0; i < all.size(); i++) {
            String checkedArg = currentParse.getCombined(i, i + requiredSpaces);

            if (!currentParse.isAssigned(checkedArg) && matchesRule(checkedArg, rulesList.get(index), currentParse)) {
                currentParse.assign(checkedArg, index);
                return true;
            }
        }

        return false;
    }

    /**
     * Parses the optional arguments of the parser and returns the resulting
     * {@link ParseContext}.
     * 
     * @param currentParse The current parse conrtext.
     * @return The resulting parse context.
     */
    private ParseContext parseOptional(ParseContext currentParse) {
        List<String> all = currentParse.getInputWords();

        for (int j = 0; j < patternList.size(); j++) {
            String argPattern = patternList.get(j);
            String associatedRule = rulesList.get(j);

            String count = getPatternCount(argPattern);
            String required = getPatternRequired(argPattern);

            if (required.equals("O")) {
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
                            if (currentParse.isAssigned(checkedArg)) {
                                if (reassign(currentParse.getCurrentArgs().indexOf(checkedArg), currentParse)) {
                                    break;
                                }
                            } else {
                                currentParse.assign(checkedArg, j);

                                break;
                            }
                        } else if (i == (all.size() - 1)) {
                            break;
                        }
                    } catch (Exception e) {
                        return currentParse;
                    }
                }
            }
        }
        return currentParse;
    }

    /**
     * Fills all variable arguments with unassigned words, given they are in sequence and
     * possible to fill based on the given {@link ParseContext} and returns the resulting {@link ParseContext}.
     * 
     * @param currentParse The current parse conrtext.
     * @return The resulting parse context.
     */
    private ParseContext fillAllVariableArgs(ParseContext currentParse) {
        List<String> all = currentParse.getInputWords();
        List<String> current = currentParse.getCurrentArgs();

        for (int i = 0; i < patternList.size(); i++) {
            if (i >= all.size()) { // Edge case where variable optional arguments in the latter half of a pattern would cause oob on short inputs
                return currentParse;
            }
            String argPattern = patternList.get(i);
            String associatedRule = rulesList.get(i);

            String count = getPatternCount(argPattern);
            int startingIndex = -1;
            if (currentParse.isFilled(i)) {
                startingIndex = all.indexOf(current.get(i));
            } else {
                startingIndex = currentParse.getWordCountBetween(0, i + 1);
            }

            if (count.equals("V")) {
                int x = 2;
                for (int j = startingIndex; j < all.size() - 1; j++) {
                    String word = all.get(j + 1);

                    if (!currentParse.isPartOfAssigned(word) && !isIdentifier(word)) {
                        String checkedArg = currentParse.getCombined(startingIndex, startingIndex + x);
                        if (matchesRule(checkedArg, associatedRule, currentParse)) {
                            currentParse.assign(checkedArg, i);
                        }
                    } else {
                        break;
                    }

                    x++;
                }
            }
        }

        return currentParse;
    }

    /**
     * Returns true if the {@code word} is an identifier, false otherwise.
     * 
     * @param word The word to check.
     * @return true if the {@code word} is an identifier, false otherwise.
     */
    private boolean isIdentifier(String word) {
        return identifiers.contains(word);
    }

    /**
     * Returns the pattern of this parser as a list.
     * 
     * @return The pattern of this parser as a list.
     */
    public List<String> getPattern() {
        return patternList;
    }

    /**
     * Returns the rules of this parser as a list.
     * 
     * @return The rules of this parser as a list.
     */
    public List<String> getRules() {
        return rulesList;
    }

    /**
     * Disables this parser's default auto-formatting when parsing.
     */
    public void disableAutoFormatting() {
        this.autoFormat = false;
    }
}
