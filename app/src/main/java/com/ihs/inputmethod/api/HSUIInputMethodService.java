package com.ihs.inputmethod.api;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.acb.adcaffe.nativead.AdCaffeNativeAd;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.ads.fullscreen.KeyboardFullScreenAd;
import com.ihs.inputmethod.analytics.KeyboardAnalyticsReporter;
import com.ihs.inputmethod.api.framework.HSEmojiSuggestionManager;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.specialcharacter.HSSpecialCharacterManager;
import com.ihs.inputmethod.constants.AdPlacements;
import com.ihs.inputmethod.feature.apkupdate.ApkUtils;
import com.ihs.inputmethod.feature.common.AdCaffeHelper;
import com.ihs.inputmethod.suggestions.CustomSearchEditText;
import com.ihs.inputmethod.uimodules.KeyboardPanelManager;
import com.ihs.inputmethod.uimodules.ui.adjustheight.AdjustHeightView;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.base.LanguageDao;
import com.ihs.inputmethod.uimodules.ui.sticker.Sticker;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerDataManager;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerPrefsUtil;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerUtils;
import com.ihs.inputmethod.websearch.WebContentSearchManager;
import com.ihs.keyboardutils.ads.KCInterstitialAd;
import com.ihs.keyboardutils.appsuggestion.AppSuggestionManager;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.kc.utils.KCAnalytics;
import com.kc.utils.KCFeatureControlUtils;
import com.keyboard.common.SplashActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ihs.inputmethod.framework.Constants.CODE_DELETE;

/**
 * Created by xu.zhang on 11/3/15.
 */
public abstract class HSUIInputMethodService extends HSInputMethodService implements AdCaffeHelper.OnNativeAdLoadListener {
    public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";
    private static final String GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending";
    private static final String GOOGLE_SEARCH_BAR_PACKAGE_NAME = "com.google.android.googlequicksearchbox";
    public static final String HS_NOTIFICATION_START_INPUT_INSIDE_CUSTOM_SEARCH_EDIT_TEXT = "CustomSearchEditText";

