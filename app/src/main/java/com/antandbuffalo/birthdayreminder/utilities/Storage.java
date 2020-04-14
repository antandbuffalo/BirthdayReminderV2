package com.antandbuffalo.birthdayreminder.utilities;

import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by i677567 on 5/10/15.
 */
public class Storage {

    public static Integer getNotificationFrequency(SharedPreferences preferences) {
        return preferences.getInt(Constants.PREFERENCE_NOTIFINCATION_FREQUENCY, 1);
    }

    public static Integer getFeaturesNotificationStatus(SharedPreferences preferences, String key) {
        return preferences.getInt(key, 0);
    }

    public static void setFeaturesNotificationStatus(SharedPreferences preferences, String key, Integer value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getRestoreTime(SharedPreferences preferences, String key) {
        return Storage.getString(preferences, key, new Date().toString());
    }
    public static Boolean setRestoreTime(SharedPreferences preferences, String key, String dateTime) {
        return Storage.putString(preferences, key, dateTime);
    }

    public static String getBackupTime(SharedPreferences preferences, String key) {
        return Storage.getString(preferences, key, new Date().toString());
    }
    public static Boolean setBackupTime(SharedPreferences preferences, String key, String dateTime) {
        return Storage.putString(preferences, key, dateTime);
    }

    public static Integer getNotificationPerDay(SharedPreferences preferences, String key) {
        return Storage.getInt(preferences, key, 1);
    }
    public static Boolean setNotificationPerDay(SharedPreferences preferences, String key, Integer days) {
        return Storage.putInt(preferences, key, days);
    }

    public static Integer getNotificationHours(SharedPreferences preferences) {
        return Storage.getInt(preferences, Constants.PREFERENCE_NOTIFICATION_TIME_HOURS);
    }
    public static Integer getNotificationMinutes(SharedPreferences preferences) {
        return Storage.getInt(preferences, Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES);
    }

    public static Integer getPreNotificationDays(SharedPreferences preferences, String key) {
        return Storage.getInt(preferences, key);
    }
    public static Boolean setPreNotificationDays(SharedPreferences preferences, String key, Integer days) {
        return Storage.putInt(preferences, key, days);
    }

    public static Boolean putString(SharedPreferences preferences, String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        return editor.commit();
    }
    public static String getString(SharedPreferences preferences, String key) {
        return Storage.getString(preferences, key, "");
    }
    public static String getString(SharedPreferences preferences, String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public static Boolean putInt(SharedPreferences preferences, String key, Integer value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        return editor.commit();
    }
    public static Integer getInt(SharedPreferences preferences, String key) {
        return Storage.getInt(preferences, key, 0);
    }
    public static Integer getInt(SharedPreferences preferences, String key, Integer defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

}
