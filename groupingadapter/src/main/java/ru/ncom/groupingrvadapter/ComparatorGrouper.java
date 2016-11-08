package ru.ncom.groupingrvadapter;

import java.util.Comparator;

/**
 *
 * @param <T>
 */
public abstract class ComparatorGrouper<T>  implements Comparator<T> {
    /**
     * @param m
     * @return
     */
    public abstract String getGroupTitle(T m);

    public abstract String getSortKey(T m);

    @Override
    public int compare(T lhs, T rhs) {
        if (lhs.equals(rhs))
            return 0;
        else return getSortKey(lhs).compareTo(getSortKey(rhs));
    }
}
