package com.brinmo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class BusinessActivity extends AppCompatActivity {

    TextView biznameview;
    TextView biztitleview;
    TextView bizdescview;
    TextView starView;
    TextView xView;
    ImageView bizimageview;
    Button bizchattap;
    Button bizordertap;
    Button bizpaytap;
    FirebaseUser mAuth;
    String bizidx;
    String biznamex, bizcity;
    String biztitlex;
    Long bizstarx;
    Long bizxx;
    String bizdescriptionx;
    String bizcatx;
    String bizphonenumberx;
    String bizwebid;
    StorageReference storageRef;
    BufferedReader reader;
    Boolean bnmsab, bnmnsb;
    String bnmnadb;
    ImageButton buttonr, button2, button3;
    TextView addText;
    String bizlink;
    FirebaseAnalytics mFirebaseAnalytics;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private com.brinmo.ReviewAdapter adapter;
    private List<Review> reviewList;
    String reviewPath;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String FIRST_SCAN = "firstScan";
    public static final String DARK_MODE = "Dark";
    private boolean isDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean(DARK_MODE, false);
        if (isDark){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);

        //Ignore the ugly loops  you see here
        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();//useless, but leave here
        Uri appLinkData = appLinkIntent.getData();

        biznameview = (TextView) findViewById(R.id.bizname);
        biztitleview = (TextView) findViewById(R.id.biztitle);
        //bizdescview = (TextView) findViewById(R.id.bizdesc);
        bizimageview = (ImageView) findViewById(R.id.bizimage);
        starView = (TextView) findViewById(R.id.starView);
        xView = (TextView) findViewById(R.id.xView);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(BusinessActivity.this);
        if (mAuth == null) {
            Intent intenth = new Intent(BusinessActivity.this, HomeActivity.class);
            startActivity(intenth);
        }

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        if (bundle.getString("bizid") == null) {
            //IF GOTTEN THROUGH LINK
            String bizidl = appLinkData.getLastPathSegment();
            bizlink = bizidl;
            FirebaseCrashlytics.getInstance().log("bizId: " + bizidl);

            //check if bizidl contains -by-
            if (bizidl.contains("-by-")) {
                String[] bizArray = bizidl.split("-by-");
                bizidl = bizArray[1];
            }

            //if gotten through applink
            ImageButton button = findViewById(R.id.facb);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intenth = new Intent(BusinessActivity.this, HomeActivity.class);
                    startActivity(intenth);
                }
            });

            //get the id
            DatabaseReference getbizid = FirebaseDatabase.getInstance().getReference("businessid/" + bizidl);
            ValueEventListener listbiz = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get the business id
                    bizidx = dataSnapshot.getValue(String.class);

                    //get stuff from account
                    FirebaseDatabase daab = FirebaseDatabase.getInstance();
                    DatabaseReference bizstuff = daab.getReference("businessinfo/" + bizidx);
                    ValueEventListener postListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get and use the values to update the UI
                            biznamex = dataSnapshot.child("name").getValue(String.class);
                            biztitlex = dataSnapshot.child("title").getValue(String.class);
                            bizstarx = dataSnapshot.child("satisfied").getValue(Long.class);
                            bizxx = dataSnapshot.child("nofcustomers").getValue(Long.class);
                            bizcity = dataSnapshot.child("cityname").getValue(String.class);
                            String biznamelow = biznamex.toLowerCase();
                            String biznamelast = biznamelow.replaceAll(" ", "-");
                            String bizcitylow = bizcity.toLowerCase();
                            String bizcitylast = bizcitylow.replaceAll(" ", "-");
                            bizwebid = biznamelast + "-" + bizcitylast;
                            //bizdescriptionx = dataSnapshot.child("description").getValue(String.class);

                            biznameview.setText(biznamex);
                            biztitleview.setText(biztitlex);
                            //bizdescview.setText(bizdescriptionx);
                            if (bizstarx < 1) {
                                if (bizxx == 0) {
                                    starView.setText("Unrated");
                                } else if (bizxx >= 1) {
                                    starView.setText("Toxic");
                                    //send a notice
                                }
                            } else if (((bizstarx / bizxx) * 10) <= 2) {
                                starView.setText(HtmlCompat.fromHtml("&#9733;&#9734;&#9734;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else if (((bizstarx / bizxx) * 10) <= 4) {
                                starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9734;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else if (((bizstarx / bizxx) * 10) <= 6) {
                                starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else if (((bizstarx / bizxx) * 10) <= 8) {
                                starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9733;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                            } else if (((bizstarx / bizxx) * 10) <= 10) {
                                starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9733;&#9733;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                            }
                            xView.setText("" + bizxx);
                            
                            Long bizratx;
                            if (bizxx != 0){
                                bizratx = (bizstarx/bizxx)*5;
                            } else {
                                bizratx = bizxx;
                            }
                            //this is for when the a business is opened through link only
                            Bundle openParams = new Bundle();
                            openParams.putString("biz_link", bizlink);
                            openParams.putString("biz_category", bizcatx);
                            openParams.putLong("biz_stars", bizratx);
                            openParams.putLong("biz_customers", bizxx);
                            mFirebaseAnalytics.logEvent("share_open", openParams);

                            String imageurii = "https://firebasestorage.googleapis.com/v0/b/bizzybody-90f3e.appspot.com/o/business%2F"+bizidx+"%2FprofileImage1_600x600.png?alt=media";
                            Picasso.get().load(imageurii).fit().centerCrop().into(bizimageview);

                            //get phonenumber
                            DatabaseReference mPhoneRef = FirebaseDatabase.getInstance().getReference("businesscontact/" + bizidx + "/pnumber");
                            ValueEventListener phoneListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get Post object and use the values to update the UI
                                    bizphonenumberx = dataSnapshot.getValue(String.class);
                                    // ...
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                                    // ...
                                }
                            };
                            mPhoneRef.addListenerForSingleValueEvent(phoneListener);

                            //get business category
                            DatabaseReference mCatRef = FirebaseDatabase.getInstance().getReference("searchface/" + bizidx + "/category");
                            ValueEventListener catListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get Post object and use the values to update the UI
                                    bizcatx = dataSnapshot.getValue(String.class);
                                    // ...
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                                    // ...
                                }
                            };
                            mCatRef.addListenerForSingleValueEvent(catListener);

                            // Get latest review
                            Long datenow = new Date().getTime();
                            Query queryr = FirebaseDatabase.getInstance().getReference("reviews/" + bizidx).orderByChild("time").endAt(datenow).limitToLast(1);
                            queryr.addListenerForSingleValueEvent(valueEventListener);

                            bizchattap = findViewById(R.id.bizchat);
                            bizchattap.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bundle shareParams = new Bundle();
                                    shareParams.putString("biz_name", biznamex);
                                    shareParams.putString("biz_category", bizcatx);
                                    shareParams.putLong("biz_stars", bizratx);
                                    shareParams.putLong("biz_customers", bizxx);
                                    mFirebaseAnalytics.logEvent("call_tap", shareParams);

                                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", bizphonenumberx, null));
                                    startActivity(intent);

                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                                            "https://brinmo.com/b/" + bizwebid);
                                    shareIntent.setType("text/plain");
                                    startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
                                }
                            });

                            bizordertap = findViewById(R.id.bizorder);
                            bizordertap.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bundle orderParams = new Bundle();
                                    orderParams.putString("biz_name", biznamex);
                                    orderParams.putString("biz_category", bizcatx);
                                    orderParams.putLong("biz_stars", bizratx);
                                    orderParams.putLong("biz_customers", bizxx);
                                    mFirebaseAnalytics.logEvent("order_tap", orderParams);

                                    if (bizcatx == "transport" && bizphonenumberx != null) {
                                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", bizphonenumberx, null));
                                        startActivity(intent);
                                    }
                                    if (bizcity != null) {
                                        Intent intenti = new Intent(BusinessActivity.this, InventoryActivity.class);
                                        intenti.putExtra("bid", bizidx);
                                        startActivity(intenti);
                                    } else {
                                        Toast.makeText(BusinessActivity.this, "This Business is Unverified", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            bizpaytap = findViewById(R.id.bizpay);
                            bizpaytap.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Bundle payParams = new Bundle();
                                    payParams.putString("biz_name", biznamex);
                                    payParams.putString("biz_category", bizcatx);
                                    payParams.putLong("biz_stars", bizratx);
                                    payParams.putLong("biz_customers", bizxx);
                                    mFirebaseAnalytics.logEvent("pay_tap", payParams);

                                    Intent intentp = new Intent(BusinessActivity.this, AmountActivity.class);
                                    intentp.putExtra("bid", bizidx);
                                    startActivity(intentp);
                                }
                            });

                            //check if user has business on any list
                            DatabaseReference mDatasab = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/satisfied/" + bizidx);
                            ValueEventListener sabpostListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get the business name from satisfied
                                    bnmsab = dataSnapshot.getValue(Boolean.class);
                                    DatabaseReference mDatansb = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/notsatisfied/" + bizidx);
                                    ValueEventListener nsbpostListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // Get the business name from notsatisfied
                                            bnmnsb = dataSnapshot.getValue(Boolean.class);
                                            DatabaseReference mDataadb = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx + "/n");
                                            ValueEventListener adbpostListener = new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    // Get the business name from added
                                                    bnmnadb = dataSnapshot.getValue(String.class);
                                                    FirebaseCrashlytics.getInstance().log("satsf-nsatf-added(biz): " + bnmsab + " : " + bnmnsb + " : " + bnmnadb + "(" + biznamex + ")");
                                                    if (bnmsab != null || bnmnsb != null) {
                                                        //If the business has been paid
                                                        buttonr = findViewById(R.id.facsh);
                                                        buttonr.setVisibility(View.VISIBLE);
                                                        buttonr.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {

                                                                Bundle shareParams = new Bundle();
                                                                shareParams.putString("biz_name", biznamex);
                                                                shareParams.putString("biz_category", bizcatx);
                                                                shareParams.putLong("biz_stars", bizratx);
                                                                shareParams.putLong("biz_customers", bizxx);
                                                                mFirebaseAnalytics.logEvent("share_tap", shareParams);

                                                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                                shareIntent.putExtra(Intent.EXTRA_TEXT,
                                                                        "https://brinmo.com/b/" + bizwebid);
                                                                shareIntent.setType("text/plain");
                                                                startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
                                                            }
                                                        });

                                                    } else if (bnmnadb != null) {
                                                        //if the business is on a list but not used before
                                                        button3 = findViewById(R.id.facs);
                                                        button3.setVisibility(View.VISIBLE);
                                                        button3.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                //Toast.makeText(BusinessActivity.this, "Removed from your list", Toast.LENGTH_SHORT).show();
                                                                button3.setVisibility(View.GONE);
                                                                DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                                mDataad.removeValue();
                                                                button2.setVisibility(View.VISIBLE);
                                                                buttonr.setVisibility(View.INVISIBLE);
                                                            }
                                                        });

                                                        buttonr = findViewById(R.id.facsh);
                                                        buttonr.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Bundle shareParams = new Bundle();
                                                                shareParams.putString("biz_name", biznamex);
                                                                shareParams.putString("biz_category", bizcatx);
                                                                shareParams.putLong("biz_stars", bizratx);
                                                                shareParams.putLong("biz_customers", bizxx);
                                                                mFirebaseAnalytics.logEvent("share_tap", shareParams);

                                                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                                shareIntent.putExtra(Intent.EXTRA_TEXT,
                                                                        "https://brinmo.com/b/" + bizwebid);
                                                                shareIntent.setType("text/plain");
                                                                startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
                                                            }
                                                        });

                                                        button2 = findViewById(R.id.faca);
                                                        button2.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                //Toast.makeText(BusinessActivity.this, "Added to your list", Toast.LENGTH_SHORT).show();
                                                                button2.setVisibility(View.GONE);
                                                                DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                                HashMap<String, Object> satsinf = new HashMap<>();
                                                                satsinf.put("n", biznamex);
                                                                satsinf.put("t", biztitlex);
                                                                satsinf.put("l", ServerValue.TIMESTAMP);
                                                                mDataad.setValue(satsinf);
                                                                button3.setVisibility(View.VISIBLE);
                                                                //buttonr.setVisibility(View.VISIBLE);

                                                                Bundle addParams = new Bundle();
                                                                addParams.putString("biz_name", biznamex);
                                                                addParams.putString("biz_category", bizcatx);
                                                                addParams.putLong("biz_stars", bizratx);
                                                                addParams.putLong("biz_customers", bizxx);
                                                                mFirebaseAnalytics.logEvent("add_tap", addParams);

                                                                final Handler handler = new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        // Do something after 1s = 1000ms
                                                                        Intent intentskip = new Intent(BusinessActivity.this, HomeActivity.class);
                                                                        startActivity(intentskip);
                                                                    }
                                                                }, 2000);
                                                            }
                                                        });
                                                    } else if (bnmnadb == null) {
                                                        //if the business isn't on any of the customer's list
                                                        button3 = findViewById(R.id.facs);
                                                        button3.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                //Toast.makeText(BusinessActivity.this, "Removed from your list", Toast.LENGTH_SHORT).show();
                                                                DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                                mDataad.removeValue();
                                                                button2.setVisibility(View.VISIBLE);
                                                                buttonr.setVisibility(View.INVISIBLE);
                                                            }
                                                        });

                                                        buttonr = findViewById(R.id.facsh);
                                                        buttonr.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                Bundle shareParams = new Bundle();
                                                                shareParams.putString("biz_name", biznamex);
                                                                shareParams.putString("biz_category", bizcatx);
                                                                shareParams.putLong("biz_stars", bizratx);
                                                                shareParams.putLong("biz_customers", bizxx);
                                                                mFirebaseAnalytics.logEvent("share_tap", shareParams);

                                                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                                shareIntent.putExtra(Intent.EXTRA_TEXT,
                                                                        "https://brinmo.com/b/" + bizwebid);
                                                                shareIntent.setType("text/plain");
                                                                startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
                                                            }
                                                        });

                                                        button2 = findViewById(R.id.faca);
                                                        button2.setVisibility(View.VISIBLE);
                                                        addText = findViewById(R.id.addtext);
                                                        addText.setVisibility(View.VISIBLE);
                                                        button2.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                //Toast.makeText(BusinessActivity.this, "Added to your list", Toast.LENGTH_SHORT).show();
                                                                button2.setVisibility(View.GONE);
                                                                addText.setVisibility(View.GONE);
                                                                DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                                HashMap<String, Object> satsinf = new HashMap<>();
                                                                satsinf.put("n", biznamex);
                                                                satsinf.put("t", biztitlex);
                                                                satsinf.put("l", ServerValue.TIMESTAMP);
                                                                mDataad.setValue(satsinf);
                                                                button3.setVisibility(View.VISIBLE);
                                                                //buttonr.setVisibility(View.VISIBLE);

                                                                Bundle addParams = new Bundle();
                                                                addParams.putString("biz_name", biznamex);
                                                                addParams.putString("biz_category", bizcatx);
                                                                addParams.putLong("biz_stars", bizratx);
                                                                addParams.putLong("biz_customers", bizxx);
                                                                mFirebaseAnalytics.logEvent("add_tap", addParams);

                                                                final Handler handler = new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        // Do something after 1s = 1000ms
                                                                        Intent intentskip = new Intent(BusinessActivity.this, HomeActivity.class);
                                                                        startActivity(intentskip);
                                                                    }
                                                                }, 2000);
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    // Getting Post failed, log a message
                                                    FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                                                    // ...
                                                }
                                            };
                                            mDataadb.addListenerForSingleValueEvent(adbpostListener);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            // Getting Post failed, log a message
                                            FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                                            // ...
                                        }
                                    };
                                    mDatansb.addListenerForSingleValueEvent(nsbpostListener);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                                    // ...
                                }
                            };
                            mDatasab.addListenerForSingleValueEvent(sabpostListener);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                            // ...
                        }
                    };
                    bizstuff.addListenerForSingleValueEvent(postListener);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                    // ...
                }
            };
            getbizid.addListenerForSingleValueEvent(listbiz);
        } else {
            //if gotten through normal search
            ImageButton button = findViewById(R.id.facb);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBackPressed();
                }
            });

            bizidx = bundle.getString("bizid");

            //get stuff from account
            mAuth = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase daab = FirebaseDatabase.getInstance();
            DatabaseReference bizstuff = daab.getReference("businessinfo/" + bizidx);
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get and use the values to update the UI
                    biznamex = dataSnapshot.child("name").getValue(String.class);
                    biztitlex = dataSnapshot.child("title").getValue(String.class);
                    bizstarx = dataSnapshot.child("satisfied").getValue(Long.class);
                    bizxx = dataSnapshot.child("nofcustomers").getValue(Long.class);
                    bizcity = dataSnapshot.child("cityname").getValue(String.class);
                    String biznamelow = biznamex.toLowerCase();
                    String biznamelast = biznamelow.replaceAll(" ", "-");
                    String bizcitylow = bizcity.toLowerCase();
                    String bizcitylast = bizcitylow.replaceAll(" ", "-");
                    bizwebid = biznamelast + "-" + bizcitylast;
                    //bizdescriptionx = dataSnapshot.child("description").getValue(String.class);

                    biznameview.setText(biznamex);
                    biztitleview.setText(biztitlex);
                    //bizdescview.setText(bizdescriptionx);
                    if (bizstarx < 1) {
                        if (bizxx == 0) {
                            starView.setText("Unrated");
                        } else if (bizxx >= 1) {
                            starView.setText("Toxic");
                            //send a notice
                        }
                    } else if (((bizstarx / bizxx) * 10) <= 2) {
                        starView.setText(HtmlCompat.fromHtml("&#9733;&#9734;&#9734;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } else if (((bizstarx / bizxx) * 10) <= 4) {
                        starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9734;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } else if (((bizstarx / bizxx) * 10) <= 6) {
                        starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } else if (((bizstarx / bizxx) * 10) <= 8) {
                        starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9733;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                    } else if (((bizstarx / bizxx) * 10) <= 10) {
                        starView.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9733;&#9733;", HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }
                    xView.setText("" + bizxx);

                    Long bizratx;
                    if (bizxx != 0){
                        bizratx = (bizstarx/bizxx)*5;
                    } else {
                        bizratx = bizxx;
                    }

                    String imageurii = "https://firebasestorage.googleapis.com/v0/b/bizzybody-90f3e.appspot.com/o/business%2F"+bizidx+"%2FprofileImage1_600x600.png?alt=media";
                    Picasso.get().load(imageurii).fit().centerCrop().into(bizimageview);

                    //get phonenumber
                    DatabaseReference mPhoneRef = FirebaseDatabase.getInstance().getReference("businesscontact/" + bizidx + "/pnumber");
                    ValueEventListener phoneListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Post object and use the values to update the UI
                            bizphonenumberx = dataSnapshot.getValue(String.class);
                            // ...
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                            // ...
                        }
                    };
                    mPhoneRef.addValueEventListener(phoneListener);

                    //get business category
                    DatabaseReference mCatRef = FirebaseDatabase.getInstance().getReference("searchface/" + bizidx + "/category");
                    ValueEventListener catListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Post object and use the values to update the UI
                            bizcatx = dataSnapshot.getValue(String.class);
                            // ...
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                            // ...
                        }
                    };
                    mCatRef.addListenerForSingleValueEvent(catListener);

                    // Get latest review
                    Long datenow = new Date().getTime();
                    Query queryr = FirebaseDatabase.getInstance().getReference("reviews/" + bizidx).orderByChild("time").endAt(datenow).limitToLast(1);
                    queryr.addListenerForSingleValueEvent(valueEventListener);

                    bizchattap = findViewById(R.id.bizchat);
                    bizchattap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle shareParams = new Bundle();
                            shareParams.putString("biz_name", biznamex);
                            shareParams.putString("biz_category", bizcatx);
                            shareParams.putLong("biz_stars", bizratx);
                            shareParams.putLong("biz_customers", bizxx);
                            mFirebaseAnalytics.logEvent("call_tap", shareParams);

                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", bizphonenumberx, null));
                            startActivity(intent);
                        }
                    });

                    bizordertap = findViewById(R.id.bizorder);
                    bizordertap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle orderParams = new Bundle();
                            orderParams.putString("biz_name", biznamex);
                            orderParams.putString("biz_category", bizcatx);
                            orderParams.putLong("biz_stars", bizratx);
                            orderParams.putLong("biz_customers", bizxx);
                            mFirebaseAnalytics.logEvent("order_tap", orderParams);

                            if (bizcatx == "transport" && bizphonenumberx != null) {
                                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", bizphonenumberx, null));
                                startActivity(intent);
                            }
                            if (bizcity != null) {
                                Intent intenti = new Intent(BusinessActivity.this, InventoryActivity.class);
                                intenti.putExtra("bid", bizidx);
                                startActivity(intenti);
                            } else {
                                Toast.makeText(BusinessActivity.this, "This Business is Unverified", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    bizpaytap = findViewById(R.id.bizpay);
                    bizpaytap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle payParams = new Bundle();
                            payParams.putString("biz_name", biznamex);
                            payParams.putString("biz_category", bizcatx);
                            payParams.putLong("biz_stars", bizratx);
                            payParams.putLong("biz_customers", bizxx);
                            mFirebaseAnalytics.logEvent("pay_tap", payParams);

                            Intent intentp = new Intent(BusinessActivity.this, AmountActivity.class);
                            intentp.putExtra("bid", bizidx);
                            startActivity(intentp);
                        }
                    });

                    //check if user has business on any list
                    DatabaseReference mDatasab = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/satisfied/" + bizidx);
                    ValueEventListener sabpostListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get the business name from satisfied
                            bnmsab = dataSnapshot.getValue(Boolean.class);
                            DatabaseReference mDatansb = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/notsatisfied/" + bizidx);
                            ValueEventListener nsbpostListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get the business name from notsatisfied
                                    bnmnsb = dataSnapshot.getValue(Boolean.class);
                                    DatabaseReference mDataadb = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx + "/n");
                                    ValueEventListener adbpostListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // Get the business name from notsatisfied
                                            bnmnadb = dataSnapshot.getValue(String.class);
                                            FirebaseCrashlytics.getInstance().log("satsf-nsats-added(biz): " + bnmsab + " : " + bnmnsb + " : " + bnmnadb + "(" + biznamex + ")");
                                            if (bnmsab != null || bnmnsb != null) {
                                                //If the business has been paid
                                                buttonr = findViewById(R.id.facsh);
                                                buttonr.setVisibility(View.VISIBLE);
                                                buttonr.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Bundle shareParams = new Bundle();
                                                        shareParams.putString("biz_name", biznamex);
                                                        shareParams.putString("biz_category", bizcatx);
                                                        shareParams.putLong("biz_stars", bizratx);
                                                        shareParams.putLong("biz_customers", bizxx);
                                                        mFirebaseAnalytics.logEvent("share_tap", shareParams);

                                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                        shareIntent.putExtra(Intent.EXTRA_TEXT,
                                                                "https://brinmo.com/b/" + bizwebid);
                                                        shareIntent.setType("text/plain");
                                                        startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
                                                    }
                                                });

                                            } else if (bnmnadb != null) {
                                                //if the business is on a list but not used before
                                                button3 = findViewById(R.id.facs);
                                                button3.setVisibility(View.VISIBLE);
                                                button3.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        //Toast.makeText(BusinessActivity.this, "Removed from your list", Toast.LENGTH_SHORT).show();
                                                        button3.setVisibility(View.GONE);
                                                        DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                        mDataad.removeValue();
                                                        button2.setVisibility(View.VISIBLE);
                                                        buttonr.setVisibility(View.INVISIBLE);
                                                    }
                                                });

                                                buttonr = findViewById(R.id.facsh);
                                                buttonr.setVisibility(View.VISIBLE);
                                                buttonr.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Bundle shareParams = new Bundle();
                                                        shareParams.putString("biz_name", biznamex);
                                                        shareParams.putString("biz_category", bizcatx);
                                                        shareParams.putLong("biz_stars", bizratx);
                                                        shareParams.putLong("biz_customers", bizxx);
                                                        mFirebaseAnalytics.logEvent("share_tap", shareParams);

                                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                        shareIntent.putExtra(Intent.EXTRA_TEXT,
                                                                "https://brinmo.com/b/" + bizwebid);
                                                        shareIntent.setType("text/plain");
                                                        startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
                                                    }
                                                });

                                                button2 = findViewById(R.id.faca);
                                                button2.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        //Toast.makeText(BusinessActivity.this, "Added to your list", Toast.LENGTH_SHORT).show();
                                                        button2.setVisibility(View.GONE);
                                                        DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                        HashMap<String, Object> satsinf = new HashMap<>();
                                                        satsinf.put("n", biznamex);
                                                        satsinf.put("t", biztitlex);
                                                        satsinf.put("l", ServerValue.TIMESTAMP);
                                                        mDataad.setValue(satsinf);
                                                        button3.setVisibility(View.VISIBLE);
                                                        //buttonr.setVisibility(View.VISIBLE);

                                                        Bundle addParams = new Bundle();
                                                        addParams.putString("biz_name", biznamex);
                                                        addParams.putString("biz_category", bizcatx);
                                                        addParams.putLong("biz_stars", bizratx);
                                                        addParams.putLong("biz_customers", bizxx);
                                                        mFirebaseAnalytics.logEvent("add_tap", addParams);

                                                        final Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                // Do something after 1s = 1000ms
                                                                Intent intentskip = new Intent(BusinessActivity.this, HomeActivity.class);
                                                                startActivity(intentskip);
                                                            }
                                                        }, 2000);
                                                    }
                                                });
                                            } else if (bnmnadb == null) {
                                                //if the business isn't on any of the customer's list
                                                button3 = findViewById(R.id.facs);
                                                button3.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        //Toast.makeText(BusinessActivity.this, "Removed from your list", Toast.LENGTH_SHORT).show();
                                                        DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                        mDataad.removeValue();
                                                        button2.setVisibility(View.VISIBLE);
                                                        buttonr.setVisibility(View.INVISIBLE);
                                                    }
                                                });

                                                buttonr = findViewById(R.id.facsh);
                                                buttonr.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Bundle shareParams = new Bundle();
                                                        shareParams.putString("biz_name", biznamex);
                                                        shareParams.putString("biz_category", bizcatx);
                                                        shareParams.putLong("biz_stars", bizratx);
                                                        shareParams.putLong("biz_customers", bizxx);
                                                        mFirebaseAnalytics.logEvent("share_tap", shareParams);

                                                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                                        shareIntent.putExtra(Intent.EXTRA_TEXT,
                                                                "https://brinmo.com/b/" + bizwebid);
                                                        shareIntent.setType("text/plain");
                                                        startActivity(Intent.createChooser(shareIntent, "Recommend to a friend"));
                                                    }
                                                });

                                                button2 = findViewById(R.id.faca);
                                                button2.setVisibility(View.VISIBLE);
                                                addText = findViewById(R.id.addtext);
                                                addText.setVisibility(View.VISIBLE);
                                                button2.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        //Toast.makeText(BusinessActivity.this, "Added to your list", Toast.LENGTH_SHORT).show();
                                                        button2.setVisibility(View.GONE);
                                                        addText.setVisibility(View.GONE);
                                                        DatabaseReference mDataad = FirebaseDatabase.getInstance().getReference("cusbusinesses/" + mAuth.getUid() + "/added/" + bizidx);
                                                        HashMap<String, Object> satsinf = new HashMap<>();
                                                        satsinf.put("n", biznamex);
                                                        satsinf.put("t", biztitlex);
                                                        satsinf.put("l", ServerValue.TIMESTAMP);
                                                        mDataad.setValue(satsinf);
                                                        button3.setVisibility(View.VISIBLE);
                                                        //buttonr.setVisibility(View.VISIBLE);

                                                        Bundle addParams = new Bundle();
                                                        addParams.putString("biz_name", biznamex);
                                                        addParams.putString("biz_category", bizcatx);
                                                        addParams.putLong("biz_stars", bizratx);
                                                        addParams.putLong("biz_customers", bizxx);
                                                        mFirebaseAnalytics.logEvent("add_tap", addParams);

                                                        final Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                // Do something after 1s = 1000ms
                                                                Intent intentskip = new Intent(BusinessActivity.this, HomeActivity.class);
                                                                startActivity(intentskip);
                                                            }
                                                        }, 2000);
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            // Getting Post failed, log a message
                                            FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                                            // ...
                                        }
                                    };
                                    mDataadb.addListenerForSingleValueEvent(adbpostListener);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    Log.w("Failed to read ya data", "loadPost:onCancelled", databaseError.toException());
                                    // ...
                                }
                            };
                            mDatansb.addListenerForSingleValueEvent(nsbpostListener);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Getting Post failed, log a message
                            FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                            // ...
                        }
                    };
                    mDatasab.addListenerForSingleValueEvent(sabpostListener);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                    // ...
                }
            };
            bizstuff.addListenerForSingleValueEvent(postListener);
        }

        recyclerView = findViewById(R.id.review_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(this, reviewList);
        recyclerView.setAdapter(adapter);

        // Get latest review
        Long datenow = new Date().getTime();
        Query queryr = FirebaseDatabase.getInstance().getReference("reviews/" + bizidx).orderByChild("time").endAt(datenow).limitToLast(1);
        queryr.addListenerForSingleValueEvent(valueEventListener);

    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            reviewList.clear();
            int revresults = 0;

            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.exists()) {
                        Review review = snapshot.getValue(Review.class);
                        review.cid = snapshot.getKey();
                        reviewList.add(review);
                        revresults = revresults + 1;
                    }
                }
                if (revresults == 0) {
                    TextView bizrev = findViewById(R.id.bizrev);
                    bizrev.setVisibility(View.VISIBLE);
                }
            } else {
                if (revresults == 0) {
                    TextView bizrev = findViewById(R.id.bizrev);
                    bizrev.setVisibility(View.VISIBLE);
                }
            }
            Collections.reverse(reviewList);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
