package com.antandbuffalo.birthdayreminder.utilities;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public interface FirebaseHandler {
    default public void onCompleteDateOfBirthSync(Task<DocumentSnapshot> task) {};
    default public void onCompleteUserPreferenceSync(Task<DocumentSnapshot> task) {};
    default public void onCompleteDateOfBirthUserPreferenceSync(Task<DocumentSnapshot> dobTask, Task<DocumentSnapshot> preferenceTask) {};
    default public void onCompleteDateOfBirthUserPreferenceSync(String type, Task<DocumentSnapshot> task) {};
}
