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
}
