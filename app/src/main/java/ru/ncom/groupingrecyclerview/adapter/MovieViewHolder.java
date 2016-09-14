package ru.ncom.groupingrecyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.ncom.groupingrecyclerview.R;
import ru.ncom.groupingrvadapter.TitledViewHolder;

public class MovieViewHolder extends RecyclerView.ViewHolder
        implements TitledViewHolder {

    public TextView title, year, genre;

    public MovieViewHolder(View view, View.OnClickListener onClickListener) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
        genre = (TextView) view.findViewById(R.id.genre);
        year = (TextView) view.findViewById(R.id.year);
        // genre and year are declared clickable in XML;
        year.setOnClickListener(onClickListener);
    }

    @Override
    public TextView getTitleView() {
        return title;
    }
}

