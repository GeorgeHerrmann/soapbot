package com.georgster.util.handler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.georgster.util.Unwrapper;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

public final class GuildInteractionHandler implements InteractionHandler {
    private Guild guild; // The guild being interacted with
    private Optional<GuildMessageChannel> activeChannel; // The message channel this handler is working with
    private Optional<ApplicationCommandInteractionEvent> activeCommandInteraction;
    private Optional<ComponentInteractionEvent> activeComponentInteraction;

    private boolean replyWasDeferred; // If a reply was deferred, requiring an edit on response.

    public GuildInteractionHandler(Guild guild) {
        this.guild = guild;
        this.replyWasDeferred = false;

        this.activeChannel = Optional.empty();
        this.activeCommandInteraction = Optional.empty();
        this.activeComponentInteraction = Optional.empty();
    }

    /**
     * Switches this handler to reply deferring mode.
     * <p>
     * If reply deferring mode is on, this handler will edit replies from
     * {@link ApplicationCommandInteractionEvent ApplicationCommandInteractionEvents},
     * and will automatically disable once a successful defer edit has been made.
     */
    public void enableReplyDeferring() {
        this.replyWasDeferred = true;
    }

    /**
     * Returns the {@link Guild} for this GuildInteractionHandler.
     * 
     * @return the {@link Guild} for this GuildInteractionHandler
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Updates this GuildInteractionHandler's {@link Guild} to the Updated {@link Guild}.
     * 
     * @param updatedGuild the updated {@link Guild} to replace the old {@link Guild}
     */
    public void setGuild(Guild guild) {
        this.guild = Objects.requireNonNull(guild);
    }

    /**
     * Sets the {@link GuildMessageChannel} this handler is actively performing actions in.
     * <p>
     * A {@link GuildInteractionHandler} will only perform channel-based actions in its {@code activeChannel}.
     * 
     * @param messageChannel The new {@code activeChannel}.
     */
    public void setActiveMessageChannel(GuildMessageChannel messageChannel) {
        this.activeChannel = Optional.of(messageChannel);
    }

    /**
     * Sets the {@link ApplicationCommandInteractionEvent} that this handler is actively performing actions for.
     * If there is an interaction active, the handler will reply to the interaction instead
     * of sending a message to the active channel on {@link #sendText(String)}.
     * 
     * @param interaction the new active interaction event
     */
    public void setActiveCommandInteraction(ApplicationCommandInteractionEvent newEvent) {
        this.activeCommandInteraction = Optional.of(newEvent);
    }

    /**
     * Sets the {@link ComponentInteractionEvent} that this handler is actively performing actions for.
     * If there is an active {@link ComponentInteractionEvent}, the handler will edit the interaction instead
     * of sending a message to the active channel on {@link #editMessageContent}.
     * 
     * @param interaction the new active select menu interaction event
     */
    public void setActiveComponentInteraction(ComponentInteractionEvent interaction) {
        activeComponentInteraction = Optional.of(interaction);
    }

    /**
     * Returns the {@link GuildMessageChannel} this handler is actively performing actions in, if present.
     * 
     * @return The {@link GuildMessageChannel} this handler is actively performing actions in, if present.
     */
    public Optional<GuildMessageChannel> getActiveGuildMessageChannel() {
        return activeChannel;
    }

    /**
     * {@inheritDoc}
     */
    public MessageChannel getActiveMessageChannel() {
        return activeChannel.orElse(null);
    }

    /**
     * Returns the {@link ApplicationCommandInteractionEvent} this handler is actively performing actions for, if present.
     * 
     * @return The {@link ApplicationCommandInteractionEvent} this handler is actively performing actions for, if present.
     */
    public Optional<ApplicationCommandInteractionEvent> getActiveCommandInteraction() {
        return activeCommandInteraction;
    }

    /**
     * Returns the {@link ComponentInteractionEvent} this handler is actively performing actions for, if present.
     * 
     * @return The {@link ComponentInteractionEvent} this handler is actively performing actions for, if present.
     */
    public Optional<ComponentInteractionEvent> getActiveComponentInteraction() {
        return activeComponentInteraction;
    }

    /**
     * Kills the active {@link ApplicationCommandInteractionEvent}.
     */
    public void killActiveCommandInteraction() {
        this.activeCommandInteraction = Optional.empty();
    }

    /**
     * Kills the active {@link ComponentInteractionEvent}.
     */
    public void killActiveComponentInteraction() {
        this.activeComponentInteraction = Optional.empty();
    }

    public Message sendPlainMessage(String text) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            if (replyWasDeferred) {
                interaction.editReply(text).block();
            } else {
                interaction.reply(text).block();
                replyWasDeferred = false;
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> activeChannel.ifPresent(channel -> message.setObject(channel.createMessage(text).block())));

