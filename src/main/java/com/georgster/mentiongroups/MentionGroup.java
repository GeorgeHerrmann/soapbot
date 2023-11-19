package com.georgster.mentiongroups;

import java.util.HashSet;
import java.util.Set;

import com.georgster.control.manager.Manageable;
import com.georgster.util.handler.GuildInteractionHandler;

/**
 * A {@link Manageable} that represents a group of members that can be mentioned at once.
 */
public final class MentionGroup implements Manageable {
    private final Set<String> memberIds; // Snowflake IDs of members
    private final String name; // Name of the group

    /**
     * Creates a new MentionGroup with the given name and members.
     * 
     * @param name     The name of the group
     * @param memberIds The IDs of the members in the group
     */
    public MentionGroup(String name, Set<String> memberIds) {
        this.name = name;
        this.memberIds = memberIds;
    }

    /**
     * Creates a new MentionGroup with the given name and no members.
     * 
     * @param name The name of the group
     */
    public MentionGroup(String name) {
        this.name = name;
        this.memberIds = new HashSet<>();
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return name;
    }

    /**
     * Returns a set of the IDs of the members in this group.
     * 
     * @return A set of the IDs of the members in this group
     */
    public Set<String> getMemberIds() {
        return memberIds;
    }

    /**
     * Adds a member to this group.
     * 
     * @param memberId The ID of the member to add
     */
    public void addMember(String memberId) {
        memberIds.add(memberId);
    }

    /**
     * Removes a member from this group.
     * 
     * @param memberId The ID of the member to remove
     */
    public void removeMember(String memberId) {
        memberIds.remove(memberId);
    }

    /**
     * Returns whether or not this group contains a member with the given ID.
     * 
     * @param memberId The ID of the member to check for
     * @return Whether or not this group contains a member with the given ID
     */
    public boolean containsMember(String memberId) {
        return memberIds.contains(memberId);
    }

    /**
     * Returns a string that mentions all members in this group.
     * 
     * @param handler The {@link GuildInteractionHandler} to use to get the members
     * @return A string that mentions all members in this group
     */
    public String getMentionString(GuildInteractionHandler handler) {
        StringBuilder builder = new StringBuilder();
        for (String memberId : memberIds) {
            builder.append(handler.getMemberById(memberId).getMention()).append(" ");
        }
        return builder.toString();
    }
}
