package com.voxel.launcher3;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.os.TransactionTooLargeException;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class LauncherAppWidgetHost extends AppWidgetHost {
    private final ArrayList<Runnable> mProviderChangeListeners;

    public LauncherAppWidgetHost(Launcher launcher, int i) {
        super(launcher, i);
        this.mProviderChangeListeners = new ArrayList<>();
    }

    public void addProviderChangeListener(Runnable runnable) {
        this.mProviderChangeListeners.add(runnable);
    }

    @Override // android.appwidget.AppWidgetHost
    protected AppWidgetHostView onCreateView(Context context, int i, AppWidgetProviderInfo appWidgetProviderInfo) {
        return new LauncherAppWidgetHostView(context);
    }

    @Override // android.appwidget.AppWidgetHost
    protected void onProvidersChanged() {
        Iterator<Runnable> it = this.mProviderChangeListeners.iterator();
        while (it.hasNext()) {
            it.next().run();
        }
    }

    public void removeProviderChangeListener(Runnable runnable) {
        this.mProviderChangeListeners.remove(runnable);
    }

    @Override // android.appwidget.AppWidgetHost
    public void startListening() {
        try {
            super.startListening();
        } catch (Exception e) {
            if (!(e.getCause() instanceof TransactionTooLargeException)) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override // android.appwidget.AppWidgetHost
    public void stopListening() {
        super.stopListening();
        clearViews();
    }
}
