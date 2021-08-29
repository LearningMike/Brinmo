package com.brinmo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;


/**
 * Created by Michael.
 */

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private Context mCtx;
    private List<Search> searchList;

    public SearchAdapter(Context mCtx, List<Search> searchList) {
        this.mCtx = mCtx;
        this.searchList = searchList;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.list_layout, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Search search = searchList.get(position);
        holder.textViewName.setText(search.n);
        holder.textViewTitle.setText(search.t);
        if (search.x == 0){
            holder.mStar.setText("New!");
        } else if (((search.s/search.x)*10) <= 2){
            holder.mX.setText(""+search.x);
            holder.mStar.setText(HtmlCompat.fromHtml("&#9733;&#9734;&#9734;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (((search.s/search.x)*10) <= 4){
            holder.mX.setText(""+search.x);
            holder.mStar.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9734;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (((search.s/search.x)*10) <= 6){
            holder.mX.setText(""+search.x);
            holder.mStar.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9734;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (((search.s/search.x)*10) <= 8){
            holder.mX.setText(""+search.x);
            holder.mStar.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9733;&#9734;", HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (((search.s/search.x)*10) <= 10){
            holder.mX.setText(""+search.x);
            holder.mStar.setText(HtmlCompat.fromHtml("&#9733;&#9733;&#9733;&#9733;&#9733;", HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle searchParams = new Bundle();
                searchParams.putString("biz_name", search.n);
                searchParams.putString("biz_title", search.t);
                searchParams.putInt("all_customers", search.x);
                searchParams.putInt("happy_customers", search.s);
                FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(mCtx);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, searchParams);

                Intent intentbiz = new Intent(mCtx, BusinessActivity.class);
                intentbiz.putExtra("bizid", search.bid);
                mCtx.startActivity(intentbiz);
            }
        });

    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewTitle, mStar, mX;

        View mView;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.sr_name);
            textViewTitle = itemView.findViewById(R.id.sr_title);
            mStar = itemView.findViewById(R.id.starView);
            mX = itemView.findViewById(R.id.xView);
            mView = itemView;
        }
    }
}
