package com.georgster.control;

import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.manager.SoapManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.AudioContext;
import com.georgster.profile.UserProfile;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.role.RoleCreateEvent;
import discord4j.core.event.domain.role.RoleUpdateEvent;

/**
 * An aggregation of all the shard-specific objects that SOAP Bot needs to run for
 * a single {@code Guild}. Each SoapClient handles all the events that occur in
 * its associated {@code Guild}, houses its {@link CommandRegistry} and has their own Set of {@link SoapManager SoapManagers}.
 */
public final class SoapClient {
    private final Snowflake flake;
    private final ClientContext context;
    
    /**
     * Creates a new {@link SoapClient} for the associated {@code Guild} represented
     * by its unique {@link Snowflake} and initializes its systems.
     * 
     * @param context The context of the {@link SoapClientManager} that created this {@link SoapClient}.
     */
    protected SoapClient(ClientContext context) {
        flake = context.getGuild().getId();
        this.context = context;
        this.context.setAudioContext(new AudioContext());
        this.context.addManagers(new SoapEventManager(context),
                           new PermissionsManager(context),
                           new UserProfileManager(context),
                           new CollectableManager(context));
        this.context.setCommandRegistry(new CommandRegistry(context));
        this.context.getCommandRegistry().registerGlobalCommands();
    }

    /**
     * Defines SOAP Bot's actions when a {@link GuildCreateEvent} is fired.
     * <p>
     * Upon the firing of a {@link GuildCreateEvent}, SOAP Bot will initialize
     * all its {@link SoapManager SoapManagers} and update all {@link UserProfile UserProfiles}
     * for the {@code Guild} in the event.
     * 
     * @param event The {@link GuildCreateEvent} that was fired.
     */
    protected void onGuildCreate(GuildCreateEvent event) {
        ThreadPoolFactory.createThreadPoolManager(event.getGuild());
    
        MultiLogger logger = new MultiLogger(new GuildInteractionHandler(event.getGuild()), getClass());
        logger.append("Logging in to server: " + context.getGuild().getName() + "\n", LogDestination.NONAPI);

        this.context.forEachManager(SoapManager::load);
        logger.append("- Initialized " + context.getGuild().getName() + "'s management system\n", LogDestination.NONAPI);

        context.getUserProfileManager().updateFromEvent(event);
        logger.append("- Updated all user profiles", LogDestination.NONAPI);

        CollectableManager t = this.context.getCollectableManager();
        t.getAll().forEach(t::update);

        logger.sendAll();
    }

    /**
     * Defines SOAP Bot's actions when a {@link MemberJoinEvent} is fired.
     * <p>
     * Upon the firing of a {@link MemberJoinEvent}, SOAP Bot will create a new
     * {@link UserProfile} for the {@link discord4j.core.object.entity.Member Member}.
     * 
     * @param event The MemberJoinEvent that was fired.
     */
    protected void onMemberJoin(MemberJoinEvent event) {
        String guildId = flake.asString();
        String memberId = event.getMember().getId().asString();
        String username = event.getMember().getTag();
        UserProfile profile = new UserProfile(guildId, memberId, username);
        context.getUserProfileManager().add(profile);
    }

    /**
     * Defines SOAP Bot's actions when a {@link MessageCreateEvent} is fired.
     * <p>
     * Upon the firing of a {@link MessageCreateEvent}, SOAP Bot will distribute
     * the event to its {@link CommandRegistry} to be handled.
     * 
     * @param event The {@link MessageCreateEvent} that was fired.
     */
    protected void onMessageCreate(MessageCreateEvent event) {
        this.context.getCommandRegistry().getAndExecute(event);
    }

    /**
     * Defines SOAP Bot's actions when a {@link RoleUpdateEvent} is fired.
     * <p>
     * Upon the firing of a {@link RoleUpdateEvent}, SOAP Bot will update its
     * {@link PermissionsManager} to reflect the changes made to the {@link discord4j.core.object.entity.Role Role}.
     * 
     * @param event The {@link RoleUpdateEvent} that was fired.
     */
    protected void onRoleUpdate(RoleUpdateEvent event) {
        this.context.getPermissionsManager().updateFromEvent(event);
    }

    /**
     * Defines SOAP Bot's actions when a {@link RoleCreateEvent} is fired.
     * <p>
     * Upon the firing of a {@link RoleCreateEvent}, SOAP Bot will update its
     * {@link PermissionsManager} with the new {@link discord4j.core.object.entity.Role Role}.
     * 
     * @param event The {@link RoleCreateEvent} that was fired.
     */
    protected void onRoleCreate(RoleCreateEvent event) {
        this.context.getPermissionsManager().addFromEvent(event);
    }

    /**
     * Defines SOAP Bot's actions when a {@link ChatInputInteractionEvent} is fired.
     * <p>
     * Upon the firing of a {@link ChatInputInteractionEvent}, SOAP Bot will distribute
     * the event to its {@link CommandRegistry} to be handled.
     * 
     * @param event The {@link ChatInputInteractionEvent} that was fired.
     */
    protected void onChatInputInteraction(ChatInputInteractionEvent event) {
        this.context.getCommandRegistry().getAndExecute(event);
    }

    /**
     * Gets the {@link Snowflake} for the Guild this {@link SoapClient} is associated with.
     * 
     * @return the {@link Snowflake} for the Guild this {@link SoapClient} is associated with.
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
