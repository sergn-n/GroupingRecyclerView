package ru.ncom.groupingrecyclerview.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.ncom.groupingrvadapter.GroupingAdapter;
import ru.ncom.groupingrvadapter.Titled;
import ru.ncom.groupingrecyclerview.R;

import ru.ncom.groupingrecyclerview.model.Movie;
import ru.ncom.groupingrecyclerview.model.MovieDb;

/**
 * Example of grouping adapter for Movie class
 */
public class MoviesAdapter extends GroupingAdapter<Movie> {

    private final String TAG = "MoviesAdapter";
    private Context mContext;

    public MoviesAdapter(MovieDb db, Context ctx) {
        super(Movie.class, db);
        mContext = ctx;
        Log.d(TAG, "Constructor: #" + this.hashCode());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case DATAROW:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_row, parent, false);
                //itemView.setOnClickListener(mToastClickListener);
                return new MovieViewHolder(itemView, mToastClickListener);
            default:
                return createHeaderViewHolder(R.layout.header_row, R.id.title, parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Titled item = getAt(position);
        // Default binding for Header
        bindTitleView(holder, position);
        // My binding for Data
        if ((item instanceof Movie) && (holder instanceof MovieViewHolder)) {
            MovieViewHolder vh = (MovieViewHolder)holder;
            Movie m = (Movie)item;
            vh.genre.setText(m.getGenre());
            vh.year.setText(m.getYear());
        }
    }

    // Optional:

    // **Ordering**

    // * Default ordering (synchronous)  - nothing to do
    /*
    @Override
    public void orderBy(String sortField) {
        Log.d(TAG, "orderBy: " + sortField);
        super.orderBy(sortField);
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        Log.d(TAG, "onSaveInstanceState: ");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        Log.d(TAG, "onRestoreInstanceState: ");
        super.onRestoreInstanceState(savedInstanceState);
    }
    */

    // * Test asynch ordering using protected doOrder()
    //
    //

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

    // **Click listeners**
    
    //  Collapse / expand group with a click on header view is already implemented in super

    // Demo Listener, is applied to childs of Data row

    public void setRecyclerView(RecyclerView rv)
    {
        Log.d(TAG, "setRecyclerView: rv=#"+rv.hashCode());
        super.setRecyclerView(rv);
        mToastClickListener = new ToastOnClickListener(rv);
    }

    private View.OnClickListener mToastClickListener;

    public class ToastOnClickListener implements View.OnClickListener {
        private final String TAG = "ToastCL(Adpt)";
        private RecyclerView mRecyclerView;

        public ToastOnClickListener(RecyclerView recycler)
        {
            mRecyclerView = recycler;
        }

        @Override
        public void onClick(final View view) {
            String itemText = null;
            Log.d(TAG, "onClick: view Class=" + view.getClass().getName());
            if (view instanceof RelativeLayout) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                itemText = "**"+getAt(itemPosition).getTitle();
            } else if (view instanceof TextView) {
                itemText = ((TextView)view).getText().toString();
            }
            Toast.makeText(mContext, itemText, Toast.LENGTH_LONG).show();
            // selection test

        }
    }
}
