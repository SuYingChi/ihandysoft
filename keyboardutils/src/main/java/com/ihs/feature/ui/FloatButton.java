package com.ihs.feature.ui;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;


public class FloatButton {

    private Context mContext;
    private View mFloatContentView;
    private boolean mIsShowing;
    private SafeWindowManager mWindowManager;
    private WindowManager.LayoutParams mWinManagerParams;
    private ClickActionListener clickActionListener;
    private int mPhoneHeight;

    public FloatButton(Context context, SafeWindowManager windowManager) {
        mContext = context;
        mWindowManager = windowManager;
        mPhoneHeight = CommonUtils.getPhoneHeight(context);
        createFloatView();
        createParams();
    }

    private void createFloatView() {
        mFloatContentView = View.inflate(mContext, R.layout.boost_plus_float_button, null);
        mFloatContentView.setFocusableInTouchMode(true);
        mFloatContentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mFloatContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickActionListener) {
                    clickActionListener.onClickAction();
                }
            }
        });
    }

    private void createParams() {
        mWinManagerParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT <= 18) {
            mWinManagerParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else {
            mWinManagerParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        mWinManagerParams.format = android.graphics.PixelFormat.TRANSLUCENT;
        mWinManagerParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mWinManagerParams.gravity = Gravity.BOTTOM | Gravity.END;
        mWinManagerParams.y = mPhoneHeight * 3 / 10;
        mWinManagerParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWinManagerParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    public void show() {
        if (mIsShowing) {
            return;
        }

        mFloatContentView.setVisibility(View.VISIBLE);
        if (null != mWindowManager && !mIsShowing) {
            mWindowManager.addView(mFloatContentView, mWinManagerParams);
            mIsShowing = true;
        }
    }

    public void remove() {
        if (null != mWindowManager && null != mFloatContentView) {
            mWindowManager.removeView(mFloatContentView);
            mIsShowing = false;
        }
    }

    public interface ClickActionListener{
        void onClickAction();
    }

    public void setClickActionListener(ClickActionListener listener){
        clickActionListener = listener;
    }

}


