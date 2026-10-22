package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import com.voxel.simplesearchlauncher.utils.FirstFrameAnimatorHelper;
import java.util.ArrayList;
import java.util.EnumSet;

/* JADX INFO: loaded from: classes.dex */
public class LauncherViewPropertyAnimator extends Animator implements Animator.AnimatorListener {
    float mAlpha;
    long mDuration;
    FirstFrameAnimatorHelper mFirstFrameHelper;
    TimeInterpolator mInterpolator;
    float mRotationY;
    float mScaleX;
    float mScaleY;
    long mStartDelay;
    View mTarget;
    float mTranslationX;
    float mTranslationY;
    ViewPropertyAnimator mViewPropertyAnimator;
    EnumSet<Properties> mPropertiesToSet = EnumSet.noneOf(Properties.class);
    boolean mRunning = false;
    ArrayList<Animator.AnimatorListener> mListeners = new ArrayList<>();

    enum Properties {
        TRANSLATION_X,
        TRANSLATION_Y,
        SCALE_X,
        SCALE_Y,
        ROTATION_Y,
        ALPHA,
        START_DELAY,
        DURATION,
        INTERPOLATOR,
        WITH_LAYER
    }

    public LauncherViewPropertyAnimator(View view) {
        this.mTarget = view;
    }

    @Override // android.animation.Animator
    public void addListener(Animator.AnimatorListener animatorListener) {
        this.mListeners.add(animatorListener);
    }

    public LauncherViewPropertyAnimator alpha(float f) {
        this.mPropertiesToSet.add(Properties.ALPHA);
        this.mAlpha = f;
        return this;
    }

    @Override // android.animation.Animator
    public void cancel() {
        ViewPropertyAnimator viewPropertyAnimator = this.mViewPropertyAnimator;
        if (viewPropertyAnimator != null) {
            viewPropertyAnimator.cancel();
        }
    }

    @Override // android.animation.Animator
    public Animator clone() {
        throw new RuntimeException("Not implemented");
    }

    @Override // android.animation.Animator
    public long getDuration() {
        return this.mDuration;
    }

    @Override // android.animation.Animator
    public ArrayList<Animator.AnimatorListener> getListeners() {
        return this.mListeners;
    }

    @Override // android.animation.Animator
    public long getStartDelay() {
        return this.mStartDelay;
    }

    @Override // android.animation.Animator
    public boolean isRunning() {
        return this.mRunning;
    }

    @Override // android.animation.Animator
    public boolean isStarted() {
        return this.mViewPropertyAnimator != null;
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationCancel(Animator animator) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onAnimationCancel(this);
        }
        this.mRunning = false;
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animator) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onAnimationEnd(this);
        }
        this.mRunning = false;
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationRepeat(Animator animator) {
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onAnimationRepeat(this);
        }
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animator) {
        this.mFirstFrameHelper.onAnimationStart(animator);
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onAnimationStart(this);
        }
        this.mRunning = true;
    }

    @Override // android.animation.Animator
    public void removeAllListeners() {
        this.mListeners.clear();
    }

    @Override // android.animation.Animator
    public void removeListener(Animator.AnimatorListener animatorListener) {
        this.mListeners.remove(animatorListener);
    }

    public LauncherViewPropertyAnimator scaleX(float f) {
        this.mPropertiesToSet.add(Properties.SCALE_X);
        this.mScaleX = f;
        return this;
    }

    public LauncherViewPropertyAnimator scaleY(float f) {
        this.mPropertiesToSet.add(Properties.SCALE_Y);
        this.mScaleY = f;
        return this;
    }

    @Override // android.animation.Animator
    public Animator setDuration(long j) {
        this.mPropertiesToSet.add(Properties.DURATION);
        this.mDuration = j;
        return this;
    }

    @Override // android.animation.Animator
    public void setInterpolator(TimeInterpolator timeInterpolator) {
        this.mPropertiesToSet.add(Properties.INTERPOLATOR);
        this.mInterpolator = timeInterpolator;
    }

    @Override // android.animation.Animator
    public void setStartDelay(long j) {
        this.mPropertiesToSet.add(Properties.START_DELAY);
        this.mStartDelay = j;
    }

    @Override // android.animation.Animator
    public void setTarget(Object obj) {
        throw new RuntimeException("Not implemented");
    }

    @Override // android.animation.Animator
    public void setupEndValues() {
    }

    @Override // android.animation.Animator
    public void setupStartValues() {
    }

    @Override // android.animation.Animator
    public void start() {
        this.mViewPropertyAnimator = this.mTarget.animate();
        this.mFirstFrameHelper = new FirstFrameAnimatorHelper(this.mViewPropertyAnimator, this.mTarget);
        if (this.mPropertiesToSet.contains(Properties.TRANSLATION_X)) {
            this.mViewPropertyAnimator.translationX(this.mTranslationX);
        }
        if (this.mPropertiesToSet.contains(Properties.TRANSLATION_Y)) {
            this.mViewPropertyAnimator.translationY(this.mTranslationY);
        }
        if (this.mPropertiesToSet.contains(Properties.SCALE_X)) {
            this.mViewPropertyAnimator.scaleX(this.mScaleX);
        }
        if (this.mPropertiesToSet.contains(Properties.ROTATION_Y)) {
            this.mViewPropertyAnimator.rotationY(this.mRotationY);
        }
        if (this.mPropertiesToSet.contains(Properties.SCALE_Y)) {
            this.mViewPropertyAnimator.scaleY(this.mScaleY);
        }
        if (this.mPropertiesToSet.contains(Properties.ALPHA)) {
            this.mViewPropertyAnimator.alpha(this.mAlpha);
        }
        if (this.mPropertiesToSet.contains(Properties.START_DELAY)) {
            this.mViewPropertyAnimator.setStartDelay(this.mStartDelay);
        }
        if (this.mPropertiesToSet.contains(Properties.DURATION)) {
            this.mViewPropertyAnimator.setDuration(this.mDuration);
        }
        if (this.mPropertiesToSet.contains(Properties.INTERPOLATOR)) {
            this.mViewPropertyAnimator.setInterpolator(this.mInterpolator);
        }
        if (this.mPropertiesToSet.contains(Properties.WITH_LAYER)) {
            this.mViewPropertyAnimator.withLayer();
        }
        this.mViewPropertyAnimator.setListener(this);
        this.mViewPropertyAnimator.start();
        LauncherAnimUtils.cancelOnDestroyActivity(this);
    }

    public LauncherViewPropertyAnimator translationY(float f) {
        this.mPropertiesToSet.add(Properties.TRANSLATION_Y);
        this.mTranslationY = f;
        return this;
    }

    public LauncherViewPropertyAnimator withLayer() {
        this.mPropertiesToSet.add(Properties.WITH_LAYER);
        return this;
    }
}
