package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import com.voxel.simplesearchlauncher.utils.FirstFrameAnimatorHelper;
import java.util.HashSet;
import java.util.WeakHashMap;

/* JADX INFO: loaded from: classes.dex */
public class LauncherAnimUtils {
    static WeakHashMap<Animator, Object> sAnimators = new WeakHashMap<>();
    static Animator.AnimatorListener sEndAnimListener = new Animator.AnimatorListener() { // from class: com.voxel.launcher3.LauncherAnimUtils.1
        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
            LauncherAnimUtils.sAnimators.remove(animator);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            LauncherAnimUtils.sAnimators.remove(animator);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            LauncherAnimUtils.sAnimators.put(animator, null);
        }
    };

    public static void cancelOnDestroyActivity(Animator animator) {
        animator.addListener(sEndAnimListener);
    }

    public static AnimatorSet createAnimatorSet() {
        AnimatorSet animatorSet = new AnimatorSet();
        cancelOnDestroyActivity(animatorSet);
        return animatorSet;
    }

    public static ObjectAnimator ofFloat(View view, String str, float... fArr) {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        objectAnimator.setTarget(view);
        objectAnimator.setPropertyName(str);
        objectAnimator.setFloatValues(fArr);
        cancelOnDestroyActivity(objectAnimator);
        new FirstFrameAnimatorHelper(objectAnimator, view);
        return objectAnimator;
    }

    public static ValueAnimator ofFloat(View view, float... fArr) {
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setFloatValues(fArr);
        cancelOnDestroyActivity(valueAnimator);
        return valueAnimator;
    }

    public static ObjectAnimator ofPropertyValuesHolder(View view, PropertyValuesHolder... propertyValuesHolderArr) {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        objectAnimator.setTarget(view);
        objectAnimator.setValues(propertyValuesHolderArr);
        cancelOnDestroyActivity(objectAnimator);
        new FirstFrameAnimatorHelper(objectAnimator, view);
        return objectAnimator;
    }

    public static ObjectAnimator ofPropertyValuesHolder(Object obj, View view, PropertyValuesHolder... propertyValuesHolderArr) {
        ObjectAnimator objectAnimator = new ObjectAnimator();
        objectAnimator.setTarget(obj);
        objectAnimator.setValues(propertyValuesHolderArr);
        cancelOnDestroyActivity(objectAnimator);
        new FirstFrameAnimatorHelper(objectAnimator, view);
        return objectAnimator;
    }

    public static void onDestroyActivity() {
        /* REWORK: jadx raw HashSet */ for (Animator animator : new HashSet<Animator>(sAnimators.keySet())) {
            if (animator.isRunning()) {
                animator.cancel();
            }
            sAnimators.remove(animator);
        }
    }
}
