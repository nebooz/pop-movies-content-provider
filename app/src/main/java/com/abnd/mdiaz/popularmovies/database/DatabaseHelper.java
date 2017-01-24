package com.abnd.mdiaz.popularmovies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Creates the Movie database used for this application.
 * <p>
 * Created by adammcneilly on 9/19/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DEBUG_TAG = "MovieAppDatabase";

    /**
     * Defines the database version. This variable must be incremented in order for onUpdate to
     * be called when necessary.
     */
    private static final int DATABASE_VERSION = 1;
    /**
     * The name of the database on the device.
     */
    private static final String DATABASE_NAME = "movieApp.db";

    /**
     * Default constructor.
     *
     * @param context The application context using this database.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is first created.
     *
     * @param db The database being created, which all SQL statements will be executed on.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        addTopMovieTable(db);
        addPopMovieTable(db);
        addFavMovieTable(db);
    }

    /**
     * Called whenever DATABASE_VERSION is incremented. This is used whenever schema changes need
     * to be made or new tables are added.
     *
     * @param db         The database being updated.
     * @param oldVersion The previous version of the database. Used to determine whether or not
     *                   certain updates should be run.
     * @param newVersion The new version of the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DEBUG_TAG, "Upgrading database. Existing contents will be lost. ["
                + oldVersion + "]->[" + newVersion + "]");
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.topMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.popMovieEntry.TABLE_NAME);
        onCreate(db);
    }

    /**
     * Inserts the movie table into the database.
     *
     * @param db The SQLiteDatabase the table is being inserted into.
     */
    private void addTopMovieTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + DatabaseContract.topMovieEntry.TABLE_NAME + " (" +
                        DatabaseContract.topMovieEntry.COLUMN_MOVIEDB_ID + " INTEGER PRIMARY KEY, " +
                        DatabaseContract.topMovieEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        DatabaseContract.topMovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        DatabaseContract.topMovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                        DatabaseContract.topMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        DatabaseContract.topMovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                        DatabaseContract.topMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL);"
        );
    }

    private void addPopMovieTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + DatabaseContract.popMovieEntry.TABLE_NAME + " (" +
                        DatabaseContract.popMovieEntry.COLUMN_MOVIEDB_ID + " INTEGER PRIMARY KEY, " +
                        DatabaseContract.popMovieEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        DatabaseContract.popMovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        DatabaseContract.popMovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                        DatabaseContract.popMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        DatabaseContract.popMovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                        DatabaseContract.popMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL);"
        );
    }

    private void addFavMovieTable(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + DatabaseContract.favMovieEntry.TABLE_NAME + " (" +
                        DatabaseContract.favMovieEntry.COLUMN_MOVIEDB_ID + " INTEGER PRIMARY KEY, " +
                        DatabaseContract.favMovieEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                        DatabaseContract.favMovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        DatabaseContract.favMovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                        DatabaseContract.favMovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        DatabaseContract.favMovieEntry.COLUMN_BACKDROP_PATH + " TEXT NOT NULL, " +
                        DatabaseContract.favMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL);"
        );
    }
}