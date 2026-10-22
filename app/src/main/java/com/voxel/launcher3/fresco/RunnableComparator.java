package com.voxel.launcher3.fresco;

import java.util.Comparator;

/* JADX INFO: loaded from: classes.dex */
public class RunnableComparator implements Comparator<Runnable> {
    @Override // java.util.Comparator
    public int compare(Runnable runnable, Runnable runnable2) {
        PriorityFutureTask priorityFutureTask = (PriorityFutureTask) runnable;
        PriorityFutureTask priorityFutureTask2 = (PriorityFutureTask) runnable2;
        int iOrdinal = priorityFutureTask2.priority.ordinal() - priorityFutureTask.priority.ordinal();
        if (iOrdinal != 0) {
            return iOrdinal;
        }
        if (priorityFutureTask.ts > priorityFutureTask2.ts) {
            return 1;
        }
        return priorityFutureTask.ts < priorityFutureTask2.ts ? -1 : 0;
    }

    @Override // java.util.Comparator
    public boolean equals(Object obj) {
        return obj instanceof RunnableComparator;
    }
}
