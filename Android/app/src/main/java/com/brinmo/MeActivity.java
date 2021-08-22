package com.brinmo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class MeActivity extends AppCompatActivity {

    StorageReference storageRef;
    String username;
    TextView dateView;
    FirebaseAnalytics mFirebaseAnalytics;
    int tap_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        dateView = findViewById(R.id.dateView);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MeActivity.this);

        ImageButton facb = findViewById(R.id.facb);
        facb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //What to do on back clicked
                onBackPressed();
            }
        });

        //this should be made into the profileimagebutton later
        ImageButton fash = findViewById(R.id.fash);
        fash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "https://brinmo.com/app/download");
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
            }
        });

        //get profile image
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        String imageurii = "https://firebasestorage.googleapis.com/v0/b/bizzybody-90f3e.appspot.com/o/customer%2F"+mUser.getUid()+"%2FprofileImage_600x600.png?alt=media";
        ImageView cimageview = (ImageView) findViewById(R.id.cimage);
        Picasso.get().load(imageurii).fit().centerCrop().into(cimageview);

        storageRef = FirebaseStorage.getInstance().getReference("customer/"+mUser.getUid()+"/profileImage_600x600.png");
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                mFirebaseAnalytics.setUserProperty("profile_image", "true");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                ImageView cimageview = (ImageView) findViewById(R.id.cimage);
                cimageview.setImageResource(R.drawable.mphonei);
                mFirebaseAnalytics.setUserProperty("profile_image", "false");
            }
        });

        //get username
        DatabaseReference uname = FirebaseDatabase.getInstance().getReference("user/"+mUser.getUid()+"/uname");
        ValueEventListener bpostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = dataSnapshot.getValue(String.class);
                TextView cname = findViewById(R.id.cname);
                cname.setText(username);
                Long createdTime = Objects.requireNonNull(mUser.getMetadata()).getCreationTimestamp();
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(createdTime);
                int mYear = c.get(Calendar.YEAR);
                SimpleDateFormat month_date = new SimpleDateFormat("MMM");
                String mMonthtext = month_date.format(c.getTime());
                String userJoined = "joined " + mMonthtext + ", "+ mYear;
                mFirebaseAnalytics.setUserProperty("date_joined", mMonthtext+"-"+mYear);
                dateView.setText(userJoined);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        uname.addListenerForSingleValueEvent(bpostListener);

        //get joined date //don't forget to save the joined year

        Button history = (Button) findViewById(R.id.me_history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MeActivity.this, "I'm working on it", Toast.LENGTH_SHORT).show();
            }
        });

        Button settings = (Button) findViewById(R.id.me_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settIntent = new Intent(MeActivity.this, SettingsActivity.class);
                startActivity(settIntent);
            }
        });

        Button addmy = (Button) findViewById(R.id.me_addmy);
        addmy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addmyIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://brinmo.com?utm_source="+username));
                startActivity(addmyIntent);
            }
        });

        Button invite = (Button) findViewById(R.id.me_invite);
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inviteIntent = new Intent(Intent.ACTION_SEND);
                inviteIntent.putExtra(Intent.EXTRA_TEXT,
                        "https://brinmo.com/?utm_source="+username);
                inviteIntent.setType("text/plain");
                startActivity(Intent.createChooser(inviteIntent, "Invite a business"));
            }
        });

        Button report = (Button) findViewById(R.id.me_report);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent reportIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://chat.whatsapp.com/Cv30Ou92wJw7k7dnjwjddt"));
                startActivity(reportIntent);
            }
        });

        Button privacy = (Button) findViewById(R.id.me_privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent privacyIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://brinmo.com/privacy?utm_source="+username+"#customers"));
                startActivity(privacyIntent);
            }
        });

        Button terms = (Button) findViewById(R.id.me_terms);
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent termsIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://brinmo.com/terms-and-conditions?utm_source="+username+"#customers"));
                startActivity(termsIntent);
            }
        });

        tap_count = 0;

        Button easteregg = (Button) findViewById(R.id.easter_egg);
        easteregg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tap_count = tap_count + 1;
                if (tap_count == 6) {
                    //send to an easteregg or test feature
                    Intent eggIntent = new Intent(MeActivity.this, ProfileimageActivity.class);
                    startActivity(eggIntent);
                }
            }
        });
    }
}
