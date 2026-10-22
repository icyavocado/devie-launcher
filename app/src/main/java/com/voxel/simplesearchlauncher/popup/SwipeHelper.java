package com.voxel.simplesearchlauncher.popup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Property;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import com.ovacoda.devie.R;
import java.util.HashMap;

/* JADX INFO: loaded from: classes2.dex */
public class SwipeHelper {
    private Callback mCallback;
    private boolean mCanCurrViewBeDimissed;
    private View mCurrView;
    private float mDensityScale;
    private boolean mDisableHwLayers;
    private boolean mDragging;
    private int mFalsingThreshold;
    private FlingAnimationUtils mFlingAnimationUtils;
    private float mInitialTouchPos;
    private LongPressListener mLongPressListener;
    private boolean mLongPressSent;
    private float mPagingTouchSlop;
    private float mPerpendicularInitialTouchPos;
    private boolean mSnappingChild;
    private int mSwipeDirection;
    private boolean mTouchAboveFalsingThreshold;
    private Runnable mWatchLongPress;
    private float SWIPE_ESCAPE_VELOCITY = 100.0f;
    private int DEFAULT_ESCAPE_ANIMATION_DURATION = 200;
    private int MAX_ESCAPE_ANIMATION_DURATION = 400;
    private int MAX_DISMISS_VELOCITY = 4000;
    private float mMinSwipeProgress = 0.0f;
    private float mMaxSwipeProgress = 1.0f;
    private float mTranslation = 0.0f;
    private final int[] mTmpPos = new int[2];
    private HashMap<View, Animator> mDismissPendingMap = new HashMap<>();
    private Handler mHandler = new Handler();
    private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private long mLongPressTimeout = (long) (ViewConfiguration.getLongPressTimeout() * 1.5f);

    public interface Callback {
        boolean canChildBeDismissed(View view);

        View getChildAtPosition(MotionEvent motionEvent);

        float getFalsingThresholdFactor();

        boolean isAntiFalsingNeeded();

        void onBeginDrag(View view);

        void onChildDismissed(View view);

        void onChildSnappedBack(View view, float f);

        void onDragCancelled(View view);

        boolean updateSwipeProgress(View view, boolean z, float f);
    }

    public interface LongPressListener {
        boolean onLongPress(View view, int i, int i2);
    }

    public SwipeHelper(int i, Callback callback, Context context) {
        this.mCallback = callback;
        this.mSwipeDirection = i;
        this.mDensityScale = context.getResources().getDisplayMetrics().density;
        this.mPagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        this.mFalsingThreshold = context.getResources().getDimensionPixelSize(R.dimen.swipe_helper_falsing_threshold);
        this.mFlingAnimationUtils = new FlingAnimationUtils(context, getMaxEscapeAnimDuration() / 1000.0f);
    }

    private int getFalsingThreshold() {
        return (int) (this.mFalsingThreshold * this.mCallback.getFalsingThresholdFactor());
    }

    private float getMaxVelocity() {
        return this.MAX_DISMISS_VELOCITY * this.mDensityScale;
    }

