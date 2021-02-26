package com.antandbuffalo.birthdayreminder;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.about.About;
import com.antandbuffalo.birthdayreminder.accountsetup.AccountSetup;
import com.antandbuffalo.birthdayreminder.addnew.AddNew;
import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.settings.Settings;
import com.antandbuffalo.birthdayreminder.sharewish.ShareWish;
import com.antandbuffalo.birthdayreminder.upcoming.UpcomingListAdapter;
import com.antandbuffalo.birthdayreminder.update.Update;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.ThemeOptions;
import com.antandbuffalo.birthdayreminder.utilities.UIUtil;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    UpcomingListAdapter upcomingListAdapter;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // https://blog.prototypr.io/implementing-dark-theme-in-android-dfe63e62145d
        // https://developer.android.com/guide/topics/ui/look-and-feel/darktheme#java
        // https://medium.com/androiddevelopers/appcompat-v23-2-daynight-d10f90c83e94
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initValues();

        applyTheme();
        checkTheme();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnowFlakes();
                Intent intent = new Intent(getApplicationContext(), AddNew.class);
                startActivityForResult(intent, Constants.ADD_NEW_MEMBER);
            }
        });

        // Util.copyFromAssetFileToDatabase("cse.txt");

        upcomingListAdapter = new UpcomingListAdapter();
        //http://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        ListView upcomingListView = (ListView)findViewById(R.id.upcomingListView);
        upcomingListView.setAdapter(upcomingListAdapter);

        upcomingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DateOfBirth dateOfBirth = upcomingListAdapter.getItem(position);
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                CharSequence items[] = new CharSequence[] {"Edit", "Wish", "Delete"};
                adb.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                startUpdate(position);
                                break;
                            case 1:
                                Intent shareWish = new Intent(MainActivity.this, ShareWish.class);
                                startActivity(shareWish);
                                break;
                            case 2: {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Confirmation")
                                    .setMessage("Are you sure you want to delete " + dateOfBirth.getName() + "?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DateOfBirthDBHelper.deleteRecordForTheId(dateOfBirth.getDobId());
                                            Storage.setDbBackupTime(new Date());
                                            Toast toast = Toast.makeText(getApplicationContext(), Constants.NOTIFICATION_DELETE_MEMBER_SUCCESS, Toast.LENGTH_SHORT);
                                            toast.show();
                                            upcomingListAdapter.refreshData();
                                        }
                                    })
                                    .setNegativeButton("No", null);
                                AlertDialog dialog = builder.create();
                                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface arg0) {
                                        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.dark_gray));
                                    }
                                });
                                dialog.show();
                                break;
                            }
                        }
                    }
                });
                //adb.setNegativeButton("Cancel", null);
                adb.setTitle(dateOfBirth.getName());
                adb.show();
            }
        });

        TextInputEditText filter = findViewById(R.id.filter);

        //EditText filter = (EditText)findViewById(R.id.upcomingFiler);
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                //upcomingListAdapter.getFilter().filter(cs);
                upcomingListAdapter.filter(cs.toString());
                upcomingListAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });
