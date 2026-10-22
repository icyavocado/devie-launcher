package com.voxel.simplesearchlauncher.dagger.module;

import android.content.Context;
import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.evie.models.frequentlyused.FrequentlyUsedModelImpl;
import com.evie.models.frequentlyused.room.AppLaunchDatabase;
import com.evie.models.frequentlyused.submodels.FrequentlyUsedLocalModel;

/* JADX INFO: loaded from: classes2.dex */
public class FrequentlyUsedModule {
    public FrequentlyUsedLocalModel providesFrequentlyUsedLocalModel(AppLaunchDatabase appLaunchDatabase) {
        return new FrequentlyUsedLocalModel(appLaunchDatabase.appLaunchDAO());
    }

    /* REWORK: cloud frequently-used cut — no retrofit/okhttp/analytics inputs. */
    public FrequentlyUsedModel providesFrequentlyUsedModel(Context context, FrequentlyUsedLocalModel frequentlyUsedLocalModel) {
        return new FrequentlyUsedModelImpl(context, frequentlyUsedLocalModel);
    }
}
