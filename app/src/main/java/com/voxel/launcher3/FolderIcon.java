package com.voxel.launcher3;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Looper;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.launcher3.dragndrop.DragView;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.notification.NotificationCount;
import com.voxel.simplesearchlauncher.notification.Notifier;
import com.voxel.simplesearchlauncher.view.BadgeDrawable;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class FolderIcon extends FrameLayout implements FolderInfo.FolderListener, Notifier.OnNotificationChangeListener {
    public static Drawable sSharedFolderLeaveBehind = null;
    private static boolean sStaticValuesDirty = true;
    private PreviewItemDrawingParams mAnimParams;
    boolean mAnimating;
    private int mAvailableSpaceInPreview;
    private Map<String, Integer> mBadgeCounts;
    private BadgeDrawable mBadgeDrawable;
    private int mBadgeSum;
    private float mBaselineIconScale;
    private int mBaselineIconSize;
    private CellParams mCellParams;
    private boolean mDayChangeReceiverRegistered;
    private BroadcastReceiver mDayChangedReceiver;
    private DeviceProfile mDeviceProfile;
    private ItemInfo mDragInfo;
    private boolean mDragMode;
    private Folder mFolder;
    private BubbleTextView mFolderName;
    FolderRingAnimator mFolderRingAnimator;
    private ArrayList<ShortcutInfo> mHiddenItems;
    private boolean mIconsVisible;
    private FolderInfo mInfo;
    private int mIntrinsicIconSize;
    public Launcher mLauncher;
    private CheckLongPressHelper mLongPressHelper;
    private float mMaxPerspectiveShift;
    private Rect mOldBounds;
    OnAlarmListener mOnOpenListener;
    private Alarm mOpenAlarm;
    private PreviewItemDrawingParams mParams;
    private Boolean mPrevTextVisible;
    private ImageView mPreviewBackground;
    private int mPreviewOffsetX;
    private int mPreviewOffsetY;
    private float mSlop;
    private int mTotalWidth;

    public static class FolderRingAnimator {
        public static int sPreviewPadding = -1;
        public static int sPreviewSize = -1;
        public static int sPreviewVerticalOffset = -1;
        public static Drawable sSharedInnerRingDrawable;
        public static Drawable sSharedOuterRingDrawable;
        private ValueAnimator mAcceptAnimator;
        private CellLayout mCellLayout;
        public int mCellX;
        public int mCellY;
        public FolderIcon mFolderIcon;
        public float mInnerRingSize;
        private ValueAnimator mNeutralAnimator;
        public float mOuterRingSize;

        public FolderRingAnimator(Context context, FolderIcon folderIcon) {
            this.mFolderIcon = null;
            this.mFolderIcon = folderIcon;
            Resources resources = context.getResources();
            if (FolderIcon.sStaticValuesDirty) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    throw new RuntimeException("FolderRingAnimator loading drawables on non-UI thread " + Thread.currentThread());
                }
                sPreviewSize = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().unscaledIconSizePx;
                sPreviewPadding = resources.getDimensionPixelSize(R.dimen.folder_preview_padding);
                sPreviewVerticalOffset = resources.getDimensionPixelSize(R.dimen.folder_vertical_offset);
                sSharedOuterRingDrawable = resources.getDrawable(R.drawable.portal_ring_outer_holo);
                sSharedInnerRingDrawable = resources.getDrawable(R.drawable.portal_ring_inner_nolip_holo);
                FolderIcon.sSharedFolderLeaveBehind = resources.getDrawable(R.drawable.portal_ring_rest);
                boolean unused = FolderIcon.sStaticValuesDirty = false;
            }
        }

        public void lambda$animateToAcceptState$0(int i, ValueAnimator valueAnimator) {
            float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            float f = i;
            this.mOuterRingSize = ((0.3f * fFloatValue) + 1.0f) * f;
            this.mInnerRingSize = ((fFloatValue * 0.15f) + 1.0f) * f;
            CellLayout cellLayout = this.mCellLayout;
            if (cellLayout != null) {
                cellLayout.invalidate();
            }
        }

        public void lambda$animateToNaturalState$1(int i, ValueAnimator valueAnimator) {
            float fFloatValue = 1.0f - ((Float) valueAnimator.getAnimatedValue()).floatValue();
            float f = i;
            this.mOuterRingSize = ((0.3f * fFloatValue) + 1.0f) * f;
            this.mInnerRingSize = ((fFloatValue * 0.15f) + 1.0f) * f;
            CellLayout cellLayout = this.mCellLayout;
            if (cellLayout != null) {
                cellLayout.invalidate();
            }
        }

        public void animateToAcceptState() {
            ValueAnimator valueAnimator = this.mNeutralAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mAcceptAnimator = LauncherAnimUtils.ofFloat(this.mCellLayout, 0.0f, 1.0f);
            this.mAcceptAnimator.setDuration(100L);
            final int i = sPreviewSize;
            this.mAcceptAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$FolderIcon$FolderRingAnimator$4JpntFUusrs9h6agbJAvC5ihOw4
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    FolderRingAnimator.this.lambda$animateToAcceptState$0( i, valueAnimator2);
                }
            });
            this.mAcceptAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.FolderIcon.FolderRingAnimator.1
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationStart(Animator animator) {
                    if (FolderRingAnimator.this.mFolderIcon != null) {
                        FolderRingAnimator.this.mFolderIcon.mPreviewBackground.setVisibility(4);
                    }
                }
            });
            this.mAcceptAnimator.start();
        }

        public void animateToNaturalState() {
            ValueAnimator valueAnimator = this.mAcceptAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            this.mNeutralAnimator = LauncherAnimUtils.ofFloat(this.mCellLayout, 0.0f, 1.0f);
            this.mNeutralAnimator.setDuration(100L);
            final int i = sPreviewSize;
            this.mNeutralAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$FolderIcon$FolderRingAnimator$qaQHcGH_Hg-yj0QWAAUWFowLmRs
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator2) {
                    FolderRingAnimator.this.lambda$animateToNaturalState$1( i, valueAnimator2);
                }
            });
            this.mNeutralAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.FolderIcon.FolderRingAnimator.2
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    if (FolderRingAnimator.this.mCellLayout != null) {
                        FolderRingAnimator.this.mCellLayout.hideFolderAccept(FolderRingAnimator.this);
                    }
                    if (FolderRingAnimator.this.mFolderIcon != null) {
                        FolderRingAnimator.this.mFolderIcon.mPreviewBackground.setVisibility(0);
                    }
                }
            });
            this.mNeutralAnimator.start();
        }

        public float getInnerRingSize() {
            return this.mInnerRingSize;
        }

        public float getOuterRingSize() {
            return this.mOuterRingSize;
        }

        public void setCell(int i, int i2) {
            this.mCellX = i;
            this.mCellY = i2;
        }

        public void setCellLayout(CellLayout cellLayout) {
            this.mCellLayout = cellLayout;
        }
    }

    public static class PreviewItemDrawingParams {
        public Drawable drawable;
        public int overlayAlpha;
        public float scale;
        public float transX;
        public float transY;

        public PreviewItemDrawingParams(float f, float f2, float f3, int i) {
            this.transX = f;
            this.transY = f2;
            this.scale = f3;
            this.overlayAlpha = i;
        }
    }

    public FolderIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFolderRingAnimator = null;
        this.mTotalWidth = -1;
        this.mAnimating = false;
        this.mIconsVisible = true;
        this.mOldBounds = new Rect();
        this.mParams = new PreviewItemDrawingParams(0.0f, 0.0f, 0.0f, 0);
        this.mAnimParams = new PreviewItemDrawingParams(0.0f, 0.0f, 0.0f, 0);
        this.mHiddenItems = new ArrayList<>();
        this.mOpenAlarm = new Alarm();
        this.mDayChangedReceiver = new BroadcastReceiver() { // from class: com.voxel.launcher3.FolderIcon.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context2, Intent intent) {
                FolderIcon.this.postInvalidate();
            }
        };
        this.mOnOpenListener = new OnAlarmListener() { // from class: com.voxel.launcher3.FolderIcon.2
            @Override // com.voxel.launcher3.OnAlarmListener
            public void onAlarm(Alarm alarm) {
                ShortcutInfo shortcutInfoMakeShortcut;
                if (FolderIcon.this.mDragInfo instanceof AppInfo) {
                    shortcutInfoMakeShortcut = ((AppInfo) FolderIcon.this.mDragInfo).makeShortcut();
                    shortcutInfoMakeShortcut.spanX = 1;
                    shortcutInfoMakeShortcut.spanY = 1;
                } else {
                    shortcutInfoMakeShortcut = (ShortcutInfo) FolderIcon.this.mDragInfo;
                }
                FolderIcon.this.mFolder.beginExternalDrag(shortcutInfoMakeShortcut);
                FolderIcon.this.mLauncher.openFolder(FolderIcon.this);
            }
        };
        init();
    }

    private void animateFirstItem(Drawable drawable, int i, boolean z, Runnable runnable) {
        animateFirstItem(drawable, i, z, runnable, 0);
    }

    private void animateFirstItem(Drawable drawable, int i, final boolean z, final Runnable runnable, int i2) {
        final PreviewItemDrawingParams previewItemDrawingParamsComputePreviewItemDrawingParams = computePreviewItemDrawingParams(i2, null);
        final float intrinsicWidth = (this.mAvailableSpaceInPreview - drawable.getIntrinsicWidth()) / 2;
        final float intrinsicHeight = ((this.mAvailableSpaceInPreview - drawable.getIntrinsicHeight()) / 2) + getPaddingTop();
        this.mAnimParams.drawable = drawable;
        ValueAnimator valueAnimatorOfFloat = LauncherAnimUtils.ofFloat(this, 0.0f, 1.0f);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$FolderIcon$Bguyuyr82PuqphXVAAbOfYxSkak
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                FolderIcon.this.lambda$animateFirstItem$1( z, intrinsicWidth, previewItemDrawingParamsComputePreviewItemDrawingParams, intrinsicHeight, valueAnimator);
            }
        });
        valueAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.FolderIcon.3
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                FolderIcon.this.mAnimating = false;
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                FolderIcon.this.mAnimating = true;
            }
        });
        valueAnimatorOfFloat.setDuration(i);
        valueAnimatorOfFloat.start();
    }

    private void computePreviewDrawingParams(Drawable drawable) {
        computePreviewDrawingParams(drawable.getIntrinsicWidth(), getMeasuredWidth());
    }

    private void drawPreviewItem(Canvas canvas, PreviewItemDrawingParams previewItemDrawingParams) {
        canvas.save();
        canvas.translate(previewItemDrawingParams.transX + this.mPreviewOffsetX, previewItemDrawingParams.transY + this.mPreviewOffsetY);
        canvas.scale(previewItemDrawingParams.scale, previewItemDrawingParams.scale);
        Drawable drawable = previewItemDrawingParams.drawable;
        if (drawable != null) {
            this.mOldBounds.set(drawable.getBounds());
            int i = this.mIntrinsicIconSize;
            drawable.setBounds(0, 0, i, i);
            drawable.draw(canvas);
            drawable.setBounds(this.mOldBounds);
        }
        canvas.restore();
    }

    public static FolderIcon fromXml(int i, Context context, ViewGroup viewGroup, FolderInfo folderInfo, IconCache iconCache) {
        return fromXml(i, context, viewGroup, folderInfo, iconCache, LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile(), true);
    }

    public static FolderIcon fromXml(int i, Context context, ViewGroup viewGroup, FolderInfo folderInfo, IconCache iconCache, DeviceProfile deviceProfile, boolean z) {
        Launcher launcher = context instanceof Launcher ? (Launcher) context : null;
        FolderIcon folderIcon = (FolderIcon) LayoutInflater.from(context).inflate(i, viewGroup, false);
        CellParams cellParamsForType = deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
        folderIcon.setClipToPadding(false);
        folderIcon.mFolderName = (BubbleTextView) folderIcon.findViewById(R.id.folder_icon_name);
        folderIcon.mPreviewBackground = (ImageView) folderIcon.findViewById(R.id.preview_background);
        folderIcon.mPreviewBackground.setImageResource(SettingsStorage.getInstance().getFolderShapeResourceId());
        folderIcon.mFolderName.setText(folderInfo.title);
        folderIcon.setCellParams(cellParamsForType);
        folderIcon.setTag(folderInfo);
        folderIcon.setOnClickListener(launcher);
        folderIcon.mInfo = folderInfo;
        folderIcon.mLauncher = launcher;
        folderIcon.mDeviceProfile = deviceProfile;
        folderIcon.setContentDescription(String.format(context.getString(R.string.folder_name_format), folderInfo.title));
        Folder folderFromXml = Folder.fromXml(context);
        if (launcher != null) {
            folderFromXml.setDragController(launcher.getDragController());
        }
        folderFromXml.setFolderIcon(folderIcon);
        folderFromXml.bind(folderInfo, z, SettingsStorage.getInstance().getFolderAlphabetSortEnabled());
        folderIcon.mFolder = folderFromXml;
        folderIcon.mFolderRingAnimator = new FolderRingAnimator(context, folderIcon);
        if (launcher != null) {
            folderInfo.addListener(folderIcon);
            folderIcon.setOnFocusChangeListener(launcher.mFocusHandler);
        }
        folderIcon.initBadgeCounts();
        return folderIcon;
    }

    private float getLocalCenterForIndex(int i, int[] iArr) {
        this.mParams = computePreviewItemDrawingParams(Math.min(4, i), this.mParams);
        this.mParams.transX += this.mPreviewOffsetX;
        this.mParams.transY += this.mPreviewOffsetY;
        float f = this.mParams.transX + ((this.mParams.scale * this.mIntrinsicIconSize) / 2.0f);
        float f2 = this.mParams.transY + ((this.mParams.scale * this.mIntrinsicIconSize) / 2.0f);
        iArr[0] = Math.round(f);
        iArr[1] = Math.round(f2);
        return this.mParams.scale;
    }

    private Drawable getTopDrawable(TextView textView) {
        Drawable drawable = textView.getCompoundDrawables()[1];
        if (drawable instanceof PreloadIconDrawable) {
            drawable = ((PreloadIconDrawable) drawable).mIcon;
        }
        return drawable instanceof LayerDrawable ? ((LayerDrawable) drawable).getDrawable(0) : drawable;
    }

    private void init() {
        this.mLongPressHelper = new CheckLongPressHelper(this);
    }

    public void lambda$animateFirstItem$1(boolean z, float f, PreviewItemDrawingParams previewItemDrawingParams, float f2, ValueAnimator valueAnimator) {
        float fFloatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        if (z) {
            fFloatValue = 1.0f - fFloatValue;
            this.mPreviewBackground.setAlpha(fFloatValue);
        }
        this.mAnimParams.transX = f + ((previewItemDrawingParams.transX - f) * fFloatValue);
        this.mAnimParams.transY = f2 + ((previewItemDrawingParams.transY - f2) * fFloatValue);
        this.mAnimParams.scale = (fFloatValue * (previewItemDrawingParams.scale - 1.0f)) + 1.0f;
        this.invalidate();
    }

    public void lambda$onDrop$0(ShortcutInfo shortcutInfo) {
        this.mHiddenItems.remove(shortcutInfo);
        this.mFolder.showItem(shortcutInfo);
        this.invalidate();
    }

    public static void onDeviceProfileChanged() {
        sStaticValuesDirty = true;
    }

    private void onDrop(final ShortcutInfo shortcutInfo, DragView dragView, Rect rect, float f, int i, Runnable runnable, DropTarget.DragObject dragObject) {
        Rect rect2;
        float descendantRectRelativeToSelf;
        shortcutInfo.cellX = -1;
        shortcutInfo.cellY = -1;
        if (dragView == null) {
            addItem(shortcutInfo);
            return;
        }
        DragLayer dragLayer = this.mLauncher.getDragLayer();
        Rect rect3 = new Rect();
        dragLayer.getViewRectRelativeToSelf(dragView, rect3);
        if (rect == null) {
            rect2 = new Rect();
            Workspace workspace = this.mLauncher.getWorkspace();
            workspace.setFinalTransitionTransform((CellLayout) getParent().getParent());
            float scaleX = getScaleX();
            float scaleY = getScaleY();
            setScaleX(1.0f);
            setScaleY(1.0f);
            descendantRectRelativeToSelf = dragLayer.getDescendantRectRelativeToSelf(this, rect2);
            setScaleX(scaleX);
            setScaleY(scaleY);
            workspace.resetTransitionTransform((CellLayout) getParent().getParent());
        } else {
            rect2 = rect;
            descendantRectRelativeToSelf = f;
        }
        int[] iArr = new int[2];
        float localCenterForIndex = getLocalCenterForIndex(i, iArr);
        iArr[0] = Math.round(iArr[0] * descendantRectRelativeToSelf);
        iArr[1] = Math.round(iArr[1] * descendantRectRelativeToSelf);
        rect2.offset(iArr[0] - (dragView.getMeasuredWidth() / 2), iArr[1] - (dragView.getMeasuredHeight() / 2));
        float f2 = localCenterForIndex * descendantRectRelativeToSelf;
        dragLayer.animateView(dragView, rect3, rect2, i >= 4 ? 0.0f : 1.0f, 1.0f, 1.0f, f2, f2, 400, new DecelerateInterpolator(2.0f), new AccelerateInterpolator(2.0f), runnable, 0, null);
        addItem(shortcutInfo);
        this.mHiddenItems.add(shortcutInfo);
        this.mFolder.hideItem(shortcutInfo);
        postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$FolderIcon$nQH84jIMUB4pRCafOkNkqzJkKdk
            @Override // java.lang.Runnable
            public final void run() {
                FolderIcon.this.lambda$onDrop$0( shortcutInfo);
            }
        }, 400L);
    }

    private void registerDayChangedReceiver() {
        if (this.mDayChangedReceiver == null || this.mDayChangeReceiverRegistered) {
            return;
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(this.mDayChangedReceiver, new IntentFilter("com.evie.day_changed"));
        this.mDayChangeReceiverRegistered = true;
    }

    private boolean willAcceptItem(ItemInfo itemInfo) {
        FolderInfo folderInfo;
        int i = itemInfo.itemType;
        return ((i != 0 && i != 1 && i != 6) || this.mFolder.isFull() || itemInfo == (folderInfo = this.mInfo) || folderInfo.opened) ? false : true;
    }

    public boolean acceptDrop(Object obj) {
        return !this.mFolder.isDestroyed() && willAcceptItem((ItemInfo) obj);
    }

    public void addItem(ShortcutInfo shortcutInfo) {
        this.mInfo.add(shortcutInfo);
    }

    @Override // android.view.View
    public void cancelLongPress() {
        super.cancelLongPress();
        this.mLongPressHelper.cancelLongPress();
    }

    public void computePreviewDrawingParams(int i, int i2) {
        if (this.mIntrinsicIconSize == i && this.mTotalWidth == i2) {
            return;
        }
        getDeviceProfile();
        this.mIntrinsicIconSize = i;
        this.mTotalWidth = i2;
        int i3 = this.mPreviewBackground.getLayoutParams().height;
        int iRound = Math.round(FolderRingAnimator.sPreviewPadding * this.mCellParams.iconScale);
        this.mAvailableSpaceInPreview = i3 - (iRound * 2);
        int i4 = this.mAvailableSpaceInPreview;
        int i5 = this.mIntrinsicIconSize;
        this.mBaselineIconScale = (((int) ((i4 / 2) * 1.8f)) * 1.0f) / ((int) (i5 * 1.1800001f));
        this.mBaselineIconSize = (int) (i5 * this.mBaselineIconScale);
        this.mMaxPerspectiveShift = this.mBaselineIconSize * 0.18f;
        this.mPreviewOffsetX = (this.mTotalWidth - i4) / 2;
        this.mPreviewOffsetY = iRound + FolderRingAnimator.sPreviewVerticalOffset;
    }

    public PreviewItemDrawingParams computePreviewItemDrawingParams(int i, PreviewItemDrawingParams previewItemDrawingParams) {
        float f;
        float f2 = 0.5f;
        if (i < 4) {
            f2 = i % 2;
            f = i / 2;
        } else {
            f = 0.5f;
        }
        float f3 = this.mBaselineIconSize * 0.45f;
        float f4 = (this.mAvailableSpaceInPreview - (2.0f * f3)) / 3.0f;
        float paddingTop = (f3 * f) + f4 + (f * f4) + getPaddingTop();
        float f5 = (f3 * f2) + f4 + (f4 * f2);
        float f6 = this.mBaselineIconScale * 0.45f;
        if (previewItemDrawingParams == null) {
            return new PreviewItemDrawingParams(f5, paddingTop, f6, 0);
        }
        previewItemDrawingParams.transX = f5;
        previewItemDrawingParams.transY = paddingTop;
        previewItemDrawingParams.scale = f6;
        return previewItemDrawingParams;
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Folder folder = this.mFolder;
        if (folder == null) {
            return;
        }
        if (!this.mIconsVisible) {
            BadgeDrawable badgeDrawable = this.mBadgeDrawable;
            if (badgeDrawable != null) {
                badgeDrawable.draw(canvas);
                return;
            }
            return;
        }
        ArrayList<View> itemsInReadingOrder = folder.getItemsInReadingOrder();
        if (itemsInReadingOrder.size() != 0 || this.mAnimating) {
            if (this.mAnimating) {
                computePreviewDrawingParams(this.mAnimParams.drawable);
            } else {
                computePreviewDrawingParams(getTopDrawable((TextView) itemsInReadingOrder.get(0)));
            }
            int iMin = Math.min(itemsInReadingOrder.size(), 4);
            if (this.mAnimating) {
                drawPreviewItem(canvas, this.mAnimParams);
            } else {
                for (int i = iMin - 1; i >= 0; i--) {
                    TextView textView = (TextView) itemsInReadingOrder.get(i);
                    if (!this.mHiddenItems.contains(textView.getTag())) {
                        Drawable topDrawable = getTopDrawable(textView);
                        this.mParams = computePreviewItemDrawingParams(i, this.mParams);
                        PreviewItemDrawingParams previewItemDrawingParams = this.mParams;
                        previewItemDrawingParams.drawable = topDrawable;
                        drawPreviewItem(canvas, previewItemDrawingParams);
                    }
                }
            }
            BadgeDrawable badgeDrawable2 = this.mBadgeDrawable;
            if (badgeDrawable2 == null || this.mDragMode) {
                return;
            }
            badgeDrawable2.draw(canvas);
        }
    }

    public CellParams getCellParams() {
        return this.mCellParams;
    }

    public DeviceProfile getDeviceProfile() {
        DeviceProfile deviceProfile = this.mDeviceProfile;
        return deviceProfile == null ? LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile() : deviceProfile;
    }

    public Folder getFolder() {
        return this.mFolder;
    }

    FolderInfo getFolderInfo() {
        return this.mInfo;
    }

    public int getPreviewOffsetX() {
        return this.mPreviewOffsetX;
    }

    public int getPreviewOffsetY() {
        return this.mPreviewOffsetY;
    }

    public boolean getTextVisible() {
        return this.mFolderName.getVisibility() == 0;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized void initBadgeCounts() {
        NotificationCount notificationCountFromShortcutInfo;
        this.mBadgeCounts = new HashMap();
        this.mBadgeDrawable = new BadgeDrawable(getContext(), null);
        this.mBadgeDrawable.setCallback(this);
        if (this.mInfo != null && this.mInfo.contents != null) {
            int i = 0;
            Notifier notifier = Notifier.getInstance(getContext());
            notifier.unregisterListenerForAll(this);
            for (ShortcutInfo shortcutInfo : this.mInfo.contents) {
                if (Notifier.supportsNotifications(shortcutInfo) && (notificationCountFromShortcutInfo = NotificationCount.fromShortcutInfo(shortcutInfo)) != null) {
                    notifier.readCount(notificationCountFromShortcutInfo);
                    this.mBadgeCounts.put(notificationCountFromShortcutInfo.packageName, Integer.valueOf(notificationCountFromShortcutInfo.count));
                    i += notificationCountFromShortcutInfo.count;
                    notifier.registerListener(notificationCountFromShortcutInfo.packageName, this);
                }
            }
            this.mBadgeSum = i;
            this.mBadgeDrawable.setBadge(this.mBadgeSum);
        }
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public synchronized void onAdd(ShortcutInfo shortcutInfo) {
        NotificationCount notificationCountFromShortcutInfo;
        if (Notifier.supportsNotifications(shortcutInfo) && (notificationCountFromShortcutInfo = NotificationCount.fromShortcutInfo(shortcutInfo)) != null) {
            Notifier notifier = Notifier.getInstance(getContext());
            notifier.readCount(notificationCountFromShortcutInfo);
            notifier.registerListener(notificationCountFromShortcutInfo.packageName, this);
            onNotificationChanged(notificationCountFromShortcutInfo);
        }
        invalidate();
        requestLayout();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        this.mFolder.onFolderIconAttachedToWindow();
        registerDayChangedReceiver();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mFolder.onFolderIconDetachedFromWindow();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(this.mDayChangedReceiver);
        this.mDayChangeReceiverRegistered = false;
    }

    public void onDragEnter(Object obj) {
        if (this.mFolder.isDestroyed()) {
            return;
        }
        ItemInfo itemInfo = (ItemInfo) obj;
        if (willAcceptItem(itemInfo)) {
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) getLayoutParams();
            CellLayout cellLayout = (CellLayout) getParent().getParent();
            this.mFolderRingAnimator.setCell(layoutParams.cellX, layoutParams.cellY);
            this.mFolderRingAnimator.setCellLayout(cellLayout);
            this.mFolderRingAnimator.animateToAcceptState();
            cellLayout.showFolderAccept(this.mFolderRingAnimator);
            this.mOpenAlarm.setOnAlarmListener(this.mOnOpenListener);
            if ((obj instanceof AppInfo) || (obj instanceof ShortcutInfo)) {
                this.mOpenAlarm.setAlarm(800L);
            }
            this.mDragInfo = itemInfo;
        }
    }

    public void onDragExit() {
        this.mFolderRingAnimator.animateToNaturalState();
        this.mOpenAlarm.cancelAlarm();
    }

    public void onDragExit(Object obj) {
        onDragExit();
    }

    public void onDrop(DropTarget.DragObject dragObject) {
        int size;
        ShortcutInfo shortcutInfoMakeShortcut = dragObject.dragInfo instanceof AppInfo ? ((AppInfo) dragObject.dragInfo).makeShortcut() : (ShortcutInfo) dragObject.dragInfo;
        this.mFolder.notifyDrop();
        if (SettingsStorage.getInstance().getFolderAlphabetSortEnabled()) {
            ArrayList arrayList = new ArrayList(this.mInfo.contents);
            arrayList.add(shortcutInfoMakeShortcut);
            Collections.sort(arrayList, Folder.ALPHA_COMPARATOR);
            int iIndexOf = arrayList.indexOf(shortcutInfoMakeShortcut);
            if (iIndexOf < 0) {
                iIndexOf = this.mInfo.contents.size();
            }
            size = iIndexOf;
        } else {
            size = this.mInfo.contents.size();
        }
        onDrop(shortcutInfoMakeShortcut, dragObject.dragView, null, 1.0f, size, dragObject.postAnimationRunnable, dragObject);
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onItemsChanged() {
        invalidate();
        requestLayout();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.mPreviewBackground.getDrawable() != null) {
            Rect rect = new Rect();
            rect.left = this.mPreviewBackground.getLeft();
            rect.right = this.mPreviewBackground.getRight();
            rect.top = this.mPreviewBackground.getTop();
            rect.bottom = this.mPreviewBackground.getBottom();
            Rect rect2 = new Rect();
            rect2.right = getWidth();
            rect2.bottom = getHeight();
            this.mBadgeDrawable.setBaseBounds(rect);
            this.mBadgeDrawable.setMaxBounds(rect2);
        }
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // com.voxel.simplesearchlauncher.notification.Notifier.OnNotificationChangeListener
    public synchronized void onNotificationChanged(NotificationCount notificationCount) {
        if (this.mBadgeCounts == null) {
            return;
        }
        if (notificationCount != null) {
            this.mBadgeCounts.put(notificationCount.packageName, Integer.valueOf(notificationCount.count));
        }
        int iIntValue = 0;
        Iterator<String> it = this.mBadgeCounts.keySet().iterator();
        while (it.hasNext()) {
            iIntValue += this.mBadgeCounts.get(it.next()).intValue();
        }
        this.mBadgeSum = iIntValue;
        this.mBadgeDrawable.setBadge(this.mBadgeSum);
        postInvalidate();
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public synchronized void onRemove(ShortcutInfo shortcutInfo) {
        NotificationCount notificationCountFromShortcutInfo;
        if (Notifier.supportsNotifications(shortcutInfo) && (notificationCountFromShortcutInfo = NotificationCount.fromShortcutInfo(shortcutInfo)) != null) {
            Notifier.getInstance(getContext()).unregisterListener(notificationCountFromShortcutInfo.packageName, this);
            this.mBadgeCounts.remove(notificationCountFromShortcutInfo.packageName);
            onNotificationChanged(null);
        }
        invalidate();
        requestLayout();
    }

    @Override // android.view.View
    protected Parcelable onSaveInstanceState() {
        sStaticValuesDirty = true;
        return super.onSaveInstanceState();
    }

    @Override // com.voxel.launcher3.FolderInfo.FolderListener
    public void onTitleChanged(CharSequence charSequence) {
        this.mFolderName.setText(charSequence.toString());
        setContentDescription(String.format(getContext().getString(R.string.folder_name_format), charSequence));
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean zOnTouchEvent = super.onTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case 0:
                this.mLongPressHelper.postCheckForLongPress();
                return zOnTouchEvent;
            case 1:
            case 3:
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

    public void performCreateAnimation(ShortcutInfo shortcutInfo, View view, ShortcutInfo shortcutInfo2, DragView dragView, Rect rect, float f, Runnable runnable) {
        int i;
        int i2;
        Drawable topDrawable = getTopDrawable((TextView) view);
        computePreviewDrawingParams(topDrawable.getIntrinsicWidth(), view.getMeasuredWidth());
        if (!SettingsStorage.getInstance().getFolderAlphabetSortEnabled() || Folder.ALPHA_COMPARATOR.compare(shortcutInfo, shortcutInfo2) <= 0) {
            i = 0;
            i2 = 1;
        } else {
            i = 1;
            i2 = 0;
        }
        animateFirstItem(topDrawable, 350, false, null, i);
        addItem(shortcutInfo);
        onDrop(shortcutInfo2, dragView, rect, f, i2, runnable, null);
    }

    public void performDestroyAnimation(View view, Runnable runnable) {
        Drawable topDrawable = getTopDrawable((TextView) view);
        computePreviewDrawingParams(topDrawable.getIntrinsicWidth(), view.getMeasuredWidth());
        animateFirstItem(topDrawable, 200, true, runnable);
    }

    public void setCellParams(CellParams cellParams) {
        if (cellParams == null) {
            throw new IllegalArgumentException("Cell params must not be null!");
        }
        this.mCellParams = cellParams;
        this.mFolderName.setCellParams(cellParams);
        ((FrameLayout.LayoutParams) this.mFolderName.getLayoutParams()).topMargin = cellParams.iconSizePx + cellParams.iconDrawablePaddingPx;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mPreviewBackground.getLayoutParams();
        layoutParams.width = cellParams.iconSizePx;
        layoutParams.height = cellParams.iconSizePx;
    }

    public void setDragMode(boolean z) {
        BubbleTextView bubbleTextView;
        if (z == this.mDragMode || (bubbleTextView = this.mFolderName) == null) {
            return;
        }
        if (z) {
            this.mPrevTextVisible = Boolean.valueOf(bubbleTextView.getVisibility() == 0);
            this.mFolderName.setVisibility(4);
        } else {
            Boolean bool = this.mPrevTextVisible;
            if (bool != null && bool.booleanValue()) {
                this.mFolderName.setVisibility(0);
            }
            this.mPrevTextVisible = null;
        }
        this.mDragMode = z;
    }

    public void setIconsVisible(boolean z) {
        if (z != this.mIconsVisible) {
            this.mIconsVisible = z;
            invalidate();
        }
    }

    public void setTextVisible(boolean z) {
        if (z) {
            this.mFolderName.setVisibility(0);
        } else {
            this.mFolderName.setVisibility(4);
        }
    }
}
