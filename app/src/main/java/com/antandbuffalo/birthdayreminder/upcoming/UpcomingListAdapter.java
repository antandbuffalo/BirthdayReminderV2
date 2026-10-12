package com.antandbuffalo.birthdayreminder.upcoming;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
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
    SimpleDateFormat dateFormatter, yearFormatter;
    boolean happyBdayMode;
    List<DateOfBirth> filteredDobs = new ArrayList<DateOfBirth>();
    public UpcomingListAdapter() {
        dateFormatter = new SimpleDateFormat("MMM");
        yearFormatter = new SimpleDateFormat("yyyy");
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
        sendWish.setFocusable(false);
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
        TextView name = (TextView)convertView.findViewById(R.id.todayNameField);
        TextView desc = (TextView)convertView.findViewById(R.id.ageField);
        TextView daysCount = (TextView)convertView.findViewById(R.id.daysCount);
        TextView daysLabel = (TextView)convertView.findViewById(R.id.daysLabel);

        name.setText(dob.getName());
        desc.setText(getSubtitle(dob));

        int days = Util.getDaysLeft(dob.getDobDate());
        daysCount.setText(days + "");
        daysLabel.setText(days == 1 ? "day" : "days");
        boolean soon = days <= 7;
        int countColor = ContextCompat.getColor(parent.getContext(), soon ? R.color.aandb_soon_text_color : R.color.aandb_muted_text_color);
        daysCount.setTextColor(countColor);
        daysLabel.setTextColor(countColor);
        daysCount.setTypeface(null, soon ? Typeface.BOLD : Typeface.NORMAL);

        Date date = dob.getDobDate();
        cal.setTime(date);

        LinearLayout circle = (LinearLayout)convertView.findViewById(R.id.todayCirclebg);
//        if(dayOfYear == currentDayOfYear) {
//            circle.setBackgroundResource(R.drawable.cirlce_today);
//        }
        if(dayOfYear != currentDayOfYear) {
            TextView dateField = (TextView)convertView.findViewById(R.id.dateField);
            TextView monthField = (TextView)convertView.findViewById(R.id.monthField);

            dateField.setText(cal.get(Calendar.DATE) + "");
            monthField.setText(dateFormatter.format(cal.getTime()));

//            LinearLayout circle = (LinearLayout)convertView.findViewById(R.id.circlebg);
            dayOfYear = Integer.parseInt(Util.getStringFromDate(dob.getDobDate(), Constants.DAY_OF_YEAR));
            if(recentDayOfYear < currentDayOfYear) {   //year end case
                if(dayOfYear > currentDayOfYear || dayOfYear < recentDayOfYear) {
                    circle.setBackgroundResource(R.drawable.cirlce_recent);
                }
                else {
                    circle.setBackgroundResource(R.drawable.cirlce_normal);
                }
            }
            else if(dayOfYear <= recentDayOfYear && dayOfYear > currentDayOfYear ){
                circle.setBackgroundResource(R.drawable.cirlce_recent);
            }
            else {
                circle.setBackgroundResource(R.drawable.cirlce_normal);
            }
        }
        desc.setVisibility(dob.getRemoveYear() || dob.getAge() >= 0 ? View.VISIBLE : View.INVISIBLE);
        return convertView;
    }

    private String getSubtitle(DateOfBirth dob) {
        if(dob.getRemoveYear()) {
            return "No birth year saved";
        }
        return "Turns " + dob.getAge() + " · born " + yearFormatter.format(dob.getDobDate());
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
            monthString = dateFormatter.format(Util.getCalendar(dob.getDobDate()).getTime());
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
