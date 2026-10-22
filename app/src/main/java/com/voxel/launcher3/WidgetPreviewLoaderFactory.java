package com.voxel.launcher3;

import android.content.Context;

/* JADX INFO: loaded from: classes.dex */
public class WidgetPreviewLoaderFactory {
    public static WidgetPreviewLoader createWidgetPreviewLoader(Context context, IconCache iconCache) {
        return new WidgetPreviewLoader(context, iconCache);
    }
}
