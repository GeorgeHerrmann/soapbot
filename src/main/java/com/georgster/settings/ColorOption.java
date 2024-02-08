package com.georgster.settings;

import java.util.Map;

import discord4j.rest.util.Color;

/**
 * A {@link UserSettings.SettingsOption} that represents an option to change a {@link Color}.
 * <p>
 * The {@link ColorOption} provides access to {@link #AVAILABLE_OPTIONS} which contains a map
 * of available color options and their String representations.
 */
public abstract class ColorOption extends UserSettings.SettingsOption {
    
    /** A map of the available {@link Color} options and their String representations. */
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

    /**
     * Creates a new {@link ColorOption} with the given default option.
     * 
     * @param defaultOption the default option.
     */
    public ColorOption(String defaultOption) {
        super(defaultOption);
    }

    /**
     * Returns the {@link Color} of this {@link ColorOption}.
     * 
     * @return the {@link Color} of this {@link ColorOption}.
     */
    public Color getColor() {
        return AVAILABLE_OPTIONS.get(option);
    }

}
