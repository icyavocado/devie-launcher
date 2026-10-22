package com.voxel.launcher3;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class FocusIndicatorView extends View implements View.OnFocusChangeListener {
    private static final int[] sTempPos = new int[2];
    private static final int[] sTempShift = new int[2];
    private final int[] mIndicatorPos;
    private boolean mInitiated;
    private View mLastFocusedView;
    private Pair<View, Boolean> mPendingCall;
    private final int[] mTargetViewPos;

    public FocusIndicatorView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mIndicatorPos = new int[2];
        this.mTargetViewPos = new int[2];
        setAlpha(0.0f);
        setBackgroundColor(getResources().getColor(R.color.focused_background));
    }

    private static void getLocationRelativeToParentPagedView(View view, int[] iArr) {
        getPagedViewScrollShift(view, sTempShift);
        view.getLocationInWindow(sTempPos);
        int[] iArr2 = sTempPos;
        int i = iArr2[0];
        int[] iArr3 = sTempShift;
        iArr[0] = i + iArr3[0];
        iArr[1] = iArr2[1] + iArr3[1];
    }

    private static void getPagedViewScrollShift(View view, int[] iArr) {
        Object parent = view.getParent();
        if (parent instanceof PagedView) {
            view.getLocationInWindow(sTempPos);
            iArr[0] = ((View) parent).getPaddingLeft() - sTempPos[0];
            iArr[1] = -((int) view.getTranslationY());
        } else if (parent instanceof View) {
            getPagedViewScrollShift((View) parent, iArr);
        } else {
            iArr[1] = 0;
            iArr[0] = 0;
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        Pair<View, Boolean> pair = this.mPendingCall;
        if (pair != null) {
            onFocusChange((View) pair.first, ((Boolean) this.mPendingCall.second).booleanValue());
        }
    }

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        this.mPendingCall = null;
        if (!this.mInitiated && getWidth() == 0) {
            this.mPendingCall = Pair.create(view, Boolean.valueOf(z));
            invalidate();
            return;
        }
        if (!this.mInitiated) {
            getLocationRelativeToParentPagedView(this, this.mIndicatorPos);
            this.mInitiated = true;
        }
        if (!z) {
            if (this.mLastFocusedView == view) {
                this.mLastFocusedView = null;
                animate().alpha(0.0f);
                return;
            }
            return;
        }
        int width = getWidth();
        int height = getHeight();
        float f = width;
        float scaleX = (view.getScaleX() * view.getWidth()) / f;
        float f2 = height;
        float scaleY = (view.getScaleY() * view.getHeight()) / f2;
        getLocationRelativeToParentPagedView(view, this.mTargetViewPos);
        int[] iArr = this.mTargetViewPos;
        int i = iArr[0];
        int[] iArr2 = this.mIndicatorPos;
        float f3 = (i - iArr2[0]) - (((1.0f - scaleX) * f) / 2.0f);
        float f4 = (iArr[1] - iArr2[1]) - (((1.0f - scaleY) * f2) / 2.0f);
        if (getAlpha() > 0.2f) {
            animate().translationX(f3).translationY(f4).scaleX(scaleX).scaleY(scaleY).alpha(1.0f);
        } else {
            setTranslationX(f3);
            setTranslationY(f4);
            setScaleX(scaleX);
            setScaleY(scaleY);
            animate().alpha(1.0f);
        }
        this.mLastFocusedView = view;
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        View view = this.mLastFocusedView;
        if (view != null) {
            this.mPendingCall = Pair.create(view, Boolean.TRUE);
            invalidate();
        }
    }
}
