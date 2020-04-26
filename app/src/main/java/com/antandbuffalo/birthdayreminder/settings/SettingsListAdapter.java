package com.antandbuffalo.birthdayreminder.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.OptionsDBHelper;
import com.antandbuffalo.birthdayreminder.models.SettingsModel;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;

import java.security.cert.PKIXRevocationChecker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by i677567 on 23/9/15.
 * http://stackoverflow.com/questions/18868194/android-xml-layout-for-a-listview-with-different-items
 */
public class SettingsListAdapter extends BaseAdapter {
    List<SettingsModel> listData;
    int currentDayOfYear, dayOfYear;
    Calendar cal;
    SimpleDateFormat dateFormatter;
    SharedPreferences settingsPref;

    SettingsListAdapter() {
        //listData = OptionsDBHelper.selectAll();
        listData = SettingsData.getDefatultValues();
        currentDayOfYear = Integer.parseInt(Util.getStringFromDate(new Date(), Constants.DAY_OF_YEAR));
        cal = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM");
        settingsPref = Util.getSharedPreference();
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public SettingsModel getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return Constants.SETTINGS_CELL_TYPES_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        SettingsModel option = listData.get(position);
        //return Integer.parseInt((String)itemTypes.get(option.getKey()));
        return getCellType(option);
        //return super.getItemViewType(position);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsModel option = listData.get(position);
        int cellType = getCellType(option);
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.settings_listitem_default, parent, false);
        }

        TextView name = (TextView)convertView.findViewById(R.id.nameField);
        name.setText(option.getTitle());

        ImageView listItemIcon = convertView.findViewById(R.id.listItemIcon);
        listItemIcon.setBackgroundResource(option.getIconId());

//        LinearLayout circle = (LinearLayout)convertView.findViewById(R.id.circlebg);
//
//        if(option.getKey().equalsIgnoreCase(Constants.SETTINGS_DELETE_ALL) || option.getKey().equalsIgnoreCase(Constants.SETTINGS_READ_FILE) || option.getKey().equalsIgnoreCase(Constants.SETTINGS_WRITE_FILE)) {
//            circle.setBackgroundResource(R.drawable.cirlce_missed);
//        }
//        else {
//            circle.setBackgroundResource(R.drawable.cirlce_normal);
//        }

        TextView currentValue = (TextView)convertView.findViewById(R.id.currentValue);
        String givenKey = option.getKey().toUpperCase();
        currentValue.setVisibility(View.VISIBLE);
        currentValue.setText(option.getValue());
        switch (givenKey) {
            case Constants.SETTINGS_NOTIFICATION_TIME: {
                currentValue.setText(Storage.getNotificationTime(Util.getSharedPreference(), parent.getContext()));
                break;
            }
            case Constants.SETTINGS_NOTIFICATION_FREQUENCY: {
                currentValue.setText(getSelectedFrequency(parent.getContext()));
                break;
            }
        }

        return convertView;
    }

    public int getSelectedValueForKey(String givenKey) {
        int preNotifDays = 0;
        if(givenKey.equalsIgnoreCase(Constants.SETTINGS_MODIFY_TODAY)) {
            preNotifDays = settingsPref.getInt(Constants.PREFERENCE_RECENT_DAYS_TODAY, 0);
        }
        else if(givenKey.equalsIgnoreCase(Constants.SETTINGS_NOTIFICATION)) {
            preNotifDays = settingsPref.getInt(Constants.PREFERENCE_PRE_NOTIFICATION_DAYS, 0);
        }
        return preNotifDays;
    }

    public String getSelectedFrequency(Context context) {
        Integer frequency = Storage.getNotificationFrequency(settingsPref);
        return frequency.toString();
    }

    public void refreshData() {
        listData = SettingsData.getDefatultValues();
        this.notifyDataSetChanged();
    }

    public int getCellType(SettingsModel option) {
        int returnValue = Constants.SETTINGS_CELL_TYPE_DATE;

        /*
        if(Constants.SETTINGS_CELL_TYPE_1_LETTER_VALUES.contains(option.getKey())) {
            returnValue = Constants.SETTINGS_CELL_TYPE_1_LETTER;
        }
        else {
            if(option.getUpdatedOn() == null) {
                returnValue = Constants.SETTINGS_CELL_TYPE_NA;
            }
            else {
                returnValue = Constants.SETTINGS_CELL_TYPE_DATE;
            }
        }*/
        if(option.getUpdatedOn() == null) {
            returnValue = Constants.SETTINGS_CELL_TYPE_1_LETTER;
        }
        else {
            returnValue = Constants.SETTINGS_CELL_TYPE_DATE;
        }

        return returnValue;
    }
}