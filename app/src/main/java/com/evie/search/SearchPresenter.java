package com.evie.search;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent; /* REWORK: jadx dropped this import; the frecents builder builds launch intents */
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import com.evie.common.SearchSettings;
import com.evie.common.dragndrop.DragSink;
import com.evie.common.services.analytics.BaseAnalyticsService;
import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.evie.models.frequentlyused.data.FrequentlyUsedApp;
import com.evie.search.dagger.DaggerSearchComponent;
import com.evie.search.dagger.SearchComponent;
import com.evie.search.dagger.SearchModule;
import com.evie.search.local.ContactManager;
import com.evie.search.model.AppLink; /* REWORK: cross-package reference */
import com.evie.search.local.LocalAppsManager;
import com.evie.search.local.LocalSearchManager;
import com.evie.search.local.LocalSearchSubitem;
import com.evie.search.model.ActionData;
import com.evie.search.model.FrequentlyUsedItem;
import com.evie.search.model.LocalSearchItem;
import com.evie.search.model.RecentItem;
import com.evie.search.model.SearchPresenterData;
import com.evie.search.model.WebSearchItem;
import com.evie.search.recyclerview.presenter.BaseItemPresenter;
import com.evie.search.recyclerview.presenter.DelimiterPresenter;
import com.evie.search.recyclerview.presenter.FrequentlyUsedItemPresenter;
import com.evie.search.recyclerview.presenter.HeaderPresenter;
import com.evie.search.recyclerview.presenter.LocalItemPresenter;
import com.evie.search.recyclerview.presenter.LocalSubitemPresenter;
import com.evie.search.recyclerview.presenter.MessagePresenter;
import com.evie.search.recyclerview.presenter.RecentItemPresenter;
import com.evie.search.recyclerview.presenter.WebItemPresenter;
import com.evie.search.recyclerview.viewholder.CustomViewHolderFactory;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;
import com.ovacoda.devie.R;

/* REWORK: remote/web suggestion managers removed; web row kept as plain intent */
public class SearchPresenter {
    private SearchAdapter mAdapter;
    private AppAvailabilityProvider mAppAvailabilityProvider;
    private ContactManager mContactManager;
    private Context mContext;
    private FrequentlyUsedModel mFrequentlyUsed;
    private final int mFrequentlyUsedHeight;
    private final String mFrequentsHeader;
    private final int mItemHeight;
    private LocalAppsManager mLocalAppsManager;
    private final String mLocalHeader;
    private List<LocalSearchItem> mLocalItems;
    private int mLocalItemsShowing;
    private LocalSearchManager mLocalSearchManager;
    private Disposable mRecentDisposable;
    private List<BaseItemPresenter> mRecentPresenters;
    private final String mRecentsHeader;
    private SearchListExpandListener mSearchListExpandListener;
    private SearchQueryChangeListener mSearchQueryChangeListener;
    private SearchSettings mSearchSettings;
    SearchShortcutHandler mSearchShortcutHandler;
    private SearchStateChangeListener mSearchStateChangeListener;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferencesListener;
    private final int mUnavailableHeight;
    private final SearchComponent searchComponent;
    private int mListExpandCounter = 0;
    private int mAvailableHeight = 0;
    private String mCurrentQuery = "";
    private Disposable mCurrentDisposable = null;
    private boolean mWebSearchAllowed = false;
    private BaseItemPresenter mCustomPresenter = null;
    private Runnable mCustomRunnable = null;
    private final List<BaseItemPresenter> mLocalPresenters = new ArrayList();
    private final List<BaseItemPresenter> mCustomPresenters = new ArrayList();
    private final List<BaseItemPresenter> mWebPresenters = new ArrayList();

