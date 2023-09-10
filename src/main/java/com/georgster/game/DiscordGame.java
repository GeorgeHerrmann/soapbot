package com.georgster.game;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.control.util.identify.UniqueIdBased;
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
public abstract class DiscordGame extends UniqueIdBased {
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

    /**
     * Creates a new DiscordGame with the provided entry amount and reward amount.
     * 
     * @param event The event that prompted the game's creation.
     * @param entryAmount The coin cost to enter (play) the game.
     * @param rewardAmount The coin reward after the game is played.
     */
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

    /**
     * Withdrawls the entry fee from all game player coin banks.
     * 
     * @throws InsufficientCoinsException If the entry fee amount of coins exceeds a user's balance.
     */
    public void withdrawlEntryAmount() throws InsufficientCoinsException {
        UserProfile profile = event.getUserProfileManager().get(owner.getId().asString());
        try {
            profile.getBank().withdrawl(entryAmount);
            event.getUserProfileManager().update(profile);
        } catch (IllegalArgumentException e) {
            throw new InsufficientCoinsException(profile.getBank(), entryAmount);
        }
    }

    /**
     * Returns the {@link UserProfile} of the owner of this game.
     * 
     * @return The {@link UserProfile} of the owner of this game.
     */
    public UserProfile getOwnerProfile() {
        return event.getUserProfileManager().get(owner.getId().asString());
    }

    /**
     * Deposits the reward amount into the player's coin banks.
     */
    private void depositRewardAmount() {
        UserProfile profile = event.getUserProfileManager().get(owner.getId().asString());

        profile.getBank().deposit(rewardAmount);
        event.getUserProfileManager().update(profile);
    }

    /**
     * Sets the reward amount for this game.
     * 
     * @param amount The new reward amount.
     */
    public void setRewardAmount(long amount) {
        this.rewardAmount = amount;
    }

    /**
     * Resets this game's entry and reward amounts.
     */
    public void clearEntryRewardAmounts() {
        this.entryAmount = 0;
        this.rewardAmount = 0;
    }

    /**
     * Sets the entry amount for this game.
     * 
     * @param amount The new entry amount.
     */
    public void setEntryAmount(long amount) {
        this.entryAmount = amount;
    }

    /**
     * Returns the entry amount for this game.
     * 
     * @return The entry amount for this game.
     */
    public long getEntryAmount() {
        return entryAmount;
    }

    /**
     * Returns the reward amount for this game.
     * 
     * @return The reward amount for this game.
     */
    public long getRewardAmount() {
        return rewardAmount;
    }

    /**
     * Returns the owner of this game.
     * 
     * @return The owner of this game.
     */
    public Member getOwner() {
        return owner;
    }

    /**
     * Returns true if the game is active, false otherwise.
     * 
     * @return true if the game is active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Ends this game.
     */
    public void end() {
        isActive = false;
    }
}
