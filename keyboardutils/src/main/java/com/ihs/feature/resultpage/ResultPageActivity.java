package com.ihs.feature.resultpage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;

import com.ihs.app.framework.activity.HSAppCompatActivity;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.common.ActivityUtils;
import com.ihs.feature.common.BaseCenterActivity;
import com.ihs.feature.common.ConcurrentUtils;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.NotificationCenter;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.resultpage.data.CardData;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.kc.utils.KCAnalytics;

import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.List;


public class ResultPageActivity extends HSAppCompatActivity
        implements ResultPageContracts.View, INotificationObserver {

    public static final String TAG = "ResultPageActivity";

    /**
     * Notification sent when this activity becomes visible to user.
     * We shall start result page animations at this notification.
     */
    public static final String NOTIFICATION_VISIBLE_TO_USER = "result_page_visible_to_user";
    public static final String NOTIFICATION_RESULT_PAGE_ATTACHED = "result_page_attached_to_window";

    public static final String EXTRA_KEY_RESULT_TYPE = "EXTRA_KEY_RESULT_TYPE";
    public static final String EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE = "EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE";
    public static final String EXTRA_KEY_BATTERY_OPTIMAL = "EXTRA_KEY_BATTERY_OPTIMAL";
    public static final String EXTRA_KEY_BATTERY_EXTEND_HOUR = "EXTRA_KEY_BATTERY_EXTEND_HOUR";
    public static final String EXTRA_KEY_BATTERY_EXTEND_MINUTE = "EXTRA_KEY_BATTERY_EXTEND_MINUTE";
    public static final String EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT = "EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT";

    public static final String PREF_KEY_INTO_BATTERY_PROTECTION_COUNT = "into_battery_protection_count";
    public static final String PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT = "into_notification_cleaner_count";

    public static final int INTO_RESULT_PAGE_COUNT_NULL = -1;
    public static final int BATTERY_PROTECTION_LIMIT_COUNT = 3;
    public static final int NOTIFICATION_CLEANER_LIMIT_COUNT = 3;

    /**
     * Responsible for resolving {@link ResultController.Type} and performing ad preload if needed.
     */
    private ResultPagePresenter mPresenter;

    private AcbNativeAd mAd;

    private int mResultType;
    private boolean mIsResultPageShow;

    /**
     * Responsible for doing actual animations.
     */
    private ResultController mResultController;

    private static BatteryActivity.RefreshListener sRefreshListener;
    private static boolean sAttached;
    private int mClearNotificationsCount;

    public static void startForBoostPlus(Activity activity, int cleanedSizeMbs) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
        intent.putExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, cleanedSizeMbs);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        KCAnalytics.logEvent("ResultPage_Show", "Type", ResultConstants.BOOST_PLUS);
    }

    public static void startForBattery(Activity activity,
                                       boolean isBatteryOptimal, String extendHour, String extendMinute, BatteryActivity.RefreshListener refreshListener) {
        if (activity == null) {
            return;
        }
        sRefreshListener = refreshListener;

        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BATTERY);
        intent.putExtra(EXTRA_KEY_BATTERY_OPTIMAL, isBatteryOptimal);
        intent.putExtra(EXTRA_KEY_BATTERY_EXTEND_HOUR, extendHour);
        intent.putExtra(EXTRA_KEY_BATTERY_EXTEND_MINUTE, extendMinute);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.no_anim, R.anim.no_anim);
        KCAnalytics.logEvent("ResultPage_Show", "Type", ResultConstants.BATTERY);
    }

    public static void startForJunkClean(Activity activity) {
        if (activity == null) {
            return;
        }
        if (JunkCleanConstant.sIsTotalSelected) {
            JunkCleanConstant.sIsTotalCleaned = true;
        }

        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_JUNK_CLEAN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.anim_popup, R.anim.anim_popup);
        KCAnalytics.logEvent("ResultPage_Show", "Type", ResultConstants.JUNK_CLEANER);
    }

    public static void startForCpuCooler(Activity activity) {
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_CPU_COOLER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.none, R.anim.none);
        KCAnalytics.logEvent("ResultPage_Show", "Type", ResultConstants.CPU_COOLER);
    }

    public static void startForNotificationCleaner(Activity activity, int clearNotificationsCount) {
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, ResultPageActivity.class);
        intent.putExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER);
        intent.putExtra(EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT, clearNotificationsCount);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.none, R.anim.none);
        KCAnalytics.logEvent("ResultPage_Show", "Type", ResultConstants.NOTIFICATION_CLEANER);
    }

    private void recordIntoBpAndNcCardTimes() {
        if (recordIntoBpCardTimes()) {
            PreferenceHelper prefNc = PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS);
            int lastNcCount = prefNc.getInt(PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT, 0);
            boolean isNcExpired = (lastNcCount >= NOTIFICATION_CLEANER_LIMIT_COUNT || lastNcCount == INTO_RESULT_PAGE_COUNT_NULL);
            int savedNcCount = isNcExpired ? INTO_RESULT_PAGE_COUNT_NULL : lastNcCount + 1;
            HSLog.d(TAG, "recordIntoBpAndNcCardTimes lastNcCount = " + lastNcCount + " savedNcCount = " + savedNcCount);
            prefNc.putInt(PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT, savedNcCount);
        }
    }

    private boolean recordIntoBpCardTimes() {
        PreferenceHelper prefBoost = PreferenceHelper.get(LauncherFiles.BOOST_PREFS);
        int lastBpCount = prefBoost.getInt(PREF_KEY_INTO_BATTERY_PROTECTION_COUNT, 0);

        boolean isBpExpired = (lastBpCount >= BATTERY_PROTECTION_LIMIT_COUNT || lastBpCount == INTO_RESULT_PAGE_COUNT_NULL || ChargingPrefsUtil.getInstance().isChargingEnabledBefore());
        int savedBpCount = isBpExpired ? INTO_RESULT_PAGE_COUNT_NULL : lastBpCount + 1;

        HSLog.d(TAG, "recordIntoBpCardTimes lastBpCount = " + lastBpCount + " savedBpCount = " + savedBpCount);
        prefBoost.putInt(PREF_KEY_INTO_BATTERY_PROTECTION_COUNT, savedBpCount);
        return isBpExpired;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HSLog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_page);
        recordIntoBpAndNcCardTimes();

        Intent intent = getIntent();
        if (null != intent) {
            mResultType = intent.getIntExtra(EXTRA_KEY_RESULT_TYPE, ResultConstants.RESULT_TYPE_BOOST_PLUS);
            mClearNotificationsCount = intent.getIntExtra(EXTRA_KEY_CLEAR_NOTIFICATONS_COUNT, 0);
            mPresenter = new ResultPagePresenter(this, mResultType);

            NotificationCenter.addObserver(NOTIFICATION_VISIBLE_TO_USER, this);
        } else {
            finish();
        }

        // Set bg color early
        ViewUtils.findViewById(this, R.id.bg_view).setBackgroundColor(getBackgroundColor());
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttachedToWindow() {
        HSLog.d(TAG, "onAttachedToWindow mResultType = " + mResultType + " mIsResultPageShow = " + mIsResultPageShow);
        super.onAttachedToWindow();
        CommonUtils.setupTransparentSystemBarsForLmp(this);
        View viewContainer = ViewUtils.findViewById(this, R.id.view_container);
        viewContainer.setPadding(0, CommonUtils.getStatusBarHeight(this), 0, 0);
        ActivityUtils.setNavigationBarColor(this, ContextCompat.getColor(this, android.R.color.black));
        if (mResultType != ResultConstants.RESULT_TYPE_BOOST_PLUS && !mIsResultPageShow) {
            mPresenter.show(ResultPageAdsManager.getInstance().getAd());
            mIsResultPageShow = true;
        }
        sAttached = true;
        ConcurrentUtils.postOnMainThread(() -> {
            if (sAttached) {
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_RESULT_PAGE_ATTACHED);
            }
        });
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sAttached = false;
    }

    public static boolean isAttached() {
        return sAttached;
    }

    @Override
    public void onReceive(String s, HSBundle hsBundle) {
        if (NOTIFICATION_VISIBLE_TO_USER.equals(s)) {
            HSLog.d(TAG, NOTIFICATION_VISIBLE_TO_USER + " notified, start show mIsResultPageShow = " + mIsResultPageShow);
            if (!mIsResultPageShow) {
                mPresenter.show(ResultPageAdsManager.getInstance().getAd());
                mIsResultPageShow = true;
            }
        }
    }

    private @ColorInt int getBackgroundColor() {
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                return ContextCompat.getColor(this, R.color.boost_plus_clean_bg);
            case ResultConstants.CARD_VIEW_TYPE_BATTERY:
                return ContextCompat.getColor(this, R.color.battery_green);
            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
                return ContextCompat.getColor(this, R.color.clean_primary_blue);
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                return ContextCompat.getColor(this, R.color.cpu_cooler_primary_blue);
        }
        return ContextCompat.getColor(this, R.color.boost_plus_clean_bg);
    }

    @Override
    public void show(ResultController.Type type, @Nullable AcbNativeAd ad, @Nullable List<CardData> cards) {
        String titleText;
        Intent intent = getIntent();
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                int cleanedSizeMbs = intent.getIntExtra(EXTRA_KEY_BOOST_PLUS_CLEANED_SIZE, 0);
                mResultController = new BoostPlusResultController(this, cleanedSizeMbs, type, ad, cards);
                titleText = getString(R.string.launcher_widget_boost_plus_title);
                break;
            case ResultConstants.RESULT_TYPE_BATTERY:
                boolean isBatteryOptimal = intent.getBooleanExtra(EXTRA_KEY_BATTERY_OPTIMAL, false);
                String extendHour = intent.getStringExtra(EXTRA_KEY_BATTERY_EXTEND_HOUR);
                String extendMinute = intent.getStringExtra(EXTRA_KEY_BATTERY_EXTEND_MINUTE);
                mResultController = new BatteryResultController(this, isBatteryOptimal, extendHour, extendMinute, type, ad, cards);
                titleText = getString(R.string.battery_title);
                break;
            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
                mResultController = new JunkCleanResultController(this, type, ad, cards);
                titleText = getString(R.string.clean_title);
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                mResultController = new CpuCoolerResultController(this, type, ad, cards);
                titleText = getString(R.string.promotion_max_card_title_cpu_cooler);
                break;
            default:
                throw new IllegalArgumentException("Unsupported result type.");
        }
        mAd = ad;
        ActivityUtils.configSimpleAppBar(this, titleText, Color.TRANSPARENT);
        startTransitionAnimation();
        if (null != sRefreshListener) {
            sRefreshListener.onResultViewFinished();
        }
    }


    private void startTransitionAnimation() {
        if (null != mResultController) {
            mResultController.startTransitionAnimation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishAndNotify();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishAndNotify();
    }

    void finishAndNotify() {
        HSGlobalNotificationCenter.sendNotification(BoostPlusActivity.NOTIFICATION_RETURN_FROM_CLEAN);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCurrentAd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sRefreshListener = null;
        mIsResultPageShow = false;
        ResultPageAdsManager.getInstance().releaseAd();
        HSGlobalNotificationCenter.removeObserver(this);
    }

    @Thunk
    void releaseCurrentAd() {
        if (mAd != null) {
            mAd.release();
        }
    }

    public void finishSelfAndParentActivity() {
        HSLog.d(ResultPageActivity.TAG, "ResultPageActivity finishSelfAndParentActivity");
        sendBroadcast(new Intent(BaseCenterActivity.INTENT_NOTIFICATION_ACTIVITY_FINISH_ACTION));
        finishAndNotify();
    }
}
