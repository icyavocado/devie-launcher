package com.voxel.simplesearchlauncher.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: loaded from: classes2.dex */
public class NamedThreadFactory implements ThreadFactory {
    private static final String DEFAULT_THREAD_PREFIX = "NamedThreadFactory";
    private final AtomicInteger mCount = new AtomicInteger(1);
    private final String mThreadNamePrefix;

    public NamedThreadFactory(String str) {
        this.mThreadNamePrefix = (str == null || str.length() == 0) ? DEFAULT_THREAD_PREFIX : str;
    }

    @Override // java.util.concurrent.ThreadFactory
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, this.mThreadNamePrefix + " #" + this.mCount.getAndIncrement());
    }
}
