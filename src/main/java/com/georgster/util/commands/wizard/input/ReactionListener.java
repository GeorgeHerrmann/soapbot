package com.georgster.util.commands.wizard.input;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.WizardState;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.reaction.ReactionEmoji;

/**
 * Sends a message with reaction emojis and records when one is presented.
 * The name of the emoji is recorded as the response.
 */
public class ReactionListener extends InputListener {
    private ReactionEmoji[] emojis;

    /**
     * Creates a new {@code ReactionListener} with the given parameters.
     * 
     * @param event The event that prompted this listener's creation.
     * @param title The title to place onto messages.
     */
    public ReactionListener(CommandExecutionEvent event, String title, ReactionEmoji... emojis) {
        super (event, title, "end");
        this.emojis = emojis;
        hasXReaction(false);
        mustMatch(false, false);
    }

    /**
     * {@inheritDoc}
     */
    public WizardState prompt(WizardState inputState) {
        for (ReactionEmoji emoji : emojis) {
            message.addReaction(emoji).block();
        }

        createListener(dispatcher ->  dispatcher.on(ReactionAddEvent.class)
            .filter(event -> event.getMember().get().getId().asString().equals(user.getId().asString()))
            .filter(event -> {
                for (ReactionEmoji emoji : emojis) {
                    if (event.getEmoji().equals(emoji)) {
                        return true;
                    }
                }
                return false;
            }).subscribe(event -> event.getEmoji().asEmojiData().name().ifPresent(this::setResponse))
        );

        createListener(dispatcher ->  dispatcher.on(ReactionRemoveEvent.class)
            .filter(event -> event.getUser().block().getId().asString().equals(user.getId().asString()))
            .filter(event -> {
                for (ReactionEmoji emoji : emojis) {
                    if (event.getEmoji().equals(emoji)) {
                        return true;
                    }
                }
                return false;
            }).subscribe(event -> event.getEmoji().asEmojiData().name().ifPresent(this::setResponse))
        );

        return waitForResponse(inputState);
    }
}
