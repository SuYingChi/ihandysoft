package com.ihs.keyboardutils.giftad;

import android.content.Intent;

import com.ihs.app.framework.HSApplication;

/**
 * Created by yanxia on 2017/5/15.
 */

public class GiftInterstitialHelper {
    public static void showInterstitialGiftAd(String placement) {
        Intent intent = new Intent(HSApplication.getContext(), InterstitialGiftActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(InterstitialGiftActivity.PLACEMENT_MESSAGE, placement);
        HSApplication.getContext().startActivity(intent);
    }
}
