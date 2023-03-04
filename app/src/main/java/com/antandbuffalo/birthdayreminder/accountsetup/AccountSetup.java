package com.antandbuffalo.birthdayreminder.accountsetup;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.models.UserPreference;
import com.antandbuffalo.birthdayreminder.models.UserProfile;
import com.antandbuffalo.birthdayreminder.utilities.AutoSyncOptions;
import com.antandbuffalo.birthdayreminder.utilities.AutoSyncService;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.FirebaseHandler;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.UIUtil;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AccountSetup extends AppCompatActivity implements FirebaseHandler {
    UserPreference userPreference;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup);

        DataHolder.getInstance().setAppContext(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Boolean showAlert = (Boolean)getIntent().getSerializableExtra("showAlert");
        if(showAlert != null && showAlert) {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser == null) {
                UIUtil.showAlertWithOk(this, "Reminder", "Please select your google account");
            } else if("none".equalsIgnoreCase(Storage.getAutoSyncFrequency())) {
                UIUtil.showAlertWithOk(this, "Reminder", "Please select auto backup frequency");
            }
        }

        Button selectAccount = findViewById(R.id.selectAccount);
        selectAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFirebaseAuth();
            }
        });

        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationBeforeExit();
            }
        });

        Button selectFrequency = findViewById(R.id.frequency);
        selectFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(AccountSetup.this);
                List<Map<String, String>> optionsList = AutoSyncOptions.getInstance().getValues();
                CharSequence options[] = new CharSequence[optionsList.size()];

                Integer selectedItem = 0;
                for(int i = 0; i < optionsList.size(); i++) {
                    options[i] = optionsList.get(i).get("value");
                    if(optionsList.get(i).get("value").equalsIgnoreCase(Storage.getAutoSyncFrequency())) {
                        selectedItem = i;
                    }
                }
                adb.setSingleChoiceItems(options, selectedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        System.out.println("i = " + i);
                        Storage.setAutoSyncFrequency(optionsList.get(i).get("key"));
                        updateAutoFrequencyUI();
                        Storage.setDbBackupTime(new Date());
                    }
                });
                adb.setPositiveButton("OK", null);
                adb.setNegativeButton("Cancel", null);
                adb.setTitle("Select Sync Frequency");

                androidx.appcompat.app.AlertDialog dialog = adb.create();
                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AccountSetup.this, R.color.dark_gray));
                    }
                });
                dialog.show();
            }
        });
        updateAutoFrequencyUI();
        loadAd();
        showSnowFlakes();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            Log.d("BRJB", item.getItemId() + " : back");
            showConfirmationBeforeExit();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.firebaseSignInCode) {
            handleFirebaseSignIn(resultCode, data);
        }
    }


    public void handleFirebaseSignIn(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            TextView accountName = findViewById(R.id.accountName);
            accountName.setText("Account: " + user.getEmail());
            Storage.setDbBackupTime(new Date());
            updateProfileToFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
            getUserPreferenceFromFirebase();
        } else {
            Toast.makeText(DataHolder.getInstance().getAppContext(), "Not able to sign in", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateProfileToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        // Create a new user with a first and last name
        UserProfile userProfile = Util.getUserProfileFromFirebaseUser(firebaseUser);
        Storage.saveUserProfileToLocal(userProfile);
        DocumentReference documentReference = db.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentProfile);
        showProgressBar();
        documentReference.set(userProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                hideProgressBar();
                Log.d("Success", "Profile updated");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                Log.e("Profile", "Error updating profile: " + e.getLocalizedMessage());
            }
        });
    }

    public void getUserPreferenceFromFirebase() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            return;
        }
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        showProgressBar();
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                hideProgressBar();
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userPreference = Storage.createUserPreferenceFromServer(document.getData());
                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Storage.updateUserPreference(userPreference, alarmManager, getApplicationContext());
                        updateAutoFrequencyUI();
                    } else {
                        Log.d("FirebaseGetData", "No such document");
                    }
                    backupOrRestore();
                } else {
                    Log.d("FirebaseGetData", "get failed with ", task.getException());
                }
            }
        });
    }

    public void backupOrRestore() {
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        AutoSyncService autoSyncService = new AutoSyncService(alarmManager, AccountSetup.this, connectivityManager);
        autoSyncService.firebaseHandler = this;

        if(userPreference != null && userPreference.serverBackupTime != null) {
            // server backup is available. so restore
            autoSyncService.userPreference = userPreference;
            showProgressBar();
            autoSyncService.restoreDateOfBirthsFromFirebase();
            DataHolder.getInstance().refresh = true;
        }
        else {
            // the server data is null. not backed up till now
            if(DateOfBirthDBHelper.selectAll().size() == 0) {
                // first time install and local db is empty. just return
                return;
            }
            // local db is not empty. backup to server
            Storage.setDbBackupTime(new Date());
            autoSyncService.backupDateOfBirthsToFirebase();
        }
    }

    public void updateAutoFrequencyUI() {
        TextView localBackup = findViewById(R.id.syncFrequency);
        for(int i=0; i < AutoSyncOptions.getInstance().getValues().size(); i++) {
            if(AutoSyncOptions.getInstance().getValues().get(i).get("key").equalsIgnoreCase(Storage.getAutoSyncFrequency())) {
                localBackup.setText("Auto Sync Frequency: " + AutoSyncOptions.getInstance().getValues().get(i).get("value"));
                break;
            }
        }
    }

    public void startFirebaseAuth() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                Constants.firebaseSignInCode);
    }

    public void showProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progresBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progresBar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCompleteDateOfBirthSync(Task<DocumentSnapshot> task) {
        System.out.println("onCompleteDateOfBirthSync");
        hideProgressBar();
        UIUtil.showAlertWithOk(AccountSetup.this, "Success", "Account data restored successfully");
    }

    public void loadAd() {
        mAdView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void showConfirmationBeforeExit() {
        String message = "Are you sure want to continue without setting up your Account and Auto Sync frequency?";
        int error = 0;
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            message = "Are you sure want to continue without setting up your Account?";
            error++;
        }
        if(Storage.getAutoSyncFrequency().equalsIgnoreCase("none")) {
            message = "Are you sure want to continue without setting up Auto Sync Frequency?";
            error++;
        }
        if(error == 2) {
            message = "Are you sure want to continue without setting up your Account and Auto Sync frequency?";
        }
        if(error == 0) {
            Storage.setFirstTimeLaunch();
            Storage.setLastAccSetupShownDate(new Date());
            finish();
            return;
        }

        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(AccountSetup.this);
        alertDialogBuilder.setTitle("Confirmation")
                .setMessage(message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Storage.setFirstTimeLaunch();
                        Storage.setLastAccSetupShownDate(new Date());
                        finish();
                    }
                })
                .setNegativeButton("No", null);

        androidx.appcompat.app.AlertDialog dialog = alertDialogBuilder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(AccountSetup.this, R.color.dark_gray));
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        showConfirmationBeforeExit();
    }

    public void showSnowFlakes() {
        if(Util.showSnow()) {
            View snowFlakes = this.findViewById(R.id.snowFlakes);
            snowFlakes.setVisibility(View.VISIBLE);
        }
    }
}

// notify parent after sync to refresh screen