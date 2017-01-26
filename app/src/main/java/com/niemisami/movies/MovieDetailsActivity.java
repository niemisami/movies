package com.niemisami.movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;
import com.niemisami.movies.utilities.TmdbJsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

//import static com.niemisami.movies.MoviesActivity.PROPNAME_HEIGHT;
//import static com.niemisami.movies.MoviesActivity.PROPNAME_SCREEN_LOCATION_LEFT;
//import static com.niemisami.movies.MoviesActivity.PROPNAME_SCREEN_LOCATION_TOP;
//import static com.niemisami.movies.MoviesActivity.PROPNAME_WIDTH;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private String mTmdbApiKey;
    private Movie mMovie;
    private TextView mTitleView, mReleaseDateView, mSynospsisView, mRatingsView;

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;


    private ImageView mPosterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        int id = getIntent().getExtras().getInt(Movie.EXTRA_ID);

        mTmdbApiKey = getResources().getString(R.string.tmdb_api_key);
        mPosterView = (ImageView) findViewById(R.id.details_poster_view);

        mTitleView = (TextView) findViewById(R.id.movie_title_label);
        mReleaseDateView = (TextView) findViewById(R.id.movie_release_date);
        mSynospsisView = (TextView) findViewById(R.id.movie_synopsis);
        mRatingsView = (TextView) findViewById(R.id.movie_rating);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
//        mErrorView = (TextView) findViewById(R.id.movie_title_label);
        new FetchMovieDetailsTask().execute(String.valueOf(id));

        initToolbar();


    }

//    private Bundle extractViewPositionForTransition(View view) {
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


    private class FetchMovieDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {

            URL movieDetailsDbUrl = NetworkUtils.buildMovieUrl(strings[0], mTmdbApiKey);

            String movies = "";

            try {

                if(NetworkUtils.isNetworkConnectionAvailable(MovieDetailsActivity.this)) {
                    movies = NetworkUtils.getResponseFromHttpUrl(movieDetailsDbUrl);
                    return movies;
                } else {
                    return null;
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.GONE);
            if (s != null) {
                try {

                    mMovie = TmdbJsonParser.getMovieDetailsFromJson(s);
                    fetchMoviePoster();

                } catch (JSONException e) {
                    e.printStackTrace();
                    mTitleView.setText(getResources().getString(R.string.error_json_parsing));
                }
                mTitleView.setText(mMovie.getTitle());
                mReleaseDateView.setText(mMovie.getReleaseDate());
                mSynospsisView.setText(mMovie.getSynopsis());
                String rating = mMovie.getRating() + "/10";
                mRatingsView.setText(rating);

            } else {
                mTitleView.setText(getResources().getString(R.string.error_fetch_failed));
                mMovie = null;
            }
        }
    }

    private void fetchMoviePoster() {
        if (mMovie != null) {
            Picasso.with(this)
                    .load(NetworkUtils.buildPosterUri("w185",mMovie.getPosterPath().substring(1)))
                    .error(R.mipmap.ic_launcher)
                    .into(mPosterView);
        }
    }

    private void initToolbar() {
        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(TAG, "Initializing toolbar: ",e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_movie_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //    private void onUiReady() {
//        mPosterView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
//            @Override
//            public boolean onPreDraw() {
//                // remove previous listener
//                mPosterView.getViewTreeObserver().removeOnPreDrawListener(this);
//                // prep the scene
//                prepareScene();
//                // run the animation
//                runEnterAnimation();
//                return true;
//            }
//        });
//    }
//
//    private void prepareScene() {
//        // capture the end values in the destionation view
//        mEndValues = captureValues(mDestinationView);
//
//        // calculate the scale and positoin deltas
//        float scaleX = scaleDelta(mStartValues, mEndValues);
//        float scaleY = scaleDelta(mStartValues, mEndValues);
//        int deltaX = translationDelta(mStartValues, mEndValues);
//        int deltaY = translationDelta(mStartValues, mEndValues);
//
//        // scale and reposition the image
//        mPosterView.setScaleX(scaleX);
//        mPosterView.setScaleY(scaleY);
//        mPosterView.setTranslationX(deltaX);
//        mPosterView.setTranslationY(deltaY);
//    }
//
//    private void runExitAnimation() {
//        mPosterView.animate()
//                .setDuration(DEFAULT_DURATION)
//                .setInterpolator(DEFAULT_INTERPOLATOR)
//                .scaleX(scaleX)
//                .scaleY(scaleY)
//                .translationX(deltaX)
//                .translationY(deltaY)
//                .withEndAction(new Runnable() {
//                    @Override
//                    public void run() {
//                        finish();
//                        overridePendingTransition(0, 0);
//                    }
//                }).start();
//    }
}
