package com.georgster.util.permissions;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code PermissionGroup} is a collection of {@code PermissibleActions} that
 * can be assigned to a group with an associated name. Generally, the name of
 * the group will be the name of the role that the group is assigned to.
 */
public class PermissionGroup {
    private String name;
    private List<PermissibleAction> actions;

    /**
     * Constructs a {@code PermissionGroup} with the given name and actions.
     * 
     * @param name    the name of the group
     * @param actions the actions the group has permission to perform
     */
    public PermissionGroup(String name, List<PermissibleAction> actions) {
        this.name = name;
        this.actions = actions;
    }

    /**
     * Constructs a {@code PermissionGroup} with the given name and no actions.
     * 
     * @param name the name of the group
     */
    public PermissionGroup(String name) {
        this.name = name;
        this.actions = new ArrayList<>();
    }

    /**
     * Returns the name of the group.
     * 
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the actions the group has permission to perform.
     * 
     * @return the actions the group has permission to perform
     */
    public List<PermissibleAction> getActions() {
        return actions;
    }

    /**
     * Returns whether or not the group has permission to perform the given action.
     * 
     * @param action the action to check
     * @return whether or not the group has permission to perform the given action
     */
    public boolean hasPermission(PermissibleAction action) {
        if (actions.contains(PermissibleAction.ADMIN)) return true;
        return actions.contains(action);
    }

    /**
     * Adds the given action to the group's list of actions if the group does not
     * already have permission to perform the action.
     * 
     * @param action the action to add
     */
    public void addPermission(PermissibleAction action) {
        if (!hasPermission(action)) {
            actions.add(action);
        }
    }

    /**
     * Removes the given action from the group's list of actions if the group has
     * permission to perform the action.
     * 
     * @param action the action to remove
     */
    public void removePermission(PermissibleAction action) {
        if (hasPermission(action)) {
            actions.remove(action);
        }
    }
}
