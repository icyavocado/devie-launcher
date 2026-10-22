package com.voxel.simplesearchlauncher.view;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class BaseRecyclerViewFastScrollBar {
    private boolean mCanThumbDetach;
    private boolean mIgnoreDragGesture;
    private boolean mIsDragging;
    private boolean mIsThumbDetached;
    private float mLastTouchY;
    private BaseRecyclerViewFastScrollPopup mPopup;
    BaseRecyclerView mRv;
    private AnimatorSet mScrollbarAnimator;
    private int mThumbActiveColor;
    private int mThumbCurvature;
    int mThumbHeight;
    private int mThumbInactiveColor;
    private int mThumbMaxWidth;
    private int mThumbMinWidth;
    Paint mThumbPaint;
    int mThumbWidth;
    private int mTouchInset;
    private int mTouchOffset;
    private int mTrackWidth;
    Point mThumbOffset = new Point(-1, -1);
    private Path mThumbPath = new Path();
    private Rect mInvalidateRect = new Rect();
    private Rect mTmpRect = new Rect();
    private Paint mTrackPaint = new Paint();

    public BaseRecyclerViewFastScrollBar(BaseRecyclerView baseRecyclerView, Resources resources) {
        this.mRv = baseRecyclerView;
        this.mPopup = new BaseRecyclerViewFastScrollPopup(baseRecyclerView, resources);
        this.mTrackPaint.setColor(baseRecyclerView.getFastScrollerTrackColor(-16777216));
        this.mTrackPaint.setAlpha(30);
        this.mThumbInactiveColor = baseRecyclerView.getFastScrollerThumbInactiveColor(resources.getColor(R.color.container_fastscroll_thumb_inactive_color));
        this.mThumbActiveColor = resources.getColor(R.color.container_fastscroll_thumb_active_color);
        this.mThumbPaint = new Paint();
        this.mThumbPaint.setAntiAlias(true);
        this.mThumbPaint.setColor(this.mThumbInactiveColor);
        this.mThumbPaint.setStyle(Paint.Style.FILL);
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.container_fastscroll_thumb_min_width);
        this.mThumbMinWidth = dimensionPixelSize;
        this.mThumbWidth = dimensionPixelSize;
        this.mThumbMaxWidth = resources.getDimensionPixelSize(R.dimen.container_fastscroll_thumb_max_width);
        this.mThumbHeight = resources.getDimensionPixelSize(R.dimen.container_fastscroll_thumb_height);
        this.mThumbCurvature = this.mThumbMaxWidth - this.mThumbMinWidth;
        this.mTouchInset = resources.getDimensionPixelSize(R.dimen.container_fastscroll_thumb_touch_inset);
    }

    public void lambda$showActiveScrollbar$0(ValueAnimator valueAnimator) {
        this.mThumbPaint.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
        this.mRv.invalidate(this.mThumbOffset.x, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
    }

    private void showActiveScrollbar(boolean z) {
        AnimatorSet animatorSet = this.mScrollbarAnimator;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        this.mScrollbarAnimator = new AnimatorSet();
        int[] iArr = new int[1];
        iArr[0] = z ? this.mThumbMaxWidth : this.mThumbMinWidth;
        ObjectAnimator objectAnimatorOfInt = ObjectAnimator.ofInt(this, "trackWidth", iArr);
        int[] iArr2 = new int[1];
        iArr2[0] = z ? this.mThumbMaxWidth : this.mThumbMinWidth;
        this.mScrollbarAnimator.playTogether(objectAnimatorOfInt, ObjectAnimator.ofInt(this, "thumbWidth", iArr2));
        if (this.mThumbActiveColor != this.mThumbInactiveColor) {
            ArgbEvaluator argbEvaluator = new ArgbEvaluator();
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(this.mThumbPaint.getColor());
            objArr[1] = Integer.valueOf(z ? this.mThumbActiveColor : this.mThumbInactiveColor);
            ValueAnimator valueAnimatorOfObject = ValueAnimator.ofObject(argbEvaluator, objArr);
            valueAnimatorOfObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.view.-$$Lambda$BaseRecyclerViewFastScrollBar$SulsH1AGAFhEd6o-0LWuE8nxvtY
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    BaseRecyclerViewFastScrollBar.this.lambda$showActiveScrollbar$0( valueAnimator);
                }
            });
            this.mScrollbarAnimator.play(valueAnimatorOfObject);
        }
        this.mScrollbarAnimator.setDuration(150L);
        this.mScrollbarAnimator.start();
    }

    private void updateThumbPath() {
        this.mThumbCurvature = this.mThumbMaxWidth - this.mThumbWidth;
        this.mThumbPath.reset();
        this.mThumbPath.moveTo(this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y);
        this.mThumbPath.lineTo(this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbPath.lineTo(this.mThumbOffset.x, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbPath.cubicTo(this.mThumbOffset.x, this.mThumbOffset.y + this.mThumbHeight, this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y + (this.mThumbHeight / 2), this.mThumbOffset.x, this.mThumbOffset.y);
        this.mThumbPath.close();
    }

    public void draw(Canvas canvas) {
        if (this.mThumbOffset.x < 0 || this.mThumbOffset.y < 0) {
            return;
        }
        if (this.mTrackPaint.getAlpha() > 0) {
            canvas.drawRect(this.mThumbOffset.x, 0.0f, this.mThumbOffset.x + this.mThumbWidth, this.mRv.getHeight(), this.mTrackPaint);
        }
        canvas.drawPath(this.mThumbPath, this.mThumbPaint);
        this.mPopup.draw(canvas);
    }

    public int getThumbHeight() {
        return this.mThumbHeight;
    }

    public int getThumbMaxWidth() {
        return this.mThumbMaxWidth;
    }

    public int getThumbWidth() {
        return this.mThumbWidth;
    }

    public int getTrackWidth() {
        return this.mTrackWidth;
    }

    public void handleTouchEvent(MotionEvent motionEvent, int i, int i2, int i3) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mRv.getContext());
        int action = motionEvent.getAction();
        int y = (int) motionEvent.getY();
        switch (action) {
            case 0:
                if (isNearThumb(i, i2)) {
                    this.mTouchOffset = i2 - this.mThumbOffset.y;
                }
                break;
            case 1:
            case 3:
                this.mTouchOffset = 0;
                this.mLastTouchY = 0.0f;
                this.mIgnoreDragGesture = false;
                if (this.mIsDragging) {
                    this.mIsDragging = false;
                    this.mPopup.animateVisibility(false);
                    showActiveScrollbar(false);
                }
                break;
            case 2:
                int i4 = y - i2;
                this.mIgnoreDragGesture |= Math.abs(i4) > viewConfiguration.getScaledPagingTouchSlop();
                if (!this.mIsDragging && !this.mIgnoreDragGesture && this.mRv.supportsFastScrolling() && isNearThumb(i, i3) && Math.abs(i4) > viewConfiguration.getScaledTouchSlop()) {
                    this.mRv.getParent().requestDisallowInterceptTouchEvent(true);
                    this.mIsDragging = true;
                    if (this.mCanThumbDetach) {
                        this.mIsThumbDetached = true;
                    }
                    this.mTouchOffset += i3 - i2;
                    this.mPopup.animateVisibility(true);
                    showActiveScrollbar(true);
                }
                if (this.mIsDragging) {
                    int i5 = this.mRv.getBackgroundPadding().top;
                    int height = (this.mRv.getHeight() - this.mRv.getBackgroundPadding().bottom) - this.mThumbHeight;
                    float fMax = Math.max(i5, Math.min(height, y - this.mTouchOffset));
                    String strScrollToPositionAtProgress = this.mRv.scrollToPositionAtProgress((fMax - i5) / (height - i5));
                    this.mPopup.setSectionName(strScrollToPositionAtProgress);
                    this.mPopup.animateVisibility(!strScrollToPositionAtProgress.isEmpty());
                    this.mRv.invalidate(this.mPopup.updateFastScrollerBounds(i3));
                    this.mLastTouchY = fMax;
                }
                break;
        }
    }

    public boolean isDraggingThumb() {
        return this.mIsDragging;
    }

    public boolean isNearThumb(int i, int i2) {
        this.mTmpRect.set(this.mThumbOffset.x, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        Rect rect = this.mTmpRect;
        int i3 = this.mTouchInset;
        rect.inset(i3, i3);
        return this.mTmpRect.contains(i, i2);
    }

    public void setDetachThumbOnFastScroll() {
        this.mCanThumbDetach = true;
    }

    public void setThumbOffset(int i, int i2) {
        if (this.mThumbOffset.x == i && this.mThumbOffset.y == i2) {
            return;
        }
        this.mInvalidateRect.set(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbOffset.set(i, i2);
        updateThumbPath();
        this.mInvalidateRect.union(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mRv.invalidate(this.mInvalidateRect);
    }

    public void setThumbWidth(int i) {
        this.mInvalidateRect.set(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mThumbWidth = i;
        updateThumbPath();
        this.mInvalidateRect.union(this.mThumbOffset.x - this.mThumbCurvature, this.mThumbOffset.y, this.mThumbOffset.x + this.mThumbWidth, this.mThumbOffset.y + this.mThumbHeight);
        this.mRv.invalidate(this.mInvalidateRect);
    }

    public void setTrackWidth(int i) {
        this.mInvalidateRect.set(this.mThumbOffset.x - this.mThumbCurvature, 0, this.mThumbOffset.x + this.mThumbWidth, this.mRv.getHeight());
        this.mTrackWidth = i;
        updateThumbPath();
        this.mInvalidateRect.union(this.mThumbOffset.x - this.mThumbCurvature, 0, this.mThumbOffset.x + this.mThumbWidth, this.mRv.getHeight());
        this.mRv.invalidate(this.mInvalidateRect);
    }
}
