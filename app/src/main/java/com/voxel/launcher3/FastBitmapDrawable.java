package com.voxel.launcher3;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import com.voxel.launcher3.graphics.IconPalette;

/* JADX INFO: loaded from: classes.dex */
public class FastBitmapDrawable extends Drawable {
    private static ColorMatrix sGhostModeMatrix;
    private final Bitmap mBitmap;
    private IconPalette mIconPalette;
    private ObjectAnimator mPressedAnimator;
    static final TimeInterpolator CLICK_FEEDBACK_INTERPOLATOR = new TimeInterpolator() { // from class: com.voxel.launcher3.-$$Lambda$FastBitmapDrawable$94_cmWno2X3YOi0McNt4Jo7TdUk
        @Override // android.animation.TimeInterpolator
        public final float getInterpolation(float f) {
            return FastBitmapDrawable.lambda$static$0(f);
        }
    };
    private static final ColorMatrix sTempMatrix = new ColorMatrix();
    private static final SparseArray<ColorFilter> sCachedBrightnessFilter = new SparseArray<>();
    private final Paint mPaint = new Paint(2);
    private int mBrightness = 0;
    private boolean mGhostModeEnabled = false;
    private boolean mPressed = false;
    private int mAlpha = 255;

    public FastBitmapDrawable(Bitmap bitmap) {
        this.mBitmap = bitmap;
        setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    static /* synthetic */ float lambda$static$0(float f) {
        if (f < 0.05f) {
            return f / 0.05f;
        }
        if (f < 0.3f) {
            return 1.0f;
        }
        return (1.0f - f) / 0.7f;
    }

    private static void setBrightnessMatrix(ColorMatrix colorMatrix, int i) {
        float f = i;
        float f2 = 1.0f - (f / 255.0f);
        colorMatrix.setScale(f2, f2, f2, 1.0f);
        float[] array = colorMatrix.getArray();
        array[4] = f;
        array[9] = f;
        array[14] = f;
    }

    private void updateFilter() {
        if (!this.mGhostModeEnabled) {
            int i = this.mBrightness;
            if (i == 0) {
                this.mPaint.setColorFilter(null);
                return;
            }
            ColorFilter porterDuffColorFilter = sCachedBrightnessFilter.get(i);
            if (porterDuffColorFilter == null) {
                porterDuffColorFilter = new PorterDuffColorFilter(Color.argb(this.mBrightness, 255, 255, 255), PorterDuff.Mode.SRC_ATOP);
                sCachedBrightnessFilter.put(this.mBrightness, porterDuffColorFilter);
            }
            this.mPaint.setColorFilter(porterDuffColorFilter);
            return;
        }
        if (sGhostModeMatrix == null) {
            sGhostModeMatrix = new ColorMatrix();
            sGhostModeMatrix.setSaturation(0.0f);
            sTempMatrix.set(new float[]{0.49019608f, 0.0f, 0.0f, 0.0f, 130.0f, 0.0f, 0.49019608f, 0.0f, 0.0f, 130.0f, 0.0f, 0.0f, 0.49019608f, 0.0f, 130.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
            sGhostModeMatrix.preConcat(sTempMatrix);
        }
        int i2 = this.mBrightness;
        if (i2 == 0) {
            this.mPaint.setColorFilter(new ColorMatrixColorFilter(sGhostModeMatrix));
            return;
        }
        setBrightnessMatrix(sTempMatrix, i2);
        sTempMatrix.postConcat(sGhostModeMatrix);
        this.mPaint.setColorFilter(new ColorMatrixColorFilter(sTempMatrix));
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.mBitmap, (Rect) null, getBounds(), this.mPaint);
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mAlpha;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public IconPalette getIconPalette() {
        if (this.mIconPalette == null) {
            this.mIconPalette = IconPalette.fromDominantColor(Utilities.findDominantColorByHue(this.mBitmap, 20), true);
        }
        return this.mIconPalette;
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mBitmap.getHeight();
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mBitmap.getWidth();
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumHeight() {
        return getBounds().height();
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumWidth() {
        return getBounds().width();
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mAlpha = i;
        this.mPaint.setAlpha(i);
    }

    public void setBrightness(int i) {
        if (this.mBrightness != i) {
            this.mBrightness = i;
            updateFilter();
            invalidateSelf();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override // android.graphics.drawable.Drawable
    public void setFilterBitmap(boolean z) {
        this.mPaint.setFilterBitmap(z);
        this.mPaint.setAntiAlias(z);
    }

    public void setGhostModeEnabled(boolean z) {
        if (this.mGhostModeEnabled != z) {
            this.mGhostModeEnabled = z;
            updateFilter();
        }
    }

    public void setPressed(boolean z) {
        if (this.mPressed != z) {
            this.mPressed = z;
            if (this.mPressed) {
                this.mPressedAnimator = ObjectAnimator.ofInt(this, "brightness", 100).setDuration(2000L);
                this.mPressedAnimator.setInterpolator(CLICK_FEEDBACK_INTERPOLATOR);
                this.mPressedAnimator.start();
            } else {
                ObjectAnimator objectAnimator = this.mPressedAnimator;
                if (objectAnimator != null) {
                    objectAnimator.cancel();
                    setBrightness(0);
                }
            }
        }
        invalidateSelf();
    }
}
