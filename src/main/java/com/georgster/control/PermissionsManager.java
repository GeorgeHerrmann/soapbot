package com.georgster.control;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientPipeline;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.ProfileType;
import com.georgster.util.GuildManager;
import com.georgster.util.permissions.PermissibleAction;
import com.georgster.util.permissions.PermissionGroup;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;

public class PermissionsManager {

    private final GuildManager manager;
    private final List<PermissionGroup> groups;
    private final ProfileHandler handler;

    public PermissionsManager(ClientPipeline pipeline) {
        this.manager = new GuildManager(pipeline.getGuild());
        this.handler = manager.getProfileHandler();
        this.groups = new ArrayList<>();
    }

    public void loadGroups() {
        if (handlerHasGroups()) {
            handler.pullGroups().forEach(groups::add);
        }
    }

    public void setupBasic() {
        if (!handlerHasGroups()) {
            manager.getAllRoles().forEach(role -> {
                PermissionGroup group = new PermissionGroup(role.getName());
                if (role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                    group.addPermission(PermissibleAction.ADMIN);
                } else {
                    group.addPermission(PermissibleAction.DEFAULT);
                }
                addGroup(group);
            });
        }
    }

    public void addGroup(PermissionGroup group) {
        if (!groupExists(group)) {
            groups.add(group);
            if (!handler.objectExists(group, ProfileType.PERMISSIONS)) {
                handler.addObject(group, ProfileType.PERMISSIONS);
            }
        }
    }

    public void removeGroup(PermissionGroup group) {
        if (groupExists(getGroup(group.getName()))) {
            groups.remove(group);
            if (handler.objectExists(group, ProfileType.PERMISSIONS)) {
                handler.removeObject(handler.pullGroup(group.getName()), ProfileType.PERMISSIONS);
            }
        }
    }

    public boolean hasPermission(Member member, PermissibleAction action) {
        return member.getRoles().any(role -> getGroup(role.getName()).hasPermission(action)).block();
    }

    public boolean groupExists(PermissionGroup group) {
        return groups.contains(group);
    }

    public boolean handlerHasGroup(PermissionGroup group) {
        return handler.objectExists(group, ProfileType.PERMISSIONS);
    }

    /**
     * Checks if the {@code ProfileHandler} has any groups in it's database.
     * 
     * @return whether or not the {@code ProfileHandler} has any groups in it's database
     */
    public boolean handlerHasGroups() {
        return !handler.pullGroups().isEmpty();
    }

    public PermissionGroup getGroup(String name) {
        return groups.stream().filter(group -> group.getName().equals(name)).findFirst().orElse(null);
    }

    public List<PermissionGroup> getGroups() {
        return groups;
    }

    public void updateGroup(PermissionGroup newGroup) {
        if (groupExists(getGroup(newGroup.getName()))) {
            groups.remove(getGroup(newGroup.getName()));
            groups.add(newGroup);
        }
    }



    public static PermissibleAction getAction(String name) {
        return PermissibleAction.valueOf(name);
    }

    public Guild getGuild() {
        return manager.getGuild();
    }
}
