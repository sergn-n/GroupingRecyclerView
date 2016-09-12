package ru.ncom.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.ncom.recyclerview.R;
import ru.ncom.recyclerview.groupingAdapter.TitledViewHolder;

/**
 * Must implement  TitledViewHolder interface
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder
        implements TitledViewHolder {

    public final TextView title;

    public HeaderViewHolder(View view) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
    }

    @Override
    public TextView getTitleView() {
        return title;
    }
}

