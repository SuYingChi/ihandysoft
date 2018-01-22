package com.ihs.keyboardutils.alerts;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
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

import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
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


/**
 * Created by yingchi.su on 2018/1/12.
 */

public class HeadSetActivity extends HSActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, INotificationObserver {
    private static final int ADD_APP = 2;
    private static final int DELETE_APP = 3;
    public String TAG = "HeadSetActivity";
    private SeekBar voiceseekBar;
    private AudioManager am;
    private BroadcastReceiver mReceiver;
    private ImageView ifNotification;
    private ImageView closeButton;
    private PopupWindow mPopWindow;
    private AcbExpressAdView acbExpressAdView;
    private LinearLayout installAppViewGroup;
    private int percent = 0;
    private TextView music;
    private TextView movie;
    private View rootview;
    private LinearLayout noadv;
    private RoundedCornerLayout adContainer;
    private HashMap<String, Drawable> lastMatchAppMap = new HashMap<String, Drawable>();
    private HashMap<String, Drawable> currentMatchAppMap = new HashMap<String, Drawable>();
    private Button cancle;
    private Button noNotification;
    private ArrayList<String> allInstallList = new ArrayList<String>();
    private HandlerThread mHandlerThead;
    private static Handler mUIHandler;
    private Handler childHandler;
    private static final int START_WORK = 1;
    private final String VOICE_CHANGE = "android.media.VOLUME_CHANGED_ACTION";

