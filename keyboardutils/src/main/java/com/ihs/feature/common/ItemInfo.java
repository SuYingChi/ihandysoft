/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ihs.feature.common;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.os.UserManagerCompat;

import java.util.Arrays;

/**
 * Represents an item in the launcher.
 */
public class ItemInfo {

    /**
     * Intent extra to store the profile. Format: UserHandle
     */
    public static final String EXTRA_PROFILE = "profile";

    public static final int NO_ID = -1;

    /**
     * The id in the settings database for this item
     */
    public long id = NO_ID;

    /**
     * One of {@link LauncherSettings.Favorites#ITEM_TYPE_APPLICATION},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_SHORTCUT},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_FOLDER},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_APPWIDGET},
     * {@link LauncherSettings.Favorites#ITEM_TYPE_CUSTOM_APPWIDGET} or
     * {@link LauncherSettings.Favorites#ITEM_TYPE_CUSTOM_FEATURE}.
     */
    public int itemType;

    /**
     * The id of the container that holds this item. For the desktop, this will be
     * {@link LauncherSettings.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). For user folders
     * it will be the id of the folder. Note that special values should be negative and folder
     * ids should be positive.
     */
    public long container = NO_ID;

    /**
     * Indicates the screen in which the shortcut appears. For items in the hotseat, this value is the
     * orientation invariant position (eg. 0, 1, 3, 4).
     */
    public long screenId = -1;

    /**
     * Indicates the X position of the associated cell.
     */
    public int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     */
    public int cellY = -1;

    /**
     * Indicates the X cell span.
     */
    public int spanX = 1;

    /**
     * Indicates the Y cell span.
     */
    public int spanY = 1;

    /**
     * Indicates the minimum X cell span.
     */
    public int minSpanX = 1;

    /**
     * Indicates the minimum Y cell span.
     */
    public int minSpanY = 1;

    /**
     * Indicates the position in an ordered list, for example, a folder.
     */
    public int rank = -1;

    /**
     * Indicates that the app is newly installed and not launched yet. This flat is intended to be
     * used only by {@link ShortcutInfo} && {@link AppInfo}.
     */
    public static final int FLAG_NEW_APP = 0x00000010;

    /**
     * Indicates that this item needs to be updated in the db
     */
    public boolean requiresDbUpdate = false;

    /**
     * Title of the item
     */
    public CharSequence title;

    /**
     * Content description of the item.
     */
    public CharSequence contentDescription;

    /**
     * The position of the item in a drag-and-drop operation.
     */
    public int[] dropPos = null;

    public UserHandleCompat user;


    public int options;

    public ItemInfo() {
        user = UserHandleCompat.myUserHandle();
    }

    ItemInfo(ItemInfo info) {
        copyFrom(info);
    }

    public void copyFrom(ItemInfo info) {
        id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        rank = info.rank;
        screenId = info.screenId;
        itemType = info.itemType;
        container = info.container;
        user = info.user;
        contentDescription = info.contentDescription;
    }

    public Intent getIntent() {
        throw new RuntimeException("Unexpected Intent");
    }

    /**
     * This should be overridden by subclasses to return correct package names.
     */
    public String getPackageName() {
        return "";
    }


    public static void writeBitmap(ContentValues values, Bitmap bitmap) {
        if (bitmap != null) {
            byte[] data = Utils.flattenBitmap(bitmap);
            values.put(LauncherSettings.Favorites.ICON, data);
        }
    }


    /**
     * It is very important that sub-classes implement this if they contain any references
     * to the activity (anything in the view hierarchy etc.). If not, leaks can result since
     * ItemInfo objects persist across rotation and can hence leak by holding stale references
     * to the old view hierarchy / activity.
     */
    public void unbind() {
    }

    public boolean inFolder() {
        return container > 0;
    }

    /**
     * Two items are considered identical if they have the same ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemInfo)) {
            return false;
        }
        ItemInfo another = (ItemInfo) o;
        return id != NO_ID && id == another.id;
    }

    @Override
    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container
                + " screen=" + screenId + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX
                + " spanY=" + spanY + " dropPos=" + Arrays.toString(dropPos)
                + " user=" + user + ")";
    }

    public boolean hasOption(int optionFlag) {
        return (options & optionFlag) != 0;
    }

    /**
     * @param option    flag to set or clear
     * @param isEnabled whether to set or clear the flag
     */
    public void setOption(int option, boolean isEnabled) {
        if (isEnabled) {
            options |= option;
        } else {
            options &= ~option;
        }
    }

}
