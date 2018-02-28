package com.ihs.inputmethod.uimodules;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.acb.adcaffe.nativead.AdCaffeNativeAd;
import com.ihs.app.framework.HSApplication;
import com.ihs.chargingscreen.utils.DisplayUtils;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.HSFloatWindowManager;
import com.ihs.inputmethod.api.HSUIInputMethodService;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSDisplayUtils;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.framework.KeyboardSwitcher;
import com.ihs.inputmethod.uimodules.settings.HSNewSettingsPanel;
import com.ihs.inputmethod.uimodules.settings.SettingsButton;
import com.ihs.inputmethod.uimodules.ui.emoticon.HSEmoticonActionBar;
import com.ihs.inputmethod.uimodules.ui.emoticon.HSEmoticonPanel;
import com.ihs.inputmethod.uimodules.ui.sticker.Sticker;
import com.ihs.inputmethod.uimodules.ui.sticker.StickerSuggestionAdapter;
import com.ihs.inputmethod.uimodules.ui.theme.analytics.ThemeAnalyticsReporter;
import com.ihs.inputmethod.uimodules.widget.bannerad.KeyboardBannerAdLayout;
import com.ihs.inputmethod.uimodules.widget.goolgeplayad.CustomBarSearchAdAdapter;
import com.ihs.inputmethod.uimodules.widget.goolgeplayad.CustomizeBarLayout;
import com.ihs.inputmethod.uimodules.widget.videoview.HSMediaView;
import com.ihs.inputmethod.view.KBImageView;
import com.ihs.inputmethod.websearch.WebContentSearchManager;
import com.ihs.keyboardutils.alerts.HSAlertDialog;
import com.ihs.keyboardutils.iap.RemoveAdsManager;
import com.ihs.panelcontainer.KeyboardPanelSwitchContainer;
import com.ihs.panelcontainer.KeyboardPanelSwitcher;
import com.ihs.panelcontainer.panel.KeyboardPanel;
import com.kc.commons.utils.KCCommonUtils;
import com.kc.utils.KCAnalytics;
import com.kc.utils.KCFeatureControlUtils;
import com.keyboard.common.SplashActivity;
import com.keyboard.core.session.KCKeyboardSession;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.view.Surface.ROTATION_0;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class KeyboardPanelManager extends KeyboardPanelSwitcher implements BaseFunctionBar.OnFunctionBarItemClickListener {

    public static final String SHOW_EMOJI_PANEL = "show_emoji_panel";
    private FrameLayout frameLayout;

    public KeyboardPanelManager() {
    }


    private KeyboardPanelSwitchContainer keyboardPanelSwitchContainer;
    private BaseFunctionBar functionBar;
    private AlertDialog loadingDialog;
    private HSMediaView hsBackgroundVideoView;
    private CustomBarSearchAdAdapter searchAdAdapter;
    private RecyclerView searchAdRecyclerView;
    private List<Integer> bannerAdSessionList;
    private List<Map<String, Object>> cameraAdInfoList;
    private Random random = new Random();


    private INotificationObserver notificationObserver = new INotificationObserver() {

        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED.equals(s)) {
                addOrUpdateBackgroundView();
            } else if (HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD.equals(s)) {
                showKeyboardWithMenu();
                functionBar.showNewMarkIfNeed();
//                updateKeyboardHeight();
            } else if (HSInputMethod.HS_NOTIFICATION_FIRST_OPEN_KEYBOARD_TODAY.equals(s)) {
                functionBar.checkNewGame();
            }
        }
    };


    private void addOrUpdateBackgroundView() {
        //set bar layout background
        Context context = HSApplication.getContext();
        if (keyboardPanelSwitchContainer != null) {
            KBImageView barBottomView = keyboardPanelSwitchContainer.getBarBottomView();

            Drawable drawable = HSKeyboardThemeManager.getSuggestionBackgroundDrawable();

            int height = context.getResources().getDimensionPixelOffset(R.dimen.config_suggestions_strip_height);
            int width = HSResourceUtils.getDefaultKeyboardWidth(context.getResources());
            KBImageView imageView = null;
            ViewGroup barViewGroup = keyboardPanelSwitchContainer.getBarViewGroup();
            String TAG_TILED_VIEW = "TAG_TILED_VIEW";
            for (int i = 0; i < barViewGroup.getChildCount(); i++) {
                if (TAG_TILED_VIEW.equals(barViewGroup.getChildAt(i).getTag())) {
                    imageView = (KBImageView) barViewGroup.getChildAt(i);
                    break;
                }
            }
            if (imageView == null) {
                imageView = new KBImageView(context);
                imageView.setTag(TAG_TILED_VIEW);
                barViewGroup.addView(imageView, 0);

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
                layoutParams.gravity = Gravity.CENTER;

                imageView.setLayoutParams(layoutParams);
            }
            imageView.setBackgroundDrawable(drawable);
            barBottomView.setBottomBackgroundDrawable(drawable, width);

        }
    }

    public View onCreateInputView(View keyboardPanelView) {
        onInputViewDestroy();
        bannerAdSessionList = (List<Integer>) HSConfig.getList("Application", "NativeAds", "KeyboardBannerAd", "SessionIndexOfDay");
        keyboardPanelSwitchContainer = new KeyboardPanelSwitchContainer();
        //todo 改为东哥backgroundView
//        keyboardPanelSwitchContainer.setThemeBackground(HSKeyboardThemeManager.getKeyboardBackgroundDrawable());
        ThemeAnalyticsReporter.getInstance().recordThemeUsage(HSKeyboardThemeManager.getCurrentThemeName());
        hsBackgroundVideoView = new HSMediaView(HSApplication.getContext());
        hsBackgroundVideoView.setTag("BackgroundView");
        hsBackgroundVideoView.setSupportSmoothScroll(false);
        hsBackgroundVideoView.init();
        keyboardPanelSwitchContainer.setBackgroundView(hsBackgroundVideoView);
        keyboardPanelSwitchContainer.setWhitePanel(HSNewSettingsPanel.class);

        keyboardPanelSwitchContainer.setWebHistoryView(WebContentSearchManager.getInstance().getWebSearchHistoryView());

        createDefaultFunctionBar();
        setFunctionBar(functionBar);
        addOrUpdateBackgroundView();

        HSGlobalNotificationCenter.addObserver(HSKeyboardThemeManager.HS_NOTIFICATION_THEME_CHANGED, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_SHOW_INPUTMETHOD, notificationObserver);
        HSGlobalNotificationCenter.addObserver(HSInputMethod.HS_NOTIFICATION_FIRST_OPEN_KEYBOARD_TODAY, notificationObserver);

        keyboardPanelSwitchContainer.getPanelViewGroup().getLayoutParams().height = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
        return keyboardPanelSwitchContainer;
    }

    @Override
    public void showKeyboardPanel() {
        keyboardPanelSwitchContainer.setKeyboardPanel(KeyboardPanel.class, KeyboardSwitcher.getInstance().getKeyboardPanelView());

        if (HSPreferenceHelper.getDefault().getBoolean(SHOW_EMOJI_PANEL, false)) {
            Handler handler = new Handler();
            handler.post(this::showEmojiPanel);
        } else {
            keyboardPanelSwitchContainer.showPanel(KeyboardPanel.class);
        }
        HSPreferenceHelper.getDefault().putBoolean(SHOW_EMOJI_PANEL, false);
    }

    private void createDefaultFunctionBar() {
        functionBar = (BaseFunctionBar) LayoutInflater.from(HSApplication.getContext()).inflate(R.layout.base_funtion_bar, null);
    }

    private void setFunctionBar(BaseFunctionBar newFunctionBar) {
        this.functionBar = newFunctionBar;
        if (this.functionBar != null) {
            keyboardPanelSwitchContainer.setBarView(this.functionBar);
            newFunctionBar.setOnFunctionBarClickListener(this);
//            keyboardPanelSwitchContainer.getBarViewGroup().setBackgroundDrawable(KeyboardThemeManager.getCurrentTheme().getSuggestionBackground(new ColorDrawable(Color.TRANSPARENT)));
            final int height = HSApplication.getContext().getResources().getDimensionPixelSize(R.dimen.config_suggestions_strip_height);
            ViewGroup.LayoutParams layoutParams = keyboardPanelSwitchContainer.getBarViewGroup().getLayoutParams();
            layoutParams.height = height;
            keyboardPanelSwitchContainer.getBarViewGroup().setLayoutParams(layoutParams);
        }
    }

    public void onInputViewDestroy() {
        if (keyboardPanelSwitchContainer != null) {
            keyboardPanelSwitchContainer.onDestroy();
            keyboardPanelSwitchContainer = null;
        }

        if (functionBar != null) {
            functionBar.onDestroy();
            functionBar = null;
        }


        HSGlobalNotificationCenter.removeObserver(notificationObserver);
    }

    @Override
    public void onFunctionBarItemClick(View view) {
        if (view.getId() == R.id.func_setting_button) {
            SettingsButton settingsButton = functionBar.getSettingsButton();
            int settingButtonType = settingsButton.getButtonType();
            switch (settingButtonType) {
                case SettingsButton.SettingButtonType.MENU:
                    settingsButton.doFunctionButtonSwitchAnimation();
                    keyboardPanelSwitchContainer.showChildPanel(HSNewSettingsPanel.class, null);
                    functionBar.hideMenuButton(functionBar.getSoftGameButton());
                    functionBar.hideMenuButton(functionBar.getWebSeachButton());
                    KCAnalytics.logEvent("keyboard_function_button_click");
                    break;

                case SettingsButton.SettingButtonType.SETTING:
                    settingsButton.doFunctionButtonSwitchAnimation();
                    keyboardPanelSwitchContainer.backToParentPanel(false);
                    functionBar.showMenuButton(functionBar.getSoftGameButton());
                    functionBar.showMenuButton(functionBar.getWebSeachButton());
                    break;

                case SettingsButton.SettingButtonType.BACK:
                    keyboardPanelSwitchContainer.backToParentPanel(false);
                    if (keyboardPanelSwitchContainer.getCurrentPanel() == keyboardPanelSwitchContainer.getKeyboardPanel()) {
                        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.MENU);
                        functionBar.showMenuButton(functionBar.getSoftGameButton());
                        functionBar.showMenuButton(functionBar.getWebSeachButton());
                    } else {
                        functionBar.setSettingButtonType(SettingsButton.SettingButtonType.SETTING);
                    }
                    break;
            }
        }

        if (view.getId() == R.id.web_search_icon) {
            keyboardPanelSwitchContainer.getKeyboardPanel().switchSuggestionState(KeyboardPanel.SUGGESTION_WEB_HISTORY);
        }

        if (view.getId() == R.id.func_cloth_button) {
            Intent intent = new Intent(HSApplication.getContext(), SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("From", "Keyboard");
            intent.putExtra(SplashActivity.JUMP_TAG, SplashActivity.JUMP_TO_THEME_HOME);
            HSApplication.getContext().startActivity(intent);
            KCAnalytics.logEvent("keyboard_cloth_button_click");
        }

        if (view.getId() == R.id.func_facemoji_button) {
            HSEmoticonActionBar.saveLastPanelName(HSEmoticonActionBar.PANEL_FACEEMOJI);
            keyboardPanelSwitchContainer.showPanelAndKeepSelf(HSEmoticonPanel.class);
            keyboardPanelSwitchContainer.setBarVisibility(GONE);
        }
    }

    public void showKeyboardWithMenu() {
        if (keyboardPanelSwitchContainer != null) {
            showKeyboardPanel();
        }
        if (functionBar != null) {
            functionBar.setSettingButtonType(SettingsButton.SettingButtonType.MENU);
            functionBar.showMenuButton(functionBar.getSoftGameButton());
            functionBar.showMenuButton(functionBar.getWebSeachButton());
        }
    }

    public void showEmojiPanel() {
        HSEmoticonActionBar.saveLastPanelName(HSEmoticonActionBar.PANEL_EMOJI);
        if (keyboardPanelSwitchContainer != null) {
            keyboardPanelSwitchContainer.showPanelAndKeepSelf(HSEmoticonPanel.class);
            keyboardPanelSwitchContainer.setBarVisibility(GONE);
        }
    }

    public void beforeStartInputView() {
        if (hsBackgroundVideoView != null) {
            hsBackgroundVideoView.setHSBackground(HSKeyboardThemeManager.getCurrentThemeBackgroundPath());
        }

        if (KCFeatureControlUtils.isFeatureReleased(HSApplication.getContext(), "feature_clipboard_bar", 1)) {
            if (keyboardPanelSwitchContainer != null) {
                keyboardPanelSwitchContainer.showClipboardBar();
            }
        }
    }

    public void onBackPressed() {
        if (hsBackgroundVideoView != null) {
            hsBackgroundVideoView.stopHSMedia();
        }
    }

    public void onHomePressed() {
        if (hsBackgroundVideoView != null) {
            hsBackgroundVideoView.stopHSMedia();
        }
    }

    public void resetKeyboardBarState() {
        if (keyboardPanelSwitchContainer != null) {
            if (keyboardPanelSwitchContainer.getKeyboardPanel() == null) {
                keyboardPanelSwitchContainer.setKeyboardPanel(KeyboardPanel.class, KeyboardSwitcher.getInstance().getKeyboardPanelView());
            }
            keyboardPanelSwitchContainer.getKeyboardPanel().switchSuggestionState(0);
            keyboardPanelSwitchContainer.getBarViewGroup().setVisibility(View.VISIBLE);
        }
    }

    public void showCustomBar() {
        if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null) {
            keyboardPanelSwitchContainer.getCustomizeBar().setVisibility(VISIBLE);
        }
    }

    public void hideCustomBar() {
        if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null) {
            keyboardPanelSwitchContainer.getCustomizeBar().setVisibility(GONE);
        }
    }

    public void showSearchAdBar(List<AdCaffeNativeAd> nativeAds, String currentAppPackageName) {
        if (keyboardPanelSwitchContainer == null || RemoveAdsManager.getInstance().isRemoveAdsPurchased()) {
            return;
        }

        if (nativeAds.isEmpty()) {
            hideCustomBar();
            return;
        }

        showCustomBar();
        for (AdCaffeNativeAd nativeAd : nativeAds) {
            nativeAd.handleImpression();
            KCAnalytics.logEvent("searchads_impression", "appName", currentAppPackageName);
        }


        if (searchAdAdapter == null) {
            searchAdAdapter = new CustomBarSearchAdAdapter();
            searchAdAdapter.setAdCaffeOnClickListener(new CustomBarSearchAdAdapter.AdCaffeOnClickListener() {
                @Override
                public void onClick(AdCaffeNativeAd adCaffeNativeAd) {
                    showLoadingAlert();
                    KCAnalytics.logEvent("searchads_click", "appName", currentAppPackageName);
                }

                @Override
                public void onHandleClickFinish(AdCaffeNativeAd adCaffeNativeAd) {
                    hideLoadingAlert();
                }
            });
        }

        if (searchAdRecyclerView == null) {
            searchAdRecyclerView = new RecyclerView(HSApplication.getContext());
            searchAdRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            searchAdRecyclerView.setBackgroundColor(Color.parseColor("#f6f6f6"));
            int padding = DisplayUtils.dip2px(5);
            searchAdRecyclerView.setPadding(padding, 0, padding, 0);
            searchAdRecyclerView.setAdapter(searchAdAdapter);
            LinearLayoutManager layoutManager = new LinearLayoutManager(HSApplication.getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            searchAdRecyclerView.setLayoutManager(layoutManager);
            searchAdRecyclerView.setHasFixedSize(true);
        }
        searchAdAdapter.setAdList(nativeAds);

        CustomizeBarLayout customizeBarLayout = new CustomizeBarLayout(HSApplication.getContext(), this::hideCustomBar);
        customizeBarLayout.setContent(searchAdRecyclerView);
        if (HSDisplayUtils.getRotation(HSApplication.getContext()) == ROTATION_0) {
            if (keyboardPanelSwitchContainer != null) {
                keyboardPanelSwitchContainer.getCustomizeBar().removeAllViews();
                keyboardPanelSwitchContainer.setCustomizeBar(customizeBarLayout);
            }
        }
    }

    private void hideLoadingAlert() {
        KCCommonUtils.dismissDialog(loadingDialog);
    }

    private void showLoadingAlert() {
        if (loadingDialog == null) {
            loadingDialog = HSAlertDialog.build(HSApplication.getContext(), 0).
                    setView(R.layout.layout_dialog_loading).
                    setCancelable(true).create();
        }
        KCCommonUtils.showDialog(loadingDialog);
    }

    public void logCustomizeBarShowed() {
        if (keyboardPanelSwitchContainer != null && keyboardPanelSwitchContainer.getCustomizeBar() != null && keyboardPanelSwitchContainer.getCustomizeBar().getVisibility() == VISIBLE) {
            KCAnalytics.logEvent("keyboard_toolBar_show", "where", "GooglePlay_Search");
        }
    }

    public void showBannerAdBar() {
        if (keyboardPanelSwitchContainer == null) {
            return;
        }
        keyboardPanelSwitchContainer.getCustomizeBar().removeAllViews();

        if (bannerAdSessionList == null || bannerAdSessionList.size() < 1) {
            return;
        }

        boolean show = HSConfig.optBoolean(false, "Application", "NativeAds", "KeyboardBannerAd", "Show");
        int hoursDelay = HSConfig.optInteger(0, "Application", "NativeAds", "KeyboardBannerAd", "HoursFromFirstUse");

        if (!KCFeatureControlUtils.isFeatureReleased(HSApplication.getContext(), "KeyboardBannerAd", hoursDelay)) {
            return;
        }

        if (!show || !bannerAdSessionList.contains((int) KCKeyboardSession.getCurrentSessionIndexOfDay())) {
            HSLog.e("cannt show banner ad");
            return;
        }
        keyboardPanelSwitchContainer.getCustomizeBar().removeAllViews();
        keyboardPanelSwitchContainer.setCustomizeBar(new KeyboardBannerAdLayout(HSApplication.getContext()));
    }


    public void showSuggestedStickers(String stickerTag, List<Sticker> stickerList) {
        if (stickerList.size() > 0) {
            StickerSuggestionAdapter stickerSuggestionAdapter;
            View stickerSuggestionView = View.inflate(HSApplication.getContext(), R.layout.view_sticker_suggestion, null);
            RecyclerView recyclerView = stickerSuggestionView.findViewById(R.id.rv_sticker);
            LinearLayoutManager linearLayoutManager
                    = new LinearLayoutManager(HSApplication.getContext(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            stickerSuggestionAdapter = new StickerSuggestionAdapter(stickerList);
            recyclerView.setAdapter(stickerSuggestionAdapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    HSFloatWindowManager.getInstance().refreshStickerWindowTimer();
                }
            });
            stickerSuggestionAdapter.setStickerTag(stickerTag);
            HSFloatWindowManager.getInstance().showStickerSuggestionWindow(stickerSuggestionView,
                    (int) keyboardPanelSwitchContainer.findViewById(R.id.container_group_wrapper).getY(), stickerList.size());
        } else {
            HSFloatWindowManager.getInstance().removeFloatingWindow();
        }
    }

    public void showAdjustKeyboardHeightView() {
        if (keyboardPanelSwitchContainer != null) {
            View adjustKeyboardHeightView = View.inflate(HSApplication.getContext(), R.layout.item_panel_keyboard_height, null);

            frameLayout = new FrameLayout(HSApplication.getContext());
            frameLayout.addView(adjustKeyboardHeightView);

            keyboardPanelSwitchContainer.addView(frameLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public void hideAdjustKeyboardHeightView() {
        if (keyboardPanelSwitchContainer != null) {
            if (frameLayout != null) {
                keyboardPanelSwitchContainer.removeView(frameLayout);
                frameLayout = null;
            }
        }
    }

    public void updateKeyboardHeight() {
        if (hsBackgroundVideoView != null) {
            ViewGroup.LayoutParams layoutParams = hsBackgroundVideoView.getLayoutParams();
            layoutParams.height = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources()) + HSResourceUtils.getDefaultSuggestionStripHeight(HSApplication.getContext().getResources());

            ViewGroup panelViewGroup = keyboardPanelSwitchContainer.getPanelViewGroup();
            ViewGroup.LayoutParams layoutParams1 = panelViewGroup.getLayoutParams();
            layoutParams1.height = HSResourceUtils.getDefaultKeyboardHeight(HSApplication.getContext().getResources());
            panelViewGroup.requestLayout();

            HSUIInputMethodService.getInstance().loadKeyboard();
        }
    }
}
