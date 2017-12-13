package com.ihs.keyboardutils.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;

/**
 * Created by yan.xia, similar to SearchView
 */

public class SearchEditTextView extends LinearLayout {

    private static final String TAG = "SearchEditTextView";

    private EditText editText;
    private ImageView searchDeleteImageView;
    private View dividerView;
    private ImageView searchImageView;

    public interface OnSearchButtonClickListener {
        void onSearchButtonClick(String searchText);
    }

    private OnSearchButtonClickListener searchButtonClickListener;

    /**
     * Callback to watch the text field for empty/non-empty
     */
    private TextWatcher mTextWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence s, int start, int before, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int after) {
            SearchEditTextView.this.onTextChanged(s);
        }

        public void afterTextChanged(Editable s) {

        }
    };

    private final OnClickListener mOnClickListener = new OnClickListener() {

        public void onClick(View v) {
            if (v == searchImageView) {
                onSearchClicked();
            } else if (v == searchDeleteImageView) {
                onCloseClicked();
            }
        }
    };

    private void onCloseClicked() {
        CharSequence text = editText.getText();
        if (TextUtils.isEmpty(text)) {
            clearFocus();
        } else {
            editText.setText("");
            editText.requestFocus();
        }
    }

    private void onSearchClicked() {
        editText.requestFocus();
        if (searchButtonClickListener != null) {
            searchButtonClickListener.onSearchButtonClick(editText.getText().toString());
        }
    }

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
        editText.addTextChangedListener(mTextWatcher);
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        searchDeleteImageView.setOnClickListener(mOnClickListener);
        searchImageView.setOnClickListener(mOnClickListener);
    }

    private void onTextChanged(CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            searchDeleteImageView.setVisibility(GONE);
            dividerView.setVisibility(GONE);
        } else {
            searchDeleteImageView.setVisibility(VISIBLE);
            dividerView.setVisibility(VISIBLE);
        }
        HSLog.d(TAG, "onTextChanged: " + s);
        invalidate();
    }

    public void setSearchButtonClickListener(OnSearchButtonClickListener searchButtonClickListener) {
        this.searchButtonClickListener = searchButtonClickListener;
    }
}
