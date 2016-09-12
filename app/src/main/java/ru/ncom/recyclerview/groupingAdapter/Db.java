package ru.ncom.recyclerview.groupingAdapter;

import java.util.Comparator;
import java.util.List;

public interface Db<T> {
    List<T> getDataList();
    List<T> orderBy(Comparator<T> cmp);
    ComparatorGrouper<T> getComparatorGrouper(String orderByFieldName);
}