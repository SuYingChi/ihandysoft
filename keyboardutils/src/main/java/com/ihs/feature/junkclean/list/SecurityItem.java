package com.ihs.feature.junkclean.list;

import android.content.Context;

import com.honeycomb.launcher.R;

public class SecurityItem extends TopBannerBaseItem {

    public SecurityItem(Context context) {
        super(context);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.promotion_security_title);
    }

    @Override
    public CharSequence getContent() {
        return getContext().getString(R.string.promotion_security_banner_description);
    }

    @Override
    public int getIconDrawableId() {
        return R.drawable.clean_security_promotion_icon_svg;
    }

}
