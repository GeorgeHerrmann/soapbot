package com.georgster.profile;

import com.georgster.control.util.identify.util.MemberIdentified;
import com.georgster.economy.CoinBank;
import com.georgster.gpt.MemberChatCompletions;

/**
 * A Profile holds data regarding a user's inside a specific Discord server.
 */
public class UserProfile extends MemberIdentified {
    private String guildId; //Snowflake Guild ID associated with this user's profile in a guild
    private String memberId; //Snowflake member ID
    private String username; //User's discord username
    private MemberChatCompletions completions;
    private CoinBank bank;

    public UserProfile(String serverId, String userId, String user) {
        super (userId);
        this.guildId = serverId;
        this.memberId = userId;
        this.username = user;
        this.completions = new MemberChatCompletions(userId);
        this.bank = new CoinBank(userId);
    }

    /**
     * Constructs a {@code Profile} for a specific {@code Member} inside of a {@code Guild}.
     * 
     * @param serverId the {@code Snowflake} ID of the {@code Guild} this profile exists in.
     * @param userId the {@code Snowflake} ID of the {@code Member} this profile is for.
     * @param user the username of the user
     */
    public UserProfile(String serverId, String userId, String user, MemberChatCompletions completions, CoinBank bank) {
        super(userId);
        this.guildId = serverId;
        this.memberId = userId;
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
        return memberId;
    }
    
    /**
     * Gets the username of the {@code Member} associated with this profile.
     * 
     * @return the username of the Member associated with this profile.
     */
    public String getUsername() {
        return username;
    }

    public MemberChatCompletions getCompletions() {
        return completions;
    }

    public CoinBank getBank() {
        return this.bank;
    }
}
