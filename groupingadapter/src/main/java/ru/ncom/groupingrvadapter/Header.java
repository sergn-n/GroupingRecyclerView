package ru.ncom.groupingrvadapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Default header
 */
public class Header<T> extends TitledSelectableItem {

    private boolean isCollapsed = false;

    // Sorted list of items under the Header
    private List<T> children = new ArrayList<>();
    List<T> getChildren() {
        return children;
    }

    public Header(String title) {
        super(title);
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean isCollapsed){
        this.isCollapsed = isCollapsed;
    }

    // ** children methods  **

    public int size(){
        return children.size();
    }

    public int indexOf(T item) {
        return children.indexOf(item);
    }

    public int add(T item, Comparator cg) {
        if (children.size() == 0) {
            children.add(item);
            return 0;
        }
        int pos = Arrays.binarySearch(children.toArray(), item, cg);
        if (pos < 0)
            pos = -1 - pos;
        children.add(pos, item);
        return pos;
    }

    public T remove(int pos) {
       return children.remove(pos);
    }

    /**
     *
     * @param items
     * @param start including
     * @param end  excluding
     * @param cg
     */
    public void merge(T[] items, int start, int end, Comparator<T> cg) {
        if (start > end)
            throw new IllegalArgumentException(" start  > end.");
        T[] newc = (T[])new Object[children.size() + end - start];
        T[] old  = (T[])children.toArray();
        int oldlen = old.length;
        int i = 0;
        int iold = 0;
        while (start < end && iold < oldlen) {
            newc[i++] = (cg.compare(old[iold], items[start]) <0)
                    ? old[iold++]
                    : items[start++];
        }
        while (iold < oldlen)
            newc[i++] = old[iold++];
        while (start < end)
            newc[i++] = items[start++];
        children = new ArrayList<T>(Arrays.asList(newc));
    }

}
