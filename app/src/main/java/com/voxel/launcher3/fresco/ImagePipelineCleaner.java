package com.voxel.launcher3.fresco;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.model.RecentItem;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.request.ImageRequest;
import com.voxel.simplesearchlauncher.fragment.Executors;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class ImagePipelineCleaner implements Application.ActivityLifecycleCallbacks {
    private Runnable mCacheCleaner = new Runnable() { // from class: com.voxel.launcher3.fresco.ImagePipelineCleaner.1
        @Override // java.lang.Runnable
        public void run() {
            if (ImagePipelineCleaner.this.mLocalSearchManager == null) {
                return;
            }
            ImagePipelineCleaner.this.mImagePipeline.clearMemoryCaches();
            Iterator<RecentItem> it = ImagePipelineCleaner.this.mLocalSearchManager.getRecentItems().iterator();
            while (it.hasNext()) {
                ImagePipelineCleaner.this.mImagePipeline.prefetchToBitmapCache(ImageRequest.fromUri(it.next().getImageUrl()), null);
            }
        }
    };
    private ImagePipeline mImagePipeline = Fresco.getImagePipeline();
    private LocalSearchManager mLocalSearchManager;

    public ImagePipelineCleaner(LocalSearchManager localSearchManager) {
        this.mLocalSearchManager = localSearchManager;
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityDestroyed(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityPaused(Activity activity) {
        Executors.getMainThreadExecutor().execute(this.mCacheCleaner, 2000L);
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityResumed(Activity activity) {
        Executors.getMainThreadExecutor().removeCallback(this.mCacheCleaner);
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStarted(Activity activity) {
    }

    @Override // android.app.Application.ActivityLifecycleCallbacks
    public void onActivityStopped(Activity activity) {
    }
}
