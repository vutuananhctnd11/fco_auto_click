package com.example.autoclicker.config;

import com.example.autoclicker.automation.AutomationAction;
import com.example.autoclicker.automation.AutomationActionType;
import com.example.autoclicker.automation.CoordinateMode;
import com.example.autoclicker.screen.CaptureRegion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration model for conditional automation execution.
 * Conditional branching is activated when an action of type CHECK_IMAGE is added.
 */
public class AutomationConfig {

    private int targetScreen = 0;
    private String targetWindowTitle = "";
    private CoordinateMode coordinateMode = CoordinateMode.WINDOW_RELATIVE;

    // Optional legacy fields for backward compatibility
    private CaptureRegion captureRegion = new CaptureRegion(100, 100, 300, 200, CoordinateMode.WINDOW_RELATIVE);
    private String templatePath = "";
    private double threshold = 0.90;
    private int repeatCount = 0; // 0 = infinite loop, > 0 = specific number of cycles
    private boolean backgroundClick = false; // true = PostMessage (no mouse takeover), false = Robot

    private List<AutomationAction> initialActions = new ArrayList<>();
    private List<AutomationAction> branchA = new ArrayList<>();
    private List<AutomationAction> branchB = new ArrayList<>();
    private List<AutomationAction> branchC = new ArrayList<>();
    private List<AutomationAction> branchD = new ArrayList<>();
    private List<AutomationAction> branchE = new ArrayList<>();
    private List<AutomationAction> branchF = new ArrayList<>();

    public AutomationConfig() {
    }

    public int getTargetScreen() {
        return targetScreen;
    }

    public void setTargetScreen(int targetScreen) {
        this.targetScreen = targetScreen;
    }

    public String getTargetWindowTitle() {
        return targetWindowTitle;
    }

    public void setTargetWindowTitle(String targetWindowTitle) {
        this.targetWindowTitle = targetWindowTitle != null ? targetWindowTitle : "";
    }

    public CoordinateMode getCoordinateMode() {
        return coordinateMode;
    }

    public void setCoordinateMode(CoordinateMode coordinateMode) {
        this.coordinateMode = coordinateMode != null ? coordinateMode : CoordinateMode.WINDOW_RELATIVE;
    }

    public CaptureRegion getCaptureRegion() {
        return captureRegion;
    }

    public void setCaptureRegion(CaptureRegion captureRegion) {
        this.captureRegion = captureRegion;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public List<AutomationAction> getInitialActions() {
        return initialActions;
    }

    public void setInitialActions(List<AutomationAction> initialActions) {
        this.initialActions = initialActions != null ? initialActions : new ArrayList<>();
    }

    public List<AutomationAction> getBranchA() {
        return branchA;
    }

    public void setBranchA(List<AutomationAction> branchA) {
        this.branchA = branchA != null ? branchA : new ArrayList<>();
    }

    public List<AutomationAction> getBranchB() {
        return branchB;
    }

    public void setBranchB(List<AutomationAction> branchB) {
        this.branchB = branchB != null ? branchB : new ArrayList<>();
    }

    public List<AutomationAction> getBranchC() {
        return branchC;
    }

    public void setBranchC(List<AutomationAction> branchC) {
        this.branchC = branchC != null ? branchC : new ArrayList<>();
    }

    public List<AutomationAction> getBranchD() {
        return branchD;
    }

    public void setBranchD(List<AutomationAction> branchD) {
        this.branchD = branchD != null ? branchD : new ArrayList<>();
    }

    public List<AutomationAction> getBranchE() {
        return branchE;
    }

    public void setBranchE(List<AutomationAction> branchE) {
        this.branchE = branchE != null ? branchE : new ArrayList<>();
    }

    public List<AutomationAction> getBranchF() {
        return branchF;
    }

    public void setBranchF(List<AutomationAction> branchF) {
        this.branchF = branchF != null ? branchF : new ArrayList<>();
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = Math.max(0, repeatCount);
    }

    public boolean isBackgroundClick() {
        return backgroundClick;
    }

    public void setBackgroundClick(boolean backgroundClick) {
        this.backgroundClick = backgroundClick;
    }

    /**
     * Checks if this configuration contains any CHECK_IMAGE action across all branches.
     */
    public boolean hasCheckImageAction() {
        return hasCheckImageInList(initialActions)
                || hasCheckImageInList(branchA)
                || hasCheckImageInList(branchB);
    }

    private boolean hasCheckImageInList(List<AutomationAction> list) {
        if (list == null) return false;
        for (AutomationAction a : list) {
            if (a.getType() == AutomationActionType.CHECK_IMAGE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates configuration before starting automation.
     *
     * @return error message if invalid, or null if valid.
     */
    public String validate() {
        if (repeatCount < 0) {
            return "Repeat count must be greater than or equal to 0 (0 = infinite).";
        }

        if (initialActions.isEmpty() && branchA.isEmpty() && branchB.isEmpty()
                && branchC.isEmpty() && branchD.isEmpty() && branchE.isEmpty() && branchF.isEmpty()) {
            return "Action list cannot be empty. Please add at least one action.";
        }

        // Validate CHECK_IMAGE actions in initial actions and level-1 branches
        String err = validateCheckImages(initialActions, "Initial Actions");
        if (err != null) return err;

        err = validateCheckImages(branchA, "Branch A");
        if (err != null) return err;

        err = validateCheckImages(branchB, "Branch B");
        if (err != null) return err;

        return null;
    }

    private String validateCheckImages(List<AutomationAction> actions, String branchName) {
        if (actions == null) return null;
        for (AutomationAction action : actions) {
            if (action.getType() == AutomationActionType.CHECK_IMAGE) {
                if (action.getTemplatePath() == null || action.getTemplatePath().isBlank()) {
                    return branchName + ": CHECK_IMAGE action is missing template image path.";
                }
                File tplFile = new File(action.getTemplatePath());
                if (!tplFile.exists() || !tplFile.isFile()) {
                    return branchName + ": Template image file does not exist: " + action.getTemplatePath();
                }
                if (action.getWidth() <= 0 || action.getHeight() <= 0) {
                    return branchName + ": CHECK_IMAGE capture region dimensions must be greater than 0.";
                }
            }
        }
        return null;
    }
}
