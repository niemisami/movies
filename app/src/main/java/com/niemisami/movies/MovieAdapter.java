package com.niemisami.movies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.niemisami.movies.data.Movie;

import java.util.List;

import static android.R.attr.resource;

/**
 * Created by Sami on 15.1.2017.
 */

public class MovieAdapter extends ArrayAdapter<Movie> {


    public MovieAdapter(Context context, List<Movie> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie= getItem(position);


        return super.getView(position, convertView, parent);
    }
}
