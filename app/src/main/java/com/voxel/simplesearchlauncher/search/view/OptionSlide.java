package com.voxel.simplesearchlauncher.search.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import com.voxel.simplesearchlauncher.view.Slide;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class OptionSlide extends Slide {
    protected TimeInterpolator mInterpolator;
    private GradientDrawable mNegativeBackground;
    protected Button mNegativeButton;
    private GradientDrawable mPositiveBackground;
    protected Button mPositiveButton;
    protected TextView mTextView;

    public OptionSlide(Context context, ViewGroup viewGroup) {
        super(LayoutInflater.from(context).inflate(R.layout.search_feedback_option_slide, viewGroup, false));
        this.mInterpolator = new DecelerateInterpolator();
        this.mTextView = (TextView) this.mView.findViewById(R.id.feedback_text);
        this.mPositiveButton = (Button) this.mView.findViewById(R.id.feedback_ok_button);
        this.mNegativeButton = (Button) this.mView.findViewById(R.id.feedback_cancel_button);
        this.mPositiveBackground = (GradientDrawable) context.getResources().getDrawable(R.drawable.feedback_positive_rounded_bg);
        this.mPositiveBackground.setCornerRadius(this.mPositiveButton.getHeight() / 2);
        this.mPositiveButton.setBackground(this.mPositiveBackground);
        this.mPositiveButton.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$OptionSlide$6GBJhmhyqEw0X58j6G8LrwAN7eQ
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                OptionSlide.this.lambda$new$0( view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mNegativeBackground = (GradientDrawable) context.getResources().getDrawable(R.drawable.feedback_negative_rounded_bg);
        this.mNegativeBackground.setCornerRadius(this.mNegativeButton.getHeight() / 2);
        this.mNegativeButton.setBackground(this.mNegativeBackground);
        this.mNegativeButton.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$OptionSlide$PP3YLkAILiqePuYsbcsnut60vyY
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                OptionSlide.this.lambda$new$1( view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
    }

    public void lambda$new$0(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i2 == i6 && i3 == i7 && i4 == i8) {
            return;
        }
        this.mPositiveBackground.setCornerRadius(view.getHeight() / 2.0f);
    }

    public void lambda$new$1(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i2 == i6 && i3 == i7 && i4 == i8) {
            return;
        }
        this.mNegativeBackground.setCornerRadius(view.getHeight() / 2.0f);
    }

    @Override // com.voxel.simplesearchlauncher.view.Slide
    public AnimatorSet getSlideEnteringAnimation(SlideLayout slideLayout) {
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.ALPHA, 0.0f, 1.0f);
        objectAnimatorOfFloat.setInterpolator(this.mInterpolator);
        objectAnimatorOfFloat.setDuration(400L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.search.view.OptionSlide.1
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                OptionSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OptionSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OptionSlide.this.mView.setLayerType(2, null);
            }
        });
        animatorSet.play(objectAnimatorOfFloat);
        return animatorSet;
    }

    @Override // com.voxel.simplesearchlauncher.view.Slide
    public AnimatorSet getSlideExitingAnimation(SlideLayout slideLayout) {
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.ALPHA, 1.0f, 0.0f);
        objectAnimatorOfFloat.setInterpolator(this.mInterpolator);
        objectAnimatorOfFloat.setDuration(400L);
        this.mView.setPivotY(this.mView.getHeight() / 2.0f);
        this.mView.setPivotX(this.mView.getWidth() / 2.0f);
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.SCALE_X, 1.0f, 0.85f);
        objectAnimatorOfFloat2.setInterpolator(this.mInterpolator);
        objectAnimatorOfFloat2.setDuration(400L);
        ObjectAnimator objectAnimatorOfFloat3 = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.SCALE_Y, 1.0f, 0.85f);
        objectAnimatorOfFloat3.setInterpolator(this.mInterpolator);
        objectAnimatorOfFloat3.setDuration(400L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.search.view.OptionSlide.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                OptionSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                OptionSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                OptionSlide.this.mView.setLayerType(2, null);
            }
        });
        animatorSet.play(objectAnimatorOfFloat);
        animatorSet.play(objectAnimatorOfFloat2);
        animatorSet.play(objectAnimatorOfFloat3);
        return animatorSet;
    }

    @Override // com.voxel.simplesearchlauncher.view.Slide
    public void onEnteringSlide() {
        this.mView.setAlpha(0.0f);
    }
}
