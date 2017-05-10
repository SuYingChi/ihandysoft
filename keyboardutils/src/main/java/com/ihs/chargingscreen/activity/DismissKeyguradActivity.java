package com.ihs.chargingscreen.activity;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewTreeObserver;

import com.ihs.app.framework.HSApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.view.Window.FEATURE_NO_TITLE;
import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

/**
 * We want show unlock pattern view automatically, so we create this activity with
 * FLAG_DISMISS_KEYGUARD.
 */
public class DismissKeyguradActivity extends Activity {

    private Handler handler= new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            finish();
            Intent intent1 = new Intent(HSApplication.getContext(), ChargingScreenActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            HSApplication.getContext().startActivity(intent1);
        }
    };
    private ViewTreeObserver.OnPreDrawListener onPreDrawListener;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        requestWindowFeature(FEATURE_NO_TITLE);

        getWindow().addFlags(FLAG_DISMISS_KEYGUARD | FLAG_FULLSCREEN);

        onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
                handler.postDelayed(runnable, isKeyguardLocked(DismissKeyguradActivity.this, false) ? 1000 : 0);
                return true;
            }
        };
        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    }

    public static void startSelfIfKeyguardSecure(Context context) {
        if (isKeyguardLocked(context, false)) {
            Intent intent = new Intent(context, DismissKeyguradActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }else{
            Intent intent1 = new Intent(context, ChargingScreenActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            context.startActivity(intent1);
        }


    }

     static boolean isKeyguardLocked(Context context, boolean defaultValue) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        try {
            Method declaredMethod = KeyguardManager.class.getDeclaredMethod("isKeyguardSecure", new Class[0]);
            declaredMethod.setAccessible(true);
            defaultValue = ((Boolean) declaredMethod.invoke(keyguardManager, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        return defaultValue;
    }

     static boolean isKeyguardSecure(Context context, boolean defaultValue) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        try {
            Method declaredMethod = KeyguardManager.class.getDeclaredMethod("isKeyguardSecure", new Class[0]);
            declaredMethod.setAccessible(true);
            defaultValue = ((Boolean) declaredMethod.invoke(keyguardManager, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        return defaultValue;
    }

}
