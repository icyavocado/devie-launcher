package com.voxel.launcher3;

/* REWORK: grid fallback for unset cell params */
import com.voxel.launcher3.DeviceProfile.ParamType;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Property;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.voxel.simplesearchlauncher.notification.NotificationCount;
import com.voxel.simplesearchlauncher.notification.Notifier;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.voxel.simplesearchlauncher.view.BadgeDrawable;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class BubbleTextView extends TextView implements Notifier.OnNotificationChangeListener {
    private final Drawable mBackground;
    private boolean mBackgroundSizeChanged;
    private BadgeDrawable mBadgeDrawable;
    private CellParams mCellParams;
    private int mCustomShadowLargeColor;
    private int mCustomShadowSmallColor;
    private final boolean mCustomShadowsEnabled;
    private boolean mDayChangeReceiverRegistered;
    private BroadcastReceiver mDayChangedReceiver;
    private boolean mIgnoreKeyPressedStateChange;
    private boolean mIgnorePressedStateChange;
    private boolean mIsTextVisible;
    private CheckLongPressHelper mLongPressHelper;
    private HolographicOutlineHelper mOutlineHelper;
    private Bitmap mPressedBackground;
    private float mSlop;
    private boolean mStayPressed;
    private int mTextColor;
    private static SparseArray<Resources.Theme> sPreloaderThemes = new SparseArray<>(2);
    private static final Property<BubbleTextView, Float> BADGE_SCALE_PROPERTY = new Property<BubbleTextView, Float>(Float.TYPE, "badgeScale") { // from class: com.voxel.launcher3.BubbleTextView.1
        @Override // android.util.Property
        public Float get(BubbleTextView bubbleTextView) {
            return Float.valueOf(bubbleTextView.mBadgeDrawable.getAnimationScale());
        }

        @Override // android.util.Property
        public void set(BubbleTextView bubbleTextView, Float f) {
            bubbleTextView.mBadgeDrawable.setAnimationScale(f.floatValue());
            bubbleTextView.invalidate();
        }
    };

    public BubbleTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BubbleTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCustomShadowLargeColor = -587202560;
        this.mCustomShadowSmallColor = -872415232;
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.BubbleTextView, i, 0);
        this.mCustomShadowsEnabled = typedArrayObtainStyledAttributes.getBoolean(0, true);
        typedArrayObtainStyledAttributes.recycle();
        if (this.mCustomShadowsEnabled) {
            this.mBackground = getBackground();
            setBackground(null);
        } else {
            this.mBackground = null;
        }
        init();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyCellParams() {
        if (this.mCellParams == null) {
            /* REWORK: createShortcut can bind while workspace.getChildAt(currentPage)
               is still null (page not created yet during LoaderTask binding), leaving
               cell params unset; fall back to the grid's workspace params. */
            DynamicGrid dynamicGrid = LauncherAppState.getInstance().getDynamicGrid();
            if (dynamicGrid != null) {
                this.mCellParams = dynamicGrid.getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
            }
            if (this.mCellParams == null) {
                return;
            }
        }
        setTextSize(0, this.mCellParams.iconTextSizePx);
        setTextVisibility(this.mCellParams.showLabels);
        Drawable drawable = getCompoundDrawables()[1];
        if (drawable != null) {
            drawable.setBounds(0, 0, this.mCellParams.iconSizePx, this.mCellParams.iconSizePx);
            setCompoundDrawables(null, drawable, null, null);
        }
        setCompoundDrawablePadding(this.mCellParams.iconDrawablePaddingPx);
    }

    private Resources.Theme getPreloaderTheme() {
        Object tag = getTag();
        int i = (tag == null || !(tag instanceof ShortcutInfo) || ((ShortcutInfo) tag).container < 0) ? R.style.PreloadIcon : R.style.PreloadIcon_Folder;
        Resources.Theme theme = sPreloaderThemes.get(i);
        if (theme != null) {
            return theme;
        }
        Resources.Theme themeNewTheme = getResources().newTheme();
        themeNewTheme.applyStyle(i, true);
        sPreloaderThemes.put(i, themeNewTheme);
        return themeNewTheme;
    }

    private void init() {
        this.mLongPressHelper = new CheckLongPressHelper(this);
        this.mOutlineHelper = HolographicOutlineHelper.obtain(getContext());
        if (this.mCustomShadowsEnabled) {
            setShadowLayer(4.0f, 0.0f, 2.0f, -587202560);
        }
    }

    private void registerDayChangedReceiver() {
        if (this.mDayChangedReceiver == null || this.mDayChangeReceiverRegistered) {
            return;
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(this.mDayChangedReceiver, new IntentFilter("com.evie.day_changed"));
        this.mDayChangeReceiverRegistered = true;
    }

    private void updateIconState() {
        boolean z = true;
        Drawable drawable = getCompoundDrawables()[1];
        if (drawable instanceof FastBitmapDrawable) {
            FastBitmapDrawable fastBitmapDrawable = (FastBitmapDrawable) drawable;
            if (!isPressed() && !this.mStayPressed) {
                z = false;
            }
            fastBitmapDrawable.setPressed(z);
        }
    }

    public void applyFromShortcutInfo(ShortcutInfo shortcutInfo, IconCache iconCache) {
        applyFromShortcutInfo(shortcutInfo, iconCache, true, false);
    }

    public void applyFromShortcutInfo(final ShortcutInfo shortcutInfo, final IconCache iconCache, boolean z, boolean z2) {
        if (iconCache.isCalendarApp(shortcutInfo.getIntent())) {
            if (this.mDayChangedReceiver == null) {
                this.mDayChangedReceiver = new BroadcastReceiver() { // from class: com.voxel.launcher3.BubbleTextView.2
                    @Override // android.content.BroadcastReceiver
                    public void onReceive(Context context, Intent intent) {
                        shortcutInfo.updateIcon(iconCache);
                        FastBitmapDrawable fastBitmapDrawableCreateIconDrawable = Utilities.createIconDrawable(shortcutInfo.getIcon(iconCache));
                        fastBitmapDrawableCreateIconDrawable.setGhostModeEnabled(shortcutInfo.isDisabled > 0);
                        BubbleTextView.this.setCompoundDrawables(null, fastBitmapDrawableCreateIconDrawable, null, null);
                        BubbleTextView.this.applyCellParams();
                    }
                };
            }
            registerDayChangedReceiver();
            shortcutInfo.updateIcon(iconCache);
        }
        FastBitmapDrawable fastBitmapDrawableCreateIconDrawable = Utilities.createIconDrawable(shortcutInfo.getIcon(iconCache));
        fastBitmapDrawableCreateIconDrawable.setGhostModeEnabled(shortcutInfo.isDisabled > 0);
        Notifier notifier = Notifier.getInstance(getContext().getApplicationContext());
        notifier.unregisterListenerForAll(this);
        if (Notifier.supportsNotifications(shortcutInfo)) {
            ComponentName component = shortcutInfo.getIntent().getComponent();
            int count = notifier.getCount(component.getPackageName(), component.getClassName());
            notifier.registerListener(component.getPackageName(), this);
            this.mBadgeDrawable = new BadgeDrawable(getContext(), fastBitmapDrawableCreateIconDrawable.getIconPalette());
            this.mBadgeDrawable.setBadge(count);
            this.mBadgeDrawable.setCallback(this);
        }
        setCompoundDrawables(null, fastBitmapDrawableCreateIconDrawable, null, null);
        if (shortcutInfo.contentDescription != null) {
            setContentDescription(shortcutInfo.contentDescription);
        }
        setText(shortcutInfo.title);
        setTag(shortcutInfo, z);
        applyCellParams();
        if (z2 || shortcutInfo.isPromise()) {
            applyState(z2);
        }
    }

    public void applyState(boolean z) {
        int installProgress;
        PreloadIconDrawable preloadIconDrawable;
        if (getTag() instanceof ShortcutInfo) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) getTag();
            if (shortcutInfo.isPromise()) {
                installProgress = shortcutInfo.hasStatusFlag(4) ? shortcutInfo.getInstallProgress() : 0;
            } else {
                installProgress = 100;
            }
            Drawable[] compoundDrawables = getCompoundDrawables();
            Drawable drawable = compoundDrawables[1];
            if (drawable != null) {
                if (drawable instanceof PreloadIconDrawable) {
                    preloadIconDrawable = (PreloadIconDrawable) drawable;
                } else {
                    PreloadIconDrawable preloadIconDrawable2 = new PreloadIconDrawable(drawable, getPreloaderTheme());
                    setCompoundDrawables(compoundDrawables[0], preloadIconDrawable2, compoundDrawables[2], compoundDrawables[3]);
                    preloadIconDrawable = preloadIconDrawable2;
                }
                preloadIconDrawable.setLevel(installProgress);
                if (z) {
                    preloadIconDrawable.maybePerformFinishedAnimation();
                }
            }
        }
    }

    @Override // android.widget.TextView, android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        this.mLongPressHelper.cancelLongPress();
    }

    void clearPressedBackground() {
        setPressed(false);
        setStayPressed(false);
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        if (!this.mCustomShadowsEnabled) {
            super.draw(canvas);
            drawBadge(canvas);
            return;
        }
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            int scrollX = getScrollX();
            int scrollY = getScrollY();
            if (this.mBackgroundSizeChanged) {
                drawable.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());
                this.mBackgroundSizeChanged = false;
            }
            if ((scrollX | scrollY) == 0) {
                drawable.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                drawable.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
        if (getCurrentTextColor() == getResources().getColor(android.R.color.transparent)) {
            getPaint().clearShadowLayer();
            super.draw(canvas);
            drawBadge(canvas);
            return;
        }
        getPaint().setShadowLayer(4.0f, 0.0f, 2.0f, this.mCustomShadowLargeColor);
        super.draw(canvas);
        canvas.save();
        canvas.clipRect(getScrollX(), getScrollY() + getExtendedPaddingTop(), getScrollX() + getWidth(), getScrollY() + getHeight(), Region.Op.INTERSECT);
        getPaint().setShadowLayer(1.75f, 0.0f, 0.0f, this.mCustomShadowSmallColor);
        super.draw(canvas);
        canvas.restore();
        drawBadge(canvas);
    }

    protected void drawBadge(Canvas canvas) {
        if (this.mBadgeDrawable == null) {
            return;
        }
        canvas.save();
        canvas.translate(getScrollX() + getCompoundPaddingLeft() + (((((getRight() - getLeft()) - getCompoundPaddingRight()) - getCompoundPaddingLeft()) - this.mBadgeDrawable.getBaseBounds().width()) / 2), getScrollY() + getPaddingTop());
        this.mBadgeDrawable.draw(canvas);
        canvas.restore();
    }

    public CellParams getCellParams() {
        return this.mCellParams;
    }

    public int getCustomShadowLargeColor() {
        return this.mCustomShadowLargeColor;
    }

    public int getCustomShadowSmallColor() {
        return this.mCustomShadowSmallColor;
    }

    public boolean isCustomShadowsEnabled() {
        return this.mCustomShadowsEnabled;
    }

    @Override // android.widget.TextView, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setCallback(this);
        }
        Drawable drawable2 = getCompoundDrawables()[1];
        if (drawable2 instanceof PreloadIconDrawable) {
            drawable2.applyTheme(getPreloaderTheme());
        }
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        if (this.mDayChangedReceiver != null) {
            registerDayChangedReceiver();
            this.mDayChangedReceiver.onReceive(getContext(), null);
        }
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Drawable drawable = this.mBackground;
        if (drawable != null) {
            drawable.setCallback(null);
        }
        if (this.mDayChangedReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this.mDayChangedReceiver);
            this.mDayChangeReceiverRegistered = false;
        }
    }

    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
    }

    @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (!super.onKeyDown(i, keyEvent)) {
            return false;
        }
        if (this.mPressedBackground != null) {
            return true;
        }
        this.mPressedBackground = this.mOutlineHelper.createMediumDropShadow(this);
        return true;
    }

    @Override // android.widget.TextView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        this.mIgnoreKeyPressedStateChange = true;
        boolean zOnKeyUp = super.onKeyUp(i, keyEvent);
        this.mPressedBackground = null;
        this.mIgnoreKeyPressedStateChange = false;
        updateIconState();
        return zOnKeyUp;
    }

    @Override // com.voxel.simplesearchlauncher.notification.Notifier.OnNotificationChangeListener
    public void onNotificationChanged(NotificationCount notificationCount) {
        BadgeDrawable badgeDrawable = this.mBadgeDrawable;
        if (badgeDrawable == null) {
            return;
        }
        badgeDrawable.setBadge(notificationCount.count);
        postInvalidate();
    }

    @Override // android.view.View
    protected boolean onSetAlpha(int i) {
        return true;
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean zOnTouchEvent = super.onTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case 0:
                if (this.mPressedBackground == null) {
                    this.mPressedBackground = this.mOutlineHelper.createMediumDropShadow(this);
                }
                this.mLongPressHelper.postCheckForLongPress();
                return zOnTouchEvent;
            case 1:
            case 3:
                if (!isPressed()) {
                    this.mPressedBackground = null;
                }
                this.mLongPressHelper.cancelLongPress();
                return zOnTouchEvent;
            case 2:
                if (!Utilities.pointInView(this, motionEvent.getX(), motionEvent.getY(), this.mSlop)) {
                    this.mLongPressHelper.cancelLongPress();
                }
                return zOnTouchEvent;
            default:
                return zOnTouchEvent;
        }
    }

    public void setBadgeEnabled(boolean z) {
        BadgeDrawable badgeDrawable = this.mBadgeDrawable;
        if (badgeDrawable == null || badgeDrawable.isBadgeEnabled() == z) {
            return;
        }
        this.mBadgeDrawable.setBadgeEnabled(z);
        if (!z) {
            invalidate();
        } else if (this.mBadgeDrawable.hasBadge()) {
            ObjectAnimator.ofFloat(this, BADGE_SCALE_PROPERTY, 0.0f, 1.0f).start();
        }
    }

    public void setCellParams(CellParams cellParams) {
        if (cellParams == null) {
            throw new IllegalArgumentException("Cell params must not be null!");
        }
        this.mCellParams = cellParams;
        applyCellParams();
    }

    @Override // android.widget.TextView
    public void setCompoundDrawables(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
        super.setCompoundDrawables(drawable, drawable2, drawable3, drawable4);
        if (drawable2 == null || this.mBadgeDrawable == null) {
            return;
        }
        updateBadgeBounds(drawable2);
    }

    public void setCustomShadowLargeColor(int i) {
        this.mCustomShadowLargeColor = i;
    }

    public void setCustomShadowSmallColor(int i) {
        this.mCustomShadowSmallColor = i;
    }

    public void setDrawableAlpha(float f) {
        Drawable[] compoundDrawables = getCompoundDrawables();
        int iFloatToIntAlpha = AlphaUtil.floatToIntAlpha(f);
        for (Drawable drawable : compoundDrawables) {
            if (drawable != null) {
                drawable.setAlpha(iFloatToIntAlpha);
            }
        }
        BadgeDrawable badgeDrawable = this.mBadgeDrawable;
        if (badgeDrawable != null) {
            badgeDrawable.setAlpha(iFloatToIntAlpha);
        }
    }

    @Override // android.widget.TextView
    protected boolean setFrame(int i, int i2, int i3, int i4) {
        if (getLeft() != i || getRight() != i3 || getTop() != i2 || getBottom() != i4) {
            this.mBackgroundSizeChanged = true;
        }
        boolean frame = super.setFrame(i, i2, i3, i4);
        if (this.mBadgeDrawable != null) {
            int right = ((getRight() - getLeft()) - getCompoundPaddingRight()) - getCompoundPaddingLeft();
            Rect baseBounds = this.mBadgeDrawable.getBaseBounds();
            Rect rect = new Rect();
            rect.left = baseBounds.left - ((right - baseBounds.width()) / 2);
            rect.right = rect.left + right;
            rect.top = baseBounds.top - getPaddingTop();
            rect.bottom = rect.top + getHeight();
            this.mBadgeDrawable.setMaxBounds(rect);
        }
        return frame;
    }

    public void setIgnorePressedStateChange(boolean z) {
        this.mIgnorePressedStateChange = z;
    }

    @Override // android.view.View
    public void setPressed(boolean z) {
        super.setPressed(z);
        if (this.mIgnoreKeyPressedStateChange || this.mIgnorePressedStateChange) {
            return;
        }
        updateIconState();
    }

    void setStayPressed(boolean z) {
        this.mStayPressed = z;
        if (!z) {
            this.mPressedBackground = null;
        }
        if (getParent() instanceof ShortcutAndWidgetContainer) {
            ((CellLayout) getParent().getParent()).setPressedIcon(this, this.mPressedBackground, this.mOutlineHelper.shadowBitmapPadding);
        }
        updateIconState();
    }

    @Override // android.view.View
    public void setTag(Object obj) {
        setTag(obj, true);
    }

    public void setTag(Object obj, boolean z) {
        if (z && obj != null) {
            LauncherModel.checkItemInfo((ItemInfo) obj);
        }
        super.setTag(obj);
    }

    @Override // android.widget.TextView
    public void setTextColor(int i) {
        this.mTextColor = i;
        super.setTextColor(i);
    }

    @Override // android.widget.TextView
    public void setTextColor(ColorStateList colorStateList) {
        this.mTextColor = colorStateList.getDefaultColor();
        super.setTextColor(colorStateList);
    }

    public void setTextVisibility(boolean z) {
        Resources resources = getResources();
        if (z) {
            super.setTextColor(this.mTextColor);
        } else {
            super.setTextColor(resources.getColor(android.R.color.transparent));
        }
        this.mIsTextVisible = z;
    }

    protected void updateBadgeBounds(Drawable drawable) {
        this.mBadgeDrawable.setBaseBounds(drawable.getBounds());
    }

    @Override // android.widget.TextView, android.view.View
    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.mBackground || super.verifyDrawable(drawable);
    }
}
