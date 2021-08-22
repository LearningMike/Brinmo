package com.brinmo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.UiModeManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private static final String SHARED_PREFS = "sharedPrefs";
    public static final String DARK_MODE = "Dark";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton facb = findViewById(R.id.facb);
        facb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                //onBackPressed();
                Intent meIntent = new Intent(SettingsActivity.this, MeActivity.class);
                startActivity(meIntent);
            }
        });

        Button changetheme = findViewById(R.id.changetheme);
        changetheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                switch (SettingsActivity.this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        editor.putBoolean(DARK_MODE, false);
                        editor.apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;

                    case Configuration.UI_MODE_NIGHT_NO:
                        editor.putBoolean(DARK_MODE, true);
                        editor.apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        break;
                }
            }
        });

        //Toast.makeText(SettingsActivity.this, ":"+(SettingsActivity.this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK), Toast.LENGTH_LONG).show();

        Button changeprofpic = findViewById(R.id.profpic);
        changeprofpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(SettingsActivity.this, ProfileimageActivity.class);
                startActivity(imageIntent);
            }
        });

        Button changelocsett = findViewById(R.id.locatsett);
        changelocsett.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(Settings. ACTION_LOCATION_SOURCE_SETTINGS )) ;
            }
        });

        Button changenotsett = findViewById(R.id.notifsett);
        changenotsett.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notintent = new Intent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notintent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    notintent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    notintent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    notintent.putExtra("app_package", getPackageName());
                    notintent.putExtra("app_uid", getApplicationInfo().uid);
                } else {
                    notintent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    notintent.addCategory(Intent.CATEGORY_DEFAULT);
                    notintent.setData(Uri.parse("package:" + getPackageName()));
                }
                startActivity(notintent);
            }
        });
    }
}
