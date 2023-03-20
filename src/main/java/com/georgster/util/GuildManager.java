package com.georgster.util;

import java.util.List;

import com.georgster.profile.ProfileHandler;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

/**
 * A GuildManager holds and manipulates data about a Guild.
 * Each Guild SOAP Bot is in has its own GuildManager, any
 * time a Guild is updated, the GuildManager should be updated
 * with the updateGuild() method.
 */
public class GuildManager {
    private Guild guild; //The guild that this GuildManager is managing
    private Channel activeChannel; //The channel that is currently active
    private ChatInputInteractionEvent activeInteraction; //The active interaction
    private SelectMenuInteractionEvent activeSelectMenuInteraction; //The active select menu interaction

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
     * Sets the channel that this manager is actively performing actions in.
     * 
     * @param channel the new active channel
     */
    public void setActiveChannel(Channel channel) {
        activeChannel = channel;
    }

    /**
     * Sets the interaction event that this manager is actively performing actions for.
     * If there is an interaction active, the manager will reply to the interaction instead
     * of sending a message to the active channel on {@link #sendText(String)}.
     * 
     * @param interaction the new active interaction event
     */
    public void setActiveInteraction(ChatInputInteractionEvent interaction) {
        activeInteraction = interaction;
    }

    /**
     * Sets the select menu interaction event that this manager is actively performing actions for.
     * 
     * @param interaction the new active select menu interaction event
     */
    public void setActiveSelectMenuInteraction(SelectMenuInteractionEvent interaction) {
        activeSelectMenuInteraction = interaction;
    }

    /**
     * Returns the active channel for this manager with basic embed formatting.
     * 
     * @return the channel that is currently active
     */
    public Channel getActiveChannel() {
        return activeChannel;
    }

    /**
     * Returns the active interaction event for this manager.
     * 
     * @return the interaction that is currently active
     */
    public ChatInputInteractionEvent getActiveInteraction() {
        return activeInteraction;
    }

    /**
     * Returns the active select menu interaction event for this manager.
     * 
     * @return the select menu interaction that is currently active
     */
    public SelectMenuInteractionEvent getActiveSelectMenuInteraction() {
        return activeSelectMenuInteraction;
    }

    /**
     * Kills the active interaction event for this manager, setting it to null.
     */
    public void killActiveInteraction() {
        activeInteraction = null;
    }

    /**
     * Kills the active select menu interaction event for this manager, setting it to null.
     */
    public void killActiveSelectMenuInteraction() {
        activeSelectMenuInteraction = null;
    }

