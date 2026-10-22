package com.voxel.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;

/* JADX INFO: loaded from: classes.dex */
public class HolographicOutlineHelper {
    static HolographicOutlineHelper INSTANCE;
    private static final Rect sTempRect = new Rect();
    private final BlurMaskFilter mMediumInnerBlurMaskFilter;
    private final BlurMaskFilter mMediumOuterBlurMaskFilter;
    private final int mShadowOffset;
    private final BlurMaskFilter mShaowBlurMaskFilter;
    private final BlurMaskFilter mThinOuterBlurMaskFilter;
    final int shadowBitmapPadding;
    private final Canvas mCanvas = new Canvas();
    private final Paint mDrawPaint = new Paint();
    private final Paint mBlurPaint = new Paint();
    private final Paint mErasePaint = new Paint();

    private HolographicOutlineHelper(Context context) {
        float screenDensity = LauncherAppState.getInstance().getScreenDensity();
        float f = 2.0f * screenDensity;
        this.mMediumOuterBlurMaskFilter = new BlurMaskFilter(f, BlurMaskFilter.Blur.OUTER);
        this.mThinOuterBlurMaskFilter = new BlurMaskFilter(1.0f * screenDensity, BlurMaskFilter.Blur.OUTER);
        this.mMediumInnerBlurMaskFilter = new BlurMaskFilter(f, BlurMaskFilter.Blur.NORMAL);
        float f2 = screenDensity * 4.0f;
        this.mShaowBlurMaskFilter = new BlurMaskFilter(f2, BlurMaskFilter.Blur.NORMAL);
        this.mShadowOffset = (int) f;
        this.shadowBitmapPadding = (int) f2;
        this.mDrawPaint.setFilterBitmap(true);
        this.mDrawPaint.setAntiAlias(true);
        this.mBlurPaint.setFilterBitmap(true);
        this.mBlurPaint.setAntiAlias(true);
        this.mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        this.mErasePaint.setFilterBitmap(true);
        this.mErasePaint.setAntiAlias(true);
    }

    public static HolographicOutlineHelper obtain(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new HolographicOutlineHelper(context);
        }
        return INSTANCE;
    }

    public void applyExpensiveOutlineWithBlur(Bitmap bitmap, Canvas canvas) {
        applyExpensiveOutlineWithBlur(bitmap, canvas, true);
    }

    public void applyExpensiveOutlineWithBlur(Bitmap bitmap, Canvas canvas, boolean z) {
        if (z) {
            int[] iArr = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(iArr, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            for (int i = 0; i < iArr.length; i++) {
                if ((iArr[i] >>> 24) < 188) {
                    iArr[i] = 0;
                }
            }
            bitmap.setPixels(iArr, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        }
        Bitmap bitmapExtractAlpha = bitmap.extractAlpha();
        this.mBlurPaint.setMaskFilter(this.mMediumOuterBlurMaskFilter);
        int[] iArr2 = new int[2];
        Bitmap bitmapExtractAlpha2 = bitmapExtractAlpha.extractAlpha(this.mBlurPaint, iArr2);
        this.mBlurPaint.setMaskFilter(this.mThinOuterBlurMaskFilter);
        int[] iArr3 = new int[2];
        Bitmap bitmapExtractAlpha3 = bitmapExtractAlpha.extractAlpha(this.mBlurPaint, iArr3);
        canvas.setBitmap(bitmapExtractAlpha);
        canvas.drawColor(-16777216, PorterDuff.Mode.SRC_OUT);
        this.mBlurPaint.setMaskFilter(this.mMediumInnerBlurMaskFilter);
        int[] iArr4 = new int[2];
        Bitmap bitmapExtractAlpha4 = bitmapExtractAlpha.extractAlpha(this.mBlurPaint, iArr4);
        canvas.setBitmap(bitmapExtractAlpha4);
        canvas.drawBitmap(bitmapExtractAlpha, -iArr4[0], -iArr4[1], this.mErasePaint);
        canvas.drawRect(0.0f, 0.0f, -iArr4[0], bitmapExtractAlpha4.getHeight(), this.mErasePaint);
        canvas.drawRect(0.0f, 0.0f, bitmapExtractAlpha4.getWidth(), -iArr4[1], this.mErasePaint);
        canvas.setBitmap(bitmap);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(bitmapExtractAlpha4, iArr4[0], iArr4[1], this.mDrawPaint);
        canvas.drawBitmap(bitmapExtractAlpha2, iArr2[0], iArr2[1], this.mDrawPaint);
        canvas.drawBitmap(bitmapExtractAlpha3, iArr3[0], iArr3[1], this.mDrawPaint);
        canvas.setBitmap(null);
        bitmapExtractAlpha3.recycle();
        bitmapExtractAlpha2.recycle();
        bitmapExtractAlpha4.recycle();
        bitmapExtractAlpha.recycle();
    }

    public Bitmap createMediumDropShadow(BubbleTextView bubbleTextView) {
        int width = bubbleTextView.getWidth();
        int i = this.shadowBitmapPadding;
        int i2 = width + i + i;
        int height = bubbleTextView.getHeight();
        int i3 = this.shadowBitmapPadding;
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(i2, height + i3 + i3 + this.mShadowOffset, Bitmap.Config.ARGB_8888);
        this.mCanvas.setBitmap(bitmapCreateBitmap);
        Rect rect = sTempRect;
        bubbleTextView.getDrawingRect(rect);
        rect.bottom = (bubbleTextView.getExtendedPaddingTop() - 3) + bubbleTextView.getLayout().getLineTop(0);
        this.mCanvas.save();
        this.mCanvas.scale(bubbleTextView.getScaleX(), bubbleTextView.getScaleY(), (bubbleTextView.getWidth() / 2) + this.shadowBitmapPadding, (bubbleTextView.getHeight() / 2) + this.shadowBitmapPadding);
        this.mCanvas.translate((-bubbleTextView.getScrollX()) + this.shadowBitmapPadding, (-bubbleTextView.getScrollY()) + this.shadowBitmapPadding);
        this.mCanvas.clipRect(rect, Region.Op.INTERSECT);
        bubbleTextView.draw(this.mCanvas);
        this.mCanvas.restore();
        int[] iArr = new int[2];
        this.mBlurPaint.setMaskFilter(this.mShaowBlurMaskFilter);
        Bitmap bitmapExtractAlpha = bitmapCreateBitmap.extractAlpha(this.mBlurPaint, iArr);
        this.mCanvas.save();
        this.mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.mCanvas.translate(iArr[0], iArr[1]);
        this.mDrawPaint.setColor(-16777216);
        this.mDrawPaint.setAlpha(30);
        this.mCanvas.drawBitmap(bitmapExtractAlpha, 0.0f, 0.0f, this.mDrawPaint);
        this.mDrawPaint.setAlpha(60);
        this.mCanvas.drawBitmap(bitmapExtractAlpha, 0.0f, this.mShadowOffset, this.mDrawPaint);
        this.mCanvas.restore();
        this.mCanvas.setBitmap(null);
        bitmapExtractAlpha.recycle();
        return bitmapCreateBitmap;
    }
}
