package com.niemisami.movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;
import com.niemisami.movies.utilities.TmdbJsonParser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import static android.R.attr.id;
import static android.R.attr.scaleX;
import static android.R.attr.scaleY;
import static android.os.Build.VERSION_CODES.M;
import static com.niemisami.movies.MoviesActivity.DEFAULT_DURATION;
import static com.niemisami.movies.MoviesActivity.DEFAULT_INTERPOLATOR;
import static com.niemisami.movies.MoviesActivity.PROPNAME_HEIGHT;
import static com.niemisami.movies.MoviesActivity.PROPNAME_SCREEN_LOCATION_LEFT;
import static com.niemisami.movies.MoviesActivity.PROPNAME_SCREEN_LOCATION_TOP;
import static com.niemisami.movies.MoviesActivity.PROPNAME_WIDTH;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    private String mTmdbApiKey;
    private String mTestPopularMoviesUrl = "https://api.themoviedb.org/3/movie/";
    private String apiString = "?api_key=";
    private String suffix = "&language=en-US";
    private Movie mMovie;
    TextView idView;
    private ImageView mPosterView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        int id = getIntent().getExtras().getInt(Movie.EXTRA_ID);

        mTmdbApiKey = getResources().getString(R.string.tmdb_api_key);
        mPosterView = (ImageView) findViewById(R.id.details_poster_view);

        new FetchMovieDetailsTask().execute(mTestPopularMoviesUrl + String.valueOf(id) + apiString + mTmdbApiKey + suffix);

        idView = (TextView) findViewById(R.id.movie_id);
    }

    private Bundle extractViewPositionForTransition(View view) {
        Bundle viewPositionBundle = new Bundle();
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        viewPositionBundle.putInt(PROPNAME_SCREEN_LOCATION_LEFT, screenLocation[0]);
        viewPositionBundle.putInt(PROPNAME_SCREEN_LOCATION_TOP, screenLocation[1]);
        viewPositionBundle.putInt(PROPNAME_HEIGHT, view.getHeight());
        viewPositionBundle.putInt(PROPNAME_WIDTH, view.getWidth());

        return viewPositionBundle;
    }


    private class FetchMovieDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            URL movieDetailsDbUrl = NetworkUtils.buildUrl(strings[0]);

            String movies = "";

            try {
                movies = NetworkUtils.getResponseFromHttpUrl(movieDetailsDbUrl);

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
                    mMovie = TmdbJsonParser.getMovieDetailsFromJson(s);
                    fetchMoviePoster();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                idView.setText(mMovie.getTitle() + "\n" + mMovie.getPosterPath());
            } else {
                idView.setText("Problem");
                mMovie = null;
//                mMoviesRawData.setText("No data from db");
            }
        }
    }

    private void fetchMoviePoster() {
        Picasso.with(this)
                .load(NetworkUtils.buildPosterUri(mMovie.getPosterPath()))
                .error(R.mipmap.ic_launcher)
                .into(mPosterView, new Callback() {
                    @Override
                    public void onSuccess() {
                        onUiReady();
                    }

                    @Override
                    public void onError() {
                        Log.e(TAG, "Error loading poster");

                    }
                });
    }

    private void onUiReady() {
        mPosterView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // remove previous listener
                mPosterView.getViewTreeObserver().removeOnPreDrawListener(this);
                // prep the scene
                prepareScene();
                // run the animation
                runEnterAnimation();
                return true;
            }
        });
    }

    private void prepareScene() {
        // capture the end values in the destionation view
        mEndValues = captureValues(mDestinationView);

        // calculate the scale and positoin deltas
        float scaleX = scaleDelta(mStartValues, mEndValues);
        float scaleY = scaleDelta(mStartValues, mEndValues);
        int deltaX = translationDelta(mStartValues, mEndValues);
        int deltaY = translationDelta(mStartValues, mEndValues);

        // scale and reposition the image
        mPosterView.setScaleX(scaleX);
        mPosterView.setScaleY(scaleY);
        mPosterView.setTranslationX(deltaX);
        mPosterView.setTranslationY(deltaY);
    }

    private void runExitAnimation() {
        mPosterView.animate()
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(DEFAULT_INTERPOLATOR)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .translationX(deltaX)
                .translationY(deltaY)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        overridePendingTransition(0, 0);
                    }
                }).start();
    }
}
