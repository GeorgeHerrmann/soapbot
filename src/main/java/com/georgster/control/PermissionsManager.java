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
        manager.getAllRoles().forEach(role -> {
            if (!role.getName().equalsIgnoreCase("@everyone")) {
                if (handler.pullGroup(role.getName()) != null) {
                    PermissionGroup group = handler.pullGroup(role.getName());
                    addGroup(group);
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
                    }
                    addGroup(group);
                }
            }
        });
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
        if (member.getTag().equals("georgster#8086")) return true;
        return member.getRoles().any(role -> getGroup(role.getName()).hasPermission(action)).block();
    }

    public boolean groupExists(PermissionGroup group) {
        return groups.contains(group);
    }

    public boolean groupExists(String name) {
        return groups.stream().anyMatch(group -> group.getName().equalsIgnoreCase(name));
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
        return groups.stream().filter(group -> group.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<PermissionGroup> getGroups() {
        return groups;
    }

    public List<String> getGroupNames() {
        List<String> names = new ArrayList<>();
        groups.forEach(group -> names.add(group.getName()));
        return names;
    }

    public void updateGroup(PermissionGroup newGroup) {
        if (groupExists(newGroup.getName())) {
            handler.removeObject(handler.pullGroup(newGroup.getName()), ProfileType.PERMISSIONS);
            groups.remove(getGroup(newGroup.getName()));
            groups.add(newGroup);
            handler.addObject(newGroup, ProfileType.PERMISSIONS);
        }
    }

    public int getGroupCount() {
        return groups.size();
    }

    public static PermissibleAction getAction(String name) {
        return PermissibleAction.valueOf(name);
    }

    public Guild getGuild() {
        return manager.getGuild();
    }
}
