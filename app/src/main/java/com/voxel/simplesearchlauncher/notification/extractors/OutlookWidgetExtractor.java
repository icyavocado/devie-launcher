package com.voxel.simplesearchlauncher.notification.extractors;

import android.content.ComponentName;
import android.content.Context;

/* JADX INFO: loaded from: classes2.dex */
public class OutlookWidgetExtractor extends WidgetExtractor {
    public OutlookWidgetExtractor(Context context) {
        super(context);
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getConfigKey() {
        return "extractor.outlook";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public String getFieldName() {
        return "inbox_unread_counter";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getPackageId() {
        return "com.microsoft.office.outlook";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public ComponentName getWidgetComponent() {
        return new ComponentName(getPackageId(), "com.acompli.acompli.InboxWidgetProvider");
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public boolean requiresConfig() {
        return true;
    }
}
