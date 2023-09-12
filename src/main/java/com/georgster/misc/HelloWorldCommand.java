package com.georgster.misc;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A simple command that responds with "Hello world!". Features the simplest
 * implementation of a command.
 */
public class HelloWorldCommand implements Command {

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        event.getGuildInteractionHandler().sendMessage("Hello world!");
        event.getLogger().append("- Responding to a !hello command request", LogDestination.API, LogDestination.NONAPI);
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("hello", "hello world", "helloworld");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !hello = Responds with 'Hello world!'";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {

        return ApplicationCommandRequest.builder()
            .name("hello")
            .description("Responds with 'Hello world!'")
            .build();
    }
}
