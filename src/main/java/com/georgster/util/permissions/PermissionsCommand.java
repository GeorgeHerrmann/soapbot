package com.georgster.util.permissions;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.CommandWizard;
import com.georgster.util.commands.ParseBuilder;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the bot's actions following the !permissions command.
 */
public class PermissionsCommand implements ParseableCommand {
    private static final String PATTERN = "V|R 1|O";

    private boolean needsNewRegistration = false; // Set to true only if the command registry should send a new command definition to Discord
    private PermissionsManager permissionsManager;

    /**
     * Creates a new PermissionsCommand with the given {@code ClientContext}.
     * 
     * @param context The context to get the {@code PermissionsManager} from
     */
    public PermissionsCommand(ClientContext context) {
        this.permissionsManager = context.getPermissionsManager();
    }

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        final GuildInteractionHandler handler = event.getGuildInteractionHandler();
        final CommandParser parser = event.getCommandParser();
        final MultiLogger logger = event.getLogger();

        logger.append("\tParsed: " + parser.getArguments().toString() + "\n", LogDestination.NONAPI);

        if (parser.get(0).equals("list")) {
            StringBuilder response = new StringBuilder("Permission Groups:\n");
            permissionsManager.getAll().forEach(group -> response.append("\t" + group.getName() + "\n"));
            response.append("Use !permissions [group] to see the permissions for a group");
            handler.sendText(response.toString());
        } else if (parser.get(0).equals("manage")) {
            managePermissions(event);
        } else if (parser.get(0).equals("addall")) {
            try {
                PermissibleAction action = PermissionsManager.getAction(parser.get(1).toUpperCase());
                permissionsManager.getAll().forEach(group -> group.addPermission(action));
                handler.sendText("Added " + action.toString() + " to all groups");
                logger.append("\tAdded " + action.toString() + " to all groups", LogDestination.NONAPI);
            } catch (IllegalArgumentException e) {
                handler.sendText("That is not a valid action. Please try again");
                logger.append("\tInvalid action: " + parser.get(1), LogDestination.NONAPI);
            }
        } else {
            String group = parser.get(0);
            if (permissionsManager.exists(group)) {
                PermissionGroup permissionGroup = permissionsManager.get(group);
                handler.sendText("Permissions for " + permissionGroup.getName() + ":\n" + permissionGroup.getActions().toString());
            } else {
                handler.sendText("That is not a valid group. Please try again");
            }
        }
    }

    /**
     * Begins the permissions wizard for a user.
     * 
     * @param member The member to manage permissions for
     * @param manager The guild manager to use
     */
    private void managePermissions(CommandExecutionEvent event) {
        CommandWizard wizard = new CommandWizard(event, "stop", "Permission Wizard");
        GuildInteractionHandler handler = event.getGuildInteractionHandler();

        handler.sendText("Welcome to the permissions wizard. At any time you can type \"stop\", or react :x: to exit the wizard");

        boolean valid = true;
        PermissionGroup group = null;

        while (valid) {
            String[] groups = new String[permissionsManager.getCount()];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = permissionsManager.getAll().get(i).getName();
            }
            String response = wizard.step("Which Role would you like to manage?", groups);
            if (response == null) {
                valid = false;
            } else {
                group = permissionsManager.get(response);
                valid = groupOptions(wizard, group);
            }
        }
        handler.sendText("Wizard closed");
    }

    /**
     * The options for a group in the wizard.
     * 
     * @param wizard The wizard to use
     * @param manager The guild manager to use
     * @param group The group to manage
     * @return True if the wizard should continue, false if it should exit
     */
    private boolean groupOptions(CommandWizard wizard, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            String response = wizard.step("What would you like to do for " + group.getName() + "?", "add", "remove", "list", "back");
            if (response == null) {
                valid = false;
            } else {
                if (response.equals("add")) {
                    valid = addPermission(wizard, group);
                } else if (response.equals("remove")) {
                    valid = removePermission(wizard, group);
                } else if (response.equals("list")) {
                    wizard.sendPrivateMessage("Permissions for " + group.getName() + ":\n" + group.getActions().toString());
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
    private boolean addPermission(CommandWizard wizard, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            group = permissionsManager.get(group.getName());
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
                permissionsManager.update(group);
                wizard.sendPrivateMessage("Added " + action.toString() + " to " + group.getName());
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
    private boolean removePermission(CommandWizard wizard, PermissionGroup group) {
        boolean valid = true;
        while (valid) {
            group = permissionsManager.get(group.getName());
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
                    permissionsManager.update(group);
                    wizard.sendPrivateMessage("Removed " + action.toString() + " from " + group.getName());
                } else {
                    wizard.sendPrivateMessage("That permission is not in the group. Please try again");
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder(PATTERN).withIdentifiers("list", "manage", "addall").build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.contains("manage") || args.contains("addall")) {
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
    @Override
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
                    .addOption(ApplicationCommandOptionData.builder()
                        .name("permission")
                        .description("The permission to add/remove (if applicable)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\nUsage:" +
        "\n\t!permissions list - List all the groups" +
        "\n\t!permissions [group] - List all the permissions for a group" +
        "\n\t!permissions manage - Manage all SOAP Bot permissions for roles in this server" +
        "\n\t!permissions addall [permission] - Add all permissions to a role";
    }
}
