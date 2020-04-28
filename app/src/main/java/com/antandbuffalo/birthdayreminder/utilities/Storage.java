package com.antandbuffalo.birthdayreminder.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.antandbuffalo.birthdayreminder.models.UserPreference;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.Map;

/**
 * Created by i677567 on 5/10/15.
 */
public class Storage {

    public static String getAutoSyncFrequency() {
        return Storage.getString(Util.getSharedPreference(), "autoSyncFrequency", AutoSyncOptions.getInstance().getValues().get(0).get("key"));
    }

    public static void setAutoSyncFrequency(String frequency) {
        Storage.putString(Util.getSharedPreference(), "autoSyncFrequency", frequency.toLowerCase());
    }


    public static String getServerBackupTime() {
        return Storage.getString(Util.getSharedPreference(), Constants.serverBackupTime);
    }

    public static void setServerBackupTime(Date givenDate) {
        Storage.putString(Util.getSharedPreference(), Constants.serverBackupTime, Util.getStringFromDate(givenDate, Constants.backupDateFormatToStore));
    }

    public static String getDbBackupTime() {
        return Storage.getString(Util.getSharedPreference(), Constants.dbBackupTime);
    }

    public static void setDbBackupTime(Date givenDate) {
        Storage.putString(Util.getSharedPreference(), Constants.dbBackupTime, Util.getStringFromDate(givenDate, Constants.backupDateFormatToStore));
    }

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

    public static Integer getNotificationPerDay(SharedPreferences preferences) {
        return Storage.getInt(preferences, Constants.PREFERENCE_NOTIFINCATION_FREQUENCY, 1);
    }
    public static Boolean setNotificationPerDay(SharedPreferences preferences, Integer days) {
        return Storage.putInt(preferences, Constants.PREFERENCE_NOTIFINCATION_FREQUENCY, days);
    }

    public static Integer getNotificationHours(SharedPreferences preferences) {
        return Storage.getInt(preferences, Constants.PREFERENCE_NOTIFICATION_TIME_HOURS);
    }
    public static Integer getNotificationMinutes(SharedPreferences preferences) {
        return Storage.getInt(preferences, Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES);
    }

    public static Boolean setNotificationHours(SharedPreferences preferences, Integer value) {
        return Storage.putInt(preferences, Constants.PREFERENCE_NOTIFICATION_TIME_HOURS, value);
    }
    public static Boolean setNotificationMinutes(SharedPreferences preferences, Integer value) {
        return Storage.putInt(preferences, Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES, value);
    }

    public static Integer getPreNotificationDays(SharedPreferences preferences) {
        return Storage.getInt(preferences, Constants.PREFERENCE_PRE_NOTIFICATION_DAYS);
    }
    public static Boolean setPreNotificationDays(SharedPreferences preferences, Integer days) {
        return Storage.putInt(preferences, Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, days);
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

    public static Boolean setWishTemplate(SharedPreferences sharedPreferences, String wishTemplate) {
        return Storage.putString(sharedPreferences, Constants.PREFERENCE_WISH_TEMPLATE, wishTemplate);
    }

    public static String getWishTemplate(SharedPreferences sharedPreferences) {
        return Storage.getString(sharedPreferences, Constants.PREFERENCE_WISH_TEMPLATE, Constants.WISH_TEMPLATE_DEFAULT);
    }


    public static String getNotificationTime(SharedPreferences sharedPreferences, Context context) {
        int hours = Storage.getNotificationHours(sharedPreferences);
        int minutes = Storage.getNotificationMinutes(sharedPreferences);
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
        if(is24HourFormat) {
            return Util.getTwoDigitsString(hours) + ":" + Util.getTwoDigitsString(minutes);
        }
        else {
            String amOrPm = "AM";
            if(hours > 12) {
                hours = hours - 12;
                amOrPm = "PM";
            }
            return Util.getTwoDigitsString(hours) + ":" + Util.getTwoDigitsString(minutes) + " " + amOrPm;
        }
    }

    public static void updateUserPreference(UserPreference userPreference) {
        Storage.setWishTemplate(Util.getSharedPreference(), userPreference.wishTemplate);
        Storage.setPreNotificationDays(Util.getSharedPreference(), userPreference.preNotificationDays);
        Storage.setNotificationPerDay(Util.getSharedPreference(), userPreference.numberOfNotifications);
        Storage.setNotificationHours(Util.getSharedPreference(), userPreference.notificationHours);
        Storage.setNotificationMinutes(Util.getSharedPreference(), userPreference.notificationMinutes);
        Storage.setDbBackupTime(userPreference.localBackupTime);
        Storage.setServerBackupTime(userPreference.serverBackupTime);
    }

    public static UserPreference getUserPreference() {
        UserPreference userPreference = new UserPreference();
        userPreference.notificationHours = Storage.getNotificationHours(Util.getSharedPreference());
        userPreference.notificationMinutes = Storage.getNotificationMinutes(Util.getSharedPreference());
        userPreference.numberOfNotifications = Storage.getNotificationPerDay(Util.getSharedPreference());
        userPreference.preNotificationDays = Storage.getPreNotificationDays(Util.getSharedPreference());
        userPreference.serverBackupTime = Util.getDateFromString(Storage.getServerBackupTime(), Constants.backupDateFormatToStore);
        userPreference.wishTemplate = Storage.getWishTemplate(Util.getSharedPreference());
        userPreference.localBackupTime = Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore);
        return userPreference;
    }

    public static UserPreference createUserPreferenceFromServer(Map<String, Object> genericPreference) {
        UserPreference userPreference = new UserPreference();
        userPreference.wishTemplate = (String) genericPreference.get("wishTemplate");
        if(genericPreference.get("preNotificationDays") != null) {
            userPreference.preNotificationDays = ((Long) genericPreference.get("preNotificationDays")).intValue();
        }
        if(genericPreference.get("numberOfNotifications") != null) {
            userPreference.numberOfNotifications = ((Long) genericPreference.get("numberOfNotifications")).intValue();
        }
        if(genericPreference.get("notificationHours") != null) {
            userPreference.notificationHours = ((Long) genericPreference.get("notificationHours")).intValue();
        }
        if(genericPreference.get("notificationMinutes") != null) {
            userPreference.notificationMinutes = ((Long) genericPreference.get("notificationMinutes")).intValue();
        }

        Timestamp local = (Timestamp) genericPreference.get("localBackupTime");
        userPreference.localBackupTime = local.toDate();

        Timestamp server = (Timestamp) genericPreference.get("serverBackupTime");
        userPreference.serverBackupTime = server.toDate();
        return userPreference;
    }

}
