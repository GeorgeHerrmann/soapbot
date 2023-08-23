package com.georgster.util.format;

import discord4j.core.object.Embed;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;

/**
 * Factory class for creating pre-formatted {@link Embed Embeds}.
 */
public class EmbedFactory {
    private EmbedFactory() throws IllegalStateException {
        throw new IllegalStateException();
    }

    /**
     * Returns a Builder for an {@link EmbedCreateSpec} preconfigured with default formatting.
     * <p>
     * This Embed will be {@code BLUE} in color, contain a title and a description.
     * 
     * @param title The title for the Embed.
     * @param description The description (text area) for the Embed.
     * @return The Builder for the embed.
     */
    public static EmbedCreateSpec.Builder getDefaultEmbedBuilder(String title, String description) {
        return EmbedCreateSpec.builder().color(Color.BLUE).description(description).title(title);
    }

    /**
     * Returns a Builder for an {@link EmbedCreateSpec} preconfigured with error formatting.
     * <p>
     * This Embed will be {@code RED} in color, contain a title and a description.
     * 
     * @param title The title for the Embed.
     * @param description The description (text area) for the Embed.
     * @return The Builder for the embed.
     */
    public static EmbedCreateSpec.Builder getErrorEmbedBuilder(String title, String description) {
        return EmbedCreateSpec.builder().color(Color.RED).description(description).title(title);
    }
}
