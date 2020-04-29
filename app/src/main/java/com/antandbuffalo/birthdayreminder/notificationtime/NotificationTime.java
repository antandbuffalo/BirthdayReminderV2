package com.antandbuffalo.birthdayreminder.notificationtime;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;

public class NotificationTime extends AppCompatActivity {
    Intent intent;
    boolean is24HourFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_time);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intent = new Intent();

        TimePicker picker=(TimePicker)findViewById(R.id.notificationTimePicker);
        is24HourFormat = android.text.format.DateFormat.is24HourFormat(this);
        picker.setIs24HourView(is24HourFormat);

        populateInitialValues(picker, Util.getSharedPreference().getInt(Constants.PREFERENCE_NOTIFICATION_TIME_HOURS, 0), Util.getSharedPreference().getInt(Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES, 0), is24HourFormat);

        loadAd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("BRJB", item.getItemId() + "");
        int id = item.getItemId();
        if (id == R.id.action_menu_done) {
            saveNotificationTime();
            Storage.setDbBackupTime(new Date());
            setResult(RESULT_OK, intent);
        }
        else if(id == android.R.id.home) {
            Log.d("BRJB", item.getItemId() + " : back");
            setResult(RESULT_CANCELED, intent);
        }
        finish();
        return true;
    }

    public void saveNotificationTime() {
        TimePicker picker=(TimePicker)findViewById(R.id.notificationTimePicker);
        int hour, minute;
        if (Build.VERSION.SDK_INT >= 23 ){
            hour = picker.getHour();
            minute = picker.getMinute();
        }
        else{
            hour = picker.getCurrentHour();
            minute = picker.getCurrentMinute();
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int frequency = Storage.getNotificationFrequency(Util.getSharedPreference());
        Util.setRepeatingAlarm(getApplicationContext(), alarmManager, hour, minute, frequency);

        SharedPreferences.Editor editor = Util.getSharedPreference().edit();
        editor.putInt(Constants.PREFERENCE_NOTIFICATION_TIME_HOURS, hour);
        editor.putInt(Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES, minute);
        editor.commit();
        Toast toast = Toast.makeText(getApplicationContext(), "Notification Time updated successfully", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void populateInitialValues(TimePicker timePicker, int hours, int minutes, boolean is24Hours) {
        if (Build.VERSION.SDK_INT >= 23 ){
            timePicker.setHour(hours);
            timePicker.setMinute(minutes);
        }
        else {
            timePicker.setCurrentHour(hours);
            timePicker.setCurrentMinute(minutes);
        }
        timePicker.setIs24HourView(is24Hours);
    }

    public void loadAd() {
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

}
