package com.ihs.keyboardutils.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ihs.keyboardutils.R;

/**
 * Copy from https://github.com/Carson-Ho/SuperEditText
 */

public class SearchEditTextView extends LinearLayout {

    private static final String TAG = "SearchEditTextView";

    private EditText editText;
    private ImageView searchDeleteImageView;
    private View dividerView;
    private ImageView searchImageView;

    public SearchEditTextView(Context context) {
        this(context, null);
    }

    public SearchEditTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.search_edit_view, this, true);
        editText = findViewById(R.id.search_edit_text);
        searchDeleteImageView = findViewById(R.id.search_delete_icon);
        dividerView = findViewById(R.id.search_divider_view);
        searchImageView = findViewById(R.id.search_button_image);
    }

}
