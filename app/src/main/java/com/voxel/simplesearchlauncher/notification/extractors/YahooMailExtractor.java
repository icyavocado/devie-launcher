package com.voxel.simplesearchlauncher.notification.extractors;

import android.content.ComponentName;
import android.content.Context;

/* JADX INFO: loaded from: classes2.dex */
public class YahooMailExtractor extends WidgetExtractor {
    public YahooMailExtractor(Context context) {
        super(context);
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getConfigKey() {
        return "extractor.yahoomail";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public String getFieldName() {
        return "new_message_badge";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getPackageId() {
        return "com.yahoo.mobile.client.android.mail";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public ComponentName getWidgetComponent() {
        return new ComponentName(getPackageId(), "com.yahoo.mail.appwidget.LauncherAppWidgetProvider");
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public boolean requiresConfig() {
        return true;
    }
}
