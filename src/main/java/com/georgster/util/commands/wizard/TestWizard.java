package com.georgster.util.commands.wizard;

import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.wizard.input.InputListenerFactory;

/**
 * A wizard used to test new {@link InputWizard} or {@link com.georgster.util.commands.wizard.input.UserInputListener} features.
 */
public class TestWizard extends InputWizard {

    private static final String TITLE = "Test Wizard";

    /**
     * Creates a new TestWizard.
     * 
     * @param event The event from {@code TestCommand}.
     */
    public TestWizard(CommandExecutionEvent event) {
        super(event, InputListenerFactory.createButtonMessageListener(event, TITLE));
    }

    /**
     * {@inheritDoc}
     */
    public void begin() {
        handler.sendText("Welcome to the test wizard. At any time you can react :x: to exit the wizard", TITLE);
        nextWindow("windowOne");
        end();
    }

    protected void windowOne() {
        String prompt = "Test screen";

        withResponse((response -> {
            sendMessage("Found response: " + response, TITLE);
            nextWindow("windowTwo");
        }), false, prompt, "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven");
    }

    protected void windowTwo() {
        String prompt = "Test Screen 2";

        withResponse((response -> {
            sendMessage("Found response: " + response, TITLE);
        }), true, prompt);
    }
    
}
