package com.georgster.input.wizard.input;

import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.input.wizard.WizardState;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.reaction.ReactionEmoji;

/**
 * Sends a message with reaction emojis and records when one is presented.
 * The codepoint of the emoji should be provided as the options and, similarly,
 * the codepoint of the emoji is recorded as the response.
 */
public class ReactionListener extends InputListener {

    private boolean lockUser; // True if this listener will only accept reactions from its primary User, false if anyone can provide inputs.
    private CommandExecutionEvent executionEvent;

    /**
     * Creates a new {@code ReactionListener} with the given parameters.
     * This listener has a custom option matching configuration, therefore
     * modifying this listener's must match rules is not advised unless
     * a specific implementation is required.
     * <p>
     * Although Most listeners only allow its primary user to provide responses,
     * this listener can be customized to allow anyone to provide reaction responses.
     * 
     * @param event The event that prompted this listener's creation.
     * @param title The title to place onto messages.
     * @param lockUser True if only the primary user can provide reaction responses, false if anyone can.
     */
    public ReactionListener(CommandExecutionEvent event, String title, boolean lockUser) {
        super (event, title, "end");
        hasXReaction(true);
        mustMatch(false, false); // This listener has custom matching logic
        this.lockUser = lockUser;
        this.executionEvent = event;
    }

    /**
     * {@inheritDoc}
     */
    public WizardState prompt(WizardState inputState) {
        sendPromptMessage(inputState.getMessage());

        String[] options = inputState.getOptions();

        List<ReactionEmoji> emojis = new ArrayList<>();

        for (String option : options) {
            ReactionEmoji emoji = ReactionEmoji.codepoints(option);
            emojis.add(emoji);

            message.addReaction(emoji).block();
        }

        createListener(dispatcher ->  dispatcher.on(ReactionAddEvent.class)
            .filter(event -> !event.getUser().block().isBot())
            .filter(event -> (lockUser && event.getMember().get().getId().asString().equals(user.getId().asString())) || (!lockUser))
            .filter(event -> emojis.stream().anyMatch(event.getEmoji()::equals))
            .subscribe(event -> { // The event returns the emoji itself, not the codepoint, so we must get it from our options array
                for (int i = 0; i < options.length; i++) { // Matches the emoji from the event to the input options
                    if (emojis.get(i).equals(event.getEmoji())) {
                        setResponse(options[i]); // Returns the input codepoint from the options array
                    }
                }
                event.getMember().ifPresent(this::setInteractingMember);
            })
        );

        createListener(dispatcher ->  dispatcher.on(ReactionRemoveEvent.class)
            .filter(event -> !event.getUser().block().isBot())
            .filter(event -> (lockUser && event.getUser().block().getId().asString().equals(user.getId().asString())) || (!lockUser))
            .filter(event -> emojis.stream().anyMatch(event.getEmoji()::equals))
            .subscribe(event -> {
                for (int i = 0; i < options.length; i++) {
                    if (emojis.get(i).equals(event.getEmoji())) {
                        setResponse(options[i]);
                    }
                }
                setInteractingMember(event.getUser().block().asMember(executionEvent.getGuildInteractionHandler().getGuild().getId()).block());
            })
        );

        return waitForResponse(inputState);
    }
}
