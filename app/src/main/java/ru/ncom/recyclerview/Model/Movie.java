package ru.ncom.recyclerview.model;

import java.util.Comparator;

/**
 * Created by Lincoln on 15/01/16.
 */
public class Movie implements Titled {
    private String title, genre, year;

    public Movie() {
    }

    public Movie(String title, String genre, String year) {
        this.title = title;
        this.genre = genre;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

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


    public static class ComparatorBy implements Comparator<Movie>{

        /**
         * Field ComparatorBy can accept
         */
        public enum CompareBy {
            TITLE, GENRE, YEAR
        }

        CompareBy mCby;

        /**
         * Create a Comparator for the field.
         * Additionaly ComparatorBy#getGroup returns grouping header
         * @param cby
         */
        public ComparatorBy(CompareBy cby){
            this.mCby = cby;
        }

        @Override
        public int compare(Movie lhs, Movie rhs) {
            switch (mCby) {
                case TITLE:
                    return lhs.getTitle().compareTo(rhs.getTitle());
                case GENRE:
                    return lhs.getGenre().compareTo(rhs.getGenre());
                case YEAR:
                    return lhs.getYear().compareTo(rhs.getYear());
                default:
                    return 0;
            }
        }

        public String getGroup (Movie m){
            switch (mCby) {
                case TITLE:
                    return m.getTitle().substring(0,1);
                case GENRE:
                    return m.getGenre().substring(0,1);
                case YEAR:
                    return m.getYear().substring(0,3)+"0s";
                default:
                    return "";
            }
        }

    }
}
