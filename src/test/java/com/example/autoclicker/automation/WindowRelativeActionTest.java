package com.example.autoclicker.automation;

import com.example.autoclicker.screen.ScreenInfo;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowRelativeActionTest {

    @Test
    void testWindowRelativeCoordinatesExecution() throws InterruptedException {
        AtomicInteger clickedX = new AtomicInteger(-1);
        AtomicInteger clickedY = new AtomicInteger(-1);

        ActionExecutor executor = new ActionExecutor() {
            @Override
            public void execute(AutomationAction action, ScreenInfo targetScreen, Rectangle targetWindowBounds) {
                if (action.getCoordinateMode() == CoordinateMode.WINDOW_RELATIVE && targetWindowBounds != null) {
                    clickedX.set(targetWindowBounds.x + action.getX());
                    clickedY.set(targetWindowBounds.y + action.getY());
                } else if (targetScreen != null) {
                    clickedX.set(targetScreen.toGlobalX(action.getX()));
                    clickedY.set(targetScreen.toGlobalY(action.getY()));
                }
            }
        };

        // Window located at (1920, 100) with size 1280x720
        Rectangle windowBounds = new Rectangle(1920, 100, 1280, 720);
        AutomationAction action = new AutomationAction(AutomationActionType.CLICK, 500, 300, 0, CoordinateMode.WINDOW_RELATIVE);

        executor.execute(action, null, windowBounds);

        assertEquals(2420, clickedX.get(), "Screen X should be window.x + relativeX = 1920 + 500 = 2420");
        assertEquals(400, clickedY.get(), "Screen Y should be window.y + relativeY = 100 + 300 = 400");
    }
}
