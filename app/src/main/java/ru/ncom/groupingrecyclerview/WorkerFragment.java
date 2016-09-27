package ru.ncom.groupingrecyclerview;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import ru.ncom.groupingrecyclerview.adapter.MoviesAdapter;
import ru.ncom.groupingrecyclerview.model.MovieDb;

/**
 * Retained fragment is a tool to persist objects (MovieDb namely) over configuration change,
 * not for long running processes which must survive activity exit.
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


    private MainActivity currentMainActivity;
    public MainActivity getCurrentMainActivity() {
        return currentMainActivity;
    }
    public void setCurrentMainActivity(MainActivity currentMainActivity) {
        this.currentMainActivity = currentMainActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this instance so it isn't destroyed when MainActivity and
        // MainFragment change configuration.
        setRetainInstance(true);
        Context appCtx = getActivity().getApplication().getApplicationContext();
        // MovieDb needs context to get local file.
        mMovieDb = new MovieDb(appCtx);
        // MoviesAdapter needs context just to show Toast.
        mMoviesAdapter = new MoviesAdapter(mMovieDb, appCtx);

    }

    //TODO convert it into dialog to show progress?
}
