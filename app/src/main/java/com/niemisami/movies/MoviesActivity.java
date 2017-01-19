package com.niemisami.movies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    @Override
    public void onMovieItemClickListener(int itemPosition) {
        if(itemPosition >= 0) {
            int movieId = movies.get(itemPosition).getId();
            Intent movieDetailIntent = new Intent(this, MovieDetailsActivity.class);
            movieDetailIntent.putExtra(Movie.EXTRA_ID, movieId);

            startActivity(movieDetailIntent);
        }

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
