package com.georgster.util;

import java.util.List;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
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

    /**
     * Creates a new GuildManager for the given guild.
     * 
     * @param guild the guild to manage
     */
    public GuildManager(Guild guild) {
        this.guild = guild;
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

}
