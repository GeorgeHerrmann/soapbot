package com.georgster.util.permissions;

import discord4j.core.object.entity.Member;

public class PermissionValidator {

    private PermissionValidator() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean hasPermission(Member member, PermissibleAction action) {
        PermissionGroup memberGroup = getMemberPermissionGroup(member);
        PermissionGroup requiredGroup = getRequiredPermissionGroup(action);
        return memberGroup.ordinal() >= requiredGroup.ordinal();
    }

    private static PermissionGroup getRequiredPermissionGroup(PermissibleAction action) {
        if (action == PermissibleAction.REMOVEEVENT) {
            return PermissionGroup.ADMIN;
        }
        return PermissionGroup.GENERAL;
    }

    private static PermissionGroup getMemberPermissionGroup(Member member) {
        if (Boolean.TRUE.equals(member.getRoles().any(role -> role.getName().equals("Admin")).block())) {
            return PermissionGroup.ADMIN;
        }
        return PermissionGroup.GENERAL;
    }
}
