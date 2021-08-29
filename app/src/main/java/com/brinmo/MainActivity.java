package com.brinmo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    static final int GOOGLE_SIGN_IN = 123;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FIRST_OPEN = "First";
    public boolean isFirst;
    private Button mTerms;
    private Button mPrivacy;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check if Google play services is updated
        final GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        final int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            //Status that you are interested is SERVICE_VERSION_UPDATE_REQUIRED
            final Dialog dialog = googleApiAvailability.getErrorDialog(this,status, 1);
            dialog.show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "brinmo_critical";
            String channelName = "Brinmo Critical Updates";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_HIGH));
        }

        setContentView(R.layout.activity_main);

        Button sign_in = findViewById(R.id.sign_in_btn);
        progressBar = findViewById(R.id.progress_circular);
        mTerms = findViewById(R.id.termsbtn);
        mPrivacy = findViewById(R.id.privacybtn);
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        isFirst = sharedPreferences.getBoolean(FIRST_OPEN, true);

        if (mAuth.getCurrentUser() != null) {
            Intent next = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(next);
        } else if (isFirst){
            Intent slide = new Intent(MainActivity.this, FirstActivity.class);
            startActivity(slide);
        } else {
            // i deleted else if isNotFirst the slide activity
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(500);
            sign_in.setVisibility(View.VISIBLE);
            sign_in.startAnimation(alphaAnimation);
            mTerms.setVisibility(View.VISIBLE);
            mTerms.startAnimation(alphaAnimation);
            mPrivacy.setVisibility(View.VISIBLE);
            mPrivacy.startAnimation(alphaAnimation);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 1s = 1000ms
                    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                    ActivityCompat.requestPermissions(MainActivity.this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
                }
            }, 1000);
        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInf = connMgr.getActiveNetworkInfo();
        if (netInf == null || !netInf.isConnected()){
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.INVISIBLE);
        }

        mTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent1 = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://brinmo.com/terms-and-conditions#customers"));
                startActivity(browserIntent1);
            }
        });

        mPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent2 = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://brinmo.com/privacy#customers"));
                startActivity(browserIntent2);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        sign_in.setOnClickListener(v -> SignInGoogle());
    }

    public void SignInGoogle() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(ActivityCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            progressBar.setVisibility(View.VISIBLE);
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
        } else {
            //you probably want to show some prompt saying location is required
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        FirebaseCrashlytics.getInstance().log("firebaseAuthWithGoogle: "+acct.getEmail()+" : "+acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseCrashlytics.getInstance().log("signInWithCredential: success");
                        Intent next = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(next);
                    } else {
                        FirebaseCrashlytics.getInstance().log("signInWithCredential: failed");

                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }
    }


}
