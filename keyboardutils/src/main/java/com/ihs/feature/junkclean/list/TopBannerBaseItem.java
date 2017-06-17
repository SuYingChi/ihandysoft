package com.ihs.feature.junkclean.list;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.util.VectorCompat;
import com.honeycomb.launcher.util.ViewUtils;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

public abstract class TopBannerBaseItem extends AbstractFlexibleItem<TopBannerBaseItem.ViewHolder>{

    protected Context mContext;

    public abstract String getTitle();

    public abstract CharSequence getContent();

    public abstract int getIconDrawableId();

    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onClick();
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mOnClickListener = listener;
    }

    protected Context getContext() {
        return mContext;
    }

    TopBannerBaseItem(Context context) {
        mContext = context;
    }

    class ViewHolder extends FlexibleViewHolder {
        View rootView;
        AppCompatImageView iconIv;
        TextView titleTv;
        TextView contentTv;

        ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            iconIv = ViewUtils.findViewById(view, R.id.icon_iv);
            rootView = ViewUtils.findViewById(view, R.id.root_view);
            titleTv = ViewUtils.findViewById(view, R.id.title_tv);
            contentTv = ViewUtils.findViewById(view, R.id.content_tv);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.2f);
        }
    }

    @Override
    public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
        holder.titleTv.setText(getTitle());
        holder.contentTv.setText(getContent());

        VectorCompat.setImageViewVectorResource(mContext, holder.iconIv, getIconDrawableId());

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnClickListener) {
                    mOnClickListener.onClick();
                }
            }
        });
    }

    @Override
    public int getLayoutRes() {
        return R.layout.clean_top_banner_item_layout;
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

}
