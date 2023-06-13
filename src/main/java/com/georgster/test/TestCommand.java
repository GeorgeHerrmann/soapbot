package com.georgster.test;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.CommandParser;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Used to test on going features. This command will be considered active if the
 * {@code ACTIVE} field is set to {@code true}.
 */
public class TestCommand implements ParseableCommand { 
    private static final boolean ACTIVE = false;

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        throw new UnsupportedOperationException("Test command is inactive. Set ACTIVE to true to enable this command");
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() { // Useful when testing using user input arguments
        return new CommandParser("V|R");
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        if (ACTIVE) {
            return List.of("test");
        } else { //if inactive, the registry will not be able to find this command using the aliases
            return List.of();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name("test")
                .description("Used to test on going features.")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "The test command is used to test on going features.";
    }

}
