package com.antandbuffalo.birthdayreminder.utilities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.BuildConfig;
import com.antandbuffalo.birthdayreminder.MainActivity;
import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.database.OptionsDBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.models.UserProfile;
import com.antandbuffalo.birthdayreminder.notification.AlarmReceiver;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by i677567 on 5/10/15.
 */
public class Util {

    public static File getCachedFile(String defaultFileName) {
        Context context = DataHolder.getInstance().getAppContext();
        File cacheFile = new File(DataHolder.getInstance().getAppContext().getCacheDir(), "dob.txt");
        try {
            InputStream inputStream = context.getAssets().open("cse.txt");
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e("BR", e.getLocalizedMessage());
        }
        return cacheFile;
    }

    public static File getLocalFile(String fileName) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // sdcard not found. read from bundle
            //SD Card not found.
            return null;
        }
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            // Get the text file
            File file = new File(sdcard, Constants.FOLDER_NAME + "/" + fileName);
            if (!file.exists()) {
                Toast.makeText(DataHolder.getInstance().getAppContext(), Constants.ERROR_NO_BACKUP_FILE, Toast.LENGTH_SHORT).show();
                return null;
            }
            return file;
        }
        catch (Exception e) {
            Log.d("BRJB", "Not able to get file");
            return null;
        }
    }

    public static int compareDateAndMonth(Date date1, Date date2) {
        Calendar calendar1 = Util.getCalendar(date1);
        Calendar calendar2 = Util.getCalendar(date2);
        if(calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH) && calendar1.get(Calendar.DATE) == calendar2.get(Calendar.DATE)) {
            return 0;
        }
        else {
            return 1;
        }
    }

    public static String getTwoDigitsString(int number) {
        if(number < 10) {
            return "0" + number;
        }
        return number + "";
    }

    public static long getDaysBetweenDates(Date date1, Date date2) {
        //http://stackoverflow.com/questions/3838527/android-java-date-difference-in-days
        Calendar calDate1 = Calendar.getInstance();
        calDate1.setTime(date1);
        Calendar calDate2 = Calendar.getInstance();
        calDate2.setTime(date2);

        long diff = calDate2.getTimeInMillis() - calDate1.getTimeInMillis();
        long days = diff / (24 * 60 * 60 * 1000);
        return days;
    }

    public static long getDaysBetweenDates(Date date1) {
        return getDaysBetweenDates(date1, new Date());
    }

    public static int convertDPtoPixel(float density, Resources resources) {
        // Get the screen's density scale
        final float scale = resources.getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (density * scale + 0.5f);
    }

    public static Date getDateFromString(String input) {
        return getDateFromString(input, Constants.DATE_FORMAT);
    }

    public static Date getDateFromString(String input, String dateFormat) {
        if(input == null) {
            return null;
        }
        DateFormat format = new SimpleDateFormat(dateFormat, Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(input);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getStringFromDate(Date date) {
        return getStringFromDate(date, Constants.DATE_FORMAT);
    }

    public static String getStringFromDate(Date date, String dateFormat) {
        if(date == null || dateFormat == null) {
            return "";
        }
        SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return dateFormater.format(date);
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Calendar getCalendar() {
        return Util.getCalendar(new Date());
    }

    public static int getAge(Date date) {
        Calendar birthDate = getCalendar(date);
        Calendar currentDate = getCalendar(new Date());
        int age = currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        int currentDayInNumber = Integer.parseInt(Util.getStringFromDate(currentDate.getTime(), Constants.DAY_OF_YEAR));
        int birthDayInNumber = Integer.parseInt(Util.getStringFromDate(birthDate.getTime(), Constants.DAY_OF_YEAR));

        if(currentDayInNumber < birthDayInNumber) {
            age--;
        }
        return age;
    }

    public static String readFromFile(String fileName) {
        String returnValue = "";
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // sdcard not found. read from bundle
            //SD Card not found.
            return Constants.ERROR_NO_SD_CARD;
        }
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            // Get the text file
            File file = new File(sdcard, Constants.FOLDER_NAME + "/" + fileName);
            if (!file.exists()) {
                Toast.makeText(DataHolder.getInstance().getAppContext(), Constants.ERROR_NO_BACKUP_FILE, Toast.LENGTH_SHORT).show();
                return Constants.ERROR_NO_BACKUP_FILE;
            }
            BufferedReader br = new BufferedReader(new BufferedReader(new FileReader(file)));
            String strLine, n1, d1, m1, y1;
            String regexStr = "^[0-9]+$";
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                DateOfBirth dob = new DateOfBirth();
                dob.setRemoveYear(false);
                String[] lineComponents = strLine.split(" ");

                n1 = lineComponents[0];
                n1 = n1.replace("_", " ");
                d1 = lineComponents[1];
                m1 = lineComponents[2];
                y1 = lineComponents[3];

                if(lineComponents.length == 5) {
                    if(lineComponents[4].equals("1")) {
                        dob.setRemoveYear(true);
                    }
                }

                if(!d1.trim().matches(regexStr) || !m1.trim().matches(regexStr) || !y1.trim().matches(regexStr)) {
                    //write code here for failure
                    continue;
                }

                String dateStr = lineComponents[1] + " " + lineComponents[2] + " " + lineComponents[3];
                dob.setName(n1);
                DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_WITH_SPACE);
                dob.setDobDate(dateFormat.parse(dateStr));
                if (DateOfBirthDBHelper.isUniqueDateOfBirthIgnoreCase(dob)) {
                    DateOfBirthDBHelper.insertDOB(dob);
                }
            }
            //Close the input stream
            br.close();
            returnValue = Constants.NOTIFICATION_SUCCESS_DATA_LOAD;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            returnValue = Constants.ERROR_UNKNOWN;
        }
        System.out.println("Read successful");
        return returnValue;
    }

    public static File getLatestLocalBackupFile() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // sdcard not found. read from bundle
            //SD Card not found.
            return null;
        }
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            // Get the text file
            File file = new File(sdcard, Constants.FOLDER_NAME + "/dob.txt");
            if (!file.exists()) {
                return null;
            }
            return file;
        }
        catch (Exception e) {
            Log.d("FileRead", "File not found");
            return null;
        }
    }

    public static File writeToFile(Context context) {
        return writeToFile(context, null);
    }

    public static File writeToFile(Context context, String backupFileNameSuffix) {
        if(!Util.createEmptyFolder()) {
            if(context != null) {
                Toast.makeText(context, "Not able to create folder", Toast.LENGTH_LONG).show();
            }
            return null;
        }
        File sdcard = Environment.getExternalStorageDirectory();
        List<DateOfBirth> dobs = DateOfBirthDBHelper.selectAll();
        if(dobs == null || dobs.size() == 0) {
            if(context != null) {
                // Toast.makeText(context, "Not date of births available", Toast.LENGTH_LONG).show();
            }
            return null;
        }
        Calendar calendar = getCalendar(new Date());
        String currentDateTime = calendar.get(Calendar.YEAR) + getTwoDigitsString(calendar.get(Calendar.MONTH) + 1) + getTwoDigitsString(calendar.get(Calendar.DATE))
                + getTwoDigitsString(calendar.get(Calendar.HOUR_OF_DAY)) + getTwoDigitsString(calendar.get(Calendar.MINUTE)) + getTwoDigitsString(calendar.get(Calendar.SECOND));
        String fileName = "/" + Constants.FOLDER_NAME + "/" + Constants.FILE_NAME + Constants.FILE_NAME_SUFFIX;
        String fileNameBackup = "/" + Constants.FOLDER_NAME + "/" + Constants.FILE_NAME + "_" + currentDateTime;
        fileNameBackup = backupFileNameSuffix != null? (fileNameBackup + "_" + backupFileNameSuffix + Constants.FILE_NAME_SUFFIX) : (fileNameBackup + Constants.FILE_NAME_SUFFIX);

        File myFile = new File(sdcard, fileName);
        File myFileBackup = new File(sdcard, fileNameBackup);
        Boolean isBackUpFileCreated = myFile.renameTo(myFileBackup);
        System.out.println(isBackUpFileCreated);

        try {
            Boolean isFileCreated = myFile.createNewFile();
            System.out.println(isFileCreated);
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            for (DateOfBirth dob : dobs) {
                String name = dob.getName().replace(" ", Constants.SPACE_REPLACER);

                Calendar cal = getCalendar(dob.getDobDate());
                String dobString = cal.get(Calendar.DATE) + " "
                        + (cal.get(Calendar.MONTH) + 1) + " "
                        + cal.get(Calendar.YEAR);
                String isRemoveYear = dob.getRemoveYear()? "1" : "0";
                myOutWriter.append(name + " " + dobString + " " + isRemoveYear);
                myOutWriter.append("\n");
            }
            myOutWriter.close();
            fOut.close();
            System.out.println("Write successful");
            if(myFile != null) {
                System.out.println(new Date(myFile.lastModified()));
            }
            //myFileBackup.delete();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            //Toast.makeText(DataHolder.getInstance().getAppContext(), Constants.ERROR_READ_WRITE_1003, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return myFile;
    }

    public static String updateFile(DateOfBirth dob) {
        String returnValue = "";
        if(!Util.createEmptyFolder()) {
            return "Not able to create folder";
        }
        File sdcard = Environment.getExternalStorageDirectory();
        long currentMillis = System.currentTimeMillis();
        String fileName = "/" + Constants.FOLDER_NAME + "/" + Constants.FILE_NAME + Constants.FILE_NAME_SUFFIX;

        File myFile = new File(sdcard, fileName);

        try {
            FileOutputStream fOut = new FileOutputStream(myFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            String name = dob.getName().replace(" ", Constants.SPACE_REPLACER);

            Calendar cal = getCalendar(dob.getDobDate());

            String dobString = cal.get(Calendar.DATE) + " "
                    + (cal.get(Calendar.MONTH) + 1) + " "
                    + cal.get(Calendar.YEAR);
            String isRemoveYear = dob.getRemoveYear()? "1" : "0";
            myOutWriter.append(name + " " + dobString + " " + isRemoveYear);
            myOutWriter.append("\n");

            myOutWriter.close();
            fOut.close();
            System.out.println("Write successful");
            returnValue = Constants.STATUS_FILE_APPEND_SUCCESS;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            //Toast.makeText(DataHolder.getInstance().getAppContext(), Constants.ERROR_READ_WRITE_1003, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            returnValue = Constants.ERROR_UNKNOWN;
        }
        return returnValue;
    }

    public static Boolean copyFromAssetFileToDatabase(String defaultFileName) {
        try {
            BufferedReader br;
            DataInputStream in = null;

            AssetManager am = DataHolder.getInstance().getAppContext().getAssets();
            InputStream fstream = am.open(defaultFileName);
            // Get the object of DataInputStream
            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            String strLine, n1, d1, m1, y1;
            DateOfBirth dateOfBirth = new DateOfBirth();
            String regexStr = "^[0-9]+$";
            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console afdf
                //System.out.println("in main ac -- " + strLine);
                dateOfBirth.setRemoveYear(false);

                String[] lineComponents = strLine.split(" ");
                n1 = lineComponents[0];
                n1 = n1.replace("_", " ");
                d1 = lineComponents[1];
                m1 = lineComponents[2];
                y1 = lineComponents[3];

                if(lineComponents.length == 5) {
                    if(lineComponents[4].equals("1")) {
                        dateOfBirth.setRemoveYear(true);
                    }
                }

                if(!d1.trim().matches(regexStr) || !m1.trim().matches(regexStr) || !y1.trim().matches(regexStr)) {
                    //write code here for failure
                    Log.e("BRJB", "Not abel to parse string. Other than number is found d: " + d1 + " m: " + m1 + " y: " + y1);
                    continue;
                }

                dateOfBirth.setName(n1);
                dateOfBirth.setDobDate(Util.getDateFromString(y1 + "-" + m1 + "-" + d1));
                if(DateOfBirthDBHelper.isUniqueDateOfBirth(dateOfBirth)) {
                    DateOfBirthDBHelper.insertDOB(dateOfBirth);
                }
            }
            // Close the input stream
            in.close();
            return true;
        }
        catch (Exception e) {// Catch exception if any
            Log.e("BRJB", e.getLocalizedMessage());
            return false;
        }
    }
    public static Boolean createEmptyFolder() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //throw error sd card not found
            //Toast.makeText(DataHolder.getInstance().getAppContext(), Constants.ERROR_READ_WRITE_1004, Toast.LENGTH_LONG).show();
            return false;
        }
        File sdcard = Environment.getExternalStorageDirectory();
        File folder = new File(sdcard + File.separator + Constants.FOLDER_NAME);
        if(!folder.exists()) {
            Boolean isFolderCreated = folder.mkdir();
            System.out.println(isFolderCreated);
            return isFolderCreated;
        }
        else {
            return true;
        }
    }
    public static String fileToLoad(String input) {
        String key = input.toLowerCase();
        HashMap<String, String> fileNames = new HashMap<String, String>();
        fileNames.put("csea", "csea.txt");
        fileNames.put("cse", "cse.txt");
        fileNames.put("myfamily", "myfamily.txt");
        fileNames.put("thuda family", "thudaFamily.txt");
        fileNames.put("thudafamily", "thudaFamily.txt");
        fileNames.put("rengalla family", "rengallaFamily.txt");
        fileNames.put("rengallafamily", "rengallaFamily.txt");
        fileNames.put("9planets", "9planets.txt");
        fileNames.put("9 planets", "9planets.txt");
        fileNames.put("dxdx", "dxdx.txt");
        fileNames.put("inext", "inext.txt");
        if(fileNames.get(key) != null) {
            return fileNames.get(key);
        }
        return null;
    }
    public static Boolean isBackupFileFound(String fileName) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //throw error sd card not found
            return false;
        }
        File sdcard = Environment.getExternalStorageDirectory();
        // Get the text file
        File file = new File(sdcard, Constants.FOLDER_NAME + "/" + fileName + Constants.FILE_NAME_SUFFIX);
        return file.exists();
    }

    public static JSONObject validateAndSetExtra(JSONObject jsonObject, String key, String value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public static JSONObject parseJSON(String givenString) {
        JSONObject jsonObject = null;
        if(givenString != null) {
            try {
                jsonObject = new JSONObject(givenString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }
    public static SharedPreferences getSharedPreference() {
        SharedPreferences settings = DataHolder.getInstance().getAppContext().getSharedPreferences(Constants.PREFERENCE_NAME, 0);
        return settings;
    }
    public static void updateBackupTime(SettingsModel option) {
        option.setSubTitle("Last backup was");
        option.setUpdatedOn(new Date());
        OptionsDBHelper.updateOption(option);
    }
    public static void updateRestoreTime(SettingsModel option) {
        option.setSubTitle("Last restore was");
        option.setUpdatedOn(new Date());
        OptionsDBHelper.updateOption(option);
    }

    public static void setDescriptionForToday(DateOfBirth dob) {
        String desc = dob.getRemoveYear()? "Today" : "Turns " + dob.getAge() + " today";
        dob.setDescription(desc);
    }

    public static void setDescriptionForUpcoming(DateOfBirth dob, Integer days) {
        // if there is no year, just display as today or the number of days
        if(dob.getRemoveYear()) {
            dob.setDescription("In " + days + " days");
            if(days < 2) {
                dob.setDescription("In " + days + " day");
            }
        }
        // year is there. display desc with age
        else {
            String upcomingDaysText = "";
            if(days != null) {
                upcomingDaysText = " in " + days + " days";
                if(days < 2) {
                    upcomingDaysText = " in " + days + " day";
                }
            }
            else {
                upcomingDaysText = "";
            }
            dob.setDescription("Will be turning " + dob.getAge() + upcomingDaysText);
        }
    }

    public static List<String> getMonths() {
        String[] months = new DateFormatSymbols().getShortMonths();
        List<String> monthsList = Arrays.asList(months);
        for(int i = 0; i < monthsList.size(); i++) {
            String digit = getTwoDigitsString(i + 1);
            monthsList.set(i, monthsList.get(i) + " (" + digit + ")");
        }

        return monthsList;
    }

    public static Integer getDefaultDayOfYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, date.getMonth());
        cal.set(Calendar.DATE, date.getDate());
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public static Integer getDayOfYear(Date date) {
        return Integer.parseInt(Util.getStringFromDate(date, Constants.DAY_OF_YEAR));
    }

    public static Integer getCurrentDayOfYear() {
        return Integer.parseInt(Util.getStringFromDate(new Date(), Constants.DAY_OF_YEAR));
    }

    public static Integer getRecentDayOfYear() {
        Calendar cal = Util.getCalendar();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, Constants.RECENT_DURATION);

        return Integer.parseInt(Util.getStringFromDate(cal.getTime(), Constants.DAY_OF_YEAR));
    }
    public static Boolean isCurrentYear(int year) {
        Calendar calendar = Util.getCalendar(new Date());
        return calendar.get(Calendar.YEAR) == year;
    }
    public static int getCurrentMonth() {
        Calendar calendar = Util.getCalendar(new Date());
        return calendar.get(Calendar.MONTH);
    }
    public static Boolean isCurrentMonth(int month) {
        return month == getCurrentMonth();
    }
    public static int getCurrentDate() {
        Calendar calendar = Util.getCalendar(new Date());
        return calendar.get(Calendar.DATE);
    }

    public static Date addDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static void setRepeatingAlarm(Context context, AlarmManager alarmManager, int hour, int minute, int frequency) {
        Log.i("UTIL", "Setting repeating alarm in util");

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 123,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 12:00 AM
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long frequencyTime = Math.round(AlarmManager.INTERVAL_DAY / frequency);

        //Send notification twice a day
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequencyTime, pendingIntent);

        //Send notification every 5 seconds
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), (60   * 1000), pendingIntent);

        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), (5 * 1000), pendingIntent);

        //https://developer.android.com/training/scheduling/alarms.html#type
    }

    public static void setHappyBirthdayAlarm(Context context, AlarmManager alarmManager, int hour, int minute) {

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 123,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 12:00 AM
        calendar.set(Calendar.DATE, 12);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
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

    public static String getNotificationMessageWithTime(Context context, Date dob) {
        boolean is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
        String time = "";
        Integer hours, minutes;
        hours = Storage.getNotificationHours();
        minutes = Storage.getNotificationMinutes();
        if(is24HourFormat) {
            time = Util.getTwoDigitsString(hours)
                    + ":"
                    + Util.getTwoDigitsString(minutes);
        }
        else {
            if(hours > 12) {
                hours = hours - 12;
                time = Util.getTwoDigitsString(hours)
                        + ":"
                        + Util.getTwoDigitsString(minutes)
                        + "pm";
            }
            else {
                time = Util.getTwoDigitsString(hours)
                        + ":"
                        + Util.getTwoDigitsString(minutes)
                        + "am";
            }
        }
        String message = "You will get notified at "
                + time
                + " on "
                + Util.getStringFromDate(dob, "dd MMM") + " every year";
        return  message;
    }

    public static Map<String, DateOfBirth> getDateOfBirthMap() {
        Map<String, DateOfBirth> dateOfBirthMap = new HashMap<>();
        List<DateOfBirth> dobs = DateOfBirthDBHelper.selectAll();
        for(DateOfBirth dateOfBirth : dobs) {
            dateOfBirthMap.put(dateOfBirth.getDobId() + "", dateOfBirth);
        }
        return dateOfBirthMap;
    }

    public static void inserDateOfBirthFromServer(Map<String, Object> dateOfBirthMap) {
        for(Object genericDob : dateOfBirthMap.values()) {
            // Print the content on the console
            DateOfBirth dob = createDateOfBirthObject((Map<String, Object>)genericDob);

            if (DateOfBirthDBHelper.isUniqueDateOfBirthIgnoreCase(dob)) {
                DateOfBirthDBHelper.insertDOB(dob);
            }
            System.out.println("Read successful");
        }
    }

    public static DateOfBirth createDateOfBirthObject(Map<String, Object> genericDob) {
        DateOfBirth dateOfBirth = new DateOfBirth();
        dateOfBirth.setDobId((Long) genericDob.get("dobId"));
        dateOfBirth.setName((String) genericDob.get("name"));
        dateOfBirth.setAge(((Long) genericDob.get("age")).intValue());
        dateOfBirth.setDescription((String) genericDob.get("description"));
        dateOfBirth.setDobDate(((Timestamp) genericDob.get("dobDate")).toDate());
        dateOfBirth.setRemoveYear((Boolean) genericDob.get("removeYear"));
        return dateOfBirth;
    }

    public static Integer getSubstractionFactor() {
        int factor = 365;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        // Jan - 31, Feb - 28 = 59. if less than or equal to 59 then use current year. otherwise next year
        if(cal.get(Calendar.DAY_OF_YEAR) <= 59) {
            // check if current year is leap yere
            if(year % 4 == 0) {
                factor = 366;
            }
        }
        else {
            if((year + 1) % 4 == 0) {
                factor = 366;
            }
        }
        return factor;
    }

    public static UserProfile getUserProfileFromFirebaseUser(FirebaseUser firebaseUser) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUid(firebaseUser.getUid());
        userProfile.setDisplayName(firebaseUser.getDisplayName());
        userProfile.setEmail(firebaseUser.getEmail());
        userProfile.setProviderId(firebaseUser.getProviderId());
        userProfile.setUpdatedAt(new Date());
        userProfile.setDeviceName(Util.getDeviceName());
        userProfile.setLastOpenedAt(new Date());
        return userProfile;
    }

    public static String getCollectionId(FirebaseUser firebaseUser) {
         return firebaseUser.getEmail().replaceAll("[^a-zA-Z0-9]", "") + "_" + firebaseUser.getUid();
    }

    public static void applyTheme() {
        if(Storage.getTheme().equalsIgnoreCase(ThemeOptions.KEY_DEFAULT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else if(Storage.getTheme().equalsIgnoreCase(ThemeOptions.KEY_LIGHT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public static boolean showSnow() {
        if(Util.getCurrentDate() == Constants.SNOW_DAY && Util.getCurrentMonth() == Constants.SNOW_MONTH) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser == null) {
                return false;
            }
            else {
                return (firebaseUser.getEmail().indexOf(Constants.JB_ICON_EMAIL) > -1
                        || firebaseUser.getEmail().indexOf(Constants.JB_ICON_EMAIL_1) > -1);
            }

        }
        return false;
    }

    public static boolean showHappyBirthdayIconAndView() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        return firebaseUser != null
                && (firebaseUser.getEmail().indexOf(Constants.JB_ICON_EMAIL) > -1
                || firebaseUser.getEmail().indexOf(Constants.JB_ICON_EMAIL_1) > -1)
                && Util.getCurrentDate() == Constants.SNOW_DAY
                && Util.getCurrentMonth() == Constants.SNOW_MONTH;
    }

    public static void showHappyBirthdayNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "aandb_br_2";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Happy Birthday";
            String Description = "Wish you all the success and Happiness in your life forever";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            notificationManager.createNotificationChannel(mChannel);
        }

        //notification opening intent
        Intent resultingIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, resultingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Wish you all the success and Happiness in your life forever"))
                .setSmallIcon(R.drawable.ic_jb_light)
                .setContentTitle("Happy Birthday")
                .setContentText("Wish you all the success and Happiness in your life forever");

        mBuilder.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        int notificationId = 102;
        notificationManager.notify(notificationId, mBuilder.build());
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
