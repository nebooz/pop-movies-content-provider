package com.abnd.mdiaz.popularmovies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.abnd.mdiaz.popularmovies.fragments.MovieDetailFragment;
import com.abnd.mdiaz.popularmovies.fragments.MovieListFragment;
import com.abnd.mdiaz.popularmovies.model.Movie;
import com.abnd.mdiaz.popularmovies.model.MovieTwo;
import com.abnd.mdiaz.popularmovies.rest.MovieLoader;
import com.abnd.mdiaz.popularmovies.rest.QueryUtils;
import com.abnd.mdiaz.popularmovies.views.adapters.MovieViewHolder;

import java.util.List;


public class MovieListActivity extends AppCompatActivity implements
        MovieViewHolder.OnMovieSelectedListener,
        MovieDetailFragment.OnDatabaseChangedListener,
        LoaderManager.LoaderCallbacks<List<MovieTwo>> {

    public static final String INTER_FRAGMENT_TAG = "InterFragment";
    private static final String TAG = MovieListActivity.class.getSimpleName();
    private final String MOVIE_DETAIL_FRAGMENT_TAG = "movieDetail";
    private boolean isTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check online status before updating the DB with movies
        boolean isConnected = checkConnectivity();

        setContentView(R.layout.activity_movie_list);

        // Call this to determine which layout we are in (tablet or phone)
        determinePaneLayout();

        LinearLayout activityBaseLayout;

        if (!isTwoPane) {

            activityBaseLayout = (LinearLayout) findViewById(R.id.movie_list_fragment_container);


        } else {

            activityBaseLayout = (LinearLayout) findViewById(R.id.large_base_layout);

        }

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.list_mini_dark);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        activityBaseLayout.setBackground(bitmapDrawable);

    }

    private boolean checkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            getSupportLoaderManager().initLoader(0, null, null);
            return true;
        } else {
            return false;
        }

    }

    @Override
    public Loader<List<MovieTwo>> onCreateLoader(int id, Bundle args) {
        return new MovieLoader(this.getApplicationContext(), QueryUtils.FULL_TEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<MovieTwo>> loader, List<MovieTwo> data) {

    }

    @Override
    public void onLoaderReset(Loader<List<MovieTwo>> loader) {


    }


    public boolean isTablet() {
        return isTwoPane;
    }

    private void determinePaneLayout() {
        FrameLayout movieDetailFragmentContainer = (FrameLayout) findViewById(R.id.large_movie_detail_fragment_container);
        // If there is a second pane for details
        if (movieDetailFragmentContainer != null) {
            Log.d(TAG, "determinePaneLayout: App is running on a Tablet!");
            isTwoPane = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "MovieListActivity has been resumed.");
    }

    @Override
    public void onMovieSelected(Movie selectedMovie) {

        if (isTwoPane) {

            MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance(selectedMovie, isTwoPane);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.large_movie_detail_fragment_container, movieDetailFragment, MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();
        }

    }

    @Override
    public void onDatabaseUpdate() {
        MovieListFragment movieListFragment = (MovieListFragment)
                getSupportFragmentManager().findFragmentById(R.id.movie_list_fragment);

        movieListFragment.getMovieList(INTER_FRAGMENT_TAG);
    }
}