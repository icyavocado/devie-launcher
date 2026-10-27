package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import com.ovacoda.devie.R;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/* JADX INFO: loaded from: classes.dex */
public class CellLayout extends ViewGroup {
    private static final Paint sPaint = new Paint();
    private float FOREGROUND_ALPHA_DAMPER;
    private NinePatchDrawable mActiveGlowBackground;
    private float mBackgroundAlpha;
    private float mBackgroundAlphaMultiplier;
    private Rect mBackgroundRect;
    private int mCellHeight;
    private CellParams mCellParams;
    private int mCellWidth;
    private PorterDuffColorFilter mColorFilter;
    private int mColorTint;
    private int mCountX;
    private int mCountY;
    private DeviceProfile mDeviceProfile;
    private int[] mDirectionVector;
    private final int[] mDragCell;
    private float[] mDragOutlineAlphas;
    private InterruptibleInOutAnimator[] mDragOutlineAnims;
    private int mDragOutlineCurrent;
    private final Paint mDragOutlinePaint;
    private Rect[] mDragOutlines;
    private boolean mDragging;
    private boolean mDrawBackground;
    private boolean mDropPending;
    private TimeInterpolator mEaseOutInterpolator;
    private int mFixedCellHeight;
    private int mFixedCellWidth;
    private int mFixedHeight;
    private int mFixedWidth;
    private int[] mFolderLeaveBehindCell;
    private ArrayList<FolderIcon.FolderRingAnimator> mFolderOuterRings;
    private int mForegroundAlpha;
    private int mForegroundPadding;
    private Rect mForegroundRect;
    private int mHeightGap;
    private View.OnTouchListener mInterceptTouchListener;
    private ArrayList<View> mIntersectingViews;
    private boolean mIsDragOverlapping;
    private boolean mIsDragTarget;
    private boolean mItemPlacementDirty;
    private boolean mLastDownOnOccupiedCell;
    private Launcher mLauncher;
    private int mMaxGap;
    private NinePatchDrawable mNormalBackground;
    boolean[][] mOccupied;
    private Rect mOccupiedRect;
    private int mOriginalHeightGap;
    private int mOriginalWidthGap;
    private Drawable mOverScrollForegroundDrawable;
    private Drawable mOverScrollLeft;
    private Drawable mOverScrollRight;
    int[] mPreviousReorderDirection;
    private HashMap<LayoutParams, Animator> mReorderAnimators;
    private float mReorderPreviewAnimationMagnitude;
    private HashMap<View, ReorderPreviewAnimation> mShakeAnimators;
    private ShortcutAndWidgetContainer mShortcutsAndWidgets;
    int[] mTempLocation;
    private Rect mTempRect;
    private final Stack<Rect> mTempRectStack;
    boolean[][] mTmpOccupied;
    private final int[] mTmpPoint;
    private final int[] mTmpXY;
    private final FastBitmapView mTouchFeedbackView;
    boolean mUseActiveGlowBackground;
    private int mWidthGap;

    private class CellAndSpan {
        int spanX;
        int spanY;
        int x;
        int y;

        public CellAndSpan() {
        }

        public CellAndSpan(int i, int i2, int i3, int i4) {
            this.x = i;
            this.y = i2;
            this.spanX = i3;
            this.spanY = i4;
        }

        public void copy(CellAndSpan cellAndSpan) {
            cellAndSpan.x = this.x;
            cellAndSpan.y = this.y;
            cellAndSpan.spanX = this.spanX;
            cellAndSpan.spanY = this.spanY;
        }

        public String toString() {
            return "(" + this.x + ", " + this.y + ": " + this.spanX + ", " + this.spanY + ")";
        }
    }

    static final class CellInfo {
        View cell;
        int cellX;
        int cellY;
        long container;
        long screenId;
        int spanX;
        int spanY;

        CellInfo(View view, ItemInfo itemInfo) {
            this.cellX = -1;
            this.cellY = -1;
            this.cell = view;
            this.cellX = itemInfo.cellX;
            this.cellY = itemInfo.cellY;
            this.spanX = itemInfo.spanX;
            this.spanY = itemInfo.spanY;
            this.screenId = itemInfo.screenId;
            this.container = itemInfo.container;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Cell[view=");
            View view = this.cell;
            sb.append(view == null ? "null" : view.getClass());
            sb.append(", x=");
            sb.append(this.cellX);
            sb.append(", y=");
            sb.append(this.cellY);
            sb.append("]");
            return sb.toString();
        }
    }

    private class ItemConfiguration {
        int dragViewSpanX;
        int dragViewSpanY;
        int dragViewX;
        int dragViewY;
        ArrayList<View> intersectingViews;
        boolean isSolution;
        HashMap<View, CellAndSpan> map;
        private HashMap<View, CellAndSpan> savedMap;
        ArrayList<View> sortedViews;

        private ItemConfiguration() {
            this.map = new HashMap<>();
            this.savedMap = new HashMap<>();
            this.sortedViews = new ArrayList<>();
            this.isSolution = false;
        }

        void add(View view, CellAndSpan cellAndSpan) {
            this.map.put(view, cellAndSpan);
            this.savedMap.put(view, CellLayout.this.new CellAndSpan());
            this.sortedViews.add(view);
        }

        int area() {
            return this.dragViewSpanX * this.dragViewSpanY;
        }

        void restore() {
            for (View view : this.savedMap.keySet()) {
                this.savedMap.get(view).copy(this.map.get(view));
            }
        }

        void save() {
            for (View view : this.map.keySet()) {
                this.map.get(view).copy(this.savedMap.get(view));
            }
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public boolean canReorder;

        @ViewDebug.ExportedProperty
        public int cellHSpan;

        @ViewDebug.ExportedProperty
        public int cellVSpan;

        @ViewDebug.ExportedProperty
        public int cellX;

        @ViewDebug.ExportedProperty
        public int cellY;
        public boolean controlsOwnPadding;
        boolean dropped;
        public boolean isFullscreen;
        public boolean isLockedToGrid;
        public int tmpCellX;
        public int tmpCellY;
        public boolean useTmpCoords;

        @ViewDebug.ExportedProperty
        public int x;

        @ViewDebug.ExportedProperty
        public int y;

        public LayoutParams(int i, int i2, int i3, int i4) {
            super(-1, -1);
            this.isLockedToGrid = true;
            this.isFullscreen = false;
            this.canReorder = true;
            this.controlsOwnPadding = false;
            this.cellX = i;
            this.cellY = i2;
            this.cellHSpan = i3;
            this.cellVSpan = i4;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.isLockedToGrid = true;
            this.isFullscreen = false;
            this.canReorder = true;
            this.controlsOwnPadding = false;
            this.cellHSpan = 1;
            this.cellVSpan = 1;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.isLockedToGrid = true;
            this.isFullscreen = false;
            this.canReorder = true;
            this.controlsOwnPadding = false;
            this.cellHSpan = 1;
            this.cellVSpan = 1;
        }

        public void setup(int i, int i2, int i3, int i4, boolean z, int i5) {
            if (this.isLockedToGrid) {
                int i6 = this.cellHSpan;
                int i7 = this.cellVSpan;
                int i8 = this.useTmpCoords ? this.tmpCellX : this.cellX;
                int i9 = this.useTmpCoords ? this.tmpCellY : this.cellY;
                if (z) {
                    i8 = (i5 - i8) - this.cellHSpan;
                }
                this.width = (((i6 * i) + ((i6 - 1) * i3)) - this.leftMargin) - this.rightMargin;
                this.height = (((i7 * i2) + ((i7 - 1) * i4)) - this.topMargin) - this.bottomMargin;
                this.x = (i8 * (i + i3)) + this.leftMargin;
                this.y = (i9 * (i2 + i4)) + this.topMargin;
            }
        }

        public String toString() {
            return "(" + this.cellX + ", " + this.cellY + ")";
        }
    }

    class ReorderPreviewAnimation {
        Animator a;
        View child;
        float finalDeltaX;
        float finalDeltaY;
        float finalScale;
        float initDeltaX;
        float initDeltaY;
        float initScale;
        int mode;
        boolean repeating = false;

        public ReorderPreviewAnimation(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
            CellLayout.this.regionToCenterPoint(i2, i3, i6, i7, CellLayout.this.mTmpPoint);
            int i8 = CellLayout.this.mTmpPoint[0];
            int i9 = CellLayout.this.mTmpPoint[1];
            CellLayout.this.regionToCenterPoint(i4, i5, i6, i7, CellLayout.this.mTmpPoint);
            int i10 = CellLayout.this.mTmpPoint[0] - i8;
            int i11 = CellLayout.this.mTmpPoint[1] - i9;
            this.finalDeltaX = 0.0f;
            this.finalDeltaY = 0.0f;
            int i12 = i == 0 ? -1 : 1;
            if (i10 != i11 || i10 != 0) {
                if (i11 == 0) {
                    this.finalDeltaX = (-i12) * Math.signum(i10) * CellLayout.this.mReorderPreviewAnimationMagnitude;
                } else if (i10 == 0) {
                    this.finalDeltaY = (-i12) * Math.signum(i11) * CellLayout.this.mReorderPreviewAnimationMagnitude;
                } else {
                    float f = i11;
                    float f2 = i10;
                    double dAtan = Math.atan(f / f2);
                    float f3 = -i12;
                    double dSignum = Math.signum(f2) * f3;
                    double dCos = Math.cos(dAtan);
                    double d = CellLayout.this.mReorderPreviewAnimationMagnitude;
                    Double.isNaN(d);
                    double dAbs = Math.abs(dCos * d);
                    Double.isNaN(dSignum);
                    this.finalDeltaX = (int) (dSignum * dAbs);
                    double dSignum2 = f3 * Math.signum(f);
                    double dSin = Math.sin(dAtan);
                    double d2 = CellLayout.this.mReorderPreviewAnimationMagnitude;
                    Double.isNaN(d2);
                    double dAbs2 = Math.abs(dSin * d2);
                    Double.isNaN(dSignum2);
                    this.finalDeltaY = (int) (dSignum2 * dAbs2);
                }
            }
            this.mode = i;
            this.initDeltaX = view.getTranslationX();
            this.initDeltaY = view.getTranslationY();
            this.finalScale = 0.97f;
            this.initScale = view.getScaleX();
            this.child = view;
        }

