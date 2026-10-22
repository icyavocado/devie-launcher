package com.voxel.simplesearchlauncher.view.stack;

import java.util.ArrayList;

/* JADX INFO: loaded from: classes2.dex */
public class PiecewiseLinearIndentationFunctor extends StackIndentationFunctor {
    private final ArrayList<Float> mBaseValues;
    private final float mLinearPart;

    public PiecewiseLinearIndentationFunctor(int i, int i2, int i3, float f) {
        super(i, i2, i3);
        this.mBaseValues = new ArrayList<>(i + 1);
        initBaseValues();
        this.mLinearPart = f;
    }

    private int getSumOfSquares(int i) {
        return (((i + 1) * i) * ((i * 2) + 1)) / 6;
    }

    private void initBaseValues() {
        int sumOfSquares = getSumOfSquares(this.mMaxItemsInStack - 1);
        this.mBaseValues.add(Float.valueOf(0.0f));
        int i = 0;
        for (int i2 = 0; i2 < this.mMaxItemsInStack - 1; i2++) {
            i += ((this.mMaxItemsInStack - i2) - 1) * ((this.mMaxItemsInStack - i2) - 1);
            this.mBaseValues.add(Float.valueOf(i / sumOfSquares));
        }
    }
}
