package com.brinmo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
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
import java.util.HashMap;
import java.util.List;

public class InventoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private com.brinmo.ItemAdapter adapter;
    private List<Item> itemList;
    String itemPath;
    TextView emptyHS;
    String bizid, picked = "nada", delcost;
    TextView orderText;
    HashMap<String, Integer> shoppingCart;
    int totalAmount;
    ImageButton placeOrder;
    int itemresults;
    FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        Bundle bundle = getIntent().getExtras();
        bizid = bundle.getString("bid");
        picked = bundle.getString("picked");

        recyclerView = findViewById(R.id.item_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(InventoryActivity.this));
        recyclerView.setHasFixedSize(true);
        itemList = new ArrayList<>();
        orderText = (TextView) findViewById(R.id.ordertext);
        placeOrder = (ImageButton) findViewById(R.id.placeorder);
        shoppingCart = new HashMap<String, Integer>();
        totalAmount = 0;
        adapter = new ItemAdapter(InventoryActivity.this, itemList, orderText, shoppingCart, totalAmount, picked);
        recyclerView.setAdapter(adapter);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorOnSecondary);
        mSwipeRefreshLayout.setRefreshing(true);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(InventoryActivity.this);

        ImageButton button = findViewById(R.id.facb);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        TextView headName = findViewById(R.id.heading_name);

        //get delivery cost
        DatabaseReference mBizRef = FirebaseDatabase.getInstance().getReference("businessinfo/"+bizid+"/delcost");
        ValueEventListener deListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get Post object and use the values to update the UI
                    delcost = dataSnapshot.getValue(String.class);
                    String headtext = "Delivery costs ₦" + delcost;
                    headName.setText(headtext);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        mBizRef.addListenerForSingleValueEvent(deListener);


        // Search
        Query queryInv = FirebaseDatabase.getInstance().getReference("bizinventory/" + bizid);
        queryInv.addValueEventListener(valueEventListener);

        //placeOrder
        placeOrder.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingCart.size() != 0) {
                    FirebaseCrashlytics.getInstance().log("shoppingCart: "+shoppingCart.toString());
                    //send to OrderActivity
                    if (orderText.getText().toString().contains("₦")){
                        String amount = orderText.getText().toString().replace("₦", "");

                        Bundle invParams = new Bundle();
                        invParams.putString("from_livefeed", picked);
                        invParams.putInt("shop_size", itemresults);
                        invParams.putInt("cart_size", shoppingCart.size());
                        invParams.putInt("cart_cost", Integer.parseInt(amount));
                        invParams.putInt("delivery_fee", Integer.parseInt(delcost));
                        mFirebaseAnalytics.logEvent("order_made", invParams);

                        Intent intentorder = new Intent(InventoryActivity.this, OrderActivity.class);
                        intentorder.putExtra("bid", bizid);
                        intentorder.putExtra("cart", shoppingCart);
                        intentorder.putExtra("sum", amount);
                        intentorder.putExtra("fee", delcost);
                        startActivity(intentorder);
                    }
                }
            }
        });
    }

    //inventoryitems
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            itemList.clear();
            itemresults = 0;

            if (dataSnapshot.exists()) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.exists()) {
                        Item item = snapshot.getValue(Item.class);
                        item.iid = snapshot.getKey();
                        item.x = bizid;
                        itemList.add(item);
                        itemresults = itemresults + 1;
                    }
                }
                if (itemresults == 0) {
                    //Business does not deliver
                    TextView emptyinventory = findViewById(R.id.empty_inventory);
                    emptyinventory.setVisibility(View.VISIBLE);
                }
            } else {
                if (itemresults == 0) {
                    //Business does not deliver
                    TextView emptyinventory = findViewById(R.id.empty_inventory);
                    emptyinventory.setVisibility(View.VISIBLE);
                }
            }
            Collections.reverse(itemList);
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

        //clear the shopping cart
        orderText.setText("Select items to order");
        shoppingCart.clear();

        //Search
        mSwipeRefreshLayout.setRefreshing(true);
        Query queryInv = FirebaseDatabase.getInstance().getReference("bizinventory/" + bizid);
        queryInv.addValueEventListener(valueEventListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        orderText.setText("Select items to order");
        shoppingCart.clear();
        onBackPressed();
        return true;
    }
}
