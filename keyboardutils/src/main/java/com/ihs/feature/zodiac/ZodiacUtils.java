package com.ihs.feature.zodiac;


import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSPreferenceHelper;

import net.appcloudbox.service.AcbHoroscopeData;

/**
 * ZodiacUtils
 * Created by yanxia on 2018/3/02.
 */
@SuppressWarnings("WeakerAccess")
public class ZodiacUtils {

    private static final String ZODIAC_PREFERENCES_NAME = "zodiac_preferences";
    private static HSPreferenceHelper preferences = HSPreferenceHelper.create(HSApplication.getContext(), ZODIAC_PREFERENCES_NAME);
    private static final String PREFERENCE_KEY_ZODIAC_INDEX_NUMBER = "zodiac_index_number";
    public static final int ZODIAC_NONE = -1;

    /**
     * @param zodiacIndex should between [0, AcbHoroscopeData.HoroscopeType.values().length -1]
     */
    public static void setZodiacIndex(int zodiacIndex) {
        preferences.putInt(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER, zodiacIndex);
    }

    public static int getCurrentSelectZodiacIndex() {
        return preferences.getInt(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER, AcbHoroscopeData.HoroscopeType.ARIES.getIndex());
    }

    public static AcbHoroscopeData.HoroscopeType getSelectZodiac() {
        int i = preferences.getInt(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER, ZODIAC_NONE);
        return AcbHoroscopeData.HoroscopeType.valueOf(i);
    }

    public static boolean hasSelectedZodiac() {
        return preferences.contains(PREFERENCE_KEY_ZODIAC_INDEX_NUMBER);
    }
}