    /**
     * Sends a text message with only content to the active channel.
     * 
     * 
     * @param text the message to send
     * @return the message that was sent
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendPlainText(String text) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                activeInteraction.reply(text).block();
                Message message = activeInteraction.getReply().block();
                killActiveInteraction();
                return message;
            } catch (Exception e) {
                throw new IllegalStateException("There was an issue replying to the event.");
            }
        } else {
            if (activeChannel != null) {
                try {
                    return ((TextChannel) activeChannel).createMessage(text).block();
                } catch (NullPointerException e) {
                    throw new IllegalStateException("There was an issue sending the message to the active channel.");
                }
            }
        }
        return null;
    }

    /**
     * Sends a text message to the active channel with basic embed formatting.
     * 
     * @param text the message to send
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendText(String text) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                activeInteraction.reply(spec).block();
                Message message = activeInteraction.getReply().block();
                killActiveInteraction();
                return message;
            } catch (Exception e) {
                throw new IllegalStateException("There was an issue replying to the event.");
            }
        } else {
            if (activeChannel != null) {
                try {
                    EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
                    MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
                    return ((TextChannel) activeChannel).createMessage(spec).block();
                } catch (NullPointerException e) {
                    throw new IllegalStateException("There was an issue sending the message to the active channel.");
                }
            }
        }
        return null;
    }

    /**
     * Sends a text message to the active channel with basic embed formatting and a title.
     * 
     * @param text the message to send
     * @param title the title of the message
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendText(String text, String title) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                activeInteraction.reply(spec).block();
                Message message = activeInteraction.getReply().block();
                killActiveInteraction();
                return message;
            } catch (Exception e) {
                throw new IllegalStateException("There was an issue replying to the event.");
            }
        } else {
            if (activeChannel != null) {
                try {
                    EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
                    MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
                    return ((TextChannel) activeChannel).createMessage(spec).block();
                } catch (NullPointerException e) {
                    throw new IllegalStateException("There was an issue sending the message to the active channel.");
                }
            }
        }
        return null;
    }

    /**
     * Sends a text message to the active channel with basic embed formatting, a title, and a component.
     * 
     * @param text the message to send
     * @param title the title of the message
     * @param component the component to add to the message
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendText(String text, String title, LayoutComponent component) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).addComponent(component).build();
                activeInteraction.reply(spec).block();
                Message message = activeInteraction.getReply().block();
                killActiveInteraction();
                return message;
            } catch (Exception e) {
                throw new IllegalStateException("There was an issue replying to the event.");
            }
        } else {
            if (activeChannel != null) {
                try {
                    EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
                    MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(component);
                    return ((TextChannel) activeChannel).createMessage(spec).block();
                } catch (NullPointerException e) {
                    throw new IllegalStateException("There was an issue sending the message to the active channel.");
                }
            }
        }
        return null;
    }

    /**
     * Edits a text message with basic embed formatting.
     * If this manager has an active select menu interaction, it will edit that instead.
     * 
     * @param message the message to send
     * @param text the message to send
     */
    public Message editMessageContent(Message message, String text) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
        if (message != null) {
            if (activeSelectMenuInteraction != null) {
                Message returns = activeSelectMenuInteraction.getReply().block();
                killActiveSelectMenuInteraction();
                return returns;
            } else {
                return message.edit().withEmbeds(embed).block();
            }
        }
        return null;
    }

    /**
     * Edits a text message with basic embed formatting and a title.
     * If this manager has an active select menu interaction, it will edit that instead.
     * 
     * @param message the message to send
     * @param text the message to send
     * @param title the title of the message
     */
    public Message editMessageContent(Message message, String text, String title) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        if (message != null) {
            if (activeSelectMenuInteraction != null) {
                activeSelectMenuInteraction.edit().withEmbeds(embed).block();
                Message returns = activeSelectMenuInteraction.getReply().block();
                killActiveSelectMenuInteraction();
                return returns;
            } else {
                return message.edit().withEmbeds(embed).block();
            }
        }
        return null;
    }

    /**
     * Edits a text message with basic embed formatting, a title, and a component.
     * If this manager has an active select menu interaction, it will edit that instead.
     * 
     * @param message the message to send
     * @param text the message to send
     * @param title the title of the message
     * @param component the component to add to the message
     */
    public Message editMessageContent(Message message, String text, String title, LayoutComponent component) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        if (message != null) {
            if (activeSelectMenuInteraction != null) {
                activeSelectMenuInteraction.edit().withEmbeds(embed).withComponents(component).block();
                Message returns = activeSelectMenuInteraction.getReply().block();
                killActiveSelectMenuInteraction();
                return returns;
            } else {
                return message.edit().withEmbeds(embed).withComponents(component).block();
            }
        }
        return null;
    }

    /**
     * Sends a text message to the provided text channel with only content.
     * 
     * @param text the message to send
     * @param channel the channel to send the message to
     * @return the message that was sent
     */
    public static Message sendPlainText(String text, TextChannel channel) {
        if (channel != null) {
            return channel.createMessage(text).block();
        }
        return null;
    }

    /**
     * Sends a text message to the given text channel with basic embed formatting.
     * 
     * @param text the message to send
     * @param channel the channel to send the message to
     */
    public static Message sendText(String text, TextChannel channel) {
        if (channel != null) {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
            MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
            return channel.createMessage(spec).block();
        }
        return null;
    }

    /**
     * Sends a text message to the given text channel with basic embed formatting and a title.
     * 
     * @param text the message to send
     * @param title the title of the message
     * @param channel the channel to send the message to
     */
    public static Message sendText(String text, String title, TextChannel channel) {
        if (channel != null) {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
            return channel.createMessage(spec).block();
        }
        return null;
    }

    /**
     * Sends a text message to the given text channel with basic embed formatting, a title, and a component.
     * 
     * @param text the message to send
     * @param title the title of the message
     * @param component the component to add to the message
     * @param channel the channel to send the message to
     */
    public static Message sendText(String text, String title, LayoutComponent component, TextChannel channel) {
        if (channel != null) {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(component);
            return channel.createMessage(spec).block();
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
     * Returns the {@code Role} in this {@code Guild} that has the given name.
     * 
     * @param roleName The name of the role to get
     * @return the {@code Role} in this {@code Guild} that has the given name
     */
    public Role getRole(String roleName) {
        for (Role role : getAllRoles()) {
            if (role.getName().equals(roleName))
                return role;
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
}
