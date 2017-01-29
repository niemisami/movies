package com.niemisami.movies;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;
import com.niemisami.movies.utilities.TmdbJsonParser;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MoviesActivity extends AppCompatActivity implements MovieAdapter.OnMovieAdapterItemClickListener {

    private static final String TAG = MoviesActivity.class.getSimpleName();

    private static final String STATE_RECYCLER_VIEW_SCROLL_POSITION = "state_recycler_view_scroll_position";
    private static final String STATE_SORTING_CRITERIA = "state_sorting_criteria";
    private static final String STATE_LAST_PAGE_LOADED = "state_last_page_loaded";

    private String mCurrentSortingCriteria;
    private String mSortingByTopRated = "top_rated";
    private String mSortingByPopular = "popular";
    private String mTmdbApiKey;
    private boolean mHasSortingCriteriaChanged, mIsFetchingData, mOptionsMenuCreated, mHasAppResumed, mApiKeyError;
    private int mMoviesPage;
    private MovieAdapter mMovieAdapter;

    private RecyclerView mMoviesRecyclerView;
    private static Bundle mBundleRecyclerViewState;

    private MenuItem mPopularMoviesMenuItem, mTopRatedMoviesMenuItem;
    private ProgressBar mProgressBar;
    private TextView mErrorView;
    private Toolbar mToolbar;
    private List<Movie> mMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        // Check whether api key file has been set correctly. Otherwise display error message
        mTmdbApiKey = getApiKey();
        if (mTmdbApiKey.length() == 0) {
            mApiKeyError = true;
            mErrorView = (TextView) findViewById(R.id.error_view);
            mErrorView.setText(R.string.error_api_key);
            mErrorView.setVisibility(View.VISIBLE);

        } else {
            if (savedInstanceState != null) {
                mBundleRecyclerViewState = savedInstanceState.getBundle(STATE_RECYCLER_VIEW_SCROLL_POSITION);
                mCurrentSortingCriteria = savedInstanceState.getString(STATE_SORTING_CRITERIA);
                mMoviesPage = savedInstanceState.getInt(STATE_LAST_PAGE_LOADED);

            } else {
                mCurrentSortingCriteria = mSortingByPopular; // default sorting criteria
                mMoviesPage = 1;
            }

            changeSortingCriteria(mCurrentSortingCriteria);
            initViews();
            fetchMovies();

        }
    }


    /**
     * TMDB api key must be stored in res/values/tmdb_api_key.xml file with id "tmdb_api_key"
     */
    @NonNull
    private String getApiKey() {
        return getResources().getString(R.string.tmdb_api_key);
    }

    /**
     * Change the title on toolbar and reset which page to load first
     */
    private void changeSortingCriteria(String sortingCriteria) {
        mCurrentSortingCriteria = sortingCriteria;
        mHasSortingCriteriaChanged = true;
        mHasAppResumed = false; // reset also recycler view layout manager position
        mMoviesPage = 1;
        displaySortingCriteria();
    }


    private void initViews() {
        initToolbar();
        initRecyclerView();
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mErrorView = (TextView) findViewById(R.id.error_view);
    }


    private void initToolbar() {
        try {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mToolbar.setTitle(R.string.action_label_popular);
            setSupportActionBar(mToolbar);
        } catch (NullPointerException e) {
            Log.e(TAG, "Initializing toolbar: ", e);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_movies, menu);
        mPopularMoviesMenuItem = menu.findItem(R.id.action_popular);
        mTopRatedMoviesMenuItem = menu.findItem(R.id.action_top_rated);
        mOptionsMenuCreated = true;
        displaySortingCriteria();
        return true;
    }


    private void initRecyclerView() {
        int movieGridSpanCount = getMovieGridSpanCount();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, movieGridSpanCount);

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(false);
        mMovieAdapter = new MovieAdapter(this);
        mMoviesRecyclerView.setAdapter(mMovieAdapter);

        // Load new movies when at the near of the end of the list
        mMoviesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleThreshold = 6;
                int itemCount = mMoviesRecyclerView.getLayoutManager().getItemCount();
                int lastVisibleItemPosition = ((GridLayoutManager) mMoviesRecyclerView.getLayoutManager())
                        .findLastVisibleItemPosition();
                boolean isEndReached = itemCount <= (lastVisibleItemPosition + visibleThreshold);

                // Also check dy (amount of vertical scroll) because onScrolled is also called when data is added to recycler view
                if (!mIsFetchingData && isEndReached && dy > 0) {
                    mIsFetchingData = true;
                    fetchMovies();
                }
            }
        });
    }

    /**
     * Grid has been tested only on Nexus 5x. Span might be needed to refine for larger screens
     */
    private int getMovieGridSpanCount() {
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = 2; // 2 at least
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 3;
        }
        return spanCount;
    }

    /**
     * Movies can be sorted by top rated and popular
     */
    private void displaySortingCriteria() {
        if (mOptionsMenuCreated) { // ensure menu items has been initialized before displaying criteria on toolbar
            if (mCurrentSortingCriteria.equals(mSortingByTopRated)) {
                mTopRatedMoviesMenuItem.setVisible(false);
                getSupportActionBar().setTitle(getString(R.string.action_label_top_rated));
                mPopularMoviesMenuItem.setVisible(true);

            } else if (mCurrentSortingCriteria.equals(mSortingByPopular)) {
                mPopularMoviesMenuItem.setVisible(false);
                getSupportActionBar().setTitle(getString(R.string.action_label_popular));
                mTopRatedMoviesMenuItem.setVisible(true);
            }
        }
    }

    /**
     * Start async task and append movies list
     */
    private void fetchMovies() {
        // Stop data load after 20th page
        if (mMoviesPage <= 20) {
            new FetchMoviesTask().execute(mCurrentSortingCriteria);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_popular:
                changeSortingCriteria(mSortingByPopular);
                fetchMovies();
                return true;
            case R.id.action_top_rated:
                changeSortingCriteria(mSortingByTopRated);
                fetchMovies();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mHasAppResumed = true;   // try to restore recycler view's layout manager position after screen rotation
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(STATE_RECYCLER_VIEW_SCROLL_POSITION, mBundleRecyclerViewState);
        outState.putString(STATE_SORTING_CRITERIA, mCurrentSortingCriteria);
        outState.putInt(STATE_LAST_PAGE_LOADED, mMoviesPage);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mApiKeyError) {
            storeLayoutManagerPosition();
        }
    }

    private void restoreLayoutManagerPosition() {
        if (mBundleRecyclerViewState != null && mHasAppResumed) {
            Parcelable movieGridState = mBundleRecyclerViewState.getParcelable(STATE_RECYCLER_VIEW_SCROLL_POSITION);
            mMoviesRecyclerView.getLayoutManager().onRestoreInstanceState(movieGridState);
            mHasAppResumed = false;
        }
    }

    /**
     * Save RecyclerView's layout manager state
     */

    private void storeLayoutManagerPosition() {
        mBundleRecyclerViewState = new Bundle();
        Parcelable movieGridState = mMoviesRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(STATE_RECYCLER_VIEW_SCROLL_POSITION, movieGridState);
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    public void onMovieItemClickListener(View view, int itemPosition) {
        if (itemPosition >= 0) {
            int movieId = mMovies.get(itemPosition).getId();
            Intent movieDetailIntent = new Intent(this, MovieDetailsActivity.class);
            movieDetailIntent.putExtra(Movie.EXTRA_ID, movieId);

            startActivity(movieDetailIntent);
        }
    }


    /**
     * */
    private class FetchMoviesTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);   // Hide and show corresponding menu items
        }

        @Override
        protected String doInBackground(String... strings) {

            URL movieDbUrl = NetworkUtils.buildMovieUrl(strings[0], mTmdbApiKey, mMoviesPage++);

            String movies = "";

            try {
                if (NetworkUtils.isNetworkConnectionAvailable(MoviesActivity.this)) {
                    movies = NetworkUtils.getResponseFromHttpUrl(movieDbUrl);
                } else {
                    return getString(R.string.error_no_internet_connection);
                }
                return movies;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.GONE);
            if (s != null) {
                if (mErrorView.getVisibility() == View.VISIBLE) {
                    mErrorView.setVisibility(View.GONE);
                }
                try {
                    List<Movie> fetchedMovies = TmdbJsonParser.getBasicMovieInfoFromJson(s);
                    if (mMovies == null || mHasSortingCriteriaChanged) {
                        mHasSortingCriteriaChanged = false;
                        mMovies = fetchedMovies;
                        mMovieAdapter.setMovies(mMovies);

                    } else if (fetchedMovies != null) {
                        //MovieAdapter holds a reference of movies list used in activity
                        mMovieAdapter.appendMovies(fetchedMovies);
                    }

                    restoreLayoutManagerPosition();

                } catch (JSONException e) {
                    e.printStackTrace();
                    mErrorView.setVisibility(View.VISIBLE);
                }
            } else {
                mMovieAdapter.setMovies(null);
                mMovies = null;
                mErrorView.setVisibility(View.VISIBLE);
            }


            mIsFetchingData = false;

        }
    }

}
