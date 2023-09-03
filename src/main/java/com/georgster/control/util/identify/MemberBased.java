package com.georgster.control.util.identify;

import discord4j.core.object.entity.Member;

/**
 * An {@link IdBased} object that is identified by a {@link Member} Id.
 */
public abstract class MemberBased implements IdBased {
    private String memberId;

    protected MemberBased(String memberId) {
        this.memberId = memberId;
    }

    protected MemberBased(Member member) {
        this.memberId = member.getId().asString();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return memberId;
    }
}
