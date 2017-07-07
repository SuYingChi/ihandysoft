package com.ihs.feature.junkclean.list;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.ihs.feature.common.FormatSizeBuilder;
import com.ihs.feature.junkclean.data.JunkManager;
import com.ihs.keyboardutils.R;

public class PowerfulCleanItem extends TopBannerBaseItem {

    public PowerfulCleanItem(Context context) {
        super(context);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.clean_powerful_clean);
    }

    @Override
    public CharSequence getContent() {
        Context context = getContext();
        FormatSizeBuilder formatSizeBuilder = new FormatSizeBuilder(JunkManager.getInstance().getHiddenSystemJunkSize());
        String appSizeText = formatSizeBuilder.sizeUnit;
        String contentText = context.getString(R.string.clean_powerful_clean_description, appSizeText, "90%");
        SpannableString contentSpannableString = new SpannableString(contentText);
        contentSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)), 0, appSizeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int start = contentText.indexOf(appSizeText);
        if (start != -1) {
            contentSpannableString.setSpan(new StyleSpan(Typeface.BOLD), start, start + appSizeText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return contentSpannableString;
    }

    @Override
    public int getIconDrawableId() {
        return R.drawable.clean_power_clean_icon_svg;
    }

}
