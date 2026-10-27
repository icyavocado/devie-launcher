package com.voxel.simplesearchlauncher.settings.gestures;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.voxel.simplesearchlauncher.allapps.AllAppsListAdapter;
import com.voxel.simplesearchlauncher.allapps.AllAppsRecyclerView;
import com.voxel.simplesearchlauncher.allapps.AlphabeticalAppsSectionsHelper;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.itemdata.SearchItemData;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import com.ovacoda.devie.R;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

/* JADX INFO: loaded from: classes2.dex */
public class SelectAppFragment extends Fragment {
    private AllAppsListAdapter mAllAppsAdapter;

    AllAppsRecyclerView mAllAppsRecyclerView;
    private CompositeDisposable mDisposables;
    LocalAppsManager mLocalAppsManager;
    private BehaviorSubject<String> mQuerySubject = BehaviorSubject.createDefault("");
    View.OnClickListener mOnClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectAppFragment$U_tnNOdHVN6bLrL5Jc0opupYXt4
        @Override // android.view.View.OnClickListener
        public final void onClick(View view) {
            SelectAppFragment.this.lambda$new$0( view);
        }
    };

    /* JADX INFO: Access modifiers changed from: private */
    public List<LocalAppData> filterApps(String str, List<LocalAppData> list) {
        if (TextUtils.isEmpty(str)) {
            return list;
        }
        String[] strArrSplit = str.trim().split("\\s+");
        ArrayList arrayList = new ArrayList(list);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            String label = ((LocalAppData) it.next()).getLabel();
            if (TextUtils.isEmpty(label)) {
                it.remove();
            } else {
                String lowerCase = label.toLowerCase();
                for (String str2 : strArrSplit) {
                    if (!lowerCase.contains(str2)) {
                        it.remove();
                        break;
                    }
                }
            }
        }
        return arrayList;
    }

    public void lambda$new$0(View view) {
        SearchItemData searchItemData = (SearchItemData) view.getTag(R.id.all_app_item_tag);
        if (searchItemData instanceof LocalAppData) {
            LocalAppData localAppData = (LocalAppData) searchItemData;
            if (this.getTargetFragment() != null) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.LAUNCHER");
                intent.setClassName(localAppData.getPackageName(), localAppData.getActivityName());
                intent.setFlags(270532608);
                this.getTargetFragment().onActivityResult(this.getTargetRequestCode(), -1, intent);
            }
        }
    }

    public List lambda$onCreate$1() throws Exception {
        List<LocalAppData> allLocalLaunchApps = this.mLocalAppsManager.getAllLocalLaunchApps();
        Collections.sort(allLocalLaunchApps, new Comparator<LocalAppData>() { // from class: com.voxel.simplesearchlauncher.settings.gestures.SelectAppFragment.1
            final Collator mCollator = Collator.getInstance(Locale.getDefault());

            {
                this.mCollator.setStrength(0);
            }

            @Override // java.util.Comparator
            public int compare(LocalAppData localAppData, LocalAppData localAppData2) {
                return this.mCollator.compare(localAppData.getLabel(), localAppData2.getLabel());
            }
        });
        return allLocalLaunchApps;
    }

    public void lambda$onCreate$3(List list) throws Exception {
        this.mAllAppsAdapter.setResultListItems(list);
        this.mAllAppsAdapter.notifyDataSetChanged();
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        super.onAttach(context);
        AndroidSupportInjection.inject(this);
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mQuerySubject.onNext("");
        this.mDisposables = new CompositeDisposable();
        setHasOptionsMenu(true);
        this.mAllAppsAdapter = new AllAppsListAdapter(getContext(), new AlphabeticalAppsSectionsHelper(getContext()), this.mOnClickListener, null);
        this.mAllAppsAdapter.setLightMode(true);
        this.mDisposables.add(Observable.combineLatest(this.mQuerySubject.throttleFirst(200L, TimeUnit.MILLISECONDS), Observable.fromCallable(new Callable() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectAppFragment$p1jkYG1F1a0D7paM0NQ27vDxG6U
            @Override // java.util.concurrent.Callable
            public final Object call() throws Exception {
                return SelectAppFragment.this.lambda$onCreate$1();
            }
        }), new BiFunction() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$XaRnOwX-mvpiK6Ub0Hr3BfLinn0
            @Override // io.reactivex.functions.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return new Pair((String) obj, (List) obj2);
            }
        }).subscribeOn(Schedulers.computation()).map(new Function() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectAppFragment$T_nJkKcDt-cAqv3q7NtCiG1vXRE
            @Override // io.reactivex.functions.Function
            public final Object apply(Object obj) {
                Pair pair = (Pair) obj;
                return SelectAppFragment.this.filterApps((String) pair.first, (List) pair.second);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectAppFragment$ZZ8RGbuPD1qDp3ZTFU1Y9blsOuU
            @Override // io.reactivex.functions.Consumer
            public final void accept(Object obj) throws Exception {
                SelectAppFragment.this.lambda$onCreate$3( (List) obj);
            }
        }));
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.select_app_menu, menu);
        MenuItem menuItemFindItem = menu.findItem(R.id.search);
        SearchView searchView = menuItemFindItem != null ? (SearchView) menuItemFindItem.getActionView() : null;
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() { // from class: com.voxel.simplesearchlauncher.settings.gestures.SelectAppFragment.2
                @Override // androidx.appcompat.widget.SearchView.OnQueryTextListener
                public boolean onQueryTextChange(String str) {
                    SelectAppFragment.this.mQuerySubject.onNext(str);
                    return true;
                }

                @Override // androidx.appcompat.widget.SearchView.OnQueryTextListener
                public boolean onQueryTextSubmit(String str) {
                    return false;
                }
            });
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.gestures.-$$Lambda$SelectAppFragment$S1T_eXmF0E7tJHBO2JZhPdfQ3ug
                @Override // android.view.View.OnFocusChangeListener
                public final void onFocusChange(View view, boolean z) {
                    Timber.d("focus " + z, new Object[0]);
                }
            });
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_select_app_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        this.mDisposables.dispose();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    @Override // androidx.fragment.app.Fragment
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        return super.onOptionsItemSelected(menuItem);
    }

    @Override // androidx.fragment.app.Fragment
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        this.mAllAppsRecyclerView = (AllAppsRecyclerView) view.findViewById(R.id.recycler_view);
        this.mAllAppsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mAllAppsRecyclerView.setAdapter(this.mAllAppsAdapter);
    }
}
