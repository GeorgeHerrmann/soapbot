package com.georgster.profile;

import java.util.ArrayList;
import java.util.List;

import com.georgster.coinfactory.model.CoinFactory;
import com.georgster.collectable.Collected;
import com.georgster.collectable.trade.Tradeable;
import com.georgster.control.util.identify.util.MemberIdentified;
import com.georgster.economy.CoinBank;
import com.georgster.elo.EloRating;
import com.georgster.gpt.MemberChatCompletions;

/**
 * A {@code Profile} for a specific {@code Member} inside of a {@code Guild}.
 * <p>
 * A {@link UserProfile} contains {@code Member}-specific information such as
 * their {@link MemberChatCompletions}, {@link CoinBank}, etc.
 * <p>
 * This {@link UserProfile} is identified by Member's {@code Snowflake} ID.
 */
public final class UserProfile extends MemberIdentified {
    private final String guildId; //Snowflake Guild ID associated with this user's profile in a guild
    private String username; //User's discord username
    private final MemberChatCompletions completions;
    private final CoinBank bank;
    private final CoinFactory factory;
    private final List<Collected> collecteds;
    private final EloRating eloRating;

    /**
     * Creates a new {@link UserProfile} for a specific {@code Member} inside of a {@code Guild}.
     * 
     * @param serverId The {@code Snowflake} ID of the {@code Guild} this profile exists in.
     * @param userId The {@code Snowflake} ID of the {@code Member} this profile is for.
     * @param user The username of the user
     */
    public UserProfile(String serverId, String userId, String user) {
        super (userId);
        this.guildId = serverId;
        this.username = user;
        this.completions = new MemberChatCompletions(userId);
        this.bank = new CoinBank(userId);
        this.collecteds = new ArrayList<>();
        this.factory = new CoinFactory(userId);
        this.eloRating = new EloRating(userId);
    }

    /**
     * Constructs a {@code Profile} for a specific {@code Member} inside of a {@code Guild}.
     * <p>
     * Generally used when loading a {@link UserProfile} from the database.
     * 
     * @param serverId The {@code Snowflake} ID of the {@code Guild} this profile exists in.
     * @param userId The {@code Snowflake} ID of the {@code Member} this profile is for.
     * @param user The username of the user
     * @param completions The {@link MemberChatCompletions} of the user
     * @param bank The {@link CoinBank} of the user
     * @param factory The {@link CoinFactory} of the user
     * @param collecteds The {@link Collected} items of the user
     * @param eloRating The {@link EloRating} of the user
     */
    public UserProfile(String serverId, String userId, String user, MemberChatCompletions completions, CoinBank bank, CoinFactory factory, List<Collected> collecteds, EloRating eloRating) {
        super(userId);
        this.guildId = serverId;
        this.username = user;
        this.completions = completions;
        this.bank = bank;
        this.collecteds = collecteds;
        this.factory = factory;
        this.eloRating = eloRating != null ? eloRating : new EloRating(userId);
    }

    /**
     * Gets the {@code Snowflake} ID of the {@code Guild} associated with this Profile.
     * 
     * @return the ID of the Guild associated with this profile.
     */
    public String getGuildId() {
        return guildId;
    }

    /**
     * Gets the {@code Snowflake} ID of the {@code Member} associated with this profile.
     * 
     * @return the ID of the Member associated with this profile.
     */
    public String getMemberId() {
        return getId();
    }
    
    /**
     * Gets the username of the {@code Member} associated with this profile.
     * 
     * @return the username of the Member associated with this profile.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the {@link MemberChatCompletions} of the {@code Member} associated with this profile.
     * 
     * @return The {@link MemberChatCompletions} of the Member associated with this profile.
     */
    public MemberChatCompletions getCompletions() {
        return completions;
    }

    /**
     * Gets the {@link Collected Collecteds} of the {@code Member} associated with this profile.
     * 
     * @return The {@link Collected Collecteds} of the Member associated with this profile.
     */
    public List<Collected> getCollecteds() {
        return collecteds;
    }

    /**
     * Adds a {@link Collected} to the {@code Member} associated with this profile.
     * 
     * @param collected The {@link Collected} to add to the Member associated with this profile.
     */
    public void addCollected(Collected collected) {
        collecteds.add(collected);
    }

    /**
     * Removes a {@link Collected} from the {@code Member} associated with this profile.
     * 
     * @param collected The {@link Collected} to remove from the Member associated with this profile.
     */
    public void removeCollected(Collected collected) {
        collecteds.removeIf(c -> c.getIdentifier().equals(collected.getIdentifier()));
    }

    public void removeCollected(String id) {
        collecteds.removeIf(collected -> collected.getIdentifier().equals(id));
    }

    /**
     * Gets the {@link CoinBank} of the {@code Member} associated with this profile.
     * 
     * @return The {@link CoinBank} of the Member associated with this profile.
     */
    public CoinBank getBank() {
        return this.bank;
    }

    /**
     * Gets the {@link CoinFactory} of the {@code Member} associated with this profile.
     * 
     * @return The {@link CoinFactory} of the Member associated with this profile.
     */
    public CoinFactory getFactory() {
        return this.factory;
    }

    /**
     * Gets the {@link EloRating} of the {@code Member} associated with this profile.
     * 
     * @return The {@link EloRating} of the Member associated with this profile.
     */
    public EloRating getEloRating() {
        return this.eloRating;
    }

    /**
     * Sets the username of the {@code Member} associated with this profile.
     * 
     * @param username The username of the Member associated with this profile.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets all {@link Tradeable Tradeables} associated with this profile.
     * 
     * @return All {@link Tradeable Tradeables} associated with this profile.
     */
    public List<Tradeable> getAllTradeables() {
        List<Tradeable> tradeables = new ArrayList<>();
        tradeables.addAll(collecteds);
        return tradeables;
    }

    /**
     * Gets a {@link Tradeable} by its unique ID.
     * 
     * @param id The unique ID of the {@link Tradeable} to get.
     * @return The {@link Tradeable} with the specified ID, or {@code null} if no {@link Tradeable} with the specified ID exists.
     */
    public Tradeable getTradeableById(String id) {
        return getAllTradeables().stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }

    /**
     * Updates a {@link Collected} in this profile.
     * 
     * @param c The {@link Collected} to update.
     */
    public void updateCollected(Collected c) {
        collecteds.removeIf(collected -> collected.getId().equals(c.getId()));
        collecteds.add(c);
    }
}
