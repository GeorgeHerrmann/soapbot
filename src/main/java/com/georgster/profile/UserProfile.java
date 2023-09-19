package com.georgster.profile;

import com.georgster.control.util.identify.util.MemberIdentified;
import com.georgster.economy.CoinBank;
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
     */
    public UserProfile(String serverId, String userId, String user, MemberChatCompletions completions, CoinBank bank) {
        super(userId);
        this.guildId = serverId;
        this.username = user;
        this.completions = completions;
        this.bank = bank;
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
     * Gets the {@link CoinBank} of the {@code Member} associated with this profile.
     * 
     * @return The {@link CoinBank} of the Member associated with this profile.
     */
    public CoinBank getBank() {
        return this.bank;
    }

    /**
     * Sets the username of the {@code Member} associated with this profile.
     * 
     * @param username The username of the Member associated with this profile.
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
