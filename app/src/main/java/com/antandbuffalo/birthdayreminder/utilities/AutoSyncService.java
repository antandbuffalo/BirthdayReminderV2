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
    final private AlarmManager alarmManager;
    final private Context context;
    final private ConnectivityManager connectivityManager;
    public FirebaseHandler firebaseHandler;

    public AutoSyncService(AlarmManager alarmManager, Context context, ConnectivityManager connectivityManager) {
        this.alarmManager = alarmManager;
        this.context = context;
        this.connectivityManager = connectivityManager;
    }

    public void syncNow() {
        // even if connectivityManager is not null, connectivityManager.getActiveNetworkInfo() can be null. So added an extra check
        if(connectivityManager != null && connectivityManager.getAllNetworks() != null && connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
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
        return today.equalsIgnoreCase(syncDate);
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
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentFriends);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Util.inserDateOfBirthFromServer(document.getData());
                    Storage.updateUserPreference(userPreference, alarmManager, context);
                    Storage.setAutoSyncDate(new Date());
                    DataHolder.getInstance().refresh = true;
                    if(firebaseHandler != null) {
                        firebaseHandler.onCompleteDateOfBirthSync(task);
                    }
                } else {
                    Log.d("FirebaseGetData", "No such document");
                }
            } else {
                Log.d("FirebaseGetData", "get failed with ", task.getException());
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
                    } else {
                        Log.d("FirebaseGetData", "No such document");
                    }
                    compareBackupTimes();
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
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentFriends);
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
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
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
