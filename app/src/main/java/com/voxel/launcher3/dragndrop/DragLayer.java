package com.voxel.launcher3.dragndrop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.voxel.launcher3.AppWidgetResizeFrame;
import com.voxel.launcher3.CellLayout;
import com.voxel.launcher3.Folder;
import com.voxel.launcher3.FolderIcon;
import com.voxel.launcher3.Insettable;
import com.voxel.launcher3.ItemInfo;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.LauncherAppWidgetHostView;
import com.voxel.launcher3.ShortcutAndWidgetContainer;
import com.voxel.launcher3.Utilities;
import com.voxel.launcher3.Workspace;
import com.ovacoda.devie.R;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public class DragLayer extends FrameLayout implements ViewGroup.OnHierarchyChangeListener {
    private View mAnchorView;
    private int mAnchorViewInitialScrollX;
    private int mChildCountOnLastUpdate;
    private TimeInterpolator mCubicEaseOutInterpolator;
    private AppWidgetResizeFrame mCurrentResizeFrame;
    private DragController mDragController;
    private ValueAnimator mDropAnim;
    private DragView mDropView;
    private ValueAnimator mFadeOutAnim;
    private Rect mHitRect;
    private boolean mHoverPointClosesFolder;
    private boolean mInScrollArea;
    private final Rect mInsets;
    private Launcher mLauncher;
    private Drawable mLeftHoverDrawable;
    private Drawable mLeftHoverDrawableActive;
    private View mOverlayView;
    private final ArrayList<AppWidgetResizeFrame> mResizeFrames;
    private Drawable mRightHoverDrawable;
    private Drawable mRightHoverDrawableActive;
    private boolean mShowPageHints;
    private int[] mTmpXY;
    private int mTopViewIndex;
    private TouchCompleteListener mTouchCompleteListener;
    private WindowInsets mWindowInsets;
    private int mXDown;
    private int mYDown;

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public boolean customPosition;
        public int x;
        public int y;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.customPosition = false;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public void setHeight(int i) {
            this.height = i;
        }

        public void setWidth(int i) {
            this.width = i;
        }

        public void setX(int i) {
            this.x = i;
        }

        public void setY(int i) {
            this.y = i;
        }
    }

    public interface TouchCompleteListener {
        void onTouchComplete();
    }

    public DragLayer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTmpXY = new int[2];
        this.mResizeFrames = new ArrayList<>();
        this.mDropAnim = null;
        this.mFadeOutAnim = null;
        this.mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
        this.mDropView = null;
        this.mAnchorViewInitialScrollX = 0;
        this.mAnchorView = null;
        this.mHoverPointClosesFolder = false;
        this.mHitRect = new Rect();
        this.mWindowInsets = null;
        this.mInsets = new Rect();
        this.mChildCountOnLastUpdate = -1;
        setMotionEventSplittingEnabled(false);
        setChildrenDrawingOrderEnabled(true);
        setOnHierarchyChangeListener(this);
        Resources resources = getResources();
        this.mLeftHoverDrawable = resources.getDrawable(R.drawable.page_hover_left);
        this.mRightHoverDrawable = resources.getDrawable(R.drawable.page_hover_right);
        this.mLeftHoverDrawableActive = resources.getDrawable(R.drawable.page_hover_left_active);
        this.mRightHoverDrawableActive = resources.getDrawable(R.drawable.page_hover_right_active);
    }

    private void drawPageHints(Canvas canvas) {
        if (this.mShowPageHints) {
            Workspace workspace = this.mLauncher.getWorkspace();
            int measuredWidth = getMeasuredWidth();
            Rect rect = new Rect();
            getDescendantRectRelativeToSelf(workspace.getChildAt(workspace.getChildCount() - 1), rect);
            rect.top = (int) (rect.top - workspace.getTranslationY());
            rect.bottom = (int) (rect.bottom - workspace.getTranslationY());
            int nextPage = workspace.getNextPage();
            boolean zIsLayoutRtl = isLayoutRtl();
            CellLayout cellLayout = (CellLayout) workspace.getChildAt(zIsLayoutRtl ? nextPage + 1 : nextPage - 1);
            CellLayout cellLayout2 = (CellLayout) workspace.getChildAt(zIsLayoutRtl ? nextPage - 1 : nextPage + 1);
            if (cellLayout != null && cellLayout.isDragTarget()) {
                Drawable drawable = (this.mInScrollArea && cellLayout.getIsDragOverlapping()) ? this.mLeftHoverDrawableActive : this.mLeftHoverDrawable;
                drawable.setBounds(0, rect.top, drawable.getIntrinsicWidth(), rect.bottom);
                drawable.draw(canvas);
            }
            if (cellLayout2 == null || !cellLayout2.isDragTarget()) {
                return;
            }
            Drawable drawable2 = (this.mInScrollArea && cellLayout2.getIsDragOverlapping()) ? this.mRightHoverDrawableActive : this.mRightHoverDrawable;
            drawable2.setBounds(measuredWidth - drawable2.getIntrinsicWidth(), rect.top, measuredWidth, rect.bottom);
            drawable2.draw(canvas);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fadeOutDragView() {
        this.mFadeOutAnim = new ValueAnimator();
        this.mFadeOutAnim.setDuration(150L);
        this.mFadeOutAnim.setFloatValues(0.0f, 1.0f);
        this.mFadeOutAnim.removeAllUpdateListeners();
        this.mFadeOutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragLayer$LVtiY9rMhwTUprYLTopEFA9Ys6M
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DragLayer.this.mDropView.setAlpha(1.0f - ((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mFadeOutAnim.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.dragndrop.DragLayer.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (DragLayer.this.mDropView != null) {
                    DragLayer.this.mDragController.onDeferredEndDrag(DragLayer.this.mDropView);
                }
                DragLayer.this.mDropView = null;
                DragLayer.this.invalidate();
            }
        });
        this.mFadeOutAnim.start();
    }

    private boolean handleTouchDown(MotionEvent motionEvent, boolean z) {
        Rect rect = new Rect();
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        for (AppWidgetResizeFrame appWidgetResizeFrame : this.mResizeFrames) {
            appWidgetResizeFrame.getHitRect(rect);
            if (rect.contains(x, y) && appWidgetResizeFrame.beginResizeIfPointInRegion(x - appWidgetResizeFrame.getLeft(), y - appWidgetResizeFrame.getTop())) {
                this.mCurrentResizeFrame = appWidgetResizeFrame;
                this.mXDown = x;
                this.mYDown = y;
                requestDisallowInterceptTouchEvent(true);
                return true;
            }
        }
        return false;
    }

    private boolean isEventOverFolder(Folder folder, MotionEvent motionEvent) {
        getDescendantRectRelativeToSelf(folder, this.mHitRect);
        return this.mHitRect.contains((int) motionEvent.getX(), (int) motionEvent.getY());
    }

    private boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }

    public static /* synthetic */ void lambda$animateView$1(DragLayer dragLayer, DragView dragView, Interpolator interpolator, Interpolator interpolator2, float f, float f2, float f3, float f4, float f5, float f6, float f7, Rect rect, Rect rect2, ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        int measuredWidth = dragView.getMeasuredWidth();
        int measuredHeight = dragView.getMeasuredHeight();
        float interpolation = interpolator == null ? fFloatValue : interpolator.getInterpolation(fFloatValue);
        float interpolation2 = interpolator2 == null ? fFloatValue : interpolator2.getInterpolation(fFloatValue);
        float f8 = f * f2;
        float f9 = f3 * f2;
        float f10 = 1.0f - fFloatValue;
        float f11 = (f4 * fFloatValue) + (f8 * f10);
        float f12 = (fFloatValue * f5) + (f10 * f9);
        float f13 = (f6 * interpolation) + ((1.0f - interpolation) * f7);
        float f14 = rect.left + (((f8 - 1.0f) * measuredWidth) / 2.0f);
        float f15 = rect.top + (((f9 - 1.0f) * measuredHeight) / 2.0f);
        int iRound = (int) (f14 + Math.round((rect2.left - f14) * interpolation2));
        int iRound2 = (int) (f15 + Math.round((rect2.top - f15) * interpolation2));
        View view = dragLayer.mAnchorView;
        int scrollX = (iRound - dragLayer.mDropView.getScrollX()) + (view == null ? 0 : (int) (view.getScaleX() * (dragLayer.mAnchorViewInitialScrollX - dragLayer.mAnchorView.getScrollX())));
        int scrollY = iRound2 - dragLayer.mDropView.getScrollY();
        dragLayer.mDropView.setTranslationX(scrollX);
        dragLayer.mDropView.setTranslationY(scrollY);
        dragLayer.mDropView.setScaleX(f11);
        dragLayer.mDropView.setScaleY(f12);
        dragLayer.mDropView.setAlpha(f13);
    }

    static /* synthetic */ void lambda$animateViewIntoPosition$0(View view, Runnable runnable) {
        view.setVisibility(0);
        if (runnable != null) {
            runnable.run();
        }
    }

    private void sendTapOutsideFolderAccessibilityEvent(boolean z) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        if (accessibilityManager.isEnabled()) {
            int i = z ? R.string.folder_tap_to_rename : R.string.folder_tap_to_close;
            AccessibilityEvent accessibilityEventObtain = AccessibilityEvent.obtain(8);
            onInitializeAccessibilityEvent(accessibilityEventObtain);
            accessibilityEventObtain.getText().add(getContext().getString(i));
            accessibilityManager.sendAccessibilityEvent(accessibilityEventObtain);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void setInsets(View view, Rect rect, Rect rect2) {
        if (view instanceof Insettable) {
            ((Insettable) view).setInsets(rect);
            return;
        }
        if (view.getFitsSystemWindows()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.topMargin += rect.top - rect2.top;
            layoutParams.leftMargin += rect.left - rect2.left;
            layoutParams.rightMargin += rect.right - rect2.right;
            layoutParams.bottomMargin += rect.bottom - rect2.bottom;
            view.setLayoutParams(layoutParams);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @TargetApi(20)
    private void setInsets(View view, WindowInsets windowInsets) {
        if (view instanceof Insettable) {
            ((Insettable) view).setInsets(windowInsets);
            return;
        }
        if (view.getFitsSystemWindows()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.topMargin = windowInsets.getSystemWindowInsetTop();
            layoutParams.leftMargin = windowInsets.getSystemWindowInsetLeft();
            layoutParams.rightMargin = windowInsets.getSystemWindowInsetRight();
            layoutParams.bottomMargin += windowInsets.getSystemWindowInsetBottom();
            view.setLayoutParams(layoutParams);
        }
    }

    private void updateChildIndices() {
        this.mTopViewIndex = -1;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof DragView) {
                this.mTopViewIndex = i;
            }
        }
        this.mChildCountOnLastUpdate = childCount;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addChildrenForAccessibility(ArrayList<View> arrayList) {
        Folder openFolder = this.mLauncher.getOpenFolder();
        if (openFolder != null) {
            arrayList.add(openFolder);
        } else {
            super.addChildrenForAccessibility(arrayList);
        }
    }

    public void addResizeFrame(ItemInfo itemInfo, LauncherAppWidgetHostView launcherAppWidgetHostView, CellLayout cellLayout) {
        AppWidgetResizeFrame appWidgetResizeFrame = new AppWidgetResizeFrame(getContext(), launcherAppWidgetHostView, cellLayout, this);
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        layoutParams.customPosition = true;
        addView(appWidgetResizeFrame, layoutParams);
        this.mResizeFrames.add(appWidgetResizeFrame);
        appWidgetResizeFrame.snapToWidget(false);
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
        WindowInsets windowInsets = this.mWindowInsets;
        if (windowInsets != null) {
            setInsets(view, windowInsets);
        } else {
            setInsets(view, this.mInsets, new Rect());
        }
    }

    public void animateView(DragView dragView, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, int i, TimeInterpolator timeInterpolator, final Runnable runnable, final int i2, View view) {
        ValueAnimator valueAnimator = this.mDropAnim;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator valueAnimator2 = this.mFadeOutAnim;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
        }
        this.mDropView = dragView;
        this.mDropView.cancelAnimation();
        if (view != null) {
            this.mAnchorViewInitialScrollX = view.getScrollX();
        }
        this.mAnchorView = view;
        this.mDropAnim = new ValueAnimator();
        this.mDropAnim.setInterpolator(timeInterpolator);
        this.mDropAnim.setDuration(i);
        this.mDropAnim.setFloatValues(0.0f, 1.0f);
        this.mDropAnim.addUpdateListener(animatorUpdateListener);
        this.mDropAnim.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.dragndrop.DragLayer.1
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
                switch (i2) {
                    case 0:
                        DragLayer.this.clearAnimatedView();
                        break;
                    case 1:
                        DragLayer.this.fadeOutDragView();
                        break;
                }
            }
        });
        this.mDropAnim.start();
    }

    public void animateView(final DragView dragView, final Rect rect, final Rect rect2, final float f, final float f2, final float f3, final float f4, final float f5, int i, final Interpolator interpolator, final Interpolator interpolator2, Runnable runnable, int i2, View view) {
        int iMax;
        float fSqrt = (float) Math.sqrt(Math.pow(rect2.left - rect.left, 2.0d) + Math.pow(rect2.top - rect.top, 2.0d));
        Resources resources = getResources();
        float integer = resources.getInteger(R.integer.config_dropAnimMaxDist);
        if (i < 0) {
            int integer2 = resources.getInteger(R.integer.config_dropAnimMaxDuration);
            if (fSqrt < integer) {
                integer2 = (int) (integer2 * this.mCubicEaseOutInterpolator.getInterpolation(fSqrt / integer));
            }
            iMax = Math.max(integer2, resources.getInteger(R.integer.config_dropAnimMinDuration));
        } else {
            iMax = i;
        }
        TimeInterpolator timeInterpolator = (interpolator2 == null || interpolator == null) ? this.mCubicEaseOutInterpolator : null;
        final float alpha = dragView.getAlpha();
        final float scaleX = dragView.getScaleX();
        animateView(dragView, new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragLayer$vCIBl0H69qXpHIy1-zD7UFR8KHY
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                DragLayer.lambda$animateView$1(DragLayer.this, dragView, interpolator2, interpolator, f2, scaleX, f3, f4, f5, f, alpha, rect, rect2, valueAnimator);
            }
        }, iMax, timeInterpolator, runnable, i2, view);
    }

    public void animateViewIntoPosition(DragView dragView, int i, int i2, int i3, int i4, float f, float f2, float f3, float f4, float f5, Runnable runnable, int i5, int i6, View view) {
        animateView(dragView, new Rect(i, i2, dragView.getMeasuredWidth() + i, dragView.getMeasuredHeight() + i2), new Rect(i3, i4, dragView.getMeasuredWidth() + i3, dragView.getMeasuredHeight() + i4), f, f2, f3, f4, f5, i6, null, null, runnable, i5, view);
    }

    public void animateViewIntoPosition(DragView dragView, final View view, int i, final Runnable runnable, View view2) {
        int iRound;
        float f;
        int iRound2;
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = (ShortcutAndWidgetContainer) view.getParent();
        CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view.getLayoutParams();
        shortcutAndWidgetContainer.measureChild(view);
        Rect rect = new Rect();
        getViewRectRelativeToSelf(dragView, rect);
        float scaleX = view.getScaleX();
        float f2 = 1.0f - scaleX;
        int[] iArr = {layoutParams.x + ((int) ((view.getMeasuredWidth() * f2) / 2.0f)), layoutParams.y + ((int) ((view.getMeasuredHeight() * f2) / 2.0f))};
        float descendantCoordRelativeToSelf = getDescendantCoordRelativeToSelf((View) view.getParent(), iArr) * scaleX;
        int i2 = iArr[0];
        int i3 = iArr[1];
        if (view instanceof TextView) {
            float intrinsicIconScaleFactor = descendantCoordRelativeToSelf / dragView.getIntrinsicIconScaleFactor();
            iRound = (int) ((i3 + Math.round(((TextView) view).getPaddingTop() * intrinsicIconScaleFactor)) - ((dragView.getMeasuredHeight() * (1.0f - intrinsicIconScaleFactor)) / 2.0f));
            if (dragView.getDragVisualizeOffset() != null) {
                iRound -= Math.round(dragView.getDragVisualizeOffset().y * intrinsicIconScaleFactor);
            }
            iRound2 = i2 - ((dragView.getMeasuredWidth() - Math.round(descendantCoordRelativeToSelf * view.getMeasuredWidth())) / 2);
            f = intrinsicIconScaleFactor;
        } else if (view instanceof FolderIcon) {
            iRound = (int) (((int) ((i3 + Math.round((view.getPaddingTop() - dragView.getDragRegionTop()) * descendantCoordRelativeToSelf)) - ((descendantCoordRelativeToSelf * 2.0f) / 2.0f))) - (((1.0f - descendantCoordRelativeToSelf) * dragView.getMeasuredHeight()) / 2.0f));
            f = descendantCoordRelativeToSelf;
            iRound2 = i2 - ((dragView.getMeasuredWidth() - Math.round(view.getMeasuredWidth() * descendantCoordRelativeToSelf)) / 2);
        } else {
            iRound = i3 - (Math.round((dragView.getHeight() - view.getMeasuredHeight()) * descendantCoordRelativeToSelf) / 2);
            f = descendantCoordRelativeToSelf;
            iRound2 = i2 - (Math.round((dragView.getMeasuredWidth() - view.getMeasuredWidth()) * descendantCoordRelativeToSelf) / 2);
        }
        int i4 = rect.left;
        int i5 = rect.top;
        view.setVisibility(4);
        animateViewIntoPosition(dragView, i4, i5, iRound2, iRound, 1.0f, 1.0f, 1.0f, f, f, new Runnable() { // from class: com.voxel.launcher3.dragndrop.-$$Lambda$DragLayer$0sc_jEnYjsKN-A6XxSytQE2z_6o
            @Override // java.lang.Runnable
            public final void run() {
                DragLayer.lambda$animateViewIntoPosition$0(view, runnable);
            }
        }, 0, i, view2);
    }

    public void animateViewIntoPosition(DragView dragView, View view, Runnable runnable, View view2) {
        animateViewIntoPosition(dragView, view, -1, runnable, view2);
    }

    public void animateViewIntoPosition(DragView dragView, int[] iArr, float f, float f2, float f3, int i, Runnable runnable, int i2) {
        Rect rect = new Rect();
        getViewRectRelativeToSelf(dragView, rect);
        animateViewIntoPosition(dragView, rect.left, rect.top, iArr[0], iArr[1], f, 1.0f, 1.0f, f2, f3, runnable, i, i2, null);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void bringChildToFront(View view) {
        super.bringChildToFront(view);
        View view2 = this.mOverlayView;
        if (view != view2 && view2 != null) {
            view2.bringToFront();
        }
        updateChildIndices();
    }

    public boolean clearAllResizeFrames() {
        if (this.mResizeFrames.size() <= 0) {
            return false;
        }
        for (AppWidgetResizeFrame appWidgetResizeFrame : this.mResizeFrames) {
            appWidgetResizeFrame.commitResize();
            removeView(appWidgetResizeFrame);
        }
        this.mResizeFrames.clear();
        return true;
    }

    public void clearAnimatedView() {
        ValueAnimator valueAnimator = this.mDropAnim;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        DragView dragView = this.mDropView;
        if (dragView != null) {
            this.mDragController.onDeferredEndDrag(dragView);
        }
        this.mDropView = null;
        invalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return this.mDragController.dispatchKeyEvent(keyEvent) || super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        return this.mDragController.dispatchUnhandledMove(view, i);
    }

    @Override // android.view.ViewGroup
    protected boolean drawChild(Canvas canvas, View view, long j) {
        boolean zDrawChild = super.drawChild(canvas, view, j);
        if (indexOfChild(view) == 0) {
            drawPageHints(canvas);
        }
        return zDrawChild;
    }

    @Override // android.view.View
    protected boolean fitSystemWindows(Rect rect) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            setInsets(getChildAt(i), rect, this.mInsets);
        }
        this.mInsets.set(rect);
        return true;
    }

    public View getAnimatedView() {
        return this.mDropView;
    }

    @Override // android.view.ViewGroup
    protected int getChildDrawingOrder(int i, int i2) {
        if (this.mChildCountOnLastUpdate != i) {
            updateChildIndices();
        }
        int i3 = this.mTopViewIndex;
        if (i3 == -1) {
            return i2;
        }
        if (i2 == i - 1) {
            return i3;
        }
        return i2 < i3 ? i2 : i2 + 1;
    }

    public float getDescendantCoordRelativeToSelf(View view, int[] iArr) {
        return getDescendantCoordRelativeToSelf(view, iArr, false);
    }

    public float getDescendantCoordRelativeToSelf(View view, int[] iArr, boolean z) {
        return Utilities.getDescendantCoordRelativeToParent(view, this, iArr, z);
    }

    public float getDescendantRectRelativeToSelf(View view, Rect rect) {
        int[] iArr = this.mTmpXY;
        iArr[0] = 0;
        iArr[1] = 0;
        float descendantCoordRelativeToSelf = getDescendantCoordRelativeToSelf(view, iArr);
        int[] iArr2 = this.mTmpXY;
        rect.set(iArr2[0], iArr2[1], (int) (iArr2[0] + (view.getMeasuredWidth() * descendantCoordRelativeToSelf)), (int) (this.mTmpXY[1] + (view.getMeasuredHeight() * descendantCoordRelativeToSelf)));
        return descendantCoordRelativeToSelf;
    }

    public Rect getInsets() {
        return this.mInsets;
    }

    public float getLocationInDragLayer(View view, int[] iArr) {
        iArr[0] = 0;
        iArr[1] = 0;
        return getDescendantCoordRelativeToSelf(view, iArr);
    }

    public void getViewRectRelativeToSelf(View view, Rect rect) {
        int[] iArr = new int[2];
        getLocationInWindow(iArr);
        int i = iArr[0];
        int i2 = iArr[1];
        view.getLocationInWindow(iArr);
        int i3 = iArr[0] - i;
        int i4 = iArr[1] - i2;
        rect.set(i3, i4, view.getMeasuredWidth() + i3, view.getMeasuredHeight() + i4);
    }

    public void hidePageHints() {
        this.mShowPageHints = false;
        invalidate();
    }

    public float mapCoordInSelfToDescendent(View view, int[] iArr) {
        return Utilities.mapCoordInSelfToDescendent(view, this, iArr);
    }

    @Override // android.view.View
    @TargetApi(20)
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            setInsets(getChildAt(i), windowInsets);
        }
        this.mWindowInsets = windowInsets;
        return windowInsets.consumeSystemWindowInsets();
    }

    @Override // android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewAdded(View view, View view2) {
        View view3 = this.mOverlayView;
        if (view3 != null) {
            view3.bringToFront();
        }
        updateChildIndices();
    }

    @Override // android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewRemoved(View view, View view2) {
        updateChildIndices();
    }

    void onEnterScrollArea() {
        this.mInScrollArea = true;
        invalidate();
    }

    void onExitScrollArea() {
        this.mInScrollArea = false;
        invalidate();
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptHoverEvent(MotionEvent motionEvent) {
        Folder openFolder;
        Launcher launcher = this.mLauncher;
        if (launcher != null && launcher.getWorkspace() != null && (openFolder = this.mLauncher.getOpenFolder()) != null && ((AccessibilityManager) getContext().getSystemService("accessibility")).isTouchExplorationEnabled()) {
            int action = motionEvent.getAction();
            if (action == 7) {
                boolean zIsEventOverFolder = isEventOverFolder(openFolder, motionEvent);
                if (!zIsEventOverFolder && !this.mHoverPointClosesFolder) {
                    sendTapOutsideFolderAccessibilityEvent(openFolder.isEditingName());
                    this.mHoverPointClosesFolder = true;
                    return true;
                }
                if (!zIsEventOverFolder) {
                    return true;
                }
                this.mHoverPointClosesFolder = false;
            } else if (action == 9) {
                if (!isEventOverFolder(openFolder, motionEvent)) {
                    sendTapOutsideFolderAccessibilityEvent(openFolder.isEditingName());
                    this.mHoverPointClosesFolder = true;
                    return true;
                }
                this.mHoverPointClosesFolder = false;
            }
        }
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0) {
            if (handleTouchDown(motionEvent, true)) {
                return true;
            }
        } else if (action == 1 || action == 3) {
            TouchCompleteListener touchCompleteListener = this.mTouchCompleteListener;
            if (touchCompleteListener != null) {
                touchCompleteListener.onTouchComplete();
            }
            this.mTouchCompleteListener = null;
        }
        clearAllResizeFrames();
        return this.mDragController.onInterceptTouchEvent(motionEvent);
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
            if (layoutParams instanceof LayoutParams) {
                LayoutParams layoutParams2 = (LayoutParams) layoutParams;
                if (layoutParams2.customPosition) {
                    childAt.layout(layoutParams2.x, layoutParams2.y, layoutParams2.x + layoutParams2.width, layoutParams2.y + layoutParams2.height);
                }
            }
        }
    }

    @Override // android.view.ViewGroup
    public boolean onRequestSendAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        Folder openFolder = this.mLauncher.getOpenFolder();
        if (openFolder == null || view == openFolder) {
            return super.onRequestSendAccessibilityEvent(view, accessibilityEvent);
        }
        return false;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        boolean z = false;
        if (action == 0) {
            if (handleTouchDown(motionEvent, false)) {
                return true;
            }
        } else if (action == 1 || action == 3) {
            TouchCompleteListener touchCompleteListener = this.mTouchCompleteListener;
            if (touchCompleteListener != null) {
                touchCompleteListener.onTouchComplete();
            }
            this.mTouchCompleteListener = null;
        }
        AppWidgetResizeFrame appWidgetResizeFrame = this.mCurrentResizeFrame;
        if (appWidgetResizeFrame != null) {
            switch (action) {
                case 1:
                case 3:
                    appWidgetResizeFrame.visualizeResizeForDelta(x - this.mXDown, y - this.mYDown);
                    this.mCurrentResizeFrame.onTouchUp();
                    this.mCurrentResizeFrame = null;
                    break;
                case 2:
                    appWidgetResizeFrame.visualizeResizeForDelta(x - this.mXDown, y - this.mYDown);
                    break;
            }
            z = true;
        }
        if (z) {
            return true;
        }
        return this.mDragController.onTouchEvent(motionEvent);
    }

    public void setTouchCompleteListener(TouchCompleteListener touchCompleteListener) {
        this.mTouchCompleteListener = touchCompleteListener;
    }

    public void setup(Launcher launcher, DragController dragController) {
        this.mLauncher = launcher;
        this.mDragController = dragController;
    }

    public void showOverlayView(View view) {
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        this.mOverlayView = view;
        addView(view, layoutParams);
        this.mOverlayView.bringToFront();
    }

    public void showPageHints() {
        this.mShowPageHints = true;
        invalidate();
    }
}
