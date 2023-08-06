package com.georgster.game;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.economy.exception.InsufficientCoinsException;
import com.georgster.logs.LogDestination;
import com.georgster.profile.UserProfile;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
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

    private long entryAmount;
    private long rewardAmount;

    private Member owner;
    private boolean isActive;

    /**
     * Creates a new DiscordGame with no entry nor reward coin amount.
     * 
     * @param event The event that prompted the game's creation.
     */
    protected DiscordGame(CommandExecutionEvent event) {
        this.event = event;
        this.isActive = false;
        this.owner = event.getDiscordEvent().getAuthorAsMember();
        this.entryAmount = 0;
        this.rewardAmount = 0;
    }

    protected DiscordGame(CommandExecutionEvent event, long entryAmount, long rewardAmount) {
        this.event = event;
        this.isActive = false;
        this.owner = event.getDiscordEvent().getAuthorAsMember();
        this.entryAmount = entryAmount;
        this.rewardAmount = rewardAmount;
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
            event.getLogger().append("- Beginning a Discord Game in a text channel\n", LogDestination.NONAPI, LogDestination.API);

            withdrawlEntryAmount();

            this.isActive = true;
            play();
            this.isActive = false;

            depositRewardAmount();
            ACTIVE_GAME_CHANNELS.remove(channelId);
        } else {
            event.getLogger().append("- A game is already active in the requested channel, cancelling the new game.", LogDestination.NONAPI);
            throw new IllegalStateException("Channel: " + event.getDiscordEvent().getChannel().getMention() + " already has a game active.");
        }
    }

    public void withdrawlEntryAmount() throws InsufficientCoinsException {
        UserProfile profile = event.getUserProfileManager().get(owner.getId().asString());
        try {
            profile.getBank().withdrawl(entryAmount);
            event.getUserProfileManager().update(profile);
        } catch (IllegalArgumentException e) {
            throw new InsufficientCoinsException(profile.getBank(), entryAmount);
        }
    }

    public UserProfile getOwnerProfile() {
        return event.getUserProfileManager().get(owner.getId().asString());
    }

    private void depositRewardAmount() {
        UserProfile profile = event.getUserProfileManager().get(owner.getId().asString());

        profile.getBank().deposit(rewardAmount);
        event.getUserProfileManager().update(profile);
    }

    public void setRewardAmount(long amount) {
        this.rewardAmount = amount;
    }

    public void clearEntryRewardAmounts() {
        this.entryAmount = 0;
        this.rewardAmount = 0;
    }

    public void setEntryAmount(long amount) {
        this.entryAmount = amount;
    }

    public long getEntryAmount() {
        return entryAmount;
    }

    public long getRewardAmount() {
        return rewardAmount;
    }

    public Member getOwner() {
        return owner;
    }

    public boolean isActive() {
        return isActive;
    }

    public void end() {
        isActive = false;
    }
}
