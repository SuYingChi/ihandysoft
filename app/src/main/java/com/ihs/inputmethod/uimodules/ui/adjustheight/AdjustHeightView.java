package com.ihs.inputmethod.uimodules.ui.adjustheight;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ihs.app.framework.HSApplication;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by jixiang on 18/2/7.
 */

public class AdjustHeightView extends RelativeLayout {
    public AdjustHeightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View view = findViewById(R.id.bottom_container);
        view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources())));



    }
}
