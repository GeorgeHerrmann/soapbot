package com.georgster.misc;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData.Builder;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.CommandRegistry;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;

/**
 * HelpCommand exists to provide users information regarding usage for SOAP Bot's commands.
 */
public class HelpCommand implements Command {

    private static final String PATTERN = "1|R"; //A regex pattern to parse the contents of a !help command request

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
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
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<HelpCommand> logger = new MultiLogger<>(manager, HelpCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        CommandParser parser = new CommandParser(PATTERN);
        String arg;
        try {
            parser.parse(pipeline.getFormattedMessage().toLowerCase());
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
                }
            }
        }
        logger.append("Responding to a !help command request", LogDestination.API);
        String[] output = SoapUtility.splitFirst(response.toString());
        manager.sendText(output[1], output[0]);

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
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Show information about SOAP Bot's commands")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("command")
                        .description("The name of a SOAP Bot command")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build());
                        ApplicationCommandOptionData.builder();
        Builder temp = ApplicationCommandOptionData.builder()
                        .name("command")
                        .description("The name of a SOAP Bot command")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false);

        register.getCommands().forEach(command -> 
            temp.addChoice(ApplicationCommandOptionChoiceData.builder()
                    .name(command.getAliases().get(0))
                    .value(command.getAliases().get(0))
                    .build())
        );

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Show information about SOAP Bot's commands")
                .addOption(temp.build())
                .build();
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
