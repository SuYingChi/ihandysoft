package com.ihs.keyboardutilslib.adactivities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutilslib.R;
import com.ihs.keyboardutilslib.nativeads.NativeAdManager;
import com.ihs.keyboardutilslib.nativeads.RefreshNativeAdView;
import com.ihs.nativeads.base.api.HSNativeAd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ihandysoft on 16/10/24.
 */

public class MainActivity extends HSActivity implements INotificationObserver {

    private LinearLayout adContainer;
    private EditText adFetchInterval;
    private EditText adPoolName;

    private TextView adFirmName;
    private TextView adPoolCount;
    private TextView adPoolCountUsed;
    private ListView adShowTimes;
    private TimeAdapter timeAdapter;
    private boolean isNew = false;

    private List<String> adTimes = new ArrayList<>();


    private int adInterval;
    private String poolName;

    private RefreshNativeAdView refreshNativeAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_main_activity);
        adContainer = (LinearLayout) findViewById(R.id.ad_container);
        adFetchInterval = (EditText) findViewById(R.id.et_ad_fetch_interval);
        adPoolName = (EditText) findViewById(R.id.et_poolname);
        adFirmName = (TextView) findViewById(R.id.tv_firm_name);
        adPoolCount = (TextView) findViewById(R.id.tv_pool_ad_count);
        adPoolCountUsed = (TextView) findViewById(R.id.tv_pool_ad_count_used);
        adShowTimes = (ListView) findViewById(R.id.lv_pool_ad_showTime);
        timeAdapter = new TimeAdapter(this, adTimes);
        adShowTimes.setAdapter(timeAdapter);
    }

    private void addObserver() {
        HSGlobalNotificationCenter.removeObserver(this);
        HSGlobalNotificationCenter.addObserver(poolName, this);
        HSGlobalNotificationCenter.addObserver(poolName + "-new", this);
        HSGlobalNotificationCenter.addObserver(poolName + "-old", this);

    }

    @Override
    protected void onPause() {
        HSGlobalNotificationCenter.removeObserver(this);
        super.onPause();
    }

    public void startFetchNativeAd(View view){
        adContainer.removeAllViews();
        adInterval = Integer.valueOf(adFetchInterval.getText().toString());
        poolName = adPoolName.getText().toString();
        adTimes.clear();
        timeAdapter.notifyDataSetChanged();
        addObserver();
        refreshNativeAdView = new RefreshNativeAdView(this, poolName, R.layout.ad_style_1, adInterval, new RefreshNativeAdView.NativeAdListener() {
            @Override
            public void onNativeAdShowed(final HSNativeAd hsNativeAd) {
                HSLog.e("dddsdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfadfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasddf");
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        adFirmName.setText(hsNativeAd.getVendor().name());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        adTimes.add(simpleDateFormat.format(new Date()) + (isNew ? ": new" : ": old"));
                        adShowTimes.setAdapter(new TimeAdapter(MainActivity.this, adTimes));
                    }
                });
            }

            @Override
            public void onNativeAdClicked(HSNativeAd hsNativeAd) {

            }
        });
        adContainer.addView(refreshNativeAdView);
    }

    @Override
    protected void onDestroy() {
        NativeAdManager.getInstance().releaseAllNativeAdPools();
        super.onDestroy();
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if(s.equals(poolName)){
            adPoolCount.setText("" + hsBundle.getInt("PoolCount"));
        }
        else if(s.equals(poolName + "-new")) {
            adPoolCountUsed.setText("" + hsBundle.getInt("HasShowedCount"));
            isNew = true;
        }
        else if(s.equals(poolName + "-old")) {
            isNew = false;
        }
    }


    static class TimeAdapter extends BaseAdapter {

        private Context context;
        private final List<String> times;

        public TimeAdapter(Context context, List<String> times){
            this.times = times;
            this.context = context;
        }

        @Override
        public int getCount() {
            return times.size();
        }

        @Override
        public Object getItem(int position) {
            return times.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null){
                convertView = new TextView(context);
                holder = new ViewHolder();
                holder.time = (TextView) convertView;
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.time.setText(times.get(position));
            return convertView;
        }

        static class ViewHolder {
            public TextView time;
        }
    }
}
