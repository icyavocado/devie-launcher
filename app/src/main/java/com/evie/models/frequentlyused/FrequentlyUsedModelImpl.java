package com.evie.models.frequentlyused;

import android.content.Context;
import com.evie.models.frequentlyused.data.FrequentlyUsedApp;
import com.evie.models.frequentlyused.room.AppLaunchEntry;
import com.evie.models.frequentlyused.submodels.FrequentlyUsedDefaultsModel;
import com.evie.models.frequentlyused.submodels.FrequentlyUsedLocalModel;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class FrequentlyUsedModelImpl implements FrequentlyUsedModel {
    private List<FrequentlyUsedApp> mCachedResults;
    private final FrequentlyUsedDefaultsModel mDefaultsModel;
    private final FrequentlyUsedLocalModel mFrequentlyUsedLocalModel;
    private final Subject<List<FrequentlyUsedApp>> mSubject = BehaviorSubject.createDefault(Collections.emptyList());
    private CompositeDisposable mDisposables = new CompositeDisposable();

    /* REWORK: cloud frequently-used removed (remote predictions + analytics
       distinct-user id); local Room ranking merged over bundled defaults. */
    public FrequentlyUsedModelImpl(Context context, FrequentlyUsedLocalModel frequentlyUsedLocalModel) {
        this.mDefaultsModel = new FrequentlyUsedDefaultsModel(context);
        this.mFrequentlyUsedLocalModel = frequentlyUsedLocalModel;
        this.mCachedResults = new ArrayList();
        CompositeDisposable compositeDisposable = this.mDisposables;
        Observable observableCombineLatest = Observable.combineLatest(this.mFrequentlyUsedLocalModel.observeFrequentlyUsedApps(), this.mDefaultsModel.getDefaultFrequentlyUsedApps(), new BiFunction() { // from class: com.evie.models.frequentlyused.-$$Lambda$FrequentlyUsedModelImpl$ma3SG6_L7-EqGfM3QS5fwKqM1lo
            @Override // io.reactivex.functions.BiFunction
            public final Object apply(Object obj, Object obj2) throws Exception {
                return FrequentlyUsedModelImpl.this.lambda$new$0( (List) obj, (List) obj2);
            }
        });
        final Subject<List<FrequentlyUsedApp>> subject = this.mSubject;
        subject.getClass();
        compositeDisposable.add(observableCombineLatest.subscribe(new Consumer() { // from class: com.evie.models.frequentlyused.-$$Lambda$PNvbEWVB-1y8Aj6vOgtqg4Sa4oI
            @Override // io.reactivex.functions.Consumer
            public final void accept(Object obj) {
                subject.onNext((List) obj);
            }
        }));
    }

    public List lambda$new$0(List list, List list2) throws Exception {
        List mergedLocal = list;
        if (mergedLocal.isEmpty() && !this.mCachedResults.isEmpty()) {
            mergedLocal = this.mCachedResults;
        }
        ArrayList arrayList = new ArrayList(mergedLocal);
        ArrayList arrayList2 = new ArrayList(list2);
        ArrayList arrayList3 = new ArrayList(arrayList.size() + arrayList2.size());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2.remove((FrequentlyUsedApp) it.next());
        }
        arrayList3.addAll(arrayList);
        arrayList3.addAll(arrayList2);
        Iterator it2 = arrayList3.iterator();
        while (it2.hasNext()) {
            if (it2.next() == null) {
                it2.remove();
            }
        }
        this.mCachedResults = arrayList3;
        return arrayList3;
    }

    @Override // com.evie.models.frequentlyused.FrequentlyUsedModel
    public void addAppLaunchEntry(AppLaunchEntry appLaunchEntry) {
        this.mFrequentlyUsedLocalModel.addAppLaunchEntry(appLaunchEntry);
    }

    @Override // io.reactivex.disposables.Disposable
    public void dispose() {
        this.mDisposables.dispose();
        this.mSubject.onComplete();
    }

    @Override // com.evie.models.frequentlyused.FrequentlyUsedModel
    public Observable<List<FrequentlyUsedApp>> getFrequentlyUsedApps() {
        return this.mSubject;
    }

    @Override // io.reactivex.disposables.Disposable
    public boolean isDisposed() {
        return this.mDisposables.isDisposed();
    }
}