package com.georgster.game.cs2.util;

import com.georgster.api.faceit.model.*;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for formatting Discord embeds for CS2 Faceit integration.
 * <p>
 * Provides static methods to create visually consistent Discord embeds for:
 * - Match reports
 * - Player statistics
 * - Player comparisons
 * - Server leaderboards
 * - Error messages
 * <p>
 * Uses consistent color scheme:
 * - Blue (#5865F2): Primary/informational
 * - Green (#57F287): Positive/success
 * - Red (#ED4245): Errors/warnings
 * - Gold (#FEE75C): Special/highlights
 */
public class CS2EmbedFormatter {
    
    // Color constants
    private static final Color PRIMARY_COLOR = Color.of(0x5865F2);    // Discord Blue
    private static final Color SUCCESS_COLOR = Color.of(0x57F287);    // Green
    private static final Color ERROR_COLOR = Color.of(0xED4245);      // Red
    private static final Color GOLD_COLOR = Color.of(0xFEE75C);       // Gold
    
    // Date formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    /**
     * Formats a match report embed.
     * 
     * @param match The match details
     * @param stats The player's lifetime stats for comparison
     * @param player The player's profile
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatMatchReport(MatchDetails match, PlayerStats stats, FaceitPlayer player) {
        Color embedColor = match.getResult().equalsIgnoreCase("win") ? SUCCESS_COLOR : ERROR_COLOR;
        
        String title = String.format("🎮 Last Match Report - %s", player.getNickname());
        
        String matchInfo = String.format("**Map**: %s\n**Score**: %s\n**Result**: %s\n**Date**: %s",
            match.getMapName(),
            match.getScore(),
            match.getResult().toUpperCase(),
            formatTimestamp(match.getMatchTimestamp())
        );
        
        String performanceStats = String.format(
            "**K/D/A**: %d/%d/%d (%.2f)\n**ADR**: %.1f\n**HS%%**: %.1f%%\n**MVPs**: %d",
            match.getKills(),
            match.getDeaths(),
            match.getAssists(),
            match.getKDRatio(),
            match.getAverageDamageRound(),
            match.getHeadshotPercentage(),
            match.getMvps()
        );
        
        String vsAverage = String.format(
            "**K/D**: %.2f (avg: %.2f) %s\n**ADR**: %.1f (avg: %.1f) %s\n**HS%%**: %.1f%% (avg: %.1f%%) %s",
            match.getKDRatio(), stats.getKillDeathRatio(), getComparisonEmoji(match.getKDRatio(), stats.getKillDeathRatio()),
            match.getAverageDamageRound(), stats.getAverageDamageRound(), getComparisonEmoji(match.getAverageDamageRound(), stats.getAverageDamageRound()),
            match.getHeadshotPercentage(), stats.getHeadshotPercentage(), getComparisonEmoji(match.getHeadshotPercentage(), stats.getHeadshotPercentage())
        );
        
        return EmbedCreateSpec.builder()
                .title(title)
                .color(embedColor)
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .addField("Match Info", matchInfo, false)
                .addField("Performance", performanceStats, true)
                .addField("vs. Lifetime Average", vsAverage, true)
                .footer("Fetched from Faceit API", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats a player statistics embed.
     * 
     * @param stats The player's statistics
     * @param player The player's profile
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatPlayerStats(PlayerStats stats, FaceitPlayer player) {
        String title = String.format("📊 CS2 Statistics - %s", player.getNickname());
        
        String profileInfo = String.format(
            "**Level**: %d\n**Elo**: %d\n**Country**: %s",
            player.getFaceitLevel(),
            player.getElo(),
            player.getCountry() != null ? player.getCountry() : "Unknown"
        );
        
        String matchStats = String.format(
            "**Matches**: %d (%d W / %d L)\n**Win Rate**: %.1f%%",
            stats.getTotalMatches(),
            stats.getWins(),
            stats.getLosses(),
            stats.getWinRate()
        );
        
        String performanceStats = String.format(
            "**K/D Ratio**: %.2f\n**ADR**: %.1f\n**HS%%**: %.1f%%\n**MVPs/Match**: %.2f",
            stats.getKillDeathRatio(),
            stats.getAverageDamageRound(),
            stats.getHeadshotPercentage(),
            stats.getMVPsPerMatch()
        );
        
        String recentForm = formatRecentForm(stats.getRecentForm());
        
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .title(title)
                .color(PRIMARY_COLOR)
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .addField("Profile", profileInfo, true)
                .addField("Match History", matchStats, true)
                .addField("Performance", performanceStats, false);
        
        if (!recentForm.isEmpty()) {
            builder.addField("Recent Form (Last 5)", recentForm, false);
        }
        
        // Add top maps if available
        if (!stats.getTopMaps().isEmpty()) {
            String topMaps = formatTopMaps(stats.getTopMaps());
            builder.addField("Top Maps", topMaps, false);
        }
        
        return builder
                .footer("Updated every 5 minutes", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats a player comparison embed.
     * 
     * @param stats1 First player's statistics
     * @param stats2 Second player's statistics
     * @param player1 First player's profile
     * @param player2 Second player's profile
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatComparison(PlayerStats stats1, PlayerStats stats2, FaceitPlayer player1, FaceitPlayer player2) {
        String title = String.format("⚔️ Player Comparison");
        
        String eloComparison = String.format(
            "%s: **%d** %s\n%s: **%d** %s",
            player1.getNickname(), player1.getElo(), player1.getElo() > player2.getElo() ? "🔥" : "",
            player2.getNickname(), player2.getElo(), player2.getElo() > player1.getElo() ? "🔥" : ""
        );
        
        String kdComparison = String.format(
            "%s: **%.2f** %s\n%s: **%.2f** %s",
            player1.getNickname(), stats1.getKillDeathRatio(), stats1.getKillDeathRatio() > stats2.getKillDeathRatio() ? "↑" : "",
            player2.getNickname(), stats2.getKillDeathRatio(), stats2.getKillDeathRatio() > stats1.getKillDeathRatio() ? "↑" : ""
        );
        
        String adrComparison = String.format(
            "%s: **%.1f** %s\n%s: **%.1f** %s",
            player1.getNickname(), stats1.getAverageDamageRound(), stats1.getAverageDamageRound() > stats2.getAverageDamageRound() ? "↑" : "",
            player2.getNickname(), stats2.getAverageDamageRound(), stats2.getAverageDamageRound() > stats1.getAverageDamageRound() ? "↑" : ""
        );
        
        String winRateComparison = String.format(
            "%s: **%.1f%%** %s\n%s: **%.1f%%** %s",
            player1.getNickname(), stats1.getWinRate(), stats1.getWinRate() > stats2.getWinRate() ? "↑" : "",
            player2.getNickname(), stats2.getWinRate(), stats2.getWinRate() > stats1.getWinRate() ? "↑" : ""
        );
        
        // Summary
        int p1Wins = 0;
        int p2Wins = 0;
        if (player1.getElo() > player2.getElo()) p1Wins++; else p2Wins++;
        if (stats1.getKillDeathRatio() > stats2.getKillDeathRatio()) p1Wins++; else p2Wins++;
        if (stats1.getAverageDamageRound() > stats2.getAverageDamageRound()) p1Wins++; else p2Wins++;
        if (stats1.getWinRate() > stats2.getWinRate()) p1Wins++; else p2Wins++;
        
        String summary = p1Wins > p2Wins 
            ? String.format("**%s** is currently performing better", player1.getNickname())
            : p2Wins > p1Wins 
                ? String.format("**%s** is currently performing better", player2.getNickname())
                : "**Balanced matchup** - both players are performing similarly";
        
        return EmbedCreateSpec.builder()
                .title(title)
                .color(GOLD_COLOR)
                .addField("Elo Rating", eloComparison, true)
                .addField("K/D Ratio", kdComparison, true)
                .addField("ADR", adrComparison, true)
                .addField("Win Rate", winRateComparison, true)
                .addField("Summary", summary, false)
                .footer("Data from Faceit API", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats a server leaderboard embed.
     * 
     * @param leaderboard The server leaderboard
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatLeaderboard(ServerLeaderboard leaderboard) {
        String title = "🏆 Server CS2 Leaderboard";
        
        StringBuilder rankingsBuilder = new StringBuilder();
        List<LeaderboardEntry> entries = leaderboard.getEntries();
        
        int displayCount = Math.min(entries.size(), 10);
        for (int i = 0; i < displayCount; i++) {
            LeaderboardEntry entry = entries.get(i);
            String medal = getMedalEmoji(entry.getRank());
            rankingsBuilder.append(String.format(
                "%s **#%d** - %s (Lvl %d)\n  Elo: %d | K/D: %.2f | <@%s>\n",
                medal,
                entry.getRank(),
                entry.getFaceitNickname(),
                entry.getFaceitLevel(),
                entry.getElo(),
                entry.getKillDeathRatio(),
                entry.getDiscordMentionId()
            ));
        }
        
        String stats = String.format(
            "**Total Linked Players**: %d\n**Server Average Elo**: %.0f",
            leaderboard.size(),
            leaderboard.getServerAverageElo()
        );
        
        return EmbedCreateSpec.builder()
                .title(title)
                .color(GOLD_COLOR)
                .description(rankingsBuilder.toString())
                .addField("Server Stats", stats, false)
                .footer("Updated every 10 minutes", null)
                .timestamp(leaderboard.getRefreshedAt())
                .build();
    }
    
    /**
     * Formats an error message embed.
     * 
     * @param errorMessage The error message to display
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatError(String errorMessage) {
        return EmbedCreateSpec.builder()
                .title("❌ Error")
                .description(errorMessage)
                .color(ERROR_COLOR)
                .footer("If this persists, contact server admins", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats a success message embed.
     * 
     * @param message The success message to display
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatSuccess(String message) {
        return EmbedCreateSpec.builder()
                .title("✅ Success")
                .description(message)
                .color(SUCCESS_COLOR)
                .timestamp(Instant.now())
                .build();
    }
    
    // Helper methods
    
    private static String getComparisonEmoji(double current, double average) {
        if (current > average) return "↑";
        if (current < average) return "↓";
        return "=";
    }
    
    private static String getMedalEmoji(int rank) {
        switch (rank) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return "  ";
        }
    }
    
    private static String formatTimestamp(long unixTimestamp) {
        if (unixTimestamp == 0) return "Unknown";
        Instant instant = Instant.ofEpochSecond(unixTimestamp);
        return DATE_FORMATTER.format(instant.atZone(ZoneId.systemDefault()));
    }
    
    private static String formatRecentForm(List<String> recentForm) {
        if (recentForm == null || recentForm.isEmpty()) {
            return "";
        }
        
        StringBuilder form = new StringBuilder();
        for (String result : recentForm) {
            form.append(result.equalsIgnoreCase("W") ? "🟢" : "🔴");
        }
        return form.toString();
    }
    
    private static String formatTopMaps(List<PlayerStats.MapStats> topMaps) {
        if (topMaps == null || topMaps.isEmpty()) {
            return "No map data available";
        }
        
        StringBuilder maps = new StringBuilder();
        int count = Math.min(topMaps.size(), 3);
        for (int i = 0; i < count; i++) {
            PlayerStats.MapStats map = topMaps.get(i);
            maps.append(String.format("**%s**: %.1f%% WR, %.2f K/D (%d matches)\n",
                map.getMapName(),
                map.getWinRate(),
                map.getKdRatio(),
                map.getMatchesPlayed()
            ));
        }
        return maps.toString();
    }
}
