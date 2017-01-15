package com.niemisami.movies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MoviesActivity extends AppCompatActivity {


    private int DEFAULT_SPAN_COUNT = 3;

    private RecyclerView mMoviesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        initRecyclerView();



    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, DEFAULT_SPAN_COUNT);
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        mMoviesRecyclerView.setLayoutManager(gridLayoutManager);
        mMoviesRecyclerView.setHasFixedSize(false);
    }


}
