package com.ihs.keyboardutils.giftad;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.FontUtils;

public class RevealFlashButton extends AppCompatButton {

    private static final float REVEAL_START_SCALE_X = 0.1f;

    private final int mLightWidth;
    private final long mRevealDuration;
    private final long mFlashDuration;

    private int mTotalTranslation;
    private int mLightOffset;

    @Thunk Animator mFlashAnimation;
    @Thunk float mLightTranslateProgress = -1f;

    @Thunk Path mPath = new Path();
    @Thunk Paint mDrawPaint;
    @Thunk Paint mLightPaint;

    public RevealFlashButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources res = context.getResources();
        mLightWidth = res.getDimensionPixelSize(R.dimen.result_page_action_button_light_width);
        mRevealDuration = res.getInteger(R.integer.config_resultPageActionButtonRevealDuration);
        mFlashDuration = res.getInteger(R.integer.config_resultPageActionButtonFlashDuration);

        setVisibility(INVISIBLE);
        setTypeface(FontUtils.getTypeface(new FontUtils.Font(HSApplication.getContext().getString(R.string.proxima_nova_semibold))));

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mDrawPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mLightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLightPaint.setStyle(Paint.Style.FILL);
        mLightPaint.setColor(ContextCompat.getColor(getContext(), R.color.result_page_button_flash_light));
        mLightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
    }

    public void reveal() {
        setScaleX(REVEAL_START_SCALE_X);
        setVisibility(VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(REVEAL_START_SCALE_X, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setScaleX((float) animation.getAnimatedValue());
                invalidate();
            }
        });
        animator.setDuration(mRevealDuration);
        animator.setInterpolator(AnimationUtils.loadInterpolator(HSApplication.getContext(), R.anim.decelerate_quint));
        animator.start();
    }

    public void flash() {
        mLightOffset = (int) (getHeight() * Math.tan(Math.toRadians(30.0)));
        mTotalTranslation = getWidth() + mLightWidth + mLightOffset;

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLightTranslateProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLightTranslateProgress = -1f;
                mFlashAnimation = null;
                invalidate();
            }
        });
        animator.setDuration(mFlashDuration);
        animator.setInterpolator(PathInterpolatorCompat.create(.45f, .87f, .76f, .88f));
        animator.start();
        mFlashAnimation = animator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }

        if (mLightTranslateProgress >= 0f) {
            drawFlashLight(canvas);
        }
    }

    private void drawFlashLight(Canvas canvas) {
        float upperRightX = mLightTranslateProgress * mTotalTranslation;
        float height = getHeight();
        mPath.reset();
        mPath.moveTo(upperRightX, 0f); // Upper-right
        mPath.lineTo(upperRightX - mLightWidth, 0f); // Upper-left
        mPath.lineTo(upperRightX - mLightWidth - mLightOffset, height); // Bottom-left
        mPath.lineTo(upperRightX - mLightOffset, height); // Bottom-right
        mPath.close();
        try {
            canvas.drawPath(mPath, mLightPaint);
        } catch (Exception e) {
            if (mFlashAnimation != null) {
                mFlashAnimation.cancel();
            }
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }
}
