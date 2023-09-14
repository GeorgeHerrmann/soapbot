package com.georgster.util.handler;

import java.util.Optional;

import com.georgster.util.Unwrapper;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

public abstract class InteractionHandler {
    protected Optional<MessageChannel> activeChannel;
    protected Optional<ComponentInteractionEvent> activeComponentInteraction;
    protected final Snowflake id;

    protected InteractionHandler(Snowflake flake) {
        this.activeChannel = Optional.empty();
        this.activeComponentInteraction = Optional.empty();
        this.id = flake;
    }

    public void killActiveComponentInteraction() {
        this.activeComponentInteraction = Optional.empty();
    }

    public void killActiveMessageChannel() {
        this.activeChannel = Optional.empty();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the provided text with no formatting.
     * 
     * @param text The {@link Message} content.
     * @return The created {@link Message}.
     */
    public Message sendPlainMessage(String text) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendPlainMessage(channel, text)));
        return message.getObject();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the provided text and default formatting.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} in a blue embed.
     * 
     * @param text The {@link Message} content.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text)));
        return message.getObject();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the
     * provided text and title and default formatting.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title)));
        return message.getObject();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the
     * provided text, title and default formatting and attaches the provided {@link LayoutComponent LayoutComponents}.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, components)));
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
        }, () -> message.setObject(InteractionHandler.modifyMessage(msg, text)));

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
        }, () -> message.setObject(InteractionHandler.modifyMessage(msg, text, title)));

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
        }, () -> message.setObject(InteractionHandler.modifyMessage(msg, text, title, components)));

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

    public MessageChannel getActiveMessageChannel() {
        return activeChannel.orElse(null);
    }

    public Optional<MessageChannel> getActiveMessageChannelOptional() {
        return activeChannel;
    }

    public String getId() {
        return id.asString();
    }

    public Snowflake getIdFlake() {
        return id;
    }

    public void setActiveComponentInteraction(ComponentInteractionEvent event) {
        this.activeComponentInteraction = Optional.of(event);
    }

    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the provided text and default formatting.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} in a blue embed.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
        return channel.createMessage(spec).block();
    }

    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the
     * provided text and title and default formatting.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
        return channel.createMessage(spec).block();
    }
    
    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the
     * provided text, title and default formatting and attaches the provided {@link LayoutComponent LayoutComponents}.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(components);
        return channel.createMessage(spec).block();
    }

    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the provided text with no formatting.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @return The created {@link Message}.
     */
    public static Message sendPlainMessage(MessageChannel channel, String text) {
        return channel.createMessage(text).block();
    }

    public static Message modifyMessage(Message message, String text) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
        return message.edit().withEmbeds(embed).withComponents().block();
    }

    public static Message modifyMessage(Message message, String text, String title) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        return message.edit().withComponents().withEmbeds(embed).block();
    }

    public static Message modifyMessage(Message message, String text, String title, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        return message.edit().withEmbeds(embed).withComponents(components).block();
    }

    public static Message modifyPlainMessage(Message message, String text) {
        return message.edit().withContentOrNull(text).block();
    }
}
