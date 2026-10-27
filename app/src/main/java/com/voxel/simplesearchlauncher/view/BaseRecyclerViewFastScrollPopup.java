package com.voxel.simplesearchlauncher.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.voxel.launcher3.Utilities;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class BaseRecyclerViewFastScrollPopup {
    private float mAlpha;
    private Animator mAlphaAnimator;
    private Drawable mBg;
    private int mBgOriginalSize;
    private Resources mRes;
    private BaseRecyclerView mRv;
    private String mSectionName;
    private Paint mTextPaint;
    private boolean mVisible;
    private Rect mBgBounds = new Rect();
    private Rect mInvalidateRect = new Rect();
    private Rect mTmpRect = new Rect();
    private Rect mTextBounds = new Rect();

    public BaseRecyclerViewFastScrollPopup(BaseRecyclerView baseRecyclerView, Resources resources) {
        this.mRes = resources;
        this.mRv = baseRecyclerView;
        this.mBgOriginalSize = resources.getDimensionPixelSize(R.dimen.container_fastscroll_popup_size);
        this.mBg = resources.getDrawable(R.drawable.container_fastscroll_popup_bg);
        Drawable drawable = this.mBg;
        int i = this.mBgOriginalSize;
        drawable.setBounds(0, 0, i, i);
        this.mTextPaint = new Paint();
        this.mTextPaint.setColor(-1);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(resources.getDimensionPixelSize(R.dimen.container_fastscroll_popup_text_size));
    }

    public void animateVisibility(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            Animator animator = this.mAlphaAnimator;
            if (animator != null) {
                animator.cancel();
            }
            float[] fArr = new float[1];
            fArr[0] = z ? 1.0f : 0.0f;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "alpha", fArr);
            ofFloat.setDuration(z ? 200L : 150L);
            ofFloat.start();
            this.mAlphaAnimator = ofFloat;
        }
    }

    public void draw(Canvas canvas) {
        if (isVisible()) {
            int iSave = canvas.save();
            canvas.translate(this.mBgBounds.left, this.mBgBounds.top);
            this.mTmpRect.set(this.mBgBounds);
            this.mTmpRect.offsetTo(0, 0);
            this.mBg.setBounds(this.mTmpRect);
            this.mBg.setAlpha((int) (this.mAlpha * 255.0f));
            this.mBg.draw(canvas);
            this.mTextPaint.setAlpha((int) (this.mAlpha * 255.0f));
            canvas.drawText(this.mSectionName, (this.mBgBounds.width() - this.mTextBounds.width()) / 2, this.mBgBounds.height() - ((this.mBgBounds.height() - this.mTextBounds.height()) / 2), this.mTextPaint);
            canvas.restoreToCount(iSave);
        }
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public boolean isVisible() {
        return this.mAlpha > 0.0f && this.mSectionName != null;
    }

    public void setAlpha(float f) {
        this.mAlpha = f;
        this.mRv.invalidate(this.mBgBounds);
    }

    public void setSectionName(String str) {
        if (str.equals(this.mSectionName)) {
            return;
        }
        this.mSectionName = str;
        this.mTextPaint.getTextBounds(str, 0, str.length(), this.mTextBounds);
        Rect rect = this.mTextBounds;
        rect.right = (int) (rect.left + this.mTextPaint.measureText(str));
    }

    public Rect updateFastScrollerBounds(int i) {
        this.mInvalidateRect.set(this.mBgBounds);
        if (isVisible()) {
            int maxScrollbarWidth = this.mRv.getMaxScrollbarWidth();
            int iHeight = (this.mBgOriginalSize - this.mTextBounds.height()) / 2;
            int i2 = this.mBgOriginalSize;
            int iMax = Math.max(i2, this.mTextBounds.width() + (iHeight * 2));
            if (Utilities.isRtl(this.mRes)) {
                this.mBgBounds.left = this.mRv.getBackgroundPadding().left + (this.mRv.getMaxScrollbarWidth() * 2);
                Rect rect = this.mBgBounds;
                rect.right = rect.left + iMax;
            } else {
                this.mBgBounds.right = (this.mRv.getWidth() - this.mRv.getBackgroundPadding().right) - (this.mRv.getMaxScrollbarWidth() * 2);
                Rect rect2 = this.mBgBounds;
                rect2.left = rect2.right - iMax;
            }
            Rect rect3 = this.mBgBounds;
            rect3.top = i - ((int) (i2 * 1.5f));
            rect3.top = Math.max(maxScrollbarWidth, Math.min(rect3.top, (this.mRv.getHeight() - maxScrollbarWidth) - i2));
            Rect rect4 = this.mBgBounds;
            rect4.bottom = rect4.top + i2;
        } else {
            this.mBgBounds.setEmpty();
        }
        this.mInvalidateRect.union(this.mBgBounds);
        return this.mInvalidateRect;
    }
}
