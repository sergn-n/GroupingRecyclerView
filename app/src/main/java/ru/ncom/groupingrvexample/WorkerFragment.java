package ru.ncom.groupingrvexample;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import ru.ncom.groupingrvexample.adapter.MoviesAdapter;
import ru.ncom.groupingrvexample.model.MovieDb;

/**
 * Retained fragment is a tool to persist objects which are costly to recreate over configuration change.
 * Not suitable for long running processes which must survive activity exit.
 */
public class WorkerFragment extends Fragment {
    public static String TAG = "WORKERFRAGMENT";

    private MovieDb mMovieDb;
    public  MovieDb getMovieDb(){
        return mMovieDb;
    }

    private MoviesAdapter mMoviesAdapter;
    public MoviesAdapter getMoviesAdapter() {
        return mMoviesAdapter;
    }

    private BaseActivity currentBaseActivity;
    public BaseActivity getCurrentBaseActivity() {
        return currentBaseActivity;
    }
    public void setCurrentBaseActivity(BaseActivity currentBaseActivity) {
        this.currentBaseActivity = currentBaseActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this instance so it isn't destroyed when BaseActivity and
        // MainFragment change configuration.
        setRetainInstance(true);
        Context appCtx = getActivity().getApplicationContext();
        // MovieDb needs context to get local file.
        mMovieDb = new MovieDb(appCtx);
        mMoviesAdapter = new MoviesAdapter(mMovieDb);
    }
}
