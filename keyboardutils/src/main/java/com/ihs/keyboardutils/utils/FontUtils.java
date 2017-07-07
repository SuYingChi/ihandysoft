package com.ihs.keyboardutils.utils;

import android.graphics.Typeface;

import com.ihs.app.framework.HSApplication;
import com.ihs.keyboardutils.R;

import java.util.HashMap;



/**
 * Utility for text fonts.
 */
public class FontUtils {

    public static class Font {
        String font;
        private boolean isCustom = true;
        public Font(String font) {
            this.font = font;
            if (HSApplication.getContext().getString(R.string.roboto_light).equals(font)
                    ||HSApplication.getContext().getString(R.string.roboto_regular).equals(font)
                    ||HSApplication.getContext().getString(R.string.roboto_medium).equals(font)
                    ||HSApplication.getContext().getString(R.string.roboto_thin).equals(font)
                    ||HSApplication.getContext().getString(R.string.roboto_condensed).equals(font)) {
                isCustom = false;
            }
        }
        public boolean isCustom(){
            return isCustom;
        }

        public String getFont() {
            return font;
        }
    }

    private static HashMap<String,Typeface> sFontCache = new HashMap<>();

    public static Typeface getTypeface(Font font) {
        return getTypeface(font, Typeface.NORMAL);
    }

    public static Typeface getTypeface(Font font, int style) {
        if (font != null) {
            String fontStr = font.getFont();
            Typeface typeface;
            if (font.isCustom()){
                typeface  = sFontCache.get(fontStr);
                if (typeface != null) {
                    return typeface;
                }
                typeface = Typeface.createFromAsset(HSApplication.getContext().getAssets(),
                        "fonts/" + fontStr);
                sFontCache.put(fontStr, typeface);
            }else {
                // Already cached by framework.
                typeface = Typeface.create(fontStr, style);
            }
            return typeface;
        }
        return null;
    }

}
