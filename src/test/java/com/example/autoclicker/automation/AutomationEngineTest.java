package com.example.autoclicker.automation;

import com.example.autoclicker.config.AutomationConfig;
import com.example.autoclicker.image.ImageDetector;
import com.example.autoclicker.image.ScreenshotService;
import com.example.autoclicker.screen.CaptureRegion;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.screen.ScreenManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AutomationEngineTest {

    private AutomationEngine engine;
    private File tempTemplateFile;

    @BeforeEach
    void setUp() throws IOException {
        ScreenshotService mockScreenshotService = new ScreenshotService() {
            @Override
            public BufferedImage capture(ScreenInfo screen, CaptureRegion region) {
                BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = img.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 100, 100);
                g.setColor(Color.BLUE);
                g.fillRect(10, 10, 30, 30);
                g.dispose();
                return img;
            }
        };

        // Create matching template
        tempTemplateFile = File.createTempFile("test-tpl-", ".png");
        BufferedImage tpl = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D tg = tpl.createGraphics();
        tg.setColor(Color.BLUE);
        tg.fillRect(0, 0, 30, 30);
        tg.dispose();
        ImageIO.write(tpl, "png", tempTemplateFile);

        ImageDetector detector = new ImageDetector();
        ScreenManager screenManager = new ScreenManager();

        ActionExecutor dummyExecutor = new ActionExecutor() {
            @Override
            public void execute(AutomationAction action, ScreenInfo targetScreen) {
            }
        };

        engine = new AutomationEngine(mockScreenshotService, detector, dummyExecutor, screenManager);
    }

    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.shutdown();
        }
        if (tempTemplateFile != null && tempTemplateFile.exists()) {
            tempTemplateFile.delete();
        }
    }

    @Test
    void testEngineWithCheckImageActionBranchesToBranchA() throws InterruptedException {
        AutomationConfig config = new AutomationConfig();
        config.setTargetScreen(0);

        AutomationAction actionInit = new AutomationAction(AutomationActionType.WAIT, 0, 0, 20);

        // Action CHECK_IMAGE in initial sequence
        AutomationAction actionCheck = new AutomationAction();
        actionCheck.setType(AutomationActionType.CHECK_IMAGE);
        actionCheck.setTemplatePath(tempTemplateFile.getAbsolutePath());
        actionCheck.setThreshold(0.85);
        actionCheck.setX(0);
        actionCheck.setY(0);
        actionCheck.setWidth(100);
        actionCheck.setHeight(100);

        AutomationAction actionA = new AutomationAction(AutomationActionType.WAIT, 0, 0, 50);

        config.setInitialActions(List.of(actionInit, actionCheck));
        config.setBranchA(List.of(actionA));
        config.setBranchB(List.of(actionA));

        List<AutomationState> recordedStates = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch branchReachedLatch = new CountDownLatch(1);

        engine.addStateListener(state -> {
            recordedStates.add(state);
            if (state == AutomationState.EXECUTING_BRANCH_A || state == AutomationState.EXECUTING_BRANCH_B) {
                branchReachedLatch.countDown();
            }
        });

        assertEquals(AutomationState.IDLE, engine.getState());
        engine.start(config);

        boolean reached = branchReachedLatch.await(5, TimeUnit.SECONDS);
        assertTrue(reached, "Engine should reach execution state within 5 seconds");
        assertTrue(engine.isRunning());

        engine.stop();
        Thread.sleep(300);

        assertEquals(AutomationState.STOPPED, engine.getState());
        assertFalse(engine.isRunning());

        assertTrue(recordedStates.contains(AutomationState.EXECUTING_INITIAL_ACTIONS));
        assertTrue(recordedStates.contains(AutomationState.CHECKING_IMAGE));
        assertTrue(recordedStates.contains(AutomationState.EXECUTING_BRANCH_A));
        assertTrue(recordedStates.contains(AutomationState.STOPPED));
    }

    @Test
    void testEngineWithoutCheckImageRunsOnlyInitialActions() throws InterruptedException {
        AutomationConfig config = new AutomationConfig();
        config.setTargetScreen(0);

        AutomationAction action1 = new AutomationAction(AutomationActionType.WAIT, 0, 0, 20);
        AutomationAction action2 = new AutomationAction(AutomationActionType.WAIT, 0, 0, 30);
        config.setInitialActions(List.of(action1, action2));

        // Branches A and B are populated but should NOT be called
        AutomationAction branchAction = new AutomationAction(AutomationActionType.WAIT, 0, 0, 50);
        config.setBranchA(List.of(branchAction));
        config.setBranchB(List.of(branchAction));

        List<AutomationState> recordedStates = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch initReachedLatch = new CountDownLatch(1);

        engine.addStateListener(state -> {
            recordedStates.add(state);
            if (state == AutomationState.EXECUTING_INITIAL_ACTIONS) {
                initReachedLatch.countDown();
            }
        });

        engine.start(config);
        boolean reached = initReachedLatch.await(5, TimeUnit.SECONDS);
        assertTrue(reached);

        Thread.sleep(300);
        engine.stop();
        Thread.sleep(200);

        assertTrue(recordedStates.contains(AutomationState.EXECUTING_INITIAL_ACTIONS));
        // Verify that without CHECK_IMAGE, it NEVER branched to Branch A or B
        assertFalse(recordedStates.contains(AutomationState.CHECKING_IMAGE));
        assertFalse(recordedStates.contains(AutomationState.EXECUTING_BRANCH_A));
        assertFalse(recordedStates.contains(AutomationState.EXECUTING_BRANCH_B));
    }

    @Test
    void testEngineStopsAfterRepeatCountReached() throws InterruptedException {
        AutomationConfig config = new AutomationConfig();
        config.setTargetScreen(0);
        config.setRepeatCount(2); // Run exactly 2 loops

        AutomationAction action = new AutomationAction(AutomationActionType.WAIT, 0, 0, 10);
        config.setInitialActions(List.of(action));

        List<Integer> loopsRecorded = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch finishLatch = new CountDownLatch(1);

        engine.addLoopListener((currentLoop, maxLoops) -> {
            loopsRecorded.add(currentLoop);
        });

        engine.addStateListener(state -> {
            if (state == AutomationState.STOPPED) {
                finishLatch.countDown();
            }
        });

        engine.start(config);
        boolean finished = finishLatch.await(5, TimeUnit.SECONDS);

        assertTrue(finished, "Engine should automatically complete and transition to STOPPED");
        assertFalse(engine.isRunning());
        assertEquals(List.of(1, 2), loopsRecorded);
    }
}