    public HeadSetActivity() {

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headsetlayout);
        mReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOICE_CHANGE);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mReceiver, filter);
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, this);
        mUIHandler = new Handler();
        initview();
        //初始化获取app信息并且与remote比对的子线程task
        initAppsinfoAndCompared();
        //开启工作任务
        childHandler.sendEmptyMessage(START_WORK);
        creatAdv();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(currentMatchAppMap.isEmpty()){
            childHandler.sendEmptyMessage(START_WORK);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        showAdv();

        if (acbExpressAdView != null) {
            acbExpressAdView.setVisibility(View.VISIBLE);
            acbExpressAdView.switchAd();
        }else {
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
        ifNotification = (ImageView) findViewById(R.id.ifNotification);
        ifNotification.setOnClickListener(this);
        closeButton = (ImageView)findViewById(R.id.close);
        closeButton.setOnClickListener(this);
        installAppViewGroup = (LinearLayout) findViewById(R.id.installed_app);
        adContainer = (RoundedCornerLayout) findViewById(R.id.adsContainer);
        noadv = (LinearLayout) findViewById(R.id.noadv);
        music = (TextView) findViewById(R.id.musicRemain);
        movie = (TextView) findViewById(R.id.movieRemain);
        rootview = LayoutInflater.from(this).inflate(R.layout.headsetlayout, null);
    }

    private void creatAdv() {
        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased() & !TextUtils.isEmpty(HeadSetManager.getInstance().getHeadSetAdPlaceMent())&acbExpressAdView==null) {
            //使用lumen测试
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
            }else {
                noadv.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showInstallAPP(HashMap<String, Drawable> MatchAppMap) {
        if (!MatchAppMap.isEmpty()) {
            installAppViewGroup.setVisibility(View.VISIBLE);
            Iterator<Map.Entry<String, Drawable>> ite = MatchAppMap.entrySet().iterator();
            for (int i = 0; i < MatchAppMap.size() && ite.hasNext(); i++) {
                Map.Entry<String, Drawable> iteEntry = ite.next();
                Drawable icon = iteEntry.getValue();
                String name = iteEntry.getKey();
                RoundCornerImageView roundCornerImageView = (RoundCornerImageView) installAppViewGroup.getChildAt(i);
                roundCornerImageView.setVisibility(View.VISIBLE);
                roundCornerImageView.setImageDrawable(icon);
                roundCornerImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openPackage(HeadSetActivity.this, name);
                    }
                });
            }
            if(MatchAppMap.size()<5) {
                for (int i = MatchAppMap.size(); i < 5; i++) {
                    RoundCornerImageView roundCornerImageView = (RoundCornerImageView) installAppViewGroup.getChildAt(i);
                  roundCornerImageView.setVisibility(View.INVISIBLE);
                }
            }
        }

    }

    private void isReloadInstallApp() {
        if (!currentMatchAppMap.isEmpty() && !currentMatchAppMap.equals(lastMatchAppMap)) {
            showInstallAPP(currentMatchAppMap);
        } else if (currentMatchAppMap.isEmpty()) {
            installAppViewGroup.setVisibility(View.GONE);
        }
        lastMatchAppMap.putAll(currentMatchAppMap);
    }

    private void initAppsinfoAndCompared() {
        mHandlerThead = new HandlerThread("getInstalledAppInfoAndCompared");
        mHandlerThead.start();
        childHandler = new Handler(mHandlerThead.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case START_WORK:
                        allInstallList = getAllAppsInfo();
                        currentMatchAppMap = compareRemote();
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                isReloadInstallApp();
                                    }
                                });
                        break;
                    case ADD_APP:
                        String packageName =(String) msg.obj;
                        allInstallList.add(packageName);
                        if(compareRemoteAdd(packageName)){
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    isReloadInstallApp();
                                }
                            });
                        }
                        break;
                    case DELETE_APP:
                        String package_Name =(String) msg.obj;
                        allInstallList.remove(package_Name);
                        if(compareRemoteRemoved(package_Name)){
                            mUIHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    isReloadInstallApp();
                                }
                            });
                        }
                        break;
                }
            }
        };
    }

    //
    private ArrayList<String> getRemoteAPPlist() {
        ArrayList<String> list = (ArrayList<String>) HSConfig.getList("Application", "RemoteAppPackageName");
        return list;
    }
    private HashMap<String, Drawable> compareRemote() {
        HashMap<String, Drawable> compareResult = new HashMap<String, Drawable>();
        if (!allInstallList.isEmpty()&&!getRemoteAPPlist().isEmpty()) {
            Iterator<String> ite = allInstallList.iterator();
            while (ite.hasNext()) {
                String packageName = ite.next();
                for (String remotePattern : getRemoteAPPlist()) {
                    if (packageName.contains(remotePattern)) {

                        PackageManager pManager = getPackageManager();
                        PackageInfo pak=null;
                        try {
                            pak = pManager.getPackageInfo(packageName,0);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(pak!=null) {
                            Drawable drawable = pak.applicationInfo.loadIcon(pManager);
                            compareResult.put(packageName, drawable);
                            break;
                        }
                    }
                }
                if(compareResult.size()==5)
                    break;
            }
        }
        return compareResult;
    }
    private boolean compareRemoteAdd(String packageName) {

        if (getRemoteAPPlist().contains(packageName)) {
            PackageManager pManager = getPackageManager();
            PackageInfo pak = null;
            try {
                pak = pManager.getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (pak != null) {
                Drawable drawable = pak.applicationInfo.loadIcon(pManager);
                if(currentMatchAppMap.size()<5) {
                    currentMatchAppMap.put(packageName, drawable);
                    return true;
                }
            }
        }
        return false;
    }
    private boolean compareRemoteRemoved(String packageName) {

        if (currentMatchAppMap.keySet().contains(packageName)) {
            PackageManager pManager = getPackageManager();
            PackageInfo pak = null;
            try {
                pak = pManager.getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (pak != null) {
                Drawable drawable = pak.applicationInfo.loadIcon(pManager);
                currentMatchAppMap.remove(packageName);
                return true;
            }
        }
        return false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "unregisterReceiver  HeadSetReceiver");
        unregisterReceiver(mReceiver);
        HSGlobalNotificationCenter.removeObserver(this);
        if (acbExpressAdView != null) {
            acbExpressAdView.destroy();
            acbExpressAdView = null;
        }
        mHandlerThead.quit();
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ifNotification) {
            showPopwindow();
        } else if (i == R.id.close) {
            finish();
        } else if (i == R.id.cancle) {
            HeadSetManager.getInstance().setOpen(false);
        } else if (i == R.id.no_notification) {
            HeadSetManager.getInstance().setOpen(true);
            Toast.makeText(this,"no display this window when plug headset",Toast.LENGTH_LONG).show();
            mPopWindow.dismiss();
        } else if (i == R.id.no_notification) {
            HeadSetManager.getInstance().setOpen(true);
            Toast.makeText(this," display this window when plug headset",Toast.LENGTH_LONG).show();
            mPopWindow.dismiss();
        }
    }

    private void showPopwindow() {
        if (mPopWindow == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.headset_isnotification_pop, null);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int width = metrics.widthPixels;
            mPopWindow = new PopupWindow(contentView, (int) (width * 0.8), LinearLayout.LayoutParams.WRAP_CONTENT);
            mPopWindow.setFocusable(true);
            mPopWindow.setContentView(contentView);
            mPopWindow.setBackgroundDrawable(new BitmapDrawable());
            mPopWindow.setOutsideTouchable(true);
            mPopWindow.setTouchable(true);
            cancle = (Button) contentView.findViewById(R.id.cancle);
            noNotification = (Button) contentView.findViewById(R.id.no_notification);
            cancle.setOnClickListener(this);
            noNotification.setOnClickListener(this);
        }
        if (!mPopWindow.isShowing())
            mPopWindow.showAtLocation(rootview, Gravity.CENTER, 0, 0);

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

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (TextUtils.equals(s, HSNotificationConstant.HS_CONFIG_CHANGED)) {
            childHandler.sendEmptyMessage(START_WORK);
        }
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
            } else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                Message message =new Message();
                message.obj = intent.getData().getEncodedSchemeSpecificPart();
                message.what = ADD_APP;
                childHandler.sendMessage(message);
            } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                final String packageName = intent.getData().getEncodedSchemeSpecificPart();
                if (lastMatchAppMap.keySet().contains(packageName)) {
                    Message message =new Message();
                    message.obj = intent.getData().getEncodedSchemeSpecificPart();
                    message.what = DELETE_APP;
                    childHandler.sendMessage(message);
                }
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
            if (mPopWindow != null && mPopWindow.isShowing()) {
                mPopWindow.dismiss();
            }
            return super.dispatchKeyEvent(event);
        } else {
            return super.dispatchKeyEvent(event);
        }
        return true;
    }

    private ArrayList<String> getAllAppsInfo() {
        ArrayList<String> list = new ArrayList<String>();
        PackageManager pManager = getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);
        for (int i = 0; i < paklist.size(); i++) {
            PackageInfo pak = paklist.get(i);
            String name = pak.packageName;
            if(pManager.getLaunchIntentForPackage(name) != null)
            list.add(name);

        }
        return list;
    }

    // 根据包名寻找MainActivity
    private static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant")
        List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    // 创建第三方应用的上下文环境
    private static Context getPackageContext(Context context, String packageName) {
        Context pkgContext = null;
        if (context.getPackageName().equals(packageName)) {
            pkgContext = context;
        } else {
            try {
                pkgContext = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return pkgContext;
    }

    private boolean openPackage(Context context, String packageName) {
        Context pkgContext = getPackageContext(context, packageName);
        Intent intent = getAppOpenIntentByPackageName(context, packageName);
        if (pkgContext != null && intent != null) {
            pkgContext.startActivity(intent);
            return true;
        }
        return false;
    }

}
