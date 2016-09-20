package ru.ncom.groupingrecyclerview;
// Reworked http://www.androidhive.info/2016/01/android-working-with-recycler-view/

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ru.ncom.groupingrvadapter.Selectable;
import ru.ncom.groupingrvadapter.SimpleRecyclerTouchListener;
import ru.ncom.groupingrvadapter.Titled;
import ru.ncom.groupingrvadapter.TitledViewHolder;

import ru.ncom.groupingrecyclerview.adapter.MoviesAdapter;
import ru.ncom.groupingrecyclerview.model.Movie;
import ru.ncom.groupingrecyclerview.model.MovieDb;

// GroupingRecyclerView demo activity : Movies
public class MainActivity extends AppCompatActivity
                       implements MoviesAdapter.AsyncDbSort.ProgressListener {

    private final String TAG = "Main";
    private MovieDb mMovieDb;
    private RecyclerView mRecyclerView;
    private Spinner mSortSpinner;
    private MoviesAdapter mAdapter;
    private TextView mProgressView;
    private Button mGoSort;
    private AdapterView.OnItemSelectedListener mSpinnerOnItemSelectedListener;

    final String ASYNCSORT = " Async sort method: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMovieDb = new MovieDb(getApplicationContext());
        // Set sort mode spinner
        final CharSequence[] sortModes = new CharSequence[Movie.getOrderByFields().size()+1];
        sortModes[0] = "";
        List<String> movieOrderByFields = Movie.getOrderByFields();
        for (int i=0; i< movieOrderByFields.size(); i++){
            sortModes[i+1] = movieOrderByFields.get(i);
        }

        mSortSpinner = (Spinner) findViewById(R.id.spinner);
        mGoSort = (Button)findViewById(R.id.go_button);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> spinnerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortModes);
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortSpinner.setAdapter(spinnerAdapter);
        mSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            private final static String TAG = "OnItemSelectedListener";

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "mSortSpinner onItemSelected: pos=" + position );
                Log.d(TAG, "mSortSpinner onItemSelected: adapterSort=" + mAdapter.getSortField() );
                String sortField = (String)parent.getItemAtPosition(position);
                mGoSort.setEnabled( (position != 0) && !sortField.equals(mAdapter.getSortField()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        if (savedInstanceState == null) {
            mGoSort.setEnabled(false);
            mSortSpinner.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);
        }

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
        // If a listener is also set for a TextView of the row in adapter / viewholder,
        // first  Toast from SimpleRecyclerTouchListener appears, then from TextView
        mRecyclerView.addOnItemTouchListener(new SimpleRecyclerTouchListener(getApplicationContext(), mRecyclerView
                , new SimpleRecyclerTouchListener.ClickListener() {
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
                        , (viewTitle.equals(dbTitle))
                                ? dbTitle + " is selected!"
                                : "They are different! \n" + dbTitle + "\n" + viewTitle
                        , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.d(TAG, "onLongClick: at pos=" + position + ": " + mAdapter.getAt(position).getTitle());
                view.setSelected(true);
                Titled item = mAdapter.getAt(position);
                if (item instanceof Selectable)
                    ((Selectable)item).setSelected(true);
            }
        }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        // Save data at screen rotation. Data will be restored in OnCreate().
        try {
            mMovieDb.save();
        }
        catch (Exception e){
            Log.e(TAG, "onStop: save() failed ",e);
        }
    }

    public void moreButtonClicked(View v) {
        mMovieDb.cloneData(1);
        mAdapter.reload();
    }

    public void goButtonClicked(View v) {
        int position = mSortSpinner.getSelectedItemPosition();
        if (position > 0) {
            String sortField = (String) mSortSpinner.getSelectedItem();
            mGoSort.setEnabled(false);

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAdapter.onRestoreInstanceState(savedInstanceState);
        // Must restore mAdapter state before it
        mSortSpinner.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);
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
}
