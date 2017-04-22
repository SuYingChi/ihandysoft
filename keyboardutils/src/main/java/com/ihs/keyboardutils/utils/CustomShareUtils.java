package com.ihs.keyboardutils.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jixiang on 17/4/12.
 */

public class CustomShareUtils {
    private final static float SHARE_COLUMN_ITEM_COUNT_PORTRAIT = 4.5f;
    private final static float SHARE_COLUMN_ITEM_COUNT_LANDSCAPE = 7.5f;

    public static Dialog shareImage(final Activity activity, Uri uri, String adPlaceName) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        return shareImage(activity, shareIntent, uri, adPlaceName);
    }

    public static Dialog shareImage(final Activity activity, ArrayList<Uri> uriList, String adPlaceName) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
        shareIntent.setType("image/*");
        return shareImage(activity, shareIntent, uriList.get(0), adPlaceName);
    }

    private static Dialog shareImage(final Activity activity, Intent shareIntent, Uri previewUri, String adPlaceName) {
        Resources resources = activity.getResources();

        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(shareIntent, 0);
        resolveInfoList = getFilteredShareList(resolveInfoList);

        View view = View.inflate(activity, R.layout.share_layout, null);
        FrameLayout shareAdContainer = (FrameLayout) view.findViewById(R.id.share_ad_container);

        View adLoadingView = View.inflate(activity, R.layout.ad_default_loading, null);
        int itemWidth = (int) (resources.getDisplayMetrics().widthPixels * 1.0f / SHARE_COLUMN_ITEM_COUNT_PORTRAIT) - resources.getDimensionPixelSize(R.dimen.share_item_column_space);
        //处理横屏
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            itemWidth = (int) ((resources.getDisplayMetrics().widthPixels * 1.0f / SHARE_COLUMN_ITEM_COUNT_LANDSCAPE)) - resources.getDimensionPixelSize(R.dimen.share_item_column_space);
            adLoadingView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (resources.getDisplayMetrics().heightPixels - resources.getDimension(R.dimen.share_layout_title_height)
                    - resources.getDimension(R.dimen.share_layout_recycler_margin_top) - resources.getDimension(R.dimen.share_layout_recycler_margin_bottom) - itemWidth)));
        } else {
            adLoadingView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (resources.getDisplayMetrics().widthPixels * 1.0f / 1.9f
                    + Math.abs(resources.getDimension(R.dimen.share_ad_icon_margin_top)) + resources.getDimension(R.dimen.share_ad_title_height)
                    + resources.getDimension(R.dimen.share_ad_title_margin_top) + resources.getDimension(R.dimen.share_ad_subtitle_height)
                    + resources.getDimension(R.dimen.share_ad_subtitle_margin_top) + resources.getDimension(R.dimen.share_ad_cation_height)
                    + resources.getDimension(R.dimen.share_ad_cation_margin_top))));
        }

        HSAlertDialog build = HSAlertDialog.build(activity, R.style.DialogSlideUpFromBottom);
        build.setView(view);
        final AlertDialog dialog = build.create();
        Window window = dialog.getWindow();
        Drawable background = dialog.getWindow().getDecorView().getBackground();
        background.setAlpha(0);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = Gravity.BOTTOM;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(attributes);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView imageView = (ImageView) view.findViewById(R.id.share_image);
        imageView.setImageURI(previewUri);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.share_list);
        ShareAdapter shareAdapter = new ShareAdapter(activity, dialog, shareIntent, resolveInfoList, itemWidth);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(shareAdapter);
        recyclerView.addItemDecoration(new ShareItemDecoration(resources.getDimensionPixelSize(R.dimen.share_item_column_space)));

        final View shareAdView = View.inflate(activity, R.layout.ad_share, null);
        View adActionView = shareAdView.findViewById(R.id.ad_call_to_action);
        adActionView.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(resources.getColor(R.color.ad_share_action_button_bg), resources.getDimension(R.dimen.corner_radius)));
        shareAdView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final NativeAdView nativeAdView = new NativeAdView(activity, shareAdView, adLoadingView);
        nativeAdView.setOnAdClickedListener(new NativeAdView.OnAdClickedListener() {
            @Override
            public void onAdClicked(NativeAdView adView) {
                dismissDialog(activity, dialog);
            }
        });
        nativeAdView.configParams(new NativeAdParams(adPlaceName, resources.getDisplayMetrics().widthPixels, 1.9f));

        shareAdContainer.addView(nativeAdView);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                nativeAdView.release();
            }
        });
        dialog.show();
        return dialog;
    }


    private static List<ResolveInfo> getFilteredShareList(List<ResolveInfo> resolveInfoList) {
        List<ResolveInfo> filteredResolveInfoList = new ArrayList();
        //优先级排前的应用包名列表
        String[] priorityFirstList = new String[]{"com.whatsapp", "com.snapchat.android", "com.facebook.katana", "com.instagram.android", "com.facebook.orca", "com.twitter.android", "com.android.mms", "com.tumblr", "com.pinterest", "com.google.android.gm"};
        PriorityResolveInfo[] priorityResolveInfoList = new PriorityResolveInfo[priorityFirstList.length];

        if (!resolveInfoList.isEmpty()) {
            for (ResolveInfo resolveInfo : resolveInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (resolveInfo.activityInfo == null)
                    continue;

                boolean isPriority = false;
                //找出优先级排前的应用，并将其位置记录
                int index = 0;
                for (int i = 0; i < priorityResolveInfoList.length; i++) {
                    if (packageName.contains(priorityFirstList[i])) {
                        isPriority = true;
                        index = i;
                        break;
                    }

                }

                if (isPriority) {
                    if (priorityResolveInfoList[index] == null) {
                        priorityResolveInfoList[index] = new PriorityResolveInfo();
                    }
                    priorityResolveInfoList[index].addResolveInfo(resolveInfo);
                    continue;
                }

                filteredResolveInfoList.add(resolveInfo);
            }

        }

        for (int i = priorityResolveInfoList.length - 1; i >= 0; i--) {
            if (priorityResolveInfoList[i] != null) {
                filteredResolveInfoList.addAll(0, priorityResolveInfoList[i].getResolveInfoList());
            }
        }
        return filteredResolveInfoList;
    }

    private static class PriorityResolveInfo {
        List<ResolveInfo> resolveInfoList;

        public List<ResolveInfo> getResolveInfoList() {
            return resolveInfoList;
        }

        public void addResolveInfo(ResolveInfo resolveInfo) {
            if (resolveInfoList == null) {
                resolveInfoList = new ArrayList<>();
            }
            resolveInfoList.add(resolveInfo);
        }
    }

    private static class ShareAdapter extends RecyclerView.Adapter<ShareViewHolder> {
        Context context;
        AlertDialog dialog;
        Intent shareIntent;
        List<ResolveInfo> resolveInfoList;
        int itemWidth;

        public ShareAdapter(Context context, AlertDialog dialog, Intent shareIntent, List<ResolveInfo> resolveInfoList, int width) {
            this.context = context;
            this.dialog = dialog;
            this.shareIntent = shareIntent;
            this.resolveInfoList = resolveInfoList;
            this.itemWidth = width;
        }

        @Override
        public ShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_app_item, parent, false);
            view.setLayoutParams(new RecyclerView.LayoutParams(itemWidth, (int) (itemWidth * 1.3f)));
            return new ShareViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ShareViewHolder holder, int position) {
            final ResolveInfo resolveInfo = resolveInfoList.get(position);
            PackageManager packageManager = HSApplication.getContext().getPackageManager();
            holder.shareAppIcon.setImageDrawable(resolveInfo.loadIcon(packageManager));
            holder.shareAppLabel.setText(resolveInfo.loadLabel(packageManager));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityInfo activity = resolveInfo.activityInfo;
                    ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                            activity.name);
                    shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    shareIntent.setComponent(name);
                    HSApplication.getContext().startActivity(shareIntent);

                    dismissDialog(context, dialog);

                    HSAnalytics.logEvent("share_app_clicked", "appName", resolveInfo.activityInfo.applicationInfo.loadLabel(HSApplication.getContext().getPackageManager()).toString());
                }
            });
        }

        @Override
        public int getItemCount() {
            return resolveInfoList != null ? resolveInfoList.size() : 0;
        }
    }

    private static class ShareItemDecoration extends RecyclerView.ItemDecoration {
        private int columnSpace;

        public ShareItemDecoration(int columnSpace) {
            this.columnSpace = columnSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
            int leftPadding = columnSpace / 2;
            int rightPadding = columnSpace / 2;

            if (position == 0) {
                leftPadding = 0;
            }
            outRect.set(leftPadding, 0, rightPadding, 0);
        }
    }


    private static class ShareViewHolder extends RecyclerView.ViewHolder {
        private ImageView shareAppIcon;
        private TextView shareAppLabel;

        public ShareViewHolder(View itemView) {
            super(itemView);
            shareAppIcon = (ImageView) itemView.findViewById(R.id.share_app_icon);
            shareAppLabel = (TextView) itemView.findViewById(R.id.share_app_label);
        }
    }

    public static void dismissDialog(Context context, Dialog dialog) {
        if (dialog != null && dialog.isShowing() && (context instanceof Activity ? (!((Activity) context).isFinishing()) : true)) {
            dialog.dismiss();
        }
    }
}
