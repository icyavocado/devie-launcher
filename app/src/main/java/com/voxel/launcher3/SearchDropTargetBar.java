package com.voxel.launcher3;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class SearchDropTargetBar extends FrameLayout implements DragController.DragListener {
    protected static final TimeInterpolator DEFAULT_INTERPOLATOR = new AccelerateInterpolator();
    private boolean mDeferOnDragEnd;
    private View mDropTargetBar;
    private boolean mDropTargetBarVisible;
    private View mQSBSearchBar;
    private ButtonDropTarget mRemoveDropTarget;
    private boolean mSearchBarForceHiddenOverride;
    private boolean mSearchBarVisible;

    public SearchDropTargetBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SearchDropTargetBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDropTargetBarVisible = false;
        this.mSearchBarVisible = true;
        this.mSearchBarForceHiddenOverride = false;
        this.mDeferOnDragEnd = false;
    }

    private void animateDropTargetBarVisibility(boolean z) {
        if (this.mDropTargetBarVisible != z) {
            this.mDropTargetBarVisible = z;
            this.mDropTargetBar.animate().cancel();
            float f = this.mDropTargetBarVisible ? 1.0f : 0.0f;
            if (Float.compare(this.mDropTargetBar.getAlpha(), f) != 0) {
                this.mDropTargetBar.animate().withLayer().alpha(f).setInterpolator(DEFAULT_INTERPOLATOR).setDuration(175L);
            }
        }
    }

    private void animateSearchBarVisibility(boolean z) {
        if (this.mSearchBarVisible != z) {
            this.mSearchBarVisible = z;
            this.mQSBSearchBar.animate().cancel();
            float f = this.mSearchBarVisible ? 1.0f : 0.0f;
            if (Float.compare(this.mQSBSearchBar.getAlpha(), f) != 0) {
                this.mQSBSearchBar.animate().withLayer().alpha(f).setInterpolator(DEFAULT_INTERPOLATOR).setDuration(175L);
            }
        }
    }

    public void deferOnDragEnd() {
        this.mDeferOnDragEnd = true;
    }

    public Rect getSearchBarBounds() {
        View view = this.mQSBSearchBar;
        if (view == null) {
            return null;
        }
        int[] iArr = new int[2];
        view.getLocationOnScreen(iArr);
        Rect rect = new Rect();
        rect.left = iArr[0];
        rect.top = iArr[1];
        rect.right = iArr[0] + this.mQSBSearchBar.getWidth();
        rect.bottom = iArr[1] + this.mQSBSearchBar.getHeight();
        return rect;
    }

    public void hideSearchBar(boolean z) {
        if (z) {
            animateSearchBarVisibility(true);
        } else {
            this.mQSBSearchBar.animate().cancel();
            this.mQSBSearchBar.setAlpha(0.0f);
        }
        this.mSearchBarForceHiddenOverride = true;
        this.mSearchBarVisible = false;
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragEnd() {
        if (this.mDeferOnDragEnd) {
            this.mDeferOnDragEnd = false;
            return;
        }
        animateDropTargetBarVisibility(false);
        if (this.mSearchBarForceHiddenOverride) {
            return;
        }
        animateSearchBarVisibility(true);
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
        if (dragObject.isDesktopLocked) {
            return;
        }
        animateDropTargetBarVisibility(true);
        if (this.mSearchBarForceHiddenOverride) {
            return;
        }
        animateSearchBarVisibility(false);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mDropTargetBar = findViewById(R.id.drag_target_bar);
        this.mRemoveDropTarget = (ButtonDropTarget) this.mDropTargetBar.findViewById(R.id.remove_target_text);
        this.mRemoveDropTarget.setSearchDropTargetBar(this);
    }

    public void setup(Launcher launcher, DragController dragController) {
        this.mRemoveDropTarget.setLauncher(launcher);
        this.mQSBSearchBar = launcher.getQsbBar();
    }

    public void showSearchBar(boolean z) {
        if (z) {
            animateSearchBarVisibility(true);
        } else {
            this.mQSBSearchBar.animate().cancel();
            this.mQSBSearchBar.setAlpha(1.0f);
        }
        this.mSearchBarForceHiddenOverride = false;
        this.mSearchBarVisible = true;
    }
}
