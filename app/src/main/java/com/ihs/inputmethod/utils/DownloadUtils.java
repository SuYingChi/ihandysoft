package com.ihs.inputmethod.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSLog;
import com.ihs.commons.utils.HSPreferenceHelper;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.keyboardutils.adbuffer.AdLoadingView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

/**
 * Created by guonan.lv on 17/8/31.
 */

public class DownloadUtils {
    private static DownloadUtils instance;
    private String filePath;
    private String objectName;

    public static DownloadUtils getInstance() {
        if (instance == null) {
            synchronized (DownloadUtils.class) {
                if (instance == null) {
                    instance = new DownloadUtils();
                }
            }
        }
        return instance;
    }

    public void saveJsonArrayToPref(String key, String value) {
        try {
            String originValue = HSPreferenceHelper.getDefault().getString(key, "");
            JSONArray jsonArray;
            if ("".equals(originValue)) {
                jsonArray = new JSONArray();
            } else {
                jsonArray = new JSONArray(originValue);
            }
            jsonArray.put(value);
            HSPreferenceHelper.getDefault().putString(key, jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initConnection(final Resources resources, final AdLoadingView adLoadingView, final HSHttpConnection connection,
                                final HSHttpConnection.OnConnectionFinishedListener onConnectionFinishedListener) {
        connection.setDownloadFile(HSFileUtils.createNewFile(filePath));
        connection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                if (onConnectionFinishedListener != null) {
                    onConnectionFinishedListener.onConnectionFinished(hsHttpConnection);
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                HSLog.e("startForegroundDownloading onConnectionFailed hsError" + hsError.getMessage());
                adLoadingView.setConnectionStateText(resources.getString(R.string.foreground_download_failed));
                adLoadingView.setConnectionProgressVisibility(View.INVISIBLE);
                if (onConnectionFinishedListener != null) {
                    onConnectionFinishedListener.onConnectionFailed(hsHttpConnection, hsError);
                }
            }
        });
        connection.setHeaderReceivedListener(new HSHttpConnection.OnHeaderReceivedListener() {
            @Override
            public void onHeaderReceived(HSHttpConnection hsHttpConnection) {
                new Handler().post(new Runnable() {
                    int initialProgress = 1;
                    @Override
                    public void run() {
                        adLoadingView.updateProgressPercent(initialProgress);
                    }
                });
            }
        });
        connection.setDataReceivedListener(new HSHttpConnection.OnDataReceivedListener() {
            @Override
            public void onDataReceived(HSHttpConnection hsHttpConnection, byte[] bytes, long received, long totalSize) {
                if (totalSize > 0) {
                    final float percent = (float) received * 100 / totalSize;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            adLoadingView.updateProgressPercent((int) percent);
                        }
                    });
                }
            }
        });
        connection.startAsync();
    }

    public void startForegroundDownloading(Context context, final String objectName, final String filePath, final String downloadUrl,
                                           final Drawable thumbnailDrawable, final AdLoadingView.OnAdBufferingListener onAdBufferingListener,
                                           final HSHttpConnection.OnConnectionFinishedListener onConnectionFinishedListener) {
        HSHttpConnection connection;
        this.objectName = objectName;
        this.filePath = filePath;
        connection = new HSHttpConnection(downloadUrl);

        final AdLoadingView adLoadingView = new AdLoadingView(context);
        final Resources resources = HSApplication.getContext().getResources();
        adLoadingView.configParams(null, thumbnailDrawable != null ? thumbnailDrawable : resources.getDrawable(R.drawable.ic_sticker_loading_image),
                resources.getString(R.string.sticker_downloading_label),
                resources.getString(R.string.sticker_downloading_successful),
                resources.getString(R.string.ad_placement_lucky),
                new AdLoadingView.OnAdBufferingListener() {
                    @Override
                    public void onDismiss(boolean downloadSuccess) {
                        if (downloadSuccess) {
                        } else {
                            // 没下载成功
                            HSHttpConnection connection = (HSHttpConnection) adLoadingView.getTag();
                            if (connection != null) {
                                connection.cancel();
                                HSFileUtils.delete(new File(filePath));
                            }
                        }
                        if (onAdBufferingListener != null) {
                            onAdBufferingListener.onDismiss(downloadSuccess);
                        }
                    }
                }, 2000, false);
        adLoadingView.showInDialog();

        initConnection(resources, adLoadingView, connection, onConnectionFinishedListener);
        adLoadingView.setTag(connection);
    }
}