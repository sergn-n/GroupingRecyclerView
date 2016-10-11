package ru.ncom.groupingrvadapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Serg on 11.09.2016.
 */
public abstract class GroupingAdapter<T extends Titled> extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements GetterAtPosition {

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
    private final Db<T> mDb;

    private final List<Titled> itemsList = new ArrayList<>();
    private ArrayList<String> mCollapsedHeaders = null;
    private String mSortFieldName = null;

    private Map<T,Header<T>> mItems2Headers = new HashMap<>();

    public GroupingAdapter(Class<T> clazz, Db<T> db) {
        this.mClass = clazz;
        this.mDb = db;
        // initially itemsList is just source data
        load();
    }

    /**
     * Loads unordered data into itemsList.
     */
    private void load() {
        saveCollapsedHeaders();
        itemsList.clear();
        mItems2Headers.clear();
        List<T> ml = mDb.getDataList();
        for (int i = 0; i < ml.size(); i++){
            itemsList.add(ml.get(i));
        }
    }

    private void saveCollapsedHeaders(){
        mCollapsedHeaders = new ArrayList<>();
        for (int i=0; i<itemsList.size(); i++){
            Titled itm = itemsList.get(i);
            if ((!isDataClass(itm)) && ((Header<T>)itm).isCollapsed())
                mCollapsedHeaders.add(itm.getTitle());
        }
    }

    public void reload(){
        load();
        if (mSortFieldName != null)
            doOrder(mSortFieldName);
        notifyDataSetChanged();
    }

    private boolean isDataClass(Titled tobj) {
        return mClass.isInstance(tobj);
    }

    @Override
    public int getItemViewType(int position) {
        if (isDataClass(itemsList.get(position)))
            return DATAROW;
        return HEADERROW;
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
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
        Titled item = itemsList.get(position);
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
        return itemsList.get(position);
    }

    public String getSortField() {
        return mSortFieldName;
    }

    // ** Ordering **

    public void orderBy(String sortField) {
        doOrder(sortField);
        notifyDataSetChanged();
    }

    /**
     * Sorts data by the specified field and creates headers. Uses {@link ComparatorGrouper}
     * provided by {@link Db}
     * @param sortField
     */
    protected void doOrder(String sortField) {
        mSortFieldName = sortField;
        ComparatorGrouper<T> mcb = mDb.getComparatorGrouper(sortField);
        List<T> ml = mDb.orderBy(mcb);
        Header<T> h = null;
        itemsList.clear();
        mItems2Headers.clear();
        for (int i = 0; i < ml.size(); i++) {
            T m = ml.get(i);
            String newTitle = mcb.getGroupTitle(m);
            if ((h==null) || !newTitle.equals(h.getTitle())) {
                h = new Header<>(newTitle);
                itemsList.add(h);
                if (mCollapsedHeaders != null //sort is fired by restoring after configuration change
                        && mCollapsedHeaders.indexOf(newTitle) >= 0){
                    h.setCollapsed(true);
                }
            }
            if (!h.isCollapsed())
                itemsList.add(m);
            h.getChildItemList().add(m);
            mItems2Headers.put(m,h);
        }
        //  Clear restored collapsed headers till next configuration change
        mCollapsedHeaders = null;
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
            doOrder(mSortFieldName);
    }

    //  **Collapse / expand group by clicking on header.**

    private int expandHeader(Header<T> h, int headerPosition) {
        int count = 0;
        List<T> childItemList = h.getChildItemList();
        if (childItemList != null) {
            count = childItemList.size();
            for (int i = 0; i < count; i++) {
                itemsList.add(headerPosition + i + 1, childItemList.get(i));
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
                itemsList.remove(headerPosition + i + 1);
            }
        }
        h.setCollapsed(true);
        return count;
    }

    private void toggleCollapseExpand(int headerPosition) {
        Titled itm = itemsList.get(headerPosition);
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

    public void delete(int position) throws IOException{
        Titled t = getAt(position);
        if (!isDataClass(t))
            return;
        T item = (T) t;
        if (!mDb.delete(item))
            return;

        // Quick and dirty, general notification
        //reload();

        // Exact notification
        int count = 0;
        int pos = 0;
        Header<T> h = mItems2Headers.get(t);
        if (h==null){ // not sorted
            pos = itemsList.indexOf(t);
            itemsList.remove(pos); //
            count++;
        }
        else {
            if (h.getChildItemList().size() == 1) {
                // the only item in group, remove header too
                pos = itemsList.indexOf(h);
                if (!h.isCollapsed()) {
                    // t must be on the itemsList immediately after h
                    itemsList.remove(pos + 1);
                    count++;
                }
                itemsList.remove(pos);
                count++;
            } else {
                h.getChildItemList().remove(t);
                if (!h.isCollapsed()) {
                    pos = itemsList.indexOf(t);
                    itemsList.remove(pos); //
                    count++;
                }
            }
            mItems2Headers.remove(t);
        }
        notifyItemRangeRemoved(pos,count);
    }
}
