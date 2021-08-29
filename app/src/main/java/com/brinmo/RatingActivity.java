package com.brinmo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RatingActivity extends AppCompatActivity {

    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    ProgressBar progressBar;
    Bundle bundle;
    String bizid, uname;
    String businessn, businesst;
    String actiontext;
    String lstrv;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        bundle  = getIntent().getExtras();
        bizid = bundle.getString("bid");

        Button ratebiz = findViewById(R.id.ratebiz);//submit review
        Button skipu = findViewById(R.id.skipu);//skip review
        TextView puseri = findViewById(R.id.puseri);//any error
        TextView pusern = findViewById(R.id.pusern);//action text
        ImageButton like = findViewById(R.id.liked);
        ImageButton dislike = findViewById(R.id.disliked);
        LinearLayout ratingbox = findViewById(R.id.ratingbox);
        LinearLayout reviewbox = findViewById(R.id.reviewbox);
        EditText pusere = findViewById(R.id.pusere);//review text

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(RatingActivity.this);

        DatabaseReference bzname = FirebaseDatabase.getInstance().getReference("businessinfo/"+bizid);
        ValueEventListener bzpostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the business name and title
                businessn = dataSnapshot.child("name").getValue(String.class);
                businesst = dataSnapshot.child("title").getValue(String.class);
                actiontext = "Rate "+businessn;
                // set the header
                pusern.setText(actiontext);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        bzname.addListenerForSingleValueEvent(bzpostListener);

        //get the username for the review
        DatabaseReference getname = FirebaseDatabase.getInstance().getReference("user/"+mAuth.getUid()+"/uname");
        ValueEventListener listname = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the business name and title
                uname = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        getname.addListenerForSingleValueEvent(listname);

        DatabaseReference bzrstuff = FirebaseDatabase.getInstance().getReference("bizcustomers/"+bizid+"/"+mAuth.getUid()+"/satisfied");
        ValueEventListener bzrpostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if userid is a bizcustomer of of bizid
                String satis = dataSnapshot.getValue(String.class);
                FirebaseCrashlytics.getInstance().log("bizCustomerSatisfied: "+satis);
                if (true){
                    like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //set positive rating, add to cusbusinesses satisfied list, remove from notsatisfied list
                            bzrstuff.setValue("1");
                            DatabaseReference mDatabs = FirebaseDatabase.getInstance().getReference("cusbusinesses/"+mAuth.getUid()+"/satisfied/"+bizid);
                            HashMap<String, Object> satsinf = new HashMap<>();
                            satsinf.put("n", businessn);
                            satsinf.put("t", businesst);
                            satsinf.put("l", ServerValue.TIMESTAMP);
                            mDatabs.setValue(true);
                            DatabaseReference mDatabn = FirebaseDatabase.getInstance().getReference("cusbusinesses/"+mAuth.getUid()+"/notsatisfied/"+bizid);
                            mDatabn.removeValue();
                            DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/"+mAuth.getUid()+"/added/"+bizid);
                            mDataad.setValue(satsinf);
                            //Switch to Review
                            ratingbox.setVisibility(View.GONE);
                            reviewbox.setVisibility(View.VISIBLE);
                            pusere.setHint("Why did you like " + businessn + " ?");
                            //load last review
                            DatabaseReference mDatarev = FirebaseDatabase.getInstance().getReference("reviews/"+bizid+"/"+mAuth.getUid()+"/rev");
                            ValueEventListener drpostListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get the last review
                                    lstrv = dataSnapshot.getValue(String.class);
                                    if (lstrv != null) {
                                        //if the last review exists, load it
                                        pusere.setText(lstrv);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                                    // ...
                                }
                            };
                            mDatarev.addListenerForSingleValueEvent(drpostListener);

                            ratebiz.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String review = pusere.getText().toString();
                                    FirebaseCrashlytics.getInstance().log("reviewBiz: "+review);
                                    if (review.length() < 4){
                                        //just skip
                                        FirebaseCrashlytics.getInstance().log("reviewBiz: skipped");
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Do something after 1s = 1000ms
                                                Intent intentskip = new Intent(RatingActivity.this, HomeActivity.class);
                                                startActivity(intentskip);
                                            }
                                        }, 2000);
                                    } else {
                                        //submit the review then skip
                                        String newrev = pusere.getText().toString();
                                        FirebaseCrashlytics.getInstance().log("reviewBiz: "+bizid+" by "+mAuth.getUid());
                                        DatabaseReference mDatanewrev = FirebaseDatabase.getInstance().getReference("reviews/"+bizid+"/"+mAuth.getUid());
                                        HashMap<String, Object> revinf = new HashMap<>();
                                        revinf.put("cname", uname);
                                        revinf.put("rev", newrev);
                                        revinf.put("time", ServerValue.TIMESTAMP);
                                        mDatanewrev.setValue(revinf);
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Do something after 1s = 1000ms
                                                Intent intentmove = new Intent(RatingActivity.this, HomeActivity.class);
                                                startActivity(intentmove);
                                            }
                                        }, 2000);
                                    }
                                }
                            });
                        }
                    });

                    dislike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //set negative rating, add to cusbusinesses notsatisfied list, remove from notsatisfied list
                            bzrstuff.setValue("0");
                            DatabaseReference mDatabn = FirebaseDatabase.getInstance().getReference("cusbusinesses/"+mAuth.getUid()+"/notsatisfied/"+bizid);
                            HashMap<String, Object> satsinf = new HashMap<>();
                            satsinf.put("n", businessn);
                            satsinf.put("t", businesst);
                            satsinf.put("l", ServerValue.TIMESTAMP);
                            mDatabn.setValue(true);
                            DatabaseReference mDatabs = FirebaseDatabase.getInstance().getReference("cusbusinesses/"+mAuth.getUid()+"/satisfied/"+bizid);
                            mDatabs.removeValue();
                            DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/"+mAuth.getUid()+"/added/"+bizid);
                            mDataad.removeValue();
                            Bundle dissParams = new Bundle();
                            if (satis.equals("1")){
                                dissParams.putBoolean("was_satisfied", true);
                                dissParams.putString("biz_name", businessn);
                                dissParams.putString("biz_title", businesst);
                                mFirebaseAnalytics.logEvent("dissapointed", dissParams);
                            } else {
                                dissParams.putBoolean("was_satisfied", false);
                                dissParams.putString("biz_name", businessn);
                                dissParams.putString("biz_title", businesst);
                                mFirebaseAnalytics.logEvent("dissapointed", dissParams);
                            }

                            //Switch to Review
                            ratingbox.setVisibility(View.GONE);
                            reviewbox.setVisibility(View.VISIBLE);
                            pusere.setHint("Why did you dislike " + businessn + " ?");

                            //load last review
                            DatabaseReference mDatarev = FirebaseDatabase.getInstance().getReference("reviews/"+bizid+"/"+mAuth.getUid()+"/rev");
                            ValueEventListener drpostListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get the last review
                                    lstrv = dataSnapshot.getValue(String.class);
                                    if (lstrv != null) {
                                        //if the last review exists, load it
                                        pusere.setText(lstrv);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                                    // ...
                                }
                            };
                            mDatarev.addListenerForSingleValueEvent(drpostListener);

                            ratebiz.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String review = pusere.getText().toString();
                                    if (review.length() < 4){
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Do something after 1s = 1000ms
                                                Intent intentskip = new Intent(RatingActivity.this, HomeActivity.class);
                                                startActivity(intentskip);
                                            }
                                        }, 2000);
                                    } else {
                                        //submit the review then skip
                                        String newrev = String.valueOf(pusere.getText());
                                        DatabaseReference mDatanewrev = FirebaseDatabase.getInstance().getReference("reviews/"+bizid+"/"+mAuth.getUid());
                                        HashMap<String, Object> revinf = new HashMap<>();
                                        revinf.put("cname", uname);
                                        revinf.put("rev", newrev);
                                        revinf.put("time", ServerValue.TIMESTAMP);
                                        mDatanewrev.setValue(revinf);
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Do something after 1s = 1000ms
                                                Intent intentskip = new Intent(RatingActivity.this, HomeActivity.class);
                                                startActivity(intentskip);
                                            }
                                        }, 2000);
                                    }
                                }
                            });
                        }
                    });

                    skipu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Do something after 1s = 1000ms
                                    Intent intentskip = new Intent(RatingActivity.this, HomeActivity.class);
                                    startActivity(intentskip);
                                }
                            }, 2000);
                        }
                    });

                } else {
                    //when a non-customer wants to review, skip
                    Intent intentskip = new Intent(RatingActivity.this, BusinessActivity.class);
                    intentskip.putExtra("bizid", bizid);
                    startActivity(intentskip);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        bzrstuff.addListenerForSingleValueEvent(bzrpostListener);


    }
}
