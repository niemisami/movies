package com.niemisami.movies.data;

import android.os.Parcelable;

/**
 * Created by Sami on 15.1.2017.
 */

public class Movie {

    public static final String EXTRA_ID = "extra_id";

    private String mTitle;
    private String mPosterPath;
    private int mId;


    public Movie(int id, String title, String imageSrc) {
        mId = id;
        mTitle = title;
        mPosterPath = imageSrc;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public int getId() {
        return mId;
    }
}
