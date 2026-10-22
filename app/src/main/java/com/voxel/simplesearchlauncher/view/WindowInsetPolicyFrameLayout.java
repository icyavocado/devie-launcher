package com.voxel.simplesearchlauncher.view;

import com.ovacoda.devie.R;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.voxel.launcher3.Insettable;

/* JADX INFO: loaded from: classes2.dex */
public class WindowInsetPolicyFrameLayout extends InterceptableFrameLayout implements Insettable {
    protected final Rect mChildInsets;
    protected Object mChildWindowInsets;
    private final Rect mTempConsumedInsets;
    private boolean shouldConsumeBottomInset;
    private boolean shouldConsumeLeftInset;
    private boolean shouldConsumeRightInset;
    private boolean shouldConsumeTopInset;

    public WindowInsetPolicyFrameLayout(Context context) {
        super(context);
        this.shouldConsumeLeftInset = true;
        this.shouldConsumeRightInset = true;
        this.shouldConsumeTopInset = true;
        this.shouldConsumeBottomInset = true;
        this.mChildWindowInsets = null;
        this.mChildInsets = new Rect();
        this.mTempConsumedInsets = new Rect();
    }

    public WindowInsetPolicyFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.shouldConsumeLeftInset = true;
        this.shouldConsumeRightInset = true;
        this.shouldConsumeTopInset = true;
        this.shouldConsumeBottomInset = true;
        this.mChildWindowInsets = null;
        this.mChildInsets = new Rect();
        this.mTempConsumedInsets = new Rect();
        initializeFromAttributes(context, attributeSet);
    }

    private void initializeFromAttributes(Context context, AttributeSet attributeSet) {
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.WindowInsetPolicyFrameLayout);
        this.shouldConsumeLeftInset = typedArrayObtainStyledAttributes.getBoolean(R.styleable.WindowInsetPolicyFrameLayout_consumeLeft, this.shouldConsumeLeftInset);
        this.shouldConsumeRightInset = typedArrayObtainStyledAttributes.getBoolean(R.styleable.WindowInsetPolicyFrameLayout_consumeRight, this.shouldConsumeRightInset);
        this.shouldConsumeTopInset = typedArrayObtainStyledAttributes.getBoolean(R.styleable.WindowInsetPolicyFrameLayout_consumeTop, this.shouldConsumeTopInset);
        this.shouldConsumeBottomInset = typedArrayObtainStyledAttributes.getBoolean(R.styleable.WindowInsetPolicyFrameLayout_consumeBottom, this.shouldConsumeBottomInset);
        typedArrayObtainStyledAttributes.recycle();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void setInsets(View view, Rect rect) {
        if (view instanceof Insettable) {
            ((Insettable) view).setInsets(rect);
            return;
        }
        if (view.getFitsSystemWindows()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.topMargin = rect.top;
            layoutParams.leftMargin = rect.left;
            layoutParams.rightMargin = rect.right;
            layoutParams.bottomMargin = rect.bottom;
            view.setLayoutParams(layoutParams);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @TargetApi(20)
    private void setInsets(View view, WindowInsets windowInsets) {
        if (view instanceof Insettable) {
            ((Insettable) view).setInsets(windowInsets);
            return;
        }
        if (view.getFitsSystemWindows()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            layoutParams.topMargin = windowInsets.getSystemWindowInsetTop();
            layoutParams.leftMargin = windowInsets.getSystemWindowInsetLeft();
            layoutParams.rightMargin = windowInsets.getSystemWindowInsetRight();
            layoutParams.bottomMargin = windowInsets.getSystemWindowInsetBottom();
            view.setLayoutParams(layoutParams);
        }
    }

    @Override // android.view.ViewGroup
    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        super.addView(view, i, layoutParams);
        Object obj = this.mChildWindowInsets;
        if (obj != null) {
            setInsets(view, (WindowInsets) obj);
        } else {
            setInsets(view, this.mChildInsets);
        }
    }

    @Override // android.view.View
    protected boolean fitSystemWindows(Rect rect) {
        this.mTempConsumedInsets.set(0, 0, 0, 0);
        if (getFitsSystemWindows()) {
            if (this.shouldConsumeLeftInset) {
                this.mTempConsumedInsets.left = rect.left;
            }
            if (this.shouldConsumeRightInset) {
                this.mTempConsumedInsets.right = rect.right;
            }
            if (this.shouldConsumeTopInset) {
                this.mTempConsumedInsets.top = rect.top;
            }
            if (this.shouldConsumeBottomInset) {
                this.mTempConsumedInsets.bottom = rect.bottom;
            }
            setPadding(this.mTempConsumedInsets.left, this.mTempConsumedInsets.top, this.mTempConsumedInsets.right, this.mTempConsumedInsets.bottom);
        }
        this.mChildInsets.left = rect.left - this.mTempConsumedInsets.left;
        this.mChildInsets.right = rect.right - this.mTempConsumedInsets.right;
        this.mChildInsets.top = rect.top - this.mTempConsumedInsets.bottom;
        this.mChildInsets.bottom = rect.bottom - this.mTempConsumedInsets.bottom;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            setInsets(getChildAt(i), this.mChildInsets);
        }
        return true;
    }

    @Override // android.view.View
    @TargetApi(20)
    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mTempConsumedInsets.set(0, 0, 0, 0);
        if (getFitsSystemWindows()) {
            if (this.shouldConsumeLeftInset) {
                this.mTempConsumedInsets.left = windowInsets.getSystemWindowInsetLeft();
            }
            if (this.shouldConsumeRightInset) {
                this.mTempConsumedInsets.right = windowInsets.getSystemWindowInsetRight();
            }
            if (this.shouldConsumeTopInset) {
                this.mTempConsumedInsets.top = windowInsets.getSystemWindowInsetTop();
            }
            if (this.shouldConsumeBottomInset) {
                this.mTempConsumedInsets.bottom = windowInsets.getSystemWindowInsetBottom();
            }
            setPadding(this.mTempConsumedInsets.left, this.mTempConsumedInsets.top, this.mTempConsumedInsets.right, this.mTempConsumedInsets.bottom);
        }
        if (this.mChildWindowInsets == null) {
            this.mChildWindowInsets = new WindowInsets(windowInsets);
        }
        this.mChildWindowInsets = ((WindowInsets) this.mChildWindowInsets).replaceSystemWindowInsets(windowInsets.getSystemWindowInsetLeft() - this.mTempConsumedInsets.left, windowInsets.getSystemWindowInsetTop() - this.mTempConsumedInsets.top, windowInsets.getSystemWindowInsetRight() - this.mTempConsumedInsets.right, windowInsets.getSystemWindowInsetBottom() - this.mTempConsumedInsets.bottom);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            setInsets(getChildAt(i), new WindowInsets((WindowInsets) this.mChildWindowInsets));
        }
        if (Build.VERSION.SDK_INT >= 28) {
            windowInsets = windowInsets.consumeDisplayCutout();
        }
        return windowInsets.consumeSystemWindowInsets().consumeStableInsets();
    }

    @Override // com.voxel.launcher3.Insettable
    public void setInsets(Rect rect) {
        fitSystemWindows(rect);
    }

    @Override // com.voxel.launcher3.Insettable
    @TargetApi(20)
    public void setInsets(WindowInsets windowInsets) {
        dispatchApplyWindowInsets(windowInsets);
    }
}
