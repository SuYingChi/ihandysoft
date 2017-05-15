package com.ihs.keyboardutils.giftad;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ihs.keyboardutils.R;

public class AllAppsScaleLinearLayout extends LinearLayout {

    private float lengthWidthRatio;

    public AllAppsScaleLinearLayout(Context context) {
        this(context, null);
    }

    public AllAppsScaleLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsScaleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AllAppsScaleLinearLayout);
        lengthWidthRatio = typedArray.getFloat(R.styleable.AllAppsScaleLinearLayout_lengthWidthRatio, 1.0f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height;
        if (widthMode == MeasureSpec.EXACTLY) {
            height = (int) (lengthWidthRatio * width);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}