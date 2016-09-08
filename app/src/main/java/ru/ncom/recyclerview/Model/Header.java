package ru.ncom.recyclerview.Model;

/**
 * Created by gerg on 08.09.2016.
 */
public class Header implements Titled {
    private String title;

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

}