package com.antandbuffalo.birthdayreminder.settings;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by i677567 on 12/10/15.
 */
public class SettingsData {

    public static List getDefatultValues() {
        List<SettingsModel> data = new ArrayList();
        SettingsModel datum;
        JSONObject extraFields;

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.SETTINGS_NOTIFICATION);
        datum.setTitle("Pre Notification");
        datum.setSubTitle("");
        datum.setIconLetter("P");
        datum.setSno(1);
        int days = Storage.getInt(Util.getSharedPreference(), Constants.PREFERENCE_PRE_NOTIFICATION_DAYS);
        datum.setValue(days + " day");
        if(days > 1) {
            datum.setValue(days + " days");
        }
        data.add(datum);

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.SETTINGS_NOTIFICATION_TIME);
        datum.setTitle("Notification Time");
        datum.setSubTitle("");
        datum.setIconLetter("N");
        datum.setSno(2);
        datum.setValue("00:00");
        data.add(datum);

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.SETTINGS_NOTIFICATION_FREQUENCY);
        datum.setTitle("Notifications Per Day");
        datum.setSubTitle("");
        datum.setIconLetter("N");
        datum.setSno(3);
        datum.setValue(Storage.getNotificationFrequency(Util.getSharedPreference()) + "");
        data.add(datum);

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.SETTINGS_WISH_TEMPLATE);
        datum.setTitle("Wish Template");
        datum.setSubTitle("");
        datum.setIconLetter("W");
        datum.setSno(4);
        data.add(datum);

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.settingsBackup);
        datum.setTitle(Constants.SETTINGS_WRITE_FILE_TITLE);
        datum.setSubTitle(Constants.SETTINGS_WRITE_FILE_SUB_TITLE);
        //datum.setUpdatedOn(new Date());
        datum.setSno(5);
        data.add(datum);

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.SETTINGS_READ_FILE);
        datum.setTitle(Constants.SETTINGS_READ_FILE_TITLE);
        datum.setSubTitle(Constants.SETTINGS_READ_FILE_SUB_TITLE);
        //datum.setUpdatedOn(new Date());
        datum.setSno(6);
        data.add(datum);

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.SETTINGS_DELETE_ALL);
        datum.setTitle(Constants.SETTINGS_DELETE_ALL_TITLE);
        datum.setSubTitle("");
        //datum.setUpdatedOn(new Date());
        datum.setSno(7);
        data.add(datum);

        datum = SettingsModel.newInstance();
        datum.setKey(Constants.SETTINGS_ABOUT);
        datum.setTitle("About");
        datum.setSubTitle("");
        datum.setIconLetter("A");
        datum.setSno(8);
        data.add(datum);

        return data;
    }
}
