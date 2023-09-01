package com.georgster.util.commands;

import java.util.List;
import java.util.function.Consumer;

import com.georgster.control.util.CommandExecutionEvent;

public class SubcommandSystem {
    private final CommandParser parser;

    public SubcommandSystem(CommandExecutionEvent event) {
        this.parser = event.getCommandParser();
    }

    public void on(Consumer<CommandParser> logic, String... arguments) {
        List<String> parsedArgs = parser.getArguments();
        for (String arg : arguments) {
            if (parsedArgs.contains(arg)) {
                logic.accept(parser);
            }
        }
    }

    public void on(Consumer<CommandParser> logic, int index, String... arguments) {
        List<String> argsList = List.of(arguments); // Put arguments array into List for simplicity
        if (argsList.contains(parser.get(index))) {
            logic.accept(parser);
        }
    }

    public void onIndex(Consumer<String> logic, int index) {
        if (parser.numArgs() - 1 >= index) {
            logic.accept(parser.get(index));
        }
    }

    public void onRule(Consumer<String> logic, String... rules) {
        for (String rule : rules) {
            String arg = parser.getMatchingRule(rule);
            if (arg != null) {
                logic.accept(arg);
            }
        }
    }

    public void onRule(Consumer<String> logic, int index, String... rules) {
        List<String> args = parser.getArguments();
        for (int i = 0; i < args.size(); i++) {
            for (String rule : rules) {
                String arg = parser.getMatchingRule(rule);
                if (arg != null && i == index) {
                    logic.accept(arg);
                }
            }
        }
    }
}
