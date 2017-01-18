package com.niemisami.movies.utilities;

import android.content.Context;

import com.niemisami.movies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sami on 18.1.2017.
 */

public class TmdbJsonParser {

    public static List<Movie> getBasicMovieInfoFromJson(String rawMovieJsonString)
            throws JSONException {

        /* Page starting from 1*/
        final String PAGE = "page";

        /* List of movies in an array*/
        final String RESULTS = "results";

        final String POSTER_PATH = "poster_path";

        final String TITLE = "title";


        JSONObject movieJson = new JSONObject(rawMovieJsonString);

        //TODO: Check error in JSON

        JSONArray movieArray = movieJson.getJSONArray(RESULTS);

        List<Movie> basicMovieInformationArray = new ArrayList<>();

        for(int i = 0; i < movieArray.length(); i++) {

            JSONObject movieObject = movieArray.getJSONObject(i);

            String title = movieObject.getString(TITLE);
            String posterPath = movieObject.getString(POSTER_PATH);

            basicMovieInformationArray.add(new Movie(title, posterPath));
        }

        return basicMovieInformationArray;
    }
}
