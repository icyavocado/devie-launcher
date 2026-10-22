package com.voxel.simplesearchlauncher.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.voxel.simplesearchlauncher.view.BaseRecyclerView;

/* JADX INFO: loaded from: classes2.dex */
public class WidgetsRecyclerView extends BaseRecyclerView {
    private BaseRecyclerView.ScrollPositionState mScrollPosState;
    private WidgetsModel mWidgets;

    public WidgetsRecyclerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WidgetsRecyclerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mScrollPosState = new BaseRecyclerView.ScrollPositionState();
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    protected void getCurScrollState(BaseRecyclerView.ScrollPositionState scrollPositionState) {
        scrollPositionState.rowIndex = -1;
        scrollPositionState.rowTopOffset = -1;
        scrollPositionState.itemPos = -1;
        WidgetsModel widgetsModel = this.mWidgets;
        if (widgetsModel == null || widgetsModel.getPackageSize() == 0) {
            return;
        }
        View childAt = getChildAt(0);
        int childPosition = getChildPosition(childAt);
        scrollPositionState.rowIndex = childPosition;
        scrollPositionState.rowTopOffset = getLayoutManager().getDecoratedTop(childAt);
        scrollPositionState.itemPos = childPosition;
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    protected int getTop(int i) {
        if (getChildCount() == 0) {
            return 0;
        }
        return getChildAt(0).getMeasuredHeight() * i;
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    public void onUpdateScrollbar(int i) {
        WidgetsModel widgetsModel = this.mWidgets;
        if (widgetsModel == null) {
            return;
        }
        int packageSize = widgetsModel.getPackageSize();
        if (packageSize == 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
            return;
        }
        getCurScrollState(this.mScrollPosState);
        if (this.mScrollPosState.rowIndex < 0) {
            this.mScrollbar.setThumbOffset(-1, -1);
        } else {
            synchronizeScrollBarThumbOffsetToViewScroll(this.mScrollPosState, packageSize);
        }
    }

    @Override // com.voxel.simplesearchlauncher.view.BaseRecyclerView
    public String scrollToPositionAtProgress(float f) {
        int packageSize;
        WidgetsModel widgetsModel = this.mWidgets;
        if (widgetsModel == null || (packageSize = widgetsModel.getPackageSize()) == 0) {
            return "";
        }
        stopScroll();
        getCurScrollState(this.mScrollPosState);
        float f2 = packageSize * f;
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(0, (int) (-(getAvailableScrollHeight(packageSize) * f)));
        if (f == 1.0f) {
            f2 -= 1.0f;
        }
        return this.mWidgets.getPackageItemInfo((int) f2).titleSectionName;
    }

    public void setWidgets(WidgetsModel widgetsModel) {
        this.mWidgets = widgetsModel;
    }
}
