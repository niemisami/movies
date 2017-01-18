package com.niemisami.movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.niemisami.movies.data.Movie;
import com.niemisami.movies.utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MoviesActivity extends AppCompatActivity {


    private int DEFAULT_SPAN_COUNT = 2;

    private RecyclerView mMoviesRecyclerView;
    private TextView mMoviesRawData;
    private String mTestPopularMoviesUrl = "https://api.themoviedb.org/3/movie/popular?api_key=";
    public static String mPosterBaseUrl = "https://image.tmdb.org/t/p/w500/";
    private String affex = "&language=en-US";
    private String mTmdbApiKey;

    private MovieAdapter mMovieAdapter;

    private List<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        mTmdbApiKey = getResources().getString(R.string.tmdb_api_key);
//        mMoviesRawData = (TextView) findViewById(R.id.movies_list);


        movies = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            movies.add(
                    new Movie("Movie " + (i + 1), "nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg"));
        }
        initRecyclerView();
//        new FetchMoviesTask().execute(mTestPopularMoviesUrl + mTmdbApiKey + affex);

    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, DEFAULT_SPAN_COUNT);
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(false);

        mMovieAdapter = new MovieAdapter();
        mMoviesRecyclerView.setAdapter(mMovieAdapter);


        mMovieAdapter.setMovies(movies);
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
//                mMoviesRawData.setText(s);
            } else {
//                mMoviesRawData.setText("No data from db");
            }
        }
    }

}
