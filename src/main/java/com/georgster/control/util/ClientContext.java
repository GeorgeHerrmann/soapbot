package com.georgster.control.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.georgster.control.CommandRegistry;
import com.georgster.control.manager.CollectableManager;
import com.georgster.control.manager.Manageable;
import com.georgster.control.manager.MentionGroupManager;
import com.georgster.control.manager.PermissionsManager;
import com.georgster.control.manager.SoapEventManager;
import com.georgster.control.manager.SoapManager;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.manager.UserSettingsManager;
import com.georgster.music.components.AudioContext;

import discord4j.core.event.EventDispatcher;
import discord4j.core.object.entity.Guild;
import discord4j.rest.RestClient;

/**
 * A context of objects from the {@code SoapClientManager} and a {@code SoapClient}
 * for all client subsystems to use.
 */
public class ClientContext {
    private final EventDispatcher dispatcher;
    private final Guild guild;
    private final Set<SoapManager<? extends Manageable>> managers;
    private AudioContext audioContext;
    private CommandRegistry commandRegistry;
    private RestClient restClient;

    /**
     * Creates a new ClientContext with the given {@link EventDispatcher} and
     * {@link Guild}.
     * 
     * @param dispatcher the event dispatcher
     * @param guild     the guild
     */
    public ClientContext(EventDispatcher dispatcher, Guild guild) {
        this.dispatcher = dispatcher;
        this.guild = guild;
        this.managers = new HashSet<>();
    }

    /**
     * Creates a new ClientContext with the given {@link EventDispatcher},
     * {@link Guild}, and {@link RestClient}.
     * 
     * @param dispatcher    the event dispatcher
     * @param guild        the guild
     * @param restClient    the rest client
     */
    public ClientContext(EventDispatcher dispatcher, Guild guild, RestClient restClient) {
        this.dispatcher = dispatcher;
        this.guild = guild;
        this.restClient = restClient;
        this.managers = new HashSet<>();
    }

    /**
     * Returns the event dispatcher.
     * 
     * @return the event dispatcher
     */
    public EventDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Returns the guild.
     * 
     * @return the guild
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Returns the audio context.
     * 
     * @return the audio context
     */
    public AudioContext getAudioContext() {
        return audioContext;
    }

    /**
     * Returns the event manager.
     * 
     * @return the event manager
     */
    public SoapEventManager getEventManager() {
        return (SoapEventManager) managers.stream().filter(SoapEventManager.class::isInstance).findFirst().orElse(null);
    }

    /**
     * Returns the permissions manager.
     * 
     * @return the permissions manager
     */
    public PermissionsManager getPermissionsManager() {
        return (PermissionsManager) managers.stream().filter(PermissionsManager.class::isInstance).findFirst().orElse(null);
    }

    /**
     * Returns the UserProfileManager.
     * 
     * @return the user profile manager.
     */
    public UserProfileManager getUserProfileManager() {
        return (UserProfileManager) managers.stream().filter(UserProfileManager.class::isInstance).findFirst().orElse(null);
    }

    /**
     * Returns the CollectableManager.
     * 
     * @return the collectable manager.
     */
    public CollectableManager getCollectableManager() {
        return (CollectableManager) managers.stream().filter(CollectableManager.class::isInstance).findFirst().orElse(null);
    }

    /**
     * Returns the MentionGroupManager
     * 
     * @return the mention group manager.
     */
    public MentionGroupManager getMentionGroupManager() {
        return (MentionGroupManager) managers.stream().filter(MentionGroupManager.class::isInstance).findFirst().orElse(null);
    }

    /**
     * Returns the global UserSettingsManager
     * 
     * @return The global user settings manager.
     */
    public UserSettingsManager getUserSettingsManager() {
        return (UserSettingsManager) managers.stream().filter(UserSettingsManager.class::isInstance).findFirst().orElse(null);
    }

    /**
     * Sets the audio context.
     * 
     * @param audioInterface the audio context
     */
    public void setAudioContext(AudioContext audioContext) {
        this.audioContext = audioContext;
    }

    /**
     * Adds the given {@link GuildedSoapManager}s to this context. Each {@link ClientContext} for
     * its {@link SoapClient} can only have one SoapManager for each type.
     * 
     * @param soapManagers The managers to add.
     */
    public void addManagers(SoapManager<?>... soapManagers) {
        for (SoapManager<?> manager : soapManagers) {
            this.managers.add(manager);
        }
    }

    /**
     * Executes the given consumer with each {@link GuildedSoapManager} in this context;
     * 
     * @param action The action to perform with each manager.
     */
    public void forEachManager(Consumer<SoapManager<? extends Manageable>> action) {
        managers.forEach(action::accept);
    }

    /**
     * Returns the rest client.
     * 
     * @return the rest client
     */
    public RestClient getRestClient() {
        return restClient;
    }

    /**
     * Sets the command registry.
     * 
     * @param commandRegistry the command registry
     */
    public void setCommandRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * Returns the command registry.
     * 
     * @return the command registry
     */
    public CommandRegistry getCommandRegistry() {
         return commandRegistry;
    }
}
