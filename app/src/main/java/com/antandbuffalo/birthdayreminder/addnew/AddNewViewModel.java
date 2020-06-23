package com.antandbuffalo.birthdayreminder.addnew;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.BirthdayInfo;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AddNewViewModel extends ViewModel {

    // months starts from 0 for Jan
    DateOfBirth dateOfBirth;
    BirthdayInfo birthdayInfo;

    public void initDefaults() {
        Calendar cal = Calendar.getInstance();

        dateOfBirth = new DateOfBirth();
        birthdayInfo = new BirthdayInfo();

        dateOfBirth.setDobDate(cal.getTime());

        birthdayInfo.name = "";
        birthdayInfo.date = cal.get(Calendar.DATE) + "";
        birthdayInfo.month = cal.get(Calendar.MONTH) + "";
        // Cannot set current year as default. Because when user selects leap year,
        // then we need a lear year (2016) to make the date a valid one
        birthdayInfo.year = Constants.REMOVE_YEAR_VALUE.toString();
        //birthdayInfo.year = cal.get(Calendar.YEAR) + "";
        birthdayInfo.isRemoveYear = true;
    }

    public List getMonths() {
        return Util.getMonths();
    }

    public Boolean isDOBAvailable(DateOfBirth dob) {
        return !DateOfBirthDBHelper.isUniqueDateOfBirthIgnoreCase(dob);
    }

    public Boolean isNameEmpty() {
        if(dateOfBirth.getName() == null) {
            return true;
        }
        return dateOfBirth.getName().trim().equalsIgnoreCase("");
    }

    public String getFileName() {
        return Util.fileToLoad(dateOfBirth.getName());
    }

    public Boolean isBackupFileFound() {
        return Util.isBackupFileFound(dateOfBirth.getName());
    }

    public Boolean loadFromFileWithName(String fileName) {
        return Util.copyFromAssetFileToDatabase(fileName);
    }

    public String loadFromBackupFileWithName(String fileName) {
        return Util.readFromFile(fileName);
    }

    public Boolean setDateOfBirth(BirthdayInfo birthdayInfo) {
        int intDate, intMonth, intYear;

        birthdayInfo.year = birthdayInfo.isRemoveYear? Constants.REMOVE_YEAR_VALUE.toString() : birthdayInfo.year;
        dateOfBirth.setName(birthdayInfo.name);

        try {
            Calendar cal = Calendar.getInstance();
            intDate = Integer.parseInt(birthdayInfo.date);
            intMonth = Integer.parseInt(birthdayInfo.month);
            intYear = Integer.parseInt(birthdayInfo.year);
            cal.set(intYear, intMonth, intDate);
            Date plainDate = cal.getTime();

            dateOfBirth.setDobDate(plainDate);
            dateOfBirth.setRemoveYear(birthdayInfo.isRemoveYear);
            dateOfBirth.setAge(Util.getAge(dateOfBirth.getDobDate()));
            return true;

        }
        catch (Exception e) {
            Log.e("PARSE_INT", e.getLocalizedMessage());
            return false;
        }
    }

    public void saveToDB() {
        DateOfBirthDBHelper.insertDOB(dateOfBirth);
        //Util.updateFile(dateOfBirth);
    }

    public boolean isValidDateOfBirth(BirthdayInfo birthdayInfo) {
        int intDate, intMonth, intYear;
        Calendar calendar = Calendar.getInstance();
        try {
            intDate = Integer.parseInt(birthdayInfo.date);
            intMonth = Integer.parseInt(birthdayInfo.month);
            intYear = Integer.parseInt(birthdayInfo.year);
            calendar.set(intYear, intMonth, intDate);
            Date plainDate = calendar.getTime();
            calendar.setTime(plainDate);

            if(calendar.get(Calendar.DATE) != intDate || calendar.get(Calendar.MONTH) != intMonth || calendar.get(Calendar.YEAR) != intYear) {
                return false;
            }
            return  true;
        }
        catch (Exception e) {
            Log.e("PARSE_INT", e.getLocalizedMessage());
            return false;
        }
    }

    public void setBirthdayInfo(String name, String date, Integer month, String year, Boolean flag) {
        birthdayInfo.name = (name != null)? name : birthdayInfo.name;
        birthdayInfo.date = (date != null)? date : birthdayInfo.date;
        birthdayInfo.month = (month != null)? month.toString() : birthdayInfo.month;
        birthdayInfo.year = (year != null)? year : birthdayInfo.year;
        birthdayInfo.isRemoveYear = (flag != null)? flag : birthdayInfo.isRemoveYear;
    }
    public void setBirthdayInfoName(String name) {
        setBirthdayInfo(name, null, null, null, null);
    }
    public void setBirthdayInfoDate(String date) {
        setBirthdayInfo(null, date, null, null, null);
    }
    public void setBirthdayInfoMonth(Integer month) {
        setBirthdayInfo(null, null, month, null, null);
    }
    public void setBirthdayInfoYear(String year) {
        setBirthdayInfo(null, null, null, year, null);
    }
    public void setBirthdayInfoRemoveYear(Boolean flag) {
        setBirthdayInfo(null, null, null, null, flag);
    }

    public Integer getMonthSpinnerPosition(String month) {
        try {
            Integer monthPosition = Integer.parseInt(month);
            return monthPosition;
        }
        catch (Exception e) {
            Log.e("PARSE_INT", e.getLocalizedMessage());
            Calendar calendar = Calendar.getInstance();
            return calendar.get(Calendar.MONTH);
        }
    }

    public void clearInputs() {
        initDefaults();
    }

    public String getAddSuccessMessage(Context context) {
        return Constants.NOTIFICATION_ADD_MEMBER_SUCCESS + ". " + Util.getNotificationMessageWithTime(context, dateOfBirth.getDobDate());
    }
}
