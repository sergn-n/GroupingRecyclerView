package ru.ncom.groupingrvexample;
// Reworked http://www.androidhive.info/2016/01/android-working-with-recycler-view/

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.Spinner;

import android.widget.Toast;

import java.io.IOException;
import java.util.List;


import ru.ncom.groupingrvadapter.GroupedList;
import ru.ncom.groupingrvadapter.GroupingAdapter;
import ru.ncom.groupingrvadapter.Titled;
import ru.ncom.groupingrvadapter.TitledViewHolder;

import ru.ncom.groupingrvexample.adapter.MovieViewHolder;
import ru.ncom.groupingrvexample.adapter.MoviesAdapter;
import ru.ncom.groupingrvexample.model.Movie;
import ru.ncom.groupingrvexample.model.MovieDb;

// Grouping RecyclerView demo activity : Movies
public class BaseActivity extends AppCompatActivity
        implements MovieDb.AsyncDbSort.ProgressListener,
            DeleteMovieDialogFragment.YesNoListener,
            AddMovieDialogFragment.YesNoListener{

    public static final String REGENERATE = "RE";

    private final String TAG = "Base";

    private MovieDb mMovieDb;
    private GroupedList<Movie> mGroupedMovies;
    private RecyclerView mGroupingRecyclerView;
    private MoviesAdapter mAdapter;

    private Spinner mSortSpinner;
    private final String SORTSPINNERPOS = "SORTSPINNERPOS";
    private int mSortSpinnerSavedPos = -1;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;
    private AdapterView.OnItemSelectedListener mSpinnerOnItemSelectedListener;

    private MenuItem mGoSort;
    private final String ISSORTFINISHED = "ISSORTFINISHED";
    private boolean mIsSortFinished = true;
    private boolean mDataActionInProgress = false;

    // Holds db across activity lifecycle
    //TODO use it to hold the state of controls and tasks instead of bundle?
    private WorkerFragment mWorker;
    public WorkerFragment getWorker() {
        return mWorker;
    }

    final String ASYNCSORT = " Async sort method: ";

    // ** Activity lifecycle members **

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retained fragment holds data and adapter
        FragmentManager fm = getSupportFragmentManager();
        mWorker = (WorkerFragment)fm.findFragmentByTag(WorkerFragment.TAG);
        if (mWorker == null){
            mWorker = new WorkerFragment();
            fm.beginTransaction().add(mWorker, WorkerFragment.TAG).commit();
            if (savedInstanceState == null) {
                // Check intent parameters on first Create and instruct worker
                Bundle b = getIntent().getExtras();
                mWorker.setRegenerate(b != null && b.getInt(REGENERATE) != 0);
            }
        }
        // Inform retained fragment that activity was recreated
        mWorker.setCurrentBaseActivity(this);

        // * for mSortSpinner
        // ** adapter
        final CharSequence[] sortModes = new CharSequence[Movie.getOrderByFields().size()+1];
        sortModes[0] = "";
        List<String> movieOrderByFields = Movie.getOrderByFields();
        for (int i=0; i< movieOrderByFields.size(); i++){
            sortModes[i+1] = movieOrderByFields.get(i);
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        mSpinnerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortModes);
        // Specify the layout to use when the list of choices appears
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // ** listener
        mSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            private final static String TAG = "OnItemSelectedListener";

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "mSortSpinner onItemSelected: (pos)="
                        + ((AppCompatTextView)view).getText() + "("+position+")" );
                Log.d(TAG, "mSortSpinner onItemSelected: adapterSort=" + mAdapter.getSortField() );
                String sortField = (String)parent.getItemAtPosition(position);
                setGoSortEnabled( (position != 0) && !sortField.equals(mAdapter.getSortField()));
                mSortSpinnerSavedPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        // Set movie recycler but its adapter
        mGroupingRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mGroupingRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mGroupingRecyclerView.setLayoutManager(mLayoutManager);
        mGroupingRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mGroupingRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // If a listener is also set for a TextView of the row in adapter / viewholder,
        // Toast from this listener's onItemClick() appears first, then from TextView listener
        mGroupingRecyclerView.addOnItemTouchListener(new OnMovieTouchListener(getApplicationContext(), mGroupingRecyclerView
                , new OnMovieTouchListener.GestureListener() {
            private final static String TAG = "GestureListener(Main)";

            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: ");
                // This titles may differ when:
                // 1) adapter is not synchronized with rv when doing async sort;
                // 2) GroupingAdapter#onBindViewHolder adds number of items in group to the title.
                TitledViewHolder tvh = (TitledViewHolder) mGroupingRecyclerView.getChildViewHolder(view);
                String viewTitle = (String)tvh.getTitleView().getText();
                Titled movie = mAdapter.getAt(position);
                String adapterTitle = movie.getTitle();
                Toast.makeText(getApplicationContext()
                        , (viewTitle.equals(adapterTitle))
                                ? adapterTitle + " is selected!"
                                : "Adapter and view titles are different: \n" + adapterTitle + "\n" + viewTitle
                        , Toast.LENGTH_SHORT).show();
                // delete movie (for emulator with no gesture emulation)
                showDeleteDialog(view, position);

            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.d(TAG, "onItemLongClick: at pos=" + position + ": " + mAdapter.getAt(position).getTitle());
                // add movie (for emulator with no gesture emulation)
                showMovieDialog(position);
            }

            @Override
            public void onItemZoomIn(View view, int position) {
                Log.d(TAG, "onItemZoomIn: at pos=" + position + ": " + mAdapter.getAt(position).getTitle());
                showMovieDialog(position);
            }

            @Override
            public void onItemZoomOut(View view, int position) {
                Log.d(TAG, "onItemZoomOut: at pos=" + position + ": " + mAdapter.getAt(position).getTitle());
                showDeleteDialog(view, position);
            }

            void showMovieDialog(int position) {
                if ((!mDataActionInProgress)
                        && (mIsSortFinished) // rv is sync. with adapter
                        && mAdapter.getItemViewType(position) == GroupingAdapter.DATAROW) {
                    mDataActionInProgress = true;
                    Log.d(TAG, "Creating Movie dialog..");
                    (new AddMovieDialogFragment())
                            .show(getSupportFragmentManager(), "tagAddMovie");
                }
            }

            void showDeleteDialog(View view, int position){
                if ((!mDataActionInProgress)
                        && (mIsSortFinished) // rv is sync. with adapter
                        && mAdapter.getItemViewType(position) == GroupingAdapter.DATAROW) {
                    mDataActionInProgress = true;
                    Log.d(TAG, "Creating delete dialog..");
                    Movie m = (Movie)mAdapter.getAt(position);
                    DeleteMovieDialogFragment.createInstance(m,
                            // when sorted, ask delete group option
                            null != mGroupedMovies.getSortFieldName()
                                ? mGroupedMovies.getComparatorGrouper().getGroupTitle(m)
                                : null)
                            .show(getSupportFragmentManager(), "tagDeleteMovie");
                }
            }
        }));

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        // Fragment's onCreate() is executed, ready to get adapter
        mMovieDb = mWorker.getMovieDb();
        mAdapter = mWorker.getMoviesAdapter();
        mGroupedMovies = mWorker.getmGroupedMovies();
        // Link newly created mGroupingRecyclerView and retaining mAdapter
        mGroupingRecyclerView.setAdapter(mAdapter);
        if (mGroupedMovies.size() == 0) {
//   -- Test addAll() on sorted list at first run
//            mGroupedMovies.sort(Movie.getOrderByFields().get(1));
//            // do not add it to db, add to mGroupedMovies only
//            mGroupedMovies.add(new Movie("Start M", "Fiction", "1951"));
            mGroupedMovies.addAll(mMovieDb.getDataList());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mGoSort = menu.findItem(R.id.go_button);
        mGoSort.setEnabled(false); // default

        // Set sort mode spinner
        MenuItem menuItem = menu.findItem(R.id.spinner);
        mSortSpinner = (Spinner)MenuItemCompat.getActionView(menuItem);
        mSortSpinner.setAdapter(mSpinnerAdapter);
        mSortSpinner.setOnItemSelectedListener(mSpinnerOnItemSelectedListener);
        if (mSortSpinnerSavedPos >= 0) { // activity was restored
            mSortSpinner.setSelection(mSortSpinnerSavedPos);
            // Enable mGoSort?
            String sortField = (String)mSortSpinner.getItemAtPosition(mSortSpinnerSavedPos);
            mGoSort.setEnabled(mSortSpinnerSavedPos>0 && !sortField.equals(mAdapter.getSortField()));
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Needn't it in the retaining adapter version
        //mAdapter.onSaveInstanceState(outState);
        outState.putInt(SORTSPINNERPOS, mSortSpinnerSavedPos);
        outState.putBoolean(ISSORTFINISHED, mIsSortFinished);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Needn't it in the retaining adapter version
        //mAdapter.onRestoreInstanceState(savedInstanceState);
        mSortSpinnerSavedPos = savedInstanceState.getInt(SORTSPINNERPOS);
        mIsSortFinished = savedInstanceState.getBoolean(ISSORTFINISHED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        // prevent activity leaking
        mGroupingRecyclerView.setAdapter(null);
    }

    // ** DeleteMovieDialogFragment.YesNoListener members **

    @Override
    public void onDeleteYes(Movie m, int delOption) {
        if (delOption == 0) {
            Log.d(TAG, "!! Gonna delete Movie =" + m.getTitle());
            try {
                mMovieDb.delete(m);
                mGroupedMovies.remove(m);
                Log.d(TAG, "Deleted Movie =" + m.getTitle());
            } catch (IOException e) {
                Log.e(TAG, "!!FAILED to delete Movie =" + m.getTitle(), e);
            }
        }
        else{
            String gtitle = mGroupedMovies.getComparatorGrouper().getGroupTitle(m);
            Log.d(TAG, "!! Gonna delete Group =" + gtitle);
            // do not delete from db
            mGroupedMovies.removeGroupByTitle(gtitle);
        }
    }

    @Override
    public void onDeleteDismiss() {
        // dismissed  by CANCEL/ OK + exec action / click outside the dialog
        Log.d(TAG, "Delete Dialog was dismissed by user action on it.");
        mDataActionInProgress = false;
    }

    // ** AddMovieDialogFragment.YesNoListener members **

    @Override
    public void onAddYes(String title, String genre, String year) {
        Movie m = new Movie(title, genre, year);
        try {
            mMovieDb.add(m);
            mGroupedMovies.add(m);
        } catch (IOException e) {
            Log.e(TAG, "!!FAILED to add movie : " + title, e);
        }
    }

    @Override
    public void onAddDismiss() {
        // dismissed  by CANCEL/ OK + exec action / click outside the dialog
        Log.d(TAG, "Movie Dialog was dismissed by user action on it.");
        mDataActionInProgress = false;
    }

    // ** MoviesAdapter.AsyncDbSort.ProgressListener members **

    @Override
    public MovieDb.AsyncDbSort.ProgressListener getCurrentInstance() {
        // mWorker should know it see onCreate()
        return mWorker.getCurrentBaseActivity();
    }

    @Override
    public void onAsyncSortStart(String msg) {
        mIsSortFinished = false;
        setGoSortEnabled(false);
        Toast.makeText(getApplicationContext(),ASYNCSORT+msg,Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onAsyncSortProgress(String msg) {
        Toast.makeText(getApplicationContext(),ASYNCSORT+msg,Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onAsyncSortDone(GroupedList<Movie> glm) {
        mIsSortFinished = true;
        Log.d(TAG, "onAsyncSortDone: ");
        Toast.makeText(getApplicationContext(),ASYNCSORT+"Done",Toast.LENGTH_SHORT)
                .show();
        mAdapter.onDataSorted(glm);
        if (mSortSpinner.getSelectedItemPosition() >0 ) {
            String sortField = (String) mSortSpinner.getSelectedItem();
            setGoSortEnabled(!sortField.equals(glm.getSortFieldName()));
        }
    }

    // ** XML menu android:onClick= **

    public void moreButtonClicked(MenuItem itm) throws IOException {
        // Direct db update requires total adapter reload.
        List<Movie> newItems = mMovieDb.cloneData(1);
        mGroupedMovies.addAll(newItems);
    }

    public void goButtonClicked(MenuItem itm) {
        int position = mSortSpinner.getSelectedItemPosition();
        if (position > 0) {
            String sortField = (String) mSortSpinner.getSelectedItem();
            setGoSortEnabled(false);
            if (position > 1) {
                // Use sync sort for GENRE and YEAR
                Toast.makeText(getApplicationContext()," Sync sort method.",Toast.LENGTH_SHORT)
                        .show();
                mGroupedMovies.sort(sortField);
            } else {
                // Use async sort for TITLE
                new MovieDb.AsyncDbSort(mGroupedMovies, BaseActivity.this).execute(sortField);
            }
        }
    }

    // ** Helper methods **

    /**
     *  setEnabled(enabled & mIsSortFinished) and apply colorFilter accordingly
     * @param enabled
     */
    private void setGoSortEnabled(boolean enabled) {
        Log.d(TAG, "setGoSortEnabled: enabled="+enabled+", mIsSortFinished=" + mIsSortFinished);
        boolean effective = enabled & mIsSortFinished;
        mGoSort.setEnabled(effective);
        if (effective)
            mGoSort.getIcon().setColorFilter(null);
        else
            mGoSort.getIcon().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

}
