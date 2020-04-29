package com.antandbuffalo.birthdayreminder.utilities;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antandbuffalo.birthdayreminder.backup.Backup;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class AutoSyncService {
    public void restoreFromFirebase() {
        restoreDateOfBirthsFromFirebase();
    }

    public void backupToFirebase() {
        backupDateOfBirthsToFirebase();
        backupUserPreferenceToFirebase();
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
                    } else {
                        Log.d("FirebaseGetData", "No such document");
                    }
                } else {
                    Log.d("FirebaseGetData", "get failed with ", task.getException());
                }
            }
        });
    }

    public void restoreUserPreferenceFromFirebase() {
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
                        Storage.updateUserPreference(Storage.createUserPreferenceFromServer(document.getData()));
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
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("F", e.getLocalizedMessage());
            }
        });
    }
}
