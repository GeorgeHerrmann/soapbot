package com.georgster.permissions;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.manager.Manageable;

import discord4j.core.object.entity.Role;

/**
 * A {@link PermissionGroup} is a collection of {@link PermissibleAction PermissibleActions} that
 * can be assigned to a group with an associated name. Generally, the name of
 * the group will be the name of the role that the group is assigned to.
 */
public class PermissionGroup implements Manageable {
    private String id;
    private String name;
    private List<PermissibleAction> actions;

    /**
     * Constructs a {@code PermissionGroup} with the given name and actions and id.
     * <p>
     * Generally used when loading a {@link PermissionGroup} from the database.
     * 
     * @param name    the name of the group
     * @param actions the actions the group has permission to perform
     * @param id      the id of the group
     */
    public PermissionGroup(String name, List<PermissibleAction> actions, String id) {
        this.name = name;
        this.actions = actions;
        this.id = id;
    }

    /**
     * Constructs a {@code PermissionGroup} with the given name, id and no actions.
     * 
     * @param name the name of the group
     * @param id   the id of the group
     */
    public PermissionGroup(String name, String id) {
        this.name = name;
        this.id = id;
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
     * Returns the id of the group.
     * 
     * @return the id of the group
     */
    public String getId() {
        return id;
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

    /**
     * Sets the name of this group to be the name present in the provided role.
     * 
     * @param newRole The role to get the name from.
     */
    public void setName(Role newRole) {
        this.name = newRole.getName();
    }

    /**
     * Returns the name of the group.
     * 
     * @return the name of the group
     */
    @Override
    public String getIdentifier() {
        return getId();
    }
}
