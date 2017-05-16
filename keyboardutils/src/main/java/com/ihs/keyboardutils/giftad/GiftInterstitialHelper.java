package com.ihs.keyboardutils.giftad;

import android.app.Activity;
import android.content.Intent;

import com.ihs.app.framework.HSApplication;

/**
 * Created by yanxia on 2017/5/15.
 */

public class GiftInterstitialHelper {

    public static void showInterstitialGiftAd(String placement) {
        showInterstitialGiftAd(null, placement);
    }

    public static void showInterstitialGiftAd(Activity activity, String placement) {
        Intent intent;
        if (null == activity) {
            intent = new Intent(HSApplication.getContext(), InterstitialGiftActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(InterstitialGiftActivity.PLACEMENT_MESSAGE, placement);
            HSApplication.getContext().startActivity(intent);
        } else {
            intent = new Intent(activity, InterstitialGiftActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra(InterstitialGiftActivity.PLACEMENT_MESSAGE, placement);
            activity.startActivity(intent);
        }
    }
}
