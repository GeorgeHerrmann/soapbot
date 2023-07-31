package com.georgster.game;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;

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
            event.getLogger().append("- Beginning a Discord Game in a text channel", LogDestination.NONAPI, LogDestination.API);
            play();
            ACTIVE_GAME_CHANNELS.remove(channelId);
        } else {
            event.getLogger().append("- A game is already active in the requested channel, cancelling the new game.", LogDestination.NONAPI);
            throw new IllegalStateException("Channel: " + event.getDiscordEvent().getChannel().getMention() + " already has a game active.");
        }
    }
}
