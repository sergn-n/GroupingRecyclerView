package ru.ncom.recyclerview.adapter;

import java.util.ArrayList;
import java.util.List;

import ru.ncom.recyclerview.model.Movie;
import ru.ncom.recyclerview.model.Titled;

/**
 * Created by gerg on 08.09.2016.
 */
public class Header implements Titled {

    private String title = null;
    private boolean isCollapsed = false;
    private final List<Movie> movieList = new ArrayList<>();

    public Header() {
    }

    public Header(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String name) {
        this.title = name;
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean isCollapsed){
        isCollapsed = true;
    }

    public List<Movie> getMovieList() {
        return movieList;
    }
}