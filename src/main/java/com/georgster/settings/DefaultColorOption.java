package com.georgster.settings;

import java.util.Set;

/**
 * A {@link ColorOption} that represents the default message color option.
 */
public final class DefaultColorOption extends ColorOption {

    /**
     * Creates a new {@link DefaultColorOption} with the default option "blue".
     */
    public DefaultColorOption() {
        super("blue");
    }

    /**
     * Creates a new {@link DefaultColorOption} with the given option.
     * @param option
     */
    public DefaultColorOption(String option) {
        super(option);
    }

    /**
     * {@inheritDoc}
     */
    public String name() {
        return "default color";
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> availableOptions() {
        return AVAILABLE_OPTIONS.keySet();
    }

}
