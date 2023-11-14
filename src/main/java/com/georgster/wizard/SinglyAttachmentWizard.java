package com.georgster.wizard;

import java.util.HashMap;
import java.util.Map;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.input.InputListenerFactory;

/**
 * An {@link InputWizard} that has a single window and can attach options via {@code Buttons} to the message with custom logic.
 * <p>
 * Options and the logic that will be run when they are selected can be added via {@link #addOption(String, Runnable)}.
 * <p>
 * All Runnable logic will be run on the same thread as this wizard.
 */
public final class SinglyAttachmentWizard extends InputWizard {
    
    private final Map<String, Runnable> options; // The options that will be attached to the message and the logic that will be run when they are selected
    private final String text; // The body text that will be displayed in the message

    /**
     * Creates a new {@code SinglyAttachmentWizard} with the given title and body text.
     * 
     * @param event The {@link CommandExecutionEvent} that triggered the wizard
     * @param title The title of the message
     * @param text The body text of the message
     */
    public SinglyAttachmentWizard(CommandExecutionEvent event, String title, String text) {
        super(event, InputListenerFactory.createButtonMessageListener(event, title).builder().requireMatch(true, true).withXReaction(false).build());
        this.text = text;
        options = new HashMap<>();
    }

    /**
     * Adds an option to the message with the given text and logic that will be run when it is selected.
     * 
     * @param option The text of the option
     * @param action The logic that will be run when the option is selected
     */
    public void addOption(String option, Runnable action) {
        options.put(option, action);
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("show");
        shutdown();
    }

    /**
     * Shows the message with the options attached.
     */
    protected void show() {
        withResponse(response -> {
            if (options.containsKey(response)) {
                options.get(response).run();
            } else {
                sendMessage("Invalid option. Please try again.", "Invalid option");
            }
        }, true, text, options.keySet().toArray(new String[0]));
    }
}
