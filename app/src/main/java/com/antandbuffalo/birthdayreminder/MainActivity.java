package com.antandbuffalo.birthdayreminder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.antandbuffalo.birthdayreminder.about.About;
import com.antandbuffalo.birthdayreminder.addnew.AddNew;
import com.antandbuffalo.birthdayreminder.database.DBHelper;
import com.antandbuffalo.birthdayreminder.models.DateOfBirth;
import com.antandbuffalo.birthdayreminder.settings.Settings;
import com.antandbuffalo.birthdayreminder.sharewish.ShareWish;
import com.antandbuffalo.birthdayreminder.upcoming.UpcomingListAdapter;
import com.antandbuffalo.birthdayreminder.update.Update;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.DataHolder;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    UpcomingListAdapter upcomingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initValues();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNew.class);
                startActivityForResult(intent, Constants.ADD_NEW_MEMBER);

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        // Util.copyFromAssetFileToDatabase("cse.txt");

        upcomingListAdapter = new UpcomingListAdapter();
        //http://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        ListView upcomingListView = (ListView)findViewById(R.id.upcomingListView);
        upcomingListView.setAdapter(upcomingListAdapter);

        upcomingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DateOfBirth dateOfBirth = upcomingListAdapter.getItem(position);
                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                CharSequence items[] = new CharSequence[] {"Edit", "Wish"};
                adb.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                startUpdate(position);
                                break;
                            case 1:
                                Intent shareWish = new Intent(MainActivity.this, ShareWish.class);
                                startActivity(shareWish);
                                break;
                        }
                    }
                });
                //adb.setNegativeButton("Cancel", null);
                adb.setTitle("Select Option");
                adb.show();
            }
        });

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
        FirebaseUser firebaseUser = checkFirebaseUser();
        if(firebaseUser != null) {
            // addUserToFirebase(initFirebase(), firebaseUser);
            getAllDocuments(initFirebase(), firebaseUser);
        }
        else {
            Log.d("FirebaseError", "User Not found. Please login first");
        }

        // startFirebaseAuth();
    }

    public void startUpdate(int position) {
        DateOfBirth dateOfBirth = upcomingListAdapter.getItem(position);

        Intent intent = new Intent(MainActivity.this, Update.class);
        intent.putExtra("currentDOB", dateOfBirth);
        startActivityForResult(intent, Constants.DELETE_MEMBER);
    }

    public FirebaseFirestore initFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db;
    }

    public void getAllDocuments(FirebaseFirestore firebaseFirestore, FirebaseUser firebaseUser) {
        firebaseFirestore.collection(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("FirebaseRead", document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d("FirebaseRead", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void addUserToFirebase(FirebaseFirestore db, FirebaseUser firebaseUser) {
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);


        // Add a new document with a generated ID
        db.collection("/" + firebaseUser.getUid() + "/" + firebaseUser.getUid())
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firebase", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "Error adding document", e);
                    }
                });
    }

    public FirebaseUser checkFirebaseUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            System.out.println("User available");
        } else {
            // No user is signed in
            System.out.println("User not available");
        }
        return user;
    }

    public void startFirebaseAuth() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
// Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                Constants.firebaseSignInCode);
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
        else if(id == R.id.action_about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == Constants.firebaseSignInCode) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                System.out.println(user.getDisplayName());
                System.out.println(user.getEmail());
                System.out.println(user.getPhoneNumber());
                System.out.println(user.getProviderId());
                System.out.println(user.getUid());

                Log.d("FirebaseAuth", user.getDisplayName());
                Log.d("FirebaseAuth", user.getEmail());
                Log.d("FirebaseAuth", user.getPhoneNumber() + " :a");
                Log.d("FirebaseAuth", user.getProviderId());
                Log.d("FirebaseAuth", user.getUid());

            } else {
                Toast.makeText(DataHolder.getInstance().getAppContext(), "Not able to sign in", Toast.LENGTH_SHORT).show();
            }
         }
         else if(requestCode == Constants.DELETE_MEMBER) {
             System.out.println("after delete activity");
             if(resultCode == RESULT_OK) {
                 upcomingListAdapter.refreshData();
             }
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

    @Override
    protected void onResume() {
        super.onResume();
        if(DataHolder.getInstance().refresh) {
            upcomingListAdapter.refreshData();
            DataHolder.getInstance().refresh = false;
        }
    }
}

// Get all documents
// https://firebase.google.com/docs/firestore/query-data/get-data#java_2
// add document with custom id
// https://stackoverflow.com/questions/48541270/how-to-add-document-with-custom-id-to-firestore
// auth rules
// https://firebase.google.com/docs/rules/basics?authuser=0#cloud-firestore
// checking user
// https://firebase.google.com/docs/auth/android/manage-users?authuser=0