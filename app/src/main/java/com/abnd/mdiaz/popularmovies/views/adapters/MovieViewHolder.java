package com.abnd.mdiaz.popularmovies.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.abnd.mdiaz.popularmovies.MovieDetailActivity;
import com.abnd.mdiaz.popularmovies.MovieListActivity;
import com.abnd.mdiaz.popularmovies.R;
import com.abnd.mdiaz.popularmovies.model.Movie;

import java.util.List;

public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = MovieViewHolder.class.getSimpleName();

    protected ImageView movieThumbnail;
    protected ImageView favoriteTag;
    private List<Movie> mMovieList;
    private Context mContext;
    private String mListType;

    private OnMovieSelectedListener listener;

    public MovieViewHolder(View itemView, List<Movie> movieList, Context context, String listType) {
        super(itemView);

        mMovieList = movieList;
        mContext = context;
        mListType = listType;

        listener = (OnMovieSelectedListener) context;
        movieThumbnail = (ImageView) itemView.findViewById(R.id.card_thumbnail);
        favoriteTag = (ImageView) itemView.findViewById(R.id.img_card_fav_tag);

        itemView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        int position = getAdapterPosition();

        Movie selectedMovie = mMovieList.get(position);
        int selectedMovieId = selectedMovie.getMovieId();

        /*
        This seems to be a very non-optimized way of doing this properly.
        I had to drag the context 2 levels down from the fragment,
        all because I can't get the proper position if the onClick is implemented
        at the RecyclerView...
        */
        boolean isTablet = ((MovieListActivity) mContext).isTablet();

        if (isTablet) {

            listener.onMovieSelected(selectedMovie, mListType);

        } else {

            Intent intent = new Intent(view.getContext(), MovieDetailActivity.class);
            intent.putExtra("is_tablet", false);
            intent.putExtra("movieId", selectedMovieId);

            Log.d(TAG, "Viewholder - onClick / ListType value: " + mListType);

            intent.putExtra("listType", mListType);

            view.getContext().startActivity(intent);

        }

    }

    public interface OnMovieSelectedListener {
        void onMovieSelected(Movie selectedMovie, String movieTable);
    }

}
