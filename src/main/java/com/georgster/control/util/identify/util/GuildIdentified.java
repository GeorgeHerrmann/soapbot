package com.georgster.control.util.identify.util;

import com.georgster.control.manager.Manageable;
import com.georgster.control.util.identify.GuildBased;

import discord4j.core.object.entity.Guild;

/**
 * A {@link Manageable} that is identified by a {@link Guild} ID.
 */
public abstract class GuildIdentified extends GuildBased implements Manageable {
    protected GuildIdentified(String guildId) {
        super(guildId);
    }

    protected GuildIdentified(Guild guild) {
        super(guild);
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return getId();
    }
}
