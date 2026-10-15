package com.antandbuffalo.birthdayreminder.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.about.About;
import com.antandbuffalo.birthdayreminder.backup.Backup;
import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.notificationfrequency.NotificationFrequency;
import com.antandbuffalo.birthdayreminder.notificationtime.NotificationTime;
import com.antandbuffalo.birthdayreminder.prenotification.PreNotification;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.DeleteDialog;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.ThemeOptions;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.antandbuffalo.birthdayreminder.wishtemplate.WishTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends AppCompatActivity {
    AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadAd();
        showSnowFlakes();
    }

    // rows that just open another screen
    private static final Map<String, Class<?>> SCREENS = new HashMap<String, Class<?>>() {{
        put(Constants.SETTINGS_NOTIFICATION, PreNotification.class);
        put(Constants.SETTINGS_NOTIFICATION_TIME, NotificationTime.class);
        put(Constants.SETTINGS_NOTIFICATION_FREQUENCY, NotificationFrequency.class);
        put(Constants.SETTINGS_WISH_TEMPLATE, WishTemplate.class);
        put(Constants.settingsBackup, Backup.class);
        put(Constants.SETTINGS_ABOUT, About.class);
    }};

    public void onSettingClick(String key) {
        Class<?> screen = SCREENS.get(key);
        if (screen != null) {
            startActivity(new Intent(this, screen));
        }
        else if (key.equals(Constants.SETTINGS_WEB)) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.WEB_URL)));
        }
        else if (key.equals(Constants.SETTINGS_SELECT_THEME)) {
            selectTheme();
        }
        else if (key.equals(Constants.SETTINGS_DELETE_ALL)) {
            long count = DBHelper.getNumberOfRows(Constants.TABLE_DATE_OF_BIRTH);
            if (count == 0) {
                Toast.makeText(Settings.this, "No birthdays to delete", Toast.LENGTH_SHORT).show();
                return;
            }
            DeleteDialog.showAll(Settings.this, count, () -> {
                DateOfBirthDBHelper.deleteAll();
                Toast.makeText(Settings.this, "All birthdays deleted", Toast.LENGTH_SHORT).show();
                DataHolder.getInstance().refresh = true;
                Storage.setDbBackupTime(new Date());
            });
        }
    }

    // one heading + rounded card per section
    public void buildSettings() {
        LinearLayout container = findViewById(R.id.settingsContainer);
        container.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (Map.Entry<String, List<SettingsModel>> section : SettingsData.getSections(this).entrySet()) {
            TextView heading = (TextView) inflater.inflate(R.layout.settings_section, container, false);
            heading.setText(section.getKey());
            container.addView(heading);

            ViewGroup group = (ViewGroup) inflater.inflate(R.layout.settings_group, container, false);
            group.setClipToOutline(true);
            container.addView(group);
            for (SettingsModel option : section.getValue()) {
                group.addView(bindRow(inflater, group, option));
            }
        }
    }

    private View bindRow(LayoutInflater inflater, ViewGroup group, SettingsModel option) {
        View row = inflater.inflate(R.layout.settings_row, group, false);
        TextView title = row.findViewById(R.id.settingsTitle);
        title.setText(option.getTitle());
        TextView subtitle = row.findViewById(R.id.settingsSubtitle);
        subtitle.setText(option.getSubTitle());
        subtitle.setVisibility(option.getSubTitle().isEmpty() ? View.GONE : View.VISIBLE);
        ImageView icon = row.findViewById(R.id.settingsIcon);
        icon.setImageResource(option.getIconId());

        ImageView trailing = row.findViewById(R.id.settingsTrailing);
        if (SettingsData.TYPE_DANGER.equals(option.getType())) {
            int danger = ContextCompat.getColor(this, R.color.aandb_error_text);
            title.setTextColor(danger);
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(danger));
            row.findViewById(R.id.settingsIconCircle).setBackgroundResource(R.drawable.settings_icon_circle_danger);
            trailing.setVisibility(View.GONE);
        }
        else if (SettingsData.TYPE_EXTERNAL.equals(option.getType())) {
            trailing.setImageResource(R.drawable.ic_settings_open);
        }
        row.setOnClickListener(v -> onSettingClick(option.getKey()));
        return row;
    }

    public void selectTheme() {
        androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(Settings.this);
        List<Map<String, String>> optionsList = ThemeOptions.getInstance().getValues();
        CharSequence options[] = new CharSequence[optionsList.size()];

        Integer selectedItem = 0;
        for(int i = 0; i < optionsList.size(); i++) {
            options[i] = optionsList.get(i).get("value");
            if(optionsList.get(i).get("key").equalsIgnoreCase(Storage.getTheme())) {
                selectedItem = i;
            }
        }
        adb.setSingleChoiceItems(options, selectedItem, null);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ListView lw = ((androidx.appcompat.app.AlertDialog) dialogInterface).getListView();
                System.out.println("selected : " + lw.getCheckedItemPosition());
                Storage.setTheme(optionsList.get(lw.getCheckedItemPosition()).get("key"));
                Storage.setDbBackupTime(new Date());
                Util.applyTheme();
                // refresh the list to update selected theme
                buildSettings();
            }
        });
        adb.setNegativeButton("Cancel", null);
        adb.setTitle("Select Theme");

        androidx.appcompat.app.AlertDialog dialog = adb.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Settings.this, R.color.dark_gray));
            }
        });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {

        }
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
        buildSettings();
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

// history
// alarm
// timeline
// assignment
// backup
// delete

