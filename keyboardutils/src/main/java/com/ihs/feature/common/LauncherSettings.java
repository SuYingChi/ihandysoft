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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Settings related utilities.
 */
public class LauncherSettings {

    /** Columns required on table staht will be subject to backup and restore. */
    public interface ChangeLogColumns extends BaseColumns {
        /**
         * The time of the last update to this row.
         * <P>Type: INTEGER</P>
         */
        String MODIFIED = "modified";
    }

    public interface BaseLauncherColumns extends ChangeLogColumns {
        /**
         * Descriptive name of the gesture that can be displayed to the user.
         * <P>Type: TEXT</P>
         */
        String TITLE = "title";

        /**
         * The Intent URL of the gesture, describing what it points to. This
         * value is given to {@link android.content.Intent#parseUri(String, int)} to create
         * an Intent that can be launched.
         * <P>Type: TEXT</P>
         */
        String INTENT = "intent";

        /**
         * The type of the gesture
         *
         * <P>Type: INTEGER</P>
         */
        String ITEM_TYPE = "itemType";

        /**
         * The gesture is an application
         */
        int ITEM_TYPE_APPLICATION = 0;

        /**
         * The gesture is an application created shortcut
         */
        int ITEM_TYPE_SHORTCUT = 1;

        /**
         * The icon type.
         * <P>Type: INTEGER</P>
         */
        String ICON_TYPE = "iconType";

        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        int ICON_TYPE_RESOURCE = 0;

        /**
         * The icon is a bitmap.
         */
        int ICON_TYPE_BITMAP = 1;

        /**
         * The icon package name, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        String ICON_PACKAGE = "iconPackage";

        /**
         * The icon resource id, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        String ICON_RESOURCE = "iconResource";

        /**
         * The custom icon bitmap, if icon type is ICON_TYPE_BITMAP.
         * <P>Type: BLOB</P>
         */
        String ICON = "icon";
    }

    /**
     * Favorites.
     */
    public static final class Favorites implements BaseLauncherColumns {

        public static final String TABLE_NAME = "favorites";

        public static final String LAST_LAUNCHED_TIME = "lastLaunchedTime";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + TABLE_NAME);

