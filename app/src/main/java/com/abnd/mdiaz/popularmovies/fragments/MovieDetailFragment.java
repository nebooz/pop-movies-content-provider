
package com.abnd.mdiaz.popularmovies.fragments;

import static android.R.color.white;

import static com.abnd.mdiaz.popularmovies.rest.QueryMovies.FAV_MOVIES_TAG;
import static com.abnd.mdiaz.popularmovies.rest.QueryMovies.POP_MOVIES_TAG;
import static com.abnd.mdiaz.popularmovies.rest.QueryMovies.TOP_MOVIES_TAG;

import android.content.ActivityNotFoundException;
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

import com.abnd.mdiaz.popularmovies.R;
import com.abnd.mdiaz.popularmovies.model.Movie;
import com.abnd.mdiaz.popularmovies.model.MovieReview;
import com.abnd.mdiaz.popularmovies.model.MovieVideo;
import com.abnd.mdiaz.popularmovies.rest.QueryMovieReviews;
import com.abnd.mdiaz.popularmovies.rest.QueryMovieVideos;
import com.abnd.mdiaz.popularmovies.rest.QueryMovies;
import com.github.florent37.picassopalette.PicassoPalette;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MovieDetailFragment extends Fragment {

    private static final String TAG = MovieDetailFragment.class.getSimpleName();
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String SMALL_IMAGE_SIZE = "w92";
    private static final String MEDIUM_IMAGE_SIZE = "w185";
    private static final String LARGE_IMAGE_SIZE = "w500";
    private static final int REVIEWS_LIMIT = 3;
    private static final int VIDEOS_LIMIT = 3;
    private ImageView backdropImageView;
    private ImageView posterImageView;
    private ImageView mFavoriteTag;
    private TextView movieTitleTextView;
    private TextView movieRatingTextView;
    private TextView movieReleaseDateTextView;
    private TextView movieSynopsisTextView;
    private TextView movieHeader;
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
    private int mDarkColor;
    private int mLightColor;
    private LinearLayout mMovieDetailReviewsContainer;
    private LinearLayout mMovieDetailVideosContainer;
    private ActionBar mActionBar;
    private String mListType;
    private Movie currentMovie;
    private Boolean favoriteStatus;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    public static MovieDetailFragment newInstance(int movieId, String movieTable,
            boolean isTablet) {

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
        mListType = getArguments().getString("movieTable", QueryMovies.TOP_MOVIES_TAG);

        favoriteStatus = isFavorite();

        String[] queryUrls = {
                QueryMovieReviews.movieReviewUrl(mMovieId),
                QueryMovieVideos.movieVideoUrl(mMovieId)
        };

        new addExtrasToDb().execute(queryUrls);

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

        currentMovie = QueryMovies.queryMovieId(getContext(), mMovieId, mListType);

        mMovieName = currentMovie.getTitle();
        String preFixedReleaseDate = currentMovie.getReleaseDate();
        mMovieRating = currentMovie.getVoteAverage();
        mMoviePosterPath = currentMovie.getPosterPath();
        mMovieBackdropPath = currentMovie.getBackdropPath();
        mMovieSynopsis = currentMovie.getOverview();

        // Proper date
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // Assign all views...
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

        mMovieDetailReviewsContainer = (LinearLayout) view
                .findViewById(R.id.movie_detail_reviews_container);

        mMovieDetailVideosContainer = (LinearLayout) view
                .findViewById(R.id.movie_detail_videos_container);

        movieHeader = (TextView) view.findViewById(R.id.txt_videos_section_header);
        reviewHeader = (TextView) view.findViewById(R.id.txt_review_section_header);

        // Assign values to views...
        movieTitleTextView.setText(mMovieName);
        movieRatingTextView.setText(String.format("%.1f", mMovieRating));
        movieReleaseDateTextView.setText(mMovieReleaseDate);
        movieSynopsisTextView.setText(mMovieSynopsis);

        // Add Fav tag if Movie is in the Fav List
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
                                 * Apparently, there is a difference between just getting the
                                 * Resource straight up or using the getColor method...
                                 */
                                int defaultDarkColor = ContextCompat.getColor(getContext(),
                                        R.color.defaultDarkColor);
                                int defaultLightColor = ContextCompat.getColor(getContext(),
                                        R.color.defaultLightColor);

                                mDarkColor = palette.getDarkMutedColor(ContextCompat
                                        .getColor(getContext(), R.color.defaultDarkColor));

                                if (mDarkColor == defaultDarkColor) {
                                    mDarkColor = palette.getDarkVibrantColor(ContextCompat
                                            .getColor(getContext(), R.color.defaultDarkColor));
                                }

                                mLightColor = palette.getLightMutedColor(ContextCompat
                                        .getColor(getContext(), R.color.defaultLightColor));

                                if (mLightColor == defaultLightColor) {
                                    mLightColor = palette.getLightVibrantColor(ContextCompat
                                            .getColor(getContext(), R.color.defaultDarkColor));
                                }

                                // Rating Score Drawable options
                                GradientDrawable gradientRoundCorners = new GradientDrawable();
                                int highlightColor = ContextCompat.getColor(getContext(), white);

                                gradientRoundCorners.setColors(new int[] {
                                        mLightColor, mDarkColor
                                });
                                gradientRoundCorners.setAlpha(190);
                                gradientRoundCorners.setStroke(3, mDarkColor);
                                gradientRoundCorners.setCornerRadius(20f);

                                movieRatingTextView.setBackground(gradientRoundCorners);
                                movieRatingTextView.setTextColor(highlightColor);
                                movieRatingTextView.setShadowLayer(20, 0, 0, Color.BLACK);

                                // Changing the color of the ActionBar to match the dark movie color
                                mActionBar.setBackgroundDrawable(new ColorDrawable(mDarkColor));

                                movieTitleTextView.setBackgroundColor(mDarkColor);
                                movieTitleTextView.setShadowLayer(10, 0, 0, Color.BLACK);

                                movieReleaseDateTextView.setBackgroundColor(mLightColor);

                                int darkAlphaColor = ColorUtils.setAlphaComponent(mDarkColor, 128);
                                int alphaTextColor = ColorUtils.setAlphaComponent(Color.WHITE, 180);
                                movieSynopsisTextView.setBackgroundColor(darkAlphaColor);
                                movieSynopsisTextView.setTextColor(alphaTextColor);
                                movieSynopsisTextView.setShadowLayer(6, 0, 0, Color.BLACK);

                                movieHeader.setBackgroundColor(mLightColor);
                                // movieHeader.setShadowLayer(10, 0, 0, Color.BLACK);
                                reviewHeader.setBackgroundColor(mLightColor);
                                // reviewHeader.setShadowLayer(10, 0, 0, Color.BLACK);

                            }
                        }));

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

        if (favoriteStatus) {
            addToFavorites.setTitle("Remove from Favorites");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_add_favs:
                if (favoriteStatus) {
                    QueryMovies.removeFromFavorites(getContext(), mMovieId);
                    mFavoriteTag.setVisibility(View.INVISIBLE);
                    favoriteStatus = false;
                } else {
                    QueryMovies.addToFavorites(getContext(), currentMovie);
                    mFavoriteTag.setVisibility(View.VISIBLE);
                    favoriteStatus = true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public boolean isFavorite() {

        return QueryMovies.isFavorite(getContext(), mMovieId);

    }

    public boolean getReviewList(int movieDbId) {

        List<MovieReview> reviewList = QueryMovieReviews.queryAllReviews(getContext(), movieDbId);

        Log.d(TAG, "getReviewList: reviewList Size: " + reviewList.size());

        if (reviewList.size() == 0) {

            TextView noReviewsTextView = (TextView) LayoutInflater.from(getContext())
                    .inflate(R.layout.movie_detail_review_content_view,
                            mMovieDetailReviewsContainer,
                            false);

            View extrasSeparator = LayoutInflater.from(getContext())
                    .inflate(R.layout.extras_separator,
                            mMovieDetailReviewsContainer,
                            false);

            noReviewsTextView.setText(R.string.no_reviews);

            mMovieDetailReviewsContainer.addView(noReviewsTextView);
            mMovieDetailReviewsContainer.addView(extrasSeparator);

            return false;

        } else {

            int reviewCounter = 0;

            for (final MovieReview currentReview : reviewList) {

                if (reviewCounter < REVIEWS_LIMIT) {

                    TextView currentReviewTextView = (TextView) LayoutInflater.from(getContext())
                            .inflate(R.layout.movie_detail_review_content_view,
                                    mMovieDetailReviewsContainer, false);

                    currentReviewTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(currentReview.getUrl()));
                            startActivity(intent);
                        }
                    });

                    View extrasSeparator = LayoutInflater.from(getContext())
                            .inflate(R.layout.extras_separator,
                                    mMovieDetailReviewsContainer,
                                    false);

                    currentReviewTextView.setText(currentReview.getContent());
                    mMovieDetailReviewsContainer.addView(currentReviewTextView);
                    mMovieDetailReviewsContainer.addView(extrasSeparator);

                }

                reviewCounter++;

            }

        }

        return true;

    }

    public boolean getVideoList(int movieDbId) {

        List<MovieVideo> movieList = QueryMovieVideos.queryAllVideos(getContext(), movieDbId);

        Log.d(TAG, "getReviewList: reviewList Size: " + movieList.size());

        if (movieList.size() == 0) {

            LinearLayout noVideosLayout = (LinearLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.movie_detail_video_comp_view, mMovieDetailVideosContainer,
                            false);

            TextView movieNameTextView = (TextView) noVideosLayout
                    .findViewById(R.id.txt_video_name);

            ImageView playImageView = (ImageView) noVideosLayout.findViewById(R.id.img_play);

            View extrasSeparator = LayoutInflater.from(getContext())
                    .inflate(R.layout.extras_separator,
                            mMovieDetailReviewsContainer,
                            false);

            movieNameTextView.setText(R.string.no_videos);
            playImageView.setVisibility(View.GONE);

            mMovieDetailVideosContainer.addView(noVideosLayout);
            mMovieDetailVideosContainer.addView(extrasSeparator);

            return false;

        } else {

            int movieCounter = 0;

            for (final MovieVideo currentVideo : movieList) {

                if (movieCounter < VIDEOS_LIMIT) {

                    LinearLayout videoLayout = (LinearLayout) LayoutInflater.from(getContext())
                            .inflate(R.layout.movie_detail_video_comp_view,
                                    mMovieDetailVideosContainer,
                                    false);

                    TextView movieNameTextView = (TextView) videoLayout
                            .findViewById(R.id.txt_video_name);

                    videoLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("vnd.youtube:" + currentVideo.getKey()));
                                startActivity(intent);
                            } catch (ActivityNotFoundException ex) {
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("http://www.youtube.com/watch?v="
                                                + currentVideo.getKey()));
                                startActivity(intent);
                            }
                        }
                    });

                    View extrasSeparator = LayoutInflater.from(getContext())
                            .inflate(R.layout.extras_separator,
                                    mMovieDetailReviewsContainer,
                                    false);

                    movieNameTextView.setText(currentVideo.getName());
                    mMovieDetailVideosContainer.addView(videoLayout);
                    mMovieDetailVideosContainer.addView(extrasSeparator);

                }

                movieCounter++;

            }

        }

        return true;

    }

    private class addExtrasToDb extends AsyncTask<String, Integer, Boolean> {
        protected Boolean doInBackground(String... urls) {

            QueryMovieReviews.fetchReviews(getContext(), urls[0]);
            QueryMovieVideos.fetchMovies(getContext(), urls[1]);

            return true;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

            getReviewList(mMovieId);
            getVideoList(mMovieId);

        }

    }

}
