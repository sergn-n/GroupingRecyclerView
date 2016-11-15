package ru.ncom.groupingrvexample.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.ncom.groupingrvadapter.ComparatorGrouper;
import ru.ncom.groupingrvadapter.GroupedList;
import ru.ncom.groupingrvadapter.Header;

/**
 * Created by gerg on 05.09.2016.
 */

public class MovieDb
{
    private static String FILENAME ="Movies.ser";
    private static String TAG ="MovieDb";

    private Context mContext;

    // Original data
    private ArrayList<Movie> movieList = null;

    public MovieDb(Context ctx) {
        mContext = ctx;
    }

    private void generate() {
        movieList = new ArrayList<>();

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

        try {
            save();
            Log.d(TAG, "Generated and saved to " + FILENAME);
        } catch (Exception ex) {
            Log.e(TAG, "Failed to save to " + FILENAME, ex);
        }
    }

    private void read() throws IOException, ClassNotFoundException {
        FileInputStream fin = mContext.openFileInput(FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fin);
        movieList = (ArrayList<Movie>)ois.readObject();
        fin.close();
    }

    public void save() throws IOException{
        FileOutputStream fout = mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(movieList);
        //oos.writeObject(movieList.get(0));
        fout.close();
    }

    /**
     * Repeat current list n times (n>0)
     * @param n
     */
    public List<Movie> cloneData(int n) throws IOException {
        getDataList();
        int size = movieList.size();
        List<Movie> newItems = new ArrayList<Movie>(n * size);
        for (int i=0; i<n; i++) {
            for (int j = 0; j < size; j++)
                newItems.add(movieList.get(j).clone());
        }
        if (n>0) {
            movieList.addAll(newItems);
            save();
        }
        return newItems;
    }

    public List<Movie> getDataList() {
        if (movieList==null || movieList.size() == 0) {
            try {
                read();
                Log.d(TAG, "Read from " + FILENAME);
            } catch (Exception e) {
                // no file to read, generate data and save.
                generate();
            }
            if (movieList.size() == 0) // empty file
                generate();
        }
        return movieList;
    }


    // #region Data Modification

    public void add (Movie m)throws IOException{
        movieList.add(m);
        save();
    }


    public boolean delete(Movie m) throws IOException{
        if (movieList.remove(m)){
            save();
            return true;
        }
        return false;
    }

    public Movie delete(int pos) throws IOException{
        Movie m = movieList.remove(pos);
        if (m != null){
            save();
        }
        return m;
    }


    // Simulate asynch operation

    public static class AsyncDbSort extends AsyncTask<String,String,String> {

        public interface ProgressListener{
            /** After config change old instance must know current instance
             * @return
             */
            ProgressListener getCurrentInstance();
            void onAsyncSortStart(String msg);
            void onAsyncSortProgress(String msg);

            /**
             *
             * @param glm sorted list
             */
            void onAsyncSortDone(GroupedList<Movie> glm);
        }

        GroupedList<Movie> mGlm;
        ProgressListener progressListener;

        public AsyncDbSort(GroupedList<Movie> glm, ProgressListener progressListener ){
            this.mGlm = glm;
            this.progressListener = progressListener;
        }

        @Override
        protected String doInBackground(String... params) {
            // If config change occurs here, it' OK, persistent adapter version
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {
                //intentionally empty
            }
            //
            mGlm.doSort(params[0]);

            publishProgress ("Sorted, notifying...");
            try {
                Thread.sleep(7000);
            }
            catch (InterruptedException e) {
                //intentionally empty
            }
            return "Done.";
        }

        // Deliver events to current instance of activity
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressListener.getCurrentInstance().onAsyncSortStart("Gonna sort it in a while...\n"
                    +"OK to change config here.");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // old instace really will do too, as listener only  shows a Toast
            super.onProgressUpdate(values);
            progressListener.getCurrentInstance().onAsyncSortProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressListener.getCurrentInstance().onAsyncSortDone(mGlm);
        }

    }

}
