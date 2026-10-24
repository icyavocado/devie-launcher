package com.ovacoda.devie.images;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.evie.search.local.LocalSearchManager;
import com.evie.search.model.RecentItem;
import com.ovacoda.devie.images.IconLoader;
import com.voxel.launcher3.MainThreadExecutor;
import com.voxel.launcher3.MainThreadExecutor;

/**
 * REWORK(fresco): was ImagePipelineCleaner. Keeps the donor lifecycle
 * behavior: when the launcher pauses, drop the LRU and warm up the recent
 * items' icons so reopening search is instant.
 */
public class IconCacheCleaner implements Application.ActivityLifecycleCallbacks {
    private static final long CLEAN_DELAY_MS = 2000;

    private final LocalSearchManager mLocalSearchManager;
    private final MainThreadExecutor mMainThreadExecutor = MainThreadExecutor.getInstance();
    private final IconLoader mIconLoader = IconLoader.getInstance();
    private final Runnable mCacheCleaner;

    public IconCacheCleaner(LocalSearchManager localSearchManager) {
        this.mLocalSearchManager = localSearchManager;
        this.mCacheCleaner = new Runnable() {
            @Override
            public void run() {
                /* Port of donor ImagePipelineCleaner: clears memory caches and
                 * pre-decodes the recents rows' icon URLs so the next search
                 * bind is a cache hit. */
                if (mLocalSearchManager == null) {
                    return;
                }
                mIconLoader.clearMemory();
                for (RecentItem recentItem : mLocalSearchManager.getRecentItems()) {
                    mIconLoader.prefetch(recentItem.getImageUrl());
                }
            }
        };
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        mMainThreadExecutor.execute(mCacheCleaner, CLEAN_DELAY_MS);
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        mMainThreadExecutor.removeCallback(mCacheCleaner);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}