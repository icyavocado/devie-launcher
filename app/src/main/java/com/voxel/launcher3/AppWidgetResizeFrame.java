package com.voxel.launcher3;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Rect;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class AppWidgetResizeFrame extends FrameLayout {
    private static Rect mTmpRect = new Rect();
    final int BACKGROUND_PADDING;
    final float DIMMED_HANDLE_ALPHA;
    final float RESIZE_THRESHOLD;
    final int SNAP_DURATION;
    private int mBackgroundPadding;
    private int mBaselineHeight;
    private int mBaselineWidth;
    private int mBaselineX;
    private int mBaselineY;
    private boolean mBottomBorderActive;
    private ImageView mBottomHandle;
    private int mBottomTouchRegionAdjustment;
    private CellLayout mCellLayout;
    private int mDeltaX;
    private int mDeltaXAddOn;
    private int mDeltaY;
    private int mDeltaYAddOn;
    int[] mDirectionVector;
    private DragLayer mDragLayer;
    int[] mLastDirectionVector;
    private Launcher mLauncher;
    private boolean mLeftBorderActive;
    private ImageView mLeftHandle;
    private int mMinHSpan;
    private int mMinVSpan;
    private int mResizeMode;
    private boolean mRightBorderActive;
    private ImageView mRightHandle;
    private int mRunningHInc;
    private int mRunningVInc;
    int[] mTmpPt;
    private boolean mTopBorderActive;
    private ImageView mTopHandle;
    private int mTopTouchRegionAdjustment;
    private int mTouchTargetWidth;
    private int mWidgetPaddingBottom;
    private int mWidgetPaddingLeft;
    private int mWidgetPaddingRight;
    private int mWidgetPaddingTop;
    private LauncherAppWidgetHostView mWidgetView;

    public AppWidgetResizeFrame(Context context, LauncherAppWidgetHostView launcherAppWidgetHostView, CellLayout cellLayout, DragLayer dragLayer) {
        super(context);
        this.mTopTouchRegionAdjustment = 0;
        this.mBottomTouchRegionAdjustment = 0;
        this.mDirectionVector = new int[2];
        this.mLastDirectionVector = new int[2];
        this.mTmpPt = new int[2];
        this.SNAP_DURATION = 150;
        this.BACKGROUND_PADDING = 24;
        this.DIMMED_HANDLE_ALPHA = 0.0f;
        this.RESIZE_THRESHOLD = 0.66f;
        this.mLauncher = (Launcher) context;
        this.mCellLayout = cellLayout;
        this.mWidgetView = launcherAppWidgetHostView;
        /* REWORK(provider-limits): donor hardcoded mode 3; honor the provider.
         * Framework values (0 none, 1 horizontal, 2 vertical, 3 both) already
         * match this class's encoding (& 1 horizontal, & 2 vertical). */
        this.mResizeMode = launcherAppWidgetHostView.getAppWidgetInfo().resizeMode;
        this.mDragLayer = dragLayer;
        int[] minSpanForWidget = Launcher.getMinSpanForWidget(this.mLauncher, launcherAppWidgetHostView.getAppWidgetInfo());
        this.mMinHSpan = minSpanForWidget[0];
        this.mMinVSpan = minSpanForWidget[1];
        setBackgroundResource(R.drawable.widget_resize_frame_holo);
        setPadding(0, 0, 0, 0);
        this.mLeftHandle = new ImageView(context);
        this.mLeftHandle.setImageResource(R.drawable.widget_resize_handle_left);
        addView(this.mLeftHandle, new FrameLayout.LayoutParams(-2, -2, 19));
        this.mRightHandle = new ImageView(context);
        this.mRightHandle.setImageResource(R.drawable.widget_resize_handle_right);
        addView(this.mRightHandle, new FrameLayout.LayoutParams(-2, -2, 21));
        this.mTopHandle = new ImageView(context);
        this.mTopHandle.setImageResource(R.drawable.widget_resize_handle_top);
        addView(this.mTopHandle, new FrameLayout.LayoutParams(-2, -2, 49));
        this.mBottomHandle = new ImageView(context);
        this.mBottomHandle.setImageResource(R.drawable.widget_resize_handle_bottom);
        addView(this.mBottomHandle, new FrameLayout.LayoutParams(-2, -2, 81));
        Rect defaultPaddingForWidget = AppWidgetHostView.getDefaultPaddingForWidget(context, launcherAppWidgetHostView.getAppWidgetInfo().provider, null);
        this.mWidgetPaddingLeft = defaultPaddingForWidget.left;
        this.mWidgetPaddingTop = defaultPaddingForWidget.top;
        this.mWidgetPaddingRight = defaultPaddingForWidget.right;
        this.mWidgetPaddingBottom = defaultPaddingForWidget.bottom;
        int i = this.mResizeMode;
        if (i == 1) {
            this.mTopHandle.setVisibility(8);
            this.mBottomHandle.setVisibility(8);
        } else if (i == 2) {
            this.mLeftHandle.setVisibility(8);
            this.mRightHandle.setVisibility(8);
        } else if (i == 0) {
            this.mLeftHandle.setVisibility(8);
            this.mRightHandle.setVisibility(8);
            this.mTopHandle.setVisibility(8);
            this.mBottomHandle.setVisibility(8);
        }
        this.mBackgroundPadding = (int) Math.ceil(this.mLauncher.getResources().getDisplayMetrics().density * 24.0f);
        this.mTouchTargetWidth = this.mBackgroundPadding * 2;
        this.mCellLayout.markCellsAsUnoccupiedForView(this.mWidgetView);
    }

    public static Rect getWidgetSizeRanges(Launcher launcher, int i, int i2, Rect rect) {
        if (rect == null) {
            rect = new Rect();
        }
        Rect cellLayoutMetrics = Workspace.getCellLayoutMetrics(launcher, 0);
        Rect cellLayoutMetrics2 = Workspace.getCellLayoutMetrics(launcher, 1);
        float f = launcher.getResources().getDisplayMetrics().density;
        int i3 = cellLayoutMetrics.left;
        int i4 = cellLayoutMetrics.top;
        int i5 = i - 1;
        int i6 = (int) (((i3 * i) + (cellLayoutMetrics.right * i5)) / f);
        int i7 = i2 - 1;
        rect.set((int) (((i * cellLayoutMetrics2.left) + (i5 * cellLayoutMetrics2.right)) / f), (int) (((i4 * i2) + (cellLayoutMetrics.bottom * i7)) / f), i6, (int) (((i2 * cellLayoutMetrics2.top) + (i7 * cellLayoutMetrics2.bottom)) / f));
        return rect;
    }

    private void resizeWidgetIfNeeded(boolean z) {
        int iMin;
        int i;
        int iMin2;
        int i2;
        int i3;
        int i4;
        int i5;
        int cellWidth = this.mCellLayout.getCellWidth() + this.mCellLayout.getWidthGap();
        int cellHeight = this.mCellLayout.getCellHeight() + this.mCellLayout.getHeightGap();
        int i6 = this.mDeltaX + this.mDeltaXAddOn;
        int i7 = this.mDeltaY + this.mDeltaYAddOn;
        float f = ((i6 * 1.0f) / cellWidth) - this.mRunningHInc;
        float f2 = ((i7 * 1.0f) / cellHeight) - this.mRunningVInc;
        int countX = this.mCellLayout.getCountX();
        int countY = this.mCellLayout.getCountY();
        int iRound = Math.abs(f) > 0.66f ? Math.round(f) : 0;
        int iRound2 = Math.abs(f2) > 0.66f ? Math.round(f2) : 0;
        if (!z && iRound == 0 && iRound2 == 0) {
            return;
        }
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) this.mWidgetView.getLayoutParams();
        int i8 = layoutParams.cellHSpan;
        int i9 = layoutParams.cellVSpan;
        int i10 = layoutParams.useTmpCoords ? layoutParams.tmpCellX : layoutParams.cellX;
        int i11 = layoutParams.useTmpCoords ? layoutParams.tmpCellY : layoutParams.cellY;
        if (this.mLeftBorderActive) {
            iMin = Math.min(layoutParams.cellHSpan - this.mMinHSpan, Math.max(-i10, iRound));
            iRound = Math.max(-(layoutParams.cellHSpan - this.mMinHSpan), Math.min(i10, iRound * (-1)));
            i = -iRound;
        } else if (this.mRightBorderActive) {
            iRound = Math.max(-(layoutParams.cellHSpan - this.mMinHSpan), Math.min(countX - (i10 + i8), iRound));
            i = iRound;
            iMin = 0;
        } else {
            iMin = 0;
            i = 0;
        }
        if (this.mTopBorderActive) {
            iMin2 = Math.min(layoutParams.cellVSpan - this.mMinVSpan, Math.max(-i11, iRound2));
            iRound2 = Math.max(-(layoutParams.cellVSpan - this.mMinVSpan), Math.min(i11, iRound2 * (-1)));
            i2 = -iRound2;
        } else if (this.mBottomBorderActive) {
            iRound2 = Math.max(-(layoutParams.cellVSpan - this.mMinVSpan), Math.min(countY - (i11 + i9), iRound2));
            i2 = iRound2;
            iMin2 = 0;
        } else {
            iMin2 = 0;
            i2 = 0;
        }
        int[] iArr = this.mDirectionVector;
        iArr[0] = 0;
        iArr[1] = 0;
        if (this.mLeftBorderActive || this.mRightBorderActive) {
            int i12 = i8 + iRound;
            i10 += iMin;
            if (i != 0) {
                this.mDirectionVector[0] = this.mLeftBorderActive ? -1 : 1;
            }
            i3 = i12;
        } else {
            i3 = i8;
        }
        if (this.mTopBorderActive || this.mBottomBorderActive) {
            int i13 = i9 + iRound2;
            int i14 = i11 + iMin2;
            if (i2 != 0) {
                this.mDirectionVector[1] = this.mTopBorderActive ? -1 : 1;
            }
            i4 = i14;
            i5 = i13;
        } else {
            i4 = i11;
            i5 = i9;
        }
        if (!z && i2 == 0 && i == 0) {
            return;
        }
        if (z) {
            int[] iArr2 = this.mDirectionVector;
            int[] iArr3 = this.mLastDirectionVector;
            iArr2[0] = iArr3[0];
            iArr2[1] = iArr3[1];
        } else {
            int[] iArr4 = this.mLastDirectionVector;
            int[] iArr5 = this.mDirectionVector;
            iArr4[0] = iArr5[0];
            iArr4[1] = iArr5[1];
        }
        int i15 = i10;
        if (this.mCellLayout.createAreaForResize(i10, i4, i3, i5, this.mWidgetView, this.mDirectionVector, z)) {
            layoutParams.tmpCellX = i15;
            layoutParams.tmpCellY = i4;
            layoutParams.cellHSpan = i3;
            layoutParams.cellVSpan = i5;
            this.mRunningVInc += i2;
            this.mRunningHInc += i;
            if (!z) {
                updateWidgetSizeRanges(this.mWidgetView, this.mLauncher, i3, i5);
            }
        }
        this.mWidgetView.requestLayout();
    }

    public static void updateWidgetSizeRanges(AppWidgetHostView appWidgetHostView, Launcher launcher, int i, int i2) {
        getWidgetSizeRanges(launcher, i, i2, mTmpRect);
        appWidgetHostView.updateAppWidgetSize(null, mTmpRect.left, mTmpRect.top, mTmpRect.right, mTmpRect.bottom);
    }

    private void visualizeResizeForDelta(int i, int i2, boolean z) {
        updateDeltas(i, i2);
        DragLayer.LayoutParams layoutParams = (DragLayer.LayoutParams) getLayoutParams();
        if (this.mLeftBorderActive) {
            int i3 = this.mBaselineX;
            int i4 = this.mDeltaX;
            layoutParams.x = i3 + i4;
            layoutParams.width = this.mBaselineWidth - i4;
        } else if (this.mRightBorderActive) {
            layoutParams.width = this.mBaselineWidth + this.mDeltaX;
        }
        if (this.mTopBorderActive) {
            int i5 = this.mBaselineY;
            int i6 = this.mDeltaY;
            layoutParams.y = i5 + i6;
            layoutParams.height = this.mBaselineHeight - i6;
        } else if (this.mBottomBorderActive) {
            layoutParams.height = this.mBaselineHeight + this.mDeltaY;
        }
        resizeWidgetIfNeeded(z);
        requestLayout();
    }

    public boolean beginResizeIfPointInRegion(int i, int i2) {
        boolean z = true;
        boolean z2 = (this.mResizeMode & 1) != 0;
        boolean z3 = (this.mResizeMode & 2) != 0;
        this.mLeftBorderActive = i < this.mTouchTargetWidth && z2;
        this.mRightBorderActive = i > getWidth() - this.mTouchTargetWidth && z2;
        this.mTopBorderActive = i2 < this.mTouchTargetWidth + this.mTopTouchRegionAdjustment && z3;
        this.mBottomBorderActive = i2 > (getHeight() - this.mTouchTargetWidth) + this.mBottomTouchRegionAdjustment && z3;
        if (!this.mLeftBorderActive && !this.mRightBorderActive && !this.mTopBorderActive && !this.mBottomBorderActive) {
            z = false;
        }
        this.mBaselineWidth = getMeasuredWidth();
        this.mBaselineHeight = getMeasuredHeight();
        this.mBaselineX = getLeft();
        this.mBaselineY = getTop();
        if (z) {
            this.mLeftHandle.setAlpha(this.mLeftBorderActive ? 1.0f : 0.0f);
            this.mRightHandle.setAlpha(this.mRightBorderActive ? 1.0f : 0.0f);
            this.mTopHandle.setAlpha(this.mTopBorderActive ? 1.0f : 0.0f);
            this.mBottomHandle.setAlpha(this.mBottomBorderActive ? 1.0f : 0.0f);
        }
        return z;
    }

    public void commitResize() {
        resizeWidgetIfNeeded(true);
        requestLayout();
    }

    public void onTouchUp() {
        int cellWidth = this.mCellLayout.getCellWidth() + this.mCellLayout.getWidthGap();
        int cellHeight = this.mCellLayout.getCellHeight() + this.mCellLayout.getHeightGap();
        this.mDeltaXAddOn = this.mRunningHInc * cellWidth;
        this.mDeltaYAddOn = this.mRunningVInc * cellHeight;
        this.mDeltaX = 0;
        this.mDeltaY = 0;
        post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$AppWidgetResizeFrame$r10t0UflCLHX2a8SmLlyNZbTFL8
            @Override // java.lang.Runnable
            public final void run() {
                AppWidgetResizeFrame.this.snapToWidget(true);
            }
        });
    }

    public void snapToWidget(boolean z) {
        DragLayer.LayoutParams layoutParams = (DragLayer.LayoutParams) getLayoutParams();
        int width = ((this.mWidgetView.getWidth() + (this.mBackgroundPadding * 2)) - this.mWidgetPaddingLeft) - this.mWidgetPaddingRight;
        int height = ((this.mWidgetView.getHeight() + (this.mBackgroundPadding * 2)) - this.mWidgetPaddingTop) - this.mWidgetPaddingBottom;
        this.mTmpPt[0] = this.mWidgetView.getLeft();
        this.mTmpPt[1] = this.mWidgetView.getTop();
        this.mDragLayer.getDescendantCoordRelativeToSelf(this.mCellLayout.getShortcutsAndWidgets(), this.mTmpPt);
        int[] iArr = this.mTmpPt;
        int i = iArr[0];
        int i2 = this.mBackgroundPadding;
        int i3 = (i - i2) + this.mWidgetPaddingLeft;
        int i4 = (iArr[1] - i2) + this.mWidgetPaddingTop;
        if (i4 < 0) {
            this.mTopTouchRegionAdjustment = -i4;
        } else {
            this.mTopTouchRegionAdjustment = 0;
        }
        int i5 = i4 + height;
        if (i5 > this.mDragLayer.getHeight()) {
            this.mBottomTouchRegionAdjustment = -(i5 - this.mDragLayer.getHeight());
        } else {
            this.mBottomTouchRegionAdjustment = 0;
        }
        if (!z) {
            layoutParams.width = width;
            layoutParams.height = height;
            layoutParams.x = i3;
            layoutParams.y = i4;
            this.mLeftHandle.setAlpha(1.0f);
            this.mRightHandle.setAlpha(1.0f);
            this.mTopHandle.setAlpha(1.0f);
            this.mBottomHandle.setAlpha(1.0f);
            requestLayout();
            return;
        }
        ObjectAnimator objectAnimatorOfPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(layoutParams, this, PropertyValuesHolder.ofInt("width", layoutParams.width, width), PropertyValuesHolder.ofInt("height", layoutParams.height, height), PropertyValuesHolder.ofInt("x", layoutParams.x, i3), PropertyValuesHolder.ofInt("y", layoutParams.y, i4));
        ObjectAnimator objectAnimatorOfFloat = LauncherAnimUtils.ofFloat(this.mLeftHandle, "alpha", 1.0f);
        ObjectAnimator objectAnimatorOfFloat2 = LauncherAnimUtils.ofFloat(this.mRightHandle, "alpha", 1.0f);
        ObjectAnimator objectAnimatorOfFloat3 = LauncherAnimUtils.ofFloat(this.mTopHandle, "alpha", 1.0f);
        ObjectAnimator objectAnimatorOfFloat4 = LauncherAnimUtils.ofFloat(this.mBottomHandle, "alpha", 1.0f);
        objectAnimatorOfPropertyValuesHolder.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$AppWidgetResizeFrame$9WTXpKSx6vOzCB2QzAav9NkwOhk
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                AppWidgetResizeFrame.this.requestLayout();
            }
        });
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        int i6 = this.mResizeMode;
        if (i6 == 2) {
            animatorSetCreateAnimatorSet.playTogether(objectAnimatorOfPropertyValuesHolder, objectAnimatorOfFloat3, objectAnimatorOfFloat4);
        } else if (i6 == 1) {
            animatorSetCreateAnimatorSet.playTogether(objectAnimatorOfPropertyValuesHolder, objectAnimatorOfFloat, objectAnimatorOfFloat2);
        } else {
            animatorSetCreateAnimatorSet.playTogether(objectAnimatorOfPropertyValuesHolder, objectAnimatorOfFloat, objectAnimatorOfFloat2, objectAnimatorOfFloat3, objectAnimatorOfFloat4);
        }
        animatorSetCreateAnimatorSet.setDuration(150L);
        animatorSetCreateAnimatorSet.start();
    }

    public void updateDeltas(int i, int i2) {
        if (this.mLeftBorderActive) {
            this.mDeltaX = i;
            this.mDeltaX = Math.min(this.mBaselineWidth - (this.mTouchTargetWidth * 2), this.mDeltaX);
        } else if (this.mRightBorderActive) {
            this.mDeltaX = i;
            this.mDeltaX = Math.max((-this.mBaselineWidth) + (this.mTouchTargetWidth * 2), this.mDeltaX);
        }
        if (this.mTopBorderActive) {
            this.mDeltaY = Math.max(-this.mBaselineY, i2);
            this.mDeltaY = Math.min(this.mBaselineHeight - (this.mTouchTargetWidth * 2), this.mDeltaY);
        } else if (this.mBottomBorderActive) {
            this.mDeltaY = Math.min(this.mDragLayer.getHeight() - (this.mBaselineY + this.mBaselineHeight), i2);
            this.mDeltaY = Math.max((-this.mBaselineHeight) + (this.mTouchTargetWidth * 2), this.mDeltaY);
        }
    }

    public void visualizeResizeForDelta(int i, int i2) {
        visualizeResizeForDelta(i, i2, false);
    }
}
