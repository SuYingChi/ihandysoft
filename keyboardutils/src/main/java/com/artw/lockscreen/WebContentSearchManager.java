package com.artw.lockscreen;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.countrycode.HSCountryCodeManager;
import com.ihs.commons.utils.HSLog;

/**
 * Copied from lib_keyboard.
 */
public class WebContentSearchManager {

    private static WebContentSearchManager instance;
    private String googleAdId = "unknown";

    private WebContentSearchManager() {
        new LoadGoogleIdTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static WebContentSearchManager getInstance() {
        if (null != instance) {
            return instance;
        }
        synchronized (WebContentSearchManager.class) {
            if (null == instance) {
                instance = new WebContentSearchManager();
            }
            return instance;
        }
    }

    private class LoadGoogleIdTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String id = "unknown";
            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(HSApplication.getContext());
                id = adInfo.getId();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return id;
        }

        @Override
        protected void onPostExecute(String id) {
            googleAdId = id;
        }
    }

    public String queryText(String content) {
        //http://trends.mobitech-search.xyz/v1/search/HNDYSFT34SP3?user_id={GOOGLE_AD_ID}&c={2_DIGIT_COUNTRY_CODE}&keywords={QUERY_WORDS}
        String queryTextPrefix = HSConfig.optString("", "Application", "SearchEngine", "url");
        if (TextUtils.isEmpty(queryTextPrefix)) {
            queryTextPrefix = "http://trends.mobitech-search.xyz/v1/search/HNDYSFT34SP3?user_id=%s&c=%s&keywords=%s";
        }

        String query = String.format(queryTextPrefix,
                googleAdId,
                HSCountryCodeManager.getInstance().getCountryCode(),
                content);
        HSLog.d("search url: ", query);
        return query;
    }
}
