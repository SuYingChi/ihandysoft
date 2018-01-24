package com.ihs.keyboardutils.alerts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.feature.common.RoundCornerImageView;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.keyboardutils.view.RoundedCornerLayout;
import com.kc.utils.HeadSetManager;

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

public class HeadSetActivity extends HSActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    public String TAG = "HeadSetActivity";
    private SeekBar voiceseekBar;
    private AudioManager am;
    private BroadcastReceiver receiver;
    private ImageView isEnable;
    private ImageView closeButton;
    private PopupWindow isEnablePopupWindow;
    private AcbExpressAdView acbExpressAdView;
    private LinearLayout installAppViewGroup;
    private int percent = 0;
    private TextView music;
    private TextView movie;
    private View rootview;
    private LinearLayout noadv;
    private RoundedCornerLayout adContainer;
    private Button cancle;
    private Button noNotification;
    private final String VOICE_CHANGE = "android.media.VOLUME_CHANGED_ACTION";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headsetlayout);
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOICE_CHANGE);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(receiver, filter);
        initview();
        creatAdv();

    }

    AsyncTask<Void, Void, HashMap<String, Drawable>> getMatchedAppsAsyncTask = new AsyncTask<Void, Void, HashMap<String, Drawable>>() {

        @Override
        protected HashMap<String, Drawable> doInBackground(Void... voids) {
            HashMap<String, Drawable>  currentMatchAppMap = getInstallAppsInfoAndcompareRemote();
            return currentMatchAppMap;
        }

        @Override
        protected void onPostExecute(HashMap<String, Drawable> stringDrawableHashMap) {
            showInstallAPP(stringDrawableHashMap);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        getMatchedAppsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (acbExpressAdView != null) {
            acbExpressAdView.setVisibility(View.VISIBLE);
            acbExpressAdView.switchAd();
        } else {
            noadv.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initview() {
        LinearLayout headsetlayout = (LinearLayout) findViewById(R.id.headsetactivity);
        voiceseekBar = (SeekBar) findViewById(R.id.brightness_seekbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Drawable wrapDrawable = DrawableCompat.wrap(voiceseekBar.getThumb());
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, android.R.color.white));
            voiceseekBar.setThumb(DrawableCompat.unwrap(wrapDrawable));
        }
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        voiceseekBar.setMax(maxVolume);
        int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        voiceseekBar.setProgress(currentVolume);
        voiceseekBar.setOnSeekBarChangeListener(this);
        headsetlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "click  headSetLayout,avoid finish headsetactivity");
            }
        });
        isEnable = (ImageView) findViewById(R.id.isNotification);
        isEnable.setOnClickListener(this);
        closeButton = (ImageView) findViewById(R.id.close);
        closeButton.setOnClickListener(this);
        installAppViewGroup = (LinearLayout) findViewById(R.id.installed_app);
        adContainer = (RoundedCornerLayout) findViewById(R.id.adsContainer);
        noadv = (LinearLayout) findViewById(R.id.noadv);
        music = (TextView) findViewById(R.id.musicRemain);
        movie = (TextView) findViewById(R.id.movieRemain);
        rootview = LayoutInflater.from(this).inflate(R.layout.headsetlayout, null);
    }

    private void creatAdv() {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased() & !TextUtils.isEmpty(HeadSetManager.getInstance().getHeadSetAdPlaceMent()) & acbExpressAdView == null) {
            acbExpressAdView = new AcbExpressAdView(this, HeadSetManager.getInstance().getHeadSetAdPlaceMent());
            if (acbExpressAdView != null) {
                acbExpressAdView.setAutoSwitchAd(false);
                acbExpressAdView.setGravity(Gravity.CENTER);
                acbExpressAdView.setExpressAdViewListener(new AcbExpressAdView.AcbExpressAdViewListener() {
                    @Override
                    public void onAdShown(AcbExpressAdView acbExpressAdView) {
                        noadv.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAdClicked(AcbExpressAdView acbExpressAdView) {
                        acbExpressAdView.setVisibility(View.GONE);
                        noadv.setVisibility(View.VISIBLE);
                        adContainer.removeView(acbExpressAdView);
                        acbExpressAdView.destroy();
                        acbExpressAdView = null;

                    }
                });
                adContainer.addView(acbExpressAdView, RoundedCornerLayout.LayoutParams.MATCH_PARENT, RoundedCornerLayout.LayoutParams.MATCH_PARENT);
            } else {
                noadv.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showInstallAPP(HashMap<String, Drawable> matchAppMap) {
        if (!matchAppMap.isEmpty()) {
            installAppViewGroup.setVisibility(View.VISIBLE);
            Iterator<Map.Entry<String, Drawable>> ite = matchAppMap.entrySet().iterator();
            for (int i = 0; i < matchAppMap.size() && ite.hasNext(); i++) {
                Map.Entry<String, Drawable> iteEntry = ite.next();
                Drawable icon = iteEntry.getValue();
                String name = iteEntry.getKey();
                RoundCornerImageView roundCornerImageView = (RoundCornerImageView) installAppViewGroup.getChildAt(i);
                roundCornerImageView.setVisibility(View.VISIBLE);
                roundCornerImageView.setImageDrawable(icon);
                roundCornerImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launnchIntent = getPackageManager().getLaunchIntentForPackage(name);
                        startActivity(launnchIntent);

                    }
                });
            }
            if (matchAppMap.size() < 5) {
                for (int i = matchAppMap.size(); i < 5; i++) {
                    RoundCornerImageView roundCornerImageView = (RoundCornerImageView) installAppViewGroup.getChildAt(i);
                    roundCornerImageView.setVisibility(View.INVISIBLE);
                }
            }
        }else {
            installAppViewGroup.setVisibility(View.GONE);
        }


    }
    private ArrayList<String> getRemoteAPPlist() {
        ArrayList<String> list = (ArrayList<String>) HSConfig.getList("Application", "RemoteAppPackageName");
        return list;
    }

    private HashMap<String, Drawable> getInstallAppsInfoAndcompareRemote() {
        ArrayList<String> list = new ArrayList<String>();
        HashMap<String, Drawable> compareResult = new HashMap<String, Drawable>();
        PackageManager pManager = getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = paklist.get(i);
            String name = pak.packageName;
                list.add(name);

        }
        if (!list.isEmpty() && !getRemoteAPPlist().isEmpty()) {
            Iterator<String> ite = list.iterator();
            ArrayList<String> remouteList = getRemoteAPPlist();
            while (ite.hasNext()) {
                String packageName = ite.next();
                for (String remotePattern : remouteList) {
                    if (packageName.contains(remotePattern)) {
                        PackageInfo pak = null;
                        try {
                            if(pManager.getLaunchIntentForPackage(packageName)!=null) {
                                pak = pManager.getPackageInfo(packageName, 0);
                                if (pak != null) {
                                    Drawable drawable = pak.applicationInfo.loadIcon(pManager);
                                    compareResult.put(packageName, drawable);

                                    break;
                                }
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (compareResult.size() == 5)
                    break;
            }
        }
        return compareResult;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (acbExpressAdView != null) {
            acbExpressAdView.destroy();
            acbExpressAdView = null;
        }
        getMatchedAppsAsyncTask.cancel(true);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.isNotification) {
            showPopwindow();
        } else if (i == R.id.close) {
            finish();
        } else if (i == R.id.disable) {
            HeadSetManager.getInstance().setEnable(false);
            Toast.makeText(this, "no display this window when plug headset", Toast.LENGTH_LONG).show();
            if(isEnablePopupWindow.isShowing()&!isFinishing())
            isEnablePopupWindow.dismiss();
        } else if (i == R.id.not_now) {
            HeadSetManager.getInstance().setEnable(true);
            Toast.makeText(this, "display this window when plug headset", Toast.LENGTH_LONG).show();
            if(isEnablePopupWindow.isShowing()&!isFinishing())
            isEnablePopupWindow.dismiss();
        }
    }

    private void showPopwindow() {
        if (isEnablePopupWindow == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.headset_isnotification_pop, null);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            isEnablePopupWindow = new PopupWindow(contentView, (int) (width * 0.8), LinearLayout.LayoutParams.WRAP_CONTENT);
            isEnablePopupWindow.setFocusable(true);
            isEnablePopupWindow.setContentView(contentView);
            isEnablePopupWindow.setBackgroundDrawable(new BitmapDrawable());
            isEnablePopupWindow.setOutsideTouchable(true);
            isEnablePopupWindow.setTouchable(true);
            cancle = (Button) contentView.findViewById(R.id.disable);
            noNotification = (Button) contentView.findViewById(R.id.not_now);
            cancle.setOnClickListener(this);
            noNotification.setOnClickListener(this);
        }
        if (!isFinishing() && !isEnablePopupWindow.isShowing())
            isEnablePopupWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekBar.setProgress(currentVolume);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStartTrackingTouch       " + "   seekBar.getProgress() " + seekBar.getProgress() + "    voiceseekBar.getProgress() " + voiceseekBar.getProgress());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d(TAG, "onStopTrackingTouch       " + "   seekBar.getProgress() " + seekBar.getProgress() + "    voiceseekBar.getProgress() " + voiceseekBar.getProgress());

    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "HeadSetReceiver  onReceive  headset==========" + intent.getAction());
            String action = intent.getAction();
            if (action.equals(VOICE_CHANGE)) {
                int currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                voiceseekBar.setProgress(currentVolume);
            } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int current = intent.getExtras().getInt("level");
                int total = intent.getExtras().getInt("scale");
                percent = current * 100 / total;
                music.setText(String.valueOf(percent * 5));
                movie.setText(String.valueOf(percent * 3));
            } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                HeadSetActivity.this.finish();
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (isEnablePopupWindow != null && isEnablePopupWindow.isShowing()) {
                isEnablePopupWindow.dismiss();
            }
            return super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
        return true;
    }

}
