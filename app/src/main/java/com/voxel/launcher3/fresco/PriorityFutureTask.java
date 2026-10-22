package com.voxel.launcher3.fresco;

import com.facebook.imagepipeline.common.Priority;
import java.util.concurrent.FutureTask;

/* JADX INFO: loaded from: classes.dex */
public class PriorityFutureTask<V> extends FutureTask<V> {
    public final Priority priority;
    public final long ts;

    public PriorityFutureTask(Priority priority, long j, Runnable runnable, V v) {
        super(runnable, v);
        this.ts = j;
        this.priority = priority;
    }

    @Override // java.util.concurrent.FutureTask
    public String toString() {
        return this.priority.toString() + " " + this.ts;
    }
}
