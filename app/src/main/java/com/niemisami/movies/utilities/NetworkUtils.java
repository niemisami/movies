package com.niemisami.movies.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static String mPosterBaseUrl = "https://image.tmdb.org/t/p";
    private static String mMovieBaseUrl = "https://api.themoviedb.org/3/movie";
    final static String QUERY_PARAM = "q";
    private final static String API_KEY_PARAM = "api_key";
    private final static String LANGUAGE_PARAM = "language";
    private final static String PAGE_PARAM = "page";
    private final static String REGION_PARAM = "region";
    private static int mDefaultPage = 1;
    private static String mDefaultLanguage = "en-US";
    private static String mDefaultRegion = "US";


    public static boolean isNetworkConnectionAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://www.google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();
                if (urlc.getResponseCode() == 200) {
                    return true;
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static URL buildMovieUrl(String queryType, String apiKey, int page) {
        Uri uri = Uri.parse(mMovieBaseUrl).buildUpon()
                .appendPath(queryType)
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .appendQueryParameter(LANGUAGE_PARAM, mDefaultLanguage)
                .appendQueryParameter(PAGE_PARAM, String.valueOf(page))
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }
    public static URL buildMovieUrl(String queryType, String apiKey) {
        Uri uri = Uri.parse(mMovieBaseUrl).buildUpon()
                .appendPath(queryType)
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .appendQueryParameter(LANGUAGE_PARAM, mDefaultLanguage)
                .appendQueryParameter(PAGE_PARAM, String.valueOf(mDefaultPage))
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static Uri buildPosterUri(String fileSize, String posterPath) {
        Uri uri = Uri.parse(mPosterBaseUrl).buildUpon()
                .appendPath(fileSize)
                .appendPath(posterPath)
                .build();
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
