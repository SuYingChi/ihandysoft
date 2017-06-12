package com.ihs.feature.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.feature.boost.BoostConditionManager;
import com.ihs.feature.boost.BoostSource;
import com.ihs.feature.boost.BoostTipInfo;
import com.ihs.feature.boost.BoostType;
import com.ihs.keyboardutils.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("WeakerAccess")
public class LauncherTipManager {

    public enum TipType {
        SET_AS_DEFAULT_ACTIVITY,
        SET_AS_DEFAULT_RETRY, // Asks user to set us as default home again
        SET_AS_DEFAULT, // Asks user to set us as default home
        SET_AS_DEFAULT_BOOSTED, // Asks user to set us as default home after boosted
        REMOVE_SEARCH_BAR_TIP,
        REMOVE_MOMENT_TIP,
        REMOVE_FOLDER_TIP,
        UNPACK_FOLDER_TIP,
        APPLY_THEME, // Invites user to apply a newly installed theme
        FIVE_STAR_RATE, // Asks user to rate on Google Play
        BOOST_PLUS_AUTHORIZE, // Asks for Accessibility permission
        USAGE_ACCESS_AUTHORIZE, // Asks for Usage Access permission
        DEVICE_ADMIN_AUTHORIZE, // Asks user to set us as Device Admin
        TURN_ON_ACCESSIBILITY_GUIDE,
        TURN_ON_BADGE_GUIDE,
        SET_AS_DEFAULT_GUIDE, // Show "Select Air Launcher" at the bottom when user is brought to home picker page in Settings
        ADVANCED_BOOST, // Suggest user to perform an advanced boost
        BOOST_TIP,
        NEW_INSTALL_APP_TIP,
        NEED_BOOST_TIP,
        MOMENT_GUIDE,
        SEARCH_BAR_GUIDE,
        HIDE_APP_GUIDE,
        ICON_BADGE,
        WEATHER_GUIDE,
        NOTIFICATION_TIP,
        UPDATE_APK_TIP,
        UPDATE_APK_INSTALL_TIP,
        BATTERY_LOW,
        CHARGING_SCREEN_GUIDE,
        NOTIFICATION_AUTHORIZE, // Asks for Notification Access permission
        AUTO_CLEAN_AUTHORIZE,
        GENERAL,
        JUNK_CLEAN_INSTALL_TIP,
        JUNK_CLEAN_UNINSTALL_TIP,
        BOOST_PLUS_ACCESSIBILITY_TIP,
        PROMOTION_GUIDE_TIP,
        FOLDER_CLOSE_AD, // Pop-up ad after the user closes folder
    }

    public enum TriggerType {
        RETURN_TO_LAUNCHER,
        FINISH_BOOST,
    }

    public enum ResultType {
        NOT_SHOW,
        NEXT_SHOW,
        SHOW_AFTER_CURRENT,
        SHOW,
        FOCUS_SHOW,
    }

    public class TipEnvironment {
        public Context context;
        public TipType requestShowTipType;
        public Object[] extras;
        int returnToLauncherCount;
        int finishBoostCount;
        public boolean result = true;

        TipEnvironment deepClone() {
            TipEnvironment ret = new TipEnvironment();
            ret.context = context;
            ret.returnToLauncherCount = returnToLauncherCount;
            ret.finishBoostCount = finishBoostCount;
            ret.result = result;
            if (extras != null) {
                ret.extras = extras.clone();
            }
            return ret;
        }

        TipEnvironment copyFrom(TipEnvironment env) {
            if (env == null) {
                return null;
            }
            context = env.context;
            requestShowTipType = env.requestShowTipType;
            returnToLauncherCount = env.returnToLauncherCount;
            finishBoostCount = env.finishBoostCount;
            result = env.result;
            if (env.extras != null) {
                extras = env.extras.clone();
            }
            return this;
        }

        public boolean hasTipShow() {
            return currentShowTipType != null;
        }

        // FIXME always true
        boolean isAnimationRunning() {
            return isAnimationRunning;
        }

        public boolean shouldShowWeakTipWithoutEnable() {
            return !hasTipShow() && !isAnimationRunning();
        }

        public boolean shouldShowWeakTip() {
            return !hasTipShow() && !isAnimationRunning() && mEnabled;
        }

        public int getReturnToLauncherCount() {
            return returnToLauncherCount;
        }

        public int getFinishBoostCount() {
            return finishBoostCount;
        }

