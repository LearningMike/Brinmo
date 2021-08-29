package com.brinmo.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.brinmo.HomeActivity;
import com.brinmo.MeActivity;
import com.brinmo.R;
import com.brinmo.SearchActivity;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    Long storenum, foodnum, laundrynum, hairsalonnum, barbershopnum, transportnum, fashionnum;
    String cccode;
    View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        final TextView textView = (TextView) ((HomeActivity) getActivity()).findViewById(R.id.home_head);
        TextView discText = (TextView) ((HomeActivity) getActivity()).findViewById(R.id.home_headd);

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (!String.valueOf(discText.getText()).equals("")){
                    textView.setText(String.valueOf(discText.getText()));

                    cccode = ((HomeActivity) getActivity()).getPlace();

                    //get business city country nums
                    DatabaseReference mPlaceRef = FirebaseDatabase.getInstance().getReference("businesscount/" + cccode);
                    ValueEventListener placeListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Post object and use the values to update the UI
                            if (dataSnapshot.exists()) {
                                TextView storeview = root.findViewById(R.id.storenum);
                                storenum = dataSnapshot.child("store").getValue(Long.class);
                                if (storenum == null){
                                    storeview.setText("0 places");
                                } else {
                                    String storefinal = storenum + " places";
                                    storeview.setText(storefinal);
                                }

                                TextView foodview = root.findViewById(R.id.foodnum);
                                foodnum = dataSnapshot.child("food").getValue(Long.class);
                                if (foodnum == null){
                                    foodview.setText("0 places");
                                } else {
                                    String foodfinal = foodnum + " places";
                                    foodview.setText(foodfinal);
                                }

                                TextView laundryview = root.findViewById(R.id.laundrynum);
                                laundrynum = dataSnapshot.child("laundry").getValue(Long.class);
                                if (laundrynum == null){
                                    laundryview.setText("0 places");
                                } else {
                                    String laundryfinal = laundrynum + " places";
                                    laundryview.setText(laundryfinal);
                                }

                                TextView hairsalonview = root.findViewById(R.id.hairsalonnum);
                                hairsalonnum = dataSnapshot.child("hairsalon").getValue(Long.class);
                                if (hairsalonnum == null){
                                    hairsalonview.setText("0 places");
                                } else {
                                    String hairsalonfinal = hairsalonnum + " places";
                                    hairsalonview.setText(hairsalonfinal);
                                }

                                TextView barbershopview = root.findViewById(R.id.barbershopnum);
                                barbershopnum = dataSnapshot.child("barbershop").getValue(Long.class);
                                if (barbershopnum == null){
                                    barbershopview.setText("0 places");
                                } else {
                                    String barbershopfinal = barbershopnum + " places";
                                    barbershopview.setText(barbershopfinal);
                                }

                                TextView transportview = root.findViewById(R.id.transportnum);
                                transportnum = dataSnapshot.child("transport").getValue(Long.class);
                                if (transportnum == null){
                                    transportview.setText("0 places");
                                } else {
                                    String transportfinal = transportnum + " places";
                                    transportview.setText(transportfinal);
                                }

                                TextView fashionview = root.findViewById(R.id.fashionnum);
                                fashionnum = dataSnapshot.child("fashion").getValue(Long.class);
                                if (fashionnum == null){
                                    fashionview.setText("0 places");
                                } else {
                                    String fashionfinal = fashionnum + " places";
                                    fashionview.setText(fashionfinal);
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
                    mPlaceRef.addListenerForSingleValueEvent(placeListener);

                } else {
                    textView.setText(s);
                }
            }
        });


        LinearLayout button = root.findViewById(R.id.food);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent intent = new Intent(root.getContext(), SearchActivity.class);
                    intent.putExtra("category", "Restaurants/Fast-food");
                    intent.putExtra("catpath", "food");
                    intent.putExtra("citycountry", ((HomeActivity)getActivity()).getPlace());
                    startActivity(intent);
                }
            }
        });

        LinearLayout button2 = root.findViewById(R.id.store);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent intent2 = new Intent(getContext(), SearchActivity.class);
                    intent2.putExtra("category", "Shops & Stores");
                    intent2.putExtra("catpath", "store");
                    intent2.putExtra("citycountry", ((HomeActivity)getActivity()).getPlace());
                    startActivity(intent2);
                }
            }
        });

        LinearLayout button3 = root.findViewById(R.id.laundry);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent intent3 = new Intent(getActivity(), SearchActivity.class);
                    intent3.putExtra("category", "Laundry Services");
                    intent3.putExtra("catpath", "laundry");
                    intent3.putExtra("citycountry", ((HomeActivity) getActivity()).getPlace());
                    startActivity(intent3);
                }
            }
        });

        LinearLayout button4 = root.findViewById(R.id.transport);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent intent4 = new Intent(getActivity(), SearchActivity.class);
                    intent4.putExtra("category", "Cab Drivers");
                    intent4.putExtra("catpath", "transport");
                    intent4.putExtra("citycountry", ((HomeActivity) getActivity()).getPlace());
                    startActivity(intent4);
                }
            }
        });

        LinearLayout button5 = root.findViewById(R.id.hairsalon);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent intent5 = new Intent(getActivity(), SearchActivity.class);
                    intent5.putExtra("category", "Hair Salons");
                    intent5.putExtra("catpath", "hairsalon");
                    intent5.putExtra("citycountry", ((HomeActivity) getActivity()).getPlace());
                    startActivity(intent5);
                }
            }
        });

        LinearLayout button6 = root.findViewById(R.id.barbershop);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent intent6 = new Intent(getActivity(), SearchActivity.class);
                    intent6.putExtra("category", "Barber Shops");
                    intent6.putExtra("catpath", "barbershop");
                    intent6.putExtra("citycountry", ((HomeActivity) getActivity()).getPlace());
                    startActivity(intent6);
                }
            }
        });

        LinearLayout button7 = root.findViewById(R.id.fashion);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent intent7 = new Intent(getActivity(), SearchActivity.class);
                    intent7.putExtra("category", "Fashion & Drip");
                    intent7.putExtra("catpath", "fashion");
                    intent7.putExtra("citycountry", ((HomeActivity) getActivity()).getPlace());
                    startActivity(intent7);
                }
            }
        });

        Button buttonadd = (Button) root.findViewById(R.id.addcategory);
        buttonadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity)getActivity()).getSearch()) {
                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://chat.whatsapp.com/Cv30Ou92wJw7k7dnjwjddt"));
                    startActivity(browserIntent);
                }
            }
        });
    }
}
