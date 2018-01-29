package com.ihs.feature.headset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;

import net.appcloudbox.ads.expressads.AcbExpressAdView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.services.concurrency.AsyncTask;


/**
 * Created by yingchi.su on 2018/1/12.
 */

public class HeadsetActivity extends HSAppCompatActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    public static final String TAG = "HeadsetActivity";
    private SeekBar volumeSeekBar;
    private AudioManager audioManager;
    private BroadcastReceiver headsetReceiver;
    private PopupWindow moreSettingPopWindow;
    private AcbExpressAdView acbExpressAdView;
    private LinearLayout installedAppViewGroup;
    private TextView musicRemain;
    private TextView movieRemain;
    private View rootView;
    private LinearLayout noAdView;
    private FrameLayout adContainer;
    public static final String VOLUME_CHANGED = "android.media.VOLUME_CHANGED_ACTION";
    private AsyncTask<Void, Void, Map<String, Drawable>> getMatchedAppsAsyncTask = new AsyncTask<Void, Void, Map<String, Drawable>>() {

        @Override
        protected Map<String, Drawable> doInBackground(Void... voids) {
            Map<String, Drawable> currentMatchAppMap = getInstallAppsInfoAndCompareRemote();
            return currentMatchAppMap;
        }

        @Override
        protected void onPostExecute(Map<String, Drawable> stringDrawableHashMap) {
            showInstalledAPP(stringDrawableHashMap);
        }
    };
    private final int installedAppViewMaximum = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headset_layout);
        headsetReceiver = new HeadsetFeatureReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOLUME_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(headsetReceiver, filter);
        initView();
        createAdView();
        getMatchedAppsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void initView() {
        LinearLayout headsetLayout = (LinearLayout) findViewById(R.id.headset_activity);
        volumeSeekBar = (SeekBar) findViewById(R.id.volume_seek_bar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Drawable wrapDrawable = DrawableCompat.wrap(volumeSeekBar.getThumb());
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, android.R.color.white));
            volumeSeekBar.setThumb(DrawableCompat.unwrap(wrapDrawable));
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(maxVolume);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(this);
        headsetLayout.setOnClickListener(new View.OnClickListener() {
            //此处空实现是为了避免点击弹窗的不可点击区域时收起弹窗,不可移除该事件监听
            @Override
            public void onClick(View v) {
            }
        });
        findViewById(R.id.headset_menu).setOnClickListener(this);;
        findViewById(R.id.close).setOnClickListener(this);
        installedAppViewGroup = (LinearLayout) findViewById(R.id.installed_app);
        adContainer = (FrameLayout) findViewById(R.id.ad_container);
        noAdView = (LinearLayout) findViewById(R.id.no_adView);
        musicRemain = (TextView) findViewById(R.id.musicRemain);
        movieRemain = (TextView) findViewById(R.id.movieRemain);
        rootView = LayoutInflater.from(this).inflate(R.layout.headset_layout, null);
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            musicRemain.setText(String.valueOf(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) * 5));
            movieRemain.setText(String.valueOf(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) * 3));
        }

    }

    private void createAdView() {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased() & !TextUtils.isEmpty(KCHeadsetManager.getInstance().getHeadsetAdPlacement())) {
            acbExpressAdView = new AcbExpressAdView(this, KCHeadsetManager.getInstance().getHeadsetAdPlacement());
            acbExpressAdView.setAutoSwitchAd(true);
            acbExpressAdView.setGravity(Gravity.CENTER);
            acbExpressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
                @Override
                public void onAdShown(AcbExpressAdView acbExpressAdView) {
                    noAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdClicked(AcbExpressAdView acbExpressAdView) {

                }
            });
            adContainer.addView(acbExpressAdView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        }
    }


    private void showInstalledAPP(Map<String, Drawable> matchAppMap) {
        if (!matchAppMap.isEmpty()) {
            installedAppViewGroup.setVisibility(View.VISIBLE);
            Iterator<Map.Entry<String, Drawable>> iterator = matchAppMap.entrySet().iterator();
            for (int i = 0; i < matchAppMap.size() && iterator.hasNext(); i++) {
                Map.Entry<String, Drawable> iteEntry = iterator.next();
                Drawable icon = iteEntry.getValue();
                String name = iteEntry.getKey();
                ImageView imageView = (ImageView) installedAppViewGroup.getChildAt(i);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageDrawable(icon);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchIntent = null;
                            launchIntent = getPackageManager().getLaunchIntentForPackage(name);
                        startActivity(launchIntent);

                    }
                });
            }
        } else {
            installedAppViewGroup.setVisibility(View.GONE);
        }
    }

    private List<String> getRemoteAPPlist() {
        ArrayList<String> list = (ArrayList<String>) HSConfig.getList("Application", "RemoteAppPackageName");
        return list;
    }

    private Map<String, Drawable> getInstallAppsInfoAndCompareRemote() {
        ArrayList<String> list = new ArrayList<String>();
        HashMap<String, Drawable> compareResult = new HashMap<String, Drawable>();
        PackageManager packageManager = getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);
        for (int i = 0; i < installedPackages.size(); i++) {
            PackageInfo packageInfo = installedPackages.get(i);
            String name = packageInfo.packageName;
            list.add(name);

        }
        if (!list.isEmpty() && !getRemoteAPPlist().isEmpty()) {
            Iterator<String> iterator = list.iterator();
            List<String> remoteAPPlist = getRemoteAPPlist();
            while (iterator.hasNext()) {
                String packageName = iterator.next();
                for (String remotePattern : remoteAPPlist) {
                    if (packageName.contains(remotePattern)) {
                        PackageInfo packageInfo = null;
                        try {
                            if (packageManager.getLaunchIntentForPackage(packageName) != null) {
                                packageInfo = packageManager.getPackageInfo(packageName, 0);
                                if (packageInfo != null) {
                                    Drawable drawable = packageInfo.applicationInfo.loadIcon(packageManager);
                                    compareResult.put(packageName, drawable);
                                    break;
                                }
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (compareResult.size() == installedAppViewMaximum)
                    break;
            }
        }
        return compareResult;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(headsetReceiver);
        if (acbExpressAdView != null) {
            acbExpressAdView.destroy();
            acbExpressAdView = null;
        }
        getMatchedAppsAsyncTask.cancel(true);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.headset_menu) {
            showPopWindow();
        } else if (i == R.id.close) {
            finish();
        } else if (i == R.id.headset_disable) {
            KCHeadsetManager.getInstance().setEnabled(false);
            dissMissPopWindow();
        } else if (i == R.id.close_pop_window) {
            dissMissPopWindow();
        }
    }

    private void dissMissPopWindow() {
        if (moreSettingPopWindow.isShowing() & !isFinishing())
            moreSettingPopWindow.dismiss();
    }

    private void showPopWindow() {
        if (moreSettingPopWindow == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.headset_disable_suggestion_pop, null);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            moreSettingPopWindow = new PopupWindow(contentView, (int) (width * 0.8), LinearLayout.LayoutParams.WRAP_CONTENT);
            moreSettingPopWindow.setFocusable(true);
            moreSettingPopWindow.setContentView(contentView);
            moreSettingPopWindow.setBackgroundDrawable(new BitmapDrawable());
            moreSettingPopWindow.setOutsideTouchable(true);
            moreSettingPopWindow.setTouchable(true);
            Button headsetDisable = (Button) contentView.findViewById(R.id.headset_disable);
            Button closePopWindow = (Button) contentView.findViewById(R.id.close_pop_window);
            headsetDisable.setOnClickListener(this);
            closePopWindow.setOnClickListener(this);
        }
        if (!isFinishing() && !moreSettingPopWindow.isShowing())
            moreSettingPopWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekBar.setProgress(currentVolume);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private class HeadsetFeatureReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            HSLog.d(TAG, "HeadsetReceiver  onReceive  headset==========" + intent.getAction());
            String action = intent.getAction();
            if (action.equals(VOLUME_CHANGED)) {
                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                volumeSeekBar.setProgress(currentVolume);
            } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int current = intent.getExtras().getInt("level");
                int total = intent.getExtras().getInt("scale");
                float percent = current * 100 / total;
                musicRemain.setText(String.valueOf(percent * 5));
                movieRemain.setText(String.valueOf(percent * 3));
            } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                HeadsetActivity.this.finish();
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (moreSettingPopWindow != null && moreSettingPopWindow.isShowing()) {
                moreSettingPopWindow.dismiss();
            }
            return super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
        return true;
    }

}
