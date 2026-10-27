package com.voxel.simplesearchlauncher.view;

import timber.log.Timber;
import android.annotation.TargetApi;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.voxel.launcher3.FastBitmapDrawable;
import com.voxel.launcher3.Launcher;
import com.voxel.launcher3.Utilities;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.voxel.simplesearchlauncher.utils.Blur;
import com.voxel.simplesearchlauncher.utils.ColorUtil;
import com.ovacoda.devie.R;
import java.util.ArrayList;

/* JADX INFO: loaded from: classes2.dex */
public class SpringySearchView extends SearchView {
    private TextView alternateHintText;
    private float backgroundOpacityMax;
    private float backgroundOpacityMin;
    private float crossOverFactor;
    private float currentFactor;
    private EditText editText;
    private int factorDistance;
    private SpringListener forcedEndValueSpringListener;
    private boolean isDragDown;
    private KeyboardRulesHandler keyboardRulesHandler;
    private boolean limitOpacityBounds;
    private boolean limitTextPaddingBounds;
    private float mBackgroundAlternativeFactorOffset;
    protected LayerDrawable mBackgroundDrawable;
    private float mBackgroundDrawableCornerRadius;
    private float mBackgroundOpacityMaxSubtle;
    protected float mDensity;
    protected int mFlingThresholdVelocity;
    private int mInverseHintColorMax;
    private int mInverseHintColorMin;
    private int mInverseHintOpacityMax;
    private int mInverseHintOpacityMin;
    private int mInverseTextColorMax;
    private int mInverseTextColorMin;
    private boolean mIsTouching;
    protected int mMinFlingVelocity;
    protected int mMinSnapVelocity;
    private OnFactorChangeListener mOnFactorChangeListener;
    private MaskRoundedFrameLayout mRounder;
    public CenterCropOffsetImageView mSubtleBackground;
    protected Drawable mSubtleBackgroundDrawable;
    private boolean mUseInverseColors;
    private boolean mUseSubtleBackground;
    private float motionFactor;
    private float opacityMax;
    private float opacityMin;
    private float previousY;
    private SpringListener reactiveEndValueSpringListener;
    private ImageView searchButton;
    private Spring spring;
    private Spring springAlternativeHintAlpha;
    private SpringSystem springSystem;
    private int textPaddingDistanceMax;
    private int textPaddingDistanceMin;
    private VelocityTracker velocityTracker;
    private ImageView voiceButton;

    public interface KeyboardRulesHandler {
        boolean canDisplayKeyboard();
    }

    public interface OnFactorChangeListener {
        void onFactorBeginChanging();

        void onFactorChanged(float f);

        void onFactorEndChanging();
    }

