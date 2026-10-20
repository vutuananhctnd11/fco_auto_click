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
import java.util.List;
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
        cacheTemplates(config.getInitialActions(), templateCache);
        cacheTemplates(config.getBranchA(), templateCache);
        cacheTemplates(config.getBranchB(), templateCache);

        running = true;
        LoggerUtil.info("Automation started. Has CHECK_IMAGE: " + config.hasCheckImageAction());

        final ScreenInfo finalScreen = targetScreen;
        activeTask = executorService.submit(() -> runLoop(config, finalScreen, templateCache));
    }

    private void cacheTemplates(List<AutomationAction> actions, Map<String, BufferedImage> templateCache) {
        if (actions == null) return;
        for (AutomationAction a : actions) {
            if (a.getType() == AutomationActionType.CHECK_IMAGE && a.getTemplatePath() != null && !a.getTemplatePath().isBlank()) {
                if (!templateCache.containsKey(a.getTemplatePath())) {
                    try {
                        templateCache.put(a.getTemplatePath(), imageDetector.loadTemplate(a.getTemplatePath()));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Cannot load template image: " + a.getTemplatePath(), e);
                    }
                }
            }
        }
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
                        // Level-1 CHECK_IMAGE -> Branches to A (MATCH) or B (NOT MATCH)
                        setState(AutomationState.CHECKING_IMAGE);
                        LoggerUtil.info("Initial CHECK_IMAGE triggered");

                        MatchResult result = performImageCheck(action, targetScreen, windowBounds, templateCache);
                        LoggerUtil.info(String.format("Initial Check confidence = %.2f (Threshold: %.2f)",
                                result.getConfidence(), action.getThreshold()));
                        notifyMatch(result, state);

                        if (!running || Thread.currentThread().isInterrupted()) {
                            break;
                        }

                        if (result.isMatched()) {
                            // Execute Branch A (with potential 2nd-level branching to C or D)
                            executeBranchWithSubBranch(
                                    config.getBranchA(),
                                    config.getBranchC(),
                                    config.getBranchD(),
                                    AutomationState.EXECUTING_BRANCH_A,
                                    AutomationState.EXECUTING_BRANCH_C,
                                    AutomationState.EXECUTING_BRANCH_D,
                                    targetScreen, windowBounds, targetHwnd, backgroundMode,
                                    templateCache, "Branch A", "Branch C", "Branch D");
                        } else {
                            // Execute Branch B (with potential 2nd-level branching to E or F)
                            executeBranchWithSubBranch(
                                    config.getBranchB(),
                                    config.getBranchE(),
                                    config.getBranchF(),
                                    AutomationState.EXECUTING_BRANCH_B,
                                    AutomationState.EXECUTING_BRANCH_E,
                                    AutomationState.EXECUTING_BRANCH_F,
                                    targetScreen, windowBounds, targetHwnd, backgroundMode,
                                    templateCache, "Branch B", "Branch E", "Branch F");
                        }

                        // Return state to Initial
                        setState(AutomationState.EXECUTING_INITIAL_ACTIONS);
                    } else {
                        // Regular action (CLICK, MOVE, SCROLL, KEY_PRESS, WAIT...)
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

    private MatchResult performImageCheck(AutomationAction action, ScreenInfo targetScreen,
                                          Rectangle windowBounds, Map<String, BufferedImage> templateCache) {
        BufferedImage tpl = templateCache.get(action.getTemplatePath());
        if (tpl == null && !action.getTemplatePath().isBlank()) {
            try {
                tpl = imageDetector.loadTemplate(action.getTemplatePath());
                templateCache.put(action.getTemplatePath(), tpl);
            } catch (Exception e) {
                LoggerUtil.warn("Could not load template image on check: " + e.getMessage());
            }
        }

        CaptureRegion region = action.getCaptureRegion();
        BufferedImage screenshot = screenshotService.capture(targetScreen, region, windowBounds);
        return (tpl != null) ?
                imageDetector.match(screenshot, tpl, action.getThreshold()) :
                MatchResult.notMatched(0.0);
    }

    private void executeBranchWithSubBranch(
            List<AutomationAction> branchActions,
            List<AutomationAction> matchSubBranch,
            List<AutomationAction> notMatchSubBranch,
            AutomationState currentBranchState,
            AutomationState matchSubState,
            AutomationState notMatchSubState,
            ScreenInfo targetScreen,
            Rectangle windowBounds,
            WinDef.HWND targetHwnd,
            boolean backgroundMode,
            Map<String, BufferedImage> templateCache,
            String branchName,
            String subMatchName,
            String subNotMatchName) throws InterruptedException {

        setState(currentBranchState);
        LoggerUtil.info(branchName + " started");

        for (AutomationAction action : branchActions) {
            if (!running || Thread.currentThread().isInterrupted()) break;

            if (action.getType() == AutomationActionType.CHECK_IMAGE) {
                // 2nd-Level CHECK_IMAGE inside branch
                setState(AutomationState.CHECKING_IMAGE);
                LoggerUtil.info(branchName + " 2nd-level CHECK_IMAGE triggered");

                MatchResult subResult = performImageCheck(action, targetScreen, windowBounds, templateCache);
                LoggerUtil.info(String.format("%s 2nd-Check confidence = %.2f (Threshold: %.2f)",
                        branchName, subResult.getConfidence(), action.getThreshold()));
                notifyMatch(subResult, state);

                if (!running || Thread.currentThread().isInterrupted()) break;

                if (subResult.isMatched()) {
                    setState(matchSubState);
                    LoggerUtil.info(subMatchName + " selected (MATCH)");
                    for (AutomationAction subAction : matchSubBranch) {
                        if (!running || Thread.currentThread().isInterrupted()) break;
                        actionExecutor.execute(subAction, targetScreen, windowBounds, targetHwnd, backgroundMode);
                    }
                    LoggerUtil.info(subMatchName + " completed");
                } else {
                    setState(notMatchSubState);
                    LoggerUtil.info(subNotMatchName + " selected (NOT MATCH)");
                    for (AutomationAction subAction : notMatchSubBranch) {
                        if (!running || Thread.currentThread().isInterrupted()) break;
                        actionExecutor.execute(subAction, targetScreen, windowBounds, targetHwnd, backgroundMode);
                    }
                    LoggerUtil.info(subNotMatchName + " completed");
                }

                // Restore branch state
                setState(currentBranchState);
            } else {
                actionExecutor.execute(action, targetScreen, windowBounds, targetHwnd, backgroundMode);
            }
        }

        LoggerUtil.info(branchName + " completed");
    }

    public void shutdown() {
        stop();
        executorService.shutdownNow();
    }
}
