package ru.ncom.groupingrvadapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serg on 11.09.2016.
 */
public abstract class GroupingAdapter<T extends Titled> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements GetterAtPosition, GroupedList.Callback<T> {

    /**
     * Row type: row of data of T type
     */
    public static final int DATAROW = 1;
    /**
     * Row type: header of the group of rows of data of T type
     */
    public static final int HEADERROW = 2;

    private final String COLLAPSEDHEADERS = "COLLAPSEDHEADERS";
    private final String SORTFIELDNAME = "SORTFIELDNAME";

    private final Class<T> mClass;

    private final List<Titled> mItemsList = new ArrayList<>();

    private ArrayList<String> mCollapsedHeaders = null;
    private String mSortFieldName = null;

    public GroupingAdapter(Class<T> clazz) {
        this.mClass = clazz;
    }

    private void saveCollapsedHeaders(){
        mCollapsedHeaders = new ArrayList<>();
        for (int i = 0; i< mItemsList.size(); i++){
            Titled itm = mItemsList.get(i);
            if ((!isDataClass(itm)) && ((Header<T>)itm).isCollapsed())
                mCollapsedHeaders.add(itm.getTitle());
        }
    }


    private boolean isDataClass(Titled tobj) {
        return mClass.isInstance(tobj);
    }

    @Override
    public int getItemViewType(int position) {
        if (isDataClass(mItemsList.get(position)))
            return DATAROW;
        return HEADERROW;
    }

    @Override
    public int getItemCount() {
        return mItemsList.size();
    }

    /**
     * Inflates specified header layout
     * Creates default ViewHolder to hold specified title TextView of the layout.
     * Alternatively you can create your own holder which must implement {@link TitledViewHolder}
     * @param headerLayoutId id of the header layout.
     * @param titleTextViewId id of the title TextView in the header layout.
     * @param parent
     * @return
     */
    public RecyclerView.ViewHolder createHeaderViewHolder(int headerLayoutId, int titleTextViewId, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(headerLayoutId, parent, false);
            return new HeaderViewHolder(itemView, titleTextViewId);
    }

    /**
     * Sets text value of the title TextView, see {@link TitledViewHolder#getTitleView()}. Sets itemView selection. If current view is a header
     * adds also a number of items under the header. For a header also sets it's expand/collapse ClickListener.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Titled item = mItemsList.get(position);
        if (holder.getItemViewType() == HEADERROW )
            holder.itemView.setOnClickListener(mCollapseExpandCL);
        if (item instanceof Selectable)
            holder.itemView.setSelected(((Selectable)item).isSelected());
        TextView v = ((TitledViewHolder)holder).getTitleView();
        String txt = item.getTitle();
        if ( !isDataClass(item) ) {
            Header<T> h = (Header<T>)item;
            txt += (" (" + h.getChildItemList().size() + ")");
        }
        v.setText(txt);
    }

    @Override
    public Titled getAt(int position) {
        return mItemsList.get(position);
    }

    public String getSortField() {
        return mSortFieldName;
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

    //  **Collapse / expand group by clicking on header.**

    private int expandHeader(Header<T> h, int headerPosition) {
        int count = 0;
        List<T> childItemList = h.getChildItemList();
        if (childItemList != null) {
            count = childItemList.size();
            for (int i = 0; i < count; i++) {
                mItemsList.add(headerPosition + i + 1, childItemList.get(i));
            }
        }
        h.setCollapsed(false);
        return count;
    }

    private int collapseHeader(Header<T> h, int headerPosition) {
        int count = 0;
        List<T> childItemList = h.getChildItemList();
        if (childItemList != null) {
            count = childItemList.size();
            for (int i = count - 1; i >= 0; i--) {
                mItemsList.remove(headerPosition + i + 1);
            }
        }
        h.setCollapsed(true);
        return count;
    }

    private void toggleCollapseExpand(int headerPosition) {
        Titled itm = mItemsList.get(headerPosition);
        if (!isDataClass(itm)) {
            // clear restored state
            mCollapsedHeaders = null;
            // toggle collapse
            Header<T> h = (Header<T>) itm;
            int childListItemCount;
            if (h.isCollapsed()) {
                childListItemCount = expandHeader(h, headerPosition);
                if (childListItemCount > 0)
                    notifyItemRangeInserted(headerPosition + 1, childListItemCount);
            } else {
                childListItemCount = collapseHeader(h, headerPosition);
                if (childListItemCount > 0)
                    notifyItemRangeRemoved(headerPosition + 1, childListItemCount);
            }
            // isSelected() changed anyway
            notifyItemChanged(headerPosition);
        }
    }

    private View.OnClickListener mCollapseExpandCL;

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

    private class CollapseExpandClickListener implements View.OnClickListener {
        private final RecyclerView mRecyclerView;

        private CollapseExpandClickListener (RecyclerView rv) {
            mRecyclerView = rv;
        }

        @Override
        public void onClick(final View view) {
            int headerPosition = mRecyclerView.getChildLayoutPosition(view);
            toggleCollapseExpand(headerPosition);
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
        mSortFieldName = gl.getSortFieldName();
        mItemsList.clear();
        for (Header<T> h: gl.getHeaders()) {
            mItemsList.add(h);
            for (T item: h.getChildItemList()) {
                mItemsList.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onUngroupedItemsAdded(List<T> items) {
        int pos = mItemsList.size();
        mItemsList.addAll(items);
        notifyItemRangeInserted(pos, items.size());
    }

    @Override
    public void onHeaderRemoved(Header h) {

    }

    @Override
    public void onGroupedItemRemoved(Header h, int tpos) {

    }

    @Override
    public void onUngroupedItemRemoved(int tpos) {

    }
}
