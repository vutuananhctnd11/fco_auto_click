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
    EXECUTING_BRANCH_C,
    EXECUTING_BRANCH_D,
    EXECUTING_BRANCH_E,
    EXECUTING_BRANCH_F,
    STOPPING,
    STOPPED,
    ERROR
}
