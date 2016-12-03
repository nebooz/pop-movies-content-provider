package com.abnd.mdiaz.popularmovies.rest;

import com.abnd.mdiaz.popularmovies.model.MoviesResponse;
import com.abnd.mdiaz.popularmovies.model.ReviewsResponse;
import com.abnd.mdiaz.popularmovies.model.TrailersResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by neboo on 25-Aug-16.
 */
public interface ApiInterface {

    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<TrailersResponse> getMovieTrailers(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewsResponse> getMovieReviews(@Path("id") int movieId, @Query("api_key") String apiKey);

}
