package com.artw.lockscreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ihs.keyboardutils.R;

/**
 * Created by yanxia on 2017/12/14.
 */

public class BrowserActivity extends AppCompatActivity {
    public static final String SEARCH_URL_EXTRA = "search_url";
    private static final String DEFAULT_SEARCH_URL = "http://www.google.com/";
    private WebView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (!LockerUtils.isKeyguardSecure(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
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
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return true; // then it is not handled by default action
            }
        });
        webView.loadUrl(url);
    }
}
