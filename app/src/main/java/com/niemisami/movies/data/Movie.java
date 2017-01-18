package com.niemisami.movies.data;

/**
 * Created by Sami on 15.1.2017.
 */

public class Movie {

    private String mTitle;
    private String mPosterPath;

    public Movie(String title, String imageSrc) {
        mTitle = title;
        mPosterPath = imageSrc;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        this.mPosterPath = posterPath;
    }
}
