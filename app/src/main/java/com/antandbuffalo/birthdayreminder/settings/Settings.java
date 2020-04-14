package com.antandbuffalo.birthdayreminder.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.antandbuffalo.birthdayreminder.R;

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

    }
}
