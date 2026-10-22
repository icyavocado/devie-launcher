package com.voxel.simplesearchlauncher.view;

import timber.log.Timber;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;
import com.voxel.simplesearchlauncher.view.stack.PiecewiseLinearIndentationFunctor;
import com.voxel.simplesearchlauncher.view.stack.StackIndentationFunctor;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class PullDownRecyclerView extends NoItemClickRecyclerView {
    private float mCurrentFactor;
    private int mPaddingBetweenElements;
    private StackIndentationFunctor mTopStackIndentationFunctor;
    private int mTopStackPeekSize;
    private int mTopStackSlowDownLength;
    private int mTopStackTotalSize;

    public PullDownRecyclerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initialize();
    }

    private void applyCurrentFactor() {
        int i;
        if (getItemAnimator() == null || !getItemAnimator().isRunning()) {
            float f = this.mCurrentFactor;
            if (f == 0.0f) {
                for (int i2 = 0; i2 < getChildCount(); i2++) {
                    View childAt = getChildAt(i2);
                    childAt.setTranslationY(-getHeight());
                    childAt.setAlpha(0.0f);
                    childAt.setVisibility(4);
                    ViewCompat.setElevation(childAt, 0.0f);
                }
                return;
            }
            if (f == 1.0f) {
                for (int i3 = 0; i3 < getChildCount(); i3++) {
                    View childAt2 = getChildAt(i3);
                    childAt2.setTranslationY(0.0f);
                    childAt2.setAlpha(1.0f);
                    childAt2.setVisibility(0);
                    ViewCompat.setElevation(childAt2, 0.0f);
                }
                return;
            }
            int i4 = 0;
            int i5 = 0;
            int iMax = 0;
            while (i4 < getChildCount()) {
                View childAt3 = getChildAt(i4);
                int height = childAt3.getHeight();
                if (childAt3.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt3.getLayoutParams();
                    height += marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
                    i = marginLayoutParams.topMargin;
                } else {
                    i = 0;
                }
                iMax = Math.max(height, iMax);
                i5 = i4 != getChildCount() + (-1) ? i5 + height : i5 + i;
                i4++;
            }
            float f2 = i5;
            float f3 = this.mCurrentFactor;
            int iRound = Math.round((f2 * f3) - (iMax * (1.0f - f3)));
            float childCount = this.mCurrentFactor * getChildCount();
            for (int i6 = 0; i6 < getChildCount(); i6++) {
                View childAt4 = getChildAt(i6);
                Math.max(childCount - i6, 0.0f);
                int iMin = Math.min(iRound, childAt4.getTop());
                childAt4.setTranslationY(iMin - childAt4.getTop());
                float fMin = 1.0f - Math.min(Math.abs(iMin - childAt4.getTop()) / childAt4.getMeasuredHeight(), 1.0f);
                childAt4.setAlpha(fMin);
                if (fMin <= 0.0f) {
                    childAt4.setVisibility(4);
                } else {
                    childAt4.setVisibility(0);
                }
            }
        }
    }

    private void initialize() {
        Context context = getContext();
        this.mTopStackPeekSize = context.getResources().getDimensionPixelSize(R.dimen.stack_peek_amount);
        this.mTopStackSlowDownLength = context.getResources().getDimensionPixelSize(R.dimen.stack_slow_down_length);
        this.mPaddingBetweenElements = 0;
        updatePadding(false);
    }

    private void updatePadding(boolean z) {
        int i = this.mTopStackSlowDownLength + this.mPaddingBetweenElements;
        int i2 = this.mTopStackPeekSize;
        this.mTopStackTotalSize = i + i2;
        this.mTopStackIndentationFunctor = new PiecewiseLinearIndentationFunctor(3, i2, this.mTopStackTotalSize - i2, 0.5f);
    }

    @Override // com.voxel.recyclerview.widget.RecyclerView, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) { /* REWORK: jadx added a bogus throws */
        try {
            super.onLayout(z, i, i2, i3, i4);
            applyCurrentFactor();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override // android.view.View
    protected void onScrollChanged(int i, int i2, int i3, int i4) {
        applyCurrentFactor();
    }

    public void setCurrentFactor(float f) {
        this.mCurrentFactor = f;
        if (getItemAnimator() != null) {
            getItemAnimator().endAnimations();
        }
        invalidate();
        requestLayout();
    }
}
