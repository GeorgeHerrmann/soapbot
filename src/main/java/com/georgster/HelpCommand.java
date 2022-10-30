package com.georgster;

import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Map;

public class HelpCommand implements Command {
    //!help - lists commands
    //!help COMMAND - explains command

    Map<String, Command> commands;

    HelpCommand(Map<String, Command> command) {
        commands = command;
    }

    public void execute(MessageCreateEvent event) {
        StringBuilder message = new StringBuilder(event.getMessage().getContent());
        StringBuilder response = new StringBuilder("Type !help followed by a command for more information regarding that command\nAvailable Commands:\n");
        message.delete(message.indexOf("!help"), message.indexOf("!help") + 6);
        for (final Map.Entry<String, Command> entry : commands.entrySet()) {
            if (message.toString().startsWith(entry.getKey())) {
                response = new StringBuilder(entry.getValue().help());
                break;
            } else {
                response.append(entry.getKey() + " ");
            }
        }
        event.getMessage().getChannel().block().createMessage(response.toString()).block();
    }

    public String help() {
        return "Command: !help" +
        "\n!help for a list of all commands" +
        "\n!help [COMMAND] for help regarding a specific command";
    }
    
}
