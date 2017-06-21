package com.ihs.feature.junkclean.list;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.feature.common.AnimatorListenerAdapter;
import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.common.VectorCompat;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.ui.ProgressWheel;
import com.ihs.feature.ui.ThreeStatesCheckBox;
import com.ihs.keyboardutils.R;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.viewholders.ExpandableViewHolder;

public class JunkHeadCategoryItem
    extends AbstractFlexibleItem<JunkHeadCategoryItem.JunkHeadCategoryViewHolder>
    implements IExpandable<JunkHeadCategoryItem.JunkHeadCategoryViewHolder, JunkSubCategoryItem>,
    IHeader<JunkHeadCategoryItem.JunkHeadCategoryViewHolder> {

    private List<JunkSubCategoryItem> junkSubCategoryItems = new ArrayList<>();
    private String category;

    private int checkedState = ThreeStatesCheckBox.ALL_CHECKED;

    private boolean expanded = true;
    public static boolean isScanStatus;

    private OnItemCheckedListener listener;

    public interface OnItemCheckedListener {
        boolean onItemChecked();
    }

    class JunkHeadCategoryViewHolder extends ExpandableViewHolder {
        AppCompatImageView indicatorIv;
        AppCompatImageView categoryIconIv;
        AppCompatImageView categoryLoadTickIv;
        TextView junkSizeTv;
        TextView categoryTv;
        ThreeStatesCheckBox checkBox;
        ProgressWheel progressWheel;
        View divideV;

        public JunkHeadCategoryViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);

            categoryIconIv = (AppCompatImageView) view.findViewById(R.id.category_icon_view);
            categoryLoadTickIv = (AppCompatImageView) view.findViewById(R.id.category_load_tick_view);
            junkSizeTv = (TextView) view.findViewById(R.id.category_junk_size);
            indicatorIv = (AppCompatImageView) view.findViewById(R.id.indicator);
            checkBox = (ThreeStatesCheckBox) view.findViewById(R.id.category_junk_check);
            progressWheel = (ProgressWheel) view.findViewById(R.id.category_progress_wheel);
            categoryTv = (TextView) view.findViewById(R.id.category);
            divideV = view.findViewById(R.id.divide);
            if (isScanStatus) {
                progressWheel.setAlpha(0f);
                progressWheel.setVisibility(View.VISIBLE);
                progressWheel.animate().alpha(1f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        spinProgressWheel();
                    }
                }).start();
            }
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.2f);
        }

        @Override
        protected void expandView(int position) {
            super.expandView(position);
            if (isScanStatus) {
                return;
            }
            if (mAdapter.isExpanded(position)) {
                mAdapter.notifyItemChanged(position, true);
            }
        }

        @Override
        protected void collapseView(int position) {
            super.collapseView(position);
            if (isScanStatus) {
                return;
            }
            if (!mAdapter.isExpanded(position)) {
                mAdapter.notifyItemChanged(position, true);
            }
        }

        void spinProgressWheel() {
            if (null == progressWheel) {
                return;
            }
            progressWheel.setFinishSpeed(500f / 360f);
            progressWheel.setSpinSpeed(125f / 360f);
            progressWheel.setBarSpinCycleTime(1000);
            progressWheel.setVisibility(View.VISIBLE);
            progressWheel.spin();
        }

        private void stopProgressWheel() {
            if (null == progressWheel) {
                return;
            }
            progressWheel.stopSpinning();
            progressWheel.setVisibility(View.GONE);
        }
    }

    public JunkHeadCategoryItem(String category) {
        this.category = category;
        initExpandStatus(isScanStatus);
    }

    private void initExpandStatus(boolean isScanStatus) {
        if (isScanStatus) {
            setExpanded(false);
        } else {
            if (JunkCleanConstant.CATEGORY_AD_JUNK.equals(category) || JunkCleanConstant.CATEGORY_MEMORY_JUNK.equals(category)) {
                setExpanded(false);
            } else {
                setExpanded(true);
            }
        }
    }

    public void setOnItemCheckBoxTouchedListener(OnItemCheckedListener listener) {
        this.listener = listener;
    }

    public void setChecked(boolean checked) {
        checkedState = checked ? ThreeStatesCheckBox.ALL_CHECKED : ThreeStatesCheckBox.ALL_UNCHECKED;

        for (JunkSubCategoryItem item : junkSubCategoryItems) {
            item.setChecked(checked);
        }
    }

    void notifyCheckedChange() {
        checkedState = ThreeStatesCheckBox.ALL_CHECKED;
        boolean hasAtLeastOneSelected = false;
        for (JunkSubCategoryItem item : junkSubCategoryItems) {
            if (item.getCheckedState() == ThreeStatesCheckBox.ALL_CHECKED) {
                hasAtLeastOneSelected = true;
            } else {
                checkedState = ThreeStatesCheckBox.PART_CHECKED;
            }
        }

        if (!hasAtLeastOneSelected) {
            checkedState = ThreeStatesCheckBox.ALL_UNCHECKED;
        }
    }

    public long getSize() {
        long junkSize = 0;
        for (JunkSubCategoryItem categoryItem : junkSubCategoryItems) {
            junkSize += categoryItem.getSize();
        }

        return junkSize;
    }

    public void addSubItem(JunkSubCategoryItem subItem) {
        junkSubCategoryItems.add(subItem);

        checkedState = ThreeStatesCheckBox.ALL_CHECKED;
        boolean hasAtLeastOneSelected = false;
        for (JunkSubCategoryItem item : junkSubCategoryItems) {
            if (item.getCheckedState() == ThreeStatesCheckBox.ALL_CHECKED) {
                hasAtLeastOneSelected = true;
            } else {
                checkedState = ThreeStatesCheckBox.PART_CHECKED;
            }
        }

        if (!hasAtLeastOneSelected) {
            checkedState = ThreeStatesCheckBox.ALL_UNCHECKED;
        }
    }

    public void removeSubItem(JunkSubCategoryItem subItem) {
        junkSubCategoryItems.remove(subItem);
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
    public int getExpansionLevel() {
        return 0;
    }

    @Override
    public List<JunkSubCategoryItem> getSubItems() {
        return junkSubCategoryItems;
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, final JunkHeadCategoryViewHolder holder, int position, List payloads) {
        holder.indicatorIv.setImageResource(isExpanded() ? R.drawable.clean_expand_arrow_top_svg : R.drawable.clean_expand_arrow_bottom_svg);

        if (isScanStatus) {
            holder.progressWheel.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
            holder.indicatorIv.setVisibility(View.GONE);
        } else {
            holder.progressWheel.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.indicatorIv.setVisibility(View.VISIBLE);
        }
        holder.categoryLoadTickIv.setVisibility(View.GONE);

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
                JunkHeadCategoryItem junkHeadCategoryItem = (JunkHeadCategoryItem) checkBox.getTag();
                junkHeadCategoryItem.setChecked(checkState == ThreeStatesCheckBox.ALL_CHECKED);
                adapter.notifyDataSetChanged();
            }
        });

        int categoryItemSize = junkSubCategoryItems.size();
        String categoryItemSizeText = (categoryItemSize == 0) || isExpanded() ? "" : " (" + categoryItemSize + ")";

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
        holder.divideV.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        switch (category) {
            case JunkCleanConstant.CATEGORY_SYSTEM_JUNK:
                holder.categoryTv.setText(String.valueOf(HSApplication.getContext().getString(R.string.clean_system_junk) + categoryItemSizeText));
                VectorCompat.setImageViewVectorResource(HSApplication.getContext(), holder.categoryIconIv, R.drawable.clean_system_junk_icon_svg);
                break;
            case JunkCleanConstant.CATEGORY_APP_JUNK:
                holder.categoryTv.setText(String.valueOf(HSApplication.getContext().getString(R.string.clean_app_junk) + categoryItemSizeText));
                VectorCompat.setImageViewVectorResource(HSApplication.getContext(), holder.categoryIconIv, R.drawable.clean_app_junk_icon_svg);
                break;
            case JunkCleanConstant.CATEGORY_AD_JUNK:
                holder.categoryTv.setText(String.valueOf(HSApplication.getContext().getString(R.string.clean_ad_junk) + categoryItemSizeText));
                VectorCompat.setImageViewVectorResource(HSApplication.getContext(), holder.categoryIconIv, R.drawable.clean_ad_junk_icon_svg);
                break;
            case JunkCleanConstant.CATEGORY_MEMORY_JUNK:
                holder.categoryTv.setText(String.valueOf(HSApplication.getContext().getString(R.string.clean_memory_cache) + categoryItemSizeText));
                VectorCompat.setImageViewVectorResource(HSApplication.getContext(), holder.categoryIconIv, R.drawable.clean_memory_junk_icon_svg);
                break;
        }
    }

    @Override
    public int getLayoutRes() {
        return R.layout.clean_item_junk_head_category;
    }

    @Override
    public JunkHeadCategoryViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new JunkHeadCategoryViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public int hashCode() {
        return category.hashCode();
    }

}