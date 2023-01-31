package com.georgster.test;

import java.util.List;

import com.georgster.Command;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandWizard;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandRequest;

/**
 * Used to test on going features. This command will be considered active if the
 * {@code ACTIVE} field is set to {@code true}.
 */
public class TestCommand implements Command {
    private boolean needsNewRegistration = true; // Set to true only if the command registry should send a new command definition to Discord
    private static final boolean ACTIVE = true;

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        /*CommandWizard wizard = new CommandWizard(manager, "end", event.getMessage().getAuthorAsMember().block());
        manager.sendText("Beginning a wizard for three inputs");
        if (!wizard.ended()) {
            Message message = wizard.step("Input 1:");
            if (message == null) {
                manager.sendText("This listener has ended");
            } else {
                manager.sendText("Listener found: " + message.getContent());
            }
        }
        if (!wizard.ended()) {
            Message message = wizard.step("Input 2:");
            if (message == null) {
                manager.sendText("This listener has ended");
            } else {
                manager.sendText("Listener found: " + message.getContent());
            }
        }
        if (!wizard.ended()) {
            Message message = wizard.step("Input 3:");
            if (message == null) {
                manager.sendText("This listener has ended");
            } else {
                manager.sendText("Listener found: " + message.getContent());
            }
        }
        manager.sendText("This listener has ended");*/
    }

    /**
     * {@inheritDoc}
     */
    public boolean needsDispatcher() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getAliases() {
        if (ACTIVE) {
            return List.of("test");
        } else { //if inactive, the registry will not be able to find this command using the aliases
            return List.of();
        }
    }

    public ApplicationCommandRequest getCommandApplicationInformation() {
        if (!needsNewRegistration) return null;

        return ApplicationCommandRequest.builder()
                .name(getAliases().get(0))
                .description("Description test")
                .build();
    }

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "The test command is used to test on going features.";
    }

}
