package com.georgster.mentiongroups;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.MentionGroupManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.SoapUtility;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.IterableStringWizard;
import com.georgster.wizard.MentionGroupWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command for interacting with {@link MentionGroup MentionGroups}.
 */
public final class MentionGroupCommand implements ParseableCommand {
    
    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        MentionGroupManager manager = event.getMentionGroupManager();
        SubcommandSystem sb = event.createSubcommandSystem();
        GuildInteractionHandler handler = event.getGuildInteractionHandler();
        MultiLogger logger = event.getLogger();

        sb.on(p -> {
            logger.append("- Opening the Mention Group Wizard at the creation window\n", LogDestination.NONAPI);
            InputWizard wizard = new MentionGroupWizard(event, false);
            wizard.begin();
        }, "create");

        sb.on(p -> {
            logger.append("- Opening the Mention Group Wizard at the editing window\n", LogDestination.NONAPI);
            InputWizard wizard = new MentionGroupWizard(event, true);
            wizard.begin();
        }, "edit");

        sb.on(p -> {
            logger.append("- Listing all mention groups", LogDestination.NONAPI);
            StringBuilder builder = new StringBuilder();

            builder.append("Mention Groups:\n");
            for (MentionGroup group : event.getMentionGroupManager().getAll()) {
                builder.append("- " + group.getIdentifier() + " *(" + group.getMemberIds().size() + " members)*").append("\n");
            }

            List<String> output = SoapUtility.splitAtEvery(builder.toString(), 10);

            SoapUtility.appendSuffixToList(output, "\nTo mention a group, use **!mention [GROUP]**"
                    + "\nTo view members of a group without pinging them, use **!mention [GROUP] silent**");

            InputWizard wizard = new IterableStringWizard(event, handler.getGuild().getName() + " Mention Groups", output);
            wizard.begin();
        }, "list");

        sb.on(p -> {
            if (p.size() > 1) {
                logger.append("- Searching for a mention group\n", LogDestination.NONAPI);
                String group = p.get(0);
                if (manager.exists(group)) {
                    logger.append("- Mention group found\n", LogDestination.NONAPI);
                    handler.sendMessage(manager.get(group).getMentionString(handler), "Mention Group " + group);
                } else {
                    logger.append("- Mention group not found\n", LogDestination.NONAPI);
                    handler.sendMessage("A Mention Group with that name does not exist. Type '!mention create' to create a new one", "Group not found");
                }
            } else {
                logger.append("- No mention group specified\n", LogDestination.NONAPI);
                handler.sendMessage("You must specify a mention group to mention silently", "No group specified");
            }
        }, "silent", "quiet", "s");

        sb.onIndexLast(group -> {
            logger.append("- Searching for a mention group\n", LogDestination.NONAPI);
            if (manager.exists(group)) {
                logger.append("- Mention group found\n", LogDestination.NONAPI);
                handler.sendPlainMessage(manager.get(group).getMentionString(handler));
            } else {
                logger.append("- Mention group not found\n", LogDestination.NONAPI);
                handler.sendMessage("A Mention Group with that name does not exist. Type '!mention create' to create a new one", "Group not found");
            }
        }, 0);
    }

    /**
     * {@inheritDoc}
     */
    public CommandParser getCommandParser() {
        return new ParseBuilder("VR", "1O").withRules("X", "I").withIdentifiers("create", "edit", "list", "silent", "quiet", "s").build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n- !mention [GROUP] - Mention a group of people in a Mention group" +
        "\n- !mention [GROUP] silent/s/quiet - Mention a group of people in a Mention group without pinging them" +
        "\n- !mention create - Create a new Mention Group" +
        "\n- !mention edit - Edit an existing Mention Group" +
        "\n- !mention list - List all mention groups";
    }

    /**
     * {@inheritDoc}
     */
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
                        .description("A group to mention, add 'silent' to mention without pinging")
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
