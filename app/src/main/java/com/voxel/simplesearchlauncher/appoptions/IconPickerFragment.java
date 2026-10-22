package com.voxel.simplesearchlauncher.appoptions;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import butterknife.ButterKnife;
import com.evie.common.iconpack.IconPackFilter;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.Utilities;
import com.voxel.recyclerview.widget.GridLayoutManager;
import com.voxel.recyclerview.widget.RecyclerView;
import com.voxel.recyclerview.widget.SpringRecyclerView;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.voxel.simplesearchlauncher.iconpack.IconPack;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import com.ovacoda.devie.R;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: classes2.dex */
public class IconPickerFragment extends LauncherStackableFragment {
    private CompositeDisposable mCompositeDisposable;
    private IconPack mIconPack;
    private IconPackFilter mIconPackFilter;
    private IconPickerAdapter mIconPickerAdapter;

    View mLoadingIndicator;
    private BehaviorSubject<String> mQuerySubject = BehaviorSubject.create();

    SpringRecyclerView mRecyclerView;

    View mSearchBar;

    EditText mSearchEditText;

    View mTitleBar;

    private class BitmapWorkerTask extends AsyncTask<String, Void, Drawable> {
        private String mResourceName;
        private final WeakReference<IconPickerAdapter.IconViewHolder> mViewHolderWeakReference;

        public BitmapWorkerTask(IconPickerAdapter.IconViewHolder iconViewHolder) {
            this.mViewHolderWeakReference = new WeakReference<>(iconViewHolder);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Drawable doInBackground(String... strArr) {
            this.mResourceName = strArr[0];
            return IconPickerFragment.this.mIconPackFilter.loadDrawable(this.mResourceName);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Drawable drawable) {
            WeakReference<IconPickerAdapter.IconViewHolder> weakReference;
            IconPickerAdapter.IconViewHolder iconViewHolder;
            if (isCancelled() || (weakReference = this.mViewHolderWeakReference) == null || drawable == null || (iconViewHolder = weakReference.get()) == null || this != iconViewHolder.getBitmapWorkerTask() || iconViewHolder == null) {
                return;
            }
            TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{new ColorDrawable(0), drawable});
            transitionDrawable.setCrossFadeEnabled(true);
            iconViewHolder.mIcon.setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(100);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class IconPickerAdapter extends RecyclerView.Adapter implements SpringRecyclerView.SpringItemPositioner {
        private int mItemHeight;
        private List<String> mList;
        private View.OnClickListener mOnClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$IconPickerAdapter$B4wLRBL8M-9rtpPV6YUKkvu2MiQ
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                IconPickerAdapter.this.lambda$new$0( view);
            }
        };

        class IconViewHolder extends RecyclerView.ViewHolder {
            WeakReference<BitmapWorkerTask> mBitmapWorkerTaskWeakReference;
            ImageView mIcon;

            IconViewHolder(View view) {
                super(view);
                this.mIcon = (ImageView) view.findViewById(R.id.icon);
            }

            BitmapWorkerTask getBitmapWorkerTask() {
                WeakReference<BitmapWorkerTask> weakReference = this.mBitmapWorkerTaskWeakReference;
                if (weakReference == null) {
                    return null;
                }
                return weakReference.get();
            }
        }

        IconPickerAdapter(List<String> list) {
            this.mList = list;
        }

        public void lambda$new$0(View view) {
            TransitionDrawable transitionDrawable = (TransitionDrawable) ((ImageView) view).getDrawable();
            if (transitionDrawable == null || transitionDrawable.getDrawable(1) == null) {
                return;
            }
            Bitmap bitmapCreateIconBitmap = Utilities.createIconBitmap(transitionDrawable.getDrawable(1), IconPickerFragment.this.getContext());
            Intent intent = new Intent();
            intent.putExtra("ICON", bitmapCreateIconBitmap);
            IconPickerFragment.this.setResultCode(-1);
            IconPickerFragment.this.setResultIntent(intent);
            IconPickerFragment.this.runExitAnimation();
        }

        public void lambda$onBindViewHolder$1(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            int iAbs = Math.abs(i4 - i2);
            if (this.mItemHeight != iAbs) {
                this.mItemHeight = iAbs;
                IconPickerFragment.this.mRecyclerView.resetOverscrollPositions();
            }
        }

        public boolean cancelPotentialWork(IconViewHolder iconViewHolder, String str) {
            BitmapWorkerTask bitmapWorkerTask = iconViewHolder.getBitmapWorkerTask();
            if (bitmapWorkerTask != null) {
                String str2 = bitmapWorkerTask.mResourceName;
                if (str2 != null && str2.equals(str)) {
                    return false;
                }
                bitmapWorkerTask.cancel(true);
            }
            return true;
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.mList.size();
        }

