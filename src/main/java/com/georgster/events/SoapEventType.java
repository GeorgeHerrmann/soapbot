package com.georgster.events;

/**
 * An enum that represents the different types of SOAPEvents that can be created.
 */
public enum SoapEventType {
    /**
     * Represents a ReserveEvent.
     */
    RESERVE,
    /**
     * Represents a PollEvent.
     */
    POLL,
    /**
     * Represents an EloBattle.
     */
    ELO_BATTLE
}
