package com.abnd.mdiaz.popularmovies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.abnd.mdiaz.popularmovies.fragments.MovieDetailFragment;
import com.abnd.mdiaz.popularmovies.model.Movie;

public class MovieDetailActivity extends AppCompatActivity {

    private final String MOVIE_DETAIL_FRAGMENT_TAG = "movieDetail";
    private MovieDetailFragment movieDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        FrameLayout activityBaseLayout = (FrameLayout) findViewById(R.id.movie_detail_fragment_container);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.list_mini_dark);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bmp);
        bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        activityBaseLayout.setBackground(bitmapDrawable);

        Movie selectedMovie = getIntent().getParcelableExtra("selected_movie");
        boolean isTablet = getIntent().getBooleanExtra("is_tablet", false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(selectedMovie.getTitle());

        if (savedInstanceState == null) {

            movieDetailFragment = MovieDetailFragment.newInstance(selectedMovie, isTablet);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.movie_detail_fragment_container, movieDetailFragment, MOVIE_DETAIL_FRAGMENT_TAG)
                    .commit();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
