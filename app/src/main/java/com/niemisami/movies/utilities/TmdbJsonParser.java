package com.niemisami.movies.utilities;

import android.content.Context;
import android.util.Log;

import com.niemisami.movies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;
import static android.R.attr.rating;
import static android.content.ContentValues.TAG;

/**
 * Created by Sami on 18.1.2017.
 */

public class TmdbJsonParser {


    private static final String TAG = TmdbJsonParser.class.getSimpleName();

    // Movie list JSON
    /* Page starting from 1*/
    private static final String PAGE = "page";

    /* List of menu_movies in an array*/
    private static final String RESULTS = "results";

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String POSTER_PATH = "poster_path";

    /*Error case*/
    private static final String STATUS_CODE = "status_code";
    private static final String STATUS_MESSAGE = "status_message";

    // Single movie details JSON
    private static final String BACKDROP_PATH = "backdrop_path";
    private static final String RELEASE_DATE = "release_date";
    private static final String SYNOPSIS = "overview";
    private static final String RATING = "vote_average";



    public static List<Movie> getBasicMovieInfoFromJson(String rawMovieJsonString)
            throws JSONException {

        JSONObject movieJson = new JSONObject(rawMovieJsonString);

        // Something went wrong and server sent error json
        if(movieJson.has(STATUS_CODE)) {
            return null;
        }

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

        // Something went wrong and server sent error json
        if(movieJson.has(STATUS_CODE)) {
            return null;
        }

        String title = movieJson.getString(TITLE);
        String posterPath = movieJson.getString(POSTER_PATH);
        int id = movieJson.getInt(ID);
        String releaseDate = movieJson.getString(RELEASE_DATE);
        String synopsis = movieJson.getString(SYNOPSIS);
        Double rating = movieJson.getDouble(RATING);
        Movie details = new Movie(id, title, posterPath, releaseDate, synopsis, rating);

        return details;
    }
}
