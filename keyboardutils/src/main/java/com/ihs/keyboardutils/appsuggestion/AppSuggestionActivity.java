package com.ihs.keyboardutils.appsuggestion;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.nativeads.KCNativeAdView;
import com.ihs.keyboardutils.utils.RippleDrawableUtils;
import com.ihs.keyboardutils.utils.ToastUtils;

/**
 * Created by Arthur on 17/12/9.
 */

public class AppSuggestionActivity extends Activity {

    private RecyclerView.Adapter adapter = new RecyclerView.Adapter() {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_suggestion_items, parent, false);
            return new itemHolder(inflate);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((itemHolder) holder).getTitle().setText("app" + position);
            ((itemHolder) holder).getIcon().setImageResource(R.drawable.boost_icon);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return 5;
        }

        class itemHolder extends RecyclerView.ViewHolder {
            TextView title;
            ImageView icon;

            public itemHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.tv_title);
                icon = itemView.findViewById(R.id.iv_icon);
            }

            public TextView getTitle() {
                return title;
            }

            public ImageView getIcon() {
                return icon;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        View mainView = View.inflate(HSApplication.getContext(), R.layout.app_suggestion_main, null);
        setContentView(mainView);

        RecyclerView listView = mainView.findViewById(R.id.recycler_view);
//        listView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        listView.setLayoutManager(new GridLayoutManager(this,5));
        listView.setAdapter(adapter);
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        ImageView ivClose = mainView.findViewById(R.id.iv_close);
        ivClose.setBackgroundDrawable(RippleDrawableUtils.getTransparentRippleBackground());
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView tvCall = mainView.findViewById(R.id.tv_call);
        tvCall.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.app_suggestion_call_btn));
        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, null);
                    startActivity(callIntent);
                } catch (Exception e) {
                    HSLog.d("open call failed");
                }
            }
        });

        TextView tvMsg = mainView.findViewById(R.id.tv_msg);
        tvMsg.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.app_suggestion_message_btn));
        tvMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              sendSMS();
            }
        });

        TextView tvSearch = mainView.findViewById(R.id.tv_search);
        tvSearch.setBackgroundDrawable(RippleDrawableUtils.getButtonRippleBackground(R.color.app_suggestion_search_btn));
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showToast("need complete");
            }
        });


        KCNativeAdView nativeAdView = new KCNativeAdView(HSApplication.getContext());
        nativeAdView.setAdLayoutView(View.inflate(this,R.layout.locker_ad_card,null));
        nativeAdView.load(getString(R.string.ad_placement_call_assist));

        FrameLayout adContainer = findViewById(R.id.alert_ad_container);
        adContainer.addView(nativeAdView);
    }

    public static void showAppSuggestion() {
        Intent intent = new Intent(HSApplication.getContext(), AppSuggestionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        HSApplication.getContext().startActivity(intent);
    }


    private void sendSMS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) // At least KitKat
        {
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(this); // Need to change the build to API 19
            Intent sendIntent = new Intent();
            sendIntent.setPackage(defaultSmsPackageName);
            startActivity(sendIntent);
        }
        else // For early versions, do what worked for you before.
        {
            Intent smsIntent = new Intent(android.content.Intent.ACTION_VIEW);
            smsIntent.setType("vnd.android-dir/mms-sms");
            startActivity(smsIntent);
        }
    }
}
