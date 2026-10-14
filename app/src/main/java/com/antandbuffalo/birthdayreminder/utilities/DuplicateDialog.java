package com.antandbuffalo.birthdayreminder.utilities;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.update.Update;


/**
 * "Already saved" pop-up for the add and edit screens. Shows the saved entry
 * and lets the user open it or go back and change what they typed.
 */
public class DuplicateDialog {

    public static void show(Activity activity, DateOfBirth existing, int currentDayOfYear, int recentDayOfYear) {
        View view = PopupDialog.inflate(activity, R.drawable.ic_app_icon_clipart_primary_dark, "Already saved",
                existing.getName() + "'s birthday is already in your list.", "View existing", "Change details");
        PopupDialog.showEntry(view, existing, currentDayOfYear, recentDayOfYear);
        PopupDialog.show(activity, view, () -> {
            // The list refreshes on resume, so edits made to the existing entry show up
            DataHolder.getInstance().refresh = true;
            Intent intent = new Intent(activity, Update.class);
            intent.putExtra("currentDOB", existing);
            activity.startActivity(intent);
            activity.finish();
        }, null);
    }
}
