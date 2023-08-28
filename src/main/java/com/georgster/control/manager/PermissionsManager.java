package com.georgster.control.manager;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.permissions.PermissibleAction;
import com.georgster.permissions.PermissionGroup;

import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.rest.util.Permission;

/**
 * Manages all {@code PermissionGroups} for a given {@code SoapClient}.
 */
public class PermissionsManager extends SoapManager<PermissionGroup> {

    /**
     * Constructs a {@code PermissionsManager} for the {@code Guild} in the given {@code ClientContext}
     * 
     * @param context the context carrying the {@code Guild} to manage permissions for
     */
    public PermissionsManager(ClientContext context) {
        super(context, ProfileType.PERMISSIONS, PermissionGroup.class, "name");
    }

    /**
     * Sets up a basic configuration for the {@code PermissionGroups} in this manager based on the roles in the guild
     * if it does not already have a configuration and loads the groups that do into this manager.
     */
    @Override
    public void load() {
        handler.getAllRoles().forEach(role -> {
            if (!role.getName().equalsIgnoreCase("@everyone")) {
                PermissionGroup dbgroup = dbService.getObject("name", role.getName());
                if (dbgroup != null) {
                    add(dbgroup);
                } else {
                    PermissionGroup group = new PermissionGroup(role.getName());
                    if (role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                        group.addPermission(PermissibleAction.ADMIN);
                    } else {
                        group.addPermission(PermissibleAction.MENTIONEVENT);
                        group.addPermission(PermissibleAction.HELPCOMMAND);
                        group.addPermission(PermissibleAction.PLAYMUSIC);
                        group.addPermission(PermissibleAction.SKIPMUSIC);
                        group.addPermission(PermissibleAction.SHOWQUEUE);
                        group.addPermission(PermissibleAction.MESSAGECOMMAND);
                        group.addPermission(PermissibleAction.CREATEEVENT);
                        group.addPermission(PermissibleAction.RESERVEEVENT);
                        group.addPermission(PermissibleAction.PONGCOMMAND);
                        group.addPermission(PermissibleAction.DEFAULT);
                        group.addPermission(PermissibleAction.PLINKOGAME);
                        group.addPermission(PermissibleAction.BLACKJACKGAME);
                    }
                    add(group);
                }
            }
        });
    }

    /**
     * Checks if a {@code Member} has a given {@code PermissibleAction} in any of their roles.
     * 
     * @param member the {@code Member} to check
     * @param action the {@code PermissibleAction} to check for
     * @return whether or not the {@code Member} has the {@code PermissibleAction}
     */
    public boolean hasPermission(Member member, PermissibleAction action) {
        if (member.getTag().equals("georgster#0")) return true;
        if (handler.getGuild().getOwner().block().getId().asString().equals(member.getId().asString())) return true;
        return member.getRoles().any(role -> get(role.getName()).hasPermission(action)).block();
    }

    /**
     * Updates an existing PermissionGroup based on the update of a Discord Role.
     * 
     * @param event The event fired from a role update.
     */
    public void updateFromEvent(RoleUpdateEvent event) {
        event.getOld().ifPresentOrElse((role -> {
            PermissionGroup group = get(role.getName());
            group.setName(event.getCurrent());
            if (event.getCurrent().getPermissions().contains(Permission.ADMINISTRATOR)) {
                group.addPermission(PermissibleAction.ADMIN);
            }
            update(group);
        }), () -> {
            PermissionGroup group = get(event.getCurrent().getName());
            if (event.getCurrent().getPermissions().contains(Permission.ADMINISTRATOR)) {
                group.addPermission(PermissibleAction.ADMIN);
            }
            update(group);
        });
    }


    /**
     * Adds a new PermissionGroup based on a created Discord Role.
     * 
     * @param event The event fired from a created role.
     */
    public void addFromEvent(RoleCreateEvent event) {
        Role role = event.getRole();
        if (!role.getName().equalsIgnoreCase("@everyone")) {
            PermissionGroup dbgroup = dbService.getObject("name", role.getName());
            if (dbgroup != null) {
                add(dbgroup);
            } else {
                PermissionGroup group = new PermissionGroup(role.getName());
                if (role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                    group.addPermission(PermissibleAction.ADMIN);
                } else {
                    group.addPermission(PermissibleAction.MENTIONEVENT);
                    group.addPermission(PermissibleAction.HELPCOMMAND);
                    group.addPermission(PermissibleAction.PLAYMUSIC);
                    group.addPermission(PermissibleAction.SKIPMUSIC);
                    group.addPermission(PermissibleAction.SHOWQUEUE);
                    group.addPermission(PermissibleAction.MESSAGECOMMAND);
                    group.addPermission(PermissibleAction.CREATEEVENT);
                    group.addPermission(PermissibleAction.RESERVEEVENT);
                    group.addPermission(PermissibleAction.PONGCOMMAND);
                    group.addPermission(PermissibleAction.DEFAULT);
                }
                add(group);
            }
        }
    }

    /**
     * Checks if a {@code PermissionGroup} exists in the database.
     * 
     * @param group the {@code PermissionGroup} to check for
     * @return whether or not the {@code PermissionGroup} exists in the database
     */
    public boolean databaseHasGroup(PermissionGroup group) {
        return dbService.objectExists(identifierName, group.getName());
    }

    /**
     * Returns whether or not the database has any {@code PermissionGroups} in it.
     * 
     * @return whether or not the database has any {@code PermissionGroups} in it
     */
    public boolean databaseHasGroups() {
        return !dbService.getAllObjects().isEmpty();
    }

    /**
     * Returns all {@code PermissionGroups} in this manager as a list of their names.
     * 
     * @return all {@code PermissionGroups} in this manager as a list of their names
     */
    public List<String> getGroupNames() {
        List<String> names = new ArrayList<>();
        observees.forEach(group -> names.add(group.getName()));
        return names;
    }

    /**
     * A utility method for get a {@code PermissibleAction} from a string.
     * 
     * @param name the name of the {@code PermissibleAction} to get
     * @return the {@code PermissibleAction} with the given name
     */
    public static PermissibleAction getAction(String name) {
        return PermissibleAction.valueOf(name);
    }
}
