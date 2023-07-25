package com.georgster.events.poll;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.DiscordEvent;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.wizard.InputWizard;
import com.georgster.util.commands.wizard.PollEventWizard;
import com.georgster.util.commands.wizard.QuickPollWizard;
import com.georgster.util.permissions.PermissibleAction;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command for interacting with {@link PollEvent PollEvents}.
 */
public class PollEventCommand implements ParseableCommand {

    private final SoapEventManager eventManager;

    /**
     * Creates a new PollEventCommand from the given context.
     * 
     * @param context The context of this command's SoapClient.
     */
    public PollEventCommand(ClientContext context) {
        this.eventManager = context.getEventManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        DiscordEvent discordEvent = event.getDiscordEvent();
        CommandParser parser = event.getCommandParser();
        MultiLogger logger = event.getLogger();

        if (parser.get(0).equals("wizard")) {
            logger.append("- Beginning the Poll Wizard\n", LogDestination.NONAPI);

            InputWizard wizard = new PollEventWizard(event);
            wizard.begin();
        } else if (parser.get(0).equals("present")) {
            InputWizard wizard = new QuickPollWizard(event);
            wizard.begin("presentQuickPolls");
        } else {
            String title = parser.get(0);
            PollEvent pollEvent = new PollEvent(title, ((TextChannel) discordEvent.getChannel()).getName(), discordEvent.getAuthorAsMember().getTag());
            pollEvent.setDateTime("1 hour");
            pollEvent.addOption("yes");
            pollEvent.addOption("no");

            eventManager.add(pollEvent);

            InputWizard wizard = new QuickPollWizard(event, pollEvent);
            wizard.begin();
        }
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new ParseBuilder("V|O").withIdentifiers("wizard", "present").build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        return PermissibleAction.POLLCOMMAND;
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
        "\n- '!poll wizard' to bring up the Poll Wizard capable of handling all types of polls" +
        "\n - '!poll [PROMPT]' to create a quick poll for one hour with 'yes' and 'no' as the options" +
        "\n - '!poll present' to bring up and present quick poll voting for a quick poll";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Create, view or vote for a poll")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("prompt")
                        .description("A quick poll prompt, should be the only option if selected")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("option")
                        .description("Bring up the wizard or present a quick poll")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("wizard")
                                .value("wizard")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("present")
                                .value("present")
                                .build())
                        .build())
                .build();
    }
}
