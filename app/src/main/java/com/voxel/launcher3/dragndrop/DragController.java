package com.voxel.launcher3.dragndrop;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import com.evie.common.dragndrop.DragSource;
import com.voxel.launcher3.DropTarget;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.ShortcutInfo;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.accessibility.DragViewStateAnnouncer;
import com.voxel.launcher3.config.FeatureFlags;
import com.voxel.launcher3.util.ItemInfoMatcher;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Iterator;

/* JADX INFO: loaded from: classes.dex */
public class DragController implements DragDriver.EventListener {
    private DropTarget.DragObject mDragObject;
    DragScroller mDragScroller;
    private DropTarget mFlingToDeleteDropTarget;
    protected final int mFlingToDeleteThresholdVelocity;
    private Handler mHandler;
    private InputMethodManager mInputMethodManager;
    private boolean mIsDragDeferred;
    private final boolean mIsRtl;
    private DropTarget mLastDropTarget;
    Launcher mLauncher;
    private int mMotionDownX;
    private int mMotionDownY;
    private View mMoveTarget;
    private DragOptions mOptions;
    private View mScrollView;
    private final int mScrollZone;
    private VelocityTracker mVelocityTracker;
    private IBinder mWindowToken;
    private Rect mRectTemp = new Rect();
    private final int[] mCoordinatesTemp = new int[2];
    private DragDriver mDragDriver = null;
    private ArrayList<DropTarget> mDropTargets = new ArrayList<>();
    private ArrayList<DragListener> mListeners = new ArrayList<>();
    int mScrollState = 0;
    private ScrollRunnable mScrollRunnable = new ScrollRunnable();
    int[] mLastTouch = new int[2];
    long mLastTouchUpTime = -1;
    int mDistanceSinceScroll = 0;
    private int[] mTmpPoint = new int[2];
    private Rect mDragLayerRect = new Rect();

    public interface DragListener {
        void onDragEnd();

