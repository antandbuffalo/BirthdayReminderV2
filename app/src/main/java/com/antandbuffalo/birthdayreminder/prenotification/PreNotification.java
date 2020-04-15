package com.antandbuffalo.birthdayreminder.prenotification;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.lang.reflect.Field;

public class PreNotification extends AppCompatActivity {
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_notificaion);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final SharedPreferences settings = Util.getSharedPreference();
        intent = new Intent();

        final NumberPicker numberPicker = (NumberPicker) findViewById(R.id.recentDaysToday);
        setNumberPickerTextColor(numberPicker, Color.WHITE);
        numberPicker.setWrapSelectorWheel(true);
        numberPicker.setMaxValue(7);
        numberPicker.setMinValue(0);
        numberPicker.setWrapSelectorWheel(false);

        int preNotifDays = settings.getInt(Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, 0);
        numberPicker.setValue(preNotifDays);

        ImageButton save = (ImageButton) findViewById(R.id.save);
        save.setBackgroundResource(R.drawable.save_button);

        ImageButton cancel = (ImageButton)findViewById(R.id.cancel);
        cancel.setBackgroundResource(R.drawable.cancel_button);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                settings.getInt(Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, numberPicker.getValue());
                editor.commit();
                Toast toast = Toast.makeText(getApplicationContext(), Constants.NOTIFICATION_SUCCESSFULLY_UPDATED, Toast.LENGTH_SHORT);
                toast.show();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
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
            Log.d("BRJB", item.getItemId() + " : done");
            NumberPicker numberPicker = (NumberPicker) findViewById(R.id.recentDaysToday);
            Storage.putInt(Util.getSharedPreference(), Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, numberPicker.getValue());
        }
        else if(id == android.R.id.home) {
            Log.d("BRJB", item.getItemId() + " : back");
        }
        finish();
        return true;
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color)
    {
        final int count = numberPicker.getChildCount();
        for(int i = 0; i < count; i++){
            View child = numberPicker.getChildAt(i);
            if(child instanceof EditText){
                try{
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint)selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText)child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                }
                catch(NoSuchFieldException e){
                    Log.w("setNumberPickerTextCol", e);
                }
                catch(IllegalAccessException e){
                    Log.w("setNumberPickerTextCol", e);
                }
                catch(IllegalArgumentException e){
                    Log.w("setNumberPickerTextC", e);
                }
            }
        }
        return false;
    }
}
