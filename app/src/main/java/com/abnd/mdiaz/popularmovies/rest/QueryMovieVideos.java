
package com.abnd.mdiaz.popularmovies.rest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.abnd.mdiaz.popularmovies.database.DatabaseContract;
import com.abnd.mdiaz.popularmovies.model.MovieVideo;
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

public class QueryMovieVideos {

    private static final String TAG = QueryMovies.class.getSimpleName();
    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String MOVIEDB_VIDEOS = "videos";
    private static final String MOVIEDB_API_KEY = "?api_key=";

    private static OkHttpClient client = new OkHttpClient();

    public QueryMovieVideos() {
    }

    public static String movieVideoUrl(int movieDbId) {
        return new StringBuilder()
                .append(MOVIEDB_BASE_URL)
                .append(String.valueOf(movieDbId))
                .append("/")
                .append(MOVIEDB_VIDEOS)
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
    private static MovieVideo getVideo(Cursor cursor) {

        int movieId;
        String videoId;
        String iso_639_1;
        String iso_3166_1;
        String key;
        String name;
        String site;
        int size;
        String type;

        movieId = cursor
                .getInt(cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_MOVIEDB_ID));
        videoId = cursor
                .getString(cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ID));
        iso_639_1 = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ISO_639_1));
        iso_3166_1 = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ISO_3166_1));
        key = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_KEY));
        name = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_NAME));
        site = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_SITE));
        size = cursor
                .getInt(cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_SIZE));
        type = cursor.getString(
                cursor.getColumnIndex(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_TYPE));

        return new MovieVideo(
                movieId,
                videoId,
                iso_639_1,
                iso_3166_1,
                key,
                name,
                site,
                size,
                type);
    }

    public static void fetchMovies(Context context, String url) {

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = httpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Error closing input stream", e);
        }

        extractVideos(context, jsonResponse);
    }

    public static List<MovieVideo> queryAllVideos(Context context, int movieDbId) {

        Uri movieReviewTableUri = DatabaseContract.movieVideoEntry.CONTENT_URI;

        String selectionClause = DatabaseContract.movieVideoEntry.COLUMN_MOVIEDB_ID + " = ?";

        String[] selectionArgs = {
                String.valueOf(movieDbId)
        };

        Cursor cursor = context.getContentResolver().query(
                movieReviewTableUri,
                null,
                selectionClause,
                selectionArgs,
                null);

        List<MovieVideo> movieVideoList = new ArrayList<>();

        assert cursor != null;

        while (cursor.moveToNext()) {

            movieVideoList.add(getVideo(cursor));

        }

        cursor.close();

        return movieVideoList;
    }

    private static void extractVideos(Context context, String videoJson) {

        try {
            // Whole thing
            JSONObject main = new JSONObject(videoJson);

            // Getting the Movie Id
            int movieDbId = main.getInt("id");

            // The videos array
            JSONArray results = main.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {

                // One 'video'
                JSONObject videoObject = results.getJSONObject(i);

                String videoId = videoObject.getString("id");
                String videoIso639 = videoObject.getString("iso_639_1");
                String videoIso3166 = videoObject.getString("iso_3166_1");
                String videoKey = videoObject.getString("key");
                String videoName = videoObject.getString("name");
                String videoSite = videoObject.getString("site");
                int videoSize = videoObject.getInt("size");
                String videoType = videoObject.getString("type");

                MovieVideo video = queryVideoId(context, videoId);

                if (video == null) {

                    insertMovieVideo(context, movieDbId, videoId, videoIso639, videoIso3166,
                            videoKey, videoName, videoSite, videoSize, videoType);

                } else {

                    updateMovieVideo(context, movieDbId, videoId, videoIso639, videoIso3166,
                            videoKey, videoName, videoSite, videoSize, videoType);

                }

            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryMovies", "Problem parsing the Movie JSON results", e);
        }

    }

    private static void insertMovieVideo(Context context, int movieDbId, String videoId,
            String videoIso639, String videoIso3166, String videoKey, String videoName,
            String videoSite, int videoSize, String videoType) {

        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();

        Uri databaseUri = DatabaseContract.movieVideoEntry.CONTENT_URI;

        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_MOVIEDB_ID, movieDbId);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ID, videoId);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ISO_639_1, videoIso639);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ISO_3166_1, videoIso3166);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_KEY, videoKey);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_NAME, videoName);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_SITE, videoSite);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_SIZE, videoSize);
        mNewValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_TYPE, videoType);

        context.getContentResolver().insert(databaseUri, mNewValues);

        Log.d(TAG, String.format("insertMovieVideo - MovieID: %d / videoID: %s", movieDbId,
                videoId));
    }

    private static void updateMovieVideo(Context context, int movieDbId, String videoId,
            String videoIso639, String videoIso3166, String videoKey, String videoName,
            String videoSite, int videoSize, String videoType) {

        ContentValues mUpdateValues = new ContentValues();

        Uri databaseUri = DatabaseContract.movieVideoEntry.CONTENT_URI;
        String databaseColumn = DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ID;

        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_MOVIEDB_ID, movieDbId);
        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ISO_639_1, videoIso639);
        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ISO_3166_1, videoIso3166);
        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_KEY, videoKey);
        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_NAME, videoName);
        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_SITE, videoSite);
        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_SIZE, videoSize);
        mUpdateValues.put(DatabaseContract.movieVideoEntry.COLUMN_VIDEO_TYPE, videoType);

        String mSelectionClause = databaseColumn + " = ?";
        String[] mSelectionArgs = {
                String.valueOf(videoId)
        };

        context.getContentResolver().update(
                databaseUri,
                mUpdateValues,
                mSelectionClause,
                mSelectionArgs);

        Log.d(TAG, String.format("updateMovieVideo - MovieID: %d / videoID: %s", movieDbId,
                videoId));
    }

    public static MovieVideo queryVideoId(Context context, String videoId) {

        Uri databaseUri;
        String databaseColumn;

        databaseColumn = DatabaseContract.movieVideoEntry.COLUMN_VIDEO_ID;
        databaseUri = DatabaseContract.movieVideoEntry.CONTENT_URI;

        // Constructs a selection clause that matches the current Video ID
        String selectionClause = new StringBuilder().append(databaseColumn).append(" = ?")
                .toString();

        // Moves the current Video ID to the selection arguments
        String[] mSelectionArgs = {
                String.valueOf(videoId)
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
            MovieVideo video = getVideo(cursor);
            cursor.close();

            return video;

        } else {

            cursor.close();
            return null;
        }

    }

}
