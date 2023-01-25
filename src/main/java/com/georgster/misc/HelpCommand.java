package com.georgster.misc;

import discord4j.core.event.domain.message.MessageCreateEvent;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.CommandRegistry;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;

/**
 * HelpCommand exists to provide users information regarding usage for SOAP Bot's commands.
 */
public class HelpCommand implements Command {

    private static final String PATTERN = "1|R"; //A regex pattern to parse the contents of a !help command request
    private CommandRegistry register; //SoapBot's Command Registry

    /**
     * Creates a HelpCommand which contains a register of all of SoapBot's commands.
     * 
     * @param command a Map of all of SOAP Bot's commands.
     */
    public HelpCommand(CommandRegistry register) {
        this.register = register;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        MultiLogger<HelpCommand> logger = new MultiLogger<>(manager, HelpCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        CommandParser parser = new CommandParser(PATTERN);
        String arg;
        try {
            parser.parse(event.getMessage().getContent());
            logger.append("\tArguments found: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);
            arg = parser.get(0).toLowerCase();
        } catch (Exception e) {
            logger.append("\tNo arguments found\n", LogDestination.NONAPI);
            arg = "";
        }
        StringBuilder response = new StringBuilder("Type !help followed by a command for more information regarding that command\nAvailable Commands:\n");
        for (Command command : register.getCommands()) {
            if (command.getAliases().contains(arg)) {
                logger.append("\tCommand found: " + command.getClass().getSimpleName() + "\n", LogDestination.NONAPI);
                response = new StringBuilder(command.help());
                break;
            } else {
                if (!command.getAliases().isEmpty()) {
                    response.append(command.getAliases().get(0) + " ");
                } else {
                    response.append("No Aliases");
                }
            }
        }
        logger.append("Responding to a !help command request", LogDestination.API);
        manager.sendText(response.toString());

        logger.sendAll();
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("help");
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !help" +
        "\nAliases: " + getAliases().toString() +
        "\n!help for a list of all commands" +
        "\n!help [COMMAND] for help regarding a specific command";
    }
    
}
