package ru.ncom.groupingrvadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects click and long click
 */
public class SimpleRecyclerTouchListener implements RecyclerView.OnItemTouchListener {

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    //private final String TAG = "SimpleRecyclerTouchListener";
    private GestureDetector gestureDetector;
    private ClickListener clickListener;

    public SimpleRecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
        this.clickListener = clickListener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            //private final String TAG = "GestureDetector";

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //Log.d(TAG, "onSingleTapUp: ");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                //Log.d(TAG, "onLongPress: e.time = " + e.getEventTime());
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

        //Log.d(TAG, "onInterceptTouchEvent: child Class="
        //        + (child != null ? child.getClass().getName() : "*No child*") );
        boolean touchEventDetected = gestureDetector.onTouchEvent(e);
        //Log.d(TAG, "onInterceptTouchEvent: gestureDetector.onTouchEvent(e)=" + touchEventDetected
        //    +"\t\n e.time = "+ e.getEventTime());
        if (child != null && clickListener != null && touchEventDetected) {
            clickListener.onClick(child, rv.getChildLayoutPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        //Log.d(TAG, "onTouchEvent: ");
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        //Log.d(TAG, "onRequestDisallowInterceptTouchEvent: ");
    }
}
/* This is long click: GestureDetector#onLongPress fires 2 times after GestureDetector#onTouchEvent() exited.
Looks like GestureDetector#onTouchEvent() makes GestureDetector keep listening to touch-related events till the gesture ends.

09-14 17:59:02.257 21628-21628/ru.ncom.groupingrecyclerview D/SimpleRecyclerTouchListener: onInterceptTouchEvent: child Class=android.widget.RelativeLayout
09-14 17:59:02.257 21628-21628/ru.ncom.groupingrecyclerview D/SimpleRecyclerTouchListener: onInterceptTouchEvent: gestureDetector.onTouchEvent(e)=false
                                                                                      e.time = 4005907
09-14 17:59:02.889 21628-21628/ru.ncom.groupingrecyclerview D/GestureDetector: onLongPress: e.time = 4005907
09-14 17:59:02.889 21628-21628/ru.ncom.groupingrecyclerview D/ClickListener(Main): onLongClick: at postype=2
09-14 17:59:08.020 21628-21628/ru.ncom.groupingrecyclerview D/SimpleRecyclerTouchListener: onInterceptTouchEvent: child Class=android.widget.RelativeLayout
09-14 17:59:08.020 21628-21628/ru.ncom.groupingrecyclerview D/SimpleRecyclerTouchListener: onInterceptTouchEvent: gestureDetector.onTouchEvent(e)=false
                                                                                      e.time = 4011676
 */
