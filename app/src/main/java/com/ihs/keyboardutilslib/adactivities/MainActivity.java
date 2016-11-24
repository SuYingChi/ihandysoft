package com.ihs.keyboardutilslib.adactivities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.keyboardutils.nativeads.NativeAdConfig;
import com.ihs.keyboardutils.nativeads.NativeAdManager;
import com.ihs.keyboardutils.nativeads.NativeAdProfile;
import com.ihs.keyboardutils.nativeads.NativeAdProvider;
import com.ihs.keyboardutils.nativeads.NativeAdView;
import com.ihs.keyboardutilslib.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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


        for (String poolName : NativeAdConfig.getAvailablePoolNames()) {
            data_list.add(poolName);
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
                for (String profile : NativeAdProfile.getAllNativeAdPoolState()) {
                    poolStates.add(profile);
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
    }

    public void showAd(View view) {
        poolName = data_list.get(adPoolName.getSelectedItemPosition()).trim();
        adTimes.clear();
        timeAdapter.notifyDataSetChanged();
        if (adContainer.findViewWithTag("Refresh") != null) {
            adContainer.removeView(refreshNativeAdView);
        }
        View view1 = LayoutInflater.from(this).inflate(R.layout.ad_style_1, null);
        new NativeAdProvider(new NativeAdProvider.NativeAdViewListener() {
            @Override
            public void NativeAdViewPrepared(NativeAdView nativeAdView) {
                refreshNativeAdView = nativeAdView;
                refreshNativeAdView.setTag("Refresh");
                adContainer.addView(refreshNativeAdView);
            }
        }).createNativeAdView(this, view1, poolName, NativeAdConfig.getNativeAdFrequency());

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (refreshNativeAdView != null) {
            refreshNativeAdView.release();
        }
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
