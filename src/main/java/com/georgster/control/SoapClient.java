package com.georgster.control;

import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.util.ClientContext;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.AudioContext;
import com.georgster.profile.DatabaseService;
import com.georgster.profile.ProfileType;
import com.georgster.profile.UserProfile;
import com.georgster.util.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

/**
 * An aggregation of all the shard-specific objects that SOAP Bot needs to run for
 * a single {@code Guild}. Each SoapClient handles all the events that occur in
 * its associated {@code Guild} and houses its {@code CommandRegistry}.
 */
public final class SoapClient {
    private final Snowflake flake;
    private final AudioContext audioInterface;
    private final CommandRegistry registry;
    private final SoapEventManager eventManager;
    private final PermissionsManager permissionsManager;
    
    /**
     * Creates a new {@code SoapClient} for the associated {@code Guild} represented
     * by its unique {@code Snowflake}. Sets up an audio interface and
     * constructs a new {@code CommandRegistry} for this client.
     */
    protected SoapClient(ClientContext context) {
        flake = context.getGuild().getId();
        audioInterface = new AudioContext();
        eventManager = new SoapEventManager(context);
        permissionsManager = new PermissionsManager(context);
        context.setAudioInterface(audioInterface);
        context.setEventManager(eventManager);
        context.setPermissionsManager(permissionsManager);
        registry = new CommandRegistry(context);
        registry.registerGlobalCommands();
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

        GuildInteractionHandler handler = new GuildInteractionHandler(event.getGuild());
    
        MultiLogger logger = new MultiLogger(handler, getClass());
        logger.append("Logging in to server: " + handler.getGuild().getName() + "\n", LogDestination.NONAPI);

        DatabaseService<UserProfile> service = new DatabaseService<>(handler.getId(), ProfileType.PROFILES, UserProfile.class);

        eventManager.restartEvents();

        permissionsManager.setupBasic();

        logger.append("\n-  Restarted " + eventManager.getCount() + " events for " + handler.getGuild().getName() + "\n", LogDestination.NONAPI);

        logger.append("\n-  Updating Server Profile for " + handler.getGuild().getName(), LogDestination.NONAPI);

        handler.getAllMembers().forEach(member -> {
          String id = member.getId().asString();
          service.addObjectIfNotExists(new UserProfile(handler.getId(), id, member.getUsername()), "memberId", id);
          service.updateObjectIfExists(new UserProfile(handler.getId(), id, member.getUsername()), "memberId", id);
        });
        logger.append("\n- Updated " + handler.getAllMembers().size() + " User Profiles for " + handler.getGuild().getName(), LogDestination.NONAPI);
        logger.sendAll();
    }

    /**
     * Defines SOAP Bot's actions when a MessageCreateEvent is fired.
     * Here, we will send the message to the CommandRegistry to be parsed have it
     * execute the associated command if it is a valid command.
     * 
     * @param event The MessageCreateEvent that was fired.
     */
    protected void onMessageCreate(MessageCreateEvent event) {
        registry.getAndExecute(event, this);
    }

    /**
     * Defines SOAP Bot's actions when a ChatInputInteractionEvent is fired.
     * 
     * @param event The ChatInputInteractionEvent that was fired.
     */
    protected void onChatInputInteraction(ChatInputInteractionEvent event) {
        registry.getAndExecute(event, this);
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
     * Gets the {@code AudioInterface} for this SoapClient.
     * 
     * @return The AudioInterface for this SoapClient.
     */
    public AudioContext getAudioInterface() {
        return audioInterface;
    }

    /**
     * Gets the {@code SoapEventManager} that is managing the events for this
     * {@code SoapClient's} associated {@code Guild}.
     * 
     * @return The SoapEventManager for this SoapClient.
     */
    public SoapEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the {@code PermissionsManager} that is managing the permissions for this
     * {@code SoapClient}.
     * 
     * @return The PermissionsManager for this SoapClient.
     */
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    /**
     * Gets the CommandRegistry for this SoapClient.
     * 
     * @return The CommandRegistry for this SoapClient.
     */
    public CommandRegistry getRegistry() {
        return registry;
    }
}
