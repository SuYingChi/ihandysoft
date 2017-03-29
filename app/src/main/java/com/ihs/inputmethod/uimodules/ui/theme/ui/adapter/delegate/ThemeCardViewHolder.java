package com.ihs.inputmethod.uimodules.ui.theme.ui.adapter.delegate;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSDrawableUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2016/12/22.
 */

public final class ThemeCardViewHolder extends RecyclerView.ViewHolder {

	View themeCardView;
	ImageView themeRealImage;
	TextView themeName;
	ImageView themeDelete;
	ImageView moreMenuImage;
	ImageView themeNewImage;

	public ThemeCardViewHolder(View itemView) {
		super(itemView);
		themeCardView = itemView.findViewById(R.id.theme_card_view);
		themeRealImage = (ImageView) itemView.findViewById(R.id.theme_image_real_view);
		themeName = (TextView) itemView.findViewById(R.id.theme_name);
		themeDelete = (ImageView) itemView.findViewById(R.id.theme_delete_view);
		themeDelete.setBackgroundDrawable(HSDrawableUtils.getDimmedForegroundDrawable(BitmapFactory.decodeResource(HSApplication.getContext().getResources(), R.drawable.preview_keyboard_delete)));
		moreMenuImage = (ImageView) itemView.findViewById(R.id.more_menu_image);
		themeNewImage = (ImageView) itemView.findViewById(R.id.theme_new_view);
	}
}
