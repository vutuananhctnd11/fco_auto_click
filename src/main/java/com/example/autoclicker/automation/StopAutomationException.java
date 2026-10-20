package com.example.autoclicker.automation;

/**
 * Exception thrown when a STOP action is encountered during script execution.
 * Triggers graceful termination of the automation engine loop across all branch depths.
 */
public class StopAutomationException extends RuntimeException {

    public StopAutomationException() {
        super("Automation stopped by STOP action");
    }

    public StopAutomationException(String message) {
        super(message);
    }
}
