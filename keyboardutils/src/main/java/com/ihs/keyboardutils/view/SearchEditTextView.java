package com.ihs.keyboardutils.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.chargingscreen.ui.RippleDrawableUtils;
import com.ihs.chargingscreen.utils.DisplayUtils;
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

    private final TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {

        /**
         * Called when the input method default action key is pressed.
         */
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                onSearchClicked();
            }
            return true;
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
        View rootView = findViewById(R.id.search_edit_text_root_view);
        int color = ContextCompat.getColor(getContext(), R.color.search_bg);
        rootView.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(color, color, DisplayUtils.dip2px(2)));
        editText = findViewById(R.id.search_edit_text);
        searchDeleteImageView = findViewById(R.id.search_delete_icon);
        dividerView = findViewById(R.id.search_divider_view);
        searchImageView = findViewById(R.id.search_button_image);
        editText.addTextChangedListener(mTextWatcher);
        editText.setOnEditorActionListener(editorActionListener);

        searchDeleteImageView.setOnClickListener(mOnClickListener);
        searchImageView.setOnClickListener(mOnClickListener);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchEditTextView, 0, 0);
            try {
                if (a.hasValue(R.styleable.SearchEditTextView_ic_delete)) {
                    int icDelete = a.getResourceId(R.styleable.SearchEditTextView_ic_delete, R.drawable.ic_close_button_circle);
                    searchDeleteImageView.setImageResource(icDelete);
                }
                if (a.hasValue(R.styleable.SearchEditTextView_ic_search)) {
                    int icSearch = a.getResourceId(R.styleable.SearchEditTextView_ic_search, R.drawable.ic_locker_search);
                    searchImageView.setImageResource(icSearch);
                }
                if (a.hasValue(R.styleable.SearchEditTextView_search_background_color)) {
                    int backgroundColor = a.getColor(R.styleable.SearchEditTextView_search_background_color, Color.GRAY);
                    rootView.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(backgroundColor, backgroundColor, DisplayUtils.dip2px(2)));
                }
            } finally {
                a.recycle();
            }
        }
    }

    /**
     * Called when the window containing this view gains or loses focus.  Note
     * that this is separate from view focus: to receive key events, both
     * your view and its window must have focus.  If a window is displayed
     * on top of yours that takes input focus, then your own window will lose
     * focus but the view focus will remain unchanged.
     *
     * @param hasWindowFocus True if the window containing this view now has
     *                       focus, false otherwise.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (editText != null) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onTextChanged(editText.getText());
                            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            }
        }
    }

    private void onTextChanged(CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            searchDeleteImageView.setVisibility(GONE);
            dividerView.setVisibility(GONE);
        } else {
            searchDeleteImageView.setVisibility(VISIBLE);
            dividerView.setVisibility(VISIBLE);
        }
        invalidate();
    }

    public void setSearchButtonClickListener(OnSearchButtonClickListener searchButtonClickListener) {
        this.searchButtonClickListener = searchButtonClickListener;
    }
}