        /**
         * The content:// style URL for a given row, identified by its id.
         *
         * @param id The row id.
         *
         * @return The unique content URL for the specified row.
         */
        public static Uri getContentUri(long id) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                    "/" + TABLE_NAME + "/" + id);
        }

        /**
         * The container holding the favorite
         * <P>Type: INTEGER</P>
         */
        public static final String CONTAINER = "container";

        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        public static final int CONTAINER_DESKTOP = -100;
        public static final int CONTAINER_HOTSEAT = -101;

        public static String containerToString(int container) {
            switch (container) {
                case CONTAINER_DESKTOP: return "desktop";
                case CONTAINER_HOTSEAT: return "hotseat";
                default: return String.valueOf(container);
            }
        }

        /**
         * The screen holding the favorite (if container is CONTAINER_DESKTOP)
         * <P>Type: INTEGER</P>
         */
        public static final String SCREEN = "screen";

        /**
         * The X coordinate of the cell holding the favorite
         * (if container is CONTAINER_HOTSEAT or CONTAINER_HOTSEAT)
         * <P>Type: INTEGER</P>
         */
        public static final String CELLX = "cellX";

        /**
         * The Y coordinate of the cell holding the favorite
         * (if container is CONTAINER_DESKTOP)
         * <P>Type: INTEGER</P>
         */
        public static final String CELLY = "cellY";

        /**
         * The X span of the cell holding the favorite
         * <P>Type: INTEGER</P>
         */
        public static final String SPANX = "spanX";

        /**
         * The Y span of the cell holding the favorite
         * <P>Type: INTEGER</P>
         */
        public static final String SPANY = "spanY";

        /**
         * The profile id of the item in the cell.
         * <P>
         * Type: INTEGER
         * </P>
         */
        public static final String PROFILE_ID = "profileId";

        /**
         * The favorite is a user created folder
         */
        public static final int ITEM_TYPE_FOLDER = 2;

        /**
         * The favorite is a widget
         */
        public static final int ITEM_TYPE_APPWIDGET = 4;

        /**
         * The favorite is a custom widget provided by the launcher
         */
        public static final int ITEM_TYPE_CUSTOM_APPWIDGET = 5;

        /**
         * The favorite is a shortcut providing custom feature by the launcher
         */
        public static final int ITEM_TYPE_CUSTOM_FEATURE = 6;

        /**
         * The appWidgetId of the widget
         *
         * <P>Type: INTEGER</P>
         */
        public static final String APPWIDGET_ID = "appWidgetId";

        /**
         * The ComponentName of the widget provider
         *
         * <P>Type: STRING</P>
         */
        public static final String APPWIDGET_PROVIDER = "appWidgetProvider";
        
        /**
         * Indicates whether this favorite is an application-created shortcut or not.
         * If the value is 0, the favorite is not an application-created shortcut, if the
         * value is 1, it is an application-created shortcut.
         * <P>Type: INTEGER</P>
         */
        @Deprecated
        static final String IS_SHORTCUT = "isShortcut";

        /**
         * The URI associated with the favorite. It is used, for instance, by
         * live folders to find the content provider.
         * <P>Type: TEXT</P>
         */
        @Deprecated
        public static final String URI = "uri";

        /**
         * The display mode if the item is a live folder.
         * <P>Type: INTEGER</P>
         *
         * @see android.provider.LiveFolders#DISPLAY_MODE_GRID
         * @see android.provider.LiveFolders#DISPLAY_MODE_LIST
         */
        @Deprecated
        public static final String DISPLAY_MODE = "displayMode";

        /**
         * Boolean indicating that his item was restored and not yet successfully bound.
         * <P>Type: INTEGER</P>
         */
        public static final String RESTORED = "restored";

        /**
         * Indicates the position of the item inside an auto-arranged view like folder or hotseat.
         * <p>Type: INTEGER</p>
         */
        public static final String RANK = "rank";

        /**
         * Stores general flag based options for {@link ItemInfo}s.
         * <p>Type: INTEGER</p>
         */
        public static final String OPTIONS = "options";

        /**
         * If the folder is a auto-category folder, this field stores the category name.
         * <p>Type: TEXT</p>
         */
        public static final String CATEGORY = "category";

        /**
         * Indicates unread messages for {@link ItemInfo}s.
         */
        public static final String BADGE = "badge";
    }

    /**
     * Workspace Screens.
     *
     * Tracks the order of workspace screens.
     */
    public static final class WorkspaceScreens implements ChangeLogColumns {

        public static final String TABLE_NAME = "workspaceScreens";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + TABLE_NAME);

        /**
         * The rank of this screen -- ie. how it is ordered relative to the other screens.
         * <P>Type: INTEGER</P>
         */
        public static final String SCREEN_RANK = "screenRank";
    }

    /**
     * Statistics
     */
    public static final class Statistics implements ChangeLogColumns {

        public static final String TABLE_NAME = "statistics";

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + TABLE_NAME);

        /**
         * The intent of the reported launch event, brought by the broadcast intent.
         * <P>Type: TEXT</P>
         */
        public static final String INTENT = "intent";

        /**
         * The container ID brought by the broadcast intent.
         * <P>Type: INTEGER</P>
         */
        public static final String CONTAINER = "container";

        /**
         * The screen ID brought by the broadcast intent.
         * <P>Type: INTEGER</P>
         */
        public static final String SCREEN = "screen";

        /**
         * The X coordinate of the cell holding the favorite,
         * brought by the broadcast intent.
         * <P>Type: INTEGER</P>
         */
        public static final String CELLX = "cellX";

        /**
         * The Y coordinate of the cell holding the favorite,
         * brought by the broadcast intent.
         * <P>Type: INTEGER</P>
         */
        public static final String CELLY = "cellY";

        /**
         * The container name brought by the {@link Stats.LaunchSourceProvider}.
         * One of the following value is allowed:
         * {@link Stats#CONTAINER_SEARCH_BOX},
         * {@link Stats#CONTAINER_ALL_APPS},
         * {@link Stats#CONTAINER_HOMESCREEN},
         * {@link Stats#CONTAINER_HOTSEAT}.
         * <P>Type: TEXT</P>
         */
        public static final String SOURCE_CONTAINER = "source_container";

        /**
         * The container page brought by the {@link Stats.LaunchSourceProvider}.
         * <P>Type: INTEGER</P>
         */
        public static final String SOURCE_CONTAINER_PAGE = "source_container_page";

        /**
         * The sub-container name brought by the {@link Stats.LaunchSourceProvider}.
         * One of the following value is allowed:
         * {@link Stats#SUB_CONTAINER_FOLDER},
         * {@link Stats#SUB_CONTAINER_ALL_APPS_A_Z},
         * {@link Stats#SUB_CONTAINER_ALL_APPS_PREDICTION},
         * {@link Stats#SUB_CONTAINER_ALL_APPS_SEARCH}.
         * <P>Type: TEXT</P>
         */
        public static final String SOURCE_SUB_CONTAINER = "source_sub_container";

        /**
         * The sub-container page brought by the {@link Stats.LaunchSourceProvider}.
         * <P>Type: INTEGER</P>
         */
        public static final String SOURCE_SUB_CONTAINER_PAGE = "source_sub_container_page";
    }

    /**
     * Launcher settings
     */
    public static final class Settings {

        public static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/settings");

        public static final String METHOD_GET_BOOLEAN = "get_boolean_setting";
        public static final String METHOD_SET_BOOLEAN = "set_boolean_setting";
        public static final String EXTRA_VALUE = "value";
        public static final String EXTRA_DEFAULT_VALUE = "default_value";

        public static final String METHOD_BACKUP_DESKTOP = "backup_desktop";
        public static final String METHOD_INVALIDATE_DESKTOP_BACKUP = "invalidate_desktop_backup";
        public static final String METHOD_RESTORE_TO_DESKTOP_BACKUP = "restore_to_desktop_backup";
        public static final String EXTRA_BACKUP_NAME = "default_value";
    }
}
