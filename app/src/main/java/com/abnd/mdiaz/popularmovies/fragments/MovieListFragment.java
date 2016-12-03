package com.abnd.mdiaz.popularmovies.fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abnd.mdiaz.popularmovies.R;
import com.abnd.mdiaz.popularmovies.model.Movie;
import com.abnd.mdiaz.popularmovies.model.MoviesResponse;
import com.abnd.mdiaz.popularmovies.rest.ApiClient;
import com.abnd.mdiaz.popularmovies.rest.ApiInterface;
import com.abnd.mdiaz.popularmovies.utils.MarginDecoration;
import com.abnd.mdiaz.popularmovies.utils.SensitiveInfo;
import com.abnd.mdiaz.popularmovies.views.adapters.MovieAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieListFragment extends Fragment {

    private static final String TAG = MovieListFragment.class.getSimpleName();
    private static final String TOP_MOVIES_TAG = "Top";
    private static final String POP_MOVIES_TAG = "Pop";
    private static final String FAV_MOVIES_TAG = "Fav";
    private static final String INTER_FRAGMENT_TAG = "InterFragment";

    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private ProgressBar mProgressBar;
    private String mListType;
    private ActionBar mActionBar;
    private TextView mEmptyFavsMessage;

    private RealmConfiguration realmConfiguration;

    private boolean validConnection;

    private Realm realm;

    public MovieListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("ListType", mListType);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (validConnection) {
            getMovieList(mListType);
        }

        Log.d(TAG, "onResume: Movie List Fragment has resumed.");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        validConnection = checkConnectivity();
    }

    private boolean checkConnectivity() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (validConnection) {

            realmConfiguration = new RealmConfiguration.Builder(getContext()).build();

            // Create a new empty instance of Realm
            realm = Realm.getInstance(realmConfiguration);

            if (savedInstanceState != null) {
                mListType = savedInstanceState.getString("ListType", POP_MOVIES_TAG);
                Log.d(TAG, "List Type: " + mListType);
            } else {
                mListType = POP_MOVIES_TAG;
            }

            AppCompatActivity mActivity = (AppCompatActivity) getActivity();
            mActionBar = mActivity.getSupportActionBar();

            setHasOptionsMenu(true);

            mAdapter = new MovieAdapter(getContext(), new ArrayList<Movie>());

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Movie List Fragment is on Pause.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);

        if (!validConnection) {

            TextView noInternetMessage = (TextView) view.findViewById(R.id.txt_no_internet);
            noInternetMessage.setVisibility(View.VISIBLE);

        } else {

            mProgressBar = (ProgressBar) view.findViewById(R.id.movie_list_progress_bar);

            mEmptyFavsMessage = (TextView) view.findViewById(R.id.txt_no_favs);

            mRecyclerView = (RecyclerView) view.findViewById(R.id.main_recycler_view);

            MarginDecoration marginDecoration = new MarginDecoration(getContext());

            mRecyclerView.addItemDecoration(marginDecoration);

            mProgressBar.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);

            getMovieList(mListType);

            mRecyclerView.setAdapter(mAdapter);

        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.movie_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_top_movies:
                if (Objects.equals(mListType, TOP_MOVIES_TAG)) {
                    Toast.makeText(getContext(), "You are looking at the Top Movies list.", Toast.LENGTH_SHORT).show();
                    break;
                }
                mListType = TOP_MOVIES_TAG;
                getMovieList(mListType);
                break;
            case R.id.menu_pop_movies:
                if (Objects.equals(mListType, POP_MOVIES_TAG)) {
                    Toast.makeText(getContext(), "You are looking at the Popular Movies list.", Toast.LENGTH_SHORT).show();
                    break;
                }
                mListType = POP_MOVIES_TAG;
                getMovieList(mListType);
                break;
            case R.id.menu_fav_movies:
                if (Objects.equals(mListType, FAV_MOVIES_TAG)) {
                    Toast.makeText(getContext(), "You are looking at the Favorite Movies list.", Toast.LENGTH_SHORT).show();
                    break;
                }
                mListType = FAV_MOVIES_TAG;
                getMovieList(mListType);
                break;
            case R.id.menu_add_favs:
                if (!Objects.equals(mListType, FAV_MOVIES_TAG)) {

                    getMovieList(mListType);

                }
                Log.d(TAG, "onOptionsItemSelected: MADE IT!");
                break;
        }

        //getMovieList(mListType);
        return super.onOptionsItemSelected(item);

    }

    private void loadAdapter(List<Movie> baseMovieList) {

        mAdapter.clearData();
        mAdapter.setMovieList(baseMovieList);

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

        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

    }

    public void getMovieList(String listType) {

        if (Objects.equals(listType, INTER_FRAGMENT_TAG)) {

            listType = mListType;

        }

        if (Objects.equals(listType, FAV_MOVIES_TAG)) {

            RealmResults<Movie> favMoviesList = realm.where(Movie.class).findAll();

            Log.d(TAG, "getMovieList: favMoviesList Size: " + favMoviesList.size());

            favMoviesList = favMoviesList.sort("title");

            /*
            If I use the Realm list straight up, there is a lot of noise in the UX, especially
            when trying to re-add a just deleted movie to the Fav list.
            */
            List<Movie> regenFavMoviesList = new ArrayList<>();

            for (Movie currentRealmMovie :
                    favMoviesList) {
                regenFavMoviesList.add(new Movie(currentRealmMovie));
            }


            if (regenFavMoviesList.size() == 0) {
                mEmptyFavsMessage.setVisibility(View.VISIBLE);
            } else {
                mEmptyFavsMessage.setVisibility(View.GONE);
            }

            loadAdapter(regenFavMoviesList);

            //loadAdapter(favMoviesList);

        } else {

            mEmptyFavsMessage.setVisibility(View.GONE);

            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            Call<MoviesResponse> call;

            switch (listType) {
                case POP_MOVIES_TAG:
                    call = apiService.getPopularMovies(SensitiveInfo.getMoviesApiKey());
                    break;
                case TOP_MOVIES_TAG:
                    call = apiService.getTopRatedMovies(SensitiveInfo.getMoviesApiKey());
                    break;
                default:
                    call = apiService.getPopularMovies(SensitiveInfo.getMoviesApiKey());
                    break;
            }

            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movieList = response.body().getResults();
                    loadAdapter(movieList);
                    //For some reason this method gets executed even when the activity is resumed...
                    Log.d(TAG, "Number of movies received: " + movieList.size());
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    // Log error here since request failed
                    Log.e(TAG, t.toString());
                }
            });

        }
    }

}
