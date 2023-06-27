package com.georgster.control.manager;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.database.UserProfile;

/**
 * Manages all {@link UserProfile}s for a {@link com.georgster.control.SoapClient}.
 */
public class UserProfileManager extends SoapManager<UserProfile> {
    
    /**
     * Creates a new UserProfileManager for the given SoapClient's {@link ClientContext}.
     * 
     * @param context The context of the SoapClient for this manager.
     */
    public UserProfileManager(ClientContext context) {
        super(context, ProfileType.PROFILES, UserProfile.class, "memberId");
    }
}
