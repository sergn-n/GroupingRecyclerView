package ru.ncom.recyclerview.adapter;

import java.util.ArrayList;
import java.util.List;

import ru.ncom.recyclerview.model.Movie;

/**
 * Created by gerg on 08.09.2016.
 */
public class Header implements Titled {

    private String title = null;
    private boolean isCollapsed = false;
    private final List<Movie> movieList = new ArrayList<>();

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
        this.isCollapsed = isCollapsed;
    }

    public List<Movie> getChildItemList() {
        return movieList;
    }
}