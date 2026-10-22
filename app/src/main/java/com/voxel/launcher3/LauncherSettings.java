package com.voxel.launcher3;

import android.net.Uri;
import android.provider.BaseColumns;

/* JADX INFO: loaded from: classes.dex */
public class LauncherSettings {

    interface BaseLauncherColumns extends ChangeLogColumns {
    }

    interface ChangeLogColumns extends BaseColumns {
    }

    public static final class Favorites implements BaseLauncherColumns {
        static final Uri CONTENT_URI = Uri.parse("content://com.ovacoda.devie.settings/favorites?notify=true");
        static final Uri OLD_CONTENT_URI = Uri.parse("content://com.android.launcher2.settings/favorites?notify=true");
        static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://com.ovacoda.devie.settings/favorites?notify=false");

        static final String containerToString(int i) {
            switch (i) {
                case -101:
                    return "hotseat";
                case -100:
                    return "desktop";
                default:
                    return String.valueOf(i);
            }
        }

        static Uri getContentUri(long j, boolean z) {
            return Uri.parse("content://com.ovacoda.devie.settings/favorites/" + j + "?notify=" + z);
        }
    }

    public static final class WorkspaceScreens implements ChangeLogColumns {
        public static final Uri CONTENT_URI = Uri.parse("content://com.ovacoda.devie.settings/workspaceScreens?notify=true");
    }
}
