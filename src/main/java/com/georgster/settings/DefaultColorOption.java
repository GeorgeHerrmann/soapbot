package com.georgster.settings;

import java.util.Set;

import discord4j.rest.util.Color;

public class DefaultColorOption extends ColorOption {

    public DefaultColorOption() {
        super("blue");
    }

    public DefaultColorOption(String option) {
        super(option);
    }

    public String name() {
        return "default color";
    }

    public Set<String> availableOptions() {
        return AVAILABLE_OPTIONS.keySet();
    }

    public Color getColor() {
        return AVAILABLE_OPTIONS.get(option);
    }

}
