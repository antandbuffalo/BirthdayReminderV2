package com.antandbuffalo.birthdayreminder.utilities;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.upcoming.UpcomingListAdapter;


/**
 * Shared look for the app's pop-ups (dialog_popup.xml): icon, title, message,
 * an optional saved-entry row and two buttons, with the grow/fade animation.
 */
public class PopupDialog {

    public static View inflate(Activity activity, int iconId, String title, String message, String secondary, String primary) {
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_popup, null);
        ((ImageView) view.findViewById(R.id.popupIcon)).setImageResource(iconId);
        ((TextView) view.findViewById(R.id.popupTitle)).setText(title);
        ((TextView) view.findViewById(R.id.popupMessage)).setText(message);
        ((Button) view.findViewById(R.id.popupSecondary)).setText(secondary);
        ((Button) view.findViewById(R.id.popupPrimary)).setText(primary);
        return view;
    }

    // Shows the saved entry with the age they are turning, like the list
    public static void showEntry(View view, DateOfBirth saved, int currentDayOfYear, int recentDayOfYear) {
        DateOfBirth row = new DateOfBirth();
        row.setName(saved.getName());
        row.setDobDate(saved.getDobDate());
        row.setRemoveYear(saved.getRemoveYear());
        row.setAge(Util.getAge(saved.getDobDate()));
        if(Util.getDayOfYear(row.getDobDate()) != currentDayOfYear) {
            row.setAge(row.getAge() + 1);
        }
        UpcomingListAdapter.bindRow(view.findViewById(R.id.popupRow), row, currentDayOfYear, recentDayOfYear);
        view.findViewById(R.id.popupRowCard).setVisibility(View.VISIBLE);
    }

    // Rounded background and grow/fade animation, for pop-ups with their own layout too
    public static AlertDialog create(Activity activity, View view) {
        AlertDialog dialog = new AlertDialog.Builder(activity).setView(view).create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
        dialog.getWindow().setWindowAnimations(R.style.PopupDialogAnimation);
        return dialog;
    }

    // Both buttons close the pop-up first; a null action just closes it
    public static void show(Activity activity, View view, Runnable onSecondary, Runnable onPrimary) {
        AlertDialog dialog = create(activity, view);

        view.findViewById(R.id.popupSecondary).setOnClickListener(v -> {
            dialog.dismiss();
            if (onSecondary != null) onSecondary.run();
        });
        view.findViewById(R.id.popupPrimary).setOnClickListener(v -> {
            dialog.dismiss();
            if (onPrimary != null) onPrimary.run();
        });
        dialog.show();
    }
}
