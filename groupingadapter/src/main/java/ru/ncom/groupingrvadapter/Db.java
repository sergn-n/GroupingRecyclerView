package ru.ncom.groupingrvadapter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public interface Db<T> {
    List<T> getDataList();

    List<T> orderBy(Comparator<T> cmp);

    ComparatorGrouper<T> getComparatorGrouper(String orderByFieldName);

    boolean delete(T item) throws IOException;

    void insert(T item, String orderByFieldName);
}