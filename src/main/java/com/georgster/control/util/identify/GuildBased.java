package com.georgster.control.util.identify;

import discord4j.core.object.entity.Guild;


/**
 * An {@link IdBased} object that is identified by a {@link Guild} Id.
 */
public abstract class GuildBased implements IdBased {
    private String guildId;

    protected GuildBased(String guildId) {
        this.guildId = guildId;
    }

    protected GuildBased(Guild guild) {
        this.guildId = guild.getId().asString();
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return guildId;
    }
}
