package ru.ncom.groupingrvadapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Serg on 11.09.2016.
 */
public abstract class GroupingAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements GetterAtPosition, GroupedList.Callback<T> {

    /**
     * Row type: Header of the group
     */
    public static final int HEADERROW = 1;
    /**
     * Row type: not a Header
     */
    public static final int DATAROW = 2;

    private final String COLLAPSEDHEADERS = "COLLAPSEDHEADERS";
    private final String SORTFIELDNAME = "SORTFIELDNAME";

    private final List<Object> mItemsList = new ArrayList<>();

    private ArrayList<String> mCollapsedHeaders = null;

    private View.OnClickListener mCollapseExpandCL;

    private String mSortFieldName = null;
    public String getSortField() {
        return mSortFieldName;
    }

    private int[] mHeader2Item;
    private int mHeader2ItemSize = 0;

    // ** RecyclerView.Adapter<> members **

    /**
     * + creates {@link CollapseExpandClickListener} to be used in {@link #createHeaderViewHolder(int, int, ViewGroup)}.
     * The listener uses {@link RecyclerView#getChildLayoutPosition(View)}
     * to get the position of the view clicked.
     * @param recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mCollapseExpandCL = new CollapseExpandClickListener(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderClass(mItemsList.get(position)))
            return HEADERROW;
        return DATAROW;
    }

    @Override
    public int getItemCount() {
        return mItemsList.size();
    }

    /**
     * Convenience method. Inflates specified header layout
     * Creates default ViewHolder to hold specified title TextView of the layout.
     * Alternatively you can create your own holder which must implement {@link TitledViewHolder}
     * @param headerLayoutId id of the header layout.
     * @param titleTextViewId id of the title TextView in the header layout.
     * @param parent
     * @return
    public RecyclerView.ViewHolder createHeaderViewHolder(int headerLayoutId, int titleTextViewId, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(headerLayoutId, parent, false);
            return new HeaderViewHolder(itemView, titleTextViewId);
    }
    */

