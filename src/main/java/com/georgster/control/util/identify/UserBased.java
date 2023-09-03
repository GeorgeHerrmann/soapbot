package com.georgster.control.util.identify;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;

/**
 * An {@link IdBased} object that is identified by a {@link User} Id.
 */
public abstract class UserBased implements IdBased {
    private String userId;

    protected UserBased(String userId) {
        this.userId = userId;
    }

    protected UserBased(User user) {
        this.userId = user.getId().asString();
    }

    protected UserBased(Member member) {
        this.userId = ((User) member).getId().asString();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return userId;
    }
}
