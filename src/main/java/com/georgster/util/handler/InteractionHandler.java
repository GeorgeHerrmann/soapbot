package com.georgster.util.handler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

public interface InteractionHandler {
    /**
     * Sends a {@link Message} with the provided {@code text} and no formatting based
     * on this {@link InteractionHandler InteractionHandler's} configuration.
     * 
     * @param text The content for the message to send.
     * @return The created {@link Message}.
     */
    public Message sendPlainMessage(String text);

    /**
     * Sends a default formatted {@link Message} with the provided {@code text} based on this
     * {@link InteractionHandler InteractionHandler's} configuration.
     * <p>
     * SOAP Bot's default formatting encapsulates the {@code text} in a {@code BLUE} embed.
     * 
     * @param text The content for the message to send.
     * @return The created {@link Message}.
     */
    public Message sendMessage(String text);

    public Message sendMessage(String text, String title);

    public Message sendMessage(String text, String title, LayoutComponent... components);

    public Message editMessage(Message msg, String text);

    public Message editMessage(Message msg, String text, String title);

    public Message editMessage(Message msg, String text, String title, LayoutComponent... components);

    public Message editPlainMessage(Message msg, String text);

    public MessageChannel getActiveMessageChannel();

    public String getId();

    public Snowflake getIdFlake();

    public void setActiveComponentInteraction(ComponentInteractionEvent event);

    public static Message sendMessage(MessageChannel channel, String text) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
        return channel.createMessage(spec).block();
    }

    public static Message sendMessage(MessageChannel channel, String text, String title) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed);
        return channel.createMessage(spec).block();
    }
    
    public static Message sendMessage(MessageChannel channel, String text, String title, LayoutComponent... components) {
        EmbedCreateSpec embed = EmbedCreateSpec.builder().color(Color.BLUE).description(text).title(title).build();
        MessageCreateSpec spec = MessageCreateSpec.create().withEmbeds(embed).withComponents(components);
        return channel.createMessage(spec).block();
    }

    public static Message sendPlainMessage(MessageChannel channel, String text) {
        return channel.createMessage(text).block();
    }
}
