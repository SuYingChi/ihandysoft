package com.ihs.feature.junkclean.list;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.LauncherPackageManager;
import com.ihs.feature.junkclean.model.ApkJunkWrapper;
import com.ihs.feature.junkclean.model.AppJunkWrapper;
import com.ihs.feature.junkclean.model.JunkWrapper;
import com.ihs.feature.junkclean.model.SystemJunkWrapper;
import com.ihs.feature.ui.ThreeStatesCheckBox;
import com.ihs.keyboardutils.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.viewholders.FlexibleViewHolder;

public class JunkDetailItem extends AbstractFlexibleItem<JunkDetailItem.JunkDetailViewHolder>
    implements ISectionable<JunkDetailItem.JunkDetailViewHolder, IHeader> {

    private JunkSubCategoryItem mParentCategory;
    private IHeader mHeader;

    private JunkWrapper mJunkWrapper;
    private OnItemCheckedListener mOnItemCheckedListener;

    public interface OnItemCheckedListener {
        boolean onItemChecked();
    }

    class JunkDetailViewHolder extends FlexibleViewHolder {
        AppCompatImageView junkIconIv;
        TextView junkSubDescTv;
        TextView junkDescTv;
        TextView junkSizeTv;
        ThreeStatesCheckBox checkBox;

        JunkDetailViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            junkSubDescTv = (TextView) view.findViewById(R.id.junk_sub_desc_view);
            junkDescTv = (TextView) view.findViewById(R.id.junk_desc_view);
            junkIconIv = (AppCompatImageView) view.findViewById(R.id.junk_icon);
            junkSizeTv = (TextView) view.findViewById(R.id.junk_size);
            checkBox = (ThreeStatesCheckBox) view.findViewById(R.id.junk_check);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.2f);
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
        }
    }

    

    public JunkDetailItem(JunkWrapper mJunkWrapper) {
        this.mJunkWrapper = mJunkWrapper;
    }

    public void setOnItemCheckBoxTouchedListener(OnItemCheckedListener mOnItemCheckedListener) {
        this.mOnItemCheckedListener = mOnItemCheckedListener;
    }

    public JunkWrapper getJunkWrapper() {
        return mJunkWrapper;
    }

    public boolean isChecked() {
        return mJunkWrapper.isMarked();
    }

    public void setChecked(boolean checked) {
        mJunkWrapper.setMarked(checked);

        if (mParentCategory != null) {
            mParentCategory.notifyCheckedChange();
        }
    }

    public long getSize() {
        return mJunkWrapper.getSize();
    }

    public JunkSubCategoryItem getParentCategory() {
        return mParentCategory;
    }

    public void setParentCategory(JunkSubCategoryItem junkSubCategoryItem) {
        mParentCategory = junkSubCategoryItem;
    }

    @Override
    public IHeader getHeader() {
        return mHeader;
    }

    @Override
    public void setHeader(IHeader header) {
        this.mHeader = header;
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public int hashCode() {
        return mJunkWrapper.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.clean_item_junk_detail;
    }

    @Override
    public JunkDetailViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new JunkDetailViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, final JunkDetailViewHolder holder, int position, List payloads) {
        holder.junkSubDescTv.setVisibility(View.GONE);

        holder.checkBox.setCheckedState(mJunkWrapper.isMarked() ? ThreeStatesCheckBox.ALL_CHECKED : ThreeStatesCheckBox.ALL_UNCHECKED);
        holder.checkBox.setTag(this);
        holder.checkBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mOnItemCheckedListener != null && !mJunkWrapper.isMarked()) {
                        return mOnItemCheckedListener.onItemChecked();
                    }
                }
                return false;
            }
        });
        holder.checkBox.setOnCheckBoxClickListener(new ThreeStatesCheckBox.OnCheckBoxClickListener() {
            @Override
            public void onClick(ThreeStatesCheckBox checkBox, @ThreeStatesCheckBox.CheckedState int checkState) {
                JunkDetailItem junkDetailItem = (JunkDetailItem) checkBox.getTag();
                junkDetailItem.setChecked(checkState == ThreeStatesCheckBox.ALL_CHECKED);
                adapter.notifyDataSetChanged();
            }
        });

        FormatSizeBuilder junkSizeBuilder = new FormatSizeBuilder(getSize());
        Context context = holder.junkSizeTv.getContext();
        holder.junkSizeTv.setText(String.valueOf(junkSizeBuilder.size + junkSizeBuilder.unit));
        if (holder.checkBox.getVisibility() != View.VISIBLE) {
            holder.junkSizeTv.setTextColor(ContextCompat.getColor(context, R.color.black_secondary));
        } else if (holder.checkBox.getCheckedState() == ThreeStatesCheckBox.ALL_UNCHECKED) {
            holder.junkSizeTv.setTextColor(ContextCompat.getColor(context, R.color.clean_text_unselected));
        } else {
            holder.junkSizeTv.setTextColor(ContextCompat.getColor(context, R.color.black_secondary));
        }

        holder.junkDescTv.setText(mJunkWrapper.getDescription());
        holder.junkIconIv.setImageResource(R.drawable.clean_file_junk_svg);
        holder.junkSubDescTv.setVisibility(View.GONE);

        Drawable drawable;
        switch (mJunkWrapper.getCategory()) {
            case SystemJunkWrapper.SYSTEM_JUNK:
                drawable = LauncherPackageManager.getInstance().getApplicationIcon(mJunkWrapper.getPackageName());

                if (drawable != null) {
                    holder.junkIconIv.setImageDrawable(drawable);
                }
                break;
            case ApkJunkWrapper.APK_JUNK:
                ApkJunkWrapper apkJunkWrapper = (ApkJunkWrapper) mJunkWrapper;
                holder.junkSubDescTv.setVisibility(View.VISIBLE);
                String descText = apkJunkWrapper.isInstall() ? HSApplication.getContext().getString(R.string.installed) : HSApplication.getContext().getString(R.string.uninstalled);
                holder.junkSubDescTv.setText(descText);
                ImageLoader.getInstance().displayImage(((ApkJunkWrapper) mJunkWrapper).getApkIconPath(), holder.junkIconIv);
                break;
            case AppJunkWrapper.APP_JUNK:
                AppJunkWrapper appJunkWrapper = (AppJunkWrapper) mJunkWrapper;
                if (appJunkWrapper.isInstall()) {
                    holder.junkDescTv.setText(appJunkWrapper.getPathType());
                }
                break;
        }
    }

}
