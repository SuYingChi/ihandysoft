package com.ihs.keyboardutils.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.artw.lockscreen.LockerSettings;
import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.HSNotificationConstant;
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.notificationcenter.INotificationObserver;
import com.ihs.commons.utils.HSBundle;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.KCAnalyticUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.view.View.GONE;
import static com.ihs.app.framework.HSApplication.getContext;

/**
 * Created by Arthur on 17/4/29.
 */

public class KCNotificationManager {

    public interface NotificationAvailabilityCallBack {
        boolean isItemDownloaded(NotificationBean notificationBean);
    }

    private static final String PREFS_FILE_NAME = "notification_prefs";
    private static final String PREFS_FINISHED_EVENT = "prefs_finished_event";
    private static final String PREFS_NEXT_EVENT_TIME = "prefs_next_event_time";
    public static final String PREFS_NOTIFICATION_ENABLE = HSApplication.getContext().getString(R.string.prefs_notification_enable);
    private static final String PREFS_NEXT_NOTIFICATION_INDEX_IN_PLIST = "next_notification_index";

    private static final int NOTIFICATION_ID = Math.abs(HSApplication.getContext().getPackageName().hashCode() / 100000);

    private static KCNotificationManager instance;
    private static final String ACTION_CHARGING = "Charging";
    private static final String ACTION_LOCKER = "Locker";

    private Context context;
    private HSPreferenceHelper spHelper;
    private Class eventReceiverClass;
    private NotificationAvailabilityCallBack notificationCallBack;
    private boolean testSend = false;
    private INotificationObserver notificationObserver = new INotificationObserver() {
        @Override
        public void onReceive(String s, HSBundle hsBundle) {
            if (s.equals(HSNotificationConstant.HS_CONFIG_CHANGED)) {
                spHelper.putInt(PREFS_NEXT_NOTIFICATION_INDEX_IN_PLIST, 0);
                scheduleNextEventTime();
            }
        }
    };

    public synchronized static KCNotificationManager getInstance() {
        if (instance == null) {
            instance = new KCNotificationManager();
        }
        return instance;
    }

    public void init(Class eventReceiverClass, NotificationAvailabilityCallBack notificationAvaliablilityCallBack, boolean testSend) {
        notificationCallBack = notificationAvaliablilityCallBack;
        HSGlobalNotificationCenter.addObserver(HSNotificationConstant.HS_CONFIG_CHANGED, notificationObserver);
        this.eventReceiverClass = eventReceiverClass;
        this.testSend = testSend;
        scheduleNextEventTime();
    }

    private KCNotificationManager() {
        //初始化notification 设置项
        if (!HSPreferenceHelper.getDefault().contains(PREFS_NOTIFICATION_ENABLE)) {
            HSPreferenceHelper.getDefault().putBoolean(PREFS_NOTIFICATION_ENABLE, true);
        }
        context = getContext();
        spHelper = HSPreferenceHelper.create(getContext(), PREFS_FILE_NAME);

//        scheduleNextEvent();
    }

