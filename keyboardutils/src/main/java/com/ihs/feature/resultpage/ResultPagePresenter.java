package com.ihs.feature.resultpage;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.annimon.stream.Stream;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.LauncherFiles;
import com.ihs.feature.common.PreferenceHelper;
import com.ihs.feature.common.Utils;
import com.ihs.feature.resultpage.data.CardData;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.permission.PermissionUtils;

import net.appcloudbox.ads.base.AcbNativeAd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Presents Boost+ / Battery / CPU Cooler / Junk Cleaner / Notification Cleaner result page contents.
 */
public class ResultPagePresenter implements ResultPageContracts.Presenter {

    public static final String TAG = ResultPagePresenter.class.getSimpleName();

    @SuppressWarnings("PointlessBooleanExpression")
    private static final boolean DEBUG_ALL_CARDS = true;// && BuildConfig.DEBUG;

    private static final String PREF_KEY_CARDS_SHOW_COUNT = "result_page_cards_show_count";
    private static final String PREF_KEY_SHOW_COUNT_PREFIX = "result_page_show_count_";

    private ResultPageContracts.View mView;

    private int mResultType;
    private ResultController.Type mType;

    ResultPagePresenter(@NonNull ResultPageContracts.View view, int resultType) {
        mView = view;
        mResultType = resultType;
    }

    @Override
    public void show(AcbNativeAd ad) {
        List<CardData> cards = null;
        recordFeatureLastUsedTime();

        mType = ResultController.Type.AD;
        if (!determineWhetherToShowChargeScreen() || DEBUG_ALL_CARDS) {
            mType = ResultController.Type.AD;
        }

        HSLog.d(TAG, "ResultPage mType = " + mType + " ad = " + ad + " cards = " + cards);
        mView.show(mType, ad, cards);
    }

    private void recordFeatureLastUsedTime() {
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BATTERY:
                PreferenceHelper.get(LauncherFiles.BATTERY_PREFS)
                        .putLong(ResultConstants.PREF_KEY_LAST_BATTERY_USED_TIME, System.currentTimeMillis());
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                PreferenceHelper.get(LauncherFiles.BOOST_PREFS)
                        .putLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, System.currentTimeMillis());
                break;
            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
                PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS)
                        .putLong(ResultConstants.PREF_KEY_LAST_JUNK_CLEAN_USED_TIME, System.currentTimeMillis());
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                PreferenceHelper.get(LauncherFiles.CPU_COOLER_PREFS)
                        .putLong(ResultConstants.PREF_KEY_LAST_CPU_COOLER_USED_TIME, System.currentTimeMillis());
                break;
            case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
                PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS)
                        .putLong(ResultConstants.PREF_KEY_LAST_NOTIFICATION_CLEANER_USED_TIME, System.currentTimeMillis());
                break;
        }
    }

    private boolean determineWhetherToShowChargeScreen() {
        boolean isChargingScreenEverEnabled = ChargingPrefsUtil.getInstance().isChargingEnabledBefore();
        int intoCount = PreferenceHelper.get(LauncherFiles.BOOST_PREFS).getInt(ResultPageActivity.PREF_KEY_INTO_BATTERY_PROTECTION_COUNT, ResultPageActivity.INTO_RESULT_PAGE_COUNT_NULL);
        HSLog.d(TAG, "ResultPage show determineWhetherToShowChargeScreen mResultType = " + mResultType + " intoCount = " + intoCount + " isChargingScreenEverEnabled = " + isChargingScreenEverEnabled);
        if (intoCount != ResultPageActivity.INTO_RESULT_PAGE_COUNT_NULL && intoCount <= ResultPageActivity.BATTERY_PROTECTION_LIMIT_COUNT
                && intoCount > 0 && !isChargingScreenEverEnabled) {
            mType = ResultController.Type.CHARGE_SCREEN;
        }
        return mType == ResultController.Type.CHARGE_SCREEN;
    }

