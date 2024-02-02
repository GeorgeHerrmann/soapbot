package com.georgster.settings;

import java.util.Set;

import discord4j.rest.util.Color;

public class InfoColorOption extends ColorOption {
    public InfoColorOption() {
        super("lightgray");
    }

    public InfoColorOption(String option) {
        super(option);
    }

    public String name() {
        return "info color";
    }

    public Set<String> availableOptions() {
        return AVAILABLE_OPTIONS.keySet();
    }

    public Color getColor() {
        return AVAILABLE_OPTIONS.get(option);
    }
}
