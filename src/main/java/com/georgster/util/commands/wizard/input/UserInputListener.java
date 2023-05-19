package com.georgster.util.commands.wizard.input;

import com.georgster.util.commands.wizard.WizardState;

public interface UserInputListener {
    public WizardState prompt(WizardState inputState);

    public void editCurrentMessageContent(String newContent);
}
