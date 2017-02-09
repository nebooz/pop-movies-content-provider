package com.abnd.mdiaz.popularmovies.fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import com.abnd.mdiaz.popularmovies.rest.QueryUtils;
import com.abnd.mdiaz.popularmovies.utils.MarginDecoration;
import com.abnd.mdiaz.popularmovies.views.adapters.MovieAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.abnd.mdiaz.popularmovies.rest.QueryUtils.FAV_MOVIES_TAG;
import static com.abnd.mdiaz.popularmovies.rest.QueryUtils.POP_MOVIES_TAG;
import static com.abnd.mdiaz.popularmovies.rest.QueryUtils.TOP_MOVIES_TAG;

public class MovieListFragment extends Fragment {

    private static final String TAG = MovieListFragment.class.getSimpleName();

    private static final String INTER_FRAGMENT_TAG = "InterFragment";

    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private ProgressBar mProgressBar;
    private String mListType;
    private ActionBar mActionBar;
    private TextView mEmptyFavsMessage;

    private boolean validConnection;

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

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new PopulateDatabase().execute(QueryUtils.TOP_MOVIES_URL, QueryUtils.POP_MOVIES_URL);

        if (validConnection) {

            if (savedInstanceState != null) {
                mListType = savedInstanceState.getString("ListType", QueryUtils.POP_MOVIES_TAG);
                Log.d(TAG, "List Type: " + mListType);
            } else {
                mListType = QueryUtils.POP_MOVIES_TAG;
            }

            AppCompatActivity mActivity = (AppCompatActivity) getActivity();
            mActionBar = mActivity.getSupportActionBar();

            setHasOptionsMenu(true);

            mAdapter = new MovieAdapter(getContext(), new ArrayList<Movie>(), mListType);

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

        loadAdapter(QueryUtils.queryAllMovies(getContext(), listType));

    }

    private class PopulateDatabase extends AsyncTask<String, Integer, Boolean> {
        protected Boolean doInBackground(String... urls) {

            int count = urls.length;
            String movieTable;

            for (int i = 0; i < count; i++) {

                if (urls[i].equals(QueryUtils.TOP_MOVIES_URL)) {
                    movieTable = QueryUtils.TOP_MOVIES_TAG;
                } else {
                    movieTable = QueryUtils.POP_MOVIES_TAG;
                }

                QueryUtils.fetchMovies(getContext(), urls[i], movieTable);

                publishProgress((int) ((i / (float) count) * 100));

            }

            return true;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

            getMovieList(mListType);

            if (result) {
                Toast.makeText(getContext(), "Movies Acquired Successfully", Toast.LENGTH_SHORT).show();

            }

        }

    }

}
