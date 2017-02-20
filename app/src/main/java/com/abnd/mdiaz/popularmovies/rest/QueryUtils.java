package com.abnd.mdiaz.popularmovies.rest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.abnd.mdiaz.popularmovies.database.DatabaseContract;
import com.abnd.mdiaz.popularmovies.model.Movie;
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

    public static final String TOP_MOVIES_TAG = "topMovies";
    public static final String POP_MOVIES_TAG = "popMovies";
    public static final String FAV_MOVIES_TAG = "favMovies";
    private static final String TAG = QueryUtils.class.getSimpleName();
    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String MOVIEDB_TOP_MOVIES = "top_rated";
    private static final String MOVIEDB_POP_MOVIES = "popular";
    private static final String MOVIEDB_API_KEY = "?api_key=";

    public static final String TOP_MOVIES_URL = new StringBuilder()
            .append(MOVIEDB_BASE_URL)
            .append(MOVIEDB_TOP_MOVIES)
            .append(MOVIEDB_API_KEY)
            .append(SensitiveInfo.getMoviesApiKey())
            .toString();

    public static final String POP_MOVIES_URL = new StringBuilder()
            .append(MOVIEDB_BASE_URL)
            .append(MOVIEDB_POP_MOVIES)
            .append(MOVIEDB_API_KEY)
            .append(SensitiveInfo.getMoviesApiKey())
            .toString();

    private static OkHttpClient client = new OkHttpClient();

    private QueryUtils() {
    }

    public static void fetchMovies(Context context, String url, String movieTable) {

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = httpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Error closing input stream", e);
        }

        extractMovies(context, jsonResponse, movieTable);
    }

    private static String httpRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private static void extractMovies(Context context, String movieJson, String movieTable) {

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

                Movie movie = queryMovieId(context, movieDbId, movieTable);

                if (movie == null) {

                    insertMovie(context, movieTable, movieName, movieReleaseDate, movieVoteAverage, moviePosterPath, movieBackdropPath, movieOverview, movieDbId);

                } else {

                    updateMovie(context, movieTable, movieName, movieReleaseDate, movieVoteAverage, moviePosterPath, movieBackdropPath, movieOverview, movieDbId);

                }

            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Movie JSON results", e);
        }

    }

    public static List<Movie> queryAllMovies(Context context, String movieTable) {

        Uri movieTableUri;
        String sortOrder = null;

        switch (movieTable) {
            case TOP_MOVIES_TAG:
                movieTableUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                sortOrder = DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
                break;
            case POP_MOVIES_TAG:
                movieTableUri = DatabaseContract.popMovieEntry.CONTENT_URI;
                break;
            case FAV_MOVIES_TAG:
                movieTableUri = DatabaseContract.favMovieEntry.CONTENT_URI;
                break;
            default:
                movieTableUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                break;
        }

        Cursor cursor = context.getContentResolver().query(
                movieTableUri,
                null,
                null,
                null,
                sortOrder);

        List<Movie> movieList = new ArrayList<>();

        assert cursor != null;

        while (cursor.moveToNext()) {

            movieList.add(getMovie(movieTable, cursor));

        }

        cursor.close();

        return movieList;
    }

    public static Movie queryMovieId(Context context, int movieDbId, String movieTable) {

        Uri databaseUri;

        switch (movieTable) {
            case QueryUtils.TOP_MOVIES_TAG:
                databaseUri = DatabaseContract.topMovieEntry.buildTopMovieUri(movieDbId);
                break;

            case QueryUtils.POP_MOVIES_TAG:
                databaseUri = DatabaseContract.popMovieEntry.buildPopMovieUri(movieDbId);
                break;

            default:
                databaseUri = DatabaseContract.topMovieEntry.buildTopMovieUri(movieDbId);
                break;
        }

        // Does a query against the table and returns a Cursor object
        Cursor cursor = context.getContentResolver().query(
                databaseUri,
                null,
                null,
                null,
                null);

        if ((cursor != null ? cursor.getCount() : 0) > 0) {

            cursor.moveToFirst();
            Movie movie = getMovie(movieTable, cursor);
            cursor.close();

            return movie;

        } else {

            cursor.close();
            return null;
        }

    }

    @NonNull
    private static Movie getMovie(String movieTable, Cursor cursor) {

        String movieTitle;
        String movieReleaseDate;
        float movieVoteAverage;
        String moviePosterPath;
        String movieBackdropPath;
        String movieOverview;
        int movieId;

        switch (movieTable) {
            case TOP_MOVIES_TAG:
                movieTitle = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_NAME));
                movieReleaseDate = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE));
                movieVoteAverage = cursor.getFloat(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE));
                moviePosterPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH));
                movieBackdropPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH));
                movieOverview = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW));
                movieId = cursor.getInt(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID));
                break;
            case POP_MOVIES_TAG:
                movieTitle = cursor.getString(cursor.getColumnIndex(DatabaseContract.popMovieEntry.COLUMN_NAME));
                movieReleaseDate = cursor.getString(cursor.getColumnIndex(DatabaseContract.popMovieEntry.COLUMN_RELEASE_DATE));
                movieVoteAverage = cursor.getFloat(cursor.getColumnIndex(DatabaseContract.popMovieEntry.COLUMN_VOTE_AVERAGE));
                moviePosterPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.popMovieEntry.COLUMN_POSTER_PATH));
                movieBackdropPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.popMovieEntry.COLUMN_BACKDROP_PATH));
                movieOverview = cursor.getString(cursor.getColumnIndex(DatabaseContract.popMovieEntry.COLUMN_OVERVIEW));
                movieId = cursor.getInt(cursor.getColumnIndex(DatabaseContract.popMovieEntry.COLUMN_MOVIEDB_ID));
                break;
            case FAV_MOVIES_TAG:
                movieTitle = cursor.getString(cursor.getColumnIndex(DatabaseContract.favMovieEntry.COLUMN_NAME));
                movieReleaseDate = cursor.getString(cursor.getColumnIndex(DatabaseContract.favMovieEntry.COLUMN_RELEASE_DATE));
                movieVoteAverage = cursor.getFloat(cursor.getColumnIndex(DatabaseContract.favMovieEntry.COLUMN_VOTE_AVERAGE));
                moviePosterPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.favMovieEntry.COLUMN_POSTER_PATH));
                movieBackdropPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.favMovieEntry.COLUMN_BACKDROP_PATH));
                movieOverview = cursor.getString(cursor.getColumnIndex(DatabaseContract.favMovieEntry.COLUMN_OVERVIEW));
                movieId = cursor.getInt(cursor.getColumnIndex(DatabaseContract.favMovieEntry.COLUMN_MOVIEDB_ID));
                break;
            default:
                movieTitle = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_NAME));
                movieReleaseDate = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE));
                movieVoteAverage = cursor.getFloat(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE));
                moviePosterPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH));
                movieBackdropPath = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH));
                movieOverview = cursor.getString(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW));
                movieId = cursor.getInt(cursor.getColumnIndex(DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID));
                break;

        }

        return new Movie(
                movieTitle,
                movieReleaseDate,
                movieVoteAverage,
                moviePosterPath,
                movieBackdropPath,
                movieOverview,
                movieId
        );
    }

    private static void insertMovie(Context context, String movieTable, String movieName, String movieReleaseDate, float movieVoteAverage, String moviePosterPath, String movieBackdropPath, String movieOverview, int movieDbId) {

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        Uri databaseUri;

        switch (movieTable) {
            case QueryUtils.TOP_MOVIES_TAG:
                databaseUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_NAME, movieName);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW, movieOverview);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID, movieDbId);
                break;

            case QueryUtils.POP_MOVIES_TAG:
                databaseUri = DatabaseContract.popMovieEntry.CONTENT_URI;
                mNewValues.put(DatabaseContract.popMovieEntry.COLUMN_NAME, movieName);
                mNewValues.put(DatabaseContract.popMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                mNewValues.put(DatabaseContract.popMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                mNewValues.put(DatabaseContract.popMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
                mNewValues.put(DatabaseContract.popMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
                mNewValues.put(DatabaseContract.popMovieEntry.COLUMN_OVERVIEW, movieOverview);
                mNewValues.put(DatabaseContract.popMovieEntry.COLUMN_MOVIEDB_ID, movieDbId);
                break;

            default:
                databaseUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_NAME, movieName);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW, movieOverview);
                mNewValues.put(DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID, movieDbId);
                break;
        }

        context.getContentResolver().insert(databaseUri, mNewValues);

        Log.d(TAG, String.format("insertMovie - Table: %s / ID: %d / Name: %s", movieTable, movieDbId, movieName));
    }

    private static void updateMovie(Context context, String movieTable, String movieName, String movieReleaseDate, float movieVoteAverage, String moviePosterPath, String movieBackdropPath, String movieOverview, int movieDbId) {

        ContentValues mUpdateValues = new ContentValues();
        Uri databaseUri;
        String databaseColumn;

        switch (movieTable) {
            case QueryUtils.TOP_MOVIES_TAG:
                databaseUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                databaseColumn = DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID;
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_NAME, movieName);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW, movieOverview);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID, movieDbId);
                break;

            case QueryUtils.POP_MOVIES_TAG:
                databaseUri = DatabaseContract.popMovieEntry.CONTENT_URI;
                databaseColumn = DatabaseContract.popMovieEntry.COLUMN_MOVIEDB_ID;
                mUpdateValues.put(DatabaseContract.popMovieEntry.COLUMN_NAME, movieName);
                mUpdateValues.put(DatabaseContract.popMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                mUpdateValues.put(DatabaseContract.popMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                mUpdateValues.put(DatabaseContract.popMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
                mUpdateValues.put(DatabaseContract.popMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
                mUpdateValues.put(DatabaseContract.popMovieEntry.COLUMN_OVERVIEW, movieOverview);
                mUpdateValues.put(DatabaseContract.popMovieEntry.COLUMN_MOVIEDB_ID, movieDbId);
                break;

            default:
                databaseUri = DatabaseContract.topMovieEntry.CONTENT_URI;
                databaseColumn = DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID;
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_NAME, movieName);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE, movieReleaseDate);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE, movieVoteAverage);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH, moviePosterPath);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH, movieBackdropPath);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_OVERVIEW, movieOverview);
                mUpdateValues.put(DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID, movieDbId);
                break;
        }

        String mSelectionClause = databaseColumn + " = ?";
        String[] mSelectionArgs = {String.valueOf(movieDbId)};

        context.getContentResolver().update(
                databaseUri,
                mUpdateValues,
                mSelectionClause,
                mSelectionArgs
        );

        Log.d(TAG, String.format("updateMovie - Table: %s / ID: %d / Name: %s", movieTable, movieDbId, movieName));
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

    public static Boolean isFavorite(Context context, int movieDbId) {

        String[] mProjection = {DatabaseContract.favMovieEntry.COLUMN_MOVIEDB_ID};

        String mSelectionClause = DatabaseContract.favMovieEntry.COLUMN_MOVIEDB_ID + " = ?";

        String[] mSelectionArgs = {String.valueOf(movieDbId)};

        Cursor cursor = context.getContentResolver().query(
                DatabaseContract.favMovieEntry.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                null);

        if (null == cursor) {

            Log.e(TAG, "isFavorite: Query Cursor returned null!, zomg!", new SQLiteDatabaseCorruptException());
            return false;

        } else if (cursor.getCount() < 1) {

            cursor.close();
            return false;

        } else {

            //Movie in Favorites
            cursor.close();
            return true;

        }

    }

}