        void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions);
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (DragController.this.mDragScroller != null) {
                if (this.mDirection == 0) {
                    DragController.this.mDragScroller.scrollLeft();
                } else {
                    DragController.this.mDragScroller.scrollRight();
                }
                DragController dragController = DragController.this;
                dragController.mScrollState = 0;
                dragController.mDistanceSinceScroll = 0;
                dragController.mDragScroller.onExitScrollArea();
                DragController.this.mLauncher.getDragLayer().onExitScrollArea();
                if (DragController.this.isDragging()) {
                    DragController dragController2 = DragController.this;
                    dragController2.checkScrollState(dragController2.mLastTouch[0], DragController.this.mLastTouch[1]);
                }
            }
        }

        void setDirection(int i) {
            this.mDirection = i;
        }
    }

    public DragController(Launcher launcher) {
        Resources resources = launcher.getResources();
        this.mLauncher = launcher;
        this.mHandler = new Handler();
        this.mScrollZone = resources.getDimensionPixelSize(R.dimen.scroll_zone);
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mFlingToDeleteThresholdVelocity = resources.getDimensionPixelSize(R.dimen.drag_flingToDeleteMinVelocity);
        this.mIsRtl = Utilities.isRtl(resources);
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
    }

    private void checkTouchMove(DropTarget dropTarget) {
        if (dropTarget != null) {
            DropTarget dropTarget2 = this.mLastDropTarget;
            if (dropTarget2 != dropTarget) {
                if (dropTarget2 != null) {
                    dropTarget2.onDragExit(this.mDragObject);
                }
                dropTarget.onDragEnter(this.mDragObject);
            }
            dropTarget.onDragOver(this.mDragObject);
        } else {
            DropTarget dropTarget3 = this.mLastDropTarget;
            if (dropTarget3 != null) {
                dropTarget3.onDragExit(this.mDragObject);
            }
        }
        this.mLastDropTarget = dropTarget;
    }

    private void clearScrollRunnable() {
        this.mHandler.removeCallbacks(this.mScrollRunnable);
        if (this.mScrollState == 1) {
            this.mScrollState = 0;
            this.mScrollRunnable.setDirection(1);
            this.mDragScroller.onExitScrollArea();
            this.mLauncher.getDragLayer().onExitScrollArea();
        }
    }

    private void endDrag() {
        if (isDragging()) {
            this.mDragDriver = null;
            this.mOptions = null;
            clearScrollRunnable();
            /* REWORK: a nested endDrag/cleanup inside drop() (deferred widget-drag
               cleanup) can null mDragObject while isDragging() still holds; the dex
               never hit that re-entrancy, so guard the object here. */
            if (this.mDragObject == null) {
                releaseVelocityTracker();
                return;
            }
            boolean z = false;
            if (this.mDragObject.dragView != null) {
                z = this.mDragObject.deferDragViewCleanupPostAnimation;
                if (!z) {
                    this.mDragObject.dragView.remove();
                }
                this.mDragObject.dragView = null;
            }
            if (!z) {
                Iterator it = new ArrayList(this.mListeners).iterator();
                while (it.hasNext()) {
                    ((DragListener) it.next()).onDragEnd();
                }
                this.mDragObject = null;
            }
        }
        releaseVelocityTracker();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private DropTarget findDropTarget(int i, int i2, int[] iArr) {
        Rect rect = this.mRectTemp;
        ArrayList<DropTarget> arrayList = this.mDropTargets;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            DropTarget dropTarget = arrayList.get(size);
            if (dropTarget.isDropEnabled()) {
                dropTarget.getHitRectRelativeToDragLayer(rect);
                DropTarget.DragObject dragObject = this.mDragObject;
                dragObject.x = i;
                dragObject.y = i2;
                if (rect.contains(i, i2)) {
                    iArr[0] = i;
                    iArr[1] = i2;
                    this.mLauncher.getDragLayer().mapCoordInSelfToDescendent((View) dropTarget, iArr);
                    return dropTarget;
                }
            }
        }
        return null;
    }

    private float getAngleBetweenVectors(PointF pointF, PointF pointF2) {
        double d = ((pointF.x * pointF2.x) + (pointF.y * pointF2.y)) / (pointF.length() * pointF2.length());
        return (float) Math.acos(Math.max(-1.0d, Math.min(1.0d, d)));
    }

    private int[] getClampedDragLayerPos(float f, float f2) {
        this.mLauncher.getDragLayer().getLocalVisibleRect(this.mDragLayerRect);
        this.mTmpPoint[0] = (int) Math.max(this.mDragLayerRect.left, Math.min(f, this.mDragLayerRect.right - 1));
        this.mTmpPoint[1] = (int) Math.max(this.mDragLayerRect.top, Math.min(f2, this.mDragLayerRect.bottom - 1));
        return this.mTmpPoint;
    }

    private void handleMoveEvent(int i, int i2) {
        this.mDragObject.dragView.move(i, i2);
        int[] iArr = this.mCoordinatesTemp;
        DropTarget dropTargetFindDropTarget = findDropTarget(i, i2, iArr);
        DropTarget.DragObject dragObject = this.mDragObject;
        dragObject.x = iArr[0];
        dragObject.y = iArr[1];
        checkTouchMove(dropTargetFindDropTarget);
        if (!this.mDragObject.isDesktopLocked) {
            double d = this.mDistanceSinceScroll;
            int[] iArr2 = this.mLastTouch;
            double dHypot = Math.hypot(iArr2[0] - i, iArr2[1] - i2);
            Double.isNaN(d);
            this.mDistanceSinceScroll = (int) (d + dHypot);
            int[] iArr3 = this.mLastTouch;
            iArr3[0] = i;
            iArr3[1] = i2;
            checkScrollState(i, i2);
        }
        if (this.mIsDragDeferred && this.mOptions.deferDragCondition.shouldStartDeferredDrag(Math.hypot(i - this.mMotionDownX, i2 - this.mMotionDownY))) {
            startDeferredDrag();
        }
    }

    private PointF isFlingingToDelete(DragSource dragSource) {
        if (this.mFlingToDeleteDropTarget == null || !dragSource.supportsFlingToDelete()) {
            return null;
        }
        this.mVelocityTracker.computeCurrentVelocity(1000, ViewConfiguration.get(this.mLauncher).getScaledMaximumFlingVelocity());
        PointF pointF = new PointF(this.mVelocityTracker.getXVelocity(), this.mVelocityTracker.getYVelocity());
        float angleBetweenVectors = 36.0f;
        if (this.mVelocityTracker.getYVelocity() < this.mFlingToDeleteThresholdVelocity) {
            angleBetweenVectors = getAngleBetweenVectors(pointF, new PointF(0.0f, -1.0f));
        } else if (LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().isVerticalBarLayout() && this.mVelocityTracker.getXVelocity() < this.mFlingToDeleteThresholdVelocity) {
            angleBetweenVectors = getAngleBetweenVectors(pointF, new PointF(-1.0f, 0.0f));
        }
        if (angleBetweenVectors <= Math.toRadians(35.0d)) {
            return pointF;
        }
        return null;
    }

    private void releaseVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void addDragListener(DragListener dragListener) {
        this.mListeners.add(dragListener);
    }

    public void addDropTarget(DropTarget dropTarget) {
        this.mDropTargets.add(dropTarget);
    }

    public void cancelDrag() {
        if (isDragging()) {
            DropTarget dropTarget = this.mLastDropTarget;
            if (dropTarget != null) {
                dropTarget.onDragExit(this.mDragObject);
            }
            DropTarget.DragObject dragObject = this.mDragObject;
            dragObject.deferDragViewCleanupPostAnimation = false;
            dragObject.cancelled = true;
            dragObject.dragComplete = true;
            dragObject.dragSource.onDropCompleted(null, this.mDragObject, false, false);
        }
        endDrag();
    }

    void checkScrollState(int i, int i2) {
        int i3 = this.mDistanceSinceScroll < ViewConfiguration.get(this.mLauncher).getScaledWindowTouchSlop() ? 900 : 500;
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        boolean z = this.mIsRtl;
        int i4 = !z ? 1 : 0;
        if (i < this.mScrollZone) {
            if (this.mScrollState == 0) {
                this.mScrollState = 1;
                if (this.mDragScroller.onEnterScrollArea(i, i2, z ? 1 : 0)) {
                    dragLayer.onEnterScrollArea();
                    this.mScrollRunnable.setDirection(z ? 1 : 0);
                    this.mHandler.postDelayed(this.mScrollRunnable, i3);
                    return;
                }
                return;
            }
            return;
        }
        if (i <= this.mScrollView.getWidth() - this.mScrollZone) {
            clearScrollRunnable();
            return;
        }
        if (this.mScrollState == 0) {
            this.mScrollState = 1;
            if (this.mDragScroller.onEnterScrollArea(i, i2, i4)) {
                dragLayer.onEnterScrollArea();
                this.mScrollRunnable.setDirection(i4);
                this.mHandler.postDelayed(this.mScrollRunnable, i3);
            }
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return this.mDragDriver != null;
    }

    public boolean dispatchUnhandledMove(View view, int i) {
        View view2 = this.mMoveTarget;
        return view2 != null && view2.dispatchUnhandledMove(view, i);
    }

    /* JADX WARN: Code duplicated, block: B:19:0x0044  */
    /* JADX WARN: Multi-variable type inference failed */
    void drop(DropTarget dropTarget, float f, float f2, PointF pointF) {
        boolean z;
        int[] iArr = this.mCoordinatesTemp;
        DropTarget.DragObject dragObject = this.mDragObject;
        dragObject.x = iArr[0];
        dragObject.y = iArr[1];
        DropTarget dropTarget2 = this.mLastDropTarget;
        if (dropTarget != dropTarget2) {
            if (dropTarget2 != null) {
                dropTarget2.onDragExit(dragObject);
            }
            this.mLastDropTarget = dropTarget;
            if (dropTarget != null) {
                dropTarget.onDragEnter(this.mDragObject);
            }
        }
        DropTarget.DragObject dragObject2 = this.mDragObject;
        dragObject2.dragComplete = true;
        if (dropTarget != null) {
            dropTarget.onDragExit(dragObject2);
            if (this.mIsDragDeferred || !dropTarget.acceptDrop(this.mDragObject)) {
                z = false;
            } else {
                if (pointF != null) {
                    dropTarget.onFlingToDelete(this.mDragObject, pointF);
                } else {
                    dropTarget.onDrop(this.mDragObject);
                }
                z = true;
            }
        } else {
            z = false;
        }
        /* REWORK: dex reads dragSource from the entry-captured local (DragController.smali
           drop(), iget p3,p3 dragSource after :cond_b); a nested cancelDrag during
           onDrop nulls the FIELD — re-reading it here NPE'd the drop. */
        dragObject.dragSource.onDropCompleted(dropTarget instanceof View ? (View) dropTarget : null, dragObject, pointF != null, z);
        if (this.mIsDragDeferred) {
            this.mOptions.deferDragCondition.onDropBeforeDeferredDrag();
        }
    }

    public void forceTouchMove() {
        int[] iArr = this.mCoordinatesTemp;
        int[] iArr2 = this.mLastTouch;
        DropTarget dropTargetFindDropTarget = findDropTarget(iArr2[0], iArr2[1], iArr);
        DropTarget.DragObject dragObject = this.mDragObject;
        dragObject.x = iArr[0];
        dragObject.y = iArr[1];
        checkTouchMove(dropTargetFindDropTarget);
    }

    public long getLastGestureUpTime() {
        return this.mDragDriver != null ? System.currentTimeMillis() : this.mLastTouchUpTime;
    }

    public boolean isDragging() {
        DragOptions dragOptions;
        return this.mDragDriver != null || ((dragOptions = this.mOptions) != null && dragOptions.isAccessibleDrag);
    }

    public void onAppsRemoved(ItemInfoMatcher itemInfoMatcher) {
        ComponentName targetComponent;
        DropTarget.DragObject dragObject = this.mDragObject;
        if (dragObject != null) {
            ItemInfo itemInfo = dragObject.dragInfo;
            if ((itemInfo instanceof ShortcutInfo) && (targetComponent = itemInfo.getTargetComponent()) != null && itemInfoMatcher.matches(itemInfo, targetComponent)) {
                cancelDrag();
            }
        }
    }

    void onDeferredEndDrag(DragView dragView) {
        dragView.remove();
        DropTarget.DragObject dragObject = this.mDragObject;
        if (dragObject == null) {
            return;
        }
        if (dragObject.deferDragViewCleanupPostAnimation) {
            Iterator it = new ArrayList(this.mListeners).iterator();
            while (it.hasNext()) {
                ((DragListener) it.next()).onDragEnd();
            }
        }
        this.mDragObject = null;
    }

    public void onDeferredEndFling(DropTarget.DragObject dragObject) {
        dragObject.dragSource.onFlingToDeleteCompleted();
    }

    public void onDragViewAnimationEnd() {
        DragDriver dragDriver = this.mDragDriver;
        if (dragDriver != null) {
            dragDriver.onDragViewAnimationEnd();
        }
    }

    @Override // com.voxel.launcher3.dragndrop.DragDriver.EventListener
    public void onDriverDragCancel() {
        cancelDrag();
    }

    @Override // com.voxel.launcher3.dragndrop.DragDriver.EventListener
    public void onDriverDragEnd(float f, float f2, DropTarget dropTarget) {
        PointF pointFIsFlingingToDelete;
        if (dropTarget != null) {
            pointFIsFlingingToDelete = null;
        } else {
            pointFIsFlingingToDelete = isFlingingToDelete(this.mDragObject.dragSource);
            dropTarget = pointFIsFlingingToDelete != null ? this.mFlingToDeleteDropTarget : findDropTarget((int) f, (int) f2, this.mCoordinatesTemp);
        }
        drop(dropTarget, f, f2, pointFIsFlingingToDelete);
        endDrag();
    }

    @Override // com.voxel.launcher3.dragndrop.DragDriver.EventListener
    public void onDriverDragMove(float f, float f2) {
        int[] clampedDragLayerPos = getClampedDragLayerPos(f, f2);
        handleMoveEvent(clampedDragLayerPos[0], clampedDragLayerPos[1]);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        DragOptions dragOptions = this.mOptions;
        if (dragOptions != null && dragOptions.isAccessibleDrag) {
            return false;
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        int action = motionEvent.getAction();
        int[] clampedDragLayerPos = getClampedDragLayerPos(motionEvent.getX(), motionEvent.getY());
        int i = clampedDragLayerPos[0];
        int i2 = clampedDragLayerPos[1];
        switch (action) {
            case 0:
                this.mMotionDownX = i;
                this.mMotionDownY = i2;
                break;
            case 1:
                this.mLastTouchUpTime = System.currentTimeMillis();
                break;
        }
        DragDriver dragDriver = this.mDragDriver;
        return dragDriver != null && dragDriver.onInterceptTouchEvent(motionEvent);
    }

    /* JADX WARN: Code duplicated, block: B:20:0x0050  */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        DragOptions dragOptions;
        if (this.mDragDriver == null || (dragOptions = this.mOptions) == null || dragOptions.isAccessibleDrag) {
            return false;
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        int action = motionEvent.getAction();
        int[] clampedDragLayerPos = getClampedDragLayerPos(motionEvent.getX(), motionEvent.getY());
        int i = clampedDragLayerPos[0];
        int i2 = clampedDragLayerPos[1];
        if (action != 3) {
            switch (action) {
                case 0:
                    this.mMotionDownX = i;
                    this.mMotionDownY = i2;
                    if (i >= this.mScrollZone && i <= this.mScrollView.getWidth() - this.mScrollZone) {
                        this.mScrollState = 0;
                    } else {
                        this.mScrollState = 1;
                        this.mHandler.postDelayed(this.mScrollRunnable, 500L);
                    }
                    break;
                case 1:
                    this.mHandler.removeCallbacks(this.mScrollRunnable);
                    break;
            }
        } else {
            this.mHandler.removeCallbacks(this.mScrollRunnable);
        }
        return this.mDragDriver.onTouchEvent(motionEvent);
    }

    public void removeDragListener(DragListener dragListener) {
        this.mListeners.remove(dragListener);
    }

    public void removeDropTarget(DropTarget dropTarget) {
        this.mDropTargets.remove(dropTarget);
    }

    public void resetLastGestureUpTime() {
        this.mLastTouchUpTime = -1L;
    }

    public void setDragScoller(DragScroller dragScroller) {
        this.mDragScroller = dragScroller;
    }

    public void setMoveTarget(View view) {
        this.mMoveTarget = view;
    }

    public void setScrollView(View view) {
        this.mScrollView = view;
    }

    public void setWindowToken(IBinder iBinder) {
        this.mWindowToken = iBinder;
    }

    public void startDeferredDrag() {
        Iterator it = new ArrayList(this.mListeners).iterator();
        while (it.hasNext()) {
            ((DragListener) it.next()).onDragStart(this.mDragObject, this.mOptions);
        }
        this.mOptions.deferDragCondition.onDragStart();
        if (this.mOptions.isDesktopLocked) {
            this.mDragObject.dragView.setColor(this.mLauncher.getResources().getColor(R.color.delete_target_hover_tint));
        }
        this.mIsDragDeferred = false;
    }

    public DragView startDrag(Bitmap bitmap, int i, int i2, DragSource dragSource, ItemInfo itemInfo, Point point, Rect rect, float f, DragOptions dragOptions) {
        float dimensionPixelSize;
        if (this.mInputMethodManager == null) {
            this.mInputMethodManager = (InputMethodManager) this.mLauncher.getSystemService("input_method");
        }
        this.mInputMethodManager.hideSoftInputFromWindow(this.mWindowToken, 0);
        this.mOptions = dragOptions;
        if (this.mOptions.systemDndStartPoint != null) {
            this.mMotionDownX = this.mOptions.systemDndStartPoint.x;
            this.mMotionDownY = this.mOptions.systemDndStartPoint.y;
        }
        int i3 = this.mMotionDownX - i;
        int i4 = this.mMotionDownY - i2;
        int i5 = rect == null ? 0 : rect.left;
        int i6 = rect == null ? 0 : rect.top;
        this.mLastDropTarget = null;
        this.mDragObject = new DropTarget.DragObject();
        this.mIsDragDeferred = !this.mOptions.deferDragCondition.shouldStartDeferredDrag(0.0d);
        Resources resources = this.mLauncher.getResources();
        if (FeatureFlags.LAUNCHER3_LEGACY_WORKSPACE_DND) {
            dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.dragViewScale);
        } else {
            dimensionPixelSize = this.mIsDragDeferred ? resources.getDimensionPixelSize(R.dimen.deferred_drag_view_scale) : 0.0f;
        }
        DropTarget.DragObject dragObject = this.mDragObject;
        DragView dragView = new DragView(this.mLauncher, bitmap, i3, i4, f, dimensionPixelSize);
        dragObject.dragView = dragView;
        this.mDragObject.isDesktopLocked = dragOptions.isDesktopLocked;
        this.mDragObject.dragComplete = false;
        if (this.mOptions.isAccessibleDrag) {
            this.mDragObject.xOffset = bitmap.getWidth() / 2;
            this.mDragObject.yOffset = bitmap.getHeight() / 2;
            this.mDragObject.accessibleDrag = true;
        } else {
            DropTarget.DragObject dragObject2 = this.mDragObject;
            dragObject2.xOffset = this.mMotionDownX - (i + i5);
            dragObject2.yOffset = this.mMotionDownY - (i2 + i6);
            dragObject2.stateAnnouncer = DragViewStateAnnouncer.createFor(dragView);
            this.mDragDriver = DragDriver.create(this.mLauncher, this, this.mDragObject, this.mOptions);
        }
        DropTarget.DragObject dragObject3 = this.mDragObject;
        dragObject3.dragSource = dragSource;
        dragObject3.dragInfo = itemInfo;
        dragObject3.originalDragInfo = new ItemInfo();
        this.mDragObject.originalDragInfo.copyFrom(itemInfo);
        if (point != null) {
            dragView.setDragVisualizeOffset(new Point(point));
        }
        if (rect != null) {
            dragView.setDragRegion(new Rect(rect));
        }
        this.mLauncher.getDragLayer().performHapticFeedback(0);
        dragView.show(this.mMotionDownX, this.mMotionDownY);
        this.mDistanceSinceScroll = 0;
        if (this.mIsDragDeferred) {
            this.mOptions.deferDragCondition.onDeferredDragStart();
        } else {
            startDeferredDrag();
        }
        int[] iArr = this.mLastTouch;
        int i7 = this.mMotionDownX;
        iArr[0] = i7;
        int i8 = this.mMotionDownY;
        iArr[1] = i8;
        handleMoveEvent(i7, i8);
        return dragView;
    }

    public DragView startDragTouchCentered(Bitmap bitmap, DragSource dragSource, ItemInfo itemInfo, Point point, Rect rect, float f, DragOptions dragOptions) {
        return startDrag(bitmap, (this.mMotionDownX - (bitmap.getWidth() / 2)) - 20, (this.mMotionDownY - (bitmap.getHeight() / 2)) - 20, dragSource, itemInfo, point, rect, f, dragOptions);
    }
}
