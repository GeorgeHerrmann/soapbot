package com.georgster.util.commands.wizard;

import java.util.Optional;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;
import com.georgster.util.thread.ThreadPoolFactory;

import discord4j.core.object.entity.Message;

/**
 * A high level, unique wizard which can swap between a {@link Message} and an {@link InputWizard}
 * with a reaction of ":repeat:".
 */
public class SwappingWizard extends InputWizard {

    private final CommandExecutionEvent event;
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
        this.event = event;
        this.message = message;
        this.wizard = wizard;
        this.messageTitle = message.getEmbeds().get(0).getTitle();
        this.messageContent = message.getEmbeds().get(0).getDescription().orElse(null);
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
            messageTitle.ifPresentOrElse(title -> handler.editMessageContent(message, messageContent, title), () -> handler.editMessageContent(message, messageContent));
        }

        withResponse((response -> {
            wizard.getInputListener().cancel();
            wizard.shutdown();
            nextWindow("swap", !onWizard);
        }), false, "", "U+1F501");
    }
    
}