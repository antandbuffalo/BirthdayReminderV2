package com.antandbuffalo.birthdayreminder.upcoming;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.sharewish.ShareWish;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.core.content.ContextCompat;


/**
 * Created by i677567 on 23/9/15.
 */
public class UpcomingListAdapter extends BaseAdapter {

    int currentDayOfYear, dayOfYear, recentDayOfYear;
    Calendar cal;
    List<DateOfBirth> dobs;
    List<DateOfBirth> allDobs;
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMM");
    boolean happyBdayMode;
    List<DateOfBirth> filteredDobs = new ArrayList<DateOfBirth>();
    public UpcomingListAdapter() {
        cal = Calendar.getInstance();
        setDefaultValues();
    }

    @Override
    public int getCount() {
        if(dobs == null) {
            return 0;
        }
        return dobs.size();
    }

    @Override
    public DateOfBirth getItem(int position) {
        return dobs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        DateOfBirth dob = dobs.get(position);
        dayOfYear = Integer.parseInt(Util.getStringFromDate(dob.getDobDate(), Constants.DAY_OF_YEAR));
        return dayOfYear == currentDayOfYear? 0 : 1;
    }

    public View getTodayView(View convertView, DateOfBirth dateOfBirth, ViewGroup parent) {
        TextView name = (TextView)convertView.findViewById(R.id.todayNameField);
        TextView desc = (TextView)convertView.findViewById(R.id.todayDesc);
        name.setText(dateOfBirth.getName());
        desc.setText(happyBdayMode ? dateOfBirth.getDescription() : getSubtitle(dateOfBirth));

        View sendWish = convertView.findViewById(R.id.sendWish);
        sendWish.setOnClickListener(view -> {
            Intent shareWish = new Intent(parent.getContext(), ShareWish.class);
            parent.getContext().startActivity(shareWish);
        });

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DateOfBirth dob = dobs.get(position);
        dayOfYear = Integer.parseInt(Util.getStringFromDate(dob.getDobDate(), Constants.DAY_OF_YEAR));
        if(convertView == null) {
            if(dayOfYear == currentDayOfYear) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                if(Util.showHappyBirthdayIconAndView()) {
//                    convertView = inflater.inflate(R.layout.list_item_jb, parent, false);
//                }
//                else {
                    convertView = inflater.inflate(R.layout.list_item_today_v2, parent, false);
                    convertView = getTodayView(convertView, dob, parent);
                    return convertView;
//                }
            }
            else {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_default, parent, false);
            }
        }
        if(dayOfYear == currentDayOfYear) {
            System.out.println("convert view exist" + convertView);
            return getTodayView(convertView, dob, parent);
        }
        bindRow(convertView, dob, currentDayOfYear, recentDayOfYear);
        TextView desc = convertView.findViewById(R.id.ageField);
        desc.setVisibility(dob.getRemoveYear() || dob.getAge() >= 0 ? View.VISIBLE : View.INVISIBLE);
        return convertView;
    }

    public static String getSubtitle(DateOfBirth dob) {
        if(dob.getRemoveYear()) {
            return "No birth year saved";
        }
        return "Turns " + dob.getAge() + " · born " + Util.getCalendar(dob.getDobDate()).get(Calendar.YEAR);
    }

    // Fills a list_item_default row. Also used by the Add New / Edit preview.
    public static void bindRow(View row, DateOfBirth dob, int currentDayOfYear, int recentDayOfYear) {
        Date date = dob.getDobDate();
        ((TextView)row.findViewById(R.id.todayNameField)).setText(dob.getName());
        ((TextView)row.findViewById(R.id.ageField)).setText(getSubtitle(dob));
        ((TextView)row.findViewById(R.id.dateField)).setText(Util.getCalendar(date).get(Calendar.DATE) + "");
        ((TextView)row.findViewById(R.id.monthField)).setText(MONTH_FORMAT.format(date));

        int dayOfYear = Util.getDayOfYear(date);
        int circle;
        if(dayOfYear == currentDayOfYear) {
            circle = R.drawable.cirlce_today;
        }
        else if(recentDayOfYear < currentDayOfYear) {   //year end case
            circle = dayOfYear > currentDayOfYear || dayOfYear < recentDayOfYear ? R.drawable.cirlce_recent : R.drawable.cirlce_normal;
        }
        else {
            circle = dayOfYear <= recentDayOfYear && dayOfYear > currentDayOfYear ? R.drawable.cirlce_recent : R.drawable.cirlce_normal;
        }
        row.findViewById(R.id.todayCirclebg).setBackgroundResource(circle);

        int days = Util.getDaysLeft(date);
        TextView daysCount = row.findViewById(R.id.daysCount);
        TextView daysLabel = row.findViewById(R.id.daysLabel);
        daysCount.setText(days + "");
        daysLabel.setText(days == 1 ? "day" : "days");
        boolean today = days == 0;
        daysCount.setVisibility(today ? View.GONE : View.VISIBLE);
        daysLabel.setVisibility(today ? View.GONE : View.VISIBLE);
        row.findViewById(R.id.todayBadge).setVisibility(today ? View.VISIBLE : View.GONE);
        boolean soon = days <= 7;
        int countColor = ContextCompat.getColor(row.getContext(), soon ? R.color.aandb_soon_text_color : R.color.aandb_muted_text_color);
        daysCount.setTextColor(countColor);
        daysLabel.setTextColor(countColor);
        daysCount.setTypeface(null, soon ? Typeface.BOLD : Typeface.NORMAL);
    }

    public void refreshData() {
        setDefaultValues();
        this.notifyDataSetChanged();
    }

    public void setDefaultValues() {
        currentDayOfYear = Integer.parseInt(Util.getStringFromDate(new Date(), Constants.DAY_OF_YEAR));
        cal.setTime(new Date());
        cal.add(Calendar.DATE, Constants.RECENT_DURATION);
        recentDayOfYear = Integer.parseInt(Util.getStringFromDate(cal.getTime(), Constants.DAY_OF_YEAR));
        allDobs = DateOfBirthDBHelper.selectUpcoming();
        dobs = DateOfBirthDBHelper.selectUpcoming();

        happyBdayMode = Util.showHappyBirthdayIconAndView();
        if(happyBdayMode) {
            allDobs = getHappyBday();
            dobs = getHappyBday();
        }
    }

    public void filter(String input) {
        filteredDobs.clear();
        // perform your search here using the searchConstraint String.
        DateOfBirth dob;
        String dateString, monthString;
        for (int i = 0; i < allDobs.size(); i++) {
            dob = allDobs.get(i);
            dateString = Util.getStringFromDate(dob.getDobDate());
            monthString = MONTH_FORMAT.format(Util.getCalendar(dob.getDobDate()).getTime());
            input = input.toLowerCase();
            if (dob.getName().toLowerCase().contains(input) || dateString.contains(input) || monthString.toLowerCase().contains(input))  {
                filteredDobs.add(dob);
            }
        }
        dobs = filteredDobs;
    }

    public List<DateOfBirth> getHappyBday() {
        List<DateOfBirth> happyBday = new ArrayList<>();
        DateOfBirth dateOfBirth = new DateOfBirth();
        dateOfBirth.setName("Happy Birthday baby");
        dateOfBirth.setAge(33);
        dateOfBirth.setDescription("Have a wonderful year ahead");
        dateOfBirth.setDobDate(new Date());
        dateOfBirth.setRemoveYear(false);
        happyBday.add(dateOfBirth);
        return happyBday;
    }
}
