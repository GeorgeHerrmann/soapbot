package com.georgster.database;

/**
 * An enum that represents the different types of guild profiles that can be created in the database.
 */
public enum ProfileType {
    /**
     * Represents a guild's user profiles profile.
     */
    PROFILES,
    /**
     * Represents a guild's events profile.
     */
    EVENTS,
    /**
     * Represents a guild's permissions profile.
     */
    PERMISSIONS,
    /**
     * Represents a guild's collectables profile.
     */
    COLLECTABLES,
    /**
     * Represents a guild's mention groups profile.
     */
    MENTIONGROUPS,
    /**
     * Represents the global user settings profile.
     */
    SETTINGS,
    /**
     * Represents a guild's elo ratings profile.
     */
    ELO_RATINGS,
    /**
     * Represents a guild's elo battles profile.
     */
    ELO_BATTLES,
    /**
     * Represents a guild's battle wizard states profile.
     */
    BATTLE_WIZARD_STATES
}
