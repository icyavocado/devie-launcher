package com.voxel.recyclerview.widget;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

/* JADX INFO: loaded from: classes.dex */
public class SpringOverScroller implements SpringListener {
    private static final SpringConfig COASTING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(0.0d, 1.0d);
    private static final SpringConfig RUBBERBANDING_CONFIG = new SpringConfig(300.0d, 45.0d);
    private boolean isCoastingX = false;
    private boolean isCoastingY = false;
    private final SpringSystem mSpringSystem = SpringSystem.create();
    private final Spring mSpringX = this.mSpringSystem.createSpring().addListener(this);
    private final Spring mSpringY;

    public SpringOverScroller() {
        this.mSpringX.setRestDisplacementThreshold(1.0d);
        this.mSpringX.setRestSpeedThreshold(175.0d);
        this.mSpringY = this.mSpringSystem.createSpring().addListener(this);
        this.mSpringY.setRestDisplacementThreshold(1.0d);
        this.mSpringY.setRestSpeedThreshold(175.0d);
    }

    public void abortAnimation() {
        this.mSpringX.setAtRest();
        this.mSpringY.setAtRest();
    }

    public void fling(int i, int i2, float f, float f2) {
        this.mSpringX.setSpringConfig(COASTING_CONFIG).setCurrentValue(i).setVelocity(f);
        this.isCoastingX = true;
        this.mSpringY.setSpringConfig(COASTING_CONFIG).setCurrentValue(i2).setVelocity(f2);
        this.isCoastingY = true;
    }

    public final int getCurrX() {
        return (int) Math.round(this.mSpringX.getCurrentValue());
    }

    public final int getCurrY() {
        return (int) Math.round(this.mSpringY.getCurrentValue());
    }

    public boolean isCoastingY() {
        return this.isCoastingY;
    }

    public final boolean isFinished() {
        return this.mSpringX.isAtRest() && this.mSpringY.isAtRest();
    }

    @Override // com.facebook.rebound.SpringListener
    public void onSpringActivate(Spring spring) {
    }

    @Override // com.facebook.rebound.SpringListener
    public void onSpringAtRest(Spring spring) {
    }

    @Override // com.facebook.rebound.SpringListener
    public void onSpringEndStateChange(Spring spring) {
    }

    @Override // com.facebook.rebound.SpringListener
    public void onSpringUpdate(Spring spring) {
        Spring spring2 = this.mSpringY;
    }

    public boolean springBack(int i, int i2, int i3, int i4, int i5, int i6) {
        this.mSpringX.setCurrentValue(i, false);
        this.mSpringY.setCurrentValue(i2, false);
        if (i > i4 || i < i3) {
            if (i > i4) {
                this.mSpringX.setEndValue(i4);
            } else if (i < i3) {
                this.mSpringX.setEndValue(i3);
            }
            this.mSpringX.setSpringConfig(RUBBERBANDING_CONFIG);
            this.isCoastingX = false;
            return true;
        }
        if (i2 <= i6 && i2 >= i5) {
            return false;
        }
        if (i2 > i6) {
            this.mSpringY.setEndValue(i6);
        } else if (i2 < i5) {
            this.mSpringY.setEndValue(i5);
        }
        this.mSpringY.setSpringConfig(RUBBERBANDING_CONFIG);
        this.isCoastingY = false;
        return true;
    }

    public void startScroll(int i, int i2, int i3, int i4) {
        this.mSpringX.setSpringConfig(RUBBERBANDING_CONFIG);
        this.mSpringY.setSpringConfig(RUBBERBANDING_CONFIG);
        this.mSpringX.setCurrentValue(i).setEndValue(i3);
        this.mSpringY.setCurrentValue(i2).setEndValue(i4);
    }
}
