package com.artw.lockscreen;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ihs.commons.utils.HSLog;

public class LockerContentProvider extends ContentProvider { //该Provider用来让外界获取Locker的相关状态
    private static final String TAG = "LockerContentProvider";
    public static final String GET_LOCKER_STATE = "GET_LOCKER_STATE";
    public static final String KEY_LOCKER_ENABLE = "LOCKER_ENABLE";
    public static final String KEY_USER_TOUCHED = "USER_TOUCHED";

    public LockerContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Call a provider-defined method.  This can be used to implement
     * interfaces that are cheaper and/or unnatural for a table-like
     * model.
     * <p>
     * <p class="note"><strong>WARNING:</strong> The framework does no permission checking
     * on this entry into the content provider besides the basic ability for the application
     * to get access to the provider at all.  For example, it has no idea whether the call
     * being executed may read or write data in the provider, so can't enforce those
     * individual permissions.  Any implementation of this method <strong>must</strong>
     * do its own permission checks on incoming calls to make sure they are allowed.</p>
     *
     * @param method method name to call.  Opaque to framework, but should not be {@code null}.
     * @param arg    provider-defined String argument.  May be {@code null}.
     * @param extras provider-defined Bundle argument.  May be {@code null}.
     * @return provider-defined return value.  May be {@code null}, which is also
     * the default for providers which don't implement any call methods.
     */
    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        HSLog.d(TAG, "call method: " + method);
        if (getContext() == null) {
            HSLog.e(TAG, "getContext is null.");
            return null;
        } else {
            Bundle bundle = new Bundle();
            boolean lockerEnabled = LockerSettings.isLockerEnabled();
            boolean isUserTouched = LockerSettings.isUserTouchedLockerSettings();
            HSLog.d(TAG, "lockerEnabled: " + lockerEnabled + " isUserTouched: " + isUserTouched);
            if (TextUtils.equals(GET_LOCKER_STATE, method)) {
                bundle.putBoolean(KEY_LOCKER_ENABLE, lockerEnabled);
                bundle.putBoolean(KEY_USER_TOUCHED, isUserTouched);
            }
            return bundle;
        }
    }
}
