package com.georgster.util;

import java.util.Random;

/**
 * Utility class for generating random numbers from a static instance of {@link Random}.
 * This class provides methods to generate random integers, longs, and doubles within specified ranges.
 * <p>
 * For general utility methods, use {@link com.georgster.util.SoapUtility SoapUtility}.
 * @see #getRandom()
 */
public final class SoapNumbers {
    private static final Random RAND = new Random();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private SoapNumbers() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Generates a random integer between the specified minimum and maximum values (inclusive).
     * 
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random integer between min and max (inclusive)
     */
    public static int getRandomInt(int min, int max) {
        return min + RAND.nextInt(max - min + 1);
    }

    /**
     * Generates a random long between the specified minimum and maximum values (inclusive).
     * 
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random long between min and max (inclusive)
     */
    public static long getRandomLong(long min, long max) {
        return min + (long) (RAND.nextDouble() * (max - min + 1));
    }

    /**
     * Generates a random double between the specified minimum and maximum values (inclusive).
     * 
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @return a random double between min and max (inclusive)
     */
    public static double getRandomDouble(double min, double max) {
        return min + (RAND.nextDouble() * (max - min));
    }

    /**
     * Returns the static instance of {@link Random} used for generating random numbers.
     * 
     * @return the static instance of {@link Random}
     */
    public Random getRandom() {
        return RAND;
    }
    
}
