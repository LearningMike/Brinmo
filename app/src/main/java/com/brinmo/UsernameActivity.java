package com.brinmo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UsernameActivity extends AppCompatActivity {

    FirebaseUser mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        Button checkpu = findViewById(R.id.checkpu);
        TextView puseri = findViewById(R.id.puseri);
        EditText pusere = findViewById(R.id.pusere);
        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (mAuth == null) {
            Intent nextmain = new Intent(UsernameActivity.this, MainActivity.class);
            startActivity(nextmain);
        }
        String mUid = mAuth.getUid();
        FirebaseDatabase daab = FirebaseDatabase.getInstance();

        checkpu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pusername = "@" + pusere.getText().toString().trim().replaceAll(" ", "_").toLowerCase();
                if (pusername.length() < 5){
                    puseri.setText(R.string.puseri_len);
                } else if (pusername.contains(".") || pusername.contains(",")){
                    puseri.setText("underscores are the only symbols allowed");
                } else {
                    //checking if username exists
                    progressBar = findViewById(R.id.progress_circular);
                    progressBar.setVisibility(View.VISIBLE);
                    DatabaseReference mUsername = daab.getReference("usernames/"+pusername);
                    ValueEventListener post2Listener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean uname = dataSnapshot.getValue(Boolean.class);
                            if (uname != null){
                                puseri.setText(R.string.puseri_tak);
                                progressBar.setVisibility(View.INVISIBLE);
                            } else {
                                //save the username
                                DatabaseReference mUsernamer = daab.getReference("usernames/");
                                DatabaseReference mUsernamerer = daab.getReference("user/"+mUid);
                                mUsernamer.child(pusername).setValue(true);
                                mUsernamerer.child("uname").setValue(pusername);
                                FirebaseCrashlytics.getInstance().setUserId(pusername);
                                Intent nextp = new Intent(UsernameActivity.this, ProfileimageActivity.class);
                                startActivity(nextp);
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
                    mUsername.addListenerForSingleValueEvent(post2Listener);
                }
            }
        });

        //check if user has username
        DatabaseReference uname = FirebaseDatabase.getInstance().getReference("user/"+mUid+"/uname");
        ValueEventListener loListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);

                if (username != null) {
                    FirebaseCrashlytics.getInstance().setUserId(username);
                    Intent nextp = new Intent(UsernameActivity.this, HomeActivity.class);
                    startActivity(nextp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        uname.addListenerForSingleValueEvent(loListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        if (mAuth == null) {
            Intent nextmain = new Intent(UsernameActivity.this, MainActivity.class);
            startActivity(nextmain);
        }
        String mUid = mAuth.getUid();

        //check if user has username
        DatabaseReference uname = FirebaseDatabase.getInstance().getReference("user/"+mUid+"/uname");
        ValueEventListener loListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String username = dataSnapshot.getValue(String.class);

                if (username != null) {
                    FirebaseCrashlytics.getInstance().setUserId(username);
                    Intent nextp = new Intent(UsernameActivity.this, HomeActivity.class);
                    startActivity(nextp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        uname.addListenerForSingleValueEvent(loListener);
    }
}
