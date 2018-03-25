package com.example.android.popularmovie1;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;

public class DetailActivity extends AppCompatActivity {

    private ImageView dvThumbnail;
    private TextView dvOriginalTitle;
    private TextView dvReleaseDate;
    private TextView dvRating;
    private TextView dvOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        dvThumbnail = (ImageView) findViewById(R.id.iv_thumbnail);
        dvOriginalTitle = (TextView) findViewById(R.id.tv_original_title);
        dvReleaseDate   = (TextView) findViewById(R.id.tv_release_date);
        dvRating        = (TextView) findViewById(R.id.tv_user_rating);
        dvOverview      = (TextView) findViewById(R.id.tv_overview);

        Intent intent = getIntent();
        if(intent.hasExtra("title")){
            String title = intent.getStringExtra("title");
            dvOriginalTitle.setText(title);
        }
        if(intent.hasExtra("release_date")){
            String release_date = "Release date: "+intent.getStringExtra("release_date");
            dvReleaseDate.setText(release_date);
        }
        if(intent.hasExtra("overview")){
            String overview = intent.getStringExtra("overview");
            dvOverview.setText(overview);
        }
        if(intent.hasExtra("rating")){
            String rating = "Rating: "+intent.getStringExtra("rating");
            dvRating.setText(rating);
        }
        if(intent.hasExtra("poster_path")){
            String poster_path = intent.getStringExtra("poster_path");
            URL posterURL = NetworkTools.buildPosterUrl(poster_path);
            Picasso.with(getBaseContext()).load(posterURL.toString()).fit().into(dvThumbnail);
            ViewGroup.LayoutParams lp;
        }
    }
}
