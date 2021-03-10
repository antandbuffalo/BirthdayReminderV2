package com.antandbuffalo.birthdayreminder.about;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.antandbuffalo.birthdayreminder.BuildConfig;
import com.antandbuffalo.birthdayreminder.R;
import com.antandbuffalo.birthdayreminder.utilities.Constants;
import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        TextView version = (TextView)findViewById(R.id.version);
        version.setText("Version " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");

        AdView mAdView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Button contactUs = (Button)findViewById(R.id.contactUs);
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                emailIntent.setType("vnd.android.cursor.item/email");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{Constants.FEEDBACK_EMAIL});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, Constants.FEEDBACK_EMAIL_SUBJECT + " v" + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
                //emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, Constants.FEEDBACK_EMAIL_POPUP_MESSAGE));
                //startActivity(emailIntent);
            }
        });

        Button share = (Button)findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create the sharing intent
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "https://play.google.com/store/apps/details?id=com.antandbuffalo.birthdayreminder";
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share..."));
            }
        });

        showSnowFlakes();
    }

    public void showSnowFlakes() {
        if(Util.isHappyBirthday()) {
            View snowFlakes = this.findViewById(R.id.snowFlakes);
            snowFlakes.setVisibility(View.VISIBLE);
            Util.showHappyBirthdayNotification(this);
        }
    }

}
