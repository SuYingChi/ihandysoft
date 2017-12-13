package com.artw.lockscreen;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.artw.lockscreen.common.LockerChargingScreenUtils;
import com.ihs.commons.utils.HSLog;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.view.SearchEditTextView;

/**
 * Created by yanxia on 2017/12/12.
 */

public class PremiumLockerSearchActivity extends AppCompatActivity {

    private SearchEditTextView searchEditTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (!LockerChargingScreenUtils.isNativeLollipop()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        if (!LockerUtils.isKeyguardSecure(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
        setContentView(R.layout.activity_premium_locker_search);
        searchEditTextView = findViewById(R.id.search_view);
        searchEditTextView.setSearchButtonClickListener(new SearchEditTextView.OnSearchButtonClickListener() {
            @Override
            public void onSearchButtonClick(String searchText) {
                HSLog.d(searchText);
            }
        });
    }
}
