package ru.ncom.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.ncom.recyclerview.R;

/**
 * Created by gerg on 08.09.2016.
 */
public class HeaderViewHolder extends RecyclerView.ViewHolder
        implements TitledViewHolder {

    public TextView title;

    public HeaderViewHolder(View view) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
    }

    @Override
    public TextView getTitleView() {
        return title;
    }
}

