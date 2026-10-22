package com.voxel.recyclerview.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class GridLayoutManager extends LinearLayoutManager {
    static final int MAIN_DIR_SPEC = View.MeasureSpec.makeMeasureSpec(0, 0);
    int[] mCachedBorders;
    final Rect mDecorInsets;
    final SparseIntArray mPreLayoutSpanIndexCache;
    final SparseIntArray mPreLayoutSpanSizeCache;
    View[] mSet;
    int mSpanCount;
    SpanSizeLookup mSpanSizeLookup;

    public static final class DefaultSpanSizeLookup extends SpanSizeLookup {
        @Override // com.voxel.recyclerview.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanIndex(int i, int i2) {
            return i % i2;
        }

        @Override // com.voxel.recyclerview.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanSize(int i) {
            return 1;
        }
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {
        private int mSpanIndex;
        private int mSpanSize;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
            this.mSpanIndex = -1;
            this.mSpanSize = 0;
        }

        public int getSpanIndex() {
            return this.mSpanIndex;
        }

        public int getSpanSize() {
            return this.mSpanSize;
        }
    }

    public static abstract class SpanSizeLookup {
        final SparseIntArray mSpanIndexCache = new SparseIntArray();
        private boolean mCacheSpanIndices = false;

        int findReferenceIndexFromCache(int i) {
            int size = this.mSpanIndexCache.size() - 1;
            int i2 = 0;
            while (i2 <= size) {
                int i3 = (i2 + size) >>> 1;
                if (this.mSpanIndexCache.keyAt(i3) < i) {
                    i2 = i3 + 1;
                } else {
                    size = i3 - 1;
                }
            }
            int i4 = i2 - 1;
            if (i4 < 0 || i4 >= this.mSpanIndexCache.size()) {
                return -1;
            }
            return this.mSpanIndexCache.keyAt(i4);
        }

        int getCachedSpanIndex(int i, int i2) {
            if (!this.mCacheSpanIndices) {
                return getSpanIndex(i, i2);
            }
            int i3 = this.mSpanIndexCache.get(i, -1);
            if (i3 != -1) {
                return i3;
            }
            int spanIndex = getSpanIndex(i, i2);
            this.mSpanIndexCache.put(i, spanIndex);
            return spanIndex;
        }

        public int getSpanGroupIndex(int i, int i2) {
            int spanSize = getSpanSize(i);
            int i3 = 0;
            int i4 = 0;
            for (int i5 = 0; i5 < i; i5++) {
                int spanSize2 = getSpanSize(i5);
                i3 += spanSize2;
                if (i3 == i2) {
                    i4++;
                    i3 = 0;
                } else if (i3 > i2) {
                    i4++;
                    i3 = spanSize2;
                }
            }
            return i3 + spanSize > i2 ? i4 + 1 : i4;
        }

        public int getSpanIndex(int i, int i2) {
            int i3;
            int spanSize;
            int iFindReferenceIndexFromCache;
            int spanSize2 = getSpanSize(i);
            if (spanSize2 == i2) {
                return 0;
            }
            if (!this.mCacheSpanIndices || this.mSpanIndexCache.size() <= 0 || (iFindReferenceIndexFromCache = findReferenceIndexFromCache(i)) < 0) {
                i3 = 0;
                spanSize = 0;
            } else {
                spanSize = this.mSpanIndexCache.get(iFindReferenceIndexFromCache) + getSpanSize(iFindReferenceIndexFromCache);
                i3 = iFindReferenceIndexFromCache + 1;
            }
            while (i3 < i) {
                int spanSize3 = getSpanSize(i3);
                spanSize += spanSize3;
                if (spanSize == i2) {
                    spanSize = 0;
                } else if (spanSize > i2) {
                    spanSize = spanSize3;
                }
                i3++;
            }
            if (spanSize2 + spanSize <= i2) {
                return spanSize;
            }
            return 0;
        }

        public abstract int getSpanSize(int i);

        public void invalidateSpanIndexCache() {
            this.mSpanIndexCache.clear();
        }
    }

    public GridLayoutManager(Context context, int i) {
        super(context);
        this.mSpanCount = -1;
        this.mPreLayoutSpanSizeCache = new SparseIntArray();
        this.mPreLayoutSpanIndexCache = new SparseIntArray();
        this.mSpanSizeLookup = new DefaultSpanSizeLookup();
        this.mDecorInsets = new Rect();
        setSpanCount(i);
    }

    private void assignSpans(RecyclerView.Recycler recycler, RecyclerView.State state, int i, int i2, boolean z) {
        int i3;
        int i4;
        int i5;
        int i6;
        int i7 = 0;
        if (z) {
            i4 = i;
            i3 = 0;
            i5 = 1;
        } else {
            i3 = i - 1;
            i4 = -1;
            i5 = -1;
        }
        if (this.mOrientation == 1 && isLayoutRTL()) {
            i7 = i2 - 1;
            i6 = -1;
        } else {
            i6 = 1;
        }
        while (i3 != i4) {
            View view = this.mSet[i3];
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            layoutParams.mSpanSize = getSpanSize(recycler, state, getPosition(view));
            if (i6 != -1 || layoutParams.mSpanSize <= 1) {
                layoutParams.mSpanIndex = i7;
            } else {
                layoutParams.mSpanIndex = i7 - (layoutParams.mSpanSize - 1);
            }
            i7 += layoutParams.mSpanSize * i6;
            i3 += i5;
        }
    }

    private void cachePreLayoutSpanMapping() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            int viewLayoutPosition = layoutParams.getViewLayoutPosition();
            this.mPreLayoutSpanSizeCache.put(viewLayoutPosition, layoutParams.getSpanSize());
            this.mPreLayoutSpanIndexCache.put(viewLayoutPosition, layoutParams.getSpanIndex());
        }
    }

    private void calculateItemBorders(int i) {
        int i2;
        int[] iArr = this.mCachedBorders;
        int i3 = 1;
        if (iArr == null || iArr.length != this.mSpanCount + 1 || iArr[iArr.length - 1] != i) {
            this.mCachedBorders = new int[this.mSpanCount + 1];
        }
        int i4 = 0;
        this.mCachedBorders[0] = 0;
        int i5 = this.mSpanCount;
        int i6 = i / i5;
        int i7 = i % i5;
        int i8 = 0;
        while (true) {
            int i9 = this.mSpanCount;
            if (i3 > i9) {
                return;
            }
            i4 += i7;
            if (i4 <= 0 || i9 - i4 >= i7) {
                i2 = i6;
            } else {
                i2 = i6 + 1;
                i4 -= i9;
            }
            i8 += i2;
            this.mCachedBorders[i3] = i8;
            i3++;
        }
    }

    private void clearPreLayoutSpanMappingCache() {
        this.mPreLayoutSpanSizeCache.clear();
        this.mPreLayoutSpanIndexCache.clear();
    }

    private void ensureAnchorIsInFirstSpan(LinearLayoutManager.AnchorInfo anchorInfo) {
        int cachedSpanIndex = this.mSpanSizeLookup.getCachedSpanIndex(anchorInfo.mPosition, this.mSpanCount);
        while (cachedSpanIndex > 0 && anchorInfo.mPosition > 0) {
            anchorInfo.mPosition--;
            cachedSpanIndex = this.mSpanSizeLookup.getCachedSpanIndex(anchorInfo.mPosition, this.mSpanCount);
        }
    }

    private int getMainDirSpec(int i) {
        return i < 0 ? MAIN_DIR_SPEC : View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    }

    private int getSpanGroupIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getSpanGroupIndex(i, this.mSpanCount);
        }
        int iConvertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
        if (iConvertPreLayoutPositionToPostLayout != -1) {
            return this.mSpanSizeLookup.getSpanGroupIndex(iConvertPreLayoutPositionToPostLayout, this.mSpanCount);
        }
        Log.w("GridLayoutManager", "Cannot find span size for pre layout position. " + i);
        return 0;
    }

    private int getSpanIndex(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getCachedSpanIndex(i, this.mSpanCount);
        }
        int i2 = this.mPreLayoutSpanIndexCache.get(i, -1);
        if (i2 != -1) {
            return i2;
        }
        int iConvertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
        if (iConvertPreLayoutPositionToPostLayout != -1) {
            return this.mSpanSizeLookup.getCachedSpanIndex(iConvertPreLayoutPositionToPostLayout, this.mSpanCount);
        }
        Log.w("GridLayoutManager", "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + i);
        return 0;
    }

    private int getSpanSize(RecyclerView.Recycler recycler, RecyclerView.State state, int i) {
        if (!state.isPreLayout()) {
            return this.mSpanSizeLookup.getSpanSize(i);
        }
        int i2 = this.mPreLayoutSpanSizeCache.get(i, -1);
        if (i2 != -1) {
            return i2;
        }
        int iConvertPreLayoutPositionToPostLayout = recycler.convertPreLayoutPositionToPostLayout(i);
        if (iConvertPreLayoutPositionToPostLayout != -1) {
            return this.mSpanSizeLookup.getSpanSize(iConvertPreLayoutPositionToPostLayout);
        }
        Log.w("GridLayoutManager", "Cannot find span size for pre layout position. It is not cached, not in the adapter. Pos:" + i);
        return 1;
    }

    private void measureChildWithDecorationsAndMargin(View view, int i, int i2) {
        calculateItemDecorationsForChild(view, this.mDecorInsets);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        view.measure(updateSpecWithExtra(i, layoutParams.leftMargin + this.mDecorInsets.left, layoutParams.rightMargin + this.mDecorInsets.right), updateSpecWithExtra(i2, layoutParams.topMargin + this.mDecorInsets.top, layoutParams.bottomMargin + this.mDecorInsets.bottom));
    }

    private void updateMeasurements() {
        calculateItemBorders(getOrientation() == 1 ? (getWidth() - getPaddingRight()) - getPaddingLeft() : (getHeight() - getPaddingBottom()) - getPaddingTop());
    }

    private int updateSpecWithExtra(int i, int i2, int i3) {
        if (i2 == 0 && i3 == 0) {
            return i;
        }
        int mode = View.MeasureSpec.getMode(i);
        return (mode == Integer.MIN_VALUE || mode == 1073741824) ? View.MeasureSpec.makeMeasureSpec((View.MeasureSpec.getSize(i) - i2) - i3, mode) : i;
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public boolean checkLayoutParams(RecyclerView.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // com.voxel.recyclerview.widget.LinearLayoutManager, com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(Context context, AttributeSet attributeSet) {
        return new LayoutParams(context, attributeSet);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public int getColumnCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 1) {
            return this.mSpanCount;
        }
        if (state.getItemCount() < 1) {
            return 0;
        }
        return getSpanGroupIndex(recycler, state, state.getItemCount() - 1);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public int getRowCountForAccessibility(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 0) {
            return this.mSpanCount;
        }
        if (state.getItemCount() < 1) {
            return 0;
        }
        return getSpanGroupIndex(recycler, state, state.getItemCount() - 1);
    }

    public int getSpanCount() {
        return this.mSpanCount;
    }

    @Override // com.voxel.recyclerview.widget.LinearLayoutManager
    void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state, LinearLayoutManager.LayoutState layoutState, LinearLayoutManager.LayoutChunkResult layoutChunkResult) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int i8;
        int decoratedMeasurementInOther;
        int decoratedMeasurementInOther2;
        View next;
        int i9 = 0;
        boolean z = layoutState.mItemDirection == 1;
        int spanIndex = this.mSpanCount;
        if (z) {
            i = 0;
            i2 = 0;
        } else {
            spanIndex = getSpanIndex(recycler, state, layoutState.mCurrentPosition) + getSpanSize(recycler, state, layoutState.mCurrentPosition);
            i = 0;
            i2 = 0;
        }
        while (i2 < this.mSpanCount && layoutState.hasMore(state) && spanIndex > 0) {
            int i10 = layoutState.mCurrentPosition;
            int spanSize = getSpanSize(recycler, state, i10);
            if (spanSize > this.mSpanCount) {
                throw new IllegalArgumentException("Item at position " + i10 + " requires " + spanSize + " spans but GridLayoutManager has only " + this.mSpanCount + " spans.");
            }
            spanIndex -= spanSize;
            if (spanIndex < 0 || (next = layoutState.next(recycler)) == null) {
                break;
            }
            i += spanSize;
            this.mSet[i2] = next;
            i2++;
        }
        if (i2 == 0) {
            layoutChunkResult.mFinished = true;
            return;
        }
        assignSpans(recycler, state, i2, i, z);
        int i11 = 0;
        for (int i12 = 0; i12 < i2; i12++) {
            View view = this.mSet[i12];
            if (layoutState.mScrapList == null) {
                if (z) {
                    addView(view);
                } else {
                    addView(view, 0);
                }
            } else if (z) {
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(this.mCachedBorders[layoutParams.mSpanIndex + layoutParams.mSpanSize] - this.mCachedBorders[layoutParams.mSpanIndex], 1073741824);
            if (this.mOrientation == 1) {
                measureChildWithDecorationsAndMargin(view, iMakeMeasureSpec, getMainDirSpec(layoutParams.height));
            } else {
                measureChildWithDecorationsAndMargin(view, getMainDirSpec(layoutParams.width), iMakeMeasureSpec);
            }
            int decoratedMeasurement = this.mOrientationHelper.getDecoratedMeasurement(view);
            if (decoratedMeasurement > i11) {
                i11 = decoratedMeasurement;
            }
        }
        int mainDirSpec = getMainDirSpec(i11);
        for (int i13 = 0; i13 < i2; i13++) {
            View view2 = this.mSet[i13];
            if (this.mOrientationHelper.getDecoratedMeasurement(view2) != i11) {
                LayoutParams layoutParams2 = (LayoutParams) view2.getLayoutParams();
                int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mCachedBorders[layoutParams2.mSpanIndex + layoutParams2.mSpanSize] - this.mCachedBorders[layoutParams2.mSpanIndex], 1073741824);
                if (this.mOrientation == 1) {
                    measureChildWithDecorationsAndMargin(view2, iMakeMeasureSpec2, mainDirSpec);
                } else {
                    measureChildWithDecorationsAndMargin(view2, mainDirSpec, iMakeMeasureSpec2);
                }
            }
        }
        layoutChunkResult.mConsumed = i11;
        if (this.mOrientation == 1) {
            if (layoutState.mLayoutDirection == -1) {
                i6 = layoutState.mOffset;
                i4 = i6 - i11;
                i5 = 0;
                i3 = 0;
            } else {
                int i14 = layoutState.mOffset;
                i4 = i14;
                i6 = i11 + i14;
                i5 = 0;
                i3 = 0;
            }
        } else if (layoutState.mLayoutDirection == -1) {
            int i15 = layoutState.mOffset;
            i5 = i15 - i11;
            i3 = i15;
            i6 = 0;
            i4 = 0;
        } else {
            int i16 = layoutState.mOffset;
            i3 = i11 + i16;
            i4 = 0;
            i5 = i16;
            i6 = 0;
        }
        while (i9 < i2) {
            View view3 = this.mSet[i9];
            LayoutParams layoutParams3 = (LayoutParams) view3.getLayoutParams();
            if (this.mOrientation == 1) {
                int paddingLeft = getPaddingLeft() + this.mCachedBorders[layoutParams3.mSpanIndex];
                decoratedMeasurementInOther2 = i6;
                i8 = paddingLeft;
                decoratedMeasurementInOther = this.mOrientationHelper.getDecoratedMeasurementInOther(view3) + paddingLeft;
                i7 = i4;
            } else {
                int paddingTop = getPaddingTop() + this.mCachedBorders[layoutParams3.mSpanIndex];
                i7 = paddingTop;
                i8 = i5;
                decoratedMeasurementInOther = i3;
                decoratedMeasurementInOther2 = this.mOrientationHelper.getDecoratedMeasurementInOther(view3) + paddingTop;
            }
            layoutDecorated(view3, i8 + layoutParams3.leftMargin, i7 + layoutParams3.topMargin, decoratedMeasurementInOther - layoutParams3.rightMargin, decoratedMeasurementInOther2 - layoutParams3.bottomMargin);
            if (layoutParams3.isItemRemoved() || layoutParams3.isItemChanged()) {
                layoutChunkResult.mIgnoreConsumed = true;
            }
            layoutChunkResult.mFocusable |= view3.isFocusable();
            i9++;
            i5 = i8;
            i4 = i7;
            i3 = decoratedMeasurementInOther;
            i6 = decoratedMeasurementInOther2;
        }
        Arrays.fill(this.mSet, (Object) null);
    }

    @Override // com.voxel.recyclerview.widget.LinearLayoutManager
    void onAnchorReady(RecyclerView.State state, LinearLayoutManager.AnchorInfo anchorInfo) {
        super.onAnchorReady(state, anchorInfo);
        updateMeasurements();
        if (state.getItemCount() > 0 && !state.isPreLayout()) {
            ensureAnchorIsInFirstSpan(anchorInfo);
        }
        View[] viewArr = this.mSet;
        if (viewArr == null || viewArr.length != this.mSpanCount) {
            this.mSet = new View[this.mSpanCount];
        }
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof LayoutParams)) {
            super.onInitializeAccessibilityNodeInfoForItem(view, accessibilityNodeInfoCompat);
            return;
        }
        LayoutParams layoutParams2 = (LayoutParams) layoutParams;
        int spanGroupIndex = getSpanGroupIndex(recycler, state, layoutParams2.getViewLayoutPosition());
        if (this.mOrientation == 0) {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(layoutParams2.getSpanIndex(), layoutParams2.getSpanSize(), spanGroupIndex, 1, this.mSpanCount > 1 && layoutParams2.getSpanSize() == this.mSpanCount, false));
        } else {
            accessibilityNodeInfoCompat.setCollectionItemInfo(AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(spanGroupIndex, 1, layoutParams2.getSpanIndex(), layoutParams2.getSpanSize(), this.mSpanCount > 1 && layoutParams2.getSpanSize() == this.mSpanCount, false));
        }
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public void onItemsAdded(RecyclerView recyclerView, int i, int i2) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public void onItemsChanged(RecyclerView recyclerView) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public void onItemsMoved(RecyclerView recyclerView, int i, int i2, int i3) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public void onItemsRemoved(RecyclerView recyclerView, int i, int i2) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public void onItemsUpdated(RecyclerView recyclerView, int i, int i2) {
        this.mSpanSizeLookup.invalidateSpanIndexCache();
    }

    @Override // com.voxel.recyclerview.widget.LinearLayoutManager, com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.isPreLayout()) {
            cachePreLayoutSpanMapping();
        }
        super.onLayoutChildren(recycler, state);
        clearPreLayoutSpanMappingCache();
    }

    public void setSpanCount(int i) {
        if (i == this.mSpanCount) {
            return;
        }
        if (i >= 1) {
            this.mSpanCount = i;
            this.mSpanSizeLookup.invalidateSpanIndexCache();
        } else {
            throw new IllegalArgumentException("Span count should be at least 1. Provided " + i);
        }
    }

    @Override // com.voxel.recyclerview.widget.LinearLayoutManager, com.voxel.recyclerview.widget.RecyclerView.LayoutManager
    public boolean supportsPredictiveItemAnimations() {
        return this.mPendingSavedState == null;
    }
}
