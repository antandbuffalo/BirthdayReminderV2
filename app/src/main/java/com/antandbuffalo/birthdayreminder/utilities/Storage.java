package com.antandbuffalo.birthdayreminder.utilities;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.antandbuffalo.birthdayreminder.BuildConfig;
import com.antandbuffalo.birthdayreminder.models.UserPreference;
import com.antandbuffalo.birthdayreminder.models.UserProfile;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.Map;

/**
 * Created by i677567 on 5/10/15.
 */
public class Storage {

    public static void saveUserProfileToLocal(UserProfile userProfile) {
        Storage.putString("uid", userProfile.getUid());
        Storage.putString("displayName", userProfile.getDisplayName());
        Storage.putString("email", userProfile.getEmail());
        Storage.putString("providerId", userProfile.getProviderId());
        Storage.putString("updatedAt", Util.getStringFromDate(userProfile.getUpdatedAt(), Constants.dateTimeFormat));
        Storage.putString("deviceName", userProfile.getDeviceName());
        Storage.putString("lastOpenedAt", Util.getStringFromDate(userProfile.getLastOpenedAt(), Constants.dateTimeFormat));
    }

    public static UserProfile getUserProfileFromLocal() {
        UserProfile userProfile = new UserProfile();
        userProfile.setUid(Storage.getString("uid"));
        userProfile.setDisplayName(Storage.getString("displayName"));
        userProfile.setEmail(Storage.getString("email"));
        userProfile.setProviderId(Storage.getString("providerId"));
        userProfile.setUpdatedAt(Util.getDateFromString(Storage.getString("updatedAt"), Constants.dateTimeFormat));
        userProfile.setDeviceName(Storage.getString("deviceName"));
        userProfile.setLastOpenedAt(Util.getDateFromString(Storage.getString("lastOpenedAt"), Constants.dateTimeFormat));
        return userProfile;
    }

    public static int getLastBuildNumber() {
        return Storage.getInt(Util.getSharedPreference(), "lastBuildNumber", Constants.defaultBuildNumber);
    }

    public static void setLastBuildNumber(int buildNumber) {
        Storage.putInt(Util.getSharedPreference(), "lastBuildNumber", buildNumber);
    }
    public static Date getRatingPresentedDate() {
        return Util.getDateFromString(Storage.getString("ratingPresentedDate", "12/12/2012"), "dd/MM/yyyy");
    }

    public static void setRatingPresentedDate(Date date) {
        Storage.putString("ratingPresentedDate", Util.getStringFromDate(date, "dd/MM/yyyy"));
    }

    public static Date getLastAccSetupShownDate() {
        return Util.getDateFromString(Storage.getString("lastAppOpenDate", "12/12/2012"), "dd/MM/yyyy");
    }

    public static void setLastAccSetupShownDate(Date date) {
        Storage.putString("lastAppOpenDate", Util.getStringFromDate(date, "dd/MM/yyyy"));
    }

    public static Boolean getAccountSetupSyncReminder() {
        return Util.getSharedPreference().getBoolean(Constants.ACCOUNT_SETUP_SYNC, false);
    }

    public static void setAccountSetupSyncReminder(Boolean status) {
        SharedPreferences.Editor editor = Util.getSharedPreference().edit();
        editor.putBoolean(Constants.ACCOUNT_SETUP_SYNC, status);
        editor.commit();
    }

    public static String getTheme() {
        return Storage.getString("theme", ThemeOptions.getInstance().getValues().get(0).get("key"));
    }

    public static void setTheme(String theme) {
        Storage.putString("theme", theme.toLowerCase());
    }

    public static void setFirstTimeLaunch() {
        SharedPreferences.Editor editor = Util.getSharedPreference().edit();
        editor.putBoolean("isFirstTimeLaunch", false);
        editor.commit();
    }

    public static Boolean isFirstTimeLaunch() {
        return Util.getSharedPreference().getBoolean("isFirstTimeLaunch", true);
    }

    public static String getAutoSyncDate() {
        return Storage.getString("autoSyncDate", AutoSyncOptions.getInstance().getValues().get(0).get("key"));
    }

    public static void setAutoSyncDate(Date date) {
        String value = Util.getStringFromDate(date, "dd/MM/yyyy");
        Storage.putString("autoSyncDate", value);
    }

