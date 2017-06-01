package com.ihs.feature.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ihs.feature.common.TypefacedTextView;

public class AdsTitleTextView extends TypefacedTextView {

    private static final String NEW_LINE_CHARACTER = "\n";
    private static final String SPACE_CHARACTER = "\t";

    public AdsTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        String oriString = (String) text;
        String newString = oriString.replaceAll(NEW_LINE_CHARACTER, SPACE_CHARACTER);
        super.setText(newString, type);
    }
}
