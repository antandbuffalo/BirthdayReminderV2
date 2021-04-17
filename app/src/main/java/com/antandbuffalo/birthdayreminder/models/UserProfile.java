package com.antandbuffalo.birthdayreminder.models;

import java.util.Date;

public class UserProfile {
    private String uid;
    private String displayName;
    private String email;
    private String providerId;
    private Date updatedAt;
    private String deviceName;
    private Date lastOpenedAt;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Date getLastOpenedAt() {
        return lastOpenedAt;
    }

    public void setLastOpenedAt(Date lastOpenedAt) {
        this.lastOpenedAt = lastOpenedAt;
    }
}
