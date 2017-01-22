package com.niemisami.movies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;
import com.niemisami.movies.utilities.TmdbJsonParser;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static android.view.View.GONE;

public class MoviesActivity extends AppCompatActivity implements MovieAdapter.OnMovieAdapterItemClickListener {


    private static final String TAG = MoviesActivity.class.getSimpleName();

    private int DEFAULT_SPAN_COUNT = 2;

    private RecyclerView mMoviesRecyclerView;
    private TextView mMoviesRawData;
    private String mTestPopularMoviesUrl = "popular";
    private String mTestTopRatedMoviesUri = "top_rated";
    private String mTmdbApiKey;

    private MovieAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private TextView mErrorView;

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

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mErrorView = (TextView) findViewById(R.id.error_view);
        initRecyclerView();
        new FetchMoviesTask().execute(mTestPopularMoviesUrl);
        initToolbar();

    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, DEFAULT_SPAN_COUNT);


        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(false);

        mMovieAdapter = new MovieAdapter(this);
        mMoviesRecyclerView.setAdapter(mMovieAdapter);

    }

    private Toolbar mToolbar;

    private void initToolbar() {
        try {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mToolbar.setTitle(R.string.action_label_popular);
            setSupportActionBar(mToolbar);
        } catch (NullPointerException e) {
            Log.e(TAG, "Initializing toolbar: ", e);
        }
    }

    private MenuItem mPopularMoviesMenuItem, mTopRatedMoviesMenuItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_movies, menu);
        mPopularMoviesMenuItem = menu.findItem(R.id.action_popular);
        mTopRatedMoviesMenuItem = menu.findItem(R.id.action_top_rated);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_popular:
                fetchPopularMovies();
                return true;
            case R.id.action_top_rated:
                fetchTopRatedMovies();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchPopularMovies() {
        mPopularMoviesMenuItem.setVisible(false);
        getSupportActionBar().setTitle(getString(R.string.action_label_popular));
        new FetchMoviesTask().execute(mTestPopularMoviesUrl);
        mTopRatedMoviesMenuItem.setVisible(true);
    }

    private void fetchTopRatedMovies() {
        mTopRatedMoviesMenuItem.setVisible(false);
        getSupportActionBar().setTitle(getString(R.string.action_label_top_rated));
        new FetchMoviesTask().execute(mTestTopRatedMoviesUri);
        mPopularMoviesMenuItem.setVisible(true);
    }

    private static final String EXTRA_VIEW_INFO = "extra_view_info";

    @Override
    public void onMovieItemClickListener(View view, int itemPosition) {
        if (itemPosition >= 0) {
            int movieId = movies.get(itemPosition).getId();
            Intent movieDetailIntent = new Intent(this, MovieDetailsActivity.class);
            movieDetailIntent.putExtra(Movie.EXTRA_ID, movieId);

            startActivity(movieDetailIntent);
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
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {

            URL movieDbUrl = NetworkUtils.buildMovieUrl(strings[0], mTmdbApiKey);


            Log.d(TAG, "onPostExecute: " + movieDbUrl.toString());

            String movies = "";

            try {
                if(NetworkUtils.isNetworkConnectionAvailable(MoviesActivity.this)) {
                    movies = NetworkUtils.getResponseFromHttpUrl(movieDbUrl);
                } else {
                    return null;
                }

                if (movies != null) {
                    return movies;
                }
                return "No internet connection";

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.GONE);
            if (s != null) {
                if(mErrorView.getVisibility() == View.VISIBLE) {
                    mErrorView.setVisibility(View.GONE);
                }
                try {
                    movies = TmdbJsonParser.getBasicMovieInfoFromJson(s);
                    mMovieAdapter.setMovies(movies);
                } catch (JSONException e) {
                    e.printStackTrace();
                    mErrorView.setVisibility(View.VISIBLE);
                }
            } else {
                mMovieAdapter.setMovies(null);
                movies = null;
                mErrorView.setVisibility(View.VISIBLE);

//                mMoviesRawData.setText("No data from db");
            }
        }
    }

}