    /* renamed from: com.evie.search.SearchPresenter$3, reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$evie$search$model$SearchPresenterData$DataType = new int[SearchPresenterData.DataType.values().length];

        static {
            try {
                $SwitchMap$com$evie$search$model$SearchPresenterData$DataType[SearchPresenterData.DataType.LOCAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$evie$search$model$SearchPresenterData$DataType[SearchPresenterData.DataType.WEB.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$evie$search$model$SearchPresenterData$DataType[SearchPresenterData.DataType.CLEAR.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
        }
    }

    public interface SearchListExpandListener {
        void onSearchListExpand();
    }

    private enum SearchObserverState {
        STARTED(0),
        LOCAL_COMPLETE(1),
        WEB_COMPLETE(2);

        private final int value;

        SearchObserverState(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }

    public interface SearchQueryChangeListener {
        void onSearchQueryChange(String str);
    }

    public enum SearchState {
        IN_PROGRESS,
        IDLE,
        RECENT_CLICKED,
        COMPLETED
    }

    public interface SearchStateChangeListener {
        void onSearchStateChange(SearchState searchState);
    }

    public SearchPresenter(Context context, FrequentlyUsedModel frequentlyUsedModel, AppAvailabilityProvider appAvailabilityProvider) {
        Context applicationContext = context.getApplicationContext();
        this.mSearchSettings = SearchSettings.getInstance(applicationContext);
        this.mAdapter = new SearchAdapter();
        this.mFrequentlyUsed = frequentlyUsedModel;
        this.mAppAvailabilityProvider = appAvailabilityProvider;
        if (this.mAppAvailabilityProvider == null) {
            this.mAppAvailabilityProvider = new AppAvailabilityProvider.DefaultProvider();
        }
        this.searchComponent = DaggerSearchComponent.builder().searchModule(new SearchModule(applicationContext, new BaseAnalyticsService(), this.mAdapter)).build();
        this.searchComponent.inject(this);
        this.mContext = applicationContext;
        this.mSharedPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() { // from class: com.evie.search.-$$Lambda$SearchPresenter$fkUNgwFswgpz5_SYBlqwPQFpP-I
            @Override // android.content.SharedPreferences.OnSharedPreferenceChangeListener
            public final void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str2) {
                SearchPresenter.this.lambda$new$0( sharedPreferences, str2);
            }
        };
        applicationContext.getSharedPreferences("com.evie.search", 0).registerOnSharedPreferenceChangeListener(this.mSharedPreferencesListener);
        Resources resources = this.mContext.getResources();
        this.mFrequentsHeader = resources.getString(R.string.search_frequently_used_header);
        this.mRecentsHeader = resources.getString(R.string.search_recent_results_header);
        this.mLocalHeader = resources.getString(R.string.search_local_results_header);
        this.mFrequentlyUsedHeight = resources.getDimensionPixelSize(R.dimen.search_header_top_spacing) + resources.getDimensionPixelSize(R.dimen.search_header_text_size) + resources.getDimensionPixelSize(R.dimen.frequently_used_item_height) + resources.getDimensionPixelSize(R.dimen.search_delimiter_small_height);
        this.mUnavailableHeight = resources.getDimensionPixelSize(R.dimen.search_header_top_spacing) + resources.getDimensionPixelSize(R.dimen.search_header_text_size) + resources.getDimensionPixelSize(R.dimen.keyboard_estimate_height);
        this.mItemHeight = resources.getDimensionPixelSize(R.dimen.search_item_height);
        this.mLocalSearchManager = LocalSearchManager.getInstance(applicationContext, true);
        this.mLocalAppsManager = LocalAppsManager.getInstance(applicationContext);
        this.mContactManager = ContactManager.getInstance(applicationContext);
        setupRecentObservable(this.mSearchSettings.getIncludeFrequentlyUsed());
    }

    private List<BaseItemPresenter> generateLocalSubitems(LocalSearchItem localSearchItem) {
        ArrayList arrayList = new ArrayList();
        if (localSearchItem.getType().equals("CONTACT") && this.mContactManager.contactRecentlyUsed(localSearchItem)) {
            /* REWORK: the dex chain declared this helper as a checked-throwing
             * contact lookup; swallow + log like the launcher does elsewhere. */
            try {
            for (ActionData actionData : this.mContactManager.getContactData(Uri.parse(localSearchItem.getLink()).getSchemeSpecificPart()).getActions()) {
                switch (actionData.getActionType()) {
                    case "CALL":
                    case "TEXT":
                        arrayList.add(new LocalSubitemPresenter(this, this.mContext, generateSubitem(localSearchItem, actionData)));
                        break;
                }
            }
            } catch (Throwable e) {
                Timber.e(e);
            }
        }
        return arrayList;
    }

    private LocalSearchSubitem generateSubitem(LocalSearchItem localSearchItem, ActionData actionData) {
        AppLink next;
        String string2 = this.mContext.getSharedPreferences("action_default_items_pref", 0).getString(Uri.parse(localSearchItem.getLink()).getSchemeSpecificPart() + actionData.getActionType(), null);
        if (string2 == null) {
            next = null;
        } else {
            next = null;
            for (AppLink appLink : actionData.getLinks()) {
                if (string2.equals(appLink.getLink())) {
                    next = appLink;
                    break;
                }
            }
            if (next == null) {
                next = actionData.getLinks().get(0);
            }
        }
        if (next == null) {
            next = actionData.getLinks().get(0);
        }
        switch (actionData.getActionType()) {
            case "CALL":
                return new LocalSearchSubitem(localSearchItem, next.getLink(), actionData.getIconUrl(),
                        this.mContext.getResources().getString(R.string.contact_action_call_type, next.getSubtext()),
                        this.mContext.getResources().getString(R.string.contact_action_call_type, localSearchItem.getLabel()));
            case "TEXT":
                return new LocalSearchSubitem(localSearchItem, next.getLink(), actionData.getIconUrl(),
                        this.mContext.getResources().getString(R.string.contact_action_text_type, next.getSubtext()),
                        this.mContext.getResources().getString(R.string.contact_action_text_type, localSearchItem.getLabel()));
            default:
                return new LocalSearchSubitem(localSearchItem, next.getLink(), actionData.getIconUrl(), null, null);
        }
    }

    private Observable<SearchPresenterData> getSearchObservable(final String str) {
        /* REWORK: jadx dropped the type args on the RxJava chains; retype them so the
         * concatEager inference resolves. */
        Observable<SearchPresenterData> observableSubscribeOn = Observable.<SearchPresenterData>fromCallable(new Callable<SearchPresenterData>() { // from class: com.evie.search.-$$Lambda$SearchPresenter$xN3IZbuvzz0lQ5SI8ShlIXTOrM0
            @Override // java.util.concurrent.Callable
            public final SearchPresenterData call() throws Exception {
                return SearchPresenter.this.lambda$getSearchObservable$2( str);
            }

        }).onErrorReturnItem(new SearchPresenterData(new ArrayList(), SearchPresenterData.DataType.LOCAL)).subscribeOn(Schedulers.io());
        ArrayList arrayList = new ArrayList();
        arrayList.add(new WebItemPresenter(this, this.mContext, new WebSearchItem(true, str)));
        SearchPresenterData searchPresenterData = new SearchPresenterData(arrayList, SearchPresenterData.DataType.WEB);
        Observable<SearchPresenterData> observableJust = Observable.just(searchPresenterData);
        return Observable.concatEager(Observable.<Observable<SearchPresenterData>>just(observableSubscribeOn, observableJust)).mergeWith(Observable.just(new SearchPresenterData(null, SearchPresenterData.DataType.CLEAR)).delay(200L, TimeUnit.MILLISECONDS));
    }

    private DisposableObserver<SearchPresenterData> getSearchObserver(final String str) {
        return new DisposableObserver<SearchPresenterData>() { // from class: com.evie.search.SearchPresenter.1
            HashSet<String> packages;
            String searchQuery;
            SearchObserverState state;

            private void updatePresenters() {
                ArrayList arrayList = new ArrayList();
                arrayList.addAll(SearchPresenter.this.mLocalPresenters);
                arrayList.addAll(SearchPresenter.this.mCustomPresenters);
                arrayList.addAll(SearchPresenter.this.mWebPresenters);
                SearchPresenter.this.mAdapter.setPresenters(arrayList);
            }

            @Override // io.reactivex.Observer
            public void onComplete() {
                if (SearchPresenter.this.mSearchStateChangeListener != null) {
                    SearchPresenter.this.mSearchStateChangeListener.onSearchStateChange(SearchState.IDLE);
                }
            }

            @Override // io.reactivex.Observer
            public void onError(Throwable th) {
                Timber.e(th.getMessage(), new Object[0]);
            }

            @Override // io.reactivex.Observer
            public void onNext(SearchPresenterData searchPresenterData) {
                if (this.searchQuery.equals(SearchPresenter.this.mCurrentQuery)) {
                    List<BaseItemPresenter> data = searchPresenterData.getData();
                    switch (AnonymousClass3.$SwitchMap$com$evie$search$model$SearchPresenterData$DataType[searchPresenterData.getDataType().ordinal()]) {
                        case 1:
                            synchronized (SearchPresenter.this.mLocalPresenters) {
                                ArrayList arrayList = new ArrayList(SearchPresenter.this.mLocalPresenters);
                                SearchPresenter.this.mLocalPresenters.clear();
                                for (int i = 0; i < data.size(); i++) {
                                    BaseItemPresenter baseItemPresenter = data.get(i);
                                    if (baseItemPresenter instanceof LocalItemPresenter) {
                                        String appPackage = ((LocalItemPresenter) baseItemPresenter).getAppPackage();
                                        if (appPackage != null) {
                                            this.packages.add(appPackage);
                                        }
                                        int iIndexOf = arrayList.indexOf(baseItemPresenter);
                                        if (iIndexOf != -1) {
                                            baseItemPresenter = (BaseItemPresenter) arrayList.get(iIndexOf);
                                        }
                                    }
                                    SearchPresenter.this.mLocalPresenters.add(baseItemPresenter);
                                }
                                updatePresenters();
                                this.state = SearchObserverState.LOCAL_COMPLETE;
                                return;
                            }
                        case 2:
                            if (SearchPresenter.this.mCustomPresenter != null && SearchPresenter.this.mCustomPresenters.isEmpty()) {
                                SearchPresenter.this.mCustomPresenters.add(SearchPresenter.this.mCustomPresenter);
                                List list = SearchPresenter.this.mCustomPresenters;
                                SearchPresenter searchPresenter = SearchPresenter.this;
                                list.add(new DelimiterPresenter(searchPresenter, searchPresenter.mContext, false));
                            }
                            synchronized (SearchPresenter.this.mWebPresenters) {
                                SearchPresenter.this.mWebPresenters.clear();
                                SearchPresenter.this.mWebPresenters.addAll(data);
                                updatePresenters();
                                this.state = SearchObserverState.WEB_COMPLETE;
                                return;
                            }
                        case 3:
                            synchronized (SearchPresenter.this.mLocalPresenters) {
                                if (this.state.getValue() < SearchObserverState.LOCAL_COMPLETE.getValue()) {
                                    SearchPresenter.this.mLocalPresenters.clear();
                                }
                            }
                            synchronized (SearchPresenter.this.mWebPresenters) {
                                if (this.state.getValue() < SearchObserverState.WEB_COMPLETE.getValue()) {
                                    SearchPresenter.this.mWebPresenters.clear();
                                    SearchPresenter.this.mCustomPresenters.clear();
                                }
                            }
                            updatePresenters();
                            return;
                        default:
                            return;
                    }
                }
            }

            @Override // io.reactivex.observers.DisposableObserver
            protected void onStart() {
                if (SearchPresenter.this.mSearchStateChangeListener != null) {
                    SearchPresenter.this.mSearchStateChangeListener.onSearchStateChange(SearchState.IN_PROGRESS);
                }
                this.searchQuery = str;
                this.state = SearchObserverState.STARTED;
                this.packages = new HashSet<>();
            }
        };
    }

    public boolean isHiddenFromSearch(LocalSearchItem localSearchItem) {
        ComponentName componentName = localSearchItem.getComponentName();
        if (componentName == null) {
            return false;
        }
        return this.mAppAvailabilityProvider.isHiddenFromSearch(componentName.getPackageName(), componentName.getClassName());
    }

    public SearchPresenterData lambda$getSearchObservable$2(String str) throws Exception {
        /* REWORK: contacts can be granted mid-lifetime (while the launcher stays
           resumed, e.g. granted from system settings); re-sync on search so the
           first query after a grant sees contacts without a launcher restart. */
        this.mLocalSearchManager.initContactsIfNeeded(this.mContext);
        this.mLocalItems = this.mLocalSearchManager.search(str);
        LinkedList linkedList = new LinkedList();
        this.mLocalItemsShowing = Math.min(this.mLocalItems.size(), 3);
        if (!this.mLocalItems.isEmpty()) {
            float rank = this.mLocalItems.get(0).getRank() * 0.65f;
            for (int i = 1; i < this.mLocalItemsShowing; i++) {
                if (this.mLocalItems.get(i).getRank() < rank) {
                    this.mLocalItemsShowing = i;
                    break;
                }
            }
            boolean z = this.mLocalItemsShowing < this.mLocalItems.size();
            for (int i2 = 0; i2 < this.mLocalItemsShowing; i2++) {
                LocalSearchItem localSearchItem = this.mLocalItems.get(i2);
                if (!this.isHiddenFromSearch(localSearchItem)) {
                    linkedList.add(new LocalItemPresenter(this, this.mContext, localSearchItem));
                    linkedList.addAll(this.generateLocalSubitems(localSearchItem));
                }
            }
            if (linkedList.size() > 0) {
                linkedList.add(0, new HeaderPresenter(this.mContext, this.mLocalHeader));
                linkedList.add(new DelimiterPresenter(this, this.mContext, z));
            }
        }
        return new SearchPresenterData(linkedList, SearchPresenterData.DataType.LOCAL);
    }

    public void lambda$new$0(SharedPreferences sharedPreferences, String str) {
        if (str.equals("include_frequently_used")) {
            this.setAvailableHeight(this.mAvailableHeight);
            this.setupRecentObservable(sharedPreferences.getBoolean(str, true));
        }
    }

    public List lambda$setupRecentObservable$5(List list) throws Exception {
        ArrayList arrayList = new ArrayList();
        arrayList.add(new HeaderPresenter(this.mContext, this.mRecentsHeader));
        if (list.isEmpty()) {
            Resources resources = this.mContext.getResources();
            arrayList.add(new MessagePresenter(this.mContext, resources.getString(R.string.search_no_recents_title), this.mContext.checkCallingOrSelfPermission("android.permission.READ_CONTACTS") == 0 ? resources.getString(R.string.search_no_recents_message) : resources.getString(R.string.search_no_recents_no_contacts)));
            return arrayList;
        }
        for (Object obj : list) {
            RecentItem recentItem = (RecentItem) obj;
            ComponentName localComponentName = recentItem.getLocalComponentName();
            if (localComponentName == null || !this.mAppAvailabilityProvider.isHiddenFromBrowse(localComponentName.getPackageName(), localComponentName.getClassName())) {
                arrayList.add(new RecentItemPresenter(this, this.mContext, recentItem));
            }
        }
        return arrayList;
    }

    public List lambda$setupRecentObservable$6(List list) throws Exception {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (int i = 0; i < list.size() && arrayList2.size() < 5; i++) {
            FrequentlyUsedApp frequentlyUsedApp = (FrequentlyUsedApp) list.get(i);
            if (!this.mAppAvailabilityProvider.isHiddenFromBrowse(frequentlyUsedApp.packageName, frequentlyUsedApp.className)) {
                try {
                    this.mContext.getPackageManager().getApplicationInfo(frequentlyUsedApp.getPackageName(), 0);
                    Intent launchIntentForPackage;
                    if (frequentlyUsedApp.getClassName().equals("ANY")) {
                        launchIntentForPackage = this.mContext.getPackageManager().getLaunchIntentForPackage(frequentlyUsedApp.getPackageName());
                    } else {
                        Intent intent = new Intent("android.intent.action.MAIN");
                        intent.setComponent(frequentlyUsedApp.toComponentName());
                        launchIntentForPackage = intent;
                    }
                    launchIntentForPackage.addCategory("android.intent.category.LAUNCHER");
                    launchIntentForPackage.setFlags(268435456);
                    arrayList2.add(new FrequentlyUsedItem(launchIntentForPackage));
                } catch (Exception unused) {/* REWORK: multi-catch listed a subclass pair; collapse */
                }
            }
        }
        if (arrayList2.size() > 0) {
            arrayList.add(new HeaderPresenter(this.mContext, this.mFrequentsHeader));
            arrayList.add(new FrequentlyUsedItemPresenter(this, this.mContext, arrayList2));
            arrayList.add(new DelimiterPresenter(this, this.mContext, false));
        }
        return arrayList;
    }

    static /* synthetic */ List lambda$setupRecentObservable$7(List list, List list2) throws Exception {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(list);
        arrayList.addAll(list2);
        return arrayList;
    }

    private void setupRecentObservable(boolean z) {
        Disposable disposable = this.mRecentDisposable;
        if (disposable != null) {
            disposable.dispose();
        }
        Observable observableSubscribeOn = this.mLocalSearchManager.getRecentSubject().map(new Function() { // from class: com.evie.search.-$$Lambda$SearchPresenter$py1I5ycEvUsC4hnT4pLPF_T3znU
            @Override // io.reactivex.functions.Function
            public final Object apply(Object obj) throws Exception {
                return SearchPresenter.this.lambda$setupRecentObservable$5( (List) obj);
            }
        }).onErrorReturnItem(new ArrayList()).subscribeOn(Schedulers.computation());
        if (z && this.mFrequentlyUsed != null) {
            observableSubscribeOn = Observable.combineLatest(this.mFrequentlyUsed.getFrequentlyUsedApps().map(new Function() { // from class: com.evie.search.-$$Lambda$SearchPresenter$r6iRohv0hGoaGuzH_FtbQquHx2c
                @Override // io.reactivex.functions.Function
                public final Object apply(Object obj) throws Exception {
                    return SearchPresenter.this.lambda$setupRecentObservable$6( (List) obj);
                }
            }).onErrorReturnItem(new ArrayList()).subscribeOn(Schedulers.computation()), observableSubscribeOn, new BiFunction() { // from class: com.evie.search.-$$Lambda$SearchPresenter$rjemiD6vaXtBXDVPGTrU58QYpgA
                @Override // io.reactivex.functions.BiFunction
                public final Object apply(Object obj, Object obj2) throws Exception {
                    return SearchPresenter.lambda$setupRecentObservable$7((List) obj, (List) obj2);
                }
            });
        }
        observableSubscribeOn.observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<List<BaseItemPresenter>>() { // from class: com.evie.search.SearchPresenter.2
            @Override // io.reactivex.Observer
            public void onComplete() {
            }

            @Override // io.reactivex.Observer
            public void onError(Throwable th) {
                Timber.e(th.getMessage(), new Object[0]);
            }

            @Override // io.reactivex.Observer
            public void onNext(List<BaseItemPresenter> list) {
                SearchPresenter.this.mRecentPresenters = list;
                if (SearchPresenter.this.mCurrentQuery.trim().isEmpty()) {
                    SearchPresenter.this.mAdapter.setPresenters(list);
                }
            }

            @Override // io.reactivex.Observer
            public void onSubscribe(Disposable disposable2) {
                SearchPresenter.this.mRecentDisposable = disposable2;
            }
        });
    }

    public void changeSearchQuery(String str) {
        this.mSearchQueryChangeListener.onSearchQueryChange(str);
    }

    public void clearDragSink() {
        this.mSearchShortcutHandler.clearDragSink();
    }

    public void clearPresenters() {
        this.mLocalPresenters.clear();
        this.mCustomPresenters.clear();
        this.mWebPresenters.clear();
        this.mAdapter.clearPresenters();
    }

    public boolean clickFirstResult() {
        return this.mAdapter.clickFirstResult();
    }

    public void dispose() {
        if (this.mCurrentDisposable != null) {
            this.mCurrentDisposable.dispose();
            this.mCurrentDisposable = null;
        }
        if (this.mRecentDisposable != null) {
            this.mRecentDisposable.dispose();
            this.mRecentDisposable = null;
        }
    }

    public void expandLocalResults() {
        int size = this.mLocalItems.size();
        int i = this.mLocalItemsShowing;
        if (size > i + 4) {
            for (int i2 = 0; i2 < 4; i2++) {
                int size2 = this.mLocalPresenters.size() - 1;
                LocalSearchItem localSearchItem = this.mLocalItems.get(this.mLocalItemsShowing + i2);
                if (!isHiddenFromSearch(localSearchItem)) {
                    LocalItemPresenter localItemPresenter = new LocalItemPresenter(this, this.mContext, localSearchItem);
                    this.mLocalPresenters.add(size2, localItemPresenter);
                    this.mAdapter.insertPresenter(size2, localItemPresenter);
                }
            }
            this.mLocalItemsShowing += 4;
        } else {
            while (i < this.mLocalItems.size()) {
                int size3 = this.mLocalPresenters.size() - 1;
                LocalSearchItem localSearchItem2 = this.mLocalItems.get(i);
                if (!isHiddenFromSearch(localSearchItem2)) {
                    LocalItemPresenter localItemPresenter2 = new LocalItemPresenter(this, this.mContext, localSearchItem2);
                    this.mLocalPresenters.add(size3, localItemPresenter2);
                    this.mAdapter.insertPresenter(size3, localItemPresenter2);
                }
                i++;
            }
            this.mLocalItemsShowing = this.mLocalItems.size();
            this.mAdapter.changePresenter(this.mLocalPresenters.size() - 1, new DelimiterPresenter(this, this.mContext, false));
        }
        new Handler().postDelayed(new Runnable() { // from class: com.evie.search.-$$Lambda$SearchPresenter$wsGdhB-W7goufaa1Esp6yph-14I
            @Override // java.lang.Runnable
            public final void run() {
                SearchPresenter.this.mAdapter.notifyDataSetChanged();
            }
        }, 500L);
        if (this.mSearchListExpandListener != null) {
            this.mSearchListExpandListener.onSearchListExpand();
        }
    }

    public SearchAdapter getAdapter() {
        return this.mAdapter;
    }

    public void inject(FrequentlyUsedItemPresenter frequentlyUsedItemPresenter) {
        this.searchComponent.inject(frequentlyUsedItemPresenter);
    }

    public void inject(LocalItemPresenter localItemPresenter) {
        this.searchComponent.inject(localItemPresenter);
    }

    public void inject(LocalSubitemPresenter localSubitemPresenter) {
        this.searchComponent.inject(localSubitemPresenter);
    }

    public void inject(RecentItemPresenter recentItemPresenter) {
        this.searchComponent.inject(recentItemPresenter);
    }

    public void inject(WebItemPresenter webItemPresenter) {
        this.searchComponent.inject(webItemPresenter);
    }

    public void logFrequentlyUsedItemClicked(AnalyticsHandler.EventProp eventProp) {
        AnalyticsHandler.getInstance().logEvent("ev_item_tap_main_list", eventProp);
        SearchStateChangeListener searchStateChangeListener = this.mSearchStateChangeListener;
        if (searchStateChangeListener != null) {
            searchStateChangeListener.onSearchStateChange(SearchState.RECENT_CLICKED);
        }
    }

    public void logRecentItemClicked(AnalyticsHandler.EventProp eventProp) {
        AnalyticsHandler.getInstance().logEvent("ev_item_tap_recent_list", eventProp);
        SearchStateChangeListener searchStateChangeListener = this.mSearchStateChangeListener;
        if (searchStateChangeListener != null) {
            searchStateChangeListener.onSearchStateChange(SearchState.RECENT_CLICKED);
        }
    }

    public void logSearchItemClicked(AnalyticsHandler.EventProp eventProp) {
        AnalyticsHandler.getInstance().logEvent("ev_item_tap_main_list", eventProp.add("search_input", this.mCurrentQuery).add("search_input_length", this.mCurrentQuery.length()));
        SearchStateChangeListener searchStateChangeListener = this.mSearchStateChangeListener;
        if (searchStateChangeListener != null) {
            searchStateChangeListener.onSearchStateChange(SearchState.COMPLETED);
        }
    }

    public void logSearchItemDragged(AnalyticsHandler.EventProp eventProp) {
    }

    public void removeCustomPresenter() {
        Runnable runnable = this.mCustomRunnable;
        if (runnable != null) {
            runnable.run();
            this.mCustomRunnable = null;
        }
        if (this.mCustomPresenter != null) {
            int size = this.mLocalPresenters.size();
            for (int i = 0; i < this.mCustomPresenters.size(); i++) {
                this.mAdapter.removePresenter(size);
            }
            this.mCustomPresenters.clear();
            this.mCustomPresenter = null;
        }
        this.mAdapter.setCustomViewHolderFactory(null);
    }

    public void search(String str) {
        if (str == null) {
            str = "";
        }
        if (str.equals(this.mCurrentQuery)) {
            return;
        }
        this.mCurrentQuery = str;
        this.mListExpandCounter = 0;
        if (this.mCurrentDisposable != null && !this.mCurrentDisposable.isDisposed()) {
            this.mCurrentDisposable.dispose();
        }
        if (!str.trim().isEmpty()) {
            this.mCurrentDisposable = (Disposable) getSearchObservable(str).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.computation()).subscribeWith(getSearchObserver(str));
            return;
        }
        this.mAdapter.setPresenters(this.mRecentPresenters);
        this.mLocalPresenters.clear();
        this.mCustomPresenters.clear();
        this.mWebPresenters.clear();
        SearchStateChangeListener searchStateChangeListener = this.mSearchStateChangeListener;
        if (searchStateChangeListener != null) {
            searchStateChangeListener.onSearchStateChange(SearchState.IDLE);
        }
    }

    public void setAvailableHeight(int i) {
        this.mAvailableHeight = i;
        if (this.mSearchSettings.getIncludeFrequentlyUsed()) {
            i -= this.mFrequentlyUsedHeight;
        }
        this.mLocalSearchManager.setMaxRecents(Math.max((i - this.mUnavailableHeight) / this.mItemHeight, 3));
    }

    public void setCustomPresenter(BaseItemPresenter baseItemPresenter, CustomViewHolderFactory customViewHolderFactory, Runnable runnable) {
        if (baseItemPresenter == null) {
            return;
        }
        this.mCustomPresenter = baseItemPresenter;
        this.mCustomRunnable = runnable;
        this.mAdapter.setCustomViewHolderFactory(customViewHolderFactory);
    }

    public void setDragSink(DragSink dragSink) {
        this.mSearchShortcutHandler.setDragSink(dragSink);
    }

    public void setSearchListExpandListener(SearchListExpandListener searchListExpandListener) {
        this.mSearchListExpandListener = searchListExpandListener;
    }

    public void setSearchQueryChangeListener(SearchQueryChangeListener searchQueryChangeListener) {
        this.mSearchQueryChangeListener = searchQueryChangeListener;
    }

    public void setSearchStateChangeListener(SearchStateChangeListener searchStateChangeListener) {
        this.mSearchStateChangeListener = searchStateChangeListener;
    }

    public void setWebSearchAllowed(boolean z) {
        this.mWebSearchAllowed = z;
    }
}