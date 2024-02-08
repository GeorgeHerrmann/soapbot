package com.georgster.settings;

import java.util.Set;

/**
 * A {@link ColorOption} that represents the error message color option.
 */
public final class ErrorColorOption extends ColorOption {

    /**
     * Creates a new {@link ErrorColorOption} with the default option "red".
     */
    public ErrorColorOption() {
        super("red");
    }

    /**
     * Creates a new {@link ErrorColorOption} with the given option.
     * 
     * @param option the option.
     */
    public ErrorColorOption(String option) {
        super(option);
    }

    /**
     * {@inheritDoc}
     */
    public String name() {
        return "error color";
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> availableOptions() {
        return AVAILABLE_OPTIONS.keySet();
    }
}
