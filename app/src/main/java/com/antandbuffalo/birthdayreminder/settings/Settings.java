package com.antandbuffalo.birthdayreminder.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
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
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.prenotification.PreNotification;
import com.antandbuffalo.birthdayreminder.utilities.Constants;

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
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        settingsListAdapter.refreshData();
    }
}
