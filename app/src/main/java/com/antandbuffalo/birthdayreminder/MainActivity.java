package com.antandbuffalo.birthdayreminder;

import static com.antandbuffalo.birthdayreminder.utilities.Constants.WEB_URL;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.antandbuffalo.birthdayreminder.utilities.FirebaseUtil;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.ThemeOptions;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Objects;
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
        fab.setOnClickListener(view -> {
            showSnowFlakes();
            Intent intent = new Intent(getApplicationContext(), AddNew.class);
            startActivityForResult(intent, Constants.ADD_NEW_MEMBER);
        });

        // Util.copyFromAssetFileToDatabase("cse.txt");

        upcomingListAdapter = new UpcomingListAdapter();
        //http://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        ListView upcomingListView = findViewById(R.id.upcomingListView);
        upcomingListView.setAdapter(upcomingListAdapter);

        upcomingListView.setOnItemClickListener((parent, view, position, id) -> {
            DateOfBirth dateOfBirth = upcomingListAdapter.getItem(position);
            AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
            CharSequence[] items = new CharSequence[]{"Edit", "Wish", "Delete"};
            adb.setItems(items, (dialogInterface, i) -> {
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
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    DateOfBirthDBHelper.deleteRecordForTheId(dateOfBirth.getDobId());
                                    Storage.setDbBackupTime(new Date());
                                    Toast toast = Toast.makeText(getApplicationContext(), Constants.NOTIFICATION_DELETE_MEMBER_SUCCESS, Toast.LENGTH_SHORT);
                                    toast.show();
                                    upcomingListAdapter.refreshData();
                                })
                                .setNegativeButton("No", null);
                        AlertDialog dialog = builder.create();
                        dialog.setOnShowListener(arg0 -> dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(MainActivity.this, R.color.dark_gray)));
                        dialog.show();
                        break;
                    }
                }
            });
            //adb.setNegativeButton("Cancel", null);
            adb.setTitle(dateOfBirth.getName());
            adb.show();
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
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
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
//        THIS IS USED TO DISPLAY CUSTOM NOTIFICATION ON SPECIFIC DAY
//        if (Util.showHappyBirthdayIconAndView()) {
//            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//             Util.setHappyBirthdayAlarm(this, alarmManager, 0, 0);
//        }
        launchAccountSetup();
        loadAd();
        showSnowFlakes();
        updateProfile();
        storeBuildNumber();
    }

    public void updateProfile() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseUtil.saveUserProfileToFirebase(FirebaseFirestore.getInstance(), firebaseUser);
        }
    }

    public void applyTheme() {
        if (Storage.getTheme().equalsIgnoreCase(ThemeOptions.KEY_DEFAULT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (Storage.getTheme().equalsIgnoreCase(ThemeOptions.KEY_LIGHT)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
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
        if (Storage.isFirstTimeLaunch()) {
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

    // used to display account setup page when user opens after 30 days
//    public void checkAccountSetup() {
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (!("none".equalsIgnoreCase(Storage.getAutoSyncFrequency())) && firebaseUser != null) {
//            return;
//        }
//
//        Intent intent = new Intent(MainActivity.this, AccountSetup.class);
//        intent.putExtra("showAlert", !"none".equalsIgnoreCase(Storage.getAutoSyncFrequency()) || firebaseUser != null);
//        startActivity(intent);
//    }

    public void startUpdate(int position) {
        DateOfBirth dateOfBirth = upcomingListAdapter.getItem(position);

        Intent intent = new Intent(MainActivity.this, Update.class);
        intent.putExtra("currentDOB", dateOfBirth);
        startActivityForResult(intent, Constants.DELETE_MEMBER);
    }

//    used for debugging
//    public FirebaseFirestore initFirebase() {
//        return FirebaseFirestore.getInstance();
//    }

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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_add_new: {
                Intent intent = new Intent(this, AddNew.class);
                startActivityForResult(intent, Constants.ADD_NEW_MEMBER);
                return true;
            }
            case R.id.action_settings: {
                Intent settings = new Intent(this, Settings.class);
                startActivity(settings);
                return true;
            }
            case R.id.action_web: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WEB_URL));
                startActivity(browserIntent);
                return true;
            }
            case R.id.action_about: {
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.firebaseSignInCode) {
            // used for debugging
//            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
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
                }
            } else {
                Toast.makeText(DataHolder.getInstance().getAppContext(), "Not able to sign in", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.DELETE_MEMBER) {
            System.out.println("after delete activity");
            if (resultCode == RESULT_OK) {
                refreshList();
            }
        }
    }

//    used for debugging store permission related details
//    public Boolean getStoragePermission(int permissionType) {
//        switch (permissionType) {
//            case Constants.MY_PERMISSIONS_READ_WRITE: {
//                if (ContextCompat.checkSelfPermission(this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                            Constants.MY_PERMISSIONS_READ_WRITE);
//
//                    return false;
//                } else {
//                    // Permission has already been granted
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DataHolder.getInstance().refresh) {
            DataHolder.getInstance().refresh = false;
            refreshList();
        }
        updateProfile();

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

    public void refreshList() {
        upcomingListAdapter.refreshData();
        //EditText filter = findViewById(R.id.upcomingFiler);
        TextInputEditText filter = findViewById(R.id.filter);
        String filterText = Objects.requireNonNull(filter.getText()).toString();
        if (!filterText.equalsIgnoreCase("")) {
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
        if (!Constants.enableAds) {
            mAdView.setVisibility(View.INVISIBLE);
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void showSnowFlakes() {
        if (Util.showSnow()) {
            View snowFlakes = this.findViewById(R.id.snowFlakes);
            snowFlakes.setVisibility(View.VISIBLE);
        }
        if (Util.showHappyBirthdayIconAndView()) {
            Util.showHappyBirthdayNotification(this);
        }
    }

    public void storeBuildNumber() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Storage.setLastBuildNumber(BuildConfig.VERSION_CODE);
            }
        }).start();
    }

    public void showRatingReview() {
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    Storage.setRatingPresentedDate(new Date());
                    finish();
                });
            } else {
                // There was some problem, log or handle the error code.
//                    @ReviewErrorCode int reviewErrorCode = task.getException().getErrorCode();
                Storage.setRatingPresentedDate(new Date());
                System.out.println("error while presenting rating review" + task.getException().getLocalizedMessage());
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        long defaultDate = Util.getDateFromString("12/12/2012", Constants.LAST_APP_OPEN_DATE_FORMAT).getTime();
        long oldDate = Storage.getRatingPresentedDate().getTime();
        long diff = new Date().getTime() - oldDate;
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        if (upcomingListAdapter.getCount() >= Constants.DOB_COUNT_TO_SHOW_RATING && (oldDate == defaultDate || days >= Constants.DAYS_TO_SHOW_RATING_30)) {
            showRatingReview();
        } else if (upcomingListAdapter.getCount() < Constants.DOB_COUNT_TO_SHOW_RATING && (oldDate == defaultDate || days >= Constants.DAYS_TO_SHOW_RATING_60)) {
            showRatingReview();
        } else {
            super.onBackPressed();
        }
    }

    // will use this to show and hide snowflakes on christmas day
//    public void hideSnowFlakes() {
//        View snowFlakes = this.findViewById(R.id.snowFlakes);
//        snowFlakes.setVisibility(View.INVISIBLE);
//    }

}

// Get all documents
// https://firebase.google.com/docs/firestore/query-data/get-data#java_2
// add document with custom id
// https://stackoverflow.com/questions/48541270/how-to-add-document-with-custom-id-to-firestore
// auth rules
// https://firebase.google.com/docs/rules/basics?authuser=0#cloud-firestore
// checking user
// https://firebase.google.com/docs/auth/android/manage-users?authuser=0