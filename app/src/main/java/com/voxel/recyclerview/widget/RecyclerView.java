package com.voxel.recyclerview.widget;

import android.content.Context;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.FocusFinder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import androidx.collection.ArrayMap;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.VelocityTrackerCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewConfigurationCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityRecordCompat;
import androidx.core.widget.EdgeEffectCompat;
import androidx.core.widget.ScrollerCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class RecyclerView extends ViewGroup {
    private static final boolean FORCE_INVALIDATE_DISPLAY_LIST;
    private static final Interpolator sQuinticInterpolator;
    private RecyclerViewAccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityManager mAccessibilityManager;
    private OnItemTouchListener mActiveOnItemTouchListener;
    private Adapter mAdapter;
    AdapterHelper mAdapterHelper;
    private boolean mAdapterUpdateDuringMeasure;
    private EdgeEffectCompat mBottomGlow;
    ChildHelper mChildHelper;
    private boolean mClipToPadding;
    private boolean mDataSetHasChangedAfterLayout;
    final List<View> mDisappearingViewsInLayoutPass;
    private boolean mEatRequestLayout;
    private boolean mFirstLayoutComplete;
    private boolean mHasFixedSize;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private boolean mIsAttached;
    ItemAnimator mItemAnimator;
    private ItemAnimator.ItemAnimatorListener mItemAnimatorListener;
    private Runnable mItemAnimatorRunner;
    protected final ArrayList<ItemDecoration> mItemDecorations;
    boolean mItemsAddedOrRemoved;
    boolean mItemsChanged;
    private int mLastTouchX;
    private int mLastTouchY;
    private LayoutManager mLayout;
    private boolean mLayoutRequestEaten;
    private EdgeEffectCompat mLeftGlow;
    private final int mMaxFlingVelocity;
    private final int mMinFlingVelocity;
    private final int[] mMinMaxLayoutPositions;
    private final RecyclerViewDataObserver mObserver;
    private final ArrayList<OnItemTouchListener> mOnItemTouchListeners;
    private SavedState mPendingSavedState;
    private final boolean mPostUpdatesOnAnimation;
    private boolean mPostedAnimatorRunner;
    final Recycler mRecycler;
    private RecyclerListener mRecyclerListener;
    private EdgeEffectCompat mRightGlow;
    protected boolean mRunningLayoutOrScroll;
    private float mScrollFactor;
    private OnScrollListener mScrollListener;
    private int mScrollPointerId;
    private int mScrollState;
    final State mState;
    private final Rect mTempRect;
    private EdgeEffectCompat mTopGlow;
    private int mTouchSlop;
    private final Runnable mUpdateChildViewsRunnable;
    private VelocityTracker mVelocityTracker;
    private final IViewFlinger mViewFlinger;

    public static abstract class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable mObservable = new AdapterDataObservable();
        private boolean mHasStableIds = false;

        public final void bindViewHolder(VH vh, int i) {
            vh.mPosition = i;
            if (hasStableIds()) {
                vh.mItemId = getItemId(i);
            }
            vh.setFlags(1, 519);
            onBindViewHolder(vh, i);
        }

        public final VH createViewHolder(ViewGroup viewGroup, int i) {
            VH vh = (VH) onCreateViewHolder(viewGroup, i);
            vh.mItemViewType = i;
            return vh;
        }

        public abstract int getItemCount();

        public long getItemId(int i) {
            return -1L;
        }

        public int getItemViewType(int i) {
            return 0;
        }

        public final boolean hasStableIds() {
            return this.mHasStableIds;
        }

        public final void notifyDataSetChanged() {
            this.mObservable.notifyChanged();
        }

        public final void notifyItemChanged(int i) {
            this.mObservable.notifyItemRangeChanged(i, 1);
        }

        public final void notifyItemInserted(int i) {
            this.mObservable.notifyItemRangeInserted(i, 1);
        }

        public final void notifyItemRemoved(int i) {
            this.mObservable.notifyItemRangeRemoved(i, 1);
        }

        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        }

        public abstract void onBindViewHolder(VH vh, int i);

        public abstract VH onCreateViewHolder(ViewGroup viewGroup, int i);

        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        }

        public boolean onFailedToRecycleView(VH vh) {
            return false;
        }

        public void onViewAttachedToWindow(VH vh) {
        }

        public void onViewDetachedFromWindow(VH vh) {
        }

        public void onViewRecycled(VH vh) {
        }

        public void registerAdapterDataObserver(AdapterDataObserver adapterDataObserver) {
            this.mObservable.registerObserver(adapterDataObserver);
        }

        public void unregisterAdapterDataObserver(AdapterDataObserver adapterDataObserver) {
            this.mObservable.unregisterObserver(adapterDataObserver);
        }
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        AdapterDataObservable() {
        }

        public void notifyChanged() {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onChanged();
            }
        }

        public void notifyItemRangeChanged(int i, int i2) {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onItemRangeChanged(i, i2);
            }
        }

        public void notifyItemRangeInserted(int i, int i2) {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onItemRangeInserted(i, i2);
            }
        }

        public void notifyItemRangeRemoved(int i, int i2) {
            for (int size = this.mObservers.size() - 1; size >= 0; size--) {
                ((AdapterDataObserver) this.mObservers.get(size)).onItemRangeRemoved(i, i2);
            }
        }
    }

    public static abstract class AdapterDataObserver {
        public void onChanged() {
        }

        public void onItemRangeChanged(int i, int i2) {
        }

        public void onItemRangeInserted(int i, int i2) {
        }

        public void onItemRangeRemoved(int i, int i2) {
        }
    }

    protected interface IViewFlinger extends Runnable {
        void fling(int i, int i2);

        void smoothScrollBy(int i, int i2);

        void smoothScrollBy(int i, int i2, int i3);

        void smoothScrollBy(int i, int i2, int i3, Interpolator interpolator);

        void stop();
    }

    public static abstract class ItemAnimator {
        private ItemAnimatorListener mListener = null;
        private ArrayList<ItemAnimatorFinishedListener> mFinishedListeners = new ArrayList<>();
        private long mAddDuration = 120;
        private long mRemoveDuration = 120;
        private long mMoveDuration = 250;
        private long mChangeDuration = 250;
        private boolean mSupportsChangeAnimations = true;

        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }

        interface ItemAnimatorListener {
            void onAddFinished(ViewHolder viewHolder);

            void onChangeFinished(ViewHolder viewHolder);

            void onMoveFinished(ViewHolder viewHolder);

            void onRemoveFinished(ViewHolder viewHolder);
        }

        public abstract boolean animateAdd(ViewHolder viewHolder);

        public abstract boolean animateChange(ViewHolder viewHolder, ViewHolder viewHolder2, int i, int i2, int i3, int i4);

        public abstract boolean animateMove(ViewHolder viewHolder, int i, int i2, int i3, int i4);

        public abstract boolean animateRemove(ViewHolder viewHolder);

        public final void dispatchAddFinished(ViewHolder viewHolder) {
            onAddFinished(viewHolder);
            ItemAnimatorListener itemAnimatorListener = this.mListener;
            if (itemAnimatorListener != null) {
                itemAnimatorListener.onAddFinished(viewHolder);
            }
        }

        public final void dispatchAddStarting(ViewHolder viewHolder) {
            onAddStarting(viewHolder);
        }

        public final void dispatchAnimationsFinished() {
            int size = this.mFinishedListeners.size();
            for (int i = 0; i < size; i++) {
                this.mFinishedListeners.get(i).onAnimationsFinished();
            }
            this.mFinishedListeners.clear();
        }

        public final void dispatchChangeFinished(ViewHolder viewHolder, boolean z) {
            onChangeFinished(viewHolder, z);
            ItemAnimatorListener itemAnimatorListener = this.mListener;
            if (itemAnimatorListener != null) {
                itemAnimatorListener.onChangeFinished(viewHolder);
            }
        }

        public final void dispatchChangeStarting(ViewHolder viewHolder, boolean z) {
            onChangeStarting(viewHolder, z);
        }

        public final void dispatchMoveFinished(ViewHolder viewHolder) {
            onMoveFinished(viewHolder);
            ItemAnimatorListener itemAnimatorListener = this.mListener;
            if (itemAnimatorListener != null) {
                itemAnimatorListener.onMoveFinished(viewHolder);
            }
        }

        public final void dispatchMoveStarting(ViewHolder viewHolder) {
            onMoveStarting(viewHolder);
        }

        public final void dispatchRemoveFinished(ViewHolder viewHolder) {
            onRemoveFinished(viewHolder);
            ItemAnimatorListener itemAnimatorListener = this.mListener;
            if (itemAnimatorListener != null) {
                itemAnimatorListener.onRemoveFinished(viewHolder);
            }
        }

        public final void dispatchRemoveStarting(ViewHolder viewHolder) {
            onRemoveStarting(viewHolder);
        }

        public abstract void endAnimation(ViewHolder viewHolder);

        public abstract void endAnimations();

        public long getAddDuration() {
            return this.mAddDuration;
        }

        public long getChangeDuration() {
            return this.mChangeDuration;
        }

        public long getMoveDuration() {
            return this.mMoveDuration;
        }

        public long getRemoveDuration() {
            return this.mRemoveDuration;
        }

        public boolean getSupportsChangeAnimations() {
            return this.mSupportsChangeAnimations;
        }

        public abstract boolean isRunning();

        public void onAddFinished(ViewHolder viewHolder) {
        }

        public void onAddStarting(ViewHolder viewHolder) {
        }

        public void onChangeFinished(ViewHolder viewHolder, boolean z) {
        }

        public void onChangeStarting(ViewHolder viewHolder, boolean z) {
        }

        public void onMoveFinished(ViewHolder viewHolder) {
        }

        public void onMoveStarting(ViewHolder viewHolder) {
        }

        public void onRemoveFinished(ViewHolder viewHolder) {
        }

        public void onRemoveStarting(ViewHolder viewHolder) {
        }

        public abstract void runPendingAnimations();

        void setListener(ItemAnimatorListener itemAnimatorListener) {
            this.mListener = itemAnimatorListener;
        }

        public void setRemoveDuration(long j) {
            this.mRemoveDuration = j;
        }
    }

    private class ItemAnimatorRestoreListener implements ItemAnimator.ItemAnimatorListener {
        private ItemAnimatorRestoreListener() {
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorListener
        public void onAddFinished(ViewHolder viewHolder) {
            viewHolder.setIsRecyclable(true);
            if (viewHolder.shouldBeKeptAsChild()) {
                return;
            }
            RecyclerView.this.removeAnimatingView(viewHolder.itemView);
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorListener
        public void onChangeFinished(ViewHolder viewHolder) {
            viewHolder.setIsRecyclable(true);
            if (viewHolder.mShadowedHolder != null && viewHolder.mShadowingHolder == null) {
                viewHolder.mShadowedHolder = null;
                viewHolder.setFlags(-65, viewHolder.mFlags);
            }
            viewHolder.mShadowingHolder = null;
            if (viewHolder.shouldBeKeptAsChild()) {
                return;
            }
            RecyclerView.this.removeAnimatingView(viewHolder.itemView);
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorListener
        public void onMoveFinished(ViewHolder viewHolder) {
            viewHolder.setIsRecyclable(true);
            if (viewHolder.shouldBeKeptAsChild()) {
                return;
            }
            RecyclerView.this.removeAnimatingView(viewHolder.itemView);
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.ItemAnimator.ItemAnimatorListener
        public void onRemoveFinished(ViewHolder viewHolder) {
            viewHolder.setIsRecyclable(true);
            if (RecyclerView.this.removeAnimatingView(viewHolder.itemView) || !viewHolder.isTmpDetached()) {
                return;
            }
            RecyclerView.this.removeDetachedView(viewHolder.itemView, false);
        }
    }

    public static abstract class ItemDecoration {
        @Deprecated
        public void getItemOffsets(Rect rect, int i, RecyclerView recyclerView) {
            rect.set(0, 0, 0, 0);
        }

        public void getItemOffsets(Rect rect, View view, RecyclerView recyclerView, State state) {
            getItemOffsets(rect, ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition(), recyclerView);
        }

        @Deprecated
        public void onDraw(Canvas canvas, RecyclerView recyclerView) {
        }

        public void onDraw(Canvas canvas, RecyclerView recyclerView, State state) {
            onDraw(canvas, recyclerView);
        }

        @Deprecated
        public void onDrawOver(Canvas canvas, RecyclerView recyclerView) {
        }

        public void onDrawOver(Canvas canvas, RecyclerView recyclerView, State state) {
            onDrawOver(canvas, recyclerView);
        }
    }

    private static class ItemHolderInfo {
        int bottom;
        ViewHolder holder;
        int left;
        int right;
        int top;

        ItemHolderInfo(ViewHolder viewHolder, int i, int i2, int i3, int i4) {
            this.holder = viewHolder;
            this.left = i;
            this.top = i2;
            this.right = i3;
            this.bottom = i4;
        }
    }

    public static abstract class LayoutManager {
        ChildHelper mChildHelper;
        RecyclerView mRecyclerView;
        private boolean mRequestedSimpleAnimations = false;
        SmoothScroller mSmoothScroller;

        private void addViewInt(View view, int i, boolean z) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (z || childViewHolderInt.isRemoved()) {
                this.mRecyclerView.addToDisappearingList(view);
            } else {
                this.mRecyclerView.removeFromDisappearingList(view);
            }
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (childViewHolderInt.wasReturnedFromScrap() || childViewHolderInt.isScrap()) {
                if (childViewHolderInt.isScrap()) {
                    childViewHolderInt.unScrap();
                } else {
                    childViewHolderInt.clearReturnedFromScrapFlag();
                }
                this.mChildHelper.attachViewToParent(view, i, view.getLayoutParams(), false);
            } else if (view.getParent() == this.mRecyclerView) {
                int iIndexOfChild = this.mChildHelper.indexOfChild(view);
                if (i == -1) {
                    i = this.mChildHelper.getChildCount();
                }
                if (iIndexOfChild == -1) {
                    throw new IllegalStateException("Added View has RecyclerView as parent but view is not a real child. Unfiltered index:" + this.mRecyclerView.indexOfChild(view));
                }
                if (iIndexOfChild != i) {
                    this.mRecyclerView.mLayout.moveView(iIndexOfChild, i);
                }
            } else {
                this.mChildHelper.addView(view, i, false);
                layoutParams.mInsetsDirty = true;
                SmoothScroller smoothScroller = this.mSmoothScroller;
                if (smoothScroller != null && smoothScroller.isRunning()) {
                    this.mSmoothScroller.onChildAttachedToWindow(view);
                }
            }
            if (layoutParams.mPendingInvalidate) {
                childViewHolderInt.itemView.invalidate();
                layoutParams.mPendingInvalidate = false;
            }
        }

        private void detachViewInternal(int i, View view) {
            this.mChildHelper.detachViewFromParent(i);
        }

        public static int getChildMeasureSpec(int i, int i2, int i3, boolean z) {
            int i4 = i - i2;
            int i5 = 0;
            int iMax = Math.max(0, i4);
            if (z) {
                if (i3 >= 0) {
                    iMax = i3;
                    i5 = 1073741824;
                } else {
                    iMax = 0;
                }
            } else if (i3 >= 0) {
                iMax = i3;
                i5 = 1073741824;
            } else if (i3 == -1) {
                i5 = 1073741824;
            } else if (i3 == -2) {
                i5 = Integer.MIN_VALUE;
            } else {
                iMax = 0;
            }
            return View.MeasureSpec.makeMeasureSpec(iMax, i5);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void onSmoothScrollerStopped(SmoothScroller smoothScroller) {
            if (this.mSmoothScroller == smoothScroller) {
                this.mSmoothScroller = null;
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int i, View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.shouldIgnore()) {
                return;
            }
            if (!childViewHolderInt.isInvalid() || childViewHolderInt.isRemoved() || childViewHolderInt.isChanged() || this.mRecyclerView.mAdapter.hasStableIds()) {
                detachViewAt(i);
                recycler.scrapView(view);
            } else {
                removeViewAt(i);
                recycler.recycleViewHolderInternal(childViewHolderInt);
            }
        }

        public void addDisappearingView(View view) {
            addDisappearingView(view, -1);
        }

        public void addDisappearingView(View view, int i) {
            addViewInt(view, i, true);
        }

        public void addView(View view) {
            addView(view, -1);
        }

        public void addView(View view, int i) {
            addViewInt(view, i, false);
        }

        public void assertNotInLayoutOrScroll(String str) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.assertNotInLayoutOrScroll(str);
            }
        }

        public void attachView(View view, int i) {
            attachView(view, i, (LayoutParams) view.getLayoutParams());
        }

        public void attachView(View view, int i, LayoutParams layoutParams) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.isRemoved()) {
                this.mRecyclerView.addToDisappearingList(view);
            } else {
                this.mRecyclerView.removeFromDisappearingList(view);
            }
            this.mChildHelper.attachViewToParent(view, i, layoutParams, childViewHolderInt.isRemoved());
        }

        public void calculateItemDecorationsForChild(View view, Rect rect) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null) {
                rect.set(0, 0, 0, 0);
            } else {
                rect.set(recyclerView.getItemDecorInsetsForChild(view));
            }
        }

        public boolean canScrollHorizontally() {
            return false;
        }

        public boolean canScrollVertically() {
            return false;
        }

        public boolean checkLayoutParams(LayoutParams layoutParams) {
            return layoutParams != null;
        }

        public int computeHorizontalScrollExtent(State state) {
            return 0;
        }

        public int computeHorizontalScrollOffset(State state) {
            return 0;
        }

        public int computeHorizontalScrollRange(State state) {
            return 0;
        }

        public int computeVerticalScrollExtent(State state) {
            return 0;
        }

        public int computeVerticalScrollOffset(State state) {
            return 0;
        }

        public int computeVerticalScrollRange(State state) {
            return 0;
        }

        public void detachAndScrapAttachedViews(Recycler recycler) {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                scrapOrRecycleView(recycler, childCount, getChildAt(childCount));
            }
        }

        public void detachViewAt(int i) {
            detachViewInternal(i, getChildAt(i));
        }

        public View findViewByPosition(int i) {
            int childCount = getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = getChildAt(i2);
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(childAt);
                if (childViewHolderInt != null && childViewHolderInt.getLayoutPosition() == i && !childViewHolderInt.shouldIgnore() && (this.mRecyclerView.mState.isPreLayout() || !childViewHolderInt.isRemoved())) {
                    return childAt;
                }
            }
            return null;
        }

        public abstract LayoutParams generateDefaultLayoutParams();

        public LayoutParams generateLayoutParams(Context context, AttributeSet attributeSet) {
            return new LayoutParams(context, attributeSet);
        }

        public LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
            if (layoutParams instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) layoutParams);
            }
            return layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
        }

        public int getBottomDecorationHeight(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.bottom;
        }

        public View getChildAt(int i) {
            ChildHelper childHelper = this.mChildHelper;
            if (childHelper != null) {
                return childHelper.getChildAt(i);
            }
            return null;
        }

        public int getChildCount() {
            ChildHelper childHelper = this.mChildHelper;
            if (childHelper != null) {
                return childHelper.getChildCount();
            }
            return 0;
        }

        public int getColumnCountForAccessibility(Recycler recycler, State state) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null || recyclerView.mAdapter == null || !canScrollHorizontally()) {
                return 1;
            }
            return this.mRecyclerView.mAdapter.getItemCount();
        }

        public int getDecoratedBottom(View view) {
            return view.getBottom() + getBottomDecorationHeight(view);
        }

        public int getDecoratedLeft(View view) {
            return view.getLeft() - getLeftDecorationWidth(view);
        }

        public int getDecoratedMeasuredHeight(View view) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            return view.getMeasuredHeight() + rect.top + rect.bottom;
        }

        public int getDecoratedMeasuredWidth(View view) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            return view.getMeasuredWidth() + rect.left + rect.right;
        }

        public int getDecoratedRight(View view) {
            return view.getRight() + getRightDecorationWidth(view);
        }

        public int getDecoratedTop(View view) {
            return view.getTop() - getTopDecorationHeight(view);
        }

        public View getFocusedChild() {
            View focusedChild;
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null || (focusedChild = recyclerView.getFocusedChild()) == null || this.mChildHelper.isHidden(focusedChild)) {
                return null;
            }
            return focusedChild;
        }

        public int getHeight() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getHeight();
            }
            return 0;
        }

        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(this.mRecyclerView);
        }

        public int getLeftDecorationWidth(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.left;
        }

        public int getPaddingBottom() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingBottom();
            }
            return 0;
        }

        public int getPaddingLeft() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingLeft();
            }
            return 0;
        }

        public int getPaddingRight() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingRight();
            }
            return 0;
        }

        public int getPaddingTop() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getPaddingTop();
            }
            return 0;
        }

        public int getPosition(View view) {
            return ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        }

        public int getRightDecorationWidth(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.right;
        }

        public int getRowCountForAccessibility(Recycler recycler, State state) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null || recyclerView.mAdapter == null || !canScrollVertically()) {
                return 1;
            }
            return this.mRecyclerView.mAdapter.getItemCount();
        }

        public int getSelectionModeForAccessibility(Recycler recycler, State state) {
            return 0;
        }

        public int getTopDecorationHeight(View view) {
            return ((LayoutParams) view.getLayoutParams()).mDecorInsets.top;
        }

        public int getWidth() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                return recyclerView.getWidth();
            }
            return 0;
        }

        public boolean isLayoutHierarchical(Recycler recycler, State state) {
            return false;
        }

        public boolean isSmoothScrolling() {
            SmoothScroller smoothScroller = this.mSmoothScroller;
            return smoothScroller != null && smoothScroller.isRunning();
        }

        public void layoutDecorated(View view, int i, int i2, int i3, int i4) {
            Rect rect = ((LayoutParams) view.getLayoutParams()).mDecorInsets;
            view.layout(i + rect.left, i2 + rect.top, i3 - rect.right, i4 - rect.bottom);
        }

        public void measureChildWithMargins(View view, int i, int i2) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            Rect itemDecorInsetsForChild = this.mRecyclerView.getItemDecorInsetsForChild(view);
            view.measure(getChildMeasureSpec(getWidth(), getPaddingLeft() + getPaddingRight() + layoutParams.leftMargin + layoutParams.rightMargin + i + itemDecorInsetsForChild.left + itemDecorInsetsForChild.right, layoutParams.width, canScrollHorizontally()), getChildMeasureSpec(getHeight(), getPaddingTop() + getPaddingBottom() + layoutParams.topMargin + layoutParams.bottomMargin + i2 + itemDecorInsetsForChild.top + itemDecorInsetsForChild.bottom, layoutParams.height, canScrollVertically()));
        }

        public void moveView(int i, int i2) {
            View childAt = getChildAt(i);
            if (childAt != null) {
                detachViewAt(i);
                attachView(childAt, i2);
            } else {
                throw new IllegalArgumentException("Cannot move a child from non-existing index:" + i);
            }
        }

        public void offsetChildrenHorizontal(int i) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.offsetChildrenHorizontal(i);
            }
        }

        public void offsetChildrenVertical(int i) {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.offsetChildrenVertical(i);
            }
        }

        public void onAdapterChanged(Adapter adapter, Adapter adapter2) {
        }

        public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> arrayList, int i, int i2) {
            return false;
        }

        public void onAttachedToWindow(RecyclerView recyclerView) {
        }

        @Deprecated
        public void onDetachedFromWindow(RecyclerView recyclerView) {
        }

        public void onDetachedFromWindow(RecyclerView recyclerView, Recycler recycler) {
            onDetachedFromWindow(recyclerView);
        }

        public View onFocusSearchFailed(View view, int i, Recycler recycler, State state) {
            return null;
        }

        public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            onInitializeAccessibilityEvent(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, accessibilityEvent);
        }

        public void onInitializeAccessibilityEvent(Recycler recycler, State state, AccessibilityEvent accessibilityEvent) {
            AccessibilityRecordCompat accessibilityRecordCompatAsRecord = AccessibilityEventCompat.asRecord(accessibilityEvent);
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null || accessibilityRecordCompatAsRecord == null) {
                return;
            }
            boolean z = true;
            if (!ViewCompat.canScrollVertically(recyclerView, 1) && !ViewCompat.canScrollVertically(this.mRecyclerView, -1) && !ViewCompat.canScrollHorizontally(this.mRecyclerView, -1) && !ViewCompat.canScrollHorizontally(this.mRecyclerView, 1)) {
                z = false;
            }
            accessibilityRecordCompatAsRecord.setScrollable(z);
            if (this.mRecyclerView.mAdapter != null) {
                accessibilityRecordCompatAsRecord.setItemCount(this.mRecyclerView.mAdapter.getItemCount());
            }
        }

        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            onInitializeAccessibilityNodeInfo(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, accessibilityNodeInfoCompat);
        }

        public void onInitializeAccessibilityNodeInfo(Recycler recycler, State state, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setClassName(RecyclerView.class.getName());
            if (ViewCompat.canScrollVertically(this.mRecyclerView, -1) || ViewCompat.canScrollHorizontally(this.mRecyclerView, -1)) {
                accessibilityNodeInfoCompat.addAction(8192);
                accessibilityNodeInfoCompat.setScrollable(true);
            }
            if (ViewCompat.canScrollVertically(this.mRecyclerView, 1) || ViewCompat.canScrollHorizontally(this.mRecyclerView, 1)) {
                accessibilityNodeInfoCompat.addAction(4096);
                accessibilityNodeInfoCompat.setScrollable(true);
            }
            accessibilityNodeInfoCompat.setCollectionInfo(AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state)));
        }

        void onInitializeAccessibilityNodeInfoForItem(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt == null || childViewHolderInt.isRemoved()) {
                return;
            }
            onInitializeAccessibilityNodeInfoForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, view, accessibilityNodeInfoCompat);
        }

        public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(canScrollVertically() ? getPosition(view) : 0, 1, canScrollHorizontally() ? getPosition(view) : 0, 1, false, false));
        }

        public View onInterceptFocusSearch(View view, int i) {
            return null;
        }

        public void onItemsAdded(RecyclerView recyclerView, int i, int i2) {
        }

        public void onItemsChanged(RecyclerView recyclerView) {
        }

        public void onItemsMoved(RecyclerView recyclerView, int i, int i2, int i3) {
        }

        public void onItemsRemoved(RecyclerView recyclerView, int i, int i2) {
        }

        public void onItemsUpdated(RecyclerView recyclerView, int i, int i2) {
        }

        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e("RecyclerView", "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        public void onMeasure(Recycler recycler, State state, int i, int i2) {
            this.mRecyclerView.defaultOnMeasure(i, i2);
        }

        @Deprecated
        public boolean onRequestChildFocus(RecyclerView recyclerView, View view, View view2) {
            return isSmoothScrolling() || recyclerView.mRunningLayoutOrScroll;
        }

        public boolean onRequestChildFocus(RecyclerView recyclerView, State state, View view, View view2) {
            return onRequestChildFocus(recyclerView, view, view2);
        }

        public void onRestoreInstanceState(Parcelable parcelable) {
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onScrollStateChanged(int i) {
        }

        boolean performAccessibilityAction(int i, Bundle bundle) {
            return performAccessibilityAction(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, i, bundle);
        }

        public boolean performAccessibilityAction(Recycler recycler, State state, int i, Bundle bundle) {
            int height;
            int width;
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView == null) {
                return false;
            }
            if (i == 4096) {
                height = ViewCompat.canScrollVertically(recyclerView, 1) ? (getHeight() - getPaddingTop()) - getPaddingBottom() : 0;
                width = ViewCompat.canScrollHorizontally(this.mRecyclerView, 1) ? (getWidth() - getPaddingLeft()) - getPaddingRight() : 0;
            } else if (i != 8192) {
                height = 0;
                width = 0;
            } else {
                height = ViewCompat.canScrollVertically(recyclerView, -1) ? -((getHeight() - getPaddingTop()) - getPaddingBottom()) : 0;
                width = ViewCompat.canScrollHorizontally(this.mRecyclerView, -1) ? -((getWidth() - getPaddingLeft()) - getPaddingRight()) : 0;
            }
            if (height == 0 && width == 0) {
                return false;
            }
            this.mRecyclerView.scrollBy(width, height);
            return true;
        }

        boolean performAccessibilityActionForItem(View view, int i, Bundle bundle) {
            return performAccessibilityActionForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, view, i, bundle);
        }

        public boolean performAccessibilityActionForItem(Recycler recycler, State state, View view, int i, Bundle bundle) {
            return false;
        }

        public void removeAndRecycleAllViews(Recycler recycler) {
            for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
                if (!RecyclerView.getChildViewHolderInt(getChildAt(childCount)).shouldIgnore()) {
                    removeAndRecycleViewAt(childCount, recycler);
                }
            }
        }

        void removeAndRecycleScrapInt(Recycler recycler) {
            int scrapCount = recycler.getScrapCount();
            for (int i = 0; i < scrapCount; i++) {
                View scrapViewAt = recycler.getScrapViewAt(i);
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(scrapViewAt);
                if (!childViewHolderInt.shouldIgnore()) {
                    if (childViewHolderInt.isTmpDetached()) {
                        this.mRecyclerView.removeDetachedView(scrapViewAt, false);
                    }
                    recycler.quickRecycleScrapView(scrapViewAt);
                }
            }
            recycler.clearScrap();
            if (scrapCount > 0) {
                this.mRecyclerView.invalidate();
            }
        }

        public void removeAndRecycleView(View view, Recycler recycler) {
            removeView(view);
            recycler.recycleView(view);
        }

        public void removeAndRecycleViewAt(int i, Recycler recycler) {
            View childAt = getChildAt(i);
            removeViewAt(i);
            recycler.recycleView(childAt);
        }

        public void removeView(View view) {
            this.mChildHelper.removeView(view);
        }

        public void removeViewAt(int i) {
            if (getChildAt(i) != null) {
                this.mChildHelper.removeViewAt(i);
            }
        }

        public boolean requestChildRectangleOnScreen(RecyclerView recyclerView, View view, Rect rect, boolean z) {
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int width = getWidth() - getPaddingRight();
            int height = getHeight() - getPaddingBottom();
            int left = view.getLeft() + rect.left;
            int top = view.getTop() + rect.top;
            int iWidth = rect.width() + left;
            int iHeight = rect.height() + top;
            int iMin = Math.min(0, left - paddingLeft);
            int iMin2 = Math.min(0, top - paddingTop);
            int iMax = Math.max(0, iWidth - width);
            int iMax2 = Math.max(0, iHeight - height);
            if (ViewCompat.getLayoutDirection(recyclerView) == 1) {
                if (iMax == 0) {
                    iMax = iMin;
                }
            } else if (iMin != 0) {
                iMax = iMin;
            }
            if (iMin2 == 0) {
                iMin2 = iMax2;
            }
            if (iMax == 0 && iMin2 == 0) {
                return false;
            }
            if (z) {
                recyclerView.scrollBy(iMax, iMin2);
            } else {
                recyclerView.smoothScrollBy(iMax, iMin2);
            }
            return true;
        }

        public void requestLayout() {
            RecyclerView recyclerView = this.mRecyclerView;
            if (recyclerView != null) {
                recyclerView.requestLayout();
            }
        }

        public int scrollHorizontallyBy(int i, Recycler recycler, State state) {
            return 0;
        }

        public void scrollToPosition(int i) {
        }

        public int scrollVerticallyBy(int i, Recycler recycler, State state) {
            return 0;
        }

        void setRecyclerView(RecyclerView recyclerView) {
            if (recyclerView == null) {
                this.mRecyclerView = null;
                this.mChildHelper = null;
            } else {
                this.mRecyclerView = recyclerView;
                this.mChildHelper = recyclerView.mChildHelper;
            }
        }

        void stopSmoothScroller() {
            SmoothScroller smoothScroller = this.mSmoothScroller;
            if (smoothScroller != null) {
                smoothScroller.stop();
            }
        }

        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        final Rect mDecorInsets;
        boolean mInsetsDirty;
        boolean mPendingInvalidate;
        ViewHolder mViewHolder;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public LayoutParams(LayoutParams layoutParams) {
            super((ViewGroup.LayoutParams) layoutParams);
            this.mDecorInsets = new Rect();
            this.mInsetsDirty = true;
            this.mPendingInvalidate = false;
        }

        public int getViewLayoutPosition() {
            return this.mViewHolder.getLayoutPosition();
        }

        public boolean isItemChanged() {
            return this.mViewHolder.isChanged();
        }

        public boolean isItemRemoved() {
            return this.mViewHolder.isRemoved();
        }
    }

    public interface OnItemTouchListener {
        boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent);

        void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent);
    }

    public static abstract class OnScrollListener {
        public void onScrollStateChanged(RecyclerView recyclerView, int i) {
        }

        public void onScrolled(RecyclerView recyclerView, int i, int i2) {
        }
    }

    public static class RecycledViewPool {
        private SparseArray<ArrayList<ViewHolder>> mScrap = new SparseArray<>();
        private SparseIntArray mMaxScrap = new SparseIntArray();
        private int mAttachCount = 0;

        private ArrayList<ViewHolder> getScrapHeapForType(int i) {
            ArrayList<ViewHolder> arrayList = this.mScrap.get(i);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                this.mScrap.put(i, arrayList);
                if (this.mMaxScrap.indexOfKey(i) < 0) {
                    this.mMaxScrap.put(i, 5);
                }
            }
            return arrayList;
        }

        void attach(Adapter adapter) {
            this.mAttachCount++;
        }

        public void clear() {
            this.mScrap.clear();
        }

        void detach() {
            this.mAttachCount--;
        }

        public ViewHolder getRecycledView(int i) {
            ArrayList<ViewHolder> arrayList = this.mScrap.get(i);
            if (arrayList == null || arrayList.isEmpty()) {
                return null;
            }
            int size = arrayList.size() - 1;
            ViewHolder viewHolder = arrayList.get(size);
            arrayList.remove(size);
            return viewHolder;
        }

        void onAdapterChanged(Adapter adapter, Adapter adapter2, boolean z) {
            if (adapter != null) {
                detach();
            }
            if (!z && this.mAttachCount == 0) {
                clear();
            }
            if (adapter2 != null) {
                attach(adapter2);
            }
        }

        public void putRecycledView(ViewHolder viewHolder) {
            int itemViewType = viewHolder.getItemViewType();
            ArrayList<ViewHolder> scrapHeapForType = getScrapHeapForType(itemViewType);
            if (this.mMaxScrap.get(itemViewType) <= scrapHeapForType.size()) {
                return;
            }
            viewHolder.resetInternal();
            scrapHeapForType.add(viewHolder);
        }
    }

    public final class Recycler {
        private RecycledViewPool mRecyclerPool;
        private ViewCacheExtension mViewCacheExtension;
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        private ArrayList<ViewHolder> mChangedScrap = null;
        final ArrayList<ViewHolder> mCachedViews = new ArrayList<>();
        private final List<ViewHolder> mUnmodifiableAttachedScrap = Collections.unmodifiableList(this.mAttachedScrap);
        private int mViewCacheMax = 2;

        public Recycler() {
        }

        private void attachAccessibilityDelegate(View view) {
            if (RecyclerView.this.mAccessibilityManager == null || !RecyclerView.this.mAccessibilityManager.isEnabled()) {
                return;
            }
            if (ViewCompat.getImportantForAccessibility(view) == 0) {
                ViewCompat.setImportantForAccessibility(view, 1);
            }
            if (ViewCompat.hasAccessibilityDelegate(view)) {
                return;
            }
            ViewCompat.setAccessibilityDelegate(view, RecyclerView.this.mAccessibilityDelegate.getItemDelegate());
        }

        private void invalidateDisplayListInt(ViewGroup viewGroup, boolean z) {
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                View childAt = viewGroup.getChildAt(childCount);
                if (childAt instanceof ViewGroup) {
                    invalidateDisplayListInt((ViewGroup) childAt, true);
                }
            }
            if (z) {
                if (viewGroup.getVisibility() == 4) {
                    viewGroup.setVisibility(0);
                    viewGroup.setVisibility(4);
                } else {
                    int visibility = viewGroup.getVisibility();
                    viewGroup.setVisibility(4);
                    viewGroup.setVisibility(visibility);
                }
            }
        }

        private void invalidateDisplayListInt(ViewHolder viewHolder) {
            if (viewHolder.itemView instanceof ViewGroup) {
                invalidateDisplayListInt((ViewGroup) viewHolder.itemView, false);
            }
        }

        void addViewHolderToRecycledViewPool(ViewHolder viewHolder) {
            ViewCompat.setAccessibilityDelegate(viewHolder.itemView, null);
            dispatchViewRecycled(viewHolder);
            viewHolder.mOwnerRecyclerView = null;
            getRecycledViewPool().putRecycledView(viewHolder);
        }

        public void clear() {
            this.mAttachedScrap.clear();
            recycleAndClearCachedViews();
        }

        void clearOldPositions() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                this.mCachedViews.get(i).clearOldPosition();
            }
            int size2 = this.mAttachedScrap.size();
            for (int i2 = 0; i2 < size2; i2++) {
                this.mAttachedScrap.get(i2).clearOldPosition();
            }
            ArrayList<ViewHolder> arrayList = this.mChangedScrap;
            if (arrayList != null) {
                int size3 = arrayList.size();
                for (int i3 = 0; i3 < size3; i3++) {
                    this.mChangedScrap.get(i3).clearOldPosition();
                }
            }
        }

        void clearScrap() {
            this.mAttachedScrap.clear();
        }

        public int convertPreLayoutPositionToPostLayout(int i) {
            if (i >= 0 && i < RecyclerView.this.mState.getItemCount()) {
                return !RecyclerView.this.mState.isPreLayout() ? i : RecyclerView.this.mAdapterHelper.findPositionOffset(i);
            }
            throw new IndexOutOfBoundsException("invalid position " + i + ". State item count is " + RecyclerView.this.mState.getItemCount());
        }

        void dispatchViewRecycled(ViewHolder viewHolder) {
            if (RecyclerView.this.mRecyclerListener != null) {
                RecyclerView.this.mRecyclerListener.onViewRecycled(viewHolder);
            }
            if (RecyclerView.this.mAdapter != null) {
                RecyclerView.this.mAdapter.onViewRecycled(viewHolder);
            }
            if (RecyclerView.this.mState != null) {
                RecyclerView.this.mState.onViewRecycled(viewHolder);
            }
        }

        ViewHolder getChangedScrapViewForPosition(int i) {
            int size;
            int iFindPositionOffset;
            ArrayList<ViewHolder> arrayList = this.mChangedScrap;
            if (arrayList == null || (size = arrayList.size()) == 0) {
                return null;
            }
            for (int i2 = 0; i2 < size; i2++) {
                ViewHolder viewHolder = this.mChangedScrap.get(i2);
                if (!viewHolder.wasReturnedFromScrap() && viewHolder.getLayoutPosition() == i) {
                    viewHolder.addFlags(32);
                    return viewHolder;
                }
            }
            if (RecyclerView.this.mAdapter.hasStableIds() && (iFindPositionOffset = RecyclerView.this.mAdapterHelper.findPositionOffset(i)) > 0 && iFindPositionOffset < RecyclerView.this.mAdapter.getItemCount()) {
                long itemId = RecyclerView.this.mAdapter.getItemId(iFindPositionOffset);
                for (int i3 = 0; i3 < size; i3++) {
                    ViewHolder viewHolder2 = this.mChangedScrap.get(i3);
                    if (!viewHolder2.wasReturnedFromScrap() && viewHolder2.getItemId() == itemId) {
                        viewHolder2.addFlags(32);
                        return viewHolder2;
                    }
                }
            }
            return null;
        }

        RecycledViewPool getRecycledViewPool() {
            if (this.mRecyclerPool == null) {
                this.mRecyclerPool = new RecycledViewPool();
            }
            return this.mRecyclerPool;
        }

        int getScrapCount() {
            return this.mAttachedScrap.size();
        }

        public List<ViewHolder> getScrapList() {
            return this.mUnmodifiableAttachedScrap;
        }

        View getScrapViewAt(int i) {
            return this.mAttachedScrap.get(i).itemView;
        }

        ViewHolder getScrapViewForId(long j, int i, boolean z) {
            for (int size = this.mAttachedScrap.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = this.mAttachedScrap.get(size);
                if (viewHolder.getItemId() == j && !viewHolder.wasReturnedFromScrap()) {
                    if (i == viewHolder.getItemViewType()) {
                        viewHolder.addFlags(32);
                        if (viewHolder.isRemoved() && !RecyclerView.this.mState.isPreLayout()) {
                            viewHolder.setFlags(2, 14);
                        }
                        return viewHolder;
                    }
                    if (!z) {
                        this.mAttachedScrap.remove(size);
                        RecyclerView.this.removeDetachedView(viewHolder.itemView, false);
                        quickRecycleScrapView(viewHolder.itemView);
                    }
                }
            }
            for (int size2 = this.mCachedViews.size() - 1; size2 >= 0; size2--) {
                ViewHolder viewHolder2 = this.mCachedViews.get(size2);
                if (viewHolder2.getItemId() == j) {
                    if (i == viewHolder2.getItemViewType()) {
                        if (!z) {
                            this.mCachedViews.remove(size2);
                        }
                        return viewHolder2;
                    }
                    if (!z) {
                        recycleCachedViewAt(size2);
                    }
                }
            }
            return null;
        }

        ViewHolder getScrapViewForPosition(int i, int i2, boolean z) {
            View viewFindHiddenNonRemovedView;
            int size = this.mAttachedScrap.size();
            for (int i3 = 0; i3 < size; i3++) {
                ViewHolder viewHolder = this.mAttachedScrap.get(i3);
                if (!viewHolder.wasReturnedFromScrap() && viewHolder.getLayoutPosition() == i && !viewHolder.isInvalid() && (RecyclerView.this.mState.mInPreLayout || !viewHolder.isRemoved())) {
                    if (i2 != -1 && viewHolder.getItemViewType() != i2) {
                        Log.e("RecyclerView", "Scrap view for position " + i + " isn't dirty but has wrong view type! (found " + viewHolder.getItemViewType() + " but expected " + i2 + ")");
                        break;
                    }
                    viewHolder.addFlags(32);
                    return viewHolder;
                }
            }
            if (!z && (viewFindHiddenNonRemovedView = RecyclerView.this.mChildHelper.findHiddenNonRemovedView(i, i2)) != null) {
                RecyclerView.this.mItemAnimator.endAnimation(RecyclerView.this.getChildViewHolder(viewFindHiddenNonRemovedView));
            }
            int size2 = this.mCachedViews.size();
            for (int i4 = 0; i4 < size2; i4++) {
                ViewHolder viewHolder2 = this.mCachedViews.get(i4);
                if (!viewHolder2.isInvalid() && viewHolder2.getLayoutPosition() == i) {
                    if (!z) {
                        this.mCachedViews.remove(i4);
                    }
                    return viewHolder2;
                }
            }
            return null;
        }

        public View getViewForPosition(int i) {
            return getViewForPosition(i, false);
        }

        /* JADX WARN: Code duplicated, block: B:81:0x0172  */
        /* JADX WARN: Code duplicated, block: B:82:0x0180  */
        /* JADX WARN: Code duplicated, block: B:84:0x0188  */
        /* JADX WARN: Code duplicated, block: B:85:0x0196  */
        /* JADX WARN: Code duplicated, block: B:90:0x019f  */
        View getViewForPosition(int i, boolean z) {
            ViewHolder viewHolderCreateViewHolder;
            boolean z2;
            /* REWORK: jadx left z3 uninitialized on one decompiled path; default it. */
            boolean z3 = false;
            ViewGroup.LayoutParams layoutParams;
            LayoutParams layoutParams2;
            ViewCacheExtension viewCacheExtension;
            View viewForPositionAndType;
            if (i < 0 || i >= RecyclerView.this.mState.getItemCount()) {
                throw new IndexOutOfBoundsException("Invalid item position " + i + "(" + i + "). Item count:" + RecyclerView.this.mState.getItemCount());
            }
            if (RecyclerView.this.mState.isPreLayout()) {
                viewHolderCreateViewHolder = getChangedScrapViewForPosition(i);
                z2 = viewHolderCreateViewHolder != null;
            } else {
                viewHolderCreateViewHolder = null;
                z2 = false;
            }
            if (viewHolderCreateViewHolder == null && (viewHolderCreateViewHolder = getScrapViewForPosition(i, -1, z)) != null) {
                if (validateViewHolderForOffsetPosition(viewHolderCreateViewHolder)) {
                    z2 = true;
                } else {
                    if (!z) {
                        viewHolderCreateViewHolder.addFlags(4);
                        if (viewHolderCreateViewHolder.isScrap()) {
                            RecyclerView.this.removeDetachedView(viewHolderCreateViewHolder.itemView, false);
                            viewHolderCreateViewHolder.unScrap();
                        } else if (viewHolderCreateViewHolder.wasReturnedFromScrap()) {
                            viewHolderCreateViewHolder.clearReturnedFromScrapFlag();
                        }
                        recycleViewHolderInternal(viewHolderCreateViewHolder);
                    }
                    viewHolderCreateViewHolder = null;
                }
            }
            if (viewHolderCreateViewHolder == null) {
                int iFindPositionOffset = RecyclerView.this.mAdapterHelper.findPositionOffset(i);
                if (iFindPositionOffset < 0 || iFindPositionOffset >= RecyclerView.this.mAdapter.getItemCount()) {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item position " + i + "(offset:" + iFindPositionOffset + ").state:" + RecyclerView.this.mState.getItemCount());
                }
                int itemViewType = RecyclerView.this.mAdapter.getItemViewType(iFindPositionOffset);
                if (RecyclerView.this.mAdapter.hasStableIds() && (viewHolderCreateViewHolder = getScrapViewForId(RecyclerView.this.mAdapter.getItemId(iFindPositionOffset), itemViewType, z)) != null) {
                    viewHolderCreateViewHolder.mPosition = iFindPositionOffset;
                    z2 = true;
                }
                if (viewHolderCreateViewHolder == null && (viewCacheExtension = this.mViewCacheExtension) != null && (viewForPositionAndType = viewCacheExtension.getViewForPositionAndType(this, i, itemViewType)) != null) {
                    viewHolderCreateViewHolder = RecyclerView.this.getChildViewHolder(viewForPositionAndType);
                    if (viewHolderCreateViewHolder == null) {
                        throw new IllegalArgumentException("getViewForPositionAndType returned a view which does not have a ViewHolder");
                    }
                    if (viewHolderCreateViewHolder.shouldIgnore()) {
                        throw new IllegalArgumentException("getViewForPositionAndType returned a view that is ignored. You must call stopIgnoring before returning this view.");
                    }
                }
                if (viewHolderCreateViewHolder == null) {
                    ViewHolder recycledView = getRecycledViewPool().getRecycledView(itemViewType);
                    if (recycledView != null) {
                        recycledView.resetInternal();
                        if (RecyclerView.FORCE_INVALIDATE_DISPLAY_LIST) {
                            invalidateDisplayListInt(recycledView);
                        }
                    }
                    viewHolderCreateViewHolder = recycledView;
                }
                if (viewHolderCreateViewHolder == null) {
                    viewHolderCreateViewHolder = RecyclerView.this.mAdapter.createViewHolder(RecyclerView.this, itemViewType);
                }
            }
            if (!RecyclerView.this.mState.isPreLayout() || !viewHolderCreateViewHolder.isBound()) {
                if (!viewHolderCreateViewHolder.isBound() || viewHolderCreateViewHolder.needsUpdate() || viewHolderCreateViewHolder.isInvalid()) {
                    int iFindPositionOffset2 = RecyclerView.this.mAdapterHelper.findPositionOffset(i);
                    RecyclerView recyclerView = RecyclerView.this;
                    viewHolderCreateViewHolder.mOwnerRecyclerView = recyclerView;
                    recyclerView.mAdapter.bindViewHolder(viewHolderCreateViewHolder, iFindPositionOffset2);
                    attachAccessibilityDelegate(viewHolderCreateViewHolder.itemView);
                    if (RecyclerView.this.mState.isPreLayout()) {
                        viewHolderCreateViewHolder.mPreLayoutPosition = i;
                    }
                    z3 = true;
                }
                layoutParams = viewHolderCreateViewHolder.itemView.getLayoutParams();
                if (layoutParams == null) {
                    layoutParams2 = (LayoutParams) RecyclerView.this.generateDefaultLayoutParams();
                    viewHolderCreateViewHolder.itemView.setLayoutParams(layoutParams2);
                } else if (RecyclerView.this.checkLayoutParams(layoutParams)) {
                    layoutParams2 = (LayoutParams) layoutParams;
                } else {
                    layoutParams2 = (LayoutParams) RecyclerView.this.generateLayoutParams(layoutParams);
                    viewHolderCreateViewHolder.itemView.setLayoutParams(layoutParams2);
                }
                layoutParams2.mViewHolder = viewHolderCreateViewHolder;
                layoutParams2.mPendingInvalidate = !z2 && z3;
                return viewHolderCreateViewHolder.itemView;
            }
            viewHolderCreateViewHolder.mPreLayoutPosition = i;
            z3 = false;
            layoutParams = viewHolderCreateViewHolder.itemView.getLayoutParams();
            if (layoutParams == null) {
                layoutParams2 = (LayoutParams) RecyclerView.this.generateDefaultLayoutParams();
                viewHolderCreateViewHolder.itemView.setLayoutParams(layoutParams2);
            } else if (RecyclerView.this.checkLayoutParams(layoutParams)) {
                layoutParams2 = (LayoutParams) RecyclerView.this.generateLayoutParams(layoutParams);
                viewHolderCreateViewHolder.itemView.setLayoutParams(layoutParams2);
            } else {
                layoutParams2 = (LayoutParams) layoutParams;
            }
            layoutParams2.mViewHolder = viewHolderCreateViewHolder;
            layoutParams2.mPendingInvalidate = !z2 && z3;
            return viewHolderCreateViewHolder.itemView;
        }

        void markItemDecorInsetsDirty() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                LayoutParams layoutParams = (LayoutParams) this.mCachedViews.get(i).itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.mInsetsDirty = true;
                }
            }
        }

        void markKnownViewsInvalid() {
            if (RecyclerView.this.mAdapter == null || !RecyclerView.this.mAdapter.hasStableIds()) {
                recycleAndClearCachedViews();
                return;
            }
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                ViewHolder viewHolder = this.mCachedViews.get(i);
                if (viewHolder != null) {
                    viewHolder.addFlags(6);
                }
            }
        }

        void offsetPositionRecordsForInsert(int i, int i2) {
            int size = this.mCachedViews.size();
            for (int i3 = 0; i3 < size; i3++) {
                ViewHolder viewHolder = this.mCachedViews.get(i3);
                if (viewHolder != null && viewHolder.getLayoutPosition() >= i) {
                    viewHolder.offsetPosition(i2, true);
                }
            }
        }

        void offsetPositionRecordsForMove(int i, int i2) {
            int i3;
            int i4;
            int i5;
            if (i < i2) {
                i4 = i;
                i3 = i2;
                i5 = -1;
            } else {
                i3 = i;
                i4 = i2;
                i5 = 1;
            }
            int size = this.mCachedViews.size();
            for (int i6 = 0; i6 < size; i6++) {
                ViewHolder viewHolder = this.mCachedViews.get(i6);
                if (viewHolder != null && viewHolder.mPosition >= i4 && viewHolder.mPosition <= i3) {
                    if (viewHolder.mPosition == i) {
                        viewHolder.offsetPosition(i2 - i, false);
                    } else {
                        viewHolder.offsetPosition(i5, false);
                    }
                }
            }
        }

        void offsetPositionRecordsForRemove(int i, int i2, boolean z) {
            int i3 = i + i2;
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                ViewHolder viewHolder = this.mCachedViews.get(size);
                if (viewHolder != null) {
                    if (viewHolder.getLayoutPosition() >= i3) {
                        viewHolder.offsetPosition(-i2, z);
                    } else if (viewHolder.getLayoutPosition() >= i) {
                        viewHolder.addFlags(8);
                        recycleCachedViewAt(size);
                    }
                }
            }
        }

        void onAdapterChanged(Adapter adapter, Adapter adapter2, boolean z) {
            clear();
            getRecycledViewPool().onAdapterChanged(adapter, adapter2, z);
        }

        void quickRecycleScrapView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            childViewHolderInt.mScrapContainer = null;
            childViewHolderInt.clearReturnedFromScrapFlag();
            recycleViewHolderInternal(childViewHolderInt);
        }

        void recycleAndClearCachedViews() {
            for (int size = this.mCachedViews.size() - 1; size >= 0; size--) {
                recycleCachedViewAt(size);
            }
            this.mCachedViews.clear();
        }

        void recycleCachedViewAt(int i) {
            addViewHolderToRecycledViewPool(this.mCachedViews.get(i));
            this.mCachedViews.remove(i);
        }

        public void recycleView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            if (childViewHolderInt.isTmpDetached()) {
                RecyclerView.this.removeDetachedView(view, false);
            }
            if (childViewHolderInt.isScrap()) {
                childViewHolderInt.unScrap();
            } else if (childViewHolderInt.wasReturnedFromScrap()) {
                childViewHolderInt.clearReturnedFromScrapFlag();
            }
            recycleViewHolderInternal(childViewHolderInt);
        }

        /* JADX WARN: Code duplicated, block: B:38:0x007c  */
        void recycleViewHolderInternal(ViewHolder viewHolder) {
            boolean z;
            boolean z2 = false;
            if (viewHolder.isScrap() || viewHolder.itemView.getParent() != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Scrapped or attached views may not be recycled. isScrap:");
                sb.append(viewHolder.isScrap());
                sb.append(" isAttached:");
                sb.append(viewHolder.itemView.getParent() != null);
                throw new IllegalArgumentException(sb.toString());
            }
            if (viewHolder.isTmpDetached()) {
                throw new IllegalArgumentException("Tmp detached view should be removed from RecyclerView before it can be recycled: " + viewHolder);
            }
            if (viewHolder.shouldIgnore()) {
                throw new IllegalArgumentException("Trying to recycle an ignored view holder. You should first call stopIgnoringView(view) before calling recycle.");
            }
            boolean zDoesTransientStatePreventRecycling = viewHolder.doesTransientStatePreventRecycling();
            if ((RecyclerView.this.mAdapter != null && zDoesTransientStatePreventRecycling && RecyclerView.this.mAdapter.onFailedToRecycleView(viewHolder)) || viewHolder.isRecyclable()) {
                if (viewHolder.isInvalid() || ((!RecyclerView.this.mState.mInPreLayout && viewHolder.isRemoved()) || viewHolder.isChanged())) {
                    z = false;
                } else {
                    int size = this.mCachedViews.size();
                    if (size == this.mViewCacheMax && size > 0) {
                        recycleCachedViewAt(0);
                    }
                    if (size < this.mViewCacheMax) {
                        this.mCachedViews.add(viewHolder);
                        z = true;
                    } else {
                        z = false;
                    }
                }
                if (!z) {
                    addViewHolderToRecycledViewPool(viewHolder);
                    z2 = true;
                }
            } else {
                z = false;
            }
            RecyclerView.this.mState.onViewRecycled(viewHolder);
            if (z || z2 || !zDoesTransientStatePreventRecycling) {
                return;
            }
            viewHolder.mOwnerRecyclerView = null;
        }

        void scrapView(View view) {
            ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
            childViewHolderInt.setScrapContainer(this);
            if (childViewHolderInt.isChanged() && RecyclerView.this.supportsChangeAnimations()) {
                if (this.mChangedScrap == null) {
                    this.mChangedScrap = new ArrayList<>();
                }
                this.mChangedScrap.add(childViewHolderInt);
            } else {
                if (childViewHolderInt.isInvalid() && !childViewHolderInt.isRemoved() && !RecyclerView.this.mAdapter.hasStableIds()) {
                    throw new IllegalArgumentException("Called scrap view with an invalid view. Invalid views cannot be reused from scrap, they should rebound from recycler pool.");
                }
                this.mAttachedScrap.add(childViewHolderInt);
            }
        }

        void setAdapterPositionsAsUnknown() {
            int size = this.mCachedViews.size();
            for (int i = 0; i < size; i++) {
                ViewHolder viewHolder = this.mCachedViews.get(i);
                if (viewHolder != null) {
                    viewHolder.addFlags(512);
                }
            }
        }

        void setRecycledViewPool(RecycledViewPool recycledViewPool) {
            RecycledViewPool recycledViewPool2 = this.mRecyclerPool;
            if (recycledViewPool2 != null) {
                recycledViewPool2.detach();
            }
            this.mRecyclerPool = recycledViewPool;
            if (recycledViewPool != null) {
                this.mRecyclerPool.attach(RecyclerView.this.getAdapter());
            }
        }

        void setViewCacheExtension(ViewCacheExtension viewCacheExtension) {
            this.mViewCacheExtension = viewCacheExtension;
        }

        public void setViewCacheSize(int i) {
            this.mViewCacheMax = i;
            for (int size = this.mCachedViews.size() - 1; size >= 0 && this.mCachedViews.size() > i; size--) {
                recycleCachedViewAt(size);
            }
        }

        void unscrapView(ViewHolder viewHolder) {
            ArrayList<ViewHolder> arrayList;
            if (viewHolder.isChanged() && RecyclerView.this.supportsChangeAnimations() && (arrayList = this.mChangedScrap) != null) {
                arrayList.remove(viewHolder);
            } else {
                this.mAttachedScrap.remove(viewHolder);
            }
            viewHolder.mScrapContainer = null;
            viewHolder.clearReturnedFromScrapFlag();
        }

        boolean validateViewHolderForOffsetPosition(ViewHolder viewHolder) {
            if (viewHolder.isRemoved()) {
                return true;
            }
            if (viewHolder.mPosition < 0 || viewHolder.mPosition >= RecyclerView.this.mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid view holder adapter position" + viewHolder);
            }
            if (RecyclerView.this.mState.isPreLayout() || RecyclerView.this.mAdapter.getItemViewType(viewHolder.mPosition) == viewHolder.getItemViewType()) {
                return !RecyclerView.this.mAdapter.hasStableIds() || viewHolder.getItemId() == RecyclerView.this.mAdapter.getItemId(viewHolder.mPosition);
            }
            return false;
        }

        void viewRangeUpdate(int i, int i2) {
            int layoutPosition;
            int i3 = i2 + i;
            int size = this.mCachedViews.size();
            for (int i4 = 0; i4 < size; i4++) {
                ViewHolder viewHolder = this.mCachedViews.get(i4);
                if (viewHolder != null && (layoutPosition = viewHolder.getLayoutPosition()) >= i && layoutPosition < i3) {
                    viewHolder.addFlags(2);
                }
            }
        }
    }

    public interface RecyclerListener {
        void onViewRecycled(ViewHolder viewHolder);
    }

    private class RecyclerViewDataObserver extends AdapterDataObserver {
        private RecyclerViewDataObserver() {
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onChanged() {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapter.hasStableIds()) {
                RecyclerView.this.mState.mStructureChanged = true;
                RecyclerView.this.setDataSetChangedAfterLayout();
            } else {
                RecyclerView.this.mState.mStructureChanged = true;
                RecyclerView.this.setDataSetChangedAfterLayout();
            }
            if (RecyclerView.this.mAdapterHelper.hasPendingUpdates()) {
                return;
            }
            RecyclerView.this.requestLayout();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeChanged(int i, int i2) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeChanged(i, i2)) {
                triggerUpdateProcessor();
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeInserted(int i, int i2) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeInserted(i, i2)) {
                triggerUpdateProcessor();
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeRemoved(int i, int i2) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeRemoved(i, i2)) {
                triggerUpdateProcessor();
            }
        }

        void triggerUpdateProcessor() {
            if (RecyclerView.this.mPostUpdatesOnAnimation && RecyclerView.this.mHasFixedSize && RecyclerView.this.mIsAttached) {
                RecyclerView recyclerView = RecyclerView.this;
                ViewCompat.postOnAnimation(recyclerView, recyclerView.mUpdateChildViewsRunnable);
            } else {
                RecyclerView.this.mAdapterUpdateDuringMeasure = true;
                RecyclerView.this.requestLayout();
            }
        }
    }

    static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() { // from class: com.voxel.recyclerview.widget.RecyclerView.SavedState.1
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        Parcelable mLayoutState;

        SavedState(Parcel parcel) {
            super(parcel);
            this.mLayoutState = parcel.readParcelable(LayoutManager.class.getClassLoader());
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void copyFrom(SavedState savedState) {
            this.mLayoutState = savedState.mLayoutState;
        }

        @Override // android.view.View.BaseSavedState, android.view.AbsSavedState, android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeParcelable(this.mLayoutState, 0);
        }
    }

    public static abstract class SmoothScroller {
        private LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private RecyclerView mRecyclerView;
        /* REWORK: jadx lost the init; the action is a scratch buffer per scroller. */
        private final Action mRecyclingAction = new Action();
        private boolean mRunning;
        private int mTargetPosition;
        private View mTargetView;

        public static class Action {
            private boolean changed;
            private int consecutiveUpdates;
            private int mDuration;
            private int mDx;
            private int mDy;
            private Interpolator mInterpolator;

            /* JADX INFO: Access modifiers changed from: private */
            public void runIfNecessary(RecyclerView recyclerView) {
                if (!this.changed) {
                    this.consecutiveUpdates = 0;
                    return;
                }
                validate();
                if (this.mInterpolator != null) {
                    recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration, this.mInterpolator);
                } else if (this.mDuration == Integer.MIN_VALUE) {
                    recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy);
                } else {
                    recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration);
                }
                this.consecutiveUpdates++;
                if (this.consecutiveUpdates > 10) {
                    Log.e("RecyclerView", "Smooth Scroll action is being updated too frequently. Make sure you are not changing it unless necessary");
                }
                this.changed = false;
            }

            private void validate() {
                if (this.mInterpolator != null && this.mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must set a positive duration");
                }
                if (this.mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }
        }

        public int getChildPosition(View view) {
            return this.mRecyclerView.getChildLayoutPosition(view);
        }

        public int getTargetPosition() {
            return this.mTargetPosition;
        }

        public boolean isPendingInitialRun() {
            return this.mPendingInitialRun;
        }

        public boolean isRunning() {
            return this.mRunning;
        }

        protected void onAnimation(int i, int i2) {
            if (!this.mRunning || this.mTargetPosition == -1) {
                stop();
            }
            this.mPendingInitialRun = false;
            View view = this.mTargetView;
            if (view != null) {
                if (getChildPosition(view) == this.mTargetPosition) {
                    onTargetFound(this.mTargetView, this.mRecyclerView.mState, this.mRecyclingAction);
                    this.mRecyclingAction.runIfNecessary(this.mRecyclerView);
                    stop();
                } else {
                    Log.e("RecyclerView", "Passed over target position while smooth scrolling.");
                    this.mTargetView = null;
                }
            }
            if (this.mRunning) {
                onSeekTargetStep(i, i2, this.mRecyclerView.mState, this.mRecyclingAction);
                this.mRecyclingAction.runIfNecessary(this.mRecyclerView);
            }
        }

        protected void onChildAttachedToWindow(View view) {
            if (getChildPosition(view) == getTargetPosition()) {
                this.mTargetView = view;
            }
        }

        protected abstract void onSeekTargetStep(int i, int i2, State state, Action action);

        protected abstract void onStop();

        protected abstract void onTargetFound(View view, State state, Action action);

        public void setTargetPosition(int i) {
            this.mTargetPosition = i;
        }

        protected final void stop() {
            if (this.mRunning) {
                onStop();
                this.mRecyclerView.mState.mTargetPosition = -1;
                this.mTargetView = null;
                this.mTargetPosition = -1;
                this.mPendingInitialRun = false;
                this.mRunning = false;
                this.mLayoutManager.onSmoothScrollerStopped(this);
                this.mLayoutManager = null;
                this.mRecyclerView = null;
            }
        }
    }

    public static class State {
        private SparseArray<Object> mData;
        private int mTargetPosition = -1;
        ArrayMap<ViewHolder, ItemHolderInfo> mPreLayoutHolderMap = new ArrayMap<>();
        ArrayMap<ViewHolder, ItemHolderInfo> mPostLayoutHolderMap = new ArrayMap<>();
        ArrayMap<Long, ViewHolder> mOldChangedHolders = new ArrayMap<>();
        int mItemCount = 0;
        private int mPreviousLayoutItemCount = 0;
        private int mDeletedInvisibleItemCountSincePreviousLayout = 0;
        private boolean mStructureChanged = false;
        private boolean mInPreLayout = false;
        private boolean mRunSimpleAnimations = false;
        private boolean mRunPredictiveAnimations = false;

        private void removeFrom(ArrayMap<Long, ViewHolder> arrayMap, ViewHolder viewHolder) {
            for (int size = arrayMap.size() - 1; size >= 0; size--) {
                if (viewHolder == arrayMap.valueAt(size)) {
                    arrayMap.removeAt(size);
                    return;
                }
            }
        }

        public int getItemCount() {
            return this.mInPreLayout ? this.mPreviousLayoutItemCount - this.mDeletedInvisibleItemCountSincePreviousLayout : this.mItemCount;
        }

        public boolean hasTargetScrollPosition() {
            return this.mTargetPosition != -1;
        }

        public boolean isPreLayout() {
            return this.mInPreLayout;
        }

        void onViewRecycled(ViewHolder viewHolder) {
            this.mPreLayoutHolderMap.remove(viewHolder);
            this.mPostLayoutHolderMap.remove(viewHolder);
            ArrayMap<Long, ViewHolder> arrayMap = this.mOldChangedHolders;
            if (arrayMap != null) {
                removeFrom(arrayMap, viewHolder);
            }
        }

        public String toString() {
            return "State{mTargetPosition=" + this.mTargetPosition + ", mPreLayoutHolderMap=" + this.mPreLayoutHolderMap + ", mPostLayoutHolderMap=" + this.mPostLayoutHolderMap + ", mData=" + this.mData + ", mItemCount=" + this.mItemCount + ", mPreviousLayoutItemCount=" + this.mPreviousLayoutItemCount + ", mDeletedInvisibleItemCountSincePreviousLayout=" + this.mDeletedInvisibleItemCountSincePreviousLayout + ", mStructureChanged=" + this.mStructureChanged + ", mInPreLayout=" + this.mInPreLayout + ", mRunSimpleAnimations=" + this.mRunSimpleAnimations + ", mRunPredictiveAnimations=" + this.mRunPredictiveAnimations + '}';
        }

        public boolean willRunPredictiveAnimations() {
            return this.mRunPredictiveAnimations;
        }
    }

    public static abstract class ViewCacheExtension {
        public abstract View getViewForPositionAndType(Recycler recycler, int i, int i2);
    }

    private class ViewFlinger implements IViewFlinger {
        private int mLastFlingX;
        private int mLastFlingY;
        private ScrollerCompat mScroller;
        private Interpolator mInterpolator = RecyclerView.sQuinticInterpolator;
        private boolean mEatRunOnAnimationRequest = false;
        private boolean mReSchedulePostAnimationCallback = false;

        public ViewFlinger() {
            this.mScroller = ScrollerCompat.create(RecyclerView.this.getContext(), RecyclerView.sQuinticInterpolator);
        }

        private int computeScrollDuration(int i, int i2, int i3, int i4) {
            int iRound;
            int iAbs = Math.abs(i);
            int iAbs2 = Math.abs(i2);
            boolean z = iAbs > iAbs2;
            int iSqrt = (int) Math.sqrt((i3 * i3) + (i4 * i4));
            int iSqrt2 = (int) Math.sqrt((i * i) + (i2 * i2));
            int width = z ? RecyclerView.this.getWidth() : RecyclerView.this.getHeight();
            int i5 = width / 2;
            float f = width;
            float f2 = i5;
            float fDistanceInfluenceForSnapDuration = f2 + (distanceInfluenceForSnapDuration(Math.min(1.0f, (iSqrt2 * 1.0f) / f)) * f2);
            if (iSqrt > 0) {
                iRound = Math.round(Math.abs(fDistanceInfluenceForSnapDuration / iSqrt) * 1000.0f) * 4;
            } else {
                if (!z) {
                    iAbs = iAbs2;
                }
                iRound = (int) (((iAbs / f) + 1.0f) * 300.0f);
            }
            return Math.min(iRound, 2000);
        }

        private void disableRunOnAnimationRequests() {
            this.mReSchedulePostAnimationCallback = false;
            this.mEatRunOnAnimationRequest = true;
        }

        private float distanceInfluenceForSnapDuration(float f) {
            double d = f - 0.5f;
            Double.isNaN(d);
            return (float) Math.sin((float) (d * 0.4712389167638204d));
        }

        private void enableRunOnAnimationRequests() {
            this.mEatRunOnAnimationRequest = false;
            if (this.mReSchedulePostAnimationCallback) {
                postOnAnimation();
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void fling(int i, int i2) {
            RecyclerView.this.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.fling(0, 0, i, i2, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }

        public void postOnAnimation() {
            if (this.mEatRunOnAnimationRequest) {
                this.mReSchedulePostAnimationCallback = true;
            } else {
                RecyclerView.this.removeCallbacks(this);
                ViewCompat.postOnAnimation(RecyclerView.this, this);
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            int i;
            boolean z;
            int i2;
            int i3;
            int i4;
            int i5;
            int i6;
            int iScrollHorizontallyBy;
            int iScrollVerticallyBy;
            int i7;
            int i8;
            int i9;
            disableRunOnAnimationRequests();
            RecyclerView.this.consumePendingUpdateOperations();
            ScrollerCompat scrollerCompat = this.mScroller;
            SmoothScroller smoothScroller = RecyclerView.this.mLayout.mSmoothScroller;
            if (scrollerCompat.computeScrollOffset()) {
                int currX = scrollerCompat.getCurrX();
                int currY = scrollerCompat.getCurrY();
                int i10 = currX - this.mLastFlingX;
                int i11 = currY - this.mLastFlingY;
                this.mLastFlingX = currX;
                this.mLastFlingY = currY;
                if (RecyclerView.this.mAdapter != null) {
                    RecyclerView.this.eatRequestLayout();
                    RecyclerView recyclerView = RecyclerView.this;
                    recyclerView.mRunningLayoutOrScroll = true;
                    if (i10 != 0) {
                        iScrollHorizontallyBy = recyclerView.mLayout.scrollHorizontallyBy(i10, RecyclerView.this.mRecycler, RecyclerView.this.mState);
                        i4 = i10 - iScrollHorizontallyBy;
                    } else {
                        iScrollHorizontallyBy = 0;
                        i4 = 0;
                    }
                    if (i11 != 0) {
                        iScrollVerticallyBy = RecyclerView.this.mLayout.scrollVerticallyBy(i11, RecyclerView.this.mRecycler, RecyclerView.this.mState);
                        i5 = i11 - iScrollVerticallyBy;
                    } else {
                        iScrollVerticallyBy = 0;
                        i5 = 0;
                    }
                    if (RecyclerView.this.supportsChangeAnimations()) {
                        int childCount = RecyclerView.this.mChildHelper.getChildCount();
                        int i12 = 0;
                        while (i12 < childCount) {
                            View childAt = RecyclerView.this.mChildHelper.getChildAt(i12);
                            ViewHolder childViewHolder = RecyclerView.this.getChildViewHolder(childAt);
                            if (childViewHolder == null || childViewHolder.mShadowingHolder == null) {
                                i9 = iScrollHorizontallyBy;
                            } else {
                                View view = childViewHolder.mShadowingHolder.itemView;
                                int left = childAt.getLeft();
                                int top = childAt.getTop();
                                i9 = iScrollHorizontallyBy;
                                if (left != view.getLeft() || top != view.getTop()) {
                                    view.layout(left, top, view.getWidth() + left, top + view.getHeight());
                                }
                            }
                            i12++;
                            iScrollHorizontallyBy = i9;
                            iScrollVerticallyBy = iScrollVerticallyBy;
                        }
                        i7 = iScrollHorizontallyBy;
                        i8 = iScrollVerticallyBy;
                    } else {
                        i7 = iScrollHorizontallyBy;
                        i8 = iScrollVerticallyBy;
                    }
                    if (smoothScroller == null || smoothScroller.isPendingInitialRun() || !smoothScroller.isRunning()) {
                        z = true;
                    } else {
                        int itemCount = RecyclerView.this.mState.getItemCount();
                        if (itemCount == 0) {
                            smoothScroller.stop();
                            z = true;
                        } else if (smoothScroller.getTargetPosition() >= itemCount) {
                            z = true;
                            smoothScroller.setTargetPosition(itemCount - 1);
                            smoothScroller.onAnimation(i10 - i4, i11 - i5);
                        } else {
                            z = true;
                            smoothScroller.onAnimation(i10 - i4, i11 - i5);
                        }
                    }
                    RecyclerView recyclerView2 = RecyclerView.this;
                    recyclerView2.mRunningLayoutOrScroll = false;
                    recyclerView2.resumeRequestLayout(false);
                    i2 = i7;
                    i3 = i8;
                } else {
                    z = true;
                    i2 = 0;
                    i3 = 0;
                    i4 = 0;
                    i5 = 0;
                }
                if (!RecyclerView.this.mItemDecorations.isEmpty()) {
                    RecyclerView.this.invalidate();
                }
                if (ViewCompat.getOverScrollMode(RecyclerView.this) != 2) {
                    RecyclerView.this.considerReleasingGlowsOnScroll(i10, i11);
                }
                if (i4 != 0 || i5 != 0) {
                    int currVelocity = (int) scrollerCompat.getCurrVelocity();
                    if (i4 == currX) {
                        i6 = 0;
                    } else if (i4 < 0) {
                        i6 = -currVelocity;
                    } else {
                        i6 = i4 > 0 ? currVelocity : 0;
                    }
                    if (i5 == currY) {
                        currVelocity = 0;
                    } else if (i5 < 0) {
                        currVelocity = -currVelocity;
                    } else if (i5 <= 0) {
                        currVelocity = 0;
                    }
                    if (ViewCompat.getOverScrollMode(RecyclerView.this) != 2) {
                        RecyclerView.this.absorbGlows(i6, currVelocity);
                    }
                    if ((i6 != 0 || i4 == currX || scrollerCompat.getFinalX() == 0) && (currVelocity != 0 || i5 == currY || scrollerCompat.getFinalY() == 0)) {
                        scrollerCompat.abortAnimation();
                    }
                }
                if (i2 != 0 || i3 != 0) {
                    RecyclerView.this.notifyOnScrolled(i2, i3);
                }
                if (!RecyclerView.this.awakenScrollBars()) {
                    RecyclerView.this.invalidate();
                }
                boolean z2 = i11 != 0 && RecyclerView.this.mLayout.canScrollVertically() && i3 == i11;
                boolean z3 = i10 != 0 && RecyclerView.this.mLayout.canScrollHorizontally() && i2 == i10;
                if ((i10 != 0 || i11 != 0) && !z3 && !z2) {
                    z = false;
                }
                if (scrollerCompat.isFinished() || !z) {
                    i = 0;
                    RecyclerView.this.setScrollState(0);
                } else {
                    postOnAnimation();
                    i = 0;
                }
            } else {
                i = 0;
            }
            if (smoothScroller != null && smoothScroller.isPendingInitialRun()) {
                smoothScroller.onAnimation(i, i);
            }
            enableRunOnAnimationRequests();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void smoothScrollBy(int i, int i2) {
            smoothScrollBy(i, i2, 0, 0);
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void smoothScrollBy(int i, int i2, int i3) {
            smoothScrollBy(i, i2, i3, RecyclerView.sQuinticInterpolator);
        }

        public void smoothScrollBy(int i, int i2, int i3, int i4) {
            smoothScrollBy(i, i2, computeScrollDuration(i, i2, i3, i4));
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void smoothScrollBy(int i, int i2, int i3, Interpolator interpolator) {
            if (this.mInterpolator != interpolator) {
                this.mInterpolator = interpolator;
                this.mScroller = ScrollerCompat.create(RecyclerView.this.getContext(), interpolator);
            }
            RecyclerView.this.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.startScroll(0, 0, i, i2, i3);
            postOnAnimation();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void stop() {
            RecyclerView.this.removeCallbacks(this);
            this.mScroller.abortAnimation();
        }
    }

    public static abstract class ViewHolder {
        public final View itemView;
        private int mFlags;
        RecyclerView mOwnerRecyclerView;
        int mPosition = -1;
        int mOldPosition = -1;
        long mItemId = -1;
        int mItemViewType = -1;
        int mPreLayoutPosition = -1;
        ViewHolder mShadowedHolder = null;
        ViewHolder mShadowingHolder = null;
        private int mIsRecyclableCount = 0;
        private Recycler mScrapContainer = null;

        public ViewHolder(View view) {
            if (view == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = view;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean doesTransientStatePreventRecycling() {
            return (this.mFlags & 16) == 0 && ViewCompat.hasTransientState(this.itemView);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean shouldBeKeptAsChild() {
            return (this.mFlags & 16) != 0;
        }

        void addFlags(int i) {
            this.mFlags = i | this.mFlags;
        }

        void clearOldPosition() {
            this.mOldPosition = -1;
            this.mPreLayoutPosition = -1;
        }

        void clearReturnedFromScrapFlag() {
            this.mFlags &= -33;
        }

        void clearTmpDetachFlag() {
            this.mFlags &= -257;
        }

        void flagRemovedAndOffsetPosition(int i, int i2, boolean z) {
            addFlags(8);
            offsetPosition(i2, z);
            this.mPosition = i;
        }

        public final int getAdapterPosition() {
            RecyclerView recyclerView = this.mOwnerRecyclerView;
            if (recyclerView == null) {
                return -1;
            }
            return recyclerView.getAdapterPositionFor(this);
        }

        public final long getItemId() {
            return this.mItemId;
        }

        public final int getItemViewType() {
            return this.mItemViewType;
        }

        public final int getLayoutPosition() {
            int i = this.mPreLayoutPosition;
            return i == -1 ? this.mPosition : i;
        }

        boolean hasAnyOfTheFlags(int i) {
            return (i & this.mFlags) != 0;
        }

        boolean isAdapterPositionUnknown() {
            return (this.mFlags & 512) != 0 || isInvalid();
        }

        boolean isBound() {
            return (this.mFlags & 1) != 0;
        }

        boolean isChanged() {
            return (this.mFlags & 64) != 0;
        }

        boolean isInvalid() {
            return (this.mFlags & 4) != 0;
        }

        public final boolean isRecyclable() {
            return (this.mFlags & 16) == 0 && !ViewCompat.hasTransientState(this.itemView);
        }

        boolean isRemoved() {
            return (this.mFlags & 8) != 0;
        }

        boolean isScrap() {
            return this.mScrapContainer != null;
        }

        boolean isTmpDetached() {
            return (this.mFlags & 256) != 0;
        }

        boolean needsUpdate() {
            return (this.mFlags & 2) != 0;
        }

        void offsetPosition(int i, boolean z) {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
            if (this.mPreLayoutPosition == -1) {
                this.mPreLayoutPosition = this.mPosition;
            }
            if (z) {
                this.mPreLayoutPosition += i;
            }
            this.mPosition += i;
            if (this.itemView.getLayoutParams() != null) {
                ((LayoutParams) this.itemView.getLayoutParams()).mInsetsDirty = true;
            }
        }

        void resetInternal() {
            this.mFlags = 0;
            this.mPosition = -1;
            this.mOldPosition = -1;
            this.mItemId = -1L;
            this.mPreLayoutPosition = -1;
            this.mIsRecyclableCount = 0;
            this.mShadowedHolder = null;
            this.mShadowingHolder = null;
        }

        void saveOldPosition() {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
        }

        void setFlags(int i, int i2) {
            this.mFlags = (i & i2) | (this.mFlags & (i2 ^ (-1)));
        }

        public final void setIsRecyclable(boolean z) {
            this.mIsRecyclableCount = z ? this.mIsRecyclableCount - 1 : this.mIsRecyclableCount + 1;
            int i = this.mIsRecyclableCount;
            if (i < 0) {
                this.mIsRecyclableCount = 0;
                Log.e("View", "isRecyclable decremented below 0: unmatched pair of setIsRecyable() calls for " + this);
                return;
            }
            if (!z && i == 1) {
                this.mFlags |= 16;
            } else if (z && this.mIsRecyclableCount == 0) {
                this.mFlags &= -17;
            }
        }

        void setScrapContainer(Recycler recycler) {
            this.mScrapContainer = recycler;
        }

        boolean shouldIgnore() {
            return (this.mFlags & 128) != 0;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("ViewHolder{" + Integer.toHexString(hashCode()) + " position=" + this.mPosition + " id=" + this.mItemId + ", oldPos=" + this.mOldPosition + ", pLpos:" + this.mPreLayoutPosition);
            if (isScrap()) {
                sb.append(" scrap");
            }
            if (isInvalid()) {
                sb.append(" invalid");
            }
            if (!isBound()) {
                sb.append(" unbound");
            }
            if (needsUpdate()) {
                sb.append(" update");
            }
            if (isRemoved()) {
                sb.append(" removed");
            }
            if (shouldIgnore()) {
                sb.append(" ignored");
            }
            if (isChanged()) {
                sb.append(" changed");
            }
            if (isTmpDetached()) {
                sb.append(" tmpDetached");
            }
            if (!isRecyclable()) {
                sb.append(" not recyclable(" + this.mIsRecyclableCount + ")");
            }
            if (isAdapterPositionUnknown()) {
                sb.append("undefined adapter position");
            }
            if (this.itemView.getParent() == null) {
                sb.append(" no parent");
            }
            sb.append("}");
            return sb.toString();
        }

        void unScrap() {
            this.mScrapContainer.unscrapView(this);
        }

        boolean wasReturnedFromScrap() {
            return (this.mFlags & 32) != 0;
        }
    }

    static {
        FORCE_INVALIDATE_DISPLAY_LIST = Build.VERSION.SDK_INT == 18 || Build.VERSION.SDK_INT == 19 || Build.VERSION.SDK_INT == 20;
        sQuinticInterpolator = new Interpolator() { // from class: com.voxel.recyclerview.widget.RecyclerView.3
            @Override // android.animation.TimeInterpolator
            public float getInterpolation(float f) {
                float f2 = f - 1.0f;
                return (f2 * f2 * f2 * f2 * f2) + 1.0f;
            }
        };
    }

    public RecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mObserver = new RecyclerViewDataObserver();
        this.mRecycler = new Recycler();
        this.mDisappearingViewsInLayoutPass = new ArrayList();
        this.mUpdateChildViewsRunnable = new Runnable() { // from class: com.voxel.recyclerview.widget.RecyclerView.1
            @Override // java.lang.Runnable
            public void run() {
                if (RecyclerView.this.mFirstLayoutComplete) {
                    if (RecyclerView.this.mDataSetHasChangedAfterLayout) {
                        RecyclerView.this.dispatchLayout();
                        return;
                    }
                    if (RecyclerView.this.mAdapterHelper.hasPendingUpdates()) {
                        RecyclerView.this.eatRequestLayout();
                        RecyclerView.this.mAdapterHelper.preProcess();
                        if (!RecyclerView.this.mLayoutRequestEaten) {
                            RecyclerView.this.rebindUpdatedViewHolders();
                        }
                        RecyclerView.this.resumeRequestLayout(true);
                    }
                }
            }
        };
        this.mTempRect = new Rect();
        this.mItemDecorations = new ArrayList<>();
        this.mOnItemTouchListeners = new ArrayList<>();
        this.mDataSetHasChangedAfterLayout = false;
        this.mRunningLayoutOrScroll = false;
        this.mItemAnimator = new DefaultItemAnimator();
        this.mScrollState = 0;
        this.mScrollPointerId = -1;
        this.mScrollFactor = Float.MIN_VALUE;
        this.mViewFlinger = createViewFlinger();
        this.mState = new State();
        this.mItemsAddedOrRemoved = false;
        this.mItemsChanged = false;
        this.mItemAnimatorListener = new ItemAnimatorRestoreListener();
        this.mPostedAnimatorRunner = false;
        this.mMinMaxLayoutPositions = new int[2];
        this.mItemAnimatorRunner = new Runnable() { // from class: com.voxel.recyclerview.widget.RecyclerView.2
            @Override // java.lang.Runnable
            public void run() {
                if (RecyclerView.this.mItemAnimator != null) {
                    RecyclerView.this.mItemAnimator.runPendingAnimations();
                }
                RecyclerView.this.mPostedAnimatorRunner = false;
            }
        };
        setFocusableInTouchMode(true);
        this.mPostUpdatesOnAnimation = Build.VERSION.SDK_INT >= 16;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaxFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        setWillNotDraw(ViewCompat.getOverScrollMode(this) == 2);
        this.mItemAnimator.setListener(this.mItemAnimatorListener);
        initAdapterManager();
        initChildrenHelper();
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(this));
    }

    private void addAnimatingView(ViewHolder viewHolder) {
        View view = viewHolder.itemView;
        boolean z = view.getParent() == this;
        this.mRecycler.unscrapView(getChildViewHolder(view));
        if (viewHolder.isTmpDetached()) {
            this.mChildHelper.attachViewToParent(view, -1, view.getLayoutParams(), true);
        } else if (z) {
            this.mChildHelper.hide(view);
        } else {
            this.mChildHelper.addView(view, true);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addToDisappearingList(View view) {
        if (this.mDisappearingViewsInLayoutPass.contains(view)) {
            return;
        }
        this.mDisappearingViewsInLayoutPass.add(view);
    }

    private void animateAppearance(ViewHolder viewHolder, Rect rect, int i, int i2) {
        View view = viewHolder.itemView;
        if (rect == null || (rect.left == i && rect.top == i2)) {
            viewHolder.setIsRecyclable(false);
            if (this.mItemAnimator.animateAdd(viewHolder)) {
                postAnimationRunner();
                return;
            }
            return;
        }
        viewHolder.setIsRecyclable(false);
        if (this.mItemAnimator.animateMove(viewHolder, rect.left, rect.top, i, i2)) {
            postAnimationRunner();
        }
    }

    private void animateChange(ViewHolder viewHolder, ViewHolder viewHolder2) {
        int i;
        int i2;
        viewHolder.setIsRecyclable(false);
        addAnimatingView(viewHolder);
        viewHolder.mShadowedHolder = viewHolder2;
        this.mRecycler.unscrapView(viewHolder);
        int left = viewHolder.itemView.getLeft();
        int top = viewHolder.itemView.getTop();
        if (viewHolder2 == null || viewHolder2.shouldIgnore()) {
            i = left;
            i2 = top;
        } else {
            int left2 = viewHolder2.itemView.getLeft();
            int top2 = viewHolder2.itemView.getTop();
            viewHolder2.setIsRecyclable(false);
            viewHolder2.mShadowingHolder = viewHolder;
            i = left2;
            i2 = top2;
        }
        if (this.mItemAnimator.animateChange(viewHolder, viewHolder2, left, top, i, i2)) {
            postAnimationRunner();
        }
    }

    private void animateDisappearance(ItemHolderInfo itemHolderInfo) {
        View view = itemHolderInfo.holder.itemView;
        addAnimatingView(itemHolderInfo.holder);
        int i = itemHolderInfo.left;
        int i2 = itemHolderInfo.top;
        int left = view.getLeft();
        int top = view.getTop();
        if (i == left && i2 == top) {
            itemHolderInfo.holder.setIsRecyclable(false);
            if (this.mItemAnimator.animateRemove(itemHolderInfo.holder)) {
                postAnimationRunner();
                return;
            }
            return;
        }
        itemHolderInfo.holder.setIsRecyclable(false);
        view.layout(left, top, view.getWidth() + left, view.getHeight() + top);
        if (this.mItemAnimator.animateMove(itemHolderInfo.holder, i, i2, left, top)) {
            postAnimationRunner();
        }
    }

    private void cancelTouch() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.clear();
        }
        releaseGlows();
        setScrollState(0);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void defaultOnMeasure(int i, int i2) {
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        if (mode != Integer.MIN_VALUE && mode != 1073741824) {
            size = ViewCompat.getMinimumWidth(this);
        }
        if (mode2 != Integer.MIN_VALUE && mode2 != 1073741824) {
            size2 = ViewCompat.getMinimumHeight(this);
        }
        setMeasuredDimension(size, size2);
    }

    private boolean didChildRangeChange(int i, int i2) {
        int layoutPosition;
        int childCount = this.mChildHelper.getChildCount();
        if (childCount == 0) {
            return (i == 0 && i2 == 0) ? false : true;
        }
        for (int i3 = 0; i3 < childCount; i3++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i3));
            if (!childViewHolderInt.shouldIgnore() && ((layoutPosition = childViewHolderInt.getLayoutPosition()) < i || layoutPosition > i2)) {
                return true;
            }
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChildAttached(View view) {
        Adapter adapter = this.mAdapter;
        if (adapter != null) {
            adapter.onViewAttachedToWindow(getChildViewHolderInt(view));
        }
        onChildAttachedToWindow(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dispatchChildDetached(View view) {
        Adapter adapter = this.mAdapter;
        if (adapter != null) {
            adapter.onViewDetachedFromWindow(getChildViewHolderInt(view));
        }
        onChildDetachedFromWindow(view);
    }

    private boolean dispatchOnItemTouch(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        OnItemTouchListener onItemTouchListener = this.mActiveOnItemTouchListener;
        if (onItemTouchListener != null) {
            if (action != 0) {
                onItemTouchListener.onTouchEvent(this, motionEvent);
                if (action == 3 || action == 1) {
                    this.mActiveOnItemTouchListener = null;
                }
                return true;
            }
            this.mActiveOnItemTouchListener = null;
        }
        if (action != 0) {
            int size = this.mOnItemTouchListeners.size();
            for (int i = 0; i < size; i++) {
                OnItemTouchListener onItemTouchListener2 = this.mOnItemTouchListeners.get(i);
                if (onItemTouchListener2.onInterceptTouchEvent(this, motionEvent)) {
                    this.mActiveOnItemTouchListener = onItemTouchListener2;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean dispatchOnItemTouchIntercept(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 3 || action == 0) {
            this.mActiveOnItemTouchListener = null;
        }
        int size = this.mOnItemTouchListeners.size();
        for (int i = 0; i < size; i++) {
            OnItemTouchListener onItemTouchListener = this.mOnItemTouchListeners.get(i);
            if (onItemTouchListener.onInterceptTouchEvent(this, motionEvent) && action != 3) {
                this.mActiveOnItemTouchListener = onItemTouchListener;
                return true;
            }
        }
        return false;
    }

    private void findMinMaxChildLayoutPositions(int[] iArr) {
        int childCount = this.mChildHelper.getChildCount();
        if (childCount == 0) {
            iArr[0] = 0;
            iArr[1] = 0;
            return;
        }
        int i = Integer.MAX_VALUE;
        int i2 = Integer.MIN_VALUE;
        for (int i3 = 0; i3 < childCount; i3++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i3));
            if (!childViewHolderInt.shouldIgnore()) {
                int layoutPosition = childViewHolderInt.getLayoutPosition();
                if (layoutPosition < i) {
                    i = layoutPosition;
                }
                if (layoutPosition > i2) {
                    i2 = layoutPosition;
                }
            }
        }
        iArr[0] = i;
        iArr[1] = i2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getAdapterPositionFor(ViewHolder viewHolder) {
        if (viewHolder.hasAnyOfTheFlags(524) || !viewHolder.isBound()) {
            return -1;
        }
        return this.mAdapterHelper.applyPendingUpdatesToPosition(viewHolder.mPosition);
    }

    static ViewHolder getChildViewHolderInt(View view) {
        if (view == null) {
            return null;
        }
        return ((LayoutParams) view.getLayoutParams()).mViewHolder;
    }

    private float getScrollFactor() {
        if (this.mScrollFactor == Float.MIN_VALUE) {
            TypedValue typedValue = new TypedValue();
            if (!getContext().getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, typedValue, true)) {
                return 0.0f;
            }
            this.mScrollFactor = typedValue.getDimension(getContext().getResources().getDisplayMetrics());
        }
        return this.mScrollFactor;
    }

    private void initChildrenHelper() {
        this.mChildHelper = new ChildHelper(new ChildHelper.Callback() { // from class: com.voxel.recyclerview.widget.RecyclerView.4
            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public void addView(View view, int i) {
                RecyclerView.this.addView(view, i);
                RecyclerView.this.dispatchChildAttached(view);
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public void attachViewToParent(View view, int i, ViewGroup.LayoutParams layoutParams) {
                ViewHolder childViewHolderInt = RecyclerView.getChildViewHolderInt(view);
                if (childViewHolderInt != null) {
                    if (!childViewHolderInt.isTmpDetached() && !childViewHolderInt.shouldIgnore()) {
                        throw new IllegalArgumentException("Called attach on a child which is not detached: " + childViewHolderInt);
                    }
                    childViewHolderInt.clearTmpDetachFlag();
                }
                RecyclerView.this.attachViewToParent(view, i, layoutParams);
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public void detachViewFromParent(int i) {
                ViewHolder childViewHolderInt;
                View childAt = getChildAt(i);
                if (childAt != null && (childViewHolderInt = RecyclerView.getChildViewHolderInt(childAt)) != null) {
                    if (childViewHolderInt.isTmpDetached() && !childViewHolderInt.shouldIgnore()) {
                        throw new IllegalArgumentException("called detach on an already detached child " + childViewHolderInt);
                    }
                    childViewHolderInt.addFlags(256);
                }
                RecyclerView.this.detachViewFromParent(i);
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public View getChildAt(int i) {
                return RecyclerView.this.getChildAt(i);
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public int getChildCount() {
                return RecyclerView.this.getChildCount();
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public ViewHolder getChildViewHolder(View view) {
                return RecyclerView.getChildViewHolderInt(view);
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public int indexOfChild(View view) {
                return RecyclerView.this.indexOfChild(view);
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public void removeAllViews() {
                int childCount = getChildCount();
                for (int i = 0; i < childCount; i++) {
                    RecyclerView.this.dispatchChildDetached(getChildAt(i));
                }
                RecyclerView.this.removeAllViews();
            }

            @Override // com.voxel.recyclerview.widget.ChildHelper.Callback
            public void removeViewAt(int i) {
                View childAt = RecyclerView.this.getChildAt(i);
                if (childAt != null) {
                    RecyclerView.this.dispatchChildDetached(childAt);
                }
                RecyclerView.this.removeViewAt(i);
            }
        });
    }

    private void onPointerUp(MotionEvent motionEvent) {
        int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
        if (MotionEventCompat.getPointerId(motionEvent, actionIndex) == this.mScrollPointerId) {
            int i = actionIndex == 0 ? 1 : 0;
            this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, i);
            int x = (int) (MotionEventCompat.getX(motionEvent, i) + 0.5f);
            this.mLastTouchX = x;
            this.mInitialTouchX = x;
            int y = (int) (MotionEventCompat.getY(motionEvent, i) + 0.5f);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
        }
    }

    private void postAnimationRunner() {
        if (this.mPostedAnimatorRunner || !this.mIsAttached) {
            return;
        }
        ViewCompat.postOnAnimation(this, this.mItemAnimatorRunner);
        this.mPostedAnimatorRunner = true;
    }

    private boolean predictiveItemAnimationsEnabled() {
        return this.mItemAnimator != null && this.mLayout.supportsPredictiveItemAnimations();
    }

    private void processAdapterUpdatesAndSetAnimationFlags() {
        if (this.mDataSetHasChangedAfterLayout) {
            this.mAdapterHelper.reset();
            markKnownViewsInvalid();
            this.mLayout.onItemsChanged(this);
        }
        if (this.mItemAnimator == null || !this.mLayout.supportsPredictiveItemAnimations()) {
            this.mAdapterHelper.consumeUpdatesInOnePass();
        } else {
            this.mAdapterHelper.preProcess();
        }
        boolean z = false;
        boolean z2 = (this.mItemsAddedOrRemoved && !this.mItemsChanged) || this.mItemsAddedOrRemoved || (this.mItemsChanged && supportsChangeAnimations());
        this.mState.mRunSimpleAnimations = this.mFirstLayoutComplete && this.mItemAnimator != null && (this.mDataSetHasChangedAfterLayout || z2 || this.mLayout.mRequestedSimpleAnimations) && (!this.mDataSetHasChangedAfterLayout || this.mAdapter.hasStableIds());
        State state = this.mState;
        if (state.mRunSimpleAnimations && z2 && !this.mDataSetHasChangedAfterLayout && predictiveItemAnimationsEnabled()) {
            z = true;
        }
        state.mRunPredictiveAnimations = z;
    }

    private void processDisappearingList(ArrayMap<View, Rect> arrayMap) {
        int size = this.mDisappearingViewsInLayoutPass.size();
        for (int i = 0; i < size; i++) {
            View view = this.mDisappearingViewsInLayoutPass.get(i);
            ViewHolder childViewHolderInt = getChildViewHolderInt(view);
            ItemHolderInfo itemHolderInfoRemove = this.mState.mPreLayoutHolderMap.remove(childViewHolderInt);
            if (!this.mState.isPreLayout()) {
                this.mState.mPostLayoutHolderMap.remove(childViewHolderInt);
            }
            if (arrayMap.remove(view) != null) {
                this.mLayout.removeAndRecycleView(view, this.mRecycler);
            } else if (itemHolderInfoRemove != null) {
                animateDisappearance(itemHolderInfoRemove);
            } else {
                animateDisappearance(new ItemHolderInfo(childViewHolderInt, view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
            }
        }
        this.mDisappearingViewsInLayoutPass.clear();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean removeAnimatingView(View view) {
        eatRequestLayout();
        boolean zRemoveViewIfHidden = this.mChildHelper.removeViewIfHidden(view);
        if (zRemoveViewIfHidden) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(view);
            this.mRecycler.unscrapView(childViewHolderInt);
            this.mRecycler.recycleViewHolderInternal(childViewHolderInt);
        }
        resumeRequestLayout(false);
        return zRemoveViewIfHidden;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void removeFromDisappearingList(View view) {
        this.mDisappearingViewsInLayoutPass.remove(view);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDataSetChangedAfterLayout() {
        if (this.mDataSetHasChangedAfterLayout) {
            return;
        }
        this.mDataSetHasChangedAfterLayout = true;
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.addFlags(512);
            }
        }
        this.mRecycler.setAdapterPositionsAsUnknown();
    }

    private void stopScrollersInternal() {
        this.mViewFlinger.stop();
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.stopSmoothScroller();
        }
    }

    protected void absorbGlows(int i, int i2) {
        if (i < 0) {
            ensureLeftGlow();
            this.mLeftGlow.onAbsorb(-i);
        } else if (i > 0) {
            ensureRightGlow();
            this.mRightGlow.onAbsorb(i);
        }
        if (i2 < 0) {
            ensureTopGlow();
            this.mTopGlow.onAbsorb(-i2);
        } else if (i2 > 0) {
            ensureBottomGlow();
            this.mBottomGlow.onAbsorb(i2);
        }
        if (i == 0 && i2 == 0) {
            return;
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        if (this.mLayout.onAddFocusables(this, arrayList, i, i2)) {
            return;
        }
        super.addFocusables(arrayList, i, i2);
    }

    void assertNotInLayoutOrScroll(String str) {
        if (this.mRunningLayoutOrScroll) {
            if (str != null) {
                throw new IllegalStateException(str);
            }
            throw new IllegalStateException("Cannot call this method while RecyclerView is computing a layout or scrolling");
        }
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return (layoutParams instanceof LayoutParams) && this.mLayout.checkLayoutParams((LayoutParams) layoutParams);
    }

    void clearOldPositions() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.clearOldPosition();
            }
        }
        this.mRecycler.clearOldPositions();
    }

    @Override // android.view.View
    public int computeHorizontalScrollExtent() {
        if (this.mLayout.canScrollHorizontally()) {
            return this.mLayout.computeHorizontalScrollExtent(this.mState);
        }
        return 0;
    }

    @Override // android.view.View
    public int computeHorizontalScrollOffset() {
        if (this.mLayout.canScrollHorizontally()) {
            return this.mLayout.computeHorizontalScrollOffset(this.mState);
        }
        return 0;
    }

    @Override // android.view.View
    public int computeHorizontalScrollRange() {
        if (this.mLayout.canScrollHorizontally()) {
            return this.mLayout.computeHorizontalScrollRange(this.mState);
        }
        return 0;
    }

    @Override // android.view.View
    public int computeVerticalScrollExtent() {
        if (this.mLayout.canScrollVertically()) {
            return this.mLayout.computeVerticalScrollExtent(this.mState);
        }
        return 0;
    }

    @Override // android.view.View
    public int computeVerticalScrollOffset() {
        if (this.mLayout.canScrollVertically()) {
            return this.mLayout.computeVerticalScrollOffset(this.mState);
        }
        return 0;
    }

    @Override // android.view.View
    public int computeVerticalScrollRange() {
        if (this.mLayout.canScrollVertically()) {
            return this.mLayout.computeVerticalScrollRange(this.mState);
        }
        return 0;
    }

    protected void considerReleasingGlowsOnScroll(int i, int i2) {
        EdgeEffectCompat edgeEffectCompat = this.mLeftGlow;
        boolean zOnRelease = (edgeEffectCompat == null || edgeEffectCompat.isFinished() || i <= 0) ? false : this.mLeftGlow.onRelease();
        EdgeEffectCompat edgeEffectCompat2 = this.mRightGlow;
        if (edgeEffectCompat2 != null && !edgeEffectCompat2.isFinished() && i < 0) {
            zOnRelease |= this.mRightGlow.onRelease();
        }
        EdgeEffectCompat edgeEffectCompat3 = this.mTopGlow;
        if (edgeEffectCompat3 != null && !edgeEffectCompat3.isFinished() && i2 > 0) {
            zOnRelease |= this.mTopGlow.onRelease();
        }
        EdgeEffectCompat edgeEffectCompat4 = this.mBottomGlow;
        if (edgeEffectCompat4 != null && !edgeEffectCompat4.isFinished() && i2 < 0) {
            zOnRelease |= this.mBottomGlow.onRelease();
        }
        if (zOnRelease) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    protected void consumePendingUpdateOperations() {
        this.mUpdateChildViewsRunnable.run();
    }

    protected IViewFlinger createViewFlinger() {
        return new ViewFlinger();
    }

    /* JADX WARN: Multi-variable type inference failed */
    void dispatchLayout() {
        ArrayMap<View, Rect> arrayMap;
        boolean z;
        if (this.mAdapter == null) {
            Log.e("RecyclerView", "No adapter attached; skipping layout");
            return;
        }
        if (this.mLayout == null) {
            Log.e("RecyclerView", "No layout manager attached; skipping layout");
            return;
        }
        this.mDisappearingViewsInLayoutPass.clear();
        eatRequestLayout();
        this.mRunningLayoutOrScroll = true;
        processAdapterUpdatesAndSetAnimationFlags();
        State state = this.mState;
        state.mOldChangedHolders = (state.mRunSimpleAnimations && this.mItemsChanged && supportsChangeAnimations()) ? new ArrayMap<>() : null;
        this.mItemsChanged = false;
        this.mItemsAddedOrRemoved = false;
        State state2 = this.mState;
        state2.mInPreLayout = state2.mRunPredictiveAnimations;
        this.mState.mItemCount = this.mAdapter.getItemCount();
        findMinMaxChildLayoutPositions(this.mMinMaxLayoutPositions);
        if (this.mState.mRunSimpleAnimations) {
            this.mState.mPreLayoutHolderMap.clear();
            this.mState.mPostLayoutHolderMap.clear();
            int childCount = this.mChildHelper.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                if (!childViewHolderInt.shouldIgnore() && (!childViewHolderInt.isInvalid() || this.mAdapter.hasStableIds())) {
                    View view = childViewHolderInt.itemView;
                    this.mState.mPreLayoutHolderMap.put(childViewHolderInt, new ItemHolderInfo(childViewHolderInt, view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
                }
            }
        }
        if (this.mState.mRunPredictiveAnimations) {
            saveOldPositions();
            if (this.mState.mOldChangedHolders != null) {
                int childCount2 = this.mChildHelper.getChildCount();
                for (int i2 = 0; i2 < childCount2; i2++) {
                    ViewHolder childViewHolderInt2 = getChildViewHolderInt(this.mChildHelper.getChildAt(i2));
                    if (childViewHolderInt2.isChanged() && !childViewHolderInt2.isRemoved() && !childViewHolderInt2.shouldIgnore()) {
                        this.mState.mOldChangedHolders.put(Long.valueOf(getChangedHolderKey(childViewHolderInt2)), childViewHolderInt2);
                        this.mState.mPreLayoutHolderMap.remove(childViewHolderInt2);
                    }
                }
            }
            boolean z2 = this.mState.mStructureChanged;
            this.mState.mStructureChanged = false;
            this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
            this.mState.mStructureChanged = z2;
            arrayMap = new ArrayMap<>();
            for (int i3 = 0; i3 < this.mChildHelper.getChildCount(); i3++) {
                View childAt = this.mChildHelper.getChildAt(i3);
                if (!getChildViewHolderInt(childAt).shouldIgnore()) {
                    int i4 = 0;
                    while (true) {
                        if (i4 >= this.mState.mPreLayoutHolderMap.size()) {
                            z = false;
                            break;
                        } else {
                            if (this.mState.mPreLayoutHolderMap.keyAt(i4).itemView == childAt) {
                                z = true;
                                break;
                            }
                            i4++;
                        }
                    }
                    if (!z) {
                        arrayMap.put(childAt, new Rect(childAt.getLeft(), childAt.getTop(), childAt.getRight(), childAt.getBottom()));
                    }
                }
            }
            clearOldPositions();
            this.mAdapterHelper.consumePostponedUpdates();
        } else {
            clearOldPositions();
            this.mAdapterHelper.consumeUpdatesInOnePass();
            if (this.mState.mOldChangedHolders != null) {
                int childCount3 = this.mChildHelper.getChildCount();
                for (int i5 = 0; i5 < childCount3; i5++) {
                    ViewHolder childViewHolderInt3 = getChildViewHolderInt(this.mChildHelper.getChildAt(i5));
                    if (childViewHolderInt3.isChanged() && !childViewHolderInt3.isRemoved() && !childViewHolderInt3.shouldIgnore()) {
                        this.mState.mOldChangedHolders.put(Long.valueOf(getChangedHolderKey(childViewHolderInt3)), childViewHolderInt3);
                        this.mState.mPreLayoutHolderMap.remove(childViewHolderInt3);
                    }
                }
            }
            arrayMap = null;
        }
        this.mState.mItemCount = this.mAdapter.getItemCount();
        this.mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
        this.mState.mInPreLayout = false;
        this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
        this.mState.mStructureChanged = false;
        this.mPendingSavedState = null;
        State state3 = this.mState;
        state3.mRunSimpleAnimations = state3.mRunSimpleAnimations && this.mItemAnimator != null;
        if (this.mState.mRunSimpleAnimations) {
            ArrayMap arrayMap2 = this.mState.mOldChangedHolders != null ? new ArrayMap() : null;
            int childCount4 = this.mChildHelper.getChildCount();
            for (int i6 = 0; i6 < childCount4; i6++) {
                ViewHolder childViewHolderInt4 = getChildViewHolderInt(this.mChildHelper.getChildAt(i6));
                if (!childViewHolderInt4.shouldIgnore()) {
                    View view2 = childViewHolderInt4.itemView;
                    long changedHolderKey = getChangedHolderKey(childViewHolderInt4);
                    if (arrayMap2 == null || this.mState.mOldChangedHolders.get(Long.valueOf(changedHolderKey)) == null) {
                        this.mState.mPostLayoutHolderMap.put(childViewHolderInt4, new ItemHolderInfo(childViewHolderInt4, view2.getLeft(), view2.getTop(), view2.getRight(), view2.getBottom()));
                    } else {
                        arrayMap2.put(Long.valueOf(changedHolderKey), childViewHolderInt4);
                    }
                }
            }
            processDisappearingList(arrayMap);
            for (int size = this.mState.mPreLayoutHolderMap.size() - 1; size >= 0; size--) {
                if (!this.mState.mPostLayoutHolderMap.containsKey(this.mState.mPreLayoutHolderMap.keyAt(size))) {
                    ItemHolderInfo itemHolderInfoValueAt = this.mState.mPreLayoutHolderMap.valueAt(size);
                    this.mState.mPreLayoutHolderMap.removeAt(size);
                    View view3 = itemHolderInfoValueAt.holder.itemView;
                    this.mRecycler.unscrapView(itemHolderInfoValueAt.holder);
                    animateDisappearance(itemHolderInfoValueAt);
                }
            }
            int size2 = this.mState.mPostLayoutHolderMap.size();
            if (size2 > 0) {
                for (int i7 = size2 - 1; i7 >= 0; i7--) {
                    ViewHolder viewHolderKeyAt = this.mState.mPostLayoutHolderMap.keyAt(i7);
                    ItemHolderInfo itemHolderInfoValueAt2 = this.mState.mPostLayoutHolderMap.valueAt(i7);
                    if (this.mState.mPreLayoutHolderMap.isEmpty() || !this.mState.mPreLayoutHolderMap.containsKey(viewHolderKeyAt)) {
                        this.mState.mPostLayoutHolderMap.removeAt(i7);
                        animateAppearance(viewHolderKeyAt, arrayMap != null ? arrayMap.get(viewHolderKeyAt.itemView) : null, itemHolderInfoValueAt2.left, itemHolderInfoValueAt2.top);
                    }
                }
            }
            int size3 = this.mState.mPostLayoutHolderMap.size();
            for (int i8 = 0; i8 < size3; i8++) {
                ViewHolder viewHolderKeyAt2 = this.mState.mPostLayoutHolderMap.keyAt(i8);
                ItemHolderInfo itemHolderInfoValueAt3 = this.mState.mPostLayoutHolderMap.valueAt(i8);
                ItemHolderInfo itemHolderInfo = this.mState.mPreLayoutHolderMap.get(viewHolderKeyAt2);
                if (itemHolderInfo != null && itemHolderInfoValueAt3 != null && (itemHolderInfo.left != itemHolderInfoValueAt3.left || itemHolderInfo.top != itemHolderInfoValueAt3.top)) {
                    viewHolderKeyAt2.setIsRecyclable(false);
                    if (this.mItemAnimator.animateMove(viewHolderKeyAt2, itemHolderInfo.left, itemHolderInfo.top, itemHolderInfoValueAt3.left, itemHolderInfoValueAt3.top)) {
                        postAnimationRunner();
                    }
                }
            }
            for (int size4 = (this.mState.mOldChangedHolders != null ? this.mState.mOldChangedHolders.size() : 0) - 1; size4 >= 0; size4--) {
                long jLongValue = this.mState.mOldChangedHolders.keyAt(size4).longValue();
                ViewHolder viewHolder = this.mState.mOldChangedHolders.get(Long.valueOf(jLongValue));
                View view4 = viewHolder.itemView;
                if (!viewHolder.shouldIgnore() && this.mRecycler.mChangedScrap != null && this.mRecycler.mChangedScrap.contains(viewHolder)) {
                    animateChange(viewHolder, (ViewHolder) arrayMap2.get(Long.valueOf(jLongValue)));
                }
            }
        }
        resumeRequestLayout(false);
        this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
        State state4 = this.mState;
        state4.mPreviousLayoutItemCount = state4.mItemCount;
        this.mDataSetHasChangedAfterLayout = false;
        this.mState.mRunSimpleAnimations = false;
        this.mState.mRunPredictiveAnimations = false;
        this.mRunningLayoutOrScroll = false;
        this.mLayout.mRequestedSimpleAnimations = false;
        if (this.mRecycler.mChangedScrap != null) {
            this.mRecycler.mChangedScrap.clear();
        }
        this.mState.mOldChangedHolders = null;
        int[] iArr = this.mMinMaxLayoutPositions;
        if (didChildRangeChange(iArr[0], iArr[1])) {
            notifyOnScrolled(0, 0);
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        boolean z;
        boolean z2;
        super.draw(canvas);
        int size = this.mItemDecorations.size();
        boolean z3 = false;
        for (int i = 0; i < size; i++) {
            this.mItemDecorations.get(i).onDrawOver(canvas, this, this.mState);
        }
        EdgeEffectCompat edgeEffectCompat = this.mLeftGlow;
        if (edgeEffectCompat == null || edgeEffectCompat.isFinished()) {
            z = false;
        } else {
            int iSave = canvas.save();
            int paddingBottom = this.mClipToPadding ? getPaddingBottom() : 0;
            canvas.rotate(270.0f);
            canvas.translate((-getHeight()) + paddingBottom, 0.0f);
            EdgeEffectCompat edgeEffectCompat2 = this.mLeftGlow;
            z = edgeEffectCompat2 != null && edgeEffectCompat2.draw(canvas);
            canvas.restoreToCount(iSave);
        }
        EdgeEffectCompat edgeEffectCompat3 = this.mTopGlow;
        if (edgeEffectCompat3 != null && !edgeEffectCompat3.isFinished()) {
            int iSave2 = canvas.save();
            if (this.mClipToPadding) {
                canvas.translate(getPaddingLeft(), getPaddingTop());
            }
            EdgeEffectCompat edgeEffectCompat4 = this.mTopGlow;
            z |= edgeEffectCompat4 != null && edgeEffectCompat4.draw(canvas);
            canvas.restoreToCount(iSave2);
        }
        EdgeEffectCompat edgeEffectCompat5 = this.mRightGlow;
        if (edgeEffectCompat5 != null && !edgeEffectCompat5.isFinished()) {
            int iSave3 = canvas.save();
            int width = getWidth();
            int paddingTop = this.mClipToPadding ? getPaddingTop() : 0;
            canvas.rotate(90.0f);
            canvas.translate(-paddingTop, -width);
            EdgeEffectCompat edgeEffectCompat6 = this.mRightGlow;
            z |= edgeEffectCompat6 != null && edgeEffectCompat6.draw(canvas);
            canvas.restoreToCount(iSave3);
        }
        EdgeEffectCompat edgeEffectCompat7 = this.mBottomGlow;
        if (edgeEffectCompat7 == null || edgeEffectCompat7.isFinished()) {
            z2 = z;
        } else {
            int iSave4 = canvas.save();
            canvas.rotate(180.0f);
            if (this.mClipToPadding) {
                canvas.translate((-getWidth()) + getPaddingRight(), (-getHeight()) + getPaddingBottom());
            } else {
                canvas.translate(-getWidth(), -getHeight());
            }
            EdgeEffectCompat edgeEffectCompat8 = this.mBottomGlow;
            if (edgeEffectCompat8 != null && edgeEffectCompat8.draw(canvas)) {
                z3 = true;
            }
            z2 = z3 | z;
            canvas.restoreToCount(iSave4);
        }
        if (!z2 && this.mItemAnimator != null && this.mItemDecorations.size() > 0 && this.mItemAnimator.isRunning()) {
            z2 = true;
        }
        if (z2) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    void eatRequestLayout() {
        if (this.mEatRequestLayout) {
            return;
        }
        this.mEatRequestLayout = true;
        this.mLayoutRequestEaten = false;
    }

    void ensureBottomGlow() {
        if (this.mBottomGlow != null) {
            return;
        }
        this.mBottomGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mBottomGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
        } else {
            this.mBottomGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    void ensureLeftGlow() {
        if (this.mLeftGlow != null) {
            return;
        }
        this.mLeftGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mLeftGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
        } else {
            this.mLeftGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
        }
    }

    void ensureRightGlow() {
        if (this.mRightGlow != null) {
            return;
        }
        this.mRightGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mRightGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
        } else {
            this.mRightGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
        }
    }

    void ensureTopGlow() {
        if (this.mTopGlow != null) {
            return;
        }
        this.mTopGlow = new EdgeEffectCompat(getContext());
        if (this.mClipToPadding) {
            this.mTopGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
        } else {
            this.mTopGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public View findChildViewUnder(float f, float f2) {
        for (int childCount = this.mChildHelper.getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = this.mChildHelper.getChildAt(childCount);
            float translationX = ViewCompat.getTranslationX(childAt);
            float translationY = ViewCompat.getTranslationY(childAt);
            if (f >= childAt.getLeft() + translationX && f <= childAt.getRight() + translationX && f2 >= childAt.getTop() + translationY && f2 <= childAt.getBottom() + translationY) {
                return childAt;
            }
        }
        return null;
    }

    ViewHolder findViewHolderForPosition(int i, boolean z) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i2 = 0; i2 < unfilteredChildCount; i2++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i2));
            if (childViewHolderInt != null && !childViewHolderInt.isRemoved()) {
                if (z) {
                    if (childViewHolderInt.mPosition == i) {
                        return childViewHolderInt;
                    }
                } else if (childViewHolderInt.getLayoutPosition() == i) {
                    return childViewHolderInt;
                }
            }
        }
        return null;
    }

    public boolean fling(int i, int i2) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            Log.e("RecyclerView", "Cannot fling without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return false;
        }
        boolean zCanScrollHorizontally = layoutManager.canScrollHorizontally();
        boolean zCanScrollVertically = this.mLayout.canScrollVertically();
        if (!zCanScrollHorizontally || Math.abs(i) < this.mMinFlingVelocity) {
            i = 0;
        }
        if (!zCanScrollVertically || Math.abs(i2) < this.mMinFlingVelocity) {
            i2 = 0;
        }
        int i3 = this.mMaxFlingVelocity;
        int iMax = Math.max(-i3, Math.min(i, i3));
        int i4 = this.mMaxFlingVelocity;
        int iMax2 = Math.max(-i4, Math.min(i2, i4));
        if (iMax == 0 && iMax2 == 0) {
            return false;
        }
        this.mViewFlinger.fling(iMax, iMax2);
        return true;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public View focusSearch(View view, int i) {
        View viewOnInterceptFocusSearch = this.mLayout.onInterceptFocusSearch(view, i);
        if (viewOnInterceptFocusSearch != null) {
            return viewOnInterceptFocusSearch;
        }
        View viewFindNextFocus = FocusFinder.getInstance().findNextFocus(this, view, i);
        if (viewFindNextFocus == null && this.mAdapter != null && this.mLayout != null) {
            eatRequestLayout();
            viewFindNextFocus = this.mLayout.onFocusSearchFailed(view, i, this.mRecycler, this.mState);
            resumeRequestLayout(false);
        }
        return viewFindNextFocus != null ? viewFindNextFocus : super.focusSearch(view, i);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            return layoutManager.generateDefaultLayoutParams();
        }
        throw new IllegalStateException("RecyclerView has no LayoutManager");
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            return layoutManager.generateLayoutParams(getContext(), attributeSet);
        }
        throw new IllegalStateException("RecyclerView has no LayoutManager");
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            return layoutManager.generateLayoutParams(layoutParams);
        }
        throw new IllegalStateException("RecyclerView has no LayoutManager");
    }

    public Adapter getAdapter() {
        return this.mAdapter;
    }

    long getChangedHolderKey(ViewHolder viewHolder) {
        return this.mAdapter.hasStableIds() ? viewHolder.getItemId() : viewHolder.mPosition;
    }

    public int getChildAdapterPosition(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            return childViewHolderInt.getAdapterPosition();
        }
        return -1;
    }

    public int getChildLayoutPosition(View view) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            return childViewHolderInt.getLayoutPosition();
        }
        return -1;
    }

    public ViewHolder getChildViewHolder(View view) {
        ViewParent parent = view.getParent();
        if (parent == null || parent == this) {
            return getChildViewHolderInt(view);
        }
        throw new IllegalArgumentException("View " + view + " is not a direct child of " + this);
    }

    public RecyclerViewAccessibilityDelegate getCompatAccessibilityDelegate() {
        return this.mAccessibilityDelegate;
    }

    public ItemAnimator getItemAnimator() {
        return this.mItemAnimator;
    }

    Rect getItemDecorInsetsForChild(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (!layoutParams.mInsetsDirty) {
            return layoutParams.mDecorInsets;
        }
        Rect rect = layoutParams.mDecorInsets;
        rect.set(0, 0, 0, 0);
        int size = this.mItemDecorations.size();
        for (int i = 0; i < size; i++) {
            this.mTempRect.set(0, 0, 0, 0);
            this.mItemDecorations.get(i).getItemOffsets(this.mTempRect, view, this, this.mState);
            rect.left += this.mTempRect.left;
            rect.top += this.mTempRect.top;
            rect.right += this.mTempRect.right;
            rect.bottom += this.mTempRect.bottom;
        }
        layoutParams.mInsetsDirty = false;
        return rect;
    }

    public LayoutManager getLayoutManager() {
        return this.mLayout;
    }

    public RecycledViewPool getRecycledViewPool() {
        return this.mRecycler.getRecycledViewPool();
    }

    public int getScrollState() {
        return this.mScrollState;
    }

    void initAdapterManager() {
        this.mAdapterHelper = new AdapterHelper(new AdapterHelper.Callback() { // from class: com.voxel.recyclerview.widget.RecyclerView.5
            void dispatchUpdate(AdapterHelper.UpdateOp updateOp) {
                switch (updateOp.cmd) {
                    case 0:
                        RecyclerView.this.mLayout.onItemsAdded(RecyclerView.this, updateOp.positionStart, updateOp.itemCount);
                        break;
                    case 1:
                        RecyclerView.this.mLayout.onItemsRemoved(RecyclerView.this, updateOp.positionStart, updateOp.itemCount);
                        break;
                    case 2:
                        RecyclerView.this.mLayout.onItemsUpdated(RecyclerView.this, updateOp.positionStart, updateOp.itemCount);
                        break;
                    case 3:
                        RecyclerView.this.mLayout.onItemsMoved(RecyclerView.this, updateOp.positionStart, updateOp.itemCount, 1);
                        break;
                }
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public ViewHolder findViewHolder(int i) {
                ViewHolder viewHolderFindViewHolderForPosition = RecyclerView.this.findViewHolderForPosition(i, true);
                if (viewHolderFindViewHolderForPosition == null || RecyclerView.this.mChildHelper.isHidden(viewHolderFindViewHolderForPosition.itemView)) {
                    return null;
                }
                return viewHolderFindViewHolderForPosition;
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public void markViewHoldersUpdated(int i, int i2) {
                RecyclerView.this.viewRangeUpdate(i, i2);
                RecyclerView.this.mItemsChanged = true;
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public void offsetPositionsForAdd(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForInsert(i, i2);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public void offsetPositionsForMove(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForMove(i, i2);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public void offsetPositionsForRemovingInvisible(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForRemove(i, i2, true);
                RecyclerView recyclerView = RecyclerView.this;
                recyclerView.mItemsAddedOrRemoved = true;
                recyclerView.mState.mDeletedInvisibleItemCountSincePreviousLayout += i2;
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public void offsetPositionsForRemovingLaidOutOrNewView(int i, int i2) {
                RecyclerView.this.offsetPositionRecordsForRemove(i, i2, false);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public void onDispatchFirstPass(AdapterHelper.UpdateOp updateOp) {
                dispatchUpdate(updateOp);
            }

            @Override // com.voxel.recyclerview.widget.AdapterHelper.Callback
            public void onDispatchSecondPass(AdapterHelper.UpdateOp updateOp) {
                dispatchUpdate(updateOp);
            }
        });
    }

    void invalidateGlows() {
        this.mBottomGlow = null;
        this.mTopGlow = null;
        this.mRightGlow = null;
        this.mLeftGlow = null;
    }

    void markItemDecorInsetsDirty() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ((LayoutParams) this.mChildHelper.getUnfilteredChildAt(i).getLayoutParams()).mInsetsDirty = true;
        }
        this.mRecycler.markItemDecorInsetsDirty();
    }

    void markKnownViewsInvalid() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.addFlags(6);
            }
        }
        markItemDecorInsetsDirty();
        this.mRecycler.markKnownViewsInvalid();
    }

    protected void notifyOnScrolled(int i, int i2) {
        onScrollChanged(0, 0, 0, 0);
        OnScrollListener onScrollListener = this.mScrollListener;
        if (onScrollListener != null) {
            onScrollListener.onScrolled(this, i, i2);
        }
    }

    public void offsetChildrenHorizontal(int i) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            this.mChildHelper.getChildAt(i2).offsetLeftAndRight(i);
        }
    }

    public void offsetChildrenVertical(int i) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            this.mChildHelper.getChildAt(i2).offsetTopAndBottom(i);
        }
    }

    void offsetPositionRecordsForInsert(int i, int i2) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i3 = 0; i3 < unfilteredChildCount; i3++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i3));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.mPosition >= i) {
                childViewHolderInt.offsetPosition(i2, false);
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForInsert(i, i2);
        requestLayout();
    }

    void offsetPositionRecordsForMove(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        if (i < i2) {
            i4 = i;
            i3 = i2;
            i5 = -1;
        } else {
            i3 = i;
            i4 = i2;
            i5 = 1;
        }
        for (int i6 = 0; i6 < unfilteredChildCount; i6++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i6));
            if (childViewHolderInt != null && childViewHolderInt.mPosition >= i4 && childViewHolderInt.mPosition <= i3) {
                if (childViewHolderInt.mPosition == i) {
                    childViewHolderInt.offsetPosition(i2 - i, false);
                } else {
                    childViewHolderInt.offsetPosition(i5, false);
                }
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForMove(i, i2);
        requestLayout();
    }

    void offsetPositionRecordsForRemove(int i, int i2, boolean z) {
        int i3 = i + i2;
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i4 = 0; i4 < unfilteredChildCount; i4++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i4));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                if (childViewHolderInt.mPosition >= i3) {
                    childViewHolderInt.offsetPosition(-i2, z);
                    this.mState.mStructureChanged = true;
                } else if (childViewHolderInt.mPosition >= i) {
                    childViewHolderInt.flagRemovedAndOffsetPosition(i - 1, -i2, z);
                    this.mState.mStructureChanged = true;
                }
            }
        }
        this.mRecycler.offsetPositionRecordsForRemove(i, i2, z);
        requestLayout();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIsAttached = true;
        this.mFirstLayoutComplete = false;
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.onAttachedToWindow(this);
        }
        this.mPostedAnimatorRunner = false;
    }

    public void onChildAttachedToWindow(View view) {
    }

    public void onChildDetachedFromWindow(View view) {
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ItemAnimator itemAnimator = this.mItemAnimator;
        if (itemAnimator != null) {
            itemAnimator.endAnimations();
        }
        this.mFirstLayoutComplete = false;
        stopScroll();
        this.mIsAttached = false;
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.onDetachedFromWindow(this, this.mRecycler);
        }
        removeCallbacks(this.mItemAnimatorRunner);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = this.mItemDecorations.size();
        for (int i = 0; i < size; i++) {
            this.mItemDecorations.get(i).onDraw(canvas, this, this.mState);
        }
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        if (this.mLayout != null && (MotionEventCompat.getSource(motionEvent) & 2) != 0 && motionEvent.getAction() == 8) {
            float axisValue = this.mLayout.canScrollVertically() ? MotionEventCompat.getAxisValue(motionEvent, 9) : 0.0f;
            float axisValue2 = this.mLayout.canScrollHorizontally() ? MotionEventCompat.getAxisValue(motionEvent, 10) : 0.0f;
            if (axisValue != 0.0f || axisValue2 != 0.0f) {
                float scrollFactor = getScrollFactor();
                scrollBy((int) (axisValue2 * scrollFactor), (int) (axisValue * scrollFactor));
            }
        }
        return false;
    }

    protected boolean onIdleTouchUp() {
        return false;
    }

    /* JADX WARN: Code duplicated, block: B:30:0x00b1  */
    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z;
        if (dispatchOnItemTouchIntercept(motionEvent)) {
            cancelTouch();
            return true;
        }
        boolean zCanScrollHorizontally = this.mLayout.canScrollHorizontally();
        boolean zCanScrollVertically = this.mLayout.canScrollVertically();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
        switch (actionMasked) {
            case 0:
                this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                int x = (int) (motionEvent.getX() + 0.5f);
                this.mLastTouchX = x;
                this.mInitialTouchX = x;
                int y = (int) (motionEvent.getY() + 0.5f);
                this.mLastTouchY = y;
                this.mInitialTouchY = y;
                if (this.mScrollState == 2) {
                    setScrollState(0);
                    return true;
                }
                break;
            case 1:
                this.mVelocityTracker.clear();
                break;
            case 2:
                int iFindPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.mScrollPointerId);
                if (iFindPointerIndex < 0) {
                    Log.e("RecyclerView", "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }
                int x2 = (int) (MotionEventCompat.getX(motionEvent, iFindPointerIndex) + 0.5f);
                int y2 = (int) (MotionEventCompat.getY(motionEvent, iFindPointerIndex) + 0.5f);
                if (this.mScrollState != 1) {
                    int i = x2 - this.mInitialTouchX;
                    int i2 = y2 - this.mInitialTouchY;
                    if (zCanScrollHorizontally) {
                        int iAbs = Math.abs(i);
                        int i3 = this.mTouchSlop;
                        if (iAbs > i3) {
                            this.mLastTouchX = this.mInitialTouchX + (i3 * (i < 0 ? -1 : 1));
                            z = true;
                        } else {
                            z = false;
                        }
                    } else {
                        z = false;
                    }
                    if (zCanScrollVertically) {
                        int iAbs2 = Math.abs(i2);
                        int i4 = this.mTouchSlop;
                        if (iAbs2 > i4) {
                            this.mLastTouchY = this.mInitialTouchY + (i4 * (i2 >= 0 ? 1 : -1));
                            z = true;
                        }
                    }
                    if (z) {
                        setScrollState(1);
                    }
                }
                break;
            case 3:
                cancelTouch();
                break;
            case 5:
                this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, actionIndex);
                int x3 = (int) (MotionEventCompat.getX(motionEvent, actionIndex) + 0.5f);
                this.mLastTouchX = x3;
                this.mInitialTouchX = x3;
                int y3 = (int) (MotionEventCompat.getY(motionEvent, actionIndex) + 0.5f);
                this.mLastTouchY = y3;
                this.mInitialTouchY = y3;
                break;
            case 6:
                onPointerUp(motionEvent);
                break;
        }
        return this.mScrollState == 1;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        eatRequestLayout();
        dispatchLayout();
        resumeRequestLayout(false);
        this.mFirstLayoutComplete = true;
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        if (this.mAdapterUpdateDuringMeasure) {
            eatRequestLayout();
            processAdapterUpdatesAndSetAnimationFlags();
            if (this.mState.mRunPredictiveAnimations) {
                this.mState.mInPreLayout = true;
            } else {
                this.mAdapterHelper.consumeUpdatesInOnePass();
                this.mState.mInPreLayout = false;
            }
            this.mAdapterUpdateDuringMeasure = false;
            resumeRequestLayout(false);
        }
        Adapter adapter = this.mAdapter;
        if (adapter != null) {
            this.mState.mItemCount = adapter.getItemCount();
        } else {
            this.mState.mItemCount = 0;
        }
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            defaultOnMeasure(i, i2);
        } else {
            layoutManager.onMeasure(this.mRecycler, this.mState, i, i2);
        }
        this.mState.mInPreLayout = false;
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        this.mPendingSavedState = (SavedState) parcelable;
        super.onRestoreInstanceState(this.mPendingSavedState.getSuperState());
        if (this.mLayout == null || this.mPendingSavedState.mLayoutState == null) {
            return;
        }
        this.mLayout.onRestoreInstanceState(this.mPendingSavedState.mLayoutState);
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        SavedState savedState2 = this.mPendingSavedState;
        if (savedState2 != null) {
            savedState.copyFrom(savedState2);
        } else {
            LayoutManager layoutManager = this.mLayout;
            if (layoutManager != null) {
                savedState.mLayoutState = layoutManager.onSaveInstanceState();
            } else {
                savedState.mLayoutState = null;
            }
        }
        return savedState;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i == i3 && i2 == i4) {
            return;
        }
        invalidateGlows();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code duplicated, block: B:30:0x00b3  */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        if (dispatchOnItemTouch(motionEvent)) {
            cancelTouch();
            return true;
        }
        boolean zCanScrollHorizontally = this.mLayout.canScrollHorizontally();
        boolean zCanScrollVertically = this.mLayout.canScrollVertically();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        int actionMasked = MotionEventCompat.getActionMasked(motionEvent);
        int actionIndex = MotionEventCompat.getActionIndex(motionEvent);
        switch (actionMasked) {
            case 0:
                this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, 0);
                int x = (int) (motionEvent.getX() + 0.5f);
                this.mLastTouchX = x;
                this.mInitialTouchX = x;
                int y = (int) (motionEvent.getY() + 0.5f);
                this.mLastTouchY = y;
                this.mInitialTouchY = y;
                return true;
            case 1:
                this.mVelocityTracker.computeCurrentVelocity(1000, this.mMaxFlingVelocity);
                float f = zCanScrollHorizontally ? -VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mScrollPointerId) : 0.0f;
                float f2 = zCanScrollVertically ? -VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mScrollPointerId) : 0.0f;
                if (((f == 0.0f && f2 == 0.0f) || !fling((int) f, (int) f2)) && !onIdleTouchUp()) {
                    setScrollState(0);
                }
                this.mVelocityTracker.clear();
                releaseGlows();
                return true;
            case 2:
                int iFindPointerIndex = MotionEventCompat.findPointerIndex(motionEvent, this.mScrollPointerId);
                if (iFindPointerIndex < 0) {
                    Log.e("RecyclerView", "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }
                int x2 = (int) (MotionEventCompat.getX(motionEvent, iFindPointerIndex) + 0.5f);
                int y2 = (int) (MotionEventCompat.getY(motionEvent, iFindPointerIndex) + 0.5f);
                if (this.mScrollState != 1) {
                    int i = x2 - this.mInitialTouchX;
                    int i2 = y2 - this.mInitialTouchY;
                    if (zCanScrollHorizontally) {
                        int iAbs = Math.abs(i);
                        int i3 = this.mTouchSlop;
                        if (iAbs > i3) {
                            this.mLastTouchX = this.mInitialTouchX + (i3 * (i < 0 ? -1 : 1));
                            z = true;
                        } else {
                            z = false;
                        }
                    } else {
                        z = false;
                    }
                    if (zCanScrollVertically) {
                        int iAbs2 = Math.abs(i2);
                        int i4 = this.mTouchSlop;
                        if (iAbs2 > i4) {
                            this.mLastTouchY = this.mInitialTouchY + (i4 * (i2 >= 0 ? 1 : -1));
                            z = true;
                        }
                    }
                    if (z) {
                        setScrollState(1);
                    }
                }
                if (this.mScrollState == 1) {
                    if (scrollByInternal(zCanScrollHorizontally ? -(x2 - this.mLastTouchX) : 0, zCanScrollVertically ? -(y2 - this.mLastTouchY) : 0)) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                this.mLastTouchX = x2;
                this.mLastTouchY = y2;
                return true;
            case 3:
                cancelTouch();
                return true;
            case 4:
            default:
                return true;
            case 5:
                this.mScrollPointerId = MotionEventCompat.getPointerId(motionEvent, actionIndex);
                int x3 = (int) (MotionEventCompat.getX(motionEvent, actionIndex) + 0.5f);
                this.mLastTouchX = x3;
                this.mInitialTouchX = x3;
                int y3 = (int) (MotionEventCompat.getY(motionEvent, actionIndex) + 0.5f);
                this.mLastTouchY = y3;
                this.mInitialTouchY = y3;
                return true;
            case 6:
                onPointerUp(motionEvent);
                return true;
        }
    }

    protected void pullGlows(int i, int i2) {
        if (i < 0) {
            ensureLeftGlow();
            this.mLeftGlow.onPull((-i) / getWidth());
        } else if (i > 0) {
            ensureRightGlow();
            this.mRightGlow.onPull(i / getWidth());
        }
        if (i2 < 0) {
            ensureTopGlow();
            this.mTopGlow.onPull((-i2) / getHeight());
        } else if (i2 > 0) {
            ensureBottomGlow();
            this.mBottomGlow.onPull(i2 / getHeight());
        }
        if (i == 0 && i2 == 0) {
            return;
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    void rebindUpdatedViewHolders() {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore()) {
                if (childViewHolderInt.isRemoved() || childViewHolderInt.isInvalid()) {
                    requestLayout();
                } else if (childViewHolderInt.needsUpdate()) {
                    if (childViewHolderInt.getItemViewType() != this.mAdapter.getItemViewType(childViewHolderInt.mPosition)) {
                        requestLayout();
                        return;
                    } else if (childViewHolderInt.isChanged() && supportsChangeAnimations()) {
                        requestLayout();
                    } else {
                        this.mAdapter.bindViewHolder(childViewHolderInt, childViewHolderInt.mPosition);
                    }
                } else {
                    continue;
                }
            }
        }
    }

    protected void releaseGlows() {
        EdgeEffectCompat edgeEffectCompat = this.mLeftGlow;
        boolean zOnRelease = edgeEffectCompat != null ? edgeEffectCompat.onRelease() : false;
        EdgeEffectCompat edgeEffectCompat2 = this.mTopGlow;
        if (edgeEffectCompat2 != null) {
            zOnRelease |= edgeEffectCompat2.onRelease();
        }
        EdgeEffectCompat edgeEffectCompat3 = this.mRightGlow;
        if (edgeEffectCompat3 != null) {
            zOnRelease |= edgeEffectCompat3.onRelease();
        }
        EdgeEffectCompat edgeEffectCompat4 = this.mBottomGlow;
        if (edgeEffectCompat4 != null) {
            zOnRelease |= edgeEffectCompat4.onRelease();
        }
        if (zOnRelease) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override // android.view.ViewGroup
    protected void removeDetachedView(View view, boolean z) {
        ViewHolder childViewHolderInt = getChildViewHolderInt(view);
        if (childViewHolderInt != null) {
            if (childViewHolderInt.isTmpDetached()) {
                childViewHolderInt.clearTmpDetachFlag();
            } else if (!childViewHolderInt.shouldIgnore()) {
                throw new IllegalArgumentException("Called removeDetachedView with a view which is not flagged as tmp detached." + childViewHolderInt);
            }
        }
        dispatchChildDetached(view);
        super.removeDetachedView(view, z);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        if (!this.mLayout.onRequestChildFocus(this, this.mState, view, view2) && view2 != null) {
            this.mTempRect.set(0, 0, view2.getWidth(), view2.getHeight());
            ViewGroup.LayoutParams layoutParams = view2.getLayoutParams();
            if (layoutParams instanceof LayoutParams) {
                LayoutParams layoutParams2 = (LayoutParams) layoutParams;
                if (!layoutParams2.mInsetsDirty) {
                    Rect rect = layoutParams2.mDecorInsets;
                    this.mTempRect.left -= rect.left;
                    this.mTempRect.right += rect.right;
                    this.mTempRect.top -= rect.top;
                    this.mTempRect.bottom += rect.bottom;
                }
            }
            offsetDescendantRectToMyCoords(view2, this.mTempRect);
            offsetRectIntoDescendantCoords(view, this.mTempRect);
            requestChildRectangleOnScreen(view, this.mTempRect, !this.mFirstLayoutComplete);
        }
        super.requestChildFocus(view, view2);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        return this.mLayout.requestChildRectangleOnScreen(this, view, rect, z);
    }

    @Override // android.view.View, android.view.ViewParent
    public void requestLayout() {
        if (this.mEatRequestLayout) {
            this.mLayoutRequestEaten = true;
        } else {
            super.requestLayout();
        }
    }

    void resumeRequestLayout(boolean z) {
        if (this.mEatRequestLayout) {
            if (z && this.mLayoutRequestEaten && this.mLayout != null && this.mAdapter != null) {
                dispatchLayout();
            }
            this.mEatRequestLayout = false;
            this.mLayoutRequestEaten = false;
        }
    }

    void saveOldPositions() {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < unfilteredChildCount; i++) {
            ViewHolder childViewHolderInt = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!childViewHolderInt.shouldIgnore()) {
                childViewHolderInt.saveOldPosition();
            }
        }
    }

    @Override // android.view.View
    public void scrollBy(int i, int i2) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            Log.e("RecyclerView", "Cannot scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        boolean zCanScrollHorizontally = layoutManager.canScrollHorizontally();
        boolean zCanScrollVertically = this.mLayout.canScrollVertically();
        if (zCanScrollHorizontally || zCanScrollVertically) {
            if (!zCanScrollHorizontally) {
                i = 0;
            }
            if (!zCanScrollVertically) {
                i2 = 0;
            }
            scrollByInternal(i, i2);
        }
    }

    protected boolean scrollByInternal(int i, int i2) {
        int iScrollHorizontallyBy;
        int i3;
        int iScrollVerticallyBy;
        int i4;
        consumePendingUpdateOperations();
        if (this.mAdapter != null) {
            eatRequestLayout();
            this.mRunningLayoutOrScroll = true;
            if (i != 0) {
                iScrollHorizontallyBy = this.mLayout.scrollHorizontallyBy(i, this.mRecycler, this.mState);
                i3 = i - iScrollHorizontallyBy;
            } else {
                iScrollHorizontallyBy = 0;
                i3 = 0;
            }
            if (i2 != 0) {
                iScrollVerticallyBy = this.mLayout.scrollVerticallyBy(i2, this.mRecycler, this.mState);
                i4 = i2 - iScrollVerticallyBy;
            } else {
                iScrollVerticallyBy = 0;
                i4 = 0;
            }
            if (supportsChangeAnimations()) {
                int childCount = this.mChildHelper.getChildCount();
                for (int i5 = 0; i5 < childCount; i5++) {
                    View childAt = this.mChildHelper.getChildAt(i5);
                    ViewHolder childViewHolder = getChildViewHolder(childAt);
                    if (childViewHolder != null && childViewHolder.mShadowingHolder != null) {
                        ViewHolder viewHolder = childViewHolder.mShadowingHolder;
                        View view = viewHolder != null ? viewHolder.itemView : null;
                        if (view != null) {
                            int left = childAt.getLeft();
                            int top = childAt.getTop();
                            if (left != view.getLeft() || top != view.getTop()) {
                                view.layout(left, top, view.getWidth() + left, view.getHeight() + top);
                            }
                        }
                    }
                }
            }
            this.mRunningLayoutOrScroll = false;
            resumeRequestLayout(false);
        } else {
            iScrollHorizontallyBy = 0;
            i3 = 0;
            iScrollVerticallyBy = 0;
            i4 = 0;
        }
        if (!this.mItemDecorations.isEmpty()) {
            invalidate();
        }
        if (ViewCompat.getOverScrollMode(this) != 2) {
            considerReleasingGlowsOnScroll(i, i2);
            pullGlows(i3, i4);
        }
        if (iScrollHorizontallyBy != 0 || iScrollVerticallyBy != 0) {
            notifyOnScrolled(iScrollHorizontallyBy, iScrollVerticallyBy);
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        return (iScrollHorizontallyBy == 0 && iScrollVerticallyBy == 0) ? false : true;
    }

    @Override // android.view.View
    public void scrollTo(int i, int i2) {
        throw new UnsupportedOperationException("RecyclerView does not support scrolling to an absolute position.");
    }

    public void scrollToPosition(int i) {
        stopScroll();
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            Log.e("RecyclerView", "Cannot scroll to position a LayoutManager set. Call setLayoutManager with a non-null argument.");
        } else {
            layoutManager.scrollToPosition(i);
            awakenScrollBars();
        }
    }

    public void setAccessibilityDelegateCompat(RecyclerViewAccessibilityDelegate recyclerViewAccessibilityDelegate) {
        this.mAccessibilityDelegate = recyclerViewAccessibilityDelegate;
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityDelegate);
    }

    public void setAdapter(Adapter adapter) {
        setAdapterInternal(adapter, false, true);
        requestLayout();
    }

    protected void setAdapterInternal(Adapter adapter, boolean z, boolean z2) {
        Adapter adapter2 = this.mAdapter;
        if (adapter2 != null) {
            adapter2.unregisterAdapterDataObserver(this.mObserver);
            this.mAdapter.onDetachedFromRecyclerView(this);
        }
        if (!z || z2) {
            ItemAnimator itemAnimator = this.mItemAnimator;
            if (itemAnimator != null) {
                itemAnimator.endAnimations();
            }
            LayoutManager layoutManager = this.mLayout;
            if (layoutManager != null) {
                layoutManager.removeAndRecycleAllViews(this.mRecycler);
                this.mLayout.removeAndRecycleScrapInt(this.mRecycler);
            }
            this.mRecycler.clear();
        }
        this.mAdapterHelper.reset();
        Adapter adapter3 = this.mAdapter;
        this.mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(this.mObserver);
            adapter.onAttachedToRecyclerView(this);
        }
        LayoutManager layoutManager2 = this.mLayout;
        if (layoutManager2 != null) {
            layoutManager2.onAdapterChanged(adapter3, this.mAdapter);
        }
        this.mRecycler.onAdapterChanged(adapter3, this.mAdapter, z);
        this.mState.mStructureChanged = true;
        markKnownViewsInvalid();
    }

    @Override // android.view.ViewGroup
    public void setClipToPadding(boolean z) {
        if (z != this.mClipToPadding) {
            invalidateGlows();
        }
        this.mClipToPadding = z;
        super.setClipToPadding(z);
        if (this.mFirstLayoutComplete) {
            requestLayout();
        }
    }

    public void setHasFixedSize(boolean z) {
        this.mHasFixedSize = z;
    }

    public void setItemAnimator(ItemAnimator itemAnimator) {
        ItemAnimator itemAnimator2 = this.mItemAnimator;
        if (itemAnimator2 != null) {
            itemAnimator2.endAnimations();
            this.mItemAnimator.setListener(null);
        }
        this.mItemAnimator = itemAnimator;
        ItemAnimator itemAnimator3 = this.mItemAnimator;
        if (itemAnimator3 != null) {
            itemAnimator3.setListener(this.mItemAnimatorListener);
        }
    }

    public void setItemViewCacheSize(int i) {
        this.mRecycler.setViewCacheSize(i);
    }

    public void setLayoutManager(LayoutManager layoutManager) {
        LayoutManager layoutManager2 = this.mLayout;
        if (layoutManager == layoutManager2) {
            return;
        }
        if (layoutManager2 != null) {
            if (this.mIsAttached) {
                layoutManager2.onDetachedFromWindow(this, this.mRecycler);
            }
            this.mLayout.setRecyclerView(null);
        }
        this.mRecycler.clear();
        this.mChildHelper.removeAllViewsUnfiltered();
        this.mLayout = layoutManager;
        if (layoutManager != null) {
            if (layoutManager.mRecyclerView != null) {
                throw new IllegalArgumentException("LayoutManager " + layoutManager + " is already attached to a RecyclerView: " + layoutManager.mRecyclerView);
            }
            this.mLayout.setRecyclerView(this);
            if (this.mIsAttached) {
                this.mLayout.onAttachedToWindow(this);
            }
        }
        requestLayout();
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.mScrollListener = onScrollListener;
    }

    public void setRecycledViewPool(RecycledViewPool recycledViewPool) {
        this.mRecycler.setRecycledViewPool(recycledViewPool);
    }

    public void setRecyclerListener(RecyclerListener recyclerListener) {
        this.mRecyclerListener = recyclerListener;
    }

    protected void setScrollState(int i) {
        if (i == this.mScrollState) {
            return;
        }
        this.mScrollState = i;
        if (i != 2) {
            stopScrollersInternal();
        }
        OnScrollListener onScrollListener = this.mScrollListener;
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(this, i);
        }
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager != null) {
            layoutManager.onScrollStateChanged(i);
        }
    }

    public void setScrollingTouchSlop(int i) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        switch (i) {
            case 0:
                break;
            case 1:
                this.mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(viewConfiguration);
                return;
            default:
                Log.w("RecyclerView", "setScrollingTouchSlop(): bad argument constant " + i + "; using default value");
                break;
        }
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    public void setViewCacheExtension(ViewCacheExtension viewCacheExtension) {
        this.mRecycler.setViewCacheExtension(viewCacheExtension);
    }

    public void smoothScrollBy(int i, int i2) {
        LayoutManager layoutManager = this.mLayout;
        if (layoutManager == null) {
            Log.e("RecyclerView", "Cannot smooth scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
            return;
        }
        if (!layoutManager.canScrollHorizontally()) {
            i = 0;
        }
        if (!this.mLayout.canScrollVertically()) {
            i2 = 0;
        }
        if (i == 0 && i2 == 0) {
            return;
        }
        this.mViewFlinger.smoothScrollBy(i, i2);
    }

    public void stopScroll() {
        setScrollState(0);
        stopScrollersInternal();
    }

    protected boolean supportsChangeAnimations() {
        ItemAnimator itemAnimator = this.mItemAnimator;
        return itemAnimator != null && itemAnimator.getSupportsChangeAnimations();
    }

    void viewRangeUpdate(int i, int i2) {
        int unfilteredChildCount = this.mChildHelper.getUnfilteredChildCount();
        int i3 = i + i2;
        for (int i4 = 0; i4 < unfilteredChildCount; i4++) {
            View unfilteredChildAt = this.mChildHelper.getUnfilteredChildAt(i4);
            ViewHolder childViewHolderInt = getChildViewHolderInt(unfilteredChildAt);
            if (childViewHolderInt != null && !childViewHolderInt.shouldIgnore() && childViewHolderInt.mPosition >= i && childViewHolderInt.mPosition < i3) {
                childViewHolderInt.addFlags(2);
                if (supportsChangeAnimations()) {
                    childViewHolderInt.addFlags(64);
                }
                ((LayoutParams) unfilteredChildAt.getLayoutParams()).mInsetsDirty = true;
            }
        }
        this.mRecycler.viewRangeUpdate(i, i2);
    }
}
