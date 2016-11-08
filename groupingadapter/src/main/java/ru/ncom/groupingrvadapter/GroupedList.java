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
    public Callback getCallback() {
        return mCallback;
    }


    // current sort field
    private String mSortFieldName = null;
    public String getSortFieldName() {
        return mSortFieldName;
    }

    private final List<T> mItemsList = new ArrayList<>();

    private Map<T,Header<T>> mItems2Headers = new HashMap<>();

    private final List<Header<T>> mHeaders = new ArrayList<>();
    public List<Header<T>> getHeaders() {
        return mHeaders;
    }



    public GroupedList(Class<T> clazz, Callback<T> cb) {
        this.mClass = clazz;
        this.mCallback = cb;
    }

    public void clear(){
        mItemsList.clear();
        mItems2Headers.clear();
        mHeaders.clear();
        mCallback.onClear();
    }

    /**
     *
     */
    private void add(T item) {
        mItemsList.add(item);
        if (mSortFieldName == null) {
            List<T> newItems = new ArrayList<>(1);
            newItems.add(item);
            mCallback.onUngroupedItemsAdded(newItems);
            return;
        }
        //TODO sorted list
        ComparatorGrouper cg = mCallback.getComparatorGrouper(mSortFieldName);
        String myGroupTitle = cg.getGroupTitle(item);
        BSearchResult bsrH = findIndexOf(myGroupTitle, (List<Titled>)(List<?>)mHeaders, 0, mHeaders.size()-1);
        Header<T> h;
        if (bsrH.isFound()) {
            // found exact header
            h = mHeaders.get(bsrH.left);
            List<Titled> children = (List<Titled>)(List<?>)h.getChildItemList();
            BSearchResult bsrC = findIndexOf(item.getTitle(), children, 0, h.getChildItemList().size()-1);
            int pos = (bsrC.left == OPENINTERVAL) ? 0 : bsrC.left + 1;
            children.add(pos, item);
            mCallback.onGroupedItemAdded(bsrH.left, item, pos);

        }
        else {
            // add new header
            h = new Header<T>(myGroupTitle);
            h.getChildItemList().add(item);
            int pos = (bsrH.left == OPENINTERVAL) ? 0 : bsrH.left + 1;
            mHeaders.add(pos, h);
            mCallback.onHeaderAdded(h,pos);
        }

    }

    public void addAll(List<T> items) {
        if (items.size() == 0) {
            return;
        }
        if (mSortFieldName == null) {
            mItemsList.addAll(items);
            mCallback.onUngroupedItemsAdded(items);
            return;
        }
        GroupedList<T> newItems = new GroupedList<T>(mClass,null);
        newItems.addAll(items);
        newItems.doSort(mSortFieldName);
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
        doSort(sortField);
        mCallback.onDataSorted(this);
    }

    public List<Header<T>> doSort(String sortField) {
        mSortFieldName = sortField;
        mItems2Headers.clear();
        mHeaders.clear();
        if (mItemsList.size() == 0)
            return mHeaders;

        ComparatorGrouper cg = mCallback.getComparatorGrouper(sortField);
        if (mItemsList.size() > 1)
            Collections.sort(mItemsList, cg);
        Header<T> h = new Header<>(cg.getGroupTitle(mItemsList.get(0)));
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
        return mHeaders;
    }

    // binary search

    private final int OPENINTERVAL =-1;
    private class BSearchResult {
        public int left = OPENINTERVAL;
        public int right = OPENINTERVAL;
        public boolean isFound(){
            return (left==right && left!= OPENINTERVAL);
        }

    }

    /**
     *  Returns interval (left right), may be open.
     * @param title
     * @param titledList
     * @param left
     * @param right
     * @return
     */
    private BSearchResult findIndexOf(String title, List<Titled> titledList, int left, int right) {
        BSearchResult bsr = new BSearchResult();
        while (left <= right) {
            final int middle = (left + right) / 2;
            Titled myItem = titledList.get(middle);
            final int cmp = myItem.getTitle().compareTo(title);
            if (cmp < 0) {
                bsr.left = middle;
                left = middle + 1;
            } else if (cmp == 0) {
                bsr.left = middle;
                bsr.right= middle;
                return bsr;
            } else {
                bsr.right = middle;
                right = middle - 1;
            }
        }
        return bsr;
    }


    public interface Callback<T2 extends Titled>{

        ComparatorGrouper<T2> getComparatorGrouper(String sortField) ;

        void onClear();

        void onDataSorted(GroupedList<T2> gl);

        void onGroupedItemAdded(int hpos, T2 item, int  pos);

        void onUngroupedItemsAdded(List<T2> items);

        void onHeaderAdded(Header<T2> h, int pos);

        void onHeaderRemoved(Header<T2> h);

        /**
         *
         * @param h header of the removed item
         * @param tpos position within the header
         */
        void onGroupedItemRemoved(Header<T2> h, int tpos);

        void onUngroupedItemRemoved(int tpos);

    }

}
