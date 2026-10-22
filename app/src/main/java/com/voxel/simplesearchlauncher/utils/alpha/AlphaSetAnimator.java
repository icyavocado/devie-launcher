package com.voxel.simplesearchlauncher.utils.alpha;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;
import com.voxel.simplesearchlauncher.utils.FirstFrameAnimatorHelper;
import java.util.Iterator;

/* JADX INFO: loaded from: classes2.dex */
public class AlphaSetAnimator extends ValueAnimator {
    private AlphaSet.AnimatableProperty mProperty;

    private AlphaSetAnimator(AlphaSet.AnimatableProperty animatableProperty) {
        this.mProperty = animatableProperty;
        addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.utils.alpha.AlphaSetAnimator.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AlphaSetAnimator.this.mProperty.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        if (this.mProperty.mAlphaSet.mNormalViews.size() > 0) {
            new FirstFrameAnimatorHelper(this, this.mProperty.mAlphaSet.mNormalViews.get(0));
            addListener(new Animator.AnimatorListener() { // from class: com.voxel.simplesearchlauncher.utils.alpha.AlphaSetAnimator.2
                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationCancel(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    Iterator<View> it = AlphaSetAnimator.this.mProperty.mAlphaSet.mNormalViews.iterator();
                    while (it.hasNext()) {
                        it.next().setLayerType(0, null);
                    }
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationRepeat(Animator animator) {
                }

                @Override // android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    Iterator<View> it = AlphaSetAnimator.this.mProperty.mAlphaSet.mNormalViews.iterator();
                    while (it.hasNext()) {
                        it.next().setLayerType(2, null);
                    }
                }
            });
        }
    }

    public static AlphaSetAnimator ofFloat(AlphaSet alphaSet, Object obj, float f) {
        AlphaSetAnimator alphaSetAnimator = new AlphaSetAnimator(new AlphaSet.AnimatableProperty(alphaSet, obj));
        alphaSetAnimator.setFloatValues(alphaSet.getValueForKey(obj, 1.0f).floatValue(), f);
        return alphaSetAnimator;
    }
}
