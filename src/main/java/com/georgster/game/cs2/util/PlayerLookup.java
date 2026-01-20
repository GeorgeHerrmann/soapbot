package com.georgster.game.cs2.util;

import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.exception.PlayerNotFoundException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for resolving Faceit players from various input formats.
 * <p>
 * Implements player lookup precedence:
 * 1. Discord mention (@user) → check linked CS2Profile
 * 2. faceit: prefix → lookup by Faceit username
 * 3. steam: prefix → lookup by Steam ID (if implemented)
 * 4. Bare text → assume Faceit username
 * <p>
 * Throws {@link PlayerNotFoundException} with clear user messages if no match found.
 */
public class PlayerLookup {
    
    private static final Logger logger = LoggerFactory.getLogger(PlayerLookup.class);
    
    // Discord mention pattern: <@USER_ID> or <@!USER_ID>
    private static final Pattern DISCORD_MENTION_PATTERN = Pattern.compile("<@!?(\\d+)>");
    
    // Prefix patterns
    private static final String FACEIT_PREFIX = "faceit:";
    private static final String STEAM_PREFIX = "steam:";
    
    /**
     * Resolves a Faceit player from various input formats using precedence order.
     * <p>
     * Precedence:
     * 1. Discord mention → check linked CS2Profile
     * 2. faceit: prefix → lookup by Faceit username
     * 3. steam: prefix → lookup by Steam ID (if implemented)
     * 4. Bare text → assume Faceit username
     * 
     * @param reference The input reference (mention, username, or prefixed identifier)
     * @param profileManager The UserProfileManager to check linked accounts
     * @param apiClient The FaceitAPIClient to query Faceit API
     * @return FaceitPlayer object with player data
     * @throws PlayerNotFoundException if player cannot be found
     * @throws FaceitAPIException if API error occurs
     */
    public static FaceitPlayer resolveFaceitPlayer(String reference, UserProfileManager profileManager, FaceitAPIClient apiClient) 
            throws FaceitAPIException {
        
        if (reference == null || reference.trim().isEmpty()) {
            throw new PlayerNotFoundException("Player reference cannot be empty");
        }
        
        reference = reference.trim();
        
        // Precedence 1: Discord mention → check linked CS2Profile
        Matcher mentionMatcher = DISCORD_MENTION_PATTERN.matcher(reference);
        if (mentionMatcher.matches()) {
            String userId = mentionMatcher.group(1);
            logger.debug("Resolving player from Discord mention: {}", userId);
            
            UserProfile profile = profileManager.get(userId);
            if (profile != null && profile.hasLinkedFaceit()) {
                UserProfile.CS2Profile cs2Profile = profile.getCS2Profile();
                logger.info("Found linked Faceit account for Discord user {}: {}", userId, cs2Profile.getFaceitNickname());
                
                // Fetch fresh player data from API
                return apiClient.fetchPlayer(cs2Profile.getFaceitNickname());
            } else {
                throw new PlayerNotFoundException("Discord user <@" + userId + "> has not linked a Faceit account. Use `!cs2 link <faceit-username>` to link.");
            }
        }
        
        // Precedence 2: faceit: prefix → lookup by Faceit username
        if (reference.toLowerCase().startsWith(FACEIT_PREFIX)) {
            String username = reference.substring(FACEIT_PREFIX.length()).trim();
            logger.debug("Resolving player from faceit: prefix: {}", username);
            
            if (username.isEmpty()) {
                throw new PlayerNotFoundException("Faceit username cannot be empty after 'faceit:' prefix");
            }
            
            return apiClient.fetchPlayer(username);
        }
        
        // Precedence 3: steam: prefix → lookup by Steam ID (if implemented)
        if (reference.toLowerCase().startsWith(STEAM_PREFIX)) {
            String steamId = reference.substring(STEAM_PREFIX.length()).trim();
            logger.debug("Resolving player from steam: prefix: {}", steamId);
            
            if (steamId.isEmpty()) {
                throw new PlayerNotFoundException("Steam ID cannot be empty after 'steam:' prefix");
            }
            
            // Steam ID lookup not yet implemented in Faceit API
            throw new PlayerNotFoundException("Steam ID lookup is not yet supported. Use faceit:username instead.");
        }
        
        // Precedence 4: Bare text → assume Faceit username
        logger.debug("Resolving player from bare text as Faceit username: {}", reference);
        return apiClient.fetchPlayer(reference);
    }
    
    /**
     * Extracts the Discord user ID from a mention string.
     * 
     * @param mention The Discord mention string (e.g., "<@123456>" or "<@!123456>")
     * @return The extracted user ID, or null if not a valid mention
     */
    public static String extractUserIdFromMention(String mention) {
        if (mention == null) {
            return null;
        }
        
        Matcher matcher = DISCORD_MENTION_PATTERN.matcher(mention.trim());
        return matcher.matches() ? matcher.group(1) : null;
    }
    
    /**
     * Checks if a string is a Discord mention.
     * 
     * @param reference The input reference string
     * @return true if the string is a Discord mention, false otherwise
     */
    public static boolean isDiscordMention(String reference) {
        if (reference == null) {
            return false;
        }
        return DISCORD_MENTION_PATTERN.matcher(reference.trim()).matches();
    }
    
    /**
     * Checks if a string has a faceit: prefix.
     * 
     * @param reference The input reference string
     * @return true if the string has faceit: prefix, false otherwise
     */
    public static boolean hasFaceitPrefix(String reference) {
        if (reference == null) {
            return false;
        }
        return reference.trim().toLowerCase().startsWith(FACEIT_PREFIX);
    }
    
    /**
     * Checks if a string has a steam: prefix.
     * 
     * @param reference The input reference string
     * @return true if the string has steam: prefix, false otherwise
     */
    public static boolean hasSteamPrefix(String reference) {
        if (reference == null) {
            return false;
        }
        return reference.trim().toLowerCase().startsWith(STEAM_PREFIX);
    }
}
