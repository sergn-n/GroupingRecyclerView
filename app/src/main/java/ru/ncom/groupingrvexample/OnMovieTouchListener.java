package ru.ncom.groupingrvexample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import ru.ncom.groupingrvadapter.Selectable;
import ru.ncom.groupingrvadapter.GetterAtPosition;

/**
 * Created by Serg on 09.10.2016.
 */

public class OnMovieTouchListener implements RecyclerView.OnItemTouchListener {

    public interface ClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onItemZoomIn(View view, int position);

        void onItemZoomOut(View view, int position);
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
                        clickListener.onItemLongClick(child, position);
                    }
                }
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                double scaleFactor = detector.getScaleFactor();
                Log.d(TAG, String.format("Scale gesture detected : %.3f", scaleFactor));

                View child = recyclerView.findChildViewUnder(detector.getFocusX(), detector.getFocusY());
                if (child == null)
                    return true;
                int position = recyclerView.getChildLayoutPosition(child);
                if (0.98f > scaleFactor) {
                    clickListener.onItemZoomOut(child, position);
                } else  if (1.02f < scaleFactor){
                    clickListener.onItemZoomIn(child, position);
                }
                return true;
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
            clickListener.onItemClick(child, rv.getChildLayoutPosition(child));
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

