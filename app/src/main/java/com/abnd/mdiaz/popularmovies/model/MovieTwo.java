package com.abnd.mdiaz.popularmovies.model;

/**
 * Created by neboo on 10-Jan-17.
 */

public class MovieTwo {

    private String mTitle;
    private String mReleaseDate;
    private float mVoteAverage;
    private String mPosterPath;
    private String mBackdropPath;
    private String mOverview;
    private int mMovieId;

    public MovieTwo(String title, String releaseDate, float voteAverage, String posterPath, String backdropPath, String overview, int movieId) {
        this.mTitle = title;
        this.mReleaseDate = releaseDate;
        this.mVoteAverage = voteAverage;
        this.mPosterPath = posterPath;
        this.mBackdropPath = backdropPath;
        this.mOverview = overview;
        this.mMovieId = movieId;
    }
}
