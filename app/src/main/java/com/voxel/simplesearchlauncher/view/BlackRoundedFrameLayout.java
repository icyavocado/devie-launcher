package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.voxel.simplesearchlauncher.drawable.RoundedCornerDrawable;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class BlackRoundedFrameLayout extends FrameLayout {
    int[] mRadius;
    RoundedCornerDrawable[] mRoundedCornerDrawables;

    public BlackRoundedFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRadius = new int[4];
        this.mRoundedCornerDrawables = new RoundedCornerDrawable[4];
        initializeFromAttributes(context, attributeSet);
        setWillNotDraw(false);
    }

    private void initializeFromAttributes(Context context, AttributeSet attributeSet) {
        int[] iArr;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.BlackRoundedFrameLayout);
        int dimensionPixelSize = typedArrayObtainStyledAttributes.getDimensionPixelSize(0, 0);
        int i = 0;
        while (true) {
            iArr = this.mRadius;
            if (i >= iArr.length) {
                break;
            }
            iArr[i] = dimensionPixelSize;
            i++;
        }
        iArr[0] = typedArrayObtainStyledAttributes.getDimensionPixelSize(3, iArr[0]);
        int[] iArr2 = this.mRadius;
        iArr2[1] = typedArrayObtainStyledAttributes.getDimensionPixelSize(4, iArr2[1]);
        int[] iArr3 = this.mRadius;
        iArr3[2] = typedArrayObtainStyledAttributes.getDimensionPixelSize(2, iArr3[2]);
        int[] iArr4 = this.mRadius;
        iArr4[3] = typedArrayObtainStyledAttributes.getDimensionPixelSize(1, iArr4[3]);
        for (int i2 = 0; i2 < 4; i2++) {
            this.mRoundedCornerDrawables[i2] = new RoundedCornerDrawable(this.mRadius[i2], RoundedCornerDrawable.CornerType.values()[i2]);
            RoundedCornerDrawable roundedCornerDrawable = this.mRoundedCornerDrawables[i2];
            int[] iArr5 = this.mRadius;
            roundedCornerDrawable.setBounds(0, 0, iArr5[i2], iArr5[i2]);
        }
        typedArrayObtainStyledAttributes.recycle();
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // android.view.View
    public boolean isOpaque() {
        return true;
    }

    @Override // android.view.View
    public void onDrawForeground(Canvas canvas) {
        float height;
        super.onDrawForeground(canvas);
        for (int i = 0; i < 4; i++) {
            int iSave = canvas.save();
            int i2 = this.mRadius[i];
            RoundedCornerDrawable roundedCornerDrawable = this.mRoundedCornerDrawables[i];
            float width = 0.0f;
            switch (RoundedCornerDrawable.CornerType.values()[i]) {
                case TOP_LEFT:
                default:
                    height = 0.0f;
                    break;
                case TOP_RIGHT:
                    width = (canvas.getWidth() - i2) - 0;
                    height = 0.0f;
                    break;
                case BOTTOM_LEFT:
                    height = canvas.getHeight() - i2;
                    break;
                case BOTTOM_RIGHT:
                    width = (canvas.getWidth() - i2) - 0;
                    height = canvas.getHeight() - i2;
                    break;
            }
            canvas.translate(width, height);
            roundedCornerDrawable.draw(canvas);
            canvas.restoreToCount(iSave);
        }
    }
}
