package com.georgster.game.cs2.wizard;

import com.georgster.api.faceit.FaceitAPIClient;
import com.georgster.api.faceit.exception.FaceitAPIException;
import com.georgster.api.faceit.model.FullMatchDetails;
import com.georgster.api.faceit.model.MatchDetails;
import com.georgster.cache.FaceitCache;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.game.cs2.util.CS2EmbedFormatter;
import com.georgster.wizard.InputWizard;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.spec.EmbedCreateSpec;

/**
 * A Wizard to handle the interactive full match view for CS2 Faceit matches.
 * <p>
 * This wizard provides an interactive button-driven interface for viewing comprehensive
 * match statistics. Users can view their individual match stats and optionally view
 * full game stats showing all players from both teams.
 * <p>
 * The wizard supports:
 * - Viewing initial match report (individual player stats)
 * - Clicking "Full Game Stats" button to view all players
 * - Optional "Back" button to return to individual stats
 */
public class CS2MatchWizard extends InputWizard {

    private final MatchDetails matchDetails;
    private final FaceitAPIClient apiClient;
    private final FaceitCache cache;
    private final String guildId;
    
    /**
     * Creates a CS2MatchWizard for the provided match.
     * 
     * @param event The event that prompted the wizard's creation
     * @param matchDetails The individual match details to display
     * @param apiClient The Faceit API client for fetching full match data
     * @param cache The cache for storing/retrieving full match details
     * @param guildId The guild ID for cache key construction
     */
    public CS2MatchWizard(CommandExecutionEvent event, MatchDetails matchDetails, 
                         FaceitAPIClient apiClient, FaceitCache cache, String guildId) {
        super(event, InputListenerFactory.createButtonMessageListener(event, "CS2 Match Details")
            .builder()
            .withXReaction(false)
            .withTimeoutDuration(300000) // 5 minute timeout
            .requireMatch(true, true)
            .build());
        
        this.matchDetails = matchDetails;
        this.apiClient = apiClient;
        this.cache = cache;
        this.guildId = guildId;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Begins the wizard by displaying the initial prompt with the "Full Game Stats" button.
     */
    @Override
    public void begin() {
        nextWindow("showInitialPrompt");
    }

    /**
     * Shows the initial prompt with the "Full Game Stats" button.
     */
    public void showInitialPrompt() {
        String prompt = "Click the button below to view full match statistics for all players.";
        String[] options = {"Full Game Stats", "Cancel"};
        
        withResponse(response -> {
            if (response.equalsIgnoreCase("Full Game Stats")) {
                nextWindow("showFullMatchStats");
            } else {
                
                // Acknowledge the interaction before shutdown
                try {
                    getInputListener().editCurrentMessageContent("Match details cancelled.");
                } catch (Exception e) {
                    logger.append("[CS2MatchWizard] Error acknowledging cancellation interaction: " + e.getMessage() + "\n", 
                        com.georgster.logs.LogDestination.NONAPI);
                }
                
                shutdown();
            }
        }, false, prompt, options);
    }

    /**
     * Fetches and displays the full match statistics showing all players from both teams.
     */
    public void showFullMatchStats() {
        
        try {
            // Check cache first
            FullMatchDetails fullMatch = cache.getFullMatchDetails(guildId, matchDetails.getMatchId());
            
            if (fullMatch == null) {
                logger.append("[CS2MatchWizard] Cache miss - fetching from API\n", 
                    com.georgster.logs.LogDestination.NONAPI);
                
                // Cache miss - fetch from API
                getInputListener().editCurrentMessageContent("Fetching full match details...");
                
                fullMatch = apiClient.fetchFullMatchDetails(matchDetails.getMatchId());
                
                // Store in cache
                if (fullMatch != null) {
                    cache.putFullMatchDetails(guildId, matchDetails.getMatchId(), fullMatch);
                }
            } else {
            }
            
            if (fullMatch != null) {
                
                // Format and display the full match stats embed
                EmbedCreateSpec embed = CS2EmbedFormatter.formatFullMatchStats(fullMatch);
                
                // Provide option to close
                String[] options = {"Close"};
                
                withResponse(response -> {
                    
                    // Acknowledge the interaction by editing the message before shutdown
                    // This prevents "interaction failed" errors
                    try {
                        getInputListener().editCurrentMessageContent("Match details closed.");
                    } catch (Exception e) {
                        logger.append("[CS2MatchWizard] Error acknowledging interaction: " + e.getMessage() + "\n", 
                            com.georgster.logs.LogDestination.NONAPI);
                    }
                    
                    shutdown();
                }, false, embed, options);
            } else {
                // Failed to fetch full match details
                logger.append("[CS2MatchWizard] Failed to fetch full match details - null response\n", 
                    com.georgster.logs.LogDestination.NONAPI);
                displayError("Unable to fetch full match details. Please try again later.");
            }
            
        } catch (FaceitAPIException e) {
            logger.append("[CS2MatchWizard] FaceitAPIException during fetch: " + e.getMessage() + "\n", 
                com.georgster.logs.LogDestination.NONAPI);
            displayError("Service temporarily unavailable. Please try again later.");
        }
    }

    /**
     * Displays an error message and provides option to close the wizard.
     * 
     * @param errorMessage The error message to display
     */
    public void displayError(String errorMessage) {
        logger.append("[CS2MatchWizard] Displaying error: " + errorMessage + "\n", 
            com.georgster.logs.LogDestination.NONAPI);
        
        EmbedCreateSpec errorEmbed = CS2EmbedFormatter.formatError(errorMessage);
        
        String[] options = {"Close"};
        withResponse(response -> {
            
            // Acknowledge the interaction by editing the message before shutdown
            try {
                getInputListener().editCurrentMessageContent("Error dialog closed.");
            } catch (Exception e) {
                logger.append("[CS2MatchWizard] Error acknowledging error dialog interaction: " + e.getMessage() + "\n", 
                    com.georgster.logs.LogDestination.NONAPI);
            }
            
            shutdown();
        }, false, errorEmbed, options);
    }
}
