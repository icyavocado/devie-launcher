package com.voxel.simplesearchlauncher.utils;

import java.lang.reflect.Array;

/* JADX INFO: loaded from: classes2.dex */
public final class HashUtil {
    private static int firstTerm(int i) {
        return i * 31;
    }

    public static int hash(int i, int i2) {
        return firstTerm(i) + i2;
    }

    public static int hash(int i, Object obj) {
        if (obj == null) {
            return hash(i, 0);
        }
        if (!isArray(obj)) {
            return hash(i, obj.hashCode());
        }
        int length = Array.getLength(obj);
        for (int i2 = 0; i2 < length; i2++) {
            Object obj2 = Array.get(obj, i2);
            if (obj2 != obj) {
                i = hash(i, obj2);
            }
        }
        return i;
    }

    private static boolean isArray(Object obj) {
        return obj.getClass().isArray();
    }
}
