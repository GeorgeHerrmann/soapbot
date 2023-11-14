package com.georgster.wizard;

import java.util.HashMap;
import java.util.Map;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.wizard.input.InputListenerFactory;

public final class SinglyAttachmentWizard extends InputWizard {
    
    private final Map<String, Runnable> options;
    private final String text;

    public SinglyAttachmentWizard(CommandExecutionEvent event, String title, String text) {
        super(event, InputListenerFactory.createButtonMessageListener(event, title).builder().requireMatch(true, true).withXReaction(false).build());
        this.text = text;
        options = new HashMap<>();
    }

    public void addOption(String option, Runnable action) {
        options.put(option, action);
    }

    public void begin() {
        nextWindow("show");
        end();
    }

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
