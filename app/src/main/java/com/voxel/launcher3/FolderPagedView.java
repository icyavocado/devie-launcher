package com.voxel.launcher3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class FolderPagedView extends PagedView {
    private static final int[] sTempPosArray = new int[2];
    private int mAllocatedContentSize;
    private CellParams mCellParams;
    private FocusIndicatorView mFocusIndicatorView;
    private Folder mFolder;
    private int mGridCountX;
    private int mGridCountY;
    private final IconCache mIconCache;
    private final LayoutInflater mInflater;
    public final boolean mIsRtl;
    private FocusHelper.PagedFolderKeyEventListener mKeyListener;
    private int mMaxCountX;
    private int mMaxCountY;
    private int mMaxItemsPerPage;
    private PageIndicator mPageIndicator;
    final HashMap<View, Runnable> mPendingAnimations;

    public FolderPagedView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPendingAnimations = new HashMap<>();
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        this.mCellParams = launcherAppState.getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.FOLDER);
        this.mMaxCountX = (int) this.mCellParams.numColumns;
        this.mMaxCountY = (int) this.mCellParams.numRows;
        this.mMaxItemsPerPage = this.mMaxCountX * this.mMaxCountY;
        this.mInflater = LayoutInflater.from(context);
        this.mIconCache = launcherAppState.getIconCache();
        this.mIsRtl = Utilities.isRtl(getResources());
        setImportantForAccessibility(1);
    }

    @SuppressLint({"RtlHardcoded"})
    private void arrangeChildren(ArrayList<View> arrayList, int i, boolean z) {
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            CellLayout cellLayout = (CellLayout) getChildAt(i2);
            cellLayout.removeAllViews();
            arrayList2.add(cellLayout);
        }
        setupContentDimensions(i);
        Iterator it = arrayList2.iterator();
        int i3 = 0;
        CellLayout cellLayoutCreateAndAddNewPage = null;
        int i4 = 0;
        int i5 = 0;
        while (i3 < i) {
            View view = arrayList.size() > i3 ? arrayList.get(i3) : null;
            if (cellLayoutCreateAndAddNewPage == null || i4 >= this.mMaxItemsPerPage) {
                cellLayoutCreateAndAddNewPage = it.hasNext() ? (CellLayout) it.next() : createAndAddNewPage();
                i4 = 0;
            }
            if (view != null) {
                CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
                int i6 = this.mGridCountX;
                int i7 = i4 % i6;
                int i8 = i4 / i6;
                ItemInfo itemInfo = (ItemInfo) view.getTag();
                if (itemInfo.cellX != i7 || itemInfo.cellY != i8 || itemInfo.rank != i5) {
                    itemInfo.cellX = i7;
                    itemInfo.cellY = i8;
                    itemInfo.rank = i5;
                    if (z) {
                        LauncherModel.addOrMoveItemInDatabase(getContext(), itemInfo, this.mFolder.mInfo.id, 0L, itemInfo.cellX, itemInfo.cellY);
                    }
                }
                layoutParams.cellX = itemInfo.cellX;
                layoutParams.cellY = itemInfo.cellY;
                cellLayoutCreateAndAddNewPage.addViewToCellLayout(view, -1, this.mFolder.mLauncher != null ? this.mFolder.mLauncher.getViewIdForItem(itemInfo) : 0, layoutParams, true);
            }
            i5++;
            i4++;
            i3++;
        }
        boolean z2 = false;
        while (it.hasNext()) {
            removeView((View) it.next());
            z2 = true;
        }
        if (z2) {
            setCurrentPage(0);
        }
        setEnableOverscroll(getPageCount() > 1);
        this.mPageIndicator.setVisibility(getPageCount() <= 1 ? 8 : 0);
    }

    private CellLayout createAndAddNewPage() {
        CellLayout cellLayout = new CellLayout(getContext());
        cellLayout.setCellDimensions(this.mCellParams.cellWidthPx, this.mCellParams.cellHeightPx);
        cellLayout.getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
        cellLayout.setImportantForAccessibility(2);
        cellLayout.setInvertIfRtl(true);
        cellLayout.setGridSize(this.mGridCountX, this.mGridCountY);
        cellLayout.setCellParams(this.mCellParams);
        addView(cellLayout, -1, generateDefaultLayoutParams());
        return cellLayout;
    }

    public void lambda$realTimeReorder$0(View view, float f, int i) {
        this.mPendingAnimations.remove(view);
        view.setTranslationX(f);
        ((CellLayout) view.getParent().getParent()).removeView(view);
        this.addViewForRank(view, (ShortcutInfo) view.getTag(), i);
    }

    private void setupContentDimensions(int i) {
        this.mAllocatedContentSize = i;
        if (i >= this.mMaxItemsPerPage) {
            this.mGridCountX = this.mMaxCountX;
            this.mGridCountY = this.mMaxCountY;
        } else {
            int i2 = this.mMaxCountX;
            if (i < i2) {
                this.mGridCountX = i;
                this.mGridCountY = 1;
            } else {
                this.mGridCountX = i2;
                this.mGridCountY = (i / i2) + (i % i2 == 0 ? 0 : 1);
            }
        }
        for (int pageCount = getPageCount() - 1; pageCount >= 0; pageCount--) {
            getPageAt(pageCount).setGridSize(this.mGridCountX, this.mGridCountY);
        }
    }

    public void addViewForRank(View view, ShortcutInfo shortcutInfo, int i) {
        int i2 = this.mMaxItemsPerPage;
        int i3 = i % i2;
        int i4 = i / i2;
        shortcutInfo.rank = i;
        int i5 = this.mGridCountX;
        shortcutInfo.cellX = i3 % i5;
        shortcutInfo.cellY = i3 / i5;
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
        layoutParams.cellX = shortcutInfo.cellX;
        layoutParams.cellY = shortcutInfo.cellY;
        getPageAt(i4).addViewToCellLayout(view, -1, this.mFolder.mLauncher != null ? this.mFolder.mLauncher.getViewIdForItem(shortcutInfo) : 0, layoutParams, true);
    }

    public int allocateRankForNewItem(ShortcutInfo shortcutInfo) {
        int itemCount = getItemCount();
        ArrayList<View> arrayList = new ArrayList<>(this.mFolder.getItemsInReadingOrder());
        arrayList.add(itemCount, null);
        arrangeChildren(arrayList, arrayList.size(), false);
        return itemCount;
    }

    public void arrangeChildren(ArrayList<View> arrayList, int i) {
        arrangeChildren(arrayList, i, true);
    }

    public ArrayList<ShortcutInfo> bindItems(ArrayList<ShortcutInfo> arrayList, boolean z) {
        ArrayList<View> arrayList2 = new ArrayList<>();
        ArrayList<ShortcutInfo> arrayList3 = new ArrayList<>();
        Iterator<ShortcutInfo> it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2.add(createNewView(it.next(), z));
        }
        arrangeChildren(arrayList2, arrayList2.size(), false);
        return arrayList3;
    }

    public void clearScrollHint() {
        if (getScrollX() != getScrollForPage(getNextPage())) {
            snapToPage(getNextPage());
        }
    }

    public void completePendingPageChanges() {
        if (this.mPendingAnimations.isEmpty()) {
            return;
        }
        for (Map.Entry<View, Runnable> entry : new HashMap<>(this.mPendingAnimations).entrySet()) { /* REWORK: jadx raw map */
            ((View) entry.getKey()).animate().cancel();
            ((Runnable) entry.getValue()).run();
        }
    }

    public View createAndAddViewForRank(ShortcutInfo shortcutInfo, int i) {
        View viewCreateNewView = createNewView(shortcutInfo, true);
        addViewForRank(viewCreateNewView, shortcutInfo, i);
        return viewCreateNewView;
    }

    @SuppressLint({"InflateParams"})
    public View createNewView(ShortcutInfo shortcutInfo, boolean z) {
        BubbleTextView bubbleTextView = (BubbleTextView) this.mInflater.inflate(R.layout.folder_application, (ViewGroup) null, false);
        bubbleTextView.setCellParams(this.mCellParams);
        bubbleTextView.applyFromShortcutInfo(shortcutInfo, this.mIconCache, z, false);
        bubbleTextView.setOnClickListener(this.mFolder);
        bubbleTextView.setOnLongClickListener(this.mFolder);
        bubbleTextView.setOnFocusChangeListener(this.mFocusIndicatorView);
        bubbleTextView.setOnKeyListener(this.mKeyListener);
        bubbleTextView.setLayoutParams(new CellLayout.LayoutParams(shortcutInfo.cellX, shortcutInfo.cellY, shortcutInfo.spanX, shortcutInfo.spanY));
        return bubbleTextView;
    }

    public int findNearestArea(int i, int i2) {
        int nextPage = getNextPage();
        CellLayout pageAt = getPageAt(nextPage);
        pageAt.findNearestArea(i, i2, 1, 1, sTempPosArray);
        if (this.mFolder.isLayoutRtl()) {
            sTempPosArray[0] = (pageAt.getCountX() - sTempPosArray[0]) - 1;
        }
        int i3 = this.mAllocatedContentSize - 1;
        int i4 = nextPage * this.mMaxItemsPerPage;
        int[] iArr = sTempPosArray;
        return Math.min(i3, i4 + (iArr[1] * this.mGridCountX) + iArr[0]);
    }

    public String getAccessibilityDescription() {
        return String.format(getContext().getString(R.string.folder_opened), Integer.valueOf(this.mGridCountX), Integer.valueOf(this.mGridCountY));
    }

    public int getAllocatedContentSize() {
        return this.mAllocatedContentSize;
    }

    public CellParams getCellParams() {
        return this.mCellParams;
    }

    @Override // com.voxel.launcher3.PagedView
    protected int getChildGap() {
        return getPaddingLeft() + getPaddingRight();
    }

    public CellLayout getCurrentCellLayout() {
        return getPageAt(getNextPage());
    }

    public int getDesiredHeight() {
        if (getPageCount() <= 0) {
            return 0;
        }
        return getPaddingBottom() + getPageAt(0).getDesiredHeight() + getPaddingTop();
    }

    public int getDesiredWidth() {
        if (getPageCount() <= 0) {
            return 0;
        }
        return getPaddingRight() + getPageAt(0).getDesiredWidth() + getPaddingLeft();
    }

    public int getItemCount() {
        int childCount = getChildCount() - 1;
        if (childCount < 0) {
            return 0;
        }
        return getPageAt(childCount).getShortcutsAndWidgets().getChildCount() + (childCount * this.mMaxItemsPerPage);
    }

    public List<View> getItemsForPage(int i) {
        ArrayList arrayList = new ArrayList();
        CellLayout pageAt = getPageAt(i);
        for (int i2 = 0; i2 < pageAt.getCountY(); i2++) {
            for (int i3 = 0; i3 < pageAt.getCountX(); i3++) {
                View childAt = pageAt.getChildAt(i3, i2);
                if (childAt != null) {
                    arrayList.add(childAt);
                }
            }
        }
        return arrayList;
    }

    public View getLastItem() {
        if (getChildCount() < 1) {
            return null;
        }
        ShortcutAndWidgetContainer shortcutsAndWidgets = getCurrentCellLayout().getShortcutsAndWidgets();
        int childCount = shortcutsAndWidgets.getChildCount() - 1;
        int i = this.mGridCountX;
        return i > 0 ? shortcutsAndWidgets.getChildAt(childCount % i, childCount / i) : shortcutsAndWidgets.getChildAt(childCount);
    }

    @Override // com.voxel.launcher3.PagedView
    public CellLayout getPageAt(int i) {
        return (CellLayout) getChildAt(i);
    }

    @Override // com.voxel.launcher3.PagedView
    protected PageIndicator.PageMarkerResources getPageIndicatorMarker(int i) {
        return new PageIndicator.PageMarkerResources(R.drawable.ic_pageindicator_current, R.drawable.ic_pageindicator_default);
    }

    public boolean isFull() {
        return false;
    }

    public int itemsPerPage() {
        return this.mMaxItemsPerPage;
    }

    public View iterateOverItems(Workspace.ItemOperator itemOperator) {
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout pageAt = getPageAt(i);
            for (int i2 = 0; i2 < pageAt.getCountY(); i2++) {
                for (int i3 = 0; i3 < pageAt.getCountX(); i3++) {
                    View childAt = pageAt.getChildAt(i3, i2);
                    if (childAt != null && itemOperator.evaluate((ItemInfo) childAt.getTag(), childAt, this)) {
                        return childAt;
                    }
                }
            }
        }
        return null;
    }

    @Override // com.voxel.launcher3.PagedView
    protected void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        Folder folder = this.mFolder;
        if (folder != null) {
            folder.updateTextViewFocus();
        }
    }

    @Override // com.voxel.launcher3.PagedView
    protected void onPageBeginMoving() {
        super.onPageBeginMoving();
        getVisiblePages(sTempPosArray);
        for (int i = sTempPosArray[0]; i <= sTempPosArray[1]; i++) {
            verifyVisibleHighResIcons(i);
        }
    }

    public boolean rankOnCurrentPage(int i) {
        return i / this.mMaxItemsPerPage == getNextPage();
    }

    public void realTimeReorder(int i, int i2) {
        int i3;
        int i4;
        /* REWORK: jadx declared the reorder cursor final while the reorder loop mutates it. */
        int i5 = i;
        completePendingPageChanges();
        int nextPage = getNextPage();
        int i6 = this.mMaxItemsPerPage;
        int i7 = i2 / i6;
        int i8 = i2 % i6;
        if (i7 != nextPage) {
            Log.e("FolderPagedView", "Cannot animate when the target cell is invisible");
        }
        int i9 = this.mMaxItemsPerPage;
        int i10 = i5 % i9;
        int i11 = i5 / i9;
        if (i2 == i5) {
            return;
        }
        int i12 = -1;
        int i13 = 0;
        if (i2 > i5) {
            if (i11 < nextPage) {
                i12 = nextPage * i9;
                i10 = 0;
            } else {
                i5 = -1;
            }
            i4 = 1;
        } else {
            if (i11 > nextPage) {
                i3 = ((nextPage + 1) * i9) - 1;
                i10 = i9 - 1;
            } else {
                i5 = -1;
                i3 = -1;
            }
            i12 = i3;
            i4 = -1;
        }
        while (i5 != i12) {
            int i14 = i5 + i4;
            int i15 = this.mMaxItemsPerPage;
            int i16 = i14 / i15;
            int i17 = i14 % i15;
            int i18 = this.mGridCountX;
            int i19 = i17 % i18;
            int i20 = i17 / i18;
            CellLayout pageAt = getPageAt(i16);
            final View childAt = pageAt.getChildAt(i19, i20);
            if (childAt != null) {
                if (nextPage != i16) {
                    pageAt.removeView(childAt);
                    addViewForRank(childAt, (ShortcutInfo) childAt.getTag(), i5);
                } else {
                    final float translationX = childAt.getTranslationX();
                    final int i5cap = i5;
                    Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$FolderPagedView$Uv1Yq0vhokZ-xcWrgQ7icXeaeDc
                        @Override // java.lang.Runnable
                        public final void run() {
                            FolderPagedView.this.lambda$realTimeReorder$0( childAt, translationX, i5cap);
                        }
                    };
                    childAt.animate().translationXBy((i4 > 0) ^ this.mIsRtl ? -childAt.getWidth() : childAt.getWidth()).setDuration(230L).setStartDelay(0L).withEndAction(runnable);
                    this.mPendingAnimations.put(childAt, runnable);
                }
            }
            i5 = i14;
        }
        if ((i8 - i10) * i4 <= 0) {
            return;
        }
        CellLayout pageAt2 = getPageAt(nextPage);
        float f = 30.0f;
        while (i10 != i8) {
            int i21 = i10 + i4;
            int i22 = this.mGridCountX;
            View childAt2 = pageAt2.getChildAt(i21 % i22, i21 / i22);
            if (childAt2 != null) {
                ((ItemInfo) childAt2.getTag()).rank -= i4;
            }
            int i23 = this.mGridCountX;
            if (pageAt2.animateChildToPosition(childAt2, i10 % i23, i10 / i23, 230, i13, true, true)) {
                int i24 = (int) (i13 + f);
                f *= 0.9f;
                i13 = i24;
            }
            i10 = i21;
        }
    }

    public void removeItem(View view) {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            getPageAt(childCount).removeView(view);
        }
    }

    public void setCellParams(CellParams cellParams) {
        this.mCellParams = cellParams;
        this.mMaxCountX = (int) this.mCellParams.numColumns;
        this.mMaxCountY = (int) this.mCellParams.numRows;
        this.mMaxItemsPerPage = this.mMaxCountX * this.mMaxCountY;
        for (int i = 0; i < getChildCount(); i++) {
            CellLayout cellLayout = (CellLayout) getChildAt(i);
            cellLayout.setCellDimensions(this.mCellParams.cellWidthPx, this.mCellParams.cellHeightPx);
            cellLayout.setCellParams(cellParams);
        }
    }

    public void setFixedSize(int i, int i2) {
        int paddingLeft = i - (getPaddingLeft() + getPaddingRight());
        int paddingTop = i2 - (getPaddingTop() + getPaddingBottom());
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            ((CellLayout) getChildAt(childCount)).setFixedSize(paddingLeft, paddingTop);
        }
    }

    public void setFolder(Folder folder) {
        this.mFolder = folder;
        this.mFocusIndicatorView = (FocusIndicatorView) folder.findViewById(R.id.focus_indicator);
        this.mKeyListener = new FocusHelper.PagedFolderKeyEventListener(folder);
        this.mPageIndicator = (PageIndicator) folder.findViewById(R.id.folder_page_indicator);
    }

    public void setMarkerScale(float f) {
        int childCount = this.mPageIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mPageIndicator.getChildAt(i);
            childAt.animate().cancel();
            childAt.setScaleX(f);
            childAt.setScaleY(f);
        }
    }

    public void showScrollHint(int i) {
        int scrollForPage = (getScrollForPage(getNextPage()) + ((int) (((i == 0) ^ this.mIsRtl ? -0.07f : 0.07f) * getWidth()))) - getScrollX();
        if (scrollForPage != 0) {
            this.mScroller.setInterpolator(new DecelerateInterpolator());
            this.mScroller.startScroll(getScrollX(), 0, scrollForPage, 0, 500);
            invalidate();
        }
    }

    public void verifyVisibleHighResIcons(int i) {
    }
}