    public SpringySearchView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.limitTextPaddingBounds = false;
        this.limitOpacityBounds = true;
        this.previousY = 0.0f;
        initialize();
    }

    private Float calculateEndValueWithVelocity(float f) {
        int i = this.mFlingThresholdVelocity;
        if (f > i) {
            return Float.valueOf(0.0f);
        }
        if ((-f) > i) {
            return Float.valueOf(1.0f);
        }
        return null;
    }

    private float calculateNewFactor(float f) {
        float currentFactor = getCurrentFactor();
        float f2 = (f * this.motionFactor) / this.factorDistance;
        if (!this.isDragDown) {
            f2 *= -1.0f;
        }
        float f3 = f2 + currentFactor;
        float fCalculateOvershootFactor = 1.0f;
        if (f3 > 1.0f && f2 > 0.0f) {
            fCalculateOvershootFactor = calculateOvershootFactor(f3);
        } else if (f3 < 0.0f && f2 < 0.0f) {
            fCalculateOvershootFactor = calculateOvershootFactor(f3);
        }
        return (f2 / fCalculateOvershootFactor) + currentFactor;
    }

    private float calculateOvershootFactor(float f) {
        if (f > 1.0f) {
            return f * 5.0f;
        }
        if (f < 0.0f) {
            return (Math.abs(f) + 1.0f) * 5.0f;
        }
        return 1.0f;
    }

    private int colorWithAlphaAndRgb(int i, int i2) {
        int iMax = Math.max(0, Math.min(i, 255));
        int iMax2 = Math.max(0, Math.min(i2, 255));
        return Color.argb(iMax, iMax2, iMax2, iMax2);
    }

    private float estimateStopPosition(float f, float f2, float f3, float f4, float f5) {
        float f6 = f5 * f3;
        return f + ((-(f3 * f3)) / ((((((f2 - f) * f4) - f6) + ((f4 * (f2 - this.crossOverFactor)) - f6)) / 2.0f) * 2.0f));
    }

    private int getTint(Bitmap bitmap) {
        int iArgb = Color.argb(128, 255, 255, 255);
        try {
            Palette.Builder builder = new Palette.Builder(bitmap);
            builder.maximumColorCount(4);
            Palette paletteGenerate = builder.generate();
            if (new ArrayList(paletteGenerate.getSwatches()).size() == 0) {
                return iArgb;
            }
            float[] hsl = paletteGenerate.getDominantSwatch().getHsl();
            float[] fArr = new float[3];
            for (int i = 0; i < hsl.length; i++) {
                fArr[i] = hsl[i];
            }
            float fMax = Math.max(0.07f, fArr[1] * 0.2f);
            float fMax2 = Math.max(0.12f, fArr[2] * 0.35f);
            fArr[1] = Math.max(0.0f, Math.min(0.4f, fArr[1] - fMax));
            fArr[2] = Math.max(0.25f, Math.min(1.0f, fArr[2] + fMax2));
            int iHSLToColor = ColorUtils.HSLToColor(fArr);
            return Color.argb(128, Color.red(iHSLToColor), Color.green(iHSLToColor), Color.blue(iHSLToColor));
        } catch (IllegalArgumentException unused) {
            return iArgb;
        }
    }

    private void initialize() {
        this.mDensity = getContext().getResources().getDisplayMetrics().density;
        float f = this.mDensity;
        this.mFlingThresholdVelocity = (int) (f * 1500.0f);
        this.mMinFlingVelocity = (int) (250.0f * f);
        this.mMinSnapVelocity = (int) (1500.0f * f);
        this.factorDistance = (int) (100.0f * f);
        this.isDragDown = true;
        this.textPaddingDistanceMin = (int) (f * 20.0f);
        this.textPaddingDistanceMax = 0;
        this.opacityMin = 1.0f;
        this.opacityMax = 1.0f;
        this.backgroundOpacityMin = 1.0f;
        this.backgroundOpacityMax = 0.9f;
        this.mBackgroundOpacityMaxSubtle = 0.0f;
        this.mInverseHintColorMin = 82;
        this.mInverseHintColorMax = 255;
        this.mInverseHintOpacityMin = 255;
        this.mInverseHintOpacityMax = Math.round(153.0f);
        this.mInverseTextColorMin = 0;
        this.mInverseTextColorMax = 255;
        this.crossOverFactor = 0.5f;
        this.editText = (EditText) findViewById(R.id.search_src_text);
        this.motionFactor = 0.6666667f;
        this.alternateHintText = (TextView) findViewById(R.id.search_src_alternate_hint);
        this.mRounder = (MaskRoundedFrameLayout) findViewById(R.id.rounder);
        this.mSubtleBackground = (CenterCropOffsetImageView) findViewById(R.id.background);
        this.velocityTracker = VelocityTracker.obtain();
        this.springSystem = SpringSystem.create();
        this.spring = this.springSystem.createSpring();
        this.spring.setCurrentValue(1.0d);
        this.spring.setSpringConfig(new SpringConfig(360.0d, 40.0d));
        this.searchButton = (ImageView) findViewById(R.id.search_mag_icon);
        this.voiceButton = (ImageView) findViewById(R.id.search_voice_btn);
        this.mRounder.setVisibility(4);
        this.reactiveEndValueSpringListener = new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.view.SpringySearchView.1
            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringActivate(Spring spring) {
                SpringySearchView springySearchView = SpringySearchView.this;
                springySearchView.notifyListenerBeginIfNeeded(springySearchView.mIsTouching, SpringySearchView.this.mIsTouching, false, true);
            }

            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringAtRest(Spring spring) {
                SpringySearchView.this.applyFactor((float) spring.getCurrentValue(), true, 0.0f);
                SpringySearchView springySearchView = SpringySearchView.this;
                springySearchView.notifyListenerEndIfNeeded(springySearchView.mIsTouching, SpringySearchView.this.mIsTouching, true, false);
            }

            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                boolean z;
                float currentValue = (float) spring.getCurrentValue();
                boolean z2 = currentValue > 1.0f || currentValue < 0.0f;
                SpringySearchView.this.updateSpringEndValue(spring.getCurrentValue());
                if (spring.getEndValue() > ((double) SpringySearchView.this.crossOverFactor)) {
                    z = Math.abs(spring.getCurrentValue() - spring.getEndValue()) <= ((double) (1.0f - SpringySearchView.this.crossOverFactor));
                } else {
                    z = Math.abs(spring.getCurrentValue() - spring.getEndValue()) <= ((double) SpringySearchView.this.crossOverFactor);
                }
                SpringySearchView.this.applyFactor(currentValue, z2 || z, (float) spring.getVelocity());
            }
        };
        this.forcedEndValueSpringListener = new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.view.SpringySearchView.2
            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringActivate(Spring spring) {
                SpringySearchView springySearchView = SpringySearchView.this;
                springySearchView.notifyListenerBeginIfNeeded(springySearchView.mIsTouching, SpringySearchView.this.mIsTouching, false, true);
            }

            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringAtRest(Spring spring) {
                SpringySearchView.this.applyFactor((float) spring.getCurrentValue(), true, 0.0f);
                SpringySearchView springySearchView = SpringySearchView.this;
                springySearchView.notifyListenerEndIfNeeded(springySearchView.mIsTouching, SpringySearchView.this.mIsTouching, true, false);
            }

            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                SpringySearchView.this.applyFactor((float) spring.getCurrentValue(), Math.abs(spring.getCurrentValue() - spring.getEndValue()) <= 0.5d, (float) spring.getVelocity());
            }
        };
        applyFactor(1.0f, false, 0.0f);
    }

    private void initializeAlternativeHintSpringIfNeeded() {
        if (this.springAlternativeHintAlpha == null) {
            this.springAlternativeHintAlpha = this.springSystem.createSpring();
            this.springAlternativeHintAlpha.setSpringConfig(new SpringConfig(100.0d, 20.0d));
            this.springAlternativeHintAlpha.addListener(new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.view.SpringySearchView.4
                @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
                public void onSpringUpdate(Spring spring) {
                    SpringySearchView springySearchView = SpringySearchView.this;
                    springySearchView.applyFactor(springySearchView.currentFactor, false, 0.0f);
                }
            });
        }
    }

    public void lambda$refreshSubtleBackgroundDrawable$0(boolean z) {
        this.mSubtleBackground.setImageDrawable(this.mSubtleBackgroundDrawable);
        this.mUseInverseColors = z;
        this.mRounder.setAlpha(1.0f);
        this.applyFactor(this.currentFactor, false, 0.0f);
        this.mRounder.invalidate();
    }

    public void lambda$refreshSubtleBackgroundDrawable$1() {
        this.mSubtleBackground.setImageDrawable(new ColorDrawable(-1));
        this.mUseInverseColors = false;
        this.mRounder.setAlpha(0.625f);
        this.applyFactor(this.currentFactor, false, 0.0f);
        this.mRounder.invalidate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyListenerBeginIfNeeded(boolean z, boolean z2, boolean z3, boolean z4) {
        if (this.mOnFactorChangeListener == null || z || z3) {
            return;
        }
        this.mOnFactorChangeListener.onFactorBeginChanging();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void notifyListenerEndIfNeeded(boolean z, boolean z2, boolean z3, boolean z4) {
        if (this.mOnFactorChangeListener == null || z2 || z4) {
            return;
        }
        this.mOnFactorChangeListener.onFactorEndChanging();
    }

    private void setBackgroundAlpha(int i) {
        LayerDrawable layerDrawable = this.mBackgroundDrawable;
        if (layerDrawable != null) {
            layerDrawable.setAlpha(Math.max(0, Math.min(255, Math.round(i))));
        }
    }

    private void setTextPadding(int i) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.editText.getLayoutParams();
        marginLayoutParams.leftMargin = i;
        this.editText.setLayoutParams(marginLayoutParams);
        ViewGroup.MarginLayoutParams marginLayoutParams2 = (ViewGroup.MarginLayoutParams) this.alternateHintText.getLayoutParams();
        marginLayoutParams2.leftMargin = i;
        this.alternateHintText.setLayoutParams(marginLayoutParams2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateSpringEndValue(double d) {
        if (d < this.crossOverFactor) {
            this.spring.setEndValue(0.0d);
        } else {
            this.spring.setEndValue(1.0d);
        }
    }

    public void applyFactor(float f, boolean z, float f2) {
        int iRound;
        int iRound2;
        int iRound3;
        this.currentFactor = f;
        int i = this.textPaddingDistanceMax - this.textPaddingDistanceMin;
        float f3 = this.opacityMax - this.opacityMin;
        float f4 = this.mUseSubtleBackground ? this.mBackgroundOpacityMaxSubtle : this.backgroundOpacityMax;
        float f5 = this.backgroundOpacityMin;
        float f6 = this.mBackgroundAlternativeFactorOffset + f;
        int iMax = this.textPaddingDistanceMin + ((int) (i * f));
        float fMax = this.opacityMin + (f3 * f6);
        float f7 = f5 + ((f4 - f5) * f6);
        if (this.mUseSubtleBackground && this.mUseInverseColors) {
            float f8 = 1.0f - f6;
            iRound = Math.round((this.mInverseTextColorMin * f8) + (this.mInverseTextColorMax * f6));
            iRound2 = Math.round((this.mInverseHintColorMin * f8) + (this.mInverseHintColorMax * f6));
            iRound3 = Math.round((this.mInverseHintOpacityMin * f8) + (this.mInverseHintOpacityMax * f6));
        } else {
            iRound = -16777216;
            iRound2 = 82;
            iRound3 = 255;
        }
        if (z) {
            if (f >= 1.0f || (f >= this.crossOverFactor && f2 > 0.0f)) {
                if (hasFocus() || this.editText.hasFocus()) {
                    clearFocus();
                    this.editText.clearFocus();
                }
            } else if (f <= 0.0f || (f <= this.crossOverFactor && f2 < 0.0f)) {
                KeyboardRulesHandler keyboardRulesHandler = this.keyboardRulesHandler;
                boolean z2 = (keyboardRulesHandler == null || keyboardRulesHandler.canDisplayKeyboard()) ? false : true;
                if (!hasFocus() && !this.editText.hasFocus() && !z2) {
                    requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
                    inputMethodManager.viewClicked(this.editText);
                    inputMethodManager.showSoftInput(this.editText, 1);
                }
            }
        }
        if (this.limitTextPaddingBounds) {
            iMax = Math.max(Math.min(this.textPaddingDistanceMin, this.textPaddingDistanceMax), Math.min(iMax, Math.max(this.textPaddingDistanceMin, this.textPaddingDistanceMax)));
        }
        if (this.limitOpacityBounds) {
            fMax = Math.max(Math.min(this.opacityMin, this.opacityMax), Math.min(fMax, Math.max(this.opacityMin, this.opacityMax)));
        }
        setTextPadding(iMax);
        setAlpha(fMax);
        if (this.mBackgroundDrawable != null) {
            setBackgroundAlpha(Math.round(f7 * 255.0f));
        }
        this.editText.setTextColor(colorWithAlphaAndRgb(255, iRound));
        this.editText.setHintTextColor(colorWithAlphaAndRgb(iRound3, iRound2));
        this.alternateHintText.setTextColor(colorWithAlphaAndRgb(iRound3, iRound2));
        this.searchButton.setColorFilter(colorWithAlphaAndRgb(255, iRound2));
        this.searchButton.setImageAlpha(Math.max(0, Math.min(iRound3, 255)));
        this.voiceButton.setColorFilter(colorWithAlphaAndRgb(255, iRound2));
        this.voiceButton.setImageAlpha(Math.max(0, Math.min(iRound3, 255)));
        Spring spring = this.springAlternativeHintAlpha;
        if (spring != null) {
            float currentValue = (float) spring.getCurrentValue();
            float f9 = (1.0f - currentValue) + (currentValue * (1.0f - this.currentFactor));
            float fLimitAlpha = AlphaUtil.limitAlpha(f9 < 0.5f ? (0.5f - f9) / 0.5f : 0.0f);
            float fLimitAlpha2 = AlphaUtil.limitAlpha(f9 > 0.5f ? (f9 - 0.5f) / 0.5f : 0.0f);
            this.alternateHintText.setAlpha(fLimitAlpha);
            this.editText.setAlpha(fLimitAlpha2);
        }
        OnFactorChangeListener onFactorChangeListener = this.mOnFactorChangeListener;
        if (onFactorChangeListener != null) {
            onFactorChangeListener.onFactorChanged(f);
        }
    }

    public void cancelMotion() {
        float currentFactor = getCurrentFactor();
        this.spring.setVelocity(0.0d);
        updateSpringEndValue(currentFactor);
    }

    public float getAlternateHintTextModeTarget() {
        Spring spring = this.springAlternativeHintAlpha;
        if (spring == null) {
            return 0.0f;
        }
        return (float) spring.getEndValue();
    }

    public float getCrossOverFactor() {
        return this.crossOverFactor;
    }

    public float getCurrentFactor() {
        return this.currentFactor;
    }

    public Float getEndValueIfAvailable() {
        if (this.spring.isAtRest()) {
            return null;
        }
        return Float.valueOf((float) this.spring.getEndValue());
    }

    public int getFactorDistance() {
        return this.factorDistance;
    }

    public void hideAlternateHintTextMode(boolean z) {
        initializeAlternativeHintSpringIfNeeded();
        if (z) {
            this.springAlternativeHintAlpha.setEndValue(0.0d);
        } else {
            this.springAlternativeHintAlpha.setEndValue(0.0d);
            this.springAlternativeHintAlpha.setCurrentValue(0.0d, true);
        }
    }

    public void hideSearchProgressSpinner() {
        ImageView imageView = (ImageView) findViewById(R.id.search_mag_icon);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.search_progressbar);
        if (imageView == null || progressBar == null) {
            return;
        }
        imageView.setVisibility(0);
        progressBar.setVisibility(8);
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        VelocityTracker velocityTracker = this.velocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.velocityTracker = null;
        }
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    protected void onSizeChanged(final int i, final int i2, int i3, int i4) {
        if (i == i3 && i2 == i4) {
            return;
        }
        if (this.mBackgroundDrawable == null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(Color.argb(30, 0, 0, 0));
            GradientDrawable gradientDrawable2 = new GradientDrawable();
            gradientDrawable2.setColor(-1);
            this.mBackgroundDrawable = new LayerDrawable(new Drawable[]{gradientDrawable, gradientDrawable2});
            this.mBackgroundDrawable.setLayerInset(1, 0, 0, 0, Math.round(this.mDensity * 1.0f));
            this.mBackgroundDrawable.setLayerInset(0, 0, Math.round(this.mDensity * 1.0f), 0, 0);
            findViewById(R.id.search_bar).setBackground(this.mBackgroundDrawable);
        if (Utilities.ATLEAST_LOLLIPOP) {
            setOutlineProvider(new ViewOutlineProvider() { // from class: com.voxel.simplesearchlauncher.view.SpringySearchView.3
                @Override // android.view.ViewOutlineProvider
                @TargetApi(21)
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, i, i2, SpringySearchView.this.mBackgroundDrawableCornerRadius);
                }
            });
        }
    }
        float f = i2 / 2.0f;
        this.mBackgroundDrawableCornerRadius = f;
        for (int i5 = 0; i5 < this.mBackgroundDrawable.getNumberOfLayers(); i5++) {
            ((GradientDrawable) this.mBackgroundDrawable.getDrawable(i5)).setCornerRadius(this.mBackgroundDrawableCornerRadius);
        }
        if (Utilities.ATLEAST_LOLLIPOP) {
            invalidateOutline();
        }
        this.mRounder.setCornerRadius(f);
        setTouchDelegate(new TouchDelegate(new Rect(0, 0, i, i2), this.editText));
        applyFactor(this.currentFactor, false, 0.0f);
    }

    public void refreshSubtleBackgroundDrawable(Drawable drawable, boolean z) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
        boolean z2 = !z;
        Drawable drawable2 = null;
        if (drawable == null) {
            try {
                Drawable drawable3 = wallpaperManager.getDrawable();
                if (Launcher.isWallpaperDrawableValid(drawable3)) {
                    drawable2 = drawable3;
                } else {
                    z2 = true;
                }
            } catch (RuntimeException e) {
                Timber.e(e);
                z2 = true;
            }
        } else {
            drawable2 = drawable;
        }
        if (drawable2 == null || z2) {
            post(new Runnable() { // from class: com.voxel.simplesearchlauncher.view.-$$Lambda$SpringySearchView$o_X_zcrJsFtY9SYDqUMTsa-hSXQ
                @Override // java.lang.Runnable
                public final void run() {
                    SpringySearchView.this.lambda$refreshSubtleBackgroundDrawable$1();
                }
            });
            return;
        }
        int[] iArr = new int[2];
        getLocationOnScreen(iArr);
        iArr[1] = iArr[1] - ((int) getTranslationY());
        drawable2.setBounds(0, 0, drawable2.getIntrinsicWidth(), drawable2.getIntrinsicHeight());
        int iCeil = (int) Math.ceil((drawable2.getIntrinsicWidth() - (iArr[0] * 2)) * 0.25f);
        int iCeil2 = (int) Math.ceil(getHeight() * 0.25f);
        if (iCeil <= 0 || iCeil2 <= 0) {
            return;
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iCeil, iCeil2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCreateBitmap);
        Matrix matrix = new Matrix();
        float f = (-iArr[1]) * 0.25f;
        float f2 = (-iArr[0]) * 0.25f;
        matrix.setScale(0.25f, 0.25f);
        matrix.postTranslate(f2, f);
        canvas.save();
        canvas.concat(matrix);
        drawable2.draw(canvas);
        canvas.restore();
        int tint = getTint(bitmapCreateBitmap);
        Paint paint = new Paint();
        paint.setColor(tint);
        canvas.drawPaint(paint);
        final boolean z3 = ColorUtil.rgbToHsl(Color.red(tint), Color.green(tint), Color.blue(tint))[2] < 0.9f;
        this.mSubtleBackgroundDrawable = new FastBitmapDrawable(Blur.fastBlur(getContext(), bitmapCreateBitmap, 10, 0.7f));
        post(new Runnable() { // from class: com.voxel.simplesearchlauncher.view.-$$Lambda$SpringySearchView$gGpnx8kpsls8IV8lGvINAhtTDKI
            @Override // java.lang.Runnable
            public final void run() {
                SpringySearchView.this.lambda$refreshSubtleBackgroundDrawable$0( z3);
            }
        });
    }

    public void sendMotionEvent(MotionEvent motionEvent) {
        float fEstimateStopPosition;
        if (this.velocityTracker == null) {
            this.velocityTracker = VelocityTracker.obtain();
        }
        this.velocityTracker.addMovement(motionEvent);
        if (motionEvent.getAction() != 3 && motionEvent.getAction() != 1) {
            boolean z = !this.spring.wasAtRest();
            this.spring.setAtRest();
            boolean z2 = this.mIsTouching;
            this.mIsTouching = true;
            notifyListenerBeginIfNeeded(z2, this.mIsTouching, z, false);
            if (motionEvent.getAction() != 0) {
                float f = this.previousY;
                if (f != 0.0f) {
                    float y = f - motionEvent.getY();
                    this.previousY = motionEvent.getY();
                    applyFactor(calculateNewFactor(y), true, 0.0f);
                    return;
                }
            }
            this.previousY = motionEvent.getY();
            return;
        }
        this.previousY = 0.0f;
        float f2 = this.currentFactor;
        if (f2 == 0.0f || f2 == 1.0f) {
            return;
        }
        float currentFactor = getCurrentFactor();
        this.spring.removeAllListeners();
        this.spring.addListener(this.reactiveEndValueSpringListener);
        this.velocityTracker.computeCurrentVelocity(1000);
        float yVelocity = ((this.velocityTracker.getYVelocity() * this.motionFactor) / this.factorDistance) / calculateOvershootFactor(currentFactor);
        float f3 = this.isDragDown ? yVelocity * (-1.0f) : yVelocity;
        double d = currentFactor;
        this.spring.setCurrentValue(d);
        this.spring.setVelocity(f3);
        Float fCalculateEndValueWithVelocity = calculateEndValueWithVelocity(this.velocityTracker.getYVelocity());
        if (fCalculateEndValueWithVelocity != null) {
            fEstimateStopPosition = fCalculateEndValueWithVelocity.floatValue();
            setEndValue(fCalculateEndValueWithVelocity.floatValue());
        } else {
            updateSpringEndValue(d);
            fEstimateStopPosition = estimateStopPosition(currentFactor, (float) this.spring.getEndValue(), f3, (float) this.spring.getSpringConfig().tension, (float) this.spring.getSpringConfig().friction);
        }
        if (fEstimateStopPosition <= this.crossOverFactor) {
            KeyboardRulesHandler keyboardRulesHandler = this.keyboardRulesHandler;
            boolean z3 = (keyboardRulesHandler == null || keyboardRulesHandler.canDisplayKeyboard()) ? false : true;
            if (!hasFocus() && !this.editText.hasFocus() && !z3) {
                requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService("input_method");
                inputMethodManager.viewClicked(this.editText);
                inputMethodManager.showSoftInput(this.editText, 1);
            }
        } else if (hasFocus() || this.editText.hasFocus()) {
            clearFocus();
            this.editText.clearFocus();
        }
        boolean z4 = !this.spring.wasAtRest();
        boolean z5 = !this.spring.isAtRest();
        boolean z6 = this.mIsTouching;
        this.mIsTouching = false;
        notifyListenerEndIfNeeded(z6, this.mIsTouching, z4, z5);
    }

    public void setBackgroundAlternativeFactorOffset(float f) {
        this.mBackgroundAlternativeFactorOffset = f;
        applyFactor(this.currentFactor, false, 0.0f);
    }

    public void setCrossOverFactor(float f) {
        this.crossOverFactor = f;
    }

    public void setEndValue(float f) {
        this.spring.removeAllListeners();
        this.spring.addListener(this.forcedEndValueSpringListener);
        this.spring.setEndValue(f);
        this.spring.setCurrentValue(this.currentFactor, false);
    }

    public void setFactorDistance(int i) {
        this.factorDistance = i;
    }

    public void setKeyboardRulesHandler(KeyboardRulesHandler keyboardRulesHandler) {
        this.keyboardRulesHandler = keyboardRulesHandler;
    }

    public void setOnFactorChangeListener(OnFactorChangeListener onFactorChangeListener) {
        this.mOnFactorChangeListener = onFactorChangeListener;
    }

    public void setSubtleOffsets(float f, float f2) {
        this.mSubtleBackground.setOffsets(f, f2);
        this.mRounder.invalidate();
    }

    public void setUseSubtleBackground(boolean z) {
        this.mUseSubtleBackground = z;
        this.mRounder.setVisibility(0);
        applyFactor(this.currentFactor, false, 0.0f);
    }

    public void showSearchProgressSpinner() {
        ImageView imageView = (ImageView) findViewById(R.id.search_mag_icon);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.search_progressbar);
        if (imageView == null || progressBar == null) {
            return;
        }
        imageView.setVisibility(4);
        progressBar.setVisibility(0);
    }
}
