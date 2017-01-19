package com.niemisami.movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;
import com.niemisami.movies.utilities.TmdbJsonParser;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

import static android.R.attr.id;
import static android.os.Build.VERSION_CODES.M;

public class MovieDetailsActivity extends AppCompatActivity {

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
                .into(mPosterView);

    }
}
