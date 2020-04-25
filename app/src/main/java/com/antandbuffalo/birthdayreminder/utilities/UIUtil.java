package com.antandbuffalo.birthdayreminder.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DateOfBirthDBHelper;
import com.antandbuffalo.birthdayreminder.settings.Settings;

import java.util.Date;

public class UIUtil {
    public static void showAlertWithOk(Context context, String title, String description) {
        new AlertDialog.Builder(context)
             //.setIcon(R.drawable.ic_error_outline_accent)
            //.setIconAttribute(android.R.attr.alertDialogIcon
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            })
            .show();
    }
}
