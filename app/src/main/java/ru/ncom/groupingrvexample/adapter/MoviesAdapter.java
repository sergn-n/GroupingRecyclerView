package ru.ncom.groupingrvexample.adapter;


import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.ncom.groupingrvadapter.ComparatorGrouper;
import ru.ncom.groupingrvadapter.GroupingAdapter;
import ru.ncom.groupingrvadapter.Header;
import ru.ncom.groupingrvadapter.Titled;
import ru.ncom.groupingrvexample.R;

import ru.ncom.groupingrvexample.model.Movie;
import ru.ncom.groupingrvexample.model.MovieDb;

/**
 * Example of grouping adapter for Movie class
 */
public class MoviesAdapter extends GroupingAdapter<Movie> {

    private final String TAG = "MoviesAdapter";

    public MoviesAdapter() {
        super(Movie.class);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case DATAROW:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_row, parent, false);
                // My datarow ViewHolder sets listener on some childs
                return new MovieViewHolder(itemView, mToastClickListener);
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.header_row, parent, false);
                return new MovieHeaderViewHolder(itemView, R.id.title);
            //return createHeaderViewHolder(R.layout.header_row, R.id.title, parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Default binding for title TextView
        super.onBindViewHolder(holder, position);
        // My binding for Data
        Titled item = getAt(position);
        switch (holder.getItemViewType()){
            case DATAROW :
                MovieViewHolder vh = (MovieViewHolder)holder;
                Movie m = (Movie)item;
                vh.genre.setText(m.getGenre());
                vh.year.setText(m.getYear());
                break;
            default: // HEADERROW
                MovieHeaderViewHolder vhh = (MovieHeaderViewHolder)holder;
                vhh.bind((Header)item);
        }
    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView rv)
    {
        Log.d(TAG, "RecyclerView #"+rv.hashCode());
        super.onAttachedToRecyclerView(rv);
        // A place to create custom listeners
        mToastClickListener = new ToastOnClickListener(rv);
    }


    // Demo listener, is applied to childs of Data row

    private View.OnClickListener mToastClickListener;

    public static class ToastOnClickListener implements View.OnClickListener {
        private final String TAG = "ToastCL(Adpt)";
        private RecyclerView mRecyclerView;

        private ToastOnClickListener(RecyclerView recycler)
        {
            mRecyclerView = recycler;
        }

        @Override
        public void onClick(final View view) {
            String itemText = null;
            Log.d(TAG, "onClick: view Class=" + view.getClass().getName());
            if (view instanceof RelativeLayout) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                itemText = "**"+ ((MoviesAdapter)mRecyclerView.getAdapter()).getAt(itemPosition).getTitle();
            } else if (view instanceof TextView) {
                itemText = ((TextView)view).getText().toString();
            }
            Toast.makeText(mRecyclerView.getContext(), itemText, Toast.LENGTH_LONG).show();
            // selection test

        }
    }

    @Override
    public ComparatorGrouper<Movie> getComparatorGrouper(String orderByFieldName) {
        return Movie.getComparatorGrouper(orderByFieldName);
    }
}
