package ru.ncom.recyclerview.Model;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by gerg on 05.09.2016.
 */

public class MovieDb
{
    // Data
    private List<Movie> movieList = new ArrayList<>();
    // Represantation
    private final List<Titled> itemsList = new ArrayList<>();

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
        itemsList.add((Titled)movie);

        // initially it's just source data
        for (int i = 0; i < movieList.size(); i++){
            itemsList.add(movieList.get(i));
        }

    }

    public List<Titled> getMovieList() { return itemsList;}

    public Titled getAt(int position) { return itemsList.get(position);}

    public void orderBy(Movie.ComparatorBy.CompareBy sortField) {
        Movie.ComparatorBy mcb =  new Movie.ComparatorBy(sortField);
        Collections.sort(movieList,mcb);
        String curTitle = null;
        itemsList.clear();
        for (int i = 0; i < movieList.size(); i++){
            Movie m = movieList.get(i);
            String newTitle = mcb.getGroup(m);
            if (!newTitle.equals(curTitle)){
                itemsList.add( new Header(newTitle));
                curTitle= newTitle;
            }
            itemsList.add(m);
        }
    }


    public void orderByAsync (Movie.ComparatorBy.CompareBy sortField, AsyncDbSort.ProgressListener progressView) {
        (new AsyncDbSort(this, progressView)).execute(sortField);

    }


    public static class AsyncDbSort extends AsyncTask<Movie.ComparatorBy.CompareBy,String,String> {

        public interface ProgressListener{
            void onStart(String msg);
            void onProgess(String msg);
            void onDone(String msg);
        }

        MovieDb db;
        ProgressListener progressListener;

        public AsyncDbSort(MovieDb db, ProgressListener progressListener ){
            this.db = db;
            this.progressListener = progressListener;
        }

        @Override
        protected String doInBackground(Movie.ComparatorBy.CompareBy... params) {
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {

            }
            db.orderBy(params[0]);
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
