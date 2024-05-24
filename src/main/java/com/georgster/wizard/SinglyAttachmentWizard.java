package com.georgster.wizard;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

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
    private final Optional<String> text; // The body text that will be displayed in the message
    private final Optional<Supplier<String>> textSupplier; // The body text that will be displayed in the message
    private final Optional<Supplier<String>> titleSupplier; // The title of the message

    /**
     * Creates a new {@code SinglyAttachmentWizard} with the given title and body text.
     * 
     * @param event The {@link CommandExecutionEvent} that triggered the wizard
     * @param title The title of the message
     * @param text The body text of the message
     */
    public SinglyAttachmentWizard(CommandExecutionEvent event, String title, String text) {
        super(event, InputListenerFactory.createButtonMessageListener(event, title).builder().requireMatch(true, true).withXReaction(false).build());
        this.text = Optional.of(text);
        this.textSupplier = Optional.empty();
        this.titleSupplier = Optional.empty();
        options = new HashMap<>();
    }

    /**
     * Creates a new {@code SinglyAttachmentWizard} with the given title {@link Supplier} and body text {@link Supplier}.
     * <p>
     * The title and body text will be retrieved from the {@link Supplier} when the wizard is started. This
     * allows for behavior such as swapping to another Wizard, then returning to this one with different text.
     * 
     * @param event The {@link CommandExecutionEvent} that triggered the wizard
     * @param title The {@link Supplier} that will provide the title of the message
     * @param text The {@link Supplier} that will provide the body text of the message
     */
    public SinglyAttachmentWizard(CommandExecutionEvent event, Supplier<String> title, Supplier<String> text) {
        super(event, InputListenerFactory.createButtonMessageListener(event, title.get()).builder().requireMatch(true, true).withXReaction(false).build());
        this.text = Optional.empty();
        this.textSupplier = Optional.of(text);
        this.titleSupplier = Optional.of(title);
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
        String text = textSupplier.isPresent() ? textSupplier.get().get() : this.text.get();
        if (titleSupplier.isPresent()) {
            getActiveListener().setTitle(titleSupplier.get().get());
        }
        withResponse(response -> {
            if (options.containsKey(response)) {
                options.get(response).run();
            } else {
                sendMessage("Invalid option. Please try again.", "Invalid option");
            }
        }, true, text, options.keySet().toArray(new String[0]));
    }
}
