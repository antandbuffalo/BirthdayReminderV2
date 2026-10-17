package com.antandbuffalo.birthdayreminder.prenotification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
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

public class PreNotification extends AppCompatActivity {
    private static final int MAX_DAYS = 7;

    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_notificaion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView heading = findViewById(R.id.remindHeading);
        heading.setText("Also remind me");
        buildOptions(Storage.getPreNotificationDays());

        loadAd();
        showSnowFlakes();
    }

    // one row per choice: 0 (on the day only) to 7 days early; tapping a row saves it and closes
    private void buildOptions(int selectedDays) {
        LinearLayout options = findViewById(R.id.remindOptions);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int days = 0; days <= MAX_DAYS; days++) {
            final int choice = days;
            UIUtil.addRadioRow(inflater, options,
                    days == 0 ? "No early reminder" : Storage.getDaysBeforeLabel(days),
                    days == 0 ? "on the day only" : null,
                    days == selectedDays, v -> save(choice));
        }
    }

    private void save(int days) {
        Storage.setPreNotificationDays(days);

        String message = days == 0 ? "Early reminder turned off"
                : "You'll also get a reminder " + Storage.getDaysBeforeLabel(days);
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        Storage.setDbBackupTime(new Date());
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void loadAd() {
        mAdView = this.findViewById(R.id.adView);
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
