package com.brinmo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Michael.
 */

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private Context mCtx;
    private List<Item> itemList;
    TextView orderText;
    HashMap<String, Integer> shoppingCart;
    int totalAmounty;
    String pickedy;
    FirebaseAnalytics mFirebaseAnalytics;

    public ItemAdapter(Context mCtx, List<Item> itemList, TextView orderText, HashMap<String, Integer> shoppingCart, int totalAmount, String picked) {
        this.mCtx = mCtx;
        this.itemList = itemList;
        this.orderText = orderText;
        this.shoppingCart = shoppingCart;
        totalAmounty = totalAmount;
        pickedy = picked;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);

        String photourl = "https://firebasestorage.googleapis.com/v0/b/bizzybody-90f3e.appspot.com/o/business%2F"+item.x+"%2F"+item.iid+"_150x150.png?alt=media";
        Picasso.get().load(photourl).transform(new CircleTransform()).into(holder.imageViewPic);

        String itemid = item.iid;
        String itemname = item.name;
        String itemcost = item.cost;
        String itembiz = item.x;
        Long itempulse = item.pulse;//useless for now
        holder.textViewName.setText(itemname);
        String money = "₦"+itemcost;
        holder.textViewCost.setText(money);
        Long datex = new Date().getTime();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mCtx);

        if (itemid.equals(pickedy)){
            //check if the screen was refreshed
            if (shoppingCart.size() == 0){
                totalAmounty = 0;
            }

            shoppingCart.put(itemid, 1);
            String costText = "₦" + itemcost+ " x 1";
            holder.textViewCost.setText(costText);

            totalAmounty = totalAmounty + Integer.parseInt(itemcost);
            String amountText = "₦"+totalAmounty;

            orderText.setText(amountText);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingCart.get(itemid) != null){
                    int num = shoppingCart.get(itemid) + 1;

                    shoppingCart.put(itemid, num);
                    String costText = "₦" + itemcost + " x " + num;
                    holder.textViewCost.setText(costText);

                    totalAmounty = totalAmounty + Integer.parseInt(itemcost);
                    String amountText = "₦"+totalAmounty;

                    orderText.setText(amountText);

                    Bundle itemParams = new Bundle();
                    itemParams.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, itemname);
                    itemParams.putInt(FirebaseAnalytics.Param.PRICE, Integer.parseInt(itemcost));
                    itemParams.putInt("item_tap", num);
                    itemParams.putInt("total_tap", shoppingCart.size());
                    itemParams.putInt("total_cart", totalAmounty);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, itemParams);
                } else {
                    //check if the screen was refreshed
                    if (shoppingCart.size() == 0){
                        totalAmounty = 0;
                    }

                    shoppingCart.put(itemid, 1);
                    String costText = "₦" + itemcost+ " x 1";
                    holder.textViewCost.setText(costText);

                    totalAmounty = totalAmounty + Integer.parseInt(itemcost);
                    String amountText = "₦"+totalAmounty;

                    orderText.setText(amountText);

                    Bundle itemParams = new Bundle();
                    itemParams.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, itemname);
                    itemParams.putInt(FirebaseAnalytics.Param.PRICE, Integer.parseInt(itemcost));
                    itemParams.putInt("item_tap", 1);
                    itemParams.putInt("total_tap", shoppingCart.size());
                    itemParams.putInt("total_cart", totalAmounty);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, itemParams);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewCost;
        ImageView imageViewPic;

        View mView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.i_name);
            textViewCost = itemView.findViewById(R.id.i_cost);
            imageViewPic = itemView.findViewById(R.id.i_image);
            mView = itemView;
        }
    }

    public class CircleTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    public static String capitailizeWord(String str) {
        StringBuffer s = new StringBuffer();

        // Declare a character of space
        // To identify that the next character is the starting
        // of a new word
        char ch = ' ';
        for (int i = 0; i < str.length(); i++) {

            // If previous character is space and current
            // character is not space then it shows that
            // current letter is the starting of the word
            if (ch == ' ' && str.charAt(i) != ' ')
                s.append(Character.toUpperCase(str.charAt(i)));
            else
                s.append(str.charAt(i));
            ch = str.charAt(i);
        }

        // Return the string with trimming
        return s.toString().trim();
    }
}
