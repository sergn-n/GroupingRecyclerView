package ru.ncom.groupingrvadapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Serg on 27.10.2016.
 * Sorted grouped list.
 * Keeps track of item's order and grouping.
 * Notifies adapter on:
 *  - group added, deleted
 *  - group items added, deleted, changed
 *  - non-grouped events
 *  - dataset changed if more then one group is changed or ordering/grouping field is changed.
 */

public class GroupedList<T extends Titled> {
    private final Class<T> mClass;
    private Callback mCallback;
    // current sort field
    private String mSortFieldName = null;

    private final List<T> mItemsList = new ArrayList<>();
    private Map<T,Header<T>> mItems2Headers = new HashMap<>();
    private final List<Header> mHeaders = new ArrayList<>();


    public GroupedList(Class<T> clazz, Callback<T> cb) {
        this.mClass = clazz;
        this.mCallback = cb;
    }

    /**
     *
     */
    private void add(T item) {
        //TODO
        throw new UnsupportedOperationException("Not implemented yet.");

    }

    public void addAll(List<T> items) {
        if (items.size() == 0) {
            return;
        }
        if (mSortFieldName == null) {
            mItemsList.addAll(items);
            return;
        }
        GroupedList<T> newItems = new GroupedList<T>(mClass,null);
        newItems.addAll(items);
        newItems.doSort(mCallback.getComparatorGrouper(mSortFieldName));
        merge(newItems);
    }

    private void merge( GroupedList<T> newItems) {
        //TODO
        throw new UnsupportedOperationException("Not implemented yet.");
    }
    /**
     * Adds the given items to the list. Does not modify the input.
     */
    public void addAll(T... items) {
        //TODO
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean remove(T item){

        int tpos = mItemsList.indexOf(item);
        if ( tpos <0 ) {
            return false;
        }
        // Remove from main
        mItemsList.remove(tpos);

        Header<T> h = mItems2Headers.get(item);
        if (h != null){ // sorted
            mItems2Headers.remove(item);
            if (h.getChildItemList().size() == 1) {
                // the only item in group, remove the entire header
                mHeaders.remove(h);
                mCallback.onHeaderRemoved(h);
            } else {
                tpos = h.getChildItemList().indexOf(item);
                h.getChildItemList().remove(tpos);
                mCallback.onGroupedItemRemoved(h, tpos);
            }
        }
        else {
            mCallback.onUngroupedItemRemoved(tpos);
        }
        return true;
    }

    /**
     * Sorts data by the specified field and creates ordered headers.
     */
    public void sort(String sortField) {
        mSortFieldName = sortField;
        mItems2Headers.clear();
        mHeaders.clear();
        if (mItemsList.size() == 0)
            return;
        ComparatorGrouper cg = mCallback.getComparatorGrouper(sortField);
        doSort(cg);

        mCallback.onDataSetChanged(mHeaders);
    }

    protected void doSort(ComparatorGrouper cg) {

        if (mItemsList.size() > 1)
            Collections.sort(mItemsList, cg);

        Header<T> h = new Header<T>(cg.getGroupTitle(mItemsList.get(0)));
        mHeaders.add(h);
        for (int i = 1; i < mItemsList.size(); i++) {
            T m = mItemsList.get(i);
            String newTitle = cg.getGroupTitle(m);
            if (!newTitle.equals(h.getTitle())) {
                //TODO sort headers instead ?
                if (newTitle.compareTo(h.getTitle()) < 0)
                    throw new IllegalArgumentException("Group headers must increase on sorted items");
                h = new Header<T>(newTitle);
                mHeaders.add(h);
            }
            h.getChildItemList().add(m);
            mItems2Headers.put(m,h);
        }
    }

    public static abstract class Callback<T2 extends Titled>{

        abstract public ComparatorGrouper<T2> getComparatorGrouper(String sortField) ;

        abstract public void onDataSetChanged(List<Header<T2>> headers);

        abstract public void onHeaderRemoved(Header<T2> h);

        /**
         *
         * @param h header of the removed item
         * @param tpos position within the header
         */
        abstract public void onGroupedItemRemoved(Header<T2> h, int tpos);

        abstract public void onUngroupedItemRemoved(int tpos);

    }


}
