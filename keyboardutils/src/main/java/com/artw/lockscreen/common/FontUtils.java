package com.artw.lockscreen.common;

import android.graphics.Typeface;
import android.util.SparseArray;

import com.ihs.keyboardutils.R;
import com.ihs.app.framework.HSApplication;

/**
 * Utility for text fonts.
 */
public class FontUtils {

    public enum Font {
        ROBOTO_LIGHT(0),
        ROBOTO_REGULAR(1),
        ROBOTO_MEDIUM(2),
        ROBOTO_THIN(3),
        ROBOTO_CONDENSED(4),
        DS_DIGIB(5),
        AKROBAT_LIGHT(6);

        private int mValue;

        Font(int value) {
            mValue = value;
        }

        int getResId() {
            switch (mValue) {
                case 0:
                    return R.string.roboto_light;
                case 1:
                    return R.string.roboto_regular;
                case 2:
                    return R.string.roboto_medium;
                case 3:
                    return R.string.roboto_thin;
                case 4:
                    return R.string.roboto_condensed;
                case 5:
                    return R.string.ds_digib;
                case 6:
                    return R.string.akrobat_light;
            }
            return R.string.roboto_regular;
        }

        public static Font ofFontResId(int resId) {
            if (resId == R.string.roboto_light) {
                return ROBOTO_LIGHT;
            } else if (resId == R.string.roboto_regular) {
                return ROBOTO_REGULAR;
            } else if (resId == R.string.roboto_medium) {
                return ROBOTO_MEDIUM;
            } else if (resId == R.string.roboto_thin) {
                return ROBOTO_THIN;
            } else if (resId == R.string.roboto_condensed) {
                return ROBOTO_CONDENSED;
            } else if (resId == R.string.ds_digib) {
                return DS_DIGIB;
            } else if (resId == R.string.akrobat_light) {
                return AKROBAT_LIGHT;
            }
            return null;
        }
    }

    private static SparseArray<Typeface> sFontCache = new SparseArray<>(5);

    public static Typeface getTypeface(Font font) {
        return getTypeface(font, Typeface.NORMAL);
    }

    public static Typeface getTypeface(Font font, int style) {
        if (font != null) {
            int fontResId = font.getResId();
            Typeface typeface = sFontCache.get(fontResId);
            if (fontResId == R.string.ds_digib || fontResId == R.string.akrobat_light) {
                if (typeface != null) {
                    return typeface;
                }
                typeface = Typeface.createFromAsset(HSApplication.getContext().getAssets(),
                        "fonts/" + HSApplication.getContext().getString(fontResId) + ".ttf");
                sFontCache.put(fontResId, typeface);
            } else {
                // Already cached by framework.
                typeface = Typeface.create(HSApplication.getContext().getString(fontResId), style);
            }
            return typeface;
        }
        return null;
    }
}
