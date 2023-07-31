package com.georgster.game;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;

import discord4j.common.util.Snowflake;

public abstract class DiscordGame {
    private static final List<Snowflake> ACTIVE_GAME_CHANNELS = new ArrayList<>(); // Channel IDs where a DiscordGame is active
    private final CommandExecutionEvent event;

    protected DiscordGame(CommandExecutionEvent event) {
        this.event = event;
    }

    protected abstract void play();

    public void startGame() throws IllegalStateException {
        Snowflake channelId = event.getDiscordEvent().getChannel().getId();
        if (!ACTIVE_GAME_CHANNELS.contains(channelId)) {
            ACTIVE_GAME_CHANNELS.add(channelId);
            play();
            ACTIVE_GAME_CHANNELS.remove(channelId);
        } else {
            throw new IllegalStateException("Channel: " + event.getDiscordEvent().getChannel().getMention() + " already has a game active.");
        }
    }
}
