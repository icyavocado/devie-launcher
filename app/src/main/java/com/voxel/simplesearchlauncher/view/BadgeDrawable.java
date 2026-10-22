package com.voxel.simplesearchlauncher.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.TypedValue;
import com.voxel.launcher3.DeviceProfile;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.graphics.IconPalette;
import com.voxel.simplesearchlauncher.notification.NotificationController;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class BadgeDrawable extends Drawable {
    public static final String TAG = "BadgeDrawable";
    private static Bitmap sBitmap = null;
    private static float sScaleFactor = 0.85f;
    private static NotificationController.Style sStyle;
    private float mAnimationScaleFactor;
    private String mBadgeText;
    private Rect mBaseBounds;
    private Context mContext;
    private Paint mDebugPaint;
    private IconPalette mIconPalette;
    private int mIconSizePx;
    private Rect mMaxBounds;
    private NinePatchDrawable mNinePatchDrawable;
    private Placement mPlacement;
    private float mScaleFactor;
    private Paint mTextPaint;
    private boolean mBadgeEnabled = true;
    /* REWORK: jadx hoisted these dpToPx() initializers into field init, which ran
       before mContext was assigned (NPE at BadgeableAppIcon construction); they
       belong in the constructor, after mContext is set. */
    private float mTextHeight;
    private float mMinWidth;
    private float mPaddingHorizontal;
    private float mPaddingTop;

    public enum Placement {
        TOP_LEFT(0, 0),
        TOP_RIGHT(1, 0),
        BOTTOM_LEFT(0, 1),
        BOTTOM_RIGHT(1, 1);

        int left;
        int top;

        Placement(int i, int i2) {
            this.left = i;
            this.top = i2;
        }
    }

    public BadgeDrawable(Context context, IconPalette iconPalette) {
        this.mContext = context;
        this.mTextHeight = dpToPx(12.5f);
        this.mMinWidth = dpToPx(12.0f);
        this.mPaddingHorizontal = dpToPx(5.5f);
        this.mPaddingTop = dpToPx(7.5f);
        NotificationController notificationController = NotificationController.getInstance(context);
        sScaleFactor = notificationController.getBadgeScale();
        sStyle = notificationController.getBadgeStyle();
        this.mScaleFactor = 1.0f;
        this.mAnimationScaleFactor = 1.0f;
        this.mDebugPaint = new Paint();
        this.mDebugPaint.setStyle(Paint.Style.STROKE);
        this.mDebugPaint.setStrokeWidth(dpToPx(1.0f));
        this.mDebugPaint.setColor(Color.parseColor("#D24436"));
        this.mDebugPaint.setAntiAlias(true);
        this.mTextPaint = new Paint();
        this.mTextPaint.setStyle(Paint.Style.FILL);
        this.mTextPaint.setColor(-1);
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setTextSize(this.mTextHeight);
        setIconPalette(iconPalette);
        this.mPlacement = Placement.TOP_RIGHT;
        createNinePatch();
    }

    private void computeLayout() {
        Rect rect;
        if (!hasBadge() || (rect = this.mBaseBounds) == null || this.mMaxBounds == null) {
            return;
        }
        this.mScaleFactor = ((rect.width() * 1.0f) / this.mIconSizePx) * sScaleFactor;
        this.mTextPaint.setTextSize(this.mTextHeight);
        float fMeasureText = this.mTextPaint.measureText(this.mBadgeText);
        float f = this.mMinWidth;
        if (fMeasureText < f) {
            fMeasureText = f;
        }
        float f2 = fMeasureText + (this.mPaddingHorizontal * 2.0f);
        float intrinsicHeight = this.mNinePatchDrawable.getIntrinsicHeight();
        float f3 = this.mScaleFactor;
        float f4 = f2 * f3;
        float f5 = f3 * intrinsicHeight;
        float totalScaleFactor = getTotalScaleFactor() * f2;
        float totalScaleFactor2 = getTotalScaleFactor() * intrinsicHeight;
        Rect rect2 = new Rect();
        if (this.mPlacement.left == 0) {
            rect2.left = (int) (this.mBaseBounds.left - (0.19999999f * f4));
            if (rect2.left < this.mMaxBounds.left) {
                rect2.left = this.mMaxBounds.left;
            }
        } else {
            rect2.left = (int) (this.mBaseBounds.right - (0.8f * f4));
            if (rect2.left + f4 > this.mMaxBounds.right) {
                rect2.left = this.mMaxBounds.right - ((int) f4);
            }
        }
        if (this.mPlacement.top == 0) {
            rect2.top = (int) (this.mBaseBounds.top - (0.0f * f5));
            if (rect2.top < this.mMaxBounds.top) {
                rect2.top = this.mMaxBounds.top;
            }
        } else {
            rect2.top = (int) (this.mBaseBounds.bottom - (1.0f * f5));
            if (rect2.top + f5 > this.mMaxBounds.bottom) {
                rect2.top = this.mMaxBounds.bottom - ((int) f5);
            }
        }
        rect2.left = (int) (rect2.left + ((f4 - totalScaleFactor) / 2.0f));
        rect2.top = (int) (rect2.top + ((f5 - totalScaleFactor2) / 2.0f));
        rect2.right = (int) (rect2.left + totalScaleFactor);
        rect2.bottom = (int) (rect2.top + totalScaleFactor2);
        setBounds(rect2);
        this.mNinePatchDrawable.setBounds(new Rect(0, 0, (int) f2, (int) intrinsicHeight));
    }

    public static synchronized Bitmap getBitmap(Context context) {
        if (sBitmap == null) {
            sBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.badge_red_rounded);
        }
        return sBitmap;
    }

    private float getTotalScaleFactor() {
        return this.mAnimationScaleFactor * this.mScaleFactor;
    }

    public static void setScaleFactor(float f) {
        sScaleFactor = f;
    }

    public static void setStyle(NotificationController.Style style) {
        sStyle = style;
    }

    protected void createNinePatch() {
        Bitmap bitmap = getBitmap(this.mContext);
        byte[] ninePatchChunk = bitmap.getNinePatchChunk();
        if (!NinePatch.isNinePatchChunk(ninePatchChunk)) {
            throw new RuntimeException("Could not find a 9-patch resources for badge");
        }
        this.mNinePatchDrawable = new NinePatchDrawable(this.mContext.getResources(), bitmap, ninePatchChunk, new Rect(), null);
    }

    protected float dpToPx(float f) {
        return TypedValue.applyDimension(1, f, this.mContext.getResources().getDisplayMetrics());
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (!hasBadge() || this.mBaseBounds == null || this.mNinePatchDrawable == null || this.mMaxBounds == null) {
            return;
        }
        computeLayout();
        if (sStyle == NotificationController.Style.DOTS) {
            BadgeRenderer.getInstance().draw(canvas, this.mIconPalette, getBaseBounds(), getMaxBounds(), this.mIconSizePx, this.mScaleFactor, this.mAnimationScaleFactor);
            return;
        }
        float totalScaleFactor = getTotalScaleFactor();
        Rect bounds = getBounds();
        canvas.save();
        canvas.translate(bounds.left, bounds.top);
        canvas.scale(totalScaleFactor, totalScaleFactor);
        this.mNinePatchDrawable.draw(canvas);
        canvas.restore();
        Rect rect = new Rect();
        this.mTextPaint.setTextSize(this.mTextHeight * totalScaleFactor);
        this.mTextPaint.getTextBounds("a", 0, 1, rect);
        canvas.drawText(this.mBadgeText, bounds.left + ((bounds.width() - this.mTextPaint.measureText(this.mBadgeText)) / 2.0f), bounds.top + rect.height() + (this.mPaddingTop * totalScaleFactor), this.mTextPaint);
    }

    public float getAnimationScale() {
        return this.mAnimationScaleFactor;
    }

    public Rect getBaseBounds() {
        return this.mBaseBounds;
    }

    public Rect getMaxBounds() {
        return this.mMaxBounds;
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    public boolean hasBadge() {
        String str;
        return (!this.mBadgeEnabled || (str = this.mBadgeText) == null || str.isEmpty()) ? false : true;
    }

    public boolean isBadgeEnabled() {
        return this.mBadgeEnabled;
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.mTextPaint.setAlpha(i);
        this.mNinePatchDrawable.setAlpha(i);
    }

    public void setAnimationScale(float f) {
        this.mAnimationScaleFactor = f;
        invalidateSelf();
    }

    public void setBadge(int i) {
        if (i == 0) {
            setBadge((String) null);
        } else if (i > 99) {
            setBadge("99+");
        } else {
            setBadge(Integer.toString(i));
        }
    }

    public void setBadge(String str) {
        this.mBadgeText = str;
        computeLayout();
        invalidateSelf();
    }

    public void setBadgeEnabled(boolean z) {
        if (this.mBadgeEnabled == z) {
            return;
        }
        this.mBadgeEnabled = z;
    }

    public void setBaseBounds(Rect rect) {
        this.mIconSizePx = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).getIconSizePx();
        this.mBaseBounds = rect;
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.mNinePatchDrawable.setColorFilter(colorFilter);
    }

    public void setIconPalette(IconPalette iconPalette) {
        if (iconPalette == null) {
            iconPalette = IconPalette.getBadgePalette(this.mContext.getResources());
        }
        this.mIconPalette = iconPalette;
        invalidateSelf();
    }

    public void setMaxBounds(Rect rect) {
        this.mMaxBounds = rect;
        invalidateSelf();
    }
}
