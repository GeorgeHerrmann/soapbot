package com.georgster.game.cs2.util;

import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.exception.PlayerNotFoundException;
import com.georgster.api.faceit.model.FaceitPlayer;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.profile.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerLookup utility class.
 * <p>
 * Test cases:
 * 1. Discord mention with linked account → resolve to FaceitPlayer
 * 2. faceit: prefix → lookup by username
 * 3. steam: prefix → lookup by Steam ID (if implemented)
 * 4. Bare text → try as Faceit username
 * 5. No match found → PlayerNotFoundException with clear message
 * 6. Empty/null reference → PlayerNotFoundException
 * 7. Discord mention without linked account → PlayerNotFoundException
 */
class PlayerLookupTest {
    
    @Mock
    private FaceitAPIClient mockApiClient;
    
    @Mock
    private UserProfileManager mockProfileManager;
    
    private FaceitPlayer mockPlayer;
    
    @BeforeEach
    void setUp() throws FaceitAPIException {
        MockitoAnnotations.openMocks(this);
        
        // Create mock FaceitPlayer
        mockPlayer = new FaceitPlayer();
        mockPlayer.setPlayerId("player123");
        mockPlayer.setNickname("TestPlayer");
        mockPlayer.setElo(1500);
        mockPlayer.setFaceitLevel(5);
    }
    
    @Test
    void testResolveDiscordMentionWithLinkedAccount_Success() throws FaceitAPIException {
        // Arrange
        String userId = "123456789";
        String mention = "<@" + userId + ">";
        
        UserProfile profile = new UserProfile("guild123", userId, "TestUser");
        UserProfile.CS2Profile cs2Profile = new UserProfile.CS2Profile();
        cs2Profile.setFaceitPlayerId("player123");
        cs2Profile.setFaceitNickname("TestPlayer");
        cs2Profile.setLinked(true);
        profile.setCS2Profile(cs2Profile);
        
        when(mockProfileManager.get(userId)).thenReturn(profile);
        when(mockApiClient.fetchPlayer("TestPlayer")).thenReturn(mockPlayer);
        
        // Act
        FaceitPlayer result = PlayerLookup.resolveFaceitPlayer(mention, mockProfileManager, mockApiClient);
        
        // Assert
        assertNotNull(result);
        assertEquals("TestPlayer", result.getNickname());
        verify(mockApiClient).fetchPlayer("TestPlayer");
    }
    
    @Test
    void testResolveDiscordMentionWithoutLinkedAccount_ThrowsException() {
        // Arrange
        String userId = "123456789";
        String mention = "<@" + userId + ">";
        
        UserProfile profile = new UserProfile("guild123", userId, "TestUser");
        profile.setCS2Profile(null); // No linked account
        
        when(mockProfileManager.get(userId)).thenReturn(profile);
        
        // Act & Assert
        PlayerNotFoundException exception = assertThrows(
            PlayerNotFoundException.class,
            () -> PlayerLookup.resolveFaceitPlayer(mention, mockProfileManager, mockApiClient)
        );
        assertTrue(exception.getMessage().contains("has not linked a Faceit account"));
    }
    
    @Test
    void testResolveFaceitPrefix_Success() throws FaceitAPIException {
        // Arrange
        String reference = "faceit:TestPlayer";
        when(mockApiClient.fetchPlayer("TestPlayer")).thenReturn(mockPlayer);
        
        // Act
        FaceitPlayer result = PlayerLookup.resolveFaceitPlayer(reference, mockProfileManager, mockApiClient);
        
        // Assert
        assertNotNull(result);
        assertEquals("TestPlayer", result.getNickname());
        verify(mockApiClient).fetchPlayer("TestPlayer");
    }
    
    @Test
    void testResolveSteamPrefix_Success() throws FaceitAPIException {
        // Arrange
        String steamId = "STEAM_0:1:123456";
        String reference = "steam:" + steamId;
        when(mockApiClient.fetchPlayerBySteamId(steamId)).thenReturn(mockPlayer);
        
        // Act
        FaceitPlayer result = PlayerLookup.resolveFaceitPlayer(reference, mockProfileManager, mockApiClient);
        
        // Assert
        assertNotNull(result);
        assertEquals("TestPlayer", result.getNickname());
        verify(mockApiClient).fetchPlayerBySteamId(steamId);
    }
    
    @Test
    void testResolveBareUsername_Success() throws FaceitAPIException {
        // Arrange
        String username = "TestPlayer";
        when(mockApiClient.fetchPlayer(username)).thenReturn(mockPlayer);
        
        // Act
        FaceitPlayer result = PlayerLookup.resolveFaceitPlayer(username, mockProfileManager, mockApiClient);
        
        // Assert
        assertNotNull(result);
        assertEquals("TestPlayer", result.getNickname());
        verify(mockApiClient).fetchPlayer(username);
    }
    
    @Test
    void testResolveEmptyReference_ThrowsException() {
        // Act & Assert
        assertThrows(
            PlayerNotFoundException.class,
            () -> PlayerLookup.resolveFaceitPlayer("", mockProfileManager, mockApiClient)
        );
        
        assertThrows(
            PlayerNotFoundException.class,
            () -> PlayerLookup.resolveFaceitPlayer("   ", mockProfileManager, mockApiClient)
        );
    }
    
    @Test
    void testResolveNullReference_ThrowsException() {
        // Act & Assert
        assertThrows(
            PlayerNotFoundException.class,
            () -> PlayerLookup.resolveFaceitPlayer(null, mockProfileManager, mockApiClient)
        );
    }
    
    @Test
    void testResolveInvalidUsername_ThrowsException() throws FaceitAPIException {
        // Arrange
        String invalidUsername = "NonExistentPlayer";
        when(mockApiClient.fetchPlayer(invalidUsername))
            .thenThrow(new PlayerNotFoundException("Player not found"));
        
        // Act & Assert
        assertThrows(
            PlayerNotFoundException.class,
            () -> PlayerLookup.resolveFaceitPlayer(invalidUsername, mockProfileManager, mockApiClient)
        );
    }
    
    @Test
    void testResolveDiscordMentionWithExclamation_Success() throws FaceitAPIException {
        // Arrange - Discord mentions can have ! after @ for nickname format
        String userId = "123456789";
        String mention = "<@!" + userId + ">";
        
        UserProfile profile = new UserProfile("guild123", userId, "TestUser");
        UserProfile.CS2Profile cs2Profile = new UserProfile.CS2Profile();
        cs2Profile.setFaceitPlayerId("player123");
        cs2Profile.setFaceitNickname("TestPlayer");
        cs2Profile.setLinked(true);
        profile.setCS2Profile(cs2Profile);
        
        when(mockProfileManager.get(userId)).thenReturn(profile);
        when(mockApiClient.fetchPlayer("TestPlayer")).thenReturn(mockPlayer);
        
        // Act
        FaceitPlayer result = PlayerLookup.resolveFaceitPlayer(mention, mockProfileManager, mockApiClient);
        
        // Assert
        assertNotNull(result);
        assertEquals("TestPlayer", result.getNickname());
    }
}
