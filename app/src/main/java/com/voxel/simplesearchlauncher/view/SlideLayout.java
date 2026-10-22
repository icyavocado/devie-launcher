package com.voxel.simplesearchlauncher.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class SlideLayout extends FrameLayout {
    boolean isAnimating;
    int mCurrentChild;
    List<Slide> mSlides;

    public SlideLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSlides = new ArrayList();
        this.isAnimating = false;
        this.mCurrentChild = 0;
    }

    public void addSlide(Slide slide, int i) {
        int i2;
        View view = slide.getView();
        super.addView(view, i);
        this.mSlides.add(i, slide);
        if (getChildCount() == 1) {
            view.setVisibility(0);
            slide.getSlideEnteringAnimation(this).start();
        } else {
            view.setVisibility(8);
        }
        if (i < 0 || (i2 = this.mCurrentChild) < i) {
            return;
        }
        setDisplayedChild(i2 + 1);
    }

    @Override // android.view.View
    public int getBaseline() {
        return getCurrentView() != null ? getCurrentView().getBaseline() : super.getBaseline();
    }

    public View getCurrentView() {
        return getChildAt(this.mCurrentChild);
    }

    public int getDisplayedChild() {
        return this.mCurrentChild;
    }

    public Slide getSlide(int i) {
        return this.mSlides.get(i);
    }

    public int indexOfSlide(Slide slide) {
        return this.mSlides.indexOf(slide);
    }

    public boolean isAnimating() {
        return this.isAnimating;
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(SlideLayout.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(SlideLayout.class.getName());
    }

    @Override // android.view.ViewGroup
    public void removeAllViews() {
        super.removeAllViews();
        this.mCurrentChild = 0;
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void removeView(View view) {
        int iIndexOfChild = indexOfChild(view);
        if (iIndexOfChild >= 0) {
            removeViewAt(iIndexOfChild);
            this.mSlides.remove(iIndexOfChild);
        }
    }

    @Override // android.view.ViewGroup
    public void removeViewAt(int i) {
        super.removeViewAt(i);
        int childCount = getChildCount();
        if (childCount == 0) {
            this.mCurrentChild = 0;
            return;
        }
        int i2 = this.mCurrentChild;
        if (i2 >= childCount) {
            setDisplayedChild(childCount - 1);
        } else if (i2 == i) {
            setDisplayedChild(i2);
        }
    }

    @Override // android.view.ViewGroup
    public void removeViewInLayout(View view) {
        removeView(view);
    }

    @Override // android.view.ViewGroup
    public void removeViews(int i, int i2) {
        super.removeViews(i, i2);
        if (getChildCount() == 0) {
            this.mCurrentChild = 0;
            return;
        }
        int i3 = this.mCurrentChild;
        if (i3 < i || i3 >= i + i2) {
            return;
        }
        setDisplayedChild(i3);
    }

    @Override // android.view.ViewGroup
    public void removeViewsInLayout(int i, int i2) {
        removeViews(i, i2);
    }

    public void setDisplayedChild(int i) {
        final int childCount;
        if (getChildCount() == 0) {
            return;
        }
        if (i >= getChildCount()) {
            childCount = 0;
        } else {
            childCount = i < 0 ? getChildCount() - 1 : i;
        }
        int i2 = this.mCurrentChild;
        if (i2 == childCount) {
            return;
        }
        final Slide slide = this.mSlides.get(i2);
        final Slide slide2 = this.mSlides.get(childCount);
        AnimatorSet slideExitingAnimation = slide.getSlideExitingAnimation(this);
        final AnimatorSet slideEnteringAnimation = slide2.getSlideEnteringAnimation(this);
        slideEnteringAnimation.addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.view.SlideLayout.1
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                SlideLayout.this.isAnimating = false;
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        });
        slideExitingAnimation.addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.view.SlideLayout.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                slide.getView().setVisibility(8);
                slide.onExitingSlide();
                slide2.onEnteringSlide();
                slide2.getView().setVisibility(0);
                SlideLayout.this.mCurrentChild = childCount;
                slideEnteringAnimation.start();
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
            }
        });
        this.isAnimating = true;
        slideExitingAnimation.start();
    }

    public void showNext() {
        setDisplayedChild(this.mCurrentChild + 1);
    }
}
