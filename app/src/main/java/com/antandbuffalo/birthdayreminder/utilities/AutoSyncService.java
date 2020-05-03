package com.antandbuffalo.birthdayreminder.utilities;

import android.app.AlarmManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.models.UserPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Map;

public class AutoSyncService {
    public UserPreference userPreference;
    private AlarmManager alarmManager;
    private Context context;
    private ConnectivityManager connectivityManager;

    public AutoSyncService(AlarmManager alarmManager, Context context, ConnectivityManager connectivityManager) {
        this.alarmManager = alarmManager;
        this.context = context;
        this.connectivityManager = connectivityManager;
    }

    public void syncNow() {
        if(connectivityManager.getAllNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            if(!isSynced()) {
                getUserPreferenceFromFirebase();
            }
            else {
                Log.d("AutoSync", "Already synced today: " + Util.getStringFromDate(new Date()));
            }
        }
        else {
            Log.d("AutoSync", "No internet connection");
        }
    }

    public Boolean isSynced() {
        String today = Util.getStringFromDate(new Date(), "dd/MM/yyyy");
        String syncDate = Storage.getAutoSyncDate();
        if(today.equalsIgnoreCase(syncDate)) {
            return true;
        }
        return false;
    }

    public void restoreFromFirebase() {
        restoreDateOfBirthsFromFirebase();
    }

    public void backupToFirebase() {
        backupDateOfBirthsToFirebase();
    }

    public void restoreDateOfBirthsFromFirebase() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            return;
        }
        DocumentReference documentReference = firebaseFirestore.collection(firebaseUser.getUid()).document("friends");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Util.inserDateOfBirthFromServer(document.getData());
                        Storage.updateUserPreference(userPreference, alarmManager, context);
                        Storage.setAutoSyncDate(new Date());
                    } else {
                        Log.d("FirebaseGetData", "No such document");
                    }
                } else {
                    Log.d("FirebaseGetData", "get failed with ", task.getException());
                }
            }
        });
    }

    public void getUserPreferenceFromFirebase() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            return;
        }
        DocumentReference documentReference = firebaseFirestore.collection(firebaseUser.getUid()).document("settings");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userPreference = Storage.createUserPreferenceFromServer(document.getData());
                        if(userPreference.serverBackupTime != null) {
                            Storage.setServerBackupTime(userPreference.serverBackupTime);
                        }
                        compareBackupTimes();
                    } else {
                        Log.d("FirebaseGetData", "No such document");
                    }
                } else {
                    Log.d("FirebaseGetData", "get failed with ", task.getException());
                }
            }
        });
    }

    public void backupDateOfBirthsToFirebase() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            return;
        }
        Map<String, DateOfBirth> dateOfBirthMap = Util.getDateOfBirthMap();
        DocumentReference documentReference = firebaseFirestore.collection(firebaseUser.getUid()).document("friends");
        documentReference.set(dateOfBirthMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Storage.setServerBackupTime(Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore));
                backupUserPreferenceToFirebase();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("F", e.getLocalizedMessage());
            }
        });
    }

    public void backupUserPreferenceToFirebase() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null) {
            return;
        }
        DocumentReference documentReference = firebaseFirestore.collection(firebaseUser.getUid()).document("settings");
        documentReference.set(Storage.getUserPreference()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Storage.setAutoSyncDate(new Date());
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("F", e.getLocalizedMessage());
            }
        });
    }

    public void compareBackupTimes() {
        Date dbBackupDate = Util.getDateFromString(Storage.getDbBackupTime(), Constants.backupDateFormatToStore);
        Date serverBackupDate = Util.getDateFromString(Storage.getServerBackupTime(), Constants.backupDateFormatToStore);

        if(serverBackupDate == null && dbBackupDate == null) {
            return;
        }

        if(serverBackupDate == null) {
            backupToFirebase();
            return;
        }
        if(dbBackupDate == null) {
            restoreDateOfBirthsFromFirebase();
            return;
        }
        if(dbBackupDate.getTime() == serverBackupDate.getTime()) {
            return;
        }
        if(serverBackupDate.getTime() > dbBackupDate.getTime()) {
            // server data is latest
            restoreDateOfBirthsFromFirebase();
        }
        else {
            // local data is latest
            backupToFirebase();
        }
    }
}
