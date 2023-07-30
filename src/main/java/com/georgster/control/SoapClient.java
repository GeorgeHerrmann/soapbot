package com.georgster.control;

import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.manager.SoapManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.database.UserProfile;
import com.georgster.gpt.MemberChatCompletions;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.AudioContext;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * An aggregation of all the shard-specific objects that SOAP Bot needs to run for
 * a single {@code Guild}. Each SoapClient handles all the events that occur in
 * its associated {@code Guild}, houses its {@code CommandRegistry} and has their own Set of {@link SoapManager}s.
 */
public final class SoapClient {
    private final Snowflake flake;
    private final ClientContext context;
    
    /**
     * Creates a new {@code SoapClient} for the associated {@code Guild} represented
     * by its unique {@code Snowflake} and initializes its systems.
     */
    protected SoapClient(ClientContext context) {
        flake = context.getGuild().getId();
        this.context = context;
        this.context.setAudioContext(new AudioContext());
        this.context.addManagers(new SoapEventManager(context),
                           new PermissionsManager(context),
                           new UserProfileManager(context));
        this.context.setCommandRegistry(new CommandRegistry(context));
        this.context.getCommandRegistry().registerGlobalCommands();
    }

    /**
     * Defines SOAP Bot's actions when a GuildCreateEvent is fired.
     * Here, we will check if the associated guild and all its members 
     * have up to date profile schemes, and if not, we will create them.
     * 
     * @param event The GuildCreateEvent that was fired.
     */
    protected void onGuildCreate(GuildCreateEvent event) {
        ThreadPoolFactory.createThreadPoolManager(event.getGuild().getId());
    
        MultiLogger logger = new MultiLogger(new GuildInteractionHandler(event.getGuild()), getClass());
        logger.append("Logging in to server: " + context.getGuild().getName() + "\n", LogDestination.NONAPI);

        this.context.forEachManager(SoapManager::load);
        logger.append("- Initialized " + context.getGuild().getName() + "'s management system\n", LogDestination.NONAPI);

        context.getUserProfileManager().updateFromEvent(event);
        logger.append("- Updated all user profiles", LogDestination.NONAPI);

        logger.sendAll();
    }

    /**
     * Defines SOAP Bot's actions when a MemberJoinEvent is fired.
     * 
     * @param event The MemberJoinEvent that was fired.
     */
    protected void onMemberJoin(MemberJoinEvent event) {
        String guildId = flake.asString();
        String memberId = event.getMember().getId().asString();
        String username = event.getMember().getTag();
        MemberChatCompletions completions = new MemberChatCompletions(memberId);
        UserProfile profile = new UserProfile(guildId, memberId, username, completions);
        context.getUserProfileManager().add(profile);
    }

    /**
     * Defines SOAP Bot's actions when a MessageCreateEvent is fired.
     * Here, we will send the message to the CommandRegistry to be parsed have it
     * execute the associated command if it is a valid command.
     * 
     * @param event The MessageCreateEvent that was fired.
     */
    protected void onMessageCreate(MessageCreateEvent event) {
        this.context.getCommandRegistry().getAndExecute(event);
    }

    /**
     * Defines SOAP Bot's actions when a ChatInputInteractionEvent is fired.
     * 
     * @param event The ChatInputInteractionEvent that was fired.
     */
    protected void onChatInputInteraction(ChatInputInteractionEvent event) {
        this.context.getCommandRegistry().getAndExecute(event);
    }

    /**
     * Gets the {@code Snowflake} for the Guild this SoapClient is controlling.
     * 
     * @return the {@code Snowflake} for the Guild this SoapClient is controlling.
     */
    protected Snowflake getSnowflake() {
        return flake;
    }

    /**
     * Returns the context of this {@link SoapClient}.
     * 
     * @return the context of this client.
     */
    protected ClientContext getContext() {
        return this.context;
    }
}
