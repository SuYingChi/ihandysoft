package com.ihs.feature.junkclean.list;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.LauncherPackageManager;
import com.ihs.feature.junkclean.data.JunkManager;
import com.ihs.feature.junkclean.model.JunkWrapper;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.ui.ThreeStatesCheckBox;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.viewholders.ExpandableViewHolder;

public class JunkSubCategoryItem extends AbstractFlexibleItem<JunkSubCategoryItem.JunkSubCategoryViewHolder>
    implements IExpandable<JunkSubCategoryItem.JunkSubCategoryViewHolder, JunkDetailItem> {

    private List<JunkDetailItem> junkDetailItems = new ArrayList<>();
    private JunkHeadCategoryItem parentCategory;

    private JunkWrapper junkWrapper;

    private String category;
    private String appName = "";

    private boolean expanded = false;
    private boolean checkBoxVisible = true;

    private long extendRandomSingleSize;
    
    private int checkedState = ThreeStatesCheckBox.ALL_CHECKED;

    private OnItemCheckedListener listener;

    public interface OnItemCheckedListener {
        boolean onItemChecked();
    }

    class JunkSubCategoryViewHolder extends ExpandableViewHolder {
        AppCompatImageView junkIconIv;
        TextView junkSubDescTv;
        TextView junkDescTv;
        TextView junkSizeTv;
        View triangleExpandV;
        ThreeStatesCheckBox checkBox;

        JunkSubCategoryViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            junkSubDescTv = (TextView) view.findViewById(R.id.junk_sub_desc_view);
            junkIconIv = (AppCompatImageView) view.findViewById(R.id.junk_icon);
            junkDescTv = (TextView) view.findViewById(R.id.junk_desc_view);
            junkSizeTv = (TextView) view.findViewById(R.id.junk_size);
            triangleExpandV = view.findViewById(R.id.triangle_expand);
            checkBox = (ThreeStatesCheckBox) view.findViewById(R.id.junk_check);
        }

        @Override
        public float getActivationElevation() {
            return CommonUtils.pxFromDp(4);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.2f);
        }

        @Override
        protected void expandView(int position) {
            super.expandView(position);
            if (mAdapter.isExpanded(position)) {
                mAdapter.notifyItemChanged(position, true);
            }
        }

        @Override
        protected void collapseView(int position) {
            super.collapseView(position);
            if (!mAdapter.isExpanded(position)) {
                mAdapter.notifyItemChanged(position, true);
            }
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
        }
    }

    public JunkSubCategoryItem(String category) {
        this.category = category;
    }

    public void setCheckBoxVisible(boolean checkBoxVisible) {
        this.checkBoxVisible = checkBoxVisible;
    }

    public void setOnItemCheckBoxTouchedListener(OnItemCheckedListener listener) {
        this.listener = listener;
    }

    public void setJunkWrapper(JunkWrapper junkWrapper) {
        this.junkWrapper = junkWrapper;

        checkedState = junkWrapper.isMarked() ? ThreeStatesCheckBox.ALL_CHECKED : ThreeStatesCheckBox.ALL_UNCHECKED;
    }

    public JunkWrapper getJunkWrapper() {
        return junkWrapper;
    }

    public int getCheckedState() {
        return checkedState;
    }

    public void setChecked(boolean checked) {
        checkedState = checked ? ThreeStatesCheckBox.ALL_CHECKED : ThreeStatesCheckBox.ALL_UNCHECKED;

        switch (category) {
            case JunkCleanConstant.CATEGORY_PATH_RULE_JUNK:
            case JunkCleanConstant.CATEGORY_MEMORY_JUNK:
                junkWrapper.setMarked(checked);
                break;
        }

        for (JunkDetailItem item : getSubItems()) {
            item.setChecked(checked);
        }

        if (parentCategory != null) {
            parentCategory.notifyCheckedChange();
        }
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    void notifyCheckedChange() {
        checkedState = ThreeStatesCheckBox.ALL_CHECKED;
        boolean hasAtLeastOneSelected = false;
        for (JunkDetailItem item : junkDetailItems) {
            if (item.isChecked()) {
                hasAtLeastOneSelected = true;
            } else {
                checkedState = ThreeStatesCheckBox.PART_CHECKED;
            }
        }

        if (!hasAtLeastOneSelected) {
            checkedState = ThreeStatesCheckBox.ALL_UNCHECKED;
        }

        switch (category) {
            case JunkCleanConstant.CATEGORY_PATH_RULE_JUNK:
            case JunkCleanConstant.CATEGORY_MEMORY_JUNK:
                junkWrapper.setMarked(checkedState == ThreeStatesCheckBox.ALL_CHECKED);
                break;
        }

        if (parentCategory != null) {
            parentCategory.notifyCheckedChange();
        }
    }

    public String getCategory() {
        return category;
    }

    public long getSize() {
        long junkSize = 0;

        switch (category) {
            case JunkCleanConstant.CATEGORY_SYSTEM_JUNK:
            case JunkCleanConstant.CATEGORY_APK_JUNK:
            case JunkCleanConstant.CATEGORY_UNINSTALL_APP_JUNK:
            case JunkCleanConstant.CATEGORY_INSTALL_APP_JUNK:
                if (junkDetailItems == null) {
                    break;
                }
                for (JunkDetailItem categoryItem : junkDetailItems) {
                    junkSize += categoryItem.getSize();
                }
                break;
            case JunkCleanConstant.CATEGORY_PATH_RULE_JUNK:
            case JunkCleanConstant.CATEGORY_MEMORY_JUNK:
                if (junkWrapper == null) {
                    break;
                }
                junkSize = junkWrapper.getSize();
                break;
            case JunkCleanConstant.CATEGORY_UNINSTALL_AD_RANDOM_JUNK:
                junkSize = JunkManager.getInstance().getAlertAdCacheRandomSize();
                break;
            case JunkCleanConstant.CATEGORY_UNINSTALL_APP_RANDOM_JUNK:
                junkSize = JunkManager.getInstance().getAlertAppCacheRandomSize();
                break;
        }
        return junkSize;
    }

    public JunkHeadCategoryItem getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(JunkHeadCategoryItem junkHeadCategoryItem) {
        parentCategory = junkHeadCategoryItem;
    }

    public void addSubItem(JunkDetailItem detailItem) {
        junkDetailItems.add(detailItem);

        checkedState = ThreeStatesCheckBox.ALL_CHECKED;
        boolean hasAtLeastOneSelected = false;
        for (JunkDetailItem item : junkDetailItems) {
            if (item.isChecked()) {
                hasAtLeastOneSelected = true;
            } else {
                checkedState = ThreeStatesCheckBox.PART_CHECKED;
            }
        }

        if (!hasAtLeastOneSelected) {
            checkedState = ThreeStatesCheckBox.ALL_UNCHECKED;
        }
    }

    public void removeSubItem(JunkDetailItem detailItem) {
        junkDetailItems.remove(detailItem);
    }

    @Override
    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public int hashCode() {
        return junkWrapper == null ? category.hashCode() : junkWrapper.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.clean_item_junk_sub_category;
    }

    @Override
    public JunkSubCategoryViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new JunkSubCategoryViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, JunkSubCategoryViewHolder holder, int position, List payloads) {
        holder.triangleExpandV.setVisibility(isExpanded() ? View.VISIBLE : View.INVISIBLE);

        holder.checkBox.setVisibility(checkBoxVisible ? View.VISIBLE : View.INVISIBLE);
        holder.checkBox.setCheckedState(checkedState);
        holder.checkBox.setTag(this);
        holder.checkBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (listener != null && checkedState != ThreeStatesCheckBox.ALL_CHECKED) {
                        return listener.onItemChecked();
                    }
                }
                return false;
            }
        });
        holder.checkBox.setOnCheckBoxClickListener(new ThreeStatesCheckBox.OnCheckBoxClickListener() {
            @Override
            public void onClick(ThreeStatesCheckBox checkBox, @ThreeStatesCheckBox.CheckedState int checkState) {
                JunkSubCategoryItem junkSubCategoryItem = (JunkSubCategoryItem) checkBox.getTag();
                junkSubCategoryItem.setChecked(checkState == ThreeStatesCheckBox.ALL_CHECKED);

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
        holder.junkIconIv.setImageResource(R.drawable.clean_fold_junk_svg);

        Drawable drawable;
        switch (category) {
            case JunkCleanConstant.CATEGORY_UNINSTALL_AD_RANDOM_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_ad_cache));
                break;
            case JunkCleanConstant.CATEGORY_UNINSTALL_APP_RANDOM_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_app_cache, appName));
                break;
            case JunkCleanConstant.CATEGORY_SYSTEM_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_hidden_cache_junk));
                break;
            case JunkCleanConstant.CATEGORY_APK_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_obsolete_apk));
                holder.junkIconIv.setImageResource(R.drawable.clean_apk_junk_svg);
                break;
            case JunkCleanConstant.CATEGORY_UNINSTALL_APP_JUNK:
                holder.junkDescTv.setText(HSApplication.getContext().getString(R.string.clean_residual_files));
                holder.junkIconIv.setImageResource(R.drawable.ic_list_uninstall_app_junk);
                break;
            case JunkCleanConstant.CATEGORY_INSTALL_APP_JUNK:

                holder.junkDescTv.setText(getAppName(junkWrapper));

                drawable = LauncherPackageManager.getInstance().getApplicationIcon(junkWrapper.getPackageName());

                if (drawable != null) {
                    holder.junkIconIv.setImageDrawable(drawable);
                }
                break;
            case JunkCleanConstant.CATEGORY_PATH_RULE_JUNK:
                holder.junkDescTv.setText(getAppName(junkWrapper));
                break;
            case JunkCleanConstant.CATEGORY_MEMORY_JUNK:
                holder.junkDescTv.setText(getAppName(junkWrapper));

                drawable = LauncherPackageManager.getInstance().getApplicationIcon(junkWrapper.getPackageName());

                if (drawable != null) {
                    holder.junkIconIv.setImageDrawable(drawable);
                }
                break;
        }
    }

    private String getAppName(JunkWrapper wrapper) {
        String localName = LauncherPackageManager.getInstance().getApplicationLabel(junkWrapper.getPackageName());
        return TextUtils.isEmpty(localName) ? junkWrapper.getDescription() : localName;
    }

    @Override
    public int getExpansionLevel() {
        return 1;
    }

    @Override
    public List<JunkDetailItem> getSubItems() {
        return junkDetailItems;
    }

}