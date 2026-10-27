package com.voxel.simplesearchlauncher.search;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import android.app.Activity;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.widget.SearchView;
import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.evie.search.AppAvailabilityProvider;
import com.evie.search.SearchPresenter;
import com.evie.search.local.LocalSearchBroadcastReceiver;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherApplication;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.utils.Choreographer;
import com.voxel.simplesearchlauncher.view.PullDownRecyclerView;
import com.voxel.simplesearchlauncher.view.SpringySearchView;
/* JADX INFO: loaded from: classes2.dex */
public class SearchUiHelper {
    private final Activity mActivity;
    private final Handler mHandler;
    private final PullDownRecyclerView mPullDownRecyclerView;
    private final SpringySearchView mQsbSearchView;
    private final ScaleState mScaleState;
    private LocalSearchBroadcastReceiver mSearchBroadcastReceiver;
    private SearchPresenter mSearchPresenter;
    private SearchViewHolderFactory mSearchViewHolderFactory;
    /* REWORK: was an anonymous-listener private field (SearchUiHelper$3.currentState);
     * javac cannot reference an anonymous type, so the state lives here now. */
    private volatile SearchPresenter.SearchState mCurrentSearchState;
    private SharedPreferences mSharedPrefs;
    private Spring mSpringScale;
    private AnalyticsHandler mAnalyticsHandler = AnalyticsHandler.getInstance();
    private Handler mSearchDelayHandler = new Handler(Looper.getMainLooper());
    private Runnable mSearchRunnable = new Runnable() { // from class: com.voxel.simplesearchlauncher.search.SearchUiHelper.1
        @Override // java.lang.Runnable
        public void run() {
            SearchUiHelper.this.mSearchPresenter.search(SearchUiHelper.this.mSearchText);
        }
    };
    private String mSearchText = "";
    private boolean mDefaultLauncherPromptShowing = false;

    /* JADX INFO: renamed from: com.voxel.simplesearchlauncher.search.SearchUiHelper$5, reason: invalid class name */
    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$evie$search$SearchPresenter$SearchState = new int[SearchPresenter.SearchState.values().length];

