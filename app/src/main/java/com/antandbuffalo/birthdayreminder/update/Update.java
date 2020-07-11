package com.antandbuffalo.birthdayreminder.update;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.antandbuffalo.birthdayreminder.MainActivity;
import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;

public class Update extends AppCompatActivity {
    EditText name;
    DateOfBirth currentDOB;
    Intent intent;

    private UpdateViewModel updateViewModel;
    int dayOfYear, currentDayOfYear, recentDayOfYear;
    TextView namePreview, desc, dateField, monthField, yearField;
    Spinner monthSpinner;
    EditText dateText, yearText;
    LinearLayout circle, todayImage;
    CheckBox removeYear;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DBHelper.createInstance(this);
        updateViewModel = ViewModelProviders.of(this).get(UpdateViewModel.class);

        currentDOB = (DateOfBirth)getIntent().getSerializableExtra("currentDOB");
        updateViewModel.initDefaults(currentDOB);
        initLayout();

        loadAd();

        name.setText(updateViewModel.birthdayInfo.name);
        dateText.setText(updateViewModel.birthdayInfo.date);
        yearText.setText(updateViewModel.birthdayInfo.year);
        removeYear.setChecked(updateViewModel.birthdayInfo.isRemoveYear);

        if(updateViewModel.birthdayInfo.isRemoveYear) {
            yearText.setVisibility(View.INVISIBLE);
        }
        else {
            yearText.setVisibility(View.VISIBLE);
        }

        currentDayOfYear = Util.getCurrentDayOfYear();
        recentDayOfYear = Util.getRecentDayOfYear();

        addMonthsToSpinner(monthSpinner);
        monthSpinner.setSelection(updateViewModel.getSelectedMonthPosition());

