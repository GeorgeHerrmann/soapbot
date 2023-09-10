package com.georgster.control.util.identify;

import java.util.Random;

/**
 * Factory class for creating new unique IDs. All IDs are 9 characters in length.
 */
public class UniqueIdFactory {
    private static final Random RANDOM_ID_GENERATOR = new Random();

    private UniqueIdFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new, unique, 9 character ID.
     * 
     * @return The generated ID.
     */
    public static String createId() {
        return RANDOM_ID_GENERATOR.nextLong(100000000, 999999999) + "";
    }
}
