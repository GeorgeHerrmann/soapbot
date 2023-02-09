package com.georgster.util.commands;

import java.util.List;

import com.georgster.util.GuildManager;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

/**
 * A wizard that allows for SOAP Bot to ask a user for a series of inputs.
 */
public class CommandWizard {
    private static final int TIMEOUT_TIME = 300; // will wait 30s for a response

    private Message message; // The most recent message sent by someone in the wizard
    private Member caller; // The user who called the wizard
    private final GuildManager manager; // The manager managing the guild from the original command
    private boolean ended; // Whether or not the wizard has ended
    private String end; // The string that ends the wizard
    private String title; // The title of messages sent by this wizard

    /**
     * Creates a new wizard by the given caller for the given guild manager that
     * ends when the given string is sent. The guild manager must have an
     * {@link GuildManager#getEventDispatcher() event dispatcher} attached
     * to it by the {@code SoapClient}. Therefore, any calling command must
     * declare it needs the EventDispatcher with {@link com.georgster.Command#needsDispatcher()}.
     * 
     * @param manager The manager managing the guild from the original command
     * @param end    The string that ends the wizard
     * @param caller The user who called the wizard
     */
    public CommandWizard(GuildManager manager, String end, String title, Member caller) {
        this.caller = caller;
        this.ended = false;
        this.end = end;
        this.manager = manager;
        message = null;
        this.title = title;
    }

    /**
     * Returns whether or not the wizard has ended.
     * 
     * @return Whether or not the wizard has ended
     */
    public boolean ended() {
        return ended;
    }

    /**
     * Prompts the caller for a message and returns their response. If the user
     * ended the wizard during this "step" or if no response was given within
     * the wizard's timeout duration, null is returned and the wizard is ended.
     * 
     * @param step The prompt to send to the user
     * @return The user's response or null if the wizard was ended
     */
    public String step(String step, String... options) {
        SelectMenu.Option[] menuOptions = new SelectMenu.Option[options.length];

        for (int i = 0; i < options.length; i++) {
            menuOptions[i] = SelectMenu.Option.of(options[i], options[i]);
        }

        SelectMenu menu = SelectMenu.of(title, menuOptions);

        step += "\nYour options are: " + String.join(", ", options);
        message = manager.sendText(step, title, ActionRow.of(menu));
        message.addReaction(ReactionEmoji.unicode("❌")).block();

        EventDispatcher dispatcher = manager.getEventDispatcher();
        StringBuilder output = new StringBuilder();

        // Create a listener that listens for the user's next message
        Disposable canceller = dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getAuthor().get().getId().asString().equals(caller.getId().asString()))
            .filter(event -> event.getMessage().getChannelId().equals(message.getChannelId()))
            .filter(event -> List.of(options).contains(event.getMessage().getContent().toLowerCase()))
            .subscribe(event -> {
                if (event.getMessage().getContent().equals(end)) {
                    ended = true;
                } else {
                    message = event.getMessage();
                    output.append(message.getContent());
                }
            });

        // Create a listener that listens for the user to end the wizard by reacting
        Disposable canceller2 = dispatcher.on(ReactionAddEvent.class)
            .filter(event -> event.getMember().get().getId().asString().equals(caller.getId().asString()))
            .filter(event -> event.getEmoji().equals(ReactionEmoji.unicode("❌")))
            .subscribe(event -> ended = true);

        Disposable canceller3 = dispatcher.on(SelectMenuInteractionEvent.class)
            .filter(event -> event.getInteraction().getMember().get().getId().asString().equals(caller.getId().asString()))
            .filter(event -> event.getMessage().get().getId().asString().equals(message.getId().asString()))
            .subscribe(event -> {output.append(event.getValues().get(0)); System.out.println("Selected " + event.getValues().get(0));});

        int timeout = 0;
        while (getMessageContents(message).equals(step)) { // Wait for the user to send a message
            try {
                if (ended || timeout > TIMEOUT_TIME) {
                    canceller.dispose();
                    canceller2.dispose();
                    canceller3.dispose();
                    ended = true;
                    return null;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            timeout ++;
        }
        canceller.dispose(); // Dispose of the listeners
        canceller2.dispose();
        canceller3.dispose();
        return output.toString();
    }

    /**
     * Returns the message contents of the given message. If the message has
     * embeds, the description of the first embed is returned. Otherwise, the
     * message's content is returned.
     * 
     * @param message The message to get the contents of
     * @return The message contents
     */
    private String getMessageContents(Message message) {
        StringBuilder contents = new StringBuilder();
        message.getEmbeds().get(0).getDescription().ifPresent(contents::append);
        contents.append(message.getContent());
        return contents.toString();
    }


}
