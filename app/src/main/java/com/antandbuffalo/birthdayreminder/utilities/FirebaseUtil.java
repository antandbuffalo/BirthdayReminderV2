package com.antandbuffalo.birthdayreminder.utilities;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.antandbuffalo.birthdayreminder.backup.Backup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
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
                snapshots.add(0, task);
                firebaseHandler.onCompleteDateOfBirthUserPreferenceSync(snapshots.get(0), snapshots.get(1));
            }
        });

        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                snapshots.add(1, task);
                firebaseHandler.onCompleteDateOfBirthUserPreferenceSync(snapshots.get(0), snapshots.get(1));
            }
        });
    }
}
