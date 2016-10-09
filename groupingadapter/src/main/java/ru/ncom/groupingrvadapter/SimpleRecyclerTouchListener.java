package ru.ncom.groupingrvadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects click and long click on RecyclerView row. Toggles row view selection and if applicable
 * toggles item selection in adapter of the RecyclerView. This is done before onLongClick is called.
 */
public class SimpleRecyclerTouchListener implements RecyclerView.OnItemTouchListener {

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    private GestureDetector gestureDetector;
    private ClickListener clickListener;

    public SimpleRecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
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
                    // is it a proper adapter?
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
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        // Looks like GestureDetector#onTouchEvent() makes GestureDetector keep
        // listening to touch-related events till the gesture ends or its override method consumes event.
        boolean eventConsumed = gestureDetector.onTouchEvent(e);
        if (child != null && clickListener != null && eventConsumed) {
            clickListener.onClick(child, rv.getChildLayoutPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}

