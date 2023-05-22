package com.georgster.util.commands.wizard;

/**
 * Enum for the different types of responses that can be returned from a wizard.
 */
public enum WizardResponse {
    /**
     * Indicates that the wizard has ended.
     */
    ENDED,
    /**
     * Indicates that the wizard should go back to the previous step.
     */
    BACK,
    /**
     * Indicates that the wizard should go to the next step.
     */
    NEXT
}
