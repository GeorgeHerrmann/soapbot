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

/**
 * A handler for interactions between a {@link Snowflake} identified Discord objects and
 * the Discord4J api. {@link InteractionHandler InteractionHandler's} generally provide a suite
 * of utility for performing actions for and/or communicating with the Discord4J API.
 * <p>
 * All {@link InteractionHandler InteractionHandler's} have a {@link Message} editing
 * and sending system, along with basic utility identifying what object is being handled.
 * Implmentations should provide more specific utility.
 */
public abstract class InteractionHandler {
    protected Optional<MessageChannel> activeChannel;
    protected Optional<ComponentInteractionEvent> activeComponentInteraction;
    protected final Snowflake id;

    /**
     * Creates a new {@link InteractionHandler} for the Discord object
     * represented by its {@link Snowflake} id.
     * 
     * @param flake The {@link Snowflake} id of the Discord Object to handler interactions for.
     */
    protected InteractionHandler(Snowflake flake) {
        this.activeChannel = Optional.empty();
        this.activeComponentInteraction = Optional.empty();
        this.id = flake;
    }

    /**
     * Resets this {@link InteractionHandler InteractionHandler's} active {@link ComponentInteractionEvent}.
     */
    public void killActiveComponentInteraction() {
        this.activeComponentInteraction = Optional.empty();
    }

    /**
     * Resets this {@link InteractionHandler InteractionHandler's} active {@link MessageChannel}.
     */
    public void killActiveMessageChannel() {
        this.activeChannel = Optional.empty();
    }

    /**
     * Sets the {@link MessageChannel} this handler is actively performing actions in.
     * <p>
     * An {@link InteractionHandler} will only perform channel-based actions in its {@code activeChannel}.
     * 
     * @param channel The new {@link MessageChannel}.
     */
    public void setActiveMessageChannel(MessageChannel channel) {
        this.activeChannel = Optional.of(channel);
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

    /**
     * Edits the provided {@link Message} with the new provided body {@code text} and
     * default formatting.
     * <p>
     * If this {@link InteractionHandler} has an {@link #setActiveComponentInteraction(ComponentInteractionEvent) active Component Interaction},
     * it will use the provided {@code msg} to reply to the interaction instead, killing the interaction once complete.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new body text for the {@link Message}.
     * @return The edited {@link Message}.
     */
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

    /**
     * Edits the provided {@link Message} with the new provided body {@code text},
     * {@code title} and default formatting.
     * <p>
     * If this {@link InteractionHandler} has an {@link #setActiveComponentInteraction(ComponentInteractionEvent) active Component Interaction},
     * it will use the provided {@code msg} to reply to the interaction instead, killing the interaction once complete.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new body text for the {@link Message}.
     * @param title The new {@link Message} title.
     * @return The edited {@link Message}.
     */
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

    /**
     * Edits the provided {@link Message} with the new provided body {@code text},
     * {@code title} and default formatting and attaches the provided {@link LayoutComponent LayoutComponents}.
     * <p>
     * If this {@link InteractionHandler} has an {@link #setActiveComponentInteraction(ComponentInteractionEvent) active Component Interaction},
     * it will use the provided {@code msg} to reply to the interaction instead, killing the interaction once complete.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new body text for the {@link Message}.
     * @param title The new {@link Message} title.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The edited {@link Message}.
     */
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

    /**
     * Edits the provided {@link Message} with the new provided {@code text} and
     * no formatting.
     * <p>
     * If this {@link InteractionHandler} has an {@link #setActiveComponentInteraction(ComponentInteractionEvent) active Component Interaction},
     * it will use the provided {@code msg} to reply to the interaction instead, killing the interaction once complete.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new content for the {@link Message}.
     * @return The edited {@link Message}.
     */
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
     * Returns the active {@link MessageChannel} this handler is sending messages to,
     * or null if one does not exist.
     * <p>
     * This method is a convienience method to extract the {@link MessageChannel} from {@link #getActiveMessageChannelOptional()} unconditionally.
     * 
     * @return The active {@link MessageChannel}, or null if one does not exist.
     */
    public MessageChannel getActiveMessageChannel() {
        return activeChannel.orElse(null);
    }

    /**
     * Returns the active {@link MessageChannel} this handler is sending messages to,
     * if present.
     * 
     * @return The active {@link MessageChannel} described by an {@link Optional}.
     */
    public Optional<MessageChannel> getActiveMessageChannelOptional() {
        return activeChannel;
    }

    /**
     * Returns the String-based {@link Snowflake} ID of the Object this
     * {@link InteractionHandler} is handling Discord interactions for.
     * 
     * @return This handler's {@link Snowflake} ID as a String.
     */
    public String getId() {
        return id.asString();
    }

    /**
     * Returns the {@link Snowflake} ID of the Object this
     * {@link InteractionHandler} is handling Discord interactions for.
     * 
     * @return This handler's {@link Snowflake} ID.
     */
    public Snowflake getIdFlake() {
        return id;
    }

    /**
     * Sets the active {@link ComponentInteractionEvent} for this handler.
     * <p>
     * If an {@link InteractionHandler} has an active {@link ComponentInteractionEvent},
     * it will reply to the event on the next edit message call, and then
     * kill the interaction.
     * 
     * @param event The {@link ComponentInteractionEvent} to set.
     */
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

    /**
     * Modifies the provided {@link Message} with the new provided body {@code text} and
     * default formatting.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new body text for the {@link Message}.
     * @return The edited {@link Message}.
     */
    public static Message modifyMessage(Message message, String text) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
        return message.edit().withEmbeds(embed).withComponents().block();
    }

    /**
     * Edits the provided {@link Message} with the new provided body {@code text},
     * {@code title} and default formatting.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new body text for the {@link Message}.
     * @param title The new {@link Message} title.
     * @return The edited {@link Message}.
     */
    public static Message modifyMessage(Message message, String text, String title) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        return message.edit().withComponents().withEmbeds(embed).block();
    }

    /**
     * Edits the provided {@link Message} with the new provided body {@code text},
     * {@code title} and default formatting and attaches the provided {@link LayoutComponent LayoutComponents}.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new body text for the {@link Message}.
     * @param title The new {@link Message} title.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The edited {@link Message}.
     */
    public static Message modifyMessage(Message message, String text, String title, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        return message.edit().withEmbeds(embed).withComponents(components).block();
    }

    /**
     * Edits the provided {@link Message} with the new provided {@code text} and
     * no formatting.
     * 
     * @param msg The {@link Message} to edit.
     * @param text The new content for the {@link Message}.
     * @return The edited {@link Message}.
     */
    public static Message modifyPlainMessage(Message message, String text) {
        return message.edit().withContentOrNull(text).block();
    }
}