        @Override
        public String toString() {
            return "TipEnvironment{" +
                    "context=" + context +
                    ", returnToLauncherCount=" + returnToLauncherCount +
                    ", finishBoostCount=" + finishBoostCount +
                    ", extras=" + Arrays.toString(extras) +
                    ", requestShowTipType=" + requestShowTipType +
                    ", currentShowTipType=" + currentShowTipType +
                    ", focusShowTipType=" + focusShowTipType +
                    '}';
        }
    }

    private static final TipType[] RETURN_TO_LAUNCHER_TIPS = new TipType[]{
            TipType.SET_AS_DEFAULT,
            TipType.FIVE_STAR_RATE,     // extra: From
            TipType.MOMENT_GUIDE,
            TipType.SEARCH_BAR_GUIDE,
            TipType.ICON_BADGE,
            TipType.WEATHER_GUIDE,
            TipType.UPDATE_APK_INSTALL_TIP,
    };

    private static final TipType[] DESKTOP_SMALL_TIPS = new TipType[]{
            TipType.HIDE_APP_GUIDE,
            TipType.MOMENT_GUIDE,
            TipType.SEARCH_BAR_GUIDE,
    };

    private static final TipType[] FINISH_BOOST_TIPS = new TipType[]{
            TipType.SET_AS_DEFAULT,
            TipType.FIVE_STAR_RATE,     // extra: From
            TipType.BOOST_TIP,          // extra: BoostType, int
    };

    private static final String PREF_KEY_BOOST_TIMES_PER_DAY_PREFIX = "boost_times_per_day_";
    private static final int AFTER_SHOW_TIP_DELAY = 500;

    private static volatile LauncherTipManager instance;
    private TipEnvironment environment;
    private Map<TipType, ITipInfo> tipInfos;
    private List<TipEnvironment> afterTipList;
    private Map<TipType, TipEnvironment> nextTipList;
    private int mDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

    private TipType currentShowTipType;
    private TipType focusShowTipType;
    private Handler handler;

    @Thunk
    boolean mEnabled = true;
    @Thunk
    boolean isAnimationRunning = false;

    private LauncherTipManager() {
        environment = new TipEnvironment();
        tipInfos = new HashMap<>();
        afterTipList = new ArrayList<>();
        nextTipList = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());

