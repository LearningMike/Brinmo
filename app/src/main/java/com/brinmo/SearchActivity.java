package com.brinmo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private com.brinmo.SearchAdapter adapter;
    private List<Search> searchList;

    FirebaseUser mAuth;
    String bizpath;
    FirebaseAnalytics mFirebaseAnalytics;
    String citycountry;
    String catforpath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Bundle bundle  = getIntent().getExtras();
        String category = bundle.getString("category");
        TextView catview = (TextView) findViewById(R.id.heading_label);
        catview.setText(category);
        catforpath = bundle.getString("catpath");
        citycountry = bundle.getString("citycountry");
        bizpath = "locations/"+citycountry+"/"+catforpath+"/";

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(SearchActivity.this);

        if (mAuth == null) {
            Intent nextmain = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(nextmain);
        }

        recyclerView = findViewById(R.id.result_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        searchList = new ArrayList<>();
        adapter = new SearchAdapter(this, searchList);
        recyclerView.setAdapter(adapter);

        /*
         * You just need to attach the value event listener to read the values
         * query6.addListenerForSingleValueEvent(valueEventListener)
         * */
        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorOnSecondary);
        mSwipeRefreshLayout.setRefreshing(true);
        Query query4 = FirebaseDatabase.getInstance().getReference().child(bizpath).limitToFirst(20);////
        query4.addListenerForSingleValueEvent(valueEventListener);////

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(bizpath);
        GeoFire geoFire = new GeoFire(ref); //this is not in use yet
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(37.7832, -122.4056), 0.6);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {

            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                // ...
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {
                // ...
            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
                // ...
            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
                // ...
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                // ...
            }

        });

        ImageButton button = findViewById(R.id.facb);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            searchList.clear();
            Bundle bundle  = getIntent().getExtras();
            String category = bundle.getString("category");
            int searchresults = 0;

            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.exists()) {
                        Search search = snapshot.getValue(Search.class);
                        search.bid = snapshot.getKey();
                        search.cat = category;
                        searchList.add(search);
                        searchresults = searchresults + 1;
                    }
                }
                if (searchresults < 5) {
                    Button inviteb = findViewById(R.id.inviteb);
                    inviteb.setVisibility(View.VISIBLE);
                    inviteb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent inviteIntent = new Intent(Intent.ACTION_SEND);
                            inviteIntent.putExtra(Intent.EXTRA_TEXT,
                                    "https://brinmo.com");
                            inviteIntent.setType("text/plain");
                            startActivity(Intent.createChooser(inviteIntent, "Invite a business"));
                        }
                    });
                }
                if (searchresults < 1){
                    TextView emptysearch = findViewById(R.id.empty_search);
                    emptysearch.setVisibility(View.VISIBLE);
                    Bundle searchParams = new Bundle();
                    Long currentTime = Calendar.getInstance().getTimeInMillis();
                    searchParams.putLong("search_time", currentTime);
                    searchParams.putString("city_country", citycountry);
                    searchParams.putString("biz_category", catforpath);
                    mFirebaseAnalytics.logEvent("empty_search", searchParams);
                }
                Long currentTime = Calendar.getInstance().getTimeInMillis();
                Bundle findParams = new Bundle();
                findParams.putLong("search_time", currentTime);
                findParams.putString("city_country", citycountry);
                findParams.putString("biz_category", catforpath);
                findParams.putInt("result_length", searchresults);
                mFirebaseAnalytics.logEvent("search_result", findParams);
            } else {
                //When empty
                if (searchresults < 5) {
                    Button inviteb = findViewById(R.id.inviteb);
                    inviteb.setVisibility(View.VISIBLE);
                    inviteb.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent inviteIntent = new Intent(Intent.ACTION_SEND);
                            inviteIntent.putExtra(Intent.EXTRA_TEXT,
                                    "https://brinmo.com");
                            inviteIntent.setType("text/plain");
                            startActivity(Intent.createChooser(inviteIntent, "Invite a business"));
                        }
                    });
                }
                if (searchresults < 1){
                    TextView emptysearch = findViewById(R.id.empty_search);
                    emptysearch.setVisibility(View.VISIBLE);
                    Bundle searchParams = new Bundle();
                    Long currentTime = Calendar.getInstance().getTimeInMillis();
                    searchParams.putLong("search_time", currentTime);
                    searchParams.putString("city_country", citycountry);
                    searchParams.putString("biz_category", catforpath);
                    mFirebaseAnalytics.logEvent("empty_search", searchParams);
                }
            }
            adapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            mSwipeRefreshLayout.setRefreshing(false);
            FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
        }
    };

    @Override
    public void onRefresh() {

        mSwipeRefreshLayout.setRefreshing(true);
        Query query4 = FirebaseDatabase.getInstance().getReference().child(bizpath).limitToFirst(20);////
        query4.addListenerForSingleValueEvent(valueEventListener);////
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}