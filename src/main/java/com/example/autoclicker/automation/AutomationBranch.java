package com.example.autoclicker.automation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a sequence of actions to be executed under a specific condition (e.g. MATCH or NOT_MATCH).
 */
public class AutomationBranch {

    private String name;
    private final List<AutomationAction> actions = new ArrayList<>();

    public AutomationBranch() {
        this.name = "Branch";
    }

    public AutomationBranch(String name) {
        this.name = name;
    }

    public AutomationBranch(String name, List<AutomationAction> initialActions) {
        this.name = name;
        if (initialActions != null) {
            this.actions.addAll(initialActions);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AutomationAction> getActions() {
        return actions;
    }

    public void setActions(List<AutomationAction> newActions) {
        actions.clear();
        if (newActions != null) {
            actions.addAll(newActions);
        }
    }

    public void addAction(AutomationAction action) {
        if (action != null) {
            actions.add(action);
        }
    }

    public void removeAction(int index) {
        if (index >= 0 && index < actions.size()) {
            actions.remove(index);
        }
    }

    public boolean moveUp(int index) {
        if (index > 0 && index < actions.size()) {
            Collections.swap(actions, index, index - 1);
            return true;
        }
        return false;
    }

    public boolean moveDown(int index) {
        if (index >= 0 && index < actions.size() - 1) {
            Collections.swap(actions, index, index + 1);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public int size() {
        return actions.size();
    }
}
