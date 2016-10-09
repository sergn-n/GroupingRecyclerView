package ru.ncom.groupingrvexample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import ru.ncom.groupingrvadapter.Selectable;
import ru.ncom.groupingrvadapter.SimpleRecyclerTouchListener;
import ru.ncom.groupingrvadapter.Titled;
import ru.ncom.groupingrvadapter.GetterAtPosition;

/**
 * Created by Ника-Ком on 09.10.2016.
 */

public class OnMovieTouchListener implements RecyclerView.OnItemTouchListener {

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);

        void onZoomIn(View view, int position);

        void onZoomOut(View view, int position);
    }

    private static final String TAG ="OnMovieTouchListener";
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private OnMovieTouchListener.ClickListener clickListener;

    public OnMovieTouchListener(Context context, final RecyclerView recyclerView, final OnMovieTouchListener.ClickListener clickListener) {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true; // consumed
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null){
                    boolean newState = !child.isSelected();
                    int position = recyclerView.getChildLayoutPosition(child);
                    child.setSelected(newState);
                    // If adapter can return underlying Selectable object, toggle the objects selected state
                    if (recyclerView.getAdapter() instanceof GetterAtPosition) {
                        Object item = ((GetterAtPosition)recyclerView.getAdapter()).getAt(position);
                        if (item instanceof Selectable)
                            ((Selectable)item).setSelected(newState);
                    }

                    if(clickListener != null) {
                        clickListener.onLongClick(child, position);
                    }
                }
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                double scaleFactor = detector.getScaleFactor();
                Log.d(TAG, "Scale gesture detected");

                View child = recyclerView.findChildViewUnder(detector.getFocusX(), detector.getFocusY());
                int position = (child != null) ? recyclerView.getChildLayoutPosition(child) : -1;
                if (1.0f > scaleFactor) {
                    Log.d("ScaleFactor", "Pinch ");
                    if (child != null) {
                        clickListener.onZoomOut(child, position);
                    }
                } else {
                    Log.d("onScaleEnd", "Zoom ");
                    if (child != null) {
                        clickListener.onZoomIn(child, position);
                    }
                }
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        // Looks like GestureDetector#onTouchEvent() makes GestureDetector keep
        // listening to touch-related events till the gesture ends or its override method consumes event.
        boolean eventConsumed = gestureDetector.onTouchEvent(e);
        Log.d(TAG, "onInterceptTouchEvent: gestureDetector . eventConsumed=" + eventConsumed);
        if (child != null && clickListener != null && eventConsumed) {
            clickListener.onClick(child, rv.getChildLayoutPosition(child));
        }
        eventConsumed = scaleGestureDetector.onTouchEvent(e);
        Log.d(TAG, "onInterceptTouchEvent: scaleGestureDetector . eventConsumed=" + eventConsumed);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}

