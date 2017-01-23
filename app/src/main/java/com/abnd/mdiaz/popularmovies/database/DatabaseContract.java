package com.abnd.mdiaz.popularmovies.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseContract {
    /**
     * The Content Authority is a name for the entire content provider, similar to the relationship
     * between a domain name and its website. A convenient string to use for content authority is
     * the package name for the app, since it is guaranteed to be unique on the device.
     */
    public static final String CONTENT_AUTHORITY = "com.abnd.mdiaz.popularmovies";
    /**
     * A list of possible paths that will be appended to the base URI for each of the different
     * tables.
     */
    public static final String PATH_TOP_MOVIES = "top_movies";
    public static final String PATH_POP_MOVIES = "pop_movies";
    /**
     * The content authority is used to create the base of all URIs which apps will use to
     * contact this content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DatabaseContract() {
    }

    public static final class topMovieEntry implements BaseColumns {
        // Content URI represents the base location for the table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOP_MOVIES).build();

        // These are special type prefixes that specify if a URI returns a list or a specific item
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_TOP_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_TOP_MOVIES;

        // Define the table schema
        public static final String TABLE_NAME = "topMovieTable";
        public static final String COLUMN_NAME = "topMovieName";
        public static final String COLUMN_RELEASE_DATE = "topMovieReleaseDate";
        public static final String COLUMN_VOTE_AVERAGE = "topMovieVoteAverage";
        public static final String COLUMN_POSTER_PATH = "topMoviePosterPath";
        public static final String COLUMN_BACKDROP_PATH = "topMovieBackdropPath";
        public static final String COLUMN_OVERVIEW = "topMovieOverview";
        public static final String COLUMN_MOVIEDB_ID = "topMovieDbId";

        // Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildTopMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class popMovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POP_MOVIES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_POP_MOVIES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_POP_MOVIES;

        // Define the table schema
        public static final String TABLE_NAME = "popMovieTable";
        public static final String COLUMN_NAME = "popMovieName";
        public static final String COLUMN_RELEASE_DATE = "popMovieReleaseDate";
        public static final String COLUMN_VOTE_AVERAGE = "popMovieVoteAverage";
        public static final String COLUMN_POSTER_PATH = "popMoviePosterPath";
        public static final String COLUMN_BACKDROP_PATH = "popMovieBackdropPath";
        public static final String COLUMN_OVERVIEW = "popMovieOverview";
        public static final String COLUMN_MOVIEDB_ID = "popMovieDbId";

        // Define a function to build a URI to find a specific movie by it's identifier
        public static Uri buildPopMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
