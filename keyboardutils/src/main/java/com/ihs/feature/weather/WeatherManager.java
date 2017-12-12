package com.ihs.feature.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateUtils;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.location.HSLocationManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;
import com.ihs.feature.common.WeatherSettings;
import com.ihs.keyboardutils.R;
import com.ihs.weather.CurrentCondition;
import com.ihs.weather.HSWeatherQuery;
import com.ihs.weather.HSWeatherQueryListener;
import com.ihs.weather.HSWeatherQueryResult;
import com.ihs.weather.HourlyForecast;

import java.util.Calendar;
import java.util.List;

/**
 * Created by guonan.lv on 17/12/12.
 */

public class WeatherManager {

    public enum WeatherStatus {
        INIT,
        UPDATING,
        FAILED,
        SUCCEEDED
    }

    public static final String LOCKER_PACKAGE_NAME = "com.fasttrack.lockscreen";
    public static final String CHARGING_PACKAGE_NAME = "com.fasttrack.lock";
    public static final String LAUNCHER_THEME_PACKAGE_NAME = "com.honeycomb.launcher";

    public static final String RES_NAME_MINIMUM_LOCKER_VERSION_CODE = "minimum_lock_screen_version_code";
    public static final String RES_NAME_MINIMUM_CHARGING_VERSION_CODE = "minimum_speed_charging_version_code";

    public static final int THEME_REPORT_EVENT_UNLOCK = 0;
    public static final int THEME_REPORT_EVENT_LONG_CLICK = 1;

    public static final String ACTION_CUSTOMIZE_SERVICE = "action.customize.service";
    public static final String ACTION_WEATHER_REQUEST = "com.fasttrack.lockscreen.WEATHER_REQUEST";
    public static final String ACTION_WEATHER_CHANGE = "com.fasttrack.lockscreen.WEATHER_CHANGE";

    public static final String BUNDLE_KEY_WEATHER_TEMPERATURE_INT = "temperature_int";
    public static final String BUNDLE_KEY_WEATHER_TEMPERATURE_FORMAT = "temperature_format";
    public static final String BUNDLE_KEY_WEATHER_ICON_NAME = "weather_icon_name";
    public static final String BUNDLE_KEY_WEATHER_ICON_ID = "weather_icon_id";
    public static final String BUNDLE_KEY_WEATHER_DESCRIPTION = "weather_description";
    public static final String BUNDLE_KEY_FROM = "from";

    private class LocationListener implements HSLocationManager.HSLocationListener {
        private boolean updateWeather = false;
        private HSLocationManager.LocationSource source;

        LocationListener(HSLocationManager.LocationSource source, boolean isUpdateWeather) {
            updateWeather = isUpdateWeather;
            this.source = source;
        }

        @Override
        public void onLocationFetched(boolean success, HSLocationManager hsLocationManager) {
            if (success) {
                if (updateWeather) {
                    HSWeatherQuery query = new HSWeatherQuery(hsLocationManager.getLocation().getLatitude(),
                            hsLocationManager.getLocation().getLongitude(),
                            weatherQueryListener);
                    query.start();
                    locationCity = hsLocationManager.getCity();
                    HSLog.i(TAG, "success query.start() la == " + hsLocationManager.getLocation().getLatitude() + "  lo == " + hsLocationManager.getLocation().getLongitude()
                            + "  city == " + hsLocationManager.getCity());
                } else {
                    HSLog.i(TAG, "success  source == " + source + "  la == " + hsLocationManager.getLocation().getLatitude() + "  lo == " + hsLocationManager.getLocation().getLongitude());
                }


                lastLocationTime = System.currentTimeMillis();
            } else {
                if (source == HSLocationManager.LocationSource.DEVICE) {
                    HSLog.i(TAG, "failed doFetch ip == " + HSLocationManager.LocationSource.IP);
                    doFetchLocation(HSLocationManager.LocationSource.IP, updateWeather);
                } else {
                    HSLog.w(TAG, "failed onQueryFinished ");
                    weatherQueryListener.onQueryFinished(false, null);
                }
            }
        }

        @Override
        public void onGeographyInfoFetched(boolean success, HSLocationManager hsLocationManager) {
            HSLog.i(TAG, "onGeographyInfoFetched success " + success + "  loc == " + hsLocationManager);
        }
    }

