package com.example.autoclicker.config;

/**
 * Top-level application configuration profile.
 */
public class AppConfig {

    private String profileName = "Default";
    private AutomationConfig automation = new AutomationConfig();

    public AppConfig() {
    }

    public AppConfig(String profileName, AutomationConfig automation) {
        this.profileName = profileName;
        this.automation = automation != null ? automation : new AutomationConfig();
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public AutomationConfig getAutomation() {
        return automation;
    }

    public void setAutomation(AutomationConfig automation) {
        this.automation = automation;
    }
}
