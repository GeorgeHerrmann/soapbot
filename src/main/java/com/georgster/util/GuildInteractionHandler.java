package com.georgster.util;

import java.util.List;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
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
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

/**
 * Handles SOAP Bot's interactions with a Discord {@code Guild}.
 */
public class GuildInteractionHandler {
    private Guild guild; //The guild being interacted with
    private Channel activeChannel; //The channel that is currently active
    private ChatInputInteractionEvent activeInteraction; //The active interaction
    private ComponentInteractionEvent activeComponentInteraction; //The active select menu interaction

    private boolean deferReply; //If a reply was deferred elsewhere, requiring a reply edit

    /**
     * Creates a new GuildInteractionHandler for the given guild.
     * 
     * @param guild the guild to handle interactions for
     */
    public GuildInteractionHandler(Guild guild) {
        this.guild = guild;
        this.deferReply = false;
    }

    /**
     * Switches this handler to reply deferring mode.
     * If reply deferring mode is on, this handler will edit replies from
     * {@code ApplicationCommandInteractionEvents}, and will automatically
     * disable once a successful defer edit has been made.
     */
    public void enableReplyDeferring() {
        this.deferReply = true;
    }

    /**
     * Returns the guild for this GuildInteractionHandler.
     * 
     * @return the guild for this GuildInteractionHandler
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Updates this GuildInteractionHandler's guild to the Updated Guild.
     * 
     * @param updatedGuild the updated guild to replace the old Guild
     */
    public void setGuild(Guild updatedGuild) {
        if (updatedGuild != null && updatedGuild.getId().equals(guild.getId())) {
            guild = updatedGuild;
        } else {
            throw new IllegalArgumentException("The updated guild's ID must match the guild this GuildManager is managing.");
        }
    }
    
    /**
     * Sets the channel that this handler is actively performing actions in.
     * 
     * @param channel the new active channel
     */
    public void setActiveChannel(Channel channel) {
        activeChannel = channel;
    }

    /**
     * Sets the interaction event that this handler is actively performing actions for.
     * If there is an interaction active, the handler will reply to the interaction instead
     * of sending a message to the active channel on {@link #sendText(String)}.
     * 
     * @param interaction the new active interaction event
     */
    public void setActiveInteraction(ChatInputInteractionEvent interaction) {
        activeInteraction = interaction;
    }

    /**
     * Sets the select menu interaction event that this handler is actively performing actions for.
     * If there is an active select menu interaction, the handler will edit the interaction instead
     * of sending a message to the active channel on {@link #editMessageContent}.
     * 
     * @param interaction the new active select menu interaction event
     */
    public void setActiveComponentInteraction(ComponentInteractionEvent interaction) {
        activeComponentInteraction = interaction;
    }

    /**
     * Returns the active channel for this handler.
     * 
     * @return the channel that is currently active
     */
    public Channel getActiveChannel() {
        return activeChannel;
    }

    /**
     * Returns the active interaction event for this handler.
     * 
     * @return the interaction that is currently active
     */
    public ChatInputInteractionEvent getActiveInteraction() {
        return activeInteraction;
    }

    /**
     * Returns the active select menu interaction event for this handler.
     * 
     * @return the select menu interaction that is currently active
     */
    public ComponentInteractionEvent getComponentInteractionEvent() {
        return activeComponentInteraction;
    }

    /**
     * Kills the active interaction event for this handler, setting it to null.
     */
    public void killActiveInteraction() {
        activeInteraction = null;
    }

    /**
     * Kills the active select menu interaction event for this handler, setting it to null.
     */
    public void killActiveComponentInteraction() {
        activeComponentInteraction = null;
    }

    /**
     * Sends a text message with only content to the active channel.
     * If there is an active interaction, the handler will reply to the interaction instead.
     * 
     * @param text the message to send
     * @return the message that was sent
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendPlainText(String text) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                if (deferReply) {
                    activeInteraction.editReply(text).block();
                } else {
                    activeInteraction.reply(text).block();
                    deferReply = false;
                }
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
     * If there is an active interaction, the handler will reply to the interaction instead.
     * 
     * @param text the message to send
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendText(String text) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
                if (deferReply) {
                    InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                    activeInteraction.editReply(spec).block();
                    deferReply = false;
                } else {
                    InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                    activeInteraction.reply(spec).block();
                }
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
     * If there is an active interaction, the handler will reply to the interaction instead.
     * 
     * @param text the message to send
     * @param title the title of the message
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendText(String text, String title) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
                if (deferReply) {
                    InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                    activeInteraction.editReply(spec).block();
                    deferReply = false;
                } else {
                    InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                    activeInteraction.reply(spec).block();
                }
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
     * If there is an active interaction, the handler will reply to the interaction instead.
     * 
     * @param text the message to send
     * @param title the title of the message
     * @param components the components to add to the message
     * @throws IllegalStateException if the active channel is not a text channel
     */
    public Message sendText(String text, String title, LayoutComponent... components) throws IllegalStateException {
        if (activeInteraction != null) {
            try {
                EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
                if (deferReply) {
                    InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                    activeInteraction.editReply(spec);
                    deferReply = false;
                } else {
                    InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                    activeInteraction.reply(spec).block();
                }
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
                    MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(components);
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
            if (activeComponentInteraction != null) {
                Message returns = activeComponentInteraction.getReply().block();
                killActiveComponentInteraction();
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
            if (activeComponentInteraction != null) {
                activeComponentInteraction.edit().withEmbeds(embed).block();
                Message returns = activeComponentInteraction.getReply().block();
                killActiveComponentInteraction();
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
     * @param components the components to add to the message
     */
    public Message editMessageContent(Message message, String text, String title, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        if (message != null) {
            if (activeComponentInteraction != null) {
                activeComponentInteraction.edit().withEmbeds(embed).withComponents(components).block();
                Message returns = activeComponentInteraction.getReply().block();
                killActiveComponentInteraction();
                return returns;
            } else {
                return message.edit().withEmbeds(embed).withComponents(components).block();
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
}
