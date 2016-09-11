package ru.ncom.recyclerview.model;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ncom.recyclerview.adapter.ComparatorGrouper;
import ru.ncom.recyclerview.adapter.Titled;

/**
 * Created by Lincoln on 15/01/16.
 */
public class Movie implements Titled {

    private String title, genre, year;

    private static final String FIELD_TITLE = "TITLE";
    private static final String FIELD_GENRE = "GENRE";
    private static final String FIELD_YEAR = "YEAR";

    private static final List<String> orderByFields = Arrays.asList(new String[] {FIELD_TITLE, FIELD_GENRE, FIELD_YEAR});

    // Must have
    /**
     * @return Fields {@link #getComparatorGrouper(String)} can accept
     */
    public static List<String> getOrderByFields() {
        return orderByFields;
    }

    // Must have
    public static ComparatorGrouper<Movie> getComparatorGrouper(String cby){
        return new MovieComparatorGrouper(cby);
    }

    public Movie(String title, String genre, String year) {
        this.title = title;
        this.genre = genre;
        this.year = year;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String name) {
        this.title = name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    private static class MovieComparatorGrouper implements ComparatorGrouper<Movie> {

        final String mCby;

        /**
         * Create a ComparatorGrouper for the Movie field.
         * @param cby
         */
        public MovieComparatorGrouper(String cby){
            if (!Movie.getOrderByFields().contains(cby))
                throw new InvalidParameterException(cby + " is not a valid order by field.");
            this.mCby = cby;
        }
        @Override
        public int compare(Movie lhs, Movie rhs) {
            switch (mCby) {
                case FIELD_TITLE:
                    return lhs.getTitle().compareTo(rhs.getTitle());
                case FIELD_GENRE:
                    return lhs.getGenre().compareTo(rhs.getGenre());
                case FIELD_YEAR:
                    return lhs.getYear().compareTo(rhs.getYear());
                default:
                    return 0;
            }
        }

        @Override
        public String getGroupTitle(Movie m){
            switch (mCby) {
                case FIELD_TITLE:
                    return m.getTitle().substring(0,1);
                case FIELD_GENRE:
                    return m.getGenre().substring(0,1);
                case FIELD_YEAR:
                    return m.getYear().substring(0,3)+"0s";
                default:
                    return "";
            }
        }
    }
}
