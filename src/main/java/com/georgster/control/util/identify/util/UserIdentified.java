package com.georgster.control.util.identify.util;

import com.georgster.control.manager.Manageable;
import com.georgster.control.util.identify.UserBased;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;

/**
 * A {@link Manageable} that is identified by a {@link User} ID.
 */
public abstract class UserIdentified extends UserBased implements Manageable {
    protected UserIdentified(String userId) {
        super(userId);
    }

    protected UserIdentified(User user) {
        super(user);
    }

    protected UserIdentified(Member member) {
        super(member);
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return getId();
    }
}
