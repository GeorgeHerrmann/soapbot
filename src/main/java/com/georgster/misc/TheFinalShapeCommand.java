package com.georgster.misc;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.util.GuildInteractionHandler;

import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * A command to see how long until The Final Shape drops.
 */
public class TheFinalShapeCommand implements Command {

    /**
     * {@inheritDoc}
     */
    public void execute(CommandExecutionEvent event) {
        GuildInteractionHandler handler = event.getGuildInteractionHandler();

        LocalDateTime finalShapeReleaseDate = LocalDateTime.of(2024, Month.of(2), 28, 12, 0);
        LocalDateTime currentTime = LocalDateTime.now();
        int months = (int) currentTime.until(finalShapeReleaseDate, ChronoUnit.MONTHS);
        int days = (int) currentTime.until(finalShapeReleaseDate.minusMonths(months), ChronoUnit.DAYS);
        int hours = (int) currentTime.until(finalShapeReleaseDate.minusMonths(months).minusDays(days), ChronoUnit.HOURS);
        int minutes = (int) currentTime.until(finalShapeReleaseDate.minusMonths(months).minusDays(days).minusHours(hours), ChronoUnit.MINUTES);

        handler.sendText("Destiny 2: The Final Shape drops in " + months + " months, " + days + " days, " + hours + " hours, " + minutes + " minutes.");
        event.getLogger().append("Showing how long until Destiny 2: The Final Shape drops", LogDestination.NONAPI, LogDestination.API);
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "Aliases: " + getAliases().toString() +
        "\n!thefinalshape - See how long until the expansion drops.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationCommandRequest getCommandApplicationInformation() {
        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("See how long until Destiny 2: The Final Shape drops")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        return List.of("thefinalshape", "tfs");
    }
}
