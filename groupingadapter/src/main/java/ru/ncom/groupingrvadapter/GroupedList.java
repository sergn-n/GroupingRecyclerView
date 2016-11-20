package ru.ncom.groupingrvadapter;

import java.util.ArrayList;
import java.util.Arrays;
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

public class GroupedList<T> {

    private Callback mCallback;
    public Callback getCallback() {
        return mCallback;
    }

    // Current sort field
    private String mSortFieldName = null;
    public String getSortFieldName() {
        return mSortFieldName;
    }

    // Unsorted list, original order
    private final List<T> mItemsList = new ArrayList<>();

    // Sorted item to group header map
    private Map<T,Header<T>> mItems2Headers = new HashMap<>();

    // Group headers
    private final List<Header<T>> mHeaders = new ArrayList<>();
    public List<Header<T>> getHeaders() {
        return mHeaders;
    }

    public GroupedList(Callback<T> cb) {
        this.mCallback = cb;
    }

    public int size(){
        return mItemsList.size();
    }

    public void clear(){
        mItemsList.clear();
        mItems2Headers.clear();
        mHeaders.clear();
        if (mCallback != null)
            mCallback.onClear();
    }

    public void add(T item) {
        mItemsList.add(item);
        if (mSortFieldName == null) {
            // List isn't sorted
            List<T> newItems = new ArrayList<>(1);
            newItems.add(item);
            if (mCallback != null)
                mCallback.onUngroupedItemsAdded(newItems);
            return;
        }
        // Add to sorted list
        if (mCallback == null)
            throw new IllegalArgumentException("No callback specified, can't get ComparatorGrouper.");
        ComparatorGrouper cg = mCallback.getComparatorGrouper(mSortFieldName);
        String myGroupTitle = cg.getGroupTitle(item);
        int hpos = binarySearch(myGroupTitle, mHeaders);
        Header<T> h;
        if (hpos >= 0) {
            // found the group new item belongs to
            h = mHeaders.get(hpos);
            List<T> children = h.getChildItemList();
            int pos = Arrays.binarySearch(children.toArray(), item, cg);
            if (pos < 0)
                pos = -1 - pos;
            children.add(pos, item);
            mItems2Headers.put(item, h);
            mCallback.onGroupedItemAdded(hpos, item, pos);
        }
        else {
            // add new header with item
            h = new Header<T>(myGroupTitle);
            mHeaders.add(-1 - hpos, h);
            h.getChildItemList().add(item);
            mItems2Headers.put(item, h);
            mCallback.onHeaderAdded(h, -1 - hpos);
        }
    }

    public void addAll(List<T> items) {
        if (items.size() == 0) {
            return;
        }
        mItemsList.addAll(items);

        if (mSortFieldName == null) {
            if (mCallback != null)
                mCallback.onUngroupedItemsAdded(items);
            return;
        }

        // doSort() uses only getComparatorGrouper() method of callback,
        // no calls of on<Event>() methods
        GroupedList<T> newItems = new GroupedList<T>(mCallback);
        newItems.addAll(items);
        newItems.doSort(mSortFieldName);
        merge(newItems);
        mCallback.onDataSorted(this);
    }

    private void merge(GroupedList<T> newItemList) {
        List<Header<T>> newHeaders = newItemList.getHeaders();
        ComparatorGrouper cg = mCallback.getComparatorGrouper(mSortFieldName);
        for (int i = 0; i < newHeaders.size(); i++){
            Header<T> newH = newHeaders.get(i);
            List<T> newChildren = newH.getChildItemList();
            String newHTitle = newHeaders.get(i).getTitle();
            int hpos = binarySearch(newHTitle, mHeaders);
            if (hpos >= 0) {
                // merge items, take no care of doubles
                Header<T> h = mHeaders.get(hpos);
                List<T> children = h.getChildItemList();
                //TODO Has the version {2 lists -> array  -> sort -> list} got better performance?
                Object[] ca = children.toArray();
                for (int j = 0; j < newChildren.size(); j++) {
                    T itm = newChildren.get(j);
                    int tpos = Arrays.binarySearch(ca, itm, cg);
                    if (tpos < 0)
                        tpos = -1 - tpos;
                    children.add(tpos, itm);
                    mItems2Headers.put(itm, h);
                }
            }
            else {
                // add new header
                mHeaders.add(-1 - hpos, newH);
                for (int j = 0; j < newChildren.size(); j++) {
                    mItems2Headers.put(newChildren.get(j), newH);
                }
            }
        }
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

        if (mSortFieldName == null) {
            // unsorted list
            if (mCallback != null)
                mCallback.onUngroupedItemRemoved(tpos);
            return true;
        }
        // sorted list
        Header<T> h = mItems2Headers.get(item);
        mItems2Headers.remove(item);
        int hpos = mHeaders.indexOf(h);
        if (h.getChildItemList().size() == 1) {
            // the only item in group, remove the entire header
            mHeaders.remove(h);
            if (mCallback != null)
                mCallback.onHeaderRemoved(hpos);
        } else {
            tpos = h.getChildItemList().indexOf(item);
            h.getChildItemList().remove(tpos);
            if (mCallback != null)
                mCallback.onGroupedItemRemoved(hpos, tpos);
        }
        return true;
    }

    /**
     * Sorts data by the specified field and creates ordered headers.
     */
    public void sort(String sortField) {
        doSort(sortField);
        if (mCallback != null)
            mCallback.onDataSorted(this);
    }

    public List<Header<T>> doSort(String sortField) {
        mSortFieldName = sortField;
        mItems2Headers.clear();
        mHeaders.clear();
        if (mItemsList.size() == 0)
            return mHeaders;

        if (mCallback == null)
            throw new IllegalArgumentException("No callback specified, can't get ComparatorGrouper.");
        ComparatorGrouper cg = mCallback.getComparatorGrouper(sortField);
        if (mItemsList.size() > 1)
            Collections.sort(mItemsList, cg);
        Header<T> h = new Header<>(cg.getGroupTitle(mItemsList.get(0)));
        mHeaders.add(h);
        for (int i = 0; i < mItemsList.size(); i++) {
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
        return mHeaders;
    }

    /**
     *
     * @param title
     * @param headers
     * @return
     */
    private int binarySearch(String title, List<Header<T>> headers) {
        int left = 0;
        int right = headers.size() - 1;
        int middle = 0;
        int cmp = 1;
        while (left <= right) {
            middle = (left + right) / 2;
            Header myItem = headers.get(middle);
            cmp = myItem.getTitle().compareTo(title);
            if (cmp < 0) {
                left = middle + 1;
            } else if (cmp == 0) {
                return middle;
            } else {
                right = middle - 1;
            }
        }
        return -middle - (cmp > 0 ? 1 : 2);
    }

    public interface Callback<T2>{

        ComparatorGrouper<T2> getComparatorGrouper(String sortField) ;

        void onClear();

        void onDataSorted(GroupedList<T2> gl);

        void onGroupedItemAdded(int hpos, T2 item, int  pos);

        void onUngroupedItemsAdded(List<T2> items);

        void onHeaderAdded(Header<T2> h, int pos);

        void onHeaderRemoved(int hpos);

        /**
         *
         * @param hpos position of the header of the removed item
         * @param tpos position within the header
         */
        void onGroupedItemRemoved(int hpos, int tpos);

        void onUngroupedItemRemoved(int tpos);

    }

}
