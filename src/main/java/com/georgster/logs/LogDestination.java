package com.georgster.logs;

/**
 * An enumeration that represents the different destinations
 * for log messages to be sent to.
 */
public enum LogDestination {
    /**
     * Repsents logs that will go to a {@code Guild's} bot-logs channel.
     */
    DISCORD,
    /**
     * Represents logs that will go to the standard output.
     */
    SYSTEM,
    /**
     * Represents logs that will go to a local log file.
     */
    FILE,
    /**
     * Represents logs that will go to the SOAP API.
     */
    API,
    /**
     * Represents the DISCORD, SYSTEM and FILE Destinations.
     */
    NONAPI

}