        private void cancel() {
            Animator animator = this.a;
            if (animator != null) {
                animator.cancel();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void completeAnimationImmediately() {
            Animator animator = this.a;
            if (animator != null) {
                animator.cancel();
            }
            AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
            this.a = animatorSetCreateAnimatorSet;
            animatorSetCreateAnimatorSet.playTogether(LauncherAnimUtils.ofFloat(this.child, "scaleX", 1.0f), LauncherAnimUtils.ofFloat(this.child, "scaleY", 1.0f), LauncherAnimUtils.ofFloat(this.child, "translationX", 0.0f), LauncherAnimUtils.ofFloat(this.child, "translationY", 0.0f));
            animatorSetCreateAnimatorSet.setDuration(150L);
            animatorSetCreateAnimatorSet.setInterpolator(new DecelerateInterpolator(1.5f));
            animatorSetCreateAnimatorSet.start();
        }

        public void lambda$animate$0(ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            float f = (this.mode == 0 && this.repeating) ? 1.0f : fFloatValue;
            float f2 = 1.0f - f;
            float f3 = (this.finalDeltaX * f) + (this.initDeltaX * f2);
            float f4 = (f * this.finalDeltaY) + (f2 * this.initDeltaY);
            this.child.setTranslationX(f3);
            this.child.setTranslationY(f4);
            float f5 = (this.finalScale * fFloatValue) + ((1.0f - fFloatValue) * this.initScale);
            this.child.setScaleX(f5);
            this.child.setScaleY(f5);
        }

        void animate() {
            if (CellLayout.this.mShakeAnimators.containsKey(this.child)) {
                ((ReorderPreviewAnimation) CellLayout.this.mShakeAnimators.get(this.child)).cancel();
                CellLayout.this.mShakeAnimators.remove(this.child);
                if (this.finalDeltaX == 0.0f && this.finalDeltaY == 0.0f) {
                    completeAnimationImmediately();
                    return;
                }
            }
            if (this.finalDeltaX == 0.0f && this.finalDeltaY == 0.0f) {
                return;
            }
            ValueAnimator valueAnimatorOfFloat = LauncherAnimUtils.ofFloat(this.child, 0.0f, 1.0f);
            this.a = valueAnimatorOfFloat;
            valueAnimatorOfFloat.setRepeatMode(2);
            valueAnimatorOfFloat.setRepeatCount(-1);
            valueAnimatorOfFloat.setDuration(this.mode == 0 ? 350L : 300L);
            valueAnimatorOfFloat.setStartDelay((int) (Math.random() * 60.0d));
            valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$CellLayout$ReorderPreviewAnimation$u5pvzT5ebbfvDaO046UyHdWha3Y
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ReorderPreviewAnimation.this.lambda$animate$0( valueAnimator);
                }
            });
            valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.CellLayout.ReorderPreviewAnimation.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                    ReorderPreviewAnimation reorderPreviewAnimation = ReorderPreviewAnimation.this;
                    reorderPreviewAnimation.initDeltaX = 0.0f;
                    reorderPreviewAnimation.initDeltaY = 0.0f;
                    reorderPreviewAnimation.initScale = 1.0f;
                    reorderPreviewAnimation.repeating = true;
                }
            });
            CellLayout.this.mShakeAnimators.put(this.child, this);
            valueAnimatorOfFloat.start();
        }
    }

    private class ViewCluster {
        int[] bottomEdge;
        boolean bottomEdgeDirty;
        boolean boundingRectDirty;
        ItemConfiguration config;
        int[] leftEdge;
        boolean leftEdgeDirty;
        int[] rightEdge;
        boolean rightEdgeDirty;
        int[] topEdge;
        boolean topEdgeDirty;
        ArrayList<View> views;
        Rect boundingRect = new Rect();
        PositionComparator comparator = new PositionComparator();

        class PositionComparator implements Comparator<View> {
            int whichEdge = 0;

            PositionComparator() {
            }

            @Override // java.util.Comparator
            public int compare(View view, View view2) {
                CellAndSpan cellAndSpan = ViewCluster.this.config.map.get(view);
                CellAndSpan cellAndSpan2 = ViewCluster.this.config.map.get(view2);
                switch (this.whichEdge) {
                    case 0:
                        return (cellAndSpan2.x + cellAndSpan2.spanX) - (cellAndSpan.x + cellAndSpan.spanX);
                    case 1:
                        return (cellAndSpan2.y + cellAndSpan2.spanY) - (cellAndSpan.y + cellAndSpan.spanY);
                    case 2:
                        return cellAndSpan.x - cellAndSpan2.x;
                    default:
                        return cellAndSpan.y - cellAndSpan2.y;
                }
            }
        }

        public ViewCluster(ArrayList<View> arrayList, ItemConfiguration itemConfiguration) {
            this.leftEdge = new int[CellLayout.this.mCountY];
            this.rightEdge = new int[CellLayout.this.mCountY];
            this.topEdge = new int[CellLayout.this.mCountX];
            this.bottomEdge = new int[CellLayout.this.mCountX];
            this.views = (ArrayList) arrayList.clone();
            this.config = itemConfiguration;
            resetEdges();
        }

        public void addView(View view) {
            this.views.add(view);
            resetEdges();
        }

        void computeEdge(int i, int[] iArr) {
            int size = this.views.size();
            for (int i2 = 0; i2 < size; i2++) {
                CellAndSpan cellAndSpan = this.config.map.get(this.views.get(i2));
                switch (i) {
                    case 0:
                        int i3 = cellAndSpan.x;
                        for (int i4 = cellAndSpan.y; i4 < cellAndSpan.y + cellAndSpan.spanY; i4++) {
                            if (i3 < iArr[i4] || iArr[i4] < 0) {
                                iArr[i4] = i3;
                            }
                        }
                        break;
                    case 1:
                        int i5 = cellAndSpan.y;
                        for (int i6 = cellAndSpan.x; i6 < cellAndSpan.x + cellAndSpan.spanX; i6++) {
                            if (i5 < iArr[i6] || iArr[i6] < 0) {
                                iArr[i6] = i5;
                            }
                        }
                        break;
                    case 2:
                        int i7 = cellAndSpan.x + cellAndSpan.spanX;
                        for (int i8 = cellAndSpan.y; i8 < cellAndSpan.y + cellAndSpan.spanY; i8++) {
                            if (i7 > iArr[i8]) {
                                iArr[i8] = i7;
                            }
                        }
                        break;
                    case 3:
                        int i9 = cellAndSpan.y + cellAndSpan.spanY;
                        for (int i10 = cellAndSpan.x; i10 < cellAndSpan.x + cellAndSpan.spanX; i10++) {
                            if (i9 > iArr[i10]) {
                                iArr[i10] = i9;
                            }
                        }
                        break;
                }
            }
        }

        public int[] getBottomEdge() {
            if (this.bottomEdgeDirty) {
                computeEdge(3, this.bottomEdge);
            }
            return this.bottomEdge;
        }

        public Rect getBoundingRect() {
            if (this.boundingRectDirty) {
                boolean z = true;
                Iterator<View> it = this.views.iterator();
                while (it.hasNext()) {
                    CellAndSpan cellAndSpan = this.config.map.get(it.next());
                    if (z) {
                        this.boundingRect.set(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
                        z = false;
                    } else {
                        this.boundingRect.union(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
                    }
                }
            }
            return this.boundingRect;
        }

        public int[] getEdge(int i) {
            switch (i) {
                case 0:
                    return getLeftEdge();
                case 1:
                    return getTopEdge();
                case 2:
                    return getRightEdge();
                default:
                    return getBottomEdge();
            }
        }

        public int[] getLeftEdge() {
            if (this.leftEdgeDirty) {
                computeEdge(0, this.leftEdge);
            }
            return this.leftEdge;
        }

        public int[] getRightEdge() {
            if (this.rightEdgeDirty) {
                computeEdge(2, this.rightEdge);
            }
            return this.rightEdge;
        }

        public int[] getTopEdge() {
            if (this.topEdgeDirty) {
                computeEdge(1, this.topEdge);
            }
            return this.topEdge;
        }

        boolean isViewTouchingEdge(View view, int i) {
            CellAndSpan cellAndSpan = this.config.map.get(view);
            int[] edge = getEdge(i);
            switch (i) {
                case 0:
                    for (int i2 = cellAndSpan.y; i2 < cellAndSpan.y + cellAndSpan.spanY; i2++) {
                        if (edge[i2] == cellAndSpan.x + cellAndSpan.spanX) {
                            return true;
                        }
                    }
                    return false;
                case 1:
                    for (int i3 = cellAndSpan.x; i3 < cellAndSpan.x + cellAndSpan.spanX; i3++) {
                        if (edge[i3] == cellAndSpan.y + cellAndSpan.spanY) {
                            return true;
                        }
                    }
                    return false;
                case 2:
                    for (int i4 = cellAndSpan.y; i4 < cellAndSpan.y + cellAndSpan.spanY; i4++) {
                        if (edge[i4] == cellAndSpan.x) {
                            return true;
                        }
                    }
                    return false;
                case 3:
                    for (int i5 = cellAndSpan.x; i5 < cellAndSpan.x + cellAndSpan.spanX; i5++) {
                        if (edge[i5] == cellAndSpan.y) {
                            return true;
                        }
                    }
                    return false;
                default:
                    return false;
            }
        }

        void resetEdges() {
            for (int i = 0; i < CellLayout.this.mCountX; i++) {
                this.topEdge[i] = -1;
                this.bottomEdge[i] = -1;
            }
            for (int i2 = 0; i2 < CellLayout.this.mCountY; i2++) {
                this.leftEdge[i2] = -1;
                this.rightEdge[i2] = -1;
            }
            this.leftEdgeDirty = true;
            this.rightEdgeDirty = true;
            this.bottomEdgeDirty = true;
            this.topEdgeDirty = true;
            this.boundingRectDirty = true;
        }

        void shift(int i, int i2) {
            Iterator<View> it = this.views.iterator();
            while (it.hasNext()) {
                CellAndSpan cellAndSpan = this.config.map.get(it.next());
                switch (i) {
                    case 0:
                        cellAndSpan.x -= i2;
                        break;
                    case 1:
                        cellAndSpan.y -= i2;
                        break;
                    case 2:
                        cellAndSpan.x += i2;
                        break;
                    default:
                        cellAndSpan.y += i2;
                        break;
                }
            }
            resetEdges();
        }

        public void sortConfigurationForEdgePush(int i) {
            this.comparator.whichEdge = i;
            Collections.sort(this.config.sortedViews, this.comparator);
        }
    }

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CellLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDropPending = false;
        this.mIsDragTarget = true;
        this.mTmpXY = new int[2];
        this.mTmpPoint = new int[2];
        this.mTempLocation = new int[2];
        this.mLastDownOnOccupiedCell = false;
        this.mFolderOuterRings = new ArrayList<>();
        this.mFolderLeaveBehindCell = new int[]{-1, -1};
        this.FOREGROUND_ALPHA_DAMPER = 0.65f;
        this.mForegroundAlpha = 0;
        this.mBackgroundAlphaMultiplier = 1.0f;
        this.mDrawBackground = true;
        this.mColorTint = 0;
        this.mFixedWidth = -1;
        this.mFixedHeight = -1;
        this.mIsDragOverlapping = false;
        this.mUseActiveGlowBackground = false;
        this.mDragOutlines = new Rect[4];
        Rect[] rectArr = this.mDragOutlines;
        this.mDragOutlineAlphas = new float[rectArr.length];
        this.mDragOutlineAnims = new InterruptibleInOutAnimator[rectArr.length];
        this.mDragOutlineCurrent = 0;
        this.mDragOutlinePaint = new Paint();
        this.mReorderAnimators = new HashMap<>();
        this.mShakeAnimators = new HashMap<>();
        this.mItemPlacementDirty = false;
        this.mDragCell = new int[2];
        this.mDragging = false;
        this.mIntersectingViews = new ArrayList<>();
        this.mOccupiedRect = new Rect();
        this.mDirectionVector = new int[2];
        this.mPreviousReorderDirection = new int[2];
        this.mTempRect = new Rect();
        this.mTempRectStack = new Stack<>();
        setWillNotDraw(false);
        setClipToPadding(false);
        if (context instanceof Launcher) {
            this.mLauncher = (Launcher) context;
        }
        this.mDeviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        this.mCellParams = this.mDeviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CellLayout, i, 0);
        this.mCellHeight = -1;
        this.mCellWidth = -1;
        this.mFixedCellHeight = -1;
        this.mFixedCellWidth = -1;
        this.mOriginalWidthGap = 0;
        this.mWidthGap = 0;
        this.mOriginalHeightGap = 0;
        this.mHeightGap = 0;
        this.mMaxGap = Integer.MAX_VALUE;
        this.mCountX = (int) this.mCellParams.numColumns;
        this.mCountY = (int) this.mCellParams.numRows;
        this.mOccupied = (boolean[][]) Array.newInstance((Class<?>) boolean.class, this.mCountX, this.mCountY);
        this.mTmpOccupied = (boolean[][]) Array.newInstance((Class<?>) boolean.class, this.mCountX, this.mCountY);
        int[] iArr = this.mPreviousReorderDirection;
        iArr[0] = -100;
        iArr[1] = -100;
        typedArrayObtainStyledAttributes.recycle();
        setAlwaysDrawnWithCacheEnabled(false);
        Resources resources = getResources();
        this.mNormalBackground = (NinePatchDrawable) resources.getDrawable(R.drawable.screenpanel);
        this.mActiveGlowBackground = (NinePatchDrawable) resources.getDrawable(R.drawable.screenpanel_hover);
        this.mForegroundPadding = resources.getDimensionPixelSize(R.dimen.workspace_overscroll_drawable_padding);
        this.mReorderPreviewAnimationMagnitude = this.mCellParams.iconSizePx * 0.12f;
        this.mNormalBackground.setFilterBitmap(true);
        this.mActiveGlowBackground.setFilterBitmap(true);
        this.mEaseOutInterpolator = new DecelerateInterpolator(2.5f);
        int[] iArr2 = this.mDragCell;
        iArr2[1] = -1;
        iArr2[0] = -1;
        int i2 = 0;
        while (true) {
            Rect[] rectArr2 = this.mDragOutlines;
            if (i2 >= rectArr2.length) {
                break;
            }
            rectArr2[i2] = new Rect(-1, -1, -1, -1);
            i2++;
        }
        this.mDragOutlinePaint.setColor(getResources().getColor(R.color.outline_color));
        int integer = resources.getInteger(R.integer.config_dragOutlineFadeTime);
        float integer2 = resources.getInteger(R.integer.config_dragOutlineMaxAlpha);
        Arrays.fill(this.mDragOutlineAlphas, 0.0f);
        /* REWORK: jadx made this loop var final while an anon captures its value;
         * split into a while loop with an effectively-final copy. */
        int i3 = 0;
        while (i3 < this.mDragOutlineAnims.length) {
            final int i3cap = i3;
            final InterruptibleInOutAnimator interruptibleInOutAnimator = new InterruptibleInOutAnimator(this, integer, 0.0f, integer2);
            interruptibleInOutAnimator.getAnimator().setInterpolator(this.mEaseOutInterpolator);
            interruptibleInOutAnimator.getAnimator().addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$CellLayout$CutH1Cj23M4YjeX05w_id7bYol0
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    CellLayout.this.lambda$new$0( interruptibleInOutAnimator, i3cap, valueAnimator);
                }
            });
            interruptibleInOutAnimator.getAnimator().addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.CellLayout.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (((Float) ((ValueAnimator) animator).getAnimatedValue()).floatValue() == 0.0f) {
                        interruptibleInOutAnimator.setTag(null);
                    }
                }
            });
            this.mDragOutlineAnims[i3] = interruptibleInOutAnimator;
            i3++;
        }
        this.mBackgroundRect = new Rect();
        this.mForegroundRect = new Rect();
        this.mShortcutsAndWidgets = new ShortcutAndWidgetContainer(context);
        this.mShortcutsAndWidgets.setDeviceProfile(this.mDeviceProfile);
        this.mShortcutsAndWidgets.setCellParams(this.mCellParams);
        this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
        this.mTouchFeedbackView = new FastBitmapView(context);
        FastBitmapView fastBitmapView = this.mTouchFeedbackView;
        double d = this.mCellParams.cellWidthPx;
        Double.isNaN(d);
        double d2 = this.mCellParams.cellHeightPx;
        Double.isNaN(d2);
        addView(fastBitmapView, (int) (d * 1.5d), (int) (d2 * 1.5d));
        addView(this.mShortcutsAndWidgets);
    }

    private boolean addViewToTempLocation(View view, Rect rect, int[] iArr, ItemConfiguration itemConfiguration) {
        CellAndSpan cellAndSpan = itemConfiguration.map.get(view);
        markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, false);
        markCellsForRect(rect, this.mTmpOccupied, true);
        findNearestArea(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, iArr, this.mTmpOccupied, (boolean[][]) null, this.mTempLocation);
        int[] iArr2 = this.mTempLocation;
        boolean z = false;
        if (iArr2[0] >= 0 && iArr2[1] >= 0) {
            cellAndSpan.x = iArr2[0];
            cellAndSpan.y = iArr2[1];
            z = true;
        }
        markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, true);
        return z;
    }

    private boolean addViewsToTempLocation(ArrayList<View> arrayList, Rect rect, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        boolean z = true;
        if (arrayList.size() == 0) {
            return true;
        }
        Iterator<View> it = arrayList.iterator();
        Rect rect2 = null;
        while (it.hasNext()) {
            CellAndSpan cellAndSpan = itemConfiguration.map.get(it.next());
            if (rect2 == null) {
                rect2 = new Rect(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
            } else {
                rect2.union(cellAndSpan.x, cellAndSpan.y, cellAndSpan.x + cellAndSpan.spanX, cellAndSpan.y + cellAndSpan.spanY);
            }
        }
        Iterator<View> it2 = arrayList.iterator();
        while (it2.hasNext()) {
            CellAndSpan cellAndSpan2 = itemConfiguration.map.get(it2.next());
            markCellsForView(cellAndSpan2.x, cellAndSpan2.y, cellAndSpan2.spanX, cellAndSpan2.spanY, this.mTmpOccupied, false);
        }
        boolean[][] zArr = (boolean[][]) Array.newInstance((Class<?>) boolean.class, rect2.width(), rect2.height());
        int i = rect2.top;
        int i2 = rect2.left;
        Iterator<View> it3 = arrayList.iterator();
        while (it3.hasNext()) {
            CellAndSpan cellAndSpan3 = itemConfiguration.map.get(it3.next());
            markCellsForView(cellAndSpan3.x - i2, cellAndSpan3.y - i, cellAndSpan3.spanX, cellAndSpan3.spanY, zArr, true);
        }
        markCellsForRect(rect, this.mTmpOccupied, true);
        findNearestArea(rect2.left, rect2.top, rect2.width(), rect2.height(), iArr, this.mTmpOccupied, zArr, this.mTempLocation);
        int[] iArr2 = this.mTempLocation;
        if (iArr2[0] < 0 || iArr2[1] < 0) {
            z = false;
        } else {
            int i3 = iArr2[0] - rect2.left;
            int i4 = this.mTempLocation[1] - rect2.top;
            Iterator<View> it4 = arrayList.iterator();
            while (it4.hasNext()) {
                CellAndSpan cellAndSpan4 = itemConfiguration.map.get(it4.next());
                cellAndSpan4.x += i3;
                cellAndSpan4.y += i4;
            }
        }
        Iterator<View> it5 = arrayList.iterator();
        while (it5.hasNext()) {
            CellAndSpan cellAndSpan5 = itemConfiguration.map.get(it5.next());
            markCellsForView(cellAndSpan5.x, cellAndSpan5.y, cellAndSpan5.spanX, cellAndSpan5.spanY, this.mTmpOccupied, true);
        }
        return z;
    }

    private void animateItemsToSolution(ItemConfiguration itemConfiguration, View view, boolean z) {
        CellAndSpan cellAndSpan;
        boolean[][] zArr = this.mTmpOccupied;
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                zArr[i][i2] = false;
            }
        }
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i3);
            if (childAt != view && (cellAndSpan = itemConfiguration.map.get(childAt)) != null) {
                animateChildToPosition(childAt, cellAndSpan.x, cellAndSpan.y, 150, 0, false, false);
                markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, zArr, true);
            }
        }
        if (z) {
            markCellsForView(itemConfiguration.dragViewX, itemConfiguration.dragViewY, itemConfiguration.dragViewSpanX, itemConfiguration.dragViewSpanY, zArr, true);
        }
    }

    private boolean attemptPushInDirection(ArrayList<View> arrayList, Rect rect, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        if (Math.abs(iArr[0]) + Math.abs(iArr[1]) > 1) {
            int i = iArr[1];
            iArr[1] = 0;
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[1] = i;
            int i2 = iArr[0];
            iArr[0] = 0;
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = i2;
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            int i3 = iArr[1];
            iArr[1] = 0;
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[1] = i3;
            int i4 = iArr[0];
            iArr[0] = 0;
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = i4;
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
        } else {
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            int i5 = iArr[1];
            iArr[1] = iArr[0];
            iArr[0] = i5;
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            if (pushViewsToTempLocation(arrayList, rect, iArr, view, itemConfiguration)) {
                return true;
            }
            iArr[0] = iArr[0] * (-1);
            iArr[1] = iArr[1] * (-1);
            int i6 = iArr[1];
            iArr[1] = iArr[0];
            iArr[0] = i6;
        }
        return false;
    }

    private void beginOrAdjustReorderPreviewAnimations(ItemConfiguration itemConfiguration, View view, int i, int i2) {
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i3);
            if (childAt != view) {
                CellAndSpan cellAndSpan = itemConfiguration.map.get(childAt);
                boolean z = (i2 != 0 || itemConfiguration.intersectingViews == null || itemConfiguration.intersectingViews.contains(childAt)) ? false : true;
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (cellAndSpan != null && !z) {
                    new ReorderPreviewAnimation(childAt, i2, layoutParams.cellX, layoutParams.cellY, cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY).animate();
                }
            }
        }
    }

    private void clearOccupiedCells() {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                this.mOccupied[i][i2] = false;
            }
        }
    }

    private void commitTempPlacement() {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                this.mOccupied[i][i2] = this.mTmpOccupied[i][i2];
            }
        }
        long idForScreen = this.mLauncher.getWorkspace().getIdForScreen(this);
        int i3 = -100;
        if (this.mLauncher.isHotseatLayout(this)) {
            idForScreen = -1;
            i3 = -101;
        }
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i4 = 0; i4 < childCount; i4++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i4);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            ItemInfo itemInfo = (ItemInfo) childAt.getTag();
            if (itemInfo != null) {
                boolean z = (itemInfo.cellX == layoutParams.tmpCellX && itemInfo.cellY == layoutParams.tmpCellY && itemInfo.spanX == layoutParams.cellHSpan && itemInfo.spanY == layoutParams.cellVSpan) ? false : true;
                int i5 = layoutParams.tmpCellX;
                layoutParams.cellX = i5;
                itemInfo.cellX = i5;
                int i6 = layoutParams.tmpCellY;
                layoutParams.cellY = i6;
                itemInfo.cellY = i6;
                itemInfo.spanX = layoutParams.cellHSpan;
                itemInfo.spanY = layoutParams.cellVSpan;
                if (z) {
                    LauncherModel.modifyItemInDatabase(this.mLauncher, itemInfo, i3, idForScreen, itemInfo.cellX, itemInfo.cellY, itemInfo.spanX, itemInfo.spanY);
                }
            }
        }
    }

    private void completeAndClearReorderPreviewAnimations() {
        Iterator<ReorderPreviewAnimation> it = this.mShakeAnimators.values().iterator();
        while (it.hasNext()) {
            it.next().completeAnimationImmediately();
        }
        this.mShakeAnimators.clear();
    }

    private void computeDirectionVector(float f, float f2, int[] iArr) {
        double dAtan = Math.atan(f2 / f);
        iArr[0] = 0;
        iArr[1] = 0;
        if (Math.abs(Math.cos(dAtan)) > 0.5d) {
            iArr[0] = (int) Math.signum(f);
        }
        if (Math.abs(Math.sin(dAtan)) > 0.5d) {
            iArr[1] = (int) Math.signum(f2);
        }
    }

    private void copyCurrentStateToSolution(ItemConfiguration itemConfiguration, boolean z) {
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            itemConfiguration.add(childAt, z ? new CellAndSpan(layoutParams.tmpCellX, layoutParams.tmpCellY, layoutParams.cellHSpan, layoutParams.cellVSpan) : new CellAndSpan(layoutParams.cellX, layoutParams.cellY, layoutParams.cellHSpan, layoutParams.cellVSpan));
        }
    }

    private void copyOccupiedArray(boolean[][] zArr) {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                zArr[i][i2] = this.mOccupied[i][i2];
            }
        }
    }

    private void copySolutionToTempState(ItemConfiguration itemConfiguration, View view) {
        for (int i = 0; i < this.mCountX; i++) {
            for (int i2 = 0; i2 < this.mCountY; i2++) {
                this.mTmpOccupied[i][i2] = false;
            }
        }
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i3);
            if (childAt != view) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                CellAndSpan cellAndSpan = itemConfiguration.map.get(childAt);
                if (cellAndSpan != null) {
                    layoutParams.tmpCellX = cellAndSpan.x;
                    layoutParams.tmpCellY = cellAndSpan.y;
                    layoutParams.cellHSpan = cellAndSpan.spanX;
                    layoutParams.cellVSpan = cellAndSpan.spanY;
                    markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, true);
                }
            }
        }
        markCellsForView(itemConfiguration.dragViewX, itemConfiguration.dragViewY, itemConfiguration.dragViewSpanX, itemConfiguration.dragViewSpanY, this.mTmpOccupied, true);
    }

    private int[] findNearestArea(int i, int i2, int i3, int i4, int[] iArr, boolean[][] zArr, boolean[][] zArr2, int[] iArr2) {
        int[] iArr3 = iArr2 != null ? iArr2 : new int[2];
        int i5 = this.mCountX;
        int i6 = this.mCountY;
        int i7 = 0;
        float f = Float.MAX_VALUE;
        int i8 = Integer.MIN_VALUE;
        while (true) {
            char c = 1;
            if (i7 >= i6 - (i4 - 1)) {
                break;
            }
            int i9 = i8;
            float f2 = f;
            int i10 = 0;
            while (i10 < i5 - (i3 - 1)) {
                int i11 = 0;
                boolean z3 = false;
                while (true) {
                    if (z3) {
                        break;
                    }
                    if (i11 >= i3) {
                        int i12 = i10 - i;
                        int i13 = i7 - i2;
                        float fSqrt = (float) Math.sqrt((i12 * i12) + (i13 * i13));
                        int[] iArr4 = this.mTmpPoint;
                        computeDirectionVector(i12, i13, iArr4);
                        int i14 = (iArr[0] * iArr4[0]) + (iArr[c] * iArr4[c]);
                        if (iArr[0] != iArr4[0] || iArr[0] == iArr4[0]) {
                        }
                        if (Float.compare(fSqrt, f2) >= 0 && (Float.compare(fSqrt, f2) != 0 || i14 <= i9)) {
                            break;
                        }
                        iArr3[0] = i10;
                        iArr3[1] = i7;
                        f2 = fSqrt;
                        i9 = i14;
                        break;
                    }
                    for (int i15 = 0; i15 < i4; i15++) {
                        if (zArr[i10 + i11][i7 + i15] && (zArr2 == null || zArr2[i11][i15])) {
                            z3 = true;
                            break;
                        }
                    }
                    i11++;
                }
                i10++;
                c = 1;
            }
            i7++;
            f = f2;
            i8 = i9;
        }
        if (f == Float.MAX_VALUE) {
            iArr3[0] = -1;
            iArr3[1] = -1;
        }
        return iArr3;
    }

    static boolean findVacantCell(int[] iArr, int i, int i2, int i3, int i4, boolean[][] zArr) {
        for (int i5 = 0; i5 < i4; i5++) {
            for (int i6 = 0; i6 < i3; i6++) {
                boolean z = !zArr[i6][i5];
                int i7 = i6;
                while (i7 < (i6 + i) - 1 && i6 < i3) {
                    boolean z2 = z;
                    for (int i8 = i5; i8 < (i5 + i2) - 1 && i5 < i4; i8++) {
                        z2 = z2 && !zArr[i7][i8];
                        if (!z2) {
                            z = z2;
                            break;
                        }
                    }
                    i7++;
                    z = z2;
                }
                if (z) {
                    iArr[0] = i6;
                    iArr[1] = i5;
                    return true;
                }
            }
        }
        return false;
    }

    private void getDirectionVectorForDrop(int i, int i2, int i3, int i4, View view, int[] iArr) {
        int[] iArr2 = new int[2];
        findNearestArea(i, i2, i3, i4, iArr2);
        Rect rect = new Rect();
        regionToRect(iArr2[0], iArr2[1], i3, i4, rect);
        rect.offset(i - rect.centerX(), i2 - rect.centerY());
        Rect rect2 = new Rect();
        getViewsIntersectingRegion(iArr2[0], iArr2[1], i3, i4, view, rect2, this.mIntersectingViews);
        int iWidth = rect2.width();
        int iHeight = rect2.height();
        regionToRect(rect2.left, rect2.top, rect2.width(), rect2.height(), rect2);
        int iCenterX = (rect2.centerX() - i) / i3;
        int iCenterY = (rect2.centerY() - i2) / i4;
        int i5 = this.mCountX;
        if (iWidth == i5 || i3 == i5) {
            iCenterX = 0;
        }
        int i6 = this.mCountY;
        if (iHeight == i6 || i4 == i6) {
            iCenterY = 0;
        }
        if (iCenterX != 0 || iCenterY != 0) {
            computeDirectionVector(iCenterX, iCenterY, iArr);
        } else {
            iArr[0] = 1;
            iArr[1] = 0;
        }
    }

    private void getViewsIntersectingRegion(int i, int i2, int i3, int i4, View view, Rect rect, ArrayList<View> arrayList) {
        if (rect != null) {
            rect.set(i, i2, i + i3, i2 + i4);
        }
        arrayList.clear();
        Rect rect2 = new Rect(i, i2, i3 + i, i4 + i2);
        Rect rect3 = new Rect();
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = this.mShortcutsAndWidgets.getChildAt(i5);
            if (childAt != view) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                rect3.set(layoutParams.cellX, layoutParams.cellY, layoutParams.cellX + layoutParams.cellHSpan, layoutParams.cellY + layoutParams.cellVSpan);
                if (Rect.intersects(rect2, rect3)) {
                    this.mIntersectingViews.add(childAt);
                    if (rect != null) {
                        rect.union(rect3);
                    }
                }
            }
        }
    }

    static /* synthetic */ void lambda$animateChildToPosition$1(LayoutParams layoutParams, int i, int i2, int i3, int i4, View view, ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        float f = 1.0f - fFloatValue;
        layoutParams.x = (int) ((i * f) + (i2 * fFloatValue));
        layoutParams.y = (int) ((f * i3) + (fFloatValue * i4));
        view.requestLayout();
    }

    public void lambda$new$0(InterruptibleInOutAnimator interruptibleInOutAnimator, int i, ValueAnimator valueAnimator) {
        if (((Bitmap) interruptibleInOutAnimator.getTag()) == null) {
            valueAnimator.cancel();
        } else {
            this.mDragOutlineAlphas[i] = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            this.invalidate(this.mDragOutlines[i]);
        }
    }

    private void lazyInitTempRectStack() {
        if (this.mTempRectStack.isEmpty()) {
            for (int i = 0; i < this.mCountX * this.mCountY; i++) {
                this.mTempRectStack.push(new Rect());
            }
        }
    }

    private void markCellsForRect(Rect rect, boolean[][] zArr, boolean z) {
        markCellsForView(rect.left, rect.top, rect.width(), rect.height(), zArr, z);
    }

    private void markCellsForView(int i, int i2, int i3, int i4, boolean[][] zArr, boolean z) {
        if (i < 0 || i2 < 0) {
            return;
        }
        for (int i5 = i; i5 < i + i3 && i5 < this.mCountX; i5++) {
            for (int i6 = i2; i6 < i2 + i4 && i6 < this.mCountY; i6++) {
                zArr[i5][i6] = z;
            }
        }
    }

    private boolean pushViewsToTempLocation(ArrayList<View> arrayList, Rect rect, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        int i;
        int i2;
        ViewCluster viewCluster = new ViewCluster(arrayList, itemConfiguration);
        Rect boundingRect = viewCluster.getBoundingRect();
        boolean z = false;
        if (iArr[0] < 0) {
            i = boundingRect.right - rect.left;
            i2 = 0;
        } else if (iArr[0] > 0) {
            i = rect.right - boundingRect.left;
            i2 = 2;
        } else if (iArr[1] < 0) {
            i = boundingRect.bottom - rect.top;
            i2 = 1;
        } else {
            i = rect.bottom - boundingRect.top;
            i2 = 3;
        }
        if (i <= 0) {
            return false;
        }
        Iterator<View> it = arrayList.iterator();
        while (it.hasNext()) {
            CellAndSpan cellAndSpan = itemConfiguration.map.get(it.next());
            markCellsForView(cellAndSpan.x, cellAndSpan.y, cellAndSpan.spanX, cellAndSpan.spanY, this.mTmpOccupied, false);
        }
        itemConfiguration.save();
        viewCluster.sortConfigurationForEdgePush(i2);
        boolean z2 = false;
        while (i > 0 && !z2) {
            for (View view2 : itemConfiguration.sortedViews) {
                if (!viewCluster.views.contains(view2) && view2 != view && viewCluster.isViewTouchingEdge(view2, i2)) {
                    if (!((LayoutParams) view2.getLayoutParams()).canReorder) {
                        z2 = true;
                        break;
                    }
                    viewCluster.addView(view2);
                    CellAndSpan cellAndSpan2 = itemConfiguration.map.get(view2);
                    markCellsForView(cellAndSpan2.x, cellAndSpan2.y, cellAndSpan2.spanX, cellAndSpan2.spanY, this.mTmpOccupied, false);
                }
            }
            i--;
            viewCluster.shift(i2, 1);
        }
        Rect boundingRect2 = viewCluster.getBoundingRect();
        if (z2 || boundingRect2.left < 0 || boundingRect2.right > this.mCountX || boundingRect2.top < 0 || boundingRect2.bottom > this.mCountY) {
            itemConfiguration.restore();
        } else {
            z = true;
        }
        Iterator<View> it2 = viewCluster.views.iterator();
        while (it2.hasNext()) {
            CellAndSpan cellAndSpan3 = itemConfiguration.map.get(it2.next());
            markCellsForView(cellAndSpan3.x, cellAndSpan3.y, cellAndSpan3.spanX, cellAndSpan3.spanY, this.mTmpOccupied, true);
        }
        return z;
    }

    private boolean rearrangementExists(int i, int i2, int i3, int i4, int[] iArr, View view, ItemConfiguration itemConfiguration) {
        CellAndSpan cellAndSpan;
        if (i < 0 || i2 < 0) {
            return false;
        }
        this.mIntersectingViews.clear();
        int i5 = i3 + i;
        int i6 = i4 + i2;
        this.mOccupiedRect.set(i, i2, i5, i6);
        if (view != null && (cellAndSpan = itemConfiguration.map.get(view)) != null) {
            cellAndSpan.x = i;
            cellAndSpan.y = i2;
        }
        Rect rect = new Rect(i, i2, i5, i6);
        Rect rect2 = new Rect();
        for (View view2 : itemConfiguration.map.keySet()) {
            if (view2 != view) {
                CellAndSpan cellAndSpan2 = itemConfiguration.map.get(view2);
                LayoutParams layoutParams = (LayoutParams) view2.getLayoutParams();
                rect2.set(cellAndSpan2.x, cellAndSpan2.y, cellAndSpan2.x + cellAndSpan2.spanX, cellAndSpan2.y + cellAndSpan2.spanY);
                if (!Rect.intersects(rect, rect2)) {
                    continue;
                } else {
                    if (!layoutParams.canReorder) {
                        return false;
                    }
                    this.mIntersectingViews.add(view2);
                }
            }
        }
        itemConfiguration.intersectingViews = new ArrayList<>(this.mIntersectingViews);
        if (attemptPushInDirection(this.mIntersectingViews, this.mOccupiedRect, iArr, view, itemConfiguration) || addViewsToTempLocation(this.mIntersectingViews, this.mOccupiedRect, iArr, view, itemConfiguration)) {
            return true;
        }
        Iterator<View> it = this.mIntersectingViews.iterator();
        while (it.hasNext()) {
            if (!addViewToTempLocation(it.next(), this.mOccupiedRect, iArr, itemConfiguration)) {
                return false;
            }
        }
        return true;
    }

    public static int[] rectToCell(int i, int i2, int[] iArr) {
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        CellParams cellParamsForType = deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
        Rect workspacePadding = deviceProfile.getWorkspacePadding(!deviceProfile.isLandscape ? 1 : 0);
        float fMin = Math.min(deviceProfile.calculateCellWidth((deviceProfile.widthPx - workspacePadding.left) - workspacePadding.right, (int) cellParamsForType.numColumns), deviceProfile.calculateCellHeight((deviceProfile.heightPx - workspacePadding.top) - workspacePadding.bottom, (int) cellParamsForType.numRows));
        int iCeil = (int) Math.ceil(i / fMin);
        int iCeil2 = (int) Math.ceil(i2 / fMin);
        if (iArr == null) {
            return new int[]{iCeil, iCeil2};
        }
        iArr[0] = iCeil;
        iArr[1] = iCeil2;
        return iArr;
    }

    private void recycleTempRects(Stack<Rect> stack) {
        while (!stack.isEmpty()) {
            this.mTempRectStack.push(stack.pop());
        }
    }

    public boolean addViewToCellLayout(View view, int i, int i2, LayoutParams layoutParams, boolean z) {
        if (view instanceof BubbleTextView) {
            ((BubbleTextView) view).setCellParams(this.mCellParams);
        } else if (view instanceof FolderIcon) {
            ((FolderIcon) view).setCellParams(this.mCellParams);
        }
        if (layoutParams.cellX < 0 || layoutParams.cellX > this.mCountX - 1 || layoutParams.cellY < 0 || layoutParams.cellY > this.mCountY - 1) {
            return false;
        }
        if (layoutParams.cellHSpan < 0) {
            layoutParams.cellHSpan = this.mCountX;
        }
        if (layoutParams.cellVSpan < 0) {
            layoutParams.cellVSpan = this.mCountY;
        }
        view.setId(i2);
        this.mShortcutsAndWidgets.addView(view, i, layoutParams);
        if (z) {
            markCellsAsOccupiedForView(view);
        }
        return true;
    }

    public boolean animateChildToPosition(final View view, int i, int i2, int i3, int i4, boolean z, boolean z2) {
        ShortcutAndWidgetContainer shortcutsAndWidgets = getShortcutsAndWidgets();
        boolean[][] zArr = this.mOccupied;
        if (!z) {
            zArr = this.mTmpOccupied;
        }
        if (shortcutsAndWidgets.indexOfChild(view) == -1) {
            return false;
        }
        final LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        ItemInfo itemInfo = (ItemInfo) view.getTag();
        if (this.mReorderAnimators.containsKey(layoutParams)) {
            this.mReorderAnimators.get(layoutParams).cancel();
            this.mReorderAnimators.remove(layoutParams);
        }
        final int i5 = layoutParams.x;
        final int i6 = layoutParams.y;
        if (z2) {
            zArr[layoutParams.cellX][layoutParams.cellY] = false;
            zArr[i][i2] = true;
        }
        layoutParams.isLockedToGrid = true;
        if (z) {
            itemInfo.cellX = i;
            layoutParams.cellX = i;
            itemInfo.cellY = i2;
            layoutParams.cellY = i2;
        } else {
            layoutParams.tmpCellX = i;
            layoutParams.tmpCellY = i2;
        }
        shortcutsAndWidgets.setupLp(layoutParams);
        layoutParams.isLockedToGrid = false;
        final int i7 = layoutParams.x;
        final int i8 = layoutParams.y;
        layoutParams.x = i5;
        layoutParams.y = i6;
        if (i5 == i7 && i6 == i8) {
            layoutParams.isLockedToGrid = true;
            return true;
        }
        ValueAnimator valueAnimatorOfFloat = LauncherAnimUtils.ofFloat(view, 0.0f, 1.0f);
        valueAnimatorOfFloat.setDuration(i3);
        this.mReorderAnimators.put(layoutParams, valueAnimatorOfFloat);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$CellLayout$hnHdFOaBIEV6irgG0Wg60QrFdvA
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                CellLayout.lambda$animateChildToPosition$1(layoutParams, i5, i7, i6, i8, view, valueAnimator);
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.CellLayout.2
            boolean cancelled = false;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!this.cancelled) {
                    layoutParams.isLockedToGrid = true;
                    view.requestLayout();
                }
                CellLayout.this.mReorderAnimators.remove(layoutParams);
            }
        });
        valueAnimatorOfFloat.setStartDelay(i4);
        valueAnimatorOfFloat.start();
        return true;
    }

    public void buildHardwareLayer() {
        this.mShortcutsAndWidgets.buildLayer();
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).cancelLongPress();
        }
    }

    void cellToCenterPoint(int i, int i2, int[] iArr) {
        regionToCenterPoint(i, i2, 1, 1, iArr);
    }

    void cellToPoint(int i, int i2, int[] iArr) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        iArr[0] = paddingLeft + (i * (this.mCellWidth + this.mWidthGap));
        iArr[1] = paddingTop + (i2 * (this.mCellHeight + this.mHeightGap));
    }

    public void cellToRect(int i, int i2, int i3, int i4, Rect rect) {
        int i5 = this.mCellWidth;
        int i6 = this.mCellHeight;
        int i7 = this.mWidthGap;
        int i8 = this.mHeightGap;
        int paddingLeft = getPaddingLeft() + (i * (i5 + i7));
        int paddingTop = getPaddingTop() + (i2 * (i6 + i8));
        rect.set(paddingLeft, paddingTop, (i3 * i5) + ((i3 - 1) * i7) + paddingLeft, (i4 * i6) + ((i4 - 1) * i8) + paddingTop);
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    public void clearDragOutlines() {
        this.mDragOutlineAnims[this.mDragOutlineCurrent].animateOut();
        int[] iArr = this.mDragCell;
        iArr[1] = -1;
        iArr[0] = -1;
    }

    boolean createAreaForResize(int i, int i2, int i3, int i4, View view, int[] iArr, boolean z) {
        int[] iArr2 = new int[2];
        regionToCenterPoint(i, i2, i3, i4, iArr2);
        ItemConfiguration itemConfigurationFindReorderSolution = findReorderSolution(iArr2[0], iArr2[1], i3, i4, i3, i4, iArr, view, true, new ItemConfiguration());
        setUseTempCoords(true);
        if (itemConfigurationFindReorderSolution != null && itemConfigurationFindReorderSolution.isSolution) {
            copySolutionToTempState(itemConfigurationFindReorderSolution, view);
            setItemPlacementDirty(true);
            animateItemsToSolution(itemConfigurationFindReorderSolution, view, z);
            if (z) {
                commitTempPlacement();
                completeAndClearReorderPreviewAnimations();
                setItemPlacementDirty(false);
            } else {
                beginOrAdjustReorderPreviewAnimations(itemConfigurationFindReorderSolution, view, 150, 1);
            }
            this.mShortcutsAndWidgets.requestLayout();
        }
        return itemConfigurationFindReorderSolution.isSolution;
    }

    void disableBackground() {
        this.mDrawBackground = false;
    }

    void disableDragTarget() {
        this.mIsDragTarget = false;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void enableHardwareLayer(boolean z) {
        this.mShortcutsAndWidgets.setLayerType(z ? 2 : 0, sPaint);
    }

    boolean findCellForSpan(int[] iArr, int i, int i2) {
        return findCellForSpanThatIntersectsIgnoring(iArr, i, i2, -1, -1, null, this.mOccupied);
    }

    boolean findCellForSpanThatIntersectsIgnoring(int[] iArr, int i, int i2, int i3, int i4, View view, boolean[][] zArr) {
        /* REWORK: jadx left this register blank; findNearestArea starts the scan cursor at 0. */
        int i5 = 0;
        markCellsAsUnoccupiedForView(view, zArr);
        int i6 = i3;
        int i7 = i4;
        boolean z = false;
        while (true) {
            int iMax = i6 >= 0 ? Math.max(0, i6 - (i - 1)) : 0;
            int i8 = i - 1;
            int iMin = this.mCountX - i8;
            if (i6 >= 0) {
                iMin = Math.min(iMin, i8 + i6 + (i == 1 ? 1 : 0));
            }
            int i9 = i2 - 1;
            int iMin2 = this.mCountY - i9;
            if (i7 >= 0) {
                iMin2 = Math.min(iMin2, i9 + i7 + (i2 == 1 ? 1 : 0));
            }
            for (int iMax2 = i7 >= 0 ? Math.max(0, i7 - (i2 - 1)) : 0; iMax2 < iMin2 && !z; iMax2++) {
                int i10 = iMax;
                while (i10 < iMin) {
                    int i11 = 0;
                    while (true) {
                        if (i11 >= i) {
                            if (iArr != null) {
                                iArr[0] = i10;
                                iArr[1] = iMax2;
                            }
                            z = true;
                            break;
                        }
                        int i12 = 0;
                        while (true) {
                            if (i12 < i2) {
                                i5 = i10 + i11;
                                if (zArr[i5][iMax2 + i12]) {
                                    break;
                                }
                                i12++;
                            } else {
                                i11++;
                            }
                        }
                    }
                    i10 = i5 + 1;
                }
            }
            if (i6 == -1 && i7 == -1) {
                markCellsAsOccupiedForView(view, zArr);
                return z;
            }
            i6 = -1;
            i7 = -1;
        }
    }

    ItemConfiguration findConfigurationNoShuffle(int i, int i2, int i3, int i4, int i5, int i6, View view, ItemConfiguration itemConfiguration) {
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        findNearestVacantArea(i, i2, i3, i4, i5, i6, null, iArr, iArr2);
        if (iArr[0] < 0 || iArr[1] < 0) {
            itemConfiguration.isSolution = false;
        } else {
            copyCurrentStateToSolution(itemConfiguration, false);
            itemConfiguration.dragViewX = iArr[0];
            itemConfiguration.dragViewY = iArr[1];
            itemConfiguration.dragViewSpanX = iArr2[0];
            itemConfiguration.dragViewSpanY = iArr2[1];
            itemConfiguration.isSolution = true;
        }
        return itemConfiguration;
    }

        /* REWORK: body restored from the upstream Launcher2 implementation (the dex is
               literally this algorithm; jadx had flattened the occupied-skip and expansion
               edges into a form that spun forever on a drop — the findNearestArea ANR). */
    int[] findNearestArea(int i, int i2, int i3, int i4, int i5, int i6, View view, boolean z, int[] iArr, int[] iArr2, boolean[][] zArr) {
        lazyInitTempRectStack();
        // mark space take by ignoreView as available (method checks if ignoreView is null)
        markCellsAsUnoccupiedForView(view, zArr);

        // For items with a spanX / spanY > 1, the passed in point (pixelX, pixelY) corresponds
        // to the center of the item, but we are searching based on the top-left cell, so
        // we translate the point over to correspond to the top-left.
        i -= (int) ((this.mCellWidth + this.mWidthGap) * (i3 - 1) / 2.0f);
        i2 -= (int) ((this.mCellHeight + this.mHeightGap) * (i4 - 1) / 2.0f);

        // Keep track of best-scoring drop area
        int[] bestXY = iArr != null ? iArr : new int[2];
        double bestDistance = Double.MAX_VALUE;
        Rect bestRect = new Rect(-1, -1, -1, -1);
        Stack<Rect> validRegions = new Stack<>();

        int countX = this.mCountX;
        int countY = this.mCountY;

        if (i3 <= 0 || i4 <= 0 || i5 <= 0 || i6 <= 0 || i5 < i3 || i6 < i4) {
            return bestXY;
        }

        for (int y = 0; y < countY - (i4 - 1); y++) {
            inner:
            for (int x = 0; x < countX - (i3 - 1); x++) {
                int ySize = -1;
                int xSize = -1;
                if (z) {
                    // First, let's see if this thing fits anywhere
                    for (int i8 = 0; i8 < i3; i8++) {
                        for (int i9 = 0; i9 < i4; i9++) {
                            if (zArr[x + i8][y + i9]) {
                                continue inner;
                            }
                        }
                    }
                    xSize = i3;
                    ySize = i4;

                    // We know that the item will fit at _some_ acceptable size, now let's see
                    // how big we can make it. We'll alternate between incrementing x and y spans
                    // until we hit a limit.
                    boolean incX = true;
                    boolean hitMaxX = xSize >= i5;
                    boolean hitMaxY = ySize >= i6;
                    while (!(hitMaxX && hitMaxY)) {
                        if (incX && !hitMaxX) {
                            for (int j = 0; j < ySize; j++) {
                                if (x + xSize > countX -1 || zArr[x + xSize][y + j]) {
                                    // We can't move out horizontally
                                    hitMaxX = true;
                                }
                            }
                            if (!hitMaxX) {
                                xSize++;
                            }
                        } else if (!hitMaxY) {
                            for (int i8 = 0; i8 < xSize; i8++) {
                                if (y + ySize > countY - 1 || zArr[x + i8][y + ySize]) {
                                    // We can't move out vertically
                                    hitMaxY = true;
                                }
                            }
                            if (!hitMaxY) {
                                ySize++;
                            }
                        }
                        hitMaxX |= xSize >= i5;
                        hitMaxY |= ySize >= i6;
                        incX = !incX;
                    }
                }
                int[] cellXY = this.mTmpXY;
                cellToCenterPoint(x, y, cellXY);

                // We verify that the current rect is not a sub-rect of any of our previous
                // candidates. In this case, the current rect is disqualified in favour of the
                // containing rect.
                Rect currentRect = this.mTempRectStack.pop();
                currentRect.set(x, y, x + xSize, y + ySize);
                boolean contained = false;
                for (Rect r : validRegions) {
                    if (r.contains(currentRect)) {
                        contained = true;
                        break;
                    }
                }
                validRegions.push(currentRect);
                double distance = Math.sqrt(Math.pow(cellXY[0] - i, 2)
                        + Math.pow(cellXY[1] - i2, 2));

                if ((distance <= bestDistance && !contained) ||
                        currentRect.contains(bestRect)) {
                    bestDistance = distance;
                    bestXY[0] = x;
                    bestXY[1] = y;
                    if (iArr2 != null) {
                        iArr2[0] = xSize;
                        iArr2[1] = ySize;
                    }
                    bestRect.set(currentRect);
                }
            }
        }
        // re-mark space taken by ignoreView as occupied
        markCellsAsOccupiedForView(view, zArr);

        // Return -1, -1 if no suitable location found
        if (bestDistance == Double.MAX_VALUE) {
            bestXY[0] = -1;
            bestXY[1] = -1;
        }
        recycleTempRects(validRegions);
        return bestXY;
    }


    int[] findNearestArea(int i, int i2, int i3, int i4, View view, boolean z, int[] iArr) {
        return findNearestArea(i, i2, i3, i4, i3, i4, view, z, iArr, null, this.mOccupied);
    }

    int[] findNearestArea(int i, int i2, int i3, int i4, int[] iArr) {
        return findNearestArea(i, i2, i3, i4, null, false, iArr);
    }

    int[] findNearestVacantArea(int i, int i2, int i3, int i4, int i5, int i6, View view, int[] iArr, int[] iArr2) {
        return findNearestArea(i, i2, i3, i4, i5, i6, view, true, iArr, iArr2, this.mOccupied);
    }

    int[] findNearestVacantArea(int i, int i2, int i3, int i4, int i5, int i6, int[] iArr, int[] iArr2) {
        return findNearestVacantArea(i, i2, i3, i4, i5, i6, null, iArr, iArr2);
    }

    int[] findNearestVacantArea(int i, int i2, int i3, int i4, View view, int[] iArr) {
        return findNearestArea(i, i2, i3, i4, view, true, iArr);
    }

    int[] findNearestVacantArea(int i, int i2, int i3, int i4, int[] iArr) {
        return findNearestVacantArea(i, i2, i3, i4, null, iArr);
    }

    ItemConfiguration findReorderSolution(int i, int i2, int i3, int i4, int i5, int i6, int[] iArr, View view, boolean z, ItemConfiguration itemConfiguration) {
        copyCurrentStateToSolution(itemConfiguration, false);
        copyOccupiedArray(this.mTmpOccupied);
        int[] iArrFindNearestArea = findNearestArea(i, i2, i5, i6, new int[2]);
        if (rearrangementExists(iArrFindNearestArea[0], iArrFindNearestArea[1], i5, i6, iArr, view, itemConfiguration)) {
            itemConfiguration.isSolution = true;
            itemConfiguration.dragViewX = iArrFindNearestArea[0];
            itemConfiguration.dragViewY = iArrFindNearestArea[1];
            itemConfiguration.dragViewSpanX = i5;
            itemConfiguration.dragViewSpanY = i6;
        } else {
            if (i5 > i3 && (i4 == i6 || z)) {
                return findReorderSolution(i, i2, i3, i4, i5 - 1, i6, iArr, view, false, itemConfiguration);
            }
            if (i6 > i4) {
                return findReorderSolution(i, i2, i3, i4, i5, i6 - 1, iArr, view, true, itemConfiguration);
            }
            itemConfiguration.isSolution = false;
        }
        return itemConfiguration;
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    public float getBackgroundAlpha() {
        return this.mBackgroundAlpha;
    }

    public float getBackgroundAlphaMultiplier() {
        return this.mBackgroundAlphaMultiplier;
    }

    public int getCellHeight() {
        return this.mCellHeight;
    }

    public CellParams getCellParams() {
        return this.mCellParams;
    }

    public int getCellWidth() {
        return this.mCellWidth;
    }

    public View getChildAt(int i, int i2) {
        return this.mShortcutsAndWidgets.getChildAt(i, i2);
    }

    public float getChildrenScale() {
        return 1.0f;
    }

    public int getCountX() {
        return this.mCountX;
    }

    public int getCountY() {
        return this.mCountY;
    }

    public int getDesiredHeight() {
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int i = this.mCountY;
        return paddingTop + (this.mCellHeight * i) + (Math.max(i - 1, 0) * this.mHeightGap);
    }

    public int getDesiredWidth() {
        int paddingLeft = getPaddingLeft() + getPaddingRight();
        int i = this.mCountX;
        return paddingLeft + (this.mCellWidth * i) + (Math.max(i - 1, 0) * this.mWidthGap);
    }

    public DeviceProfile getDeviceProfile() {
        return this.mDeviceProfile;
    }

    public float getDistanceFromCell(float f, float f2, int[] iArr) {
        cellToCenterPoint(iArr[0], iArr[1], this.mTmpPoint);
        return (float) Math.sqrt(Math.pow(f - this.mTmpPoint[0], 2.0d) + Math.pow(f2 - this.mTmpPoint[1], 2.0d));
    }

    public int getHeightGap() {
        return this.mHeightGap;
    }

    public boolean getIsDragOverlapping() {
        return this.mIsDragOverlapping;
    }

    public ShortcutAndWidgetContainer getShortcutsAndWidgets() {
        return this.mShortcutsAndWidgets;
    }

    public int getWidthGap() {
        return this.mWidthGap;
    }

    public void hideFolderAccept(FolderIcon.FolderRingAnimator folderRingAnimator) {
        this.mFolderOuterRings.remove(folderRingAnimator);
        invalidate();
    }

    public boolean isDragTarget() {
        return this.mIsDragTarget;
    }

    public boolean isDropPending() {
        return this.mDropPending;
    }

    boolean isItemPlacementDirty() {
        return this.mItemPlacementDirty;
    }

    boolean isNearestDropLocationOccupied(int i, int i2, int i3, int i4, View view, int[] iArr) {
        int[] iArrFindNearestArea = findNearestArea(i, i2, i3, i4, iArr);
        getViewsIntersectingRegion(iArrFindNearestArea[0], iArrFindNearestArea[1], i3, i4, view, null, this.mIntersectingViews);
        return !this.mIntersectingViews.isEmpty();
    }

    public boolean isOccupied(int i, int i2) {
        if (i >= this.mCountX || i2 >= this.mCountY) {
            throw new RuntimeException("Position exceeds the bound of this CellLayout");
        }
        return this.mOccupied[i][i2];
    }

    public boolean lastDownOnOccupiedCell() {
        return this.mLastDownOnOccupiedCell;
    }

    public void markCellsAsOccupiedForView(View view) {
        markCellsAsOccupiedForView(view, this.mOccupied);
    }

    public void markCellsAsOccupiedForView(View view, boolean[][] zArr) {
        if (view == null || view.getParent() != this.mShortcutsAndWidgets) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        markCellsForView(layoutParams.cellX, layoutParams.cellY, layoutParams.cellHSpan, layoutParams.cellVSpan, zArr, true);
    }

    public void markCellsAsUnoccupiedForView(View view) {
        markCellsAsUnoccupiedForView(view, this.mOccupied);
    }

    public void markCellsAsUnoccupiedForView(View view, boolean[][] zArr) {
        if (view == null || view.getParent() != this.mShortcutsAndWidgets) {
            return;
        }
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        markCellsForView(layoutParams.cellX, layoutParams.cellY, layoutParams.cellHSpan, layoutParams.cellVSpan, zArr, false);
    }

    void onDragEnter() {
        this.mDragging = true;
    }

    void onDragExit() {
        if (this.mDragging) {
            this.mDragging = false;
        }
        int[] iArr = this.mDragCell;
        iArr[1] = -1;
        iArr[0] = -1;
        this.mDragOutlineAnims[this.mDragOutlineCurrent].animateOut();
        this.mDragOutlineCurrent = (this.mDragOutlineCurrent + 1) % this.mDragOutlineAnims.length;
        revertTempState();
        setIsDragOverlapping(false);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        NinePatchDrawable ninePatchDrawable;
        if (this.mDrawBackground && this.mBackgroundAlpha > 0.0f) {
            if (this.mUseActiveGlowBackground) {
                ninePatchDrawable = this.mActiveGlowBackground;
                ninePatchDrawable.setColorFilter(this.mColorFilter);
            } else {
                ninePatchDrawable = this.mNormalBackground;
                ninePatchDrawable.setColorFilter(this.mColorFilter);
            }
            ninePatchDrawable.setAlpha((int) (this.mBackgroundAlpha * this.mBackgroundAlphaMultiplier * 255.0f));
            ninePatchDrawable.setBounds(this.mBackgroundRect);
            ninePatchDrawable.draw(canvas);
        }
        Paint paint = this.mDragOutlinePaint;
        int i = 0;
        while (true) {
            Rect[] rectArr = this.mDragOutlines;
            if (i >= rectArr.length) {
                break;
            }
            float f = this.mDragOutlineAlphas[i];
            if (f > 0.0f) {
                this.mTempRect.set(rectArr[i]);
                Utilities.scaleRectAboutCenter(this.mTempRect, getChildrenScale());
                Bitmap bitmap = (Bitmap) this.mDragOutlineAnims[i].getTag();
                paint.setAlpha((int) (f + 0.5f));
                canvas.drawBitmap(bitmap, (Rect) null, this.mTempRect, paint);
            }
            i++;
        }
        int i2 = FolderIcon.FolderRingAnimator.sPreviewSize;
        getDeviceProfile();
        for (int i3 = 0; i3 < this.mFolderOuterRings.size(); i3++) {
            FolderIcon.FolderRingAnimator folderRingAnimator = this.mFolderOuterRings.get(i3);
            cellToPoint(folderRingAnimator.mCellX, folderRingAnimator.mCellY, this.mTempLocation);
            View childAt = getChildAt(folderRingAnimator.mCellX, folderRingAnimator.mCellY);
            if (childAt != null) {
                int[] iArr = this.mTempLocation;
                int i4 = iArr[0] + (this.mCellWidth / 2);
                int paddingTop = iArr[1] + (i2 / 2) + childAt.getPaddingTop();
                Drawable drawable = FolderIcon.FolderRingAnimator.sSharedOuterRingDrawable;
                int outerRingSize = (int) (folderRingAnimator.getOuterRingSize() * getChildrenScale());
                canvas.save();
                int i5 = outerRingSize / 2;
                canvas.translate(i4 - i5, paddingTop - i5);
                drawable.setBounds(0, 0, outerRingSize, outerRingSize);
                drawable.draw(canvas);
                canvas.restore();
                Drawable drawable2 = FolderIcon.FolderRingAnimator.sSharedInnerRingDrawable;
                int innerRingSize = (int) (folderRingAnimator.getInnerRingSize() * getChildrenScale());
                canvas.save();
                int i6 = innerRingSize / 2;
                canvas.translate(i4 - i6, paddingTop - i6);
                drawable2.setBounds(0, 0, innerRingSize, innerRingSize);
                drawable2.draw(canvas);
                canvas.restore();
            }
        }
        int[] iArr2 = this.mFolderLeaveBehindCell;
        if (iArr2[0] < 0 || iArr2[1] < 0) {
            return;
        }
        Drawable drawable3 = FolderIcon.sSharedFolderLeaveBehind;
        int intrinsicWidth = drawable3.getIntrinsicWidth();
        int intrinsicHeight = drawable3.getIntrinsicHeight();
        int[] iArr3 = this.mFolderLeaveBehindCell;
        cellToPoint(iArr3[0], iArr3[1], this.mTempLocation);
        int[] iArr4 = this.mFolderLeaveBehindCell;
        View childAt2 = getChildAt(iArr4[0], iArr4[1]);
        if (childAt2 != null) {
            int[] iArr5 = this.mTempLocation;
            int i7 = iArr5[0] + (this.mCellWidth / 2);
            int paddingTop2 = iArr5[1] + (i2 / 2) + childAt2.getPaddingTop();
            canvas.save();
            int i8 = intrinsicWidth / 2;
            canvas.translate(i7 - i8, paddingTop2 - i8);
            drawable3.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            drawable3.draw(canvas);
            canvas.restore();
        }
    }

    void onDropChild(View view) {
        if (view != null) {
            ((LayoutParams) view.getLayoutParams()).dropped = true;
            view.requestLayout();
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        View.OnTouchListener onTouchListener = this.mInterceptTouchListener;
        return onTouchListener != null && onTouchListener.onTouch(this, motionEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int paddingLeft = getPaddingLeft() + ((int) Math.ceil((((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) - (this.mCountX * this.mCellWidth)) / 2.0f));
        int paddingTop = getPaddingTop();
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            getChildAt(i5).layout(paddingLeft, paddingTop, (paddingLeft + i3) - i, (paddingTop + i4) - i2);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int i4;
        DeviceProfile deviceProfile = getDeviceProfile();
        int mode = View.MeasureSpec.getMode(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        int paddingLeft = size - (getPaddingLeft() + getPaddingRight());
        int paddingTop = size2 - (getPaddingTop() + getPaddingBottom());
        if (this.mFixedCellWidth < 0 || this.mFixedCellHeight < 0) {
            int iCalculateCellWidth = deviceProfile.calculateCellWidth(paddingLeft, this.mCountX);
            int iCalculateCellHeight = deviceProfile.calculateCellHeight(paddingTop, this.mCountY);
            if (iCalculateCellWidth != this.mCellWidth || iCalculateCellHeight != this.mCellHeight) {
                this.mCellWidth = iCalculateCellWidth;
                this.mCellHeight = iCalculateCellHeight;
                this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
            }
        }
        int i5 = this.mFixedWidth;
        if (i5 <= 0 || (i3 = this.mFixedHeight) <= 0) {
            if (mode == 0 || mode2 == 0) {
                throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
            }
            i5 = paddingLeft;
            i3 = paddingTop;
        }
        int i6 = this.mCountX - 1;
        int i7 = this.mCountY - 1;
        int i8 = this.mOriginalWidthGap;
        if (i8 < 0 || (i4 = this.mOriginalHeightGap) < 0) {
            int i9 = paddingLeft - (this.mCountX * this.mCellWidth);
            int i10 = paddingTop - (this.mCountY * this.mCellHeight);
            this.mWidthGap = Math.min(this.mMaxGap, i6 > 0 ? i9 / i6 : 0);
            this.mHeightGap = Math.min(this.mMaxGap, i7 > 0 ? i10 / i7 : 0);
            this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
        } else {
            this.mWidthGap = i8;
            this.mHeightGap = i4;
        }
        int childCount = getChildCount();
        int iMax = 0;
        int iMax2 = 0;
        for (int i11 = 0; i11 < childCount; i11++) {
            View childAt = getChildAt(i11);
            childAt.measure(View.MeasureSpec.makeMeasureSpec(i5, 1073741824), View.MeasureSpec.makeMeasureSpec(i3, 1073741824));
            iMax = Math.max(iMax, childAt.getMeasuredWidth());
            iMax2 = Math.max(iMax2, childAt.getMeasuredHeight());
        }
        if (this.mFixedWidth <= 0 || this.mFixedHeight <= 0) {
            setMeasuredDimension(size, size2);
        } else {
            setMeasuredDimension(iMax, iMax2);
        }
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        Rect rect = new Rect();
        this.mNormalBackground.getPadding(rect);
        this.mBackgroundRect.set(-rect.left, -rect.top, rect.right + i, rect.bottom + i2);
        Rect rect2 = this.mForegroundRect;
        int i5 = this.mForegroundPadding;
        rect2.set(i5, i5, i - i5, i2 - i5);
    }

    /* JADX WARN: Code duplicated, block: B:17:0x0047  */
    /* JADX WARN: Code duplicated, block: B:46:0x00fa  */
    /* JADX WARN: Code duplicated, block: B:47:0x00fc  */
    /* JADX WARN: Code duplicated, block: B:53:0x010b  */
    int[] performReorder(int i, int i2, int i3, int i4, int i5, int i6, View view, int[] iArr, int[] iArr2, int i7) {
        int[] iArrFindNearestArea = findNearestArea(i, i2, i5, i6, iArr);
        int[] iArr3 = iArr2 == null ? new int[2] : iArr2;
        if (i7 == 2 || i7 == 3 || i7 == 4) {
            int[] iArr4 = this.mPreviousReorderDirection;
            if (iArr4[0] != -100) {
                int[] iArr5 = this.mDirectionVector;
                iArr5[0] = iArr4[0];
                iArr5[1] = iArr4[1];
                if (i7 == 2 || i7 == 3) {
                    int[] iArr6 = this.mPreviousReorderDirection;
                    iArr6[0] = -100;
                    iArr6[1] = -100;
                }
            } else {
                getDirectionVectorForDrop(i, i2, i5, i6, view, this.mDirectionVector);
                int[] iArr7 = this.mPreviousReorderDirection;
                int[] iArr8 = this.mDirectionVector;
                iArr7[0] = iArr8[0];
                iArr7[1] = iArr8[1];
            }
        } else {
            getDirectionVectorForDrop(i, i2, i5, i6, view, this.mDirectionVector);
            int[] iArr9 = this.mPreviousReorderDirection;
            int[] iArr10 = this.mDirectionVector;
            iArr9[0] = iArr10[0];
            iArr9[1] = iArr10[1];
        }
        ItemConfiguration itemConfigurationFindReorderSolution = findReorderSolution(i, i2, i3, i4, i5, i6, this.mDirectionVector, view, true, new ItemConfiguration());
        ItemConfiguration itemConfigurationFindConfigurationNoShuffle = findConfigurationNoShuffle(i, i2, i3, i4, i5, i6, view, new ItemConfiguration());
        if (itemConfigurationFindReorderSolution.isSolution && itemConfigurationFindReorderSolution.area() >= itemConfigurationFindConfigurationNoShuffle.area()) {
            itemConfigurationFindConfigurationNoShuffle = itemConfigurationFindReorderSolution;
        } else if (!itemConfigurationFindConfigurationNoShuffle.isSolution) {
            itemConfigurationFindConfigurationNoShuffle = null;
        }
        if (i7 == 0) {
            if (itemConfigurationFindConfigurationNoShuffle != null) {
                beginOrAdjustReorderPreviewAnimations(itemConfigurationFindConfigurationNoShuffle, view, 0, 0);
                iArrFindNearestArea[0] = itemConfigurationFindConfigurationNoShuffle.dragViewX;
                iArrFindNearestArea[1] = itemConfigurationFindConfigurationNoShuffle.dragViewY;
                iArr3[0] = itemConfigurationFindConfigurationNoShuffle.dragViewSpanX;
                iArr3[1] = itemConfigurationFindConfigurationNoShuffle.dragViewSpanY;
            } else {
                iArr3[1] = -1;
                iArr3[0] = -1;
                iArrFindNearestArea[1] = -1;
                iArrFindNearestArea[0] = -1;
            }
            return iArrFindNearestArea;
        }
        boolean z2 = true;
        setUseTempCoords(true);
        if (itemConfigurationFindConfigurationNoShuffle != null) {
            iArrFindNearestArea[0] = itemConfigurationFindConfigurationNoShuffle.dragViewX;
            iArrFindNearestArea[1] = itemConfigurationFindConfigurationNoShuffle.dragViewY;
            iArr3[0] = itemConfigurationFindConfigurationNoShuffle.dragViewSpanX;
            iArr3[1] = itemConfigurationFindConfigurationNoShuffle.dragViewSpanY;
            if (i7 == 1 || i7 == 2 || i7 == 3) {
                copySolutionToTempState(itemConfigurationFindConfigurationNoShuffle, view);
                setItemPlacementDirty(true);
                animateItemsToSolution(itemConfigurationFindConfigurationNoShuffle, view, i7 == 2);
                if (i7 == 2 || i7 == 3) {
                    commitTempPlacement();
                    completeAndClearReorderPreviewAnimations();
                    setItemPlacementDirty(false);
                } else {
                    beginOrAdjustReorderPreviewAnimations(itemConfigurationFindConfigurationNoShuffle, view, 150, 1);
                }
            }
        } else {
            iArr3[1] = -1;
            iArr3[0] = -1;
            iArrFindNearestArea[1] = -1;
            iArrFindNearestArea[0] = -1;
            z2 = false;
        }
        if (i7 == 2 || !z2) {
            setUseTempCoords(false);
        }
        this.mShortcutsAndWidgets.requestLayout();
        return iArrFindNearestArea;
    }

    public void prepareChildForDrag(View view) {
        markCellsAsUnoccupiedForView(view);
    }

    void regionToCenterPoint(int i, int i2, int i3, int i4, int[] iArr) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int i5 = this.mCellWidth;
        int i6 = this.mWidthGap;
        iArr[0] = paddingLeft + (i * (i5 + i6)) + (((i5 * i3) + ((i3 - 1) * i6)) / 2);
        int i7 = this.mCellHeight;
        int i8 = this.mHeightGap;
        iArr[1] = paddingTop + (i2 * (i7 + i8)) + (((i7 * i4) + ((i4 - 1) * i8)) / 2);
    }

    void regionToRect(int i, int i2, int i3, int i4, Rect rect) {
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int i5 = this.mCellWidth;
        int i6 = this.mWidthGap;
        int i7 = paddingLeft + (i * (i5 + i6));
        int i8 = this.mCellHeight;
        int i9 = this.mHeightGap;
        int i10 = paddingTop + (i2 * (i8 + i9));
        rect.set(i7, i10, (i5 * i3) + ((i3 - 1) * i6) + i7, (i8 * i4) + ((i4 - 1) * i9) + i10);
    }

    @Override // android.view.ViewGroup
    public void removeAllViews() {
        clearOccupiedCells();
        this.mShortcutsAndWidgets.removeAllViews();
    }

    @Override // android.view.ViewGroup
    public void removeAllViewsInLayout() {
        if (this.mShortcutsAndWidgets.getChildCount() > 0) {
            clearOccupiedCells();
            this.mShortcutsAndWidgets.removeAllViewsInLayout();
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void removeView(View view) {
        markCellsAsUnoccupiedForView(view);
        this.mShortcutsAndWidgets.removeView(view);
    }

    @Override // android.view.ViewGroup
    public void removeViewAt(int i) {
        markCellsAsUnoccupiedForView(this.mShortcutsAndWidgets.getChildAt(i));
        this.mShortcutsAndWidgets.removeViewAt(i);
    }

    @Override // android.view.ViewGroup
    public void removeViewInLayout(View view) {
        markCellsAsUnoccupiedForView(view);
        this.mShortcutsAndWidgets.removeViewInLayout(view);
    }

    @Override // android.view.ViewGroup
    public void removeViews(int i, int i2) {
        for (int i3 = i; i3 < i + i2; i3++) {
            markCellsAsUnoccupiedForView(this.mShortcutsAndWidgets.getChildAt(i3));
        }
        this.mShortcutsAndWidgets.removeViews(i, i2);
    }

    @Override // android.view.ViewGroup
    public void removeViewsInLayout(int i, int i2) {
        for (int i3 = i; i3 < i + i2; i3++) {
            markCellsAsUnoccupiedForView(this.mShortcutsAndWidgets.getChildAt(i3));
        }
        this.mShortcutsAndWidgets.removeViewsInLayout(i, i2);
    }

    public void restoreInstanceState(SparseArray<Parcelable> sparseArray) {
        try {
            dispatchRestoreInstanceState(sparseArray);
        } catch (IllegalArgumentException e) {
            if (LauncherAppState.isDogfoodBuild()) {
                throw e;
            }
            Log.e("CellLayout", "Ignoring an error while restoring a view instance state", e);
        }
    }

    void revertTempState() {
        completeAndClearReorderPreviewAnimations();
        if (isItemPlacementDirty()) {
            int childCount = this.mShortcutsAndWidgets.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = this.mShortcutsAndWidgets.getChildAt(i);
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.tmpCellX != layoutParams.cellX || layoutParams.tmpCellY != layoutParams.cellY) {
                    layoutParams.tmpCellX = layoutParams.cellX;
                    layoutParams.tmpCellY = layoutParams.cellY;
                    animateChildToPosition(childAt, layoutParams.cellX, layoutParams.cellY, 150, 0, false, false);
                }
            }
            setItemPlacementDirty(false);
        }
    }

    public void setBackgroundAlpha(float f) {
        if (this.mBackgroundAlpha != f) {
            this.mBackgroundAlpha = f;
            invalidate();
        }
    }

    public void setBackgroundAlphaMultiplier(float f) {
        if (this.mBackgroundAlphaMultiplier != f) {
            this.mBackgroundAlphaMultiplier = f;
            invalidate();
        }
    }

    public void setCellDimensions(int i, int i2) {
        this.mCellWidth = i;
        this.mFixedCellWidth = i;
        this.mCellHeight = i2;
        this.mFixedCellHeight = i2;
        this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
    }

    public void setCellParams(CellParams cellParams) {
        if (cellParams == null) {
            throw new IllegalArgumentException("Cell params must not be null!");
        }
        if (cellParams != this.mCellParams) {
            this.mCellParams = cellParams;
            this.mShortcutsAndWidgets.setCellParams(this.mCellParams);
        }
    }

    @Override // android.view.ViewGroup
    protected void setChildrenDrawingCacheEnabled(boolean z) {
        this.mShortcutsAndWidgets.setChildrenDrawingCacheEnabled(z);
    }

    @Override // android.view.ViewGroup
    protected void setChildrenDrawnWithCacheEnabled(boolean z) {
        this.mShortcutsAndWidgets.setChildrenDrawnWithCacheEnabled(z);
    }

    void setColor(int i) {
        boolean z;
        if (i != this.mColorTint) {
            this.mColorTint = i;
            z = true;
            if (i != 0) {
                this.mColorFilter = new PorterDuffColorFilter(i, PorterDuff.Mode.SRC_ATOP);
            } else {
                this.mColorFilter = null;
            }
        } else {
            z = false;
        }
        if (z) {
            invalidate();
        }
    }

    public void setDeviceProfile(DeviceProfile deviceProfile) {
        if (deviceProfile == null) {
            throw new IllegalArgumentException("Device profile must not be null!");
        }
        if (deviceProfile != this.mDeviceProfile) {
            this.mDeviceProfile = deviceProfile;
            this.mShortcutsAndWidgets.setDeviceProfile(deviceProfile);
        }
    }

    public void setDropPending(boolean z) {
        this.mDropPending = z;
    }

    public void setFixedSize(int i, int i2) {
        this.mFixedWidth = i;
        this.mFixedHeight = i2;
    }

    public void setGridSize(int i, int i2) {
        this.mCountX = i;
        this.mCountY = i2;
        this.mOccupied = (boolean[][]) Array.newInstance((Class<?>) boolean.class, this.mCountX, this.mCountY);
        this.mTmpOccupied = (boolean[][]) Array.newInstance((Class<?>) boolean.class, this.mCountX, this.mCountY);
        this.mTempRectStack.clear();
        this.mShortcutsAndWidgets.setCellDimensions(this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCountX, this.mCountY);
        requestLayout();
    }

    public void setInvertIfRtl(boolean z) {
        this.mShortcutsAndWidgets.setInvertIfRtl(z);
    }

    void setIsDragOverlapping(boolean z) {
        if (this.mIsDragOverlapping != z) {
            this.mIsDragOverlapping = z;
            setUseActiveGlowBackground(this.mIsDragOverlapping);
            invalidate();
        }
    }

    void setItemPlacementDirty(boolean z) {
        this.mItemPlacementDirty = z;
    }

    public void setOnInterceptTouchListener(View.OnTouchListener onTouchListener) {
        this.mInterceptTouchListener = onTouchListener;
    }

    /* JADX WARN: Code duplicated, block: B:6:0x000b A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:7:0x000d  */
    /* JADX WARN: Code duplicated, block: B:9:0x0013  */
    void setOverScrollAmount(float f, boolean z) {
        Drawable drawable;
        Drawable drawable2;
        if (z) {
            Drawable drawable3 = this.mOverScrollForegroundDrawable;
            Drawable drawable4 = this.mOverScrollLeft;
            if (drawable3 != drawable4) {
                this.mOverScrollForegroundDrawable = drawable4;
            } else if (!z) {
                drawable = this.mOverScrollForegroundDrawable;
                drawable2 = this.mOverScrollRight;
                if (drawable != drawable2) {
                    this.mOverScrollForegroundDrawable = drawable2;
                }
            }
        } else if (!z) {
            drawable = this.mOverScrollForegroundDrawable;
            drawable2 = this.mOverScrollRight;
            if (drawable != drawable2) {
                this.mOverScrollForegroundDrawable = drawable2;
            }
        }
        this.mForegroundAlpha = Math.round(f * this.FOREGROUND_ALPHA_DAMPER * 255.0f);
        invalidate();
    }

    void setPressedIcon(BubbleTextView bubbleTextView, Bitmap bitmap, int i) {
        if (bubbleTextView == null || bitmap == null) {
            this.mTouchFeedbackView.setBitmap(null);
            this.mTouchFeedbackView.animate().cancel();
            return;
        }
        this.mTouchFeedbackView.setTranslationX((bubbleTextView.getLeft() + ((int) Math.ceil((((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight()) - (this.mCountX * this.mCellWidth)) / 2.0f))) - i);
        this.mTouchFeedbackView.setTranslationY(bubbleTextView.getTop() - i);
        if (this.mTouchFeedbackView.setBitmap(bitmap)) {
            this.mTouchFeedbackView.setAlpha(0.0f);
            this.mTouchFeedbackView.animate().alpha(1.0f).setDuration(2000L).setInterpolator(FastBitmapDrawable.CLICK_FEEDBACK_INTERPOLATOR).start();
        }
    }

    public void setShortcutAndWidgetAlpha(float f) {
        this.mShortcutsAndWidgets.setAlpha(f);
    }

    void setUseActiveGlowBackground(boolean z) {
        this.mUseActiveGlowBackground = z;
    }

    public void setUseTempCoords(boolean z) {
        int childCount = this.mShortcutsAndWidgets.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((LayoutParams) this.mShortcutsAndWidgets.getChildAt(i).getLayoutParams()).useTmpCoords = z;
        }
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void showFolderAccept(FolderIcon.FolderRingAnimator folderRingAnimator) {
        this.mFolderOuterRings.add(folderRingAnimator);
    }

    void visualizeDropLocation(View view, Bitmap bitmap, int i, int i2, int i3, int i4, int i5, int i6, boolean z, Point point, Rect rect) {
        int width;
        int height;
        int[] iArr = this.mDragCell;
        int i7 = iArr[0];
        int i8 = iArr[1];
        if (bitmap == null) {
            return;
        }
        if (i3 == i7 && i4 == i8) {
            return;
        }
        int[] iArr2 = this.mDragCell;
        iArr2[0] = i3;
        iArr2[1] = i4;
        int[] iArr3 = this.mTmpPoint;
        cellToPoint(i3, i4, iArr3);
        int i9 = iArr3[0];
        int i10 = iArr3[1];
        if (view != null && point == null) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int i11 = i9 + marginLayoutParams.leftMargin;
            height = i10 + marginLayoutParams.topMargin + ((view.getHeight() - bitmap.getHeight()) / 2);
            width = i11 + ((((this.mCellWidth * i5) + ((i5 - 1) * this.mWidthGap)) - bitmap.getWidth()) / 2);
        } else if (point == null || rect == null) {
            width = i9 + ((((this.mCellWidth * i5) + ((i5 - 1) * this.mWidthGap)) - bitmap.getWidth()) / 2);
            height = i10 + ((((this.mCellHeight * i6) + ((i6 - 1) * this.mHeightGap)) - bitmap.getHeight()) / 2);
        } else {
            width = i9 + point.x + ((((this.mCellWidth * i5) + ((i5 - 1) * this.mWidthGap)) - rect.width()) / 2);
            height = i10 + point.y + ((int) Math.max(0.0f, (this.mCellHeight - getShortcutsAndWidgets().getCellContentHeight()) / 2.0f));
        }
        int i12 = this.mDragOutlineCurrent;
        this.mDragOutlineAnims[i12].animateOut();
        Rect[] rectArr = this.mDragOutlines;
        this.mDragOutlineCurrent = (i12 + 1) % rectArr.length;
        Rect rect2 = rectArr[this.mDragOutlineCurrent];
        rect2.set(width, height, bitmap.getWidth() + width, bitmap.getHeight() + height);
        if (z) {
            cellToRect(i3, i4, i5, i6, rect2);
        }
        this.mDragOutlineAnims[this.mDragOutlineCurrent].setTag(bitmap);
        this.mDragOutlineAnims[this.mDragOutlineCurrent].animateIn();
    }
}
