package com.antandbuffalo.birthdayreminder.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.about.About;
import com.antandbuffalo.birthdayreminder.backup.Backup;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.notificationfrequency.NotificationFrequency;
import com.antandbuffalo.birthdayreminder.notificationtime.NotificationTime;
import com.antandbuffalo.birthdayreminder.prenotification.PreNotification;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.ThemeOptions;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.antandbuffalo.birthdayreminder.wishtemplate.WishTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Settings extends AppCompatActivity {
    SettingsListAdapter settingsListAdapter;
    AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ListView settingsList = (ListView)findViewById(R.id.settingsList);
        settingsListAdapter = new SettingsListAdapter();
        //http://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        settingsList.setAdapter(settingsListAdapter);

        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingsModel selectedOption = settingsListAdapter.listData.get(position);
                if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_NOTIFICATION)) {
                    Intent intent = new Intent(view.getContext(), PreNotification.class);
                    startActivityForResult(intent, Constants.REFRESH_SETTINGS);
                }
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.settingsBackup)) {
                    Intent intent = new Intent(view.getContext(), Backup.class);
                    startActivityForResult(intent, Constants.REFRESH_SETTINGS);
                }
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_NOTIFICATION_TIME)) {
                    Intent intent = new Intent(view.getContext(), NotificationTime.class);
                    startActivityForResult(intent, Constants.REFRESH_SETTINGS);
                }
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_NOTIFICATION_FREQUENCY)) {
                    Intent intent = new Intent(view.getContext(), NotificationFrequency.class);
                    startActivityForResult(intent, Constants.REFRESH_SETTINGS);
                }
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_WISH_TEMPLATE)) {
                    Intent intent = new Intent(view.getContext(), WishTemplate.class);
                    startActivityForResult(intent, Constants.REFRESH_SETTINGS);
                }
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_ABOUT)) {
                    Intent intent = new Intent(view.getContext(), About.class);
                    startActivityForResult(intent, Constants.REFRESH_SETTINGS);
                }
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_SELECT_THEME)) {
                    selectTheme();
                }
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_DELETE_ALL)) {
                    //put confirmation here
                    androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(Settings.this);
                        // .setIcon(android.R.drawable.ic_dialog_alert)
                        // .setIconAttribute(android.R.attr.alertDialogIcon)
                    alertDialogBuilder.setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete all?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Settings.this, DateOfBirthDBHelper.deleteAll(), Toast.LENGTH_SHORT).show();
                                DataHolder.getInstance().refresh = true;
                                Storage.setDbBackupTime(new Date());
                            }
                        })
                        .setNegativeButton("No", null);

                    androidx.appcompat.app.AlertDialog dialog = alertDialogBuilder.create();
                    dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Settings.this, R.color.dark_gray));
                        }
                    });
                    dialog.show();
                }
            }
        });
        loadAd();
        showSnowFlakes();
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
                settingsListAdapter.refreshData();
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
        settingsListAdapter.refreshData();
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

