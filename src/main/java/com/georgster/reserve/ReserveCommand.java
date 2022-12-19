package com.georgster.reserve;

import java.util.Arrays;
import java.util.List;

import com.georgster.Command;
import com.georgster.profile.ProfileHandler;
import com.georgster.util.SoapGeneralHandler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;

/*
 * !reserve "name" "playercount" "time" (optional)
 * !reserve "name"
 */
public class ReserveCommand implements Command {
    public void execute(MessageCreateEvent event) {
        List<String> message = Arrays.asList(event.getMessage().getContent().split(" "));
        if (message.size() < 2) {
            event.getMessage().getChannel().block().createMessage(help()).block();
            return;
        }
        try {
            TextChannel channel = (TextChannel) event.getMessage().getChannel().block();
            ReserveEvent reserve = assignCorrectCommand(message, event.getGuild().block().getId().asString(), channel.getName());

            if (ProfileHandler.checkEventDuplicates(event.getGuild().block().getId().asString(), reserve)) {
                if (!reserve.isFull()) {
                    event.getMessage().getAuthor().ifPresent(user -> {
                        if (reserve.alreadyReserved(user.getTag())) {
                            event.getMessage().getChannel().block().createMessage("You have already reserved for this event").block();
                        } else {
                            reserve.addReservedUser(user.getTag());
                            ProfileHandler.removeEvent(event.getGuild().block().getId().asString(), reserve);
                            reserve.addReserved();
                            reserve.addReservedUser(user.getTag());
                            ProfileHandler.addEvent(event.getGuild().block().getId().asString(), reserve);
                            event.getMessage().getChannel().block().createMessage("Number of people reserved to event " + reserve.getIdentifier() + ": " + reserve.getReserved() + "/" + reserve.getNumPeople()).block();
                        }
                    });
                } else {
                    event.getMessage().getChannel().block().createMessage("Event " + reserve.getIdentifier() + " is full").block();
                }
            } else {
                event.getMessage().getAuthor().ifPresent(user -> reserve.addReservedUser(user.getTag()));
                ProfileHandler.addEvent(event.getGuild().block().getId().asString(), reserve);
                SoapGeneralHandler.runDaemon(() -> ReserveEventHandler.scheduleEvent(reserve, event.getMessage().getChannel().block(), event.getGuild().block().getId().asString()));
                String messageString = "";
                if (reserve.isTimeless()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled with " + reserve.getNumPeople() + " spots available!";
                } else if (reserve.isUnlimited()) {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + reserve.getTime()+ "!";
                } else {
                    messageString = "Event " + reserve.getIdentifier() + " scheduled for " + reserve.getTime() + " with " + reserve.getNumPeople() + " spots available!";
                }
                event.getMessage().getChannel().block().createMessage(messageString).block();
            }
        } catch (IllegalArgumentException e) {
            event.getMessage().getChannel().block().createMessage("This event doesn't exist. Type !help reserve to see how to make a new event.").block();
        }

    }

    private ReserveEvent assignCorrectCommand(List<String> message, String id, String channelName) throws IllegalArgumentException {

        if (message.size() == 2) {
            if (ProfileHandler.eventExists(id, message.get(1))) {
                return ProfileHandler.pullEvent(id, message.get(1));
            }
        } else if (message.size() == 3) {
            try {
                return new ReserveEvent(message.get(1), Integer.parseInt(message.get(2)), channelName);
            } catch (NumberFormatException e) {
                return new ReserveEvent(message.get(1), message.get(2), channelName);
            }
        } else if (message.size() == 4) {
            return new ReserveEvent(message.get(1), Integer.parseInt(message.get(2)), message.get(3), channelName);
        }
        throw new IllegalArgumentException("Incorrect Reserve Event Format");
    }

    public String help() {
        return "placeholder";
    }
}
