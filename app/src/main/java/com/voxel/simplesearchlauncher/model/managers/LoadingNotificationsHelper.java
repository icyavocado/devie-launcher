package com.voxel.simplesearchlauncher.model.managers;

import com.voxel.simplesearchlauncher.utils.NamedThreadFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes2.dex */
@Deprecated
public class LoadingNotificationsHelper {
    private static LocalAppsManager sLocalAppsManager;
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(1, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue(), new NamedThreadFactory(LoadingNotificationsHelper.class.getSimpleName()));
    private static List<StartupCompletedListener> startupCompletedListeners = new ArrayList();

    public interface StartupCompletedListener {
        void onStartupCompleted();
    }

    static /* synthetic */ void lambda$notifyStartupCompleted$0() {
        for (int size = startupCompletedListeners.size() - 1; size >= 0; size--) {
            startupCompletedListeners.get(size).onStartupCompleted();
        }
    }

    public static void notifyStartupCompleted() {
        if (startupCompletedListeners.size() > 0) {
            THREAD_POOL_EXECUTOR.execute(new Runnable() { // from class: com.voxel.simplesearchlauncher.model.managers.-$$Lambda$LoadingNotificationsHelper$YyvycySfvcswAGimxoaf3QO9GdE
                @Override // java.lang.Runnable
                public final void run() {
                    LoadingNotificationsHelper.lambda$notifyStartupCompleted$0();
                }
            });
        }
    }

    public static void registerStartupCompletedListener(StartupCompletedListener startupCompletedListener) {
        if (startupCompletedListener == null || startupCompletedListeners.contains(startupCompletedListener)) {
            return;
        }
        startupCompletedListeners.add(startupCompletedListener);
        LocalAppsManager localAppsManager = sLocalAppsManager;
        if (localAppsManager == null || !localAppsManager.isStartupCompleted()) {
            return;
        }
        startupCompletedListener.onStartupCompleted();
    }

    public static void setLocalAppsManager(LocalAppsManager localAppsManager) {
        sLocalAppsManager = localAppsManager;
    }
}
