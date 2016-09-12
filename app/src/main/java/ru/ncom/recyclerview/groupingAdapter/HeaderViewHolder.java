package ru.ncom.recyclerview.groupingAdapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.ncom.recyclerview.R;
import ru.ncom.recyclerview.groupingAdapter.TitledViewHolder;

/**
 * Default ViewHolder for a header layout. Knows only title TextView.
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder
        implements TitledViewHolder {

    public final TextView title;

    public HeaderViewHolder(View view, int titleTextViewId) {
        super(view);
        title = (TextView) view.findViewById(titleTextViewId);
    }

    @Override
    public TextView getTitleView() {
        return title;
    }
}

