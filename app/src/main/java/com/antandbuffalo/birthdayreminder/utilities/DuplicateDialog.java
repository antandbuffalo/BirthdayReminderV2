package com.antandbuffalo.birthdayreminder.utilities;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.upcoming.UpcomingListAdapter;
import com.antandbuffalo.birthdayreminder.update.Update;


/**
 * "Already saved" pop-up for the add and edit screens. Shows the saved entry
 * and lets the user open it or go back and change what they typed.
 */
public class DuplicateDialog {

    public static void show(Activity activity, DateOfBirth existing, int currentDayOfYear, int recentDayOfYear) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_duplicate, null);
        ((TextView) view.findViewById(R.id.duplicateMessage)).setText(existing.getName() + "'s birthday is already in your list.");

        // Same turning-age shown in the list and the edit preview
        DateOfBirth row = new DateOfBirth();
        row.setName(existing.getName());
        row.setDobDate(existing.getDobDate());
        row.setRemoveYear(existing.getRemoveYear());
        row.setAge(existing.getAge());
        if(Util.getDayOfYear(row.getDobDate()) != currentDayOfYear) {
            row.setAge(row.getAge() + 1);
        }
        UpcomingListAdapter.bindRow(view.findViewById(R.id.duplicateRow), row, currentDayOfYear, recentDayOfYear);

        AlertDialog dialog = new AlertDialog.Builder(activity).setView(view).create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);

        view.findViewById(R.id.changeDetails).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.viewExisting).setOnClickListener(v -> {
            dialog.dismiss();
            // The list refreshes on resume, so edits made to the existing entry show up
            DataHolder.getInstance().refresh = true;
            Intent intent = new Intent(activity, Update.class);
            intent.putExtra("currentDOB", existing);
            activity.startActivity(intent);
            activity.finish();
        });
        dialog.show();
    }
}
