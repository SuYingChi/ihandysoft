package com.ihs.inputmethod.uimodules.ui.adjustheight;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.kc.utils.KCAnalytics;

/**
 * Created by jixiang on 18/2/7.
 */

public class AdjustHeightView extends RelativeLayout implements View.OnClickListener {

    private View bottomContainer;
    private View imageController;
    private View adjustHeightControllerContainer;

    private int keyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
    private int defaultKeyboardHeight = (int) (keyboardHeight / HSResourceUtils.getKeyboardHeightPercent());
    private int defaultSuggestionStripHeight = HSResourceUtils.getDefaultSuggestionStripHeight(getResources());
    private int halfControllerHeight = getResources().getDimensionPixelSize(R.dimen.keyboard_adjust_height_controller_height) / 2;

    private float downY;
    private float diffY;
    private float imageControllerExternalTouchSize = HSDisplayUtils.dip2px(30);
    private boolean canAdjust = false;

    public AdjustHeightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        bottomContainer = findViewById(R.id.bottom_container);
        bottomContainer.getLayoutParams().height = keyboardHeight;

        imageController = findViewById(R.id.image_controller);
        imageController.setOnTouchListener(touchListener);

        adjustHeightControllerContainer = findViewById(R.id.adjust_height_controller_container);
        adjustHeightControllerContainer.setClickable(true);
        LayoutParams layoutParams = (LayoutParams) adjustHeightControllerContainer.getLayoutParams();
        layoutParams.bottomMargin = keyboardHeight + defaultSuggestionStripHeight - halfControllerHeight;

        findViewById(R.id.btn_reset).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.root_view).setOnTouchListener(touchListener);
    }

    private OnTouchListener touchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = event.getRawY();
                    int[] loc = new int[2];
                    imageController.getLocationOnScreen(loc);

                    int controllerCenterX = loc[0] + imageController.getWidth() / 2;
                    int controllerCenterY = loc[1] + imageController.getHeight() / 2;

                    float slotX = Math.abs(controllerCenterX - event.getRawX());
                    float slotY = Math.abs(controllerCenterY - downY);

                    if (slotX < imageController.getWidth() / 2 + imageControllerExternalTouchSize && slotY < imageController.getHeight() / 2 + imageControllerExternalTouchSize) {
                        canAdjust = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (canAdjust) {
                        float currentY = event.getRawY();
                        diffY = downY - currentY;
                        downY = currentY;

                        LayoutParams layoutParams = (LayoutParams) adjustHeightControllerContainer.getLayoutParams();
                        layoutParams.bottomMargin += (int) (diffY);

                        int newKeyboardHeight = layoutParams.bottomMargin - defaultSuggestionStripHeight + halfControllerHeight;
                        if (newKeyboardHeight > defaultKeyboardHeight * 1.2f) {
                            newKeyboardHeight = (int) (defaultKeyboardHeight * 1.2f);
                        } else if (newKeyboardHeight < defaultKeyboardHeight * 0.8f) {
                            newKeyboardHeight = (int) (defaultKeyboardHeight * 0.8f);
                        }
                        float keyboardHeightPercent = newKeyboardHeight * 1.0f / defaultKeyboardHeight;
                        HSResourceUtils.setKeyboardHeightPercent(keyboardHeightPercent);

                        newKeyboardHeight = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());

                        layoutParams.bottomMargin = newKeyboardHeight + defaultSuggestionStripHeight - halfControllerHeight;
                        adjustHeightControllerContainer.setLayoutParams(layoutParams);
                        bottomContainer.getLayoutParams().height = newKeyboardHeight;

                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (canAdjust) {
                        canAdjust = false;
                        HSUIInputMethodService.getKeyboardPanelMananger().updateKeyboardHeight();
                    } else {
                        int[] location = new int[2];
                        imageController.getLocationOnScreen(location);
                        if (downY < location[1] + imageController.getHeight() / 2) {
                            HSUIInputMethodService.getKeyboardPanelMananger().hideAdjustKeyboardHeightView();
                        }
                    }
                    break;
            }
            return true;
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                LayoutParams layoutParams = (LayoutParams) adjustHeightControllerContainer.getLayoutParams();
                layoutParams.bottomMargin = defaultKeyboardHeight + defaultSuggestionStripHeight - halfControllerHeight;

                layoutParams = (LayoutParams) bottomContainer.getLayoutParams();
                layoutParams.height = defaultKeyboardHeight;
                bottomContainer.requestLayout();

                HSResourceUtils.setKeyboardHeightPercent(1);
                logLastKeyboardHeight();
                HSUIInputMethodService.getKeyboardPanelMananger().updateKeyboardHeight();
                HSUIInputMethodService.getKeyboardPanelMananger().hideAdjustKeyboardHeightView();
                break;
            case R.id.btn_confirm:
                logLastKeyboardHeight();
                HSUIInputMethodService.getKeyboardPanelMananger().hideAdjustKeyboardHeightView();
                break;
        }
    }

    public static void logLastKeyboardHeight() {
        KCAnalytics.logEvent("relative_height_value", "relative_value", (int) ((HSResourceUtils.getKeyboardHeightPercent() - 1) * 100) + "%-" + HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources()));
    }
}
