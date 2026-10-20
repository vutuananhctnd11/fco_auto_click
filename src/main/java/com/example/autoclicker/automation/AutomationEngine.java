package com.example.autoclicker.automation;

import com.example.autoclicker.config.AutomationConfig;
import com.example.autoclicker.image.ImageDetector;
import com.example.autoclicker.image.MatchResult;
import com.example.autoclicker.image.ScreenshotService;
import com.example.autoclicker.screen.CaptureRegion;
import com.example.autoclicker.screen.ScreenInfo;
import com.example.autoclicker.screen.ScreenManager;
import com.example.autoclicker.util.LoggerUtil;
import com.example.autoclicker.window.WindowInfo;
import com.example.autoclicker.window.WindowManager;
import com.sun.jna.platform.win32.WinDef;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * State machine engine executing conditional automation on a background worker thread.
 * When a CHECK_IMAGE action is present in initial actions, it evaluates and branches to Branch A / Branch B.
 * Otherwise, it executes the initial actions sequence continuously.
 */
public class AutomationEngine {

    private final ScreenshotService screenshotService;
    private final ImageDetector imageDetector;
    private final ActionExecutor actionExecutor;
    private final ScreenManager screenManager;
    private final WindowManager windowManager;

    private final ExecutorService executorService;
    private final CopyOnWriteArrayList<Consumer<AutomationState>> stateListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<BiConsumer<MatchResult, AutomationState>> matchListeners = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<BiConsumer<Integer, Integer>> loopListeners = new CopyOnWriteArrayList<>();

    private volatile AutomationState state = AutomationState.IDLE;
    private volatile boolean running = false;
    private Future<?> activeTask;

    public AutomationEngine(ScreenshotService screenshotService,
                            ImageDetector imageDetector,
                            ActionExecutor actionExecutor,
                            ScreenManager screenManager) {
        this(screenshotService, imageDetector, actionExecutor, screenManager, new WindowManager());
    }

    public AutomationEngine(ScreenshotService screenshotService,
                            ImageDetector imageDetector,
                            ActionExecutor actionExecutor,
                            ScreenManager screenManager,
                            WindowManager windowManager) {
        this.screenshotService = screenshotService;
        this.imageDetector = imageDetector;
        this.actionExecutor = actionExecutor;
        this.screenManager = screenManager;
        this.windowManager = windowManager != null ? windowManager : new WindowManager();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "AutomationEngine-Worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void addStateListener(Consumer<AutomationState> listener) {
        stateListeners.add(listener);
    }

    public void removeStateListener(Consumer<AutomationState> listener) {
        stateListeners.remove(listener);
    }

    public void addMatchListener(BiConsumer<MatchResult, AutomationState> listener) {
        matchListeners.add(listener);
    }

    public void removeMatchListener(BiConsumer<MatchResult, AutomationState> listener) {
        matchListeners.remove(listener);
    }

    public void addLoopListener(BiConsumer<Integer, Integer> listener) {
        loopListeners.add(listener);
    }

    public void removeLoopListener(BiConsumer<Integer, Integer> listener) {
        loopListeners.remove(listener);
    }

    public AutomationState getState() {
        return state;
    }

    public boolean isRunning() {
        return running;
    }

