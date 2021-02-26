package com.antandbuffalo.birthdayreminder.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.MainActivity;
import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.backup.Backup;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.utilities.AutoSyncService;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    NotificationManager notificationManager;
    Integer notificationIcon = R.drawable.ic_notification;
    Integer iconColor = R.color.colorPrimary;
    public AlarmReceiver() {
    }

    public void showAccountAndFrequencyReminder(Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        CharSequence from = "Birthday Reminder";
        CharSequence message = null;
        if(firebaseUser == null) {
            message = "Don't forget to setup your Google account to backup your data";
        }
        if("none".equalsIgnoreCase(Storage.getAutoSyncFrequency())) {
            message = "Don't forget to select auto sync frequency to backup automatically";
        }
        if("none".equalsIgnoreCase(Storage.getAutoSyncFrequency()) && firebaseUser == null) {
            message = "Don't forget to setup your Google account and auto sync frequency to backup your data";
        }

        if(message == null) {
            return;
        }

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = setChannel(notificationManager);

        Intent backIntent = new Intent(context, MainActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //notification opening intent
        Intent resultingIntent = new Intent(context, Backup.class);

        // reference
        // https://stackoverflow.com/questions/13800680/back-to-main-activity-from-notification-created-activity

        // PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent contentIntent = PendingIntent.getActivities(context, 0, new Intent[] { backIntent, resultingIntent }, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(notificationIcon)
                .setContentTitle(from)
                .setContentText(message);
        mBuilder.setColor(ContextCompat.getColor(context, iconColor));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 104;
        notificationManager.notify(notificationId, mBuilder.build());

        // set that notification is shown
        Storage.setAccountSetupSyncReminder(true);
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
                .setSmallIcon(notificationIcon)
                .setContentTitle(from)
                .setContentText(message);
        mBuilder.setColor(ContextCompat.getColor(context, iconColor));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 102;
        notificationManager.notify(notificationId, mBuilder.build());
    }

    public void showNewFeatureNotification(Context context) {
        Integer shown = Storage.getFeaturesNotificationStatus("newFeature");
        if(shown > 0) {
            return;
        }

        CharSequence from = "New Features added";
        CharSequence message = "Complete revamp of UI. Sync with cloud. Multiple device support, Dark theme etc. Please have a look";

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = setChannel(notificationManager);

        //notification opening intent
        Intent resultingIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(notificationIcon)
                .setContentTitle(from)
                .setContentText(message);
        mBuilder.setColor(ContextCompat.getColor(context, iconColor));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 103;
        notificationManager.notify(notificationId, mBuilder.build());

        Storage.setFeaturesNotificationStatus("newFeature", 1);
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
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        autoSync(alarmManager, context, connectivityManager);

        // check whether account and frequency has been setup and show notification
        if(checkAccountAndFrequency()) {
            showAccountAndFrequencyReminder(context);
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
                .setSmallIcon(notificationIcon)
                .setContentTitle(from)
                .setContentText(message);
        mBuilder.setColor(ContextCompat.getColor(context, iconColor));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 101;
        notificationManager.notify(notificationId, mBuilder.build());
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        if(Util.isHappyBirthday()) {
            Util.showHappyBirthdayNotification(context);
        }
    }

    public boolean checkAccountAndFrequency() {
        Calendar calendar = Util.getCalendar(new Date());
        Integer date = calendar.get(Calendar.DATE);
        // it is an important date. Hope you remember ;)
        if(date == Constants.DATE_ACCOUNT_SETUP_SYNC) {
            return !Storage.getAccountSetupSyncReminder();
        }
        else {
            Storage.setAccountSetupSyncReminder(false);
            return false;
        }
    }

    public void autoSync(AlarmManager alarmManager, Context context, ConnectivityManager connectivityManager) {
        Calendar calendar = Util.getCalendar(new Date());
        Integer dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Integer date = calendar.get(Calendar.DATE);
        AutoSyncService autoSyncService = new AutoSyncService(alarmManager, context, connectivityManager);
        if(Storage.getAutoSyncFrequency().equalsIgnoreCase("daily")) {
            autoSyncService.syncNow();
        }
        else if(Storage.getAutoSyncFrequency().equalsIgnoreCase("weekly") && dayOfWeek == 1) {
            autoSyncService.syncNow();
        }
        else if(Storage.getAutoSyncFrequency().equalsIgnoreCase("monthly") && date == 1) {
            autoSyncService.syncNow();
        }
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
