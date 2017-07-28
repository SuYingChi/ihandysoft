package com.ihs.feature.battery;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScanIconAnimationView extends LinearLayout {

    public static final int ITEMS_PER_ROW = 6;
    public static final int MARGIN_IN = 66;
    public static final int MARGIN_OUT = 71;
    public static final int START_IN_OFFSET = 35;
    public static final int START_OUT_OFFSET = 35;
    public static final int INTERVAL_STOP_TIME = 200;
    public static final int DURATION_IN = 285;
    public static final int DURATION_OUT = 285;
    public static final int DURATION_LIGHT = 800;
    public static final int DURATION_ALPHA_ADD = 300;
    public static final int DURATION_ALPHA_NO_CHANGE = 200;
    public static final int DURATION_ALPHA_REDUCE = 300;
    public static final int ICON_LEFT_RIGHT_MARGIN = 20;
    public static final int LIGHT_RIGHT_MARGIN = 15;

    public static final float INTERPOLATOR_FACTOR = 3.0f;
    public static final float ALPHA_ICON_MIN = 0.0f;
    public static final float ALPHA_ICON_MAX = 0.8f;

    public static final int ICON_ZERO = 0;
    public static final int ICON_ONE = 1;
    public static final int ICON_TWO = 2;
    public static final int ICON_THREE = 3;
    public static final int ICON_FOUR = 4;
    public static final int ICON_FIVE = 5;

    public int marginIn = 164;
    public int marginOut = 178;
    
    private List<BatteryAppInfo> appInfoList;
    private LayoutInflater inflater;
    private int currentAnimationTimes;
    private boolean isEnd = false;
    private Handler handler = new Handler();
    private int leftRightMargin;
    private ImageView lightIv;
    private HashMap<String, List<BatteryAppInfo>> cacheData = new HashMap<>();

    public ScanIconAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScanIconAnimationView(Context context) {
        super(context);
        init(context);
    }

    private void init (Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        inflater = LayoutInflater.from(context);
        leftRightMargin = CommonUtils.pxFromDp(ICON_LEFT_RIGHT_MARGIN);
        marginIn = CommonUtils.pxFromDp(MARGIN_IN);
        marginOut = CommonUtils.pxFromDp(MARGIN_OUT);

        List<View> innerViews = new ArrayList<>();
        for (int i = 0; i < ITEMS_PER_ROW; i++) {
            View itemV = inflater.inflate(R.layout.battery_app_icon_animation_item, null);
            innerViews.add(itemV);
        }
        removeAllViewsInLayout();
        addInnerViews(innerViews);
    }

    public void setData (List<BatteryAppInfo> appInfoList) {
        if (null != appInfoList) {
            // add below code for ConcurrentModificationException
            BatteryAppInfo[] appInfoArray = new BatteryAppInfo[appInfoList.size()];
            appInfoArray = appInfoList.toArray(appInfoArray);
            this.appInfoList = Arrays.asList(appInfoArray);
        }
        currentAnimationTimes = 0;
        isEnd = false;
    }

    public void setEnd (boolean isEnd) {
        this.isEnd = isEnd;
        if (isEnd) {
            for (int i = 0; i < ITEMS_PER_ROW; i ++) {
                View childView = getChildAt(i);
                if (null == childView) {
                    continue;
                }

                ImageView childIv = (ImageView) childView.findViewById(R.id.app_icon_iv);
                if (null == childIv) {
                    continue;
                }
                childIv.setImageDrawable(null);
            }

            if (null != lightIv) {
                lightIv.clearAnimation();
                lightIv.setVisibility(View.GONE);
            }
            handler.removeCallbacksAndMessages(null);
        }
    }

    private List<BatteryAppInfo> getCurrentData (int currentAnimationTimes) {
        List<BatteryAppInfo> currentAppInfos = cacheData.get(String.valueOf(currentAnimationTimes));

        int count = appInfoList.size();
        int totalTimes;
        if (count % ITEMS_PER_ROW == 0) {
            totalTimes = count / ITEMS_PER_ROW;
        } else {
            totalTimes = (count -  count % ITEMS_PER_ROW) / ITEMS_PER_ROW;
        }

        if (currentAnimationTimes + 1 >= totalTimes) {
            this.currentAnimationTimes = -1;
        }

        if (null != currentAppInfos) {
            return currentAppInfos;
        }

        int start = currentAnimationTimes * ITEMS_PER_ROW;
        int end = currentAnimationTimes * ITEMS_PER_ROW + ITEMS_PER_ROW;
        try {
            currentAppInfos = appInfoList.subList(start, end);
            cacheData.put(String.valueOf(currentAnimationTimes), currentAppInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentAppInfos;
    }

    private void addInnerViews(List<View> innerViews) {
        if (null != innerViews) {
            int childrenCount = innerViews.size();
            for (int index = 0; index < childrenCount; ++index) {
                View child = innerViews.get(index);
                if (null != child.getParent()) {
                    ((ViewGroup) child.getParent()).removeView(child);
                }
                addInnerView(child, index);
            }
        }
    }

    private void addInnerView(View child, int position) {
        int width = 0;
        int height = LayoutParams.WRAP_CONTENT;
        LayoutParams innerParams = new LayoutParams(width, height);
        innerParams.weight = 1;
        innerParams.gravity = Gravity.CENTER_VERTICAL;
        if (position == 0) {
            innerParams.setMargins(leftRightMargin, 0 , 0, 0);
        } else if (position == ITEMS_PER_ROW - 1) {
            innerParams.setMargins(0, 0 , leftRightMargin, 0);
        }
        addView(child, innerParams);
    }

    public void startAnimation (ImageView lightIv) {
        this.lightIv = lightIv;
        lightIv.setVisibility(View.VISIBLE);
        isEnd = false;
        setVisibility(View.VISIBLE);
        List<BatteryAppInfo> currentAppInfoList = getCurrentData(currentAnimationTimes);
        if (null == currentAppInfoList) {
            return;
        }
        currentAnimationTimes++;

        startLightAnimation();

        for (int i = 0; i < ITEMS_PER_ROW; i ++) {
            View childView = getChildAt(i);
            ImageView childIv = (ImageView) childView.findViewById(R.id.app_icon_iv);
            childIv.setImageDrawable(currentAppInfoList.get(i).getAppDrawable());

            final AnimationSet animationSet = new AnimationSet(false);
            TranslateAnimation translateAnimation = null;
            AlphaAnimation alphaAnimation = null;
            switch (i) {
                case ICON_ZERO:
                    translateAnimation = new TranslateAnimation(marginIn, 0, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MIN, ALPHA_ICON_MAX);
                    break;
                case ICON_ONE:
                    translateAnimation = new TranslateAnimation(marginIn, 0, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MIN, ALPHA_ICON_MAX);
                    animationSet.setStartOffset(START_IN_OFFSET);
                    break;
                case ICON_TWO:
                    translateAnimation = new TranslateAnimation(marginIn, 0, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MIN, ALPHA_ICON_MAX);
                    animationSet.setStartOffset(START_IN_OFFSET * 2);
                    break;
                case ICON_THREE:
                    translateAnimation = new TranslateAnimation(marginIn, 0, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MIN, ALPHA_ICON_MAX);
                    animationSet.setStartOffset(START_IN_OFFSET * 3);
                    break;
                case ICON_FOUR:
                    translateAnimation = new TranslateAnimation(marginIn, 0, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MIN, ALPHA_ICON_MAX);
                    animationSet.setStartOffset(START_IN_OFFSET * 4);
                    break;
                case ICON_FIVE:
                    translateAnimation = new TranslateAnimation(marginIn, 0, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MIN, ALPHA_ICON_MAX);
                    animationSet.setStartOffset(START_IN_OFFSET * 5);
                    break;
                default:
                    break;
            }

            final int currentPosition = i;
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setInterpolator(new DecelerateInterpolator(INTERPOLATOR_FACTOR));
            animationSet.setDuration(DURATION_IN);
            childView.startAnimation(animationSet);
            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (currentPosition == ITEMS_PER_ROW - 1) {
                        if (!isEnd) {
                            //startAnimation();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startAnimationOut();
                                }
                            }, INTERVAL_STOP_TIME);

                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    private void startLightAnimation () {
        float toX = CommonUtils.getPhoneWidth(getContext()) + CommonUtils.pxFromDp(LIGHT_RIGHT_MARGIN);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, toX, 0, 0);
        translateAnimation.setDuration(DURATION_LIGHT);

        AlphaAnimation alphaAddAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAddAnimation.setDuration(DURATION_ALPHA_ADD);

        AlphaAnimation alphaNoChangeAnimation = new AlphaAnimation(1.0f, 1.0f);
        alphaNoChangeAnimation.setDuration(DURATION_ALPHA_NO_CHANGE);
        alphaNoChangeAnimation.setStartOffset(DURATION_ALPHA_ADD);

        AlphaAnimation alphaReduceAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaReduceAnimation.setDuration(DURATION_ALPHA_REDUCE);
        alphaReduceAnimation.setStartOffset(DURATION_ALPHA_ADD + DURATION_ALPHA_NO_CHANGE);

        final AnimationSet animationSet = new AnimationSet(false);

        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAddAnimation);
        animationSet.addAnimation(alphaNoChangeAnimation);
        animationSet.addAnimation(alphaReduceAnimation);

        lightIv.setVisibility(View.VISIBLE);
        lightIv.setImageResource(R.drawable.battery_scan_icon_light);
        lightIv.startAnimation(animationSet);
    }

    private void startAnimationOut () {
        for (int i = 0; i < ITEMS_PER_ROW; i ++) {
            View childView = getChildAt(i);
            final AnimationSet animationSet = new AnimationSet(false);
            TranslateAnimation translateAnimation = null;
            AlphaAnimation alphaAnimation = null;
            switch (i) {
                case ICON_ZERO:
                    translateAnimation = new TranslateAnimation(0, -marginOut, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MAX, ALPHA_ICON_MIN);
                    break;
                case ICON_ONE:
                    translateAnimation = new TranslateAnimation(0, -marginOut, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MAX, ALPHA_ICON_MIN);
                    animationSet.setStartOffset(START_OUT_OFFSET);
                    break;
                case ICON_TWO:
                    translateAnimation = new TranslateAnimation(0, -marginOut, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MAX, ALPHA_ICON_MIN);
                    animationSet.setStartOffset(START_OUT_OFFSET * 2);
                    break;
                case ICON_THREE:
                    translateAnimation = new TranslateAnimation(0, -marginOut, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MAX, ALPHA_ICON_MIN);
                    animationSet.setStartOffset(START_OUT_OFFSET * 3);
                    break;
                case ICON_FOUR:
                    translateAnimation = new TranslateAnimation(0, -marginOut, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MAX, ALPHA_ICON_MIN);
                    animationSet.setStartOffset(START_OUT_OFFSET * 4);
                    break;
                case ICON_FIVE:
                    translateAnimation = new TranslateAnimation(0, -marginOut, 0, 0);
                    alphaAnimation = new AlphaAnimation(ALPHA_ICON_MAX, ALPHA_ICON_MIN);
                    animationSet.setStartOffset(START_OUT_OFFSET * 5);
                    break;
                default:
                    break;
            }

            final int currentPosition = i;
            animationSet.setFillAfter(true);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(alphaAnimation);
            animationSet.setInterpolator(new AccelerateInterpolator());
            animationSet.setDuration(DURATION_OUT);
            childView.startAnimation(animationSet);
            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (currentPosition == ITEMS_PER_ROW - 1) {
                        if (!isEnd) {
                            startAnimation(lightIv);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }
}
