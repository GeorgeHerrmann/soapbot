package com.georgster.control.util;

import com.georgster.control.CommandRegistry;
import com.georgster.control.PermissionsManager;
import com.georgster.control.SoapEventManager;
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
    private AudioContext audioContext;
    private SoapEventManager eventManager;
    private PermissionsManager permissionsManager;
    private CommandRegistry commandRegistry;
    private RestClient restClient;

    /**
     * Creates a new ClientContext with the given {@code EventDispatcher} and
     * {@code Guild}.
     * 
     * @param dispatcher the event dispatcher
     * @param guild     the guild
     */
    public ClientContext(EventDispatcher dispatcher, Guild guild) {
        this.dispatcher = dispatcher;
        this.guild = guild;
    }

    /**
     * Creates a new ClientContext with the given {@code EventDispatcher},
     * {@code Guild}, and {@code RestClient}.
     * 
     * @param dispatcher    the event dispatcher
     * @param guild        the guild
     * @param restClient    the rest client
     */
    public ClientContext(EventDispatcher dispatcher, Guild guild, RestClient restClient) {
        this.dispatcher = dispatcher;
        this.guild = guild;
        this.restClient = restClient;
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
        return eventManager;
    }

    /**
     * Returns the permissions manager.
     * 
     * @return the permissions manager
     */
    public PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    /**
     * Sets the audio context.
     * 
     * @param audioInterface the audio context
     */
    public void setAudioInterface(AudioContext audioContext) {
        this.audioContext = audioContext;
    }

    /**
     * Sets the event manager.
     * 
     * @param eventManager the event manager
     */
    public void setEventManager(SoapEventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Sets the permissions manager.
     * 
     * @param permissionsManager the permissions manager
     */
    public void setPermissionsManager(PermissionsManager permissionsManager) {
        this.permissionsManager = permissionsManager;
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
