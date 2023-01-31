package com.georgster.util.permissions;

import java.util.Arrays;
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
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandRequest;

//!permissions list - list of all groups
//!permissions [group] - list of all permissions for a group
//!permissions manage - manage permissions for a group
public class PermissionsCommand implements Command {
    private static final String PATTERN = "V|R";

    private boolean needsNewRegistration = true; // Set to true only if the command registry should send a new command definition to Discord
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
            } else {
                String group = parser.get(0);
                if (permissionsManager.groupExists(group)) {
                    PermissionGroup permissionGroup = permissionsManager.getGroup(group);
                    manager.sendText("Permissions for " + permissionGroup.getName() + ":\n" + permissionGroup.getActions().toString());
                } else {
                    manager.sendText("That is not a valid group. Please try again");
                }
            }
        } catch (Exception e) {
            logger.append("The user did not provide valid arguments, showing the help message\n", LogDestination.NONAPI);
            manager.sendText(help());
        }
        logger.sendAll();
    }

    //list all the groups
    //access a group - will list all information for that group
    //manage that group - will allow you to add/remove permissions
    private void managePermissions(Member member, GuildManager manager) {
        CommandWizard wizard = new CommandWizard(manager, "stop", member);
        manager.sendText("Welcome to the permissions wizard. At any time you can type \"stop\", or react :x: to exit the wizard");

        boolean valid = true;
        PermissionGroup group = null;

        while (valid) {
            Message response = wizard.step("Which Role would you like to manage? Valid roles are:\n"
            + permissionsManager.getGroupNames().toString(), "Permission wizard");
            if (response == null) {
                valid = false;
            } else {
                String role = response.getContent().toLowerCase();
                if (permissionsManager.groupExists(role)) {
                    group = permissionsManager.getGroup(role);
                    valid = groupOptions(wizard, manager, group);
                } else {
                    manager.sendText("That is not a valid role. Please try again");
                }
            }
        }
        manager.sendText("Wizard closed");
    }

    private boolean groupOptions(CommandWizard wizard, GuildManager manager, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            Message response = wizard.step("What would you like to do? Valid options are:\n"
            + "add - add a permission to the group\n"
            + "remove - remove a permission from the group\n"
            + "list - list all permissions for the group\n"
            + "back - go back to the previous menu", "Permission wizard");
            if (response == null) {
                valid = false;
            } else {
                String option = response.getContent().toLowerCase();
                if (option.equals("add")) {
                    valid = addPermission(wizard, manager, group);
                } else if (option.equals("remove")) {
                    valid = removePermission(wizard, manager, group);
                } else if (option.equals("list")) {
                    manager.sendText("Permissions for " + group.getName() + ":\n" + group.getActions().toString());
                } else if (option.equals("back")) {
                    return true;
                } else {
                    manager.sendText("That is not a valid option. Please try again");
                }
            }
        }
        return false;
    }

    private boolean addPermission(CommandWizard wizard, GuildManager manager, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            Message response = wizard.step("What permission would you like to add? Valid permissions are:\n"
            +   Arrays.toString(PermissibleAction.values()) + "\n or type back to go back", "Permission wizard");
            if (response == null) {
                valid = false;
            } else if (response.getContent().equalsIgnoreCase("back")) {
                    return true;
            } else {
                String permission = response.getContent().toLowerCase();
                try {
                    PermissibleAction action = PermissibleAction.valueOf(permission.toUpperCase());
                    group.addPermission(action);
                    permissionsManager.updateGroup(group);
                    manager.sendText("Added " + action.toString() + " to " + group.getName());
                } catch (IllegalArgumentException e) {
                    manager.sendText("That is not a valid permission. Please try again");
                }
            }
        }
        return false;
    }

    private boolean removePermission(CommandWizard wizard, GuildManager manager, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            Message response = wizard.step("What permission would you like to remove? This group's permissions are:\n"
            +   group.getActions().toString() + "\n or type back to go back", "Permission wizard");
            if (response == null) {
                valid = false;
            } else if (response.getContent().equalsIgnoreCase("back")) {
                return true;
            } else {
                String permission = response.getContent().toLowerCase();
                try {
                    PermissibleAction action = PermissibleAction.valueOf(permission.toUpperCase());
                    if (group.getActions().contains(action)) {
                        group.removePermission(action);
                        permissionsManager.updateGroup(group);
                        manager.sendText("Removed " + action.toString() + " from " + group.getName());
                    } else {
                        manager.sendText("That permission is not in the group. Please try again");
                    }
                } catch (IllegalArgumentException e) {
                    manager.sendText("That is not a valid permission. Please try again");
                }
            }
        }
        return false;
    }

    public List<String> getAliases() {
        return List.of("permissions", "perms");
    }

    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Description test")
                .build();
    }

    public String help() {
        return "Manage permissions for the bot.";
    }

    public boolean needsDispatcher() {
        return true;
    }
}
