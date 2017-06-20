package com.ihs.feature.ui;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;

/**
 * General interface for scroll bar or alphabet bar used in a {@link FastScrollRecyclerView}.
 *
 * A reference of {@link FastScrollRecyclerView} instance should be passed in in the constructor method of this scroller.
 */
public abstract class FastScroller {

    protected boolean mHidden = false;

    public void show() {
        mHidden = false;
    }

    public void hide() {
        mHidden = true;
    }

    // Thumb attachment status
    public abstract void setDetachThumbOnFastScroll();
    public abstract boolean isThumbDetached();
    public abstract void reattachThumbToScroll();

    // Touch event handling
    public abstract void handleTouchEvent(MotionEvent ev, int downX, int downY, int lastY);
    public abstract boolean isDraggingThumb();
    public abstract float getLastTouchY();
    public abstract void setThumbOffset(int x, int y);
    public abstract Point getThumbOffset();

    // Look of scroller and containing view
    public abstract void draw(Canvas canvas);
    public abstract int getThumbWidth();
    public abstract int getThumbMaxWidth();
    public abstract int getThumbHeight();
}
