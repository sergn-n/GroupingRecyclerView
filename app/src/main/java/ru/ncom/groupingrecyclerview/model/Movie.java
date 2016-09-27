package ru.ncom.groupingrecyclerview.model;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

import ru.ncom.groupingrvadapter.ComparatorGrouper;
import ru.ncom.groupingrvadapter.TitledSelectableItem;

public class Movie extends TitledSelectableItem implements Serializable {

    private String genre, year;

    public Movie(String title, String genre, String year) {
        super(title);
        this.genre = genre;
        this.year = year;
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

    // #Region ComparatorGrouper support

    private static final String FIELD_TITLE = "TITLE";
    private static final String FIELD_GENRE = "GENRE";
    private static final String FIELD_YEAR = "YEAR";

    private static final List<String> orderByFields = Arrays.asList(new String[] {FIELD_TITLE, FIELD_GENRE, FIELD_YEAR});

    /**
     * @return Fields {@link #getComparatorGrouper(String)} can accept
     */
    public static List<String> getOrderByFields() {
        return orderByFields;
    }

    public static ComparatorGrouper<Movie> getComparatorGrouper(String orderByField) {
        switch (orderByField) {
            case FIELD_TITLE:
                return new MovieComparatorGrouperTitle();
            case FIELD_GENRE:
                return new MovieComparatorGrouperGenre();
            case FIELD_YEAR:
                return new MovieComparatorGrouperYear();
            default:
                throw new InvalidParameterException(orderByField + " is not a valid orderBy field.");
        }
    }

    /**
     * Groups movies by the first letter of Title
     */
    private static class MovieComparatorGrouperTitle implements ComparatorGrouper<Movie> {

        @Override
        public int compare(Movie lhs, Movie rhs) {
            return lhs.getTitle().compareTo(rhs.getTitle());
        }

        @Override
        public String getGroupTitle(Movie m) {
            return m.getTitle().substring(0,1);
        }
    }

    /**
     * Groups movies by the Genre
     */
    private static class MovieComparatorGrouperGenre implements ComparatorGrouper<Movie> {

        @Override
        public int compare(Movie lhs, Movie rhs) {
            return lhs.getGenre().compareTo(rhs.getGenre());
        }

        @Override
        public String getGroupTitle(Movie m) {
            return m.getGenre();
        }
    }

    /**
     * Groups movies by the decade based on Year
     */
    private static class MovieComparatorGrouperYear implements ComparatorGrouper<Movie> {

        @Override
        public int compare(Movie lhs, Movie rhs) {
            return lhs.getYear().compareTo(rhs.getYear());
        }

        public String getGroupTitle(Movie m) {
            return m.getYear().substring(0, 3) + "0s";
        }
    }

    public Movie clone(){
        //Titled is not clonable, no super()
        return new Movie(getTitle(),genre, year);
    }
}
