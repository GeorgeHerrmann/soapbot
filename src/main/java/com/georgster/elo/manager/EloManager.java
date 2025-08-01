package com.georgster.elo.manager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.georgster.control.util.ClientContext;
import com.georgster.control.manager.GuildedSoapManager;
import com.georgster.database.ProfileType;
import com.georgster.elo.model.EloRank;
import com.georgster.elo.model.EloRating;
import com.georgster.elo.model.EloCalculator;

/**
 * Manages EloRating objects for a guild.
 */
public class EloManager extends GuildedSoapManager<EloRating> {

    /**
     * Creates a new EloManager for the given ClientContext.
     * 
     * @param context The context of the SoapClient for this manager
     */
    public EloManager(ClientContext context) {
        super(context, ProfileType.ELO_RATINGS, EloRating.class, "memberId");
    }

    /**
     * Gets the leaderboard of all players sorted by rating (highest first).
     * 
     * @return List of EloRating objects sorted by rating
     */
    public List<EloRating> getLeaderboard() {
        return observees.stream()
                .sorted(Comparator.comparingInt(EloRating::getRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Gets all players in a specific rank.
     * 
     * @param rank The rank to filter by
     * @return List of EloRating objects in the specified rank
     */
    public List<EloRating> getRankings(EloRank rank) {
        return observees.stream()
                .filter(rating -> rating.getRank() == rank)
                .sorted(Comparator.comparingInt(EloRating::getRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Processes the result of a battle and updates both players' ratings.
     * 
     * @param winnerId The ID of the winning player
     * @param loserId The ID of the losing player
     */
    public void processBattleResult(String winnerId, String loserId) {
        EloRating winner = getOrCreateRating(winnerId);
        EloRating loser = getOrCreateRating(loserId);

        // Calculate rating changes
        int winnerNewRating = EloCalculator.calculateNewRating(winner, loser, true);
        int loserNewRating = EloCalculator.calculateNewRating(loser, winner, false);

        int winnerGain = winnerNewRating - winner.getRating();
        int loserLoss = loser.getRating() - loserNewRating;

        // Update ratings
        winner.recordWin(loserId, winnerGain);
        loser.recordLoss(winnerId, loserLoss);

        // Update in database
        update(winner);
        update(loser);
    }

    /**
     * Gets an existing EloRating or creates a new one with default values.
     * 
     * @param memberId The member's ID
     * @return The EloRating for the member
     */
    public EloRating getOrCreateRating(String memberId) {
        EloRating existing = get(memberId);
        if (existing == null) {
            existing = new EloRating(memberId);
            add(existing);
        }
        return existing;
    }

    /**
     * Gets the rank for a given rating value.
     * 
     * @param rating The rating value
     * @return The corresponding EloRank
     */
    public EloRank getRankForRating(int rating) {
        return EloRank.fromRating(rating);
    }

    /**
     * Gets the player's position on the leaderboard (1-indexed).
     * 
     * @param memberId The member's ID
     * @return The player's position, or -1 if not found
     */
    public int getLeaderboardPosition(String memberId) {
        List<EloRating> leaderboard = getLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getMemberId().equals(memberId)) {
                return i + 1; // 1-indexed
            }
        }
        return -1;
    }

    /**
     * Gets the top N players on the leaderboard.
     * 
     * @param count The number of top players to return
     * @return List of top EloRating objects
     */
    public List<EloRating> getTopPlayers(int count) {
        return getLeaderboard().stream()
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Gets players with similar ratings to the given player (within 200 points).
     * 
     * @param memberId The member's ID
     * @return List of players with similar ratings
     */
    public List<EloRating> getSimilarRankedPlayers(String memberId) {
        EloRating playerRating = get(memberId);
        if (playerRating == null) {
            return List.of();
        }

        int targetRating = playerRating.getRating();
        int range = 200;

        return observees.stream()
                .filter(rating -> !rating.getMemberId().equals(memberId))
                .filter(rating -> Math.abs(rating.getRating() - targetRating) <= range)
                .sorted(Comparator.comparingInt(EloRating::getRating).reversed())
                .collect(Collectors.toList());
    }
}
