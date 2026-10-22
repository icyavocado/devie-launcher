package com.voxel.launcher3.compat;

import android.content.Context;
import com.voxel.launcher3.Utilities;
import java.util.HashSet;

/* JADX INFO: loaded from: classes.dex */
public abstract class PackageInstallerCompat {
    private static PackageInstallerCompat sInstance;
    private static final Object sInstanceLock = new Object();

    public static final class PackageInstallInfo {
        public final String packageName;
        public int progress;
        public int state;

        public PackageInstallInfo(String str) {
            this.packageName = str;
        }

        public PackageInstallInfo(String str, int i, int i2) {
            this.packageName = str;
            this.state = i;
            this.progress = i2;
        }
    }

    public static PackageInstallerCompat getInstance(Context context) {
        PackageInstallerCompat packageInstallerCompat;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.isLmpOrAbove()) {
                    sInstance = new PackageInstallerCompatVL(context);
                } else {
                    sInstance = new PackageInstallerCompatV16(context) { // from class: com.voxel.launcher3.compat.PackageInstallerCompat.1
                    };
                }
            }
            packageInstallerCompat = sInstance;
        }
        return packageInstallerCompat;
    }

    public abstract void onFinishBind();

    public abstract void onPause();

    public abstract void onResume();

    public abstract void onStop();

    public abstract HashSet<String> updateAndGetActiveSessionCache();
}
