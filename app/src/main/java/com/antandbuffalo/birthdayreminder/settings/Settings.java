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
import com.antandbuffalo.birthdayreminder.wishtemplate.WishTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {

        }
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsListAdapter.refreshData();
    }

    public void loadAd() {
        mAdView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}

// history
// alarm
// timeline
// assignment
// backup
// delete

