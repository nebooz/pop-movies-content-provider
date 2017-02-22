package com.abnd.mdiaz.popularmovies.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.abnd.mdiaz.popularmovies.R;
import com.abnd.mdiaz.popularmovies.model.Movie;
import com.abnd.mdiaz.popularmovies.model.MovieReview;
import com.abnd.mdiaz.popularmovies.rest.QueryUtils;
import com.github.florent37.picassopalette.PicassoPalette;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.R.color.white;
import static com.abnd.mdiaz.popularmovies.rest.QueryUtils.FAV_MOVIES_TAG;
import static com.abnd.mdiaz.popularmovies.rest.QueryUtils.POP_MOVIES_TAG;
import static com.abnd.mdiaz.popularmovies.rest.QueryUtils.TOP_MOVIES_TAG;

public class MovieDetailFragment extends Fragment {

    private static final String TAG = MovieDetailFragment.class.getSimpleName();
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SMALL_IMAGE_SIZE = "w92";
    private static final String MEDIUM_IMAGE_SIZE = "w185";
    private static final String LARGE_IMAGE_SIZE = "w500";
    OnDatabaseChangedListener mCallback;
    private ImageView backdropImageView;
    private ImageView posterImageView;
    private ImageView mFavoriteTag;
    private TextView movieTitleTextView;
    private TextView movieRatingTextView;
    private TextView movieReleaseDateTextView;
    private TextView movieSynopsisTextView;
    private TextView trailerHeader;
    private TextView reviewHeader;
    private ScrollView movieDetailScrollView;
    private ProgressBar mMovieDetailProgressBar;
    private String mMovieName;
    private int mMovieId;
    private String mMoviePosterPath;
    private String mMovieBackdropPath;
    private double mMovieRating;
    private String mMovieSynopsis;
    private String mMovieReleaseDate;
    private boolean isTablet;
    private int mDarkColor;
    private int mLightColor;
    private LinearLayout mMovieDetailExtrasContainer;
    private LinearLayout mReviewContainer;
    private ActionBar mActionBar;
    private String mListType;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    public static MovieDetailFragment newInstance(int movieId, String movieTable, boolean isTablet) {

        MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putInt("movieId", movieId);
        args.putString("movieTable", movieTable);
        args.putBoolean("is_tablet", isTablet);
        movieDetailFragment.setArguments(args);
        return movieDetailFragment;

    }

