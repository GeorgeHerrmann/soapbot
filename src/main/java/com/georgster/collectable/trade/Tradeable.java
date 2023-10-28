package com.georgster.collectable.trade;

import com.georgster.profile.UserProfile;

/**
 * An object that can be traded between {@link UserProfile UserProfiles}.
 */
public interface Tradeable {
    /**
     * Trades this {@link Tradeable} from the {@code owner's} {@link UserProfile}
     * to the {@code reciever reciever's} {@link UserProfile}.
     * 
     * @param owner The {@link UserProfile} of the {@link UserProfile} who owns this {@link Tradeable}.
     * @param reciever The {@link UserProfile} of the {@link UserProfile} who is recieving this {@link Tradeable}.
     */
    public void trade(UserProfile owner, UserProfile reciever);
}
