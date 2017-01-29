package com.niemisami.movies;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Parcelable;
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

    private int mLastFetchedMoviesPage = 1;

    private String mCurrentSortingCriteria;
    private String mSortingByTopRated = "top_rated";
    private String mSortingByPopular = "popular";
    private String mTmdbApiKey;
    private boolean mHasSortingCriteriaChanged;

    private RecyclerView mMoviesRecyclerView;
    private MovieAdapter mMovieAdapter;
    private ProgressBar mProgressBar;
    private TextView mErrorView;
    private List<Movie> mMovies;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);


        mTmdbApiKey = getApiKey();
        if (mTmdbApiKey.length() == 0) {// Check whether api key file has been set
            mErrorView.setText(R.string.error_api_key);

        } else {
            initViews();
        }
    }


    private MenuItem mPopularMoviesMenuItem, mTopRatedMoviesMenuItem;

    private int getMovieGridSpanCount() {
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = 2; // 2 at least
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 3;
        }
        return spanCount;
    }

    private void changeSortingCriteria(String sortingCriteria) {
        mCurrentSortingCriteria = sortingCriteria;
        mHasSortingCriteriaChanged = true;
        displaySortingCriteria();
    }

    private void displaySortingCriteria() {
        if(mTopRatedMoviesMenuItem != null){
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

    private int timesCalled = 0;
    private void fetchMovies() {
        // Stop data load after 20th page
        Log.w(TAG, "fetchMovies: " + ++timesCalled );
        if (mLastFetchedMoviesPage <= 20) {
            new FetchMoviesTask().execute(mCurrentSortingCriteria);
        }
    }

    private void initViews() {
        initToolbar();
        initRecyclerView();
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mErrorView = (TextView) findViewById(R.id.error_view);
    }

    @NonNull
    private String getApiKey() {
        return getResources().getString(R.string.tmdb_api_key);
    }

    boolean isFetchingData;

    private void initRecyclerView() {
        int movieGridSpanCount = getMovieGridSpanCount();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, movieGridSpanCount);

        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(false);
        mMovieAdapter = new MovieAdapter(this);
        mMoviesRecyclerView.setAdapter(mMovieAdapter);

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

//                Log.d(TAG, "onScrolled: " + dx + "x " + dy + "y " + itemCount + " " + lastVisibleItemPosition + isEndReached);
                mToolbar.setTitle(itemCount + " " + lastVisibleItemPosition + isEndReached);
                
                if (!isFetchingData && isEndReached && dy > 0) {
                    isFetchingData = true;
                    fetchMovies();
                }

            }
        });


    }

    private Toolbar mToolbar;

    private void initToolbar() {
        Log.d(TAG, "initToolbar: ");
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
        Log.d(TAG, "onCreateOptionsMenu:  ");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_movies, menu);
        mPopularMoviesMenuItem = menu.findItem(R.id.action_popular);
        mTopRatedMoviesMenuItem = menu.findItem(R.id.action_top_rated);
        displaySortingCriteria();

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


    private static Bundle mBundleRecyclerViewState;

    private static final String STATE_RECYCLER_VIEW_SCROLL_POSITION = "state_recycler_view_scroll_position";
    private static final String STATE_SORTING_CRITERIA = "state_sorting_criteria";


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        // restore RecyclerView state
        restoreLayoutManagerPosition();
        if (mCurrentSortingCriteria == null ||
                !mCurrentSortingCriteria.equals(mSortingByPopular) && !mCurrentSortingCriteria.equals(mSortingByTopRated)) {
            changeSortingCriteria(mSortingByPopular);
        }
        else {
            changeSortingCriteria(mCurrentSortingCriteria);
        }
        fetchMovies();

    }

    @Override
    protected void onPause() {
        super.onPause();
        storeLayoutManagerPosition();
    }

    private void restoreLayoutManagerPosition() {
        if (mBundleRecyclerViewState != null) {
            Parcelable movieGridState = mBundleRecyclerViewState.getParcelable(STATE_RECYCLER_VIEW_SCROLL_POSITION);
            mMoviesRecyclerView.getLayoutManager().onRestoreInstanceState(movieGridState);
        }
    }

    private void storeLayoutManagerPosition() {
        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable movieGridState = mMoviesRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(STATE_RECYCLER_VIEW_SCROLL_POSITION, movieGridState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(STATE_RECYCLER_VIEW_SCROLL_POSITION, mBundleRecyclerViewState);
        outState.putString(STATE_SORTING_CRITERIA, mCurrentSortingCriteria);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mBundleRecyclerViewState = savedInstanceState.getBundle(STATE_RECYCLER_VIEW_SCROLL_POSITION);
            mCurrentSortingCriteria = savedInstanceState.getString(STATE_SORTING_CRITERIA);
        }
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

//    private static final String EXTRA_VIEW_INFO = "extra_view_info";
//    private Bundle bundleViewInfoForTransition(View view) {
//        Bundle viewPositionBundle = new Bundle();
//        int[] screenLocation = new int[2];
//        view.getLocationOnScreen(screenLocation);
//        viewPositionBundle.putInt(PROPNAME_SCREEN_LOCATION_LEFT, screenLocation[0]);
//        viewPositionBundle.putInt(PROPNAME_SCREEN_LOCATION_TOP, screenLocation[1]);
//        viewPositionBundle.putInt(PROPNAME_HEIGHT, view.getHeight());
//        viewPositionBundle.putInt(PROPNAME_WIDTH, view.getWidth());
//
//        return viewPositionBundle;
//    }


    private class FetchMoviesTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);   // Hide and show corresponding menu items
        }

        @Override
        protected String doInBackground(String... strings) {

            URL movieDbUrl = NetworkUtils.buildMovieUrl(strings[0], mTmdbApiKey, mLastFetchedMoviesPage++);

            String movies = "";

            try {
                if (NetworkUtils.isNetworkConnectionAvailable(MoviesActivity.this)) {
                    movies = NetworkUtils.getResponseFromHttpUrl(movieDbUrl);
                } else {
                    return "No internet connection";
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
                        mLastFetchedMoviesPage = 1;
                        mMovies = fetchedMovies;
                        mMovieAdapter.setMovies(mMovies);

                    } else if (fetchedMovies != null) {
                        mMovies.addAll(fetchedMovies);
                        mMovieAdapter.appendMovies(fetchedMovies);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mErrorView.setVisibility(View.VISIBLE);
                }
            } else {
                mMovieAdapter.setMovies(null);
                mMovies = null;
                mErrorView.setVisibility(View.VISIBLE);
            }


            isFetchingData = false;

        }
    }

}
