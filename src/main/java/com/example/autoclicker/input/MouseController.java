package com.example.autoclicker.input;

import com.example.autoclicker.util.LoggerUtil;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Low-level controller for mouse automation using java.awt.Robot.
 */
public class MouseController {

    private final Robot robot;

    public MouseController() {
        try {
            this.robot = new Robot();
            this.robot.setAutoWaitForIdle(false);
        } catch (AWTException e) {
            throw new IllegalStateException("Failed to initialize Robot in MouseController", e);
        }
    }

    public void move(int x, int y) {
        robot.mouseMove(x, y);
    }

    public void click(int x, int y) {
        move(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void doubleClick(int x, int y, int intervalMs) {
        move(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        int sleepTime = intervalMs > 0 ? intervalMs : 50;
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void rightClick(int x, int y) {
        move(x, y);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }
}
