package com.georgster.reserve;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.profile.ProfileHandler;

import discord4j.core.event.domain.message.MessageCreateEvent;

/*
 * !reserve "name" "playercount" "time" (optional)
 * !reserve "name"
 */
public class ReserveCommand implements Command {
    public void execute(MessageCreateEvent event) {
        List<String> message = Arrays.asList(event.getMessage().getContent().split(" "));
        ReserveEvent reserve;

        if (message.size() < 2) {
            event.getMessage().getChannel().block().createMessage(help()).block();
            return;
        }
        try {
            reserve = new ReserveEvent(message.get(1), Integer.parseInt(message.get(2)), message.get(3));
        } catch (IndexOutOfBoundsException e) {
            reserve = ProfileHandler.pullEvent(event.getGuild().block().getId().asString(), message.get(1));
        }
        if (ProfileHandler.checkEventDuplicates(event.getGuild().block().getId().asString(), reserve)) {
            if (!reserve.isFull()) {
                ProfileHandler.removeEvent(event.getGuild().block().getId().asString(), reserve);
                reserve.addReserved();
                ProfileHandler.addEvent(event.getGuild().block().getId().asString(), reserve);
                event.getMessage().getChannel().block().createMessage("Number of people reserved to event " + reserve.getIdentifier() + ": " + reserve.getReserved() + "/" + reserve.getNumPeople()).block();
            } else {
                event.getMessage().getChannel().block().createMessage("Event " + reserve.getIdentifier() + " is full").block();
            }
        } else {
            ProfileHandler.addEvent(event.getGuild().block().getId().asString(), reserve);
            ReserveEventHandler.scheduleEvent(reserve, event.getMessage().getChannel().block());
            String messageString = "Event " + reserve.getIdentifier() + " scheduled for " + reserve.getTime() + " with " + reserve.getNumPeople() + " spots available!";
            event.getMessage().getChannel().block().createMessage(messageString).block();
        }

    }

    public String help() {
        return "placeholder";
    }
}
