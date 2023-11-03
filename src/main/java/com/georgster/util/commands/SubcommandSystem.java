package com.georgster.util.commands;

import java.util.List;
import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;

/**
 * A facade for commands to create argument-based subcommands with a {@link ParsedArguments}.
 */
public final class SubcommandSystem {
    private final ParsedArguments parser;
    private boolean hasExecuted;

    /**
     * Creates a SubcommandSystem which will use the {@link ParsedArguments} in the {@link CommandExecutionEvent}.
     * 
     * @param event The event that prompted this system's creation.
     */
    public SubcommandSystem(CommandExecutionEvent event) {
        this.parser = event.getParsedArguments();
        this.hasExecuted = false;
    }

    /**
     * Subscribes logic to this system if any parsed argument matches any of the {@code arguments}.
     * The {@link Consumer} provides the {@link ParsedArguments} to access other arguments, if necessary.
     * <p>
     * This method is generally intended to be used for constant identifiers. For a method that
     * provides simpler access for non-constant identifying arguments, use {@link #onIndex(Consumer, int)}}.
     * <p>
     * <b>Note:</b> Only the first argument that matches will execute the {@link Consumer}.
     * 
     * @param logic The {@link Consumer} providing the {@link ParsedArguments} which will be executed.
     * @param arguments The argument identifiers for the subcommand.
     */
    public void on(Consumer<ParsedArguments> logic, String... arguments) {
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
     * The {@link Consumer} provides the {@link ParsedArguments} to access other arguments, if necessary.
     * <p>
     * This method is generally intended to be used for constant identifiers. For a method that
     * provides simpler access for non-constant identifying arguments, use {@link #onIndex(Consumer, int)}}.
     * <p>
     * <b>Note:</b> Only the first argument that matches will execute the {@link Consumer}.
     * 
     * @param logic The {@link Consumer} providing the {@link ParsedArguments} which will be executed.
     * @param index The argument index.
     * @param arguments The argument identifiers for the subcommand.m arguments
     */
    public void on(Consumer<ParsedArguments> logic, int index, String... arguments) {
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
     * @param logic The {@link Consumer} providing the {@link ParsedArguments} which will be executed.
     * @param index The argument index.
     */
    public void onIndex(Consumer<String> logic, int index) {
        if (parser.size() - 1 >= index) {
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
     * @param logic The {@link Consumer} providing the {@link ParsedArguments} which will be executed.
     * @param index The argument index.
     */
    public void onIndexLast(Consumer<String> logic, int index) {
        if (!hasExecuted && parser.size() - 1 >= index) {
            logic.accept(parser.get(index));
            hasExecuted = true;
        }
    }

    /**
     * Subscribes logic to this system if this system recieved an empty input <i>(no arguments)</i>.
     * <p>
     * <b>Note:</b> The commands {@link CommandParser CommandParser's} pattern and rules must allow
     * for an empty input <i>(i.e a pattern with no required arguments, and no restrictve rules)</i>.
     * 
     * @param logic The {@link Runnable} containing the logic that will be run if no input is given.
     */
    public void on(Runnable logic) {
        if (parser.getArguments().isEmpty()) {
            logic.run();
            hasExecuted = true;
        }
    }

    /**
     * Returns true if this system has already executed at least one subcommand, false otherwise.
     * 
     * @return True if this system has already executed at least one subcommand, false otherwise.
     */
    public boolean hasExecuted() {
        return hasExecuted;
    }
}
