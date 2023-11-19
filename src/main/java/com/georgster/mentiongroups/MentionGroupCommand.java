package com.georgster.mentiongroups;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.MentionGroupManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.MentionGroupWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

public final class MentionGroupCommand implements ParseableCommand {
    
    public void execute(CommandExecutionEvent event) {
        MentionGroupManager manager = event.getMentionGroupManager();
        SubcommandSystem sb = event.createSubcommandSystem();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();

        sb.on(p -> {
            logger.append("Opening the Mention Group Wizard\n", LogDestination.NONAPI);
            InputWizard wizard = new MentionGroupWizard(event, false);
            wizard.begin();
        }, "create");

        sb.on(p -> {
            logger.append("Opening the Mention Group Wizard\n", LogDestination.NONAPI);
            InputWizard wizard = new MentionGroupWizard(event, true);
            wizard.begin();
        }, "edit");

        sb.on(p -> {
            logger.append("Listing all mention groups", LogDestination.NONAPI);
            StringBuilder builder = new StringBuilder();

            builder.append("Mention Groups:\n");
            for (MentionGroup group : event.getMentionGroupManager().getAll()) {
                builder.append("- " + group.getIdentifier() + " *(" + group.getMemberIds().size() + ") members*").append("\n");
            }
            builder.append("\nTo mention a group, use !mention [GROUP]");

            handler.sendMessage(builder.toString(), handler.getGuild().getName() + " Mention Groups");
        }, "list");

        sb.onIndexLast(group -> {
            logger.append("Searching for a mention group\n", LogDestination.NONAPI);
            if (manager.exists(group)) {
                logger.append("Mention group found\n", LogDestination.NONAPI);
                handler.sendPlainMessage(manager.get(group).getMentionString(handler));
            } else {
                logger.append("Mention group not found\n", LogDestination.NONAPI);
                handler.sendMessage("A Mention Group with that name does not exist. Type '!mention create' to create a new one", "Group not found");
            }
        }, 0);
    }

    public CommandParser getCommandParser() {
        return new CommandParser("VR");
    }

    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !mention [GROUP] - Mention a group of people in a Mention group" +
        "\n- !mention create - Create a new Metion Group" +
        "\n- !mention edit - Edit an existing Mention Group" +
        "\n- !mention list - List all mention groups";
    }

    public List<String> getAliases() {
        return List.of("mention", "ping", "p");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Interact with Mention Groups")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("group")
                        .description("A group to mention")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("option")
                        .description("Subcommands for Mention groups")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("create")
                                .value("create")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("edit")
                                .value("edit")
                                .build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder()
                                .name("list")
                                .value("list")
                                .build())
                        .build())
                .build();
    }

}
