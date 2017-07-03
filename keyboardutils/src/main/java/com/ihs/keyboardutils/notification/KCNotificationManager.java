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
import com.ihs.chargingscreen.utils.ChargingManagerUtil;
import com.ihs.chargingscreen.utils.ChargingPrefsUtil;
import com.ihs.commons.config.HSConfig;
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

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
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

    private static final int NOTIFICATION_ID = Math.abs(HSApplication.getContext().getPackageName().hashCode() / 100000);

    private static KCNotificationManager instance;
    private static final String ACTION_CHARGING = "Charging";
    private static final String ACTION_LOCKER = "Locker";

    private Context context;
    private HSPreferenceHelper spHelper;
    private Class eventReceiverClass;
    private NotificationAvailabilityCallBack notificationCallBack;

    public synchronized static KCNotificationManager getInstance() {
        if (instance == null) {
            instance = new KCNotificationManager();
        }
        return instance;
    }

    public void init(Class eventReceiverClass, NotificationAvailabilityCallBack notificationAvaliablilityCallBack) {
        notificationCallBack = notificationAvaliablilityCallBack;
        this.eventReceiverClass = eventReceiverClass;
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
        if(true){
            setNextTriggerTime(System.currentTimeMillis()+10000);
            return;
        }

        //先检查是否有已经保存好的时间
        long nextTime = spHelper.getLong(PREFS_NEXT_EVENT_TIME, 0);
        if (nextTime > System.currentTimeMillis()) {
            setNextTriggerTime(nextTime);
            return;
       }
        List<Float> list = new ArrayList<>();
        try {
            list = (List<Float>) HSConfig.getList("Application", "LocalNotificationsPushTime");
        } catch (Exception e) {
            e.printStackTrace();
            HSLog.e("没有配置通知时间");
        }

        if (list == null) {
            return;
        }

        //减少2秒真实时间用于计算耗时，保证 例如0.30的通知出不来，能够从0.29再次开始查找0.30是否能出
        long now = System.currentTimeMillis() - 2000;
        Calendar today = Calendar.getInstance();
        long nextEventTime = Long.MAX_VALUE;
        for (Float time : list) {
            int hour = time.intValue();
            int min = (int) ((time - hour) * 60);
            today.set(Calendar.HOUR_OF_DAY, hour);
            today.set(Calendar.MINUTE, min);
            long timeInMillis = today.getTimeInMillis();
            if (timeInMillis > now) {
                if (timeInMillis < nextEventTime) {
                    nextEventTime = timeInMillis;
                }
            }
        }
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

        int chargingShowedTimes = spHelper.getInt(ACTION_CHARGING, 0);
        int lockerShowedTimes = spHelper.getInt(ACTION_LOCKER, 0);
        int lockerCanShowTimes = 0;
        int chargingCanShowTimes = 0;

        //找出charging 或者locker出现的次数。
        for (Map<String, ?> config : configs) {
            NotificationBean notificationBean = new NotificationBean((Map<String, Object>) config);
            if (notificationBean.getActionType().equals(ACTION_LOCKER)) {
                lockerCanShowTimes++;
            } else if (notificationBean.getActionType().equals(ACTION_CHARGING)) {
                chargingCanShowTimes++;
            }
        }

        NotificationBean notificationToSend = null;
        for (int i = 0; i < configs.size(); i++) {
            NotificationBean bean = null;
            try {
                Map<String, Object> value = (Map<String, Object>) configs.get(i);
                bean = new NotificationBean(value);
            } catch (Exception e) {
                HSLog.e("wrong config for ==> " + configs.get(i));
            }

            if (bean == null) {
                continue;
            }

            if (finishedEvent.contains(bean.getSPKey())) {
                continue;
            }

            if (bean.getActionType().equals(ACTION_LOCKER)) {
                if (isLockerEnabled()) {
                    continue;
                } else if (lockerShowedTimes < lockerCanShowTimes) {
                    notificationToSend = bean;
                    break;
                }
            } else if (bean.getActionType().equals(ACTION_CHARGING)) {
                if (isChargingEnabled()) {
                    continue;
                } else if (chargingShowedTimes < chargingCanShowTimes) {
                    notificationToSend = bean;
                    break;
                }
            } else {
                //如果下载过了，就记录到不再发送列表里面
                if (notificationCallBack.isItemDownloaded(bean)) {
                    spHelper.putString(PREFS_FINISHED_EVENT, recordedEvent + bean.getSPKey() + ",");
                    continue;
                } else {
                    notificationToSend = bean;
                    break;
                }
            }
        }

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
        contentView.setTextViewText(R.id.notification_title, notificationToSend.getTitle());
        contentView.setTextViewText(R.id.notification_description, notificationToSend.getMessage());


        //如果iconurl没有给的话 就用默认icon
        if (TextUtils.isEmpty(notificationToSend.getIconUrl())) {
            //如果背景也没给就直接默认模式
            if (TextUtils.isEmpty(notificationToSend.getBgUrl())) {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon));
                tryToNotify(mBuilder, beanCopy);
            } else {
                //如果给了背景就要用自定义样式
                ImageLoader.getInstance().loadImage(notificationToSend.getBgUrl(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        contentView.setImageViewBitmap(R.id.notification_background, loadedImage);
                        mBuilder.setContent(contentView);
                        tryToNotify(mBuilder, beanCopy);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
            }

        } else {

            //如果icon 不为空的情况下
            //如果bg为空，则从网上加载icon
            if (TextUtils.isEmpty(notificationToSend.getBgUrl())) {
                ImageLoader.getInstance().loadImage(notificationToSend.getIconUrl(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        mBuilder.setLargeIcon(loadedImage);
                        tryToNotify(mBuilder, beanCopy);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                });
            } else {

                //如果bg也不为空就要用remoteview 并将会产生两次图片请求的回传。
                final int[] imgRequestCompleteCount = {2};
                ImageLoader.getInstance().loadImage(notificationToSend.getIconUrl(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        scheduleNextEventTime();
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
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

                ImageLoader.getInstance().loadImage(notificationToSend.getBgUrl(), new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
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

        //无论此次结果如何，均请求下一次。
        scheduleNextEventTime();
    }

    private void tryToNotify(NotificationCompat.Builder mBuilder, NotificationBean notificationBean) {
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
                case ACTION_LOCKER:
                case ACTION_CHARGING:
                    int chargingShowed = spHelper.getInt(notificationBean.getActionType(), 0);
                    spHelper.putInt(notificationBean.getActionType(), ++chargingShowed);
                    break;
                default:
                    String finishedEvents = spHelper.getString(PREFS_FINISHED_EVENT, "");
                    spHelper.putString(PREFS_FINISHED_EVENT, finishedEvents + notificationBean.getSPKey() + ",");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        KCAnalyticUtil.logEvent("notification_show", notificationBean.getActionType());
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