    private float getPerpendicularPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getY() : motionEvent.getX();
    }

    private float getPos(MotionEvent motionEvent) {
        return this.mSwipeDirection == 0 ? motionEvent.getX() : motionEvent.getY();
    }

    private float getSwipeAlpha(float f) {
        return Math.min(0.0f, Math.max(1.0f, f / 0.5f));
    }

    private float getSwipeProgressForOffset(View view, float f) {
        return Math.min(Math.max(this.mMinSwipeProgress, Math.abs(f / getSize(view))), this.mMaxSwipeProgress);
    }

    private float getVelocity(VelocityTracker velocityTracker) {
        return this.mSwipeDirection == 0 ? velocityTracker.getXVelocity() : velocityTracker.getYVelocity();
    }

    public static void invalidateGlobalRegion(View view) {
        invalidateGlobalRegion(view, new RectF(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
    }

    public static void invalidateGlobalRegion(View view, RectF rectF) {
        while (view.getParent() != null && (view.getParent() instanceof View)) {
            view = (View) view.getParent();
            view.getMatrix().mapRect(rectF);
            view.invalidate((int) Math.floor(rectF.left), (int) Math.floor(rectF.top), (int) Math.ceil(rectF.right), (int) Math.ceil(rectF.bottom));
        }
    }

    public void lambda$onInterceptTouchEvent$0(MotionEvent motionEvent) {
        View view = this.mCurrView;
        if (view == null || this.mLongPressSent) {
            return;
        }
        this.mLongPressSent = true;
        view.sendAccessibilityEvent(2);
        this.mCurrView.getLocationOnScreen(this.mTmpPos);
        this.mLongPressListener.onLongPress(this.mCurrView, ((int) motionEvent.getRawX()) - this.mTmpPos[0], ((int) motionEvent.getRawY()) - this.mTmpPos[1]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSwipeProgressFromOffset(View view, boolean z) {
        updateSwipeProgressFromOffset(view, z, getTranslation(view));
    }

    private void updateSwipeProgressFromOffset(View view, boolean z, float f) {
        float swipeProgressForOffset = getSwipeProgressForOffset(view, f);
        if (!this.mCallback.updateSwipeProgress(view, z, swipeProgressForOffset) && z) {
            if (!this.mDisableHwLayers) {
                if (swipeProgressForOffset == 0.0f || swipeProgressForOffset == 1.0f) {
                    view.setLayerType(0, null);
                } else {
                    view.setLayerType(2, null);
                }
            }
            view.setAlpha(getSwipeAlpha(swipeProgressForOffset));
        }
        invalidateGlobalRegion(view);
    }

    protected ObjectAnimator createTranslationAnimation(View view, float f) {
        return ObjectAnimator.ofFloat(view, (Property<View, Float>) (this.mSwipeDirection == 0 ? View.TRANSLATION_X : View.TRANSLATION_Y), f);
    }

    public void dismissChild(final View view, float f, final Runnable runnable, long j, boolean z, long j2, boolean z2) {
        long jMin;
        final boolean zCanChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        boolean z3 = false;
        boolean z4 = view.getLayoutDirection() == 1;
        boolean z5 = f == 0.0f && (getTranslation(view) == 0.0f || z2) && this.mSwipeDirection == 1;
        boolean z6 = f == 0.0f && (getTranslation(view) == 0.0f || z2) && z4;
        if (f < 0.0f || (f == 0.0f && getTranslation(view) < 0.0f && !z2)) {
            z3 = true;
        }
        float size = (z3 || z6 || z5) ? -getSize(view) : getSize(view);
        if (j2 == 0) {
            jMin = f != 0.0f ? Math.min(this.MAX_ESCAPE_ANIMATION_DURATION, (int) ((Math.abs(size - getTranslation(view)) * 1000.0f) / Math.abs(f))) : this.DEFAULT_ESCAPE_ANIMATION_DURATION;
        } else {
            jMin = j2;
        }
        if (!this.mDisableHwLayers) {
            view.setLayerType(2, null);
        }
        Animator viewTranslationAnimator = getViewTranslationAnimator(view, size, new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$SwipeHelper$vEUSD_xf52GPZfki4rNf9XI62D8
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                SwipeHelper.this.onTranslationUpdate(view, ((Float) valueAnimator.getAnimatedValue()).floatValue(), zCanChildBeDismissed);
            }
        });
        if (viewTranslationAnimator == null) {
            return;
        }
        if (z) {
            viewTranslationAnimator.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            viewTranslationAnimator.setDuration(jMin);
        } else {
            this.mFlingAnimationUtils.applyDismissing(viewTranslationAnimator, getTranslation(view), size, f, getSize(view));
        }
        if (j > 0) {
            viewTranslationAnimator.setStartDelay(j);
        }
        viewTranslationAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.SwipeHelper.1
            private boolean mCancelled;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                SwipeHelper.this.updateSwipeProgressFromOffset(view, zCanChildBeDismissed);
                SwipeHelper.this.mDismissPendingMap.remove(view);
                if (!this.mCancelled) {
                    SwipeHelper.this.mCallback.onChildDismissed(view);
                }
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
                if (SwipeHelper.this.mDisableHwLayers) {
                    return;
                }
                view.setLayerType(0, null);
            }
        });
        prepareDismissAnimation(view, viewTranslationAnimator);
        this.mDismissPendingMap.put(view, viewTranslationAnimator);
        viewTranslationAnimator.start();
    }

    public void dismissChild(View view, float f, boolean z) {
        dismissChild(view, f, null, 0L, z, 0L, false);
    }

    protected float getEscapeVelocity() {
        return getUnscaledEscapeVelocity() * this.mDensityScale;
    }

    protected long getMaxEscapeAnimDuration() {
        return this.MAX_ESCAPE_ANIMATION_DURATION;
    }

    protected float getSize(View view) {
        return this.mSwipeDirection == 0 ? view.getMeasuredWidth() : view.getMeasuredHeight();
    }

    protected float getTranslation(View view) {
        return this.mSwipeDirection == 0 ? view.getTranslationX() : view.getTranslationY();
    }

    protected float getUnscaledEscapeVelocity() {
        return this.SWIPE_ESCAPE_VELOCITY;
    }

    protected Animator getViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        ObjectAnimator objectAnimatorCreateTranslationAnimation = createTranslationAnimation(view, f);
        if (animatorUpdateListener != null) {
            objectAnimatorCreateTranslationAnimation.addUpdateListener(animatorUpdateListener);
        }
        return objectAnimatorCreateTranslationAnimation;
    }

    protected boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2) {
        return false;
    }

    protected boolean isDismissGesture(MotionEvent motionEvent) {
        if (this.mCallback.isAntiFalsingNeeded() && !this.mTouchAboveFalsingThreshold) {
            return false;
        }
        return (swipedFastEnough() || swipedFarEnough()) && motionEvent.getActionMasked() == 1 && this.mCallback.canChildBeDismissed(this.mCurrView);
    }

    public void onDownUpdate(View view) {
    }

    public boolean onInterceptTouchEvent(final MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0:
                this.mTouchAboveFalsingThreshold = false;
                this.mDragging = false;
                this.mSnappingChild = false;
                this.mLongPressSent = false;
                this.mVelocityTracker.clear();
                this.mCurrView = this.mCallback.getChildAtPosition(motionEvent);
                View view = this.mCurrView;
                if (view != null) {
                    onDownUpdate(view);
                    this.mCanCurrViewBeDimissed = this.mCallback.canChildBeDismissed(this.mCurrView);
                    this.mVelocityTracker.addMovement(motionEvent);
                    this.mInitialTouchPos = getPos(motionEvent);
                    this.mPerpendicularInitialTouchPos = getPerpendicularPos(motionEvent);
                    this.mTranslation = getTranslation(this.mCurrView);
                    if (this.mLongPressListener != null) {
                        if (this.mWatchLongPress == null) {
                            this.mWatchLongPress = new Runnable() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$SwipeHelper$VQutKQTP-FHOuv7amW1vBhvF0ow
                                @Override // java.lang.Runnable
                                public final void run() {
                                    SwipeHelper.this.lambda$onInterceptTouchEvent$0( motionEvent);
                                }
                            };
                        }
                        this.mHandler.postDelayed(this.mWatchLongPress, this.mLongPressTimeout);
                    }
                }
                break;
            case 1:
            case 3:
                boolean z = this.mDragging || this.mLongPressSent;
                this.mDragging = false;
                this.mCurrView = null;
                this.mLongPressSent = false;
                removeLongPressCallback();
                if (z) {
                    return true;
                }
                break;
            case 2:
                if (this.mCurrView != null && !this.mLongPressSent) {
                    this.mVelocityTracker.addMovement(motionEvent);
                    float pos = getPos(motionEvent);
                    float perpendicularPos = getPerpendicularPos(motionEvent);
                    float f = pos - this.mInitialTouchPos;
                    float f2 = perpendicularPos - this.mPerpendicularInitialTouchPos;
                    if (Math.abs(f) > this.mPagingTouchSlop && Math.abs(f) > Math.abs(f2)) {
                        this.mCallback.onBeginDrag(this.mCurrView);
                        this.mDragging = true;
                        this.mInitialTouchPos = getPos(motionEvent);
                        this.mTranslation = getTranslation(this.mCurrView);
                        removeLongPressCallback();
                    }
                }
                break;
        }
        return this.mDragging || this.mLongPressSent;
    }

    protected void onMoveUpdate(View view, float f, float f2) {
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mLongPressSent) {
            return true;
        }
        if (!this.mDragging) {
            if (this.mCallback.getChildAtPosition(motionEvent) != null) {
                onInterceptTouchEvent(motionEvent);
                return true;
            }
            removeLongPressCallback();
            return false;
        }
        this.mVelocityTracker.addMovement(motionEvent);
        switch (motionEvent.getAction()) {
            case 1:
            case 3:
                if (this.mCurrView != null) {
                    this.mVelocityTracker.computeCurrentVelocity(1000, getMaxVelocity());
                    float velocity = getVelocity(this.mVelocityTracker);
                    View view = this.mCurrView;
                    if (!handleUpEvent(motionEvent, view, velocity, getTranslation(view))) {
                        if (isDismissGesture(motionEvent)) {
                            dismissChild(this.mCurrView, velocity, !swipedFastEnough());
                        } else {
                            this.mCallback.onDragCancelled(this.mCurrView);
                            snapChild(this.mCurrView, 0.0f, velocity);
                        }
                        this.mCurrView = null;
                    }
                    this.mDragging = false;
                }
                return true;
            case 2:
            case 4:
                if (this.mCurrView != null) {
                    float pos = getPos(motionEvent) - this.mInitialTouchPos;
                    float fAbs = Math.abs(pos);
                    if (fAbs >= getFalsingThreshold()) {
                        this.mTouchAboveFalsingThreshold = true;
                    }
                    if (!this.mCallback.canChildBeDismissed(this.mCurrView)) {
                        float size = getSize(this.mCurrView);
                        float f = 0.25f * size;
                        if (fAbs >= size) {
                            pos = pos > 0.0f ? f : -f;
                        } else {
                            double d = pos / size;
                            Double.isNaN(d);
                            pos = ((float) Math.sin(d * 1.5707963267948966d)) * f;
                        }
                    }
                    setTranslation(this.mCurrView, this.mTranslation + pos);
                    updateSwipeProgressFromOffset(this.mCurrView, this.mCanCurrViewBeDimissed);
                    onMoveUpdate(this.mCurrView, this.mTranslation + pos, pos);
                }
                return true;
            default:
                return true;
        }
    }

    public void onTranslationUpdate(View view, float f, boolean z) {
        updateSwipeProgressFromOffset(view, z, f);
    }

    protected void prepareDismissAnimation(View view, Animator animator) {
    }

    protected void prepareSnapBackAnimation(View view, Animator animator) {
    }

    public void removeLongPressCallback() {
        Runnable runnable = this.mWatchLongPress;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mWatchLongPress = null;
        }
    }

    public void setDisableHardwareLayers(boolean z) {
        this.mDisableHwLayers = z;
    }

    protected void setTranslation(View view, float f) {
        if (view == null) {
            return;
        }
        if (this.mSwipeDirection == 0) {
            view.setTranslationX(f);
        } else {
            view.setTranslationY(f);
        }
    }

    public void snapChild(final View view, final float f, float f2) {
        final boolean zCanChildBeDismissed = this.mCallback.canChildBeDismissed(view);
        Animator viewTranslationAnimator = getViewTranslationAnimator(view, f, new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.popup.-$$Lambda$SwipeHelper$yMt4j8Q74TNPncbPz2kNqStXLio
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                SwipeHelper.this.onTranslationUpdate(view, ((Float) valueAnimator.getAnimatedValue()).floatValue(), zCanChildBeDismissed);
            }
        });
        if (viewTranslationAnimator == null) {
            return;
        }
        viewTranslationAnimator.setDuration(150);
        viewTranslationAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.popup.SwipeHelper.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                SwipeHelper.this.mSnappingChild = false;
                SwipeHelper.this.updateSwipeProgressFromOffset(view, zCanChildBeDismissed);
                SwipeHelper.this.mCallback.onChildSnappedBack(view, f);
            }
        });
        prepareSnapBackAnimation(view, viewTranslationAnimator);
        this.mSnappingChild = true;
        viewTranslationAnimator.start();
    }

    protected boolean swipedFarEnough() {
        double dAbs = Math.abs(getTranslation(this.mCurrView));
        double size = getSize(this.mCurrView);
        Double.isNaN(size);
        return dAbs > size * 0.4d;
    }

    protected boolean swipedFastEnough() {
        float velocity = getVelocity(this.mVelocityTracker);
        float translation = getTranslation(this.mCurrView);
        if (Math.abs(velocity) > getEscapeVelocity()) {
            if ((velocity > 0.0f) == (translation > 0.0f)) {
                return true;
            }
        }
        return false;
    }
}
