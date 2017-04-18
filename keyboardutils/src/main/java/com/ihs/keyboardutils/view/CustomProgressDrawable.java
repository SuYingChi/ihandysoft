package com.ihs.keyboardutils.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by dsapphire on 16/2/27.
 */
public final class CustomProgressDrawable extends Drawable {


    @Override
    public void draw(Canvas canvas) {
        if (getLevel() == 0) {
            return;
        }
        drawBar(canvas, getLevel(), mColor);
    }

    private void drawBar(Canvas canvas, int level, int color) {
        Rect bounds = getBounds();
        int length = (int) (bounds.width() * level * 0.01f);
        int xpos = bounds.left;
        int mBarHeight = bounds.height();
        int ypos = bounds.bottom - mBarHeight;
        int radius = mBarHeight / 2;

        mPaint.setColor(mBackgroundColor);
        RectF rectF = new RectF(xpos, ypos, xpos + bounds.width(), ypos + mBarHeight);
        canvas.drawRoundRect(rectF, radius, radius, mPaint);

        mPaint.setColor(color);
        rectF = new RectF(xpos, ypos, xpos + length, ypos + mBarHeight);
        canvas.drawRoundRect(rectF, radius, radius, mPaint);
    }


    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mBackgroundColor = 0x19000000;
    private int mColor = 0xff558cfc;
    private int mPadding = 10;
    private int mBarWidth = 20;
    private int mLevel = 0;
    private boolean mHideWhenZero = false;

    /**
     * Sets the progress bar color.
     */
    public void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            invalidateSelf();
        }
    }

    /**
     * Gets the progress bar color.
     */
    public int getColor() {
        return mColor;
    }

    /**
     * Sets the progress bar background color.
     */
    public void setBackgroundColor(int backgroundColor) {
        if (mBackgroundColor != backgroundColor) {
            mBackgroundColor = backgroundColor;
            invalidateSelf();
        }
    }

    /**
     * Sets the progress bar padding.
     */
    public void setPadding(int padding) {
        if (mPadding != padding) {
            mPadding = padding;
            invalidateSelf();
        }
    }

    /**
     * Gets the progress bar padding.
     */
    @Override
    public boolean getPadding(Rect padding) {
        padding.set(mPadding, mPadding, mPadding, mPadding);
        return mPadding != 0;
    }

    /**
     * Sets the progress bar width.
     */
    public void setBarWidth(int barWidth) {
        if (mBarWidth != barWidth) {
            mBarWidth = barWidth;
            invalidateSelf();
        }
    }


    @Override
    protected boolean onLevelChange(int level) {
        mLevel = level;
        invalidateSelf();
        return true;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return getOpacityFromColor(mPaint.getColor());
    }

    /**
     * Gets the opacity from a color. Inspired by Android ColorDrawable.
     *
     * @param color
     * @return opacity expressed by one of PixelFormat constants
     */
    public static int getOpacityFromColor(int color) {
        int colorAlpha = color >>> 24;
        if (colorAlpha == 255) {
            return PixelFormat.OPAQUE;
        } else if (colorAlpha == 0) {
            return PixelFormat.TRANSPARENT;
        } else {
            return PixelFormat.TRANSLUCENT;
        }

    }
}