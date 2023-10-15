package com.georgster.test;

import java.util.List;

import com.georgster.Command;
import com.georgster.ParseableCommand;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.CommandParser;
import com.georgster.wizard.CollectableWizard;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Used to test on going features. This command will be considered active if the
 * {@code ACTIVE} field is set to {@code true}.
 */
public class TestCommand implements Command { 
    private static final boolean ACTIVE = true;

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        new CollectableWizard(event).begin();
        //throw new UnsupportedOperationException("Test command is currently inactive");
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() { // Useful when testing using user input arguments
        return new CommandParser("VR");
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