    public static final int WEATHER_TEXT_TEMP_ONLY = 0;
    public static final int WEATHER_TEXT_TEMP_CONDITION = 1;
    public static final int WEATHER_TEXT_CONDITION_TEMP = 2;

    private static final String TAG = WeatherManager.class.getSimpleName();
    public static final int WEATHER_UPDATE_INTERVAL = 3 * 3600; // s
    private static final int LOCATION_UPDATE_INTERVAL = 30 * 60; // s

    private static WeatherManager sInstance;
    private Context context;

    private WeatherStatus status;
    boolean hasRegisteredTimeTick;
    private long lastUpdateTime = Long.MIN_VALUE;
    private long lastLocationTime = Long.MIN_VALUE;
    private String locationCity = "";
    private HSLocationManager locationManager;
    private CurrentCondition currentWeatherCondition;
    private List<HourlyForecast> hourlyForecasts;
    private HSWeatherQueryListener weatherQueryListener;
    private boolean isReceiverRegistered = false;

    public static final String NOTIFICATION_WEATHER_CONDITION_CHANGED = "NOTIFICATION_WEATHER_CONDITION_CHANGED";

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            HSLog.d("weather intent == " + intent);
            if (intent != null && ACTION_WEATHER_REQUEST.equals(intent.getAction())) {
                updateIfNeeded();
            }
        }
    };

    public static WeatherManager getInstance() {
        return sInstance;
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new WeatherManager(context);
        }
    }

    public CurrentCondition getCurrentWeatherCondition() {
        return currentWeatherCondition;
    }

    public List<HourlyForecast> getHourlyForecasts() {
        return hourlyForecasts;
    }

    public String getLocationCity() {
        return currentWeatherCondition.getCityName();
    }

    private WeatherManager(Context context) {
        this.context = context;
        locationManager = new HSLocationManager(context);
        status = WeatherStatus.INIT;
        currentWeatherCondition = null;
        hasRegisteredTimeTick = false;
        weatherQueryListener = new HSWeatherQueryListener() {
            @Override
            public void onQueryFinished(boolean success, HSWeatherQueryResult result) {
                if (success) {
                    currentWeatherCondition = result.getCurrentCondition();
                    hourlyForecasts = result.getHourlyForecasts();
                    lastUpdateTime = System.currentTimeMillis();
                    HSLog.i(TAG, "" + currentWeatherCondition);
                    status = WeatherStatus.SUCCEEDED;
                } else {
                    status = WeatherStatus.FAILED;
                }
                HSGlobalNotificationCenter.sendNotification(NOTIFICATION_WEATHER_CONDITION_CHANGED);
                sendBroadcast();
            }
        };
        doFetchLocation(HSLocationManager.LocationSource.DEVICE, false);

        registerDataReceiver();
    }

    public void registerDataReceiver() {
        if (!isReceiverRegistered) {
            HSLog.d("weather register ");
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_WEATHER_REQUEST);
            HSApplication.getContext().registerReceiver(weatherReceiver, intentFilter);
            isReceiverRegistered = true;
        }
    }

    public void unregisterDataReceiver() {
        if (isReceiverRegistered) {
            HSLog.d("weather unregister ");
            HSApplication.getContext().unregisterReceiver(weatherReceiver);
            isReceiverRegistered = false;
        }
    }

    private void sendBroadcast() {
        if (currentWeatherCondition != null && currentWeatherCondition.getCondition() != HSWeatherQueryResult.Condition.UNKNOWN) {
            Intent intent = new Intent(ACTION_WEATHER_CHANGE);

            if (!WeatherSettings.shouldDisplayFahrenheit()) {
//                currentWeatherCondition.getCelsius() + "°C";
                intent.putExtra(BUNDLE_KEY_WEATHER_TEMPERATURE_INT, currentWeatherCondition.getCelsius());
                intent.putExtra(BUNDLE_KEY_WEATHER_TEMPERATURE_FORMAT, "%1s°C");
            } else {
                intent.putExtra(BUNDLE_KEY_WEATHER_TEMPERATURE_INT, currentWeatherCondition.getFahrenheit());
                intent.putExtra(BUNDLE_KEY_WEATHER_TEMPERATURE_FORMAT, "%1s°F");
            }

            intent.putExtra(BUNDLE_KEY_WEATHER_ICON_NAME, getWeatherConditionIconName());
            intent.putExtra(BUNDLE_KEY_WEATHER_ICON_ID, getWeatherConditionIconResourceID());
            intent.putExtra(BUNDLE_KEY_WEATHER_DESCRIPTION, getLocalSimpleConditionDescription());

            context.sendBroadcast(intent);

            HSLog.i("weather intent == " + intent);
        }
    }

    public void updateWeather() {
        if (status == WeatherStatus.UPDATING) {
            HSLog.w("status == WeatherStatus.UPDATING return");
            return;
        }
        status = WeatherStatus.UPDATING;

        HSLog.i("time == " + (System.currentTimeMillis() - lastLocationTime) + "  loc == " + locationManager.getLocation()
                + "  city == " + locationManager.getCity());
        if ((System.currentTimeMillis() - lastLocationTime) < HSConfig.optInteger(LOCATION_UPDATE_INTERVAL, "Application", "Weather", "LocationUpdateInterval")
                && null != locationManager.getLocation()
                && (null == currentWeatherCondition || currentWeatherCondition.getCondition() == HSWeatherQueryResult.Condition.UNKNOWN)) {
            // 30min ${LOCATION_UPDATE_INTERVAL} 内获取的位置，并且当前天气不可用，直接获取天气
            HSLog.i("fetchWeather la == " + locationManager.getLocation().getLatitude() + "  lo == " + locationManager.getLocation().getLongitude()
                    + "  city == " + locationManager.getCity() + "  saveCity == " + locationCity);
            doFetchWeather(locationManager.getLocation().getLatitude(), locationManager.getLocation().getLongitude());
        } else {
            doFetchLocation(HSLocationManager.LocationSource.DEVICE, true);
        }
    }

    private void doFetchWeather(double latitude, double longitude) {
        HSWeatherQuery query = new HSWeatherQuery(latitude, longitude, weatherQueryListener);
        query.start();
    }

    private void doFetchLocation(final HSLocationManager.LocationSource source, boolean isUpdateWeather) {
        HSLog.i("doFetchLocation source == " + source + "  update == " + isUpdateWeather);
        locationManager.stopFetching();
        locationManager = new HSLocationManager(context);
        locationManager.fetchLocation(source, new LocationListener(source, isUpdateWeather));
    }

    public void updateIfNeeded() {
        long curTime = System.currentTimeMillis();
        HSLog.d("updateIfNeeded");
        if (status == WeatherStatus.UPDATING && ((curTime - lastUpdateTime) > HSConfig.optInteger(WEATHER_UPDATE_INTERVAL, "Application", "Weather", "WeatherUpdateInterval") * DateUtils.SECOND_IN_MILLIS
                || (curTime - lastLocationTime) > HSConfig.optInteger(LOCATION_UPDATE_INTERVAL, "Application", "Weather", "LocationUpdateInterval") * DateUtils.SECOND_IN_MILLIS)) {
            HSLog.i("UPDATING change to failed  update == " + (curTime - lastUpdateTime) + "  loc == " + (curTime - lastLocationTime));
            status = WeatherStatus.FAILED;
            locationManager.stopFetching();
        }

        if (status == WeatherStatus.FAILED || status == WeatherStatus.INIT) {
            HSLog.i("FAILED updateWeather");
            updateWeather();
        } else if (status == WeatherStatus.SUCCEEDED) {
            if ((System.currentTimeMillis() - lastUpdateTime) > HSConfig.optInteger(WEATHER_UPDATE_INTERVAL, "Application", "Weather", "WeatherUpdateInterval") * DateUtils.SECOND_IN_MILLIS) {
                HSLog.i("time up updateWeather");
                updateWeather();
            } else if ((curTime - lastLocationTime) > HSConfig.optInteger(LOCATION_UPDATE_INTERVAL, "Application", "Weather", "LocationUpdateInterval") * DateUtils.SECOND_IN_MILLIS) {
                HSLog.i("time up location");
                doFetchLocation(HSLocationManager.LocationSource.DEVICE, false);
            } else {
                HSLog.i("time == " + ((curTime - lastUpdateTime)));
            }
        } else {
            HSLog.w("updateIfNeeded status == " + status);
        }
        HSGlobalNotificationCenter.sendNotification(NOTIFICATION_WEATHER_CONDITION_CHANGED);
        sendBroadcast();
    }

    public String getWeatherText(int type) {
        if (currentWeatherCondition == null) {
            return context.getString(R.string.unknown);
        }
        if (currentWeatherCondition.getCondition() == HSWeatherQueryResult.Condition.UNKNOWN) {
            return context.getString(R.string.unknown);
        }

        switch (type) {
            case WEATHER_TEXT_TEMP_ONLY:
                return getTemperatureDescription();
            case WEATHER_TEXT_CONDITION_TEMP:
                return getLocalSimpleConditionDescription() + ", " + getTemperatureDescription();
            case WEATHER_TEXT_TEMP_CONDITION:
                return getTemperatureDescription() + ", " + getLocalSimpleConditionDescription();
        }
        return getTemperatureDescription();
    }

    public String getTemperatureDescription() {
        if (currentWeatherCondition == null) {
            return context.getString(R.string.unknown);
        }
        if (currentWeatherCondition.getCondition() == HSWeatherQueryResult.Condition.UNKNOWN) {
            return context.getString(R.string.unknown);
        }
        if (!WeatherSettings.shouldDisplayFahrenheit()) {
            return currentWeatherCondition.getCelsius() + "°C";
        }
        return currentWeatherCondition.getFahrenheit() + "°F";
    }

    public String getLocalSimpleConditionDescription() {
        if (currentWeatherCondition == null)
            return context.getString(R.string.unknown);
        return getSimpleConditionDescription(currentWeatherCondition.getCondition());
    }

    public String getSimpleConditionDescription(HSWeatherQueryResult.Condition condition) {
        return context.getString(getSimpleConditionDescriptionResourceID(condition));
    }

    public int getSimpleConditionDescriptionResourceID(HSWeatherQueryResult.Condition condition) {
        int id = R.string.unknown;
        if (condition == null) {
            return id;
        }
        switch (condition) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case WARM:
                id = R.string.sunny;
                break;
            case OVERCAST:
                id = R.string.overcast;
                break;
            case FAIR:
            case CLEAR:
                id = R.string.clear;
                break;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                id = R.string.cloudy;
                break;
            case RAIN:
            case CHANCE_OF_RAIN:
            case RAIN_SHOWER:
                id = R.string.rain;
                break;
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
                id = R.string.drizzle;
                break;
            case STORM:
            case CHANCE_OF_STORM:
                id = R.string.storm;
                break;
            case SNOW:
            case CHANCE_OF_SNOW:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
                id = R.string.snow;
                break;
            case SNOW_SHOWER:
                id = R.string.snowshower;
                break;
            case SLEET:
            case RAIN_SNOW:
            case CHANCE_OF_SLEET:
                id = R.string.sleet;
                break;
            case HAZY:
            case SMOKE:
            case FOG:
            case MIST:
                id = R.string.hazy;
                break;
            case DUST:
                id = R.string.dust;
                break;
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                id = R.string.thunderstorm;
                break;
            case COLD:
            case FROZEN_MIX:
            case CHANCE_OF_FROZEN_MIX:
            case ICY:
                id = R.string.cold;
                break;
            case WINDY:
                id = R.string.windy;
                break;
            case HOT:
                id = R.string.hot;
                break;
        }
        return id;
    }

    private boolean isNight() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        HSLog.d(TAG, "Hour in days " + hour);
        return hour < 6 || hour >= 19;
    }

    public int getWeatherConditionIconResourceID() {
        if (currentWeatherCondition == null
                || currentWeatherCondition.getCondition() == HSWeatherQueryResult.Condition.UNKNOWN
                || currentWeatherCondition.getCondition() == null)
            return R.drawable.ic_locker;
        switch (currentWeatherCondition.getCondition()) {
            case SUNNY:
            case MOSTLY_SUNNY:
            case PARTLY_SUNNY:
            case WARM:
            case FAIR:
            case CLEAR:
                return isNight() ? R.drawable.ic_locker : R.drawable.ic_locker;
            case OVERCAST:
                return R.drawable.ic_locker;
            case CLOUDY:
            case MOSTLY_CLOUDY:
            case PARTLY_CLOUDY:
                return isNight() ? R.drawable.ic_locker : R.drawable.ic_locker;
            case RAIN:
            case CHANCE_OF_RAIN:
            case RAIN_SHOWER:
                return R.drawable.ic_locker;
            case DRIZZLE:
            case CHANCE_OF_DRIZZLE:
                return R.drawable.ic_locker;
            case STORM:
            case CHANCE_OF_STORM:
                return R.drawable.ic_locker;
            case SNOW:
            case CHANCE_OF_SNOW:
            case FLURRIES:
            case CHANCE_OF_FLURRY:
                return R.drawable.ic_locker;
            case SNOW_SHOWER:
                return R.drawable.ic_locker;
            case SLEET:
            case RAIN_SNOW:
            case CHANCE_OF_SLEET:
                return R.drawable.ic_locker;
            case HAZY:
            case SMOKE:
            case FOG:
            case MIST:
                return R.drawable.ic_locker;
            case DUST:
                return R.drawable.ic_locker;
            case THUNDERSTORM:
            case CHANCE_OF_THUNDERSTORM:
            case SCATTERED_THUNDERSTORM:
                return R.drawable.ic_locker;
            case COLD:
            case FROZEN_MIX:
            case CHANCE_OF_FROZEN_MIX:
            case ICY:
                return R.drawable.ic_locker;
            case WINDY:
                return R.drawable.ic_locker;
            case HOT:
                return R.drawable.ic_locker;
        }
        return R.drawable.ic_locker;
    }

    public String getWeatherConditionIconName() {
        int resID;
        if (currentWeatherCondition == null
                || currentWeatherCondition.getCondition() == null
                || currentWeatherCondition.getCondition() == HSWeatherQueryResult.Condition.UNKNOWN) {
            resID = R.string.weather_unknown;
        } else {
            switch (currentWeatherCondition.getCondition()) {
                case SUNNY:
                case MOSTLY_SUNNY:
                case PARTLY_SUNNY:
                case WARM:
                case FAIR:
                case CLEAR:
                    resID = isNight() ? R.string.weather_clear : R.string.weather_sunny;
                    break;
                case OVERCAST:
                    resID = R.string.weather_overcast;
                    break;
                case CLOUDY:
                case MOSTLY_CLOUDY:
                case PARTLY_CLOUDY:
                    resID = isNight() ? R.string.weather_cloudy_night : R.string.weather_cloudy;
                    break;
                case RAIN:
                case CHANCE_OF_RAIN:
                case RAIN_SHOWER:
                    resID = R.string.weather_rain;
                    break;
                case DRIZZLE:
                case CHANCE_OF_DRIZZLE:
                    resID = R.string.weather_drizzle;
                    break;
                case STORM:
                case CHANCE_OF_STORM:
                    resID = R.string.weather_rainshower;
                    break;
                case SNOW:
                case CHANCE_OF_SNOW:
                case FLURRIES:
                case CHANCE_OF_FLURRY:
                    resID = R.string.weather_snow;
                    break;
                case SNOW_SHOWER:
                    resID = R.string.weather_snowshower;
                    break;
                case SLEET:
                case RAIN_SNOW:
                case CHANCE_OF_SLEET:
                    resID = R.string.weather_sleet;
                    break;
                case HAZY:
                case SMOKE:
                case FOG:
                case MIST:
                    resID = R.string.weather_hazy;
                    break;
                case DUST:
                    resID = R.string.weather_dust;
                    break;
                case THUNDERSTORM:
                case CHANCE_OF_THUNDERSTORM:
                case SCATTERED_THUNDERSTORM:
                    resID = R.string.weather_thunderstorm;
                    break;
                case COLD:
                case FROZEN_MIX:
                case CHANCE_OF_FROZEN_MIX:
                case ICY:
                    resID = R.string.weather_cold;
                    break;
                case WINDY:
                    resID = R.string.weather_windy;
                    break;
                case HOT:
                    resID = R.string.weather_hot;
                    break;
                default:
                    resID = R.string.unknown;
                    break;
            }
        }
        return context.getString(resID);
    }
}
