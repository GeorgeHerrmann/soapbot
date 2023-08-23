package com.georgster.input.wizard;

import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.input.wizard.input.InputListenerFactory;
import com.georgster.util.permissions.PermissibleAction;
import com.georgster.util.permissions.PermissionGroup;

/**
 * A {@link InputWizard} for managing {@code PermissionGroups}.
 */
public class PermissionsWizard extends InputWizard {
    private static final String TITLE = "Permissions Wizard";

    private PermissionsManager permissionsManager;

    /**
     * Creates a new permissions wizard.
     * 
     * @param event Command execution event that triggered the wizard.
     * @param permissionsManager Permissions manager to use.
     */
    public PermissionsWizard(CommandExecutionEvent event) {
        super (event, InputListenerFactory.createMenuMessageListener(event, TITLE));
        this.permissionsManager = event.getPermissionsManager();
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        handler.sendText("Welcome to the permissions wizard. At any time you can type \"stop\", or react :x: to exit the wizard", TITLE);
        nextWindow("managePermissions");
        end();
    }

    /**
     * The main menu for the permissions wizard.
     */
    protected void managePermissions() {
        String[] groups = new String[permissionsManager.getCount()];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = permissionsManager.getAll().get(i).getName();
        }
        withResponse((response -> {
            PermissionGroup group = permissionsManager.get(response);
            nextWindow("groupOptions", group);
        }), false, "Which Role would you like to manage?", groups);
    }

    /**
     * Screen for managing a group's permissions.
     * 
     * @param group The group to manage.
     */
    protected void groupOptions(PermissionGroup group) {
        withResponse((response -> {
            if (response.equals("add")) {
                nextWindow("addPermission", group);
            } else if (response.equals("remove")) {
                nextWindow("removePermission", group);
            } else if (response.equals("list")) {
                handler.sendText("Permissions for " + group.getName() + ":\n" + group.getActions().toString(), TITLE);
            }
        }), true, "What would you like to do for " + group.getName() + "?", "add", "remove", "list");
    }

    /**
     * Screen for adding a permission to a group.
     * 
     * @param inputGroup The group to add the permission to.
     */
    protected void addPermission(PermissionGroup inputGroup) {
        final PermissionGroup group = permissionsManager.get(inputGroup.getName());
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
        final PermissionGroup group = permissionsManager.get(inputGroup.getName());
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
