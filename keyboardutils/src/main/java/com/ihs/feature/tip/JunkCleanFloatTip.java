package com.ihs.feature.tip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.artw.lockscreen.common.NavUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.feature.common.LauncherConstants;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.junkclean.JunkCleanAnimationActivity;
import com.ihs.feature.ui.FloatWindowDialog;
import com.ihs.feature.ui.FloatWindowManager;
import com.ihs.feature.ui.FloatingDialog;
import com.ihs.feature.ui.SafeWindowManager;
import com.ihs.keyboardutils.R;


@SuppressLint("ViewConstructor")
public abstract class JunkCleanFloatTip extends FloatWindowDialog implements View.OnClickListener {

    private Data mData;

    public abstract FloatWindowManager.Type getTipType();

    public static class Data {
        public CharSequence title;
        public CharSequence content;
        public String appName;
        public Bitmap iconBitmap;
        public Drawable iconDrawable;
        public long junkSize;

        public void clear() {
            title = null;
            iconBitmap = null;
            iconDrawable = null;
            content = null;
            appName = null;
        }
    }

    public JunkCleanFloatTip(Context context) {
        super(context);
        initView(context, null);
    }

    public JunkCleanFloatTip(Context context, Data data) {
        super(context);
        initView(context, data);
    }

    protected void initView(Context context, Data data) {
        mData = data;
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.clean_float_dialog, this);

        View containerView = ViewUtils.findViewById(this, R.id.floating_dialog_container);
        containerView.setBackgroundColor(FloatingDialog.BACKGROUND_COLOR);

        ImageView closeIv =  ViewUtils.findViewById(this, R.id.close_iv);
        Button cleanBtn = ViewUtils.findViewById(this, R.id.action_btn);
        TextView titleTv = ViewUtils.findViewById(this, R.id.content_tv);
        TextView contentTv = ViewUtils.findViewById(this, R.id.content_tv);

        ImageView contentImage = ViewUtils.findViewById(this, R.id.content_image);
        if (data != null) {
            if (null != data.iconBitmap) {
                contentImage.setImageBitmap(data.iconBitmap);
            } else if (null != data.iconDrawable) {
                contentImage.setImageDrawable(data.iconDrawable);
            }
            if (!TextUtils.isEmpty(data.title)) {
                titleTv.setText(data.title);
            }
            if (!TextUtils.isEmpty(data.content)) {
                contentTv.setText(data.content);
            }
        }

        closeIv.setOnClickListener(this);
        cleanBtn.setOnClickListener(this);
    }

    @Override
    protected FloatWindowManager.Type getType() {
        return getTipType();
    }

    @Override
    public void dismiss() {
        FloatWindowManager.getInstance().removeDialog(getType());
        HSGlobalNotificationCenter.sendNotification(LauncherConstants.NOTIFICATION_TIP_DISMISS);
        if (mData != null) {
            mData.clear();
        }
    }

    @Override
    public WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            lp.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        }
        this.setLayoutParams(lp);
        return lp;
    }

    @Override
    public boolean shouldDismissOnLauncherStop() {
        return true;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.close_iv) {
            dismiss();

        } else if (i == R.id.action_btn) {
            int cleanAnimationType = JunkCleanAnimationActivity.INTENT_FROM_CLEAN;
            FloatWindowManager.Type type = getTipType();
            if (type == FloatWindowManager.Type.JUNK_CLEAN_INSTALL) {
                cleanAnimationType = JunkCleanAnimationActivity.INTENT_FROM_INSTALL_ALERT;
            } else if (type == FloatWindowManager.Type.JUNK_CLEAN_UNINSTALL) {
                cleanAnimationType = JunkCleanAnimationActivity.INTENT_FROM_UNINSTALL_ALERT;
            }
            Intent junkCleanAnimationIntent = new Intent(v.getContext(), JunkCleanAnimationActivity.class);
            junkCleanAnimationIntent.putExtra(JunkCleanAnimationActivity.INTENT_KEY_FROM, cleanAnimationType);
            if (null != mData) {
                String appName = mData.appName;
                if (!TextUtils.isEmpty(appName)) {
                    junkCleanAnimationIntent.putExtra(JunkCleanAnimationActivity.INTENT_KEY_APP_NAME, appName);
                }
                junkCleanAnimationIntent.putExtra(JunkCleanAnimationActivity.INTENT_KEY_JUNK_SIZE, mData.junkSize);
            }
            junkCleanAnimationIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            NavUtils.startActivitySafely(v.getContext(), junkCleanAnimationIntent);
            dismiss();

        } else {
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            dismiss();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onAddedToWindow(SafeWindowManager windowManager) {

    }
}
