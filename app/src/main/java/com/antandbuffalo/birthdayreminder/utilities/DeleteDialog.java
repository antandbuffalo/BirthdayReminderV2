package com.antandbuffalo.birthdayreminder.utilities;

import android.app.Activity;
import android.view.View;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;


/**
 * Delete pop-ups. "Delete Name?" on the edit screen shows the saved entry so
 * the user can see it is the right person; "Delete all" in settings shows the
 * count and points to backup.
 */
public class DeleteDialog {

    public static void show(Activity activity, DateOfBirth saved, int currentDayOfYear, int recentDayOfYear, Runnable onDelete) {
        View view = PopupDialog.inflate(activity, R.drawable.ic_delete_white, "Delete " + saved.getName() + "?",
                "You won't get reminders for this birthday any more.", "Cancel", "Delete");
        PopupDialog.showEntry(view, saved, currentDayOfYear, recentDayOfYear);
        PopupDialog.show(activity, view, null, onDelete);
    }

    public static void showAll(Activity activity, long count, Runnable onDelete) {
        View view = PopupDialog.inflate(activity, R.drawable.ic_delete_white, count == 1 ? "Delete 1 birthday?" : "Delete all " + count + " birthdays?",
                "You won't get any reminders. Back up first if you might want them back.", "Cancel", "Delete all");
        PopupDialog.show(activity, view, null, onDelete);
    }
}
