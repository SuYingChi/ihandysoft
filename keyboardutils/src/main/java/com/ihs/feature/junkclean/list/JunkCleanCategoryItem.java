package com.ihs.feature.junkclean.list;

import android.animation.Animator;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.junkclean.model.JunkWrapper;
import com.honeycomb.launcher.util.CommonUtils;
import com.honeycomb.launcher.util.FormatSizeBuilder;
import com.honeycomb.launcher.util.LauncherPackageManager;
import com.ihs.app.framework.HSApplication;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

public class JunkCleanCategoryItem extends AbstractFlexibleItem<JunkCleanCategoryItem.JunkCleanCategoryItemViewHolder> {

    public static final String SYSTEM_JUNK = "SYSTEM_JUNK";
    public static final String UNINSTALL_APP_JUNK = "UNINSTALL_APP_JUNK";
    public static final String APK_JUNK = "APK_JUNK";
    public static final String INSTALL_APP_JUNK = "INSTALL_APP_JUNK";
    public static final String PATH_RULE_JUNK = "PATH_RULE_JUNK";
    public static final String MEMORY_JUNK = "MEMORY_JUNK";

    private JunkWrapper mJunkWrapper;
    private String mCategory;
    private long mJunkSize;

    class JunkCleanCategoryItemViewHolder extends FlexibleViewHolder {
        AppCompatImageView junkIconIv;
        TextView junkDescTv;
        TextView mJunkSizeTv;

        JunkCleanCategoryItemViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            junkIconIv = (AppCompatImageView) view.findViewById(R.id.junk_icon);
            junkDescTv = (TextView) view.findViewById(R.id.junk_desc_view);
            mJunkSizeTv = (TextView) view.findViewById(R.id.junk_size);
        }

        @Override
        public float getActivationElevation() {
            return CommonUtils.pxFromDp(4);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.2f);
        }
    }

    public JunkCleanCategoryItem(String mCategory) {
        this.mCategory = mCategory;
    }

    public void setJunkWrapper(JunkWrapper mJunkWrapper) {
        this.mJunkWrapper = mJunkWrapper;
        mJunkSize = mJunkWrapper.getSize();
    }

    public long getJunkSize() {
        return mJunkSize;
    }

    public void setJunkSize(long size) {
        mJunkSize = size;
    }

    public String getCategory() {
        return mCategory;
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public int hashCode() {
        return mJunkWrapper == null ? mCategory.hashCode() : mJunkWrapper.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.clean_item_layout;
    }


    @Override
    public JunkCleanCategoryItemViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new JunkCleanCategoryItemViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, JunkCleanCategoryItemViewHolder holder, int position, List payloads) {
        FormatSizeBuilder mJunkSizeBuilder = new FormatSizeBuilder(mJunkSize);
        holder.mJunkSizeTv.setText(mJunkSizeBuilder.sizeUnit);
        holder.junkIconIv.setImageResource(R.drawable.clean_fold_junk_svg);

        Drawable drawable;
        switch (mCategory) {
            case SYSTEM_JUNK:
                holder.junkDescTv.setText(mJunkWrapper.getDescription());

                drawable = LauncherPackageManager.getInstance().getApplicationIcon(mJunkWrapper.getPackageName());

                if (drawable != null) {
                    holder.junkIconIv.setImageDrawable(drawable);
                }

                break;
            case APK_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_obsolete_apk));
                break;
            case UNINSTALL_APP_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_residual_files));
                break;
            case INSTALL_APP_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_app_junk));
                break;
            case PATH_RULE_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_ad_junk));
                break;
            case MEMORY_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_memory_cache));
                break;
        }
    }

}