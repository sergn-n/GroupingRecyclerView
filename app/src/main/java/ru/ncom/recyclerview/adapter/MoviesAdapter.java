package ru.ncom.recyclerview.adapter;

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
import ru.ncom.recyclerview.R;

import ru.ncom.recyclerview.model.Movie;
import ru.ncom.recyclerview.model.MovieDb;

/**
 * Example of grouping adapter for Movie class
 */
public class MoviesAdapter extends GroupingAdapter<Movie> {

    private final String TAG = "MoviesAdapter";
    private final RecyclerView mRecyclerView;

    public MoviesAdapter(MovieDb db, RecyclerView rv) {
        super(Movie.class, db, rv);
        mRecyclerView = rv;
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
        // My binding for Data
        if ((item instanceof Movie) && (holder instanceof MovieViewHolder)) {
            MovieViewHolder vh = (MovieViewHolder)holder;
            Movie m = (Movie)item;
            vh.genre.setText(m.getGenre());
            vh.year.setText(m.getYear());
        }
        // Default binding for Header
        BindTitleView(holder, position);
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
     * NOTE it has no effect when screen is rotated before onPostExecute!
     * AsyncTask will communicate with "old" MainActivity instance!
     * @param sortField
     * @param progressView
     */
    public void orderByAsync (String sortField, AsyncDbSort.ProgressListener progressView) {
        (new AsyncDbSort(this, progressView)).execute(sortField);
    }

    public static class AsyncDbSort extends AsyncTask<String,String,String> {

        public interface ProgressListener{
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
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                //intentionally empty
            }
            return "Done.";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressListener.onStart("Starting sort...");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressListener.onProgess(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressListener.onDone(s);
        }

    }

    // **Click listeners**
    
    //  Collapse / expand group with a click on header view is already implemented in super

    // Demo Listener, is applied to childs of Data row

    private final View.OnClickListener mToastClickListener = new ToastOnClickListener();

    public class ToastOnClickListener implements View.OnClickListener {
        private final String TAG = "ToastCL(Adpt)";
        @Override
        public void onClick(final View view) {
            String item = null;
            Log.d(TAG, "onClick: view Class=" + view.getClass().getName());
            if (view instanceof RelativeLayout) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                item = "**"+getAt(itemPosition).getTitle();
            } else if (view instanceof TextView) {
                item = ((TextView)view).getText().toString();
            }
            Toast.makeText(mRecyclerView.getContext(), item, Toast.LENGTH_LONG).show();
        }
    }
}
