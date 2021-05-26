package com.antandbuffalo.birthdayreminder.notificationfrequency;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.NumberPicker;
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

public class NotificationFrequency extends AppCompatActivity {
    Intent intent;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_frequency);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settings = Util.getSharedPreference();

        intent = new Intent();
        final NumberPicker numberPicker = (NumberPicker) findViewById(R.id.notificationFrequency);
        // Util.setNumberPickerTextColor(numberPicker, Color.WHITE);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setMaxValue(24);
        numberPicker.setMinValue(1);
        numberPicker.setWrapSelectorWheel(false);

        populateSpinnerFrequency(numberPicker);
        loadAd();
        showSnowFlakes();
    }

    public void saveNotificationFrequency() {
        final NumberPicker numberPicker = (NumberPicker) findViewById(R.id.notificationFrequency);
        int hours = settings.getInt(Constants.PREFERENCE_NOTIFICATION_TIME_HOURS, 0);
        int minutes = settings.getInt(Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Util.setRepeatingAlarm(getApplicationContext(), alarmManager, hours, minutes, numberPicker.getValue());

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(Constants.PREFERENCE_NOTIFINCATION_FREQUENCY, numberPicker.getValue());
        editor.commit();
        Toast toast = Toast.makeText(getApplicationContext(), "Number of Notifications per day updated Successfully", Toast.LENGTH_SHORT);
        toast.show();
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
            saveNotificationFrequency();
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

    public void populateSpinnerFrequency(NumberPicker numberPicker) {
        numberPicker.setValue(Storage.getNotificationFrequency());
    }

    public void loadAd() {
        AdView mAdView = this.findViewById(R.id.adView);
        if(!Constants.enableAds) {
            mAdView.setVisibility(View.INVISIBLE);
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void showSnowFlakes() {
        if(Util.showSnow()) {
            View snowFlakes = this.findViewById(R.id.snowFlakes);
            snowFlakes.setVisibility(View.VISIBLE);
        }
    }
}
