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
            "**K/D Ratio**: %.2f\n**ADR**: %.1f\n**HS%%**: %.1f%%",
            stats.getKillDeathRatio(),
            stats.getAverageDamageRound(),
            stats.getHeadshotPercentage()
        );
        
        // Clutch statistics
        String clutchStats = String.format(
            "**1v1**: %d/%d (%.0f%% win rate)\n**1v2**: %d/%d (%.0f%% win rate)",
            stats.getTotalOneVOneWins(),
            stats.getTotalOneVOneCount(),
            stats.getOneVOneWinRate() * 100,
            stats.getTotalOneVTwoWins(),
            stats.getTotalOneVTwoCount(),
            stats.getOneVTwoWinRate() * 100
        );
        
        // Utility & Flash statistics
        String utilityStats = String.format(
            "**Utility/Round**: %.2f (%.0f%% success)\n**Utility Dmg/Round**: %.1f\n**Flashes/Round**: %.2f (%.0f%% success)\n**Enemies Flashed/Round**: %.2f",
            stats.getUtilityUsagePerRound(),
            stats.getUtilitySuccessRate() * 100,
            stats.getUtilityDamagePerRound(),
            stats.getFlashesPerRound(),
            stats.getFlashSuccessRate() * 100,
            stats.getEnemiesFlashedPerRound()
        );
        
        // Entry & Sniper statistics
        String specialStats = String.format(
            "**Entry Rate**: %.0f%% (%.0f%% success)\n**Entry Frags**: %d/%d\n**Sniper Kills**: %d (%.0f%% of rounds)\n**Longest Win Streak**: %d\n**Current Win Streak**: %d",
            stats.getEntryRate() * 100,
            stats.getEntrySuccessRate() * 100,
            stats.getTotalEntryWins(),
            stats.getTotalEntryCount(),
            stats.getTotalSniperKills(),
            stats.getSniperKillRate() * 100,
            stats.getLongestWinStreak(),
            stats.getCurrentWinStreak()
        );
        
        String recentForm = formatRecentForm(stats.getRecentForm());
        
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder()
                .title(title)
                .color(PRIMARY_COLOR)
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .addField("Profile", profileInfo, true)
                .addField("Match History", matchStats, true)
                .addField("Performance", performanceStats, false)
                .addField("Clutch Performance", clutchStats, true)
                .addField("Utility & Flashes", utilityStats, true)
                .addField("Advanced Stats", specialStats, false);
        
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
        String title = String.format("Player Comparison - %s vs %s", player1.getNickname(), player2.getNickname());
        
        // Color codes for visual distinction
        String p1Color = "`🔴`";  // Red for player 1
        String p2Color = "`🔵`";  // Blue for player 2
        
        // Rank Section
        String eloComp = player1.getElo() > player2.getElo()
            ? String.format("%s **%s** (Lvl %d) - Elo: %d / **%s** (Lvl %d) - Elo: %d", p1Color, player1.getNickname(), player1.getFaceitLevel(), player1.getElo(), player2.getNickname(), player2.getFaceitLevel(), player2.getElo())
            : player2.getElo() > player1.getElo()
            ? String.format("**%s** (Lvl %d) - Elo: %d / %s **%s** (Lvl %d) - Elo: %d", player1.getNickname(), player1.getFaceitLevel(), player1.getElo(), p2Color, player2.getNickname(), player2.getFaceitLevel(), player2.getElo())
            : String.format("**%s** (Lvl %d) - Elo: %d / **%s** (Lvl %d) - Elo: %d", player1.getNickname(), player1.getFaceitLevel(), player1.getElo(), player2.getNickname(), player2.getFaceitLevel(), player2.getElo());
        
        String rankSection = eloComp;
        
        // Core Stats with player indicators (only on winning stat)
        String kdComparison = stats1.getKillDeathRatio() > stats2.getKillDeathRatio() 
            ? String.format("%s **K/D**: %.2f / %.2f", p1Color, stats1.getKillDeathRatio(), stats2.getKillDeathRatio())
            : stats2.getKillDeathRatio() > stats1.getKillDeathRatio()
            ? String.format("**K/D**: %.2f / %s **%.2f**", stats1.getKillDeathRatio(), p2Color, stats2.getKillDeathRatio())
            : String.format("**K/D**: %.2f / %.2f", stats1.getKillDeathRatio(), stats2.getKillDeathRatio());
        
        String adrComparison = stats1.getAverageDamageRound() > stats2.getAverageDamageRound()
            ? String.format("%s **ADR**: %.1f / %.1f", p1Color, stats1.getAverageDamageRound(), stats2.getAverageDamageRound())
            : stats2.getAverageDamageRound() > stats1.getAverageDamageRound()
            ? String.format("**ADR**: %.1f / %s **%.1f**", stats1.getAverageDamageRound(), p2Color, stats2.getAverageDamageRound())
            : String.format("**ADR**: %.1f / %.1f", stats1.getAverageDamageRound(), stats2.getAverageDamageRound());
        
        String hsComparison = stats1.getHeadshotPercentage() > stats2.getHeadshotPercentage()
            ? String.format("%s **HS%%**: %.1f%% / %.1f%%", p1Color, stats1.getHeadshotPercentage(), stats2.getHeadshotPercentage())
            : stats2.getHeadshotPercentage() > stats1.getHeadshotPercentage()
            ? String.format("**HS%%**: %.1f%% / %s **%.1f%%**", stats1.getHeadshotPercentage(), p2Color, stats2.getHeadshotPercentage())
            : String.format("**HS%%**: %.1f%% / %.1f%%", stats1.getHeadshotPercentage(), stats2.getHeadshotPercentage());
        
        String coreStats = String.format("%s\n%s\n%s", kdComparison, adrComparison, hsComparison);
        
        // Match Record with player indicators (only on winning stat)
        String matchRecord = stats1.getWinRate() > stats2.getWinRate()
            ? String.format("%s **Matches**: %d W/L (%d%% WR) / **%d** W/L (%d%% WR)", p1Color, stats1.getTotalMatches(), Math.round(stats1.getWinRate()), stats2.getTotalMatches(), Math.round(stats2.getWinRate()))
            : stats2.getWinRate() > stats1.getWinRate()
            ? String.format("**Matches**: %d W/L (%d%% WR) / %s **%d** W/L (%d%% WR)", stats1.getTotalMatches(), Math.round(stats1.getWinRate()), p2Color, stats2.getTotalMatches(), Math.round(stats2.getWinRate()))
            : String.format("**Matches**: %d W/L (%d%% WR) / **%d** W/L (%d%% WR)", stats1.getTotalMatches(), Math.round(stats1.getWinRate()), stats2.getTotalMatches(), Math.round(stats2.getWinRate()));
        
        // Determine each player's unique strengths
        String player1Strengths = getPlayerStrengths(stats1, stats2, player1.getNickname());
        String player2Strengths = getPlayerStrengths(stats2, stats1, player2.getNickname());
        
        // Clutch Stats with player indicators (only on winning stat)
        String ov1Comparison = stats1.getOneVOneWinRate() > stats2.getOneVOneWinRate()
            ? String.format("%s **1v1**: %.0f%% (%d/%d) / %.0f%% (%d/%d)", p1Color, stats1.getOneVOneWinRate() * 100, stats1.getTotalOneVOneWins(), stats1.getTotalOneVOneCount(), stats2.getOneVOneWinRate() * 100, stats2.getTotalOneVOneWins(), stats2.getTotalOneVOneCount())
            : stats2.getOneVOneWinRate() > stats1.getOneVOneWinRate()
            ? String.format("**1v1**: %.0f%% (%d/%d) / %s **%.0f%%** (%d/%d)", stats1.getOneVOneWinRate() * 100, stats1.getTotalOneVOneWins(), stats1.getTotalOneVOneCount(), p2Color, stats2.getOneVOneWinRate() * 100, stats2.getTotalOneVOneWins(), stats2.getTotalOneVOneCount())
            : String.format("**1v1**: %.0f%% (%d/%d) / %.0f%% (%d/%d)", stats1.getOneVOneWinRate() * 100, stats1.getTotalOneVOneWins(), stats1.getTotalOneVOneCount(), stats2.getOneVOneWinRate() * 100, stats2.getTotalOneVOneWins(), stats2.getTotalOneVOneCount());
        
        String ov2Comparison = stats1.getOneVTwoWinRate() > stats2.getOneVTwoWinRate()
            ? String.format("%s **1v2**: %.0f%% (%d/%d) / %.0f%% (%d/%d)", p1Color, stats1.getOneVTwoWinRate() * 100, stats1.getTotalOneVTwoWins(), stats1.getTotalOneVTwoCount(), stats2.getOneVTwoWinRate() * 100, stats2.getTotalOneVTwoWins(), stats2.getTotalOneVTwoCount())
            : stats2.getOneVTwoWinRate() > stats1.getOneVTwoWinRate()
            ? String.format("**1v2**: %.0f%% (%d/%d) / %s **%.0f%%** (%d/%d)", stats1.getOneVTwoWinRate() * 100, stats1.getTotalOneVTwoWins(), stats1.getTotalOneVTwoCount(), p2Color, stats2.getOneVTwoWinRate() * 100, stats2.getTotalOneVTwoWins(), stats2.getTotalOneVTwoCount())
            : String.format("**1v2**: %.0f%% (%d/%d) / %.0f%% (%d/%d)", stats1.getOneVTwoWinRate() * 100, stats1.getTotalOneVTwoWins(), stats1.getTotalOneVTwoCount(), stats2.getOneVTwoWinRate() * 100, stats2.getTotalOneVTwoWins(), stats2.getTotalOneVTwoCount());
        
        String clutchStats = String.format("%s\n%s", ov1Comparison, ov2Comparison);
        
        // Aggression Stats with player indicators (only on winning stat)
        String entryRateComp = stats1.getEntryRate() > stats2.getEntryRate()
            ? String.format("%s **Entry Rate**: %.0f%% / %.0f%%", p1Color, stats1.getEntryRate() * 100, stats2.getEntryRate() * 100)
            : stats2.getEntryRate() > stats1.getEntryRate()
            ? String.format("**Entry Rate**: %.0f%% / %s **%.0f%%**", stats1.getEntryRate() * 100, p2Color, stats2.getEntryRate() * 100)
            : String.format("**Entry Rate**: %.0f%% / %.0f%%", stats1.getEntryRate() * 100, stats2.getEntryRate() * 100);
        
        String entrySuccessComp = stats1.getEntrySuccessRate() > stats2.getEntrySuccessRate()
            ? String.format("%s **Entry Success**: %.0f%% / %.0f%%", p1Color, stats1.getEntrySuccessRate() * 100, stats2.getEntrySuccessRate() * 100)
            : stats2.getEntrySuccessRate() > stats1.getEntrySuccessRate()
            ? String.format("**Entry Success**: %.0f%% / %s **%.0f%%**", stats1.getEntrySuccessRate() * 100, p2Color, stats2.getEntrySuccessRate() * 100)
            : String.format("**Entry Success**: %.0f%% / %.0f%%", stats1.getEntrySuccessRate() * 100, stats2.getEntrySuccessRate() * 100);
        
        String sniperComp = stats1.getSniperKillRate() > stats2.getSniperKillRate()
            ? String.format("%s **Sniper Rate**: %.0f%% / %.0f%%", p1Color, stats1.getSniperKillRate() * 100, stats2.getSniperKillRate() * 100)
            : stats2.getSniperKillRate() > stats1.getSniperKillRate()
            ? String.format("**Sniper Rate**: %.0f%% / %s **%.0f%%**", stats1.getSniperKillRate() * 100, p2Color, stats2.getSniperKillRate() * 100)
            : String.format("**Sniper Rate**: %.0f%% / %.0f%%", stats1.getSniperKillRate() * 100, stats2.getSniperKillRate() * 100);
        
        String aggressionStats = String.format("%s\n%s\n%s", entryRateComp, entrySuccessComp, sniperComp);
        
        // Overall winner
        int p1Wins = 0;
        if (player1.getElo() > player2.getElo()) p1Wins++;
        if (stats1.getKillDeathRatio() > stats2.getKillDeathRatio()) p1Wins++;
        if (stats1.getAverageDamageRound() > stats2.getAverageDamageRound()) p1Wins++;
        if (stats1.getWinRate() > stats2.getWinRate()) p1Wins++;
        if (stats1.getHeadshotPercentage() > stats2.getHeadshotPercentage()) p1Wins++;
        if (stats1.getOneVOneWinRate() > stats2.getOneVOneWinRate()) p1Wins++;
        if (stats1.getEntrySuccessRate() > stats2.getEntrySuccessRate()) p1Wins++;
        
        String verdict = "";
        if (p1Wins > 3) {
            verdict = String.format("%s **%s** has the edge - stronger fundamentals overall", p1Color, player1.getNickname());
        } else if (p1Wins < 3) {
            verdict = String.format("%s **%s** has the edge - stronger fundamentals overall", p2Color, player2.getNickname());
        } else {
            verdict = "**Close match** - Both players are well-balanced competitors";
        }
        
        return EmbedCreateSpec.builder()
                .title(title)
                .color(GOLD_COLOR)
                .addField("Rank & Rating", rankSection, false)
                .addField("Core Performance", coreStats, false)
                .addField("Win Streaks", String.format(
                    "%s / %s\n%s / %s",
                    stats1.getLongestWinStreak() > stats2.getLongestWinStreak() 
                        ? String.format("%s **Longest**: %d", p1Color, stats1.getLongestWinStreak())
                        : stats2.getLongestWinStreak() > stats1.getLongestWinStreak()
                        ? String.format("%s **Longest**: %d", p2Color, stats2.getLongestWinStreak())
                        : String.format("**Longest**: %d", stats1.getLongestWinStreak()),
                    stats2.getLongestWinStreak() > stats1.getLongestWinStreak()
                        ? String.format("%s **%d**", p2Color, stats2.getLongestWinStreak())
                        : stats1.getLongestWinStreak() > stats2.getLongestWinStreak()
                        ? String.format("**%d**", stats2.getLongestWinStreak())
                        : String.format("**%d**", stats2.getLongestWinStreak()),
                    stats1.getCurrentWinStreak() > stats2.getCurrentWinStreak()
                        ? String.format("%s **Current**: %d", p1Color, stats1.getCurrentWinStreak())
                        : stats2.getCurrentWinStreak() > stats1.getCurrentWinStreak()
                        ? String.format("%s **Current**: %d", p2Color, stats2.getCurrentWinStreak())
                        : String.format("**Current**: %d", stats1.getCurrentWinStreak()),
                    stats2.getCurrentWinStreak() > stats1.getCurrentWinStreak()
                        ? String.format("%s **%d**", p2Color, stats2.getCurrentWinStreak())
                        : stats1.getCurrentWinStreak() > stats2.getCurrentWinStreak()
                        ? String.format("**%d**", stats2.getCurrentWinStreak())
                        : String.format("**%d**", stats2.getCurrentWinStreak())
                ), false)
                .addField("Match Record", matchRecord, false)
                .addField("Clutch Performance", clutchStats, false)
                .addField("Aggression", aggressionStats, false)
                .addField(player1.getNickname() + "'s Strengths", player1Strengths, true)
                .addField(player2.getNickname() + "'s Strengths", player2Strengths, true)
                .addField("Verdict", verdict, false)
                .footer("Only the leading player in each stat is highlighted", null)
                .timestamp(Instant.now())
                .build();
    }
    
    private static String getPlayerStrengths(PlayerStats stats, PlayerStats opponentStats, String playerName) {
        java.util.ArrayList<String> strengths = new java.util.ArrayList<>();
        
        if (stats.getKillDeathRatio() > opponentStats.getKillDeathRatio()) {
            strengths.add("Superior K/D");
        }
        if (stats.getAverageDamageRound() > opponentStats.getAverageDamageRound()) {
            strengths.add("Higher ADR");
        }
        if (stats.getHeadshotPercentage() > opponentStats.getHeadshotPercentage()) {
            strengths.add("Better Precision");
        }
        if (stats.getOneVOneWinRate() > opponentStats.getOneVOneWinRate()) {
            strengths.add("1v1 Clutches");
        }
        if (stats.getEntrySuccessRate() > opponentStats.getEntrySuccessRate()) {
            strengths.add("Entry Fragging");
        }
        if (stats.getSniperKillRate() > opponentStats.getSniperKillRate()) {
            strengths.add("Sniper Control");
        }
        if (stats.getFlashesPerRound() > opponentStats.getFlashesPerRound()) {
            strengths.add("Flash Support");
        }
        if (stats.getWinRate() > opponentStats.getWinRate()) {
            strengths.add("Consistency");
        }
        
        if (strengths.isEmpty()) {
            return "Balanced player";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(3, strengths.size()); i++) {
            if (i > 0) result.append("\n");
            result.append("• ").append(strengths.get(i));
        }
        return result.toString();
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
     * Formats a link success embed.
     * 
     * @param player The Faceit player that was linked
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatLinkSuccess(FaceitPlayer player) {
        String description = String.format(
            "**Nickname**: %s\n**Faceit Level**: %d\n**Elo**: %d\n**Country**: %s",
            player.getNickname(),
            player.getFaceitLevel(),
            player.getElo(),
            player.getCountry() != null ? player.getCountry() : "Unknown"
        );
        
        String statusMessage = "Account linked successfully. Use `/cs2 stats` to view your stats.";
        
        return EmbedCreateSpec.builder()
                .title("✅ Faceit Account Linked!")
                .description(description)
                .addField("Status", statusMessage, false)
                .color(SUCCESS_COLOR)
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .footer("Data from Faceit API", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats an error message embed.
     * 
     * @param title The error title
     * @param errorMessage The error message to display
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatError(String title, String errorMessage) {
        return EmbedCreateSpec.builder()
                .title(title)
                .description(errorMessage)
                .color(ERROR_COLOR)
                .footer("If this persists, contact server admins", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats an error message embed with default title.
     * 
     * @param errorMessage The error message to display
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatError(String errorMessage) {
        return formatError("❌ Error", errorMessage);
    }
    
    /**
     * Formats a loading message embed.
     * 
     * @param message The loading message to display
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatLoadingMessage(String message) {
        return EmbedCreateSpec.builder()
                .title("⏳ Loading...")
                .description(message)
                .color(PRIMARY_COLOR)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats a "no recent matches" message embed with player profile info.
     * 
     * @param player The player's Faceit profile
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatNoRecentMatches(FaceitPlayer player) {
        String profileInfo = String.format(
            "**Nickname**: %s\n**Level**: %d\n**Elo**: %d",
            player.getNickname(),
            player.getFaceitLevel(),
            player.getElo()
        );
        
        return EmbedCreateSpec.builder()
                .title("📭 No Recent Matches")
                .description(player.getNickname() + " has no recent matches on record.")
                .addField("Player Profile", profileInfo, false)
                .addField("Status", "Play a match and try again in a few minutes.", false)
                .color(Color.of(0xf39c12)) // Orange color
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .footer("Data from Faceit API", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats an "insufficient data" message embed for players with limited match history.
     * 
     * @param player The player's Faceit profile
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatInsufficientData(FaceitPlayer player) {
        String profileInfo = String.format(
            "**Nickname**: %s\n**Level**: %d\n**Elo**: %d\n**Country**: %s",
            player.getNickname(),
            player.getFaceitLevel(),
            player.getElo(),
            player.getCountry() != null ? player.getCountry() : "Unknown"
        );
        
        return EmbedCreateSpec.builder()
                .title("📭 Limited Match History")
                .description(player.getNickname() + " has limited CS2 match history.")
                .addField("Player Profile", profileInfo, false)
                .addField("Note", "Statistics improve with more matches. Play some games and check back!", false)
                .color(Color.of(0xf39c12)) // Orange color
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .footer("Data from Faceit API", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats a match history embed showing last matches with quick stats.
     * 
     * @param matches List of MatchDetails (up to 10 matches)
     * @param player The player's Faceit profile
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatMatchHistory(List<MatchDetails> matches, FaceitPlayer player) {
        String title = String.format("📋 Match History - %s", player.getNickname());
        String subtitle = String.format("Last %d Matches", Math.min(matches.size(), 10));
        
        // Build match list
        StringBuilder matchList = new StringBuilder();
        int count = Math.min(matches.size(), 10);
        for (int i = 0; i < count; i++) {
            MatchDetails match = matches.get(i);
            String resultEmoji = match.getResult().equalsIgnoreCase("win") ? "✅" : "❌";
            String matchLine = String.format(
                "**#%d** %s **%s** | %s | K/D: %d/%d (%.2f) | ADR: %.1f | %s",
                i + 1,
                resultEmoji,
                match.getMapName(),
                match.getScore(),
                match.getKills(),
                match.getDeaths(),
                match.getKDRatio(),
                match.getAverageDamageRound(),
                formatTimestamp(match.getMatchTimestamp())
            );
            matchList.append(matchLine).append("\n");
        }
        
        // Calculate recent statistics summary
        int wins = 0;
        double totalKD = 0.0;
        double totalADR = 0.0;
        StringBuilder recentFormEmojis = new StringBuilder();
        
        for (MatchDetails match : matches) {
            if (match.getResult().equalsIgnoreCase("win")) {
                wins++;
                recentFormEmojis.append("🟢 ");
            } else {
                recentFormEmojis.append("🔴 ");
            }
            totalKD += match.getKDRatio();
            totalADR += match.getAverageDamageRound();
        }
        
        int totalMatches = matches.size();
        int losses = totalMatches - wins;
        double winRate = (totalMatches > 0) ? (wins * 100.0 / totalMatches) : 0.0;
        double avgKD = (totalMatches > 0) ? (totalKD / totalMatches) : 0.0;
        double avgADR = (totalMatches > 0) ? (totalADR / totalMatches) : 0.0;
        
        String summaryStats = String.format(
            "**Record**: %dW - %dL (%.1f%% win rate)\n**Average K/D**: %.2f\n**Average ADR**: %.1f",
            wins, losses, winRate, avgKD, avgADR
        );
        
        return EmbedCreateSpec.builder()
                .title(title)
                .description(subtitle)
                .color(PRIMARY_COLOR)
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .addField("Recent Form", recentFormEmojis.toString().trim(), false)
                .addField("Match List", matchList.toString(), false)
                .addField("Last " + totalMatches + " Matches Summary", summaryStats, false)
                .footer("Fetched from Faceit API", null)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * Formats a "no match history" message embed for players with 0 recorded matches.
     * 
     * @param player The player's Faceit profile
     * @return EmbedCreateSpec ready for Discord message
     */
    public static EmbedCreateSpec formatNoMatchHistory(FaceitPlayer player) {
        String profileInfo = String.format(
            "**Level**: %d\n**Elo**: %d",
            player.getFaceitLevel(),
            player.getElo()
        );
        
        return EmbedCreateSpec.builder()
                .title("📭 No Match History")
                .description(player.getNickname() + " has no recorded matches yet.")
                .addField("Profile", profileInfo, false)
                .addField("Status", "Play some matches and check back!", false)
                .color(Color.of(0xf39c12)) // Orange color
                .thumbnail(player.getAvatar() != null ? player.getAvatar() : "")
                .footer("Data from Faceit API", null)
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
