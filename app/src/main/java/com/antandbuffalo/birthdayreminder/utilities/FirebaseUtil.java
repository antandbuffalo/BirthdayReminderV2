package com.antandbuffalo.birthdayreminder.utilities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.antandbuffalo.birthdayreminder.models.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static void saveUserProfileToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        if(db == null || firebaseUser == null) {
            return;
        }
        // Create a new user with a first and last name
        UserProfile userProfile = Util.getUserProfileFromFirebaseUser(firebaseUser);
        Storage.saveUserProfileToLocal(userProfile);
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

    public static void getUserProfileFromFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        if(firebaseUser == null) {
            return;
        }
        // Create a new user with a first and last name
        UserProfile userProfile = Util.getUserProfileFromFirebaseUser(firebaseUser);
        DocumentReference documentReference = db.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentProfile);
        documentReference.set(userProfile).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Success", "got profile data");
                Storage.saveUserProfileToLocal(userProfile);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Profile", "Error getting profile: " + e.getLocalizedMessage());
            }
        });
    }

    public static void restoreDateOfBirthsFromFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser, FirebaseHandler firebaseHandler) {
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentFriends);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                firebaseHandler.onCompleteDateOfBirthSync(task);
            }
        });
    }

    public static void restoreUserPreferenceFromFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser, FirebaseHandler firebaseHandler) {
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                firebaseHandler.onCompleteUserPreferenceSync(task);
            }
        });
    }

    public static void restoreDateOfBirthAndUserPreferenceFromFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser, FirebaseHandler firebaseHandler) {
        List<Task<DocumentSnapshot>> snapshots = new ArrayList<>(Arrays.asList(null, null));

        DocumentReference documentReferenceDob = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentFriends);
        documentReferenceDob.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.i("AUTO_RESTORE", "Date of birth ready");
                snapshots.add(0, task);
                firebaseHandler.onCompleteDateOfBirthUserPreferenceSync("dob", task);
            }
        });

        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.i("AUTO_RESTORE", "User pref ready");
                snapshots.add(1, task);
                firebaseHandler.onCompleteDateOfBirthUserPreferenceSync("preference", task);
            }
        });
    }
}
