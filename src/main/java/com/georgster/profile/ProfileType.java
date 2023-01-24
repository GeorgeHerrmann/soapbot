package com.georgster.profile;

/**
 * An enum that represents the different types of server profiles that can be created.
 * The enumeration for each profile directly corresponds to the name of the profile.
 * For example, a server's events.json profile is represented by the enum EVENTS.
 */
public enum ProfileType {
    /**
     * Represents a server's events.json profile.
     */
    EVENTS,
    /**
     * Represents a server's permissions.json profile.
     */
    PERMISSIONS
}
