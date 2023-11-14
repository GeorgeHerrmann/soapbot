package com.georgster.wizard;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.thread.ThreadPoolFactory;
import com.georgster.wizard.input.InputListenerFactory;

/**
 * An {@link InputWizard} which can swap between two different {@link InputWizard InputWizards} with a reaction of ":repeat:".
 * <p>
 * The {@link AlternateWizard} is very similar to the {@link SwappingWizard}, however features wizard-wizard swapping instead of wizard-message swapping.
 * <p>
 * The {@link AlternateWizard} will begin from the first window of {@code wizard1}, then switch to the first window of the other wizard on each "swap".
 * 
 * @see {@link SwappingWizard} for wizard-message swapping.
 */
public final class AlternateWizard extends InputWizard {
    
    private final InputWizard wizard1;
    private final InputWizard wizard2;

    /**
     * Creates a new AlternateWizard from the provided event that prompted this wizard's creation and wizards that will be swapped.
     * <p>
     * The {@link AlternateWizard} will begin from the first window of {@code wizard1}.
     * 
     * @param event The event that prompted this wizard's creation.
     * @param wizard1 The first wizard to swap to and from.
     * @param wizard2 The second wizard to swap to and from.
     */
    public AlternateWizard(CommandExecutionEvent event, InputWizard wizard1, InputWizard wizard2) {
        super(event,  InputListenerFactory.createReactionListener(event, "").builder().withPromptMessages(false).withXReaction(false).withTimeoutDuration(120000).build());
        this.wizard1 = wizard1;
        this.wizard2 = wizard2;
    }

    /**
     * Creates a new AlternateWizard from the provided event that prompted this wizard's creation and wizards that will be swapped, with the option to use a unique {@link InteractionHandler}.
     * <p>
     * The {@link AlternateWizard} will begin from the first window of {@code wizard1}.
     * 
     * @param event The event that prompted this wizard's creation.
     * @param wizard1 The first wizard to swap to and from.
     * @param wizard2 The second wizard to swap to and from.
     * @param uniqueHandler True if this wizard should use a unique {@link InteractionHandler}, rather than the one from the provided {@code event}.
     */
    public AlternateWizard(CommandExecutionEvent event, InputWizard wizard1, InputWizard wizard2, boolean uniqueHandler) {
        super(event,  InputListenerFactory.createReactionListener(event, "").builder().withPromptMessages(false).withXReaction(false).withTimeoutDuration(120000).build());
        this.wizard1 = wizard1;
        this.wizard2 = wizard2;
        if (uniqueHandler) {
            this.handler = new GuildInteractionHandler(event.getGuildInteractionHandler().getGuild());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        ThreadPoolFactory.scheduleGeneralTask(event.getGuildInteractionHandler().getGuild().getId().asString(), wizard1::begin);
        int timeout = 0;
        while (!wizard1.hasStarted()) { // Our wizards run on their own threads, so we need to wait for them to start
            try {
                timeout++;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            if (timeout == 50) {
                throw new IllegalStateException("Wizard1 timed out before starting");
            }
        }
        getInputListener().setCurrentMessage(wizard1.getActiveListener().getCurrentMessage());
        nextWindow("swap", true);
    }

    /**
     * Swaps the display to either Wizard1 or Wizard 2 depending on {@code onWizard1}.
     * 
     * @param onWizard1 True if wizard1 should be displayed, false if wizard2 should be displayed.
     */
    protected void swap(Boolean onWizard1) {
        if (Boolean.TRUE.equals(onWizard1) && !wizard1.awaitingResponse()) {
            ThreadPoolFactory.scheduleGeneralTask(event.getGuildInteractionHandler().getGuild().getId().asString(), wizard1::restart);
        } else if (Boolean.FALSE.equals(onWizard1) && !wizard2.awaitingResponse()) {
            ThreadPoolFactory.scheduleGeneralTask(event.getGuildInteractionHandler().getGuild().getId().asString(), wizard2::restart);
        }

        withResponse((response -> {
            if (Boolean.TRUE.equals(onWizard1) && wizard2.getActiveListener().getCurrentMessage() == null) {
                wizard2.getActiveListener().setCurrentMessage(wizard1.getActiveListener().getCurrentMessage());
                wizard2.getInputListener().setCurrentMessage(wizard1.getActiveListener().getCurrentMessage());
            }
            wizard1.cancelCurrentListener();
            wizard1.shutdown();
            wizard2.cancelCurrentListener();
            wizard2.shutdown();
            nextWindow("swap", !onWizard1);
        }), false, "", "U+1F501");
    }

}