        static {
            try {
                $SwitchMap$com$evie$search$SearchPresenter$SearchState[SearchPresenter.SearchState.IDLE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$evie$search$SearchPresenter$SearchState[SearchPresenter.SearchState.IN_PROGRESS.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$evie$search$SearchPresenter$SearchState[SearchPresenter.SearchState.RECENT_CLICKED.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$evie$search$SearchPresenter$SearchState[SearchPresenter.SearchState.COMPLETED.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public interface ScaleState {
        float getScale();

        View getScaledView();
    }

    public SearchUiHelper(Activity activity, SpringySearchView springySearchView, PullDownRecyclerView pullDownRecyclerView, FrequentlyUsedModel frequentlyUsedModel, AppAvailabilityProvider appAvailabilityProvider, ScaleState scaleState) {
        // REWORK: the decompiled component has no inject(SearchUiHelper) member; the
        // view-holder factory below is constructed directly instead.
        this.mActivity = activity;
        this.mHandler = new Handler(activity.getMainLooper());
        this.mQsbSearchView = springySearchView;
        this.mPullDownRecyclerView = pullDownRecyclerView;
        this.mScaleState = scaleState;
        this.mSearchPresenter = new SearchPresenter(activity, frequentlyUsedModel, appAvailabilityProvider);
        this.mSearchBroadcastReceiver = new LocalSearchBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        intentFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intentFilter.addDataScheme("package");
        /* REWORK: API 34+ requires an explicit exported state; NOT_EXPORTED is
           safe — filters are system or same-app broadcasts. */
        ContextCompat.registerReceiver(this.mActivity.getApplicationContext(), this.mSearchBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.evie.search.DECAY_USAGE_IF_NEEDED");
        ContextCompat.registerReceiver(this.mActivity.getApplicationContext(), this.mSearchBroadcastReceiver, intentFilter2, ContextCompat.RECEIVER_NOT_EXPORTED);
        this.mSharedPrefs = activity.getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0);
        this.mSearchPresenter.setSearchListExpandListener(new SearchPresenter.SearchListExpandListener() { // from class: com.voxel.simplesearchlauncher.search.-$$Lambda$SearchUiHelper$XlsRNcS_JijPGHJeVTZd6xSolrI
            @Override // com.evie.search.SearchPresenter.SearchListExpandListener
            public final void onSearchListExpand() {
                SearchUiHelper.this.lambda$new$0();
            }
        });
        this.mSearchPresenter.setSearchQueryChangeListener(new SearchPresenter.SearchQueryChangeListener() { // from class: com.voxel.simplesearchlauncher.search.-$$Lambda$SearchUiHelper$GMUydjc0gLllskp1cH6tRAxNU3g
            @Override // com.evie.search.SearchPresenter.SearchQueryChangeListener
            public final void onSearchQueryChange(String str) {
                SearchUiHelper.this.lambda$new$1( str);
            }
        });
        this.mPullDownRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.simplesearchlauncher.search.-$$Lambda$SearchUiHelper$z5AVREnUVW7qZwM-JlbBof3Jtrw
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                SearchUiHelper.this.lambda$new$2( view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mSpringScale = SpringSystem.create().createSpring();
        this.mSpringScale.setCurrentValue(1.0d);
        this.mSpringScale.setSpringConfig(new SpringConfig(300.0d, 40.0d));
        this.mSpringScale.addListener(new SpringListener() { // from class: com.voxel.simplesearchlauncher.search.SearchUiHelper.2
            private boolean mIsFirstUpdate = true;

            @Override // com.facebook.rebound.SpringListener
            public void onSpringActivate(Spring spring) {
            }

            @Override // com.facebook.rebound.SpringListener
            public void onSpringAtRest(Spring spring) {
                this.mIsFirstUpdate = true;
                Choreographer.getInstance().animationEnded();
            }

            @Override // com.facebook.rebound.SpringListener
            public void onSpringEndStateChange(Spring spring) {
            }

            @Override // com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                if (this.mIsFirstUpdate) {
                    this.mIsFirstUpdate = false;
                }
                SearchUiHelper.this.updateMainLayerScale();
            }
        });
        this.mPullDownRecyclerView.setAdapter(this.mSearchPresenter.getAdapter());
        this.mSearchPresenter.setSearchStateChangeListener(new SearchPresenter.SearchStateChangeListener() { // from class: com.voxel.simplesearchlauncher.search.SearchUiHelper.3
            private Runnable showSearchProgressRunnable = new Runnable() { // from class: com.voxel.simplesearchlauncher.search.SearchUiHelper.3.1
                @Override // java.lang.Runnable
                public void run() {
                    if (SearchUiHelper.this.mQsbSearchView == null || SearchUiHelper.this.mCurrentSearchState == null || SearchUiHelper.this.mCurrentSearchState != SearchPresenter.SearchState.IN_PROGRESS) {
                        return;
                    }
                    SearchUiHelper.this.mQsbSearchView.showSearchProgressSpinner();
                }
            };

            @Override // com.evie.search.SearchPresenter.SearchStateChangeListener
            public void onSearchStateChange(SearchPresenter.SearchState searchState) {
                SearchUiHelper.this.mCurrentSearchState = searchState;
                SearchUiHelper.this.mHandler.removeCallbacks(this.showSearchProgressRunnable);
                switch (AnonymousClass5.$SwitchMap$com$evie$search$SearchPresenter$SearchState[SearchUiHelper.this.mCurrentSearchState.ordinal()]) {
                    case 1:
                        Handler handler = SearchUiHelper.this.mHandler;
                        final SpringySearchView springySearchView2 = SearchUiHelper.this.mQsbSearchView;
                        springySearchView2.getClass();
                        handler.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.search.-$$Lambda$wsQYSbGBQsfU-VIBrM9U16OvEpY
                            @Override // java.lang.Runnable
                            public final void run() {
                                springySearchView2.hideSearchProgressSpinner();
                            }
                        });
                        break;
                    case 2:
                        SearchUiHelper.this.mHandler.postDelayed(this.showSearchProgressRunnable, 500L);
                        break;
                    case 3:
                        ((InputMethodManager) SearchUiHelper.this.mActivity.getApplicationContext().getSystemService("input_method")).hideSoftInputFromWindow(SearchUiHelper.this.mQsbSearchView.getWindowToken(), 0);
                        break;
                    case 4:
                        /* REWORK: the rate-us survey was cut (no Play Store); the
                         * completed-search counter only existed to trigger it. */
                        ((InputMethodManager) SearchUiHelper.this.mActivity.getApplicationContext().getSystemService("input_method")).hideSoftInputFromWindow(SearchUiHelper.this.mQsbSearchView.getWindowToken(), 0);
                        SearchUiHelper.this.mQsbSearchView.clearFocus();
                        if (!SearchUiHelper.this.mSharedPrefs.getBoolean("launcher.has_completed_search", false)) {
                            SearchUiHelper.this.mSharedPrefs.edit().putBoolean("launcher.has_completed_search", true).apply();
                        }
                        break;
                }
            }
        });
        this.mSearchViewHolderFactory = new SearchViewHolderFactory(activity, this.mSearchPresenter);
    }

    public void lambda$new$0() {
        if (this.mQsbSearchView != null) {
            ((InputMethodManager) this.mActivity.getApplicationContext().getSystemService("input_method")).hideSoftInputFromWindow(this.mQsbSearchView.getWindowToken(), 0);
            this.mQsbSearchView.clearFocus();
        }
    }

    public void lambda$new$1(String str) {
        this.updateSearchInput(str);
        this.mQsbSearchView.setQuery(str, false);
    }

    public void lambda$new$2(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i2 == i6 && i3 == i7 && i4 == i8) {
            return;
        }
        this.mSearchPresenter.setAvailableHeight(Math.abs(i4 - i2));
    }

    public void lambda$showSetLauncherSearchPromptIfNeeded$4() {
        this.mDefaultLauncherPromptShowing = false;
        this.mSharedPrefs.edit().putBoolean("launcher.has_completed_search_default_launcher_prompt", true).apply();
    }

    public void cleanup() {
        this.mSearchDelayHandler.removeCallbacks(this.mSearchRunnable);
        this.mSearchPresenter.setSearchListExpandListener(null);
        this.mSearchPresenter.setSearchStateChangeListener(null);
        this.mSearchPresenter.setSearchQueryChangeListener(null);
        this.mSearchPresenter.dispose();
        try {
            this.mActivity.getApplicationContext().unregisterReceiver(this.mSearchBroadcastReceiver);
        } catch (IllegalArgumentException unused) {
        }
    }

    public SearchView.OnQueryTextListener getQueryTextListener() {
        return new SearchView.OnQueryTextListener() { // from class: com.voxel.simplesearchlauncher.search.SearchUiHelper.4
            @Override // androidx.appcompat.widget.SearchView.OnQueryTextListener
            public boolean onQueryTextChange(String str) {
                Float endValueIfAvailable = SearchUiHelper.this.mQsbSearchView.getEndValueIfAvailable();
                if (str.isEmpty() && endValueIfAvailable != null && endValueIfAvailable.floatValue() == 1.0f) {
                    return true;
                }
                SearchUiHelper.this.updateSearchInput(str);
                return true;
            }

            @Override // androidx.appcompat.widget.SearchView.OnQueryTextListener
            public boolean onQueryTextSubmit(String str) {
                if (SearchUiHelper.this.mSearchText == null || SearchUiHelper.this.mSearchText.isEmpty()) {
                    return true;
                }
                if (SettingsStorage.getInstance().getSearchDoneButtonBehavior() != 1 || SearchUiHelper.this.mSearchPresenter.clickFirstResult()) {
                    ((InputMethodManager) SearchUiHelper.this.mActivity.getApplicationContext().getSystemService("input_method")).hideSoftInputFromWindow(SearchUiHelper.this.mQsbSearchView.getWindowToken(), 0);
                    SearchUiHelper.this.mQsbSearchView.clearFocus();
                }
                return true;
            }
        };
    }

    public SearchPresenter getSearchPresenter() {
        return this.mSearchPresenter;
    }

    public String getSearchText() {
        return this.mSearchText;
    }

    public boolean showSetLauncherSearchPromptIfNeeded() {
        if (this.mSharedPrefs.getBoolean("launcher.has_completed_search_default_launcher_prompt", false)) {
            if (this.mDefaultLauncherPromptShowing) {
                this.mDefaultLauncherPromptShowing = false;
                this.mSearchPresenter.removeCustomPresenter();
            }
        } else {
            if (!ResetLauncherActivity.isLauncherDefault(this.mActivity)) {
                this.mSearchPresenter.setCustomPresenter(new SearchDefaultLauncherPresenter(this.mActivity), this.mSearchViewHolderFactory, new Runnable() { // from class: com.voxel.simplesearchlauncher.search.-$$Lambda$SearchUiHelper$i26z_Gkt5ONzOEOP7HfM6T95GvY
                    @Override // java.lang.Runnable
                    public final void run() {
                        SearchUiHelper.this.lambda$showSetLauncherSearchPromptIfNeeded$4();
                    }
                });
                if (!this.mDefaultLauncherPromptShowing) {
                    this.mDefaultLauncherPromptShowing = true;
                }
                return true;
            }
            this.mSharedPrefs.edit().putBoolean("launcher.has_completed_search_default_launcher_prompt", true).apply();
        }
        return false;
    }

    public void updateMainLayerScale() {
        if (this.mScaleState.getScaledView() == null) {
            return;
        }
        float scale = this.mScaleState.getScale();
        Spring spring = this.mSpringScale;
        if (spring != null) {
            scale *= (float) spring.getCurrentValue();
        }
        View scaledView = this.mScaleState.getScaledView();
        scaledView.setScaleY(scale);
        scaledView.setScaleX(scale);
    }

    public boolean updateSearchInput(String str) {
        this.mSearchText = str.trim();
        this.mSearchDelayHandler.removeCallbacks(this.mSearchRunnable);
        if (this.mSearchText.isEmpty()) {
            this.mSearchDelayHandler.post(this.mSearchRunnable);
            return true;
        }
        this.mSearchDelayHandler.postDelayed(this.mSearchRunnable, 50L);
        return true;
    }
}
