package com.georgster.wizard.input;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.WizardState;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.reaction.ReactionEmoji;

/**
 * Sends a message with reaction emojis and records when one is presented.
 * The codepoint of the emoji should be provided as the options and, similarly,
 * the codepoint of the emoji is recorded as the response.
 * <p>
 * Whether or not the reaction was "added" or "removed" will be present
 * in the output {@link WizardState WizardState's} notes.
 */
public class ReactionListener extends InputListener {

    /**
     * Creates a new {@code ReactionListener} with the given parameters.
     * This listener has a custom option matching configuration, therefore
     * modifying this listener's must match rules is not advised unless
     * a specific implementation is required.
     * 
     * @param event The event that prompted this listener's creation.
     * @param title The title to place onto messages.
     */
    public ReactionListener(CommandExecutionEvent event, String title) {
        super (event, title, "end");
        hasXReaction(true);
        mustMatch(false, false); // This listener has custom matching logic
    }

    /**
     * {@inheritDoc}
     */
    public WizardState prompt(WizardState inputState) {
        inputState.getEmbed().ifPresentOrElse(this::sendPromptMessage,
        () -> sendPromptMessage(inputState.getMessage()));

        String[] options = inputState.getOptions();

        List<ReactionEmoji> emojis = new ArrayList<>();

        for (String option : options) {
            ReactionEmoji emoji = ReactionEmoji.codepoints(option);
            emojis.add(emoji);

            message.getMessage().addReaction(emoji).block();
        }

        createListener(dispatcher ->  dispatcher.on(ReactionAddEvent.class)
            .filter(event -> !event.getUser().block().isBot())
            .filter(event -> emojis.stream().anyMatch(event.getEmoji()::equals))
            .subscribe(event -> { // The event returns the emoji itself, not the codepoint, so we must get it from our options array
                for (int i = 0; i < options.length; i++) { // Matches the emoji from the event to the input options
                    if (emojis.get(i).equals(event.getEmoji())) {
                        setResponse(options[i], event.getUser().block(), "added"); // Returns the input codepoint from the options array
                    }
                }
            })
        );

        createListener(dispatcher ->  dispatcher.on(ReactionRemoveEvent.class)
            .filter(event -> !event.getUser().block().isBot())
            .filter(event -> emojis.stream().anyMatch(event.getEmoji()::equals))
            .subscribe(event -> {
                for (int i = 0; i < options.length; i++) {
                    if (emojis.get(i).equals(event.getEmoji())) {
                        setResponse(options[i], event.getUser().block(), "removed");
                    }
                }
            })
        );

        return waitForResponse(inputState);
    }
}
