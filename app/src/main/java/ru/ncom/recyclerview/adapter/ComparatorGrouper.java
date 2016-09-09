package ru.ncom.recyclerview.adapter;

import java.util.Comparator;

/**
 * Created by gerg on 09.09.2016.
 */

public interface ComparatorGrouper<T>  extends Comparator<T> {
    /**
     * @param m
     * @return grouping header of the object m
     */
    public String getGroup (T m) ;

}
