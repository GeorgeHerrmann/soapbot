package com.georgster.events.poll;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.events.SoapEventType;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.DiscordEvent;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.PollEventWizard;
import com.georgster.wizard.QuickPollWizard;

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
    private static final SoapEventType TYPE = SoapEventType.POLL;

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
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();
        SubcommandSystem subcommands = event.createSubcommandSystem();

        subcommands.on(() -> {
            logger.append("- Beginning the Poll Wizard\n", LogDestination.NONAPI);

            InputWizard wizard = new PollEventWizard(event);
            wizard.begin();
        });

        subcommands.on(p -> {
            InputWizard wizard = new QuickPollWizard(event);
            wizard.begin("presentQuickPolls");
        }, "present", "quickpolls", "qp");

        subcommands.on(p -> {
            if (eventManager.hasAny(TYPE)) {
                logger.append("- Beginning the Poll Wizard from the Voting Screen\n", LogDestination.NONAPI);
                new PollEventWizard(event).begin("pollVotingOptions");  
            } else {
                handler.sendMessage("There are no polls to vote for, type !poll create to custom create a poll, or !poll [PROMPT] to create a QuickPoll.", "No Polls Available", MessageFormatting.ERROR);
                logger.append("- No polls found in the event manager\n", LogDestination.NONAPI);
            }
        }, "vote");

        subcommands.on(p -> {
            logger.append("- Beginning the Poll Wizard from the Create a Poll Screen\n", LogDestination.NONAPI);
              new PollEventWizard(event).begin("createPoll");
        }, "create");

        subcommands.on(p -> {
            if (eventManager.hasAny(TYPE)) {
                logger.append("- Beginning the Poll Wizard from the Viewing Screen\n", LogDestination.NONAPI);
                new PollEventWizard(event).begin("pollViewingOptions");
            } else {
                handler.sendMessage("There are no polls to vote for, type !poll create to custom create a poll, or !poll [PROMPT] to create a QuickPoll.", "No Polls Available", MessageFormatting.ERROR);
                logger.append("- No polls found in the event manager\n", LogDestination.NONAPI);
            }
        }, "view");

        subcommands.onIndexLast(title -> {
            if (!eventManager.exists(title, TYPE)) {
                PollEvent pollEvent = new PollEvent(title, ((TextChannel) discordEvent.getChannel()).getName(), discordEvent.getAuthorAsMember().getId().asString());
                pollEvent.setDateTime("1 hour");
                pollEvent.addOption("yes");
                pollEvent.addOption("no");

                eventManager.add(pollEvent);

                InputWizard wizard = new QuickPollWizard(event, pollEvent);
                wizard.begin();
            } else {
                handler.sendMessage("This poll already exists, try a different title", "Polls", MessageFormatting.ERROR);
            }
        }, 0);
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new ParseBuilder("VO").withIdentifiers("present", "quickpolls", "qp", "create", "vote", "view").build();
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
        "\n- '!poll' to bring up the full Poll Wizard capable of handling all types of polls" +
        "\n - '!poll [PROMPT]' to create a quick poll for one hour with 'yes' and 'no' as the options" +
        "\n - '!poll present' or '!poll quickpolls' to bring up and present quick poll voting for a quick poll" +
        "\n - '!poll vote' to vote for a pre-existing poll" +
        "\n - '!poll view' to view a pre-existing poll" +
        "\n - '!poll create' to custom create a poll";
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
                        .description("Subcommands for handling polls")
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
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("create")
                                .value("create")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("view")
                                .value("view")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("vote")
                                .value("vote")
                                .build())
                        .build())
                .build();
    }
}
