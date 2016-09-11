package ru.ncom.recyclerview.adapter;

import java.util.Comparator;

/**
 *
 * @param <T>
 */
public interface ComparatorGrouper<T>  extends Comparator<T> {
    /**
     * @param m
     * @return
     */
    public String getGroupTitle(T m) ;
}
