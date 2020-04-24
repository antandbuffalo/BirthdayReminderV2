package com.antandbuffalo.birthdayreminder.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.backup.Backup;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.notificationtime.NotificationTime;
import com.antandbuffalo.birthdayreminder.prenotification.PreNotification;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.util.Date;

public class Settings extends AppCompatActivity {
    SettingsListAdapter settingsListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

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
                else if (selectedOption.getKey().equalsIgnoreCase(Constants.SETTINGS_DELETE_ALL)) {
                    //put confirmation here
                    new AlertDialog.Builder(Settings.this)
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete all?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(Settings.this, DateOfBirthDBHelper.deleteAll(), Toast.LENGTH_SHORT).show();
                                DataHolder.getInstance().refresh = true;
                                Storage.setDbBackupTime(new Date());
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsListAdapter.refreshData();
    }
}
