package com.georgster;

import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.Map;

import com.georgster.api.ActionWriter;
import com.georgster.util.CommandParser;
import com.georgster.util.GuildManager;
import com.georgster.util.ParseBuilder;

/**
 * HelpCommand exists to provide users information regarding usage for SOAP Bot's commands.
 */
public class HelpCommand implements Command {

    private static final String PATTERN = "1|R"; //A regex pattern to parse the contents of a !help command request
    private Map<String, Command> commands; //A Map of all commands SOAP Bot has

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
    public void execute(MessageCreateEvent event, GuildManager manager) {
        CommandParser parser = new ParseBuilder(PATTERN).build();
        parser.parse(event.getMessage().getContent());
        String arg;
        try {
            arg = parser.get(1);
        } catch (IndexOutOfBoundsException e) {
            arg = "";
        }
        StringBuilder response = new StringBuilder("Type !help followed by a command for more information regarding that command\nAvailable Commands:\n");
        ActionWriter.writeAction("Having the HelpCommand parser parse the contents of a !help command request");
        for (final Map.Entry<String, Command> entry : commands.entrySet()) {
            if (arg.toLowerCase().equals(entry.getKey())) {
                response = new StringBuilder(entry.getValue().help());
                break;
            } else {
                response.append(entry.getKey() + " ");
            }
        }
        ActionWriter.writeAction("Responding to a !help command request");
        manager.sendText(response.toString());
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
