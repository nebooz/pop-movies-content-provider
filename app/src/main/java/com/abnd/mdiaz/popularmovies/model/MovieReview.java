
package com.abnd.mdiaz.popularmovies.model;

/**
 * Created by neboo on 20-Feb-17.
 */

public class MovieReview {

    private int mMovieId;
    private String mReviewId;
    private String mAuthor;
    private String mContent;
    private String mUrl;

    public MovieReview(int movieId, String reviewId, String author, String content, String url) {
        this.mMovieId = movieId;
        this.mReviewId = reviewId;
        this.mAuthor = author;
        this.mContent = content;
        this.mUrl = url;
    }

    public int getMovieId() {
        return mMovieId;
    }

    public void setMovieId(int movieId) {
        this.mMovieId = movieId;
    }

    public String getReviewId() {
        return mReviewId;
    }

    public void setReviewId(String reviewId) {
        this.mReviewId = reviewId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }
}
