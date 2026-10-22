package com.voxel.simplesearchlauncher.view;

import android.animation.AnimatorSet;
import android.view.View;

/* JADX INFO: loaded from: classes2.dex */
public abstract class Slide {
    protected final View mView;

    public Slide(View view) {
        this.mView = view;
    }

    public abstract AnimatorSet getSlideEnteringAnimation(SlideLayout slideLayout);

    public abstract AnimatorSet getSlideExitingAnimation(SlideLayout slideLayout);

    final View getView() {
        return this.mView;
    }

    public void onEnteringSlide() {
    }

    public void onExitingSlide() {
    }
}
