package com.ihs.keyboardutils.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.keyboardutils.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.ihs.app.framework.HSApplication.getContext;

/**
 * Created by Arthur on 17/4/29.
 */

public class KCNotificationManager {
    private static final String PREFS_FILE_NAME = "notification_prefs";
    private static int intervalDuration = 5 * 1000;//24 * 3600 * 1000;
    //方法延迟或者计算误差
    private static final int METHOD_EXCUTION_ERROR_TIME = 10;
    private static final int HANDLER_MSG_WHAT = 10;

    private static final int NOTIFICATION_ID = Math.abs(HSApplication.getContext().hashCode() / 100000);


    private static KCNotificationManager instance;
    private ArrayList<NotificationBean> notificationBeanList;

    private Map<String, Intent> intentMap;
    private Context context;
    private INotificationListener notificationListener;
    private HSPreferenceHelper spHelper;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (hasMessages(HANDLER_MSG_WHAT)) {
                return;
            }
            scheduleNotify();
        }
    };

    public interface INotificationListener {
        Map<String, Intent> onInitIntent(ArrayList<String> eventList);
    }

    public synchronized static KCNotificationManager getInstance() {
        if (instance == null) {
            instance = new KCNotificationManager();
        }
        return instance;
    }

    public void init(INotificationListener listener) {
        this.notificationListener = listener;
        refreshConfig();
        scheduleNotify();
    }

    private KCNotificationManager() {
        context = getContext();
        spHelper = HSPreferenceHelper.create(getContext(), PREFS_FILE_NAME);
    }

    private void scheduleNotify() {

        //循环已经按priority排序的bean列表
        for (NotificationBean notificationBean : notificationBeanList) {

            //从本地去出保存的 maxShowCount和lastShow。格式：("beanEventName","eventShowTimes,lastShow")
            String[] eventRecord = spHelper.getString(notificationBean.getEvent(), "0,0").split(",");
            int eventShowTimes = Integer.valueOf(eventRecord[0]);
            long lastShow = Long.valueOf(eventRecord[1]);

            //如果达到可以出现的条件
            if ((notificationBean.getMaxShowCount() == 0 ||
                    eventShowTimes < notificationBean.getMaxShowCount()) && //出现次数小于等于最大次数，从1开始记录
                    //出现间隔
                    System.currentTimeMillis() - lastShow >= intervalDuration * notificationBean.getInterval()) {

                //一天只发送一个notification
                sendNotification(notificationBean);

                eventShowTimes++;

                spHelper.putString(notificationBean.getEvent(), String.format(Locale.ENGLISH, "%d,%d", eventShowTimes, System.currentTimeMillis()));
                break;
            }
        }

        handler.sendEmptyMessageDelayed(HANDLER_MSG_WHAT, intervalDuration);
    }

    public void refreshConfig() {
        Map<String, ?> configs = HSConfig.getMap("Application", "LocalNotifications");
        if (configs == null) {
            HSLog.e("没有配置本地提醒");
            return;
        }
        intentMap = new HashMap<>();
        notificationBeanList = new ArrayList<>();
        ArrayList<String> eventList = new ArrayList<>();
        for (Map.Entry<String, ?> entry : configs.entrySet()) {
            String eventType = entry.getKey();
            try {
                Map<String, Object> value = (Map<String, Object>) entry.getValue();
                NotificationBean bean = new NotificationBean(value);
                bean.setEvent(eventType);

                notificationBeanList.add(bean);
                eventList.add(eventType);
            } catch (Exception e) {
                HSLog.e("wrong config for ==> " + eventType);
            }
        }
        Collections.sort(notificationBeanList);

        if (HSLog.isDebugging()) {
            for (NotificationBean notificationBean : notificationBeanList) {
                HSLog.d(notificationBean.toString());
            }
        }
        intentMap = notificationListener.onInitIntent(eventList);
    }

    private void sendNotification(NotificationBean notificationBean) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notificationBean.getTitle())
                .setContentText(notificationBean.getMessage());

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(notificationBean.getMessage());
        bigText.setBigContentTitle(notificationBean.getTitle());
        mBuilder.setStyle(bigText);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (intentMap != null) {
            Intent intent = intentMap.get(notificationBean.getEvent());
            if (intent != null) {
                intent.setAction(Long.toString(System.currentTimeMillis()));
                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                getContext(),
                                0,
                                intent,
                                PendingIntent.FLAG_ONE_SHOT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
            }
        }


        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        try {
            //用event名 加 packageName的hashcode来确保，每个程序有自己的一套通知系统，并且，每种通知事件不重复。
            manager.notify(notificationBean.getEvent(), NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setIntervalDuration(int intervalDuration) {
        KCNotificationManager.intervalDuration = intervalDuration;
    }
}
