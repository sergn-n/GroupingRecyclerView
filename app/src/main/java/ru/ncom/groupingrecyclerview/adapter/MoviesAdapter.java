package ru.ncom.groupingrecyclerview.adapter;


import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.ncom.groupingrvadapter.GroupingAdapter;
import ru.ncom.groupingrvadapter.Header;
import ru.ncom.groupingrvadapter.HeaderViewHolder;
import ru.ncom.groupingrvadapter.Selectable;
import ru.ncom.groupingrvadapter.Titled;
import ru.ncom.groupingrecyclerview.R;

import ru.ncom.groupingrecyclerview.model.Movie;
import ru.ncom.groupingrecyclerview.model.MovieDb;

/**
 * Example of grouping adapter for Movie class
 */
public class MoviesAdapter extends GroupingAdapter<Movie> {

    private final String TAG = "MoviesAdapter";
    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = -90f;

    public MoviesAdapter(MovieDb db) {
        super(Movie.class, db);
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
                Header itm = (Header)item;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    if (itm.isCollapsed()) {
                        vhh.expandedIcon.setRotation(ROTATED_POSITION);
                    } else {
                        vhh.expandedIcon.setRotation(INITIAL_POSITION);
                }
            }
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

    // Simulate asynch operation: ordering using protected doOrder()
    /**
     * @param sortField
     * @param progressView
     */
    public void orderByAsync (String sortField, AsyncDbSort.ProgressListener progressView) {
        (new AsyncDbSort(this, progressView)).execute(sortField);
    }

    public static class AsyncDbSort extends AsyncTask<String,String,String> {

        public interface ProgressListener{
            /** After config change old instance must know current instance
             * @return
             */
            ProgressListener getCurrentInstance();
            void onStart(String msg);
            void onProgess(String msg);
            void onDone(String msg);
        }

        MoviesAdapter ma;
        ProgressListener progressListener;

        public AsyncDbSort(MoviesAdapter ma, ProgressListener progressListener ){
            this.ma = ma;
            this.progressListener = progressListener;
        }

        @Override
        protected String doInBackground(String... params) {
            // If config change occurs here, it' OK, persistent adapter version
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {
                //intentionally empty
            }
            //
            ma.doOrder(params[0]);
            publishProgress ("Sorted, notifying...");
            try {
                Thread.sleep(7000);
            }
            catch (InterruptedException e) {
                //intentionally empty
            }
            return "Done.";
        }

        // Deliver events to current instance of activity
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressListener.getCurrentInstance().onStart("Gonna sort it in a while...\n"
                    +"OK to change config here.");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            // old instace really will do too, as listener only  shows a Toast
            super.onProgressUpdate(values);
            progressListener.getCurrentInstance().onProgess(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressListener.getCurrentInstance().onDone(s);
        }

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
}
