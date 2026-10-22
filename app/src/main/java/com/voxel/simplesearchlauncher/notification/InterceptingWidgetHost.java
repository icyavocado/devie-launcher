package com.voxel.simplesearchlauncher.notification;

import timber.log.Timber;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import com.voxel.simplesearchlauncher.notification.extractors.WidgetExtractor;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: loaded from: classes2.dex */
public final class InterceptingWidgetHost extends AppWidgetHost {
    private static InterceptingWidgetHost sInstance;
    private boolean mIsListening;
    private Map<Integer, WidgetExtractor> mWidgetExtractors;

    public InterceptingWidgetHost(Context context, int i) {
        super(context, i);
        this.mWidgetExtractors = new HashMap();
    }

    public static synchronized void deleteInstanceHost() {
        if (sInstance != null) {
            sInstance.deleteHost();
            sInstance = null;
        }
    }

    public static synchronized InterceptingWidgetHost getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new InterceptingWidgetHost(context, 2013);
        }
        return sInstance;
    }

    public AppWidgetHostView createViewForExtractor(WidgetExtractor widgetExtractor, AppWidgetProviderInfo appWidgetProviderInfo) {
        this.mWidgetExtractors.put(Integer.valueOf(widgetExtractor.getWidgetId()), widgetExtractor);
        return super.createView(widgetExtractor.getContext(), widgetExtractor.getWidgetId(), appWidgetProviderInfo);
    }

    @Override // android.appwidget.AppWidgetHost
    public void deleteAppWidgetId(int i) {
        this.mWidgetExtractors.remove(Integer.valueOf(i));
        super.deleteAppWidgetId(i);
    }

    @Override // android.appwidget.AppWidgetHost
    protected AppWidgetHostView onCreateView(Context context, int i, AppWidgetProviderInfo appWidgetProviderInfo) {
        return new InterceptingWidgetHostView(context, this.mWidgetExtractors.get(Integer.valueOf(i)));
    }

    @Override // android.appwidget.AppWidgetHost
    protected void onProviderChanged(int i, AppWidgetProviderInfo appWidgetProviderInfo) {
        super.onProviderChanged(i, appWidgetProviderInfo);
        WidgetExtractor widgetExtractor = this.mWidgetExtractors.get(Integer.valueOf(i));
        if (widgetExtractor != null) {
            try {
                widgetExtractor.restoreWidget();
                widgetExtractor.notifyListener();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public synchronized void startListeningIfNeeded() {
        if (this.mIsListening) {
            return;
        }
        try {
            sInstance.startListening();
            this.mIsListening = true;
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
