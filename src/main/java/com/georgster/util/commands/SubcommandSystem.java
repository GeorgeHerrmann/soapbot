package com.georgster.util.commands;

import java.util.List;
import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;

/**
 * A facade for commands to create argument-based subcommands with a {@link CommandParser}.
 */
public class SubcommandSystem {
    private final CommandParser parser;
    private boolean hasExecuted;

    /**
     * Creates a SubcommandSystem which will use the {@link CommandParser} in the {@link CommandExecutionEvent}.
     * 
     * @param event The event that prompted this system's creation.
     */
    public SubcommandSystem(CommandExecutionEvent event) {
        this.parser = event.getCommandParser();
        this.hasExecuted = false;
    }

    /**
     * Subscribes logic to this system if any parsed argument matches any of the {@code arguments}.
     * The {@link Consumer} provides the {@link CommandParser} to access other arguments, if necessary.
     * <p>
     * This method is generally intended to be used for constant identifiers. For a method that
     * provides simpler access for non-constant identifying arguments, use {@link #onIndex(Consumer, int)},
     * {@link #onRule(Consumer, String...)} or {@link #onRule(Consumer, int, String...)}.
     * <p>
     * <b>Note:</b> Only the first argument that matches will execute the {@link Consumer}.
     * 
     * @param logic The {@link Consumer} providing the {@link CommandParser} which will be executed.
     * @param arguments The argument identifiers for the subcommand.
     */
    public void on(Consumer<CommandParser> logic, String... arguments) {
        List<String> parsedArgs = parser.getArguments();
        for (String arg : arguments) {
            if (parsedArgs.contains(arg)) {
                logic.accept(parser);
                hasExecuted = true;
                return;
            }
        }
    }

    /**
     * Subscribes logic to this system if the parsed argument at {@code index} matches any of the {@code arguments}.
     * The {@link Consumer} provides the {@link CommandParser} to access other arguments, if necessary.
     * <p>
     * This method is generally intended to be used for constant identifiers. For a method that
     * provides simpler access for non-constant identifying arguments, use {@link #onIndex(Consumer, int)},
     * {@link #onRule(Consumer, String...)} or {@link #onRule(Consumer, int, String...)}.
     * <p>
     * <b>Note:</b> Only the first argument that matches will execute the {@link Consumer}.
     * 
     * @param logic The {@link Consumer} providing the {@link CommandParser} which will be executed.
     * @param index The argument index.
     * @param arguments The argument identifiers for the subcommand.m arguments
     */
    public void on(Consumer<CommandParser> logic, int index, String... arguments) {
        List<String> argsList = List.of(arguments); // Put arguments array into List for simplicity
        if (argsList.contains(parser.get(index))) {
            logic.accept(parser);
            hasExecuted = true;
        }
    }

    /**
     * Subscribes logic to this system if there is an argument at the provided {@code index}.
     * The {@link Consumer} provides the argument at the index.
     * <p>
     * This method is generally intended to be used for non-constant arguments. For a method
     * that provides simpler access for constant identifying arguments, use {@link #on(Consumer, String...)} or
     * {@link #on(Consumer, int, String...)}.
     * 
     * @param logic The {@link Consumer} providing the {@link CommandParser} which will be executed.
     * @param index The argument index.
     */
    public void onIndex(Consumer<String> logic, int index) {
        if (parser.numArgs() - 1 >= index) {
            logic.accept(parser.get(index));
            hasExecuted = true;
        }
    }

    /**
     * Subscribes logic to this system if this system has <b>NOT</b> already executed
     * a subcommand and there is an argument at the provided {@code index}.
     * The {@link Consumer} provides the argument at the index.
     * <p>
     * This method is generally intended to be used for non-constant arguments. For a method
     * that provides simpler access for constant identifying arguments, use {@link #on(Consumer, String...)} or
     * {@link #on(Consumer, int, String...)}.
     * 
     * @param logic The {@link Consumer} providing the {@link CommandParser} which will be executed.
     * @param index The argument index.
     */
    public void onIndexLast(Consumer<String> logic, int index) {
        if (!hasExecuted && parser.numArgs() - 1 >= index) {
            logic.accept(parser.get(index));
            hasExecuted = true;
        }
    }

    /**
     * Subscribes logic to this system if there is an argument which matches any of the provided {@link CommandParser#setRules(String) rules}.
     * The {@link Consumer} provides the argument at the index.
     * <p>
     * This method is generally intended to be used for non-constant arguments. For a method
     * that provides simpler access for constant identifying arguments, use {@link #on(Consumer, String...)} or
     * {@link #on(Consumer, int, String...)}. 
     * 
     * @param logic The {@link Consumer} providing the {@link CommandParser} which will be executed.
     * @param rules The {@link CommandParser#setRules(String) rules} to be checked against.
     * @see {@link CommandParser#setRules(String) CommandParser rules}.
     */
    public void onRule(Consumer<String> logic, String... rules) {
        for (String rule : rules) {
            String arg = parser.getMatchingRule(rule);
            if (arg != null) {
                logic.accept(arg);
                hasExecuted = true;
            }
        }
    }

    /**
     * Subscribes logic to this system if there is an argument at {@code index} which matches any of the provided {@link CommandParser#setRules(String) rules}.
     * The {@link Consumer} provides the argument at the index.
     * <p>
     * This method is generally intended to be used for non-constant arguments. For a method
     * that provides simpler access for constant identifying arguments, use {@link #on(Consumer, String...)} or
     * {@link #on(Consumer, int, String...)}. 
     * 
     * @param logic The {@link Consumer} providing the {@link CommandParser} which will be executed.
     * @param index The argument index.
     * @param rules The {@link CommandParser#setRules(String) rules} to be checked against.
     * @see {@link CommandParser#setRules(String) CommandParser rules}.
     */
    public void onRule(Consumer<String> logic, int index, String... rules) {
        List<String> args = parser.getArguments();
        for (int i = 0; i < args.size(); i++) {
            for (String rule : rules) {
                String arg = parser.getMatchingRule(rule);
                if (arg != null && i == index) {
                    logic.accept(arg);
                    hasExecuted = true;
                }
            }
        }
    }
}
