package ru.ncom.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.ncom.recyclerview.R;
import ru.ncom.recyclerview.model.Movie;
import ru.ncom.recyclerview.model.Titled;

public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int MOVIEROW = 1;
    public final int HEADERROW = 2;

    private List<Titled> moviesList;
    private RecyclerView mRecyclerView;

    public MoviesAdapter(List<Titled> moviesList, RecyclerView rv) {

        this.mRecyclerView = rv;
        this.moviesList = moviesList;
    }

    public class ToastOnClickListener implements View.OnClickListener {
        private final String TAG = "ToastOnClickLstnr(Adpt)";
        @Override
        public void onClick(final View view) {
            String item = null;
            Log.d(TAG, "onClick: view Class=" + view.getClass().getName());
            if (view instanceof RelativeLayout) {
                int itemPosition = mRecyclerView.getChildLayoutPosition(view);
                item = "**"+moviesList.get(itemPosition).getTitle();
            } else if (view instanceof TextView) {
                item = ((TextView)view).getText().toString();
            }
            Toast.makeText(mRecyclerView.getContext(), item, Toast.LENGTH_LONG).show();
        }
    }

    private final View.OnClickListener mOnClickListener = new ToastOnClickListener();

    @Override
    public int getItemViewType(int position) {
        if (moviesList.get(position) instanceof Movie)
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
        Titled item = moviesList.get(position);
        if ((item instanceof Movie) && (holder instanceof MovieViewHolder)) {
            MovieViewHolder vh = (MovieViewHolder)holder;
            Movie m =  (Movie) item;
            vh.genre.setText(m.getGenre());
            vh.year.setText(m.getYear());
        }
        ((TitledViewHolder) holder).getTitleView().setText(item.getTitle());    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
