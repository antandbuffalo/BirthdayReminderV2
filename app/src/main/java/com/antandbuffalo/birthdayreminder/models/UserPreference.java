package com.antandbuffalo.birthdayreminder.models;

import androidx.annotation.Keep;

import java.util.Date;
import java.util.List;

@Keep
public class UserPreference {
    public Date serverBackupTime;
    public Date localBackupTime;
    public Integer preNotificationDays;
    public Integer notificationHours;
    public Integer notificationMinutes;
    public Integer numberOfNotifications;
    public String wishTemplate;
    public List<String> wishes;
    public String versionName;
    public Integer versionCode;
    public String autoSyncFrequency;
    public String theme;
}
