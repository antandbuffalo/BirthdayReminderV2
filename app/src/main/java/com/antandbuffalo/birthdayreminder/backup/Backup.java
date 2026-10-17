package com.antandbuffalo.birthdayreminder.backup;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.models.UserProfile;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.FirebaseHandler;
import com.antandbuffalo.birthdayreminder.utilities.FirebaseUtil;
import com.antandbuffalo.birthdayreminder.utilities.PopupDialog;
import com.antandbuffalo.birthdayreminder.utilities.ReviewPrompt;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.UIUtil;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Backup extends AppCompatActivity implements FirebaseHandler {
    // auto sync choices: key stored in preferences, label, when it runs (see AlarmReceiver), toast
    private static final String[][] AUTO_SYNC = {
            {"none", "Off", "", "Auto sync turned off"},
            {"daily", "Daily", "", "Auto sync: daily"},
            {"weekly", "Weekly", "Sundays", "Auto sync: weekly, on Sundays"},
            {"monthly", "Monthly", "1st of the month", "Auto sync: monthly, on the 1st"},
    };

    AdView mAdView;
    Integer progressCounter = 0;
    Task<DocumentSnapshot> dobDone, preferenceDone;

    // status line state while the backup time is loading or couldn't load
    boolean checking = false;
    boolean offline = false;
    // the row that's backing up or restoring; the rest of the page is disabled until it's done
    int busyRow = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup);

        initValues();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Backup & restore");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView) findViewById(R.id.accountHeading)).setText("Google account");
        ((TextView) findViewById(R.id.backupHeading)).setText("Backup");
        ((TextView) findViewById(R.id.autoSyncHeading)).setText("Auto sync");
        ((TextView) findViewById(R.id.fileHeading)).setText("On this phone");
        findViewById(R.id.autoSyncOptions).setClipToOutline(true);

        bindRow(R.id.backupNowRow, R.drawable.ic_settings_backup, "Back up now");
        bindRow(R.id.restoreRow, R.drawable.ic_backup_restore, "Restore");
        bindRow(R.id.restoreFileRow, R.drawable.ic_backup_restore, "Restore from a file");
        setSubtitle(R.id.restoreFileRow, "From a file saved earlier on this phone", false);

        findViewById(R.id.accountRow).setOnClickListener(v -> startFirebaseAuth());
        findViewById(R.id.signOut).setOnClickListener(v -> removeAccountConfirmation());
        findViewById(R.id.backupNowRow).setOnClickListener(v -> onBackupNow());
        findViewById(R.id.restoreRow).setOnClickListener(v -> onRestore());
        findViewById(R.id.signIn).setOnClickListener(v -> startFirebaseAuth());
        findViewById(R.id.restoreFileRow).setOnClickListener(v -> restoreFromFile());

        render();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            refreshServerBackupTime();
        }
        loadAd();
        showSnowFlakes();
    }

    public void initValues() {
        DBHelper.createInstance(this);
        DataHolder.getInstance().setAppContext(getApplicationContext());
    }

    // settings_row with no chevron: these rows act on this page
    private void bindRow(int rowId, int iconId, String title) {
        View row = findViewById(rowId);
        ((ImageView) row.findViewById(R.id.settingsIcon)).setImageResource(iconId);
        ((TextView) row.findViewById(R.id.settingsTitle)).setText(title);
        row.findViewById(R.id.settingsTrailing).setVisibility(View.GONE);
    }

    private void setSubtitle(int rowId, String text, boolean recommended) {
        TextView subtitle = findViewById(rowId).findViewById(R.id.settingsSubtitle);
        subtitle.setText(text);
        UIUtil.highlight(subtitle, recommended);
    }

    // ---- page state ----

    public void render() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        findViewById(R.id.signedInView).setVisibility(user != null ? View.VISIBLE : View.GONE);
        findViewById(R.id.signedOutView).setVisibility(user != null ? View.GONE : View.VISIBLE);
        if (user == null) {
            return;
        }
        String email = user.getEmail() == null ? "" : user.getEmail();
        ((TextView) findViewById(R.id.accountEmail)).setText(email);
        ((TextView) findViewById(R.id.accountInitial)).setText(email.isEmpty() ? "" : email.substring(0, 1).toUpperCase());
        renderStatus();
        buildAutoSyncOptions();
    }

    private void renderStatus() {
        Date server = Storage.getServerBackupDate();
        Date local = Storage.getDbBackupDate();

        TextView title = findViewById(R.id.backupStatusTitle);
        title.setText(server == null ? "Not backed up yet"
                : Html.fromHtml("Last backup: <b>" + formatBackupTime(server) + "</b>"));

        String detail = "";
        boolean warn = false;
        int recommended = 0;
        if (checking) {
            detail = "Checking…";
        } else if (offline) {
            detail = "Offline · couldn't check, showing last known";
        } else if (server == null) {
            recommended = R.id.backupNowRow;
        } else if (local != null && local.after(server)) {
            detail = "Changed " + formatBackupTime(local) + " · not backed up";
            warn = true;
            recommended = R.id.backupNowRow;
        } else if (local == null || local.before(server)) {
            detail = local == null ? "Newer than this phone"
                    : "Newer than this phone (changed " + Util.getStringFromDate(local, "d MMM") + ")";
            warn = true;
            recommended = R.id.restoreRow;
        } else {
            detail = "Up to date";
        }

        TextView detailView = findViewById(R.id.backupStatusDetail);
        detailView.setText(detail);
        UIUtil.highlight(detailView, warn);
        findViewById(R.id.backupStatusDetailRow).setVisibility(detail.isEmpty() ? View.GONE : View.VISIBLE);
        findViewById(R.id.backupStatusDot).setVisibility(warn ? View.VISIBLE : View.GONE);
        findViewById(R.id.backupStatusSpinner).setVisibility(checking ? View.VISIBLE : View.GONE);

        boolean up = recommended == R.id.backupNowRow;
        boolean restore = recommended == R.id.restoreRow;
        setSubtitle(R.id.backupNowRow, server == null
                ? (up ? "Recommended · saves your birthdays and settings" : "Saves your birthdays and settings")
                : (up ? "Recommended · overwrites your backup" : "Overwrites your backup"), up);
        setSubtitle(R.id.restoreRow, restore ? "Recommended · adds missing birthdays" : "Brings back missing birthdays", restore);
    }

    // "today, 8:02 AM", "yesterday, 9:15 PM", "12 Oct, 9:15 PM", "12 Oct 2025"
    private String formatBackupTime(Date date) {
        Calendar then = Util.getCalendar(date);
        Calendar now = Calendar.getInstance();
        String time = Storage.formatTime(this, then.get(Calendar.HOUR_OF_DAY), then.get(Calendar.MINUTE));
        if (then.get(Calendar.YEAR) != now.get(Calendar.YEAR)) {
            return Util.getStringFromDate(date, "d MMM yyyy");
        }
        int days = now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR);
        if (days == 0) {
            return "today, " + time;
        }
        if (days == 1) {
            return "yesterday, " + time;
        }
        return Util.getStringFromDate(date, "d MMM") + ", " + time;
    }

    private void buildAutoSyncOptions() {
        LinearLayout options = findViewById(R.id.autoSyncOptions);
        options.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        String selected = Storage.getAutoSyncFrequency();
        for (String[] choice : AUTO_SYNC) {
            UIUtil.addRadioRow(inflater, options, choice[1], choice[2], choice[0].equalsIgnoreCase(selected), v -> saveAutoSync(choice));
        }
    }

    private void saveAutoSync(String[] choice) {
        if (busyRow != 0 || choice[0].equalsIgnoreCase(Storage.getAutoSyncFrequency())) {
            return;
        }
        Storage.setAutoSyncFrequency(choice[0]);
        Storage.setDbBackupTime(new Date());
        Toast.makeText(this, choice[3], Toast.LENGTH_SHORT).show();
        buildAutoSyncOptions();
        renderStatus();
    }

    // 0 clears it; the auto sync rows are rebuilt each time so they check busyRow themselves
    private void setBusy(int rowId) {
        busyRow = rowId;
        boolean free = rowId == 0;
        UIUtil.setEnabledDimmed(findViewById(R.id.accountRow), free);
        UIUtil.setEnabledDimmed(findViewById(R.id.signOut), free);
        UIUtil.setEnabledDimmed(findViewById(R.id.autoSyncOptions), free);
        UIUtil.setEnabledDimmed(findViewById(R.id.backupNowRow), free || rowId == R.id.backupNowRow);
        UIUtil.setEnabledDimmed(findViewById(R.id.restoreRow), free || rowId == R.id.restoreRow);
        findViewById(R.id.backupStatus).setAlpha(free ? 1f : 0.4f);
        if (free) {
            renderStatus();
        } else {
            setSubtitle(rowId, rowId == R.id.backupNowRow ? "Backing up…" : "Restoring…", false);
        }
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return network != null && network.isConnected();
    }

    // ---- pop-ups ----

    private void showMessage(int iconId, String title, String message) {
        View view = PopupDialog.inflate(this, iconId, title, message, "", "OK");
        view.findViewById(R.id.popupSecondary).setVisibility(View.GONE);
        PopupDialog.show(this, view, null, null);
    }

    private void showOffline(String action) {
        refreshServerBackupTime();
        showMessage(R.drawable.ic_cloud_off, "You're offline", "Connect to the internet to " + action + ". Nothing was changed.");
    }

    // ---- actions ----

    private void onBackupNow() {
        if (!isOnline()) {
            showOffline("back up");
            return;
        }
        // an empty phone would replace the backup with nothing
        if (DBHelper.getNumberOfRows(Constants.TABLE_DATE_OF_BIRTH) == 0) {
            View view = PopupDialog.inflate(this, R.drawable.ic_settings_backup, "Nothing to back up",
                    "This phone has no birthdays. Backing up now would erase the birthdays in your backup.\n\nRestore them to this phone instead?",
                    "Cancel", "Restore");
            PopupDialog.show(this, view, null, () -> restoreBirthdaysAndSettingsFromFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser()));
            return;
        }
        View view = PopupDialog.inflate(this, R.drawable.ic_settings_backup, "Back up now?",
                "Your backup will be replaced with the birthdays and settings on this phone.", "Cancel", "Back up");
        PopupDialog.show(this, view, null, () -> {
            if (getStoragePermission(Constants.MY_PERMISSIONS_READ_WRITE)) {
                updateLocalBackup();
            } else {
                Toast.makeText(Backup.this, "Allow storage access to back up to a file", Toast.LENGTH_SHORT).show();
            }
            backupDateOfBirthsToFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser());
        });
    }

    private void onRestore() {
        if (!isOnline()) {
            showOffline("restore");
            return;
        }
        View view = PopupDialog.inflate(this, R.drawable.ic_backup_restore, "Restore from backup?",
                "Birthdays in your backup that aren't on this phone will be added. Nothing on this phone gets deleted. Birthdays you deleted after this backup will come back.\n\nYour settings (reminder time, wishes, theme) will be replaced with the ones in the backup.",
                "Cancel", "Restore");
        PopupDialog.show(this, view, null, () -> restoreBirthdaysAndSettingsFromFirebase(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance().getCurrentUser()));
    }

    private void restoreFromFile() {
        String status = Util.readFromFile("dob.txt");
        if (Constants.ERROR_NO_BACKUP_FILE.equals(status) || Constants.ERROR_NO_SD_CARD.equals(status)) {
            showMessage(R.drawable.ic_backup_restore, "No backup file", "There's no backup file on this phone.");
            return;
        }
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        DataHolder.getInstance().refresh = true;
    }

    public void updateLocalBackup() {
        File file = Util.writeToFile(this);
        if (file != null) {
            Storage.setDbBackupTime(new Date(file.lastModified()));
        }
    }

    // the backup time on the server, read fresh so the status never shows a stale or another account's time
    public void refreshServerBackupTime() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }
        if (!isOnline()) {
            offline = true;
            renderStatus();
            return;
        }
        checking = true;
        offline = false;
        renderStatus();
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        // activity-scoped listeners: a closed page doesn't wait on a slow fetch
        documentReference.get(Source.SERVER).addOnSuccessListener(this, documentSnapshot -> {
                    checking = false;
                    if (!documentSnapshot.exists()) {
                        Storage.setServerBackupTime(null);
                    } else {
                        Date backupTime = serverBackupTimeOf(documentSnapshot);
                        if (backupTime != null) {
                            Storage.setServerBackupTime(backupTime);
                        }
                    }
                    renderStatus();
                })
                .addOnFailureListener(this, e -> {
                    Log.e("ERROR", "Not able to get last updated time");
                    checking = false;
                    offline = true;
                    renderStatus();
                });
    }

    // the backup time stored in the settings document, if it has one
    private static Date serverBackupTimeOf(DocumentSnapshot document) {
        Timestamp timestamp = (Timestamp) document.get(Constants.serverBackupTime);
        return timestamp == null ? null : timestamp.toDate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.firebaseSignInCode) {
            handleFirebaseSignIn(resultCode, data);
        }
    }

    public Boolean getStoragePermission(int permissionType) {
        switch (permissionType) {
            case Constants.MY_PERMISSIONS_READ_WRITE: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            Constants.MY_PERMISSIONS_READ_WRITE);

                    return false;
                } else {
                    // Permission has already been granted
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.MY_PERMISSIONS_READ_WRITE
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateLocalBackup();
        }
    }

    public void handleFirebaseSignIn(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            // could be a different account: drop the old account's backup time until this one's loads
            Storage.setServerBackupTime(null);
            updateProfileToFirebase(FirebaseFirestore.getInstance(), user);
            // signing in isn't a change on this phone: leave the local time alone so the
            // status (and auto sync) compare real data, not the sign-in moment
            offline = false;
            render();

            Log.i("AUTO_RESTORE", "Calling auto restore");
            showProgressBar();
            showProgressBar();
            dobDone = null;
            preferenceDone = null;
            FirebaseUtil.restoreDateOfBirthAndUserPreferenceFromFirebase(FirebaseFirestore.getInstance(), user, Backup.this);
        } else {
            Toast.makeText(this, Constants.SIGN_IN_FAILED, Toast.LENGTH_SHORT).show();
        }
    }

    public void startFirebaseAuth() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                Constants.firebaseSignInCode);
    }

    public void signoutFromFirebase() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    Storage.setServerBackupTime(null);
                    checking = false;
                    offline = false;
                    render();
                });
    }

    public void backupDateOfBirthsToFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            return;
        }
        if (Storage.getDbBackupTime() == null || Storage.getDbBackupTime().isEmpty()) {
            Storage.setDbBackupTime(new Date());
        }
        showProgressBar();
        setBusy(R.id.backupNowRow);
        Map<String, DateOfBirth> dateOfBirthMap = Util.getDateOfBirthMap();
        DocumentReference documentReference = firebaseFirestore.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentFriends);
        documentReference.set(dateOfBirthMap).addOnSuccessListener(aVoid -> {
                    hideProgressBar();
                    Log.d("Success", "data updated successfully");
                    Storage.setServerBackupTime(Storage.getDbBackupDate());
                    backupUserPreferenceToFirebase(firebaseFirestore, firebaseUser);
                    ReviewPrompt.showNow(Backup.this);
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    setBusy(0);
                    Log.e("F", e.getLocalizedMessage());
                    Toast.makeText(Backup.this, Constants.BACKUP_FAILED, Toast.LENGTH_LONG).show();
                });
    }

    public void backupUserPreferenceToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        showProgressBar();
        DocumentReference documentReference = db.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentSettings);
        documentReference.set(Storage.getUserPreference()).addOnSuccessListener(aVoid -> {
                    hideProgressBar();
                    offline = false;
                    setBusy(0);
                    Log.d("Success", "Preferences updated successfully");
                    Toast.makeText(Backup.this, "Birthdays and settings backed up", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    hideProgressBar();
                    setBusy(0);
                    Log.e("F", e.getLocalizedMessage());
                    Toast.makeText(Backup.this, Constants.BACKUP_FAILED, Toast.LENGTH_LONG).show();
                });
    }

    public void updateProfileToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            return;
        }
        UserProfile userProfile = Util.getUserProfileFromFirebaseUser(firebaseUser);
        Storage.saveUserProfileToLocal(userProfile);
        DocumentReference documentReference = db.collection(Util.getCollectionId(firebaseUser)).document(Constants.firebaseDocumentProfile);
        documentReference.set(userProfile)
                .addOnSuccessListener(aVoid -> Log.d("Success", "Profile updated"))
                .addOnFailureListener(e -> Log.e("Profile", "Error updating profile: " + e.getLocalizedMessage()));
    }

    public void restoreBirthdaysAndSettingsFromFirebase(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            return;
        }
        showProgressBar();
        setBusy(R.id.restoreRow);
        Task<DocumentSnapshot> dobTask = FirebaseUtil.getDocument(firebaseFirestore, firebaseUser, Constants.firebaseDocumentFriends);
        Task<DocumentSnapshot> preferenceTask = FirebaseUtil.getDocument(firebaseFirestore, firebaseUser, Constants.firebaseDocumentSettings);
        Tasks.whenAllComplete(dobTask, preferenceTask).addOnCompleteListener(done -> {
            hideProgressBar();
            setBusy(0);
            if (!dobTask.isSuccessful()) {
                Toast.makeText(Backup.this, "Couldn't restore. Check your connection", Toast.LENGTH_LONG).show();
                return;
            }
            if (!dobTask.getResult().exists()) {
                showMessage(R.drawable.ic_backup_restore, "No backup found", "There's no backup on " + firebaseUser.getEmail() + " yet.");
                return;
            }
            restoreBirthdaysAndSettings(dobTask, preferenceTask);
        });
    }

    public void restoreBirthdaysAndSettings(Task<DocumentSnapshot> dobTask, Task<DocumentSnapshot> preferenceTask) {
        boolean restored = postProcessDateOfBirths(dobTask);
        postProcessUserPreference(preferenceTask);
        if (restored) {
            Toast.makeText(Backup.this, "Birthdays and settings restored", Toast.LENGTH_SHORT).show();
            ReviewPrompt.showNow(Backup.this);
        }
    }

    // after sign-in: offer the account's backup, if it has one
    public void autoRestoreConfirmation(Task<DocumentSnapshot> dobTask, Task<DocumentSnapshot> preferenceTask) {
        Date backupTime = null;
        if (preferenceTask.isSuccessful() && preferenceTask.getResult().exists()) {
            backupTime = serverBackupTimeOf(preferenceTask.getResult());
            if (backupTime != null) {
                Storage.setServerBackupTime(backupTime);
            }
        }
        if (preferenceTask.isSuccessful()) {
            renderStatus();
        } else {
            // couldn't read the backup time, so don't claim there's no backup
            refreshServerBackupTime();
        }
        if (!dobTask.isSuccessful() || !dobTask.getResult().exists()) {
            return;
        }
        String from = backupTime == null ? "" : " from " + formatBackupTime(backupTime);
        View view = PopupDialog.inflate(this, R.drawable.ic_backup_restore, "Backup found",
                "You have a backup" + from + " on " + FirebaseAuth.getInstance().getCurrentUser().getEmail() + ". Restore it now?\n\nBirthdays are added, nothing on this phone gets deleted. You can also do this later with Restore.",
                "Not now", "Restore");
        PopupDialog.show(this, view, null, () -> restoreBirthdaysAndSettings(dobTask, preferenceTask));
    }

    public void removeAccountConfirmation() {
        View view = PopupDialog.inflate(this, R.drawable.ic_cloud_off, "Sign out?",
                "Your backup stays in your Google account. Auto sync stops until you sign in again.", "Cancel", "Sign out");
        PopupDialog.show(this, view, null, this::signoutFromFirebase);
    }

    public void showProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressCounter == 0) {
            progressBar.setVisibility(View.VISIBLE);
        }
        progressCounter++;
    }

    public void hideProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressCounter--;
        if (progressCounter == 0) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void loadAd() {
        mAdView = this.findViewById(R.id.adView);
        if (!Constants.enableAds) {
            mAdView.setVisibility(View.INVISIBLE);
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public boolean postProcessDateOfBirths(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                Log.d("FirebaseGetData", "DocumentSnapshot data: " + document.getData());
                Util.inserDateOfBirthFromServer(document.getData());
                DataHolder.getInstance().refresh = true;
                return true;
            } else {
                Log.d("FirebaseGetData", "No such document");
            }
        } else {
            Log.d("FirebaseGetData", "get failed with ", task.getException());
        }
        return false;
    }

    public void postProcessUserPreference(Task<DocumentSnapshot> task) {
        if (task.isSuccessful()) {
            DocumentSnapshot document = task.getResult();
            if (document.exists()) {
                Log.d("FirebaseGetData", "DocumentSnapshot data: " + document.getData());
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Storage.updateUserPreference(Storage.createUserPreferenceFromServer(document.getData()), alarmManager, getApplicationContext());
                render();
                Util.applyTheme();
                DataHolder.getInstance().refreshSettings = true;
            } else {
                Log.d("FirebaseGetData", "No such document");
            }
        } else {
            Log.d("FirebaseGetData", "get failed with ", task.getException());
        }
    }

    @Override
    public void onCompleteDateOfBirthUserPreferenceSync(String type, Task<DocumentSnapshot> task) {
        Log.i("AUTO_RESTORE", "One is done");
        hideProgressBar();
        if (type.equalsIgnoreCase("dob")) {
            dobDone = task;
        } else if (type.equalsIgnoreCase("preference")) {
            preferenceDone = task;
        }

        if (dobDone != null && preferenceDone != null) {
            Log.i("AUTO_RESTORE", "Both done");
            autoRestoreConfirmation(dobDone, preferenceDone);
        }
    }

    public void showSnowFlakes() {
        if (Util.showSnow()) {
            View snowFlakes = this.findViewById(R.id.snowFlakes);
            snowFlakes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    public void onPause() {
        // Pause the AdView.
        mAdView.pause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Destroy the AdView.
        mAdView.destroy();
        super.onDestroy();
    }
}
