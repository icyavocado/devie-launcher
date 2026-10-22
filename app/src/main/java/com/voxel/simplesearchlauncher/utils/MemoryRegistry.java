package com.voxel.simplesearchlauncher.utils;

import com.facebook.common.memory.MemoryTrimType;
import com.facebook.common.memory.MemoryTrimmable;
import com.facebook.common.memory.MemoryTrimmableRegistry;
import java.util.Iterator;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: classes.dex */
public class MemoryRegistry implements MemoryTrimmableRegistry {
    private WeakHashMap<MemoryTrimmable, Boolean> mTrimmables = new WeakHashMap<>();

    @Override // com.facebook.common.memory.MemoryTrimmableRegistry
    public synchronized void registerMemoryTrimmable(MemoryTrimmable memoryTrimmable) {
        this.mTrimmables.put(memoryTrimmable, true);
    }

    @Override // com.facebook.common.memory.MemoryTrimmableRegistry
    public synchronized void unregisterMemoryTrimmable(MemoryTrimmable memoryTrimmable) {
        /* REWORK: the donor app never unregistered; fbcore's interface still has it. */
        this.mTrimmables.remove(memoryTrimmable);
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void trimMemory(MemoryTrimType memoryTrimType) {
        Iterator<MemoryTrimmable> it = this.mTrimmables.keySet().iterator();
        while (it.hasNext()) {
            it.next().trim(memoryTrimType);
        }
    }
}
