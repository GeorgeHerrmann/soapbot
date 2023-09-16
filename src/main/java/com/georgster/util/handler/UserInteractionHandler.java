package com.georgster.util.handler;

import java.util.Optional;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;

/**
 * An {@link InteractionHandler} for a Discord {@link User}.
 * <p>
 * The {@link User User's} {@link PrivateChannel} is this handler's
 * default active {@link MessageChannel}.
 */
public final class UserInteractionHandler extends InteractionHandler {
    private final User user;

    /**
     * Creates a new {@link UserInteractionHandler} from the provided
     * {@link User}, and sets the active {@link MessageChannel} to
     * the user's {@link PrivateChannel}.
     * 
     * @param user The {@link User} to handle interactions for.
     */
    public UserInteractionHandler(User user) {
        super(user.getId());
        this.user = user;
        setActiveMessageChannel(user.getPrivateChannel().block());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * A {@link UserInteractionHandler} will only accept a {@link PrivateChannel}.
     * 
     * @param messageChannel The new {@code activeChannel}.
     */
    @Override
    public void setActiveMessageChannel(MessageChannel messageChannel) {
        if (messageChannel instanceof PrivateChannel) {
            this.activeChannel = Optional.of(messageChannel);
        } else {
            throw new IllegalArgumentException("Only a PrivateChannel may be used for a UserInteractionHandler");
        }
    }

    /**
     * Returns the {@link User} this handler is handling interactions for.
     * 
     * @return The {@link User} this handler is handling interactions for.
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns this handler's {@link User} as a {@link Member} from
     * the {@code Guild} of the provided String-based {@link Snowflake} {@code guildId}.
     * 
     * @param guildId The String-based {@link Snowflake} id.
     * @return A {@link Member} from the associated {@code Guild}.
     */
    public Member getAsMember(String guildId) {
        return user.asMember(Snowflake.of(guildId)).block();
    }

    /**
     * Returns this handler's {@link User} as a {@link Member} from
     * the {@code Guild} of the provided {@link Snowflake} {@code guildId}.
     * 
     * @param guildId The {@link Snowflake} id.
     * @return A {@link Member} from the associated {@code Guild}.
     */
    public Member getAsMember(Snowflake guildId) {
        return user.asMember(guildId).block();
    }


}
