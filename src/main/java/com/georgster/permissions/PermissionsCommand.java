package com.georgster.permissions;

import java.util.List;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.commands.ParseBuilder;
import com.georgster.util.commands.SubcommandSystem;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.PermissionsWizard;

import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Represents the bot's actions following the !permissions command.
 */
public class PermissionsCommand implements ParseableCommand {
    private static final String PATTERN = "V|R 1|O";

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
        final SubcommandSystem subcommands = event.createSubcommandSystem();
        final MultiLogger logger = event.getLogger();

        subcommands.on(p -> {
            StringBuilder response = new StringBuilder("Permission Groups:\n");
            permissionsManager.getAll().forEach(group -> response.append("\t" + group.getName() + "\n"));
            response.append("Use !permissions [group] to see the permissions for a group");
            handler.sendText(response.toString());
        }, "list");

        subcommands.on(p -> {
            InputWizard wizard = new PermissionsWizard(event);
            wizard.begin();
        }, "manage");

        subcommands.on(p -> {
            try {
                PermissibleAction action = PermissionsManager.getAction(p.get(1).toUpperCase());
                permissionsManager.getAll().forEach(group -> {
                    group.addPermission(action);
                    permissionsManager.update(group);
                });
                handler.sendText("Added " + action.toString() + " to all groups");
                logger.append("- Added " + action.toString() + " to all groups", LogDestination.NONAPI);
            } catch (IllegalArgumentException e) {
                handler.sendText("That is not a valid action. Please try again");
                logger.append("- Invalid action: " + p.get(1), LogDestination.NONAPI);
            }
        }, "addall");

        subcommands.on(p -> {
            try {
                PermissibleAction action = PermissionsManager.getAction(p.get(1).toUpperCase());
                permissionsManager.getAll().forEach(group -> {
                    group.removePermission(action);
                    permissionsManager.update(group);
                });
                handler.sendText("Removed " + action.toString() + " from all groups");
                logger.append("- Removed " + action.toString() + " from all groups", LogDestination.NONAPI);
            } catch (IllegalArgumentException e) {
                handler.sendText("That is not a valid action. Please try again");
                logger.append("- Invalid action: " + p.get(1), LogDestination.NONAPI);
            }
        }, "removeall");

        subcommands.onIndexLast(group -> {
            if (permissionsManager.exists(group)) {
                PermissionGroup permissionGroup = permissionsManager.get(group);
                handler.sendText("Permissions for " + permissionGroup.getName() + ":\n" + permissionGroup.getActions().toString());
            } else {
                handler.sendText("That is not a valid group. Please try again");
            }
        }, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandParser getCommandParser() {
        return new ParseBuilder(PATTERN).withIdentifiers("list", "manage", "addall", "removeall").build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public PermissibleAction getRequiredPermission(List<String> args) {
        if (args.contains("manage") || args.contains("addall") || args.contains("removeall")) {
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
        "\n!permissions list - List all the groups" +
        "\n!permissions [group] - List all the permissions for a group" +
        "\n!permissions manage - Manage all SOAP Bot permissions for roles in this server" +
        "\n!permissions addall [permission] - Add a permission to all roles";
    }
}
