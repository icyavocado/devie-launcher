package com.voxel.recyclerview.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import androidx.core.view.ViewCompat;

/* JADX INFO: loaded from: classes.dex */
public class SpringRecyclerView extends RecyclerView {
    private int mBottom;
    private int mCurrentPositionX;
    private int mCurrentPositionY;
    private SpringPositionAdapterDataObserver mObserver;
    private float mOffsetYRemainder;
    private int mTop;

    public interface SpringItemPositioner {
        int getItemViewHeightForPosition(int i);

        boolean hasUniformViewHeight();
    }

    private class SpringPositionAdapterDataObserver extends RecyclerView.AdapterDataObserver {
        private SpringPositionAdapterDataObserver() {
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onChanged() {
            SpringRecyclerView.this.resetOverscrollPositions();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeChanged(int i, int i2) {
            onChanged();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeInserted(int i, int i2) {
            onChanged();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.AdapterDataObserver
        public void onItemRangeRemoved(int i, int i2) {
            onChanged();
        }
    }

    private class SpringViewFlinger implements RecyclerView.IViewFlinger {
        private int mLastFlingX;
        private int mLastFlingY;
        private boolean mEatRunOnAnimationRequest = false;
        private boolean mReSchedulePostAnimationCallback = false;
        private SpringOverScroller mScroller = new SpringOverScroller();

        public SpringViewFlinger() {
        }

        private void disableRunOnAnimationRequests() {
            this.mReSchedulePostAnimationCallback = false;
            this.mEatRunOnAnimationRequest = true;
        }

        private void enableRunOnAnimationRequests() {
            this.mEatRunOnAnimationRequest = false;
            if (this.mReSchedulePostAnimationCallback) {
                postOnAnimation();
            }
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void fling(int i, int i2) {
            SpringRecyclerView.this.setScrollState(2);
            this.mLastFlingX = SpringRecyclerView.this.mCurrentPositionX;
            this.mLastFlingY = SpringRecyclerView.this.mCurrentPositionY;
            SpringRecyclerView springRecyclerView = SpringRecyclerView.this;
            float fCalculateDragOverscrollFactor = springRecyclerView.calculateDragOverscrollFactor(springRecyclerView.calculateOverscrollX());
            SpringRecyclerView springRecyclerView2 = SpringRecyclerView.this;
            this.mScroller.fling(this.mLastFlingX, this.mLastFlingY, i * fCalculateDragOverscrollFactor, i2 * springRecyclerView2.calculateDragOverscrollFactor(springRecyclerView2.calculateOverscrollY()));
            postOnAnimation();
        }

        public void postOnAnimation() {
            if (this.mEatRunOnAnimationRequest) {
                this.mReSchedulePostAnimationCallback = true;
            } else {
                SpringRecyclerView.this.removeCallbacks(this);
                ViewCompat.postOnAnimation(SpringRecyclerView.this, this);
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            int iScrollHorizontallyBy;
            int i;
            int iScrollVerticallyBy;
            int i2;
            int i3;
            disableRunOnAnimationRequests();
            SpringRecyclerView.this.consumePendingUpdateOperations();
            SpringOverScroller springOverScroller = this.mScroller;
            RecyclerView.SmoothScroller smoothScroller = SpringRecyclerView.this.getLayoutManager().mSmoothScroller;
            int currX = springOverScroller.getCurrX();
            int currY = springOverScroller.getCurrY();
            int i4 = currX - this.mLastFlingX;
            int i5 = currY - this.mLastFlingY;
            this.mLastFlingX = currX;
            this.mLastFlingY = currY;
            if (SpringRecyclerView.this.getAdapter() != null) {
                SpringRecyclerView.this.eatRequestLayout();
                SpringRecyclerView springRecyclerView = SpringRecyclerView.this;
                springRecyclerView.mRunningLayoutOrScroll = true;
                if (i4 != 0) {
                    iScrollHorizontallyBy = springRecyclerView.getLayoutManager().scrollHorizontallyBy(i4, SpringRecyclerView.this.mRecycler, SpringRecyclerView.this.mState);
                    i = i4 - iScrollHorizontallyBy;
                } else {
                    iScrollHorizontallyBy = 0;
                    i = 0;
                }
                if (i5 != 0) {
                    iScrollVerticallyBy = SpringRecyclerView.this.getLayoutManager().scrollVerticallyBy(i5, SpringRecyclerView.this.mRecycler, SpringRecyclerView.this.mState);
                    i2 = i5 - iScrollVerticallyBy;
                } else {
                    iScrollVerticallyBy = 0;
                    i2 = 0;
                }
                if (SpringRecyclerView.this.supportsChangeAnimations()) {
                    int childCount = SpringRecyclerView.this.mChildHelper.getChildCount();
                    for (int i6 = 0; i6 < childCount; i6++) {
                        View childAt = SpringRecyclerView.this.mChildHelper.getChildAt(i6);
                        RecyclerView.ViewHolder childViewHolder = SpringRecyclerView.this.getChildViewHolder(childAt);
                        if (childViewHolder != null && childViewHolder.mShadowingHolder != null) {
                            View view = childViewHolder.mShadowingHolder.itemView;
                            int left = childAt.getLeft();
                            int top = childAt.getTop();
                            if (left != view.getLeft() || top != view.getTop()) {
                                view.layout(left, top, view.getWidth() + left, top + view.getHeight());
                            }
                        }
                    }
                }
                if (smoothScroller != null && !smoothScroller.isPendingInitialRun() && smoothScroller.isRunning()) {
                    int itemCount = SpringRecyclerView.this.mState.getItemCount();
                    if (itemCount == 0) {
                        smoothScroller.stop();
                    } else if (smoothScroller.getTargetPosition() >= itemCount) {
                        smoothScroller.setTargetPosition(itemCount - 1);
                        smoothScroller.onAnimation(i4 - i, i5 - i2);
                    } else {
                        smoothScroller.onAnimation(i4 - i, i5 - i2);
                    }
                }
                SpringRecyclerView springRecyclerView2 = SpringRecyclerView.this;
                springRecyclerView2.mRunningLayoutOrScroll = false;
                springRecyclerView2.resumeRequestLayout(false);
            } else {
                iScrollHorizontallyBy = 0;
                i = 0;
                iScrollVerticallyBy = 0;
                i2 = 0;
            }
            if (!SpringRecyclerView.this.mItemDecorations.isEmpty()) {
                SpringRecyclerView.this.invalidate();
            }
            if (i != 0 || i2 != 0) {
                SpringRecyclerView.this.getLayoutManager().offsetChildrenVertical(-i2);
            }
            if (this.mScroller.isCoastingY() && SpringRecyclerView.this.calculateOverscrollY() != 0) {
                this.mScroller.springBack(0, SpringRecyclerView.this.getCurrentScrollPositionY(), 0, 0, SpringRecyclerView.this.mTop, SpringRecyclerView.this.mBottom);
            }
            if (iScrollHorizontallyBy != 0 || iScrollVerticallyBy != 0 || i != 0 || i2 != 0) {
                SpringRecyclerView.this.notifyOnScrolled(iScrollHorizontallyBy + i, iScrollVerticallyBy + i2);
            }
            if (!SpringRecyclerView.this.awakenScrollBars()) {
                SpringRecyclerView.this.invalidate();
            }
            if (springOverScroller.isFinished()) {
                i3 = 0;
                SpringRecyclerView.this.setScrollState(0);
            } else {
                i3 = 0;
                postOnAnimation();
            }
            if (smoothScroller != null && smoothScroller.isPendingInitialRun()) {
                smoothScroller.onAnimation(i3, i3);
            }
            enableRunOnAnimationRequests();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void smoothScrollBy(int i, int i2) {
            smoothScrollBy(i, i2, 0, 0);
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void smoothScrollBy(int i, int i2, int i3) {
            smoothScrollBy(i, i2, i3, (Interpolator) null);
        }

        public void smoothScrollBy(int i, int i2, int i3, int i4) {
            smoothScrollBy(i, i2, 0, (Interpolator) null);
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void smoothScrollBy(int i, int i2, int i3, Interpolator interpolator) {
            SpringRecyclerView.this.setScrollState(2);
            this.mLastFlingX = SpringRecyclerView.this.mCurrentPositionX;
            this.mLastFlingY = SpringRecyclerView.this.mCurrentPositionY;
            SpringOverScroller springOverScroller = this.mScroller;
            int i4 = this.mLastFlingX;
            int i5 = this.mLastFlingY;
            springOverScroller.startScroll(i4, i5, i + i4, i2 + i5);
            postOnAnimation();
        }

        @Override // com.voxel.recyclerview.widget.RecyclerView.IViewFlinger
        public void stop() {
            SpringRecyclerView.this.removeCallbacks(this);
            this.mScroller.abortAnimation();
        }
    }

    public SpringRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCurrentPositionX = 0;
        this.mCurrentPositionY = 0;
        this.mOffsetYRemainder = 0.0f;
        this.mTop = Integer.MIN_VALUE;
        this.mBottom = Integer.MAX_VALUE;
        this.mObserver = new SpringPositionAdapterDataObserver();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public float calculateDragOverscrollFactor(int i) {
        if (i == 0) {
            return 1.0f;
        }
        return 80.0f / (Math.abs(i) + 80.0f);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int calculateOverscrollX() {
        int i = this.mCurrentPositionX;
        if (i < 0) {
            return i;
        }
        int iComputeHorizontalScrollRange = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        int i2 = this.mCurrentPositionX;
        if (i2 > iComputeHorizontalScrollRange) {
            return i2 - iComputeHorizontalScrollRange;
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int calculateOverscrollY() {
        int i = this.mCurrentPositionY;
        int i2 = this.mTop;
        if (i < i2) {
            return i - i2;
        }
        int i3 = this.mBottom;
        if (i > i3) {
            return i - i3;
        }
        return 0;
    }

    private int handleOffsetYRemainder(float f) {
        this.mOffsetYRemainder += f % 1.0f;
        float f2 = this.mOffsetYRemainder;
        int i = ((int) f) + ((int) f2);
        this.mOffsetYRemainder = f2 - ((int) f2);
        return i;
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView
    protected RecyclerView.IViewFlinger createViewFlinger() {
        return new SpringViewFlinger();
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView
    void dispatchLayout() {
        int itemViewHeightForPosition;
        super.dispatchLayout();
        resetOverscrollPositions();
        if (getChildCount() <= 0) {
            this.mCurrentPositionY = 0;
            return;
        }
        SpringItemPositioner springItemPositioner = (SpringItemPositioner) getAdapter();
        View childAt = getChildAt(0);
        for (int i = 1; i < getChildCount(); i++) {
            View childAt2 = getChildAt(i);
            if (childAt2.getTop() < childAt.getTop()) {
                childAt = childAt2;
            }
        }
        int childAdapterPosition = getChildAdapterPosition(childAt);
        if (springItemPositioner.hasUniformViewHeight()) {
            itemViewHeightForPosition = (springItemPositioner.getItemViewHeightForPosition(0) * childAdapterPosition) + 0;
        } else {
            int itemViewHeightForPosition2 = 0;
            for (int i2 = 0; i2 < childAdapterPosition; i2++) {
                itemViewHeightForPosition2 += springItemPositioner.getItemViewHeightForPosition(i2);
            }
            itemViewHeightForPosition = itemViewHeightForPosition2;
        }
        this.mCurrentPositionY = itemViewHeightForPosition - getLayoutManager().getDecoratedTop(childAt);
    }

    public int getCurrentScrollPositionX() {
        return this.mCurrentPositionX;
    }

    public int getCurrentScrollPositionY() {
        return this.mCurrentPositionY;
    }

    public int getScrollBottom() {
        return this.mBottom;
    }

    public int getScrollTop() {
        return this.mTop;
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView
    public void offsetChildrenHorizontal(int i) {
        this.mCurrentPositionX -= i;
        super.offsetChildrenHorizontal(i);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView
    public void offsetChildrenVertical(int i) {
        this.mCurrentPositionY -= i;
        super.offsetChildrenVertical(i);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView
    public boolean onIdleTouchUp() {
        int iCalculateOverscrollY = calculateOverscrollY();
        if (iCalculateOverscrollY == 0) {
            return false;
        }
        smoothScrollBy(0, -iCalculateOverscrollY);
        return true;
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView, android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i == i3 && i2 == i4) {
            return;
        }
        resetOverscrollPositions();
    }

    @Override // android.view.View
    protected boolean overScrollBy(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z) {
        int i9;
        boolean z2;
        int i10;
        boolean z3;
        computeHorizontalScrollRange();
        computeHorizontalScrollExtent();
        computeVerticalScrollRange();
        computeVerticalScrollExtent();
        int i11 = i3 + i;
        int i12 = i4 + i2;
        int i13 = (-i7) - 0;
        int i14 = i7 + i5 + 0;
        int i15 = (-i8) - 0;
        int i16 = i8 + i6 + 0;
        if (i11 > i14) {
            i9 = i11 - i14;
            z2 = true;
        } else if (i11 < i13) {
            i9 = i11 - i13;
            z2 = true;
        } else {
            i9 = 0;
            z2 = false;
        }
        if (i12 > i16) {
            i10 = i12 - i16;
            z3 = true;
        } else if (i12 < i15) {
            i10 = i12 - i15;
            z3 = true;
        } else {
            i10 = 0;
            z3 = false;
        }
        if (i9 != 0) {
            calculateDragOverscrollFactor(i9);
        }
        float fCalculateDragOverscrollFactor = i10 != 0 ? (calculateDragOverscrollFactor(i10) + calculateDragOverscrollFactor(i10 - i2)) / 2.0f : 1.0f;
        getScrollState();
        if (getScrollState() == 1 && z3) {
            offsetChildrenVertical(handleOffsetYRemainder(-(i2 * fCalculateDragOverscrollFactor)));
        }
        return z2 || z3;
    }

    public void resetOverscrollPositions() {
        int itemViewHeightForPosition;
        this.mTop = 0;
        if (getAdapter() == null || getAdapter().getItemCount() <= 0) {
            this.mBottom = 0;
            return;
        }
        SpringItemPositioner springItemPositioner = (SpringItemPositioner) getAdapter();
        if (springItemPositioner.hasUniformViewHeight()) {
            itemViewHeightForPosition = springItemPositioner.getItemViewHeightForPosition(0) * getAdapter().getItemCount();
        } else {
            int itemViewHeightForPosition2 = 0;
            for (int i = 0; i < getAdapter().getItemCount(); i++) {
                itemViewHeightForPosition2 += springItemPositioner.getItemViewHeightForPosition(i);
            }
            itemViewHeightForPosition = itemViewHeightForPosition2;
        }
        this.mBottom = itemViewHeightForPosition - getHeight();
        this.mBottom = Math.max(this.mBottom, 0);
    }

    /* JADX WARN: Code duplicated, block: B:15:0x005b  */
    @Override // com.voxel.recyclerview.widget.RecyclerView
    protected boolean scrollByInternal(int i, int i2) {
        int i3;
        int i4;
        int i5;
        int i6;
        int iScrollVerticallyBy;
        int i7;
        int i8 = this.mCurrentPositionY;
        consumePendingUpdateOperations();
        if (getAdapter() != null) {
            eatRequestLayout();
            this.mRunningLayoutOrScroll = true;
            if (i != 0) {
                int iScrollHorizontallyBy = getLayoutManager().scrollHorizontallyBy(i, this.mRecycler, this.mState);
                i4 = i - iScrollHorizontallyBy;
                i5 = iScrollHorizontallyBy;
            } else {
                i4 = 0;
                i5 = 0;
            }
            if (i2 == 0) {
                iScrollVerticallyBy = 0;
                i7 = 0;
            } else if (calculateOverscrollY() != 0 ? overScrollBy(0, i2, 0, this.mCurrentPositionY, 0, this.mBottom, 0, 0, false) : false) {
                iScrollVerticallyBy = 0;
                i7 = 0;
            } else {
                iScrollVerticallyBy = getLayoutManager().scrollVerticallyBy(i2, this.mRecycler, this.mState);
                i7 = i2 - iScrollVerticallyBy;
            }
            if (supportsChangeAnimations()) {
                int childCount = this.mChildHelper.getChildCount();
                for (int i9 = 0; i9 < childCount; i9++) {
                    View childAt = this.mChildHelper.getChildAt(i9);
                    RecyclerView.ViewHolder childViewHolder = getChildViewHolder(childAt);
                    if (childViewHolder != null && childViewHolder.mShadowingHolder != null) {
                        RecyclerView.ViewHolder viewHolder = childViewHolder.mShadowingHolder;
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
            i6 = iScrollVerticallyBy;
            i3 = i7;
        } else {
            i3 = 0;
            i4 = 0;
            i5 = 0;
            i6 = 0;
        }
        this.mRunningLayoutOrScroll = false;
        resumeRequestLayout(false);
        if (!this.mItemDecorations.isEmpty()) {
            invalidate();
        }
        if (ViewCompat.getOverScrollMode(this) != 2 && (i4 != 0 || i3 != 0)) {
            overScrollBy(i4, i3, this.mCurrentPositionX, this.mCurrentPositionY, 0, this.mBottom, 0, 0, false);
        }
        if (i5 != 0 || i4 != 0 || this.mCurrentPositionY != i8) {
            notifyOnScrolled(i5 + i4, i6 + i3);
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        return (i5 == 0 && i6 == 0) ? false : true;
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView
    protected void setAdapterInternal(RecyclerView.Adapter adapter, boolean z, boolean z2) {
        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(this.mObserver);
        }
        super.setAdapterInternal(adapter, z, z2);
        if (getAdapter() != null) {
            getAdapter().registerAdapterDataObserver(this.mObserver);
        }
        resetOverscrollPositions();
    }
}
