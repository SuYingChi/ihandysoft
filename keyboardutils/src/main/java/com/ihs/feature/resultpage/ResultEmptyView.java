package com.ihs.feature.resultpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatImageView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artw.lockscreen.common.NavUtils;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.battery.BatteryUtils;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.common.DeviceManager;
import com.ihs.feature.common.DeviceUtils;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.VectorCompat;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.cpucooler.CpuCoolerScanActivity;
import com.ihs.feature.cpucooler.util.CpuPreferenceHelper;
import com.ihs.feature.junkclean.JunkCleanActivity;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultEmptyView extends RelativeLayout {

    public static final int TYPE_BOOST_PLUS = 0;
    public static final int TYPE_JUNK_CLEAN = 1;

    private static final int EVALUATION_TYPE_BOOST_PLUS = 0;
    private static final int EVALUATION_TYPE_JUNK_CLEAN = 1;
    private static final int EVALUATION_TYPE_BATTERY = 2;
    private static final int EVALUATION_TYPE_CPU_COOLER = 3;
    private static final int EVALUATION_TYPE_NOTIFICATION = 4;

    private static final int THRESHOLD_BOOST_APP_COUNT = 2;
    private static final int THRESHOLD_JUNK_SIZE = 50;
    private static final int THRESHOLD_BATTERY_LEVEL = 40;
    private static final int THRESHOLD_CPU_TEMPERATURE = 43;
    private static final int THRESHOLD_NOTIFICATION_COUNT = 6;

    private static final String[] FLURRY_VALUE_TYPE = new String[]{
            "Boost+",
            "JunkCleaner",
            "Battery",
            "CPU",
            "Notification"
    };

    private class Model {
        int type;
        int iconDrawableId;
        String titleString;
        String descriptionString;
        int buttonResourceId;
    }

    private int mType;
    private long mMemoryCache = 0;
    private int mBoostAppCount = 0;
    private List<Model> models = new ArrayList<>();
    private int mFirstEvaluatedType;

    private AppCompatImageView mIvBanner;
    private TextView mTvSubtitle;

    private View mContainerSingle;
    private ImageView mSingleIcon;
    private TextView mSingleTitle;
    private TextView mSingleButton;

    private View mContainerDouble;
    private View mContainerDoubleLeft;
    private View mContainerDoubleRight;
    private ImageView mDoubleLeftIcon;
    private TextView mDoubleLeftTitle;
    private TextView mDoubleLeftDescription;
    private TextView mDoubleLeftButton;
    private ImageView mDoubleRightIcon;
    private TextView mDoubleRightTitle;
    private TextView mDoubleRightDescription;
    private TextView mDoubleRightButton;

    private Runnable mEvaluationRunnable = () -> {
        evaluate(EVALUATION_TYPE_BATTERY);
        evaluate(EVALUATION_TYPE_CPU_COOLER);
        evaluate(EVALUATION_TYPE_NOTIFICATION);

        showPromotionViews();
    };

    public ResultEmptyView(Context context) {
        this(context, null);
    }

    public ResultEmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResultEmptyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();

        mIvBanner = ViewUtils.findViewById(this, R.id.iv_banner);
        mTvSubtitle = ViewUtils.findViewById(this, R.id.tv_subtitle);
        mContainerSingle = ViewUtils.findViewById(this, R.id.container_single);
        mSingleIcon = ViewUtils.findViewById(this, R.id.notification_icon_single);
        mSingleTitle = ViewUtils.findViewById(this, R.id.notification_title_single);
        mSingleButton = ViewUtils.findViewById(this, R.id.notification_btn_single_text);

        mContainerDouble = ViewUtils.findViewById(this, R.id.container_double);
        mContainerDoubleLeft = ViewUtils.findViewById(this, R.id.container_double_left);
        mContainerDoubleRight = ViewUtils.findViewById(this, R.id.container_double_right);
        mDoubleLeftIcon = ViewUtils.findViewById(this, R.id.double_left_icon);
        mDoubleLeftTitle = ViewUtils.findViewById(this, R.id.double_left_title);
        mDoubleLeftDescription = ViewUtils.findViewById(this, R.id.double_left_description);
        mDoubleLeftButton = ViewUtils.findViewById(this, R.id.double_left_button);
        mDoubleRightIcon = ViewUtils.findViewById(this, R.id.double_right_icon);
        mDoubleRightTitle = ViewUtils.findViewById(this, R.id.double_right_title);
        mDoubleRightDescription = ViewUtils.findViewById(this, R.id.double_right_description);
        mDoubleRightButton = ViewUtils.findViewById(this, R.id.double_right_button);
    }

    public void setType(int type) {
        mType = type;

        if (mType == TYPE_JUNK_CLEAN) {
            VectorCompat.setImageViewVectorResource(getContext(), mIvBanner, R.drawable.boost_plus_opt_page_clean);
            mTvSubtitle.setText(getContext().getString(R.string.optimalpage_junkclean_desc));
        }
    }

    public void setMemoryCache(long memoryCache) {
        mMemoryCache = memoryCache / 1024 / 1024;
    }

    public void startPromotionEvaluation() {
        models.clear();

        if (mType == TYPE_BOOST_PLUS) {
            evaluate(EVALUATION_TYPE_JUNK_CLEAN);
            mEvaluationRunnable.run();
        } else if (mType == TYPE_JUNK_CLEAN) {
            evaluate(EVALUATION_TYPE_BOOST_PLUS);
        }
    }

    private void showPromotionViews() {
        if (models.size() == 1) {
            Model model = models.get(0);

            mContainerSingle.setTranslationY(CommonUtils.pxFromDp(90));
            mContainerSingle.setVisibility(View.VISIBLE);
            mSingleIcon.setImageResource(model.iconDrawableId);
            mSingleTitle.setText(model.titleString);
            mSingleButton.setText(model.buttonResourceId);
            bindClickEvent(mContainerSingle, model.type, false);
            bindClickEvent(mSingleButton, model.type, false);

            mContainerSingle.animate()
                    .translationY(0)
                    .setDuration(500)
                    .setStartDelay(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            Map<String, String> params = new HashMap<>();
            params.put("Type", FLURRY_VALUE_TYPE[model.type]);
            params.put("Number", "One");
            HSAnalytics.logEvent("OptimalPage_Module_Show", params);
        } else if (models.size() == 2) {
            mContainerDouble.setTranslationY(CommonUtils.pxFromDp(150));
            mContainerDouble.setVisibility(View.VISIBLE);

            Model leftModel = models.get(0).type == EVALUATION_TYPE_BOOST_PLUS ? models.get(0) : models.get(1);
            Model rightModel = models.get(0).type == EVALUATION_TYPE_BOOST_PLUS ? models.get(1) : models.get(0);

            mDoubleLeftIcon.setImageResource(leftModel.iconDrawableId);
            mDoubleLeftTitle.setText(leftModel.titleString);
            mDoubleLeftDescription.setText(leftModel.descriptionString);
            mDoubleLeftButton.setText(leftModel.buttonResourceId);
            bindClickEvent(mContainerDoubleLeft, leftModel.type, true);

            mDoubleRightIcon.setImageResource(rightModel.iconDrawableId);
            mDoubleRightTitle.setText(rightModel.titleString);
            mDoubleRightDescription.setText(rightModel.descriptionString);
            mDoubleRightButton.setText(rightModel.buttonResourceId);
            bindClickEvent(mContainerDoubleRight, rightModel.type, true);

            mContainerDouble.animate()
                    .translationY(0)
                    .setDuration(500)
                    .setStartDelay(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();

            Map<String, String> params = new HashMap<>();
            params.put("Type", FLURRY_VALUE_TYPE[leftModel.type]);
            params.put("Number", "Two");
            HSAnalytics.logEvent("OptimalPage_Module_Show", params);
            params = new HashMap<>();
            params.put("Type", FLURRY_VALUE_TYPE[rightModel.type]);
            params.put("Number", "Two");
            HSAnalytics.logEvent("OptimalPage_Module_Show", params);
        }
    }

    private void bindClickEvent(View view, int type, boolean isDouble) {
        switch (type) {
            case EVALUATION_TYPE_JUNK_CLEAN:
                view.setOnClickListener(v -> {
                    NavUtils.startActivitySafely(getContext(), new Intent(getContext(), JunkCleanActivity.class));
                    ((Activity) getContext()).finish();

                    Map<String, String> params = new HashMap<>();
                    params.put("Type", FLURRY_VALUE_TYPE[type]);
                    params.put("Number", isDouble ? "Two" : "One");
                    HSAnalytics.logEvent("OptimalPage_Module_Click", params);
                    HSAnalytics.logEvent("JunkCleaner_Open", "Type", "OptimalPage");
                });
                break;
            case EVALUATION_TYPE_BOOST_PLUS:
                view.setOnClickListener(v -> {
                    NavUtils.startActivitySafely(getContext(), new Intent(getContext(), BoostPlusActivity.class));
                    ((Activity) getContext()).finish();

                    Map<String, String> params = new HashMap<>();
                    params.put("Type", FLURRY_VALUE_TYPE[type]);
                    params.put("Number", isDouble ? "Two" : "One");
                    HSAnalytics.logEvent("OptimalPage_Module_Click", params);
                    HSAnalytics.logEvent("BoostPlus_Open", "Type", "OptimalPage");
                });
                break;
            case EVALUATION_TYPE_BATTERY:
                view.setOnClickListener(v -> {
                    NavUtils.startActivitySafely(getContext(), new Intent(getContext(), BatteryActivity.class));
                    ((Activity) getContext()).finish();

                    Map<String, String> params = new HashMap<>();
                    params.put("Type", FLURRY_VALUE_TYPE[type]);
                    params.put("Number", isDouble ? "Two" : "One");
                    HSAnalytics.logEvent("OptimalPage_Module_Click", params);
                    HSAnalytics.logEvent("Battery_OpenFrom", "type", "OptimalPage");
                });
                break;
            case EVALUATION_TYPE_CPU_COOLER:
                view.setOnClickListener(v -> {
                    NavUtils.startActivitySafely(getContext(), new Intent(getContext(), CpuCoolerScanActivity.class));
                    ((Activity) getContext()).finish();

                    Map<String, String> params = new HashMap<>();
                    params.put("Type", FLURRY_VALUE_TYPE[type]);
                    params.put("Number", isDouble ? "Two" : "One");
                    HSAnalytics.logEvent("OptimalPage_Module_Click", params);
                    HSAnalytics.logEvent("CPUCooler_Open", "Type", "OptimalPage");
                });
                break;
            case EVALUATION_TYPE_NOTIFICATION:
                view.setOnClickListener(v -> {
                    ((Activity) getContext()).finish();

                    Map<String, String> params = new HashMap<>();
                    params.put("Type", FLURRY_VALUE_TYPE[type]);
                    params.put("Number", isDouble ? "Two" : "One");
                    HSAnalytics.logEvent("OptimalPage_Module_Click", params);
                });
                break;
            default:
                break;
        }
    }

    private void evaluate(int evaluateType) {
        if (models.size() >= 2) {
            return;
        }

        switch (evaluateType) {
            case EVALUATION_TYPE_BOOST_PLUS:
                long lastOpenBoostPlusTime = PreferenceHelper.get(LauncherFiles.BOOST_PREFS)
                        .getLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, -1);
                if (System.currentTimeMillis() - lastOpenBoostPlusTime > 5 * DateUtils.MINUTE_IN_MILLIS) {
                    DeviceUtils.getRunningPackageListFromMemory(false, appCount -> {
                        if (appCount >= THRESHOLD_BOOST_APP_COUNT) {
                            mBoostAppCount = appCount;
                            if (models.size() == 0) {
                                mFirstEvaluatedType = EVALUATION_TYPE_BOOST_PLUS;

                                Model model = new Model();
                                model.type = EVALUATION_TYPE_BOOST_PLUS;
                                model.iconDrawableId = R.drawable.empty_view_boost_big;
                                model.titleString = getContext().getString(R.string.result_empty_view_boost_title_single, String.valueOf(appCount));
                                model.buttonResourceId = R.string.result_empty_view_boost_buttton;
                                models.add(model);
                            } else {
                                Model model = new Model();
                                model.type = EVALUATION_TYPE_BOOST_PLUS;
                                model.iconDrawableId = R.drawable.empty_view_boost_small;
                                model.titleString = getContext().getString(R.string.result_empty_view_boost_title_double, String.valueOf(appCount));
                                model.descriptionString = getContext().getString(R.string.result_empty_view_boost_description_double);
                                model.buttonResourceId = R.string.result_empty_view_boost_buttton;
                                models.add(model);

                                reEvaluateFirstModel();
                            }
                        }

                        mEvaluationRunnable.run();
                    });
                } else {
                    mEvaluationRunnable.run();
                }
                break;
            case EVALUATION_TYPE_JUNK_CLEAN:
                long lastJunkCleanUsedTime = PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS)
                        .getLong(ResultConstants.PREF_KEY_LAST_JUNK_CLEAN_USED_TIME, -1);
                if (mMemoryCache >= THRESHOLD_JUNK_SIZE
                        && System.currentTimeMillis() - lastJunkCleanUsedTime > 5 * DateUtils.MINUTE_IN_MILLIS) {
                    if (models.size() == 0) {
                        mFirstEvaluatedType = EVALUATION_TYPE_JUNK_CLEAN;

                        Model model = new Model();
                        model.type = EVALUATION_TYPE_JUNK_CLEAN;
                        model.iconDrawableId = R.drawable.empty_view_clean_big;
                        model.titleString = getContext().getString(R.string.result_empty_view_clean_title_single, String.valueOf(mMemoryCache));
                        model.buttonResourceId = R.string.result_empty_view_clean_buttton;
                        models.add(model);
                    } else {
                        Model model = new Model();
                        model.type = EVALUATION_TYPE_JUNK_CLEAN;
                        model.iconDrawableId = R.drawable.empty_view_clean_small;
                        model.titleString = getContext().getString(R.string.result_empty_view_clean_title_double, String.valueOf(mMemoryCache));
                        model.descriptionString = getContext().getString(R.string.result_empty_view_clean_description_double);
                        model.buttonResourceId = R.string.result_empty_view_clean_buttton;
                        models.add(model);

                        reEvaluateFirstModel();
                    }
                }
                break;
            case EVALUATION_TYPE_BATTERY:
                if (DeviceManager.getInstance().getBatteryLevel() < THRESHOLD_BATTERY_LEVEL
                        && !BatteryUtils.hasUserUsedBatteryRecently(5 * DateUtils.MINUTE_IN_MILLIS)) {
                    if (models.size() == 0) {
                        mFirstEvaluatedType = EVALUATION_TYPE_BATTERY;

                        Model model = new Model();
                        model.type = EVALUATION_TYPE_BATTERY;
                        model.iconDrawableId = R.drawable.empty_view_battery_big;
                        model.titleString = getContext().getString(R.string.result_empty_view_battery_title_single,
                                String.valueOf(DeviceManager.getInstance().getBatteryLevel()));
                        model.buttonResourceId = R.string.result_empty_view_battery_buttton;
                        models.add(model);
                    } else {
                        Model model = new Model();
                        model.type = EVALUATION_TYPE_BATTERY;
                        model.iconDrawableId = R.drawable.empty_view_battery_small;
                        model.titleString = getContext().getString(R.string.result_empty_view_battery_title_double,
                                String.valueOf(DeviceManager.getInstance().getBatteryLevel()));
                        model.descriptionString = getContext().getString(R.string.result_empty_view_battery_description_double);
                        model.buttonResourceId = R.string.result_empty_view_battery_buttton;
                        models.add(model);

                        reEvaluateFirstModel();
                    }
                }
                break;
            case EVALUATION_TYPE_CPU_COOLER:
                int cpuTemperature = (int) DeviceManager.getInstance().getCpuTemperatureCelsius();
                if (cpuTemperature >= THRESHOLD_CPU_TEMPERATURE
                        && !CpuPreferenceHelper.hasUserUsedCpuCoolerRecently(5 * DateUtils.MINUTE_IN_MILLIS)) {
                    if (models.size() == 0) {
                        mFirstEvaluatedType = EVALUATION_TYPE_CPU_COOLER;

                        Model model = new Model();
                        model.type = EVALUATION_TYPE_CPU_COOLER;
                        model.iconDrawableId = R.drawable.empty_view_cpu_big;
                        model.titleString = getContext().getString(R.string.result_empty_view_cpucooler_title_single,
                                String.valueOf(cpuTemperature));
                        model.buttonResourceId = R.string.result_empty_view_cpucooler_buttton;
                        models.add(model);
                    } else {
                        Model model = new Model();
                        model.type = EVALUATION_TYPE_CPU_COOLER;
                        model.iconDrawableId = R.drawable.empty_view_cpu_small;
                        model.titleString = getContext().getString(R.string.result_empty_view_cpucooler_title_double,
                                String.valueOf(cpuTemperature));
                        model.descriptionString = getContext().getString(R.string.result_empty_view_cpucooler_description_double);
                        model.buttonResourceId = R.string.result_empty_view_cpucooler_buttton;
                        models.add(model);

                        reEvaluateFirstModel();
                    }
                }
                break;
//            case EVALUATION_TYPE_NOTIFICATION:
//                int notificationCount = NotificationCleanerProvider.fetchBlockedAndTimeValidNotificationCount(false);
//                long lastNotificationCleanerUsedTime = PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS)
//                        .getLong(ResultConstants.PREF_KEY_LAST_NOTIFICATION_CLEANER_USED_TIME, -1);
//                if (notificationCount >= THRESHOLD_NOTIFICATION_COUNT
//                        && System.currentTimeMillis() - lastNotificationCleanerUsedTime > 5 * DateUtils.MINUTE_IN_MILLIS) {
//                    if (models.size() == 0) {
//                        mFirstEvaluatedType = EVALUATION_TYPE_NOTIFICATION;
//
//                        Model model = new Model();
//                        model.type = EVALUATION_TYPE_NOTIFICATION;
//                        model.iconDrawableId = R.drawable.empty_view_notification_big;
//                        model.titleString = getContext().getString(R.string.result_empty_view_notification_title_single,
//                                String.valueOf(notificationCount));
//                        model.buttonResourceId = R.string.result_empty_view_notification_buttton;
//                        models.add(model);
//                    } else {
//                        Model model = new Model();
//                        model.type = EVALUATION_TYPE_NOTIFICATION;
//                        model.iconDrawableId = R.drawable.empty_view_notification_small;
//                        model.titleString = getContext().getString(R.string.result_empty_view_notification_title_double,
//                                String.valueOf(notificationCount));
//                        model.descriptionString = getContext().getString(R.string.result_empty_view_notification_description_double);
//                        model.buttonResourceId = R.string.result_empty_view_notification_buttton;
//                        models.add(model);
//
//                        reEvaluateFirstModel();
//                    }
//                }
//                break;
            default:
                break;
        }
    }

    private void reEvaluateFirstModel() {
        if (mFirstEvaluatedType >= 0) {
            if (mFirstEvaluatedType == EVALUATION_TYPE_BOOST_PLUS) {
                models.get(0).iconDrawableId = R.drawable.empty_view_boost_small;
                models.get(0).titleString = getContext().getString(R.string.result_empty_view_boost_title_double, String.valueOf(mBoostAppCount));
                models.get(0).descriptionString = getContext().getString(R.string.result_empty_view_boost_description_double);
            } else {
                int typeToReEvaluate = mFirstEvaluatedType;
                mFirstEvaluatedType = -1;
                models.remove(0);
                evaluate(typeToReEvaluate);
            }
        }
    }
}
