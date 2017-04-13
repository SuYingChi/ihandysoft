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

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.ihs.keyboardutils.nativeads.NativeAdParams;
import com.ihs.keyboardutils.nativeads.NativeAdView;

import java.util.List;

/**
 * Created by jixiang on 17/4/12.
 */

public class CustomShareUtils {


    public static void shareImage(Activity activity, Uri uri) {
        final Context context = activity != null ? activity : HSApplication.getContext();

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> launchables = pm.queryIntentActivities(shareIntent, 0);


        View view = View.inflate(context, R.layout.share_layout, null);

        HSAlertDialog build = HSAlertDialog.build(R.style.DialogSlideUpFromBottom);
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
        imageView.setImageURI(uri);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.share_list);
        ShareAdapter shareAdapter = new ShareAdapter(context, dialog, uri, launchables);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(shareAdapter);
        recyclerView.addItemDecoration(new ShareItemDecoration(context.getResources().getDimensionPixelSize(R.dimen.share_item_column_space)));


        View adLoadingView = View.inflate(context, R.layout.ad_default_loading, null);
        adLoadingView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (context.getResources().getDisplayMetrics().widthPixels * 1.0f / 1.9f + context.getResources().getDisplayMetrics().density * 100)));

        final View shareAdView = View.inflate(context, R.layout.ad_share, null);
        View adActionView = shareAdView.findViewById(R.id.ad_call_to_action);
        adActionView.setBackgroundDrawable(RippleDrawableUtils.getCompatRippleDrawable(context.getResources().getColor(R.color.ad_share_action_button_bg),context.getResources().getDimension(R.dimen.corner_radius)));
        shareAdView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final NativeAdView nativeAdView = new NativeAdView(context, shareAdView, adLoadingView);
        nativeAdView.setOnAdClickedListener(new NativeAdView.OnAdClickedListener() {
            @Override
            public void onAdClicked(NativeAdView adView) {
                dismissDialog(context,dialog);
            }
        });
        nativeAdView.configParams(new NativeAdParams(context.getString(R.string.ad_native_placement_custom_share), context.getResources().getDisplayMetrics().widthPixels, 1.9f));

        FrameLayout shareAdContainer = (FrameLayout) view.findViewById(R.id.share_ad_container);
        shareAdContainer.addView(nativeAdView);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                nativeAdView.release();
            }
        });
        dialog.show();
    }

    private static class ShareAdapter extends RecyclerView.Adapter<ShareViewHolder> {
        Context context;
        AlertDialog dialog;
        Uri uri;
        List<ResolveInfo> resolveInfoList;

        public ShareAdapter(Context context, AlertDialog dialog, Uri uri, List<ResolveInfo> resolveInfoList) {
            this.context = context;
            this.dialog = dialog;
            this.uri = uri;
            this.resolveInfoList = resolveInfoList;
        }

        @Override
        public ShareViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ShareViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.share_app_item, parent, false));
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
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("image/*");

                    ActivityInfo activity = resolveInfo.activityInfo;
                    ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                            activity.name);
                    shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    shareIntent.setComponent(name);
                    HSApplication.getContext().startActivity(shareIntent);

                    dismissDialog(context, dialog);
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
            } else if (position == parent.getChildCount() - 1) {
                rightPadding = 0;
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
