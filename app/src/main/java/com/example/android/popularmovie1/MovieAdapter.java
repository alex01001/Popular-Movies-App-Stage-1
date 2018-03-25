package com.example.android.popularmovie1;


import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter <MovieAdapter.MyViewHolder>{

    final private MovieItemClickListener onClickListener;
    private LayoutInflater inflater;
    List<Movie> data = Collections.emptyList();
    private Context context;

    public interface MovieItemClickListener {
        void onMovieItemClick(int ClickedItemIndex);
    }

    public MovieAdapter (Context tContext, MovieItemClickListener listener){
        context = tContext;
        inflater = LayoutInflater.from(tContext);
        onClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.grid_item,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Movie current = data.get(position);
        URL posterURL = NetworkTools.buildPosterUrl(current.getPosterPath());
        Picasso.with(context).load(posterURL.toString()).resize(185,277).centerCrop().into(holder.posterImg);

        ViewGroup.LayoutParams lp;
        lp = holder.posterImg.getLayoutParams();
        lp.height = 270*Resources.getSystem().getDisplayMetrics().widthPixels/(2*185)-16;
    }
    public void setMovieData (List<Movie> mData){
        data = mData;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(data==null) return 0;
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        private ImageView posterImg;

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            onClickListener.onMovieItemClick(clickedPosition);
        }


        public MyViewHolder(View itemView) {
            super(itemView);
            posterImg = (ImageView) itemView.findViewById(R.id.iv_poster);
            itemView.setOnClickListener(this);
        }
    }
}
