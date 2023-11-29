package com.georgster.control.manager;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.mentiongroups.MentionGroup;

/**
 * Manages all {@link MentionGroup MentionGroups} for a SOAP Client.
 */
public final class MentionGroupManager extends GuildedSoapManager<MentionGroup> {
    
    /**
     * Creates a new MentionGroupManager with the given {@link ClientContext}.
     * 
     * @param context The context of the {@link SoapClient} that created this {@link MentionGroupManager}.
     */
    public MentionGroupManager(ClientContext context) {
        super(context, ProfileType.MENTIONGROUPS, MentionGroup.class, "name");
    }

}
