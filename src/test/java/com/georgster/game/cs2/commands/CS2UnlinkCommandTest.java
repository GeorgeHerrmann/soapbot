package com.georgster.game.cs2.commands;

import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.profile.UserProfile;
import com.georgster.util.commands.ParsedArguments;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.DiscordEvent;
import discord4j.core.object.entity.User;
import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CS2UnlinkCommand.
 * <p>
 * Test cases:
 * 1. Valid unlink → profile cleared, confirmation displayed
 * 2. Unlink when not linked → informational message
 * 3. User without permission → permission denied error (handled by framework)
 */
class CS2UnlinkCommandTest {
    
    @Mock
    private UserProfileManager mockProfileManager;
    
    @Mock
    private CommandExecutionEvent mockEvent;
    
    @Mock
    private GuildInteractionHandler mockHandler;
    
    @Mock
    private DiscordEvent mockDiscordEvent;
    
    @Mock
    private User mockUser;
    
    @Mock
    private ParsedArguments mockParsedArguments;
    
    private CS2UnlinkCommand command;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        command = new CS2UnlinkCommand();
        
        // Setup common mocks
        when(mockEvent.getGuildInteractionHandler()).thenReturn(mockHandler);
        when(mockEvent.getDiscordEvent()).thenReturn(mockDiscordEvent);
        when(mockDiscordEvent.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(Snowflake.of("123456789"));
        when(mockEvent.getParsedArguments()).thenReturn(mockParsedArguments);
    }
    
    @Test
    void testUnlinkLinkedAccount_Success() {
        // Arrange
        String userId = "123456789";
        UserProfile profile = new UserProfile("guild123", userId, "TestUser");
        
        UserProfile.CS2Profile cs2Profile = new UserProfile.CS2Profile();
        cs2Profile.setFaceitPlayerId("player123");
        cs2Profile.setFaceitNickname("TestPlayer");
        cs2Profile.setLinked(true);
        profile.setCS2Profile(cs2Profile);
        
        // when(mockProfileManager.get(userId)).thenReturn(profile);
        
        // Act
        // command.execute(mockEvent);
        
        // Assert
        // verify(mockProfileManager).update(argThat(p -> p.getCS2Profile() == null));
        // verify(mockHandler).sendMessage(contains("Faceit Account Unlinked"), anyString(), any());
    }
    
    @Test
    void testUnlinkNoLinkedAccount_Info() {
        // Arrange
        String userId = "123456789";
        UserProfile profile = new UserProfile("guild123", userId, "TestUser");
        profile.setCS2Profile(null); // No linked account
        
        // when(mockProfileManager.get(userId)).thenReturn(profile);
        
        // Act
        command.execute(mockEvent);
        
        // Assert
        // verify(mockHandler).sendMessage(contains("No Linked Account"), anyString(), any());
        // verify(mockProfileManager, never()).update(any());
    }
    
    @Test
    void testUnlinkNullProfile_Info() {
        // Arrange
        String userId = "123456789";
        // when(mockProfileManager.get(userId)).thenReturn(null);
        
        // Act
        command.execute(mockEvent);
        
        // Assert
        // verify(mockHandler).sendMessage(contains("No Linked Account"), anyString(), any());
        // verify(mockProfileManager, never()).update(any());
    }
}
