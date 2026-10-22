package com.voxel.launcher3.util;

import android.annotation.TargetApi;
import android.graphics.Rect;

/* JADX INFO: loaded from: classes.dex */
@TargetApi(21)
public class PillRevealOutlineProvider extends RevealOutlineAnimation {
    private int mCenterX;
    private int mCenterY;
    private float mFinalRadius;
    protected Rect mPillRect;

    public PillRevealOutlineProvider(int i, int i2, Rect rect, float f) {
        this.mCenterX = i;
        this.mCenterY = i2;
        this.mPillRect = rect;
        this.mFinalRadius = f;
        this.mOutlineRadius = f;
    }

    @Override // com.voxel.launcher3.util.RevealOutlineAnimation
    public void setProgress(float f) {
        int iMax = (int) (f * Math.max(this.mCenterX, this.mPillRect.width() - this.mCenterX));
        this.mOutline.left = Math.max(this.mPillRect.left, this.mCenterX - iMax);
        this.mOutline.top = Math.max(this.mPillRect.top, this.mCenterY - iMax);
        this.mOutline.right = Math.min(this.mPillRect.right, this.mCenterX + iMax);
        this.mOutline.bottom = Math.min(this.mPillRect.bottom, this.mCenterY + iMax);
        this.mOutlineRadius = Math.min(this.mFinalRadius, this.mOutline.height() / 2);
    }

    @Override // com.voxel.launcher3.util.RevealOutlineAnimation
    public boolean shouldRemoveElevationDuringAnimation() {
        return false;
    }
}
