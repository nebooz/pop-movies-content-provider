
package com.abnd.mdiaz.popularmovies.rest;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.abnd.mdiaz.popularmovies.database.DatabaseContract;
import com.abnd.mdiaz.popularmovies.database.DatabaseHelper;

/**
 * Created by neboo on 21-Jan-17.
 */

public class MovieProvider extends ContentProvider {

    private static final String TAG = MovieProvider.class.getSimpleName();

    // Use an int for each URI we will run, this represents the different queries
    private static final int TOP_MOVIE = 100;
    private static final int TOP_MOVIE_ID = 101;
    private static final int POP_MOVIE = 200;
    private static final int POP_MOVIE_ID = 201;
    private static final int FAV_MOVIE = 300;
    private static final int FAV_MOVIE_ID = 301;
    private static final int MOVIE_REVIEW = 1000;
    private static final int MOVIE_VIDEO = 2000;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DatabaseHelper mOpenHelper;

    /**
     * Builds a UriMatcher that is used to determine witch database request is being made.
     */
    public static UriMatcher buildUriMatcher() {
        String content = DatabaseContract.CONTENT_AUTHORITY;

        // All paths to the UriMatcher have a corresponding code to return
        // when a match is found (the ints above).
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(content, DatabaseContract.PATH_TOP_MOVIES, TOP_MOVIE);
        matcher.addURI(content, DatabaseContract.PATH_TOP_MOVIES + "/#", TOP_MOVIE_ID);
        matcher.addURI(content, DatabaseContract.PATH_POP_MOVIES, POP_MOVIE);
        matcher.addURI(content, DatabaseContract.PATH_POP_MOVIES + "/#", POP_MOVIE_ID);
        matcher.addURI(content, DatabaseContract.PATH_FAV_MOVIES, FAV_MOVIE);
        matcher.addURI(content, DatabaseContract.PATH_FAV_MOVIES + "/#", FAV_MOVIE_ID);
        matcher.addURI(content, DatabaseContract.PATH_MOVIE_REVIEWS, MOVIE_REVIEW);
        matcher.addURI(content, DatabaseContract.PATH_MOVIE_VIDEOS, MOVIE_VIDEO);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor retCursor;
        long _id;
        switch (sUriMatcher.match(uri)) {
            case MOVIE_REVIEW:
                retCursor = db.query(
                        DatabaseContract.movieReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TOP_MOVIE:
                retCursor = db.query(
                        DatabaseContract.topMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TOP_MOVIE_ID:
                _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        DatabaseContract.topMovieEntry.TABLE_NAME,
                        projection,
                        DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID + " = ?",
                        new String[] {
                                String.valueOf(_id)
                        },
                        null,
                        null,
                        sortOrder);
                Log.d(TAG, "query: TOP_MOVIE_ID");
                break;
            case POP_MOVIE:
                retCursor = db.query(
                        DatabaseContract.popMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case POP_MOVIE_ID:
                _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        DatabaseContract.popMovieEntry.TABLE_NAME,
                        projection,
                        DatabaseContract.popMovieEntry.COLUMN_MOVIEDB_ID + " = ?",
                        new String[] {
                                String.valueOf(_id)
                        },
                        null,
                        null,
                        sortOrder);
                Log.d(TAG, "query: POP_MOVIE_ID");
                break;
            case FAV_MOVIE:
                retCursor = db.query(
                        DatabaseContract.favMovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAV_MOVIE_ID:
                _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        DatabaseContract.favMovieEntry.TABLE_NAME,
                        projection,
                        DatabaseContract.favMovieEntry.COLUMN_MOVIEDB_ID + " = ?",
                        new String[] {
                                String.valueOf(_id)
                        },
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set the notification URI for the cursor to the one passed into the function. This
        // causes the cursor to register a content observer to watch for changes that happen to
        // this URI and any of it's descendants. By descendants, we mean any URI that begins
        // with this path.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MOVIE_REVIEW:
                return DatabaseContract.movieReviewEntry.CONTENT_TYPE;
            case TOP_MOVIE:
                return DatabaseContract.topMovieEntry.CONTENT_TYPE;
            case TOP_MOVIE_ID:
                return DatabaseContract.topMovieEntry.CONTENT_ITEM_TYPE;
            case POP_MOVIE:
                return DatabaseContract.popMovieEntry.CONTENT_TYPE;
            case POP_MOVIE_ID:
                return DatabaseContract.popMovieEntry.CONTENT_ITEM_TYPE;
            case FAV_MOVIE:
                return DatabaseContract.favMovieEntry.CONTENT_TYPE;
            case FAV_MOVIE_ID:
                return DatabaseContract.favMovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_REVIEW:
                _id = db.insert(DatabaseContract.movieReviewEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = DatabaseContract.movieReviewEntry.buildMovieReviewUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            case TOP_MOVIE:
                _id = db.insert(DatabaseContract.topMovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = DatabaseContract.topMovieEntry.buildTopMovieUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            case POP_MOVIE:
                _id = db.insert(DatabaseContract.popMovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = DatabaseContract.popMovieEntry.buildPopMovieUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            case FAV_MOVIE:
                _id = db.insert(DatabaseContract.favMovieEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = DatabaseContract.favMovieEntry.buildFavMovieUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Use this on the URI passed into the function to notify any observers that the uri has
        // changed.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows; // Number of rows effected

        switch (sUriMatcher.match(uri)) {
            case MOVIE_REVIEW:
                rows = db.delete(DatabaseContract.movieReviewEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case TOP_MOVIE:
                rows = db.delete(DatabaseContract.topMovieEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case POP_MOVIE:
                rows = db.delete(DatabaseContract.popMovieEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case FAV_MOVIE:
                rows = db.delete(DatabaseContract.favMovieEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because null could delete all rows:
        if (selection == null || rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows;

        switch (sUriMatcher.match(uri)) {
            case MOVIE_REVIEW:
                rows = db.update(DatabaseContract.movieReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TOP_MOVIE:
                rows = db.update(DatabaseContract.topMovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case POP_MOVIE:
                rows = db.update(DatabaseContract.popMovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case FAV_MOVIE:
                rows = db.update(DatabaseContract.favMovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }
}
