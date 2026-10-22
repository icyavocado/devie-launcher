package com.voxel.launcher3.util;

import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public abstract class StringFilter {

    /* JADX INFO: renamed from: com.voxel.launcher3.util.StringFilter$1, reason: invalid class name */
    static class AnonymousClass1 extends StringFilter {
        @Override // com.voxel.launcher3.util.StringFilter
        public boolean matches(String str) {
            return true;
        }
    }

    private StringFilter() {
    }

    /* synthetic */ StringFilter(AnonymousClass1 anonymousClass1) {
        this();
    }

    public static StringFilter of(final Set<String> set) {
        return new StringFilter() { // from class: com.voxel.launcher3.util.StringFilter.2

            @Override // com.voxel.launcher3.util.StringFilter
            public boolean matches(String str) {
                return set.contains(str);
            }
        };
    }

    public abstract boolean matches(String str);
}