    private InputConnection insideConnection = null;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (reason != null && reason.equals("homekey")) {
                    getKeyboardPanelMananger().onHomePressed();
                }
            } else if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                if (intent.hasExtra("state")) {
                    int state = intent.getIntExtra("state", -1);
                    switch (state) {
                        case 0: //unPlugged
                            break;
                        case 1: // plug In
                            KCAnalytics.logEvent("headphone_pluggedin");
                            break;
                        default:
                    }
                }
            }
        }
    };
    private boolean isInputViewShowing = false;
    private String currentAppPackageName = "";
    private KeyboardFullScreenAd openFullScreenAd;
    private KeyboardFullScreenAd closeFullScreenAd;
    private AdCaffeHelper adCaffeHelper;
    private INotificationObserver keyboardNotificationObserver = (eventName, notification) -> {
        switch (eventName) {
            case HSInputMethod.HS_NOTIFICATION_START_INPUT_INSIDE:
                CustomSearchEditText customSearchEditText = (CustomSearchEditText) notification.getObject(HS_NOTIFICATION_START_INPUT_INSIDE_CUSTOM_SEARCH_EDIT_TEXT);
                onStartInputInside(customSearchEditText);
                break;
            case HSInputMethod.HS_NOTIFICATION_FINISH_INPUT_INSIDE:
                onFinishInputInside();
                break;
            case HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD:
                if (shouldShowGoogleAD()) {
                    getKeyboardPanelMananger().logCustomizeBarShowed();
                }
                break;
        }
    };
    private Handler handler = new Handler(Looper.getMainLooper());

    public static KeyboardPanelManager getKeyboardPanelMananger() {
        return (KeyboardPanelManager) keyboardPanelSwitcher;
    }

    @Override
    public void onCreate() {
        KeyboardAnalyticsReporter.getInstance().recordKeyboardOnCreateStart();
        super.onCreate();

        SplashActivity.recordAppFirstOpen("keyboard enable");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(this.receiver, intentFilter);

        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_START_INPUT_INSIDE, keyboardNotificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_FINISH_INPUT_INSIDE, keyboardNotificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD, keyboardNotificationObserver);

        KeyboardAnalyticsReporter.getInstance().recordKeyboardOnCreateEnd();
        openFullScreenAd = new KeyboardFullScreenAd(AdPlacements.INTERSTITIAL_SPRING, "Open");
        closeFullScreenAd = new KeyboardFullScreenAd(AdPlacements.INTERSTITIAL_SPRING, "Close");
        adCaffeHelper = new AdCaffeHelper(this, this);
    }

    private long lastBackAdShownTime = 0L;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (lastBackAdShownTime > 0) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBackAdShownTime >= 2_000) {
                    lastBackAdShownTime = 0L;
                } else {
                    return true;
                }
            }

            if (isInputViewShowing) {
                getKeyboardPanelMananger().onBackPressed();
                if (!isInOwnApp()) {
                    if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                        closeFullScreenAd.show();
                    }
                }
            } else {
                boolean adShown = showBackAdIfNeeded();
                if (adShown) {
                    lastBackAdShownTime = System.currentTimeMillis();
                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean showBackAdIfNeeded() {
        if (RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return false;
        }

        boolean enabled = HSConfig.optBoolean(false, "Application", "InterstitialAds", "BackButton", "Show");

        if (!enabled) {
            return false;
        }

        int hours = HSConfig.optInteger(0, "Application", "InterstitialAds", "BackButton", "HoursFromFirstUse");
        if (!KCFeatureControlUtils.isFeatureReleased(this, "BackButton", hours)) {
            return false;
        }

        HSPreferenceHelper prefs = HSPreferenceHelper.create(this, "BackAd");

        long lastBackAdShowTimeMillis = prefs.getLong("LastBackAdShowTime", 0);
        long currentTimeMillis = System.currentTimeMillis();
        long backAdShowCountOfDay = prefs.getLong("BackAdShowCountOfDay", 0);

        if (!DateUtils.isToday(lastBackAdShowTimeMillis)) {
            backAdShowCountOfDay = 0;
            prefs.putLong("BackAdShowCountOfDay", 0);
        }

        float minIntervalByHour = HSConfig.optFloat(0, "Application", "InterstitialAds", "BackButton", "MinIntervalByHour");
        int maxCountPerDay = HSConfig.optInteger(0, "Application", "InterstitialAds", "BackButton", "MaxCountPerDay");

        int minCountPerDay = HSConfig.optInteger(0, "Application", "InterstitialAds", "BackButton", "MinCountPerDay");
        int minCountTriggerHour = HSConfig.optInteger(-1, "Application", "InterstitialAds", "BackButton", "MinCountTriggerHour");
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean isAggressive = false;
        if (minCountPerDay > 0 && minCountTriggerHour >= 0 &&
                backAdShowCountOfDay < minCountPerDay && currentHour >= minCountTriggerHour) {
            isAggressive = true;
        }

        if (isInRightAppForBackAd(isAggressive)) {
            if (currentTimeMillis - lastBackAdShowTimeMillis >= minIntervalByHour * 3600 * 1000 && backAdShowCountOfDay < maxCountPerDay) {
                boolean adShown = KCInterstitialAd.show(AdPlacements.INTERSTITIAL_SPRING, null, null, true);
                if (adShown) {
                    backAdShowCountOfDay++;
                    prefs.putLong("BackAdShowCountOfDay", backAdShowCountOfDay);
                    prefs.putLong("LastBackAdShowTime", currentTimeMillis);
                    return true;
                } else {
                    KCInterstitialAd.load(AdPlacements.INTERSTITIAL_SPRING);
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isInRightAppForBackAd(boolean isAggressive) {
        if (isInOwnApp() || isInputViewShowing) {
            return false;
        }

        if (currentAppPackageName == null) {
            return false;
        }

        List<String> packageNameBlackList = new ArrayList<>();
        packageNameBlackList.add("android");

        if (!isAggressive) {
            List<String> configBlackList = (List<String>) HSConfig.getList("Application", "InterstitialAds", "BackButton", "PackageNameExclude");
            if (configBlackList != null) {
                packageNameBlackList.addAll(configBlackList);
            }
        }

        for (String blockedPackageName : packageNameBlackList) {
            if (currentAppPackageName.contains(blockedPackageName)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInOwnApp() {
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        return editorInfo != null && TextUtils.equals(currentAppPackageName, getPackageName());
    }
//
//    private void requestKeywordListIfConditionSatisfied() {
//        if (System.currentTimeMillis() - HSPreferenceHelper.getDefault().getLong(SEARCH_AD_UPDATE_TIME, 0)
//                < TimeUnit.MINUTES.toMillis(HSConfig.getInteger("Application", "SearchAd", "updateTimeInMin"))) {
//            return;
//        }
//        File tempFile = HSFileUtils.createNewFile(getKeywordFilePathBase() + KEYWORD_TEMP_FILE_NAME + System.currentTimeMillis());
//        File destFile = HSFileUtils.createNewFile(getKeywordFilePathBase() + KEYWORD_FINAL_FILE_NAME);
//        HSHttpConnection connection = new HSHttpConnection(KEYWORD_REQUEST_URL);
//        connection.setDownloadFile(tempFile);
//        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
//            @Override
//            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
//                readKeywordListFromFile(tempFile);
//                HSPreferenceHelper.getDefault().putLong(SEARCH_AD_UPDATE_TIME, System.currentTimeMillis());
//                if (destFile.exists()) {
//                    destFile.delete();
//                }
//                tempFile.renameTo(destFile);
//                tempFile.delete();
//            }
//
//            @Override
//            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
//                tempFile.delete();
//            }
//        });
//        connection.startAsync();
//    }
//
//    private void readKeywordListFromFile(File file) {
//        List<String> tempKeywordList = new ArrayList<>();
//        try {
//            InputStreamReader read = new InputStreamReader(
//                    new FileInputStream(file), "UTF-8");// 考虑到编码格式
//            BufferedReader bufferedReader = new BufferedReader(read);
//            String lineTxt;
//
//            while ((lineTxt = bufferedReader.readLine()) != null) {
//                tempKeywordList.add(lineTxt);
//            }
//            trie = new Trie();
//            for (String keyword : tempKeywordList) {
//                trie.insert(keyword);
//            }
//            bufferedReader.close();
//            read.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String getKeywordFilePathBase() {
//        return HSApplication.getContext().getFilesDir() + File.separator + ASSETS_KEYWORD_FILE_PATH + File.separator;
//    }

    @Override
    public View onCreateInputView() {
        KeyboardAnalyticsReporter.getInstance().recordKeyboardStartTime("CreateAndStartInputView");
        return super.onCreateInputView();
    }

    public void onStartInputInside(CustomSearchEditText editText) {

        /**
         * 标志调用该方法时键盘不是真正的收起。
         */
        super.onFinishInputView(true);
        EditorInfo editorInfo = editText.getEditorInfo();
        insideConnection = editText.onCreateInputConnection(editorInfo);
        HSSpecialCharacterManager.onConnectInnerEditor();
        /**
         * 设置编辑框类型为自定义搜索类型，从而启动对应的键盘布局。
         */
        HSInputMethod.setCustomSearchInputType(editorInfo);
        onStartInputView(editorInfo, true);
    }

    public void onFinishInputInside() {
        if (insideConnection != null) {
            onFinishInput();
            cleanupInternalStateForInsideEditText();
            resetEditInfo();
        }

    }

    private void resetEditInfo() {
        /**
         * Fix - Suggestions failed when we quit from inner editor.
         * Root - Not update input attributes in settings for old editor.
         * Resolution - Let settings update input attributes for old editor.
         */
        onStartInputView(getCurrentInputEditorInfo(), false);
    }

    @Override
    public InputConnection getCurrentInputConnection() {
        if (insideConnection != null) {
            return insideConnection;
        }
        return super.getCurrentInputConnection();
    }

    @Override
    public void onFinishInput() {
        if (insideConnection != null) {
            insideConnection = null;
        }
        super.onFinishInput();
    }

    @Override
    public void onStartInputView(EditorInfo editorInfo, boolean restarting) {
        isInputViewShowing = true;
        Log.e("time log", "time log service onstartInputView started");
        KeyboardAnalyticsReporter.getInstance().recordKeyboardStartTime("StartInputView");
        super.onStartInputView(editorInfo, restarting);
        getKeyboardPanelMananger().beforeStartInputView();

        if (insideConnection == null && restarting) {
            getKeyboardPanelMananger().showKeyboardWithMenu();
        }
        Log.e("time log", "time log service onstartInputView finished");

        KeyboardAnalyticsReporter.getInstance().onKeyboardSessionStart();
        KeyboardAnalyticsReporter.getInstance().recordKeyboardEndTime();

        if (!restarting) {
            if (!isInOwnApp()) {
                if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                    openFullScreenAd.show();
                    openFullScreenAd.preLoad();
                }

                if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
                    closeFullScreenAd.preLoad();
                }
            }
        }

    }


    @Override
    public void onComputeInsets(Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (getKeyboardPanelMananger().isAdjustKeyboardHeightViewShow()) {
            outInsets.visibleTopInsets = 0;
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE;
            outInsets.touchableRegion.setEmpty();
        }
    }

    @Override
    public void onFinishInputView(final boolean finishingInput) {
        isInputViewShowing = false;
        if (WebContentSearchManager.ControlStripState.PANEL_CONTROL != WebContentSearchManager.stripState) {
            HSGlobalNotificationCenter.sendNotificationOnMainThread(WebContentSearchManager.SHOW_CONTROL_PANEL_STRIP_VIEW);
        }

        if (insideConnection != null) {
            insideConnection = null;
        }
        HSEmojiSuggestionManager.cleanupFollowEmojiForTypedWords();

        KeyboardAnalyticsReporter.getInstance().onKeyboardSessionEnd();
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onDestroy() {
        WebContentSearchManager.getInstance().storeHistory();

        unregisterReceiver(this.receiver);
        super.onDestroy();
        getKeyboardPanelMananger().onInputViewDestroy();
        HSGlobalNotificationCenter.removeObserver(keyboardNotificationObserver);
    }

    @Override
    public void loadKeyboard() {
        LanguageDao.updateCurrentLanguage();
        super.loadKeyboard();
    }

    @Override
    public void setInputView(View view) {
        super.setInputView(view);

        WebContentSearchManager.stripState = WebContentSearchManager.ControlStripState.PANEL_CONTROL;
    }

    @Override
    public void hideWindow() {
        super.hideWindow();
        getKeyboardPanelMananger().resetKeyboardBarState();
        HSLog.e("keyboard lifecycle ----hide window----");
    }

    @Override
    public void showWindow(boolean showInput) {
        super.showWindow(showInput);
    }

    @Override
    public void onStartInput(EditorInfo editorInfo, boolean restarting) {
        super.onStartInput(editorInfo, restarting);
        if (restarting) {
            getKeyboardPanelMananger().resetKeyboardBarState();
        }

        adCaffeHelper.requestKeywordListIfConditionSatisfied(editorInfo.packageName);
        // 这里单独记了packageName，而没有通过getCurrentInputEditorInfo()方法
        // 因为这个方法在键盘出来后，一直返回的是键盘曾经出现过的那个App，而这里的editorInfo则对应实际进入的App
        currentAppPackageName = editorInfo.packageName;
        isAppSupportSticker = true;
        AppSuggestionManager.getInstance().addNewRecentApp(currentAppPackageName);
        AppSuggestionManager.getInstance().setCurrentTopAppName(currentAppPackageName);
//        if (!restarting) {
//            isAppSupportSticker = StickerUtils.isEditTextSupportSticker(currentAppPackageName);
//        }
    }

    @Override
    public void onKeyboardWindowShow() {
        super.onKeyboardWindowShow();

        if (!RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            if (!adCaffeHelper.shouldShowSearchAdForCurrentApp(currentAppPackageName)
                    && !TextUtils.equals(currentAppPackageName, HSApplication.getContext().getPackageName())) {
                getKeyboardPanelMananger().showBannerAdBar();
            }
        }

        handler.removeCallbacks(showUpdateAlertRunnable);
        handler.postDelayed(showUpdateAlertRunnable, 1000);

        // Stop clearing image loader cache
        handler.removeCallbacks(clearImageLoaderCacheRunnable);
    }


    @Override
    public void onKeyboardWindowHide() {
        // Start clearing image loader cache
        handler.postDelayed(clearImageLoaderCacheRunnable, HSApplication.isDebugging ? 5 * 1000 : 5 * 60 * 1000);
        HSFloatWindowManager.getInstance().removeFloatingWindow();
        isAppSupportSticker = false;
        HSFloatWindowManager.getInstance().removeGameTipView();
    }

    private boolean shouldShowGoogleAD() {
        return TextUtils.equals(currentAppPackageName, GOOGLE_PLAY_PACKAGE_NAME) || TextUtils.equals(currentAppPackageName, GOOGLE_SEARCH_BAR_PACKAGE_NAME);
    }

    private List<String> splitIntoWords(CharSequence sentence) {
        List<String> words = new ArrayList<>();
        if (!TextUtils.isEmpty(sentence)) {
            Matcher matcher = Pattern.compile("\\w+").matcher(sentence);
            while (matcher.find()) {
                words.add(matcher.group());
            }
        }
        return words;
    }

    private static Runnable clearImageLoaderCacheRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                ImageLoader.getInstance().clearMemoryCache();
                System.gc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static Runnable showUpdateAlertRunnable = new Runnable() {
        @Override
        public void run() {
            ApkUtils.checkAndShowUpdateAlert();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCodeInput(int codePoint, int x, int y, boolean isKeyRepeat) {
        try {
            if (codePoint == '\n' && currentAppPackageName.equals(GOOGLE_PLAY_PACKAGE_NAME)) {
                StringBuilder sb = new StringBuilder();
                sb.append(this.getCurrentInputConnection().getTextBeforeCursor(100, 0));
                sb.append(this.getCurrentInputConnection().getTextAfterCursor(100, 0));
                String text = sb.toString();
                HSLog.i("Key enter pressed in google play.");
                HSLog.i("CodeInput:" + text);
                KCAnalytics.logEvent("keyboard_googleplay_search_content", "codeInput", text);
            }
        } catch (Exception e) {
            HSLog.i("Failed to log key enter in google play.");
        }

        super.onCodeInput(codePoint, x, y, isKeyRepeat);
        checkKeywordAndLoad(codePoint);
    }

    private void checkKeywordAndLoad(int codePoint) {
        if (!adCaffeHelper.shouldShowSearchAdForCurrentApp(currentAppPackageName)) {
            return;
        }
        if (codePoint == CODE_DELETE || //delete key
                (codePoint > 0 && (Character.isLetter(codePoint) || Character.isDigit(codePoint)))) {
            adCaffeHelper.checkKeywordAndLoad(splitIntoWords(getInputLogic().mConnection.getAllText()), success -> {
                if (!success) {
                    getKeyboardPanelMananger().hideCustomBar();
                } else {
                    KCAnalytics.logEvent("searchads_request", "appName", currentAppPackageName);
                }
            });
        }
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();

        if (getKeyboardPanelMananger().isAdjustKeyboardHeightViewShow()) {
            AdjustHeightView.logLastKeyboardHeight();
            getKeyboardPanelMananger().hideAdjustKeyboardHeightView();
        }
    }

    @Override
    protected void showStickerSuggestionByName(String stickerTag, ArrayList<String> stickerNameByString) {
        List<Sticker> stickerList = new ArrayList<>();
        if (!TextUtils.isEmpty(stickerTag) && Character.isLetter(stickerTag.codePointAt(stickerTag.length() - 1))) {
            if (stickerNameByString == null || stickerNameByString.size() <= 0) {
                HSFloatWindowManager.getInstance().removeFloatingWindow();
            }
        }

        if (stickerNameByString != null && stickerNameByString.size() > 0) {
            for (String stickerName : stickerNameByString) {
                if (StickerDataManager.getInstance().isStickerGroupDownloaded(StickerUtils.getGroupNameByStickerName(stickerName))) {
                    Sticker sticker = StickerDataManager.getInstance().getSticker(stickerName);
                    if (sticker != null) {
                        stickerList.add(sticker);
                    }
                }
            }
            getKeyboardPanelMananger().showSuggestedStickers(stickerTag, StickerPrefsUtil.getInstance().sortStickerListByUsedTimes(stickerList));
            KCAnalytics.logEvent("keyboard_sticker_prediction_show", "sticker tag", stickerTag);
        }
    }

    @Override
    public void onNativeAdLoadFail(HSError hsError) {
        HSLog.e("lv_eee AdCaffe Error", hsError.getMessage() + " " + hsError.getCode());
    }

    @Override
    public void onNativeAdLoadSuccess(List<AdCaffeNativeAd> nativeAds, boolean hasMore, int nextOffset) {
        if (!nativeAds.isEmpty()) {
            KCAnalytics.logEvent("searchads_ad_match", "appName", currentAppPackageName);
        }
        getKeyboardPanelMananger().showSearchAdBar(nativeAds, currentAppPackageName);
    }
}
