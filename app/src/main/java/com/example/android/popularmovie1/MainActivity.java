package com.example.android.popularmovie1;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieItemClickListener {

    private RecyclerView mRecyclerView;
    private MovieAdapter adapter;
    private byte outputArraysLength;
    boolean sortPopular; // Sort Mode. True = Popular movies, False = top rated movies
    public List<Movie> movieList;
    TextView errorMessageTextView;
    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        errorMessageTextView = (TextView) findViewById(R.id.tv_error_message_diaplay);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_postersGrid);
        sortPopular = true;
        outputArraysLength = 4;

        adapter = new MovieAdapter(getBaseContext(), this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 2));
        makeSearchQuery();
    }
    // check if we are connected to a network
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    private void makeSearchQuery() {
        if(isOnline()) {
            URL MovieUrl = NetworkTools.buildUrl(sortPopular);
            String searchResults = null;
            new MovieQueryTask().execute(MovieUrl);
        }else {
            showErrorMessage();
        }
    }

    private void showPosterGrid(){
        mRecyclerView.setVisibility(View.VISIBLE);
        errorMessageTextView.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.GONE);
    }
    // shows error when unable to load data
    private void showErrorMessage(){
        mRecyclerView.setVisibility(View.GONE);
        errorMessageTextView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.GONE);
    }
    private void showLoadingIndicator(){
        mRecyclerView.setVisibility(View.GONE);
        errorMessageTextView.setVisibility(View.GONE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMovieItemClick(int clickedItemIndex) {
        Context context = MainActivity.this;
        Class detActivity = DetailActivity.class;
        Intent intent = new Intent(context,detActivity);
        intent.putExtra("title", movieList.get(clickedItemIndex).getOriginalTitle());
        intent.putExtra("release_date", movieList.get(clickedItemIndex).getReleaseDate());
        intent.putExtra("overview", movieList.get(clickedItemIndex).getOverview());
        intent.putExtra("rating", movieList.get(clickedItemIndex).getVoteAverage().toString());
        intent.putExtra("poster_path", movieList.get(clickedItemIndex).getPosterPath());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedMenuItem = item.getItemId();
        if (selectedMenuItem == R.id.action_sortMP) {
            sortPopular=true;
        }else if (selectedMenuItem == R.id.action_sortTR) {
            sortPopular = false;
        }
        makeSearchQuery();
        return super.onOptionsItemSelected(item);
    }

    public class MovieQueryTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingIndicator();
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String searchResults = null;
            try {
                searchResults = NetworkTools.getResponseFromHttpUrl(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return searchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s==null){
                showErrorMessage();
                return;
            }
            // parsing the response.
            movieList = new ArrayList<>();
            outputArraysLength = 0;

            if(s!=null && !s.equals("")) {
                JSONObject movieJSON;
                JSONArray movies;
                try {
                    movieJSON = new JSONObject(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    movies = movieJSON.getJSONArray("results");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                for (int i=0; i<movies.length(); i++) {
                    try {
                        JSONObject movieItem =  movies.getJSONObject(i);
                        Movie movie = new Movie();
                        movie.setVoteCount(movieItem.getInt("vote_count"));
                        movie.setId(movieItem.getInt("id"));
                        movie.setVideo(movieItem.getBoolean("video"));
                        movie.setVoteAverage(movieItem.getDouble("vote_average"));
                        movie.setTitle(movieItem.getString("title"));
                        movie.setPopularity(movieItem.getDouble("popularity"));
                        movie.setPosterPath(movieItem.getString("poster_path"));
                        movie.setOriginalLanguage(movieItem.getString("original_language"));
                        movie.setOriginalTitle(movieItem.getString("original_title"));

                        List<Integer> genIDs = new ArrayList<Integer>();
                        JSONArray genre_ids = movieItem.getJSONArray("genre_ids");
                        for (int j=0; j<genre_ids.length(); j++) {
                            genIDs.add(genre_ids.getInt(j));
                        }
                        movie.setGenreIds(genIDs);
                        movie.setBackdropPath(movieItem.getString("backdrop_path"));
                        movie.setAdult(movieItem.getBoolean("adult"));
                        movie.setOverview(movieItem.getString("overview"));
                        movie.setPosterPath(movieItem.getString("poster_path"));
                        movie.setReleaseDate(movieItem.getString("release_date"));
                        movieList.add(movie);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.setMovieData(movieList);
                showPosterGrid();
            }
        }
    }
}
