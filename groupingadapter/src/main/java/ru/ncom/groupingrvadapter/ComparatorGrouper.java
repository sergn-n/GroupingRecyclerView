package ru.ncom.groupingrvadapter;

import java.util.Comparator;

/**
 *
 * @param <T>
 */
public abstract class ComparatorGrouper<T>  implements Comparator<T> {

    public abstract String getSortKey(T m);

    /**
     * Default Group Title is Sort Key.
     * @param m
     * @return
     */
    public String getGroupTitle(T m) {
        return getSortKey(m);
    };

    @Override
    public int compare(T lhs, T rhs) {
        if (lhs.equals(rhs))
            return 0;
        else return getSortKey(lhs).compareTo(getSortKey(rhs));
    }
}
