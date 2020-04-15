package com.antandbuffalo.birthdayreminder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.settings.Settings;
import com.antandbuffalo.birthdayreminder.upcoming.UpcomingListAdapter;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    DriveServiceHelper driveServiceHelper;
    UpcomingListAdapter upcomingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                createFile();
            }
        });

        initValues();
//        driveSignIn();
        Util.copyFromAssetFileToDatabase("cse.txt");

        upcomingListAdapter = new UpcomingListAdapter();
        //http://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        ListView upcomingListView = (ListView)findViewById(R.id.upcomingListView);
        upcomingListView.setAdapter(upcomingListAdapter);

        EditText filter = (EditText)findViewById(R.id.upcomingFiler);
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                upcomingListAdapter.getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });

    }

    public void initValues() {
        DBHelper.createInstance(this);
        DataHolder.getInstance().setAppContext(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, Settings.class);
            startActivity(settings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void driveSignIn() {
        // https://stackoverflow.com/questions/54052220/how-to-access-the-application-data-on-google-drive-on-android-with-its-rest-api

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken("405608185466-1ertijbnjfc99cgdvbmplv7r0c840vu6.apps.googleusercontent.com")x
                .requestEmail()
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        startActivityForResult(googleSignInClient.getSignInIntent(), 999);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999) {
            handleSignIn(data);
        }
    }

    public void handleSignIn(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.d("JBL", "Signed in as " + googleSignInAccount.getEmail());

                        // Use the authenticated account to sign in to the Drive service.
                        GoogleAccountCredential credential =
                                GoogleAccountCredential.usingOAuth2(
                                        getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_APPDATA));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());
                        Drive googleDriveService =
                                new Drive.Builder(
                                        AndroidHttp.newCompatibleTransport(),
                                        new GsonFactory(),
                                        credential)
                                        .setApplicationName("Birthday Reminder V2")
                                        .build();
                        driveServiceHelper = new DriveServiceHelper(googleDriveService);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("FAIL", e.getLocalizedMessage());
                    }
                });
    }

    /**
     * Creates a new file via the Drive REST API.
     */
    private void createFile() {
        if (driveServiceHelper != null) {
            Log.d("JBL", "Creating a file.");

            driveServiceHelper.createFile(null)
                    .addOnSuccessListener(fileId -> {
                        readFile(fileId);
                        if(getStoragePermission(Constants.MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE)) {
                            Util.downloadFileFromGoogleDrive(driveServiceHelper, fileId);
                        }
                    })
                    .addOnFailureListener(exception ->
                            Log.e("JBL", "Couldn't create file.", exception));
        }
    }

    /**
     * Retrieves the title and content of a file identified by {@code fileId} and populates the UI.
     */
    private void readFile(String fileId) {
        if (driveServiceHelper != null) {
            Log.d("JBL", "Reading file " + fileId);

            driveServiceHelper.readFile(fileId)
                    .addOnSuccessListener(nameAndContent -> {
                        String name = nameAndContent.first;
                        String content = nameAndContent.second;

                        Log.d("JBL", "File name: " + name);
                        Log.d("JBL", "File content: " + content);
                        String[] lines = content.split("\r\n|\r|\n");
                        for (String line : lines) {
                            Log.d("JBL", line);
                        }
                    })
                    .addOnFailureListener(exception ->
                            Log.e("JBL", "Couldn't read file.", exception));
        }
    }

    public Boolean getStoragePermission(int permissionType) {
        switch (permissionType) {
            case Constants.MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            Constants.MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

                    return false;
                } else {
                    // Permission has already been granted
                    return true;
                }
            }
            case Constants.MY_PERMISSIONS_READ_EXTERNAL_STORAGE: {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            Constants.MY_PERMISSIONS_READ_EXTERNAL_STORAGE);

                    return false;
                } else {
                    // Permission has already been granted
                    return true;
                }
            }

        }
        return false;
    }
}