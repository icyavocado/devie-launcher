package com.voxel.simplesearchlauncher.search.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import com.evie.search.SearchPresenter;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class NoFeedbackSlide extends TextSlide {
    private SearchPresenter mSearch;

    /* JADX INFO: renamed from: com.voxel.simplesearchlauncher.search.view.NoFeedbackSlide$1, reason: invalid class name */
    class AnonymousClass1 implements Animator.AnimatorListener {
        AnonymousClass1() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            NoFeedbackSlide.this.mView.postDelayed(new Runnable() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$NoFeedbackSlide$1$cZcm3YW0Wdauu2OuIqYuOph9RK0
                @Override // java.lang.Runnable
                public final void run() {
                    NoFeedbackSlide.this.mSearch.removeCustomPresenter();
                }
            }, 3000L);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
        }
    }

    public NoFeedbackSlide(Context context, SlideLayout slideLayout, SearchPresenter searchPresenter) {
        super(context, slideLayout);
        this.mSearch = searchPresenter;
        this.mTextView.setText(R.string.feedback_no_feedback);
    }

    @Override // com.voxel.simplesearchlauncher.search.view.TextSlide, com.voxel.simplesearchlauncher.view.Slide
    public AnimatorSet getSlideEnteringAnimation(SlideLayout slideLayout) {
        AnimatorSet slideEnteringAnimation = super.getSlideEnteringAnimation(slideLayout);
        slideEnteringAnimation.addListener(new AnonymousClass1());
        return slideEnteringAnimation;
    }
}
