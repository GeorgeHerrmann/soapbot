package com.georgster.util.commands;

import com.georgster.util.GuildManager;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.Disposable;

/**
 * A wizard that allows for SOAP Bot to ask a user for a series of inputs.
 */
public class CommandWizard {
    private static final int TIMEOUT_TIME = 200; // will wait 20s for a response

    private Message message; // The most recent message sent by the user in the wizard
    private Member caller; // The user who called the wizard
    private final GuildManager manager; // The manager managing the guild from the original command
    private boolean ended; // Whether or not the wizard has ended
    private String end; // The string that ends the wizard

    /**
     * Creates a new wizard by the given caller for the given guild manager that
     * ends when the given string is sent. The guild manager must have an
     * {@link GuildManager#getEventDispatcher() event dispatcher} attached
     * to it by the {@code SoapClient}. Therefore, any calling command must
     * declare it has a wizard feature with {@link com.georgster.Command#hasWizard()}.
     * 
     * @param manager The manager managing the guild from the original command
     * @param end    The string that ends the wizard
     * @param caller The user who called the wizard
     */
    public CommandWizard(GuildManager manager, String end, Member caller) {
        this.caller = caller;
        this.ended = false;
        this.end = end;
        this.manager = manager;
        message = null;
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
    public Message step(String step) {
        message = manager.sendText(step);
        message.addReaction(ReactionEmoji.unicode("❌")).block();

        EventDispatcher dispatcher = manager.getEventDispatcher();

        // Create a listener that listens for the user's next message
        Disposable canceller = dispatcher.on(MessageCreateEvent.class)
            .filter(event -> event.getMessage().getAuthor().get().getId().asString().equals(caller.getId().asString()))
            .subscribe(event -> {
                if (event.getMessage().getContent().equals(end)) {
                    ended = true;
                } else {
                    message = event.getMessage();
                }
            });

        // Create a listener that listens for the user to end the wizard by reacting
        Disposable canceller2 = dispatcher.on(ReactionAddEvent.class)
            .filter(event -> event.getMember().get().getId().asString().equals(caller.getId().asString()))
            .filter(event -> event.getEmoji().equals(ReactionEmoji.unicode("❌")))
            .subscribe(event -> ended = true);

        int timeout = 0;
        while (message.getContent().equals(step)) { // Wait for the user to send a message
            try {
                if (ended || timeout > TIMEOUT_TIME) {
                    canceller.dispose();
                    canceller2.dispose();
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
        canceller.dispose(); // Dispose of the listener
        canceller2.dispose();
        return message;
    }


}