//    private boolean determineWhetherToShowNotificationCleaner() {
//        boolean isNotificationCleanerEnabled = NotificationCleanerProvider.isNotificationOrganizerSwitchOn();
//        if (mResultType != ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER) {
//            int intoCount = PreferenceHelper.get(LauncherFiles.NOTIFICATION_CLEANER_PREFS)
//                    .getInt(ResultPageActivity.PREF_KEY_INTO_NOTIFICATION_CLEANER_COUNT, ResultPageActivity.INTO_RESULT_PAGE_COUNT_NULL);
//            HSLog.d(TAG, "ResultPage show determineWhetherToShowNotificationCleaner mResultType = " + mResultType + " intoCount = " + intoCount);
//            if (intoCount != ResultPageActivity.INTO_RESULT_PAGE_COUNT_NULL
//                    && intoCount <= ResultPageActivity.NOTIFICATION_CLEANER_LIMIT_COUNT
//                    && intoCount > 0 && !isNotificationCleanerEnabled) {
//                mType = ResultController.Type.NOTIFICATION_CLEANER;
//            }
//        }
//        return mType == ResultController.Type.NOTIFICATION_CLEANER;
//    }

    private List<CardData> setupCards() {
        boolean shouldShowBatteryCard = shouldShowBatteryCard();
        boolean shouldShowBoostPlusCard = shouldShowBoostPlusCard();
        boolean shouldShowJunkCleanCard = shouldShowJunkCleanCard();
        boolean shouldShowCpuCoolerCard = shouldShowCpuCoolerCard();
        boolean shouldShowSecurityCard = shouldShowSecurityCard();
        boolean shouldShowMaxCard = shouldShowMaxCard();
        HSLog.d(TAG, "ResultPage Cards | shouldShowBatteryCard = " + shouldShowBatteryCard
                + ", shouldShowBoostPlusCard = " + shouldShowBoostPlusCard
                + ", shouldShowJunkCleanCard = " + shouldShowJunkCleanCard
                + ", shouldShowCpuCoolerCard = " + shouldShowCpuCoolerCard
                + ", shouldShowSecurityCard = " + shouldShowSecurityCard
                + ", shouldShowMaxCard = " + shouldShowMaxCard);

        // {@code allCards} is for ordering only, cards shown shall be added to {@code cards} below
        List<CardData> allCards = new ArrayList<>(9);
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_BATTERY));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_SECURITY));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_CPU_COOLER));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_MAX_GAME_BOOSTER));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_MAX_APP_LOCKER));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_MAX_DATA_THIEVES));
        allCards.add(new CardData(ResultConstants.CARD_VIEW_TYPE_ACCESSIBILITY));
        sortAndReorderCards(allCards);

        PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS).incrementAndGetInt(PREF_KEY_CARDS_SHOW_COUNT);

        List<CardData> cards = new ArrayList<>(4);
        if (DEBUG_ALL_CARDS) {
            cards.addAll(allCards);
            dumpCards(cards, "Debugging, all cards are displayed");
            return cards;
        }

        if (shouldShowSecurityCard) {
            addCard(cards, ResultConstants.CARD_VIEW_TYPE_SECURITY);
        }

        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BATTERY:
                if (shouldShowMaxCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_MAX_DATA_THIEVES);
                }
                if (shouldShowBoostPlusCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS);
                }
                if (shouldShowJunkCleanCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER);
                }
                if (shouldShowCpuCoolerCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_CPU_COOLER);
                }
                break;
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                if (shouldShowMaxCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_MAX_GAME_BOOSTER);
                }
                if (shouldShowBatteryCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BATTERY);
                }
                if (shouldShowJunkCleanCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER);
                }
                if (shouldShowCpuCoolerCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_CPU_COOLER);
                }
                if (!PermissionUtils.isAccessibilityGranted()) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_ACCESSIBILITY);
                }
                break;
            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
                if (shouldShowMaxCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_MAX_APP_LOCKER);
                }
                if (shouldShowBatteryCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BATTERY);
                }
                if (shouldShowBoostPlusCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS);
                }
                if (shouldShowCpuCoolerCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_CPU_COOLER);
                }
                break;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                if (shouldShowMaxCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_MAX_APP_LOCKER);
                }
                if (shouldShowBatteryCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BATTERY);
                }
                if (shouldShowBoostPlusCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS);
                }
                if (shouldShowJunkCleanCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER);
                }
                break;
            case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
                if (shouldShowMaxCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_MAX_APP_LOCKER);
                }
                if (shouldShowBatteryCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BATTERY);
                }
                if (shouldShowBoostPlusCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS);
                }
                if (shouldShowJunkCleanCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER);
                }
                if (shouldShowCpuCoolerCard) {
                    addCard(cards, ResultConstants.CARD_VIEW_TYPE_CPU_COOLER);
                }
                break;
        }
        if (cards.isEmpty()) {
            addCard(cards, ResultConstants.CARD_VIEW_TYPE_DEFAULT);
        }
        Collections.sort(cards, (c1, c2) -> allCards.indexOf(c1) - allCards.indexOf(c2));
        dumpCards(cards, "Displayed cards in final order");
        return cards;
    }

    /**
     * Check whether we shall show charging screen content for result page. If NOT, ad preload is needed.
     * This method is static so that checking is possible before actual presenter instantiation.
     *
     * @param resultType Result page type ({@link ResultConstants#RESULT_TYPE_BOOST_PLUS} or
     *                   {@link ResultConstants#RESULT_TYPE_BATTERY}).
     */
    public static boolean shouldShowAd(int resultType) {
        if (PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS).getInt(PREF_KEY_SHOW_COUNT_PREFIX + resultType, 0) >= 3) {
            HSLog.d(TAG, "Charging screen card NOT showing as result page has been visited for more than 3 times");
            return true;
        }
        if (ChargingPrefsUtil.getInstance().isChargingEnabledBefore()) {
            HSLog.d(TAG, "Charging screen card NOT showing as charging screen feature has been enabled at least once");
            return true;
        }
        return false;
    }

    private boolean shouldShowBatteryCard() {
        long lastBatteryUsedTime = PreferenceHelper.get(LauncherFiles.BATTERY_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_BATTERY_USED_TIME, -1);
        long timeSinceLastUse = System.currentTimeMillis() - lastBatteryUsedTime;
        HSLog.d(TAG, timeSinceLastUse + " ms since last used battery");
        return timeSinceLastUse > 5 * DateUtils.MINUTE_IN_MILLIS;
    }

    private boolean shouldShowBoostPlusCard() {
        long lastBoostPlusUsedTime = PreferenceHelper.get(LauncherFiles.BOOST_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_BOOST_PLUS_USED_TIME, -1);
        long timeSinceLastUse = System.currentTimeMillis() - lastBoostPlusUsedTime;
        HSLog.d(TAG, timeSinceLastUse + " ms since last used Boost+");
        return timeSinceLastUse > 5 * DateUtils.MINUTE_IN_MILLIS;
    }

    private boolean shouldShowJunkCleanCard() {
        long lastJunkCleanUsedTime = PreferenceHelper.get(LauncherFiles.JUNK_CLEAN_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_JUNK_CLEAN_USED_TIME, -1);
        long timeSinceLastUse = System.currentTimeMillis() - lastJunkCleanUsedTime;
        HSLog.d(TAG, timeSinceLastUse + " ms since last used Junk Clean");
        return timeSinceLastUse > 5 * DateUtils.MINUTE_IN_MILLIS;
    }

    private boolean shouldShowCpuCoolerCard() {
        long lastCpuCoolerUsedTime = PreferenceHelper.get(LauncherFiles.CPU_COOLER_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_CPU_COOLER_USED_TIME, -1);
        long timeSinceLastUse = System.currentTimeMillis() - lastCpuCoolerUsedTime;
        HSLog.d(TAG, timeSinceLastUse + " ms since last used Cpu Cooler");
        return timeSinceLastUse > 5 * DateUtils.MINUTE_IN_MILLIS;
    }

    private boolean shouldShowSecurityCard() {
        return shouldShowAppPromotionCard("SecurityPackage");
    }

    private boolean shouldShowMaxCard() {
        return shouldShowAppPromotionCard("MaxPackage");
    }

    private boolean shouldShowAppPromotionCard(String appConfigName) {
        boolean isNetworkAvailable = Utils.isNetworkAvailable(-1);
        boolean isAppEverInstalled = Utils.isPackageEverInstalled(
                HSConfig.optString("", "Application", "Promotions", appConfigName));
        HSLog.d(TAG, "isNetworkAvailable: " + isNetworkAvailable + ", isAppEverInstalled: " + isAppEverInstalled);
        return isNetworkAvailable && !isAppEverInstalled;
    }

    private void sortAndReorderCards(List<CardData> cards) {
        //noinspection unchecked
        Collections.sort(cards);

        dumpCards(cards, "All cards in descending priority order");

        int cardsCount = cards.size();
        int showCount = PreferenceHelper.get(LauncherFiles.DESKTOP_PREFS).getInt(PREF_KEY_CARDS_SHOW_COUNT, 0);
        int moveLastToFirstTimes = showCount % cardsCount;

        HSLog.d(TAG, "Move last to first for " + moveLastToFirstTimes + " time(s)");
        for (int i = 0; i < moveLastToFirstTimes; i++) {
            int lastPriority = cards.get(cardsCount - 1).getPriority();
            do {
                cards.add(0, cards.remove(cardsCount - 1));
            } while (cards.get(cardsCount - 1).getPriority() == lastPriority);
        }
        dumpCards(cards, "All cards in rotated order");
    }

    private void addCard(List<CardData> cards, int cardType) {
        cards.add(new CardData(cardType));
    }

    private void dumpCards(List<CardData> cards, String dumpName) {
        StringBuilder str = new StringBuilder(dumpName).append(": \n==========\n");
        Stream.of(cards).forEach(card -> str.append(card.toString()).append('\n'));
        HSLog.d(TAG, str.append("==========\n\n").toString());
    }
}
