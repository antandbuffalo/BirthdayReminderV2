package com.antandbuffalo.birthdayreminder.accountsetup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.backup.Backup;
import com.antandbuffalo.birthdayreminder.utilities.AutoSyncOptions;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountSetup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_setup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button selectAccount = findViewById(R.id.selectAccount);
        selectAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFirebaseAuth();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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


    public void handleFirebaseSignIn(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (resultCode == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            TextView accountName = findViewById(R.id.accountName);
            accountName.setText("Account: " + user.getEmail());
            updateProfileToFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
        } else {
            Toast.makeText(DataHolder.getInstance().getAppContext(), "Not able to sign in", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateProfileToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        // Create a new user with a first and last name
        Map<String, String> userProfile = new HashMap<>();
        userProfile.put("uid", firebaseUser.getUid());
        userProfile.put("displayName", firebaseUser.getDisplayName());
        userProfile.put("email", firebaseUser.getEmail());
        userProfile.put("providerId", firebaseUser.getProviderId());

        DocumentReference documentReference = db.collection(firebaseUser.getUid()).document("profile");
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
}
