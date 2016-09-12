package ru.ncom.recyclerview.groupingAdapter;

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
