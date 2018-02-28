package com.ihs.inputmethod.uimodules.ui.adjustheight;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 18/2/7.
 */

public class AdjustHeightView extends RelativeLayout implements View.OnClickListener {

    private RelativeLayout adjustHeightControllerContainer;
    private ImageView imageController;

    private int keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
    private int defaultKeyboardHeight = (int) (keyboardHeight / HSResourceUtils.getKeyboardHeightPercent());
    private int defaultSuggestionStripHeight = HSResourceUtils.getDefaultSuggestionStripHeight(getResources());
    private int controllerHalfHeight = getResources().getDimensionPixelSize(R.dimen.keyboard_adjust_height_controller_height) / 2;

    private float downY;
    private float diffY;

    public AdjustHeightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View bottomContainer = findViewById(R.id.bottom_container);
        bottomContainer.getLayoutParams().height = keyboardHeight;

        imageController = findViewById(R.id.image_controller);
        imageController.setOnTouchListener(touchListener);

        adjustHeightControllerContainer = findViewById(R.id.adjust_height_controller_container);
        adjustHeightControllerContainer.setClickable(true);
        LayoutParams layoutParams = (LayoutParams) adjustHeightControllerContainer.getLayoutParams();
        layoutParams.bottomMargin = keyboardHeight + defaultSuggestionStripHeight - controllerHalfHeight;

        findViewById(R.id.btn_reset).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    private OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float currentY = event.getRawY();
                    diffY = downY - currentY;
                    downY = currentY;

                    HSLog.w("cjx", "downY:" + downY + " , diffY :" + diffY);

                    LayoutParams layoutParams = (LayoutParams) adjustHeightControllerContainer.getLayoutParams();
                    layoutParams.bottomMargin += (int) (diffY);

                    int newKeyboardHeight = layoutParams.bottomMargin - defaultSuggestionStripHeight + controllerHalfHeight;
                    if (newKeyboardHeight > defaultKeyboardHeight * 1.2f) {
                        layoutParams.bottomMargin = (int) (defaultKeyboardHeight * 1.2f) + defaultSuggestionStripHeight - controllerHalfHeight;
                    } else if (newKeyboardHeight < defaultKeyboardHeight * 0.8f) {
                        layoutParams.bottomMargin = (int) (defaultKeyboardHeight * 0.8f) + defaultSuggestionStripHeight - controllerHalfHeight;
                    }

                    adjustHeightControllerContainer.setLayoutParams(layoutParams);
                    HSResourceUtils.setKeyboardHeightPercent(newKeyboardHeight * 1.0f / defaultKeyboardHeight);
                    break;
                case MotionEvent.ACTION_UP:
                    HSUIInputMethodService.getKeyboardPanelMananger().updateKeyboardHeight();
                    break;
            }
            return true;
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                HSResourceUtils.setKeyboardHeightPercent(1);
                HSUIInputMethodService.getKeyboardPanelMananger().hideAdjustKeyboardHeightView();
                break;
            case R.id.btn_confirm:
                HSUIInputMethodService.getKeyboardPanelMananger().hideAdjustKeyboardHeightView();
                break;
        }
    }
}
