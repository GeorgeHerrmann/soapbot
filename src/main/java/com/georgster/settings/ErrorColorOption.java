package com.georgster.settings;

import java.util.Set;

import discord4j.rest.util.Color;

public final class ErrorColorOption extends ColorOption {
    public ErrorColorOption() {
        super("red");
    }

    public ErrorColorOption(String option) {
        super(option);
    }

    public String name() {
        return "error color";
    }

    public Set<String> availableOptions() {
        return AVAILABLE_OPTIONS.keySet();
    }

    public Color getColor() {
        return AVAILABLE_OPTIONS.get(option);
    }
}
