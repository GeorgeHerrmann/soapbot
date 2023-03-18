package com.georgster.control;

import com.georgster.control.util.ClientPipeline;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.music.components.AudioInterface;
import com.georgster.profile.ProfileHandler;
import com.georgster.profile.UserProfile;
import com.georgster.util.GuildManager;

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
    private final AudioInterface audioInterface;
    private final CommandRegistry registry;
    private final SoapEventManager eventManager;
    private final PermissionsManager permissionsManager;
    
    /**
     * Creates a new {@code SoapClient} for the associated {@code Guild} represented
     * by its unique {@code Snowflake}. Sets up an audio interface and
     * constructs a new {@code CommandRegistry} for this client.
     */
    protected SoapClient(ClientPipeline pipeline) {
        flake = pipeline.getGuild().getId();
        audioInterface = new AudioInterface();
        eventManager = new SoapEventManager(pipeline.getGuild());
        permissionsManager = new PermissionsManager(pipeline);
        pipeline.setAudioInterface(audioInterface);
        pipeline.setEventManager(eventManager);
        pipeline.setPermissionsManager(permissionsManager);
        registry = new CommandRegistry(pipeline);
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
        /* Though we could have the client itself distribute GuildManagers, we would still have to update it on each event fire
        to ensure it has up to date Guild information, so it makes more sense to just make a new one with the Guild in the event */
        GuildManager manager = new GuildManager(event.getGuild());
    
        MultiLogger logger = new MultiLogger(manager, getClass());
        logger.append("Logging in to server: " + manager.getGuild().getName() + "\n", LogDestination.NONAPI);
        ProfileHandler handler = manager.getProfileHandler();

        eventManager.restartEvents();

        permissionsManager.setupBasic();

        logger.append("\n\t Restarted " + eventManager.getEventCount() + " events for " + manager.getGuild().getName() + "\n", LogDestination.NONAPI);

        if (!handler.serverProfileExists()) { //If the guild this event was fired from does not have a profile scheme, or has an out of date profile scheme, we create one
          logger.append("\n\t Updating Server Profile for " + manager.getGuild().getName(), LogDestination.NONAPI);
          handler.createServerProfile();
        }
        manager.getAllMembers().forEach(member -> {
          String id = member.getId().asString();
          if (!handler.userProfileExists(id)) {
            handler.createUserProfile(id);
          }
          handler.updateUserProfile(new UserProfile(manager.getId(), id, member.getUsername())); //We will always update the user's profile to make sure it is up to date
        });
        logger.append("\n\t Updated " + manager.getAllMembers().size() + " User Profiles for " + manager.getGuild().getName(), LogDestination.NONAPI);
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
    public AudioInterface getAudioInterface() {
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
