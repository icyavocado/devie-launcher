package com.voxel.simplesearchlauncher.touch;

import android.content.Context;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

/* JADX INFO: loaded from: classes2.dex */
public class SpringyMotionTracker {
    private Context mContext;
    private float mCurrentFactor;
    protected float mDensity;
    private Direction mDirection;
    private int mFactorDistance;
    protected int mFlingThresholdVelocity;
    private boolean mIsTouching;
    protected int mMinFlingVelocity;
    protected int mMinSnapVelocity;
    private float mMotionFactor;
    private OnFactorChangeListener mOnFactorChangeListener;
    private float mPreviousCoord = 0.0f;
    private Spring mSpring;
    private SpringListener mSpringListener;
    private SpringSystem mSpringSystem;
    VelocityTracker mVelocityTracker;

    public enum Direction {
        HORIZONTAL,
        VERTICAL
    }

    public interface OnFactorChangeListener {
        void onFactorBeginChanging();

        void onFactorChanged(float f);

        void onFactorEndChanging();
    }

    public SpringyMotionTracker(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null!");
        }
        this.mContext = context;
        this.mDensity = this.mContext.getResources().getDisplayMetrics().density;
        float f = this.mDensity;
        this.mFlingThresholdVelocity = (int) (500.0f * f);
        this.mMinFlingVelocity = (int) (250.0f * f);
        this.mMinSnapVelocity = (int) (f * 1500.0f);
        this.mDirection = Direction.HORIZONTAL;
        this.mFactorDistance = 0;
        this.mMotionFactor = 1.0f;
        this.mSpringSystem = SpringSystem.create();
        this.mSpring = this.mSpringSystem.createSpring();
        this.mSpring.setCurrentValue(0.0d);
        this.mSpringListener = new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.touch.SpringyMotionTracker.1
            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringActivate(Spring spring) {
                SpringyMotionTracker springyMotionTracker = SpringyMotionTracker.this;
                springyMotionTracker.notifyListenerBeginIfNeeded(springyMotionTracker.mIsTouching, SpringyMotionTracker.this.mIsTouching, false, true);
            }

            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringAtRest(Spring spring) {
                SpringyMotionTracker.this.setFactorBySpring((float) spring.getCurrentValue());
                SpringyMotionTracker springyMotionTracker = SpringyMotionTracker.this;
                springyMotionTracker.notifyListenerEndIfNeeded(springyMotionTracker.mIsTouching, SpringyMotionTracker.this.mIsTouching, true, false);
            }

            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                SpringyMotionTracker.this.setFactorBySpring((float) spring.getCurrentValue());
            }
        };
        this.mSpring.addListener(this.mSpringListener);
    }

    private Float calculateEndValueWithVelocity(float f) {
        int i = this.mFlingThresholdVelocity;
        if (f > i) {
            return Float.valueOf(1.0f);
        }
        if ((-f) > i) {
            return Float.valueOf(0.0f);
        }
        return null;
    }

    private float calculateNewFactor(float f) {
        float f2 = this.mCurrentFactor;
        float f3 = (f * this.mMotionFactor) / this.mFactorDistance;
        float f4 = f3 + f2;
        float fCalculateOvershootFactor = 1.0f;
        if (f4 > 1.0f && f3 > 0.0f) {
            fCalculateOvershootFactor = calculateOvershootFactor(f4);
        } else if (f4 < 0.0f && f3 < 0.0f) {
            fCalculateOvershootFactor = calculateOvershootFactor(f4);
        }
        return (f3 / fCalculateOvershootFactor) + f2;
    }

    private float calculateOvershootFactor(float f) {
        if (f > 1.0f) {
            return f * 5.0f;
        }
        if (f < 0.0f) {
            return (Math.abs(f) + 1.0f) * 5.0f;
        }
        return 1.0f;
    }

    private float getRelevantCoordinateFromEvent(MotionEvent motionEvent) {
        switch (this.mDirection) {
            case HORIZONTAL:
                return motionEvent.getX();
            case VERTICAL:
                return motionEvent.getY();
            default:
                throw new UnsupportedOperationException("Unhandled direction type!");
        }
    }

    private float getRelevantVelocityFromTracker(VelocityTracker velocityTracker) {
        switch (this.mDirection) {
            case HORIZONTAL:
                return velocityTracker.getXVelocity();
            case VERTICAL:
                return velocityTracker.getYVelocity();
            default:
                throw new UnsupportedOperationException("Unhandled direction type!");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyListenerBeginIfNeeded(boolean z, boolean z2, boolean z3, boolean z4) {
        if (this.mOnFactorChangeListener == null || z || z3) {
            return;
        }
        this.mOnFactorChangeListener.onFactorBeginChanging();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyListenerEndIfNeeded(boolean z, boolean z2, boolean z3, boolean z4) {
        if (this.mOnFactorChangeListener == null || z2 || z4) {
            return;
        }
        this.mOnFactorChangeListener.onFactorEndChanging();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setFactorBySpring(float f) {
        this.mCurrentFactor = f;
        OnFactorChangeListener onFactorChangeListener = this.mOnFactorChangeListener;
        if (onFactorChangeListener != null) {
            onFactorChangeListener.onFactorChanged(f);
        }
    }

    private void updateSpringEndValue(double d) {
        if (d < 0.5d) {
            this.mSpring.setEndValue(0.0d);
        } else {
            this.mSpring.setEndValue(1.0d);
        }
    }

    public float getCurrentFactor() {
        return this.mCurrentFactor;
    }

    public Float getEndValueIfAvailable() {
        if (this.mSpring.isAtRest()) {
            return null;
        }
        return Float.valueOf((float) this.mSpring.getEndValue());
    }

    public int getFactorDistance() {
        return this.mFactorDistance;
    }

    public void sendMotionEvent(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
        float relevantCoordinateFromEvent = getRelevantCoordinateFromEvent(motionEvent);
        if (motionEvent.getAction() != 3 && motionEvent.getAction() != 1) {
            boolean z = !this.mSpring.wasAtRest();
            this.mSpring.setAtRest();
            boolean z2 = this.mIsTouching;
            this.mIsTouching = true;
            notifyListenerBeginIfNeeded(z2, this.mIsTouching, z, false);
            if (motionEvent.getAction() != 0) {
                float f = this.mPreviousCoord;
                if (f != 0.0f) {
                    this.mPreviousCoord = relevantCoordinateFromEvent;
                    setFactor(calculateNewFactor(relevantCoordinateFromEvent - f));
                    return;
                }
            }
            this.mPreviousCoord = relevantCoordinateFromEvent;
            return;
        }
        this.mPreviousCoord = 0.0f;
        float f2 = this.mCurrentFactor;
        this.mVelocityTracker.computeCurrentVelocity(1000);
        float relevantVelocityFromTracker = getRelevantVelocityFromTracker(this.mVelocityTracker);
        this.mSpring.setVelocity(((this.mMotionFactor * relevantVelocityFromTracker) / this.mFactorDistance) / calculateOvershootFactor(f2));
        Float fCalculateEndValueWithVelocity = calculateEndValueWithVelocity(relevantVelocityFromTracker);
        if (fCalculateEndValueWithVelocity != null) {
            setEndValue(fCalculateEndValueWithVelocity.floatValue());
        } else {
            updateSpringEndValue(f2);
        }
        this.mSpring.setCurrentValue(f2, false);
        boolean z3 = !this.mSpring.wasAtRest();
        boolean z4 = !this.mSpring.isAtRest();
        boolean z5 = this.mIsTouching;
        this.mIsTouching = false;
        notifyListenerEndIfNeeded(z5, this.mIsTouching, z3, z4);
        this.mVelocityTracker.recycle();
        this.mVelocityTracker = null;
    }

    public void setDirection(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException("direction cannot be null!");
        }
        this.mDirection = direction;
    }

    public void setEndValue(float f) {
        this.mSpring.setEndValue(f);
        this.mSpring.setCurrentValue(this.mCurrentFactor, false);
    }

    public void setFactor(float f) {
        this.mCurrentFactor = f;
        this.mSpring.setAtRest();
        OnFactorChangeListener onFactorChangeListener = this.mOnFactorChangeListener;
        if (onFactorChangeListener != null) {
            onFactorChangeListener.onFactorChanged(f);
        }
    }

    public void setFactorDistance(int i) {
        this.mFactorDistance = i;
    }

    public void setOnFactorChangeListener(OnFactorChangeListener onFactorChangeListener) {
        this.mOnFactorChangeListener = onFactorChangeListener;
    }

    public void setSpringConfig(SpringConfig springConfig) {
        this.mSpring.setSpringConfig(springConfig);
    }
}
