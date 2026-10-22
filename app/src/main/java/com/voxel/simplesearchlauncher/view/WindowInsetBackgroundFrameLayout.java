package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.WindowInsets;

/* JADX INFO: loaded from: classes2.dex */
public class WindowInsetBackgroundFrameLayout extends WindowInsetPolicyFrameLayout {
    private static Rect sDrawRect = new Rect();
    private GradientDrawable mBottomDrawable;
    private int[] mBottomGradientColors;
    private boolean mDrawBottom;
    private boolean mDrawTop;
    private final Paint mPaint;
    private GradientDrawable mTopDrawable;
    private int[] mTopGradientColors;

    public WindowInsetBackgroundFrameLayout(Context context) {
        super(context);
        this.mPaint = new Paint();
        this.mPaint.setDither(true);
        this.mDrawTop = true;
        this.mDrawBottom = true;
        this.mTopGradientColors = new int[2];
        this.mBottomGradientColors = new int[2];
        this.mTopDrawable = new GradientDrawable();
        this.mTopDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        this.mBottomDrawable = new GradientDrawable();
        this.mBottomDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        int[] iArr = this.mTopGradientColors;
        iArr[0] = 0;
        iArr[1] = 0;
        int[] iArr2 = this.mBottomGradientColors;
        iArr2[0] = 0;
        iArr2[1] = 0;
        setWillNotDraw(false);
    }

    public WindowInsetBackgroundFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mPaint = new Paint();
        this.mPaint.setDither(true);
        this.mDrawTop = true;
        this.mDrawBottom = true;
        this.mTopGradientColors = new int[2];
        this.mBottomGradientColors = new int[2];
        this.mTopDrawable = new GradientDrawable();
        this.mTopDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        this.mBottomDrawable = new GradientDrawable();
        this.mBottomDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
        int[] iArr = this.mTopGradientColors;
        iArr[0] = 0;
        iArr[1] = 0;
        int[] iArr2 = this.mBottomGradientColors;
        iArr2[0] = 0;
        iArr2[1] = 0;
        setWillNotDraw(false);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mChildWindowInsets != null) {
            WindowInsets windowInsets = (WindowInsets) this.mChildWindowInsets;
            sDrawRect.left = windowInsets.getSystemWindowInsetLeft();
            sDrawRect.top = windowInsets.getSystemWindowInsetTop();
            sDrawRect.right = windowInsets.getSystemWindowInsetRight();
            sDrawRect.bottom = windowInsets.getSystemWindowInsetBottom();
        } else {
            sDrawRect.set(this.mChildInsets);
        }
        if (sDrawRect.top != 0 && this.mDrawTop) {
            this.mTopDrawable.setBounds(0, 0, canvas.getWidth(), sDrawRect.top);
            this.mTopDrawable.draw(canvas);
        }
        if (sDrawRect.bottom == 0 || !this.mDrawBottom) {
            return;
        }
        this.mBottomDrawable.setBounds(0, canvas.getHeight() - sDrawRect.bottom, canvas.getWidth(), canvas.getHeight());
        this.mBottomDrawable.draw(canvas);
    }

    @Override // com.voxel.simplesearchlauncher.view.WindowInsetPolicyFrameLayout, android.view.View
    protected boolean fitSystemWindows(Rect rect) {
        boolean zFitSystemWindows = super.fitSystemWindows(rect);
        invalidate();
        return zFitSystemWindows;
    }

    @Override // com.voxel.simplesearchlauncher.view.WindowInsetPolicyFrameLayout, android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        WindowInsets windowInsetsOnApplyWindowInsets = super.onApplyWindowInsets(windowInsets);
        invalidate();
        return windowInsetsOnApplyWindowInsets;
    }

    public void setBottomEndInsetBackgroundColor(int i) {
        int[] iArr = this.mBottomGradientColors;
        iArr[1] = i;
        this.mBottomDrawable.setColors(iArr);
        invalidate();
    }

    public void setBottomStartInsetBackgroundColor(int i) {
        int[] iArr = this.mBottomGradientColors;
        iArr[0] = i;
        this.mBottomDrawable.setColors(iArr);
        invalidate();
    }

    public void setDrawBottom(boolean z) {
        this.mDrawBottom = z;
        invalidate();
    }

    public void setDrawTop(boolean z) {
        this.mDrawTop = z;
        invalidate();
    }

    public void setEndInsetBackgroundColor(int i) {
        setTopEndInsetBackgroundColor(i);
        setBottomEndInsetBackgroundColor(i);
    }

    public void setStartInsetBackgroundColor(int i) {
        setTopStartInsetBackgroundColor(i);
        setBottomStartInsetBackgroundColor(i);
    }

    public void setTopEndInsetBackgroundColor(int i) {
        int[] iArr = this.mTopGradientColors;
        iArr[1] = i;
        this.mTopDrawable.setColors(iArr);
        invalidate();
    }

    public void setTopStartInsetBackgroundColor(int i) {
        int[] iArr = this.mTopGradientColors;
        iArr[0] = i;
        this.mTopDrawable.setColors(iArr);
        invalidate();
    }
}
