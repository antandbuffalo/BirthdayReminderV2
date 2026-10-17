package com.antandbuffalo.birthdayreminder.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.accountsetup.AccountSetup;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.settings.Settings;

import java.util.Date;

public class UIUtil {
    public static void showAlertWithOk(Context context, String title, String description) {
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null);
        androidx.appcompat.app.AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public static void showConfirmationDialog(Context context, String title, String content, DialogInterface.OnClickListener okListener) {
        androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        // .setIcon(android.R.drawable.ic_dialog_alert)
        // .setIconAttribute(android.R.attr.alertDialogIcon)
        alertDialogBuilder.setTitle(title)
                .setMessage(content)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        okListener.onClick(dialog, which);
                    }
                })
                .setNegativeButton("No", null);

        androidx.appcompat.app.AlertDialog dialog = alertDialogBuilder.create();
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.dark_gray));
            }
        });
        dialog.show();
    }

    // disabled controls also fade, since a tinted button otherwise looks the same
    public static void setEnabledDimmed(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1f : 0.4f);
    }

    // teal + medium weight for the chosen / important line, muted otherwise
    public static void highlight(TextView view, boolean on) {
        view.setTextColor(ContextCompat.getColor(view.getContext(), on ? R.color.aandb_soon_text_color : R.color.aandb_muted_text_color));
        view.setTypeface(on ? Typeface.create("sans-serif-medium", Typeface.NORMAL) : Typeface.DEFAULT);
    }

    // one row of a settings-style radio list (remind_me_option.xml); the chosen row reads teal
    public static View addRadioRow(LayoutInflater inflater, ViewGroup list, String label, String hint, boolean selected, View.OnClickListener onClick) {
        View row = inflater.inflate(R.layout.remind_me_option, list, false);
        TextView labelView = row.findViewById(R.id.remindLabel);
        labelView.setText(label);
        if (hint != null && !hint.isEmpty()) {
            TextView hintView = row.findViewById(R.id.remindHint);
            hintView.setText(hint);
            hintView.setVisibility(View.VISIBLE);
        }
        if (selected) {
            ((RadioButton) row.findViewById(R.id.remindRadio)).setChecked(true);
            labelView.setTextColor(ContextCompat.getColor(list.getContext(), R.color.aandb_soon_text_color));
            labelView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
        row.setOnClickListener(onClick);
        list.addView(row);
        return row;
    }
}
