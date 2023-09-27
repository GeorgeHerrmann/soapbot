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
 * A handler for interactions between a {@link Snowflake} identified Discord object and
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
     * An enum describing the formatting of a {@link Message} sent by an {@link InteractionHandler}.
     */
    public enum MessageFormatting {
        /**
         * The default formatting for SOAP Bot {@link Message Messages}.
         * <p>
         * Default formatting wraps the {@link Message} in a blue embed.
         */
        DEFAULT,
        /**
         * The error formatting for SOAP Bot {@link Message Messages}.
         * <p>
         * Error formatting wraps the {@link Message} in a red embed.
         */
        ERROR,
        /**
         * The info formatting for SOAP Bot {@link Message Messages}.
         * <p>
         * Info formatting wraps the {@link Message} in a grey-chateu embed.
         */
        INFO
    }

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
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the provided text and formatting.
     * 
     * @param text The {@link Message} content.
     * @param format The {@link MessageFormatting} to use.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, MessageFormatting format) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, format)));
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
     * provided text, title and formatting.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param format The {@link MessageFormatting} to use.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title, MessageFormatting format) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, format)));
        return message.getObject();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the
     * provided text and title and default formatting and attaches an image of the provided url.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title, String imageUrl) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl)));
        return message.getObject();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the
     * provided text, title and formatting and attaches an image of the provided url.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @param format The {@link MessageFormatting} to use.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title, String imageUrl, MessageFormatting format) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl, format)));
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
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the
     * provided text, title and formatting and attaches the provided {@link LayoutComponent LayoutComponents}.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @param format The {@link MessageFormatting} to use.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title, MessageFormatting format, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, format, components)));
        return message.getObject();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the
     * provided text, title and default formatting and attaches the provided {@link LayoutComponent LayoutComponents}
     * and an image of the provided url.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title, String imageUrl, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl, components)));
        return message.getObject();
    }

    /**
     * Sends a {@link Message} in this handler's active {@link MessageChannel} with the
     * provided text, title and formatting and attaches the provided {@link LayoutComponent LayoutComponents}
     * and an image of the provided url.
     * 
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @param format The {@link MessageFormatting} to use.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text, String title, String imageUrl, MessageFormatting format, LayoutComponent... components) {
        Unwrapper<Message> message = new Unwrapper<>();
        activeChannel.ifPresent(channel -> message.setObject(InteractionHandler.sendMessage(channel, text, title, imageUrl, format, components)));
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
        Color color = msg.getEmbeds().isEmpty() ? Color.BLUE : msg.getEmbeds().get(0).getColor().orElse(Color.BLUE); // Attempts to copy the color if it exists
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(color).description(text).build();

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
        Color color = msg.getEmbeds().isEmpty() ? Color.BLUE : msg.getEmbeds().get(0).getColor().orElse(Color.BLUE); // Attempts to copy the color if it exists
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(color).description(text).title(title).build();

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
        Color color = msg.getEmbeds().isEmpty() ? Color.BLUE : msg.getEmbeds().get(0).getColor().orElse(Color.BLUE); // Attempts to copy the color if it exists
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(color).description(text).title(title).build();

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
     * Sends a {@link Message} in the provided {@link MessageChannel} with the provided text and formatting.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param format The {@link MessageFormatting} to use.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, MessageFormatting format) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).build();
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
     * provided text, title and formatting.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param format The {@link MessageFormatting} to use.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title, MessageFormatting format) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
        return channel.createMessage(spec).block();
    }

    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the
     * provided text and title and default formatting and attaches an image of the provided url.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title, String imageUrl) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).image(imageUrl).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
        return channel.createMessage(spec).block();
    }

    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the
     * provided text, title and formatting and attaches an image of the provided url.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @param format The {@link MessageFormatting} to use.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title, String imageUrl, MessageFormatting format) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).image(imageUrl).build();
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
     * Sends a {@link Message} in the provided {@link MessageChannel} with the
     * provided text, title and formatting and attaches the provided {@link LayoutComponent LayoutComponents}.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param format The {@link MessageFormatting} to use.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title, MessageFormatting format, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(components);
        return channel.createMessage(spec).block();
    }

    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the
     * provided text, title and default formatting and attaches the provided {@link LayoutComponent LayoutComponents}
     * and an image of the provided url.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title, String imageUrl, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).image(imageUrl).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(components);
        return channel.createMessage(spec).block();
    }

    /**
     * Sends a {@link Message} in the provided {@link MessageChannel} with the
     * provided text, title and default formatting and attaches the provided {@link LayoutComponent LayoutComponents}
     * and an image of the provided url.
     * <p>
     * SOAP Bot's default formatting wraps the {@code text} and {@code title} in a blue embed.
     * 
     * @param channel The {@link MessageChannel} to send the message in.
     * @param text The {@link Message} content.
     * @param title The {@link Message} title, present at the top of the embed.
     * @param imageUrl The url of the image to attach to the {@link Message}.
     * @param format The {@link MessageFormatting} to use.
     * @param components The {@link LayoutComponent LayoutComponents} to attach to the {@link Message}.
     * @return The created {@link Message}.
     */
    public static Message sendMessage(MessageChannel channel, String text, String title, String imageUrl, MessageFormatting format, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(getColor(format)).description(text).title(title).image(imageUrl).build();
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
        Color color = message.getEmbeds().isEmpty() ? Color.BLUE : message.getEmbeds().get(0).getColor().orElse(Color.BLUE); // Attempts to copy the color if it exists

        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(color).description(text).build();
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
        Color color = message.getEmbeds().isEmpty() ? Color.BLUE : message.getEmbeds().get(0).getColor().orElse(Color.BLUE); // Attempts to copy the color if it exists
        
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(color).description(text).title(title).build();
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
        Color color = message.getEmbeds().isEmpty() ? Color.BLUE : message.getEmbeds().get(0).getColor().orElse(Color.BLUE); // Attempts to copy the color if it exists

        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(color).description(text).title(title).build();
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

    /**
     * Returns the {@link Color} associated with the provided {@link MessageFormatting},
     * or {@link Color#BLUE} if one does not exist.
     * 
     * @param format The {@link MessageFormatting} to get the {@link Color} for.
     * @return The {@link Color} associated with the provided {@link MessageFormatting}.
     */
    protected static Color getColor(MessageFormatting format) {
        switch (format) {
            case DEFAULT:
                return Color.BLUE;
            case ERROR:
                return Color.RED;
            case INFO:
                return Color.GRAY_CHATEAU;
            default:
                return Color.BLUE;
        }
    }
}
