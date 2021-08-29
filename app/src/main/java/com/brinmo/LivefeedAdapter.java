package com.brinmo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Date;
import java.util.List;


/**
 * Created by Michael.
 */

public class LivefeedAdapter extends RecyclerView.Adapter<LivefeedAdapter.LivefeedViewHolder> {

    private Context mCtx;
    private List<Livefeed> livefeedList;

    public LivefeedAdapter(Context mCtx, List<Livefeed> livefeedList) {
        this.mCtx = mCtx;
        this.livefeedList = livefeedList;
    }

    @NonNull
    @Override
    public LivefeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.livefeed_layout, parent, false);
        return new LivefeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LivefeedViewHolder holder, int position) {
        Livefeed livefeed = livefeedList.get(position);

        String photourl = "https://firebasestorage.googleapis.com/v0/b/bizzybody-90f3e.appspot.com/o/business%2F"+livefeed.x+"%2F"+livefeed.iid+"_150x150.png?alt=media";
        Picasso.get().load(photourl).transform(new LivefeedAdapter.CircleTransform()).into(holder.imageViewPic);

        String lfitemname = livefeed.iid.replaceAll("-"," ");
        String newlfitemname = capitailizeWord(lfitemname);
        holder.textViewName.setText(newlfitemname);
        holder.textViewBiz.setText(livefeed.n);
        Long datex = new Date().getTime();

        //get the minutes/hours
        if ((datex - livefeed.t) < 60000){
            holder.textViewTime.setText("just now");
        } else if ((datex - livefeed.t) < (60000*2)) {
            holder.textViewTime.setText("a minute ago");
        } else if ((datex - livefeed.t) > (60000*2) && (datex - livefeed.t) < (60000*60)) {
            int minutes = Integer.parseInt(String.valueOf(Math.round(Math.ceil((datex - livefeed.t)/60000))));
            holder.textViewTime.setText(String.format("%s minutes ago", minutes));
        } else if ((datex - livefeed.t) < (60000*60*2)){
            holder.textViewTime.setText("an hour ago");
        } else if ((datex - livefeed.t) > (60000*60)) {
            int hours = Integer.parseInt(String.valueOf(Math.round(Math.ceil((datex - livefeed.t)/(60000*60)))));
            holder.textViewTime.setText(String.format("%s hours ago", hours));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLiv = new Intent(mCtx, InventoryActivity.class);
                intentLiv.putExtra("bid", livefeed.x);
                intentLiv.putExtra("picked", livefeed.iid);
                mCtx.startActivity(intentLiv);
            }
        });
    }

    @Override
    public int getItemCount() {
        return livefeedList.size();
    }

    class LivefeedViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewBiz, textViewTime;
        ImageView imageViewPic;

        View mView;

        public LivefeedViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.i_name);
            textViewBiz = itemView.findViewById(R.id.i_biz);
            textViewTime = itemView.findViewById(R.id.i_time);
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
