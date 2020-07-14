package com.antandbuffalo.birthdayreminder.backup;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.models.UserProfile;
import com.antandbuffalo.birthdayreminder.settings.Settings;
import com.antandbuffalo.birthdayreminder.utilities.AutoSyncOptions;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.FirebaseHandler;
import com.antandbuffalo.birthdayreminder.utilities.FirebaseUtil;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Backup extends AppCompatActivity implements FirebaseHandler {
    AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ProgressBar progressBar = findViewById(R.id.progresBar);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d("FirebaseUser", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            TextView textView = findViewById(R.id.accountName);
            textView.setText("Account: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
            getFirebaseLastUpdatedTime(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser(), "updateUI");
        }
        else {
            Log.d("FirebaseError", "User Not found. Please login first");
            startFirebaseAuth();
        }

        Button backupNow = findViewById(R.id.backupNow);
        backupNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getStoragePermission(Constants.MY_PERMISSIONS_READ_WRITE)) {
                    updateLocalBackup();
                }
                else {
                    Toast.makeText(DataHolder.getInstance().getAppContext(), "Please provide permission to access local storage", Toast.LENGTH_SHORT).show();
                }
                if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                    backupToFirebaseConfirmation();
                }
                else {
                    UIUtil.showAlertWithOk(Backup.this, "Error", "Please select account to backup");
                }
            }
        });

        Button restoreNow = findViewById(R.id.restoreNow);
        restoreNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restoreDateOfBirthsFromFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
                restoreUserPreferenceFromFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
            }
        });

        Button selectAccount = findViewById(R.id.selectAccount);
        selectAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFirebaseAuth();
            }
        });

        Button removeAccount = findViewById(R.id.removeAccount);
        removeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAccountConfirmation();
            }
        });

        Button selectFrequency = findViewById(R.id.frequency);
        selectFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder adb = new androidx.appcompat.app.AlertDialog.Builder(Backup.this);
                //CharSequence items[] = new CharSequence[] {AutoSyncFrequency.NONE.getFrequency(), AutoSyncFrequency.DAILY.name(), AutoSyncFrequency.WEEKLY.name(), AutoSyncFrequency.MONTHLY.name()};
                List<Map<String, String>> optionsList = AutoSyncOptions.getInstance().getValues();
                CharSequence options[] = new CharSequence[optionsList.size()];

                Integer selectedItem = 0;
                for(int i = 0; i < optionsList.size(); i++) {
                    options[i] = optionsList.get(i).get("value");
                    if(optionsList.get(i).get("key").equalsIgnoreCase(Storage.getAutoSyncFrequency())) {
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
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Backup.this, R.color.dark_gray));
                    }
                });
                dialog.show();
            }
        });
        updateBackupTimeUI();
        updateAutoFrequencyUI();
        loadAd();
    }

    public void updateLocalBackup() {
        File file = Util.writeToFile(this);
        if(file != null) {
            Storage.setDbBackupTime(new Date(file.lastModified()));
        }
        updateBackupTimeUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("BRJB", item.getItemId() + "");
        int id = item.getItemId();
        if(id == android.R.id.home) {
            Log.d("BRJB", item.getItemId() + " : back");
        }
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.firebaseSignInCode) {
            handleFirebaseSignIn(resultCode, data);
        }
    }

    public Boolean getStoragePermission(int permissionType) {
        switch (permissionType) {
            case Constants.MY_PERMISSIONS_READ_WRITE: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
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

    public void readWritePermission(Boolean isGranted) {
        if(isGranted) {
            updateLocalBackup();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_READ_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    readWritePermission(true);

                } else {
                    // permission denied, boo! Disable the
                    readWritePermission(false);
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void handleFirebaseSignIn(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            System.out.println(user.getDisplayName());
            System.out.println(user.getEmail());
            System.out.println(user.getPhoneNumber());
            System.out.println(user.getProviderId());
            System.out.println(user.getUid());
            TextView accountName = findViewById(R.id.accountName);
            accountName.setText("Account: " + user.getEmail());
            updateProfileToFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
            Storage.setDbBackupTime(new Date());

            showProgressBar();
            FirebaseUtil.restoreDateOfBirthAndUserPreferenceFromFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser(), Backup.this);
        } else {
            Toast.makeText(DataHolder.getInstance().getAppContext(), "Not able to sign in", Toast.LENGTH_SHORT).show();
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

    public void signoutFromFirebase() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    TextView accountName = findViewById(R.id.accountName);
                    accountName.setText("Account: ");
                    Storage.setServerBackupTime(null);
                    TextView serverBackup = findViewById(R.id.cloudBackup);
                    serverBackup.setText("Server: ");
                }
            });
    }

    public void backupDateOfBirthsToFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        // Create a new user with a first and last name
        showProgressBar();
        Map<String, DateOfBirth> dateOfBirthMap = Util.getDateOfBirthMap();
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentFriends);
        documentReference.set(dateOfBirthMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                hideProgressBar();
                Log.d("Success", "data updated successfully");
                //updateLastUpdatedTime(db, firebaseUser);
                Storage.setServerBackupTime(Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore));
                backupUserPreferenceToFirebase(firebaseFirestore, firebaseUser);
                Toast.makeText(DataHolder.getInstance().getAppContext(), "Birthday informations uploaded successfully", Toast.LENGTH_SHORT).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                Log.e("F", e.getLocalizedMessage());
                Toast.makeText(DataHolder.getInstance().getAppContext(), "Error while updating to server", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void backupUserPreferenceToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        showProgressBar();
        DocumentReference documentReference = db.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        documentReference.set(Storage.getUserPreference()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                hideProgressBar();
                Log.d("Success", "Preferences updated successfully");
                Toast.makeText(DataHolder.getInstance().getAppContext(), "Your preferences uploaded successfully", Toast.LENGTH_SHORT).show();
                updateBackupTimeUI();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressBar();
                Log.e("F", e.getLocalizedMessage());
                Toast.makeText(DataHolder.getInstance().getAppContext(), "Error while updating to server", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void updateProfileToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        // Create a new user with a first and last name
        UserProfile userProfile = Util.getUserProfileFromFirebaseUser(firebaseUser);
        DocumentReference documentReference = db.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentProfile);
        documentReference.set(userProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Success", "Profile updated");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Profile", "Error updating profile: " + e.getLocalizedMessage());
            }
        });
    }

    public void restoreDateOfBirthsFromFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        showProgressBar();
        FirebaseUtil.restoreDateOfBirthsFromFirebase(firebaseFirestore, firebaseUser, Backup.this);
    }

    public void restoreUserPreferenceFromFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        showProgressBar();
        FirebaseUtil.restoreUserPreferenceFromFirebase(firebaseFirestore, firebaseUser, Backup.this);
    }

    public void updateLastUpdatedTime(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        Map<String, Date> updateTime = new HashMap<>();
        updateTime.put(Constants.serverBackupTime, Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore));
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        documentReference.set(updateTime).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Success", "Last updated time updated");
                Storage.setServerBackupTime(Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore));
                TextView cloudBackup = findViewById(R.id.cloudBackup);
                cloudBackup.setText("Server: " + Storage.getServerBackupTime());
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("F", e.getLocalizedMessage());
            }
        });
    }

    public void getFirebaseLastUpdatedTime(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser, String caller) {
        if(firebaseUser == null) {
            return;
        }
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Timestamp serverTimestamp = (Timestamp) documentSnapshot.get(Constants.serverBackupTime);

                if(serverTimestamp != null) {
                    String formattedDate = Util.getStringFromDate(serverTimestamp.toDate(), Constants.backupDateFormatToStore);
                    Storage.putString(Constants.serverBackupTime, formattedDate);
                    switch (caller) {
                        case "updateUI": {
                            updateBackupTimeUI();
                            break;
                        }
                        case "saveLocally": {
                            break;
                        }
                    }
                }
                if(caller.equalsIgnoreCase("syncNow")) {
                    compareBackupTimes();
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("ERROR", "Not able to get last updated time");
            }
        });
    }


    public void getFirebaseLastUpdatedTime(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        getFirebaseLastUpdatedTime(firebaseFirestore, firebaseUser, "");
    }

    public void backupToFirebaseConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(Backup.this);
        alertDialogBuilder.setTitle("Confirmation")
            .setMessage("This action will replace existing server content with local content. Are you sure want to continue?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    backupDateOfBirthsToFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
                }
            })
            .setNegativeButton("No", null);
        androidx.appcompat.app.AlertDialog dialog = alertDialogBuilder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Backup.this, R.color.dark_gray));
            }
        });
        dialog.show();
    }

    public void syncNow() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getAllNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            getFirebaseLastUpdatedTime(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser(), "syncNow");
        }
        else {
            Toast.makeText(DataHolder.getInstance().getAppContext(), "Please connect to Internet", Toast.LENGTH_LONG).show();
        }
    }

    public void compareBackupTimes() {
        Date dbBackupDate = Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore);
        Date serverBackupDate = Util.getDateFromString(Storage.getServerBackupTime(), Constants.backupDateFormatToStore);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(serverBackupDate == null) {
            backupDateOfBirthsToFirebase(FirebaseFirestore.getInstance(), firebaseUser);
            return;
        }
        if(dbBackupDate == null) {
            restoreDateOfBirthsFromFirebase(FirebaseFirestore.getInstance(), firebaseUser);
            return;
        }
        if(dbBackupDate.getTime() == serverBackupDate.getTime()) {
            return;
        }
        if(dbBackupDate.getTime() > serverBackupDate.getTime()) {
            // Local data is latest. Upload to server
            backupDateOfBirthsToFirebase(FirebaseFirestore.getInstance(), firebaseUser);
        }
        else {
            // Server data is latest. Download
            restoreDateOfBirthsFromFirebase(FirebaseFirestore.getInstance(), firebaseUser);
        }
    }

    public void updateBackupTimeUI() {
        TextView localBackup = findViewById(R.id.localBackup);
        localBackup.setText("Local: " + Storage.getDbBackupTime());

        TextView serverBackup = findViewById(R.id.cloudBackup);
        serverBackup.setText("Server: " + Storage.getServerBackupTime());
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

    public void showProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progresBar);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progresBar);
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void loadAd() {
        mAdView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void removeAccountConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(Backup.this);
        alertDialogBuilder.setTitle("Confirmation")
                .setMessage("Are you sure want to remove your account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        signoutFromFirebase();
                    }
                })
                .setNegativeButton("No", null);
        androidx.appcompat.app.AlertDialog dialog = alertDialogBuilder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Backup.this, R.color.dark_gray));
            }
        });
        dialog.show();
    }

    public void autoRestoreConfirmation(Task<DocumentSnapshot> dobTask, Task<DocumentSnapshot> preferenceTask) {
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(Backup.this);
        alertDialogBuilder.setTitle("Confirmation")
                .setMessage("We found a backup for this account. Do you want to restore now? You can always restore later using the Restore option")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postProcessDateOfBirths(dobTask);
                        postProcessUserPreference(preferenceTask);
                    }
                })
                .setNegativeButton("No", null);
        androidx.appcompat.app.AlertDialog dialog = alertDialogBuilder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(Backup.this, R.color.dark_gray));
            }
        });
        dialog.show();
    }

    public void postProcessDateOfBirths(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                Log.d("FirebaseGetData", "DocumentSnapshot data: " + document.getData());
                Util.inserDateOfBirthFromServer(document.getData());
                DataHolder.getInstance().refresh = true;
                Toast.makeText(Backup.this, "Successfully Restored Birthday Informations from server", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("FirebaseGetData", "No such document");
            }
        } else {
            Log.d("FirebaseGetData", "get failed with ", task.getException());
        }
    }

    @Override
    public void onCompleteDateOfBirthSync(Task<DocumentSnapshot> task) {
        hideProgressBar();
        postProcessDateOfBirths(task);
    }

    public void postProcessUserPreference(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                Log.d("FirebaseGetData", "DocumentSnapshot data: " + document.getData());
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Storage.updateUserPreference(Storage.createUserPreferenceFromServer(document.getData()), alarmManager, getApplicationContext());
                updateBackupTimeUI();
                updateAutoFrequencyUI();
                Util.applyTheme();
                DataHolder.getInstance().refreshSettings = true;
                Toast.makeText(Backup.this, "Successfully Restored your preferences from server", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("FirebaseGetData", "No such document");
            }
        } else {
            Log.d("FirebaseGetData", "get failed with ", task.getException());
        }
    }

    @Override
    public void onCompleteUserPreferenceSync(Task<DocumentSnapshot> task) {
        hideProgressBar();
        postProcessUserPreference(task);
    }

    @Override
    public void onCompleteDateOfBirthUserPreferenceSync(Task<DocumentSnapshot> dobTask, Task<DocumentSnapshot> preferenceTask) {
        if(dobTask != null && preferenceTask != null) {
            hideProgressBar();
            autoRestoreConfirmation(dobTask, preferenceTask);
        }
    }

    // download prgress link
    // https://developers.google.com/drive/android/files

    // use firebase instead of app data
    // https://ammar.lanui.online/integrate-google-drive-rest-api-on-android-app-bc4ddbd90820

}
