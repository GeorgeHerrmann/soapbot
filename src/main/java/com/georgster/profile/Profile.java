package com.georgster.profile;

public class Profile {
    private String guildId; //Snowflake Guild ID associated with this user's profile in a guild
    private String memberId; //Snowflake member ID
    private String username; //User's discord username
    public Profile(String serverId, String userId, String user) {
        guildId = serverId;
        memberId = userId;
        username = user;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getMemberId() {
        return memberId;
    }
    
    public String getUsername() {
        return username;
    }
}
