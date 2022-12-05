package com.georgster;

import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Map;

import com.georgster.api.ActionWriter;

/**
 * HelpCommand exists to provide users information regarding usage for SOAP Bot's commands.
 */
public class HelpCommand implements Command {

    Map<String, Command> commands; //A Map of all commands SOAP Bot has

    /**
     * Creates a HelpCommand which contains a Map of all of SOAP Bot's commands and their associated object.
     * 
     * @param command a Map of all of SOAP Bot's commands.
     */
    HelpCommand(Map<String, Command> command) {
        commands = command;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event) {
        StringBuilder message = new StringBuilder(event.getMessage().getContent().toLowerCase());
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
        ActionWriter.writeAction("Executing the Help Command");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !help" +
        "\n!help for a list of all commands" +
        "\n!help [COMMAND] for help regarding a specific command";
    }
    
}
