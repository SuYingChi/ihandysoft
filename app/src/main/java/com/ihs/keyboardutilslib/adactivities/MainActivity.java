package com.ihs.keyboardutilslib.adactivities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.ihs.keyboardutils.nativeads.NativeAdConfig;
import com.ihs.keyboardutilslib.R;
import com.ihs.keyboardutils.nativeads.NativeAdManager;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ihandysoft on 16/10/24.
 */

public class MainActivity extends HSActivity {

    private LinearLayout adContainer;
    private Button btnPoolStates;
    private Spinner adPoolName;

    private TextView adPoolInfo;
    private ListView adShowTimes;
    private TimeAdapter timeAdapter;

    private List<String> adTimes = new ArrayList<>();

    private List<String> data_list = new ArrayList<>();

    private ArrayAdapter<String> arr_adapter;


    private String poolName;

    private NativeAdView refreshNativeAdView;

    private ArrayList<NativeAdManager.NativeAdProxy> getAllPoolState() {
        NativeAdManager nativeAdManager = NativeAdManager.getInstance();
        try {
            Method method = nativeAdManager.getClass().getDeclaredMethod("getAllPoolState");
            method.setAccessible(true);
            return (ArrayList<NativeAdManager.NativeAdProxy>) method.invoke(nativeAdManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_main_activity);
        adContainer = (LinearLayout) findViewById(R.id.ad_container);
        btnPoolStates = (Button) findViewById(R.id.btn_pool_state);
        adPoolName = (Spinner) findViewById(R.id.spinner_poolname);
        adPoolInfo = (TextView) findViewById(R.id.tv_pool_info);
        adShowTimes = (ListView) findViewById(R.id.lv_pool_ad_showTime);
        timeAdapter = new TimeAdapter(this, adTimes);
        adShowTimes.setAdapter(timeAdapter);

        refreshNativeAdView = new NativeAdView(MainActivity.this);
        refreshNativeAdView.setTag("Refresh");

        for (NativeAdManager.NativeAdProxy nativeAdProxy : getAllPoolState()) {
            String nativeAdPoolState = nativeAdProxy.toString();
            data_list.add(nativeAdPoolState.substring(0, nativeAdPoolState.indexOf("(")));
        }


        //适配器
        arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
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
                ArrayList<String> poolStates = new ArrayList<String>();
                for (NativeAdManager.NativeAdProxy nativeAdProxy : getAllPoolState()) {
                    poolStates.add(nativeAdProxy.toString());
                }
                String[] arrPoolStates = new String[poolStates.size()];
                arrPoolStates = poolStates.toArray(arrPoolStates);
                builder.setItems(arrPoolStates, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });
    }

    public void hideAd(View view) {
        new AlertDialog.Builder(this).setTitle("Test").show();
        //startActivity(new Intent(this, SecondActivity.class));
    }

    public void showAd(View view) {
        poolName = data_list.get(adPoolName.getSelectedItemPosition()).trim();

        for (NativeAdManager.NativeAdProxy nativeAdProxy : getAllPoolState()) {
            if (nativeAdProxy.toString().startsWith(poolName)) {
                adPoolInfo.setText(nativeAdProxy.toString());
                break;
            }
        }
        adTimes.clear();
        timeAdapter.notifyDataSetChanged();
        if (NativeAdManager.getInstance().existNativeAd(poolName)) {
            refreshNativeAdView.setConfigParams(poolName, R.layout.ad_style_1_demo, NativeAdConfig.getNativeAdFrequency());
            if(adContainer.findViewWithTag("Refresh") == null) {
                adContainer.addView(refreshNativeAdView);
            }
        } else {
            HSGlobalNotificationCenter.addObserver(NativeAdManager.NOTIFICATION_NEW_AD, iNotificationObserver);
        }
    }

    INotificationObserver iNotificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (NativeAdManager.NOTIFICATION_NEW_AD.equals(s)) {
                if (poolName.equals(hsBundle.getString(NativeAdManager.NATIVE_AD_POOL_NAME))) {
                    HSGlobalNotificationCenter.removeObserver(NativeAdManager.NOTIFICATION_NEW_AD, iNotificationObserver);
                    refreshNativeAdView.setConfigParams(poolName, R.layout.ad_style_1_demo, NativeAdConfig.getNativeAdFrequency());
                    if (adContainer.findViewWithTag("Refresh") == null) {
                        adContainer.addView(refreshNativeAdView);
                    }
                }
            }
            else if(NativeAdView.NOTIFICATION_NATIVE_AD_SHOWED.equals(s)){
                for (NativeAdManager.NativeAdProxy nativeAdProxy : getAllPoolState()) {
                    if (nativeAdProxy.toString().startsWith(poolName)) {
                        adPoolInfo.setText(nativeAdProxy.toString());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        adTimes.add(0, simpleDateFormat.format(new Date()) + " : " + (hsBundle.getBoolean("Flag") ? "old" : "new"));
                        timeAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
            else if(NativeAdView.NOTIFICATION_NATIVE_AD_CLIKED.equals(s)) {

            }
        }
    };

    @Override
    protected void onPause() {
        HSGlobalNotificationCenter.removeObserver(iNotificationObserver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        HSGlobalNotificationCenter.addObserver(NativeAdView.NOTIFICATION_NATIVE_AD_SHOWED, iNotificationObserver);
        HSGlobalNotificationCenter.addObserver(NativeAdView.NOTIFICATION_NATIVE_AD_CLIKED, iNotificationObserver);
    }

    @Override
    protected void onDestroy() {
        refreshNativeAdView.release();
        super.onDestroy();
    }

    static class TimeAdapter extends BaseAdapter {

        private Context context;
        private final List<String> times;

        public TimeAdapter(Context context, List<String> times) {
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
            if (convertView == null) {
                convertView = new TextView(context);
                holder = new ViewHolder();
                holder.time = (TextView) convertView;
                convertView.setTag(holder);
            } else {
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
