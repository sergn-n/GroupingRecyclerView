package ru.ncom.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class MovieViewHolder extends RecyclerView.ViewHolder
        implements TitledViewHolder {
    public TextView title, year, genre;

    @Override
    public TextView getTitleView() {
        return title;
    }

    public MovieViewHolder(View view, View.OnClickListener onClickListener) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
        genre = (TextView) view.findViewById(R.id.genre);
        year = (TextView) view.findViewById(R.id.year);
        // genre and year are declared clickable in XML;
        year.setOnClickListener(onClickListener);
    }
}

