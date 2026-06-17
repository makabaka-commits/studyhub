package com.studyhub.dto;

public class VersionResponse {

    private String appName;

    private String version;

    private String javaVersion;

    private String activeProfile;

    public String getAppName() {
        return appName;
    }

    public String getVersion() {
        return version;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }
}
