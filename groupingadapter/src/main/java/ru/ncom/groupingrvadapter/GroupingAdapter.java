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
public  abstract class GroupingAdapter<T extends Titled> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int DATAROW = 1;
    public final int HEADERROW = 2;
    private final String COLLAPSEDHEADERS = "COLLAPSEDHEADERS";

    private final Class<T> mClass;
    private final Db<T> mDb;
    private final RecyclerView mRecyclerView;

    private final List<Titled> itemsList = new ArrayList<>();
    ArrayList<String> mCollapsedHeaders = null;

    public GroupingAdapter(Class<T> clazz, Db<T> db, RecyclerView rv) {
        this.mClass = clazz;
        this.mRecyclerView = rv;
        this.mDb = db;
        // initially itemsList is just source data
        List<T> ml = mDb.getDataList();
        for (int i = 0; i < ml.size(); i++){
            itemsList.add(ml.get(i));
        }
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
     * Inflates specified header layout and sets it's expand/collapse ClickListener.
     * Creates default ViewHolder with a view for the title TextView.
     * Alternatively you can create your own holder which must implement {@link TitledViewHolder}
     * @param headerLayoutId id of the header layout.
     * @param titleTextViewId id of the title TextView in the header layout.
     * @param parent
     * @return
     */
    public RecyclerView.ViewHolder createHeaderViewHolder(int headerLayoutId, int titleTextViewId, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(headerLayoutId, parent, false);
            itemView.setOnClickListener(mCollapseExpandCL);
            return new HeaderViewHolder(itemView, titleTextViewId);
    }

    /**
     * Sets text of the title view see {@link TitledViewHolder#getTitleView()}. If current view is a header
     * adds also a number of items under the header.
     * @param holder
     * @param position
     */
    public void BindTitleView(RecyclerView.ViewHolder holder, int position) {
        Titled item = itemsList.get(position);
        TextView v = ((TitledViewHolder)holder).getTitleView();
        String txt = item.getTitle();
        if ( !isDataClass(item) ) {
            Header<T> h = (Header<T>)item;
            txt +=  (" (" + h.getChildItemList().size() + ")");
        }
        v.setText(txt);
    }

    public Titled getAt(int position) {
        return itemsList.get(position);
    }

    // ** Ordering **

    public void orderBy(String sortField) {
        doOrder(sortField);
        notifyDataSetChanged();
    }

    protected void doOrder(String sortField) {

        ComparatorGrouper<T> mcb = mDb.getComparatorGrouper(sortField);
        List<T> ml = mDb.orderBy(mcb);
        Header<T> h = null;
        itemsList.clear();
        for (int i = 0; i < ml.size(); i++) {
            T m = ml.get(i);
            String newTitle = mcb.getGroupTitle(m);
            if ((h==null) || !newTitle.equals(h.getTitle())) {
                h = new Header<>(newTitle);
                itemsList.add(h);
                if (mCollapsedHeaders != null //sort is fired by restoring after screen rotation
                        && mCollapsedHeaders.indexOf(newTitle) >= 0){
                    h.setCollapsed(true);
                }
            }
            if (!h.isCollapsed())
                itemsList.add(m);
            h.getChildItemList().add(m);
        }
        //  Clear restored collapsed headers till next screen rotation
        mCollapsedHeaders = null;
    }

    /**
     * Saves the list of collapsed headers.
     * @param outState
     */
    public void onSaveInstanceState(Bundle outState){
        ArrayList<String> collapsedHeaders = new ArrayList<>();
        for (int i=0; i<itemsList.size(); i++){
            Titled itm = itemsList.get(i);
            if ((!isDataClass(itm)) && ((Header<T>)itm).isCollapsed())
                collapsedHeaders.add(itm.getTitle());
        }
        outState.putStringArrayList(COLLAPSEDHEADERS,collapsedHeaders);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        mCollapsedHeaders = savedInstanceState.getStringArrayList(COLLAPSEDHEADERS);
    }

    // **Click listeners**

    //  Collapse / expand group by clicking on header.
    protected final View.OnClickListener mCollapseExpandCL = new CollapseExpandClickListener();

    public class CollapseExpandClickListener implements View.OnClickListener {

        @Override
        public void onClick(final View view) {

            int headerPosition = mRecyclerView.getChildLayoutPosition(view);
            Titled itm = itemsList.get(headerPosition);
            if (!isDataClass(itm)) {
                //  clear restored state
                mCollapsedHeaders = null;

                Header<T> h = (Header<T>)itm;
                if (h.isCollapsed() ) {
                    // expand
                    List<T> childItemList = h.getChildItemList();
                    if (childItemList != null) {
                        int childListItemCount = childItemList.size();
                        for (int i = 0; i < childListItemCount; i++) {
                            itemsList.add(headerPosition + i + 1, childItemList.get(i));
                        }
                        h.setCollapsed(false);
                        notifyItemRangeInserted(headerPosition + 1, childListItemCount);
                    }
                }
                else{
                    // collapse
                    List<T> childItemList = h.getChildItemList();
                    if (childItemList != null) {
                        int childListItemCount = childItemList.size();
                        for (int i = childListItemCount - 1; i >= 0; i--) {
                            itemsList.remove(headerPosition + i + 1);
                        }
                        h.setCollapsed(true);
                        notifyItemRangeRemoved(headerPosition + 1, childListItemCount);
                    }
                }
            }
        }
    }
}
