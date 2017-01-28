package com.abnd.mdiaz.popularmovies.rest;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.abnd.mdiaz.popularmovies.model.Movie;

import java.util.List;

/**
 * Created by neboo on 11-Aug-16.
 */
public class MovieLoader extends AsyncTaskLoader<List<Movie>> {

    private String mUrl;

    public MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Movie> loadInBackground() {

        return QueryUtils.fetchMovies(getContext(), mUrl);

    }
}