        HSGlobalNotificationCenter.addObserver(LauncherConstants.NOTIFICATION_TIP_DISMISS, new INotificationObserver() {
            @Override
            public void onReceive(String name, HSBundle info) {
                if (LauncherConstants.NOTIFICATION_TIP_DISMISS.equals(name)) {
                    notifyDismiss();
                }
            }
        });
    }

    public static LauncherTipManager getInstance() {
        if (instance == null) {
            synchronized (BoostConditionManager.class) {
                if (instance == null) {
                    instance = new LauncherTipManager();
                }
            }
        }
        return instance;
    }

    public void setAnimationRunning(boolean running, long duration) {
        isAnimationRunning = running;
        if (!running && !environment.hasTipShow()) {
            notifyDismiss();
        }
        if (running && duration > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isAnimationRunning = false;
                }
            }, duration);
        }
    }
    public static final String PREF_KEY_DEFAULT_SCREEN_VISIT_COUNT = "default.screen.visit.count";


    public TipEnvironment getEnvironment() {
        environment.returnToLauncherCount = PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS)
                .getInt(PREF_KEY_DEFAULT_SCREEN_VISIT_COUNT, 0);
        return environment.deepClone();
    }

    public ResultType showTip(Context context, TipType type, Object... extras) {
        TipEnvironment env = getEnvironment();
        env.context = context;
        env.requestShowTipType = type;
        env.extras = extras;

        return showTip(env);
    }

    protected ResultType showTip(TipEnvironment env) {
        if (!mEnabled) {
            return ResultType.NOT_SHOW;
        }

        ResultType retType = ResultType.NOT_SHOW;
        if (env.requestShowTipType != null) {
            retType = checkTipShow(env.requestShowTipType, env);
            HSLog.d("showTip retType == " + retType + "\n env == " + env);

            boolean isRecodeConflict = true;
            TipType cur = currentShowTipType;
            TipType req = env.requestShowTipType;
            if (retType == ResultType.SHOW_AFTER_CURRENT) {
                afterTipList.add(env);
            } else if (retType == ResultType.NEXT_SHOW) {
                nextTipList.put(env.requestShowTipType, env);
            } else if (retType == ResultType.FOCUS_SHOW) {
                doShow(env, true);
            } else if (retType == ResultType.SHOW) {
                doShow(env, false);
                isRecodeConflict = false;
            } else {
                isRecodeConflict = false;
            }

            if (isRecodeConflict && cur != null && req != null) {
                HSAnalytics.logEvent("Remind_Conflict", "type", cur.name() + "," + req.name());
            }
            if (!env.result) {
                HSLog.w("showTip failed to not show");
                retType = ResultType.NOT_SHOW;
            }
        }
        return retType;
    }

    public void dismiss(TipType type) {
        if (type == currentShowTipType) {
            ITipInfo info = tipInfos.get(type);
            info.dismiss();
        }
    }

    public void notifyDismiss() {
        boolean showAfter = false;
        environment.requestShowTipType = null;

        if (focusShowTipType != null) {
            if (focusShowTipType == currentShowTipType) {
                focusShowTipType = null;
            }
        } else {
            showAfter = true;
        }

        HSLog.d("showTip dismiss  after == " + showAfter + "  afterList.size == " + afterTipList.size());
        currentShowTipType = null;
        environment.extras = null;
        environment.context = null;

        if (showAfter && afterTipList.size() > 0) {
            TipEnvironment env = afterTipList.remove(0);
            doShow(env, false, AFTER_SHOW_TIP_DELAY);
        }
    }

    public void showFinishBoostAlert(Context context, BoostSource source, BoostType type, int boostedPercentage) {
        if (!mEnabled) {
            HSLog.d("FloatWindowManager", "Disable at now");
            return;
        }
        showBoostTip(context, source, type, boostedPercentage);
    }

    private void showBoostTip(Context context, BoostSource source, BoostType type, int boostedPercentage) {
        if (CommonUtils.ATLEAST_JB_MR2 && boostedPercentage >= 5 && source != BoostSource.LOCKER_TOGGLE) {
            PreferenceHelper.getDefault().incrementAndGetInt(PREF_KEY_BOOST_TIMES_PER_DAY_PREFIX + mDayOfYear);
        }
        showTip(context, TipType.BOOST_TIP, type, boostedPercentage, source);
    }

    int getBoostTimesToday() {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        if (mDayOfYear == day) {
            return HSPreferenceHelper.getDefault().getInt(PREF_KEY_BOOST_TIMES_PER_DAY_PREFIX + mDayOfYear, 0);
        } else {
            HSPreferenceHelper.getDefault().remove(PREF_KEY_BOOST_TIMES_PER_DAY_PREFIX + mDayOfYear);
            mDayOfYear = day;
            return 0;
        }
    }

    private ResultType checkTipShow(TipType type, TipEnvironment env) {
        ITipInfo info = tipInfos.get(type);
        if (info == null) {
            info = createTipInfo(type);
            tipInfos.put(type, info);
        }
        ResultType retType = ResultType.NOT_SHOW;
        if (info.isValidExtras(env)) {
            retType = info.wantToShow(env);
        }

        return retType;
    }

    private boolean doShow(TipEnvironment env, boolean isFocus) {
        return doShow(env, isFocus, 0);
    }

    private boolean doShow(final TipEnvironment env, boolean isFocus, long delay) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            HSLog.e("no main looper");
        }
        final ITipInfo info = tipInfos.get(env.requestShowTipType);
        if (isFocus) {
            focusShowTipType = info.getTipType();
            if (currentShowTipType != null) {
                ITipInfo curTip = tipInfos.get(currentShowTipType);
                if (curTip != null) {
                    curTip.dismiss();
                }
            }
        }

        if (delay != 0) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    environment.copyFrom(env);
                    currentShowTipType = info.getTipType();
                    HSLog.d("showTip doShow == " + info.getTipType() + "\n env == " + environment);
                    info.show(environment);
                }
            }, delay);
        } else {
            environment.copyFrom(env);
            currentShowTipType = info.getTipType();
            HSLog.d("showTip doShow == " + info.getTipType() + "\n env == " + environment);
            info.show(environment);
        }

        return environment.result;
    }

    private ITipInfo createTipInfo(TipType type) {
        ITipInfo info = null;
        switch (type) {
            case BOOST_TIP:
                info = new BoostTipInfo();
                break;
            case NEED_BOOST_TIP:
                info = new NeedBoostTipInfo();
                break;
            default:
                break;
        }

        if (info == null) {
            throw new RuntimeException("no ITipInfo found!! ");
        }
        return info;
    }
}
