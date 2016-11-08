package com.ihs.keyboardutils.panelcontainer;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Arthur on 16/11/8.
 */

public class ViewUtils {
    public static void removeViewFromParent(View view) {
        try {
            ((ViewGroup) view.getParent()).removeView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
