package com.brinmo.ui.home;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.brinmo.CheckOutActivity;
import com.brinmo.HomeActivity;
import com.brinmo.Livefeed;
import com.brinmo.LivefeedAdapter;
import com.brinmo.Mylist;
import com.brinmo.MylistAdapter;
import com.brinmo.R;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private com.brinmo.MylistAdapter adapter;
    private List<Mylist> mylistList;
    String mylistPath;
    TextView emptyHS;
    FirebaseAnalytics mFirebaseAnalytics;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.mylist_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setHasFixedSize(true);
        mylistList = new ArrayList<>();
        adapter = new MylistAdapter(root.getContext(), mylistList);
        recyclerView.setAdapter(adapter);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(root.getContext());

        //mylist here
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mylistList.clear();
                int myresults = 0;

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(snapshot.exists()) {
                            Mylist mylist = snapshot.getValue(Mylist.class);
                            mylist.bid = snapshot.getKey();
                            mylistList.add(mylist);
                            myresults = myresults + 1;
                            if (emptyHS != null) {
                                emptyHS.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    if (myresults == 0) {
                        if (emptyHS != null) {
                            emptyHS.setVisibility(View.VISIBLE);
                        }
                        mFirebaseAnalytics.setUserProperty("list_length", "empty");
                    } else if (myresults == 1){
                        mFirebaseAnalytics.setUserProperty("list_length", "one");
                    } else if (myresults > 1 && myresults < 6){
                        mFirebaseAnalytics.setUserProperty("list_length", "two-five");
                    } else {
                        mFirebaseAnalytics.setUserProperty("list_length", "over-five");
                    }
                } else {
                    if (emptyHS != null) {
                        emptyHS.setVisibility(View.VISIBLE);
                    }
                    if (myresults == 0) {
                        mFirebaseAnalytics.setUserProperty("list_length", "empty");
                    }
                }
                Collections.reverse(mylistList); //reverse results
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
            }
        };

        //get list of added businesses (3)
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        // Search
        Long datenow = new Date().getTime();
        Query queryl1 = FirebaseDatabase.getInstance().getReference("cusbusinesses/"+mUser.getUid()+"/added").orderByChild("l").endAt(datenow).limitToLast(20);;
        queryl1.addValueEventListener(valueEventListener);


        //get referrerbusiness
        InstallReferrerClient referrerClient;

        referrerClient = InstallReferrerClient.newBuilder(root.getContext()).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        // Connection established.
                        ReferrerDetails response = null;
                        try {
                            response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();
                            FirebaseCrashlytics.getInstance().log("AppInstallReferrer: "+referrerUrl);
                            mFirebaseAnalytics = FirebaseAnalytics.getInstance(root.getContext());
                            mFirebaseAnalytics.setUserProperty("install_src", referrerUrl);
                            String refid = referrerUrl.replace("id=com.brinmo&referrer=","");
                            long referrerClickTime = response.getReferrerClickTimestampSeconds();
                            long appInstallTime = response.getInstallBeginTimestampSeconds();
                            boolean instantExperienceLaunched = response.getGooglePlayInstantParam();
                            //*****\\get the bizname, biztitle with refid and save to cusbusinesses added
                        } catch (RemoteException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                        referrerClient.endConnection();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        final TextView textView = (TextView) ((HomeActivity) getActivity()).findViewById(R.id.home_head);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        emptyHS = (TextView) ((HomeActivity) getActivity()).findViewById(R.id.emptyhome);

    }
}
