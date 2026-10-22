package com.voxel.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class ButtonDropTarget extends TextView implements DropTarget, DragController.DragListener {
    protected boolean mActive;
    private int mBottomDragPadding;
    protected int mHoverColor;
    protected Launcher mLauncher;
    protected SearchDropTargetBar mSearchDropTargetBar;
    protected final int mTransitionDuration;

    public ButtonDropTarget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ButtonDropTarget(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mHoverColor = 0;
        Resources resources = getResources();
        this.mTransitionDuration = resources.getInteger(R.integer.config_dropTargetBgTransitionDuration);
        this.mBottomDragPadding = resources.getDimensionPixelSize(R.dimen.drop_target_drag_padding);
    }

    private boolean isRtl() {
        return getLayoutDirection() == 1;
    }

    @Override // com.voxel.launcher3.DropTarget
    public boolean acceptDrop(DropTarget.DragObject dragObject) {
        return false;
    }

    protected Drawable getCurrentDrawable() {
        for (Drawable drawable : getCompoundDrawablesRelative()) {
            if (drawable != null) {
                return drawable;
            }
        }
        return null;
    }

    @Override // com.voxel.launcher3.DropTarget
    public void getHitRectRelativeToDragLayer(Rect rect) {
        super.getHitRect(rect);
        rect.bottom += this.mBottomDragPadding;
        int[] iArr = new int[2];
        this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, iArr);
        rect.offsetTo(iArr[0], iArr[1]);
    }

    public int getHoverColor() {
        return this.mHoverColor;
    }

    Rect getIconRect(int i, int i2, int i3, int i4) {
        int paddingLeft;
        int paddingRight;
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        Rect rect = new Rect();
        dragLayer.getViewRectRelativeToSelf(this, rect);
        if (isRtl()) {
            paddingRight = rect.right - getPaddingRight();
            paddingLeft = paddingRight - i3;
        } else {
            paddingLeft = getPaddingLeft() + rect.left;
            paddingRight = paddingLeft + i3;
        }
        int measuredHeight = rect.top + ((getMeasuredHeight() - i4) / 2);
        rect.set(paddingLeft, measuredHeight, paddingRight, measuredHeight + i4);
        rect.offset((-(i - i3)) / 2, (-(i2 - i4)) / 2);
        return rect;
    }

    @Override // com.voxel.launcher3.DropTarget
    public boolean isDropEnabled() {
        return this.mActive;
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragEnd() {
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragEnter(DropTarget.DragObject dragObject) {
        dragObject.dragView.setColor(this.mHoverColor);
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragExit(DropTarget.DragObject dragObject) {
        dragObject.dragView.setColor(0);
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragOver(DropTarget.DragObject dragObject) {
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDrop(DropTarget.DragObject dragObject) {
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onFlingToDelete(DropTarget.DragObject dragObject, PointF pointF) {
    }

    void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void setSearchDropTargetBar(SearchDropTargetBar searchDropTargetBar) {
        this.mSearchDropTargetBar = searchDropTargetBar;
    }
}
