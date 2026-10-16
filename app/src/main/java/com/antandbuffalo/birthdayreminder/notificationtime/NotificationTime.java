package com.antandbuffalo.birthdayreminder.notificationtime;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import androidx.core.text.HtmlCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.UIUtil;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;

public class NotificationTime extends AppCompatActivity {
    private static final int MAX_PER_DAY = 24;

    TimePicker picker;
    TextView timesLine;
    TextView perDayValue;
    View perDayMinus;
    View perDayPlus;
    int perDay;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_time);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView heading = findViewById(R.id.timeHeading);
        heading.setText("Remind me at");
        // rounded card corners also clip the picker's teal header
        findViewById(R.id.timeCard).setClipToOutline(true);

        timesLine = findViewById(R.id.timesLine);
        picker = findViewById(R.id.notificationTimePicker);
        picker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(this));
        picker.setHour(Storage.getNotificationHours());
        picker.setMinute(Storage.getNotificationMinutes());
        picker.setOnTimeChangedListener((view, hour, minute) -> showTimesLine());

        perDayValue = findViewById(R.id.perDayValue);
        perDayMinus = findViewById(R.id.perDayMinus);
        perDayPlus = findViewById(R.id.perDayPlus);
        perDayMinus.setOnClickListener(v -> setPerDay(perDay - 1));
        perDayPlus.setOnClickListener(v -> setPerDay(perDay + 1));
        setPerDay(Storage.getNotificationPerDay());

        loadAd();
        showSnowFlakes();
    }

    // 1 to 24; the − / + buttons fade at the ends
    private void setPerDay(int value) {
        perDay = Math.max(1, Math.min(MAX_PER_DAY, value));
        perDayValue.setText(String.valueOf(perDay));
        UIUtil.setEnabledDimmed(perDayMinus, perDay > 1);
        UIUtil.setEnabledDimmed(perDayPlus, perDay < MAX_PER_DAY);
        showTimesLine();
    }

    // one line that follows "Times a day": the first reminder, and how the rest are spread over the day
    private void showTimesLine() {
        int hour = picker.getHour();
        int minute = picker.getMinute();
        String first = "<b>" + Storage.formatTime(this, hour, minute) + "</b>";
        String line = Util.capitalize(Storage.getTimesADayLabel(perDay));
        if (perDay <= 1) {
            line += " at " + first;
        } else if (perDay == 2) {
            line += ": " + first + " and <b>" + Storage.formatTime(this, (hour + 12) % 24, minute) + "</b>";
        } else {
            line += ", " + everyLabel(perDay) + " from " + first;
        }
        timesLine.setText(HtmlCompat.fromHtml(line, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    // 24 / perDay hours; uneven gaps are rounded to the nearest half hour, e.g. 7 a day -> about every 3½ hours
    private String everyLabel(int perDay) {
        if (24 % perDay == 0) {
            int hours = 24 / perDay;
            return hours == 1 ? "every hour" : "every " + hours + " hours";
        }
        int halfHours = Math.round(48f / perDay);
        String hours = halfHours / 2 + (halfHours % 2 == 1 ? "½" : "");
        return "about every " + hours + (hours.equals("1") ? " hour" : " hours");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_menu_done) {
            saveNotificationTime();
            Storage.setDbBackupTime(new Date());
        }
        finish();
        return true;
    }

    public void saveNotificationTime() {
        int hour = picker.getHour();
        int minute = picker.getMinute();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Util.setRepeatingAlarm(getApplicationContext(), alarmManager, hour, minute, perDay);
        Storage.setNotificationPerDay(perDay);
        Storage.setNotificationHours(hour);
        Storage.setNotificationMinutes(minute);
        Toast.makeText(getApplicationContext(), "Reminders now at " + Storage.formatTime(this, hour, minute) + ", " + Storage.getTimesADayLabel(perDay), Toast.LENGTH_SHORT).show();
    }

    public void loadAd() {
        mAdView = findViewById(R.id.adView);
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
    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    public void onPause() {
        // Pause the AdView.
        mAdView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Destroy the AdView.
        mAdView.destroy();
        super.onDestroy();
    }
}
