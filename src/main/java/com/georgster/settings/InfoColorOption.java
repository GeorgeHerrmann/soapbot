package com.georgster.settings;

import java.util.Set;

/**
 * A {@link ColorOption} that represents the info message color option.
 */
public class InfoColorOption extends ColorOption {

    /**
     * Creates a new {@link InfoColorOption} with the default option "lightgray".
     */
    public InfoColorOption() {
        super("lightgray");
    }

    /**
     * Creates a new {@link InfoColorOption} with the given option.
     * 
     * @param option the option.
     */
    public InfoColorOption(String option) {
        super(option);
    }

    /**
     * {@inheritDoc}
     */
    public String name() {
        return "info color";
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> availableOptions() {
        return AVAILABLE_OPTIONS.keySet();
    }
}
