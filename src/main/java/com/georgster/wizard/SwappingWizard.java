package com.georgster.wizard;

import java.util.Optional;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.input.InputListenerFactory;

import discord4j.core.object.entity.Message;

/**
 * A high level, unique wizard which can swap between a {@link Message} and an {@link InputWizard} with a reaction of ":repeat:".
 * <p>
 * The {@link SwappingWizard} is very similar to the {@link AlternateWizard}, however features wizard-message swapping instead of wizard-wizard swapping.
 * <p>
 * This wizard will begin from the first window of the provided {@code wizard}, then switch to the first window of the other wizard on each "swap".
 * @see {@link AlternateWizard} for wizard-wizard swapping.
 */
public class SwappingWizard extends InputWizard {

    private Message message;
    private Optional<String> messageTitle;
    private String messageContent;
    private InputWizard wizard;

    /**
     * Creates a new SwappingWizard from the provided arguments.
     * 
     * @param event The event that prompted this wizard's creation.
     * @param message The message to swap to and from.
     * @param wizard The Wizard to swap to and from.
     */
    public SwappingWizard(CommandExecutionEvent event, Message message, InputWizard wizard) { // No prompt messages sent with the reaction listener, so title is irrelevant
        super(event, InputListenerFactory.createReactionListener(event, "").builder().withPromptMessages(false).withXReaction(false).build());
        this.message = message;
        this.wizard = wizard;
        this.messageTitle = message.getEmbeds().get(0).getTitle();
        this.messageContent = message.getEmbeds().get(0).getDescription().orElse(null);
    }

    /**
     * Creates a new SwappingWizard from the provided arguments, with the option to use a unique {@link InteractionHandler}.
     * 
     * @param event The event that prompted this wizard's creation.
     * @param message The message to swap to and from.
     * @param wizard The Wizard to swap to and from.
     * @param uniqueHandler True if this wizard should use a unique {@link InteractionHandler}, rather than the one from the provided {@code event}.
     */
    public SwappingWizard(CommandExecutionEvent event, Message message, InputWizard wizard, boolean uniqueHandler) { // No prompt messages sent with the reaction listener, so title is irrelevant
        super(event, InputListenerFactory.createReactionListener(event, "").builder().withPromptMessages(false).withXReaction(false).build());
        this.message = message;
        this.wizard = wizard;
        this.messageTitle = message.getEmbeds().get(0).getTitle();
        this.messageContent = message.getEmbeds().get(0).getDescription().orElse(null);
        if (uniqueHandler) {
            this.handler = new GuildInteractionHandler(event.getGuildInteractionHandler().getGuild());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        wizard.getInputListener().setCurrentMessage(message);
        getInputListener().setCurrentMessage(message);
        nextWindow("swap", false);
    }

    /**
     * Swaps the display of either the Message or Wizard.
     * 
     * @param onWizard True if the wizard should be displayed, false otherwise.
     */
    protected void swap(Boolean onWizard) {
        if (Boolean.TRUE.equals(onWizard)) {
            ThreadPoolFactory.scheduleGeneralTask(event.getGuildInteractionHandler().getGuild().getId().asString(), wizard::restart);
        } else {
            messageTitle.ifPresentOrElse(title -> handler.editMessage(message, messageContent, title), () -> handler.editMessage(message, messageContent));
        }

        withResponse((response -> {
            wizard.cancelCurrentListener();
            wizard.shutdown();
            nextWindow("swap", !onWizard);
        }), false, "", "U+1F501");
    }
    
}