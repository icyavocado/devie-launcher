package com.voxel.simplesearchlauncher.search.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import com.evie.search.SearchPresenter;
import com.voxel.simplesearchlauncher.view.SlideLayout;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class NoRatingSlide extends TextSlide {
    private SearchPresenter mSearch;

    /* JADX INFO: renamed from: com.voxel.simplesearchlauncher.search.view.NoRatingSlide$1, reason: invalid class name */
    class AnonymousClass1 implements Animator.AnimatorListener {
        AnonymousClass1() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            NoRatingSlide.this.mView.postDelayed(new Runnable() { // from class: com.voxel.simplesearchlauncher.search.view.-$$Lambda$NoRatingSlide$1$czv5TYBaRHmrtc3KPhmEK6wPBqk
                @Override // java.lang.Runnable
                public final void run() {
                    NoRatingSlide.this.mSearch.removeCustomPresenter();
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

    public NoRatingSlide(Context context, SlideLayout slideLayout, SearchPresenter searchPresenter) {
        super(context, slideLayout);
        this.mSearch = searchPresenter;
        this.mTextView.setText(R.string.feedback_no_rating);
    }

    @Override // com.voxel.simplesearchlauncher.search.view.TextSlide, com.voxel.simplesearchlauncher.view.Slide
    public AnimatorSet getSlideEnteringAnimation(SlideLayout slideLayout) {
        AnimatorSet slideEnteringAnimation = super.getSlideEnteringAnimation(slideLayout);
        slideEnteringAnimation.addListener(new AnonymousClass1());
        return slideEnteringAnimation;
    }
}
