package com.antandbuffalo.birthdayreminder.settings;

import android.content.Context;
import android.text.format.DateUtils;

import com.antandbuffalo.birthdayreminder.BuildConfig;
import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.ThemeOptions;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by i677567 on 12/10/15.
 */
public class SettingsData {
    public static final String TYPE_DANGER = "danger";
    public static final String TYPE_EXTERNAL = "external";

    // section title -> rows, in display order
    public static Map<String, List<SettingsModel>> getSections(Context context) {
        Map<String, List<SettingsModel>> sections = new LinkedHashMap<>();

        List<SettingsModel> reminders = new ArrayList<>();
        reminders.add(row(Constants.SETTINGS_NOTIFICATION, "Remind me", getRemindMe(Storage.getPreNotificationDays()), R.drawable.ic_settings_remind));
        reminders.add(row(Constants.SETTINGS_NOTIFICATION_TIME, "Reminder time", Storage.getNotificationTime(context), R.drawable.ic_settings_time));
        reminders.add(row(Constants.SETTINGS_NOTIFICATION_FREQUENCY, "Times a day", Storage.getNotificationFrequency() + "", R.drawable.ic_settings_repeat));
        sections.put("Reminders", reminders);

        List<SettingsModel> wishes = new ArrayList<>();
        wishes.add(row(Constants.SETTINGS_WISH_TEMPLATE, "Wish template", "“" + Storage.getWishTemplate().trim() + "”", R.drawable.ic_settings_wish));
        sections.put("Wishes", wishes);

        List<SettingsModel> data = new ArrayList<>();
        data.add(row(Constants.settingsBackup, "Backup & restore", getLastBackup(), R.drawable.ic_settings_backup));
        data.add(row(Constants.SETTINGS_DELETE_ALL, "Delete all birthdays", "", R.drawable.ic_settings_delete, TYPE_DANGER));
        sections.put("Your data", data);

        List<SettingsModel> app = new ArrayList<>();
        app.add(row(Constants.SETTINGS_SELECT_THEME, "Theme", getTheme(), R.drawable.ic_settings_theme));
        app.add(row(Constants.SETTINGS_WEB, "Web version", "", R.drawable.ic_settings_web, TYPE_EXTERNAL));
        app.add(row(Constants.SETTINGS_ABOUT, "About", "Version " + BuildConfig.VERSION_NAME, R.drawable.ic_settings_info));
        sections.put("App", app);

        return sections;
    }

    private static SettingsModel row(String key, String title, String subTitle, int iconId) {
        return row(key, title, subTitle, iconId, null);
    }

    private static SettingsModel row(String key, String title, String subTitle, int iconId, String type) {
        SettingsModel datum = SettingsModel.newInstance();
        datum.setKey(key);
        datum.setTitle(title);
        datum.setSubTitle(subTitle);
        datum.setIconId(iconId);
        datum.setType(type);
        return datum;
    }

    private static String getRemindMe(int days) {
        if (days == 0) {
            return "On the day";
        }
        // the on-the-day reminder always fires; this setting adds one extra, N days earlier
        return "On the day and " + Storage.getDaysBeforeLabel(days);
    }

    private static String getLastBackup() {
        Date backupTime = Util.getDateFromString(Storage.getServerBackupTime(), Constants.backupDateFormatToStore);
        if (backupTime == null) {
            return "Not backed up yet";
        }
        long now = System.currentTimeMillis();
        if (now - backupTime.getTime() < DateUtils.MINUTE_IN_MILLIS) {
            return "Last backup just now";
        }
        return "Last backup " + DateUtils.getRelativeTimeSpanString(backupTime.getTime(), now, DateUtils.MINUTE_IN_MILLIS);
    }

    private static String getTheme() {
        for (Map<String, String> option : ThemeOptions.getInstance().getValues()) {
            if (Storage.getTheme().equalsIgnoreCase(option.get("key"))) {
                return option.get("value");
            }
        }
        return "";
    }
}
