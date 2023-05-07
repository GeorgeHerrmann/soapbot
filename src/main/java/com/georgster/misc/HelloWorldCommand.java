package com.georgster.misc;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;

public class HelloWorldCommand implements Command {
    public void execute(CommandExecutionEvent event) {
        event.getGuildInteractionHandler().sendText("Hello world!");
        event.getLogger().append("\tResponding to a !hello command request", LogDestination.API, LogDestination.NONAPI);
    }

    public List<String> getAliases() {
        return List.of("hello", "hello world", "helloworld");
    }

    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t- !hello = Responds with 'Hello world!'";
    }
}
