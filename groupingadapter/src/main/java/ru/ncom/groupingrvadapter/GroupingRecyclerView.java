package ru.ncom.groupingrvadapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by gerg on 26.09.2016.
 */

public class GroupingRecyclerView extends RecyclerView {

    public GroupingRecyclerView(Context context) {
        super(context);
    }

    public GroupingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GroupingRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * .. and sets itself as a provider of view position for the GroupingAdapter
     * @param adapter
     */
    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter instanceof GroupingAdapter) {
            ((GroupingAdapter)adapter).setRecyclerView(this);
        }
        super.setAdapter(adapter);
    }
}
