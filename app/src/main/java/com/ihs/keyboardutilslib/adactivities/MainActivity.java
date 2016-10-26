package com.ihs.keyboardutilslib.adactivities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.keyboardutilslib.R;
import com.ihs.keyboardutils.nativeads.NativeAdManager;
import com.ihs.keyboardutils.nativeads.RefreshableNativeAdView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ihandysoft on 16/10/24.
 */

public class MainActivity extends HSActivity implements INotificationObserver {

    private LinearLayout adContainer;
    private Button btnPoolStates;
    private Spinner adPoolName;

    private TextView adFirmName;
    private TextView adPoolCount;
    private TextView adPoolCountUsed;
    private ListView adShowTimes;
    private TimeAdapter timeAdapter;
    private boolean isNew = false;

    private List<String> adTimes = new ArrayList<>();

    private List<String> data_list = new ArrayList<>();
    private ArrayAdapter<String> arr_adapter;


    private String poolName;

    private RefreshableNativeAdView refreshNativeAdView;

    private String[] getAllPoolState() {
        NativeAdManager nativeAdManager = NativeAdManager.getInstance();
        try {
            Method method = nativeAdManager.getClass().getDeclaredMethod("getAllPoolState", new Class<?>[]{});
            method.setAccessible(true);
            return (String[]) method.invoke(nativeAdManager, new Object[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return new String[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_main_activity);
        adContainer = (LinearLayout) findViewById(R.id.ad_container);
        btnPoolStates = (Button) findViewById(R.id.btn_pool_state);
        adPoolName = (Spinner) findViewById(R.id.spinner_poolname);
        adFirmName = (TextView) findViewById(R.id.tv_firm_name);
        adPoolCount = (TextView) findViewById(R.id.tv_pool_ad_count);
        adPoolCountUsed = (TextView) findViewById(R.id.tv_pool_ad_count_used);
        adShowTimes = (ListView) findViewById(R.id.lv_pool_ad_showTime);
        timeAdapter = new TimeAdapter(this, adTimes);
        adShowTimes.setAdapter(timeAdapter);



        for(String key : getAllPoolState()){
            data_list.add(key.substring(0, key.indexOf("-")));
        }


        //适配器
        arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        adPoolName.setAdapter(arr_adapter);
        adPoolName.setSelection(0);


        btnPoolStates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("广告池状态");
                //    设置一个下拉的列表选择项
                builder.setItems(getAllPoolState(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });
    }

    private void addObserver() {
        HSGlobalNotificationCenter.removeObserver(this);
        HSGlobalNotificationCenter.addObserver(poolName, this);
        HSGlobalNotificationCenter.addObserver(poolName + "-new", this);
        HSGlobalNotificationCenter.addObserver(poolName + "-old", this);

    }

    public void goSecondActivity(View view){
        startActivity(new Intent(this, SecondActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        addObserver();
    }

    @Override
    protected void onPause() {
        HSGlobalNotificationCenter.removeObserver(this);
        super.onPause();
    }

    public void startFetchNativeAd(View view){
        adContainer.removeAllViews();
        poolName = data_list.get(adPoolName.getSelectedItemPosition()).trim();

        for(String key : getAllPoolState()){
            if(key.startsWith(poolName)){
                adPoolCount.setText(key.split(" - ")[2]);
                adPoolCountUsed.setText(key.split(" - ")[1]);
            }
        }
        addObserver();
        adTimes.clear();
        timeAdapter.notifyDataSetChanged();
        refreshNativeAdView = new RefreshableNativeAdView(this, poolName, R.layout.ad_style_1, getAdFrequency());
        adContainer.addView(refreshNativeAdView);
    }

    public int getAdFrequency(){
        return HSConfig.optInteger(0,"Application", "NativeAds", "fetchAdInterval");
    }



    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if(s.equals(poolName)){
            adPoolCount.setText("" + hsBundle.getInt("PoolCount"));
        }
        else if(s.startsWith(poolName + "-")) {
            adPoolCountUsed.setText("" + hsBundle.getInt("HasShowedCount"));
            adFirmName.setText(hsBundle.getString("FirmName"));
            adTimes.add(0, hsBundle.getString("ShowNativeAdTime"));
            timeAdapter.notifyDataSetChanged();
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
