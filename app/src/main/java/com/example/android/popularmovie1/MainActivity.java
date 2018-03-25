package com.example.android.popularmovie1;

import android.content.ContentValues;
import android.content.Context;
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
     //   String priceQuery = mSearchBoxEditText.getText().toString();
        if(isOnline()) {
            URL MovieUrl = NetworkTools.buildUrl(sortPopular);
            String searchResults = null;
            Log.i("sss", MovieUrl.toString());
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
    public void onMovieItemClick(int ClickedItemIndex) {
        Context context = MainActivity.this;
        String message = String.valueOf(ClickedItemIndex);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

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
//            Context context = MainActivity.this;
//            String message = "sort clicked";
//            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
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
//            mRecyclerView.setVisibility(View.GONE);
//            mLoadingIndicator.setVisibility(View.VISIBLE);
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
            Log.i("sss", "started");
            return searchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            // parsing the response.
            Log.i("sss", s);
            movieList = new ArrayList<>();
            outputArraysLength = 0;
            //List<URL> posterUrlList = new ArrayList<URL>();


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
                        movieList.add(movie);


                        Log.d("getPosterPath", movie.getPosterPath());
                        Log.d("title", movie.getTitle());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                adapter.setMovieData(movieList);
//                mRecyclerView.setVisibility(View.VISIBLE);
//                mLoadingIndicator.setVisibility(View.INVISIBLE);
                showPosterGrid();

            }
        }
    }
}


/*
    private EditText mSearchBoxEditText;
    private RecyclerView mRecyclerView;

    private PriceAdapter adapter;

    private static int NUMBER_OF_SECONDS_IN_INTERVAL = 86400;
    private static int MAX_OUTPUT_ARRAY_LIST = 25;

    private byte outputArraysLength;

    private List<PriceListItem> priceList;

    private Toast mToast;

    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_priceLines);

        outputArraysLength = 0;

        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading);

        adapter = new PriceAdapter(getBaseContext(),this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
    }

    // method to display the result query URL
    private void makeSearchQuery() {
        String priceQuery = mSearchBoxEditText.getText().toString();
        URL priceUrl = NetworkTools.buildUrl(priceQuery,MAX_OUTPUT_ARRAY_LIST);
        String searchResults = null;
        Log.i("sss", priceUrl.toString());
        new PriceQueryTask().execute(priceUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        makeSearchQuery();
        return super.onOptionsItemSelected(item);
    }


    public void mDisplayError(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        Toast.makeText(this,R.string.error_message,Toast.LENGTH_LONG).show();
    }
    public String convertUnixDateToSting(long unixSeconds){
        Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formating (see comment at the bottom
        String formattedDate = sdf.format(date);
        int pos = formattedDate.indexOf(' ');
        if(pos == -1){
            return "";
        }
        else {
            formattedDate = formattedDate.substring(0,pos);
        }
        return formattedDate;
    }

    @Override
    public void onPriceItemClick(int ClickedItemIndex) {
        String msg = priceList.get(ClickedItemIndex).date +" "+ priceList.get(ClickedItemIndex).price;
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

public class PriceQueryTask extends AsyncTask<URL, Void, String> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    protected String doInBackground(URL... urls) {
        URL searchUrl = urls[0];
        String searchResults = null;
        try{
            searchResults = NetworkTools.getResponseFromHttpUrl(searchUrl);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Log.i("sss", "started");
        return searchResults;
    }

    @Override
    protected void onPostExecute(String s) {
        // parsing the response. see the response format below
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        float previousPrice = 0;
        float currentCloePriceF = 0;
        if(s!=null && !s.equals("")) {

            mRecyclerView.setVisibility(View.VISIBLE);

            priceList = new ArrayList<>();
            outputArraysLength = 0;
            int pos = s.indexOf("TIMEZONE_OFFSET");
            if(pos==-1){
                mDisplayError();
            }else{
                s = s.substring(pos+1,s.length());
                pos = s.indexOf("\n");
                s = s.substring(pos+1,s.length());
                pos = s.indexOf("\n");
                int commaPos = -1, aPos = -1;
                String currentDate, currentClosePrice;
                String currentDateStr;
                int currentIncrement = 0;
                long currentUnixDate =0;
                String currentString = "";
                String currentChange = "0.00%";
                // looping through lines of data, extracting comma separated values
                while (pos>-1){

                    currentString = s.substring(0,pos);
                    Log.i("sss", currentString);
                    s = s.substring(pos+1,s.length());
                    pos = currentString.indexOf("TIMEZONE_OFFSET");
                    if(pos==-1){
                        commaPos = currentString.indexOf(',');
                        currentDate = currentString.substring(0,commaPos);
                        currentString = currentString.substring(commaPos+1,currentString.length());
                        commaPos = currentString.indexOf(',');
                        currentClosePrice = currentString.substring(0,commaPos);
                        // In the first position of the data line there is either a base-date in Unix format (which starts with symbol 'a'
                        // or the increment of the date relative to the base-date (measured in number of seconds in the interval
                        // see the format sample below

                        if(currentDate.indexOf('a')>-1){
                            try {
                                currentUnixDate = Long.parseLong(currentDate.substring(1, currentDate.length()));
                            }
                            catch ( NumberFormatException e) {
                                Log.e("error", e.toString());
                                mDisplayError();
                                continue;
                            }
                            currentDateStr = convertUnixDateToSting(currentUnixDate);
                        }else {
                            try {
                                currentIncrement = Integer.parseInt(currentDate);
                            }
                            catch ( NumberFormatException e) {
                                Log.e("error", e.toString());
                                mDisplayError();
                                continue;
                            }
                            currentDateStr = convertUnixDateToSting(currentUnixDate +currentIncrement*NUMBER_OF_SECONDS_IN_INTERVAL);
                        }
                        currentCloePriceF = Float.valueOf(currentClosePrice);

                        if(previousPrice == 0){
                            currentChange = "0.00%";
                        }
                        else{
                            currentChange = String.format("%.2f",100*(currentCloePriceF-previousPrice)/previousPrice) +'%';
                        }
                        PriceListItem current = new PriceListItem();

                        if(previousPrice == 0) {
                            current.img = R.drawable.neutral;
                        }
                        else if(previousPrice>currentCloePriceF) {
                            current.img = R.drawable.down;
                        }else{
                            current.img = R.drawable.up;
                        }
                        current.date =currentDateStr;
                        current.price = "$" + currentClosePrice;
                        current.change = currentChange;
                        // filling the data source

                        priceList.add(current);
                        previousPrice = currentCloePriceF;


                        outputArraysLength+=1;

                    }
                    pos = s.indexOf("\n");
                }
            }
            adapter.setPriceData(priceList);
        }else{
            mDisplayError();
        }

    }
}
*/
