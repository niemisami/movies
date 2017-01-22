package com.niemisami.movies.data;

import android.os.Parcelable;

/**
 * Created by Sami on 15.1.2017.
 */

public class Movie {

    public static final String EXTRA_ID = "extra_id";

    private String mTitle, mPosterPath, mReleaseDate, mSynopsis;
    private double mAverageStars;
    private int mId;


    public Movie(int id, String title, String posterPath) {
        this(id, title, posterPath, "", "" , 0);

    }

    public Movie(int id, String title, String posterPath, String releaseDate, String synopsis, double averageStars) {
        mId = id;
        mTitle = title;
        mPosterPath = posterPath;
        mReleaseDate = releaseDate;
        mSynopsis = synopsis;
        mAverageStars = averageStars;

    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public double getRating() {
        return mAverageStars;
    }
}
