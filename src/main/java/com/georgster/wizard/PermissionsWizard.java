package com.georgster.wizard;

import java.util.HashSet;
import java.util.Set;

import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.permissions.PermissibleAction;
import com.georgster.permissions.PermissionGroup;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.wizard.input.InputListener;
import com.georgster.wizard.input.InputListenerFactory;

/**
 * An {@link InputWizard} for managing {@code PermissionGroups}.
 */
public class PermissionsWizard extends InputWizard {
    private static final String TITLE = "Permissions Wizard";

    private PermissionsManager permissionsManager;
    private final GuildInteractionHandler guildHandler;

    /**
     * Creates a new permissions wizard.
     * 
     * @param event Command execution event that triggered the wizard.
     * @param permissionsManager Permissions manager to use.
     */
    public PermissionsWizard(CommandExecutionEvent event) {
        super (event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.permissionsManager = event.getPermissionsManager();
        this.guildHandler = event.getGuildInteractionHandler();
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("managePermissions");
        end();
    }

    /**
     * The main menu for the permissions wizard.
     */
    protected void managePermissions() {
        Set<String> groups = new HashSet<>();

        for (int i = 0; i < permissionsManager.getCount(); i++) {
            groups.add(permissionsManager.getAll().get(i).getName());
        }
        withResponse((response -> {
            PermissionGroup group = permissionsManager.get(guildHandler.getRole(response).getId().asString());
            nextWindow("groupOptions", group);
        }), false, "Which Role would you like to manage?", groups.toArray(new String[groups.size()]));
    }

    /**
     * Screen for managing a group's permissions.
     * 
     * @param group The group to manage.
     */
    protected void groupOptions(PermissionGroup group) {
        InputListener buttonListener = InputListenerFactory.createButtonMessageListener(this.event, TITLE);

        withResponse((response -> {
            if (response.equals("add")) {
                nextWindow("addPermission", group);
            } else if (response.equals("remove")) {
                nextWindow("removePermission", group);
            } else if (response.equals("list")) {
                handler.sendMessage("Permissions for " + group.getName() + ":\n" + group.getActions().toString(), TITLE);
            }
        }), true, buttonListener, "What would you like to do for " + group.getName() + "?", "add", "remove", "list");
    }

    /**
     * Screen for adding a permission to a group.
     * 
     * @param inputGroup The group to add the permission to.
     */
    protected void addPermission(PermissionGroup inputGroup) {
        final PermissionGroup group = permissionsManager.get(inputGroup.getId());
        String[] perms = new String[PermissibleAction.values().length ];
        for (PermissibleAction action : PermissibleAction.values()) {
            perms[action.ordinal()] = action.toString();
        }

        withResponse((response -> {
            PermissibleAction action = PermissibleAction.valueOf(response.toUpperCase());
            group.addPermission(action);
            permissionsManager.update(group);
            sendMessage("Added " + action.toString() + " to " + group.getName(), TITLE);
        }), true, "What permission would you like to add?", perms);
    }

    /**
     * Screen for removing a permission from a group.
     * 
     * @param inputGroup The group to remove the permission from.
     */
    protected void removePermission(PermissionGroup inputGroup) {
        final PermissionGroup group = permissionsManager.get(inputGroup.getId());
        String[] perms = new String[group.getActions().size()];
        for (int i = 0; i < group.getActions().size(); i++) {
            perms[i] = group.getActions().get(i).toString();
        }

        withResponse((response -> {
            PermissibleAction action = PermissibleAction.valueOf(response.toUpperCase());
            if (group.getActions().contains(action)) {
                group.removePermission(action);
                permissionsManager.update(group);
                sendMessage("Removed " + action.toString() + " from " + group.getName(), TITLE);
            } else {
                sendMessage("That permission is not in the group. Please try again", TITLE);
            }
        }), true, "What permission would you like to remove?", perms);
    }
}
