package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.ovacoda.devie.R;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes.dex */
public abstract class PagedView extends ViewGroup implements ViewGroup.OnHierarchyChangeListener {
    private int DELETE_SLIDE_IN_SIDE_PAGE_DURATION;
    private int DRAG_TO_DELETE_FADE_OUT_DURATION;
    private int NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT;
    private int REORDERING_DROP_REPOSITION_DURATION;
    protected int REORDERING_REORDER_REPOSITION_DURATION;
    private int REORDERING_SIDE_PAGE_HOVER_TIMEOUT;
    protected int REORDERING_ZOOM_IN_OUT_DURATION;
    protected int mActivePointerId;
    protected boolean mAllowOverScroll;
    private boolean mCancelTap;
    protected int mCellCountX;
    protected int mCellCountY;
    protected boolean mCenterPagesVertically;
    protected int mChildCountOnLastLayout;
    protected int mCurrentPage;
    private Interpolator mDefaultInterpolator;
    private boolean mDeferringForDelete;
    private RemoveDropTarget mDeleteDropTarget;
    protected float mDensity;
    private float mDownMotionX;
    private float mDownMotionY;
    private float mDownScrollX;
    protected View mDragView;
    private float mDragViewBaselineLeft;
    protected boolean mFadeInAdjacentScreens;
    protected boolean mFirstLayout;
    protected int mFlingThresholdVelocity;
    protected boolean mForceDrawAllChildrenNextFrame;
    protected boolean mForceScreenScrolled;
    private boolean mFreeScroll;
    private int mFreeScrollMaxScrollX;
    private int mFreeScrollMinScrollX;
    protected final Rect mInsets;
    protected boolean mIsPageMoving;
    private boolean mIsReordering;
    protected final boolean mIsRtl;
    protected float mLastMotionX;
    protected float mLastMotionXRemainder;
    protected float mLastMotionY;
    private int mLastScreenCenter;
    protected View.OnLongClickListener mLongClickListener;
    protected int mMaxScrollX;
    private int mMaximumVelocity;
    protected int mMinFlingVelocity;
    private float mMinScale;
    protected int mMinSnapVelocity;
    protected int mNextPage;
    private int mNormalChildHeight;
    private View.OnTouchListener mOnInterceptTouchListener;
    protected int mOverScrollX;
    PageIndicator mPageIndicator;
    int mPageIndicatorViewId;
    private PageInteractionListener mPageInteractionListener;
    protected int mPageLayoutHeightGap;
    protected int mPageLayoutWidthGap;
    private int[] mPageScrolls;
    int mPageSpacing;
    private PageSwitchListener mPageSwitchListener;
    private float mParentDownMotionX;
    private float mParentDownMotionY;
    private int mPostReorderingPreZoomInRemainingAnimationCount;
    private Runnable mPostReorderingPreZoomInRunnable;
    private boolean mReorderingStarted;
    protected int mRestorePage;
    protected LauncherScroller mScroller;
    private int mSidePageHoverIndex;
    private Runnable mSidePageHoverRunnable;
    protected float mSmoothingTime;
    protected int[] mTempVisiblePagesRange;
    protected float mTotalMotionX;
    protected int mTouchSlop;
    protected int mTouchState;
    protected float mTouchX;
    protected int mUnboundedScrollX;
    private boolean mUseMinScale;
    protected boolean mUsePagingTouchSlop;
    private VelocityTracker mVelocityTracker;
    private Rect mViewport;
    private boolean mWasHoveringOverDelete;
    private static final Matrix sTmpInvMatrix = new Matrix();
    private static final float[] sTmpPoint = new float[2];
    private static final int[] sTmpIntPoint = new int[2];
    private static final Rect sTmpRect = new Rect();
    private static final Rect sAltTmpRect = new Rect();

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public boolean isFullScreenPage;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.isFullScreenPage = false;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.isFullScreenPage = false;
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
            this.isFullScreenPage = false;
        }
    }

    public interface PageInteractionListener {
        void onPageBeginMoving();

        void onPageEndMoving();

        void onScrollInteractionBegin();

        void onScrollInteractionEnd();
    }

    public interface PageSwitchListener {
        void onPageSwitch(View view, int i);
    }

    private static class ScrollInterpolator implements Interpolator {
        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float f2 = f - 1.0f;
            return (f2 * f2 * f2 * f2 * f2) + 1.0f;
        }
    }

    public PagedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public PagedView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFreeScroll = false;
        this.mFreeScrollMinScrollX = -1;
        this.mFreeScrollMaxScrollX = -1;
        this.mFirstLayout = true;
        this.mRestorePage = -1001;
        this.mNextPage = -1;
        this.mPageSpacing = 0;
        this.mLastScreenCenter = -1;
        this.mTouchState = 0;
        this.mForceScreenScrolled = false;
        this.mCellCountX = 0;
        this.mCellCountY = 0;
        this.mAllowOverScroll = true;
        this.mTempVisiblePagesRange = new int[2];
        this.mActivePointerId = -1;
        this.mFadeInAdjacentScreens = false;
        this.mUsePagingTouchSlop = true;
        this.mIsPageMoving = false;
        this.mViewport = new Rect();
        this.REORDERING_DROP_REPOSITION_DURATION = 200;
        this.REORDERING_REORDER_REPOSITION_DURATION = 300;
        this.REORDERING_ZOOM_IN_OUT_DURATION = 250;
        this.REORDERING_SIDE_PAGE_HOVER_TIMEOUT = 80;
        this.mMinScale = 1.0f;
        this.mUseMinScale = false;
        this.mSidePageHoverIndex = -1;
        this.mReorderingStarted = false;
        this.NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT = 2;
        this.mDeferringForDelete = false;
        this.DELETE_SLIDE_IN_SIDE_PAGE_DURATION = 250;
        this.DRAG_TO_DELETE_FADE_OUT_DURATION = 350;
        this.mInsets = new Rect();
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.PagedView, i, 0);
        this.mPageLayoutWidthGap = typedArrayObtainStyledAttributes.getDimensionPixelSize(2, 0);
        this.mPageLayoutHeightGap = typedArrayObtainStyledAttributes.getDimensionPixelSize(1, 0);
        this.mPageIndicatorViewId = typedArrayObtainStyledAttributes.getResourceId(0, -1);
        typedArrayObtainStyledAttributes.recycle();
        setHapticFeedbackEnabled(false);
        this.mIsRtl = Utilities.isRtl(getResources());
        init();
    }

    private void abortScrollerAnimation(boolean z) {
        this.mScroller.abortAnimation();
        if (z) {
            this.mNextPage = -1;
        }
    }

    private void acquireVelocityTrackerAndAddMovement(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
    }

    private void animateDragViewToOriginalPosition() {
        if (this.mDragView != null) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(this.REORDERING_DROP_REPOSITION_DURATION);
            animatorSet.playTogether(ObjectAnimator.ofFloat(this.mDragView, "translationX", 0.0f), ObjectAnimator.ofFloat(this.mDragView, "translationY", 0.0f), ObjectAnimator.ofFloat(this.mDragView, "scaleX", 1.0f), ObjectAnimator.ofFloat(this.mDragView, "scaleY", 1.0f));
            animatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.PagedView.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    PagedView.this.onPostReorderingAnimationCompleted();
                }
            });
            animatorSet.start();
        }
    }

    private Runnable createPostDeleteAnimationRunnable(final View view) {
        return new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$PagedView$G7tbC_IbD6gifZ2dH_EthCuMbeE
            @Override // java.lang.Runnable
            public final void run() {
                PagedView.this.lambda$createPostDeleteAnimationRunnable$4( view);
            }
        };
    }

    private float distanceInfluenceForSnapDuration(float f) {
        double d = f - 0.5f;
        Double.isNaN(d);
        return (float) Math.sin((float) (d * 0.4712389167638204d));
    }

    private void forceFinishScroller() {
        this.mScroller.forceFinished(true);
        this.mNextPage = -1;
        if (this.mTouchState == 0) {
            pageEndMoving();
        }
    }

    private boolean isHoveringOverDeleteDropTarget(int i, int i2) {
        RemoveDropTarget removeDropTarget;
        if (getChildCount() <= 1 || (removeDropTarget = this.mDeleteDropTarget) == null || removeDropTarget.getVisibility() != 0) {
            return false;
        }
        sAltTmpRect.set(0, 0, 0, 0);
        View view = (View) this.mDeleteDropTarget.getParent();
        if (view != null) {
            view.getGlobalVisibleRect(sAltTmpRect);
        }
        this.mDeleteDropTarget.getGlobalVisibleRect(sTmpRect);
        sTmpRect.offset(-sAltTmpRect.left, -sAltTmpRect.top);
        return sTmpRect.contains(i, i2);
    }

    private boolean isTouchPointInViewportWithBuffer(int i, int i2) {
        sTmpRect.set(this.mViewport.left - (this.mViewport.width() / 2), this.mViewport.top, this.mViewport.right + (this.mViewport.width() / 2), this.mViewport.bottom);
        return sTmpRect.contains(i, i2);
    }

    public void lambda$createPostDeleteAnimationRunnable$4(View view) {
        int childOffset;
        int viewportOffsetX;
        int iIndexOfChild = this.indexOfChild(view);
        this.getFreeScrollPageRange(this.mTempVisiblePagesRange);
        int[] iArr = this.mTempVisiblePagesRange;
        boolean z = iArr[0] == iArr[1];
        boolean z2 = z || iIndexOfChild > this.mTempVisiblePagesRange[0];
        if (z2) {
            this.snapToPageImmediately(iIndexOfChild - 1);
        }
        int i = z ? 0 : this.mTempVisiblePagesRange[0];
        int iMin = Math.min(this.mTempVisiblePagesRange[1], this.getPageCount() - 1);
        if (!z2) {
            i = iIndexOfChild + 1;
        }
        if (z2) {
            iMin = iIndexOfChild - 1;
        }
        ArrayList arrayList = new ArrayList();
        while (i <= iMin) {
            View childAt = this.getChildAt(i);
            if (z2) {
                childOffset = i == 0 ? ((this.getViewportOffsetX() + this.getChildOffset(i)) - this.getChildWidth(i)) - this.mPageSpacing : this.getViewportOffsetX() + this.getChildOffset(i - 1);
                viewportOffsetX = this.getViewportOffsetX() + this.getChildOffset(i);
            } else {
                childOffset = this.getChildOffset(i) - this.getChildOffset(i - 1);
                viewportOffsetX = 0;
            }
            AnimatorSet animatorSet = (AnimatorSet) childAt.getTag();
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            childAt.setAlpha(Math.max(childAt.getAlpha(), 0.01f));
            childAt.setTranslationX(childOffset - viewportOffsetX);
            AnimatorSet animatorSet2 = new AnimatorSet();
            animatorSet2.playTogether(ObjectAnimator.ofFloat(childAt, "translationX", 0.0f), ObjectAnimator.ofFloat(childAt, "alpha", 1.0f));
            arrayList.add(animatorSet2);
            childAt.setTag(R.id.paged_view_anim_tag, animatorSet2);
            i++;
        }
        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.playTogether(arrayList);
        animatorSet3.setDuration(this.DELETE_SLIDE_IN_SIDE_PAGE_DURATION);
        animatorSet3.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.PagedView.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                PagedView.this.mDeferringForDelete = false;
                PagedView.this.onEndReordering();
                PagedView.this.onRemoveViewAnimationCompleted();
            }
        });
        animatorSet3.start();
        this.getPageIndicator().removeMarker(this.indexOfChild(view), false);
        this.removeView(view);
        this.onRemoveView(view, true);
    }

    public void lambda$endReordering$3(Runnable runnable) {
        runnable.run();
        this.enableFreeScroll();
    }

    public void lambda$onEndReordering$1() {
        this.mDeleteDropTarget.setVisibility(8);
        this.mDeleteDropTarget.resetHoverColor();
    }

    public void lambda$onTouchEvent$0(int i, int i2) {
        this.snapToPage(i);
        int i3 = i2 < i ? -1 : 1;
        int i4 = i2 > i ? i2 - 1 : i;
        for (int i5 = i2 < i ? i2 + 1 : i; i5 <= i4; i5++) {
            View childAt = this.getChildAt(i5);
            int viewportOffsetX = this.getViewportOffsetX() + this.getChildOffset(i5);
            int viewportOffsetX2 = this.getViewportOffsetX() + this.getChildOffset(i5 + i3);
            AnimatorSet animatorSet = (AnimatorSet) childAt.getTag(R.id.paged_view_anim_tag);
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            childAt.setTranslationX(viewportOffsetX - viewportOffsetX2);
            AnimatorSet animatorSet2 = new AnimatorSet();
            animatorSet2.setDuration(this.REORDERING_REORDER_REPOSITION_DURATION);
            animatorSet2.playTogether(ObjectAnimator.ofFloat(childAt, "translationX", 0.0f));
            animatorSet2.start();
            childAt.setTag(animatorSet2);
        }
        this.removeView(this.mDragView);
        this.onRemoveView(this.mDragView, false);
        this.addView(this.mDragView, i);
        this.onAddView(this.mDragView, i);
        this.mSidePageHoverIndex = -1;
        PageIndicator pageIndicator = this.mPageIndicator;
        if (pageIndicator != null) {
            pageIndicator.setActiveMarker(this.getNextPage());
        }
    }

    private float[] mapPointFromParentToView(View view, float f, float f2) {
        sTmpPoint[0] = f - view.getLeft();
        sTmpPoint[1] = f2 - view.getTop();
        view.getMatrix().invert(sTmpInvMatrix);
        sTmpInvMatrix.mapPoints(sTmpPoint);
        return sTmpPoint;
    }

    private float[] mapPointFromViewToParent(View view, float f, float f2) {
        float[] fArr = sTmpPoint;
        fArr[0] = f;
        fArr[1] = f2;
        view.getMatrix().mapPoints(sTmpPoint);
        float[] fArr2 = sTmpPoint;
        fArr2[0] = fArr2[0] + view.getLeft();
        float[] fArr3 = sTmpPoint;
        fArr3[1] = fArr3[1] + view.getTop();
        return sTmpPoint;
    }

    private void onDropToDelete() {
        View view = this.mDragView;
        ArrayList arrayList = new ArrayList();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator(2.0f));
        animatorSet.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 0.0f), ObjectAnimator.ofFloat(view, "scaleY", 0.0f));
        arrayList.add(animatorSet);
        AnimatorSet animatorSet2 = new AnimatorSet();
        animatorSet2.setInterpolator(new LinearInterpolator());
        animatorSet2.playTogether(ObjectAnimator.ofFloat(view, "alpha", 0.0f));
        arrayList.add(animatorSet2);
        final Runnable runnableCreatePostDeleteAnimationRunnable = createPostDeleteAnimationRunnable(view);
        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.playTogether(arrayList);
        animatorSet3.setDuration(this.DRAG_TO_DELETE_FADE_OUT_DURATION);
        animatorSet3.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.PagedView.4
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                runnableCreatePostDeleteAnimationRunnable.run();
            }
        });
        animatorSet3.start();
        this.mDeferringForDelete = true;
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int action = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(action) == this.mActivePointerId) {
            int i = action == 0 ? 1 : 0;
            float x = motionEvent.getX(i);
            this.mDownMotionX = x;
            this.mLastMotionX = x;
            this.mLastMotionY = motionEvent.getY(i);
            this.mLastMotionXRemainder = 0.0f;
            this.mActivePointerId = motionEvent.getPointerId(i);
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    private float overScrollInfluenceCurve(float f) {
        float f2 = f - 1.0f;
        return (f2 * f2 * f2) + 1.0f;
    }

    private void releaseVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void removeMarkerForView(int i) {
        if (this.mPageIndicator == null || isReordering(false)) {
            return;
        }
        this.mPageIndicator.removeMarker(i, true);
    }

    private void resetTouchState() {
        releaseVelocityTracker();
        endReordering();
        this.mCancelTap = false;
        this.mTouchState = 0;
        this.mActivePointerId = -1;
    }

    private void sendScrollAccessibilityEvent() {
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
            int i = 4096;
            AccessibilityEvent accessibilityEventObtain = AccessibilityEvent.obtain(4096);
            accessibilityEventObtain.setItemCount(getChildCount());
            accessibilityEventObtain.setFromIndex(this.mCurrentPage);
            accessibilityEventObtain.setToIndex(getNextPage());
            if (getNextPage() < this.mCurrentPage) {
                i = 8192;
            }
            accessibilityEventObtain.setAction(i);
            sendAccessibilityEventUnchecked(accessibilityEventObtain);
        }
    }

    private void setEnableFreeScroll(boolean z) {
        this.mFreeScroll = z;
        if (this.mFreeScroll) {
            updateFreescrollBounds();
            getFreeScrollPageRange(this.mTempVisiblePagesRange);
            int currentPage = getCurrentPage();
            int[] iArr = this.mTempVisiblePagesRange;
            if (currentPage < iArr[0]) {
                setCurrentPage(iArr[0]);
            } else {
                int currentPage2 = getCurrentPage();
                int[] iArr2 = this.mTempVisiblePagesRange;
                if (currentPage2 > iArr2[1]) {
                    setCurrentPage(iArr2[1]);
                }
            }
        }
        setEnableOverscroll(!z);
    }

    private void updateDragViewTranslationDuringDrag() {
        if (this.mDragView != null) {
            float scrollX = (this.mLastMotionX - this.mDownMotionX) + (getScrollX() - this.mDownScrollX) + (this.mDragViewBaselineLeft - this.mDragView.getLeft());
            float f = this.mLastMotionY - this.mDownMotionY;
            this.mDragView.setTranslationX(scrollX);
            this.mDragView.setTranslationY(f);
        }
    }

    private void updatePageIndicator() {
        PageIndicator pageIndicator = this.mPageIndicator;
        if (pageIndicator != null) {
            pageIndicator.setContentDescription(getPageIndicatorDescription());
            if (isReordering(false)) {
                return;
            }
            this.mPageIndicator.setActiveMarker(getNextPage());
        }
    }

    private int validateNewPage(int i) {
        if (this.mFreeScroll) {
            getFreeScrollPageRange(this.mTempVisiblePagesRange);
            int[] iArr = this.mTempVisiblePagesRange;
            i = Math.max(iArr[0], Math.min(i, iArr[1]));
        }
        return Math.max(0, Math.min(i, getPageCount() - 1));
    }

    protected float acceleratedOverFactor(float f) {
        float viewportWidth = (f / getViewportWidth()) * 2.0f;
        if (viewportWidth == 0.0f) {
            return 0.0f;
        }
        return Math.abs(viewportWidth) >= 1.0f ? viewportWidth / Math.abs(viewportWidth) : viewportWidth;
    }

    @Override // android.view.ViewGroup, android.view.View
    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        int i3 = this.mCurrentPage;
        if (i3 >= 0 && i3 < getPageCount()) {
            getPageAt(this.mCurrentPage).addFocusables(arrayList, i, i2);
        }
        if (i == 17) {
            int i4 = this.mCurrentPage;
            if (i4 > 0) {
                getPageAt(i4 - 1).addFocusables(arrayList, i, i2);
                return;
            }
            return;
        }
        if (i != 66 || this.mCurrentPage >= getPageCount() - 1) {
            return;
        }
        getPageAt(this.mCurrentPage + 1).addFocusables(arrayList, i, i2);
    }

    public void addFullScreenPage(View view) {
        LayoutParams layoutParamsGenerateDefaultLayoutParams = generateDefaultLayoutParams();
        layoutParamsGenerateDefaultLayoutParams.isFullScreenPage = true;
        super.addView(view, 0, layoutParamsGenerateDefaultLayoutParams);
    }

    protected void cancelCurrentPageLongPress() {
        View pageAt = getPageAt(this.mCurrentPage);
        if (pageAt != null) {
            pageAt.cancelLongPress();
        }
    }

    @Override // android.view.ViewGroup
    protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    @Override // android.view.View
    public void computeScroll() {
        computeScrollHelper();
    }

    protected boolean computeScrollHelper() {
        if (this.mScroller.computeScrollOffset()) {
            if (getScrollX() != this.mScroller.getCurrX() || getScrollY() != this.mScroller.getCurrY() || this.mOverScrollX != this.mScroller.getCurrX()) {
                scrollTo((int) (this.mScroller.getCurrX() * (1.0f / (this.mFreeScroll ? getScaleX() : 1.0f))), this.mScroller.getCurrY());
            }
            invalidate();
            return true;
        }
        if (this.mNextPage == -1) {
            return false;
        }
        sendScrollAccessibilityEvent();
        this.mCurrentPage = validateNewPage(this.mNextPage);
        this.mNextPage = -1;
        notifyPageSwitchListener();
        if (this.mTouchState == 0) {
            pageEndMoving();
        }
        onPostReorderingAnimationCompleted();
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled()) {
            announceForAccessibility(getCurrentPageDescription());
        }
        return true;
    }

    protected void dampedOverScroll(float f) {
        float viewportWidth = getViewportWidth();
        float f2 = f / viewportWidth;
        if (f2 == 0.0f) {
            return;
        }
        float fAbs = (f2 / Math.abs(f2)) * overScrollInfluenceCurve(Math.abs(f2));
        if (Math.abs(fAbs) >= 1.0f) {
            fAbs /= Math.abs(fAbs);
        }
        int iRound = Math.round(fAbs * 0.07f * viewportWidth);
        if (f < 0.0f) {
            this.mOverScrollX = iRound;
            super.scrollTo(this.mOverScrollX, getScrollY());
        } else {
            this.mOverScrollX = this.mMaxScrollX + iRound;
            super.scrollTo(this.mOverScrollX, getScrollY());
        }
        invalidate();
    }

    protected void determineScrollingStart(MotionEvent motionEvent) {
        determineScrollingStart(motionEvent, 1.0f);
    }

    protected void determineScrollingStart(MotionEvent motionEvent, float f) {
        int iFindPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
        if (iFindPointerIndex == -1) {
            return;
        }
        float x = motionEvent.getX(iFindPointerIndex);
        if (isTouchPointInViewportWithBuffer((int) x, (int) motionEvent.getY(iFindPointerIndex))) {
            if (((int) Math.abs(x - this.mLastMotionX)) > Math.round(f * ((float) this.mTouchSlop))) {
                this.mTouchState = 1;
                this.mTotalMotionX += Math.abs(this.mLastMotionX - x);
                this.mLastMotionX = x;
                this.mLastMotionXRemainder = 0.0f;
                this.mTouchX = getViewportOffsetX() + getScrollX();
                this.mSmoothingTime = System.nanoTime() / 1.0E9f;
                onScrollInteractionBegin();
                pageBeginMoving();
            }
        }
    }

    public void disableFreeScroll() {
        setEnableFreeScroll(false);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int viewportWidth = this.mOverScrollX + (getViewportWidth() / 2);
            if (viewportWidth != this.mLastScreenCenter || this.mForceScreenScrolled) {
                this.mForceScreenScrolled = false;
                screenScrolled(viewportWidth);
                this.mLastScreenCenter = viewportWidth;
            }
            getVisiblePages(this.mTempVisiblePagesRange);
            int[] iArr = this.mTempVisiblePagesRange;
            int i = iArr[0];
            int i2 = iArr[1];
            if (i == -1 || i2 == -1) {
                return;
            }
            long drawingTime = getDrawingTime();
            canvas.save();
            canvas.clipRect(getScrollX(), getScrollY(), (getScrollX() + getRight()) - getLeft(), (getScrollY() + getBottom()) - getTop());
            for (int i3 = childCount - 1; i3 >= 0; i3--) {
                View pageAt = getPageAt(i3);
                if (pageAt != this.mDragView && (this.mForceDrawAllChildrenNextFrame || (i <= i3 && i3 <= i2 && shouldDrawChild(pageAt)))) {
                    drawChild(canvas, pageAt, drawingTime);
                }
            }
            View view = this.mDragView;
            if (view != null) {
                drawChild(canvas, view, drawingTime);
            }
            this.mForceDrawAllChildrenNextFrame = false;
            canvas.restore();
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        if (i == 17) {
            if (getCurrentPage() > 0) {
                snapToPage(getCurrentPage() - 1);
                return true;
            }
        } else if (i == 66 && getCurrentPage() < getPageCount() - 1) {
            snapToPage(getCurrentPage() + 1);
            return true;
        }
        return super.dispatchUnhandledMove(view, i);
    }

    public void enableFreeScroll() {
        setEnableFreeScroll(true);
    }

    void endReordering() {
        if (this.mReorderingStarted) {
            this.mReorderingStarted = false;
            final Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$PagedView$eJbelvsDLDG0HqzEqtSy4OcaPfE
                @Override // java.lang.Runnable
                public final void run() {
                    PagedView.this.onEndReordering();
                }
            };
            if (!this.mDeferringForDelete) {
                this.mPostReorderingPreZoomInRunnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$PagedView$mjF_EIm6H22Ec7782DA7B_FmzrY
                    @Override // java.lang.Runnable
                    public final void run() {
                        PagedView.this.lambda$endReordering$3( runnable);
                    }
                };
                this.mPostReorderingPreZoomInRemainingAnimationCount = this.NUM_ANIMATIONS_RUNNING_BEFORE_ZOOM_OUT;
                snapToPage(indexOfChild(this.mDragView), 0);
                animateDragViewToOriginalPosition();
            }
            this.mDragView = null;
        }
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void focusableViewAvailable(View view) {
        View pageAt = getPageAt(this.mCurrentPage);
        for (View view2 = view; view2 != pageAt; view2 = (View) view2.getParent()) {
            if (view2 == this || !(view2.getParent() instanceof View)) {
                return;
            }
        }
        super.focusableViewAvailable(view);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    @Override // android.view.ViewGroup
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    protected int getChildGap() {
        return 0;
    }

    protected int getChildOffset(int i) {
        if (i < 0 || i > getChildCount() - 1) {
            return 0;
        }
        return getPageAt(i).getLeft() - getViewportOffsetX();
    }

    protected int getChildWidth(int i) {
        return getPageAt(i).getMeasuredWidth();
    }

    public int getCurrentPage() {
        return this.mCurrentPage;
    }

    protected String getCurrentPageDescription() {
        return String.format(getContext().getString(R.string.default_scroll_format), Integer.valueOf(getNextPage() + 1), Integer.valueOf(getChildCount()));
    }

    public RemoveDropTarget getDeleteDropTarget() {
        return this.mDeleteDropTarget;
    }

    protected void getFreeScrollPageRange(int[] iArr) {
        iArr[0] = 0;
        iArr[1] = Math.max(0, getChildCount() - 1);
    }

    public int getLayoutTransitionOffsetForPage(int i) {
        int[] iArr = this.mPageScrolls;
        int paddingRight = 0;
        if (iArr == null || i >= iArr.length || i < 0) {
            return 0;
        }
        View childAt = getChildAt(i);
        if (!((LayoutParams) childAt.getLayoutParams()).isFullScreenPage) {
            paddingRight = this.mIsRtl ? getPaddingRight() : getPaddingLeft();
        }
        return (int) (childAt.getX() - ((this.mPageScrolls[i] + paddingRight) + getViewportOffsetX()));
    }

    protected float getMaxScrollProgress() {
        return 1.0f;
    }

    public int getMaxScrollX() {
        return this.mMaxScrollX;
    }

    int getNearestHoverOverPageIndex() {
        View view = this.mDragView;
        if (view == null) {
            return -1;
        }
        int left = (int) (view.getLeft() + (this.mDragView.getMeasuredWidth() / 2) + this.mDragView.getTranslationX());
        getFreeScrollPageRange(this.mTempVisiblePagesRange);
        int i = Integer.MAX_VALUE;
        int iIndexOfChild = indexOfChild(this.mDragView);
        for (int i2 = this.mTempVisiblePagesRange[0]; i2 <= this.mTempVisiblePagesRange[1]; i2++) {
            View pageAt = getPageAt(i2);
            int iAbs = Math.abs(left - (pageAt.getLeft() + (pageAt.getMeasuredWidth() / 2)));
            if (iAbs < i) {
                iIndexOfChild = i2;
                i = iAbs;
            }
        }
        return iIndexOfChild;
    }

    public int getNextPage() {
        int i = this.mNextPage;
        return i != -1 ? i : this.mCurrentPage;
    }

    public int getNormalChildHeight() {
        return this.mNormalChildHeight;
    }

    public View.OnTouchListener getOnInterceptTouchListener() {
        return this.mOnInterceptTouchListener;
    }

    public View getPageAt(int i) {
        return getChildAt(i);
    }

    public int getPageCount() {
        return getChildCount();
    }

    public int getPageForView(View view) {
        if (view == null) {
            return -1;
        }
        ViewParent parent = view.getParent();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (parent == getPageAt(i)) {
                return i;
            }
        }
        return -1;
    }

    PageIndicator getPageIndicator() {
        return this.mPageIndicator;
    }

    protected View.OnClickListener getPageIndicatorClickListener() {
        return null;
    }

    protected String getPageIndicatorDescription() {
        return getCurrentPageDescription();
    }

    protected PageIndicator.PageMarkerResources getPageIndicatorMarker(int i) {
        return new PageIndicator.PageMarkerResources();
    }

    int getPageNearestToCenterOfScreen() {
        int viewportOffsetX = getViewportOffsetX() + getScrollX() + (getViewportWidth() / 2);
        int childCount = getChildCount();
        int i = Integer.MAX_VALUE;
        int i2 = -1;
        for (int i3 = 0; i3 < childCount; i3++) {
            int measuredWidth = getPageAt(i3).getMeasuredWidth();
            if (measuredWidth != 0) {
                int iAbs = Math.abs(((getViewportOffsetX() + getChildOffset(i3)) + (measuredWidth / 2)) - viewportOffsetX);
                if (iAbs < i) {
                    i2 = i3;
                    i = iAbs;
                }
            }
        }
        return i2;
    }

    protected int getPageSnapDuration() {
        return isInOverScroll() ? 350 : 750;
    }

    int getRestorePage() {
        return this.mRestorePage;
    }

    public int getScrollForPage(int i) {
        int[] iArr = this.mPageScrolls;
        if (iArr == null || i >= iArr.length || i < 0) {
            return 0;
        }
        return iArr[i];
    }

    protected float getScrollProgress(int i, View view, int i2) {
        int scrollForPage = i - (getScrollForPage(i2) + (getViewportWidth() / 2));
        int childCount = getChildCount();
        int i3 = i2 + 1;
        if ((scrollForPage < 0 && !this.mIsRtl) || (scrollForPage > 0 && this.mIsRtl)) {
            i3 = i2 - 1;
        }
        return Math.max(Math.min(scrollForPage / (((i3 < 0 || i3 > childCount + (-1)) ? view.getMeasuredWidth() + this.mPageSpacing : Math.abs(getScrollForPage(i3) - getScrollForPage(i2))) * 1.0f), getMaxScrollProgress()), -getMaxScrollProgress());
    }

    public int getScrollerFinalX() {
        return this.mScroller.getFinalX();
    }

    int getViewportHeight() {
        return this.mViewport.height();
    }

    int getViewportOffsetX() {
        return (getMeasuredWidth() - getViewportWidth()) / 2;
    }

    int getViewportOffsetY() {
        return (getMeasuredHeight() - getViewportHeight()) / 2;
    }

    int getViewportWidth() {
        return this.mViewport.width();
    }

    protected void getVisiblePages(int[] iArr) {
        int childCount = getChildCount();
        int[] iArr2 = sTmpIntPoint;
        iArr2[1] = 0;
        iArr2[0] = 0;
        iArr[0] = -1;
        iArr[1] = -1;
        if (childCount <= 0) {
            iArr[0] = -1;
            iArr[1] = -1;
            return;
        }
        int viewportWidth = getViewportWidth();
        int childCount2 = getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount2; i2++) {
            View pageAt = getPageAt(i2);
            int[] iArr3 = sTmpIntPoint;
            iArr3[0] = 0;
            Utilities.getDescendantCoordRelativeToParent(pageAt, this, iArr3, false);
            int[] iArr4 = sTmpIntPoint;
            if (iArr4[0] <= viewportWidth) {
                iArr4[0] = pageAt.getMeasuredWidth();
                Utilities.getDescendantCoordRelativeToParent(pageAt, this, sTmpIntPoint, false);
                if (sTmpIntPoint[0] < 0) {
                    if (iArr[0] != -1) {
                        break;
                    }
                } else {
                    if (iArr[0] < 0) {
                        iArr[0] = i2;
                    }
                    i = i2;
                }
            } else {
                if (iArr[0] != -1) {
                    break;
                }
            }
        }
        iArr[1] = i;
    }

    protected int indexToPage(int i) {
        return i;
    }

    protected void init() {
        this.mScroller = new LauncherScroller(getContext());
        setDefaultInterpolator(new ScrollInterpolator());
        this.mCurrentPage = 0;
        this.mCenterPagesVertically = true;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        this.mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mDensity = getResources().getDisplayMetrics().density;
        float f = this.mDensity;
        this.mFlingThresholdVelocity = (int) (500.0f * f);
        this.mMinFlingVelocity = (int) (250.0f * f);
        this.mMinSnapVelocity = (int) (f * 1500.0f);
        setOnHierarchyChangeListener(this);
        setWillNotDraw(false);
    }

    public boolean isAllowOverScroll() {
        return this.mAllowOverScroll;
    }

    protected boolean isInOverScroll() {
        int i = this.mOverScrollX;
        return i > this.mMaxScrollX || i < 0;
    }

    protected boolean isPageMoving() {
        return this.mIsPageMoving;
    }

    boolean isReordering(boolean z) {
        boolean z2 = this.mIsReordering;
        if (z) {
            return z2 & (this.mTouchState == 4);
        }
        return z2;
    }

    protected void notifyPageSwitchListener() {
        PageSwitchListener pageSwitchListener = this.mPageSwitchListener;
        if (pageSwitchListener != null) {
            pageSwitchListener.onPageSwitch(getPageAt(getNextPage()), getNextPage());
        }
        updatePageIndicator();
    }

    public void onAddView(View view, int i) {
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        int i;
        super.onAttachedToWindow();
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) getParent()).getParent();
        if (this.mPageIndicator != null || (i = this.mPageIndicatorViewId) <= -1) {
            return;
        }
        this.mPageIndicator = (PageIndicator) viewGroup.findViewById(i);
        this.mPageIndicator.removeAllMarkers(true);
        ArrayList<PageIndicator.PageMarkerResources> arrayList = new ArrayList<>();
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            arrayList.add(getPageIndicatorMarker(i2));
        }
        this.mPageIndicator.addMarkers(arrayList, true);
        View.OnClickListener pageIndicatorClickListener = getPageIndicatorClickListener();
        if (pageIndicatorClickListener != null) {
            this.mPageIndicator.setOnClickListener(pageIndicatorClickListener);
        }
        updatePageIndicator();
    }

    @Override // android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewAdded(View view, View view2) {
        if (this.mPageIndicator != null && !isReordering(false)) {
            int iIndexOfChild = indexOfChild(view2);
            this.mPageIndicator.addMarker(iIndexOfChild, getPageIndicatorMarker(iIndexOfChild), true);
        }
        this.mForceScreenScrolled = true;
        updateFreescrollBounds();
        invalidate();
    }

    @Override // android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewRemoved(View view, View view2) {
        this.mForceScreenScrolled = true;
        updateFreescrollBounds();
        invalidate();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPageIndicator = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void onEndReordering() {
        this.mDeleteDropTarget.animate().withEndAction(null).cancel();
        this.mDeleteDropTarget.animate().alpha(0.0f).withEndAction(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$PagedView$eSPfy_qzT-tyNBE1-QBNoVwSkj8
            @Override // java.lang.Runnable
            public final void run() {
                PagedView.this.lambda$onEndReordering$1();
            }
        });
        this.mWasHoveringOverDelete = false;
        this.mIsReordering = false;
    }

    @Override // android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        float f;
        float axisValue;
        if ((motionEvent.getSource() & 2) != 0 && motionEvent.getAction() == 8) {
            if ((motionEvent.getMetaState() & 1) != 0) {
                axisValue = motionEvent.getAxisValue(9);
                f = 0.0f;
            } else {
                f = -motionEvent.getAxisValue(9);
                axisValue = motionEvent.getAxisValue(10);
            }
            if (axisValue != 0.0f || f != 0.0f) {
                boolean z = false;
                if (!this.mIsRtl ? axisValue > 0.0f || f > 0.0f : axisValue < 0.0f || f < 0.0f) {
                    z = true;
                }
                if (z) {
                    scrollRight();
                } else {
                    scrollLeft();
                }
                return true;
            }
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent motionEvent) {
        return true;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setScrollable(getPageCount() > 1);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setScrollable(getPageCount() > 1);
        if (getCurrentPage() < getPageCount() - 1) {
            accessibilityNodeInfo.addAction(4096);
        }
        if (getCurrentPage() > 0) {
            accessibilityNodeInfo.addAction(8192);
        }
        accessibilityNodeInfo.setClassName(getClass().getName());
        accessibilityNodeInfo.setLongClickable(false);
        if (Utilities.ATLEAST_LOLLIPOP) {
            accessibilityNodeInfo.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK);
        }
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        View.OnTouchListener onTouchListener = this.mOnInterceptTouchListener;
        if (onTouchListener != null && onTouchListener.onTouch(this, motionEvent)) {
            return true;
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        if (getChildCount() <= 0) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        if (action == 2 && this.mTouchState == 1) {
            return true;
        }
        int i = action & 255;
        if (i != 6) {
            switch (i) {
                case 0:
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    this.mDownMotionX = x;
                    this.mDownMotionY = y;
                    this.mDownScrollX = getScrollX();
                    this.mLastMotionX = x;
                    this.mLastMotionY = y;
                    float[] fArrMapPointFromViewToParent = mapPointFromViewToParent(this, x, y);
                    this.mParentDownMotionX = fArrMapPointFromViewToParent[0];
                    this.mParentDownMotionY = fArrMapPointFromViewToParent[1];
                    this.mLastMotionXRemainder = 0.0f;
                    this.mTotalMotionX = 0.0f;
                    this.mActivePointerId = motionEvent.getPointerId(0);
                    if (this.mScroller.isFinished() || Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) < this.mTouchSlop / 3) {
                        this.mTouchState = 0;
                        if (!this.mScroller.isFinished() && !this.mFreeScroll) {
                            setCurrentPage(getNextPage());
                            pageEndMoving();
                        }
                    } else if (!isTouchPointInViewportWithBuffer((int) this.mDownMotionX, (int) this.mDownMotionY)) {
                        this.mTouchState = 0;
                    } else {
                        this.mTouchState = 1;
                    }
                    break;
                case 1:
                case 3:
                    resetTouchState();
                    break;
                case 2:
                    if (this.mActivePointerId != -1) {
                        determineScrollingStart(motionEvent);
                    }
                    break;
            }
        } else {
            onSecondaryPointerUp(motionEvent);
            releaseVelocityTracker();
        }
        return this.mTouchState != 0;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int paddingTop;
        if (getChildCount() == 0) {
            return;
        }
        int childCount = getChildCount();
        int viewportOffsetX = getViewportOffsetX();
        int viewportOffsetY = getViewportOffsetY();
        this.mViewport.offset(viewportOffsetX, viewportOffsetY);
        int i6 = this.mIsRtl ? childCount - 1 : 0;
        int i7 = this.mIsRtl ? -1 : childCount;
        int i8 = this.mIsRtl ? -1 : 1;
        int paddingTop2 = getPaddingTop() + getPaddingBottom();
        int paddingLeft = (((LayoutParams) getChildAt(i6).getLayoutParams()).isFullScreenPage ? 0 : getPaddingLeft()) + viewportOffsetX;
        if (this.mPageScrolls == null || childCount != this.mChildCountOnLastLayout) {
            this.mPageScrolls = new int[childCount];
        }
        while (i6 != i7) {
            View pageAt = getPageAt(i6);
            if (pageAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) pageAt.getLayoutParams();
                if (layoutParams.isFullScreenPage) {
                    paddingTop = viewportOffsetY;
                } else {
                    paddingTop = getPaddingTop() + viewportOffsetY + this.mInsets.top;
                    if (this.mCenterPagesVertically) {
                        paddingTop += ((((getViewportHeight() - this.mInsets.top) - this.mInsets.bottom) - paddingTop2) - pageAt.getMeasuredHeight()) / 2;
                    }
                }
                int measuredWidth = pageAt.getMeasuredWidth();
                pageAt.layout(paddingLeft, paddingTop, paddingLeft + pageAt.getMeasuredWidth(), pageAt.getMeasuredHeight() + paddingTop);
                this.mPageScrolls[i6] = (paddingLeft - (layoutParams.isFullScreenPage ? 0 : getPaddingLeft())) - viewportOffsetX;
                int paddingRight = this.mPageSpacing;
                int i9 = i6 + i8;
                LayoutParams layoutParams2 = i9 != i7 ? (LayoutParams) getPageAt(i9).getLayoutParams() : null;
                if (layoutParams.isFullScreenPage) {
                    paddingRight = getPaddingLeft();
                } else if (layoutParams2 != null && layoutParams2.isFullScreenPage) {
                    paddingRight = getPaddingRight();
                }
                paddingLeft += measuredWidth + paddingRight + getChildGap();
            }
            i6 += i8;
        }
        LayoutTransition layoutTransition = getLayoutTransition();
        if (layoutTransition == null || !layoutTransition.isRunning()) {
            updateMaxScrollX();
        } else {
            layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener() { // from class: com.voxel.launcher3.PagedView.1
                @Override // android.animation.LayoutTransition.TransitionListener
                public void endTransition(LayoutTransition layoutTransition2, ViewGroup viewGroup, View view, int i10) {
                    if (layoutTransition2.isRunning()) {
                        return;
                    }
                    layoutTransition2.removeTransitionListener(this);
                    PagedView.this.updateMaxScrollX();
                }

                @Override // android.animation.LayoutTransition.TransitionListener
                public void startTransition(LayoutTransition layoutTransition2, ViewGroup viewGroup, View view, int i10) {
                }
            });
        }
        if (this.mFirstLayout && (i5 = this.mCurrentPage) >= 0 && i5 < childCount) {
            updateCurrentPageScroll();
            this.mFirstLayout = false;
        }
        if (this.mScroller.isFinished() && this.mChildCountOnLastLayout != childCount) {
            int i10 = this.mRestorePage;
            if (i10 != -1001) {
                setCurrentPage(i10);
                this.mRestorePage = -1001;
            } else {
                setCurrentPage(getNextPage());
            }
        }
        this.mChildCountOnLastLayout = childCount;
        if (isReordering(true)) {
            updateDragViewTranslationDuringDrag();
        }
    }

    @Override // android.view.View
    protected void onMeasure(int i, int i2) {
        int i3;
        int i4;
        int viewportWidth;
        int viewportHeight;
        int i5;
        if (getChildCount() == 0) {
            super.onMeasure(i, i2);
            return;
        }
        int mode = View.MeasureSpec.getMode(i);
        int size = View.MeasureSpec.getSize(i);
        int mode2 = View.MeasureSpec.getMode(i2);
        int size2 = View.MeasureSpec.getSize(i2);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int iMax = (int) (Math.max(displayMetrics.widthPixels + this.mInsets.left + this.mInsets.right, displayMetrics.heightPixels + this.mInsets.top + this.mInsets.bottom) * 2.0f);
        if (this.mUseMinScale) {
            float f = iMax;
            float f2 = this.mMinScale;
            i3 = (int) (f / f2);
            i4 = (int) (f / f2);
        } else {
            i3 = size;
            i4 = size2;
        }
        this.mViewport.set(0, 0, size, size2);
        if (mode == 0 || mode2 == 0) {
            super.onMeasure(i, i2);
            return;
        }
        if (size <= 0 || size2 <= 0) {
            super.onMeasure(i, i2);
            return;
        }
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int paddingLeft = getPaddingLeft() + getPaddingRight();
        int childCount = getChildCount();
        int i6 = 0;
        for (int i7 = 0; i7 < childCount; i7++) {
            View pageAt = getPageAt(i7);
            if (pageAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) pageAt.getLayoutParams();
                int i8 = 1073741824;
                if (layoutParams.isFullScreenPage) {
                    viewportWidth = (getViewportWidth() - this.mInsets.left) - this.mInsets.right;
                    viewportHeight = getViewportHeight();
                    i5 = 1073741824;
                } else {
                    i5 = layoutParams.width == -2 ? Integer.MIN_VALUE : 1073741824;
                    i8 = layoutParams.height == -2 ? Integer.MIN_VALUE : 1073741824;
                    viewportWidth = ((getViewportWidth() - paddingLeft) - this.mInsets.left) - this.mInsets.right;
                    viewportHeight = ((getViewportHeight() - paddingTop) - this.mInsets.top) - this.mInsets.bottom;
                    this.mNormalChildHeight = viewportHeight;
                }
                if (i6 == 0) {
                    i6 = viewportWidth;
                }
                pageAt.measure(View.MeasureSpec.makeMeasureSpec(viewportWidth, i5), View.MeasureSpec.makeMeasureSpec(viewportHeight, i8));
            }
        }
        setMeasuredDimension(i3, i4);
    }

    protected void onPageBeginMoving() {
        PageInteractionListener pageInteractionListener = this.mPageInteractionListener;
        if (pageInteractionListener != null) {
            pageInteractionListener.onPageBeginMoving();
        }
    }

    protected void onPageEndMoving() {
        PageInteractionListener pageInteractionListener = this.mPageInteractionListener;
        if (pageInteractionListener != null) {
            pageInteractionListener.onPageEndMoving();
        }
    }

    void onPostReorderingAnimationCompleted() {
        this.mPostReorderingPreZoomInRemainingAnimationCount--;
        Runnable runnable = this.mPostReorderingPreZoomInRunnable;
        if (runnable == null || this.mPostReorderingPreZoomInRemainingAnimationCount != 0) {
            return;
        }
        runnable.run();
        this.mPostReorderingPreZoomInRunnable = null;
    }

    public void onRemoveView(View view, boolean z) {
    }

    public void onRemoveViewAnimationCompleted() {
    }

    @Override // android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        int i2 = this.mNextPage;
        if (i2 == -1) {
            i2 = this.mCurrentPage;
        }
        View pageAt = getPageAt(i2);
        if (pageAt != null) {
            return pageAt.requestFocus(i, rect);
        }
        return false;
    }

    protected void onScrollInteractionBegin() {
        PageInteractionListener pageInteractionListener = this.mPageInteractionListener;
        if (pageInteractionListener != null) {
            pageInteractionListener.onScrollInteractionBegin();
        }
    }

    protected void onScrollInteractionEnd() {
        PageInteractionListener pageInteractionListener = this.mPageInteractionListener;
        if (pageInteractionListener != null) {
            pageInteractionListener.onScrollInteractionEnd();
        }
    }

    protected void onStartReordering() {
        this.mTouchState = 4;
        this.mIsReordering = true;
        if (getChildCount() > 1) {
            this.mDeleteDropTarget.setVisibility(0);
            this.mDeleteDropTarget.animate().withEndAction(null).cancel();
            this.mDeleteDropTarget.animate().alpha(1.0f);
        }
        invalidate();
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int i;
        super.onTouchEvent(motionEvent);
        if (getChildCount() <= 0) {
            return super.onTouchEvent(motionEvent);
        }
        acquireVelocityTrackerAndAddMovement(motionEvent);
        int action = motionEvent.getAction() & 255;
        if (action != 6) {
            boolean z = false;
            switch (action) {
                case 0:
                    if (!this.mScroller.isFinished()) {
                        abortScrollerAnimation(false);
                    }
                    float x = motionEvent.getX();
                    this.mLastMotionX = x;
                    this.mDownMotionX = x;
                    float y = motionEvent.getY();
                    this.mLastMotionY = y;
                    this.mDownMotionY = y;
                    this.mDownScrollX = getScrollX();
                    float[] fArrMapPointFromViewToParent = mapPointFromViewToParent(this, this.mLastMotionX, this.mLastMotionY);
                    this.mParentDownMotionX = fArrMapPointFromViewToParent[0];
                    this.mParentDownMotionY = fArrMapPointFromViewToParent[1];
                    this.mLastMotionXRemainder = 0.0f;
                    this.mTotalMotionX = 0.0f;
                    this.mActivePointerId = motionEvent.getPointerId(0);
                    if (this.mTouchState == 1) {
                        onScrollInteractionBegin();
                        pageBeginMoving();
                    }
                    break;
                case 1:
                    int i2 = this.mTouchState;
                    if (i2 == 1) {
                        int i3 = this.mActivePointerId;
                        float x2 = motionEvent.getX(motionEvent.findPointerIndex(i3));
                        VelocityTracker velocityTracker = this.mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, this.mMaximumVelocity);
                        int xVelocity = (int) velocityTracker.getXVelocity(i3);
                        int i4 = (int) (x2 - this.mDownMotionX);
                        float measuredWidth = getPageAt(this.mCurrentPage).getMeasuredWidth();
                        boolean z2 = ((float) Math.abs(i4)) > 0.4f * measuredWidth;
                        this.mTotalMotionX += Math.abs((this.mLastMotionX + this.mLastMotionXRemainder) - x2);
                        boolean z3 = this.mTotalMotionX > 25.0f && Math.abs(xVelocity) > this.mFlingThresholdVelocity;
                        if (this.mFreeScroll) {
                            if (!this.mScroller.isFinished()) {
                                abortScrollerAnimation(true);
                            }
                            float scaleX = getScaleX();
                            int scrollX = (int) (getScrollX() * scaleX);
                            this.mScroller.setInterpolator(this.mDefaultInterpolator);
                            this.mScroller.fling(scrollX, getScrollY(), (int) ((-xVelocity) * scaleX), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
                            invalidate();
                        } else {
                            boolean z4 = ((float) Math.abs(i4)) > measuredWidth * 0.33f && Math.signum((float) xVelocity) != Math.signum((float) i4) && z3;
                            boolean z5 = !this.mIsRtl ? i4 >= 0 : i4 <= 0;
                            if (!this.mIsRtl ? xVelocity < 0 : xVelocity > 0) {
                                z = true;
                            }
                            if ((!(!z2 || z5 || z3) || (z3 && !z)) && (i = this.mCurrentPage) > 0) {
                                if (!z4) {
                                    i--;
                                }
                                snapToPageWithVelocity(i, xVelocity);
                            } else if (!((z2 && z5 && !z3) || (z3 && z)) || this.mCurrentPage >= getChildCount() - 1) {
                                snapToDestination();
                            } else {
                                snapToPageWithVelocity(z4 ? this.mCurrentPage : this.mCurrentPage + 1, xVelocity);
                            }
                        }
                        onScrollInteractionEnd();
                    } else if (i2 == 2) {
                        int iMax = Math.max(0, this.mCurrentPage - 1);
                        if (iMax != this.mCurrentPage) {
                            snapToPage(iMax);
                        } else {
                            snapToDestination();
                        }
                    } else if (i2 == 3) {
                        int iMin = Math.min(getChildCount() - 1, this.mCurrentPage + 1);
                        if (iMin != this.mCurrentPage) {
                            snapToPage(iMin);
                        } else {
                            snapToDestination();
                        }
                    } else if (i2 == 4) {
                        this.mLastMotionX = motionEvent.getX();
                        this.mLastMotionY = motionEvent.getY();
                        float[] fArrMapPointFromViewToParent2 = mapPointFromViewToParent(this, this.mLastMotionX, this.mLastMotionY);
                        this.mParentDownMotionX = fArrMapPointFromViewToParent2[0];
                        this.mParentDownMotionY = fArrMapPointFromViewToParent2[1];
                        updateDragViewTranslationDuringDrag();
                        if (isHoveringOverDeleteDropTarget((int) this.mParentDownMotionX, (int) this.mParentDownMotionY)) {
                            onDropToDelete();
                        }
                    } else if (!this.mCancelTap) {
                        onUnhandledTap(motionEvent);
                    }
                    removeCallbacks(this.mSidePageHoverRunnable);
                    resetTouchState();
                    break;
                case 2:
                    int i5 = this.mTouchState;
                    if (i5 == 1) {
                        int iFindPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (iFindPointerIndex == -1) {
                            return true;
                        }
                        float x3 = motionEvent.getX(iFindPointerIndex);
                        float f = (this.mLastMotionX + this.mLastMotionXRemainder) - x3;
                        this.mTotalMotionX += Math.abs(f);
                        if (Math.abs(f) < 1.0f) {
                            awakenScrollBars();
                        } else {
                            this.mTouchX += f;
                            this.mSmoothingTime = System.nanoTime() / 1.0E9f;
                            int i6 = (int) f;
                            scrollBy(i6, 0);
                            this.mLastMotionX = x3;
                            this.mLastMotionXRemainder = f - i6;
                        }
                    } else if (i5 != 4) {
                        determineScrollingStart(motionEvent);
                    } else {
                        this.mLastMotionX = motionEvent.getX();
                        this.mLastMotionY = motionEvent.getY();
                        float[] fArrMapPointFromViewToParent3 = mapPointFromViewToParent(this, this.mLastMotionX, this.mLastMotionY);
                        this.mParentDownMotionX = fArrMapPointFromViewToParent3[0];
                        this.mParentDownMotionY = fArrMapPointFromViewToParent3[1];
                        updateDragViewTranslationDuringDrag();
                        final int iIndexOfChild = indexOfChild(this.mDragView);
                        boolean zIsHoveringOverDeleteDropTarget = isHoveringOverDeleteDropTarget((int) this.mParentDownMotionX, (int) this.mParentDownMotionY);
                        setPageHoveringOverDeleteDropTarget(iIndexOfChild, zIsHoveringOverDeleteDropTarget, this.mWasHoveringOverDelete);
                        this.mWasHoveringOverDelete = zIsHoveringOverDeleteDropTarget;
                        final int nearestHoverOverPageIndex = getNearestHoverOverPageIndex();
                        if (nearestHoverOverPageIndex <= -1 || nearestHoverOverPageIndex == indexOfChild(this.mDragView) || zIsHoveringOverDeleteDropTarget) {
                            removeCallbacks(this.mSidePageHoverRunnable);
                            this.mSidePageHoverIndex = -1;
                        } else {
                            int[] iArr = this.mTempVisiblePagesRange;
                            iArr[0] = 0;
                            iArr[1] = getPageCount() - 1;
                            getFreeScrollPageRange(this.mTempVisiblePagesRange);
                            int[] iArr2 = this.mTempVisiblePagesRange;
                            if (iArr2[0] <= nearestHoverOverPageIndex && nearestHoverOverPageIndex <= iArr2[1] && nearestHoverOverPageIndex != this.mSidePageHoverIndex && this.mScroller.isFinished()) {
                                this.mSidePageHoverIndex = nearestHoverOverPageIndex;
                                this.mSidePageHoverRunnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$PagedView$Megkwl-HTsNVO1DjyLCt0ZKAxTY
                                    @Override // java.lang.Runnable
                                    public final void run() {
                                        PagedView.this.lambda$onTouchEvent$0( nearestHoverOverPageIndex, iIndexOfChild);
                                    }
                                };
                                postDelayed(this.mSidePageHoverRunnable, this.REORDERING_SIDE_PAGE_HOVER_TIMEOUT);
                            }
                        }
                    }
                    break;
                case 3:
                    if (this.mTouchState == 1) {
                        snapToDestination();
                    }
                    resetTouchState();
                    break;
            }
        } else {
            onSecondaryPointerUp(motionEvent);
            releaseVelocityTracker();
        }
        return true;
    }

    protected void onUnhandledTap(MotionEvent motionEvent) {
        ((Launcher) getContext()).onClick(this);
    }

    protected void overScroll(float f) {
        dampedOverScroll(f);
    }

    protected void pageBeginMoving() {
        if (this.mIsPageMoving) {
            return;
        }
        this.mIsPageMoving = true;
        onPageBeginMoving();
    }

    protected void pageEndMoving() {
        if (this.mIsPageMoving) {
            this.mIsPageMoving = false;
            onPageEndMoving();
        }
    }

    @Override // android.view.View
    public boolean performAccessibilityAction(int i, Bundle bundle) {
        if (super.performAccessibilityAction(i, bundle)) {
            return true;
        }
        if (i == 4096) {
            if (getCurrentPage() >= getPageCount() - 1) {
                return false;
            }
            scrollRight();
            return true;
        }
        if (i != 8192 || getCurrentPage() <= 0) {
            return false;
        }
        scrollLeft();
        return true;
    }

    @Override // android.view.View
    public boolean performLongClick() {
        this.mCancelTap = true;
        return super.performLongClick();
    }

    @Override // android.view.ViewGroup
    public void removeAllViewsInLayout() {
        PageIndicator pageIndicator = this.mPageIndicator;
        if (pageIndicator != null) {
            pageIndicator.removeAllMarkers(true);
        }
        super.removeAllViewsInLayout();
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void removeView(View view) {
        removeMarkerForView(indexOfChild(view));
        super.removeView(view);
    }

    @Override // android.view.ViewGroup
    public void removeViewAt(int i) {
        removeMarkerForView(i);
        super.removeViewAt(i);
    }

    @Override // android.view.ViewGroup
    public void removeViewInLayout(View view) {
        removeMarkerForView(indexOfChild(view));
        super.removeViewInLayout(view);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        int iIndexToPage = indexToPage(indexOfChild(view));
        if (iIndexToPage < 0 || iIndexToPage == getCurrentPage() || isInTouchMode()) {
            return;
        }
        snapToPage(iIndexToPage);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean requestChildRectangleOnScreen(View view, Rect rect, boolean z) {
        int iIndexToPage = indexToPage(indexOfChild(view));
        if (iIndexToPage == this.mCurrentPage && this.mScroller.isFinished()) {
            return false;
        }
        snapToPage(iIndexToPage);
        return true;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public void requestDisallowInterceptTouchEvent(boolean z) {
        if (z) {
            getPageAt(this.mCurrentPage).cancelLongPress();
        }
        super.requestDisallowInterceptTouchEvent(z);
    }

    protected void screenScrolled(int i) {
        int i2 = this.mOverScrollX;
        boolean z = i2 < 0 || i2 > this.mMaxScrollX;
        if (!this.mFadeInAdjacentScreens || z) {
            return;
        }
        for (int i3 = 0; i3 < getChildCount(); i3++) {
            View childAt = getChildAt(i3);
            if (childAt != null) {
                childAt.setAlpha(1.0f - Math.abs(getScrollProgress(i, childAt, i3)));
            }
        }
        invalidate();
    }

    @Override // android.view.View
    public void scrollBy(int i, int i2) {
        scrollTo(this.mUnboundedScrollX + i, getScrollY() + i2);
    }

    public void scrollLeft() {
        if (getNextPage() > 0) {
            snapToPage(getNextPage() - 1);
        }
    }

    public void scrollRight() {
        if (getNextPage() < getChildCount() - 1) {
            snapToPage(getNextPage() + 1);
        }
    }

    @Override // android.view.View
    public void scrollTo(int i, int i2) {
        if (this.mFreeScroll) {
            if (!this.mScroller.isFinished() && (i > this.mFreeScrollMaxScrollX || i < this.mFreeScrollMinScrollX)) {
                forceFinishScroller();
            }
            i = Math.max(Math.min(i, this.mFreeScrollMaxScrollX), this.mFreeScrollMinScrollX);
        }
        this.mUnboundedScrollX = i;
        boolean z = !this.mIsRtl ? i >= 0 : i <= this.mMaxScrollX;
        boolean z2 = !this.mIsRtl ? i <= this.mMaxScrollX : i >= 0;
        if (z) {
            super.scrollTo(this.mIsRtl ? this.mMaxScrollX : 0, i2);
            if (this.mAllowOverScroll) {
                if (this.mIsRtl) {
                    overScroll(i - this.mMaxScrollX);
                } else {
                    overScroll(i);
                }
            }
        } else if (z2) {
            super.scrollTo(this.mIsRtl ? 0 : this.mMaxScrollX, i2);
            if (this.mAllowOverScroll) {
                if (this.mIsRtl) {
                    overScroll(i);
                } else {
                    overScroll(i - this.mMaxScrollX);
                }
            }
        } else {
            this.mOverScrollX = i;
            super.scrollTo(i, i2);
        }
        this.mTouchX = i;
        this.mSmoothingTime = System.nanoTime() / 1.0E9f;
        if (isReordering(true)) {
            float[] fArrMapPointFromParentToView = mapPointFromParentToView(this, this.mParentDownMotionX, this.mParentDownMotionY);
            this.mLastMotionX = fArrMapPointFromParentToView[0];
            this.mLastMotionY = fArrMapPointFromParentToView[1];
            updateDragViewTranslationDuringDrag();
        }
    }

    @Override // android.view.View, android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEvent(int i) {
        if (i != 4096) {
            super.sendAccessibilityEvent(i);
        }
    }

    public void setCurrentPage(int i) {
        if (!this.mScroller.isFinished()) {
            abortScrollerAnimation(true);
        }
        if (getChildCount() == 0) {
            return;
        }
        this.mForceScreenScrolled = true;
        this.mCurrentPage = validateNewPage(i);
        updateCurrentPageScroll();
        notifyPageSwitchListener();
        invalidate();
    }

    protected void setDefaultInterpolator(Interpolator interpolator) {
        this.mDefaultInterpolator = interpolator;
        this.mScroller.setInterpolator(this.mDefaultInterpolator);
    }

    public void setDeleteDropTarget(RemoveDropTarget removeDropTarget) {
        this.mDeleteDropTarget = removeDropTarget;
    }

    protected void setEnableOverscroll(boolean z) {
        this.mAllowOverScroll = z;
    }

    public void setMinScale(float f) {
        this.mMinScale = f;
        this.mUseMinScale = true;
        requestLayout();
    }

    public void setOnInterceptTouchListener(View.OnTouchListener onTouchListener) {
        this.mOnInterceptTouchListener = onTouchListener;
    }

    @Override // android.view.View
    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.mLongClickListener = onLongClickListener;
        int pageCount = getPageCount();
        for (int i = 0; i < pageCount; i++) {
            getPageAt(i).setOnLongClickListener(onLongClickListener);
        }
        super.setOnLongClickListener(onLongClickListener);
    }

    protected void setPageHoveringOverDeleteDropTarget(int i, boolean z, boolean z2) {
        if (z != z2) {
            if (z) {
                this.mDeleteDropTarget.setHoverColor();
            } else {
                this.mDeleteDropTarget.resetHoverColor();
            }
        }
    }

    public void setPageInteractionListener(PageInteractionListener pageInteractionListener) {
        this.mPageInteractionListener = pageInteractionListener;
    }

    public void setPageSpacing(int i) {
        this.mPageSpacing = i;
        requestLayout();
    }

    public void setPageSwitchListener(PageSwitchListener pageSwitchListener) {
        this.mPageSwitchListener = pageSwitchListener;
        PageSwitchListener pageSwitchListener2 = this.mPageSwitchListener;
        if (pageSwitchListener2 != null) {
            pageSwitchListener2.onPageSwitch(getPageAt(this.mCurrentPage), this.mCurrentPage);
        }
    }

    void setRestorePage(int i) {
        this.mRestorePage = i;
    }

    @Override // android.view.View
    public void setScaleX(float f) {
        super.setScaleX(f);
        if (isReordering(true)) {
            float[] fArrMapPointFromParentToView = mapPointFromParentToView(this, this.mParentDownMotionX, this.mParentDownMotionY);
            this.mLastMotionX = fArrMapPointFromParentToView[0];
            this.mLastMotionY = fArrMapPointFromParentToView[1];
            updateDragViewTranslationDuringDrag();
        }
    }

    protected boolean shouldDrawChild(View view) {
        return view.getVisibility() == 0;
    }

    protected void snapToDestination() {
        snapToPage(getPageNearestToCenterOfScreen(), getPageSnapDuration());
    }

    public void snapToPage(int i) {
        snapToPage(i, getPageSnapDuration());
    }

    protected void snapToPage(int i, int i2) {
        snapToPage(i, i2, false, null);
    }

    protected void snapToPage(int i, int i2, int i3) {
        snapToPage(i, i2, i3, false, null);
    }

    protected void snapToPage(int i, int i2, int i3, boolean z, TimeInterpolator timeInterpolator) {
        int iAbs;
        int i4;
        int iValidateNewPage = validateNewPage(i);
        this.mNextPage = iValidateNewPage;
        View focusedChild = getFocusedChild();
        if (focusedChild != null && iValidateNewPage != (i4 = this.mCurrentPage) && focusedChild == getPageAt(i4)) {
            focusedChild.clearFocus();
        }
        sendScrollAccessibilityEvent();
        pageBeginMoving();
        awakenScrollBars(i3);
        if (z) {
            iAbs = 0;
        } else {
            iAbs = i3 == 0 ? Math.abs(i2) : i3;
        }
        if (!this.mScroller.isFinished()) {
            abortScrollerAnimation(false);
        }
        if (timeInterpolator != null) {
            this.mScroller.setInterpolator(timeInterpolator);
        } else {
            this.mScroller.setInterpolator(this.mDefaultInterpolator);
        }
        this.mScroller.startScroll(this.mUnboundedScrollX, 0, i2, 0, iAbs);
        updatePageIndicator();
        if (z) {
            computeScroll();
        }
        this.mForceScreenScrolled = true;
        invalidate();
    }

    protected void snapToPage(int i, int i2, TimeInterpolator timeInterpolator) {
        snapToPage(i, i2, false, timeInterpolator);
    }

    protected void snapToPage(int i, int i2, boolean z, TimeInterpolator timeInterpolator) {
        int iValidateNewPage = validateNewPage(i);
        snapToPage(iValidateNewPage, getScrollForPage(iValidateNewPage) - this.mUnboundedScrollX, i2, z, timeInterpolator);
    }

    protected void snapToPageImmediately(int i) {
        snapToPage(i, getPageSnapDuration(), true, null);
    }

    protected void snapToPageWithVelocity(int i, int i2) {
        int iValidateNewPage = validateNewPage(i);
        int viewportWidth = getViewportWidth() / 2;
        int scrollForPage = getScrollForPage(iValidateNewPage) - this.mUnboundedScrollX;
        if (Math.abs(i2) < this.mMinFlingVelocity || isInOverScroll()) {
            snapToPage(iValidateNewPage, getPageSnapDuration());
            return;
        }
        float fMin = Math.min(1.0f, (Math.abs(scrollForPage) * 1.0f) / (viewportWidth * 2));
        float f = viewportWidth;
        snapToPage(iValidateNewPage, scrollForPage, Math.round(Math.abs((f + (distanceInfluenceForSnapDuration(fMin) * f)) / Math.max(this.mMinSnapVelocity, Math.abs(i2))) * 1000.0f) * 4);
    }

    public boolean startReordering(View view) {
        int iIndexOfChild = indexOfChild(view);
        if (this.mTouchState != 0 || iIndexOfChild == -1) {
            return false;
        }
        int[] iArr = this.mTempVisiblePagesRange;
        iArr[0] = 0;
        iArr[1] = getPageCount() - 1;
        getFreeScrollPageRange(this.mTempVisiblePagesRange);
        this.mReorderingStarted = true;
        int[] iArr2 = this.mTempVisiblePagesRange;
        if (iArr2[0] > iIndexOfChild || iIndexOfChild > iArr2[1]) {
            return false;
        }
        this.mDragView = getChildAt(iIndexOfChild);
        this.mDragView.animate().scaleX(1.15f).scaleY(1.15f).setDuration(100L).start();
        this.mDragViewBaselineLeft = this.mDragView.getLeft();
        snapToPage(getPageNearestToCenterOfScreen());
        disableFreeScroll();
        onStartReordering();
        return true;
    }

    protected void updateCurrentPageScroll() {
        int i = this.mCurrentPage;
        int scrollForPage = (i < 0 || i >= getPageCount()) ? 0 : getScrollForPage(this.mCurrentPage);
        scrollTo(scrollForPage, 0);
        this.mScroller.setFinalX(scrollForPage);
        forceFinishScroller();
    }

    void updateFreescrollBounds() {
        getFreeScrollPageRange(this.mTempVisiblePagesRange);
        if (this.mIsRtl) {
            this.mFreeScrollMinScrollX = getScrollForPage(this.mTempVisiblePagesRange[1]);
            this.mFreeScrollMaxScrollX = getScrollForPage(this.mTempVisiblePagesRange[0]);
        } else {
            this.mFreeScrollMinScrollX = getScrollForPage(this.mTempVisiblePagesRange[0]);
            this.mFreeScrollMaxScrollX = getScrollForPage(this.mTempVisiblePagesRange[1]);
        }
    }

    void updateMaxScrollX() {
        int childCount = getChildCount();
        if (childCount > 0) {
            this.mMaxScrollX = getScrollForPage(this.mIsRtl ? 0 : childCount - 1);
        } else {
            this.mMaxScrollX = 0;
        }
    }
}