        return message.getObject();
    }

    public Message sendMessage(String text) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
            MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
            activeChannel.ifPresent(channel -> message.setObject(channel.createMessage(spec).block()));
        });

        return message.getObject();
    }

    public Message sendMessage(String text, String title) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
            activeChannel.ifPresent(channel -> message.setObject(channel.createMessage(spec).block()));
        });
        return message.getObject();
    }

    public Message sendMessage(String text, String title, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeCommandInteraction.ifPresentOrElse(interaction -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            if (replyWasDeferred) {
                InteractionReplyEditSpec spec = InteractionReplyEditSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.editReply(spec).block();
                replyWasDeferred = false;
            } else {
                InteractionApplicationCommandCallbackSpec spec = InteractionApplicationCommandCallbackSpec.builder().addEmbed(embed).addAllComponents(List.of(components)).build();
                interaction.reply(spec).block();
            }
            message.setObject(interaction.getReply().block());
            killActiveCommandInteraction();
        }, () -> {
            EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
            MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(components);
            activeChannel.ifPresent(channel -> message.setObject(channel.createMessage(spec).block()));
        });
        return message.getObject();
    }

    public Message editMessage(Message msg, String text) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();

        Unwrapper<Message> message = new Unwrapper<>();
        activeComponentInteraction.ifPresentOrElse(interaction -> {
            interaction.edit().withComponents().withEmbeds(embed).block();
            Message returns = interaction.getReply().block();
            killActiveComponentInteraction();
            message.setObject(returns);
        }, () -> message.setObject(msg.edit().withEmbeds(embed).withComponents().block()));

        return message.getObject();
    }

    public Message editMessage(Message msg, String text, String title) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();

        Unwrapper<Message> message = new Unwrapper<>();
        activeComponentInteraction.ifPresentOrElse(interaction -> {
            interaction.edit().withComponents().withEmbeds(embed).block();
            Message returns = interaction.getReply().block();
            killActiveComponentInteraction();
            message.setObject(returns);
        }, () -> message.setObject(msg.edit().withComponents().withEmbeds(embed).block()));

        return message.getObject();
    }

    public Message editMessage(Message msg, String text, String title, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();

        Unwrapper<Message> message = new Unwrapper<>();
        activeComponentInteraction.ifPresentOrElse(interaction -> {
            interaction.edit().withEmbeds(embed).withComponents(components).block();
            Message returns = interaction.getReply().block();
            killActiveComponentInteraction();
            message.setObject(returns);
        }, () -> message.setObject(msg.edit().withEmbeds(embed).withComponents(components).block()));

        return message.getObject();
    }

    public Message editPlainMessage(Message msg, String text) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeComponentInteraction.ifPresentOrElse(interaction -> {
            interaction.edit().withContent(text).block();
            Message returns = interaction.getReply().block();
            killActiveComponentInteraction();
            message.setObject(returns);
        }, () -> message.setObject(msg.edit().withContentOrNull(text).block()));

        return message.getObject();
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
     * Returns a list of all the message channels in the guild.
     * 
     * @return a list of all the message channels in the guild
     */
    public List<GuildMessageChannel> getMessageChannels() {
        return guild.getChannels().ofType(GuildMessageChannel.class).collectList().block();
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
     * Returns the {@link Snowflake} ID of the guild as a String.
     *  
     * @return the {@link Snowflake} ID of the guild as a String.
     */
    public String getId() {
        return guild.getId().asString();
    }

    /**
     * Returns the {@link Snowflake} ID of the guild..
     *  
     * @return the {@link Snowflake} ID of the guild.
     */
    public Snowflake getIdFlake() {
        return guild.getId();
    }

    /**
     * Returns the {@code Member} in this {@code Guild} that has the given tag.
     * 
     * @param memberTag The tag of the member to get
     * @return the {@code Member} in this {@code Guild} that has the given tag
     */
    public Member getMemberByTag(String memberTag) {
        for (Member member : getAllMembers()) {
            if (member.getTag().equals(memberTag))
                return member;
        }
        return null;
    }

    /**
     * Returns the {@code Member} in this {@code Guild} that has the given username.
     * 
     * @param memberTag The username of the member to get
     * @return the {@code Member} in this {@code Guild} that has the given username
     */
    public Member getMemberByName(String memberName) {
        for (Member member : getAllMembers()) {
            if (member.getUsername().equals(memberName)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Returns the {@code Member} in this {@code Guild} that has the given id.
     * 
     * @param memberTag The id of the member to get
     * @return the {@code Member} in this {@code Guild} that has the given id
     */
    public Member getMemberById(String id) {
        for (Member member : getAllMembers()) {
            if (member.getId().asString().equals(id)) {
                return member;
            }
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
     * Returns the {@link GuildMessageChannel} in this {@link Guild} that has the given name.
     * 
     * @param channelName The name of the channel to get
     * @return the {@link GuildMessageChannel} in this {@link Guild} that has the given name
     */
    public GuildMessageChannel getMessageChannel(String channelName) {
        for (GuildMessageChannel channel : getMessageChannels()) {
            if (channel.getName().equals(channelName)) {
                return channel;
            }
        }
        return null;
    }
}
