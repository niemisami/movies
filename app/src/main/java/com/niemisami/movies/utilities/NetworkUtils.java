package com.niemisami.movies.utilities;

import android.net.Uri;
import android.util.Log;
import android.view.Display;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static String mPosterBaseUrl = "https://image.tmdb.org/t/p/w185";

    public static URL buildUrl(String movieStringURL) {
        Uri uri = Uri.parse(movieStringURL).buildUpon().build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static Uri buildPosterUri(String posterPath) {

        Uri uri = Uri.parse(mPosterBaseUrl + posterPath);
        return uri;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }


}
