package com.georgster.util.handler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ComponentInteractionEvent;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

public interface InteractionHandler {
    public Message sendPlainMessage(String text);

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
}
