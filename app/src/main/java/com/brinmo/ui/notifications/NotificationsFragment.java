package com.brinmo.ui.notifications;

import android.os.Bundle;
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

import com.brinmo.HomeActivity;
import com.brinmo.Livefeed;
import com.brinmo.LivefeedAdapter;
import com.brinmo.R;
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

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private com.brinmo.LivefeedAdapter adapter;
    private List<Livefeed> livefeedList;
    String livefeedPath;
    TextView emptyHS;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        root = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = root.findViewById(R.id.livefeed_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setHasFixedSize(true);
        livefeedList = new ArrayList<>();
        adapter = new LivefeedAdapter(root.getContext(), livefeedList);
        recyclerView.setAdapter(adapter);

        //livefeed here
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                livefeedList.clear();
                int liveresults = 0;

                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(snapshot.exists()) {
                            Livefeed livefeed = snapshot.getValue(Livefeed.class);
                            livefeed.iid = snapshot.getKey();
                            livefeedList.add(livefeed);
                            liveresults = liveresults + 1;
                        }
                    }
                    if (liveresults == 0) {
                        //Nothing fresh today
                    }
                } else {
                    if (liveresults == 0) {
                        //Nothing fresh today
                    }
                }
                Collections.reverse(livefeedList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
            }
        };

        //get city-country
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser.getUid() != null) {
            DatabaseReference uname = FirebaseDatabase.getInstance().getReference("user/" + mUser.getUid());
            ValueEventListener bpostListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String dbcity = dataSnapshot.child("city").getValue(String.class);
                    String dbcountry = dataSnapshot.child("country").getValue(String.class);
                    if (dbcity == null || dbcountry == null) {
                        //user has not given location
                        Toast.makeText(root.getContext(), "Location Required", Toast.LENGTH_SHORT).show();
                        FirebaseCrashlytics.getInstance().log("locationError: Location not available");
                    } else {
                        dbcity = dbcity.toLowerCase();
                        dbcity = dbcity.replaceAll(" ", "-");
                        dbcountry = dbcountry.toLowerCase();
                        dbcountry = dbcountry.replaceAll(" ", "-");

                        // Search
                        Long datenow = new Date().getTime();
                        Query querylf = FirebaseDatabase.getInstance().getReference("livefeed/" + dbcity + "-" + dbcountry).orderByChild("t").endAt(datenow).limitToLast(50);
                        querylf.addValueEventListener(valueEventListener);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    FirebaseCrashlytics.getInstance().log("databaseError: " + databaseError.toString());
                    // ...
                }
            };
            uname.addValueEventListener(bpostListener);
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        final TextView textView = (TextView) ((HomeActivity) getActivity()).findViewById(R.id.home_head);
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        emptyHS = ((HomeActivity) getActivity()).findViewById(R.id.emptyhome);
    }
}
