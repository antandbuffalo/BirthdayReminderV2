package com.antandbuffalo.birthdayreminder.wishtemplate;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.PopupDialog;
import com.antandbuffalo.birthdayreminder.utilities.Storage;
import com.antandbuffalo.birthdayreminder.utilities.UIUtil;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Date;
import java.util.List;

// My wishes: up to five saved wishes; the ticked one is filled in by Send wish
public class WishTemplate extends AppCompatActivity {
    AdView mAdView;
    private List<String> wishes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wish_template);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView heading = findViewById(R.id.wishHeading);
        heading.setText("My wishes");
        wishes = Storage.getWishes();
        buildList();

        loadAd();
        showSnowFlakes();
    }

    // tapping a row makes it the default; the pencil opens the edit pop-up
    private void buildList() {
        LinearLayout list = findViewById(R.id.wishList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < wishes.size(); i++) {
            View row = inflater.inflate(R.layout.wish_row, list, false);
            TextView text = row.findViewById(R.id.wishText);
            text.setText(wishes.get(i));
            if (isDefault(i)) {
                ((RadioButton) row.findViewById(R.id.wishRadio)).setChecked(true);
                text.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                row.findViewById(R.id.wishDefault).setVisibility(View.VISIBLE);
            }
            final int index = i;
            row.setOnClickListener(v -> makeDefault(index));
            row.findViewById(R.id.wishEdit).setOnClickListener(v -> showEditor(index));
            list.addView(row);
        }

        if (wishes.size() < Constants.MAX_WISHES) {
            View add = inflater.inflate(R.layout.wish_add_row, list, false);
            ((TextView) add.findViewById(R.id.wishCount)).setText(wishes.size() + " of " + Constants.MAX_WISHES);
            add.setOnClickListener(v -> showEditor(-1));
            list.addView(add);
        }
    }

    private boolean isDefault(int index) {
        return wishes.get(index).equals(Storage.getWishTemplate());
    }

    private void makeDefault(int index) {
        if (isDefault(index)) {
            return;
        }
        Storage.setWishTemplate(wishes.get(index));
        saveList();
    }

    // index -1 adds a new wish
    private void showEditor(int index) {
        boolean adding = index < 0;

        View view = getLayoutInflater().inflate(R.layout.dialog_wish, null);
        ((TextView) view.findViewById(R.id.wishDialogTitle)).setText(adding ? "Add a wish" : "Edit wish");
        EditText input = view.findViewById(R.id.wishDialogText);
        Button save = view.findViewById(R.id.wishDialogSave);
        Button delete = view.findViewById(R.id.wishDialogDelete);

        if (!adding) {
            input.setText(wishes.get(index));
            input.setSelection(input.getText().length());
        }
        UIUtil.setEnabledDimmed(save, input.getText().toString().trim().length() > 0);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                UIUtil.setEnabledDimmed(save, s.toString().trim().length() > 0);
            }
        });
        // keep at least one wish so Send wish always has something to fill in
        delete.setVisibility(adding || wishes.size() <= 1 ? View.GONE : View.VISIBLE);

        AlertDialog dialog = PopupDialog.create(this, view);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        view.findViewById(R.id.wishDialogCancel).setOnClickListener(v -> dialog.dismiss());
        save.setOnClickListener(v -> {
            dialog.dismiss();
            saveWish(index, input.getText().toString().trim());
        });
        delete.setOnClickListener(v -> {
            dialog.dismiss();
            deleteWish(index);
        });
        dialog.show();
        input.requestFocus();
    }

    private void saveWish(int index, String text) {
        if (index < 0) {
            wishes.add(text);
            toast("Wish added");
        } else {
            if (isDefault(index)) {
                Storage.setWishTemplate(text);
            }
            wishes.set(index, text);
            toast("Wish saved");
        }
        saveList();
    }

    private void deleteWish(int index) {
        boolean wasDefault = isDefault(index);
        wishes.remove(index);
        if (wasDefault) {
            Storage.setWishTemplate(wishes.get(0));
        }
        toast(wasDefault ? "Wish deleted. Send wish now uses the first one" : "Wish deleted");
        saveList();
    }

    // stores the list, marks local data as changed so auto sync backs it up, and redraws
    private void saveList() {
        Storage.setWishes(wishes);
        Storage.setDbBackupTime(new Date());
        buildList();
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void loadAd() {
        mAdView = this.findViewById(R.id.adView);
        if(!Constants.enableAds) {
            mAdView.setVisibility(View.INVISIBLE);
            return;
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void showSnowFlakes() {
        if(Util.showSnow()) {
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
