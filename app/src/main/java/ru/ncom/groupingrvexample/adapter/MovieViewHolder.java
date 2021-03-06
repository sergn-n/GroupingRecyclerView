package ru.ncom.groupingrvexample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.ncom.groupingrvexample.R;
import ru.ncom.groupingrvadapter.TitledViewHolder;
import ru.ncom.groupingrvexample.model.Movie;

public class MovieViewHolder extends RecyclerView.ViewHolder
        implements TitledViewHolder {

    public TextView title, year, genre;

    public MovieViewHolder(View view, View.OnClickListener onClickListener) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
        genre = (TextView) view.findViewById(R.id.genre);
        year = (TextView) view.findViewById(R.id.year);
        // genre and year are declared clickable in XML;
        // The listener is called on long click too!
        year.setOnClickListener(onClickListener);
    }

    public void bind (Movie m){
        title.setText(m.getTitle());
        genre.setText(m.getGenre());
        year.setText(m.getYear());
    }

    @Override
    public TextView getTitleView() {
        return title;
    }
}

