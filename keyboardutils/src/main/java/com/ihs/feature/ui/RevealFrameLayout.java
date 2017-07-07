package com.ihs.feature.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;


public class RevealFrameLayout extends FrameLayout implements RevealViewGroup {
    private PaintFlagsDrawFilter paintFlagsDrawFilter;
    private ViewRevealManager manager;
    private PointF centerPoint;
    private float radius;
    private Paint paint;
    private Path path;

    private Float cx, cy;

    public RevealFrameLayout(Context context) {
        this(context, null);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        manager = new ViewRevealManager();
        centerPoint = new PointF();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();

        paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        setBackgroundColor(Color.TRANSPARENT);
    }

    public void setCircleCenter(float cx, float cy) {
        this.cx = cx;
        this.cy = cy;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        try {
            canvas.save();

            if (cx == null || cy == null) {
                manager.transform(canvas, child);
            } else {
                manager.transform(canvas, child, cx, cy);
            }
            return super.drawChild(canvas, child, drawingTime);
        } finally {
            canvas.restore();
        }
    }

    @Override
    public ViewRevealManager getViewRevealManager() {
        return manager;
    }

    @Override
    public void setColor(int color) {
        paint.setColor(color);
    }

    @Override
    public void setCenterPoint(float x, float y) {
        centerPoint.set(x, y);
    }

    @Override
    public void updateRadius(float radius) {
        this.radius = radius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(paintFlagsDrawFilter);
        canvas.clipRect(0, 0, getWidth() + 1, getHeight() + 1);
        canvas.drawCircle(centerPoint.x, centerPoint.y, radius, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
