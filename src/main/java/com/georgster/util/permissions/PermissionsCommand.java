package com.georgster.util.permissions;

import java.util.List;

import com.georgster.Command;
import com.georgster.control.PermissionsManager;
import com.georgster.control.util.CommandPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.CommandWizard;
import com.georgster.util.commands.ParseBuilder;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the bot's actions following the !permissions command.
 */
public class PermissionsCommand implements Command {
    private static final String PATTERN = "V|R";

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private final PermissionsManager permissionsManager;

    /**
     * Creates a new PermissionsCommand.
     * 
     * @param permissionsManager the permissions manager
     */
    public PermissionsCommand(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        MultiLogger<PermissionsCommand> logger = new MultiLogger<>(manager, PermissionsCommand.class);
        logger.append("**Executing: " + this.getClass().getSimpleName() + "**\n", LogDestination.NONAPI);

        CommandParser parser = new ParseBuilder(PATTERN).withIdentifiers("list", "manage").build();

        try {
            parser.parse(pipeline.getFormattedMessage());

            if (pipeline.getPermissionsManager().hasPermissionSendError(manager, logger, getRequiredPermission(parser.getArguments()), pipeline.getAuthorAsMember())) {
                logger.append("\tParsed: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

                if (parser.get(0).equals("list")) {
                    StringBuilder response = new StringBuilder("Permission Groups:\n");
                    permissionsManager.getGroups().forEach(group -> response.append("\t" + group.getName() + "\n"));
                    response.append("Use !permissions [group] to see the permissions for a group");
                    manager.sendText(response.toString());
                } else if (parser.get(0).equals("manage")) {
                    Member member = pipeline.getAuthorAsMember();
                    managePermissions(member, manager);
                } else {
                    String group = parser.get(0);
                    if (permissionsManager.groupExists(group)) {
                        PermissionGroup permissionGroup = permissionsManager.getGroup(group);
                        manager.sendText("Permissions for " + permissionGroup.getName() + ":\n" + permissionGroup.getActions().toString());
                    } else {
                        manager.sendText("That is not a valid group. Please try again");
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.append("The user did not provide valid arguments, showing the help message\n", LogDestination.NONAPI);
            manager.sendText(help());
        }
        logger.sendAll();
    }

    /**
     * Begins the permissions wizard for a user.
     * 
     * @param member The member to manage permissions for
     * @param manager The guild manager to use
     */
    private void managePermissions(Member member, GuildManager manager) {
        CommandWizard wizard = new CommandWizard(manager, "stop", "Permission Wizard", member);
        manager.sendText("Welcome to the permissions wizard. At any time you can type \"stop\", or react :x: to exit the wizard");

        boolean valid = true;
        PermissionGroup group = null;

        while (valid) {
            String[] groups = new String[permissionsManager.getGroupCount()];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = permissionsManager.getGroups().get(i).getName();
            }
            String response = wizard.step("Which Role would you like to manage?", groups);
            if (response == null) {
                valid = false;
            } else {
                group = permissionsManager.getGroup(response);
                valid = groupOptions(wizard, manager, group);
            }
        }
        manager.sendText("Wizard closed");
    }

    /**
     * The options for a group in the wizard.
     * 
     * @param wizard The wizard to use
     * @param manager The guild manager to use
     * @param group The group to manage
     * @return True if the wizard should continue, false if it should exit
     */
    private boolean groupOptions(CommandWizard wizard, GuildManager manager, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            String response = wizard.step("What would you like to do for " + group.getName() + "?", "add", "remove", "list", "back");
            if (response == null) {
                valid = false;
            } else {
                if (response.equals("add")) {
                    valid = addPermission(wizard, manager, group);
                } else if (response.equals("remove")) {
                    valid = removePermission(wizard, manager, group);
                } else if (response.equals("list")) {
                    wizard.swapEditedMessage("Permissions for " + group.getName() + ":\n" + group.getActions().toString());
                } else if (response.equals("back")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The "add permisson" option in the wizard.
     * 
     * @param wizard The wizard to use
     * @param manager The guild manager to use
     * @param group the group to add permissions to
     * @return True if the wizard should continue, false if it should exit
     */
    private boolean addPermission(CommandWizard wizard, GuildManager manager, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            group = permissionsManager.getGroup(group.getName());
            String[] perms = new String[PermissibleAction.values().length + 1];
            for (PermissibleAction action : PermissibleAction.values()) {
                perms[action.ordinal()] = action.toString();
            }
            perms[PermissibleAction.values().length] = "back";
            String response = wizard.step("What permission would you like to add?", perms);
            if (response == null) {
                valid = false;
            } else if (response.equalsIgnoreCase("back")) {
                    return true;
            } else {
                PermissibleAction action = PermissibleAction.valueOf(response.toUpperCase());
                group.addPermission(action);
                permissionsManager.updateGroup(group);
                wizard.swapEditedMessage("Added " + action.toString() + " to " + group.getName());
            }
        }
        return false;
    }

    /**
     * The "remove permissions" option in the wizard.
     * 
     * @param wizard The wizard to use
     * @param manager The guild manager to use
     * @param group The group to remove permissions from
     * @return True if the wizard should continue, false if it should exit
     */
    private boolean removePermission(CommandWizard wizard, GuildManager manager, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            group = permissionsManager.getGroup(group.getName());
            String[] perms = new String[group.getActions().size() + 1];
            for (int i = 0; i < group.getActions().size(); i++) {
                perms[i] = group.getActions().get(i).toString();
            }
            perms[group.getActions().size()] = "back";
            String response = wizard.step("What permission would you like to remove?", perms);
            if (response == null) {
                valid = false;
            } else if (response.equalsIgnoreCase("back")) {
                return true;
            } else {
                PermissibleAction action = PermissibleAction.valueOf(response.toUpperCase());
                if (group.getActions().contains(action)) {
                    group.removePermission(action);
                    permissionsManager.updateGroup(group);
                    wizard.swapEditedMessage("Removed " + action.toString() + " from " + group.getName());
                } else {
                    wizard.swapEditedMessage("That permission is not in the group. Please try again");
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.contains("manage")) {
            return PermissibleAction.MANAGEPERMISSIONS;
        } else {
            return PermissibleAction.PERMISSIONSCOMMAND;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("permissions", "perms", "perm");
    }

    /**
     * {@inheritDoc}
     */
    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Control the permissions for SOAP Bot")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("action")
                        .description("A group name, list, or manage")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Command: !permissions" +
        "\nAliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t!permissions list - List all the groups" +
        "\n\t!permissions [group] - List all the permissions for a group" +
        "\n\t!permissions manage - Manage all SOAP Bot permissions for roles in this server";
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return true;
    }
}
