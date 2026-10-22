package com.voxel.simplesearchlauncher.allapps;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.evie.common.dragndrop.DragObjectAbstraction;
import com.evie.common.dragndrop.DragSource;
import com.evie.search.local.LocalSearchManager;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.request.ImageRequest;
import com.voxel.launcher3.BaseApplication;
import com.voxel.launcher3.CellParams;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherApplication;
import com.voxel.launcher3.fresco.HybridNetworkFetcher;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.adapter.ListSeparator;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.iconpack.IconPackManager;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.itemdata.SearchItemData;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.settings.LayoutSettingsManager;
import com.voxel.simplesearchlauncher.view.BadgeableAppIcon;
import com.voxel.simplesearchlauncher.view.FastScrollRecyclerView;
import com.voxel.simplesearchlauncher.view.FastScrollRecyclerViewItemDecoration;
import dagger.android.support.AndroidSupportInjection;
import com.ovacoda.devie.R;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class AllAppsFragment extends Fragment implements DragSource {
    private static final String TAG = "AllAppsFragment";
    private OnFragmentInteractionListener fragmentInteractionListener;
    LocalAppsManager localAppsManager;
    AnalyticsHandler mAnalyticsHandler;
    private HeaderElevationController mElevationController;
    private FastScrollRecyclerView mFastScrollRecyclerView;
    private AllAppsGridAdapter mGridAdapter;
    private ImageView mGridButton;
    private GridLayoutManager mGridLayoutManager;
    IconPackManager mIconPackManager;
    LayoutSettingsManager mLayoutSettingsManager;
    private LinearLayoutManager mLinearLayoutManager;
    private AllAppsListAdapter mListAdapter;
    private ImageView mListButton;
    private LocalSearchManager mLocalSearchManager;
    private ProgressBar mProgressBar;
    private boolean mQueuedRefresh;
    private RecyclerView mRecyclerView;
    private AllAppsRecyclerView mScrollBarRecyclerView;
    private boolean mIsStartupComplete = false;
    private boolean mShouldShowSetDefaultLauncherRow = false;
    private AsyncTask<Void, Void, RefreshResult> mRefreshAppListTask = null;
    private Runnable mResetPositionRunnable = new Runnable() { // from class: com.voxel.simplesearchlauncher.allapps.-$$Lambda$AllAppsFragment$K3wKEGZd7iWkUw0z6V764zcavnI
        @Override // java.lang.Runnable
        public final void run() {
            AllAppsFragment.this.resetRecyclerViewPosition();
        }
    };
    View.OnClickListener mOnClickListener = new AnonymousClass1();
    View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.2
        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            LocalAppData localAppData = (LocalAppData) view.getTag(R.id.all_app_item_tag);
            if (localAppData == null || AllAppsFragment.this.fragmentInteractionListener == null) {
                return false;
            }
            return AllAppsFragment.this.fragmentInteractionListener.onAllAppsLongPress(AllAppsFragment.this, view, localAppData);
        }
    };
    private final DataSetObserver mLayoutSettingsObserver = new DataSetObserver() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.3
        @Override // android.database.DataSetObserver
        public void onChanged() {
            AllAppsFragment.this.refreshLayout();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            onChanged();
        }
    };
    private final DataSetObserver mAppsManagerObserver = new DataSetObserver() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.4
        @Override // android.database.DataSetObserver
        public void onChanged() {
            AllAppsFragment.this.requestRefresh();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            onChanged();
        }
    };
    private final DataSetObserver mIconPackObserver = new DataSetObserver() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.5
        @Override // android.database.DataSetObserver
        public void onChanged() {
            AllAppsFragment.this.notifyAdapters();
        }

        @Override // android.database.DataSetObserver
        public void onInvalidated() {
            AllAppsFragment.this.notifyAdapters();
        }
    };
    private final BroadcastReceiver mCalendarDayChangeReceiver = new BroadcastReceiver() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.6
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            AllAppsFragment.this.notifyAdapters();
        }
    };

    /* JADX INFO: renamed from: com.voxel.simplesearchlauncher.allapps.AllAppsFragment$1, reason: invalid class name */
    class AnonymousClass1 implements View.OnClickListener {
        AnonymousClass1() {
        }

        static /* synthetic */ void lambda$onClick$0(LocalAppData localAppData, View view) {
            AppsInfoDataHelper appsInfoDataHelper;
            AppsInfoDataHelper.AppInfo app;
            if (localAppData.getHasOpened() || (app = (appsInfoDataHelper = new AppsInfoDataHelper(view.getContext())).getApp(localAppData.getAppPackageName(), localAppData.getAppActivityName())) == null) {
                return;
            }
            app.opened = true;
            appsInfoDataHelper.update(app);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(final View view) {
            FragmentActivity activity;
            SearchItemData searchItemData = (SearchItemData) view.getTag(R.id.all_app_item_tag);
            if (searchItemData == null || (activity = AllAppsFragment.this.getActivity()) == null) {
                return;
            }
            searchItemData.handleAction(SearchItemData.ActionType.CLICK, activity);
            if (searchItemData instanceof LocalAppData) {
                AllAppsFragment.this.mAnalyticsHandler.recordAppLaunch(activity, searchItemData.getIntent(activity), "all_apps");
                final LocalAppData localAppData = (LocalAppData) searchItemData;
                AllAppsFragment.this.mLocalSearchManager.recordAppAccess(localAppData.getAppPackageName(), localAppData.getAppActivityName(), localAppData.getLabel());
                view.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.allapps.-$$Lambda$AllAppsFragment$1$AQBwFJL8Sgh6TLanIaHjUFaz1Xg
                    @Override // java.lang.Runnable
                    public final void run() {
                        AllAppsFragment.AnonymousClass1.lambda$onClick$0(localAppData, view);
                    }
                });
            }
        }
    }

    /* JADX INFO: renamed from: com.voxel.simplesearchlauncher.allapps.AllAppsFragment$8, reason: invalid class name */
    class AnonymousClass8 extends AsyncTask<Void, Void, RefreshResult> {
        private List<LocalAppData> mLocalApps;

        AnonymousClass8() {
        }

        private HashMap<String, Integer> calculateMapIndex(List<SearchItemData> list, int i) {
            char upperCase;
            HashMap<String, Integer> map = new HashMap<>();
            map.put(String.valueOf('#'), Integer.valueOf(i));
            char c = 'A';
            while (i < list.size()) {
                SearchItemData searchItemData = list.get(i);
                if (!TextUtils.isEmpty(searchItemData.getLabel())) {
                    String strTrim = searchItemData.getLabel().trim();
                    if (!TextUtils.isEmpty(strTrim) && (upperCase = Character.toUpperCase(strTrim.charAt(0))) >= 'A' && upperCase <= 'Z') {
                        while (upperCase >= c) {
                            map.put(String.valueOf(c), Integer.valueOf(i));
                            c = (char) (c + 1);
                            if (c > 'Z') {
                                break;
                            }
                        }
                        if (c > 'Z') {
                            break;
                        }
                    }
                }
                i++;
            }
            if (c <= 'Z') {
                while (c <= 'Z') {
                    map.put(String.valueOf(c), Integer.valueOf(Math.max(list.size() - 1, 0)));
                    c = (char) (c + 1);
                }
            }
            return map;
        }

        static /* synthetic */ int lambda$doInBackground$0(LocalAppData localAppData, LocalAppData localAppData2) {
            long firstInstallTime = localAppData2.getFirstInstallTime() - localAppData.getFirstInstallTime();
            if (firstInstallTime < 0) {
                return -1;
            }
            return firstInstallTime > 0 ? 1 : 0;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public RefreshResult doInBackground(Void... voidArr) {
            this.mLocalApps = AllAppsFragment.this.localAppsManager.getAllLocalLaunchApps();
            AnonymousClass1 anonymousClass1 = null;
            if (this.mLocalApps.size() == 0) {
                Log.d(AllAppsFragment.TAG, "refreshAppList waiting for local apps load to complete");
                return null;
            }
            AllAppsFragment.this.mIsStartupComplete = true;
            Collections.sort(this.mLocalApps, new Comparator<LocalAppData>() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.8.1
                final Collator mCollator = Collator.getInstance(Locale.getDefault());

                {
                    this.mCollator.setStrength(0);
                }

                @Override // java.util.Comparator
                public int compare(LocalAppData localAppData, LocalAppData localAppData2) {
                    return this.mCollator.compare(localAppData.getLabel(), localAppData2.getLabel());
                }
            });
            long jCurrentTimeMillis = System.currentTimeMillis();
            ArrayList arrayList = new ArrayList();
            int spanCount = AllAppsFragment.this.mGridLayoutManager.getSpanCount() * 2;
            if (SettingsStorage.getInstance().getAllAppsShowNewApps()) {
                for (LocalAppData localAppData : this.mLocalApps) {
                    if (!BaseApplication.getApplication().getPackageName().equals(localAppData.getPackageName()) && !localAppData.getHasOpened()) {
                        long firstInstallTime = jCurrentTimeMillis - localAppData.getFirstInstallTime();
                        if (firstInstallTime < 21600000 && firstInstallTime > 0) {
                            arrayList.add(localAppData);
                        }
                    }
                }
                Collections.sort(arrayList, new Comparator() { // from class: com.voxel.simplesearchlauncher.allapps.-$$Lambda$AllAppsFragment$8$zAnz0sl-bPxKB4mfk2oe4nhCGI8
                    @Override // java.util.Comparator
                    public final int compare(Object obj, Object obj2) {
                        return AllAppsFragment.AnonymousClass8.lambda$doInBackground$0((LocalAppData) obj, (LocalAppData) obj2);
                    }
                });
            }
            ArrayList arrayList2 = new ArrayList();
            if (AllAppsFragment.this.mShouldShowSetDefaultLauncherRow) {
                arrayList2.add(new SetDefaultLauncherItem(AllAppsFragment.this, anonymousClass1));
                if (arrayList.size() == 0) {
                    arrayList2.add(new ListSeparator());
                }
            }
            if (arrayList.size() > 0) {
                arrayList2.add(AllAppsFragment.this.new NewSectionSeparator());
            }
            for (int i = 0; i < spanCount && i < arrayList.size(); i++) {
                arrayList2.add(arrayList.get(i));
            }
            if (arrayList.size() > 0) {
                arrayList2.add(new ListSeparator());
            }
            int size = arrayList2.size();
            arrayList2.addAll(this.mLocalApps);
            HashMap<String, Integer> mapCalculateMapIndex = calculateMapIndex(arrayList2, size);
            RefreshResult refreshResult = new RefreshResult(AllAppsFragment.this, anonymousClass1);
            refreshResult.mSearchItemDatas = arrayList2;
            refreshResult.mMapIndex = mapCalculateMapIndex;
            return refreshResult;
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(RefreshResult refreshResult) {
            synchronized (this) {
                AllAppsFragment.this.mRefreshAppListTask = null;
                if (AllAppsFragment.this.mQueuedRefresh) {
                    AllAppsFragment.this.mQueuedRefresh = false;
                    AllAppsFragment.this.refreshAppList();
                }
            }
            if (refreshResult == null || refreshResult.mSearchItemDatas == null) {
                return;
            }
            if (AllAppsFragment.this.mProgressBar != null) {
                AllAppsFragment.this.mProgressBar.setVisibility(8);
            }
            boolean resultListItems = AllAppsFragment.this.mListAdapter.setResultListItems(refreshResult.mSearchItemDatas);
            AllAppsFragment.this.mListAdapter.setMapIndex(refreshResult.mMapIndex);
            if (resultListItems) {
                AllAppsFragment.this.mListAdapter.notifyDataSetChanged();
            }
            boolean resultListItems2 = AllAppsFragment.this.mGridAdapter.setResultListItems(refreshResult.mSearchItemDatas);
            AllAppsFragment.this.mGridAdapter.setMapIndex(refreshResult.mMapIndex);
            if (resultListItems2) {
                AllAppsFragment.this.mGridAdapter.notifyDataSetChanged();
            }
            RecyclerView.LayoutManager layoutManager = AllAppsFragment.this.mRecyclerView.getLayoutManager();
            AllAppsFragment.this.prefetchIcons(this.mLocalApps, layoutManager instanceof LinearLayoutManager ? ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition() : 0);
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        BadgeableAppIcon mIcon;
        TextView mText;

        public AppViewHolder(View view) {
            super(view);
            this.mIcon = (BadgeableAppIcon) view.findViewById(R.id.list_item_icon);
            this.mText = (TextView) view.findViewById(R.id.text);
        }
    }

    public class NewSectionSeparator extends SearchItemData {
        public NewSectionSeparator() {
        }

        @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
        public String getItemId() {
            return "__list_header__";
        }

        @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
        public void handleAction(SearchItemData.ActionType actionType, Activity activity) {
        }
    }

    public interface OnFragmentInteractionListener {
        boolean canShowAllAppsSetDefaultPrompt();

        boolean onAllAppsDropCompleted(AllAppsFragment allAppsFragment, boolean z);

        boolean onAllAppsLongPress(AllAppsFragment allAppsFragment, View view, LocalAppData localAppData);
    }

    private class RefreshResult {
        HashMap<String, Integer> mMapIndex;
        List<SearchItemData> mSearchItemDatas;

        private RefreshResult() {
        }

        /* synthetic */ RefreshResult(AllAppsFragment allAppsFragment, AnonymousClass1 anonymousClass1) {
            this();
        }
    }

    private class SetDefaultLauncherItem extends SearchItemData {
        private SetDefaultLauncherItem() {
        }

        /* synthetic */ SetDefaultLauncherItem(AllAppsFragment allAppsFragment, AnonymousClass1 anonymousClass1) {
            this();
        }

        @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
        public String getItemId() {
            return "__setDefaultLauncherItem__";
        }

        @Override // com.voxel.simplesearchlauncher.model.itemdata.SearchItemData
        public void handleAction(SearchItemData.ActionType actionType, Activity activity) {
            ResetLauncherActivity.resetHome(AllAppsFragment.this.getActivity(), true, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean isValidIndex(int i, int i2) {
        return i >= 0 && i < i2;
    }

    public void lambda$onCreateView$1(View view) {
        SettingsStorage.getInstance().setAllAppsDisplayType("list");
        this.setListMode();
    }

    public void lambda$onCreateView$2(View view) {
        SettingsStorage.getInstance().setAllAppsDisplayType("grid");
        this.setGridMode();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyAdapters() {
        this.mGridAdapter.notifyDataSetChanged();
        this.mListAdapter.notifyDataSetChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void prefetchIcon(LocalAppData localAppData) {
        Fresco.getImagePipeline().prefetchToDiskCache(ImageRequest.fromUri(HybridNetworkFetcher.getMagicResourceUri(localAppData)), null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void prefetchIcons(final List<LocalAppData> list, final int i) {
        Log.d(TAG, "prefetchIcons");
        new AsyncTask<Void, Void, Void>() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.9
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                int i2;
                boolean z;
                int size = list.size();
                if (AllAppsFragment.this.isValidIndex(i, size)) {
                    AllAppsFragment.this.prefetchIcon((LocalAppData) list.get(i));
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                int i3 = 1;
                while (i2 < 200) {
                    int i4 = i + i3;
                    if (AllAppsFragment.this.isValidIndex(i4, size)) {
                        AllAppsFragment.this.prefetchIcon((LocalAppData) list.get(i4));
                        i2++;
                        z = true;
                    } else {
                        z = false;
                    }
                    int i5 = i - i3;
                    if (AllAppsFragment.this.isValidIndex(i5, size)) {
                        AllAppsFragment.this.prefetchIcon((LocalAppData) list.get(i5));
                        i2++;
                        z = true;
                    }
                    if (!z) {
                        break;
                    }
                    i3++;
                }
                Log.d(AllAppsFragment.TAG, "prefetched: " + i2);
                return null;
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshAppList() {
        Log.d(TAG, "refreshAppList");
        this.mQueuedRefresh = false;
        this.mRefreshAppListTask = new AnonymousClass8();
        this.mRefreshAppListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshLayout() {
        setCellParams(LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.ALLAPPS));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void requestRefresh() {
        synchronized (this) {
            if (this.mRefreshAppListTask == null) {
                refreshAppList();
            } else {
                this.mQueuedRefresh = true;
            }
        }
    }

    private boolean setActiveRecyclerView(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter;
        RecyclerView recyclerView2 = this.mRecyclerView;
        if (recyclerView2 == recyclerView) {
            return false;
        }
        RecyclerView.LayoutManager layoutManager = null;
        if (recyclerView2 != null) {
            adapter = recyclerView2.getAdapter();
            RecyclerView.LayoutManager layoutManager2 = this.mRecyclerView.getLayoutManager();
            this.mRecyclerView.setAdapter(null);
            this.mRecyclerView.setLayoutManager(null);
            this.mRecyclerView.setVisibility(8);
            this.mRecyclerView.removeOnScrollListener(this.mElevationController);
            layoutManager = layoutManager2;
        } else {
            adapter = null;
        }
        this.mRecyclerView = recyclerView;
        this.mRecyclerView.setLayoutManager(layoutManager);
        this.mRecyclerView.setAdapter(adapter);
        this.mRecyclerView.setVisibility(0);
        this.mRecyclerView.addOnScrollListener(this.mElevationController);
        return true;
    }

    private boolean setGridMode() {
        if (this.mRecyclerView.getLayoutManager() == this.mGridLayoutManager) {
            return false;
        }
        RecyclerView.LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        int iFindFirstVisibleItemPosition = layoutManager instanceof LinearLayoutManager ? ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition() : 0;
        this.mListButton.setImageResource(R.drawable.all_apps_list_button_icon);
        this.mGridButton.setImageResource(R.drawable.all_apps_grid_active);
        this.mRecyclerView.setLayoutManager(this.mGridLayoutManager);
        this.mRecyclerView.setAdapter(this.mGridAdapter);
        this.mRecyclerView.scrollToPosition(iFindFirstVisibleItemPosition);
        return true;
    }

    private boolean setLettersIndex() {
        return setActiveRecyclerView(this.mFastScrollRecyclerView);
    }

    private boolean setListMode() {
        if (this.mRecyclerView.getLayoutManager() == this.mLinearLayoutManager) {
            return false;
        }
        RecyclerView.LayoutManager layoutManager = this.mRecyclerView.getLayoutManager();
        int iFindFirstVisibleItemPosition = layoutManager instanceof LinearLayoutManager ? ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition() : 0;
        this.mListButton.setImageResource(R.drawable.all_apps_list_active);
        this.mGridButton.setImageResource(R.drawable.all_apps_grid_button_icon);
        this.mRecyclerView.setLayoutManager(this.mLinearLayoutManager);
        this.mRecyclerView.setAdapter(this.mListAdapter);
        this.mRecyclerView.scrollToPosition(iFindFirstVisibleItemPosition);
        return true;
    }

    private boolean setScrollBarIndex() {
        return setActiveRecyclerView(this.mScrollBarRecyclerView);
    }

    private boolean setShouldShowSetDefaultLauncherRow(boolean z) {
        if (this.mShouldShowSetDefaultLauncherRow == z) {
            return false;
        }
        this.mShouldShowSetDefaultLauncherRow = z;
        if (!this.mIsStartupComplete) {
            return false;
        }
        requestRefresh();
        return true;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 1.0f;
    }

    public RecyclerView getRecyclerView() {
        return this.mRecyclerView;
    }

    public boolean isNearIndexBar(int i, int i2) {
        RecyclerView recyclerView = this.mRecyclerView;
        FastScrollRecyclerView fastScrollRecyclerView = this.mFastScrollRecyclerView;
        if (recyclerView == fastScrollRecyclerView) {
            return fastScrollRecyclerView.isNearIndexBar(i, i2);
        }
        AllAppsRecyclerView allAppsRecyclerView = this.mScrollBarRecyclerView;
        if (recyclerView == allAppsRecyclerView) {
            return allAppsRecyclerView.getScrollBar().isNearThumb(i, i2);
        }
        throw new IllegalStateException();
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
        try {
            if (context instanceof OnFragmentInteractionListener) {
                this.fragmentInteractionListener = (OnFragmentInteractionListener) context;
            } else {
                if (!(getParentFragment() instanceof OnFragmentInteractionListener)) {
                    throw new ClassCastException();
                }
                this.fragmentInteractionListener = (OnFragmentInteractionListener) getParentFragment();
            }
        } catch (ClassCastException unused) {
            throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        Log.d(TAG, "onCreate");
        super.onCreate(bundle);
        AlphabeticalAppsSectionsHelper alphabeticalAppsSectionsHelper = new AlphabeticalAppsSectionsHelper(getContext());
        if (LauncherAppState.getInstance().getDynamicGrid() == null) {
            LauncherAppState.getInstance().initDynamicGrid(getActivity());
        }
        CellParams cellParamsForType = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.ALLAPPS);
        this.mGridLayoutManager = new GridLayoutManager(getActivity(), Math.round(cellParamsForType.numColumns));
        this.mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() { // from class: com.voxel.simplesearchlauncher.allapps.AllAppsFragment.7
            @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
            public int getSpanSize(int i) {
                return AllAppsFragment.this.mGridAdapter.getSpanSize(i);
            }
        });
        this.mLinearLayoutManager = new LinearLayoutManager(getActivity());
        this.mGridAdapter = new AllAppsGridAdapter(getContext(), alphabeticalAppsSectionsHelper, this.mGridLayoutManager, cellParamsForType, this.mOnClickListener, this.mOnLongClickListener);
        this.mListAdapter = new AllAppsListAdapter(getContext(), alphabeticalAppsSectionsHelper, this.mOnClickListener, this.mOnLongClickListener);
        this.localAppsManager.registerDataSetObserver(this.mAppsManagerObserver);
        this.mIconPackManager.registerDataSetObserver(this.mIconPackObserver);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(this.mCalendarDayChangeReceiver, new IntentFilter("com.evie.day_changed"));
        this.mLocalSearchManager = LocalSearchManager.getInstance(getContext().getApplicationContext(), true);
        this.mLayoutSettingsManager.registerDataSetObserver(this.mLayoutSettingsObserver);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Log.d(TAG, "onCreateView");
        View viewInflate = layoutInflater.inflate(R.layout.fragment_all_apps, viewGroup, false);
        this.mFastScrollRecyclerView = (FastScrollRecyclerView) viewInflate.findViewById(R.id.fastscroll_list);
        this.mFastScrollRecyclerView.addItemDecoration(new FastScrollRecyclerViewItemDecoration(getContext()));
        this.mScrollBarRecyclerView = (AllAppsRecyclerView) viewInflate.findViewById(R.id.scrollbar_list);
        this.mListButton = (ImageView) viewInflate.findViewById(R.id.list_button);
        this.mGridButton = (ImageView) viewInflate.findViewById(R.id.grid_button);
        View viewFindViewById = viewInflate.findViewById(R.id.top_fade);
        viewFindViewById.getBackground().setAlpha(0);
        ArrayList arrayList = new ArrayList();
        arrayList.add(viewFindViewById);
        this.mElevationController = new HeaderElevationController.ControllerV16(getContext(), arrayList);
        updateStyle();
        this.mListButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.allapps.-$$Lambda$AllAppsFragment$vzvTtXrAR9zUL3XNH_xPWx6qzHQ
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AllAppsFragment.this.lambda$onCreateView$1( view);
            }
        });
        this.mGridButton.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.allapps.-$$Lambda$AllAppsFragment$nmYQ6HyxHgaQ04L9yKTEjMq1MCs
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                AllAppsFragment.this.lambda$onCreateView$2( view);
            }
        });
        this.mProgressBar = (ProgressBar) viewInflate.findViewById(R.id.progress_bar);
        if (this.mIsStartupComplete) {
            this.mProgressBar.setVisibility(8);
        }
        return viewInflate;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        this.localAppsManager.unregisterDataSetObserver(this.mAppsManagerObserver);
        this.mIconPackManager.unregisterDataSetObserver(this.mIconPackObserver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this.mCalendarDayChangeReceiver);
        this.mLayoutSettingsManager.unregisterDataSetObserver(this.mLayoutSettingsObserver);
        super.onDestroy();
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onDropCompleted(View view, DragObjectAbstraction dragObjectAbstraction, boolean z, boolean z2) {
        if (z) {
            return;
        }
        if (!z2) {
            dragObjectAbstraction.setDeferDragViewCleanupPostAnimation(false);
        }
        OnFragmentInteractionListener onFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onAllAppsDropCompleted(this, z2);
        }
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onFlingToDeleteCompleted() {
    }

    public void onFocusChanged(Handler handler, boolean z) {
        if (z) {
            handler.removeCallbacks(this.mResetPositionRunnable);
        } else {
            handler.postDelayed(this.mResetPositionRunnable, 60000L);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        AsyncTask<Void, Void, RefreshResult> asyncTask = this.mRefreshAppListTask;
        if (asyncTask != null) {
            asyncTask.cancel(true);
            this.mRefreshAppListTask = null;
            this.mQueuedRefresh = false;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        updateStyle();
        OnFragmentInteractionListener onFragmentInteractionListener = this.fragmentInteractionListener;
        if ((onFragmentInteractionListener == null || !onFragmentInteractionListener.canShowAllAppsSetDefaultPrompt()) ? setShouldShowSetDefaultLauncherRow(false) : setShouldShowSetDefaultLauncherRow(!ResetLauncherActivity.isLauncherDefault(getActivity()))) {
            return;
        }
        requestRefresh();
    }

    public void resetRecyclerViewPosition() {
        this.mRecyclerView.getLayoutManager().scrollToPosition(0);
        this.mElevationController.reset();
    }

    public void setCellParams(CellParams cellParams) {
        this.mGridLayoutManager.setSpanCount(Math.round(cellParams.numColumns));
        this.mGridAdapter.recalculateRowIndexes();
        this.mGridAdapter.setCellParams(cellParams);
        resetRecyclerViewPosition();
    }

    public void setVisible(boolean z) {
        if (getView() != null) {
            float f = z ? 1.0f : 0.0f;
            View view = getView();
            view.animate().cancel();
            view.animate().alpha(f);
        }
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsFlingToDelete() {
        return false;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsRemoveDropTarget() {
        return false;
    }

    public void updateStyle() {
        boolean lettersIndex = SettingsStorage.getInstance().getAllAppsIndexType() == 0 ? setLettersIndex() | false : setScrollBarIndex() | false;
        String allAppsDisplayType = SettingsStorage.getInstance().getAllAppsDisplayType();
        if (allAppsDisplayType.equals("list")) {
            lettersIndex |= setListMode();
        } else if (allAppsDisplayType.equals("grid")) {
            lettersIndex |= setGridMode();
        }
        if (lettersIndex) {
            resetRecyclerViewPosition();
        }
    }
}
