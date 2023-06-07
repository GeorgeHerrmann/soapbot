package com.georgster.util.commands.wizard;

import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;
import com.georgster.util.permissions.PermissibleAction;
import com.georgster.util.permissions.PermissionGroup;

/**
 * Wizard for managing permissions.
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
        handler.sendText("Welcome to the permissions wizard. At any time you can type \"stop\", or react :x: to exit the wizard");
        managePermissions();
        end();
    }

    /**
     * The main menu for the permissions wizard.
     */
    private void managePermissions() {
        while (isActive()) {
            String[] groups = new String[permissionsManager.getCount()];
            for (int i = 0; i < groups.length; i++) {
                groups[i] = permissionsManager.getAll().get(i).getName();
            }
            withResponseFirst((response -> {
                PermissionGroup group = permissionsManager.get(response);
                groupOptions(group);
            }), "Which Role would you like to manage?", groups);
        }
    }

    /**
     * Screen for managing a group's permissions.
     * 
     * @param group The group to manage.
     */
    private void groupOptions(PermissionGroup group) {
        while (isActive()) {
            WizardResponse output = withResponse((response -> {
                if (response.equals("add")) {
                    addPermission(group);
                } else if (response.equals("remove")) {
                    removePermission(group);
                } else if (response.equals("list")) {
                    handler.sendText("Permissions for " + group.getName() + ":\n" + group.getActions().toString());
                }
            }), "What would you like to do for " + group.getName() + "?", "add", "remove", "list");
            if (output == WizardResponse.BACK) {
                return;
            }
        }
    }

    /**
     * Screen for adding a permission to a group.
     * 
     * @param inputGroup The group to add the permission to.
     */
    private void addPermission(PermissionGroup inputGroup) {
        while (isActive()) {
            final PermissionGroup group = permissionsManager.get(inputGroup.getName());
            String[] perms = new String[PermissibleAction.values().length ];
            for (PermissibleAction action : PermissibleAction.values()) {
                perms[action.ordinal()] = action.toString();
            }

            WizardResponse output = withResponse((response -> {
                PermissibleAction action = PermissibleAction.valueOf(response.toUpperCase());
                group.addPermission(action);
                permissionsManager.update(group);
                handler.sendText("Added " + action.toString() + " to " + group.getName());
            }), "What permission would you like to add?", perms);
            if (output == WizardResponse.BACK) {
                return;
            }
        }
    }

    /**
     * Screen for removing a permission from a group.
     * 
     * @param inputGroup The group to remove the permission from.
     */
    private void removePermission(PermissionGroup inputGroup) {
        while (isActive()) {
            final PermissionGroup group = permissionsManager.get(inputGroup.getName());
            String[] perms = new String[group.getActions().size()];
            for (int i = 0; i < group.getActions().size(); i++) {
                perms[i] = group.getActions().get(i).toString();
            }

            WizardResponse output = withResponse((response -> {
                PermissibleAction action = PermissibleAction.valueOf(response.toUpperCase());
                if (group.getActions().contains(action)) {
                    group.removePermission(action);
                    permissionsManager.update(group);
                    handler.sendText("Removed " + action.toString() + " from " + group.getName());
                } else {
                    handler.sendText("That permission is not in the group. Please try again");
                }
            }), "What permission would you like to remove?", perms);
            if (output == WizardResponse.BACK) {
                return;
            }
        }
    }
}
