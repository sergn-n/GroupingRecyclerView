package ru.ncom.groupingrvexample.adapter;

import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import ru.ncom.groupingrvadapter.Header;
import ru.ncom.groupingrvadapter.HeaderViewHolder;
import ru.ncom.groupingrvexample.R;

/**
 * Created by gerg on 28.09.2016.
 */

class MovieHeaderViewHolder extends HeaderViewHolder {
    public ImageView expandedIcon;
    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = -90f;

    /**
     *  + collapsed/expaned indicator, {@code R.id.arrow_expand_imageview}
     * @param view
     * @param titleTextViewId
     */
    public MovieHeaderViewHolder(View view, int titleTextViewId) {
        super(view, titleTextViewId);
        expandedIcon = (ImageView)view.findViewById(R.id.arrow_expand_imageview);
    }

    /**
     * Sets collapsed/expaned indicator.
     * @param h
     */
    public void bind(Header h){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (h.isCollapsed()) {
                expandedIcon.setRotation(ROTATED_POSITION);
            } else {
                expandedIcon.setRotation(INITIAL_POSITION);
            }
        }
    }
}
