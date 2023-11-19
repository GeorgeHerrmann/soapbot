package com.georgster.mentiongroups;

import java.util.HashSet;
import java.util.Set;

import com.georgster.control.manager.Manageable;
import com.georgster.util.handler.GuildInteractionHandler;

public final class MentionGroup implements Manageable {
    private final Set<String> memberIds;
    private final String name;

    public MentionGroup(String name, Set<String> memberIds) {
        this.name = name;
        this.memberIds = memberIds;
    }

    public MentionGroup(String name) {
        this.name = name;
        this.memberIds = new HashSet<>();
    }

    public String getIdentifier() {
        return name;
    }

    public Set<String> getMemberIds() {
        return memberIds;
    }

    public void addMember(String memberId) {
        memberIds.add(memberId);
    }

    public void removeMember(String memberId) {
        memberIds.remove(memberId);
    }

    public boolean containsMember(String memberId) {
        return memberIds.contains(memberId);
    }

    public String getMentionString(GuildInteractionHandler handler) {
        StringBuilder builder = new StringBuilder();
        for (String memberId : memberIds) {
            builder.append(handler.getMemberById(memberId).getMention()).append(" ");
        }
        return builder.toString();
    }
}
