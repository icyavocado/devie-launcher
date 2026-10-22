package com.voxel.simplesearchlauncher.allapps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.voxel.simplesearchlauncher.view.BaseRecyclerView;

/* JADX INFO: loaded from: classes.dex */
public class AllAppsRecyclerView extends BaseRecyclerView {
    private BaseRecyclerView.ScrollPositionState mScrollPosState;

    static abstract class AllAppsAdapter extends RecyclerView.Adapter {
        AllAppsAdapter() {
        }

        abstract int getRowCount();

        abstract int getRowHeight(int i);

        abstract int getRowIndex(int i);

        abstract String getSectionTitle(int i);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public AllAppsRecyclerView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        this.mScrollPosState = new BaseRecyclerView.ScrollPositionState();
        getResources();
        addOnItemTouchListener(this);
        this.mScrollbar.setDetachThumbOnFastScroll();
    }

    @Override // androidx.recyclerview.widget.RecyclerView
    public AllAppsAdapter getAdapter() {
        return (AllAppsAdapter) super.getAdapter();
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    protected void getCurScrollState(BaseRecyclerView.ScrollPositionState scrollPositionState) {
        int currentRecyclerViewPosition;
        scrollPositionState.rowIndex = -1;
        scrollPositionState.rowTopOffset = -1;
        scrollPositionState.itemPos = -1;
        if (getAdapter() == null || getAdapter().getItemCount() == 0 || (currentRecyclerViewPosition = getCurrentRecyclerViewPosition()) == -1) {
            return;
        }
        View viewFindViewByPosition = getLayoutManager().findViewByPosition(currentRecyclerViewPosition);
        scrollPositionState.rowIndex = getAdapter().getRowIndex(currentRecyclerViewPosition);
        scrollPositionState.rowTopOffset = getLayoutManager().getDecoratedTop(viewFindViewByPosition);
        scrollPositionState.itemPos = currentRecyclerViewPosition;
    }

    public int getCurrentRecyclerViewPosition() {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        return 0;
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    protected int getTop(int i) {
        if (getChildCount() == 0 || i <= 0) {
            return 0;
        }
        int rowHeight = 0;
        for (int i2 = 0; i2 < i; i2++) {
            rowHeight += getAdapter().getRowHeight(i2);
        }
        return rowHeight;
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    public void onUpdateScrollbar(int i) {
        if (getAdapter() == null || getAdapter().getItemCount() == 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        int rowCount = getAdapter().getRowCount();
        getCurScrollState(this.mScrollPosState);
        if (this.mScrollPosState.rowIndex < 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        getAvailableScrollBarHeight();
        if (getAvailableScrollHeight(rowCount) <= 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
        } else {
            synchronizeScrollBarThumbOffsetToViewScroll(this.mScrollPosState, rowCount);
        }
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    public String scrollToPositionAtProgress(float f) {
        int rowCount;
        if (getAdapter() == null || getAdapter().getItemCount() == 0 || (rowCount = getAdapter().getRowCount()) == 0) {
            return "";
        }
        stopScroll();
        getCurScrollState(this.mScrollPosState);
        float itemCount = getAdapter().getItemCount() * f;
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(0, (int) (-(getAvailableScrollHeight(rowCount) * f)));
        if (f == 1.0f) {
            itemCount -= 1.0f;
        }
        return getAdapter().getSectionTitle((int) itemCount);
    }
}
