package ru.ncom.recyclerview.groupingAdapter;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.ncom.recyclerview.R;

import ru.ncom.recyclerview.adapter.HeaderViewHolder;
import ru.ncom.recyclerview.adapter.MovieViewHolder;
import ru.ncom.recyclerview.adapter.Titled;
import ru.ncom.recyclerview.adapter.TitledViewHolder;


/**
 * Created by Ника-Ком on 11.09.2016.
 */
public  abstract class GroupingAdapter<T extends Titled> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface Db<T> {
        List<T> getDataList();
        List<T> orderBy(Comparator<T> cmp);
        ComparatorGrouper<T> getComparatorGrouper(String orderByFieldName);
    }

    public static abstract class ComparatorGrouper<TC> implements Comparator<TC> {
        final String mCby;

        ComparatorGrouper(String cby){
            mCby = cby;
        }

        abstract String getGroupTitle(TC item);
    }

    private boolean isDataClass(Titled tobj) {
        return mClass.isInstance(tobj);
    }

    public final int DATAROW = 1;
    public final int HEADERROW = 2;
    private final String COLLAPSEDHEADERS = "COLLAPSEDHEADERS";

    private final List<Titled> itemsList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    ArrayList<String> mCollapsedHeaders = null;
    private Class<T> mClass;

    private Db<T> mDb = null;

    public GroupingAdapter(Class<T> clazz, Db<T> db, RecyclerView rv) {
        this.mClass = clazz;
        this.mRecyclerView = rv;
        this.mDb = db;
        List<T> ml = mDb.getDataList();
        // initially it's just source data
        for (int i = 0; i < ml.size(); i++){
            itemsList.add(ml.get(i));
        }
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
        List<T> subml = null;
        Header<T> h = null;
        itemsList.clear();
        for (int i = 0; i < ml.size(); i++) {
            T m = ml.get(i);
            String newTitle = mcb.getGroupTitle(m);
            if ((h==null) || !newTitle.equals(h.getTitle())) {
                h = new Header<T>(newTitle);
                subml = h.getChildItemList();
                itemsList.add(h);
                if (mCollapsedHeaders != null //sort is fired by restoring after screen rotation
                        && mCollapsedHeaders.indexOf(newTitle) >= 0){
                    h.setCollapsed(true);
                }
            }
            if (!h.isCollapsed())
                itemsList.add(m);
            subml.add(m);
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
                collapsedHeaders.add(((Header<T>)itm).getTitle());
        }
        outState.putStringArrayList(COLLAPSEDHEADERS,collapsedHeaders);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        mCollapsedHeaders = savedInstanceState.getStringArrayList(COLLAPSEDHEADERS);
    }

    // **Click listeners**

    //  Collapse / expand group by clicking on header view
    private final View.OnClickListener mCollapseExpandCL = new CollapseExpandClickListener();

    public class CollapseExpandClickListener implements View.OnClickListener {
        private final String TAG = "CollapsExpandCL(Adpt)";

        @Override
        public void onClick(final View view) {

            int headerPosition = mRecyclerView.getChildLayoutPosition(view);
            Titled itm =itemsList.get(headerPosition);
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
