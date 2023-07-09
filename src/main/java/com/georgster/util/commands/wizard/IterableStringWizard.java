package com.georgster.util.commands.wizard;

import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

/**
 * A wizard that allows a user to iterate over a List of Strings with buttons.
 */
public class IterableStringWizard extends InputWizard {
    
    private List<String> args;

    /**
     * Creates a new IterableStringWizard from the provided arguments.
     * 
     * @param event The event that prompted this wizard's creation.
     * @param title The title of the wizard.
     * @param args The Strings to iterate over.
     */
    public IterableStringWizard(CommandExecutionEvent event, String title, List<String> args) {
        super(event, InputListenerFactory.createButtonMessageListener(event, title).builder().requireMatch(true, true).withXReaction(false).build());

        this.args = args;
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        nextWindow("iterateState", 0);
    }

    /**
     * The current window of this wizard.
     * 
     * @param index The index of the List to get the current String.
     */
    protected void iterateState(Integer index) {
        boolean hasPrevious = index != 0;
        boolean hasNext = index != args.size() - 1;

        String prompt = args.get(index);
        String[] choices = new String[0];

        if (hasPrevious) {
            if (hasNext) {
                choices = new String[]{"back", "next"};
            } else {
                choices = new String[]{"back"};
            }
        } else if (hasNext) {
            choices = new String[]{"next"};
        }

        withResponse((response -> {
            if (response.equals("back")) {
                nextWindow("iterateState", index - 1);
            } else if (response.equals("next")) {
                nextWindow("iterateState", index + 1);
            }
        }), false, prompt, choices);
    }

}
