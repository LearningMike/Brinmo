package com.brinmo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.widget.Toolbar;


import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.internal.Version;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    DrawerLayout drawer;
    ImageButton facx;
    ImageButton facb;
    FirebaseUser mAuth;
    StorageReference storageRef;
    ProgressBar progressBar;
    TextView username;
    ImageView proImage;
    String city;
    String country;
    Boolean searchAllowed = false;
    int firstLo = 0;
    Dialog updateDialog;
    ImageView closePopup;

    private final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    LocationManager lm;
    ConnectivityManager connMgr;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String FIRST_SCAN = "firstScan";
    public static final String DARK_MODE = "Dark";
    private boolean isDark;
    Dialog epicDialog;
    ImageView closepopup;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String brinmoVersion;
    FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //this darkness stuff can't be in onResume
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean(DARK_MODE, false);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(HomeActivity.this);
        if (isDark){
            mFirebaseAnalytics.setUserProperty("dark_mode", "true");
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else{
            mFirebaseAnalytics.setUserProperty("dark_mode", "false");
        }

        //IF YOU'RE TESTING SOMETHING GO TO ONRESUME, IT MOSTLY OVERRIDES THIS ONCREATE

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        brinmoVersion = BuildConfig.VERSION_NAME;
        String mUid = mAuth.getUid();
        FirebaseDatabase daab = FirebaseDatabase.getInstance();
        DatabaseReference mUser = daab.getReference("user/"+mUid+"/uname");

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_home, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        progressBar = findViewById(R.id.progress_circular);
        //progressBar.setProgressTintList()
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInf = connMgr.getActiveNetworkInfo();
        if (netInf == null || !netInf.isConnected()){
            TextView noInternetC = findViewById(R.id.internet_unavailable);
            noInternetC.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(HomeActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
        } else if (mAuth == null) {
            Intent nextmain = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(nextmain);
        }

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get and use the values to update the UI
                String uname = dataSnapshot.getValue(String.class);
                if (uname == null){
                    progressBar.setVisibility(View.INVISIBLE);
                    Intent nextp = new Intent(HomeActivity.this, UsernameActivity.class);
                    startActivity(nextp);
                } else {
                    //when user has a username
                    mFirebaseAnalytics = FirebaseAnalytics.getInstance(HomeActivity.this);
                    mFirebaseAnalytics.setUserId(mUid);
                    getLocationPermission();
                    progressBar.setVisibility(View.INVISIBLE);
                    facx.setVisibility(View.VISIBLE);
                    facb.setVisibility(View.VISIBLE);
                    TextView noInternetC = findViewById(R.id.internet_unavailable);
                    noInternetC.setVisibility(View.INVISIBLE);
                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        mUser.addValueEventListener(postListener);

        facx = findViewById(R.id.facx);
        facx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/2349033872404"));
                startActivity(chatIntent);
            }
        });

        facb = findViewById(R.id.facb);
        facb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent me = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(me);
            }
        });

        DatabaseReference mVersion = daab.getReference("criticalupdate/android");

        ValueEventListener versionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get and use the values to update the UI
                String latestVersion = dataSnapshot.getValue(String.class);
                if (brinmoVersion != null && latestVersion!=null){
                    Float brinmoV = Float.parseFloat(brinmoVersion);
                    Float latestV = Float.parseFloat(latestVersion);
                    if (brinmoV < latestV) {
                        FirebaseCrashlytics.getInstance().log("appVersion: critical"+latestV+" ,"+"user"+brinmoV);
                        progressBar.setVisibility(View.INVISIBLE);
                        updateDialog = new Dialog(HomeActivity.this);
                        updateDialog.setContentView(R.layout.updatie);
                        closePopup = (ImageView) updateDialog.findViewById(R.id.closepop);
                        closePopup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String appPackageName = BuildConfig.APPLICATION_ID;
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                updateDialog.dismiss();
                            }
                        });
                        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        updateDialog.show();
                    }
                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        mVersion.addListenerForSingleValueEvent(versionListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //setContentView(R.layout.activity_home);

        //duplicate of onCreate()
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (mAuth.getUid() == null){
            Toast.makeText(HomeActivity.this ,"You have to log in!", Toast.LENGTH_LONG).show();
            Intent login = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(login);
        }
        brinmoVersion = BuildConfig.VERSION_NAME;
        String mUid = mAuth.getUid();
        FirebaseDatabase daab = FirebaseDatabase.getInstance();
        DatabaseReference mUser = daab.getReference("user/"+mUid+"/uname");

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_home, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        progressBar = findViewById(R.id.progress_circular);
        //progressBar.setProgressTintList()
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInf = connMgr.getActiveNetworkInfo();
        if (netInf == null || !netInf.isConnected()){
            //This is the only toast allowed
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
        } else if (mAuth == null) {
            Intent nextmain = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(nextmain);
        }

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get and use the values to update the UI
                String uname = dataSnapshot.getValue(String.class);
                if (uname == null){
                    progressBar.setVisibility(View.INVISIBLE);
                    Intent nextp = new Intent(HomeActivity.this, UsernameActivity.class);
                    startActivity(nextp);
                } else {
                    //when user has a username
                    getLocationPermission();
                    progressBar.setVisibility(View.INVISIBLE);
                    facx.setVisibility(View.VISIBLE);
                    facb.setVisibility(View.VISIBLE);
                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        mUser.addListenerForSingleValueEvent(postListener);

        facx = findViewById(R.id.facx);
        facx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/2349033872404"));
                startActivity(chatIntent);
            }
        });

        facb = findViewById(R.id.facb);
        facb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent me = new Intent(HomeActivity.this, MeActivity.class);
                startActivity(me);
            }
        });

        DatabaseReference mVersion = daab.getReference("criticalupdate/android");

        ValueEventListener versionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get and use the values to update the UI
                String latestVersion = dataSnapshot.getValue(String.class);
                if (brinmoVersion != null && latestVersion!=null){
                    Float brinmoV = Float.parseFloat(brinmoVersion);
                    Float latestV = Float.parseFloat(latestVersion);
                    if (brinmoV < latestV) {
                        FirebaseCrashlytics.getInstance().log("appVersion: critical"+latestV+" ,"+"user"+brinmoV);
                        progressBar.setVisibility(View.INVISIBLE);
                        updateDialog = new Dialog(HomeActivity.this);
                        updateDialog.setContentView(R.layout.updatie);
                        closePopup = (ImageView) updateDialog.findViewById(R.id.closepop);
                        closePopup.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String appPackageName = BuildConfig.APPLICATION_ID;
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                updateDialog.dismiss();
                            }
                        });
                        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        updateDialog.show();
                    }
                }
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        mVersion.addListenerForSingleValueEvent(versionListener);
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(ActivityCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationGetSet();
        } else {
            //you probably want to show some prompt saying location is required
            Toast.makeText(getApplicationContext(), "Location Required", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(HomeActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void locationGetSet(){
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    //save lat and long to db
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    Double latitude = location.getLatitude();
                    Double longitude = location.getLongitude();
                    String locString = latitude.toString() + "-" + longitude.toString();
                    FirebaseCrashlytics.getInstance().log("gotLocation: "+locString);
                    DatabaseReference mLocation = FirebaseDatabase.getInstance().getReference("user/"+mUser.getUid()+"/lat-long");
                    mLocation.setValue(locString);
                    new GetAddress().execute(String.format("%.4f,%.4f", latitude, longitude));
                } else {
                    //just incase it's because the user has location turned off
                    locationEnabled();
                    /**
                     * This would happen if:
                     * Location is turned off in the device settings. The result could be null even if the last location was previously retrieved because disabling location also clears the cache.
                     * The device never recorded its location, which could be the case of a new device or a device that has been restored to factory settings.
                     * Google Play services on the device have restarted, and there is no active Fused Location Provider client that has requested location after the services restarted. To avoid this situation you can create a new client and request location updates yourself.
                     */
                    FirebaseCrashlytics.getInstance().log("locationError: did not get location, so we had to ask ourselves");
                    locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    locationRequest.setInterval(5 * 60 * 1000);
                    locationRequest.setFastestInterval(60 * 1000);
                    locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                FirebaseCrashlytics.getInstance().log("locationError: damn, the new location is null too");
                                return;
                            }
                            for (Location location : locationResult.getLocations()) {
                                if (location != null) {
                                    mFusedLocationClient.removeLocationUpdates(locationCallback);
                                    //save lat and long to db
                                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                    Double latitude = location.getLatitude();
                                    Double longitude = location.getLongitude();
                                    String locString = latitude.toString() + "-" + longitude.toString();
                                    FirebaseCrashlytics.getInstance().log("gotLocation: "+locString);
                                    DatabaseReference mLocation = FirebaseDatabase.getInstance().getReference("user/"+mUser.getUid()+"/lat-long");
                                    mLocation.setValue(locString);
                                    firstLo = firstLo + 1;
                                    FirebaseCrashlytics.getInstance().log("gotLocation: location request count, "+firstLo);
                                    new GetAddress().execute(String.format("%.4f,%.4f", latitude, longitude));
                                } else {
                                    FirebaseCrashlytics.getInstance().log("locationError: Ah shit, new location is null too.");
                                    //might be worse but user should just turnon device location open google maps or restart their phone
                                }
                            }
                        }
                    };
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    private void locationEnabled () {
        lm = (LocationManager)
                getSystemService(Context. LOCATION_SERVICE ) ;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setMessage( "Please turn on your Location." )
                    .setPositiveButton( "Settings" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivity( new Intent(Settings. ACTION_LOCATION_SOURCE_SETTINGS )) ;
                                }
                            })
                    .setNegativeButton("Cancel", new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //GET OLD LOCATION
                                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                                    DatabaseReference uname = FirebaseDatabase.getInstance().getReference("user/"+mUser.getUid()+"/city");
                                    ValueEventListener loListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String dbcity;
                                            if (dataSnapshot.exists()) {
                                                dbcity = dataSnapshot.getValue(String.class);
                                            } else {
                                                dbcity = "unknown";
                                            }

                                            TextView locatio = findViewById(R.id.home_headd);
                                            TextView locati = findViewById(R.id.home_head);
                                            locatio.setText("Discover "+dbcity);
                                            progressBar.setVisibility(View.INVISIBLE);

                                            searchAllowed = true;
                                            city = dbcity;
                                            country = "Nigeria";

                                            if (String.valueOf(locati.getText()).equals("Discover") || String.valueOf(locati.getText()).equals("")) {
                                                AlphaAnimation alphaAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                                                alphaAnimation1.setDuration(100);
                                                locati.startAnimation(alphaAnimation1);
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        locati.setText("Discover " + dbcity);
                                                        AlphaAnimation alphaAnimation2 = new AlphaAnimation(0.0f, 1.0f);
                                                        alphaAnimation2.setDuration(300);
                                                        locati.startAnimation(alphaAnimation2);
                                                    }
                                                }, 100);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            // Getting Post failed, log a message
                                            FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                                            // ...
                                        }
                                    };
                                    uname.addValueEventListener(loListener);
                                }
                            })
                    .show() ;
        }
    }

    public boolean getSearch(){
        return searchAllowed;
    }

    public String getPlace(){
        if (city != null && country != null) {
            return city.toLowerCase() + "-" + country.toLowerCase();
        } else {
            return "yola-nigeria";
        }
    }

    private class GetAddress extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //...
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                double lat = Double.parseDouble(strings[0].split(",")[0]);
                double lng = Double.parseDouble(strings[0].split(",")[1]);
                FirebaseCrashlytics.getInstance().log("gotLocation: "+ lat +" $ "+lng);
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                Address obj = addresses.get(0);
                city = obj.getLocality();
                country = obj.getCountryName();

                FirebaseCrashlytics.getInstance().log("gotLocation: "+city+" % "+country);
            }  catch (Exception ex) {
                ///use old city and country?
                FirebaseCrashlytics.getInstance().recordException(ex);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                FirebaseCrashlytics.getInstance().log("gotlocation: "+city+"_"+country);
                if ((city!=null) && (country!=null)){
                    //location display and animation
                    if (!searchAllowed && (city.length() > 1) && country.length() > 1){
                        searchAllowed = true;

                        //save city and country here
                        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference mLocity = FirebaseDatabase.getInstance().getReference("user/"+mUser.getUid()+"/city");
                        DatabaseReference mLocountry = FirebaseDatabase.getInstance().getReference("user/"+mUser.getUid()+"/country");
                        mLocity.setValue(city);
                        mLocountry.setValue(country);
                        mFirebaseAnalytics.setUserProperty("user_city", city+"-"+country);
                        Bundle logParams = new Bundle();
                        Long currentTime = Calendar.getInstance().getTimeInMillis();
                        logParams.putLong("last_time", currentTime);
                        logParams.putString("city_country", city+"-"+country);
                        mFirebaseAnalytics.logEvent("enter_log", logParams);

                        TextView locatio = findViewById(R.id.home_headd);
                        TextView locati = findViewById(R.id.home_head);
                        locatio.setText("Discover "+city);
                        progressBar.setVisibility(View.INVISIBLE);

                        if (String.valueOf(locati.getText()).equals("Discover") || String.valueOf(locati.getText()).equals("")) {
                            AlphaAnimation alphaAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                            alphaAnimation1.setDuration(100);
                            locati.startAnimation(alphaAnimation1);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    locati.setText("Discover " + city);
                                    AlphaAnimation alphaAnimation2 = new AlphaAnimation(0.0f, 1.0f);
                                    alphaAnimation2.setDuration(300);
                                    locati.startAnimation(alphaAnimation2);
                                }
                            }, 100);
                        }
                    }
                } else {
                    //Unknown Location display and animation
                    FirebaseCrashlytics.getInstance().log("noLocation: "+city+"/"+country);

                    //get old city-country
                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference uname = FirebaseDatabase.getInstance().getReference("user/"+mUser.getUid()+"/city");
                    ValueEventListener loListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String dbcity;
                            if (dataSnapshot.exists()) {
                                dbcity = dataSnapshot.getValue(String.class);
                            } else {
                                dbcity = "unknown";
                            }

                            TextView locatio = findViewById(R.id.home_headd);
                            TextView locati = findViewById(R.id.home_head);
                            locatio.setText("Discover "+dbcity);
                            progressBar.setVisibility(View.INVISIBLE);

                            searchAllowed = true;
                            city = dbcity;
                            country = "Nigeria";

                            if (String.valueOf(locati.getText()).equals("Discover") || String.valueOf(locati.getText()).equals("")) {
                                AlphaAnimation alphaAnimation1 = new AlphaAnimation(1.0f, 0.0f);
                                alphaAnimation1.setDuration(100);
                                locati.startAnimation(alphaAnimation1);
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        locati.setText("Discover " + dbcity);
                                        AlphaAnimation alphaAnimation2 = new AlphaAnimation(0.0f, 1.0f);
                                        alphaAnimation2.setDuration(300);
                                        locati.startAnimation(alphaAnimation2);
                                    }
                                }, 100);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                            // ...
                        }
                    };
                    uname.addValueEventListener(loListener);
                }
            } catch (Exception errr){
                FirebaseCrashlytics.getInstance().recordException(errr);
            }
        }
    }

}
