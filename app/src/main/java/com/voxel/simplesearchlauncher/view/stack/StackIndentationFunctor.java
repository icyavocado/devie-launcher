package com.voxel.simplesearchlauncher.view.stack;

/* JADX INFO: loaded from: classes2.dex */
public abstract class StackIndentationFunctor {
    protected int mDistanceToPeekStart;
    protected int mMaxItemsInStack;
    protected int mPeekSize;
    protected boolean mStackStartsAtPeek;
    protected int mTotalTransitionDistance;

    StackIndentationFunctor(int i, int i2, int i3) {
        this.mDistanceToPeekStart = i3;
        this.mStackStartsAtPeek = this.mDistanceToPeekStart == 0;
        this.mMaxItemsInStack = i;
        this.mPeekSize = i2;
        updateTotalTransitionDistance();
    }

    private void updateTotalTransitionDistance() {
        this.mTotalTransitionDistance = this.mDistanceToPeekStart + this.mPeekSize;
    }
}
