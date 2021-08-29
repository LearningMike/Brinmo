package com.brinmo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Date;
import java.util.List;


/**
 * Created by Michael.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context mCtx;
    private List<Review> reviewList;

    public ReviewAdapter(Context mCtx, List<Review> reviewList) {
        this.mCtx = mCtx;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.review_layout, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        String photourl = "https://firebasestorage.googleapis.com/v0/b/bizzybody-90f3e.appspot.com/o/customer%2F"+review.cid+"%2FprofileImage_150x150.png?alt=media";
        Picasso.get().load(photourl).transform(new CircleTransform()).into(holder.imageViewPic);

        holder.textViewName.setText(review.cname);
        holder.textViewReview.setText(review.rev);
        Long datex = new Date().getTime();

        //get the days
        if ((datex - review.time) < 86400000){
            holder.textViewTime.setText("today");
        } else if ((datex - review.time) < (86400000*2)) {
            holder.textViewTime.setText("yesterday");
        } else {
            int days = Integer.parseInt(String.valueOf(Math.round(Math.ceil((datex - review.time)/86400000))));
            holder.textViewTime.setText(String.format("%s days ago", days));
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewReview, textViewTime;
        ImageView imageViewPic;

        View mView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.c_name);
            textViewReview = itemView.findViewById(R.id.c_review);
            textViewTime = itemView.findViewById(R.id.c_time);
            imageViewPic = itemView.findViewById(R.id.c_image);
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
