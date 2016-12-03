package com.abnd.mdiaz.popularmovies.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
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
import com.abnd.mdiaz.popularmovies.model.Review;
import com.abnd.mdiaz.popularmovies.model.ReviewsResponse;
import com.abnd.mdiaz.popularmovies.model.Trailer;
import com.abnd.mdiaz.popularmovies.model.TrailersResponse;
import com.abnd.mdiaz.popularmovies.rest.ApiClient;
import com.abnd.mdiaz.popularmovies.rest.ApiInterface;
import com.abnd.mdiaz.popularmovies.utils.SensitiveInfo;
import com.github.florent37.picassopalette.PicassoPalette;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private Movie mMovie;
    private Realm realm;
    private boolean isTablet;
    private int mDarkColor;
    private int mLightColor;
    private LinearLayout mTrailerContainer;
    private LinearLayout mReviewContainer;

    public MovieDetailFragment() {
        // Required empty public constructor
    }

    public static MovieDetailFragment newInstance(Movie movie, boolean isTablet) {

        MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("selectedMovie", movie);
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

        mMovie = getArguments().getParcelable("selectedMovie");

        // Create a new instance of Realm.
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(getContext()).build();
        realm = Realm.getInstance(realmConfiguration);

        // Getting all values from selected movie.
        mMovieName = mMovie.getTitle();
        mMovieId = mMovie.getMovieId();
        mMoviePosterPath = mMovie.getPosterPath();
        mMovieBackdropPath = mMovie.getBackdropPath();
        mMovieRating = mMovie.getVoteAverage();
        mMovieSynopsis = mMovie.getOverview();
        String preFixedReleaseDate = mMovie.getReleaseDate();

        //Proper date
        mMovieReleaseDate = this.getString(R.string.release_date) + dateFormat(preFixedReleaseDate);

    }

    private void getTrailerList(int movieId) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<TrailersResponse> call = apiService.getMovieTrailers(movieId, SensitiveInfo.getMoviesApiKey());

        call.enqueue(new Callback<TrailersResponse>() {
            @Override
            public void onResponse(Call<TrailersResponse> call, Response<TrailersResponse> response) {

                List<Trailer> trailerList = response.body().getTrailers();

                if (trailerList.size() == 0) {

                    ConstraintLayout currentTrailerView = (ConstraintLayout) LayoutInflater.from(getContext())
                            .inflate(R.layout.movie_detail_trailer_view, mTrailerContainer, false);

                    TextView currentTrailerName = (TextView) currentTrailerView.findViewById(R.id.txt_trailer_element);

                    ImageView playImage = (ImageView) currentTrailerView.findViewById(R.id.img_trailer_play);
                    playImage.setVisibility(View.GONE);
                    currentTrailerName.setText("No trailers available.");
                    mTrailerContainer.addView(currentTrailerView);

                } else {

                    for (final Trailer currentTrailer : trailerList) {

                        ConstraintLayout currentTrailerView = (ConstraintLayout) LayoutInflater.from(getContext())
                                .inflate(R.layout.movie_detail_trailer_view, mTrailerContainer, false);

                        TextView currentTrailerName = (TextView) currentTrailerView.findViewById(R.id.txt_trailer_element);

                        currentTrailerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + currentTrailer.getKey()));
                                    startActivity(intent);
                                } catch (ActivityNotFoundException ex) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse("http://www.youtube.com/watch?v=" + currentTrailer.getKey()));
                                    startActivity(intent);
                                }
                            }
                        });

                        currentTrailerName.setText(currentTrailer.getName());
                        mTrailerContainer.addView(currentTrailerView);
                    }

                }

                Log.d(TAG, "Number of trailers received: " + trailerList.size());
            }

            @Override
            public void onFailure(Call<TrailersResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });

    }

    private void getReviewList(int movieId) {

        ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        Call<ReviewsResponse> call = apiService.getMovieReviews(movieId, SensitiveInfo.getMoviesApiKey());

        call.enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {

                List<Review> reviewList = response.body().getReviews();

                if (reviewList.size() == 0) {

                    ConstraintLayout currentReviewView = (ConstraintLayout) LayoutInflater.from(getContext())
                            .inflate(R.layout.movie_detail_review_view, mReviewContainer, false);

                    TextView currentReviewAuthor = (TextView) currentReviewView.findViewById(R.id.txt_review_author_element);

                    View contentDivider = currentReviewView.findViewById(R.id.content_divider);
                    contentDivider.setVisibility(View.GONE);

                    TextView currentReviewContent = (TextView) currentReviewView.findViewById(R.id.txt_review_content_element);
                    currentReviewContent.setVisibility(View.GONE);

                    currentReviewAuthor.setText("No reviews available.");
                    mReviewContainer.addView(currentReviewView);

                } else {

                    for (final Review currentReview : reviewList) {

                        ConstraintLayout currentReviewView = (ConstraintLayout) LayoutInflater.from(getContext())
                                .inflate(R.layout.movie_detail_review_view, mReviewContainer, false);

                        TextView currentReviewAuthor = (TextView) currentReviewView.findViewById(R.id.txt_review_author_element);
                        TextView currentReviewContent = (TextView) currentReviewView.findViewById(R.id.txt_review_content_element);

                        currentReviewView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW,
                                        Uri.parse(currentReview.getUrl()));
                                startActivity(intent);
                            }
                        });

                        currentReviewAuthor.setText(String.format("%s%s", getString(R.string.review_author), currentReview.getAuthor()));

                        currentReviewContent.setText(currentReview.getContent());
                        mReviewContainer.addView(currentReviewView);
                    }

                }

                Log.d(TAG, "Number of reviews received: " + reviewList.size());
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                // Log error here since request failed
                Log.e(TAG, t.toString());
            }
        });

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
        trailerHeader = (TextView) view.findViewById(R.id.txt_trailer_header);
        reviewHeader = (TextView) view.findViewById(R.id.txt_review_header);

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

                                movieRatingTextView.setBackgroundColor(mDarkColor);
                                movieRatingTextView.setShadowLayer(10, 0, 0, Color.BLACK);

                                movieTitleTextView.setBackgroundColor(mDarkColor);
                                movieTitleTextView.setShadowLayer(10, 0, 0, Color.BLACK);

                                movieReleaseDateTextView.setBackgroundColor(mLightColor);

                                int darkAlphaColor = ColorUtils.setAlphaComponent(mDarkColor, 128);
                                int alphaTextColor = ColorUtils.setAlphaComponent(Color.WHITE, 180);
                                movieSynopsisTextView.setBackgroundColor(darkAlphaColor);
                                movieSynopsisTextView.setTextColor(alphaTextColor);
                                movieSynopsisTextView.setShadowLayer(6, 0, 0, Color.BLACK);

                                trailerHeader.setBackgroundColor(mDarkColor);
                                reviewHeader.setBackgroundColor(mDarkColor);

                            }
                        })
        );

        mTrailerContainer = (LinearLayout) view.findViewById(R.id.trailer_list_container);
        getTrailerList(mMovieId);

        mReviewContainer = (LinearLayout) view.findViewById(R.id.review_list_container);
        getReviewList(mMovieId);

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

        //int currentMovieId = mMovie.getMovieId();

        RealmResults<Movie> favCheck = realm.where(Movie.class).equalTo("movieId", mMovieId).findAll();

        if (favCheck.size() > 0) {
            return true;
        } else {
            return false;
        }

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
                if (!isFavorite()) {

                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            realm.copyToRealm(mMovie);

                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {

                            Toast.makeText(getContext(), "Current movie added to Favorites!", Toast.LENGTH_SHORT).show();
                            mFavoriteTag.setVisibility(View.VISIBLE);
                            if (isTablet) {

                                mCallback.onDatabaseUpdate();

                            }
                            getActivity().invalidateOptionsMenu();

                        }
                    });

                    return true;

                } else {

                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {

                            Movie currentMovie = realm.where(Movie.class).equalTo("movieId", mMovieId).findFirst();
                            currentMovie.deleteFromRealm();

                        }
                    }, new Realm.Transaction.OnSuccess() {
                        @Override
                        public void onSuccess() {

                            Toast.makeText(getContext(), "Current movie was removed from Favorites!", Toast.LENGTH_SHORT).show();
                            mFavoriteTag.setVisibility(View.GONE);
                            if (isTablet) {

                                mCallback.onDatabaseUpdate();

                            }
                            getActivity().invalidateOptionsMenu();

                        }
                    });

                    return true;

                }
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    public interface OnDatabaseChangedListener {
        void onDatabaseUpdate();
    }

}
