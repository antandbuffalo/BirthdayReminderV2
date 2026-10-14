package com.antandbuffalo.birthdayreminder.update;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.sharewish.ShareWish;
import com.antandbuffalo.birthdayreminder.upcoming.UpcomingListAdapter;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DeleteDialog;
import com.antandbuffalo.birthdayreminder.utilities.DuplicateDialog;
import com.antandbuffalo.birthdayreminder.utilities.ReviewPrompt;
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
    int currentDayOfYear, recentDayOfYear;
    TextView namePreview, desc, dateHint;
    View yearBox, previewRow;
    Spinner monthSpinner;
    EditText dateText, yearText;
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
            yearBox.setVisibility(View.INVISIBLE);
        }
        else {
            yearBox.setVisibility(View.VISIBLE);
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
                    yearBox.setVisibility(View.INVISIBLE);
                }
                else {
                    yearBox.setVisibility(View.VISIBLE);
                }
                addMonthsToSpinner(monthSpinner);
                monthSpinner.setSelection(updateViewModel.getSelectedMonthPosition());
                preview();
            }
        });

        intent = new Intent();
        showSnowFlakes();

        findViewById(R.id.sendWish).setOnClickListener(view -> startActivity(new Intent(this, ShareWish.class)));
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
        dateHint.setVisibility(View.GONE);
        yearText.setActivated(false);
        if(!updateViewModel.isValidDateOfBirth(updateViewModel.birthdayInfo)) {
            if(updateViewModel.birthdayInfo.name == null || ("".equalsIgnoreCase(updateViewModel.birthdayInfo.name))) {
                namePreview.setText("Enter Name");
                desc.setText("Age");
            } else {
                namePreview.setText("Error in birthdate");
                desc.setText("Please enter valid date");
                dateHint.setText("Not a valid date");
                dateHint.setVisibility(View.VISIBLE);
            }
            return;
        }
        populateBirthdayInfo();
        updateViewModel.setDateOfBirth(updateViewModel.birthdayInfo);
        DateOfBirth dob = updateViewModel.dateOfBirth;
        // Show the age they are turning, like the list does
        if(Util.getDayOfYear(dob.getDobDate()) != currentDayOfYear) {
            dob.setAge(dob.getAge() + 1);
        }
        UpcomingListAdapter.bindRow(previewRow, dob, currentDayOfYear, recentDayOfYear);
        if(updateViewModel.birthdayInfo.name.trim().isEmpty()) {
            namePreview.setText("Enter Name");
        }

        boolean isFuture = !dob.getRemoveYear() && dob.getDobDate().after(new Date());
        if(isFuture) {
            dateHint.setText("Date is in the future");
            dateHint.setVisibility(View.VISIBLE);
            yearText.setActivated(true);
        }
        desc.setVisibility(isFuture ? View.INVISIBLE : View.VISIBLE);
    }

    public void initLayout() {
        name = (EditText)findViewById(R.id.personName);

        monthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        dateText = findViewById(R.id.todayBDate);
        yearText = findViewById(R.id.year);

        removeYear = (CheckBox) findViewById(R.id.removeYear);

        namePreview = (TextView)findViewById(R.id.todayNameField);
        desc = (TextView)findViewById(R.id.ageField);
        dateHint = findViewById(R.id.dateHint);
        yearBox = findViewById(R.id.yearBox);
        previewRow = findViewById(R.id.previewRow);

    }

    public void update() {
        populateBirthdayInfo();
        updateViewModel.setDateOfBirth(updateViewModel.birthdayInfo);

        if (updateViewModel.isNameEmpty()) {
            //show error
            Toast.makeText(getApplicationContext(), Constants.NAME_EMPTY, Toast.LENGTH_SHORT).show();
        } else {
            DateOfBirth duplicate = updateViewModel.findDuplicate(updateViewModel.dateOfBirth);
            if (duplicate != null) {
                DuplicateDialog.show(this, duplicate, currentDayOfYear, recentDayOfYear);
            } else {
                Log.i("after update", currentDOB.getName());
                updateViewModel.update();
                Storage.setDbBackupTime(new Date());
                String status = Constants.NOTIFICATION_UPDATE_MEMBER_SUCCESS + ". You will get notified at 12:00am and 12:00pm on " + Util.getStringFromDate(currentDOB.getDobDate(), "dd MMM") + " every year";
                status = Constants.NOTIFICATION_UPDATE_MEMBER_SUCCESS + ". " + Util.getNotificationMessageWithTime(getApplicationContext(), updateViewModel.dateOfBirth.getDobDate());
                Toast toast = Toast.makeText(getApplicationContext(), status, Toast.LENGTH_LONG);
                toast.show();
                ReviewPrompt.markPositiveMoment();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    public void delete() {
        DeleteDialog.show(this, currentDOB, currentDayOfYear, recentDayOfYear, () -> {
            updateViewModel.delete(currentDOB.getDobId());
            Storage.setDbBackupTime(new Date());
            Toast.makeText(getApplicationContext(), currentDOB.getName() + " deleted", Toast.LENGTH_SHORT).show();
            currentDOB = null;
            setResult(RESULT_OK, intent);
            clearInputs();
            finish();
        });
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
        mAdView = findViewById(R.id.adView);
        if(!Constants.enableAds) {
            mAdView.setVisibility(View.INVISIBLE);
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void clearInputs() {
        name.setText("");
    }

    public void showSnowFlakes() {
        if(Util.showSnow()) {
            View snowFlakes = this.findViewById(R.id.snowFlakes);
            snowFlakes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    public void onPause() {
        // Pause the AdView.
        mAdView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Destroy the AdView.
        mAdView.destroy();
        super.onDestroy();
    }
}
