
package com.abnd.mdiaz.popularmovies.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.abnd.mdiaz.popularmovies.R;
import com.abnd.mdiaz.popularmovies.model.Movie;
import com.abnd.mdiaz.popularmovies.rest.QueryMovies;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SMALL_IMAGE_SIZE = "w92";
    private static final String MEDIUM_IMAGE_SIZE = "w185";
    private static final String LARGE_IMAGE_SIZE = "w500";

    private Context mContext;
    private List<Movie> mMovieList = new ArrayList<>();
    private String mListType;

    public MovieAdapter(Context context, List<Movie> movieList, String listType) {
        mContext = context;
        mMovieList = movieList;
        mListType = listType;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_card, parent, false);

        return new MovieViewHolder(v, mMovieList, mContext, mListType);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {

        Movie currentMovie = mMovieList.get(position);

        if (QueryMovies.isFavorite(mContext, currentMovie.getMovieId())) {

            holder.favoriteTag.setVisibility(View.VISIBLE);

        } else {

            holder.favoriteTag.setVisibility(View.GONE);
        }

        String fullPosterPath = IMAGE_BASE_URL + MEDIUM_IMAGE_SIZE + currentMovie.getPosterPath();

        Picasso.with(mContext).load(fullPosterPath).into(holder.movieThumbnail);

    }

    @Override
    public int getItemCount() {
        return (null != mMovieList ? mMovieList.size() : 0);
    }

    public void setListType(String listType) {
        mListType = listType;
        Log.d(TAG, "setListType: ListType Value: " + listType);
    }

    public void setMovieList(List<Movie> movieList) {

        mMovieList.addAll(movieList);
        notifyDataSetChanged();

    }

    public void clearData() {

        mMovieList.clear();
    }
}
