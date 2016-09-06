package ru.ncom.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    private List<Movie> moviesList;
    // (1)
    private RecyclerView  mRecyclerView;
    //
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            genre = (TextView) view.findViewById(R.id.genre);
            year = (TextView) view.findViewById(R.id.year);
            // genre and year are declared clickable in XML;
            year.setOnClickListener(mOnClickListener);
        }
    }


    public MoviesAdapter(List<Movie> moviesList, RecyclerView rv) {
        // (1)
        this.mRecyclerView = rv;
        this.moviesList = moviesList;
        //
    }

    // (1)
    public class MyOnClickListener implements View.OnClickListener {
        private final String TAG = "MyOnClickListener(Adpt)";
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

    private final View.OnClickListener mOnClickListener = new MyOnClickListener();
    //
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_row, parent, false);
        //(1)
        // itemView.setOnClickListener(mOnClickListener);
        //
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Movie movie = moviesList.get(position);
        holder.title.setText(movie.getTitle());
        holder.genre.setText(movie.getGenre());
        holder.year.setText(movie.getYear());
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
