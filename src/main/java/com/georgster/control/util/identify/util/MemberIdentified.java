package com.georgster.control.util.identify.util;

import com.georgster.control.manager.Manageable;
import com.georgster.control.util.identify.MemberBased;

import discord4j.core.object.entity.Member;

/**
 * A {@link Manageable} that is identified by a {@link Member} ID.
 */
public abstract class MemberIdentified extends MemberBased implements Manageable {
    protected MemberIdentified(String memberId) {
        super(memberId);
    }

    protected MemberIdentified(Member member) {
        super(member);
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return getId();
    }
}
