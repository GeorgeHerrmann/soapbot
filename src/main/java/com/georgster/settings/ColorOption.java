package com.georgster.settings;

import java.util.Map;

import discord4j.rest.util.Color;

public abstract class ColorOption extends UserSettings.SettingsOption {
    
    protected static final Map<String, Color> AVAILABLE_OPTIONS;

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
            Map.entry("lightgray", Color.LIGHT_GRAY),
            Map.entry("darkgray", Color.DARK_GRAY),
            Map.entry("cyan", Color.CYAN),
            Map.entry("magenta", Color.MAGENTA)
        );
    }

    public ColorOption(String defaultOption) {
        super(defaultOption);
    }

}
