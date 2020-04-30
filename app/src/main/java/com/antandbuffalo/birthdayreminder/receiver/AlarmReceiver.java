package com.antandbuffalo.birthdayreminder.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.antandbuffalo.birthdayreminder.MainActivity;
import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.util.Date;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    NotificationManager notificationManager;
    public AlarmReceiver() {
    }
  
    public void showPreNotifications(Context context, int preNotifDays) {
        Date futureDate = Util.addDays(preNotifDays);
        List<DateOfBirth> preNotifList = DateOfBirthDBHelper.selectForTheDate(context, futureDate);
        if(preNotifList == null || preNotifList.size() == 0) {
            return;
        }
        CharSequence from = "Birthday - Upcoming in " + preNotifDays + " days";
        if(preNotifDays == 1) {
            from = "Birthday - Upcoming in " + preNotifDays + " day";
        }

        CharSequence message = "";
        String sep = "";

        for (DateOfBirth dateOfBirth : preNotifList) {
            message = message + sep + dateOfBirth.getName();
            sep = ", ";
        }

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = setChannel(notificationManager);

        //notification opening intent
        Intent resultingIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(from)
                .setContentText(message);
        mBuilder.setColor(Color.argb(255, 121, 85, 72));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 102;
        notificationManager.notify(notificationId, mBuilder.build());
    }

    public void showNewFeatureNotification(Context context) {
        SharedPreferences settings = Util.getSharedPreference();
        Integer shown = Storage.getFeaturesNotificationStatus(Constants.NEW_FEATURE_2_6_0_34);
        if(shown > 0) {
            return;
        }

        CharSequence from = "New Features added";
        CharSequence message = "You can change the notification time, number of notifications per day in settings. Please have a look";

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = setChannel(notificationManager);

        //notification opening intent
        Intent resultingIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(from)
                .setContentText(message);
        mBuilder.setColor(Color.argb(255, 121, 85, 72));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 103;
        notificationManager.notify(notificationId, mBuilder.build());

        Storage.setFeaturesNotificationStatus(Constants.NEW_FEATURE_2_6_0_34, 1);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving
        System.out.println("inside schedule receiver");
        // Need to set the context first to make the app work properly
        DataHolder.getInstance().setAppContext(context);

        showNewFeatureNotification(context);
        final SharedPreferences settings = Util.getSharedPreference();
        int preNotifDays = settings.getInt(Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, 0);
        if(preNotifDays > 0) {
            showPreNotifications(context, preNotifDays);
        }
        List<DateOfBirth> todayList = DateOfBirthDBHelper.selectToday(context);
        if(todayList == null || todayList.size() == 0) {
            return;
        }
        CharSequence from = "Birthday Today";
        CharSequence message = "";
        String sep = "";

        for (DateOfBirth dateOfBirth : todayList) {
            message = message + sep + dateOfBirth.getName();
            sep = ", ";
        }

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = setChannel(notificationManager);
        //notification opening intent
        Intent resultingIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(from)
                .setContentText(message);
        mBuilder.setColor(Color.argb(255, 121, 85, 72));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 101;
        notificationManager.notify(notificationId, mBuilder.build());
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public String setChannel(NotificationManager notificationManager) {
        String CHANNEL_ID = "aandb_br_1";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "BirthdayReminder";
            String Description = "Birthday Reminder notification channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            //mChannel.enableLights(true);
            //mChannel.enableVibration(true);
            //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }
        return CHANNEL_ID;
    }
}
