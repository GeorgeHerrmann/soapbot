package com.georgster.util;

import java.util.List;

import com.georgster.control.SoapEventManager;
import com.georgster.profile.ProfileHandler;

import discord4j.core.event.EventDispatcher;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;

/**
 * A GuildManager holds and manipulates data about a Guild.
 * Each Guild SOAP Bot is in has its own GuildManager, any
 * time a Guild is updated, the GuildManager should be updated
 * with the updateGuild() method.
 */
public class GuildManager {
    private Guild guild; //The guild that this GuildManager is managing
    private Channel activeChannel; //The channel that is currently active
    private SoapEventManager eventManager; //The event manager for this guild
    private EventDispatcher dispatcher; //The event dispatcher for this guild

    /**
     * Creates a new GuildManager for the given guild.
     * 
     * @param guild the guild to manage
     */
    public GuildManager(Guild guild) {
        this.guild = guild;
    }

    /**
     * Creates a new GuildManager for the given guild.
     * 
     * @param guild the guild to manage
     * @param eventManager the event manager for this guild
     */
    public GuildManager(Guild guild, SoapEventManager eventManager) {
        this.guild = guild;
        this.eventManager = eventManager;
    }

    /**
     * Creates a new GuildManager for the given guild.
     * 
     * @param guild the guild to manage
     * @param dispatcher the event dispatcher for this guild
     */
    public GuildManager(Guild guild, EventDispatcher dispatcher) {
        this.guild = guild;
        this.dispatcher = dispatcher;
    }

    /**
     * Creates a new GuildManager for the given guild.
     * 
     * @param guild the guild to manage
     * @param eventManager the event manager for this guild
     * @param dispatcher the event dispatcher for this guild
     */
    public GuildManager(Guild guild, SoapEventManager eventManager, EventDispatcher dispatcher) {
        this.guild = guild;
        this.eventManager = eventManager;
        this.dispatcher = dispatcher;
    }

    /**
     * Returns the guild that this GuildManager is managing.
     * 
     * @return the guild that this GuildManager is managing
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Updates this GuildManager's guild to the Updated Guild.
     * This method should be called when data in the guild has been updated,
     * i.e a new member joining, a new role being created, etc.
     * 
     * @param updatedGuild the updated guild to replace the old Guild
     */
    public void updateGuild(Guild updatedGuild) {
        if (updatedGuild != null && updatedGuild.getId().equals(guild.getId())) {
            guild = updatedGuild;
        } else {
            throw new IllegalArgumentException("The updated guild's ID must match the guild this GuildManager is managing.");
        }
    }
    
    /**
     * Sets the channel that this manager is actively performing actions in.
     * 
     * @param channel the new active channel
     */
    public void setActiveChannel(Channel channel) {
        activeChannel = channel;
    }

    /**
     * Returns the active channel for this manager.
     * 
     * @return the channel that is currently active
     */
    public Channel getActiveChannel() {
        return activeChannel;
    }

    /**
     * Sends a text message to the active channel.
     * 
     * @param text the message to send
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendText(String text) throws IllegalStateException {
        if (activeChannel != null) {
            try {
                return ((TextChannel) activeChannel).createMessage(text).block();
            } catch (Exception e) {
                throw new IllegalStateException("There was an issue sending the message to the active channel.");
            }
        }
        return null;
    }

    /**
     * Sends a text message to the given text channel.
     * 
     * @param text the message to send
     * @param channel the channel to send the message to
     */
    public static Message sendText(String text, TextChannel channel) {
        if (channel != null) {
            return channel.createMessage(text).block();
        }
        return null;
    }

    /**
     * Returns a list of all the channels in the guild.
     * 
     * @return a list of all the channels in the guild
     */
    public List<GuildChannel> getAllChannels() {
        return guild.getChannels().collectList().block();
    }

    /**
     * Returns a list of all the text channels in the guild.
     * 
     * @return a list of all the text channels in the guild
     */
    public List<TextChannel> getTextChannels() {
        return guild.getChannels().ofType(TextChannel.class).collectList().block();
    }

    /**
     * Returns a list of all the voice channels in the guild.
     * 
     * @return a list of all the voice channels in the guild
     */
    public List<VoiceChannel> getVoiceChannels() {
        return guild.getChannels().ofType(VoiceChannel.class).collectList().block();
    }

    /**
     * Returns a list of all the members in the guild.
     * 
     * @return a list of all the members in the guild
     */
    public List<Member> getAllMembers() {
        return guild.getMembers().collectList().block();
    }

    /**
     * Returns a list of all the roles in the guild.
     * 
     * @return a list of all the roles in the guild
     */
    public List<Role> getAllRoles() {
        return guild.getRoles().collectList().block();
    }

    /**
     * Returns the {@code Snowflake} ID of the guild.
     *  
     * @return the {@code Snowflake} ID of the guild
     */
    public String getId() {
        return guild.getId().asString();
    }

    /**
     * Returns the {@code Member} in this {@code Guild} that has the given tag.
     * 
     * @param memberTag The tag of the member to get
     * @return the {@code Member} in this {@code Guild} that has the given tag
     */
    public Member getMember(String memberTag) {
        for (Member member : getAllMembers()) {
            if (member.getTag().equals(memberTag))
                return member;
        }
        return null;
    }

    /**
     * Returns the {@code TextChannel} in this {@code Guild} that has the given name.
     * 
     * @param channelName The name of the channel to get
     * @return the {@code TextChannel} in this {@code Guild} that has the given name
     */
    public TextChannel getTextChannel(String channelName) {
        for (TextChannel channel : getTextChannels()) {
            if (channel.getType() == Channel.Type.GUILD_TEXT && channel.getName().equals(channelName)) //Only really used to match text channels
                return channel;
        }
        return null;
    }

    /**
     * Returns a {@code ProfileHandler} to manage this guild's profiles.
     * 
     * @return a {@code ProfileHandler} to manage this guild's profiles
     */
    public ProfileHandler getProfileHandler() {
        return new ProfileHandler(guild.getId().asString());
    }

    /**
     * Returns the {@code SoapEventManager} associated with this guild.
     * 
     * @return the {@code SoapEventManager} associated with this guild
     */
    public SoapEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Returns the {@code EventDispatcher} associated with this guild.
     * 
     * @return the {@code EventDispatcher} associated with this guild
     */
    public EventDispatcher getEventDispatcher() {
        return dispatcher;
    }
}
