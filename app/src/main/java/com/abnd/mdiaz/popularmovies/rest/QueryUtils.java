package com.abnd.mdiaz.popularmovies.rest;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.abnd.mdiaz.popularmovies.database.DatabaseContract;
import com.abnd.mdiaz.popularmovies.model.MovieTwo;
import com.abnd.mdiaz.popularmovies.utils.SensitiveInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QueryUtils {

    public static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String MOVIEDB_TOP_MOVIES = "top_rated";
    private static final String MOVIEDB_POP_MOVIES = "popular";
    private static final String MOVIEDB_API_KEY = "?api_key=";
    public static final String FULL_TEST_URL = new StringBuilder().append(MOVIEDB_BASE_URL).append(MOVIEDB_TOP_MOVIES).append(MOVIEDB_API_KEY).append(SensitiveInfo.getMoviesApiKey()).toString();
    private static OkHttpClient client = new OkHttpClient();

    private QueryUtils() {
    }

    public static List<MovieTwo> fetchMovies(Context context, String url) {

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = httpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        return extractMovies(context, jsonResponse);
    }

    private static String httpRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static List<MovieTwo> extractMovies(Context context, String movieJson) {

        List<MovieTwo> movies = new ArrayList<>();

        try {
            //Whole thing
            JSONObject main = new JSONObject(movieJson);

            //The movie array
            JSONArray results = main.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                //One 'movie'
                JSONObject movieObject = results.getJSONObject(i);

                String movieName = movieObject.getString("title");
                String movieReleaseDate = movieObject.getString("release_date");
                float movieVoteAverage = (float) movieObject.getDouble("vote_average");
                String moviePosterPath = movieObject.getString("poster_path");
                String movieBackdropPath = movieObject.getString("backdrop_path");
                String movieOverview = movieObject.getString("overview");
                int movieDbId = movieObject.getInt("id");

                // Defines a new Uri object that receives the result of the insertion
                Uri mNewUri;

                // Defines an object to contain the new values to insert
                ContentValues mNewValues = new ContentValues();

                /*
                 * Sets the values of each column and inserts the movie. The arguments to the "put"
                 * method are "column name" and "value"
                 */
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_NAME, movieName);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW, movieOverview);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID, movieDbId);

                mNewUri = context.getContentResolver().insert(
                        DatabaseContract.topMovieEntry.CONTENT_URI, mNewValues
                );

            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the News JSON results", e);
        }

        return movies;
    }

    private static Bitmap getBitmap(String thumbnail) {

        if (thumbnail != null) {
            try {
                return BitmapFactory.decodeStream((InputStream) new URL(thumbnail).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}