package com.antandbuffalo.birthdayreminder.notification;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

public class BootComplete extends BroadcastReceiver {
    public BootComplete() {
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
        Integer hours = Storage.getNotificationHours();
        Integer minutes = Storage.getNotificationMinutes();
        Integer frequency = Storage.getNotificationFrequency();
        Util.setRepeatingAlarm(context, am, hours, minutes, frequency);
    }
}
