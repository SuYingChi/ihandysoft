package com.ihs.feature.zodiac;


import android.support.annotation.NonNull;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.keyboardutils.R;

import net.appcloudbox.service.AcbHoroscopeData;

/**
 * ZodiacUtils
 * Created by yanxia on 2018/3/02.
 */
@SuppressWarnings("WeakerAccess")
public class ZodiacUtils {

    public static final String EXTRA_ZODIAC_INDEX_NUMBER = "zodiac_index_number";

    private static final String ZODIAC_PREFERENCES_NAME = "zodiac_preferences";
    private static HSPreferenceHelper preferences = HSPreferenceHelper.create(HSApplication.getContext(), ZODIAC_PREFERENCES_NAME);
    private static final String PREFERENCE_KEY_ZODIAC_INDEX_NUMBER = "zodiac_index_number";
    public static final int ZODIAC_NONE = -1;

    /**
     * @param zodiac 对应星座
     */
    public static void setZodiacIndex(@NonNull AcbHoroscopeData.HoroscopeType zodiac) {
        preferences.putInt(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER, zodiac.getIndex());
    }

    public static int getCurrentSelectZodiacIndex() {
        return preferences.getInt(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER, AcbHoroscopeData.HoroscopeType.ARIES.getIndex());
    }

    public static AcbHoroscopeData.HoroscopeType getSelectZodiac() {
        int i = preferences.getInt(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER, 0);
        return AcbHoroscopeData.HoroscopeType.valueOf(i);
    }

    public static boolean hasSelectedZodiac() {
        return preferences.contains(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER);
    }

    public static String getZodiacName(@NonNull AcbHoroscopeData.HoroscopeType zodiac) {
        String zodiacName;
        switch (zodiac) {
            case AQUARIUS:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_aquarius);
                break;
            case PISCES:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_pisces);
                break;
            case ARIES:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_aries);
                break;
            case TAURUS:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_taurus);
                break;
            case GEMINI:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_gemini);
                break;
            case CANCER:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_cancer);
                break;
            case LEO:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_leo);
                break;
            case VIRGO:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_virgo);
                break;
            case LIBRA:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_libra);
                break;
            case SCORPIO:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_scorpio);
                break;
            case SAGITTARIUS:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_sagittarius);
                break;
            case CAPRICORN:
                zodiacName = HSApplication.getContext().getString(R.string.zodiac_name_capricorn);
                break;
            default:
                zodiacName = "none";
                break;
        }
        return zodiacName;
    }
}
