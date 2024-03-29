package com.georgster.misc;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
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
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParsedArguments;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.IterableStringWizard;
import com.georgster.wizard.SwappingWizard;

/**
 * The HelpCommand exists to provide users information regarding usage for SOAP Bot's commands.
 */
public class HelpCommand implements ParseableCommand {
    
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
        ParsedArguments parser = event.getParsedArguments();

        for (Command command : register.getCommands()) {
            if (command.getAliases().contains(parser.get(0))) {
                logger.append("- Command found: " + command.getClass().getSimpleName() + "\n", LogDestination.NONAPI);

                InputWizard helpWizard = new IterableStringWizard(event, command.getClass().getSimpleName(), SoapUtility.splitHelpString(command.help()));
                Message msg = event.getGuildInteractionHandler().sendMessage(command.help(), command.getClass().getSimpleName());
                InputWizard switcher = new SwappingWizard(event, msg, helpWizard);
                switcher.begin();
                break;
            }
        }
        logger.append("Responding to a !help command request", LogDestination.API);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new CommandParser("1R");
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

        register.getCommands().forEach(command -> {
            List<String> aliases = command.getAliases();
            if (!aliases.isEmpty()) {
                temp.addChoice(ApplicationCommandOptionChoiceData.builder()
                    .name(aliases.get(0))
                    .value(aliases.get(0))
                    .build());
            }
        });

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
        StringBuilder response = new StringBuilder("Type !help followed by a command for more information regarding that command\n\t- Available Commands:\n");
        for (Command command : register.getCommands()) {
            if (!command.getAliases().isEmpty()) {
                response.append("\t- " + command.getAliases().get(0) + "\n");
            }
        }

        return "Aliases: " + getAliases().toString() +
        "\n" + response.toString();

    }
    
}
