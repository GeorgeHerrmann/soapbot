package com.georgster.elo.manager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.georgster.control.manager.GuildedSoapManager;
import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.elo.EloCalculator;
import com.georgster.elo.EloRank;
import com.georgster.elo.EloRating;

/**
 * Manages all {@link EloRating EloRatings} for a guild.
 * Handles rating calculations, leaderboards, and battle processing.
 */
public class EloManager extends GuildedSoapManager<EloRating> {
    
    /**
     * Creates a new EloManager for the given guild context.
     * 
     * @param context The context of the SoapClient for this manager
     */
    public EloManager(ClientContext context) {
        super(context, ProfileType.ELO_RATINGS, EloRating.class, "memberId");
    }

    /**
     * Gets or creates an EloRating for a member.
     * If the member doesn't have a rating, creates one with default values (1500 rating).
     * 
     * @param memberId The Discord member ID
     * @return The EloRating for the member
     */
    public EloRating getOrCreateRating(String memberId) {
        EloRating rating = get(memberId);
        if (rating == null) {
            rating = new EloRating(memberId);
            add(rating);
        }
        return rating;
    }

    /**
     * Gets the leaderboard sorted by rating (highest first).
     * 
     * @return List of EloRatings sorted by rating descending
     */
    public List<EloRating> getLeaderboard() {
        return observees.stream()
                .sorted(Comparator.comparingInt(EloRating::getRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Gets the leaderboard for a specific rank tier.
     * 
     * @param rank The rank to filter by
     * @return List of EloRatings in the specified rank, sorted by rating descending
     */
    public List<EloRating> getRankings(EloRank rank) {
        return observees.stream()
                .filter(rating -> rating.getRank() == rank)
                .sorted(Comparator.comparingInt(EloRating::getRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Gets the top N players by rating.
     * 
     * @param limit The maximum number of players to return
     * @return List of top EloRatings
     */
    public List<EloRating> getTopPlayers(int limit) {
        return getLeaderboard().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Gets a member's rank position on the leaderboard (1-based).
     * 
     * @param memberId The member ID to find
     * @return The rank position (1 = highest), or -1 if not found
     */
    public int getLeaderboardPosition(String memberId) {
        List<EloRating> leaderboard = getLeaderboard();
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getMemberId().equals(memberId)) {
                return i + 1; // 1-based ranking
            }
        }
        return -1; // Not found
    }

    /**
     * Processes the result of an Elo battle, updating both players' ratings.
     * 
     * @param winnerId The member ID of the winner
     * @param loserId The member ID of the loser
     */
    public void processBattleResult(String winnerId, String loserId) {
        EloRating winner = getOrCreateRating(winnerId);
        EloRating loser = getOrCreateRating(loserId);

        // Calculate new ratings
        int winnerNewRating = EloCalculator.calculateNewRating(winner, loser, true);
        int loserNewRating = EloCalculator.calculateNewRating(loser, winner, false);

        // Update ratings
        winner.updateAfterMatch(winnerNewRating, true, loserId);
        loser.updateAfterMatch(loserNewRating, false, winnerId);

        // Save to database
        update(winner);
        update(loser);
    }

    /**
     * Gets all players who have played at least one match.
     * 
     * @return List of EloRatings for active players
     */
    public List<EloRating> getActivePlayers() {
        return observees.stream()
                .filter(rating -> rating.getMatchesPlayed() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Gets players within a certain rating range.
     * 
     * @param minRating Minimum rating (inclusive)
     * @param maxRating Maximum rating (inclusive)
     * @return List of EloRatings within the range
     */
    public List<EloRating> getPlayersInRatingRange(int minRating, int maxRating) {
        return observees.stream()
                .filter(rating -> rating.getRating() >= minRating && rating.getRating() <= maxRating)
                .sorted(Comparator.comparingInt(EloRating::getRating).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Finds suitable opponents for a player based on rating proximity.
     * Returns players within ±200 rating points, sorted by rating similarity.
     * 
     * @param memberId The player looking for opponents
     * @param excludeSelf Whether to exclude the player themselves
     * @return List of potential opponents
     */
    public List<EloRating> findSuitableOpponents(String memberId, boolean excludeSelf) {
        EloRating player = getOrCreateRating(memberId);
        int playerRating = player.getRating();
        
        return observees.stream()
                .filter(rating -> !excludeSelf || !rating.getMemberId().equals(memberId))
                .filter(rating -> Math.abs(rating.getRating() - playerRating) <= 200)
                .sorted(Comparator.comparingInt(rating -> 
                    Math.abs(rating.getRating() - playerRating)))
                .collect(Collectors.toList());
    }

    /**
     * Gets statistics about the guild's Elo system.
     * 
     * @return EloStats object with various statistics
     */
    public EloStats getGuildStats() {
        List<EloRating> activePlayers = getActivePlayers();
        
        if (activePlayers.isEmpty()) {
            return new EloStats(0, 0, 0, 0, 0, null);
        }

        int totalPlayers = activePlayers.size();
        int totalMatches = activePlayers.stream().mapToInt(EloRating::getMatchesPlayed).sum() / 2; // Divide by 2 since each match involves 2 players
        int highestRating = activePlayers.stream().mapToInt(EloRating::getRating).max().orElse(0);
        int lowestRating = activePlayers.stream().mapToInt(EloRating::getRating).min().orElse(0);
        double averageRating = activePlayers.stream().mapToInt(EloRating::getRating).average().orElse(0);
        
        EloRating topPlayer = activePlayers.stream()
                .max(Comparator.comparingInt(EloRating::getRating))
                .orElse(null);

        return new EloStats(totalPlayers, totalMatches, highestRating, lowestRating, averageRating, topPlayer);
    }

    /**
     * Simple statistics container for guild Elo data.
     */
    public static class EloStats {
        private final int totalPlayers;
        private final int totalMatches;
        private final int highestRating;
        private final int lowestRating;
        private final double averageRating;
        private final EloRating topPlayer;

        public EloStats(int totalPlayers, int totalMatches, int highestRating, 
                       int lowestRating, double averageRating, EloRating topPlayer) {
            this.totalPlayers = totalPlayers;
            this.totalMatches = totalMatches;
            this.highestRating = highestRating;
            this.lowestRating = lowestRating;
            this.averageRating = averageRating;
            this.topPlayer = topPlayer;
        }

        public int getTotalPlayers() { return totalPlayers; }
        public int getTotalMatches() { return totalMatches; }
        public int getHighestRating() { return highestRating; }
        public int getLowestRating() { return lowestRating; }
        public double getAverageRating() { return averageRating; }
        public EloRating getTopPlayer() { return topPlayer; }
    }
}
