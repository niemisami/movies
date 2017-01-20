package com.niemisami.movies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;
import com.niemisami.movies.utilities.TmdbJsonParser;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MoviesActivity extends AppCompatActivity implements MovieAdapter.OnMovieAdapterItemClickListener {


    private int DEFAULT_SPAN_COUNT = 2;


    private RecyclerView mMoviesRecyclerView;
    private TextView mMoviesRawData;
    private String mTestPopularMoviesUrl = "https://api.themoviedb.org/3/movie/popular?api_key=";
    public static String mPosterBaseUrl = "https://image.tmdb.org/t/p/w500/";
    private String suffix = "&language=en-US";
    private String mTmdbApiKey;

    private MovieAdapter mMovieAdapter;

    private List<Movie> movies;


    public static final String PROPNAME_SCREEN_LOCATION_LEFT = "prop_screen_location_left";
    public static final String PROPNAME_SCREEN_LOCATION_TOP = "prop_screen_location_top";
    public static final String PROPNAME_WIDTH = "prop_width";
    public static final String PROPNAME_HEIGHT = "prop_height";

    public static final AccelerateDecelerateInterpolator DEFAULT_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    public static final int DEFAULT_DURATION = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        mTmdbApiKey = getResources().getString(R.string.tmdb_api_key);

        initRecyclerView();

        new FetchMoviesTask().execute(mTestPopularMoviesUrl + mTmdbApiKey + suffix);

    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, DEFAULT_SPAN_COUNT);
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(false);

        mMovieAdapter = new MovieAdapter(this);
        mMoviesRecyclerView.setAdapter(mMovieAdapter);

    }

    private static final String EXTRA_VIEW_INFO = "extra_view_info";
    @Override
    public void onMovieItemClickListener(View view, int itemPosition) {
        if(itemPosition >= 0) {
            int movieId = movies.get(itemPosition).getId();
            Intent movieDetailIntent = new Intent(this, MovieDetailsActivity.class);
            movieDetailIntent.putExtra(Movie.EXTRA_ID, movieId);

            movieDetailIntent.putExtra(EXTRA_VIEW_INFO, bundleViewInfoForTransition(view));

            startActivity(movieDetailIntent);
            overridePendingTransition(0,0);
        }
    }




    private Bundle bundleViewInfoForTransition(View view) {
        Bundle viewPositionBundle = new Bundle();
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        viewPositionBundle.putInt(PROPNAME_SCREEN_LOCATION_LEFT, screenLocation[0]);
        viewPositionBundle.putInt(PROPNAME_SCREEN_LOCATION_TOP, screenLocation[1]);
        viewPositionBundle.putInt(PROPNAME_HEIGHT, view.getHeight());
        viewPositionBundle.putInt(PROPNAME_WIDTH, view.getWidth());

        return viewPositionBundle;
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            URL movieDbUrl = NetworkUtils.buildUrl(strings[0]);

            String movies = "";

            try {
                movies = NetworkUtils.getResponseFromHttpUrl(movieDbUrl);

                return movies;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                try {
                    movies = TmdbJsonParser.getBasicMovieInfoFromJson(s);
                    mMovieAdapter.setMovies(movies);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                mMovieAdapter.setMovies(null);
                movies = null;
//                mMoviesRawData.setText("No data from db");
            }
        }
    }

}
