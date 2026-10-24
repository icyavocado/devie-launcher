package com.voxel.launcher3;

import android.content.Context;
import com.evie.models.frequentlyused.FrequentlyUsedAnalyticsFilter;
import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.dagger.LauncherAppComponent;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.AppsInfoIconFilter;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import javax.inject.Provider;

/* JADX INFO: loaded from: classes.dex */
/* REWORK(fresco): OkHttpClient + ConnectionPoolKiller fields are gone with the
 * drawee pipeline. */
public class FlavorInitializer {
    private final Context mContext;
    FrequentlyUsedModel mFrequentlyUsedModel;
    Provider<AppsInfoIconFilter> mIconFilterProvider;
    IconPackManager mIconPackManager;
    LauncherAppComponent mLauncherAppComponent;
    SettingsStorage mSettingsStorage;

    public FlavorInitializer(Context context) {
        this.mContext = context;
    }

    public void postDaggerInit() {
        LauncherApplication.getInjector(this.mContext).getLauncherAppComponent().inject(this);
    }

    public void preDaggerInit() {
        AnalyticsHandler.init(this.mContext, new FrequentlyUsedAnalyticsFilter(this.mFrequentlyUsedModel));
    }
}
