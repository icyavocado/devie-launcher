package com.voxel.simplesearchlauncher.notification;

import timber.log.Timber;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.widget.RemoteViews;
import com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor;

/* JADX INFO: loaded from: classes2.dex */
public class InterceptingWidgetHostView extends AppWidgetHostView {
    WidgetExtractor mExtractor;

    public InterceptingWidgetHostView(Context context, WidgetExtractor widgetExtractor) {
        super(context);
        this.mExtractor = widgetExtractor;
    }

    @Override // android.appwidget.AppWidgetHostView
    public void updateAppWidget(RemoteViews remoteViews) {
        try {
            super.updateAppWidget(remoteViews);
            if (this.mExtractor != null) {
                this.mExtractor.notifyListener();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