    /**
     * Sets itemView selection. If current view is a header
     * adds also a number of items under the header. For a header also sets it's expand/collapse ClickListener.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = mItemsList.get(position);
        if (item instanceof Selectable)
            holder.itemView.setSelected(((Selectable)item).isSelected());
        if (holder.getItemViewType() == HEADERROW ) {
            holder.itemView.setOnClickListener(mCollapseExpandCL);
            TextView v = ((HeaderViewHolder)holder).getTitleView();
            Header<T> h = (Header<T>)item;
            String txt = h.getTitle();
            txt += (" (" + h.size() + ")");
            v.setText(txt);
        }
    }

    // ** **

    @Override
    public Object getAt(int position) {
        return mItemsList.get(position);
    }

    // Call those methods from Activity when non-retaining adapter is used

    public void onSaveInstanceState(Bundle outState){
        outState.putString(SORTFIELDNAME,mSortFieldName);
        saveCollapsedHeaders();
        outState.putStringArrayList(COLLAPSEDHEADERS,mCollapsedHeaders);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        mCollapsedHeaders = savedInstanceState.getStringArrayList(COLLAPSEDHEADERS);
        mSortFieldName = savedInstanceState.getString(SORTFIELDNAME);
        if (mSortFieldName != null)
            //TODO restore collapsing
            // doOrder(mSortFieldName)
        ;
    }

    //  ** Collapse / expand group by clicking on header. **

    private int expandHeader(Header<T> h, int headerPosition, int hpos) {
        int count = h.size();
        mItemsList.addAll(headerPosition + 1, h.getChildren());
        if (count > 0) {
            for (int i = hpos + 1; i < mHeader2ItemSize; i++)
                mHeader2Item[i] += count;
            notifyItemRangeInserted(headerPosition + 1, count);
        }
        h.setCollapsed(false);
        return count;
    }

    private int collapseHeader(Header<T> h, int headerPosition, int hpos) {
        int count = h.size();
        for (int i = count - 1; i >= 0; i--) {
            mItemsList.remove(headerPosition + i + 1);
        }
        if (count > 0) {
            for (int i = hpos + 1; i < mHeader2ItemSize; i++)
                mHeader2Item[i] -= count;
            notifyItemRangeRemoved(headerPosition + 1, count);
        }
        h.setCollapsed(true);
        return count;
    }

    private void toggleCollapseExpand(int headerPosition) {
        Object itm = mItemsList.get(headerPosition);
        if (isHeaderClass(itm)) {
            // toggle collapse
            Header<T> h = (Header<T>) itm;
            int hpos = Arrays.binarySearch(mHeader2Item, 0, mHeader2ItemSize, headerPosition);
            if (h.isCollapsed()) {
                expandHeader(h, headerPosition, hpos);
            } else {
                collapseHeader(h, headerPosition, hpos);
            }
            // isCollapsed changed
            notifyItemChanged(headerPosition);
        }
    }

    private void saveCollapsedHeaders(){
        mCollapsedHeaders = new ArrayList<>();
        for (int i = 0; i< mItemsList.size(); i++){
            Object itm = mItemsList.get(i);
            if ((isHeaderClass(itm)) && ((Header<T>)itm).isCollapsed())
                mCollapsedHeaders.add(((Header<T>)itm).getTitle());
        }
    }

    private boolean isHeaderClass(Object tobj) {
        return Header.class.isInstance(tobj);
    }

    private class CollapseExpandClickListener implements View.OnClickListener {
        private final RecyclerView mRecyclerView;

        private CollapseExpandClickListener (RecyclerView rv) {
            mRecyclerView = rv;
        }

        @Override
        public void onClick(final View view) {
             toggleCollapseExpand(mRecyclerView.getChildLayoutPosition(view));
        }
    }

    // ** Data manipulation **

    @Override
    public void onClear() {
        mItemsList.clear();
        notifyDataSetChanged();
    }

    @Override
    public abstract ComparatorGrouper<T> getComparatorGrouper(String sortField) ;

    @Override
    public void onDataSorted(GroupedList<T> gl) {
        mHeader2ItemSize = gl.getHeaders().size();
        mHeader2Item = new int[mHeader2ItemSize + 12];
        mSortFieldName = gl.getSortFieldName();
        mItemsList.clear();
        int hIdx = 0;
        for (Header<T> h: gl.getHeaders()) {
            mItemsList.add(h);
            mHeader2Item[hIdx++] = mItemsList.size() - 1;
            if (!h.isCollapsed())
                mItemsList.addAll(h.getChildren());
        }
        notifyDataSetChanged();
    }

    @Override
    public void onGroupedItemAdded(int hpos, T item, int pos) {
        int tpos = mHeader2Item[hpos];
        Header<T> h = (Header<T>)mItemsList.get(tpos);
        if (!h.isCollapsed()) {
            pos = tpos + pos + 1;
            mItemsList.add(pos, item);
            // Adjust tail of index by +1
            for (int i = hpos + 1; i < mHeader2ItemSize; i++)
                mHeader2Item[i]++;
            notifyItemInserted(pos);
        }
        // update header's count of items
        notifyItemChanged(tpos);
    }

    @Override
    public void onUngroupedItemsAdded(List<T> items) {
        int pos = mItemsList.size();
        mItemsList.addAll(items);
        notifyItemRangeInserted(pos, items.size());
    }

    @Override
    public void onHeaderAdded(Header<T> h, int pos){
        int shift = h.size() + 1;
        // Update mHeader2Item index
        int[] a;
        if (mHeader2ItemSize == mHeader2Item.length){
            a = new int[mHeader2ItemSize + 12];
            // copy head of index
            System.arraycopy(mHeader2Item,0,a,0,pos);
        }
        else {
            // assert mHeader2ItemSize < mHeader2Item.length
            a = mHeader2Item;
        }
        // copy tail of idx adding shift
        for (int i =  mHeader2ItemSize; i > pos; i--)
            a[i] = mHeader2Item[i-1] + shift;
        // set new header pos
        a[pos] = (pos < mHeader2ItemSize)
                ? mHeader2Item[pos]  // index of current header at pos
                : mItemsList.size(); // end of the items
        mHeader2ItemSize++;
        mHeader2Item = a;

        // Add header and children as items
        mItemsList.add(mHeader2Item[pos], h);
        mItemsList.addAll(mHeader2Item[pos] + 1, h.getChildren());

        notifyItemRangeInserted(mHeader2Item[pos], shift);
    }

    @Override
    public void onHeaderRemoved(int hpos) {
        int pos = mHeader2Item[hpos];
        // number of items to delete
        int shift = (hpos < mHeader2ItemSize - 1
                ? mHeader2Item[hpos + 1]
                : mItemsList.size()) - pos;
        // Remove items
        for (int j = 1; j <= shift; j++) {
            mItemsList.remove(pos + shift - j);
        }
        // Copy index tail with shift
        for (int i = hpos ; i < mHeader2ItemSize - 1; i++)
            mHeader2Item[i] = mHeader2Item[i + 1] - shift;
        mHeader2ItemSize--;
        notifyItemRangeRemoved(pos, shift);
    }

    @Override
    public void onGroupedItemRemoved(int hpos, int pos) {
        int tpos = mHeader2Item[hpos];
        Header<T> h = (Header<T>)mItemsList.get(tpos);
        if (!h.isCollapsed()) {
            pos = tpos + pos + 1;
            mItemsList.remove(pos);
            // Adjust index tail by -1
            for (int i = hpos + 1; i < mHeader2ItemSize; i++)
                mHeader2Item[i]--;
            notifyItemRemoved(pos);
        }
        // update header's count of items
        notifyItemChanged(tpos);
    }

    @Override
    public void onUngroupedItemRemoved(int pos) {
        mItemsList.remove(pos);
        notifyItemRemoved(pos);
    }
}
