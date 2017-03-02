package com.ihs.chargingscreen.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BubbleView extends View {


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!hasMessages(0)) {
                sendEmptyMessageDelayed(0, 2000);
                addBubble();
            }
        }
    };

    private List<Bubble> bubbles = new ArrayList<Bubble>();
    private Random random = new Random();//生成随机数

    private Paint paint;
    private List<Bubble> bubbleList;


    public BubbleView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
        paint = new Paint();
        paint.setColor(0Xffffff);//灰白色
        paint.setAlpha(25);//设置不透明度：透明为0，完全不透明为255
    }

    public BubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        bubbleList = new ArrayList<Bubble>(bubbles);
        //依次绘制气泡
        for (Bubble bubble : bubbleList) {
            //碰到上边界从数组中移除
            if (bubble.getY() - bubble.getSpeedY() <= -bubble.getRadius() * 2) {
                bubbles.remove(bubble);
            } else {
                int i = bubbles.indexOf(bubble);
                if (bubble.getX() + bubble.getSpeedX() <= bubble
                        .getRadius()) {
                    bubble.setSpeedX(-bubble.getSpeedX());
                } else if (bubble.getX() + bubble.getSpeedX() >= getWidth() - bubble
                        .getRadius()) {
                    bubble.setSpeedX(-bubble.getSpeedX());
                } else {
                    bubble.setX(bubble.getX() + bubble.getSpeedX());
                }
                bubble.setY(bubble.getY() - bubble.getSpeedY());

                bubbles.set(i, bubble);
//                canvas.drawBitmap(bubble.getBitmap(), bubble.getX(),
//                        bubble.getY(), paint);
                canvas.drawCircle(bubble.getX(), bubble.getY(), bubble.getRadius(), paint);
            }
        }
        //刷新屏幕
        invalidate();
    }

    private float randomGenerator(float min, float max) {
        return min + (float) (Math.random() * ((max - min) + 1));
    }

    private void addBubble() {
        new Thread() {
            public void run() {
                Bubble bubble = new Bubble();
                float speedY = 0;
                while (speedY < 1) {
                    speedY = random.nextFloat() * 8;
                }
                bubble.setSpeedY(speedY);
                bubble.setRadius(randomGenerator(10, (float) (getWidth() * 0.3)));
                bubble.setX(randomGenerator(bubble.getRadius() + 1, getWidth() - bubble.getRadius() - 1));
                bubble.setY(getHeight() + bubble.getRadius());

                float speedX = 0;
                while (speedX == 0) {
                    speedX = random.nextFloat() - 0.5f;
                }
                bubble.setSpeedX(speedX * 8);
                bubbles.add(bubble);
            }
        }.start();
    }

    public void start() {
        handler.sendEmptyMessage(0);
    }

    public void stop() {
        handler.removeMessages(0);
        bubbles.clear();
    }


    private class Bubble {
        private float speedY;
        private float speedX;
        private float x;
        private float y;

        private float radius;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getSpeedY() {
            return speedY;
        }

        public void setSpeedY(float speedY) {
            this.speedY = speedY;
        }

        public float getSpeedX() {
            return speedX;
        }

        public void setSpeedX(float speedX) {
            this.speedX = speedX;
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

    }
}