package com.voxel.simplesearchlauncher.fragment;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import androidx.core.view.GestureDetectorCompat;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.voxel.launcher3.LauncherApplication;
import com.voxel.recyclerview.widget.OverscrollLinearLayoutManager;
import com.voxel.recyclerview.widget.RecyclerView;
import com.voxel.recyclerview.widget.SpringRecyclerView;
import com.voxel.simplesearchlauncher.view.RoundedFrameLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public abstract class RecyclerViewCardFragment extends SpringCardFragment {
    private static final String TAG = "RecyclerViewCardFragment";
    protected RoundedFrameLayout mBackground;
    private boolean mBackgroundDirty = true;
    protected View mContentView;
    private int mFlingThresholdVelocity;
    private GestureDetectorCompat mGestureDetector;
    protected OverscrollLinearLayoutManager mLayoutManager;
    private int mOverscrollDragThreshold;
    protected SpringRecyclerView mRecyclerView;

    /* JADX INFO: Access modifiers changed from: protected */
    public abstract class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SpringRecyclerView.SpringItemPositioner {
        protected Adapter() {
        }

        public abstract boolean isItemInBackground(int i);
    }

    static /* synthetic */ boolean lambda$onCreateChildContentView$0(View view, MotionEvent motionEvent) {
        return true;
    }

    public boolean lambda$onCreateChildContentView$1(View view, MotionEvent motionEvent) {
        if (this.isTransitioning()) {
            return true;
        }
        if (motionEvent.getActionMasked() != 1 || this.mRecyclerView.getCurrentScrollPositionY() >= 0 || Math.abs(this.mRecyclerView.getCurrentScrollPositionY()) <= this.mOverscrollDragThreshold) {
            this.mGestureDetector.onTouchEvent(motionEvent);
            return false;
        }
        this.runExitAnimation();
        return true;
    }

    public void lambda$onCreateChildContentView$2(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i2 == i6 && i3 == i7 && i4 == i8) {
            return;
        }
        this.markBackgroundDirty();
        this.recalculateBackgroundHeight();
    }

    private void showRecyclerView() {
        this.mRecyclerView.setVisibility(0);
        Adapter adapter = getAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() { // from class: com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment.3
            @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
            public void onChanged() {
                RecyclerViewCardFragment.this.markBackgroundDirty();
                RecyclerViewCardFragment.this.recalculateBackgroundHeight();
            }
        });
        this.mRecyclerView.setAdapter(adapter);
    }

    protected abstract Adapter getAdapter();

    protected int getTotalActualItemsHeight() {
        if (getAdapter() == null) {
            return 0;
        }
        Adapter adapter = getAdapter();
        int itemViewHeightForPosition = 0;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (adapter.isItemInBackground(i)) {
                itemViewHeightForPosition += adapter.getItemViewHeightForPosition(i);
            }
        }
        return itemViewHeightForPosition;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    protected int getTotalSpringDistanceOffset() {
        if (getAdapter() == null || getAdapter().getItemCount() == 0) {
            return 0;
        }
        recalculateBackgroundHeight();
        return -((AbsoluteLayout.LayoutParams) this.mBackground.getLayoutParams()).y;
    }

    protected final void markBackgroundDirty() {
        this.mBackgroundDirty = true;
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mFlingThresholdVelocity = (int) (getActivity().getResources().getDisplayMetrics().density * 1500.0f);
        this.mOverscrollDragThreshold = getActivity().getResources().getDimensionPixelSize(R.dimen.entity_overscroll_exit_drag_distance);
        this.mGestureDetector = new GestureDetectorCompat(getActivity(), new GestureDetector.SimpleOnGestureListener() { // from class: com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment.1
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
                if (RecyclerViewCardFragment.this.isTransitioning() || RecyclerViewCardFragment.this.mRecyclerView == null || RecyclerViewCardFragment.this.mRecyclerView.getCurrentScrollPositionY() >= 0 || f2 <= RecyclerViewCardFragment.this.mFlingThresholdVelocity) {
                    return true;
                }
                RecyclerViewCardFragment.this.runExitAnimation();
                return true;
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    protected View onCreateChildContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mContentView = layoutInflater.inflate(R.layout.fragment_action_list, viewGroup, false);
        this.mContentView.setOnTouchListener(new View.OnTouchListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$RecyclerViewCardFragment$xSlBdn3rozWX9IboNecmzu0Kuyg
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return RecyclerViewCardFragment.lambda$onCreateChildContentView$0(view, motionEvent);
            }
        });
        this.mBackground = (RoundedFrameLayout) this.mContentView.findViewById(R.id.background);
        int dimensionPixelOffset = getResources().getDimensionPixelOffset(R.dimen.entity_corner_radius);
        this.mBackground.setClipOutlines(true);
        float f = dimensionPixelOffset;
        this.mBackground.setTopLeftCornerRadius(f);
        this.mBackground.setTopRightCornerRadius(f);
        this.mBackground.setBottomLeftCornerRadius(f);
        this.mBackground.setBottomRightCornerRadius(f);
        this.mRecyclerView = (SpringRecyclerView) this.mContentView.findViewById(R.id.recycler_view);
        this.mLayoutManager = new OverscrollLinearLayoutManager(getActivity());
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mRecyclerView.setHasFixedSize(false);
        this.mRecyclerView.setOnTouchListener(new View.OnTouchListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$RecyclerViewCardFragment$m4a6q14kFp3bmIEBpgkzHjwoY40
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return RecyclerViewCardFragment.this.lambda$onCreateChildContentView$1( view, motionEvent);
            }
        });
        this.mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() { // from class: com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment.2
            @Override // com.voxel.recyclerview.widget.RecyclerView.OnScrollListener
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                if (recyclerView == RecyclerViewCardFragment.this.mRecyclerView) {
                    RecyclerViewCardFragment.this.invalidateBackgroundWashAlpha();
                    RecyclerViewCardFragment.this.mBackground.setTranslationY(-RecyclerViewCardFragment.this.mRecyclerView.getCurrentScrollPositionY());
                }
            }
        });
        showRecyclerView();
        this.mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$RecyclerViewCardFragment$wZv7pgOs4SIgrSR4rP1Fnoxjebs
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                RecyclerViewCardFragment.this.lambda$onCreateChildContentView$2( view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        return this.mContentView;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment, androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        this.mContentView = null;
        this.mBackground = null;
        this.mRecyclerView = null;
        super.onDestroyView();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    protected void onFinishedEnterTransition() {
        super.onFinishedEnterTransition();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    protected void onFragmentWillClose() {
        super.onFragmentWillClose();
        SpringRecyclerView springRecyclerView = this.mRecyclerView;
        if (springRecyclerView != null) {
            springRecyclerView.stopScroll();
        }
        this.mSpringY.addListener(new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.fragment.RecyclerViewCardFragment.4
            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                if (RecyclerViewCardFragment.this.mRecyclerView != null) {
                    RecyclerViewCardFragment.this.mRecyclerView.stopScroll();
                }
            }
        });
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment
    protected void onStartedEnterTransition() {
        super.onStartedEnterTransition();
        if (getView() != null) {
            getView().setVisibility(0);
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.SpringCardFragment, androidx.fragment.app.Fragment
    public void onViewStateRestored(Bundle bundle) {
        super.onViewStateRestored(bundle);
        this.mRecyclerView.scrollToPosition(0);
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    protected final void recalculateBackgroundHeight() {
        if (usesBackgroundCard() && getAdapter() != null && this.mBackgroundDirty) {
            this.mBackgroundDirty = false;
            Adapter adapter = getAdapter();
            AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) this.mBackground.getLayoutParams();
            int itemViewHeightForPosition = 0;
            for (int i = 0; i < adapter.getItemCount() && !adapter.isItemInBackground(i); i++) {
                itemViewHeightForPosition += adapter.getItemViewHeightForPosition(i);
            }
            layoutParams.y = itemViewHeightForPosition;
            layoutParams.height = getTotalActualItemsHeight();
            if (layoutParams.height == 0) {
                throw new IllegalStateException("Total actual items height cannot be zero! Consider returning false for usesBackgroundCard().");
            }
            this.mBackground.setLayoutParams(layoutParams);
        }
    }

    protected boolean usesBackgroundCard() {
        return true;
    }
}
