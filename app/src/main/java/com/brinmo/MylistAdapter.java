package com.brinmo;

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

public class MylistAdapter extends RecyclerView.Adapter<MylistAdapter.MylistViewHolder> {

    private Context mCtx;
    private List<Mylist> mylistList;

    public MylistAdapter(Context mCtx, List<Mylist> mylistList) {
        this.mCtx = mCtx;
        this.mylistList = mylistList;
    }

    @NonNull
    @Override
    public MylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.mylist_layout, parent, false);
        return new MylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MylistViewHolder holder, int position) {
        Mylist mylist = mylistList.get(position);

        String photourl = "https://firebasestorage.googleapis.com/v0/b/bizzybody-90f3e.appspot.com/o/business%2F"+mylist.bid+"%2FprofileImage1_150x150.png?alt=media";
        Picasso.get().load(photourl).transform(new CircleTransform()).into(holder.imageViewPic);

        holder.textViewName.setText(mylist.n);
        holder.textViewTitle.setText(mylist.t);
        Long datex = new Date().getTime();

        //get the days
        if ((datex - mylist.l) < 86400000){
            holder.textViewTime.setText("today");
        } else if ((datex - mylist.l) < (86400000*2)) {
            holder.textViewTime.setText("yesterday");
        } else {
            int days = Integer.parseInt(String.valueOf(Math.round(Math.ceil((datex - mylist.l)/86400000))));
            holder.textViewTime.setText(String.format("%s days ago", days));
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentbiz = new Intent(mCtx, BusinessActivity.class);
                intentbiz.putExtra("bizid", mylist.bid);
                mCtx.startActivity(intentbiz);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mylistList.size();
    }

    class MylistViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewTitle, textViewTime;
        ImageView imageViewPic;

        View mView;

        public MylistViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.b_name);
            textViewTitle = itemView.findViewById(R.id.b_type);
            textViewTime = itemView.findViewById(R.id.b_time);
            imageViewPic = itemView.findViewById(R.id.b_image);
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
}
