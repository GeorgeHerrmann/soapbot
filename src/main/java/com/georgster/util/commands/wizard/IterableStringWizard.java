package com.georgster.util.commands.wizard;

import java.util.List;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

public class IterableStringWizard extends InputWizard {
    
    private List<String> args;

    public IterableStringWizard(CommandExecutionEvent event, String title, List<String> args) {
        super(event, InputListenerFactory.createButtonMessageListener(event, title));

        this.args = args;
    }

    public void begin() {
        nextWindow("iterateState", 0);
    }

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
