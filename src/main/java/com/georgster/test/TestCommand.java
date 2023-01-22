package com.georgster.test;

import java.util.List;

import com.georgster.Command;
import com.georgster.util.GuildManager;
import com.georgster.util.commands.CommandWizard;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

/**
 * Used to test on going features. This command will be considered active if the
 * {@code ACTIVE} field is set to {@code true}.
 */
public class TestCommand implements Command {
    private static final boolean ACTIVE = false;

    /**
     * {@inheritDoc}
     */
    public void execute(MessageCreateEvent event, GuildManager manager) {
        CommandWizard wizard = new CommandWizard(manager, "end", event.getMessage().getAuthorAsMember().block());
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
        manager.sendText("This listener has ended");
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasWizard() {
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

    /**
     * {@inheritDoc}
     */
    public String help() {
        return "The test command is used to test on going features.";
    }

}
