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

import java.util.ArrayList;
import java.util.List;

import ru.ncom.recyclerview.R;
import ru.ncom.recyclerview.model.Movie;
import ru.ncom.recyclerview.model.MovieDb;
import ru.ncom.recyclerview.model.Titled;

public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int MOVIEROW = 1;
    public final int HEADERROW = 2;
    // Represantation

    private final List<Titled> itemsList = new ArrayList<>();
    private MovieDb mDb = null;
    private RecyclerView mRecyclerView;

    public MoviesAdapter(MovieDb db, RecyclerView rv) {

        this.mRecyclerView = rv;
        this.mDb = db;
        List<Movie> ml = mDb.getMovieList();
        // initially it's just source data
        for (int i = 0; i <ml.size(); i++){
            itemsList.add(ml.get(i));
        }
    }

    public Titled getAt(int position) {
        return itemsList.get(position);
    }

    public void orderBy(Movie.ComparatorBy.CompareBy sortField) {
        doOrder(sortField);
        notifyDataSetChanged();
    }

    private void doOrder(Movie.ComparatorBy.CompareBy sortField) {
        Movie.ComparatorBy mcb = new Movie.ComparatorBy(sortField);
        List<Movie> ml = mDb.orderBy(mcb);
        List<Movie> subml = null;
        String oldTitle = null;
        itemsList.clear();
        for (int i = 0; i < ml.size(); i++) {
            Movie m = ml.get(i);
            String newTitle = mcb.getGroup(m);
            if (!newTitle.equals(oldTitle)) {
                Header h = new Header(newTitle);
                subml = h.getMovieList();
                itemsList.add(h);
                oldTitle = newTitle;
            }
            itemsList.add(m);
            subml.add(m);
        }
    }

    public void orderByAsync (Movie.ComparatorBy.CompareBy sortField, AsyncDbSort.ProgressListener progressView) {
        (new AsyncDbSort(this, progressView)).execute(sortField);

    }


    public static class AsyncDbSort extends AsyncTask<Movie.ComparatorBy.CompareBy,String,String> {

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
        protected String doInBackground(Movie.ComparatorBy.CompareBy... params) {
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e) {

            }
            ma.doOrder(params[0]);
            publishProgress ("Sorted, notifying...");
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {

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

    public class ToastOnClickListener implements View.OnClickListener {
        private final String TAG = "ToastOnClickLstnr(Adpt)";
        @Override
        public void onClick(final View view) {
            String item = null;
            Log.d(TAG, "onClick: view Class=" + view.getClass().getName());
            if (view instanceof RelativeLayout) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                item = "**"+itemsList.get(itemPosition).getTitle();
            } else if (view instanceof TextView) {
                item = ((TextView)view).getText().toString();
            }
            Toast.makeText(mRecyclerView.getContext(), item, Toast.LENGTH_LONG).show();
        }
    }

    private final View.OnClickListener mOnClickListener = new ToastOnClickListener();

    @Override
    public int getItemViewType(int position) {
        if (itemsList.get(position) instanceof Movie)
            return MOVIEROW;
        return HEADERROW;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case MOVIEROW:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.movie_row, parent, false);
                //itemView.setOnClickListener(mOnClickListener);
                return new MovieViewHolder(itemView, mOnClickListener);
            default:
                itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_row, parent, false);
                return new HeaderViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Titled item = itemsList.get(position);
        if ((item instanceof Movie) && (holder instanceof MovieViewHolder)) {
            MovieViewHolder vh = (MovieViewHolder)holder;
            Movie m =  (Movie) item;
            vh.genre.setText(m.getGenre());
            vh.year.setText(m.getYear());
        }
        ((TitledViewHolder) holder).getTitleView().setText(item.getTitle());    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }
}
