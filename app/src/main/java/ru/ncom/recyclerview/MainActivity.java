package ru.ncom.recyclerview;
// Reworked http://www.androidhive.info/2016/01/android-working-with-recycler-view/
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.ncom.recyclerview.adapter.MoviesAdapter;
import ru.ncom.recyclerview.groupingAdapter.TitledViewHolder;
import ru.ncom.recyclerview.model.Movie;
import ru.ncom.recyclerview.model.MovieDb;
import ru.ncom.recyclerview.groupingAdapter.Titled;

public class MainActivity extends AppCompatActivity
                       implements MoviesAdapter.AsyncDbSort.ProgressListener {

    private MovieDb mMovieDb = new MovieDb();
    private RecyclerView mRecyclerView;
    private MoviesAdapter mAdapter;
    private TextView mProgressView;
    final String ASYNCSORT = " Async sort method: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set sort mode spinner
        final CharSequence[] sortModes = new CharSequence[Movie.getOrderByFields().size()+1];
        sortModes[0] = "";
        List<String> movieOrderByFields = Movie.getOrderByFields();
        for (int i=0; i< movieOrderByFields.size(); i++){
            sortModes[i+1] = movieOrderByFields.get(i);
        }

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortModes);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private final static String TAG = "OnItemSelectedListener";

            boolean firstOnItemSelected = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "spinner onItemSelected: pos=" + position +" first call="+firstOnItemSelected);
                // When setting listener, framework calls its onItemSelected() so
                // Spinner's onItemSelected is called twice on screen rotation.
                // Ignore first call.
                if(firstOnItemSelected) {
                    firstOnItemSelected = false;
                    return;
                }
                if (position > 0) {
                    String sortField = (String)parent.getItemAtPosition(position);
                    if (position > 1) {
                        // Use sync sort for GENRE and YEAR
                        mProgressView.setText(" Sync sort method.");
                        mAdapter.orderBy(sortField);
                    } else {
                        // Use async sort for TITLE
                        mAdapter.orderByAsync(sortField, MainActivity.this);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mProgressView = (TextView) findViewById(R.id.orderProgress);

        // Set movie recycler
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mAdapter = new MoviesAdapter(mMovieDb, mRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        // Also set listener for a TextView of the row in adapter / viewholder.
        // First appears Toast from addOnItemTouchListener, then from TextView
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener() {
            private final String TAG = "ClickListener(Main)";

            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "onClick: ");
                // This titles may differ when adapter is not synchronized with db.
                TitledViewHolder mvh = (TitledViewHolder) mRecyclerView.getChildViewHolder(view);
                String viewTitle = (String)mvh.getTitleView().getText();
                Titled movie = mAdapter.getAt(position);
                String dbTitle = movie.getTitle();
                Toast.makeText(getApplicationContext()
                        , (viewTitle == dbTitle)
                                ? dbTitle + " is selected!"
                                : "They are different! \n" + dbTitle + "\n" + viewTitle
                        , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAdapter.onRestoreInstanceState(savedInstanceState);
    }

    // ProgressListener members

    @Override
    public void onStart(String msg) {
        mProgressView.setText(ASYNCSORT+msg);
    }

    @Override
    public void onProgess(String msg) {
        mProgressView.setText(ASYNCSORT+msg);
    }

    @Override
    public void onDone(String msg) {
        mProgressView.setText(ASYNCSORT+msg);
        mAdapter.notifyDataSetChanged();
    }
    //

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private final String TAG = "RecyclerTouchListener";
        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                private final String TAG = "GestureDetector";

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    Log.d(TAG, "onSingleTapUp: ");
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            //RecyclerView.ViewHolder holder = rv.getChildViewHolder(child);
            boolean touchEventDetected = gestureDetector.onTouchEvent(e);
            Log.d(TAG, "onInterceptTouchEvent: child Class="
                    + (child != null ? child.getClass().getName() : "*No child*")
                    + "\n \t, gestureDetector.onTouchEvent(e)=" + touchEventDetected);
            if (child != null && clickListener != null && touchEventDetected) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            Log.d(TAG, "onTouchEvent: ");
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            Log.d(TAG, "onRequestDisallowInterceptTouchEvent: ");
        }
    }

}
/*
***Click not on year
09-04 18:11:07.809 25032-25032/ru.ncom.recyclerview D/RecyclerTouchListener: onInterceptTouchEvent: child Class=android.widget.RelativeLayout
                                                                                       	, gestureDetector.onTouchEvent(e)=false
09-04 18:11:07.909 25032-25032/ru.ncom.recyclerview D/GestureDetector: onSingleTapUp:
09-04 18:11:07.919 25032-25032/ru.ncom.recyclerview D/RecyclerTouchListener: onInterceptTouchEvent: child Class=android.widget.RelativeLayout
                                                                                       	, gestureDetector.onTouchEvent(e)=true
09-04 18:11:07.919 25032-25032/ru.ncom.recyclerview D/ClickListener(Main): onClick:
** click on year
09-04 18:12:43.329 25032-25032/ru.ncom.recyclerview D/RecyclerTouchListener: onInterceptTouchEvent: child Class=android.widget.RelativeLayout
                                                                                       	, gestureDetector.onTouchEvent(e)=false
09-04 18:12:43.469 25032-25032/ru.ncom.recyclerview D/GestureDetector: onSingleTapUp:
09-04 18:12:43.469 25032-25032/ru.ncom.recyclerview D/RecyclerTouchListener: onInterceptTouchEvent: child Class=android.widget.RelativeLayout
                                                                                       	, gestureDetector.onTouchEvent(e)=true
09-04 18:12:43.479 25032-25032/ru.ncom.recyclerview D/ClickListener(Main): onClick:
09-04 18:12:43.719 25032-25032/ru.ncom.recyclerview D/ToastOnClickListener(Adpt): onClick: view Class=android.support.v7.widget.AppCompatTextView
*/