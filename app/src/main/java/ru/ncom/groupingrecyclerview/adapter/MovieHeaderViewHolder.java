package ru.ncom.groupingrecyclerview.adapter;

import android.view.View;
import android.widget.ImageView;

import ru.ncom.groupingrvadapter.HeaderViewHolder;
import ru.ncom.groupingrecyclerview.R;

/**
 * Created by gerg on 28.09.2016.
 */

class MovieHeaderViewHolder extends HeaderViewHolder {
    public ImageView expandedIcon;

    public MovieHeaderViewHolder(View view, int titleTextViewId) {
        super(view, titleTextViewId);
        expandedIcon = (ImageView)view.findViewById(R.id.arrow_expand_imageview);
    }
}
