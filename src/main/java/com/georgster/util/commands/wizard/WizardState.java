package com.georgster.util.commands.wizard;

public class WizardState {
    private boolean hasEnded;
    private String message;
    private String[] options;

    public WizardState(String message, String... options) {
        this.hasEnded = false;
        this.message = message;
        this.options = options;
    }

    public boolean hasEnded() {
        return hasEnded;
    }

    public String getMessage() {
        return message;
    }

    public String getOption(int index) {
        return options[index];
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getOptions() {
        return options;
    }

    public void end() {
        hasEnded = true;
    }
}
