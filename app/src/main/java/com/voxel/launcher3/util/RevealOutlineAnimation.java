package com.voxel.launcher3.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewOutlineProvider;
import com.voxel.launcher3.Utilities;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
public abstract class RevealOutlineAnimation extends ViewOutlineProvider {
    protected Rect mOutline = new Rect();
    protected float mOutlineRadius;

    public void lambda$createRevealAnimator$0(View view, ValueAnimator valueAnimator) {
        this.setProgress(((Float) valueAnimator.getAnimatedValue()).floatValue());
        view.invalidateOutline();
        if (Utilities.ATLEAST_LOLLIPOP_MR1) {
            return;
        }
        view.invalidate();
    }

    public ValueAnimator createRevealAnimator(final View view, boolean z) {
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(z ? new float[]{1.0f, 0.0f} : new float[]{0.0f, 1.0f});
        final float elevation = view.getElevation();
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.util.RevealOutlineAnimation.1
            private boolean mWasCanceled = false;

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                this.mWasCanceled = true;
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (this.mWasCanceled) {
                    return;
                }
                view.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
                view.setClipToOutline(false);
                if (RevealOutlineAnimation.this.shouldRemoveElevationDuringAnimation()) {
                    view.setTranslationZ(0.0f);
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                view.setOutlineProvider(RevealOutlineAnimation.this);
                view.setClipToOutline(true);
                if (RevealOutlineAnimation.this.shouldRemoveElevationDuringAnimation()) {
                    view.setTranslationZ(-elevation);
                }
            }
        });
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.util.-$$Lambda$RevealOutlineAnimation$ni_OqtFjG4sy4Adyybh5TUuV4d0
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                RevealOutlineAnimation.this.lambda$createRevealAnimator$0( view, valueAnimator);
            }
        });
        return valueAnimatorOfFloat;
    }

    @Override // android.view.ViewOutlineProvider
    public void getOutline(View view, Outline outline) {
        outline.setRoundRect(this.mOutline, this.mOutlineRadius);
    }

    public abstract void setProgress(float f);

    abstract boolean shouldRemoveElevationDuringAnimation();
}