    private static String dateFormat(String releaseDate) {

        String inputString = "yyyy-MM-dd";
        String outputString = "MMMM dd, yyyy";

        SimpleDateFormat parser = new SimpleDateFormat(inputString);
        SimpleDateFormat properForm = new SimpleDateFormat(outputString);

        String formattedDate = null;

        try {
            Date properDate = parser.parse(releaseDate);
            formattedDate = properForm.format(properDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return formattedDate;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mMovieId = getArguments().getInt("movieId", 1);
        mListType = getArguments().getString("movieTable", QueryUtils.TOP_MOVIES_TAG);

        new addReviewsToDatabase().execute(QueryUtils.movieReviewUrl(mMovieId));

        Log.d(TAG, "onCreate - mListType value: " + mListType);

        AppCompatActivity mActivity = (AppCompatActivity) getActivity();
        mActionBar = mActivity.getSupportActionBar();

        switch (mListType) {
            case TOP_MOVIES_TAG:
                mActionBar.setTitle("Top Movies");
                break;
            case POP_MOVIES_TAG:
                mActionBar.setTitle("Popular Movies");
                break;
            case FAV_MOVIES_TAG:
                mActionBar.setTitle("Favorite Movies");
                break;
        }

        Movie selectedMovie = QueryUtils.queryMovieId(getContext(), mMovieId, mListType);

        mMovieName = selectedMovie.getTitle();
        String preFixedReleaseDate = selectedMovie.getReleaseDate();
        mMovieRating = selectedMovie.getVoteAverage();
        mMoviePosterPath = selectedMovie.getPosterPath();
        mMovieBackdropPath = selectedMovie.getBackdropPath();
        mMovieSynopsis = selectedMovie.getOverview();

        //Proper date
        mMovieReleaseDate = this.getString(R.string.release_date) + dateFormat(preFixedReleaseDate);

    }

    @Override
    public void onStart() {
        super.onStart();
        mMovieDetailProgressBar.setVisibility(View.GONE);
        movieDetailScrollView.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        //Assign all views...
        movieDetailScrollView = (ScrollView) view.findViewById(R.id.movie_detail_scrollview);
        mMovieDetailProgressBar = (ProgressBar) view.findViewById(R.id.movie_detail_progress_bar);

        mMovieDetailProgressBar.setVisibility(View.VISIBLE);
        movieDetailScrollView.setVisibility(View.GONE);

        backdropImageView = (ImageView) view.findViewById(R.id.img_backdrop);
        posterImageView = (ImageView) view.findViewById(R.id.img_poster);
        movieTitleTextView = (TextView) view.findViewById(R.id.txt_title);
        movieRatingTextView = (TextView) view.findViewById(R.id.txt_rating);
        movieReleaseDateTextView = (TextView) view.findViewById(R.id.txt_release_date);
        movieSynopsisTextView = (TextView) view.findViewById(R.id.txt_synopsis);

        mMovieDetailExtrasContainer = (LinearLayout) view.findViewById(R.id.movie_detail_extras_container);


        //trailerHeader = (TextView) view.findViewById(R.id.txt_trailer_header);
        //reviewHeader = (TextView) view.findViewById(R.id.txt_review_header);

        //Assign values to views...
        movieTitleTextView.setText(mMovieName);
        movieRatingTextView.setText(String.format("%.1f", mMovieRating));
        movieReleaseDateTextView.setText(mMovieReleaseDate);
        movieSynopsisTextView.setText(mMovieSynopsis);

        //Add Fav tag if Movie is in the Fav List
        mFavoriteTag = (ImageView) view.findViewById(R.id.img_favorite_tag);
        if (isFavorite()) {
            mFavoriteTag.setVisibility(View.VISIBLE);
        }

        String fullPosterPath = IMAGE_BASE_URL + MEDIUM_IMAGE_SIZE + mMoviePosterPath;
        String fullBackdropPath = IMAGE_BASE_URL + LARGE_IMAGE_SIZE + mMovieBackdropPath;

        Picasso.with(getContext()).load(fullBackdropPath).into(backdropImageView);

        Picasso.with(getContext()).load(fullPosterPath).into(
                posterImageView,
                PicassoPalette.with(fullPosterPath, posterImageView)
                        .intoCallBack(new PicassoPalette.CallBack() {
                            @Override
                            public void onPaletteLoaded(Palette palette) {

                                /*
                                Apparently, there is a difference between just getting the Resource
                                straight up or using the getColor method...
                                */
                                int defaultDarkColor = ContextCompat.getColor(getContext(),
                                        R.color.defaultDarkColor);
                                int defaultLightColor = ContextCompat.getColor(getContext(),
                                        R.color.defaultLightColor);

                                mDarkColor = palette.getDarkMutedColor(ContextCompat.
                                        getColor(getContext(), R.color.defaultDarkColor));

                                if (mDarkColor == defaultDarkColor) {
                                    mDarkColor = palette.getDarkVibrantColor(ContextCompat.
                                            getColor(getContext(), R.color.defaultDarkColor));
                                }

                                mLightColor = palette.getLightMutedColor(ContextCompat.
                                        getColor(getContext(), R.color.defaultLightColor));

                                if (mLightColor == defaultLightColor) {
                                    mLightColor = palette.getLightVibrantColor(ContextCompat.
                                            getColor(getContext(), R.color.defaultDarkColor));
                                }

                                //Rating Score Drawable options
                                GradientDrawable gradientRoundCorners = new GradientDrawable();
                                int highlightColor = ContextCompat.getColor(getContext(), white);

                                gradientRoundCorners.setColors(new int[]{mLightColor, mDarkColor});
                                gradientRoundCorners.setAlpha(190);
                                gradientRoundCorners.setStroke(3, mDarkColor);
                                gradientRoundCorners.setCornerRadius(20f);

                                movieRatingTextView.setBackground(gradientRoundCorners);
                                movieRatingTextView.setTextColor(highlightColor);
                                movieRatingTextView.setShadowLayer(20, 0, 0, Color.BLACK);

                                //Changing the color of the ActionBar to match the dark movie color
                                mActionBar.setBackgroundDrawable(new ColorDrawable(mDarkColor));

                                movieTitleTextView.setBackgroundColor(mDarkColor);
                                movieTitleTextView.setShadowLayer(10, 0, 0, Color.BLACK);

                                movieReleaseDateTextView.setBackgroundColor(mLightColor);

                                int darkAlphaColor = ColorUtils.setAlphaComponent(mDarkColor, 128);
                                int alphaTextColor = ColorUtils.setAlphaComponent(Color.WHITE, 180);
                                movieSynopsisTextView.setBackgroundColor(darkAlphaColor);
                                movieSynopsisTextView.setTextColor(alphaTextColor);
                                movieSynopsisTextView.setShadowLayer(6, 0, 0, Color.BLACK);

                                //trailerHeader.setBackgroundColor(mDarkColor);
                                //reviewHeader.setBackgroundColor(mDarkColor);

                            }
                        })
        );

        //mTrailerContainer = (LinearLayout) view.findViewById(R.id.trailer_list_container);
        //getTrailerList(mMovieId);

        //mReviewContainer = (LinearLayout) view.findViewById(R.id.review_list_container);
        //getReviewList(mMovieId);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.movie_detail_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem addToFavorites = menu.findItem(R.id.menu_add_favs);

        if (isFavorite()) {
            addToFavorites.setTitle("Remove from Favorites");
        }

    }

    public boolean isFavorite() {

        return QueryUtils.isFavorite(getContext(), mMovieId);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        isTablet = getArguments().getBoolean("is_tablet");

        if (isTablet) {

            try {
                mCallback = (OnDatabaseChangedListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString()
                        + " must implement OnDatabaseChangedListener");
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_add_favs:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public boolean getReviewList(int movieDbId) {

        List<MovieReview> reviewList = QueryUtils.queryAllReviews(getContext(), movieDbId);

        Log.d(TAG, "getReviewList: reviewList Size: " + reviewList.size());

        if (reviewList.size() == 0) {

            TextView noReviewsTextView = (TextView) LayoutInflater.from(getContext())
                    .inflate(R.layout.movie_detail_review_content_view, mMovieDetailExtrasContainer, false);

            noReviewsTextView.setText("No reviews available.");

            mMovieDetailExtrasContainer.addView(noReviewsTextView);

            return false;

        } else {

            for (final MovieReview currentReview : reviewList) {

                TextView currentReviewTextView = (TextView) LayoutInflater.from(getContext())
                        .inflate(R.layout.movie_detail_review_content_view, mMovieDetailExtrasContainer, false);

                currentReviewTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(currentReview.getUrl()));
                        startActivity(intent);
                    }
                });

                currentReviewTextView.setText(currentReview.getContent());
                mMovieDetailExtrasContainer.addView(currentReviewTextView);

            }

        }

        return true;

    }

    public interface OnDatabaseChangedListener {
        void onDatabaseUpdate();
    }

    private class addReviewsToDatabase extends AsyncTask<String, Integer, Boolean> {
        protected Boolean doInBackground(String... urls) {

            int count = urls.length;
            for (int i = 0; i < count; i++) {

                QueryUtils.fetchReviews(getContext(), urls[i]);

                publishProgress((int) ((i / (float) count) * 100));

            }

            return true;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

            if (getReviewList(mMovieId)) {
                Toast.makeText(getContext(), "Reviews Acquired Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No Reviews Available", Toast.LENGTH_SHORT).show();
            }

        }

    }


}