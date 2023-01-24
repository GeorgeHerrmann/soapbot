package com.georgster.util.permissions;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.PermissionsManager;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.CommandWizard;
import com.georgster.util.commands.ParseBuilder;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;

//!permissions list - list of all groups
//!permissions [group] - list of all permissions for a group
//!permissions manage - manage permissions for a group
public class PermissionsCommand implements Command {
    private static final String PATTERN = "1|R";

    private final PermissionsManager permissionsManager;

    public PermissionsCommand(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    public void execute(MessageCreateEvent event, GuildManager manager) {
        MultiLogger<PermissionsCommand> logger = new MultiLogger<>(manager, PermissionsCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        CommandParser parser = new ParseBuilder(PATTERN).withIdentifiers("list", "manage").build();

        try {
            parser.parse(event.getMessage().getContent().toLowerCase());
            logger.append("\tParsed: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

            if (parser.get(0).equals("list")) {
                StringBuilder response = new StringBuilder("Permission Groups:\n");
                permissionsManager.getGroups().forEach(group -> response.append("\t" + group.getName() + "\n"));
                response.append("Use !permissions [group] to see the permissions for a group");
                manager.sendText(response.toString());
            } else if (parser.get(0).equals("manage")) {
                Member member = event.getMessage().getAuthorAsMember().block();
                if (permissionsManager.hasPermission(member, PermissibleAction.MANAGEPERMISSIONS)) {
                    managePermissions(member, manager);
                } else {
                    manager.sendText("You must be in a group that has access to " + PermissibleAction.MANAGEPERMISSIONS.toString() + " to use this command");
                }
            }
        } catch (Exception e) {
            logger.append("The user did not provide valid arguments, showing the help message\n", LogDestination.NONAPI);
            manager.sendText(help());
        }
        logger.sendAll();
    }

    private void managePermissions(Member member, GuildManager manager) {
        CommandWizard wizard = new CommandWizard(manager, "stop", member);
        manager.sendText("Welcome to the permissions wizard. ")
    }

    public List<String> getAliases() {
        return List.of("permissions", "perms");
    }

    public String help() {
        return "Manage permissions for the bot.";
    }

    public boolean needsDispatcher() {
        return true;
    }
}
