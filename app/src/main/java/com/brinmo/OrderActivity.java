package com.brinmo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class OrderActivity extends AppCompatActivity {

    String bizid, paySum, delFee;
    HashMap<String, Integer> shoppingCart;
    EditText place, number;
    String message, address, userphone, locationx, finalmessage;
    Button pay;
    TextView header;
    int finalamount, amountc;
    Boolean chargeauthcode = false;
    FirebaseUser mAuth;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Bundle bundle = getIntent().getExtras();
        bizid = bundle.getString("bid");
        shoppingCart = (HashMap<String, Integer>) bundle.get("cart");
        paySum = bundle.getString("sum");
        delFee = bundle.getString("fee");
        message = "";
        finalmessage = "";
        mAuth = FirebaseAuth.getInstance().getCurrentUser();

        //create the SMS message
        for (String i : shoppingCart.keySet()) {
            String item = i.replaceAll("-", " ");
            message = message + "("+shoppingCart.get(i)+")" + item + ", ";
        }

        header = findViewById(R.id.pcity);
        place = findViewById(R.id.address);
        number = findViewById(R.id.userphone);
        pay = findViewById(R.id.paytapo);

        //get the biz city
        DatabaseReference mCityRef = FirebaseDatabase.getInstance().getReference("businessinfo/"+bizid+"/cityname");
        ValueEventListener cityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                locationx = dataSnapshot.getValue(String.class);
                String headertext = "Delivery within " + locationx;
                header.setText(headertext);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        mCityRef.addListenerForSingleValueEvent(cityListener);

        //get username
        DatabaseReference mUsernameRef = FirebaseDatabase.getInstance().getReference("user/"+mAuth.getUid()+"/uname");
        ValueEventListener usernameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                username = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        mUsernameRef.addListenerForSingleValueEvent(usernameListener);

        //check for chargeauthcode existence or reusability
        /////\\\\\ only make this work when you've tested the userCharge system rigorously /////\\\\\
        DatabaseReference userinfo = FirebaseDatabase.getInstance().getReference("user/"+mAuth.getUid()+"/chargeauthcode/paystackng");
        ValueEventListener upostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the reusable value in chargeauthcode
                //////chargeauthcode = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        userinfo.addListenerForSingleValueEvent(upostListener);

        finalamount = ((Integer.parseInt(paySum) + Integer.parseInt(delFee)) * 100);//in kobo
        amountc = (int) Math.round(((finalamount/100.0)*4));//in kobo

        if ((finalamount + 5000) >= 230000){
            finalamount = finalamount + 5000;
        }

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = place.getText().toString();
                userphone = number.getText().toString();
                String subs = userphone.substring(0, 1);
                if (subs == "0"){
                    userphone = userphone.replaceFirst("0", "+234");
                } else{
                    userphone = "+234" + userphone;
                }
                finalmessage = username+" paid for delivery of " + message + "to "+ userphone + "\n : " + address;
                //Toast.makeText(OrderActivity.this, finalmessage, Toast.LENGTH_LONG).show();

                //Payment Block: This is used at the Amount Activity too
                if (chargeauthcode != true){
                    //save to message here to test smsFunction

                    //chargeauthcode doesn't exist or isn't chargeable, choose what psp Activity to use
                    //maybe in future, choose the card form or card scanner depending on the phone's ability/version :-)
                    Intent intent = new Intent(OrderActivity.this, CheckOutActivity.class);
                    intent.putExtra("amount", finalamount);
                    intent.putExtra("charge", amountc);
                    intent.putExtra("bid", bizid);
                    intent.putExtra("oldnew", "new");
                    intent.putExtra("ttype", "order");
                    intent.putExtra("sms", finalmessage);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(OrderActivity.this, CheckOutActivity.class);
                    intent.putExtra("amount", finalamount);
                    intent.putExtra("charge", amountc);
                    intent.putExtra("bid", bizid);
                    intent.putExtra("oldnew", "old");
                    intent.putExtra("ttype", "order");
                    intent.putExtra("sms", finalmessage);
                    startActivity(intent);
                }
            }
        });
    }
}
