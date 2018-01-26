package com.artw.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;

import com.ihs.app.framework.HSApplication;

/**
 * Created by Arthur on 2018/1/26.
 */

public class SearchIntentReceiver extends BroadcastReceiver {
    public static final String SEARCH_URL_EXTRA = "search_url";
    public static final String SEARCH_INTENT_ACTION = "com.keyboard.search";
    private static final String DEFAULT_SEARCH_URL = "http://www.google.com/";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SEARCH_INTENT_ACTION.equals(intent.getAction())) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            customTabsIntent.launchUrl(context, Uri.parse(getSearchUrl(intent)));
        }
    }

    private String getSearchUrl(@NonNull Intent intent) {
        String url = DEFAULT_SEARCH_URL;
        if (intent.hasExtra(SEARCH_URL_EXTRA)) {
            url = intent.getStringExtra(SEARCH_URL_EXTRA);
        }
        return url;
    }

    public static void sendSearchIntent(String searchUrl) {
        Intent intent = new Intent(SearchIntentReceiver.SEARCH_INTENT_ACTION);
        intent.putExtra(SearchIntentReceiver.SEARCH_URL_EXTRA, searchUrl);
        HSApplication.getContext().sendBroadcast(intent);
    }
}
