package com.voxel.launcher3.util;

/* REWORK: android.util.MutableInt is a hidden-API class that some platform builds
 * strip; ship the same two-field utility locally. */
public final class MutableInt {
    public int value;

    public MutableInt(int value) {
        this.value = value;
    }
}
