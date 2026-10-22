package com.voxel.simplesearchlauncher.notification.extractors;

import android.content.ComponentName;
import android.content.Context;

/* JADX INFO: loaded from: classes2.dex */
public class WhatsappExtractor extends WidgetExtractor {
    public WhatsappExtractor(Context context) {
        super(context);
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getConfigKey() {
        return "extractor.whatsapp";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public String getFieldName() {
        return "count";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.Extractor
    public String getPackageId() {
        return "com.whatsapp";
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    public ComponentName getWidgetComponent() {
        return new ComponentName(getPackageId(), "com.whatsapp.appwidget.WidgetProvider");
    }

    @Override // com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor
    protected void onWidgetRestored() {
        super.onWidgetRestored();
        this.mHostView.updateAppWidgetSize(null, 100, 80, 100, 80);
    }
}
