package com.niemisami.movies.utilities;

import android.content.Context;

import com.niemisami.movies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;

/**
 * Created by Sami on 18.1.2017.
 */

public class TmdbJsonParser {


    // Movie list JSON
    /* Page starting from 1*/
    private static final String PAGE = "page";

    /* List of movies in an array*/
    private static final String RESULTS = "results";

    private static final String TITLE = "title";
    private static final String POSTER_PATH = "poster_path";
    private static final String ID = "id";

    // Single movie details JSON
    private static final String BACKDROP_PATH = "backdrop_path";

    public static List<Movie> getBasicMovieInfoFromJson(String rawMovieJsonString)
            throws JSONException {

        JSONObject movieJson = new JSONObject(rawMovieJsonString);

        //TODO: Check error in JSON

        JSONArray movieArray = movieJson.getJSONArray(RESULTS);

        List<Movie> basicMovieInformationArray = new ArrayList<>();

        for (int i = 0; i < movieArray.length(); i++) {

            JSONObject movieObject = movieArray.getJSONObject(i);

            String title = movieObject.getString(TITLE);
            String posterPath = movieObject.getString(POSTER_PATH);
            int id = movieObject.getInt(ID);

            basicMovieInformationArray.add(new Movie(id, title, posterPath));
        }

        return basicMovieInformationArray;
    }

    public static Movie getMovieDetailsFromJson(String rawMovieJsonString) throws JSONException {


        JSONObject movieJson = new JSONObject(rawMovieJsonString);

        //TODO: Check error in JSON

        String title = movieJson.getString(TITLE);
        String posterPath = movieJson.getString(POSTER_PATH);
        int id = movieJson.getInt(ID);
        Movie details = new Movie(id, title, posterPath);

        return details;
    }
}
