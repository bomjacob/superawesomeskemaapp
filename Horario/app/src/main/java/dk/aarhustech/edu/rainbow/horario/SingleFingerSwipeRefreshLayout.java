package dk.aarhustech.edu.rainbow.horario;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


public class SingleFingerSwipeRefreshLayout extends SwipeRefreshLayout {
    public SingleFingerSwipeRefreshLayout(Context context) {
        super(context);
    }

    public SingleFingerSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {;
        return ev.getPointerCount() == 1 && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return ev.getPointerCount() == 1 && super.onTouchEvent(ev);
    }
}