    private void scheduleNextEventTime() {
        if (testSend) {
            setNextTriggerTime(System.currentTimeMillis() + 10000);
            return;
        }

        //先检查是否有已经保存好的时间
        long nextTime = spHelper.getLong(PREFS_NEXT_EVENT_TIME, 0);
        if (nextTime > System.currentTimeMillis()) {
            setNextTriggerTime(nextTime);
            return;
        }
        List<Object> list = new ArrayList<>();
        try {
            list = (List<Object>) HSConfig.getList("Application", "LocalNotificationsPushTime");
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.e("没有配置通知时间");
        }

        if (list == null) {
            return;
        }

        //减少2秒真实时间用于计算耗时，保证 例如0.30的通知出不来，能够从0.29再次开始查找0.30是否能出
        long now = System.currentTimeMillis() - 2000;
        Calendar requestTime = Calendar.getInstance();
        long nextEventTime = Long.MAX_VALUE;

        long lastPushTime = spHelper.getLong(PREFS_NEXT_EVENT_TIME, 0);


        for (int i = 0; i < list.size(); i++) {
            Object time = list.get(i);
            int hour = 0;
            int min = 0;
            if (time instanceof Integer) {
                hour = (Integer) time;
                min = 0;
            } else if (time instanceof Double) {
                hour = ((Double) time).intValue();
                min = (int) ((((Double) time) - hour) * 60);
            }

            requestTime.set(Calendar.HOUR_OF_DAY, hour);
            requestTime.set(Calendar.MINUTE, min);
            requestTime.set(Calendar.SECOND, 0);

            long timeInMillis = requestTime.getTimeInMillis();
            if (timeInMillis - lastPushTime < 1000 || timeInMillis < now) {
                timeInMillis = timeInMillis + TimeUnit.DAYS.toMillis(1);
            }

            if (timeInMillis < nextEventTime) {
                nextEventTime = timeInMillis;
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nextEventTime);
        HSLog.e("下次通知时间 " + calendar.getTime().toString());
        spHelper.putLong(PREFS_NEXT_EVENT_TIME, nextEventTime);
        setNextTriggerTime(nextEventTime);
    }

    public void sendNotification() {
        if (!HSPreferenceHelper.getDefault().getBoolean(PREFS_NOTIFICATION_ENABLE, true)) {
            return;
        }

        if (!ChargingManagerUtil.isNetworkAvailable(context)) {
            scheduleNextEventTime();
            return;
        }

        String recordedEvent = spHelper.getString(PREFS_FINISHED_EVENT, "");
        List<String> finishedEvent = Arrays.asList(recordedEvent.split(","));


        List<Map<String, ?>> configs = null;
        try {
            configs = (List<Map<String, ?>>) HSConfig.getList("Application", "LocalNotifications");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (configs == null) {
            HSLog.e("没有配置本地提醒");
            return;
        }

        int nextNotificationIndex = spHelper.getInt(PREFS_NEXT_NOTIFICATION_INDEX_IN_PLIST, 0);

        if (nextNotificationIndex >= configs.size()) {
            HSLog.e("通知循环完毕");
            return;
        }

        NotificationBean notificationToSend = getNextAvailableBean(configs, finishedEvent, recordedEvent, nextNotificationIndex);

        if (notificationToSend == null) {
            return;
        }

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notificationToSend.getTitle())
                .setContentText(notificationToSend.getMessage())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL);

        final NotificationBean beanCopy = notificationToSend;
        final RemoteViews contentView = new RemoteViews(HSApplication.getContext().getPackageName(), R.layout.notification_custom);

        contentView.setImageViewResource(R.id.notification_icon, context.getApplicationInfo().icon);

        contentView.setTextViewText(R.id.notification_description, notificationToSend.getMessage());
        contentView.setTextColor(R.id.notification_description, notificationToSend.getMessageColor());

        contentView.setTextViewText(R.id.notification_title, notificationToSend.getTitle());
        contentView.setTextColor(R.id.notification_title, notificationToSend.getTitleColor());

        contentView.setInt(R.id.notification_background, "setBackgroundColor", notificationToSend.getBgColor());

        contentView.setTextViewText(R.id.notification_action, notificationToSend.getButtonText());
        contentView.setTextColor(R.id.notification_action, notificationToSend.getButtonTextColor());
        contentView.setInt(R.id.notification_action, "setBackgroundResource", R.drawable.notification_action_bg);

        switch (notificationToSend.getStyle()) {
            //系统默认样式
            case 0:
            default:
                //icon为空 则用默认icon 否则网络请求次数加一
                if (TextUtils.isEmpty(notificationToSend.getIconUrl())) {
                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon));
                    tryToNotify(mBuilder, beanCopy);
                    HSLog.e("notification 默认 no icon ");
                } else {
                    ImageLoader.getInstance().loadImage(notificationToSend.getIconUrl(), new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            HSLog.e("icon 加载失败 url: " + imageUri);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            HSLog.e("icon 加载成功 " + imageUri);
                            mBuilder.setLargeIcon(loadedImage);
                            tryToNotify(mBuilder, beanCopy);
                            HSLog.e("notification 默认 with icon");
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    });
                }
                break;

            //附带背景不带button样式
            case 1:
            case 2: //带button 不配置的项都为默认。

                //如果bg也不为空就要用remoteview 并将会产生两次图片请求的回传。
                int requestCount = 0;

                //icon为空 则用默认icon 否则网络请求次数加一
                if (TextUtils.isEmpty(notificationToSend.getIconUrl())) {
                    mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon));
                } else {
                    requestCount++;
                }

                //bg为空则默认bg 否则网络请求次数加一
                if (!TextUtils.isEmpty(notificationToSend.getBgUrl())) {
                    requestCount++;
                }

                if (notificationToSend.getStyle() == 1) {
                    contentView.setViewVisibility(R.id.notification_action, GONE);
                    HSLog.e("notification 自定义除button");
                } else if (notificationToSend.getStyle() == 2) {
                    HSLog.e("notification 完全自定义");
                }

                //如果没有任何需要网络加载的直接发送。
                if (requestCount == 0) {
                    tryToNotify(mBuilder, beanCopy);
                } else {
                    final int[] imgRequestCompleteCount = {requestCount};

                    if (!TextUtils.isEmpty(notificationToSend.getIconUrl())) {
                        ImageLoader.getInstance().loadImage(notificationToSend.getIconUrl(), new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {

                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                HSLog.e("icon 加载失败 url: " + imageUri);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                HSLog.e("icon 加载成功 " + imageUri);
                                imgRequestCompleteCount[0]--;
                                contentView.setImageViewBitmap(R.id.notification_icon, loadedImage);
                                mBuilder.setContent(contentView);
                                if (imgRequestCompleteCount[0] == 0) {
                                    tryToNotify(mBuilder, beanCopy);
                                }
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {

                            }
                        });
                    }

                    if (!TextUtils.isEmpty(notificationToSend.getBgUrl())) {
                        ImageLoader.getInstance().loadImage(notificationToSend.getBgUrl(), new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {

                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                                HSLog.e("bg 加载失败 url: " + imageUri);
                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                HSLog.e("bg 加载成功 " + imageUri);
                                imgRequestCompleteCount[0]--;
                                contentView.setImageViewBitmap(R.id.notification_background, loadedImage);
                                mBuilder.setContent(contentView);
                                if (imgRequestCompleteCount[0] == 0) {
                                    tryToNotify(mBuilder, beanCopy);
                                }
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {

                            }
                        });
                    }
                }
                break;
        }

        //无论此次结果如何，均请求下一次。
        scheduleNextEventTime();
    }

    private NotificationBean getNextAvailableBean(List<Map<String, ?>> configs, List<String> finishedEvent, String recordedEvent, int nextNotificationIndex) {
        NotificationBean bean = null;
        try {
            Map<String, Object> value = (Map<String, Object>) configs.get(nextNotificationIndex);
            bean = new NotificationBean(value);
        } catch (Exception e) {
            HSLog.e("wrong config for ==> " + configs.get(nextNotificationIndex));
        }

        if (bean == null) {
            if (nextNotificationIndex >= configs.size()) {
                return null;
            } else {
                return getNextAvailableBean(configs, finishedEvent, recordedEvent, ++nextNotificationIndex);
            }
        }

        if (!TextUtils.isEmpty(bean.getName()) && finishedEvent.contains(bean.getSPKey())) {
            return getNextAvailableBean(configs, finishedEvent, recordedEvent, ++nextNotificationIndex);
        }

        if (bean.getActionType().equals(ACTION_LOCKER)) {
            if (isLockerEnabled() || LockerSettings.isUserTouchedLockerSettings()) {
                return getNextAvailableBean(configs, finishedEvent, recordedEvent, ++nextNotificationIndex);
            } else {
                spHelper.putInt(PREFS_NEXT_NOTIFICATION_INDEX_IN_PLIST, ++nextNotificationIndex);
                return bean;
            }
        } else if (bean.getActionType().equals(ACTION_CHARGING)) {
            if (isChargingEnabled() || ChargingPrefsUtil.isUserTouchedChargingSetting()) {
                return getNextAvailableBean(configs, finishedEvent, recordedEvent, ++nextNotificationIndex);
            } else {
                spHelper.putInt(PREFS_NEXT_NOTIFICATION_INDEX_IN_PLIST, ++nextNotificationIndex);
                return bean;
            }
        } else {
            //如果下载过了，就记录到不再发送列表里面
            if (notificationCallBack.isItemDownloaded(bean)) {
                spHelper.putString(PREFS_FINISHED_EVENT, recordedEvent + bean.getSPKey() + ",");
                return getNextAvailableBean(configs, finishedEvent, recordedEvent, ++nextNotificationIndex);
            } else {
                spHelper.putInt(PREFS_NEXT_NOTIFICATION_INDEX_IN_PLIST, ++nextNotificationIndex);
                return bean;
            }
        }
    }

    private void tryToNotify(NotificationCompat.Builder mBuilder, NotificationBean notificationBean) {
        HSLog.e("本次通知actionType" + notificationBean.getActionType() + " name " + notificationBean.getName());
        Intent intent = new Intent(context, eventReceiverClass);
        intent.setAction(Long.toString(System.currentTimeMillis()));
        intent.putExtra("actionType", notificationBean.getActionType());
        intent.putExtra("name", notificationBean.getName());
        PendingIntent resultPendingIntent;

        resultPendingIntent = PendingIntent.getBroadcast(
                getContext(),
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT
        );
        mBuilder.setContentIntent(resultPendingIntent);


        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        try {
            //用event名 加 packageName的hashcode来确保，每个程序有自己的一套通知系统，并且，每种通知事件不重复。
            manager.notify(notificationBean.getActionType(), NOTIFICATION_ID, mBuilder.build());
            switch (notificationBean.getActionType()) {
                default:
                    if (!TextUtils.isEmpty(notificationBean.getName())) {
                        String finishedEvents = spHelper.getString(PREFS_FINISHED_EVENT, "");
                        spHelper.putString(PREFS_FINISHED_EVENT, finishedEvents + notificationBean.getSPKey() + ",");
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        KCAnalyticUtil.logEvent("local_push_showed", notificationBean.getActionType());
        KCAnalyticUtil.logEvent("local_push_showed_content_name", notificationBean.getName());
    }

    public static void logNotificationClick(String actionType, String name) {
        KCAnalyticUtil.logEvent("local_push_showed", actionType);
        KCAnalyticUtil.logEvent("local_push_showed_content_name", name);
    }

    private boolean isChargingEnabled() {
        return ChargingPrefsUtil.getChargingEnableStates() == ChargingPrefsUtil.CHARGING_DEFAULT_ACTIVE;
    }

    private boolean isLockerEnabled() {
        return LockerSettings.getLockerEnableStates() == LockerSettings.LOCKER_DEFAULT_ACTIVE;
    }

    private void setNextTriggerTime(long time) {
        Intent intent = new Intent(context, NotificationExecutionReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        try {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