        preview();

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Item select", "" + position);
                updateViewModel.setSelectedMonth(monthSpinner.getSelectedItemPosition());
                preview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i("Item select", "" + parent);
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateViewModel.setName(name.getText().toString());
                preview();
            }
        });

        dateText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateViewModel.setBirthdayInfoDate(dateText.getText().toString());
                preview();
            }
        });

        yearText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateViewModel.setBirthdayInfoYear(yearText.getText().toString());
                preview();
            }
        });

        removeYear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateViewModel.setRemoveYear(isChecked);
                if(isChecked) {
                    yearText.setVisibility(View.INVISIBLE);
                }
                else {
                    yearText.setVisibility(View.VISIBLE);
                }
                addMonthsToSpinner(monthSpinner);
                monthSpinner.setSelection(updateViewModel.getSelectedMonthPosition());
                preview();
            }
        });

        intent = new Intent();
    }

    public void addMonthsToSpinner(Spinner spinner) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, updateViewModel.getMonths());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void populateBirthdayInfo() {
        updateViewModel.setBirthdayInfoName(name.getText().toString());
        updateViewModel.setBirthdayInfoDate(dateText.getText().toString());
        updateViewModel.setBirthdayInfoMonth(monthSpinner.getSelectedItemPosition());
        updateViewModel.setBirthdayInfoRemoveYear(removeYear.isChecked());

        updateViewModel.setBirthdayInfoYear(updateViewModel.birthdayInfo.isRemoveYear? Constants.REMOVE_YEAR_VALUE.toString() : yearText.getText().toString());
    }

    public void preview() {
        if(!updateViewModel.isValidDateOfBirth(updateViewModel.birthdayInfo)) {
            if(updateViewModel.birthdayInfo.name == null || ("".equalsIgnoreCase(updateViewModel.birthdayInfo.name))) {
                namePreview.setText("Enter Name");
                desc.setText("Age");
            } else {
                namePreview.setText("Error in birthdate");
                desc.setText("Please enter valid date");
            }
            return;
        }
        populateBirthdayInfo();

        namePreview.setText(updateViewModel.birthdayInfo.name);
        updateViewModel.setDateOfBirth(updateViewModel.birthdayInfo);
        dayOfYear = Util.getDayOfYear(updateViewModel.dateOfBirth.getDobDate());

        if(dayOfYear == currentDayOfYear) {
            circle.setBackgroundResource(R.drawable.cirlce_today);
            todayImage.setVisibility(View.VISIBLE);
            circle.setVisibility(View.INVISIBLE);
        }
        else if(recentDayOfYear < currentDayOfYear) {   //year end case
            todayImage.setVisibility(View.INVISIBLE);
            circle.setVisibility(View.VISIBLE);
            if(dayOfYear > currentDayOfYear || dayOfYear < recentDayOfYear) {
                circle.setBackgroundResource(R.drawable.cirlce_recent);
            }
            else {
                circle.setBackgroundResource(R.drawable.cirlce_normal);
            }
        }
        else if(dayOfYear <= recentDayOfYear && dayOfYear > currentDayOfYear ){
            todayImage.setVisibility(View.INVISIBLE);
            circle.setVisibility(View.VISIBLE);
            circle.setBackgroundResource(R.drawable.cirlce_recent);
        }
        else {
            todayImage.setVisibility(View.INVISIBLE);
            circle.setVisibility(View.VISIBLE);
            circle.setBackgroundResource(R.drawable.cirlce_normal);
        }

        populateDescription(updateViewModel.dateOfBirth);

        if(updateViewModel.getRemoveYear()) {
            yearField.setVisibility(View.INVISIBLE);
            //desc.setVisibility(View.INVISIBLE);
        }
        else {
            if(updateViewModel.dateOfBirth.getAge() < 0) {
                desc.setVisibility(View.INVISIBLE);
            } else {
                desc.setVisibility(View.VISIBLE);
            }
            yearField.setVisibility(View.VISIBLE);
        }

        dateField.setText(updateViewModel.birthdayInfo.date);
        monthField.setText(Util.getStringFromDate(updateViewModel.dateOfBirth.getDobDate(), "MMM"));
        yearField.setText(updateViewModel.birthdayInfo.year + "");
        desc.setText(updateViewModel.dateOfBirth.getDescription());
    }

    public void populateDescription(DateOfBirth dateOfBirth) {
        int currentDayInNumber = Integer.parseInt(Util.getStringFromDate(new Date(), Constants.DAY_OF_YEAR));
        int birthdayInNumber = Integer.parseInt(Util.getStringFromDate(dateOfBirth.getDobDate(), Constants.DAY_OF_YEAR));

        int diff = Util.getDefaultDayOfYear(dateOfBirth.getDobDate()) - Util.getDefaultDayOfYear(new Date());
        if(diff < 0) {
            diff = Util.getSubstractionFactor() + diff;
        }

        if(currentDayInNumber == birthdayInNumber) {
            Util.setDescriptionForToday(dateOfBirth);
        }
        else {
            dateOfBirth.setAge(dateOfBirth.getAge() + 1);
            Util.setDescriptionForUpcoming(dateOfBirth, diff);
        }
    }

    public void initLayout() {
        name = (EditText)findViewById(R.id.personName);

        monthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        dateText = findViewById(R.id.date);
        yearText = findViewById(R.id.year);

        removeYear = (CheckBox) findViewById(R.id.removeYear);

        namePreview = (TextView)findViewById(R.id.nameField);
        desc = (TextView)findViewById(R.id.ageField);
        dateField = (TextView)findViewById(R.id.dateField);
        monthField = (TextView)findViewById(R.id.monthField);
        yearField = (TextView)findViewById(R.id.yearField);

        circle = (LinearLayout)findViewById(R.id.circlebg);
        todayImage = (LinearLayout)findViewById(R.id.todayImage);

        mAdView = findViewById(R.id.adView);
    }

    public void update() {
        populateBirthdayInfo();
        updateViewModel.setDateOfBirth(updateViewModel.birthdayInfo);

        if (updateViewModel.isNameEmpty()) {
            //show error
            Toast.makeText(getApplicationContext(), Constants.NAME_EMPTY, Toast.LENGTH_SHORT).show();
        } else {
            if (updateViewModel.isDOBAvailable(updateViewModel.dateOfBirth)) {
                //put confirmation here
                new AlertDialog.Builder(Update.this)
                        //.setIcon(android.R.drawable.ic_dialog_info)
                        // .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setTitle(Constants.ERROR)
                        .setMessage(Constants.USER_EXIST)
                        .setPositiveButton(Constants.OK, null)
                        .show();
            } else {
                Log.i("after update", currentDOB.getName());
                updateViewModel.update();
                Storage.setDbBackupTime(new Date());
                String status = Constants.NOTIFICATION_UPDATE_MEMBER_SUCCESS + ". You will get notified at 12:00am and 12:00pm on " + Util.getStringFromDate(currentDOB.getDobDate(), "dd MMM") + " every year";
                status = Constants.NOTIFICATION_UPDATE_MEMBER_SUCCESS + ". " + Util.getNotificationMessageWithTime(getApplicationContext(), updateViewModel.dateOfBirth.getDobDate());
                Toast toast = Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG);
                toast.show();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    public void delete() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Update.this);
        builder.setTitle("Confirmation")
            .setMessage("Are you sure you want to delete " + currentDOB.getName() + "?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateViewModel.delete(currentDOB.getDobId());
                    Storage.setDbBackupTime(new Date());
                    currentDOB = null;
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.NOTIFICATION_DELETE_MEMBER_SUCCESS, Toast.LENGTH_SHORT);
                    toast.show();
                    setResult(RESULT_OK, intent);
                    clearInputs();
                    finish();
                }
            })
            .setNegativeButton("No", null);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Update.this, R.color.dark_gray));
            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_menu_done: {
                update();
                break;
            }
            case R.id.action_menu_delete: {
                delete();
                break;
            }
            case android.R.id.home: {
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        }
        return true;
    }

    public void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void clearInputs() {
        name.setText("");
    }
}
