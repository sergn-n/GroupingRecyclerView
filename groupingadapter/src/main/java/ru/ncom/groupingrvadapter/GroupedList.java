package ru.ncom.groupingrvadapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**.
 * Grouped list of items of T type.
 * Keeps track of item's grouping according to the items' sort key and group headers provided by callback..
 * List is generally not ordered except immediately after sort or addAll method call.
 * Notifies adapter (callback) on:
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

    // All items
    private final List<T> mItemsList = new ArrayList<>();

    // Sorted item to group header map
    private Map<T,Header<T>> mItems2Headers = new HashMap<>();

    // Group headers
    private final List<Header<T>> mHeaders = new ArrayList<>();
    List<Header<T>> getHeaders() {
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
            int pos = h.add(item, cg);
            mItems2Headers.put(item, h);
            mCallback.onGroupedItemAdded(hpos, item, pos);
        }
        else {
            // add new header with item
            h = new Header<T>(myGroupTitle);
            mHeaders.add(-1 - hpos, h);
            h.add(item, cg);
            mItems2Headers.put(item, h);
            mCallback.onHeaderAdded(h, -1 - hpos);
        }
    }

    /**
     * Adds items to the list.
     */
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

        // List is grouped

        if (mHeaders.size() == 0) {
            // will throw if mCallback == null
            doSort(mSortFieldName);
        } else {
            mergeItems((T[])items.toArray());
        }
        mCallback.onDataSorted(this);
    }

    /**
     * Adds items to the list.
     * @param items null will throw NullPointerException
     */
    public void addAll(T... items) {
        if (items.length == 0) {
            return;
        }
        List<T> itemList = Arrays.asList(items);
        mItemsList.addAll(itemList);

        if (mSortFieldName == null) {
            if (mCallback != null)
                mCallback.onUngroupedItemsAdded(itemList);
            return;
        }

        // List is grouped

        if (mHeaders.size() == 0) {
            // will throw if mCallback == null
            doSort(mSortFieldName);
        } else {
            mergeItems(items);
        }
        mCallback.onDataSorted(this);
    }

    /**
     *  Merge sorted non-empty items to non-empty Headers
     * @param items
     */
    private void mergeItems(T[] items){
        ComparatorGrouper<T> cg = throwIfNoCallback();
        Arrays.sort(items, cg);
        int iStart = 0;
        int iEnd = 0;
        Header<T> h = mHeaders.get(0);
        for (int i = 0; i < items.length; i++){
            String myGroupTitle = cg.getGroupTitle(items[i]);
            if (!h.getTitle().equals(myGroupTitle)) {
                h.merge(items, iStart, iEnd, cg);
                iStart = i;
                iEnd = i;
                int hpos = binarySearch(myGroupTitle, mHeaders);
                if (hpos < 0) {
                    // new Header
                    hpos = -1 - hpos;
                    h = new Header<>(myGroupTitle);
                    mHeaders.add(hpos, h);
                }
                else
                    h = mHeaders.get(hpos);
            }
            iEnd++;
            mItems2Headers.put(items[i], h);
        }
        h.merge(items, iStart, iEnd, cg);
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
        if (h.size() == 1) {
            // the only item in group, remove the entire header
            mHeaders.remove(h);
            if (mCallback != null)
                mCallback.onHeaderRemoved(hpos);
        } else {
            tpos = h.indexOf(item);
            h.remove(tpos);
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
        List<String> collapsedTitles = null;
        if (mItemsList.size() > 0 && mSortFieldName != null && mSortFieldName.equals(sortField)){
            // remember collapsing for the same ordering
            collapsedTitles = new ArrayList<>(mHeaders.size());
            for (Header<T> h: mHeaders) {
                if (h.isCollapsed())
                    collapsedTitles.add(h.getTitle());
            }
        }
        mSortFieldName = sortField;
        mItems2Headers.clear();
        mHeaders.clear();
        if (mItemsList.size() == 0)
            return mHeaders;

        ComparatorGrouper cg = throwIfNoCallback();
        if (mItemsList.size() > 1)
            Collections.sort(mItemsList, cg);
        // First Header
        String newTitle = cg.getGroupTitle(mItemsList.get(0));
        Header<T> h = new Header<>(newTitle);
        mHeaders.add(h);
        if (collapsedTitles != null && collapsedTitles.contains(newTitle))
            h.setCollapsed(true);
        int iStart = 0;
        int iEnd = 0;
        T[] items = (T[]) mItemsList.toArray();
        for (int i = 0; i < mItemsList.size(); i++) {
            T m = items[i];
            newTitle = cg.getGroupTitle(m);
            if (!newTitle.equals(h.getTitle())) {
                h.merge(items, iStart, iEnd, cg);
                iStart = i;
                iEnd = i;
                //TODO sort headers instead ?
                if (newTitle.compareTo(h.getTitle()) < 0)
                    throw new IllegalArgumentException("Group headers must increase on sorted items");
                h = new Header<T>(newTitle);
                mHeaders.add(h);
                if (collapsedTitles != null && collapsedTitles.contains(newTitle))
                    h.setCollapsed(true);
            }
            iEnd++;
            mItems2Headers.put(m,h);
        }
        h.merge(items, iStart, iEnd, cg);
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

    private ComparatorGrouper<T> throwIfNoCallback(){
        if (mCallback == null)
            throw new IllegalArgumentException("No callback specified, can't get ComparatorGrouper.");
        return mCallback.getComparatorGrouper(mSortFieldName);
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
