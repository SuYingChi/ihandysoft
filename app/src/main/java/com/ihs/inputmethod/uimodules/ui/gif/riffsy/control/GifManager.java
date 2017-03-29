package com.ihs.inputmethod.uimodules.ui.gif.riffsy.control;

import android.content.ClipDescription;
import android.net.Uri;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.util.Pair;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.ihs.inputmethod.api.analytics.HSGoogleAnalyticsUtils;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.framework.HSInputMethodService;
import com.ihs.inputmethod.api.utils.HSFileUtils;
import com.ihs.inputmethod.uimodules.mediacontroller.shares.ShareUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.dao.DaoHelper;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.model.GifItem;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.download.GifDownloadTask;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.net.request.BaseRequest;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.ui.view.GifView;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.DirectoryUtils;
import com.ihs.inputmethod.uimodules.ui.gif.riffsy.utils.MediaShareUtils;

import java.io.File;

public final class GifManager {

    private static GifManager mInstance;

    private GifManager() {

    }

    public static GifManager getInstance() {
        if (mInstance == null) {
            init();
        }
        return mInstance;
    }

    public static void init() {
        if (mInstance == null) {
            synchronized (GifManager.class) {
                if (mInstance == null) {
                    mInstance = new GifManager();
                    DaoHelper.init();
                    DataManager.init();
                }
            }
        }
    }





    synchronized public void sendRequest(final BaseRequest request) {
        if (request != null) {
            RequestHandler.handleRequest(request);
        }
    }

    public void share(final GifItem gif, final String packageName, final GifDownloadTask.Callback callback) {
        if (!DirectoryUtils.isSDCardEnabled()) {
            HSInputMethod.inputText(gif.getUrl());
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "link");
            return;
        }


//        final File hdGif = new File(DirectoryUtils.getHDGifDownloadFolder(), gif.id);
        final File hdGif = new File(DirectoryUtils.getGifDownloadFolder(), gif.id);

        String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(HSInputMethodService.getInstance().getCurrentInputEditorInfo());
        boolean gifSupported = false;
        for (String mime_Type : mimeTypes) {
            if (ClipDescription.compareMimeTypes(mime_Type, "image/gif")) {
                gifSupported = true;
            }
        }
        if (gifSupported) {
            commitGifImage(Uri.fromFile(hdGif), "");
            HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "direct_send_gif");
            return;
        }


        File uri = DirectoryUtils.getDownloadGifUri(gif.id);

        // send mode
        final Pair<Integer, String> pair = MediaShareUtils.refreshCurrentShareMode(packageName);

        final int mode = pair.first;
        final String format = pair.second;
        final String mimeType;

        if (format.equals(MediaShareUtils.IMAGE_SHARE_FORMAT_MP4)) {
            if (gif.getMp4Url() != null) {
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "intent");
                shareMp4(gif, callback);
                return;
            }
        }

        mimeType = "image/*";
        final String targetFilePath = DirectoryUtils.getImageExportFolder() + "/" + gif.getId() + ".gif";

        switch (mode) {
            // image
            case MediaShareUtils.IMAGE_SHARE_MODE_INTENT:
                HSFileUtils.copyFile(uri.getAbsolutePath(), targetFilePath);
                try {
                    MediaShareUtils.shareImageByIntent(Uri.fromFile(new File(targetFilePath)), mimeType, packageName);
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "intent");
                } catch (Exception e) {
                    HSInputMethod.inputText(gif.getUrl());
                    HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "link");
                }
                break;
            // save to gallery
            case ShareUtils.IMAGE_SHARE_MODE_EXPORT:
                HSFileUtils.copyFile(uri.getAbsolutePath(), targetFilePath);
                MediaShareUtils.shareImageByExport(targetFilePath);
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "export");
                break;
            case MediaShareUtils.IMAGE_SHARE_MODE_LINK:
                HSInputMethod.inputText(gif.getUrl());
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "link");
                break;
            default:
                HSInputMethod.inputText(gif.getUrl());
                HSGoogleAnalyticsUtils.getInstance().logAppEvent("keyboard_gif_share_mode", "link");
        }
    }

    private void shareMp4(final GifItem gif, GifDownloadTask.Callback callback) {
        DownloadManager.getInstance().loadMp4(gif.id + ".mp4", gif.getMp4Url(), callback);
    }


    /**
     * Commits a GIF image
     *
     * @param contentUri       Content URI of the GIF image to be sent
     * @param imageDescription Description of the GIF image to be sent
     */
    private void commitGifImage(Uri contentUri, String imageDescription) {
        InputContentInfoCompat inputContentInfo = new InputContentInfoCompat(contentUri,
                new ClipDescription(imageDescription, new String[]{"image/gif"}), null);
        InputConnection inputConnection = HSInputMethodService.getInstance().getCurrentInputConnection();// getCurrentInputConnection();
        EditorInfo editorInfo = HSInputMethodService.getInstance().getCurrentInputEditorInfo();// getCurrentInputEditorInfo();
        int flags = 0;
        if (android.os.Build.VERSION.SDK_INT >= 25) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        InputConnectionCompat.commitContent(
                inputConnection, editorInfo, inputContentInfo, flags, null);
    }

}