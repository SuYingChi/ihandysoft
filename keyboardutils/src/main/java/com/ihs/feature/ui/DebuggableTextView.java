package com.ihs.feature.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ihs.keyboardutils.BuildConfig;


/**
 * All {@link TextView} subclasses should extend this class for testing long text on user interface.
 */
public class DebuggableTextView extends JellyBeanSpanFixTextView {

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_LONG_TEXT = false && BuildConfig.DEBUG;

    private static final CharSequence LONG_TEXT = DEBUG_LONG_TEXT ?
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. " +
            "Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet. " +
            "Duis sagittis ipsum. Praesent mauris. Fusce nec tellus sed augue semper porta. " +
            "Mauris massa. Vestibulum lacinia arcu eget nulla." : null;

    public DebuggableTextView(Context context) {
        super(context);
    }

    public DebuggableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DebuggableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        if (DEBUG_LONG_TEXT) {
            super.setText(LONG_TEXT, type);
        } else {
            super.setText(text, type);
        }
    }
}
