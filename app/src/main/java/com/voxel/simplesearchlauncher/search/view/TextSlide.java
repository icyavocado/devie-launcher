package com.voxel.simplesearchlauncher.search.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import com.voxel.simplesearchlauncher.view.Slide;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class TextSlide extends Slide {
    protected TimeInterpolator mInterpolator;
    protected TextView mTextView;

    public TextSlide(Context context, ViewGroup viewGroup) {
        super(LayoutInflater.from(context).inflate(R.layout.search_feedback_text_slide, viewGroup, false));
        this.mInterpolator = new DecelerateInterpolator();
        this.mTextView = (TextView) this.mView.findViewById(R.id.feedback_text);
    }

    @Override // com.voxel.simplesearchlauncher.view.Slide
    public AnimatorSet getSlideEnteringAnimation(SlideLayout slideLayout) {
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.ALPHA, 0.0f, 1.0f);
        objectAnimatorOfFloat.setInterpolator(this.mInterpolator);
        objectAnimatorOfFloat.setDuration(400L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.search.view.TextSlide.1
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                TextSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                TextSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                TextSlide.this.mView.setLayerType(2, null);
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
        ObjectAnimator objectAnimatorOfFloat2 = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.SCALE_X, 1.0f, 0.75f);
        objectAnimatorOfFloat2.setInterpolator(this.mInterpolator);
        objectAnimatorOfFloat2.setDuration(400L);
        ObjectAnimator objectAnimatorOfFloat3 = ObjectAnimator.ofFloat(this.mView, (Property<View, Float>) View.SCALE_Y, 1.0f, 0.75f);
        objectAnimatorOfFloat3.setInterpolator(this.mInterpolator);
        objectAnimatorOfFloat3.setDuration(400L);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.search.view.TextSlide.2
            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                TextSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                TextSlide.this.mView.setLayerType(0, null);
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationRepeat(Animator animator) {
            }

            @Override // android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                TextSlide.this.mView.setLayerType(2, null);
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
