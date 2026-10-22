package com.voxel.launcher3.fresco;

import com.voxel.simplesearchlauncher.utils.NamedThreadFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes.dex */
public class PriorityThreadpoolExecutor extends ThreadPoolExecutor {
    private static final String DEFAULT_THREAD_PREFIX = "PriorityThreadpoolExecutor";

    public PriorityThreadpoolExecutor(int i, int i2, long j, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue, String str) {
        super(i, i2, j, timeUnit, blockingQueue, new NamedThreadFactory(str));
    }

    @Override // java.util.concurrent.AbstractExecutorService
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T t) {
        PriorityRunnable priorityRunnable = (PriorityRunnable) runnable;
        return new PriorityFutureTask(priorityRunnable.priority, priorityRunnable.ts, runnable, t);
    }

    @Override // java.util.concurrent.AbstractExecutorService, java.util.concurrent.ExecutorService
    public <T> Future<T> submit(Callable<T> callable) {
        throw new UnsupportedOperationException("Just runnables for now");
    }
}
