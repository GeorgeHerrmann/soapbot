package com.georgster.events.poll;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.util.commands.wizard.InputWizard;
import com.georgster.util.commands.wizard.PollEventWizard;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command for interacting with {@code PollEvents}.
 */
public class PollEventCommand implements Command {

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        event.getLogger().append("- Beginning the Poll Wizard\n", LogDestination.NONAPI);

        InputWizard wizard = new PollEventWizard(event);
        wizard.begin();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("poll", "polls", "pe", "pevent", "pollevent");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- '!poll' to bring up the Poll Wizard";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Create, view or vote for a poll")
                .build();
    }
}
