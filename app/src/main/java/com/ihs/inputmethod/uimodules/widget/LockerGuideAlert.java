package com.ihs.inputmethod.uimodules.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.uimodules.R;

import net.appcloudbox.autopilot.AutopilotConfig;

import java.util.ArrayList;
import java.util.List;

public class LockerGuideAlert extends AlertDialog implements View.OnClickListener {
    private static final int TYPE_0 = 0;
    private static final int TYPE_1 = 1;

    private View.OnClickListener enableClickListener;

    public LockerGuideAlert(@NonNull Context context) {
        super(context, R.style.LockerGuideDialog);
    }

    public void setEnableClickListener(View.OnClickListener enableClickListener) {
        this.enableClickListener = enableClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locker_guide_alert);

        TextView title = findViewById(R.id.title);
        title.setText(HSConfig.optString(getContext().getResources().getString(R.string.locker_alert_title), "Application", "DownloadScreenLocker", "title"));

        Button enableBtn = findViewById(R.id.enable_btn);
        enableBtn.setText(HSConfig.optString(getContext().getResources().getString(R.string.enable_now), "Application", "DownloadScreenLocker", "button"));
        enableBtn.setOnClickListener(this);

        ImageView closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(this);

        ImageView headBg = findViewById(R.id.head_bg);
        ImageView bottomBg = findViewById(R.id.bottom_bg);

        if (!(getContext() instanceof Activity)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(HSApplication.getContext())) {
                getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            } else {
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        int type = Double.valueOf(AutopilotConfig.getDoubleToTestNow("topic-1512033355055", "ui_type", TYPE_0)).intValue();
        type = 1;//test

        List<String> text = (List<String>) HSConfig.getList("Application", "DownloadScreenLocker", "body");
        if (text == null || text.size() == 0) {
            text = new ArrayList<>();
            text.add("Security");
            text.add("Efficiency");
            text.add("Personalization");
            text.add("Small size");
        }

        ItemAdapter adapter = new ItemAdapter(text, type);

        int width = HSDisplayUtils.getScreenWidthForContent();

        if (type == TYPE_0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setPadding((int) (100.0 * width / 1080), 0, (int) (80.0 * width / 1080), 0);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 1;
                }
            });
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setBackgroundResource(R.drawable.locker_guide_alert_style1_middle_bg);
            recyclerView.setPadding((int) (107.0 * width / 1080), 0, (int) (86.0 * width / 1080), 0);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) enableBtn.getLayoutParams();
            layoutParams.gravity = Gravity.CENTER;

            FrameLayout.LayoutParams titleLayoutParms = (FrameLayout.LayoutParams) title.getLayoutParams();
            titleLayoutParms.topMargin = HSDisplayUtils.dip2px(100);

            headBg.setImageResource(R.drawable.locker_guide_alert_style1_head);
            bottomBg.setImageResource(R.drawable.locker_guide_alert_style1_bottom);
        }
        recyclerView.setAdapter(adapter);
    }

    private class ItemAdapter extends RecyclerView.Adapter<VHolder> {
        List<String> data;
        int type;

        public ItemAdapter(List<String> data, int type) {
            this.data = data;
            this.type = type;
        }

        @Override
        public VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            VHolder vHolder = new VHolder(View.inflate(parent.getContext(), R.layout.item_locker_introduce, null));
            int width = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels - parent.getPaddingLeft() - parent.getPaddingRight();
            HSLog.d("cjx", "parent.getMeasuredWidth():" + width + ",parent.getPaddingLeft():" + parent.getPaddingLeft() + ",parent.getPaddingRight():" + parent.getPaddingRight());
            if (type == TYPE_1) {
                vHolder.itemView.setLayoutParams(new ViewGroup.LayoutParams(width / 2, ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                vHolder.itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            return vHolder;
        }

        @Override
        public void onBindViewHolder(VHolder holder, int position) {
            String text = data.get(position);
            holder.text.setText(text);
        }

        @Override
        public int getItemCount() {
            if (data != null) {
                return data.size();
            }
            return 0;
        }

    }


    public static class VHolder extends RecyclerView.ViewHolder {
        TextView text;

        public VHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        dismiss();

        if (id == R.id.enable_btn) {
            if (enableClickListener != null) {
                enableClickListener.onClick(v);
            }
        } else if (id == R.id.close_btn) {

        }
    }


    @Override
    public void show() {
        try {
            super.show();
            /**
             * 设置dialog宽度全屏
             */
            WindowManager.LayoutParams params = getWindow().getAttributes();  //获取对话框当前的参数值、
            params.width = HSDisplayUtils.getScreenWidthForContent();    //宽度设置全屏宽度
            getWindow().setAttributes(params);     //设置生效
        } catch (Exception e) {
        }
    }
}
