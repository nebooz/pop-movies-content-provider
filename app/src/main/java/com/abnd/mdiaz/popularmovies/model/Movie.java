
package com.abnd.mdiaz.popularmovies.model;

/**
 * Created by neboo on 10-Jan-17.
 */

public class Movie {

    private String mTitle;
    private String mReleaseDate;
    private float mVoteAverage;
    private String mPosterPath;
    private String mBackdropPath;
    private String mOverview;
    private int mMovieId;

    public Movie(String title, String releaseDate, float voteAverage, String posterPath,
            String backdropPath, String overview, int movieId) {
        this.mTitle = title;
        this.mReleaseDate = releaseDate;
        this.mVoteAverage = voteAverage;
        this.mPosterPath = posterPath;
        this.mBackdropPath = backdropPath;
        this.mOverview = overview;
        this.mMovieId = movieId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setmeleaseDate(String releaseDate) {
        this.mReleaseDate = releaseDate;
    }

    public float getVoteAverage() {
        return mVoteAverage;
    }

    public void setVoteAverage(float voteAverage) {
        this.mVoteAverage = voteAverage;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public void setPosterPath(String posterPath) {
        this.mPosterPath = posterPath;
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.mBackdropPath = backdropPath;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        this.mOverview = overview;
    }

    public int getMovieId() {
        return mMovieId;
    }

    public void setMovieId(int movieId) {
        this.mMovieId = movieId;
    }
}
