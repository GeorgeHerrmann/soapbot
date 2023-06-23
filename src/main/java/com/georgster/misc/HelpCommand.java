package com.georgster.misc;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData.Builder;
import discord4j.discordjson.json.ApplicationCommandRequest;

import java.util.List;

import com.georgster.Command;
import com.georgster.ParseableCommand;
import com.georgster.control.CommandRegistry;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;

/**
 * The HelpCommand exists to provide users information regarding usage for SOAP Bot's commands.
 */
public class HelpCommand implements ParseableCommand {

    private static final String PATTERN = "1|O"; //A regex pattern to parse the contents of a !help command request
    
    private CommandRegistry register; //SoapBot's Command Registry

    /**
     * Creates a new {@code HelpCommand} with the given {@code ClientContext}.
     * 
     * @param context The context to get the {@code CommandRegistry} from
     */
    public HelpCommand(ClientContext context) {
        register = context.getCommandRegistry();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MultiLogger logger = event.getLogger();
        CommandParser parser = event.getCommandParser();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();

        String arg = parser.get(0).toLowerCase();
        try {
            arg = parser.get(0).toLowerCase();
        } catch (Exception e) {
            arg = "";
        }

        StringBuilder response = new StringBuilder("Type !help followed by a command for more information regarding that command\nAvailable Commands:\n");
        for (Command command : register.getCommands()) {
            if (command.getAliases().contains(arg)) {
                logger.append("- Command found: " + command.getClass().getSimpleName() + "\n", LogDestination.NONAPI);
                response = new StringBuilder(command.getClass().getSimpleName() + "\n" + command.help());
                break;
            } else {
                if (!command.getAliases().isEmpty()) {
                    response.append(command.getAliases().get(0) + " ");
                }
            }
        }
        logger.append("Responding to a !help command request", LogDestination.API);
        String[] output = SoapUtility.splitFirst(response.toString());
        handler.sendText(output[1], output[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new CommandParser(PATTERN);
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
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {

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
        StringBuilder response = new StringBuilder("Type !help followed by a command for more information regarding that command\nAvailable Commands:\n");
        for (Command command : register.getCommands()) {
            if (!command.getAliases().isEmpty()) {
                response.append(command.getAliases().get(0) + " ");
            }
        }

        return "Aliases: " + getAliases().toString() +
        "\n" + response.toString();

    }
    
}
