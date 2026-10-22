package com.evie.models.frequentlyused;

import com.evie.models.frequentlyused.data.FrequentlyUsedApp;
import com.evie.models.frequentlyused.room.AppLaunchEntry;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public interface FrequentlyUsedModel extends Disposable {
    void addAppLaunchEntry(AppLaunchEntry appLaunchEntry);

    Observable<List<FrequentlyUsedApp>> getFrequentlyUsedApps();
}
