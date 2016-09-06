package ru.ncom.recyclerview;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gerg on 05.09.2016.
 */

public class MovieDb
{
    private List<Movie> movieList = new ArrayList<> ();

    public MovieDb() {

        Movie movie = new Movie("Mad Max: Fury Road", "Action & Adventure", "2015");
        movieList.add(movie);

        movie = new Movie("Inside Out", "Animation, Kids & Family", "2015");
        movieList.add(movie);

        movie = new Movie("Star Wars: Episode VII - The Force Awakens", "Action", "2015");
        movieList.add(movie);

        movie = new Movie("Shaun the Sheep", "Animation", "2015");
        movieList.add(movie);

        movie = new Movie("The Martian", "Science Fiction & Fantasy", "2015");
        movieList.add(movie);

        movie = new Movie("Mission: Impossible Rogue Nation", "Action", "2015");
        movieList.add(movie);

        movie = new Movie("Up", "Animation", "2009");
        movieList.add(movie);

        movie = new Movie("Star Trek", "Science Fiction", "2009");
        movieList.add(movie);

        movie = new Movie("The LEGO Movie", "Animation", "2014");
        movieList.add(movie);

        movie = new Movie("Iron Man", "Action & Adventure", "2008");
        movieList.add(movie);

        movie = new Movie("Aliens", "Science Fiction", "1986");
        movieList.add(movie);

        movie = new Movie("Chicken Run", "Animation", "2000");
        movieList.add(movie);

        movie = new Movie("Back to the Future", "Science Fiction", "1985");
        movieList.add(movie);

        movie = new Movie("Raiders of the Lost Ark", "Action & Adventure", "1981");
        movieList.add(movie);

        movie = new Movie("Goldfinger", "Action & Adventure", "1965");
        movieList.add(movie);

        movie = new Movie("Guardians of the Galaxy", "Science Fiction & Fantasy", "2014");
        movieList.add(movie);
    }

    public List<Movie> getMovieList() { return movieList;}

    public Movie getAt(int position) {return movieList.get(position);}

    public void orderBy(Movie.ComparatorBy.CompareBy sortField) {
        Collections.sort(movieList, new Movie.ComparatorBy(sortField));
    }


    public void orderByAsync (Movie.ComparatorBy.CompareBy sortField, AsyncDbSort.ProgressListener progressView) {
        (new AsyncDbSort(movieList, progressView)).execute(sortField);

    }


    static class AsyncDbSort extends AsyncTask<Movie.ComparatorBy.CompareBy,String,String> {

        public interface ProgressListener{
            void onStart(String msg);
            void onProgess(String msg);
            void onDone(String msg);
        }

        List<Movie> movieList2;
        ProgressListener progressListener;

        public AsyncDbSort(List<Movie> ml, ProgressListener progressListener ){
            this.movieList2 = ml;
            this.progressListener = progressListener;
        }

        @Override
        protected String doInBackground(Movie.ComparatorBy.CompareBy... params) {
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {

            }
            Collections.sort(movieList2, new Movie.ComparatorBy(params[0]));
            publishProgress ("Sorted...");
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {

            }
            return "Done.";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressListener.onStart("Starting sort...");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressListener.onProgess(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressListener.onDone(s);
        }

    }
}
