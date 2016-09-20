package ru.ncom.groupingrecyclerview.model;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.ncom.groupingrvadapter.ComparatorGrouper;
import ru.ncom.groupingrvadapter.Db;


/**
 * Created by gerg on 05.09.2016.
 */

public class MovieDb implements Db<Movie>
{
    private static String FILENAME ="Movies.ser";
    private static String TAG ="MovieDb";

    private Context mContext;
    // Data
    private ArrayList<Movie> movieList;

    public MovieDb(Context ctx) {
        mContext = ctx;
        try {
            read();
            Log.d(TAG, "Read from "+ FILENAME);
        }
        catch (Exception e) {
            // no file to read, generate data and save.
            generate();
            try {
                save();
                Log.d(TAG, "Generated and saved to "+ FILENAME);
            }
            catch (Exception ex)
            {
                Log.e(TAG,"Failed to save to "+ FILENAME, ex);
            }
        }
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

    }

    private void read() throws IOException, ClassNotFoundException {
        FileInputStream fin = mContext.openFileInput(FILENAME);
        ObjectInputStream ois = new ObjectInputStream(fin);
        movieList = (ArrayList<Movie>)ois.readObject();
        //Movie m = (Movie)ois.readObject();
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
     * Repeat current list 2**n times (n>0)
     * @param n
     */
    public void cloneData(int n) {
        for (int i=0; i<n; i++)
            movieList.addAll(movieList);
    }

    @Override
    public List<Movie> getDataList() { return movieList;}

    @Override
    public List<Movie> orderBy(Comparator<Movie> mcb) {
        Collections.sort(movieList,mcb);
        return movieList;
    }

    @Override
    public ComparatorGrouper<Movie> getComparatorGrouper(String orderByFieldName) {
        return Movie.getComparatorGrouper(orderByFieldName);
    }
}
