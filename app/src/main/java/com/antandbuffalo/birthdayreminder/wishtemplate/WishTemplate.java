package com.antandbuffalo.birthdayreminder.wishtemplate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.util.Date;

public class WishTemplate extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wish_template);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String currentTemplate = Storage.getWishTemplate(Util.getSharedPreference());

        EditText wishTemplate = findViewById(R.id.wishTemplate);
        wishTemplate.setText(currentTemplate);
    }

    public void save() {
        EditText wishTemplate = findViewById(R.id.wishTemplate);
        Storage.setWishTemplate(Util.getSharedPreference(), wishTemplate.getText().toString());
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
            save();
            Storage.setDbBackupTime(new Date());
        }
        else if(id == android.R.id.home) {

        }
        finish();
        return true;
    }
}
