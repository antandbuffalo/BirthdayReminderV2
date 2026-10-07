package com.antandbuffalo.birthdayreminder.utilities;

import android.app.Activity;

import com.antandbuffalo.birthdayreminder.database.DBHelper;

import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.Date;

// Asks for a Play Store review right after a positive moment. Add / edit / send wish
// close their screen and land on the main screen, so they only mark the moment and the
// main screen shows the dialog. Backup / restore stay on their screen, so they show it there.
public class ReviewPrompt {
    private static boolean pending = false;

    public static void markPositiveMoment() {
        pending = true;
    }

    public static void showIfDue(Activity activity) {
        if (!pending) {
            return;
        }
        pending = false;
        showNow(activity);
    }

    public static void showNow(Activity activity) {
        if (activity.isFinishing()
                || Util.getDaysBetweenDates(Storage.getRatingPresentedDate()) < Constants.DAYS_TO_SHOW_RATING
                || DBHelper.getNumberOfRows(Constants.TABLE_DATE_OF_BIRTH) < Constants.DOB_COUNT_TO_SHOW_RATING) {
            return;
        }

        ReviewManager manager = ReviewManagerFactory.create(activity);
        // Activity-scoped listener: dropped if the user leaves the screen first, so the ask is kept for later
        manager.requestReviewFlow().addOnCompleteListener(activity, task -> {
            // The API does not tell whether the dialog was shown or the user reviewed,
            // so the date is recorded either way.
            Storage.setRatingPresentedDate(new Date());
            if (task.isSuccessful()) {
                manager.launchReviewFlow(activity, task.getResult());
            } else {
                System.out.println("error while presenting rating review" + task.getException().getLocalizedMessage());
            }
        });
    }
}
