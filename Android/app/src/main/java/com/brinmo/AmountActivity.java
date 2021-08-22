package com.brinmo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AmountActivity extends AppCompatActivity {
    //AmountActivity is for Nigeria using paystack\\
    Button paytap;
    EditText amount;
    String amounttext;
    int finalamount; //amount user wants to pay
    TextView paychargeview;
    int amountc; //the charge if a customer surpasses 2300
    Boolean chargeauthcode = false;
    FirebaseUser mAuth;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String FIRST_PAY = "firstPay";
    public boolean isFirstPay;
    Dialog paystackDialog;
    ImageView closePopup;
    FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount);

        Bundle bundle  = getIntent().getExtras();
        String bizidx = bundle.getString("bid");

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(AmountActivity.this);
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        isFirstPay = sharedPreferences.getBoolean(FIRST_PAY, true);

        if (isFirstPay){
            //show paystackie popup
            paystackDialog = new Dialog(AmountActivity.this);
            paystackDialog.setContentView(R.layout.paystackie);
            closePopup = (ImageView) paystackDialog.findViewById(R.id.closepop);
            closePopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putBoolean(FIRST_PAY, false);
                    editor.apply();
                    paystackDialog.dismiss();
                }
            });
            paystackDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            paystackDialog.show();
        }

        //check for chargeauthcode existence or reusability
        /////\\\\\ only make this work when you've tested the userChage system rigorously /////\\\\\
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

        amount = findViewById(R.id.amount);
        paychargeview = findViewById(R.id.paycharge);

        // on key change
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable mEdit) {
                amounttext = String.valueOf(amount.getText());
                if (amounttext.isEmpty()){
                    paychargeview.setText("Free Transfer");
                } else if (Integer.parseInt(amounttext) < 99){
                    paychargeview.setText("Minimum: ₦100");
                } else if (Integer.parseInt(amounttext) < 2300 && Integer.parseInt(amounttext) > 99){
                    paychargeview.setText("Free Transfer");
                } else if (Integer.parseInt(amounttext) >= 2300 && Integer.valueOf(amounttext) < 99000) {
                    paychargeview.setText("+ ₦50 Charge");
                } else {
                    paychargeview.setText("Not allowed");
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });


        paytap = findViewById(R.id.paytap);
        paytap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Bundle amountParams = new Bundle();
                amountParams.putBoolean("first_payment", isFirstPay);
                amountParams.putInt("pay_amount", finalamount);
                amountParams.putInt("pay_charge", amountc);
                mFirebaseAnalytics.logEvent("enter_amount", amountParams);

                amounttext = String.valueOf(amount.getText());
                finalamount = (Integer.parseInt(amounttext) * 100);//in kobo
                amountc = (int) Math.round(((finalamount/100.0)*4));//in kobo

                if ((finalamount + 5000) >= 230000){
                    finalamount = finalamount + 5000;
                }

                if (amounttext.equals("")) {
                    Toast.makeText(AmountActivity.this, "insert an amount to pay", Toast.LENGTH_LONG).show();
                } else if (amounttext.length() < 3) {
                    Toast.makeText(AmountActivity.this, "amount must be greater than ₦100", Toast.LENGTH_LONG).show();
                } else if (amounttext.length() > 5){
                    Toast.makeText(AmountActivity.this, "amount must be less than ₦100k", Toast.LENGTH_LONG).show();
                } else {
                    //Payment Block: This is used at the Order Activity too
                    if (chargeauthcode != true){
                        //chargeauthcode doesn't exist or isn't chargeable, choose what psp Activity to use
                        //maybe in future, choose the card form or card scanner depending on the phone's ability/version :-)
                        Intent intent = new Intent(AmountActivity.this, CheckOutActivity.class);
                        intent.putExtra("amount", finalamount);
                        intent.putExtra("charge", amountc);
                        intent.putExtra("bid", bizidx);
                        intent.putExtra("oldnew", "new");
                        intent.putExtra("ttype", "transfer");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(AmountActivity.this, CheckOutActivity.class);
                        intent.putExtra("amount", finalamount);
                        intent.putExtra("charge", amountc);
                        intent.putExtra("bid", bizidx);
                        intent.putExtra("oldnew", "old");
                        intent.putExtra("ttype", "transfer");
                        startActivity(intent);
                    }
                }
            }
        });


    }
}
