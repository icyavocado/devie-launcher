package com.voxel.launcher3;

import android.app.ActivityManager;
import android.content.Context;
import timber.log.Timber;

/* JADX INFO: loaded from: classes.dex */
public class MemoryCrashHandler {
    public void initPostFabric(final Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { // from class: com.voxel.launcher3.MemoryCrashHandler.1
            private final Thread.UncaughtExceptionHandler mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(Thread thread, Throwable th) {
                Timber.d("crash detected", new Object[0]);
                if ((th instanceof OutOfMemoryError) || (th != null && (th.getCause() instanceof OutOfMemoryError))) {
                    Runtime runtime = Runtime.getRuntime();
                    long j = 1048576;
                    ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
                }
                this.mUncaughtExceptionHandler.uncaughtException(thread, th);
            }
        });
    }
}
