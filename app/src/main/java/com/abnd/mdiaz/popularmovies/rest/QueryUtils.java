package com.abnd.mdiaz.popularmovies.rest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
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

                Cursor movieCursor = queryMovieId(context, movieDbId);

                // Some providers return null if an error occurs, others throw an exception
                if (null == movieCursor) {

                    Log.e(LOG_TAG, "extractMovies: Query Cursor returned null!, zomg!", new SQLiteDatabaseCorruptException());

                } else if (movieCursor.getCount() < 1) {

                    //No Matches

                    movieCursor.close();

                    insertMovie(context, movieName, movieReleaseDate, movieVoteAverage, moviePosterPath, movieBackdropPath, movieOverview, movieDbId);

                } else {

                    //Movie already in the DB

                    movieCursor.close();

                    updateMovie(context, movieName, movieReleaseDate, movieVoteAverage, moviePosterPath, movieBackdropPath, movieOverview, movieDbId);

                }

            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Movie JSON results", e);
        }

        List<MovieTwo> baseList = new ArrayList<>();

        return baseList;

    }

    public static Cursor queryAllMovies(Context context, String movieTable) {

        Uri movieTableUri;

        switch (movieTable) {
            case "topMovies":
                movieTableUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                break;
            case "popMovies":
                movieTableUri = DatabaseContract.popMovieEntry.CONTENT_URI;
                break;
            case "favMovies":
                movieTableUri = DatabaseContract.favMovieEntry.CONTENT_URI;
                break;
            default:
                movieTableUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                break;
        }

        return context.getContentResolver().query(
                movieTableUri,
                null,
                null,
                null,
                null);
    }

    private static Cursor queryMovieId(Context context, int movieDbId) {
        // A "projection" defines the columns that will be returned for each row
        // Contract class constant for the MOVIEDB_ID column name
        String[] mProjection = {DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID};

        // Constructs a selection clause that matches the current Movie ID
        String mSelectionClause = DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID + " = ?";

        // Moves the current Movie ID to the selection arguments
        String[] mSelectionArgs = {String.valueOf(movieDbId)};

        // Does a query against the table and returns a Cursor object

        return context.getContentResolver().query(
                DatabaseContract.topMovieEntry.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                null);
    }

    private static void insertMovie(Context context, String movieName, String movieReleaseDate, float movieVoteAverage, String moviePosterPath, String movieBackdropPath, String movieOverview, int movieDbId) {
        // Defines a new Uri object that receives the result of the insertion
        Uri mNewUri;

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

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

        Log.d(LOG_TAG, String.format("insertMovie - ID:%d / Name: %s", movieDbId, movieName));
    }

    private static int updateMovie(Context context, String movieName, String movieReleaseDate, float movieVoteAverage, String moviePosterPath, String movieBackdropPath, String movieOverview, int movieDbId) {

        int mRowsUpdated = 0;

        ContentValues mUpdateValues = new ContentValues();

        String mSelectionClause = DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID + " = ?";
        String[] mSelectionArgs = {String.valueOf(movieDbId)};

        mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_NAME, movieName);
        mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
        mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
        mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
        mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
        mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW, movieOverview);

        mRowsUpdated = context.getContentResolver().update(
                DatabaseContract.topMovieEntry.CONTENT_URI,
                mUpdateValues,
                mSelectionClause,
                mSelectionArgs
        );

        Log.d(LOG_TAG, String.format("updateMovie - ID:%d / Name: %s", movieDbId, movieName));

        return mRowsUpdated;
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