package com.voxel.simplesearchlauncher.view.launcher;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.voxel.launcher3.DropTarget;
import com.voxel.launcher3.PagedView;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class IndicatorContainer extends FrameLayout implements PagedView.PageInteractionListener, DragController.DragListener {
    Animator.AnimatorListener mAllAppsAnimationListener;
    ImageView mAllAppsIndicator;
    ValueAnimator.AnimatorUpdateListener mAllAppsUpdateListener;
    ValueAnimator mCurrentAllAppsAnimator;
    private boolean mDragging;
    Handler mHandler;
    private Runnable mHideRunnable;
    View mPageIndicator;

    public IndicatorContainer(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAllAppsUpdateListener = new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.view.launcher.IndicatorContainer.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                IndicatorContainer.this.mAllAppsIndicator.setImageAlpha(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        };
        this.mAllAppsAnimationListener = new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.view.launcher.IndicatorContainer.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                IndicatorContainer.this.mCurrentAllAppsAnimator = null;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                IndicatorContainer.this.mCurrentAllAppsAnimator = null;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        };
        this.mHideRunnable = new Runnable() { // from class: com.voxel.simplesearchlauncher.view.launcher.-$$Lambda$IndicatorContainer$Dp3HMK5v5ZHmDJ3-WbBfdIeFymk
            @Override // java.lang.Runnable
            public final void run() {
                IndicatorContainer.this.hidePageIndicatorImpl();
            }
        };
    }

    private void hidePageIndicator() {
        if (this.mDragging) {
            return;
        }
        postDelayed(this.mHideRunnable, 500L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void hidePageIndicatorImpl() {
        removeCallbacks(this.mHideRunnable);
        this.mPageIndicator.animate().cancel();
        this.mPageIndicator.animate().alpha(0.0f).setDuration(225L);
        int imageAlpha = this.mAllAppsIndicator.getImageAlpha();
        ValueAnimator valueAnimator = this.mCurrentAllAppsAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mCurrentAllAppsAnimator = ValueAnimator.ofInt(imageAlpha, 255);
        this.mCurrentAllAppsAnimator.addUpdateListener(this.mAllAppsUpdateListener);
        this.mCurrentAllAppsAnimator.addListener(this.mAllAppsAnimationListener);
        this.mCurrentAllAppsAnimator.setDuration(225L);
        this.mCurrentAllAppsAnimator.start();
    }

    private void showPageIndicator() {
        removeCallbacks(this.mHideRunnable);
        this.mPageIndicator.animate().cancel();
        this.mPageIndicator.animate().alpha(1.0f).setDuration(100L);
        int imageAlpha = this.mAllAppsIndicator.getImageAlpha();
        ValueAnimator valueAnimator = this.mCurrentAllAppsAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mCurrentAllAppsAnimator = ValueAnimator.ofInt(imageAlpha, 0);
        this.mCurrentAllAppsAnimator.addUpdateListener(this.mAllAppsUpdateListener);
        this.mCurrentAllAppsAnimator.addListener(this.mAllAppsAnimationListener);
        this.mCurrentAllAppsAnimator.setDuration(100L);
        this.mCurrentAllAppsAnimator.start();
    }

    public ImageView getAllAppsIndicator() {
        return this.mAllAppsIndicator;
    }

    public View getPageIndicator() {
        return this.mPageIndicator;
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragEnd() {
        this.mDragging = false;
        hidePageIndicator();
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
        this.mDragging = true;
        showPageIndicator();
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPageIndicator = findViewById(R.id.page_indicator);
        this.mAllAppsIndicator = (ImageView) findViewById(R.id.all_apps_chevron);
        this.mPageIndicator.setAlpha(0.0f);
        this.mHandler = new Handler();
    }

    @Override // com.voxel.launcher3.PagedView.PageInteractionListener
    public void onPageBeginMoving() {
        showPageIndicator();
    }

    @Override // com.voxel.launcher3.PagedView.PageInteractionListener
    public void onPageEndMoving() {
        hidePageIndicator();
    }

    @Override // com.voxel.launcher3.PagedView.PageInteractionListener
    public void onScrollInteractionBegin() {
    }

    @Override // com.voxel.launcher3.PagedView.PageInteractionListener
    public void onScrollInteractionEnd() {
    }

    public void setAllAppsIndicator(ImageView imageView) {
        this.mAllAppsIndicator = imageView;
    }

    public void setPageIndicator(View view) {
        this.mPageIndicator = view;
    }
}
