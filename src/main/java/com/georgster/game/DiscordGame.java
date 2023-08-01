package com.georgster.game;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.Channel;

/**
 * A longer-executing game taking place in a Discord {@link Channel}.
 * {@link #play()} is the entry point for solely game logic, whereas all classes
 * can use {@link #startGame()} to begin the game.
 * <p>
 * Each {@link Channel} may only have one active DiscordGame.
 */
public abstract class DiscordGame {
    private static final List<Snowflake> ACTIVE_GAME_CHANNELS = new ArrayList<>(); // Channel IDs where a DiscordGame is active
    private final CommandExecutionEvent event; // The event that prompted the game's creation

    /**
     * Creates a new DiscordGame.
     * 
     * @param event The event that prompted the game's creation.
     */
    protected DiscordGame(CommandExecutionEvent event) {
        this.event = event;
    }

    /**
     * The entry point for the logic for a game. Non-DiscordGame classes should <b>NOT</b>
     * use this method to start the game, using {@link #startGame()} instead.
     */
    protected abstract void play();

    /**
     * Begins this game in the {@link Channel} from this game's {@link CommandExecutionEvent},
     * if that channel does not currently have a DiscordGame active.
     * 
     * @throws IllegalStateException If the {@link Channel} already has a DiscordGame active.
     */
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