//        FirebaseUser firebaseUser = checkFirebaseUser();
//        if(firebaseUser != null) {
//            // addUserToFirebase(initFirebase(), firebaseUser);
//            getAllDocuments(initFirebase(), firebaseUser);
//        }
//        else {
//            Log.d("FirebaseError", "User Not found. Please login first");
//        }

        // startFirebaseAuth();
        setRepeatingAlarm();
        launchAccountSetup();
        loadAd();
        showSnowFlakes();
    }

    public void applyTheme() {
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

    public void checkTheme() {
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        System.out.println("Mode - " + currentNightMode);
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                break;
        }
    }

    public void launchAccountSetup() {
        if(Storage.isFirstTimeLaunch()) {
            // backup the older version of dob before upgrade to v2
            Util.writeToFile(this, "v2");
            Intent intent = new Intent(MainActivity.this, AccountSetup.class);
            startActivity(intent);
        }
        // this needs to be properly tested before enabling. To fix a crash issue in prod, currently commenting this out.
//        else {
//            long oldDate = Storage.getLastAccSetupShownDate().getTime();
//            long diff = new Date().getTime() - oldDate;
//            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//            if(days > 30) {
//                checkAccountSetup();
//            }
//        }
    }

    public void checkAccountSetup() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(!("none".equalsIgnoreCase(Storage.getAutoSyncFrequency())) && firebaseUser != null) {
            return;
        }

        Intent intent = new Intent(MainActivity.this, AccountSetup.class);
        if("none".equalsIgnoreCase(Storage.getAutoSyncFrequency()) && firebaseUser == null) {
            intent.putExtra("showAlert", false);
        }
        else {
            intent.putExtra("showAlert", true);
        }
        startActivity(intent);
    }

    public void startUpdate(int position) {
        DateOfBirth dateOfBirth = upcomingListAdapter.getItem(position);

        Intent intent = new Intent(MainActivity.this, Update.class);
        intent.putExtra("currentDOB", dateOfBirth);
        startActivityForResult(intent, Constants.DELETE_MEMBER);
    }

    public FirebaseFirestore initFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db;
    }

    public void initValues() {
        DBHelper.createInstance(this);
        DataHolder.getInstance().setAppContext(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, Settings.class);
            startActivity(settings);
            return true;
        }
        else if(id == R.id.action_about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == Constants.firebaseSignInCode) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                System.out.println(user.getDisplayName());
                System.out.println(user.getEmail());
                System.out.println(user.getPhoneNumber());
                System.out.println(user.getProviderId());
                System.out.println(user.getUid());

                Log.d("FirebaseAuth", user.getDisplayName());
                Log.d("FirebaseAuth", user.getEmail());
                Log.d("FirebaseAuth", user.getPhoneNumber() + " :a");
                Log.d("FirebaseAuth", user.getProviderId());
                Log.d("FirebaseAuth", user.getUid());

            } else {
                Toast.makeText(DataHolder.getInstance().getAppContext(), "Not able to sign in", Toast.LENGTH_SHORT).show();
            }
         }
         else if(requestCode == Constants.DELETE_MEMBER) {
             System.out.println("after delete activity");
             if(resultCode == RESULT_OK) {
                 refreshList();
             }
         }
    }

    public Boolean getStoragePermission(int permissionType) {
        switch (permissionType) {
            case Constants.MY_PERMISSIONS_READ_WRITE: {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Constants.MY_PERMISSIONS_READ_WRITE);

                    return false;
                } else {
                    // Permission has already been granted
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(DataHolder.getInstance().refresh) {
            DataHolder.getInstance().refresh = false;
            refreshList();
        }
    }

    public void refreshList() {
        upcomingListAdapter.refreshData();
        //EditText filter = findViewById(R.id.upcomingFiler);
        TextInputEditText filter = findViewById(R.id.filter);
        String filterText = filter.getText().toString();
        if(!filterText.equalsIgnoreCase("")) {
            upcomingListAdapter.filter(filterText);
        }
    }

    public void setRepeatingAlarm() {
        Log.i("MAIN", "Setting repeating alarm in main activity");
        SharedPreferences settings = Util.getSharedPreference();
        int hour = Storage.getInt(settings, Constants.PREFERENCE_NOTIFICATION_TIME_HOURS, 0);
        int minute = Storage.getInt(settings, Constants.PREFERENCE_NOTIFICATION_TIME_MINUTES, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int frequency = Storage.getNotificationFrequency();
        Util.setRepeatingAlarm(this, alarmManager, hour, minute, frequency);
    }

    public void loadAd() {
        mAdView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void showSnowFlakes() {
        if(Util.isHappyBirthday()) {
            View snowFlakes = this.findViewById(R.id.snowFlakes);
            snowFlakes.setVisibility(View.VISIBLE);
            Util.showHappyBirthdayNotification(this);
        }
    }

    public void hideSnowFlakes() {
        View snowFlakes = this.findViewById(R.id.snowFlakes);
        snowFlakes.setVisibility(View.INVISIBLE);
    }

}

// TODO:
// Show confirmation for remove account
//

// Get all documents
// https://firebase.google.com/docs/firestore/query-data/get-data#java_2
// add document with custom id
// https://stackoverflow.com/questions/48541270/how-to-add-document-with-custom-id-to-firestore
// auth rules
// https://firebase.google.com/docs/rules/basics?authuser=0#cloud-firestore
// checking user
// https://firebase.google.com/docs/auth/android/manage-users?authuser=0