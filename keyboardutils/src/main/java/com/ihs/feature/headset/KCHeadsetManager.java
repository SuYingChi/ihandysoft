package com.ihs.feature.headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;

/**
 * Created by yingchi.su on 2018/1/17.
 */

public class KCHeadsetManager {
    private volatile static KCHeadsetManager headsetManager;
    private HeadsetReceiver headsetReceiver;

    private String placement;
    private boolean isEnabled = false;
    private boolean isRegisterReceiver = false;

    private KCHeadsetManager() {
        headsetReceiver = new HeadsetReceiver();
    }

    public static KCHeadsetManager getInstance() {
        if (headsetManager == null) {
            synchronized (KCHeadsetManager.class) {
                if (headsetManager == null) {
                    headsetManager = new KCHeadsetManager();
                }
            }
        }
        return headsetManager;
    }

    public void setHeadsetAdPlacement(String placement) {
        this.placement = placement;
    }

    public String getHeadsetAdPlacement() {
        return placement;
    }

    //从设置获取该功能新的的开关值，（暂返回默认值false，留待后续补充获取开关值的逻辑）
    private boolean getHeadsetEnabledValueFromSetting() {
        return false;
    }

    //将该功能的新的开关值设给设置的开关并更新headsetReceiver的注册状态，留待后续补充
    public void setEnabled(boolean newEnableValue) {
        isEnabled = newEnableValue;
        updateHeadsetReceiver();
    }

    private class HeadsetReceiver extends BroadcastReceiver {
        public static final String TAG = "HeadsetReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_HEADSET_PLUG)) {
                if (intent.getIntExtra("state", 0) == 1) {
                    HSLog.d(TAG, " onReceive  headset==========" + Intent.ACTION_HEADSET_PLUG + "      state================" + intent.getIntExtra("state", 0));
                    Intent mIntent = new Intent(context, HeadsetActivity.class);
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(mIntent);
                }
            }
        }
    }

    public void init(String string) {
        setHeadsetAdPlacement(string);
        isEnabled = getHeadsetEnabledValueFromSetting();
        updateHeadsetReceiver();
    }

    private void updateHeadsetReceiver() {
        if (!isRegisterReceiver && isEnabled) {
            HSApplication.getContext().registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            isRegisterReceiver = true;
        } else if (isRegisterReceiver && !isEnabled) {
            HSApplication.getContext().unregisterReceiver(headsetReceiver);
            isRegisterReceiver = false;
        }
    }
}
