package com.voxel.launcher3.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.util.Property;

/* JADX INFO: loaded from: classes.dex */
public class PropertyResetListener<T, V> extends AnimatorListenerAdapter {
    private Property<T, V> mPropertyToReset;
    private V mResetToValue;

    public PropertyResetListener(Property<T, V> property, V v) {
        this.mPropertyToReset = property;
        this.mResetToValue = v;
    }

    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animator) {
        this.mPropertyToReset.set((T) ((ObjectAnimator) animator).getTarget(), this.mResetToValue);
    }
}
