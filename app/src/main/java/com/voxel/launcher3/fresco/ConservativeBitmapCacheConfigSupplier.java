package com.voxel.launcher3.fresco;

import android.app.ActivityManager;
import com.facebook.common.internal.Supplier;
import com.facebook.imagepipeline.cache.DefaultBitmapMemoryCacheParamsSupplier;
import com.facebook.imagepipeline.cache.MemoryCacheParams;

/* JADX INFO: loaded from: classes.dex */
public class ConservativeBitmapCacheConfigSupplier implements Supplier<MemoryCacheParams> {
    private final ActivityManager mActivityManager;

    public ConservativeBitmapCacheConfigSupplier(ActivityManager activityManager) {
        this.mActivityManager = activityManager;
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.facebook.common.internal.Supplier
    public MemoryCacheParams get() {
        MemoryCacheParams memoryCacheParams = new DefaultBitmapMemoryCacheParamsSupplier(this.mActivityManager).get();
        int i = memoryCacheParams.maxCacheSize / 2;
        return new MemoryCacheParams(i > 15728640 ? 15728640 : i, memoryCacheParams.maxCacheEntries, memoryCacheParams.maxEvictionQueueSize, memoryCacheParams.maxEvictionQueueEntries, memoryCacheParams.maxCacheEntrySize);
    }
}