    public static String getAutoSyncFrequency() {
        return Storage.getString("autoSyncFrequency", AutoSyncOptions.getInstance().getValues().get(0).get("key"));
    }

    public static void setAutoSyncFrequency(String frequency) {
        Storage.putString("autoSyncFrequency", frequency.toLowerCase());
    }

    public static String getServerBackupTime() {
        return Storage.getString(Constants.serverBackupTime);
    }

    public static void setServerBackupTime(Date givenDate) {
        Storage.putString(Constants.serverBackupTime, Util.getStringFromDate(givenDate, Constants.backupDateFormatToStore));
    }

    public static String getDbBackupTime() {
        return Storage.getString(Constants.dbBackupTime);
    }

    public static void setDbBackupTime(Date givenDate) {
        Storage.putString(Constants.dbBackupTime, Util.getStringFromDate(givenDate, Constants.backupDateFormatToStore));
    }

    public static Integer getNotificationFrequency() {
        return Util.getSharedPreference().getInt(Constants.PREFERENCE_NOTIFINCATION_FREQUENCY, 1);
    }

    public static Integer getFeaturesNotificationStatus(String key) {
        return Util.getSharedPreference().getInt(key, 0);
    }

    public static void setFeaturesNotificationStatus(String key, Integer value) {
        SharedPreferences.Editor editor = Util.getSharedPreference().edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String getRestoreTime(String key) {
        return Storage.getString(key, new Date().toString());
    }

    public static Boolean setRestoreTime(String key, String dateTime) {
        return Storage.putString(key, dateTime);
    }

    public static String getBackupTime(String key) {
        return Storage.getString(key, new Date().toString());
    }

    public static Boolean setBackupTime(String key, String dateTime) {
        return Storage.putString(key, dateTime);
    }

    public static Integer getNotificationPerDay() {
        return Storage.getInt(Util.getSharedPreference(), Constants.PREFERENCE_NOTIFINCATION_FREQUENCY, 1);
    }

    public static Boolean setNotificationPerDay(Integer days) {
        return Storage.putInt(Util.getSharedPreference(), Constants.PREFERENCE_NOTIFINCATION_FREQUENCY, days);
    }

    public static Integer getNotificationHours() {
        return Storage.getInt(Util.getSharedPreference(), Constants.PREFERENCE_NOTIFICATION_TIME_HOURS);
    }

    public static Integer getNotificationMinutes() {
        return Storage.getInt(Util.getSharedPreference(), Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES);
    }

    public static Boolean setNotificationHours(Integer value) {
        return Storage.putInt(Util.getSharedPreference(), Constants.PREFERENCE_NOTIFICATION_TIME_HOURS, value);
    }

    public static Boolean setNotificationMinutes(Integer value) {
        return Storage.putInt(Util.getSharedPreference(), Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES, value);
    }

    public static Integer getPreNotificationDays() {
        return Storage.getInt(Util.getSharedPreference(), Constants.PREFERENCE_PRE_NOTIFICATION_DAYS);
    }

    public static Boolean setPreNotificationDays(Integer days) {
        return Storage.putInt(Util.getSharedPreference(), Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, days);
    }

    public static Boolean putString(String key, String value) {
        SharedPreferences.Editor editor = Util.getSharedPreference().edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getString(String key) {
        return Storage.getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return Util.getSharedPreference().getString(key, defaultValue);
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

    public static Boolean setWishTemplate(String wishTemplate) {
        return Storage.putString(Constants.PREFERENCE_WISH_TEMPLATE, wishTemplate);
    }

    public static String getWishTemplate() {
        return Storage.getString(Constants.PREFERENCE_WISH_TEMPLATE, Constants.WISH_TEMPLATE_DEFAULT);
    }


    public static String getNotificationTime(Context context) {
        int hours = Storage.getNotificationHours();
        int minutes = Storage.getNotificationMinutes();
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
        if (is24HourFormat) {
            return Util.getTwoDigitsString(hours) + ":" + Util.getTwoDigitsString(minutes);
        } else {
            String amOrPm = "AM";
            if (hours > 12) {
                hours = hours - 12;
                amOrPm = "PM";
            }
            return Util.getTwoDigitsString(hours) + ":" + Util.getTwoDigitsString(minutes) + " " + amOrPm;
        }
    }

    public static void updateUserPreference(UserPreference userPreference, AlarmManager alarmManager, Context context) {
        Storage.setWishTemplate(userPreference.wishTemplate);
        Storage.setPreNotificationDays(userPreference.preNotificationDays);
        Storage.setDbBackupTime(userPreference.localBackupTime);
        Storage.setServerBackupTime(userPreference.serverBackupTime);

        Boolean scheduleNotification = false;
        if (Storage.getNotificationPerDay() != userPreference.numberOfNotifications ||
                Storage.getNotificationHours() != userPreference.notificationHours ||
                Storage.getNotificationMinutes() != userPreference.notificationMinutes) {
            scheduleNotification = true;
        }

        Storage.setNotificationPerDay(userPreference.numberOfNotifications);
        Storage.setNotificationHours(userPreference.notificationHours);
        Storage.setNotificationMinutes(userPreference.notificationMinutes);

        if (alarmManager != null && context != null && scheduleNotification) {
            Util.setRepeatingAlarm(context, alarmManager, userPreference.notificationHours, userPreference.notificationMinutes, userPreference.numberOfNotifications);
        }

        if (userPreference.autoSyncFrequency != null) {
            Storage.setAutoSyncFrequency(userPreference.autoSyncFrequency);
        }

        if (userPreference.theme != null) {
            Storage.setTheme(userPreference.theme);
        } else {
            Storage.setTheme(ThemeOptions.getInstance().getValues().get(0).get("key"));
        }
    }

    public static UserPreference getUserPreference() {
        UserPreference userPreference = new UserPreference();
        userPreference.notificationHours = Storage.getNotificationHours();
        userPreference.notificationMinutes = Storage.getNotificationMinutes();
        userPreference.numberOfNotifications = Storage.getNotificationPerDay();
        userPreference.preNotificationDays = Storage.getPreNotificationDays();
        userPreference.serverBackupTime = Util.getDateFromString(Storage.getServerBackupTime(), Constants.backupDateFormatToStore);
        userPreference.wishTemplate = Storage.getWishTemplate();
        userPreference.localBackupTime = Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore);
        userPreference.versionName = BuildConfig.VERSION_NAME;
        userPreference.versionCode = BuildConfig.VERSION_CODE;
        userPreference.autoSyncFrequency = Storage.getAutoSyncFrequency();
        userPreference.theme = Storage.getTheme();
        return userPreference;
    }

    public static UserPreference createUserPreferenceFromServer(Map<String, Object> genericPreference) {
        UserPreference userPreference = new UserPreference();
        userPreference.wishTemplate = (String) genericPreference.get("wishTemplate");
        if (genericPreference.get("preNotificationDays") != null) {
            userPreference.preNotificationDays = ((Long) genericPreference.get("preNotificationDays")).intValue();
        }
        if (genericPreference.get("numberOfNotifications") != null) {
            userPreference.numberOfNotifications = ((Long) genericPreference.get("numberOfNotifications")).intValue();
        }
        if (genericPreference.get("notificationHours") != null) {
            userPreference.notificationHours = ((Long) genericPreference.get("notificationHours")).intValue();
        }
        if (genericPreference.get("notificationMinutes") != null) {
            userPreference.notificationMinutes = ((Long) genericPreference.get("notificationMinutes")).intValue();
        }

        Timestamp local = (Timestamp) genericPreference.get("localBackupTime");
        if (local != null) {
            userPreference.localBackupTime = local.toDate();
        }

        Timestamp server = (Timestamp) genericPreference.get("serverBackupTime");
        if (server != null) {
            userPreference.serverBackupTime = server.toDate();
        }

        userPreference.versionName = (String) genericPreference.get("versionName");
        if (genericPreference.get("versionCode") != null) {
            userPreference.versionCode = ((Long) genericPreference.get("versionCode")).intValue();
        }

        if (genericPreference.get("autoSyncFrequency") != null) {
            userPreference.autoSyncFrequency = (String) genericPreference.get("autoSyncFrequency");
        }

        if (genericPreference.get("theme") != null) {
            userPreference.theme = (String) genericPreference.get("theme");
        }

        return userPreference;
    }

}
