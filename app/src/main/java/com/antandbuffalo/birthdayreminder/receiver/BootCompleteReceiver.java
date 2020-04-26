package com.antandbuffalo.birthdayreminder.receiver;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

public class BootCompleteReceiver extends BroadcastReceiver {
    public BootCompleteReceiver() {
    }
    AlarmManager am;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // setOneTimeAlarm();
        System.out.println("inside boot complete");
        setRepeatingAlarm(context);
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setRepeatingAlarm(Context context) {
        DataHolder.getInstance().setAppContext(context);
        SharedPreferences preference = Util.getSharedPreference();
        Integer hours = Storage.getNotificationHours(preference);
        Integer minutes = Storage.getNotificationMinutes(preference);
        Integer frequency = Storage.getNotificationFrequency(preference);
        Util.setRepeatingAlarm(context, am, hours, minutes, frequency);
    }
}
