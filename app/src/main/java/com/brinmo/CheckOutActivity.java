package com.brinmo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.brinmo.CardBackFragment;
import com.brinmo.R;
import com.brinmo.CCFragment.CCNameFragment;
import com.brinmo.CCFragment.CCNumberFragment;
import com.brinmo.CCFragment.CCSecureCodeFragment;
import com.brinmo.CCFragment.CCValidityFragment;
import com.brinmo.Utils.CreditCardUtils;
import com.brinmo.Utils.ViewPagerAdapter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.exceptions.ExpiredAccessCodeException;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;


import static java.lang.Integer.parseInt;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class CheckOutActivity extends FragmentActivity implements FragmentManager.OnBackStackChangedListener {

    //CheckOutActivity for Nigeria using paystack
    @BindView(R.id.btnNext)
    Button btnNext;

    public com.brinmo.CardFrontFragment cardFrontFragment;
    public CardBackFragment cardBackFragment;

    //This is our viewPager
    private ViewPager viewPager;

    com.brinmo.CCFragment.CCNumberFragment numberFragment;
    com.brinmo.CCFragment.CCNameFragment nameFragment;
    com.brinmo.CCFragment.CCValidityFragment validityFragment;
    com.brinmo.CCFragment.CCSecureCodeFragment secureCodeFragment;

    String backend_url = "";
    String paystack_public_key = "pk_live_";

    int total_item;
    boolean backTrack = false;

    private boolean mShowingBack = false;

    String cardNumber, cardCVV, cardValidity, cardName;
    String bizidx, oldnew, ttype, dnumber;
    String username, bizpnumber, bizsubaccount, bizemail, bizpsp;
    int amountx, chargex;
    String messagex, thereference;
    Map<String, String> timey;
    String bname;
    Bundle bundle;

    ProgressDialog dialog;
    private Charge charge;
    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private Transaction transaction;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String FIRST_AMOUNT = "firstAmount";
    private boolean isFirst;
    Dialog epicDialog;
    ImageView closepopup;
    TextView payerror;
    FirebaseAnalytics mFirebaseAnalytics;
    //SharedPreferences sharedPreferences;
    //SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        ButterKnife.bind(this);


        cardFrontFragment = new com.brinmo.CardFrontFragment();
        cardBackFragment = new CardBackFragment();

        if (BuildConfig.DEBUG && (backend_url.equals(""))) {
            throw new AssertionError("Please set a backend url before running the sample");
        }
        if (BuildConfig.DEBUG && (paystack_public_key.equals(""))) {
            throw new AssertionError("Please set a public key before running the sample");
        }
        //PaystackSdk.setPublicKey(paystack_public_key);

        if (savedInstanceState == null) {
            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, cardFrontFragment).commit();

        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }

        PaystackSdk.initialize(getApplicationContext());

        getFragmentManager().addOnBackStackChangedListener(this);

        bundle  = getIntent().getExtras();
        bizidx = bundle.getString("bid");
        oldnew = bundle.getString("oldnew");
        ttype = bundle.getString("ttype");
        dnumber = bundle.getString("dnumber");
        amountx = bundle.getInt("amount");
        chargex = bundle.getInt("charge");
        messagex = bundle.getString("sms");

        Button backb = findViewById(R.id.btnBack);
        backb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        String timex = String.valueOf(Calendar.getInstance().getTimeInMillis());
        thereference = mAuth.getUid()+"_T_"+timex;
        timey = ServerValue.TIMESTAMP;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(CheckOutActivity.this);

        DatabaseReference userdata = FirebaseDatabase.getInstance().getReference("user/"+mAuth.getUid()+"/uname");
        ValueEventListener upostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the @username
                username = dataSnapshot.getValue(String.class);
                //send notice if empty or null
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        userdata.addListenerForSingleValueEvent(upostListener);

        //Business Name
        DatabaseReference bizdata = FirebaseDatabase.getInstance().getReference("businessinfo/"+bizidx+"/name");
        ValueEventListener bizListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the business name
                bname = dataSnapshot.getValue(String.class);
                //send notice if empty or null
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        bizdata.addListenerForSingleValueEvent(bizListener);

        DatabaseReference bizcinfo = FirebaseDatabase.getInstance().getReference("businesscontact/"+bizidx);
        ValueEventListener bpostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the subaccount and pnumber
                bizsubaccount = dataSnapshot.child("code").getValue(String.class);
                bizpnumber = dataSnapshot.child("pnumber").getValue(String.class);
                bizemail = dataSnapshot.child("email").getValue(String.class);
                bizpsp = dataSnapshot.child("psp").getValue(String.class);
                // send a all hands alert if this is empty or null
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting data failed, send notice
                FirebaseCrashlytics.getInstance().log("databaseError: "+databaseError.toString());
                // ...
            }
        };
        bizcinfo.addListenerForSingleValueEvent(bpostListener);

        if (oldnew.equals("new")) {
            //if the users card is new

            //Initializing viewPager
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            viewPager.setOffscreenPageLimit(4);
            setupViewPager(viewPager);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == total_item) {
                        btnNext.setText("FINISH");
                    } else {
                        btnNext.setText("NEXT");
                    }

                    FirebaseCrashlytics.getInstance().log("onPageSelected: "+position);

                    if (position == total_item) {
                        flipCard();
                        backTrack = true;
                    } else if (position == total_item - 1 && backTrack) {
                        flipCard();
                        backTrack = false;
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = viewPager.getCurrentItem();
                    if (pos < total_item) {
                        viewPager.setCurrentItem(pos + 1);
                    } else {
                        checkEntries();
                    }

                }
            });
        } else if (oldnew.equals("old")){
            //first don't even think of doing this if it's not repeatedly asked for
            //Test this (seriously) before deploying\\
            //And using this should mean you don't send SMS at all from the app\\
            //that is: you have finished building the webhook SMS function\\
            //AND CONSIDER GOING TO FINGERPRINTACTIVITY AND DOING THIS THERE IF userCharge DOES NOT HAVE AUTHENTICATION
            dialog = new ProgressDialog(CheckOutActivity.this);
            dialog.setMessage("Processing transaction...");
            setContentView(R.layout.activity_secure);
            dialog.show();
            payerror = findViewById(R.id.payerror);
            String phonenumber = bizpnumber.replace("+","");

            //get authorization code from chargeauthcode/paystackng
            //check if it's chargeable, display card details in payerror
            //save stuff to userCharge in the DB
            //do all these in promises

            //go to ratingActivity (after saving to userCharge)
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 1s = 1000ms
                    Intent intent = new Intent(CheckOutActivity.this, RatingActivity.class);
                    intent.putExtra("bid", bizidx);
                    startActivity(intent);
                }
            }, 4000);
        }
    }

    public void checkEntries() {
        cardName = nameFragment.getName();
        cardNumber = numberFragment.getCardNumber();
        cardValidity = validityFragment.getValidity();
        cardCVV = secureCodeFragment.getValue();

        //if (TextUtils.isEmpty(cardName)) {
        //   Toast.makeText(CheckOutActivity.this, "Enter Valid Name", Toast.LENGTH_SHORT).show();
        //}
        if (TextUtils.isEmpty(cardNumber) || !CreditCardUtils.isValid(cardNumber.replace(" ", ""))) {
            Toast.makeText(CheckOutActivity.this, "Enter Valid Card Number", Toast.LENGTH_SHORT).show();
            Bundle cardParams = new Bundle();
            cardParams.putBoolean("input_success", false);
            cardParams.putString("attempt_ref", thereference);
            cardParams.putLong("attempt_time", Calendar.getInstance().getTimeInMillis());
            mFirebaseAnalytics.logEvent("enter_card", cardParams);
            startAFreshCharge(true);
        } else if (TextUtils.isEmpty(cardValidity) || !CreditCardUtils.isValidDate(cardValidity)) {
            Toast.makeText(CheckOutActivity.this, "Enter Valid Expiry Date", Toast.LENGTH_SHORT).show();
            Bundle cardParams = new Bundle();
            cardParams.putBoolean("input_success", false);
            cardParams.putString("attempt_ref", thereference);
            cardParams.putLong("attempt_time", Calendar.getInstance().getTimeInMillis());
            mFirebaseAnalytics.logEvent("enter_card", cardParams);
            startAFreshCharge(true);
        } else if (TextUtils.isEmpty(cardCVV) || cardCVV.length() < 3) {
            Toast.makeText(CheckOutActivity.this, "Enter Valid Security Code", Toast.LENGTH_SHORT).show();
            Bundle cardParams = new Bundle();
            cardParams.putBoolean("input_success", false);
            cardParams.putString("attempt_ref", thereference);
            cardParams.putLong("attempt_time", Calendar.getInstance().getTimeInMillis());
            mFirebaseAnalytics.logEvent("enter_card", cardParams);
            startAFreshCharge(true);
        } else {
            try {
                Bundle cardParams = new Bundle();
                cardParams.putBoolean("input_success", true);
                cardParams.putString("attempt_ref", thereference);
                cardParams.putLong("attempt_time", Calendar.getInstance().getTimeInMillis());
                mFirebaseAnalytics.logEvent("enter_card", cardParams);
                startAFreshCharge(true);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);

            }
        }
    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
    }

    private void setupViewPager(ViewPager viewPager) {
        com.brinmo.Utils.ViewPagerAdapter adapter = new com.brinmo.Utils.ViewPagerAdapter(getSupportFragmentManager());
        numberFragment = new CCNumberFragment();
        nameFragment = new CCNameFragment();
        validityFragment = new CCValidityFragment();
        secureCodeFragment = new CCSecureCodeFragment();
        adapter.addFragment(numberFragment);
        //adapter.addFragment(nameFragment);
        adapter.addFragment(validityFragment);
        adapter.addFragment(secureCodeFragment);

        total_item = adapter.getCount() - 1;
        viewPager.setAdapter(adapter);

    }

    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }
        // Flip to the back.
        //setCustomAnimations(int enter, int exit, int popEnter, int popExit)

        mShowingBack = true;

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.fragment_container, cardBackFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        int pos = viewPager.getCurrentItem();
        if (pos > 0) {
            viewPager.setCurrentItem(pos - 1);
        } else {
            super.onBackPressed();
        }
    }

    public void nextClick() {
        btnNext.performClick();
    }

    //Paystack stuff\\
    private void startAFreshCharge(boolean local) {
        // initialize the charge
        charge = new Charge();
        charge.setCard(loadCardFromForm());
        //editor.putBoolean(FIRST_AMOUNT, false);
        //editor.apply();
        dialog = new ProgressDialog(CheckOutActivity.this);
        dialog.setMessage("Performing transaction...");
        setContentView(R.layout.activity_secure);
        dialog.show();
        payerror = findViewById(R.id.payerror);

        if (local) {
            //This is how we roll now

            //charge.setAccessCode(result);
            charge.setAmount(amountx);
            charge.setEmail(mAuth.getEmail());
            charge.setReference(thereference);
            charge.setSubaccount(bizsubaccount);
            charge.setTransactionCharge(chargex);
            charge.setCurrency("NGN");
            /**try {
                charge.putCustomField("Charged From", "Android SDK");
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
            chargeCard();
        } else {
            // Perform transaction/initialize on our server to get an access code
            // documentation: https://developers.paystack.co/reference#initialize-a-transaction
            new CheckOutActivity.fetchAccessCodeFromServer().execute(backend_url + "/new-access-code");
        }
    }

    /**
     * Method to validate the form, and set errors on the edittexts.
     */
    private Card loadCardFromForm() {
        //validate fields
        Card card;

        //String cardNum = mEditCardNum.getText().toString().trim();
        String dateconcat = String.valueOf(cardValidity);
        int monthnum = Integer.parseInt(dateconcat.substring(0, 2));
        int yearnum = Integer.parseInt(dateconcat.substring(3,5));
        FirebaseCrashlytics.getInstance().log("dateError: "+dateconcat);
        //build card object with ONLY the number, update the other fields later
        card = new Card.Builder(String.valueOf(cardNumber), monthnum, yearnum, String.valueOf(cardCVV)).build();
        /*String cvc = mEditCVC.getText().toString().trim();
        //update the cvc field of the card
        card.setCvc(cvc);

        //validate expiry month;
        String sMonth = mEditExpiryMonth.getText().toString().trim();
        int month = 0;
        try {
            month = parseInt(sMonth);
        } catch (Exception ignored) {
        }

        card.setExpiryMonth(month);

        String sYear = mEditExpiryYear.getText().toString().trim();
        int year = 0;
        try {
            year = parseInt(sYear);
        } catch (Exception ignored) {
        }
        card.setExpiryYear(year);*/

        return card;
    }

    @Override
    public void onPause() {
        super.onPause();

        if ((dialog != null) && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
    }

    private void chargeCard() {
        transaction = null;
        PaystackSdk.chargeCard(CheckOutActivity.this, charge, new Paystack.TransactionCallback() {
            // This is called only after transaction is successful
            @Override
            public void onSuccess(Transaction transaction) {
                dismissDialog();

                CheckOutActivity.this.transaction = transaction;
                //No errors
                Toast.makeText(CheckOutActivity.this, "Transaction Successful", Toast.LENGTH_LONG).show();
                //Toast.makeText(CheckOutActivity.this, transaction.getReference(), Toast.LENGTH_LONG).show();
                updateTextViews();
                new CheckOutActivity.verifyOnServer().execute(transaction.getReference());
                // get month-year
                Calendar caleb = Calendar.getInstance();
                Date datem = new Date();
                caleb.setTimeInMillis(datem.getTime());
                String monthyear = caleb.get(MONTH)+"-"+caleb.get(YEAR);
                //save the transaction record to the database
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("transactions/"+bizidx+"/"+monthyear).push();
                HashMap<String, Object> timestampNow = new HashMap<>();
                timestampNow.put("timestamp", timey);
                timestampNow.put("amountinkobo", amountx);
                timestampNow.put("customername", username);
                timestampNow.put("customerid", mAuth.getUid());
                timestampNow.put("type", ttype+"::"+thereference+"::"+bizpnumber);
                //ttype: whether its an order or transfer
                //thereference: paystack transaction reference
                //bizpnumber: phonenumber the SMS was sent to / SMS reference
                mDatabase.setValue(timestampNow);
                //send SMS
                String smsm = username + " paid N"+(amountx/100)+" to your account: " + bizemail;
                String phonenumber = bizpnumber.replace("+","");
                if (ttype.equals("order")) {
                    smsm = messagex;

                    //Maybe check if the business delivery option is within 24hrs?
                    //SET THE DELIVERY UPDATE ALARM HERE
                    setAlarm(bizidx, datem.getTime()+3600000, bname);
                }
                String finalSmsm = smsm;

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            URL url = new URL("https://termii.com/api/sms/send");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                            conn.setRequestProperty("Accept","application/json");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("to", phonenumber);
                            jsonParam.put("from", "Brinmo");
                            jsonParam.put("sms", finalSmsm);
                            jsonParam.put("type", "plain");
                            jsonParam.put("channel", "generic");
                            jsonParam.put("api_key", "");

                            FirebaseCrashlytics.getInstance().log("JSON "+ jsonParam.toString());
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                            os.writeBytes(jsonParam.toString());

                            os.flush();
                            os.close();

                            FirebaseCrashlytics.getInstance().log("CONN: "+ conn.getResponseCode());
                            FirebaseCrashlytics.getInstance().log("MSG: "+conn.getResponseMessage());

                            conn.disconnect();
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);//send an mail or suntin
                        }
                    }
                });

                thread.start();

                //Main Event
                Bundle purchaseParams = new Bundle();
                purchaseParams.putString(FirebaseAnalytics.Param.TRANSACTION_ID, thereference);
                //purchaseParams.putString(FirebaseAnalytics.Param.AFFILIATION, "Brinmo");make this the buusiness name
                purchaseParams.putString(FirebaseAnalytics.Param.CURRENCY, "NGN");
                purchaseParams.putInt(FirebaseAnalytics.Param.VALUE, amountx);
                purchaseParams.putString(FirebaseAnalytics.Param.PAYMENT_TYPE, ttype);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, purchaseParams);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 1s = 1000ms
                        Intent intent = new Intent(CheckOutActivity.this, RatingActivity.class);
                        intent.putExtra("bid", bizidx);
                        startActivity(intent);
                    }
                }, 3000);

            }

            // This is called only before requesting OTP
            // Save reference so you may send to server if
            // error occurs with OTP
            // No need to dismiss dialog
            @Override
            public void beforeValidate(Transaction transaction) {
                CheckOutActivity.this.transaction = transaction;
                //Toast.makeText(CheckOutActivity.this, transaction.getReference(), Toast.LENGTH_LONG).show();
                Toast.makeText(CheckOutActivity.this, "Validating...", Toast.LENGTH_LONG).show();
                updateTextViews();
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                // If an access code has expired, simply ask your server for a new one
                // and restart the charge instead of displaying error
                CheckOutActivity.this.transaction = transaction;
                if (error instanceof ExpiredAccessCodeException) {
                    CheckOutActivity.this.startAFreshCharge(false);
                    CheckOutActivity.this.chargeCard();
                    return;
                }

                dismissDialog();
                //send me an SMS/notice and popup the error name and message
                if (transaction.getReference() != null) {
                    payerror.append(transaction.getReference() + "Error: " + error.getMessage() + " ::\n");
                    FirebaseCrashlytics.getInstance().log("paymentError: "+transaction.getReference()+" : "+error.getMessage());
                    new CheckOutActivity.verifyOnServer().execute(transaction.getReference());
                } else {
                    payerror.append(transaction.getReference() + "Error: " + error.getMessage() + " ::\n");
                    FirebaseCrashlytics.getInstance().log("paymentError: "+ error.getMessage());
                }
                updateTextViews();
            }

        });
    }

    private void dismissDialog() {
        if ((dialog != null) && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void updateTextViews() {
        /*
        if (transaction.getReference() != null) {
            mTextReference.setText(String.format("Reference: %s", transaction.getReference()));
        } else {
            mTextReference.setText("No transaction");
        }*/
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() < 1;
    }

    private class fetchAccessCodeFromServer extends AsyncTask<String, Void, String> {
        private String error;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                charge.setAccessCode(result);
                charge.setAmount(amountx);
                charge.setEmail(mAuth.getEmail());
                charge.setReference(thereference);
                charge.setSubaccount(bizsubaccount);
                charge.setTransactionCharge(chargex);
                charge.setCurrency("NGN");
                chargeCard();
            } else {
                payerror.append("Error: " + error + " ::\n");
                FirebaseCrashlytics.getInstance().log("paymentError: "+error.toString());
                dismissDialog();
            }
        }

        @Override
        protected String doInBackground(String... ac_url) {
            try {
                URL url = new URL(ac_url[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                url.openStream()));

                String inputLine;
                inputLine = in.readLine();
                in.close();
                return inputLine;
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            return null;
        }
    }

    private class verifyOnServer extends AsyncTask<String, Void, String> {
        private String reference;
        private String error;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                FirebaseCrashlytics.getInstance().log("paymentSuccess: "+result.toString());

            } else {
                payerror.append("Error: "+ this.reference + " : " + error + " ::\n");
                FirebaseCrashlytics.getInstance().log("paymentError: problem verifying "+this.reference+", "+error.toString());
                dismissDialog();
            }
        }

        @Override
        protected String doInBackground(String... reference) {
            try {
                this.reference = reference[0];
                URL url = new URL(backend_url + "/verify/" + this.reference);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                url.openStream()));

                String inputLine;
                inputLine = in.readLine();
                in.close();
                return inputLine;
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            return null;
        }
    }

    private void setAlarm(String bid, Long timeinmilli, String bname) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), AlarmBroadcast.class);
        intent.putExtra("bid", bid);
        intent.putExtra("amess", "You placed an order to "+bname+" an hour ago. Have they delivered yet? \n\nIgnore this if you're not expecting the delivery today 😏");
        intent.putExtra("bizname", bname);
        intent.putExtra("username", username);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(CheckOutActivity.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        am.set(AlarmManager.RTC_WAKEUP, timeinmilli, pendingIntent);

    }
}
