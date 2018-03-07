package com.ihs.inputmethod.uimodules.settings;

import android.content.Context;
import android.content.res.Resources;
import android.support.percent.PercentFrameLayout;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 18/3/7.
 */

public class SettingItemView extends PercentFrameLayout {
    private ImageView imageView;
    private TextView textView;

    public SettingItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        imageView = findViewById(R.id.iv_settings_item);
        textView = findViewById(R.id.tv_settings_item);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Resources resources = HSApplication.getContext().getResources();
        // 高度 = (键盘高 * 0.95 （因为topMargin是0.05）- 底部导航栏10dp ) / 2 - 图片上边距 - 文字上边距
        int height = (int) ((HSResourceUtils.getDefaultKeyboardHeight(resources) * 0.95 - resources.getDimensionPixelSize(R.dimen.setting_panel_indicator_view_height)) / 2)
                - resources.getDimensionPixelSize(R.dimen.setting_panel_item_margin_top) - resources.getDimensionPixelSize(R.dimen.setting_panel_item_text_margin_top);
        // 图片大小 = 高度*0.7 和 宽*0.95 直接的最小值
        int imageSize = (int) Math.min(height * 0.7, resources.getDisplayMetrics().widthPixels * 0.95 / SettingsViewPager.colCount);
        // 文字高度 = 高度*0.3
        int textHeight = (int) (height * 0.3);

        imageView.getLayoutParams().width = imageView.getLayoutParams().height = imageSize;
        textView.getLayoutParams().height = textHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