    private synchronized void setState(AutomationState newState) {
        this.state = newState;
        for (Consumer<AutomationState> listener : stateListeners) {
            try {
                listener.accept(newState);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyMatch(MatchResult result, AutomationState currentState) {
        for (BiConsumer<MatchResult, AutomationState> listener : matchListeners) {
            try {
                listener.accept(result, currentState);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyLoop(int currentLoop, int maxLoops) {
        for (BiConsumer<Integer, Integer> listener : loopListeners) {
            try {
                listener.accept(currentLoop, maxLoops);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Starts the automation loop with the provided configuration.
     */
    public synchronized void start(AutomationConfig config) {
        if (running) {
            LoggerUtil.warn("Automation is already running.");
            return;
        }

        String validationError = config.validate();
        if (validationError != null) {
            LoggerUtil.error("Configuration validation failed: " + validationError);
            throw new IllegalArgumentException(validationError);
        }

        ScreenInfo targetScreen = screenManager.getScreen(config.getTargetScreen());
        if (targetScreen == null) {
            targetScreen = screenManager.getPrimaryScreen();
        }

        // Cache templates for CHECK_IMAGE actions to avoid disk IO overhead
        Map<String, BufferedImage> templateCache = new HashMap<>();
        for (AutomationAction a : config.getInitialActions()) {
            if (a.getType() == AutomationActionType.CHECK_IMAGE && !a.getTemplatePath().isBlank()) {
                try {
                    templateCache.put(a.getTemplatePath(), imageDetector.loadTemplate(a.getTemplatePath()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot load template image: " + a.getTemplatePath(), e);
                }
            }
        }

        running = true;
        LoggerUtil.info("Automation started. Has CHECK_IMAGE: " + config.hasCheckImageAction());

        final ScreenInfo finalScreen = targetScreen;
        activeTask = executorService.submit(() -> runLoop(config, finalScreen, templateCache));
    }

    /**
     * Stops the running automation engine immediately.
     */
    public synchronized void stop() {
        if (!running && state == AutomationState.IDLE) {
            return;
        }

        LoggerUtil.info("Stopping automation engine...");
        setState(AutomationState.STOPPING);
        running = false;

        if (activeTask != null && !activeTask.isDone()) {
            activeTask.cancel(true);
        }
    }

    private void runLoop(AutomationConfig config, ScreenInfo targetScreen, Map<String, BufferedImage> templateCache) {
        int maxLoops = config.getRepeatCount();
        int currentLoop = 0;
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                currentLoop++;
                notifyLoop(currentLoop, maxLoops);
                LoggerUtil.info(String.format("Starting loop cycle #%d%s",
                        currentLoop, (maxLoops > 0 ? (" / " + maxLoops) : " (Infinite)")));

                // 1. Resolve Target Window (if configured)
                Rectangle windowBounds = null;
                WinDef.HWND targetHwnd = null;
                boolean windowRequired = config.getTargetWindowTitle() != null && !config.getTargetWindowTitle().isBlank();

                if (windowRequired) {
                    WindowInfo windowInfo = windowManager.findWindowByTitle(config.getTargetWindowTitle());
                    if (windowInfo == null || !windowManager.isWindowValid(windowInfo.getHwnd())) {
                        LoggerUtil.warn("Target Window: NOT FOUND (" + config.getTargetWindowTitle() + ") - Waiting...");
                        Thread.sleep(1000);
                        continue;
                    }
                    targetHwnd = windowInfo.getHwnd();
                    windowBounds = windowManager.getWindowBounds(windowInfo.getHwnd());
                    if (windowBounds == null) {
                        LoggerUtil.warn("Could not retrieve bounds for target window.");
                        Thread.sleep(1000);
                        continue;
                    }

                    // Activate and bring window to front so game receives inputs
                    if (!config.isBackgroundClick()) {
                        windowManager.activateWindow(targetHwnd);
                    }
                }

                boolean backgroundMode = config.isBackgroundClick();

                // 2. Execute Initial Actions
                setState(AutomationState.EXECUTING_INITIAL_ACTIONS);
                for (AutomationAction action : config.getInitialActions()) {
                    if (!running || Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    // Dynamically refresh window bounds
                    if (windowRequired && windowBounds != null) {
                        WindowInfo w = windowManager.findWindowByTitle(config.getTargetWindowTitle());
                        if (w != null) {
                            targetHwnd = w.getHwnd();
                            windowBounds = windowManager.getWindowBounds(w.getHwnd());
                        }
                    }

                    if (action.getType() == AutomationActionType.CHECK_IMAGE) {
                        // CHECK_IMAGE triggers conditional branching
                        setState(AutomationState.CHECKING_IMAGE);
                        LoggerUtil.info("Action CHECK_IMAGE triggered");

                        BufferedImage tpl = templateCache.get(action.getTemplatePath());
                        if (tpl == null && !action.getTemplatePath().isBlank()) {
                            tpl = imageDetector.loadTemplate(action.getTemplatePath());
                            templateCache.put(action.getTemplatePath(), tpl);
                        }

                        CaptureRegion region = action.getCaptureRegion();
                        BufferedImage screenshot = screenshotService.capture(targetScreen, region, windowBounds);
                        MatchResult result = (tpl != null) ?
                                imageDetector.match(screenshot, tpl, action.getThreshold()) :
                                MatchResult.notMatched(0.0);

                        LoggerUtil.info(String.format("Match confidence = %.2f (Threshold: %.2f)",
                                result.getConfidence(), action.getThreshold()));
                        notifyMatch(result, state);

                        if (!running || Thread.currentThread().isInterrupted()) {
                            break;
                        }

                        if (result.isMatched()) {
                            setState(AutomationState.EXECUTING_BRANCH_A);
                            LoggerUtil.info("Branch A selected (MATCH)");
                            for (AutomationAction branchAction : config.getBranchA()) {
                                if (!running || Thread.currentThread().isInterrupted()) break;
                                actionExecutor.execute(branchAction, targetScreen, windowBounds, targetHwnd, backgroundMode);
                            }
                            LoggerUtil.info("Branch A completed");
                        } else {
                            setState(AutomationState.EXECUTING_BRANCH_B);
                            LoggerUtil.info("Branch B selected (NOT MATCH)");
                            for (AutomationAction branchAction : config.getBranchB()) {
                                if (!running || Thread.currentThread().isInterrupted()) break;
                                actionExecutor.execute(branchAction, targetScreen, windowBounds, targetHwnd, backgroundMode);
                            }
                            LoggerUtil.info("Branch B completed");
                        }

                        // Return state to Initial
                        setState(AutomationState.EXECUTING_INITIAL_ACTIONS);
                    } else {
                        // Regular action (CLICK, DOUBLE_CLICK, RIGHT_CLICK, KEY_PRESS, WAIT)
                        actionExecutor.execute(action, targetScreen, windowBounds, targetHwnd, backgroundMode);
                    }
                }

                // Check if loop limit reached
                if (maxLoops > 0 && currentLoop >= maxLoops) {
                    LoggerUtil.info(String.format("Completed requested loop count (%d/%d). Automation finished.",
                            currentLoop, maxLoops));
                    break;
                }

                // Small loop interval before next cycle
                if (running && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LoggerUtil.info("Automation loop interrupted.");
        } catch (Throwable e) {
            LoggerUtil.error("Unexpected error in automation engine: " + e.getMessage(), e);
            setState(AutomationState.ERROR);
            running = false;
            return;
        } finally {
            running = false;
            setState(AutomationState.STOPPED);
            LoggerUtil.info("Automation stopped");
        }
    }

    public void shutdown() {
        stop();
        executorService.shutdownNow();
    }
}
