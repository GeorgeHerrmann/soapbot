package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientPipeline;
import com.georgster.profile.DatabaseService;
import com.georgster.profile.ProfileType;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.permissions.PermissibleAction;
import com.georgster.util.permissions.PermissionGroup;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;

/**
 * Manages all {@code PermissionGroups} for a given {@code SoapClient}.
 */
public class PermissionsManager {

    private final GuildInteractionHandler handler;
    private final List<PermissionGroup> groups;
    private final DatabaseService<PermissionGroup> dbService;

    /**
     * Constructs a {@code PermissionsManager} for the {@code Guild} in the given {@code ClientPipeline}
     * 
     * @param pipeline the pipeline carrying the {@code Guild} to manage permissions for
     */
    public PermissionsManager(ClientPipeline pipeline) {
        this.handler = new GuildInteractionHandler(pipeline.getGuild());
        this.groups = new ArrayList<>();
        this.dbService = new DatabaseService<>(handler.getId(), ProfileType.PERMISSIONS, PermissionGroup.class);
    }

    /**
     * Loads all {@code PermissionGroups} from the database into this manager.
     */
    public void loadGroups() {
        if (databaseHasGroups()) {
            dbService.getAllObjects().forEach(groups::add);
        }
    }

    /**
     * Sets up a basic configuration for the {@code PermissionGroups} in this manager based on the roles in the guild
     * if it does not already have a configuration and loads the groups that do into this manager.
     */
    public void setupBasic() {
        handler.getAllRoles().forEach(role -> {
            if (!role.getName().equalsIgnoreCase("@everyone")) {
                PermissionGroup dbgroup = dbService.getObject("name", role.getName());
                if (dbgroup != null) {
                    addGroup(dbgroup);
                } else {
                    PermissionGroup group = new PermissionGroup(role.getName());
                    if (role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                        group.addPermission(PermissibleAction.ADMIN);
                    } else {
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
                    addGroup(group);
                }
            }
        });
    }

    /**
     * Adds a {@code PermissionGroup} to this manager and the database if it does not already exist.
     * 
     * @param group the {@code PermissionGroup} to add
     */
    public void addGroup(PermissionGroup group) {
        if (!groupExists(group)) {
            groups.add(group);
            dbService.addObjectIfNotExists(group, "name", group.getName());
        }
    }

    /**
     * Removes a {@code PermissionGroup} from this manager and the database if it exists.
     * 
     * @param group the {@code PermissionGroup} to remove
     */
    public void removeGroup(PermissionGroup group) {
        if (groupExists(getGroup(group.getName()))) {
            groups.remove(group);
            dbService.removeObjectIfExists("name", group.getName());
        }
    }

    /**
     * Checks if a {@code Member} has a given {@code PermissibleAction} in any of their roles.
     * 
     * @param member the {@code Member} to check
     * @param action the {@code PermissibleAction} to check for
     * @return whether or not the {@code Member} has the {@code PermissibleAction}
     */
    public boolean hasPermission(Member member, PermissibleAction action) {
        if (member.getTag().equals("georgster#8086")) return true;
        return member.getRoles().any(role -> getGroup(role.getName()).hasPermission(action)).block();
    }

    /**
     * Checks if a PermissionGroup exists in this manager.
     * 
     * @param group the {@code PermissionGroup} to check for
     * @return whether or not the {@code PermissionGroup} exists in this manager
     */
    public boolean groupExists(PermissionGroup group) {
        return groups.contains(group);
    }

    /**
     * Checks if a {@code PermissionGroup} exists in this manager by name.
     * 
     * @param name the name of the {@code PermissionGroup} to check for
     * @return whether or not the {@code PermissionGroup} exists in this manager
     */
    public boolean groupExists(String name) {
        return groups.stream().anyMatch(group -> group.getName().equalsIgnoreCase(name));
    }

    /**
     * Checks if a {@code PermissionGroup} exists in the database.
     * 
     * @param group the {@code PermissionGroup} to check for
     * @return whether or not the {@code PermissionGroup} exists in the database
     */
    public boolean databaseHasGroup(PermissionGroup group) {
        return dbService.objectExists("name", group.getName());
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
     * Returns a {@code PermissionGroup} from this manager by name, or null if one does not exist.
     * 
     * @param name the name of the {@code PermissionGroup} to get
     * @return the {@code PermissionGroup} with the given name, or null if it does not exist
     */
    public PermissionGroup getGroup(String name) {
        return groups.stream().filter(group -> group.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    /**
     * Returns all {@code PermissionGroups} in this manager.
     * 
     * @return all {@code PermissionGroups} in this manager
     */
    public List<PermissionGroup> getGroups() {
        return groups;
    }

    /**
     * Returns all {@code PermissionGroups} in this manager as a list of their names.
     * 
     * @return all {@code PermissionGroups} in this manager as a list of their names
     */
    public List<String> getGroupNames() {
        List<String> names = new ArrayList<>();
        groups.forEach(group -> names.add(group.getName()));
        return names;
    }

    /**
     * Updates a {@code PermissionGroup} in this manager and the database if it exists.
     * 
     * @param newGroup the {@code PermissionGroup} to update
     */
    public void updateGroup(PermissionGroup newGroup) {
        if (groupExists(newGroup.getName())) {
            groups.remove(getGroup(newGroup.getName()));
            groups.add(newGroup);
            dbService.updateObjectIfExists(newGroup, "name", newGroup.getName());
        }
    }

    /**
     * Returns the number of {@code PermissionGroups} in this manager.
     * 
     * @return the number of {@code PermissionGroups} in this manager
     */
    public int getGroupCount() {
        return groups.size();
    }

    /**
     * A utility method to get a {@code PermissibleAction} from a string.
     * 
     * @param name the name of the {@code PermissibleAction} to get
     * @return the {@code PermissibleAction} with the given name
     */
    public static PermissibleAction getAction(String name) {
        return PermissibleAction.valueOf(name);
    }

    /**
     * Returns the {@code Guild} this manager is managing permissions for.
     * 
     * @return the {@code Guild} this manager is managing permissions for
     */
    public Guild getGuild() {
        return handler.getGuild();
    }
}
