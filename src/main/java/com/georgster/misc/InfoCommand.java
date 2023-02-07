package com.georgster.misc;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.georgster.Command;
import com.georgster.control.util.CommandPipeline;
import com.georgster.util.GuildManager;

import discord4j.discordjson.json.ApplicationCommandRequest;

public class InfoCommand implements Command {
    public void execute(CommandPipeline pipeline, GuildManager manager) {
        LocalDateTime dropDate = LocalDateTime.of(2023, 2, 28, 12, 0).plusHours(5);
        //Get the current LocalDateTime in EST
        LocalDateTime now = LocalDateTime.now().plusHours(5);
        long daysUntil = ChronoUnit.DAYS.between(now, dropDate);
        //hoursUntil is the remainder of the daysUntil calculation
        long hoursUntil = ChronoUnit.HOURS.between(now.plusDays(daysUntil), dropDate);
        //minutesUntil is the remainder of the hoursUntil calculation
        long minutesUntil = ChronoUnit.MINUTES.between(now.plusDays(daysUntil).plusHours(hoursUntil), dropDate);
        manager.sendText("Lightfall drops in " + daysUntil + " days, " + hoursUntil + " hours, and " + minutesUntil + " minutes.");
    }

    public boolean needsDispatcher() {
        return false;
    }

    public List<String> getAliases() {
        return List.of("lightfall");
    }

    public ApplicationCommandRequest getCommandApplicationInformation() {
        return null;
    }

    public String help() {
        return "Shows the amount of time until Lightfall drops.";
    }
}
