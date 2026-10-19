package com.example.autoclicker.automation;

/**
 * State machine states for the AutomationEngine.
 */
public enum AutomationState {
    IDLE,
    EXECUTING_INITIAL_ACTIONS,
    CHECKING_IMAGE,
    EXECUTING_BRANCH_A,
    EXECUTING_BRANCH_B,
    STOPPING,
    STOPPED,
    ERROR
}
