package com.artw.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;

/**
 * Created by yanxia on 2017/12/14.
 */

public class BrowserActivity extends AppCompatActivity {
    public static final String SHOW_WHEN_LOCKED = "show_when_locked";
    public static final String SEARCH_URL_EXTRA = "search_url";
    private static final String DEFAULT_SEARCH_URL = "http://www.google.com/";
    private WebView webView;
    private ProgressBar progressBar;
    private boolean progressDialogShowed = false;
    private boolean isFromLockScreen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        boolean showWhenLocked = getIntent().getBooleanExtra(SHOW_WHEN_LOCKED, false);
        if (showWhenLocked) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            isFromLockScreen = true;
            registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        }
        setContentView(R.layout.activity_browser);
        Intent intent = getIntent();
        String url = DEFAULT_SEARCH_URL;
        if (intent.hasExtra(SEARCH_URL_EXTRA)) {
            url = intent.getStringExtra(SEARCH_URL_EXTRA);
        }
        webView = findViewById(R.id.browser_web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        progressBar = findViewById(R.id.browser_progress_bar);

        webView.setWebViewClient(new WebViewClient() {
            /**
             * Notify the host application that a page has started loading. This method
             * is called once for each main frame load so a page with iframes or
             * framesets will call onPageStarted one time for the main frame. This also
             * means that onPageStarted will not be called when the contents of an
             * embedded frame changes, i.e. clicking a link whose target is an iframe,
             * it will also not be called for fragment navigations (navigations to
             * #fragment_id).
             *
             * @param view    The WebView that is initiating the callback.
             * @param url     The url to be loaded.
             * @param favicon The favicon for this page if it already exists in the
             */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                HSLog.d("BrowserActivity onPageStarted");
                super.onPageStarted(view, url, favicon);
                if (progressBar.getVisibility() != View.VISIBLE && !progressDialogShowed) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressDialogShowed = true;
                }
            }

            /**
             * Notify the host application that a page has finished loading. This method
             * is called only for main frame. When onPageFinished() is called, the
             * rendering picture may not be updated yet. To get the notification for the
             * new Picture, use {@link WebView.PictureListener#onNewPicture}.
             *
             * @param view The WebView that is initiating the callback.
             * @param url  The url of the page.
             */
            @Override
            public void onPageFinished(WebView view, String url) {
                HSLog.d("BrowserActivity onPageFinished");
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

//            /**
//             * Give the host application a chance to take over the control when a new
//             * url is about to be loaded in the current WebView. If WebViewClient is not
//             * provided, by default WebView will ask Activity Manager to choose the
//             * proper handler for the url. If WebViewClient is provided, return true
//             * means the host application handles the url, while return false means the
//             * current WebView handles the url.
//             * This method is not called for requests using the POST "method".
//             *
//             * @param view The WebView that is initiating the callback.
//             * @param url  The url to be loaded.
//             * @return True if the host application wants to leave the current WebView
//             * and handle the url itself, otherwise return false.
//             * @deprecated Use {@link #shouldOverrideUrlLoading(WebView, WebResourceRequest)
//             * shouldOverrideUrlLoading(WebView, WebResourceRequest)} instead.
//             */
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                HSLog.d("BrowserActivity shouldOverrideUrlLoading");
//                view.loadUrl(url);
//                return true; // then it is not handled by default action
//            }
        });
        webView.loadUrl(url);
    }

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFromLockScreen) {
            unregisterReceiver(screenOffReceiver);
        }
    }
}
