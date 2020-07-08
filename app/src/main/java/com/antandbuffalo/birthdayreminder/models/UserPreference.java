package com.antandbuffalo.birthdayreminder.models;

import android.content.Intent;

import java.util.Date;

public class UserPreference {
    public Date serverBackupTime;
    public Date localBackupTime;
    public Integer preNotificationDays;
    public Integer notificationHours;
    public Integer notificationMinutes;
    public Integer numberOfNotifications;
    public String wishTemplate;
    public String versionName;
    public Integer versionCode;
    public String autoSyncFrequency;
    public String theme;
}
