package com.georgster.settings;

import java.util.Map;
import java.util.Set;

import discord4j.rest.util.Color;

public class DefaultColorOption extends UserSettings.SettingsOption {
    
    private static final Map<String, Color> AVAILABLE_OPTIONS;

    static {
        AVAILABLE_OPTIONS = Map.ofEntries(
            Map.entry("red", Color.RED),
            Map.entry("orange", Color.ORANGE),
            Map.entry("yellow", Color.YELLOW),
            Map.entry("green", Color.GREEN),
            Map.entry("blue", Color.BLUE),
            Map.entry("pink", Color.PINK),
            Map.entry("white", Color.WHITE),
            Map.entry("black", Color.BLACK),
            Map.entry("gray", Color.GRAY),
            Map.entry("lightGray", Color.LIGHT_GRAY),
            Map.entry("darkGray", Color.DARK_GRAY),
            Map.entry("cyan", Color.CYAN),
            Map.entry("magenta", Color.MAGENTA)
        );
    }

    public DefaultColorOption() {
        super("blue");
    }

    public DefaultColorOption(String option) {
        super(option);
    }

    public String name() {
        return "Default Color";
    }

    public Set<String> availableOptions() {
        return AVAILABLE_OPTIONS.keySet();
    }

    public Color getColor() {
        return AVAILABLE_OPTIONS.get(option);
    }

}
