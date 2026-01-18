package com.georgster.permissions;

/**
 * An enum of all the actions requiring permissions that can be performed by a user.
 * Any action not declared by a Command will be considered a {@link #DEFAULT} action.
 */
public enum PermissibleAction {
    BLACKJACKGAME,
    ECHOCOMMAND,
    GPTCOMMAND,
    MANAGEEVENTS,
    MANAGEALLEVENTS,
    MANAGEPERMISSIONS,
    ADMIN,
    HELPCOMMAND,
    CREATEEVENT,
    RESERVEEVENT,
    PONGCOMMAND,
    PLAYMUSIC,
    SKIPMUSIC,
    SHOWQUEUE,
    MESSAGECOMMAND,
    PLINKOGAME,
    PERMISSIONSCOMMAND,
    MENTIONEVENT,
    POLLCOMMAND,
    TESTCOMMAND,
    TRADECOMMAND,
    CARDCOMMAND,
    BANKCOMMAND,
    MENTIONGROUPCOMMAND,
    COINFACTORYCOMMAND,
    SUMMARYCOMMAND,
    DEFAULT
}