        @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
        public int getItemViewHeightForPosition(int i) {
            int spanCount = ((GridLayoutManager) IconPickerFragment.this.mRecyclerView.getLayoutManager()).getSpanCount();
            int i2 = i % spanCount;
            int i3 = i / spanCount;
            if (i2 == 0) {
                return this.mItemHeight;
            }
            return 0;
        }

        @Override // com.voxel.recyclerview.widget.SpringRecyclerView.SpringItemPositioner
        public boolean hasUniformViewHeight() {
            return false;
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            IconViewHolder iconViewHolder = (IconViewHolder) viewHolder;
            String str = this.mList.get(i);
            if (cancelPotentialWork(iconViewHolder, str)) {
                iconViewHolder.mIcon.setImageDrawable(null);
                BitmapWorkerTask bitmapWorkerTask = IconPickerFragment.this.new BitmapWorkerTask(iconViewHolder);
                iconViewHolder.mBitmapWorkerTaskWeakReference = new WeakReference<>(bitmapWorkerTask);
                bitmapWorkerTask.execute(str);
            }
            if (this.mItemHeight == 0) {
                iconViewHolder.mIcon.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$IconPickerAdapter$HMxUkRputG4HscXUMYzrT66SVBA
                    @Override // android.view.View.OnLayoutChangeListener
                    public final void onLayoutChange(View view, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
                        IconPickerAdapter.this.lambda$onBindViewHolder$1( view, i2, i3, i4, i5, i6, i7, i8, i9);
                    }
                });
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.Adapter
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            IconViewHolder iconViewHolder = new IconViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.icon_picker_item, viewGroup, false));
            iconViewHolder.mIcon.setOnClickListener(this.mOnClickListener);
            return iconViewHolder;
        }
    }

    static /* synthetic */ void lambda$onViewCreated$0(View view) {
    }

    public void lambda$onViewCreated$1(View view, boolean z) {
        if (z || !TextUtils.isEmpty(this.mSearchEditText.getText())) {
            return;
        }
        this.hideSearchBar();
    }

    public void lambda$onViewCreated$4() throws Exception {
        this.mLoadingIndicator.animate().alpha(0.0f).withLayer().withEndAction(new Runnable() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$EKaChV65193Wpf64dPpAJs8UpHA
            @Override // java.lang.Runnable
            public final void run() {
                IconPickerFragment.this.mLoadingIndicator.setVisibility(8);
            }
        });
        if (this.getActivity() != null) {
            this.setupIconAdapter();
        }
    }

    public void lambda$setupIconAdapter$6(List list) throws Exception {
        this.mIconPickerAdapter.mList = list;
        this.mIconPickerAdapter.notifyDataSetChanged();
    }

    public static IconPickerFragment newInstance(IconPack iconPack, IconPackFilter iconPackFilter) {
        IconPickerFragment iconPickerFragment = new IconPickerFragment();
        iconPickerFragment.mIconPack = iconPack;
        iconPickerFragment.mIconPackFilter = iconPackFilter;
        return iconPickerFragment;
    }

    private void setupIconAdapter() {
        this.mIconPickerAdapter = new IconPickerAdapter(this.mIconPackFilter.getDrawableList());
        this.mRecyclerView.setAdapter(this.mIconPickerAdapter);
        this.mCompositeDisposable.add(this.mQuerySubject.subscribeOn(Schedulers.computation()).debounce(200L, TimeUnit.MILLISECONDS).map(new Function() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$R_4GRMtIDCoFwjuKDqMc8G0mgd4
            @Override // io.reactivex.functions.Function
            public final Object apply(Object obj) {
                return IconSearchHelper.queryList((String) obj, IconPickerFragment.this.mIconPackFilter.getDrawableList());
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$rUjeY5agD8zghaBtcATPyISsIQ0
            @Override // io.reactivex.functions.Consumer
            public final void accept(Object obj) throws Exception {
                IconPickerFragment.this.lambda$setupIconAdapter$6( (List) obj);
            }
        }));
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public boolean canSaveInstanceState() {
        return false;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void focusFragment(boolean z) {
    }

    public void hideSearchBar() {
        this.mSearchEditText.clearFocus();
        ((InputMethodManager) getContext().getSystemService("input_method")).hideSoftInputFromWindow(this.mSearchEditText.getWindowToken(), 0);
        this.mTitleBar.setVisibility(0);
        this.mTitleBar.clearAnimation();
        this.mTitleBar.animate().alpha(1.0f).withEndAction(null);
        this.mSearchBar.clearAnimation();
        this.mSearchBar.animate().alpha(0.0f).withEndAction(new Runnable() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$h7juJPIe9_Vn0O3KuYzaYgM9vVQ
            @Override // java.lang.Runnable
            public final void run() {
                IconPickerFragment.this.mSearchBar.setVisibility(4);
            }
        });
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        CompositeDisposable compositeDisposable = this.mCompositeDisposable;
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        this.mCompositeDisposable = new CompositeDisposable();
        super.onCreate(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.icon_picker_fragment, viewGroup, false);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment, androidx.fragment.app.Fragment
    public void onDestroy() {
        CompositeDisposable compositeDisposable = this.mCompositeDisposable;
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            this.mCompositeDisposable = null;
        }
        super.onDestroy();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z) {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPushLauncherStackedAppearance(LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
    }

    public void onSearchButtonClick() {
        showSearchBar();
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        ButterKnife.bind(this, view);
        view.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$xPhg3e3nroTKVg5JBZJOLC5wJj0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                IconPickerFragment.lambda$onViewCreated$0(view2);
            }
        });
        view.setAlpha(0.0f);
        view.animate().alpha(1.0f).withLayer();
        View viewFindViewById = view.findViewById(R.id.list_item);
        ImageView imageView = (ImageView) viewFindViewById.findViewById(R.id.icon);
        TextView textView = (TextView) viewFindViewById.findViewById(R.id.text);
        TextView textView2 = (TextView) viewFindViewById.findViewById(R.id.text2);
        imageView.setImageDrawable(this.mIconPack.getIcon());
        textView.setText(this.mIconPack.getLabel());
        textView2.setText(getContext().getString(R.string.change_icon_icon_count, Integer.valueOf(this.mIconPackFilter.getDrawableList().size())));
        this.mSearchBar.setAlpha(0.0f);
        this.mSearchEditText.setTextColor(-16777216);
        this.mSearchEditText.setHintTextColor(-7829368);
        this.mSearchEditText.setHint(R.string.search_bar_hint);
        this.mSearchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$7yXZ_yysD8ptmObvGPWNDfI4kQw
            @Override // android.view.View.OnFocusChangeListener
            public final void onFocusChange(View view2, boolean z) {
                IconPickerFragment.this.lambda$onViewCreated$1( view2, z);
            }
        });
        this.mSearchEditText.addTextChangedListener(new TextWatcher() { // from class: com.voxel.simplesearchlauncher.appoptions.IconPickerFragment.1
            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                IconPickerFragment.this.mQuerySubject.onNext(editable.toString());
            }

            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }
        });
        this.mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), (int) LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().numBaseColumns));
        this.mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() { // from class: com.voxel.simplesearchlauncher.appoptions.IconPickerFragment.2
            @Override // com.voxel.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (IconPickerFragment.this.mSearchEditText.hasFocus()) {
                    IconPickerFragment.this.mSearchEditText.clearFocus();
                    ((InputMethodManager) IconPickerFragment.this.getContext().getSystemService("input_method")).hideSoftInputFromWindow(IconPickerFragment.this.mSearchEditText.getWindowToken(), 0);
                }
            }
        });
        if (!this.mIconPackFilter.isDrawableListVerified()) {
            this.mCompositeDisposable.add(Completable.fromRunnable(new Runnable() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$AxOVVVfvLyvM2JQdcJYcFKRV5T4
                @Override // java.lang.Runnable
                public final void run() {
                    IconPickerFragment.this.mIconPackFilter.verifyDrawableList();
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$uyhvlPOwBCnHTNogks1umvx6bcw
                @Override // io.reactivex.functions.Action
                public final void run() throws Exception {
                    IconPickerFragment.this.lambda$onViewCreated$4();
                }
            }));
            return;
        }
        this.mLoadingIndicator.setVisibility(8);
        if (getActivity() != null) {
            setupIconAdapter();
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public boolean requiresFullScreen() {
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void runExitAnimation() {
        notifyFragmentWillRequestClose();
        notifyFragmentRequestClose();
    }

    public void showSearchBar() {
        this.mSearchEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
        inputMethodManager.viewClicked(this.mSearchEditText);
        inputMethodManager.showSoftInput(this.mSearchEditText, 1);
        this.mSearchBar.setVisibility(0);
        this.mSearchBar.clearAnimation();
        this.mSearchBar.animate().alpha(1.0f).withEndAction(null);
        this.mTitleBar.clearAnimation();
        this.mTitleBar.animate().alpha(0.0f).withEndAction(new Runnable() { // from class: com.voxel.simplesearchlauncher.appoptions.-$$Lambda$IconPickerFragment$nGilOJ-QTjAE1tA-MPwpSs5xDDA
            @Override // java.lang.Runnable
            public final void run() {
                IconPickerFragment.this.mTitleBar.setVisibility(4);
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void unfocusFragment(boolean z) {
    }
}
