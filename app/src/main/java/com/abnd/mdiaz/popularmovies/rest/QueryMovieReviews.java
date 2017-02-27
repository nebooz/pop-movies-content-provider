
package com.abnd.mdiaz.popularmovies.rest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.abnd.mdiaz.popularmovies.database.DatabaseContract;
import com.abnd.mdiaz.popularmovies.model.MovieReview;
import com.abnd.mdiaz.popularmovies.utils.SensitiveInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by neboo on 26-Feb-17.
 */

public class QueryMovieReviews {

    private static final String TAG = QueryMovies.class.getSimpleName();
    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String MOVIEDB_REVIEWS = "reviews";
    private static final String MOVIEDB_API_KEY = "?api_key=";

    private static OkHttpClient client = new OkHttpClient();

    public QueryMovieReviews() {
    }

    public static String movieReviewUrl(int movieDbId) {
        return new StringBuilder()
                .append(MOVIEDB_BASE_URL)
                .append(String.valueOf(movieDbId))
                .append("/")
                .append(MOVIEDB_REVIEWS)
                .append(MOVIEDB_API_KEY)
                .append(SensitiveInfo.getMoviesApiKey())
                .toString();
    }

    private static String httpRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @NonNull
    private static MovieReview getReview(Cursor cursor) {

        int movieId;
        String reviewId;
        String reviewAuthor;
        String reviewContent;
        String reviewUrl;

        movieId = cursor
                .getInt(cursor.getColumnIndex(DatabaseContract.movieReviewEntry.COLUMN_MOVIEDB_ID));
        reviewId = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_ID));
        reviewAuthor = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_AUTHOR));
        reviewContent = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_CONTENT));
        reviewUrl = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_URL));

        return new MovieReview(
                movieId,
                reviewId,
                reviewAuthor,
                reviewContent,
                reviewUrl);
    }

    public static void fetchReviews(Context context, String url) {

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = httpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Error closing input stream", e);
        }

        extractReviews(context, jsonResponse);
    }

    public static List<MovieReview> queryAllReviews(Context context, int movieDbId) {

        Uri movieReviewTableUri = DatabaseContract.movieReviewEntry.CONTENT_URI;

        String selectionClause = DatabaseContract.movieReviewEntry.COLUMN_MOVIEDB_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(movieDbId)
        };

        Cursor cursor = context.getContentResolver().query(
                movieReviewTableUri,
                null,
                selectionClause,
                selectionArgs,
                null);

        List<MovieReview> movieReviewList = new ArrayList<>();

        assert cursor != null;

        while (cursor.moveToNext()) {

            movieReviewList.add(getReview(cursor));

        }

        cursor.close();

        return movieReviewList;
    }

    private static void extractReviews(Context context, String movieJson) {

        try {
            // Whole thing
            JSONObject main = new JSONObject(movieJson);

            // Getting the Movie Id
            int movieDbId = main.getInt("id");

            // The review array
            JSONArray results = main.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                // One 'review'
                JSONObject reviewObject = results.getJSONObject(i);

                String reviewId = reviewObject.getString("id");
                String reviewAuthor = reviewObject.getString("author");
                String reviewContent = reviewObject.getString("content");
                String reviewUrl = reviewObject.getString("url");

                MovieReview review = queryReviewId(context, reviewId);

                if (review == null) {

                    insertMovieReview(context, movieDbId, reviewId, reviewAuthor, reviewContent,
                            reviewUrl);

                } else {

                    updateMovieReview(context, movieDbId, reviewId, reviewAuthor, reviewContent,
                            reviewUrl);

                }

            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryMovies", "Problem parsing the Movie JSON results", e);
        }

    }

    private static void insertMovieReview(Context context, int movieDbId, String reviewId,
            String author, String content, String url) {

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        Uri databaseUri = DatabaseContract.movieReviewEntry.CONTENT_URI;
        mNewValues.put(DatabaseContract.movieReviewEntry.COLUMN_MOVIEDB_ID, movieDbId);
        mNewValues.put(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_ID, reviewId);
        mNewValues.put(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_AUTHOR, author);
        mNewValues.put(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_CONTENT, content);
        mNewValues.put(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_URL, url);

        context.getContentResolver().insert(databaseUri, mNewValues);

        Log.d(TAG, String.format("insertMovieReview - MovieID: %d / ReviewID: %s", movieDbId,
                reviewId));
    }

    private static void updateMovieReview(Context context, int movieDbId, String reviewId,
            String author, String content, String url) {

        ContentValues mUpdateValues = new ContentValues();

        Uri databaseUri = DatabaseContract.movieReviewEntry.CONTENT_URI;
        String databaseColumn = DatabaseContract.movieReviewEntry.COLUMN_REVIEW_ID;

        mUpdateValues.put(DatabaseContract.movieReviewEntry.COLUMN_MOVIEDB_ID, movieDbId);
        mUpdateValues.put(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_AUTHOR, author);
        mUpdateValues.put(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_CONTENT, content);
        mUpdateValues.put(DatabaseContract.movieReviewEntry.COLUMN_REVIEW_URL, url);

        String mSelectionClause = databaseColumn + " = ?";
        String[] mSelectionArgs = {
                String.valueOf(reviewId)
        };

        context.getContentResolver().update(
                databaseUri,
                mUpdateValues,
                mSelectionClause,
                mSelectionArgs);

        Log.d(TAG, String.format("updateMovieReview - MovieID: %d / ReviewID: %s", movieDbId,
                reviewId));
    }

    public static MovieReview queryReviewId(Context context, String reviewId) {

        Uri databaseUri;
        String databaseColumn;

        databaseColumn = DatabaseContract.movieReviewEntry.COLUMN_REVIEW_ID;
        databaseUri = DatabaseContract.movieReviewEntry.CONTENT_URI;

        // Constructs a selection clause that matches the current Review ID
        String selectionClause = new StringBuilder().append(databaseColumn).append(" = ?")
                .toString();

        // Moves the current Review ID to the selection arguments
        String[] mSelectionArgs = {
                String.valueOf(reviewId)
        };

        // Does a query against the table and returns a Cursor object
        Cursor cursor = context.getContentResolver().query(
                databaseUri,
                null,
                selectionClause,
                mSelectionArgs,
                null);

        if ((cursor != null ? cursor.getCount() : 0) > 0) {

            cursor.moveToFirst();
            MovieReview review = getReview(cursor);
            cursor.close();

            return review;

        } else {

            cursor.close();
            return null;
        }

    }

}
