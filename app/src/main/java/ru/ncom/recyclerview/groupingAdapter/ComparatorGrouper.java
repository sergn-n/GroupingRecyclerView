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
    String getGroupTitle(T m) ;
}
