package com.antandbuffalo.birthdayreminder.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
}
