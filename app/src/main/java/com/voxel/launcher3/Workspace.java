package com.voxel.launcher3;

import timber.log.Timber;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Choreographer;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import com.evie.common.dragndrop.DragObjectAbstraction;
import com.evie.common.dragndrop.DragSource;
import com.voxel.launcher3.compat.AppWidgetManagerCompat;
import com.voxel.launcher3.compat.PackageInstallerCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.voxel.launcher3.dragndrop.DragScroller;
import com.voxel.launcher3.dragndrop.DragView;
import com.voxel.launcher3.graphics.DragPreviewProvider;
import com.voxel.launcher3.util.ItemInfoMatcher;
import com.voxel.launcher3.util.WallpaperUtils;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.utils.ScrollUtil;
import com.voxel.simplesearchlauncher.utils.alpha.AlphaSet;
import com.voxel.simplesearchlauncher.utils.alpha.AlphaSetAnimator;
import com.voxel.simplesearchlauncher.widget.PendingAddShortcutInfo;
import com.voxel.simplesearchlauncher.widget.PendingAddWidgetInfo;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/* JADX INFO: loaded from: classes.dex */
public class Workspace extends PagedView implements View.OnTouchListener, ViewGroup.OnHierarchyChangeListener, DragSource, DropTarget, Insettable, LauncherTransitionable, DragController.DragListener, DragScroller {
    private static boolean ENFORCE_DRAG_EVENT_ORDER = false;
    static Rect mLandscapeCellLayoutMetrics;
    static Rect mPortraitCellLayoutMetrics;
    private static boolean sAccessibilityEnabled;
    private static final Rect sTempRect = new Rect();
    private boolean mAddToExistingFolderOnDrop;
    boolean mAnimatingViewIntoPlace;
    private ValueAnimator mBackgroundFadeInAnimation;
    private ValueAnimator mBackgroundFadeOutAnimation;
    private final Runnable mBindPages;
    private int mCameraDistance;
    private final Canvas mCanvas;
    boolean mChildrenLayersEnabled;
    private float mChildrenOutlineAlpha;
    private ObjectAnimator mChildrenOutlineFadeInAnimation;
    private ObjectAnimator mChildrenOutlineFadeOutAnimation;
    private boolean mCreateUserFolderOnDrop;
    private float mCurrentScale;
    Launcher.CustomContentCallbacks mCustomContentCallbacks;
    private String mCustomContentDescription;
    private long mCustomContentShowTime;
    boolean mCustomContentShowing;
    private int mDefaultPage;
    private boolean mDeferDropAfterUninstall;
    private boolean mDeferRemoveExtraEmptyScreen;
    private Runnable mDeferredAction;
    private Runnable mDelayedResizeRunnable;
    private Runnable mDelayedSnapToPageRunnable;
    private Point mDisplaySize;
    private DragController mDragController;
    private FolderIcon.FolderRingAnimator mDragFolderRingAnimator;
    private CellLayout.CellInfo mDragInfo;
    private int mDragMode;
    private FolderIcon mDragOverFolderIcon;
    private int mDragOverX;
    private int mDragOverY;
    private CellLayout mDragOverlappingLayout;
    private ShortcutAndWidgetContainer mDragSourceInternal;
    private CellLayout mDragTargetLayout;
    private float[] mDragViewVisualCenter;
    private CellLayout mDropToLayout;
    private final Alarm mFolderCreationAlarm;
    private IconCache mIconCache;
    private boolean mInScrollArea;
    boolean mIsDragOccuring;
    private boolean mIsSwitchingState;
    private int mLastChildCount;
    private float mLastCustomContentScrollProgress;
    private int mLastReorderX;
    private int mLastReorderY;
    private float mLastSetWallpaperOffsetSteps;
    private Launcher mLauncher;
    private LayoutTransition mLayoutTransition;
    private float[] mNewAlphas;
    private float[] mNewBackgroundAlphas;
    private float mNewScale;
    private int mNumPagesForWallpaperParallax;
    private float[] mOldAlphas;
    private float[] mOldBackgroundAlphas;
    private int mOriginalDefaultPage;
    private DragPreviewProvider mOutlineProvider;
    float mOverScrollEffect;
    private boolean mOverscrollEffectSet;
    private float mOverviewModeShrinkFactor;
    private Runnable mRemoveEmptyScreenRunnable;
    private final Alarm mReorderAlarm;
    private final ArrayList<Integer> mRestoredPages;
    private float mSavedRotationY;
    private int mSavedScrollX;
    private SparseArray<Parcelable> mSavedStates;
    private float mSavedTranslationX;
    private ArrayList<Long> mScreenOrder;
    private SpringLoadedDragController mSpringLoadedDragController;
    private float mSpringLoadedShrinkFactor;
    private State mState;
    private boolean mStripScreensOnPageStopMoving;
    private int[] mTargetCell;
    private int[] mTempCell;
    private float[] mTempCellLayoutCenterCoordinates;
    private int[] mTempEstimate;
    private Matrix mTempInverseMatrix;
    private int[] mTempPt;
    private int[] mTempVisiblePagesRange;
    private final int[] mTempXY;
    private long mTouchDownTime;
    private float mTransitionProgress;
    private boolean mUninstallSuccessful;
    private boolean mWallpaperIsLiveWallpaper;
    private final WallpaperManager mWallpaperManager;
    WallpaperOffsetInterpolator mWallpaperOffset;
    private IBinder mWindowToken;
    private boolean mWorkspaceFadeInAdjacentScreens;
    private HashMap<Long, CellLayout> mWorkspaceScreens;
    private float mXDown;
    private float mYDown;
    private final ZoomInInterpolator mZoomInInterpolator;

    static class AlphaUpdateListener implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {
        View view;

        public AlphaUpdateListener(View view) {
            this.view = view;
        }

        public static void updateVisibility(View view) {
            int i = Workspace.sAccessibilityEnabled ? 8 : 4;
            if (view.getAlpha() < 0.01f && view.getVisibility() != i) {
                view.setVisibility(i);
            } else {
                if (view.getAlpha() <= 0.01f || view.getVisibility() == 0) {
                    return;
                }
                view.setVisibility(0);
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            updateVisibility(this.view);
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
            this.view.setVisibility(0);
        }

        @Override // android.animation.ValueAnimator.AnimatorUpdateListener
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            updateVisibility(this.view);
        }
    }

    private class DeferredWidgetRefresh implements Runnable {
        private final LauncherAppWidgetHost mHost;
        private final ArrayList<LauncherAppWidgetInfo> mInfos;
        private final Handler mHandler = new Handler();
        private boolean mRefreshPending = true;

        public DeferredWidgetRefresh(ArrayList<LauncherAppWidgetInfo> arrayList, LauncherAppWidgetHost launcherAppWidgetHost) {
            this.mInfos = arrayList;
            this.mHost = launcherAppWidgetHost;
            this.mHost.addProviderChangeListener(this);
            this.mHandler.postDelayed(this, 10000L);
        }

        @Override // java.lang.Runnable
        public void run() {
            this.mHost.removeProviderChangeListener(this);
            this.mHandler.removeCallbacks(this);
            if (this.mRefreshPending) {
                this.mRefreshPending = false;
                for (LauncherAppWidgetInfo launcherAppWidgetInfo : this.mInfos) {
                    if (launcherAppWidgetInfo.hostView instanceof PendingAppWidgetHostView) {
                        PendingAppWidgetHostView pendingAppWidgetHostView = (PendingAppWidgetHostView) launcherAppWidgetInfo.hostView;
                        Workspace.this.mLauncher.removeAppWidget(launcherAppWidgetInfo);
                        ((CellLayout) pendingAppWidgetHostView.getParent().getParent()).removeView(pendingAppWidgetHostView);
                        Workspace.this.mLauncher.bindAppWidget(launcherAppWidgetInfo);
                    }
                }
            }
        }
    }

    class FolderCreationAlarmListener implements OnAlarmListener {
        int cellX;
        int cellY;
        CellLayout layout;

        public FolderCreationAlarmListener(CellLayout cellLayout, int i, int i2) {
            this.layout = cellLayout;
            this.cellX = i;
            this.cellY = i2;
        }

        @Override // com.voxel.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            if (Workspace.this.mDragFolderRingAnimator != null) {
                Workspace.this.mDragFolderRingAnimator.animateToNaturalState();
            }
            Workspace workspace = Workspace.this;
            workspace.mDragFolderRingAnimator = new FolderIcon.FolderRingAnimator(workspace.mLauncher, null);
            Workspace.this.mDragFolderRingAnimator.setCell(this.cellX, this.cellY);
            Workspace.this.mDragFolderRingAnimator.setCellLayout(this.layout);
            Workspace.this.mDragFolderRingAnimator.animateToAcceptState();
            this.layout.showFolderAccept(Workspace.this.mDragFolderRingAnimator);
            this.layout.clearDragOutlines();
            Workspace.this.setDragMode(1);
        }
    }

    static class InverseZInterpolator implements TimeInterpolator {
        private ZInterpolator zInterpolator;

        public InverseZInterpolator(float f) {
            this.zInterpolator = new ZInterpolator(f);
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return 1.0f - this.zInterpolator.getInterpolation(1.0f - f);
        }
    }

    interface ItemOperator {
        boolean evaluate(ItemInfo itemInfo, View view, View view2);
    }

    class ReorderAlarmListener implements OnAlarmListener {
        View child;
        DragView dragView;
        float[] dragViewCenter;
        int minSpanX;
        int minSpanY;
        int spanX;
        int spanY;

        public ReorderAlarmListener(float[] fArr, int i, int i2, int i3, int i4, DragView dragView, View view) {
            this.dragViewCenter = fArr;
            this.minSpanX = i;
            this.minSpanY = i2;
            this.spanX = i3;
            this.spanY = i4;
            this.child = view;
            this.dragView = dragView;
        }

        @Override // com.voxel.launcher3.OnAlarmListener
        public void onAlarm(Alarm alarm) {
            int[] iArr = new int[2];
            Workspace workspace = Workspace.this;
            workspace.mTargetCell = workspace.findNearestArea((int) workspace.mDragViewVisualCenter[0], (int) Workspace.this.mDragViewVisualCenter[1], this.minSpanX, this.minSpanY, Workspace.this.mDragTargetLayout, Workspace.this.mTargetCell);
            Workspace workspace2 = Workspace.this;
            workspace2.mLastReorderX = workspace2.mTargetCell[0];
            Workspace workspace3 = Workspace.this;
            workspace3.mLastReorderY = workspace3.mTargetCell[1];
            Workspace workspace4 = Workspace.this;
            workspace4.mTargetCell = workspace4.mDragTargetLayout.performReorder((int) Workspace.this.mDragViewVisualCenter[0], (int) Workspace.this.mDragViewVisualCenter[1], this.minSpanX, this.minSpanY, this.spanX, this.spanY, this.child, Workspace.this.mTargetCell, iArr, 1);
            if (Workspace.this.mTargetCell[0] < 0 || Workspace.this.mTargetCell[1] < 0) {
                Workspace.this.mDragTargetLayout.revertTempState();
            } else {
                Workspace.this.setDragMode(3);
            }
            Workspace.this.mDragTargetLayout.visualizeDropLocation(this.child, Workspace.this.mOutlineProvider.generatedDragOutline, (int) Workspace.this.mDragViewVisualCenter[0], (int) Workspace.this.mDragViewVisualCenter[1], Workspace.this.mTargetCell[0], Workspace.this.mTargetCell[1], iArr[0], iArr[1], (iArr[0] == this.spanX && iArr[1] == this.spanY) ? false : true, this.dragView.getDragVisualizeOffset(), this.dragView.getDragRegion());
        }
    }

    enum State {
        NORMAL,
        NORMAL_HIDDEN,
        SPRING_LOADED,
        OVERVIEW,
        OVERVIEW_HIDDEN
    }

    class WallpaperOffsetInterpolator implements Choreographer.FrameCallback {
        boolean mAnimating;
        float mAnimationStartOffset;
        long mAnimationStartTime;
        int mNumScreens;
        boolean mWaitingForUpdate;
        float mFinalOffset = 0.0f;
        float mCurrentOffset = 0.5f;
        private final int ANIMATION_DURATION = 250;
        private final int MIN_PARALLAX_PAGE_SPAN = 3;
        boolean mScrollingWallpaperType = false;
        Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.Workspace.WallpaperOffsetInterpolator.1
            @Override // java.lang.Runnable
            public void run() {
                try {
                    try {
                        Workspace.this.mWallpaperManager.setWallpaperOffsets(Workspace.this.mWindowToken, Workspace.this.mWallpaperOffset.getCurrX(), 0.5f);
                    } catch (SecurityException unused) {
                    }
                    Workspace.this.mLauncher.setWallpaperOffsets(Workspace.this.mWallpaperOffset.getCurrX(), 0.5f);
                    WallpaperOffsetInterpolator.this.setWallpaperOffsetSteps();
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                }
            }
        };
        Choreographer mChoreographer = Choreographer.getInstance();
        Interpolator mInterpolator = new DecelerateInterpolator(1.5f);

        public WallpaperOffsetInterpolator() {
        }

        private void animateToFinal() {
            this.mAnimating = true;
            this.mAnimationStartOffset = this.mCurrentOffset;
            this.mAnimationStartTime = System.currentTimeMillis();
        }

        private int getNumScreensExcludingEmptyAndCustom() {
            int childCount = Workspace.this.getChildCount() - numEmptyScreensToIgnore();
            return (Workspace.this.mCustomContentCallbacks == null || !Workspace.this.mCustomContentCallbacks.shouldIncludeInWallpaperScroll()) ? childCount : childCount - Workspace.this.numCustomPages();
        }

        private int numEmptyScreensToIgnore() {
            return (Workspace.this.getChildCount() - Workspace.this.numCustomPages() < 3 || !Workspace.this.hasExtraEmptyScreen()) ? 0 : 1;
        }

        private void scheduleUpdate() {
            if (this.mWaitingForUpdate) {
                return;
            }
            this.mChoreographer.postFrameCallback(this);
            this.mWaitingForUpdate = true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setWallpaperOffsetSteps() {
            if (Workspace.this.mNumPagesForWallpaperParallax == 0) {
                return;
            }
            float f = 1.0f / Workspace.this.mNumPagesForWallpaperParallax;
            if (f != Workspace.this.mLastSetWallpaperOffsetSteps) {
                Workspace.this.mWallpaperManager.setWallpaperOffsetSteps(f, 1.0f);
                Workspace.this.mLastSetWallpaperOffsetSteps = f;
            }
        }

        private void updateOffset(boolean z) {
            if (Workspace.this.mWorkspaceScreens == null || Workspace.this.mWorkspaceScreens.isEmpty()) {
                return;
            }
            boolean scrollingWallpaperType = SettingsStorage.getInstance().getScrollingWallpaperType();
            boolean z2 = scrollingWallpaperType != this.mScrollingWallpaperType;
            this.mScrollingWallpaperType = scrollingWallpaperType;
            if (z2 || this.mWaitingForUpdate || z) {
                this.mWaitingForUpdate = false;
                if (z2 || (computeScrollOffset() && Workspace.this.mWindowToken != null)) {
                    try {
                        if (!this.mScrollingWallpaperType) {
                            try {
                                Workspace.this.mWallpaperManager.setWallpaperOffsets(Workspace.this.mWindowToken, 0.5f, 0.5f);
                            } catch (SecurityException unused) {
                            }
                            Workspace.this.mLauncher.setWallpaperOffsets(0.5f, 0.5f);
                        } else if (z) {
                            Workspace.this.postDelayed(this.runnable, 0L);
                        } else {
                            this.runnable.run();
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e("Launcher.Workspace", "Error updating wallpaper offset: " + e);
                    }
                }
            }
        }

        private float wallpaperOffsetForCurrentScroll() {
            if (Workspace.this.getChildCount() <= 1) {
                return 0.0f;
            }
            int iNumEmptyScreensToIgnore = numEmptyScreensToIgnore();
            int iNumCustomPages = (Workspace.this.mCustomContentCallbacks == null || !Workspace.this.mCustomContentCallbacks.shouldIncludeInWallpaperScroll()) ? Workspace.this.numCustomPages() : 0;
            int childCount = (Workspace.this.getChildCount() - 1) - iNumEmptyScreensToIgnore;
            if (!Workspace.this.mIsRtl) {
                int i = iNumCustomPages;
                iNumCustomPages = childCount;
                childCount = i;
            }
            int scrollForPage = Workspace.this.getScrollForPage(childCount);
            int scrollForPage2 = Workspace.this.getScrollForPage(iNumCustomPages) - scrollForPage;
            if (scrollForPage2 == 0) {
                return 0.0f;
            }
            float fMax = Math.max(0.0f, Math.min(1.0f, ((Workspace.this.getScrollX() - scrollForPage) - Workspace.this.getLayoutTransitionOffsetForPage(0)) / scrollForPage2));
            int numScreensExcludingEmptyAndCustom = getNumScreensExcludingEmptyAndCustom();
            int iMax = Workspace.this.mWallpaperIsLiveWallpaper ? numScreensExcludingEmptyAndCustom - 1 : Math.max(3, numScreensExcludingEmptyAndCustom - 1);
            Workspace.this.mNumPagesForWallpaperParallax = iMax;
            return (fMax * (((Workspace.this.mIsRtl ? (iMax - numScreensExcludingEmptyAndCustom) + 1 : 0) + numScreensExcludingEmptyAndCustom) - 1)) / iMax;
        }

        public boolean computeScrollOffset() {
            float f = this.mCurrentOffset;
            if (this.mAnimating) {
                long jCurrentTimeMillis = System.currentTimeMillis() - this.mAnimationStartTime;
                float interpolation = this.mInterpolator.getInterpolation(jCurrentTimeMillis / 250.0f);
                float f2 = this.mAnimationStartOffset;
                this.mCurrentOffset = f2 + ((this.mFinalOffset - f2) * interpolation);
                this.mAnimating = jCurrentTimeMillis < 250;
            } else {
                this.mCurrentOffset = this.mFinalOffset;
            }
            if (Math.abs(this.mCurrentOffset - this.mFinalOffset) > 1.0E-7f) {
                scheduleUpdate();
            }
            return Math.abs(f - this.mCurrentOffset) > 1.0E-7f;
        }

        @Override // android.view.Choreographer.FrameCallback
        public void doFrame(long j) {
            updateOffset(false);
        }

        public float getCurrX() {
            return this.mCurrentOffset;
        }

        public void jumpToFinal() {
            this.mCurrentOffset = this.mFinalOffset;
        }

        public void setFinalX(float f) {
            scheduleUpdate();
            this.mFinalOffset = Math.max(0.0f, Math.min(f, 1.0f));
            int numScreensExcludingEmptyAndCustom = getNumScreensExcludingEmptyAndCustom();
            int i = this.mNumScreens;
            if (numScreensExcludingEmptyAndCustom != i) {
                if (i > 0) {
                    animateToFinal();
                }
                this.mNumScreens = getNumScreensExcludingEmptyAndCustom();
            }
        }

        public void syncWithScroll() {
            Workspace.this.mWallpaperOffset.setFinalX(wallpaperOffsetForCurrentScroll());
            updateOffset(true);
        }
    }

    static class ZInterpolator implements TimeInterpolator {
        private float focalLength;

        public ZInterpolator(float f) {
            this.focalLength = f;
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            float f2 = this.focalLength;
            return (1.0f - (f2 / (f + f2))) / (1.0f - (f2 / (f2 + 1.0f)));
        }
    }

    static class ZoomInInterpolator implements TimeInterpolator {
        private final InverseZInterpolator inverseZInterpolator = new InverseZInterpolator(0.35f);
        private final DecelerateInterpolator decelerate = new DecelerateInterpolator(3.0f);

        ZoomInInterpolator() {
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float f) {
            return this.decelerate.getInterpolation(this.inverseZInterpolator.getInterpolation(f));
        }
    }

    public Workspace(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public Workspace(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mChildrenOutlineAlpha = 0.0f;
        this.mTouchDownTime = -1L;
        this.mCustomContentShowTime = -1L;
        this.mWorkspaceScreens = new HashMap<>();
        this.mScreenOrder = new ArrayList<>();
        this.mDeferRemoveExtraEmptyScreen = false;
        this.mTargetCell = new int[2];
        this.mDragOverX = -1;
        this.mDragOverY = -1;
        this.mLastCustomContentScrollProgress = -1.0f;
        this.mCustomContentDescription = "";
        this.mDragTargetLayout = null;
        this.mDragOverlappingLayout = null;
        this.mDropToLayout = null;
        this.mTempCell = new int[2];
        this.mTempPt = new int[2];
        this.mTempEstimate = new int[2];
        this.mDragViewVisualCenter = new float[2];
        this.mTempCellLayoutCenterCoordinates = new float[2];
        this.mTempInverseMatrix = new Matrix();
        this.mState = State.NORMAL;
        this.mIsSwitchingState = false;
        this.mAnimatingViewIntoPlace = false;
        this.mIsDragOccuring = false;
        this.mChildrenLayersEnabled = true;
        this.mStripScreensOnPageStopMoving = false;
        this.mInScrollArea = false;
        this.mTempXY = new int[2];
        this.mTempVisiblePagesRange = new int[2];
        this.mLastSetWallpaperOffsetSteps = 0.0f;
        this.mDisplaySize = new Point();
        this.mFolderCreationAlarm = new Alarm();
        this.mReorderAlarm = new Alarm();
        this.mDragFolderRingAnimator = null;
        this.mDragOverFolderIcon = null;
        this.mCreateUserFolderOnDrop = false;
        this.mAddToExistingFolderOnDrop = false;
        this.mCanvas = new Canvas();
        this.mDragMode = 0;
        this.mLastReorderX = -1;
        this.mLastReorderY = -1;
        this.mRestoredPages = new ArrayList<>();
        this.mLastChildCount = -1;
        this.mOverScrollEffect = 0.0f;
        this.mBindPages = new Runnable() { // from class: com.voxel.launcher3.Workspace.1
            @Override // java.lang.Runnable
            public void run() {
                Workspace.this.mLauncher.getModel().bindRemainingSynchronousPages();
            }
        };
        this.mZoomInInterpolator = new ZoomInInterpolator();
        this.mLauncher = (Launcher) context;
        Resources resources = getResources();
        this.mWorkspaceFadeInAdjacentScreens = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().shouldFadeAdjacentWorkspaceScreens();
        this.mFadeInAdjacentScreens = false;
        this.mWallpaperManager = WallpaperManager.getInstance(context);
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.Workspace, i, 0);
        this.mSpringLoadedShrinkFactor = resources.getInteger(R.integer.config_workspaceSpringLoadShrinkPercentage) / 100.0f;
        this.mOverviewModeShrinkFactor = deviceProfile.getOverviewModeScale();
        this.mCameraDistance = resources.getInteger(R.integer.config_cameraDistance);
        /* REWORK: the dex read styleable index 2 (workspaceDefaultPage, unused in
           our layouts) with default 1 — a leftover of the feed build where the
           custom-content page sat at index 0. With the feed cut, the first page
           (defaultScreen, our launcher.xml sets it to 0) is the home page. */
        int i2 = typedArrayObtainStyledAttributes.getInt(0, 0);
        this.mDefaultPage = i2;
        this.mOriginalDefaultPage = i2;
        typedArrayObtainStyledAttributes.recycle();
        setOnHierarchyChangeListener(this);
        setHapticFeedbackEnabled(false);
        initWorkspace();
        setMotionEventSplittingEnabled(true);
        setImportantForAccessibility(1);
    }

    private void animateBackgroundGradient(float f, boolean z) {
        final AlphaSet mainLayerDarkenAlpha = this.mLauncher.getMainLayerDarkenAlpha();
        ValueAnimator valueAnimator = this.mBackgroundFadeInAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mBackgroundFadeInAnimation = null;
        }
        ValueAnimator valueAnimator2 = this.mBackgroundFadeOutAnimation;
        if (valueAnimator2 != null) {
            valueAnimator2.cancel();
            this.mBackgroundFadeOutAnimation = null;
        }
        float fFloatValue = mainLayerDarkenAlpha.getValueForKey(this, 0.0f).floatValue();
        if (f != fFloatValue) {
            if (!z) {
                mainLayerDarkenAlpha.putValue(this, f);
                return;
            }
            this.mBackgroundFadeOutAnimation = LauncherAnimUtils.ofFloat(this, fFloatValue, f);
            this.mBackgroundFadeOutAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$EIxSf5ey20b6k2mkPs8ypwpPPug
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator3) {
                    mainLayerDarkenAlpha.putValue(Workspace.this, ((Float) valueAnimator3.getAnimatedValue()).floatValue());
                }
            });
            this.mBackgroundFadeOutAnimation.setInterpolator(new DecelerateInterpolator(1.5f));
            this.mBackgroundFadeOutAnimation.setDuration(350L);
            this.mBackgroundFadeOutAnimation.start();
        }
    }

    private float calculateMaxDistanceForFolderCreation() {
        return LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getCellParamsForType(DeviceProfile.ParamType.WORKSPACE).iconSizePx * 0.55f;
    }

    private void cleanupAddToFolder() {
        FolderIcon folderIcon = this.mDragOverFolderIcon;
        if (folderIcon != null) {
            folderIcon.onDragExit(null);
            this.mDragOverFolderIcon = null;
        }
    }

    private void cleanupFolderCreation() {
        FolderIcon.FolderRingAnimator folderRingAnimator = this.mDragFolderRingAnimator;
        if (folderRingAnimator != null) {
            folderRingAnimator.animateToNaturalState();
            this.mDragFolderRingAnimator = null;
        }
        this.mFolderCreationAlarm.setOnAlarmListener(null);
        this.mFolderCreationAlarm.cancelAlarm();
    }

    private void cleanupReorder(boolean z) {
        if (z) {
            this.mReorderAlarm.cancelAlarm();
        }
        this.mLastReorderX = -1;
        this.mLastReorderY = -1;
    }

    private void convertFinalScreenToEmptyScreenIfNecessary() {
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - convertFinalScreenToEmptyScreenIfNecessary()", true);
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
            return;
        }
        if (hasExtraEmptyScreen() || this.mScreenOrder.size() == 0) {
            return;
        }
        ArrayList<Long> arrayList = this.mScreenOrder;
        long jLongValue = arrayList.get(arrayList.size() - 1).longValue();
        if (jLongValue == -301) {
            return;
        }
        CellLayout cellLayout = this.mWorkspaceScreens.get(Long.valueOf(jLongValue));
        if (cellLayout.getShortcutsAndWidgets().getChildCount() != 0 || cellLayout.isDropPending()) {
            return;
        }
        this.mWorkspaceScreens.remove(Long.valueOf(jLongValue));
        this.mScreenOrder.remove(Long.valueOf(jLongValue));
        this.mWorkspaceScreens.put(-201L, cellLayout);
        this.mScreenOrder.add(-201L);
        this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        Launcher.addDumpLog("Launcher.Workspace", "11683562 -   extra empty screen: " + jLongValue, true);
    }

    private void enableHwLayersOnVisiblePages() {
        if (this.mChildrenLayersEnabled) {
            int childCount = getChildCount();
            getVisiblePages(this.mTempVisiblePagesRange);
            int[] iArr = this.mTempVisiblePagesRange;
            int i = iArr[0];
            int i2 = iArr[1];
            if (i == i2) {
                if (i2 < childCount - 1) {
                    i2++;
                } else if (i > 0) {
                    i--;
                }
            }
            CellLayout cellLayout = this.mWorkspaceScreens.get(-301L);
            int i3 = 0;
            while (i3 < childCount) {
                CellLayout cellLayout2 = (CellLayout) getPageAt(i3);
                cellLayout2.enableHardwareLayer(cellLayout2 != cellLayout && i <= i3 && i3 <= i2 && shouldDrawChild(cellLayout2));
                i3++;
            }
        }
    }

    private void enableOverviewMode(boolean z, int i, boolean z2) {
        State state = State.OVERVIEW;
        if (!z) {
            state = State.NORMAL;
        }
        Animator changeStateAnimation = getChangeStateAnimation(state, z2, 0, i);
        if (changeStateAnimation != null) {
            onTransitionPrepare();
            changeStateAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.Workspace.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    Workspace.this.onTransitionEnd();
                }
            });
            changeStateAnimation.start();
        }
    }

    private void enforceDragParity(View view, String str, int i, int i2) {
        Object tag = view.getTag(R.id.drag_event_parity);
        int iIntValue = (tag == null ? 0 : ((Integer) tag).intValue()) + i;
        view.setTag(R.id.drag_event_parity, Integer.valueOf(iIntValue));
        if (iIntValue != i2) {
            Log.e("Launcher.Workspace", str + ": Drag contract violated: " + iIntValue);
        }
    }

    private void enforceDragParity(String str, int i, int i2) {
        enforceDragParity(this, str, i, i2);
        for (int i3 = 0; i3 < getChildCount(); i3++) {
            enforceDragParity(getChildAt(i3), str, i, i2);
        }
    }

    private void fadeAndRemoveEmptyScreen(int i, int i2, final Runnable runnable, final boolean z) {
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - fadeAndRemoveEmptyScreen()", true);
        PropertyValuesHolder propertyValuesHolderOfFloat = PropertyValuesHolder.ofFloat("alpha", 0.0f);
        PropertyValuesHolder propertyValuesHolderOfFloat2 = PropertyValuesHolder.ofFloat("backgroundAlpha", 0.0f);
        final CellLayout cellLayout = this.mWorkspaceScreens.get(-201L);
        this.mRemoveEmptyScreenRunnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$WsF3QNC74QJlrrDsgzDvwGVFBtY
            @Override // java.lang.Runnable
            public final void run() {
                Workspace.this.lambda$fadeAndRemoveEmptyScreen$1( cellLayout, z);
            }
        };
        ObjectAnimator objectAnimatorOfPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(cellLayout, propertyValuesHolderOfFloat, propertyValuesHolderOfFloat2);
        objectAnimatorOfPropertyValuesHolder.setDuration(i2);
        objectAnimatorOfPropertyValuesHolder.setStartDelay(i);
        objectAnimatorOfPropertyValuesHolder.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.Workspace.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (Workspace.this.mRemoveEmptyScreenRunnable != null) {
                    Workspace.this.mRemoveEmptyScreenRunnable.run();
                }
                Runnable runnable2 = runnable;
                if (runnable2 != null) {
                    runnable2.run();
                }
            }
        });
        objectAnimatorOfPropertyValuesHolder.start();
    }

    private CellLayout findMatchingPageForDragOver(DragView dragView, float f, float f2, boolean z) {
        int childCount = getChildCount();
        CellLayout cellLayout = null;
        float f3 = Float.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            if (this.mScreenOrder.get(i).longValue() != -301) {
                CellLayout cellLayout2 = (CellLayout) getChildAt(i);
                float[] fArr = {f, f2};
                cellLayout2.getMatrix().invert(this.mTempInverseMatrix);
                mapPointFromSelfToChild(cellLayout2, fArr, this.mTempInverseMatrix);
                if (fArr[0] >= 0.0f && fArr[0] <= cellLayout2.getWidth() && fArr[1] >= 0.0f && fArr[1] <= cellLayout2.getHeight()) {
                    return cellLayout2;
                }
                if (!z) {
                    float[] fArr2 = this.mTempCellLayoutCenterCoordinates;
                    fArr2[0] = cellLayout2.getWidth() / 2;
                    fArr2[1] = cellLayout2.getHeight() / 2;
                    mapPointFromChildToSelf(cellLayout2, fArr2);
                    fArr[0] = f;
                    fArr[1] = f2;
                    float fSquaredDistance = squaredDistance(fArr, fArr2);
                    if (fSquaredDistance < f3) {
                        cellLayout = cellLayout2;
                        f3 = fSquaredDistance;
                    }
                }
            }
        }
        return cellLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int[] findNearestArea(int i, int i2, int i3, int i4, CellLayout cellLayout, int[] iArr) {
        return cellLayout.findNearestArea(i, i2, i3, i4, iArr);
    }

    static Rect getCellLayoutMetrics(Launcher launcher, int i) {
        DeviceProfile deviceProfile = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        CellParams cellParamsForType = deviceProfile.getCellParamsForType(DeviceProfile.ParamType.WORKSPACE);
        Display defaultDisplay = launcher.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        Point point2 = new Point();
        defaultDisplay.getCurrentSizeRange(point, point2);
        int i2 = (int) cellParamsForType.numColumns;
        int i3 = (int) cellParamsForType.numRows;
        if (i == 0) {
            if (mLandscapeCellLayoutMetrics == null) {
                Rect workspacePadding = deviceProfile.getWorkspacePadding(0);
                int i4 = (point2.x - workspacePadding.left) - workspacePadding.right;
                int i5 = (point.y - workspacePadding.top) - workspacePadding.bottom;
                mLandscapeCellLayoutMetrics = new Rect();
                mLandscapeCellLayoutMetrics.set(deviceProfile.calculateCellWidth(i4, i2), deviceProfile.calculateCellHeight(i5, i3), 0, 0);
            }
            return mLandscapeCellLayoutMetrics;
        }
        if (i != 1) {
            return null;
        }
        if (mPortraitCellLayoutMetrics == null) {
            Rect workspacePadding2 = deviceProfile.getWorkspacePadding(1);
            int i6 = (point.x - workspacePadding2.left) - workspacePadding2.right;
            int i7 = (point2.y - workspacePadding2.top) - workspacePadding2.bottom;
            mPortraitCellLayoutMetrics = new Rect();
            mPortraitCellLayoutMetrics.set(deviceProfile.calculateCellWidth(i6, i2), deviceProfile.calculateCellHeight(i7, i3), 0, 0);
        }
        return mPortraitCellLayoutMetrics;
    }

    private float[] getDragViewVisualCenter(int i, int i2, int i3, int i4, DragView dragView, float[] fArr) {
        if (fArr == null) {
            fArr = new float[2];
        }
        int dimensionPixelSize = i + getResources().getDimensionPixelSize(R.dimen.dragViewOffsetX);
        int dimensionPixelSize2 = (i2 + getResources().getDimensionPixelSize(R.dimen.dragViewOffsetY)) - i4;
        fArr[0] = (dimensionPixelSize - i3) + (dragView.getDragRegion().width() / 2);
        fArr[1] = dimensionPixelSize2 + (dragView.getDragRegion().height() / 2);
        return fArr;
    }

    private void getFinalPositionForDropAnimation(int[] iArr, float[] fArr, DragView dragView, CellLayout cellLayout, ItemInfo itemInfo, int[] iArr2, boolean z, boolean z2) {
        float fHeight;
        Rect rectEstimateItemPosition = estimateItemPosition(cellLayout, iArr2[0], iArr2[1], itemInfo.spanX, itemInfo.spanY);
        iArr[0] = rectEstimateItemPosition.left;
        iArr[1] = rectEstimateItemPosition.top;
        setFinalTransitionTransform(cellLayout);
        float descendantCoordRelativeToSelf = this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(cellLayout, iArr, true);
        resetTransitionTransform(cellLayout);
        float f = 1.0f;
        if (z2) {
            float fWidth = (rectEstimateItemPosition.width() * 1.0f) / dragView.getMeasuredWidth();
            fHeight = (rectEstimateItemPosition.height() * 1.0f) / dragView.getMeasuredHeight();
            f = fWidth;
        } else {
            fHeight = 1.0f;
        }
        iArr[0] = (int) (iArr[0] - ((dragView.getMeasuredWidth() - (rectEstimateItemPosition.width() * descendantCoordRelativeToSelf)) / 2.0f));
        iArr[1] = (int) (iArr[1] - ((dragView.getMeasuredHeight() - (rectEstimateItemPosition.height() * descendantCoordRelativeToSelf)) / 2.0f));
        fArr[0] = f * descendantCoordRelativeToSelf;
        fArr[1] = fHeight * descendantCoordRelativeToSelf;
    }

    private View getFirstMatch(final ItemOperator itemOperator) {
        final View[] viewArr = new View[1];
        mapOverItems(false, new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$6C7sOUd2Ywkq90CYy75kVjjWb2M
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Workspace.lambda$getFirstMatch$15(itemOperator, viewArr, itemInfo, view, view2);
            }
        });
        return viewArr[0];
    }

    private void getOverviewModePages(int[] iArr) {
        int iNumCustomPages = numCustomPages();
        int childCount = getChildCount() - 1;
        iArr[0] = Math.max(0, Math.min(iNumCustomPages, getChildCount() - 1));
        iArr[1] = Math.max(0, childCount);
    }

    public static Drawable getTextViewIcon(TextView textView) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                return drawable;
            }
        }
        return null;
    }

    private void initAnimationArrays() {
        int childCount = getChildCount();
        if (this.mLastChildCount == childCount) {
            return;
        }
        this.mOldBackgroundAlphas = new float[childCount];
        this.mOldAlphas = new float[childCount];
        this.mNewBackgroundAlphas = new float[childCount];
        this.mNewAlphas = new float[childCount];
    }

    private boolean isDragWidget(DropTarget.DragObject dragObject) {
        return (dragObject.dragInfo instanceof LauncherAppWidgetInfo) || (dragObject.dragInfo instanceof PendingAddWidgetInfo);
    }

    private boolean isExternalDragWidget(DropTarget.DragObject dragObject) {
        return dragObject.dragSource != this && isDragWidget(dragObject);
    }

    static /* synthetic */ void lambda$animateWidgetDrop$9(View view, Runnable runnable) {
        if (view != null) {
            view.setVisibility(0);
        }
        if (runnable != null) {
            runnable.run();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean lambda$clearDropTargets$16(ItemInfo itemInfo, View view, View view2) {
        if (!(view instanceof DropTarget)) {
            return false;
        }
        this.mDragController.removeDropTarget((DropTarget) view);
        return false;
    }

    public void lambda$fadeAndRemoveEmptyScreen$1(CellLayout cellLayout, boolean z) {
        if (this.hasExtraEmptyScreen()) {
            this.mWorkspaceScreens.remove(-201L);
            this.mScreenOrder.remove((Object) (-201L));
            this.removeView(cellLayout);
            if (z) {
                this.stripEmptyScreens();
            }
        }
    }

    static /* synthetic */ boolean lambda$getFirstMatch$15(ItemOperator itemOperator, View[] viewArr, ItemInfo itemInfo, View view, View view2) {
        if (!itemOperator.evaluate(itemInfo, view, view2)) {
            return false;
        }
        viewArr[0] = view;
        return true;
    }

    static /* synthetic */ boolean lambda$getWidgetForAppWidgetId$14(int i, ItemInfo itemInfo, View view, View view2) {
        return (itemInfo instanceof LauncherAppWidgetInfo) && ((LauncherAppWidgetInfo) itemInfo).appWidgetId == i;
    }

    public void lambda$onDrop$5(Runnable runnable) {
        if (this.isPageMoving()) {
            this.mDelayedResizeRunnable = runnable;
        } else {
            runnable.run();
        }
    }

    public void lambda$onDrop$6(Runnable runnable) {
        this.mAnimatingViewIntoPlace = false;
        this.updateChildrenLayersEnabled(false);
        if (runnable != null) {
            runnable.run();
        }
    }

    public void lambda$onDropCompleted$10(View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        this.onDropCompleted(view, dragObject, z, z2);
        this.mDeferredAction = null;
    }

    static /* synthetic */ void lambda$onDropCompleted$11(View view, CellLayout cellLayout) {
        view.setVisibility(0);
        cellLayout.onDropChild(view);
    }

    public void lambda$onDropExternal$8(PendingAddItemInfo pendingAddItemInfo, ItemInfo itemInfo, long j, long j2) {
        this.deferRemoveExtraEmptyScreen();
        int i = pendingAddItemInfo.itemType;
        if (i == 1) {
            this.mLauncher.processShortcutFromDrop(pendingAddItemInfo.componentName, j, j2, this.mTargetCell, null);
        } else {
            if (i == 4) {
                this.mLauncher.addAppWidgetFromDrop((PendingAddWidgetInfo) pendingAddItemInfo, j, j2, this.mTargetCell, new int[]{itemInfo.spanX, itemInfo.spanY}, null);
                return;
            }
            throw new IllegalStateException("Unknown item type: " + pendingAddItemInfo.itemType);
        }
    }

    static /* synthetic */ boolean lambda$removeItemsByComponentName$19(HashSet hashSet, UserHandleCompat userHandleCompat, HashMap<FolderInfo, ArrayList<ShortcutInfo>> map, ArrayList arrayList, HashMap map2, ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
        ArrayList arrayList2;
        if (!(itemInfo instanceof FolderInfo)) {
            if (!hashSet.contains(componentName) || !itemInfo2.user.equals(userHandleCompat)) {
                return false;
            }
            arrayList.add(map2.get(itemInfo2));
            return true;
        }
        if (!hashSet.contains(componentName) || !itemInfo2.user.equals(userHandleCompat)) {
            return false;
        }
        FolderInfo folderInfo = (FolderInfo) itemInfo;
        if (map.containsKey(folderInfo)) {
            arrayList2 = (ArrayList) map.get(folderInfo);
        } else {
            arrayList2 = new ArrayList();
            map.put(folderInfo, arrayList2);
        }
        arrayList2.add((ShortcutInfo) itemInfo2);
        return true;
    }

    static /* synthetic */ boolean lambda$removeItemsByMatcher$17(ItemInfoMatcher itemInfoMatcher, HashMap<FolderInfo, ArrayList<ShortcutInfo>> map, ArrayList arrayList, HashMap map2, ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
        ArrayList arrayList2;
        if (!(itemInfo instanceof FolderInfo)) {
            if (!itemInfoMatcher.matches(itemInfo2, componentName)) {
                return false;
            }
            arrayList.add(map2.get(itemInfo2));
            return true;
        }
        if (!itemInfoMatcher.matches(itemInfo2, componentName)) {
            return false;
        }
        FolderInfo folderInfo = (FolderInfo) itemInfo;
        if (map.containsKey(folderInfo)) {
            arrayList2 = (ArrayList) map.get(folderInfo);
        } else {
            arrayList2 = new ArrayList();
            map.put(folderInfo, arrayList2);
        }
        arrayList2.add((ShortcutInfo) itemInfo2);
        return true;
    }

    static /* synthetic */ boolean lambda$removeItemsByPackageName$18(HashSet hashSet, UserHandleCompat userHandleCompat, HashSet hashSet2, ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
        if (componentName == null || !hashSet.contains(componentName.getPackageName()) || !itemInfo2.user.equals(userHandleCompat)) {
            return false;
        }
        hashSet2.add(componentName);
        return true;
    }

    public boolean lambda$restorePendingWidgets$24(Set set, ArrayList arrayList, ItemInfo itemInfo, View view, View view2) {
        if (!(itemInfo instanceof LauncherAppWidgetInfo)) {
            return false;
        }
        LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
        if (!launcherAppWidgetInfo.hasRestoreFlag(2) || !set.contains(launcherAppWidgetInfo.providerName.getPackageName())) {
            return false;
        }
        arrayList.add(launcherAppWidgetInfo);
        launcherAppWidgetInfo.restoreStatus &= -3;
        LauncherModel.updateItemInDatabase(this.getContext(), launcherAppWidgetInfo);
        return false;
    }

    public boolean lambda$updatePackageBadge$22(UserHandleCompat userHandleCompat, String str, ItemInfo itemInfo, View view, View view2) {
        if ((itemInfo instanceof ShortcutInfo) && (view instanceof BubbleTextView)) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
            ComponentName targetComponent = shortcutInfo.getTargetComponent();
            if (userHandleCompat.equals(shortcutInfo.user) && targetComponent != null && shortcutInfo.isPromise() && str.equals(targetComponent.getPackageName())) {
                if (shortcutInfo.hasStatusFlag(2)) {
                    this.mIconCache.getTitleAndIcon(shortcutInfo, shortcutInfo.promisedIntent, userHandleCompat, true);
                } else {
                    shortcutInfo.updateIcon(this.mIconCache);
                }
                ((BubbleTextView) view).applyFromShortcutInfo(shortcutInfo, this.mIconCache, true, false);
                if (view2 != null) {
                    view2.invalidate();
                }
            }
        }
        return false;
    }

    static /* synthetic */ boolean lambda$updatePackageState$23(PackageInstallerCompat.PackageInstallInfo packageInstallInfo, ItemInfo itemInfo, View view, View view2) {
        if ((itemInfo instanceof ShortcutInfo) && (view instanceof BubbleTextView)) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
            ComponentName targetComponent = shortcutInfo.getTargetComponent();
            if (shortcutInfo.isPromise() && targetComponent != null && packageInstallInfo.packageName.equals(targetComponent.getPackageName())) {
                shortcutInfo.setInstallProgress(packageInstallInfo.progress);
                if (packageInstallInfo.state == 2) {
                    shortcutInfo.status &= -5;
                }
                ((BubbleTextView) view).applyState(false);
            }
        } else if ((view instanceof PendingAppWidgetHostView) && (itemInfo instanceof LauncherAppWidgetInfo)) {
            LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) itemInfo;
            if (launcherAppWidgetInfo.providerName.getPackageName().equals(packageInstallInfo.packageName)) {
                launcherAppWidgetInfo.installProgress = packageInstallInfo.progress;
                ((PendingAppWidgetHostView) view).applyState();
            }
        }
        return false;
    }

    public boolean lambda$updateShortcuts$20(HashSet hashSet, ItemInfo itemInfo, View view, View view2) {
        if ((itemInfo instanceof ShortcutInfo) && (view instanceof BubbleTextView) && hashSet.contains(itemInfo)) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
            BubbleTextView bubbleTextView = (BubbleTextView) view;
            Drawable textViewIcon = getTextViewIcon(bubbleTextView);
            bubbleTextView.applyFromShortcutInfo(shortcutInfo, this.mIconCache, true, shortcutInfo.isPromise() != ((textViewIcon instanceof PreloadIconDrawable) && ((PreloadIconDrawable) textViewIcon).hasNotCompleted()));
            if (view2 != null) {
                view2.invalidate();
            }
        }
        return false;
    }

    public boolean lambda$updateShortcutsAndWidgetsPerUser$21(HashMap map, UserHandleCompat userHandleCompat, HashSet hashSet, HashSet hashSet2, ItemInfo itemInfo, View view, View view2) {
        boolean z;
        boolean z2;
        if ((itemInfo instanceof ShortcutInfo) && (view instanceof BubbleTextView)) {
            ShortcutInfo shortcutInfo = (ShortcutInfo) itemInfo;
            ComponentName targetComponent = shortcutInfo.getTargetComponent();
            AppInfo appInfo = (AppInfo) map.get(targetComponent);
            if (userHandleCompat.equals(shortcutInfo.user) && targetComponent != null && LauncherModel.isShortcutInfoUpdateable(itemInfo) && hashSet.contains(targetComponent.getPackageName())) {
                if (shortcutInfo.isPromise()) {
                    if (shortcutInfo.hasStatusFlag(2)) {
                        PackageManager packageManager = this.getContext().getPackageManager();
                        if (packageManager.resolveActivity(new Intent("android.intent.action.MAIN").setComponent(targetComponent).addCategory("android.intent.category.LAUNCHER"), 65536) == null) {
                            Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(targetComponent.getPackageName());
                            if (launchIntentForPackage != null) {
                                appInfo = (AppInfo) map.get(launchIntentForPackage.getComponent());
                            }
                            if (launchIntentForPackage == null || map == null) {
                                hashSet2.add(shortcutInfo.getTargetComponent());
                                return false;
                            }
                            shortcutInfo.promisedIntent = launchIntentForPackage;
                        }
                    }
                    shortcutInfo.intent = shortcutInfo.promisedIntent;
                    shortcutInfo.promisedIntent = null;
                    shortcutInfo.status &= -8;
                    shortcutInfo.updateIcon(this.mIconCache);
                    LauncherModel.updateItemInDatabase(this.getContext(), shortcutInfo);
                    z = true;
                    z2 = true;
                } else {
                    z = false;
                    z2 = false;
                }
                if (appInfo != null) {
                    shortcutInfo.updateIcon(this.mIconCache);
                    shortcutInfo.title = appInfo.title.toString();
                    shortcutInfo.contentDescription = appInfo.contentDescription;
                    z = true;
                }
                if (z) {
                    ((BubbleTextView) view).applyFromShortcutInfo(shortcutInfo, this.mIconCache, true, z2);
                    if (view2 != null) {
                        view2.invalidate();
                    }
                }
            }
        }
        return false;
    }

    private void manageFolderFeedback(ItemInfo itemInfo, CellLayout cellLayout, int[] iArr, float f, View view) {
        boolean zWillCreateUserFolder = willCreateUserFolder(itemInfo, cellLayout, iArr, f, false);
        if (this.mDragMode == 0 && zWillCreateUserFolder && !this.mFolderCreationAlarm.alarmPending()) {
            this.mFolderCreationAlarm.setOnAlarmListener(new FolderCreationAlarmListener(cellLayout, iArr[0], iArr[1]));
            this.mFolderCreationAlarm.setAlarm(0L);
            return;
        }
        boolean zWillAddToExistingUserFolder = willAddToExistingUserFolder(itemInfo, cellLayout, iArr, f);
        if (zWillAddToExistingUserFolder && this.mDragMode == 0) {
            this.mDragOverFolderIcon = (FolderIcon) view;
            this.mDragOverFolderIcon.onDragEnter(itemInfo);
            if (cellLayout != null) {
                cellLayout.clearDragOutlines();
            }
            setDragMode(2);
            return;
        }
        if (this.mDragMode == 2 && !zWillAddToExistingUserFolder) {
            setDragMode(0);
        }
        if (this.mDragMode != 1 || zWillCreateUserFolder) {
            return;
        }
        setDragMode(0);
    }

    private void moveToScreen(int i, boolean z) {
        if (!workspaceInModalState()) {
            if (z) {
                snapToPage(i);
            } else {
                setCurrentPage(i);
            }
        }
        View childAt = getChildAt(i);
        if (childAt != null) {
            childAt.requestFocus();
        }
    }

    /* JADX WARN: Code duplicated, block: B:26:0x0098  */
    /* JADX WARN: Code duplicated, block: B:66:0x0189  */
    private void onDropExternal(int[] iArr, Object obj, CellLayout cellLayout, boolean z, DropTarget.DragObject dragObject) {
        int i;
        int i2;
        ItemInfo itemInfo;
        View viewCreateShortcut;
        char c;
        boolean z2;
        AppWidgetHostView appWidgetHostView;
        ItemInfo itemInfo2;
        boolean z3;
        int i3;
        int i4;
        char c2;
        /* REWORK: jadx left this def blank on the no-resize paths. */
        boolean z4 = false;
        Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$zV5yGOHv9A4eOQvWqaaQp6efGsA
            @Override // java.lang.Runnable
            public final void run() {
                Workspace.this.mLauncher.exitSpringLoadedDragModeDelayed(true, 300, null);
            }
        };
        ItemInfo itemInfo3 = (ItemInfo) obj;
        int i5 = itemInfo3.spanX;
        int i6 = itemInfo3.spanY;
        CellLayout.CellInfo cellInfo = this.mDragInfo;
        if (cellInfo != null) {
            i = cellInfo.spanX;
            i2 = this.mDragInfo.spanY;
        } else {
            i = i5;
            i2 = i6;
        }
        final long j = this.mLauncher.isHotseatLayout(cellLayout) ? -101L : -100L;
        final long idForScreen = getIdForScreen(cellLayout);
        if (!this.mLauncher.isHotseatLayout(cellLayout) && idForScreen != getScreenIdForPageIndex(this.mCurrentPage) && this.mState != State.SPRING_LOADED) {
            snapToScreenId(idForScreen, null);
        }
        if (itemInfo3 instanceof PendingAddItemInfo) {
            final PendingAddItemInfo pendingAddItemInfo = (PendingAddItemInfo) obj;
            if (pendingAddItemInfo.itemType == 1) {
                this.mTargetCell = findNearestArea(iArr[0], iArr[1], i, i2, cellLayout, this.mTargetCell);
                float[] fArr = this.mDragViewVisualCenter;
                float distanceFromCell = cellLayout.getDistanceFromCell(fArr[0], fArr[1], this.mTargetCell);
                if (willCreateUserFolder(dragObject.dragInfo, cellLayout, this.mTargetCell, distanceFromCell, true) || willAddToExistingUserFolder(dragObject.dragInfo, cellLayout, this.mTargetCell, distanceFromCell)) {
                    z2 = false;
                } else {
                    z2 = true;
                }
            } else {
                z2 = true;
            }
            ItemInfo itemInfo4 = dragObject.dragInfo;
            if (z2) {
                int i7 = itemInfo4.spanX;
                int i8 = itemInfo4.spanY;
                if (itemInfo4.minSpanX <= 0 || itemInfo4.minSpanY <= 0) {
                    i3 = i7;
                    i4 = i8;
                } else {
                    i3 = itemInfo4.minSpanX;
                    i4 = itemInfo4.minSpanY;
                }
                int[] iArr2 = new int[2];
                float[] fArr2 = this.mDragViewVisualCenter;
                appWidgetHostView = null;
                this.mTargetCell = cellLayout.performReorder((int) fArr2[0], (int) fArr2[1], i3, i4, itemInfo3.spanX, itemInfo3.spanY, null, this.mTargetCell, iArr2, 3);
                itemInfo2 = itemInfo4;
                if (iArr2[0] == itemInfo2.spanX) {
                    c2 = 1;
                    if (iArr2[1] == itemInfo2.spanY) {
                        z4 = false;
                    }
                    itemInfo2.spanX = iArr2[0];
                    itemInfo2.spanY = iArr2[c2];
                    z3 = z4;
                } else {
                    c2 = 1;
                }
                z4 = true;
                itemInfo2.spanX = iArr2[0];
                itemInfo2.spanY = iArr2[c2];
                z3 = z4;
            } else {
                appWidgetHostView = null;
                itemInfo2 = itemInfo4;
                z3 = false;
            }
            final ItemInfo itemInfo5 = itemInfo2;
            Runnable runnable2 = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$OIzUxxktudgahsK7soTJygJzj2M
                @Override // java.lang.Runnable
                public final void run() {
                    Workspace.this.lambda$onDropExternal$8( pendingAddItemInfo, itemInfo5, j, idForScreen);
                }
            };
            AppWidgetHostView appWidgetHostView2 = pendingAddItemInfo.itemType == 4 ? ((PendingAddWidgetInfo) pendingAddItemInfo).boundWidget : appWidgetHostView;
            if ((appWidgetHostView2 instanceof AppWidgetHostView) && z3) {
                AppWidgetResizeFrame.updateWidgetSizeRanges(appWidgetHostView2, this.mLauncher, itemInfo2.spanX, itemInfo2.spanY);
            }
            animateWidgetDrop(itemInfo3, cellLayout, dragObject.dragView, runnable2, (pendingAddItemInfo.itemType != 4 || ((PendingAddWidgetInfo) pendingAddItemInfo).info.configure == null) ? 0 : 1, appWidgetHostView2, true);
            return;
        }
        int i9 = itemInfo3.itemType;
        if (i9 != 6) {
            switch (i9) {
                case 0:
                case 1:
                    if (itemInfo3.container == -1 && (itemInfo3 instanceof AppInfo)) {
                        ShortcutInfo shortcutInfo = new ShortcutInfo((AppInfo) itemInfo3);
                        dragObject.dragInfo = shortcutInfo;
                        itemInfo3 = shortcutInfo;
                    }
                    itemInfo = itemInfo3;
                    viewCreateShortcut = this.mLauncher.createShortcut(R.layout.application, cellLayout, (ShortcutInfo) itemInfo3);
                    break;
                case 2:
                    itemInfo = itemInfo3;
                    viewCreateShortcut = FolderIcon.fromXml(R.layout.folder_icon, this.mLauncher, cellLayout, (FolderInfo) itemInfo3, this.mIconCache);
                    break;
                default:
                    throw new IllegalStateException("Unknown item type: " + itemInfo3.itemType);
            }
        } else {
            /* REWORK: donor guards the ShortcutInfo→(AppInfo) wrap with
               instance-of too (smali cond_d:pswitch_1 shared block: container==-1
               AND instanceof AppInfo). jadx dropped the instanceof here, so
               dropping a pinned shortcut (plain ShortcutInfo) to the desktop
               crashed with ClassCastException (issue #1 repro'd live). */
            if (itemInfo3.container == -1 && (itemInfo3 instanceof AppInfo)) {
                ShortcutInfo shortcutInfo2 = new ShortcutInfo((AppInfo) itemInfo3);
                dragObject.dragInfo = shortcutInfo2;
                itemInfo3 = shortcutInfo2;
            }
            itemInfo = itemInfo3;
            viewCreateShortcut = this.mLauncher.createShortcut(R.layout.application, cellLayout, (ShortcutInfo) itemInfo3);
        }
        if (iArr != null) {
            this.mTargetCell = findNearestArea(iArr[0], iArr[1], i, i2, cellLayout, this.mTargetCell);
            float[] fArr3 = this.mDragViewVisualCenter;
            float distanceFromCell2 = cellLayout.getDistanceFromCell(fArr3[0], fArr3[1], this.mTargetCell);
            dragObject.postAnimationRunnable = runnable;
            if (createUserFolderIfNecessary(viewCreateShortcut, j, cellLayout, this.mTargetCell, distanceFromCell2, true, dragObject.dragView, dragObject.postAnimationRunnable)) {
                return;
            }
            if (addToExistingFolderIfNecessary(viewCreateShortcut, cellLayout, this.mTargetCell, distanceFromCell2, dragObject, true)) {
                return;
            }
        }
        if (iArr != null) {
            float[] fArr4 = this.mDragViewVisualCenter;
            c = 1;
            this.mTargetCell = cellLayout.performReorder((int) fArr4[0], (int) fArr4[1], 1, 1, 1, 1, null, this.mTargetCell, null, 3);
        } else {
            c = 1;
            cellLayout.findCellForSpan(this.mTargetCell, 1, 1);
        }
        Launcher launcher = this.mLauncher;
        int[] iArr3 = this.mTargetCell;
        LauncherModel.addOrMoveItemInDatabase(launcher, itemInfo, j, idForScreen, iArr3[0], iArr3[c]);
        int[] iArr4 = this.mTargetCell;
        int i10 = iArr4[0];
        int i11 = iArr4[c];
        ItemInfo itemInfo6 = itemInfo;
        addInScreen(viewCreateShortcut, j, idForScreen, i10, i11, itemInfo6.spanX, itemInfo6.spanY, z);
        cellLayout.onDropChild(viewCreateShortcut);
        cellLayout.getShortcutsAndWidgets().measureChild(viewCreateShortcut);
        if (dragObject.dragView != null) {
            setFinalTransitionTransform(cellLayout);
            this.mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, viewCreateShortcut, runnable, this);
            resetTransitionTransform(cellLayout);
        }
    }

    private void onResetScrollArea() {
        setCurrentDragOverlappingLayout(null);
        this.mInScrollArea = false;
    }

    private void restorePendingWidgets(final Set<String> set) {
        final ArrayList arrayList = new ArrayList();
        mapOverItems(false, new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$06tbQcXBFlk_69aZO1d4ei7aVn0
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Workspace.this.lambda$restorePendingWidgets$24( set, arrayList, itemInfo, view, view2);
            }
        });
        if (arrayList.isEmpty()) {
            return;
        }
        DeferredWidgetRefresh deferredWidgetRefresh = new DeferredWidgetRefresh(arrayList, this.mLauncher.getAppWidgetHost());
        if (AppWidgetManagerCompat.getInstance(getContext()).findProvider(((LauncherAppWidgetInfo) arrayList.get(0)).providerName, ((LauncherAppWidgetInfo) arrayList.get(0)).user) != null) {
            deferredWidgetRefresh.run();
        }
    }

    private void setChildrenBackgroundAlphaMultipliers(float f) {
        for (int i = 0; i < getChildCount(); i++) {
            ((CellLayout) getChildAt(i)).setBackgroundAlphaMultiplier(f);
        }
    }

    private void setState(State state) {
        this.mState = state;
        updateInteractionForState();
        updateAccessibilityFlags();
    }

    private void setupLayoutTransition() {
        this.mLayoutTransition = new LayoutTransition();
        this.mLayoutTransition.enableTransitionType(3);
        this.mLayoutTransition.enableTransitionType(1);
        this.mLayoutTransition.disableTransitionType(2);
        this.mLayoutTransition.disableTransitionType(0);
        setLayoutTransition(this.mLayoutTransition);
    }

    private static float squaredDistance(float[] fArr, float[] fArr2) {
        float f = fArr[0] - fArr2[0];
        float f2 = fArr2[1] - fArr2[1];
        return (f * f) + (f2 * f2);
    }

    private void updateAccessibilityFlags() {
        setImportantForAccessibility(this.mState == State.NORMAL ? 1 : 4);
    }

    private void updateChildrenLayersEnabled(boolean z) {
        boolean z2 = true;
        boolean z3 = this.mState == State.OVERVIEW || this.mIsSwitchingState;
        if (!z && !z3 && !this.mAnimatingViewIntoPlace && !isPageMoving()) {
            z2 = false;
        }
        if (z2 != this.mChildrenLayersEnabled) {
            this.mChildrenLayersEnabled = z2;
            if (this.mChildrenLayersEnabled) {
                enableHwLayersOnVisiblePages();
                return;
            }
            for (int i = 0; i < getPageCount(); i++) {
                ((CellLayout) getChildAt(i)).enableHardwareLayer(false);
            }
        }
    }

    private void updatePageAlphaValues(int i) {
        boolean z = this.mOverScrollX < 0 || this.mOverScrollX > this.mMaxScrollX;
        if (!this.mWorkspaceFadeInAdjacentScreens || workspaceInModalState() || this.mIsSwitchingState || z) {
            return;
        }
        for (int iNumCustomPages = numCustomPages(); iNumCustomPages < getChildCount(); iNumCustomPages++) {
            CellLayout cellLayout = (CellLayout) getChildAt(iNumCustomPages);
            if (cellLayout != null) {
                cellLayout.getShortcutsAndWidgets().setAlpha(1.0f - Math.abs(getScrollProgress(i, cellLayout, iNumCustomPages)));
            }
        }
    }

    private void updateShortcutsAndWidgetsPerUser(ArrayList<AppInfo> arrayList, final UserHandleCompat userHandleCompat) {
        final HashMap map = new HashMap();
        final HashSet hashSet = new HashSet();
        for (AppInfo appInfo : arrayList) {
            map.put(appInfo.componentName, appInfo);
            hashSet.add(appInfo.componentName.getPackageName());
        }
        final HashSet<ComponentName> hashSet2 = new HashSet<>();
        mapOverItems(true, new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$lyZePwJSkPJHSAd0DYgJB288yNE
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Workspace.this.lambda$updateShortcutsAndWidgetsPerUser$21( map, userHandleCompat, hashSet, hashSet2, itemInfo, view, view2);
            }
        });
        if (!hashSet2.isEmpty()) {
            removeItemsByComponentName(hashSet2, userHandleCompat);
        }
        if (userHandleCompat.equals(UserHandleCompat.myUserHandle())) {
            restorePendingWidgets(hashSet);
        }
    }

    private void updateStateForCustomContent(int i) {
        float fMax;
        float fMin;
        if (hasCustomContent()) {
            int iIndexOf = this.mScreenOrder.indexOf(-301L);
            int scrollX = (getScrollX() - getScrollForPage(iIndexOf)) - getLayoutTransitionOffsetForPage(iIndexOf);
            float scrollForPage = getScrollForPage(iIndexOf + 1) - getScrollForPage(iIndexOf);
            float f = scrollForPage - scrollX;
            float f2 = f / scrollForPage;
            fMin = this.mIsRtl ? Math.min(0.0f, f) : Math.max(0.0f, f);
            fMax = Math.max(0.0f, f2);
        } else {
            fMax = 0.0f;
            fMin = 0.0f;
        }
        if (Float.compare(fMax, this.mLastCustomContentScrollProgress) == 0) {
            return;
        }
        CellLayout cellLayout = this.mWorkspaceScreens.get(-301L);
        if (fMax > 0.0f && cellLayout.getVisibility() != 0 && !workspaceInModalState()) {
            cellLayout.setVisibility(0);
        }
        this.mLastCustomContentScrollProgress = fMax;
        Launcher.CustomContentCallbacks customContentCallbacks = this.mCustomContentCallbacks;
        if (customContentCallbacks != null) {
            customContentCallbacks.onCustomContentScrollProgressChanged(fMax, fMin);
        }
    }

    @Override // com.voxel.launcher3.DropTarget
    public boolean acceptDrop(DropTarget.DragObject dragObject) {
        CellLayout cellLayout;
        int i;
        int i2;
        int i3;
        int i4;
        if (dragObject.isDesktopLocked) {
            return false;
        }
        CellLayout cellLayout2 = this.mDropToLayout;
        if (dragObject.dragSource == this) {
            cellLayout = cellLayout2;
        } else {
            if (cellLayout2 == null || !transitionStateShouldAllowDrop()) {
                return false;
            }
            this.mDragViewVisualCenter = getDragViewVisualCenter(dragObject.x, dragObject.y, dragObject.xOffset, dragObject.yOffset, dragObject.dragView, this.mDragViewVisualCenter);
            if (this.mLauncher.isHotseatLayout(cellLayout2)) {
                mapPointFromSelfToHotseatLayout(this.mLauncher.getHotseat(), this.mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(cellLayout2, this.mDragViewVisualCenter, null);
            }
            CellLayout.CellInfo cellInfo = this.mDragInfo;
            if (cellInfo != null) {
                int i5 = cellInfo.spanX;
                i = cellInfo.spanY;
                i2 = i5;
            } else {
                ItemInfo itemInfo = dragObject.dragInfo;
                int i6 = itemInfo.spanX;
                i = itemInfo.spanY;
                i2 = i6;
            }
            if (dragObject.dragInfo instanceof PendingAddWidgetInfo) {
                i3 = ((PendingAddWidgetInfo) dragObject.dragInfo).minSpanX;
                i4 = ((PendingAddWidgetInfo) dragObject.dragInfo).minSpanY;
            } else {
                i3 = i2;
                i4 = i;
            }
            float[] fArr = this.mDragViewVisualCenter;
            this.mTargetCell = findNearestArea((int) fArr[0], (int) fArr[1], i3, i4, cellLayout2, this.mTargetCell);
            float[] fArr2 = this.mDragViewVisualCenter;
            float distanceFromCell = cellLayout2.getDistanceFromCell(fArr2[0], fArr2[1], this.mTargetCell);
            if (this.mCreateUserFolderOnDrop && willCreateUserFolder(dragObject.dragInfo, cellLayout2, this.mTargetCell, distanceFromCell, true)) {
                return true;
            }
            if (this.mAddToExistingFolderOnDrop && willAddToExistingUserFolder(dragObject.dragInfo, cellLayout2, this.mTargetCell, distanceFromCell)) {
                return true;
            }
            float[] fArr3 = this.mDragViewVisualCenter;
            cellLayout = cellLayout2;
            this.mTargetCell = cellLayout2.performReorder((int) fArr3[0], (int) fArr3[1], i3, i4, i2, i, null, this.mTargetCell, new int[2], 4);
            int[] iArr = this.mTargetCell;
            if (!(iArr[0] >= 0 && iArr[1] >= 0)) {
                boolean zIsHotseatLayout = this.mLauncher.isHotseatLayout(cellLayout);
                if (this.mTargetCell != null && zIsHotseatLayout) {
                    Hotseat hotseat = this.mLauncher.getHotseat();
                    int[] iArr2 = this.mTargetCell;
                    if (hotseat.isAllAppsButtonRank(hotseat.getOrderInHotseat(iArr2[0], iArr2[1]))) {
                        return false;
                    }
                }
                this.mLauncher.showOutOfSpaceMessage(zIsHotseatLayout);
                return false;
            }
        }
        if (getIdForScreen(cellLayout) != -201 || commitExtraEmptyScreen() != -1) {
            return true;
        }
        this.mLauncher.showWorkspaceIsLoadingMessage();
        return false;
    }

    public boolean addExtraEmptyScreen() {
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - addExtraEmptyScreen()", true);
        if (this.mWorkspaceScreens.containsKey(-201L)) {
            return false;
        }
        insertNewWorkspaceScreen(-201L);
        return true;
    }

    public void addExtraEmptyScreenOnDrag() {
        boolean z;
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - addExtraEmptyScreenOnDrag()", true);
        this.mRemoveEmptyScreenRunnable = null;
        ShortcutAndWidgetContainer shortcutAndWidgetContainer = this.mDragSourceInternal;
        boolean z2 = false;
        if (shortcutAndWidgetContainer != null) {
            z = shortcutAndWidgetContainer.getChildCount() == 1;
            if (indexOfChild((CellLayout) this.mDragSourceInternal.getParent()) == getChildCount() - 1) {
                z2 = true;
            }
        } else {
            z = false;
        }
        if ((z && z2) || this.mWorkspaceScreens.containsKey(-201L)) {
            return;
        }
        insertNewWorkspaceScreen(-201L);
    }

    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        if (this.mLauncher.isAllAppsVisible()) {
            return;
        }
        Folder openFolder = this.mLauncher.getOpenFolder();
        if (openFolder != null) {
            openFolder.addFocusables(arrayList, i);
        } else {
            super.addFocusables(arrayList, i, i2);
        }
    }

    void addInScreen(View view, long j, long j2, int i, int i2, int i3, int i4) {
        addInScreen(view, j, j2, i, i2, i3, i4, false, false);
    }

    void addInScreen(View view, long j, long j2, int i, int i2, int i3, int i4, boolean z) {
        addInScreen(view, j, j2, i, i2, i3, i4, z, false);
    }

    /* JADX WARN: Multi-variable type inference failed */
    void addInScreen(View view, long j, long j2, int i, int i2, int i3, int i4, boolean z, boolean z2) {
        int i5;
        int cellYFromOrder;
        CellLayout layout;
        CellLayout.LayoutParams layoutParams;
        if (j == -100 && getScreenWithId(j2) == null) {
            Log.e("Launcher.Workspace", "Skipping child, screenId " + j2 + " not found");
            new Throwable().printStackTrace();
            return;
        }
        if (j2 == -201) {
            throw new RuntimeException("Screen id should not be EXTRA_EMPTY_SCREEN_ID");
        }
        if (j == -101) {
            layout = this.mLauncher.getHotseat().getLayout();
            view.setOnKeyListener(new HotseatIconKeyEventListener());
            if (z2) {
                int i6 = (int) j2;
                int cellXFromOrder = this.mLauncher.getHotseat().getCellXFromOrder(i6);
                cellYFromOrder = this.mLauncher.getHotseat().getCellYFromOrder(i6);
                i5 = cellXFromOrder;
            } else {
                i5 = i;
                cellYFromOrder = i2;
                this.mLauncher.getHotseat().getOrderInHotseat(i5, cellYFromOrder);
            }
        } else {
            i5 = i;
            cellYFromOrder = i2;
            CellLayout screenWithId = getScreenWithId(j2);
            view.setOnKeyListener(new IconKeyEventListener());
            layout = screenWithId;
        }
        ViewGroup.LayoutParams layoutParams2 = view.getLayoutParams();
        if (layoutParams2 == null || !(layoutParams2 instanceof CellLayout.LayoutParams)) {
            layoutParams = new CellLayout.LayoutParams(i5, cellYFromOrder, i3, i4);
        } else {
            layoutParams = (CellLayout.LayoutParams) layoutParams2;
            layoutParams.cellX = i5;
            layoutParams.cellY = cellYFromOrder;
            layoutParams.cellHSpan = i3;
            layoutParams.cellVSpan = i4;
        }
        if (i3 < 0 && i4 < 0) {
            layoutParams.isLockedToGrid = false;
        }
        boolean z3 = view instanceof Folder;
        if (!layout.addViewToCellLayout(view, z ? 0 : -1, this.mLauncher.getViewIdForItem((ItemInfo) view.getTag()), layoutParams, !z3)) {
            Launcher.addDumpLog("Launcher.Workspace", "Failed to add to item at (" + layoutParams.cellX + "," + layoutParams.cellY + ") to CellLayout", true);
        }
        if (!z3) {
            view.setHapticFeedbackEnabled(false);
            view.setOnLongClickListener(this.mLongClickListener);
        }
        if (view instanceof DropTarget) {
            this.mDragController.addDropTarget((DropTarget) view);
        }
    }

    void addInScreenFromBind(View view, long j, long j2, int i, int i2, int i3, int i4) {
        addInScreen(view, j, j2, i, i2, i3, i4, false, true);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void addToCustomContentPage(View view, Launcher.CustomContentCallbacks customContentCallbacks, String str) {
        if (getPageIndexForScreenId(-301L) < 0) {
            throw new RuntimeException("Expected custom content screen to exist");
        }
        CellLayout screenWithId = getScreenWithId(-301L);
        CellLayout.LayoutParams layoutParams = new CellLayout.LayoutParams(0, 0, screenWithId.getCountX(), screenWithId.getCountY());
        layoutParams.canReorder = false;
        layoutParams.isFullscreen = true;
        if (view instanceof Insettable) {
            ((Insettable) view).setInsets(this.mInsets);
        }
        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        screenWithId.removeAllViews();
        screenWithId.addViewToCellLayout(view, 0, 0, layoutParams, true);
        this.mCustomContentDescription = str;
        this.mCustomContentCallbacks = customContentCallbacks;
    }

    boolean addToExistingFolderIfNecessary(View view, CellLayout cellLayout, int[] iArr, float f, DropTarget.DragObject dragObject, boolean z) {
        if (f > calculateMaxDistanceForFolderCreation()) {
            return false;
        }
        View childAt = cellLayout.getChildAt(iArr[0], iArr[1]);
        if (!this.mAddToExistingFolderOnDrop) {
            return false;
        }
        this.mAddToExistingFolderOnDrop = false;
        if (childAt instanceof FolderIcon) {
            FolderIcon folderIcon = (FolderIcon) childAt;
            if (folderIcon.acceptDrop(dragObject.dragInfo)) {
                ItemInfo itemInfo = dragObject.dragInfo;
                folderIcon.onDrop(dragObject);
                if (!z) {
                    getParentCellLayoutForView(this.mDragInfo.cell).removeView(this.mDragInfo.cell);
                }
                return true;
            }
        }
        return false;
    }

    /* JADX WARN: Code duplicated, block: B:22:0x009f  */
    /* JADX WARN: Code duplicated, block: B:23:0x00b7 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:25:0x00ba  */
    public void animateWidgetDrop(ItemInfo itemInfo, CellLayout cellLayout, DragView dragView, final Runnable runnable, int i, final View view, boolean z) {
        /* REWORK: jadx left the endStyle register blank on one path; every write in
         * this method assigns 1, so default it. */
        int i2 = 1;
        DragLayer dragLayer;
        Rect rect = new Rect();
        this.mLauncher.getDragLayer().getViewRectRelativeToSelf(dragView, rect);
        int[] iArr = new int[2];
        float[] fArr = new float[2];
        getFinalPositionForDropAnimation(iArr, fArr, dragView, cellLayout, itemInfo, this.mTargetCell, z, !(itemInfo instanceof PendingAddShortcutInfo));
        int integer = this.mLauncher.getResources().getInteger(R.integer.config_dropAnimMaxDuration) - 200;
        if ((view instanceof AppWidgetHostView) && z) {
            Log.d("Launcher.Workspace", "6557954 Animate widget drop, final view is appWidgetHostView");
            this.mLauncher.getDragLayer().removeView(view);
        }
        if ((i != 2 && !z) || view == null) {
            if (itemInfo.itemType == 4 && z) {
                i2 = 1;
                float fMin = Math.min(fArr[0], fArr[1]);
                fArr[1] = fMin;
                fArr[0] = fMin;
            }
            dragLayer = this.mLauncher.getDragLayer();
            if (i == 4) {
                this.mLauncher.getDragLayer().animateViewIntoPosition(dragView, iArr, 0.0f, 0.1f, 0.1f, 0, runnable, integer);
            } else {
                dragLayer.animateViewIntoPosition(dragView, rect.left, rect.top, iArr[0], iArr[i2], 1.0f, 1.0f, 1.0f, fArr[0], fArr[i2], new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$TNLy62qjAindgnK4cka2D6v_ikk
                    @Override // java.lang.Runnable
                    public final void run() {
                        Workspace.lambda$animateWidgetDrop$9(view, runnable);
                    }
                }, i != i2 ? 0 : 2, integer, this);
            }
        }
        /* REWORK: the dex only builds the cross-fade bitmap when the bound host
           view exists (widget-picker drags carry no host view yet); jadx dropped
           the guard and the null-view NPE crashed every widget drop. */
        if (view != null) {
            dragView.setCrossFadeBitmap(createWidgetBitmap(itemInfo, view));
            dragView.crossFade((int) (integer * 0.8f));
        }
        i2 = 1;
        dragLayer = this.mLauncher.getDragLayer();
        if (i == 4) {
            this.mLauncher.getDragLayer().animateViewIntoPosition(dragView, iArr, 0.0f, 0.1f, 0.1f, 0, runnable, integer);
        } else {
            if (i != i2) {
            }
            dragLayer.animateViewIntoPosition(dragView, rect.left, rect.top, iArr[0], iArr[i2], 1.0f, 1.0f, 1.0f, fArr[0], fArr[i2], new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$TNLy62qjAindgnK4cka2D6v_ikk
                @Override // java.lang.Runnable
                public final void run() {
                    Workspace.lambda$animateWidgetDrop$9(view, runnable);
                }
            }, i != i2 ? 0 : 2, integer, this);
        }
    }

    @Override // android.view.View
    public void announceForAccessibility(CharSequence charSequence) {
        if (this.mLauncher.isAllAppsVisible()) {
            return;
        }
        super.announceForAccessibility(charSequence);
    }

    public DragView beginDragShared(View view, DragSource dragSource, ItemInfo itemInfo, DragPreviewProvider dragPreviewProvider, DragOptions dragOptions) {
        Point point;
        Rect rect;
        int i;
        view.clearFocus();
        view.setPressed(false);
        this.mOutlineProvider = dragPreviewProvider;
        Bitmap bitmapCreateDragBitmap = dragPreviewProvider.createDragBitmap(this.mCanvas);
        int i2 = dragPreviewProvider.previewPadding / 2;
        float scaleAndPosition = dragPreviewProvider.getScaleAndPosition(bitmapCreateDragBitmap, this.mTempXY);
        int[] iArr = this.mTempXY;
        int i3 = iArr[0];
        int i4 = iArr[1];
        LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile();
        boolean z = view instanceof BubbleTextView;
        if (z) {
            int iconSizePx = ((BubbleTextView) view).getCellParams().getIconSizePx();
            int paddingTop = view.getPaddingTop();
            int width = (bitmapCreateDragBitmap.getWidth() - iconSizePx) / 2;
            point = new Point(-i2, i2);
            Rect rect2 = new Rect(width, paddingTop, width + iconSizePx, iconSizePx + paddingTop);
            i = i4 + paddingTop;
            rect = rect2;
        } else if (view instanceof FolderIcon) {
            int iconSizePx2 = ((FolderIcon) view).getCellParams().getIconSizePx();
            Point point2 = new Point(-i2, i2 - view.getPaddingTop());
            Rect rect3 = new Rect(0, view.getPaddingTop(), view.getWidth(), iconSizePx2);
            i = i4;
            rect = rect3;
            point = point2;
        } else {
            point = null;
            rect = null;
            i = i4;
        }
        if (z) {
            ((BubbleTextView) view).clearPressedBackground();
        }
        if (view.getParent() instanceof ShortcutAndWidgetContainer) {
            this.mDragSourceInternal = (ShortcutAndWidgetContainer) view.getParent();
        }
        DragView dragViewStartDrag = this.mDragController.startDrag(bitmapCreateDragBitmap, i3, i, dragSource, itemInfo, point, rect, scaleAndPosition, dragOptions);
        dragViewStartDrag.setIntrinsicIconScaleFactor(dragSource.getIntrinsicIconScaleFactor());
        bitmapCreateDragBitmap.recycle();
        return dragViewStartDrag;
    }

    public DragView beginDragShared(View view, DragSource dragSource, DragOptions dragOptions) {
        Object tag = view.getTag();
        if (tag instanceof ItemInfo) {
            return beginDragShared(view, dragSource, (ItemInfo) tag, new DragPreviewProvider(view), dragOptions);
        }
        throw new IllegalStateException("Drag started with a view that has no tag set. This will cause a crash (issue 11627249) down the line. View: " + view + "  tag: " + view.getTag());
    }

    public void buildPageHardwareLayers() {
        updateChildrenLayersEnabled(true);
        if (getWindowToken() != null) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((CellLayout) getChildAt(i)).buildHardwareLayer();
            }
        }
        updateChildrenLayersEnabled(false);
    }

    void clearChildrenCache() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            CellLayout cellLayout = (CellLayout) getChildAt(i);
            cellLayout.setChildrenDrawnWithCacheEnabled(false);
            if (!isHardwareAccelerated()) {
                cellLayout.setChildrenDrawingCacheEnabled(false);
            }
        }
    }

    void clearDropTargets() {
        mapOverItems(false, new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$l08ph6ovjeC7hR8sE3lU61d11pg
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Workspace.this.lambda$clearDropTargets$16( itemInfo, view, view2);
            }
        });
    }

    public long commitExtraEmptyScreen() {
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - commitExtraEmptyScreen()", true);
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
            return -1L;
        }
        int pageIndexForScreenId = getPageIndexForScreenId(-201L);
        CellLayout cellLayout = this.mWorkspaceScreens.get(-201L);
        this.mWorkspaceScreens.remove(-201L);
        this.mScreenOrder.remove((Object) (-201L));
        long jGenerateNewScreenId = LauncherAppState.getLauncherProvider().generateNewScreenId();
        this.mWorkspaceScreens.put(Long.valueOf(jGenerateNewScreenId), cellLayout);
        this.mScreenOrder.add(Long.valueOf(jGenerateNewScreenId));
        if (getPageIndicator() != null) {
            getPageIndicator().updateMarker(pageIndexForScreenId, getPageIndicatorMarker(pageIndexForScreenId));
        }
        this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        return jGenerateNewScreenId;
    }

    @Override // com.voxel.launcher3.PagedView, android.view.View
    public void computeScroll() {
        super.computeScroll();
        this.mWallpaperOffset.syncWithScroll();
    }

    public void createCustomContentContainer() {
        CellLayout cellLayout = (CellLayout) this.mLauncher.getLayoutInflater().inflate(R.layout.workspace_screen, (ViewGroup) null);
        cellLayout.disableBackground();
        cellLayout.disableDragTarget();
        this.mWorkspaceScreens.put(-301L, cellLayout);
        this.mScreenOrder.add(0, -301L);
        cellLayout.setPadding(0, 0, 0, 0);
        addFullScreenPage(cellLayout);
        this.mDefaultPage = this.mOriginalDefaultPage + 1;
        if (this.mRestorePage != -1001) {
            this.mRestorePage++;
        } else {
            setCurrentPage(getCurrentPage() + 1);
        }
    }

    boolean createUserFolderIfNecessary(View view, long j, CellLayout cellLayout, int[] iArr, float f, boolean z, DragView dragView, Runnable runnable) {
        boolean z2;
        if (f > calculateMaxDistanceForFolderCreation()) {
            return false;
        }
        View childAt = cellLayout.getChildAt(iArr[0], iArr[1]);
        CellLayout.CellInfo cellInfo = this.mDragInfo;
        if (cellInfo != null) {
            z2 = this.mDragInfo.cellX == iArr[0] && this.mDragInfo.cellY == iArr[1] && getParentCellLayoutForView(cellInfo.cell) == cellLayout;
        } else {
            z2 = false;
        }
        if (childAt == null || z2 || !this.mCreateUserFolderOnDrop) {
            return false;
        }
        this.mCreateUserFolderOnDrop = false;
        long idForScreen = iArr == null ? this.mDragInfo.screenId : getIdForScreen(cellLayout);
        boolean z3 = childAt.getTag() instanceof ShortcutInfo;
        boolean z4 = view.getTag() instanceof ShortcutInfo;
        if (!z3 || !z4) {
            return false;
        }
        ShortcutInfo shortcutInfo = (ShortcutInfo) view.getTag();
        ShortcutInfo shortcutInfo2 = (ShortcutInfo) childAt.getTag();
        if (!z) {
            getParentCellLayoutForView(this.mDragInfo.cell).removeView(this.mDragInfo.cell);
        }
        Rect rect = new Rect();
        float descendantRectRelativeToSelf = this.mLauncher.getDragLayer().getDescendantRectRelativeToSelf(childAt, rect);
        cellLayout.removeView(childAt);
        FolderIcon folderIconAddFolder = this.mLauncher.addFolder(cellLayout, j, idForScreen, iArr[0], iArr[1]);
        shortcutInfo2.cellX = -1;
        shortcutInfo2.cellY = -1;
        shortcutInfo.cellX = -1;
        shortcutInfo.cellY = -1;
        if (dragView != null) {
            folderIconAddFolder.performCreateAnimation(shortcutInfo2, childAt, shortcutInfo, dragView, rect, descendantRectRelativeToSelf, runnable);
        } else {
            folderIconAddFolder.addItem(shortcutInfo2);
            folderIconAddFolder.addItem(shortcutInfo);
        }
        return true;
    }

    public Bitmap createWidgetBitmap(ItemInfo itemInfo, View view) {
        int[] iArrEstimateItemSize = this.mLauncher.getWorkspace().estimateItemSize(itemInfo.spanX, itemInfo.spanY, false);
        int visibility = view.getVisibility();
        view.setVisibility(0);
        int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(iArrEstimateItemSize[0], 1073741824);
        int iMakeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(iArrEstimateItemSize[1], 1073741824);
        if (iArrEstimateItemSize[0] <= 0 || iArrEstimateItemSize[1] <= 0) {
            Timber.e(new IllegalArgumentException("unscaled x = " + iArrEstimateItemSize[0] + " unscaled y = " + iArrEstimateItemSize[1] + " item = " + itemInfo));
            if (iArrEstimateItemSize[0] <= 0) {
                iArrEstimateItemSize[0] = 1;
            }
            if (iArrEstimateItemSize[1] <= 0) {
                iArrEstimateItemSize[1] = 1;
            }
        }
        Bitmap bitmapCreateBitmap = Bitmap.createBitmap(iArrEstimateItemSize[0], iArrEstimateItemSize[1], Bitmap.Config.ARGB_8888);
        this.mCanvas.setBitmap(bitmapCreateBitmap);
        view.measure(iMakeMeasureSpec, iMakeMeasureSpec2);
        view.layout(0, 0, iArrEstimateItemSize[0], iArrEstimateItemSize[1]);
        view.draw(this.mCanvas);
        this.mCanvas.setBitmap(null);
        view.setVisibility(visibility);
        return bitmapCreateBitmap;
    }

    public void deferRemoveExtraEmptyScreen() {
        this.mDeferRemoveExtraEmptyScreen = true;
    }

    @Override // com.voxel.launcher3.PagedView
    protected void determineScrollingStart(MotionEvent motionEvent) {
        Launcher.CustomContentCallbacks customContentCallbacks;
        if (isFinishedSwitchingState()) {
            float x = motionEvent.getX() - this.mXDown;
            float fAbs = Math.abs(x);
            float fAbs2 = Math.abs(motionEvent.getY() - this.mYDown);
            if (Float.compare(fAbs, 0.0f) == 0) {
                return;
            }
            float fAtan = (float) Math.atan(fAbs2 / fAbs);
            if (fAbs > this.mTouchSlop || fAbs2 > this.mTouchSlop) {
                cancelLongPress();
                cancelCurrentPageLongPress();
            }
            boolean z = this.mTouchDownTime - this.mCustomContentShowTime > 200;
            boolean z2 = !this.mIsRtl ? x <= 0.0f : x >= 0.0f;
            boolean z3 = getScreenIdForPageIndex(getCurrentPage()) == -301;
            if (z2 && z3 && z) {
                return;
            }
            if (!z3 || (customContentCallbacks = this.mCustomContentCallbacks) == null || customContentCallbacks.isScrollingAllowed()) {
                if (z3) {
                    CellLayout cellLayout = (CellLayout) getPageAt(getCurrentPage());
                    if (ScrollUtil.viewCanScroll(cellLayout, false, x <= 0.0f ? -1 : 1, Math.round(motionEvent.getX() - cellLayout.getLeft()), Math.round(motionEvent.getY() - cellLayout.getTop()))) {
                        return;
                    }
                }
                if (fAtan > 1.0471976f) {
                    return;
                }
                if (fAtan > 0.5235988f) {
                    super.determineScrollingStart(motionEvent, (((float) Math.sqrt((fAtan - 0.5235988f) / 0.5235988f)) * 4.0f) + 1.0f);
                } else {
                    super.determineScrollingStart(motionEvent);
                }
            }
        }
    }

    void disableLayoutTransitions() {
        setLayoutTransition(null);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        this.mSavedStates = sparseArray;
    }

    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup, android.view.View
    public boolean dispatchUnhandledMove(View view, int i) {
        if (workspaceInModalState() || !isFinishedSwitchingState()) {
            return false;
        }
        return super.dispatchUnhandledMove(view, i);
    }

    void enableChildrenCache(int i, int i2) {
        if (i > i2) {
            i2 = i;
            i = i2;
        }
        int childCount = getChildCount();
        int iMin = Math.min(i2, childCount - 1);
        for (int iMax = Math.max(i, 0); iMax <= iMin; iMax++) {
            CellLayout cellLayout = (CellLayout) getChildAt(iMax);
            cellLayout.setChildrenDrawnWithCacheEnabled(true);
            cellLayout.setChildrenDrawingCacheEnabled(true);
        }
    }

    void enableLayoutTransitions() {
        setLayoutTransition(this.mLayoutTransition);
    }

    public boolean enterOverviewMode() {
        if (this.mTouchState != 0) {
            return false;
        }
        enableOverviewMode(true, -1, true);
        return true;
    }

    public Rect estimateItemPosition(CellLayout cellLayout, int i, int i2, int i3, int i4) {
        Rect rect = new Rect();
        cellLayout.cellToRect(i, i2, i3, i4, rect);
        return rect;
    }

    public int[] estimateItemSize(int i, int i2, boolean z) {
        int[] iArr = new int[2];
        if (getChildCount() <= 0) {
            iArr[0] = Integer.MAX_VALUE;
            iArr[1] = Integer.MAX_VALUE;
            return iArr;
        }
        Rect rectEstimateItemPosition = estimateItemPosition((CellLayout) getChildAt(numCustomPages()), 0, 0, i, i2);
        iArr[0] = rectEstimateItemPosition.width();
        iArr[1] = rectEstimateItemPosition.height();
        if (z) {
            float f = iArr[0];
            float f2 = this.mSpringLoadedShrinkFactor;
            iArr[0] = (int) (f * f2);
            iArr[1] = (int) (iArr[1] * f2);
        }
        return iArr;
    }

    public void exitOverviewMode(int i, boolean z) {
        enableOverviewMode(false, i, z);
    }

    public void exitOverviewMode(boolean z) {
        exitOverviewMode(-1, z);
    }

    public boolean exitWidgetResizeMode() {
        return this.mLauncher.getDragLayer().clearAllResizeFrames();
    }

    ArrayList<ShortcutAndWidgetContainer> getAllShortcutAndWidgetContainers() {
        ArrayList<ShortcutAndWidgetContainer> arrayList = new ArrayList<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            arrayList.add(((CellLayout) getChildAt(i)).getShortcutsAndWidgets());
        }
        if (this.mLauncher.getHotseat() != null) {
            arrayList.add(this.mLauncher.getHotseat().getLayout().getShortcutsAndWidgets());
        }
        return arrayList;
    }

    Animator getChangeStateAnimation(State state, boolean z, int i, int i2) {
        return getChangeStateAnimation(state, z, i, i2, null);
    }

    Animator getChangeStateAnimation(State state, boolean z, int i, int i2, ArrayList<View> arrayList) {
        float f;
        float f2;
        if (this.mState == state) {
            return null;
        }
        initAnimationArrays();
        AnimatorSet animatorSetCreateAnimatorSet = z ? LauncherAnimUtils.createAnimatorSet() : null;
        State state2 = this.mState;
        boolean z2 = state2 == State.NORMAL;
        State state3 = State.SPRING_LOADED;
        State state4 = State.NORMAL_HIDDEN;
        State state5 = State.OVERVIEW_HIDDEN;
        boolean z3 = state2 == State.OVERVIEW;
        setState(state);
        boolean z4 = state == State.NORMAL;
        boolean z5 = state == State.SPRING_LOADED;
        boolean z6 = state == State.NORMAL_HIDDEN;
        boolean z7 = state == State.OVERVIEW_HIDDEN;
        boolean z8 = state == State.OVERVIEW;
        float f3 = (z5 || z8) ? 1.0f : 0.0f;
        float f4 = (z4 || z5) ? 1.0f : 0.0f;
        float f5 = z8 ? 1.0f : 0.0f;
        float f6 = !z4 ? 0.0f : 1.0f;
        float overviewModeTranslationY = (z8 || z7) ? getOverviewModeTranslationY() : 0.0f;
        boolean z9 = z2 && z6;
        boolean z10 = z3 && z7;
        boolean z11 = z6 && z4;
        boolean z12 = z2 && z8;
        boolean z13 = z3 && z4;
        this.mNewScale = 1.0f;
        if (z3) {
            disableFreeScroll();
        } else if (z8) {
            enableFreeScroll();
        }
        if (state != State.NORMAL) {
            if (z5) {
                this.mNewScale = this.mSpringLoadedShrinkFactor;
            } else if (z8 || z7) {
                this.mNewScale = this.mOverviewModeShrinkFactor;
            }
        }
        int integer = (z9 || z10) ? getResources().getInteger(R.integer.config_workspaceUnshrinkTime) : (z12 || z13) ? getResources().getInteger(R.integer.config_overviewTransitionTime) : getResources().getInteger(R.integer.config_appsCustomizeWorkspaceShrinkTime);
        int pageNearestToCenterOfScreen = i2;
        if (pageNearestToCenterOfScreen == -1) {
            pageNearestToCenterOfScreen = getPageNearestToCenterOfScreen();
        }
        if (pageNearestToCenterOfScreen == -1) {
            pageNearestToCenterOfScreen = this.mDefaultPage;
        }
        snapToPage(pageNearestToCenterOfScreen, integer, this.mZoomInInterpolator);
        int i3 = 0;
        while (i3 < getChildCount()) {
            CellLayout cellLayout = (CellLayout) getChildAt(i3);
            boolean z14 = i3 == pageNearestToCenterOfScreen;
            float alpha = cellLayout.getShortcutsAndWidgets().getAlpha();
            if (z6 || z7) {
                f = 0.0f;
            } else if (z4 && this.mWorkspaceFadeInAdjacentScreens) {
                f = (i3 == pageNearestToCenterOfScreen || i3 < numCustomPages()) ? 1.0f : 0.0f;
            } else {
                f = 1.0f;
            }
            if (!this.mIsSwitchingState && (z9 || z11)) {
                if (z11 && z14) {
                    f2 = 0.0f;
                } else if (z14) {
                    f2 = alpha;
                } else {
                    f2 = 0.0f;
                    f = 0.0f;
                }
                cellLayout.setShortcutAndWidgetAlpha(f2);
                alpha = f2;
            }
            this.mOldAlphas[i3] = alpha;
            this.mNewAlphas[i3] = f;
            if (z) {
                this.mOldBackgroundAlphas[i3] = cellLayout.getBackgroundAlpha();
                this.mNewBackgroundAlphas[i3] = f3;
            } else {
                cellLayout.setBackgroundAlpha(f3);
                cellLayout.setShortcutAndWidgetAlpha(f);
            }
            i3++;
            pageNearestToCenterOfScreen = pageNearestToCenterOfScreen;
        }
        View qsbBar = this.mLauncher.getQsbBar();
        AlphaSet overviewPanelAlpha = this.mLauncher.getOverviewPanelAlpha();
        AlphaSet hotseatAlpha = this.mLauncher.getHotseatAlpha();
        AlphaSet indicatorAlpha = this.mLauncher.getIndicatorAlpha();
        if (z) {
            LauncherViewPropertyAnimator launcherViewPropertyAnimator = new LauncherViewPropertyAnimator(this);
            long j = integer;
            launcherViewPropertyAnimator.scaleX(this.mNewScale).scaleY(this.mNewScale).translationY(overviewModeTranslationY).setDuration(j).setInterpolator(this.mZoomInInterpolator);
            animatorSetCreateAnimatorSet.play(launcherViewPropertyAnimator);
            /* REWORK: jadx made the loop var final while an anon captures its value;
             * split into a while loop with an effectively-final copy per iteration. */
            int i4 = 0;
            while (i4 < getChildCount()) {
                final int i4cap = i4;
                final CellLayout cellLayout2 = (CellLayout) getChildAt(i4cap);
                float alpha2 = cellLayout2.getShortcutsAndWidgets().getAlpha();
                if (this.mOldAlphas[i4] == 0.0f && this.mNewAlphas[i4] == 0.0f) {
                    cellLayout2.setBackgroundAlpha(this.mNewBackgroundAlphas[i4]);
                    cellLayout2.setShortcutAndWidgetAlpha(this.mNewAlphas[i4]);
                } else {
                    if (arrayList != null) {  /* REWORK: jadx int-ified a null check */
                        arrayList.add(cellLayout2);
                    }
                    float f7 = this.mOldAlphas[i4];
                    float[] fArr = this.mNewAlphas;
                    if (f7 != fArr[i4] || alpha2 != fArr[i4]) {
                        LauncherViewPropertyAnimator launcherViewPropertyAnimator2 = new LauncherViewPropertyAnimator(cellLayout2.getShortcutsAndWidgets());
                        launcherViewPropertyAnimator2.alpha(this.mNewAlphas[i4]).setDuration(j).setInterpolator(this.mZoomInInterpolator);
                        animatorSetCreateAnimatorSet.play(launcherViewPropertyAnimator2);
                    }
                    if (this.mOldBackgroundAlphas[i4] != 0.0f || this.mNewBackgroundAlphas[i4] != 0.0f) {
                        ValueAnimator valueAnimatorOfFloat = LauncherAnimUtils.ofFloat(cellLayout2, 0.0f, 1.0f);
                        valueAnimatorOfFloat.setInterpolator(this.mZoomInInterpolator);
                        valueAnimatorOfFloat.setDuration(j);
                        valueAnimatorOfFloat.addUpdateListener(new LauncherAnimatorUpdateListener() { // from class: com.voxel.launcher3.Workspace.5
                            @Override // com.voxel.launcher3.LauncherAnimatorUpdateListener
                            public void onAnimationUpdate(float f8, float f9) {
                                cellLayout2.setBackgroundAlpha((f8 * Workspace.this.mOldBackgroundAlphas[i4cap]) + (f9 * Workspace.this.mNewBackgroundAlphas[i4cap]));
                            }
                        });
                        animatorSetCreateAnimatorSet.play(valueAnimatorOfFloat);
                    }
                }
                i4++;
            }
            AlphaSetAnimator alphaSetAnimatorOfFloat = AlphaSetAnimator.ofFloat(indicatorAlpha, this, f4);
            AlphaSetAnimator alphaSetAnimatorOfFloat2 = AlphaSetAnimator.ofFloat(hotseatAlpha, this, f4);
            LauncherViewPropertyAnimator launcherViewPropertyAnimatorWithLayer = new LauncherViewPropertyAnimator(qsbBar).alpha(f6).withLayer();
            launcherViewPropertyAnimatorWithLayer.addListener(new AlphaUpdateListener(qsbBar));
            AlphaSetAnimator alphaSetAnimatorOfFloat3 = AlphaSetAnimator.ofFloat(overviewPanelAlpha, this, f5);
            if (z12) {
                alphaSetAnimatorOfFloat.setInterpolator(new DecelerateInterpolator(2.0f));
                alphaSetAnimatorOfFloat2.setInterpolator(new DecelerateInterpolator(2.0f));
                alphaSetAnimatorOfFloat3.setInterpolator(null);
            } else if (z13) {
                alphaSetAnimatorOfFloat.setInterpolator(null);
                alphaSetAnimatorOfFloat2.setInterpolator(null);
                alphaSetAnimatorOfFloat3.setInterpolator(new DecelerateInterpolator(2.0f));
            }
            alphaSetAnimatorOfFloat3.setDuration(j);
            alphaSetAnimatorOfFloat.setDuration(j);
            alphaSetAnimatorOfFloat2.setDuration(j);
            launcherViewPropertyAnimatorWithLayer.setDuration(j);
            animatorSetCreateAnimatorSet.play(alphaSetAnimatorOfFloat3);
            animatorSetCreateAnimatorSet.play(alphaSetAnimatorOfFloat2);
            animatorSetCreateAnimatorSet.play(launcherViewPropertyAnimatorWithLayer);
            animatorSetCreateAnimatorSet.play(alphaSetAnimatorOfFloat);
            animatorSetCreateAnimatorSet.setStartDelay(i);
        } else {
            overviewPanelAlpha.putValue(this, f5);
            hotseatAlpha.putValue(this, f4);
            indicatorAlpha.putValue(this, f4);
            qsbBar.setAlpha(f6);
            AlphaUpdateListener.updateVisibility(qsbBar);
            updateCustomContentVisibility();
            setScaleX(this.mNewScale);
            setScaleY(this.mNewScale);
            setTranslationY(overviewModeTranslationY);
        }
        this.mLauncher.updateVoiceButtonProxyVisible(false);
        if (z4) {
            animateBackgroundGradient(0.0f, z);
        } else {
            animateBackgroundGradient(getResources().getInteger(R.integer.config_workspaceScrimAlpha) / 100.0f, z);
        }
        return animatorSetCreateAnimatorSet;
    }

    Animator getChangeStateAnimation(State state, boolean z, ArrayList<View> arrayList) {
        return getChangeStateAnimation(state, z, 0, -1, arrayList);
    }

    public float getChildrenOutlineAlpha() {
        return this.mChildrenOutlineAlpha;
    }

    public View getContent() {
        return this;
    }

    public CellLayout getCurrentDropLayout() {
        return (CellLayout) getChildAt(getNextPage());
    }

    @Override // com.voxel.launcher3.PagedView
    protected String getCurrentPageDescription() {
        int i = this.mNextPage != -1 ? this.mNextPage : this.mCurrentPage;
        int iNumCustomPages = numCustomPages();
        return (hasCustomContent() && getNextPage() == 0) ? this.mCustomContentDescription : String.format(getContext().getString(R.string.workspace_scroll_format), Integer.valueOf((i + 1) - iNumCustomPages), Integer.valueOf(getChildCount() - iNumCustomPages));
    }

    public int getCurrentPageOffsetFromCustomContent() {
        return getNextPage() - numCustomPages();
    }

    protected Launcher.CustomContentCallbacks getCustomContentCallbacks() {
        return this.mCustomContentCallbacks;
    }

    @Override // android.view.ViewGroup
    public int getDescendantFocusability() {
        if (workspaceInModalState()) {
            return 393216;
        }
        return super.getDescendantFocusability();
    }

    public CellLayout.CellInfo getDragInfo() {
        return this.mDragInfo;
    }

    @Override // com.voxel.launcher3.PagedView
    protected void getFreeScrollPageRange(int[] iArr) {
        getOverviewModePages(iArr);
    }

    @Override // com.voxel.launcher3.DropTarget
    public void getHitRectRelativeToDragLayer(Rect rect) {
        this.mLauncher.getDragLayer().getDescendantRectRelativeToSelf(this, rect);
    }

    public long getIdForScreen(CellLayout cellLayout) {
        for (Long l : this.mWorkspaceScreens.keySet()) {
            if (this.mWorkspaceScreens.get(l) == cellLayout) {
                return l.longValue();
            }
        }
        return -1L;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public float getIntrinsicIconScaleFactor() {
        return 1.0f;
    }

    public int getNumScreens() {
        ArrayList<Long> arrayList = this.mScreenOrder;
        if (arrayList == null) {
            return 0;
        }
        return arrayList.size();
    }

    int getOverviewModeTranslationY() {
        Rect overviewModeButtonBarRect = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getOverviewModeButtonBarRect();
        int viewportHeight = getViewportHeight();
        int normalChildHeight = (int) (this.mOverviewModeShrinkFactor * getNormalChildHeight());
        return (-((viewportHeight - normalChildHeight) / 2)) + this.mInsets.top + ((((viewportHeight - this.mInsets.top) - overviewModeButtonBarRect.height()) - normalChildHeight) / 2);
    }

    public int getPageIndexForScreenId(long j) {
        return indexOfChild(this.mWorkspaceScreens.get(Long.valueOf(j)));
    }

    @Override // com.voxel.launcher3.PagedView
    protected View.OnClickListener getPageIndicatorClickListener() {
        if (((AccessibilityManager) getContext().getSystemService("accessibility")).isTouchExplorationEnabled()) {
            return new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$p1fwzFjAphStUaMkjkovmLuQeRc
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Workspace.this.enterOverviewMode();
                }
            };
        }
        return null;
    }

    @Override // com.voxel.launcher3.PagedView
    protected String getPageIndicatorDescription() {
        return getCurrentPageDescription() + ", " + getResources().getString(R.string.settings_button_text);
    }

    @Override // com.voxel.launcher3.PagedView
    protected PageIndicator.PageMarkerResources getPageIndicatorMarker(int i) {
        return (getScreenIdForPageIndex(i) != -201 || this.mScreenOrder.size() - numCustomPages() <= 1) ? super.getPageIndicatorMarker(i) : new PageIndicator.PageMarkerResources(R.drawable.ic_pageindicator_current, R.drawable.ic_pageindicator_add);
    }

    CellLayout getParentCellLayoutForView(View view) {
        for (CellLayout cellLayout : getWorkspaceAndHotseatCellLayouts()) {
            if (cellLayout.getShortcutsAndWidgets().indexOfChild(view) > -1) {
                return cellLayout;
            }
        }
        return null;
    }

    public long getScreenIdForPageIndex(int i) {
        if (i < 0 || i >= this.mScreenOrder.size()) {
            return -1L;
        }
        return this.mScreenOrder.get(i).longValue();
    }

    ArrayList<Long> getScreenOrder() {
        return this.mScreenOrder;
    }

    public CellLayout getScreenWithId(long j) {
        return this.mWorkspaceScreens.get(Long.valueOf(j));
    }

    State getState() {
        return this.mState;
    }

    ArrayList<ComponentName> getUniqueComponents(boolean z, ArrayList<ComponentName> arrayList) {
        ArrayList<ComponentName> arrayList2 = new ArrayList<>();
        getUniqueIntents(this.mLauncher.getHotseat().getLayout(), arrayList2, arrayList, false);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getUniqueIntents((CellLayout) getChildAt(i), arrayList2, arrayList, false);
        }
        return arrayList2;
    }

    /* JADX WARN: Code duplicated, block: B:23:0x0069  */
    /* JADX WARN: Code duplicated, block: B:26:0x007a  */
    /* JADX WARN: Code duplicated, block: B:28:0x0088  */
    /* JADX WARN: Code duplicated, block: B:33:0x00ab  */
    /* JADX WARN: Code duplicated, block: B:35:0x00b1  */
    /* JADX WARN: Code duplicated, block: B:36:0x00b5 A[DONT_INVERT] */
    /* JADX WARN: Code duplicated, block: B:37:0x00b7  */
    /* JADX WARN: Code duplicated, block: B:39:0x00c5  */
    /* JADX WARN: Code duplicated, block: B:45:0x00cb A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:49:0x00c8 A[SYNTHETIC] */
    /* JADX WARN: Code duplicated, block: B:51:0x00c8 A[SYNTHETIC] */
    void getUniqueIntents(CellLayout cellLayout, ArrayList<ComponentName> arrayList, ArrayList<ComponentName> arrayList2, boolean z) {
        FolderIcon folderIcon;
        ArrayList<View> itemsInReadingOrder;
        /* REWORK: jadx left this cursor uninitialized; each branch starts it at 0. */
        int i = 0;
        ShortcutInfo shortcutInfo;
        ComponentName component;
        Uri data;
        int childCount = cellLayout.getShortcutsAndWidgets().getChildCount();
        ArrayList arrayList3 = new ArrayList();
        for (int i2 = 0; i2 < childCount; i2++) {
            arrayList3.add(cellLayout.getShortcutsAndWidgets().getChildAt(i2));
        }
        for (int i3 = 0; i3 < childCount; i3++) {
            View view = (View) arrayList3.get(i3);
            ItemInfo itemInfo = (ItemInfo) view.getTag();
            if (itemInfo instanceof ShortcutInfo) {
                ShortcutInfo shortcutInfo2 = (ShortcutInfo) itemInfo;
                ComponentName component2 = shortcutInfo2.intent.getComponent();
                Uri data2 = shortcutInfo2.intent.getData();
                if (data2 == null || data2.equals(Uri.EMPTY)) {
                    if (arrayList.contains(component2)) {
                        if (z) {
                            cellLayout.removeViewInLayout(view);
                            LauncherModel.deleteItemFromDatabase(this.mLauncher, shortcutInfo2);
                        }
                        if (arrayList2 != null) {
                            arrayList2.add(component2);
                        }
                    } else {
                        arrayList.add(component2);
                    }
                    if (view instanceof FolderIcon) {
                        folderIcon = (FolderIcon) view;
                        itemsInReadingOrder = folderIcon.getFolder().getItemsInReadingOrder();
                        for (i = 0; i < itemsInReadingOrder.size(); i++) {
                            if (itemsInReadingOrder.get(i).getTag() instanceof ShortcutInfo) {
                                shortcutInfo = (ShortcutInfo) itemsInReadingOrder.get(i).getTag();
                                component = shortcutInfo.intent.getComponent();
                                data = shortcutInfo.intent.getData();
                                if (data != null || data.equals(Uri.EMPTY)) {
                                    if (arrayList.contains(component)) {
                                        if (z) {
                                            folderIcon.getFolderInfo().remove(shortcutInfo);
                                            LauncherModel.deleteItemFromDatabase(this.mLauncher, shortcutInfo);
                                        }
                                        if (arrayList2 != null) {
                                            arrayList2.add(component);
                                        }
                                    } else {
                                        arrayList.add(component);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (view instanceof FolderIcon) {
                folderIcon = (FolderIcon) view;
                itemsInReadingOrder = folderIcon.getFolder().getItemsInReadingOrder();
                while (i < itemsInReadingOrder.size()) {
                    if (itemsInReadingOrder.get(i).getTag() instanceof ShortcutInfo) {
                        shortcutInfo = (ShortcutInfo) itemsInReadingOrder.get(i).getTag();
                        component = shortcutInfo.intent.getComponent();
                        data = shortcutInfo.intent.getData();
                        if (data != null) {
                            if (arrayList.contains(component)) {
                                arrayList.add(component);
                            } else {
                                if (z) {
                                    folderIcon.getFolderInfo().remove(shortcutInfo);
                                    LauncherModel.deleteItemFromDatabase(this.mLauncher, shortcutInfo);
                                }
                                if (arrayList2 != null) {
                                    arrayList2.add(component);
                                }
                            }
                        } else if (arrayList.contains(component)) {
                            arrayList.add(component);
                        } else {
                            if (z) {
                                folderIcon.getFolderInfo().remove(shortcutInfo);
                                LauncherModel.deleteItemFromDatabase(this.mLauncher, shortcutInfo);
                            }
                            if (arrayList2 != null) {
                                arrayList2.add(component);
                            }
                        }
                    }
                }
            }
        }
    }

    public LauncherAppWidgetHostView getWidgetForAppWidgetId(final int i) {
        return (LauncherAppWidgetHostView) getFirstMatch(new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$F1X0yQriunp_VXCZXD88bAGdCT0
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Workspace.lambda$getWidgetForAppWidgetId$14(i, itemInfo, view, view2);
            }
        });
    }

    ArrayList<CellLayout> getWorkspaceAndHotseatCellLayouts() {
        ArrayList<CellLayout> arrayList = new ArrayList<>();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            arrayList.add((CellLayout) getChildAt(i));
        }
        if (this.mLauncher.getHotseat() != null) {
            arrayList.add(this.mLauncher.getHotseat().getLayout());
        }
        return arrayList;
    }

    public boolean hasCustomContent() {
        return this.mScreenOrder.size() > 0 && this.mScreenOrder.get(0).longValue() == -301;
    }

    public boolean hasExtraEmptyScreen() {
        return this.mWorkspaceScreens.containsKey(-201L) && getChildCount() - numCustomPages() > 1;
    }

    void hideCustomContentIfNecessary() {
        if ((this.mState != State.NORMAL) && hasCustomContent()) {
            disableLayoutTransitions();
            this.mWorkspaceScreens.get(-301L).setVisibility(4);
            enableLayoutTransitions();
        }
    }

    void hideOutlines() {
        if (workspaceInModalState() || this.mIsSwitchingState) {
            return;
        }
        ObjectAnimator objectAnimator = this.mChildrenOutlineFadeInAnimation;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        ObjectAnimator objectAnimator2 = this.mChildrenOutlineFadeOutAnimation;
        if (objectAnimator2 != null) {
            objectAnimator2.cancel();
        }
        this.mChildrenOutlineFadeOutAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 0.0f);
        this.mChildrenOutlineFadeOutAnimation.setDuration(375L);
        this.mChildrenOutlineFadeOutAnimation.setStartDelay(0L);
        this.mChildrenOutlineFadeOutAnimation.start();
    }

    protected void initWorkspace() {
        this.mCurrentPage = this.mDefaultPage;
        Launcher.setScreen(this.mCurrentPage);
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        launcherAppState.getDynamicGrid().getDeviceProfile();
        this.mIconCache = launcherAppState.getIconCache();
        setWillNotDraw(false);
        setClipChildren(false);
        setClipToPadding(false);
        setChildrenDrawnWithCacheEnabled(true);
        setMinScale(this.mOverviewModeShrinkFactor);
        setupLayoutTransition();
        this.mWallpaperOffset = new WallpaperOffsetInterpolator();
        this.mLauncher.getWindowManager().getDefaultDisplay().getSize(this.mDisplaySize);
        this.mFlingThresholdVelocity = (int) (this.mDensity * 500.0f);
        setWallpaperDimension();
    }

    public long insertNewWorkspaceScreen(long j) {
        return insertNewWorkspaceScreen(j, getChildCount());
    }

    public long insertNewWorkspaceScreen(long j, int i) {
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - insertNewWorkspaceScreen(): " + j + " at index: " + i, true);
        if (this.mWorkspaceScreens.containsKey(Long.valueOf(j))) {
            throw new RuntimeException("Screen id " + j + " already exists!");
        }
        CellLayout cellLayout = (CellLayout) this.mLauncher.getLayoutInflater().inflate(R.layout.workspace_screen, (ViewGroup) null);
        cellLayout.setOnLongClickListener(this.mLongClickListener);
        cellLayout.setOnClickListener(this.mLauncher);
        cellLayout.setSoundEffectsEnabled(false);
        this.mWorkspaceScreens.put(Long.valueOf(j), cellLayout);
        this.mScreenOrder.add(i, Long.valueOf(j));
        addView(cellLayout, i);
        return j;
    }

    public long insertNewWorkspaceScreenBeforeEmptyScreen(long j) {
        int iIndexOf = this.mScreenOrder.indexOf(-201L);
        if (iIndexOf < 0) {
            iIndexOf = this.mScreenOrder.size();
        }
        return insertNewWorkspaceScreen(j, iIndexOf);
    }

    @Override // com.voxel.launcher3.DropTarget
    public boolean isDropEnabled() {
        return true;
    }

    public boolean isFinishedSwitchingState() {
        return !this.mIsSwitchingState || this.mTransitionProgress > 0.5f;
    }

    public boolean isInOverviewMode() {
        return this.mState == State.OVERVIEW;
    }

    public boolean isOnOrMovingToCustomContent() {
        return hasCustomContent() && getNextPage() == 0;
    }

    boolean isPointInSelfOverHotseat(int i, int i2, Rect rect) {
        if (rect == null) {
            new Rect();
        }
        int[] iArr = this.mTempPt;
        iArr[0] = i;
        iArr[1] = i2;
        this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, this.mTempPt, true);
        Rect hotseatRect = LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().getHotseatRect();
        int[] iArr2 = this.mTempPt;
        return hotseatRect.contains(iArr2[0], iArr2[1]);
    }

    public boolean isSwitchingState() {
        return this.mIsSwitchingState;
    }

    boolean isTouchActive() {
        return this.mTouchState != 0;
    }

    void mapOverItems(boolean z, ItemOperator itemOperator) {
        ArrayList<ShortcutAndWidgetContainer> allShortcutAndWidgetContainers = getAllShortcutAndWidgetContainers();
        int size = allShortcutAndWidgetContainers.size();
        for (int i = 0; i < size; i++) {
            ShortcutAndWidgetContainer shortcutAndWidgetContainer = allShortcutAndWidgetContainers.get(i);
            int childCount = shortcutAndWidgetContainer.getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                View childAt = shortcutAndWidgetContainer.getChildAt(i2);
                ItemInfo itemInfo = (ItemInfo) childAt.getTag();
                if (z && (itemInfo instanceof FolderInfo) && (childAt instanceof FolderIcon)) {
                    FolderIcon folderIcon = (FolderIcon) childAt;
                    ArrayList<View> itemsInReadingOrder = folderIcon.getFolder().getItemsInReadingOrder();
                    int size2 = itemsInReadingOrder.size();
                    for (int i3 = 0; i3 < size2; i3++) {
                        View view = itemsInReadingOrder.get(i3);
                        if (itemOperator.evaluate((ItemInfo) view.getTag(), view, folderIcon)) {
                            return;
                        }
                    }
                } else if (itemOperator.evaluate(itemInfo, childAt, null)) {
                    return;
                }
            }
        }
    }

    void mapPointFromChildToSelf(View view, float[] fArr) {
        fArr[0] = fArr[0] + view.getLeft();
        fArr[1] = fArr[1] + view.getTop();
    }

    void mapPointFromSelfToChild(View view, float[] fArr, Matrix matrix) {
        fArr[0] = fArr[0] - view.getLeft();
        fArr[1] = fArr[1] - view.getTop();
    }

    void mapPointFromSelfToHotseatLayout(Hotseat hotseat, float[] fArr) {
        int[] iArr = this.mTempPt;
        iArr[0] = (int) fArr[0];
        iArr[1] = (int) fArr[1];
        this.mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(this, this.mTempPt, true);
        this.mLauncher.getDragLayer().mapCoordInSelfToDescendent(hotseat.getLayout(), this.mTempPt);
        int[] iArr2 = this.mTempPt;
        fArr[0] = iArr2[0];
        fArr[1] = iArr2[1];
    }

    public boolean moveToDefaultScreen(boolean z) {
        boolean z2 = true;
        if (this.mNextPage == -1 ? this.mDefaultPage == this.mCurrentPage : this.mDefaultPage == this.mNextPage) {
            z2 = false;
        }
        moveToScreen(this.mDefaultPage, z);
        return z2;
    }

    @Override // com.voxel.launcher3.PagedView
    protected void notifyPageSwitchListener() {
        super.notifyPageSwitchListener();
        Launcher.setScreen(getNextPage());
        if (hasCustomContent() && getNextPage() == 0 && !this.mCustomContentShowing) {
            this.mCustomContentShowing = true;
            Launcher.CustomContentCallbacks customContentCallbacks = this.mCustomContentCallbacks;
            if (customContentCallbacks != null) {
                customContentCallbacks.onShow(false);
                this.mCustomContentShowTime = System.currentTimeMillis();
                this.mLauncher.updateVoiceButtonProxyVisible(false);
                return;
            }
            return;
        }
        if (hasCustomContent() && getNextPage() != 0 && this.mCustomContentShowing) {
            this.mCustomContentShowing = false;
            Launcher.CustomContentCallbacks customContentCallbacks2 = this.mCustomContentCallbacks;
            if (customContentCallbacks2 != null) {
                customContentCallbacks2.onHide();
                this.mLauncher.resetQSBScroll();
                this.mLauncher.updateVoiceButtonProxyVisible(false);
            }
        }
    }

    public int numCustomPages() {
        return hasCustomContent() ? 1 : 0;
    }

    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mWindowToken = getWindowToken();
        DragController dragController = this.mDragController;
        if (dragController != null) {
            dragController.setWindowToken(this.mWindowToken);
        }
    }

    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup.OnHierarchyChangeListener
    public void onChildViewAdded(View view, View view2) {
        if (!(view2 instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        CellLayout cellLayout = (CellLayout) view2;
        cellLayout.setOnInterceptTouchListener(this);
        cellLayout.setClickable(true);
        cellLayout.setImportantForAccessibility(2);
        super.onChildViewAdded(view, view2);
    }

    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mWindowToken = null;
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragEnd() {
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enforceDragParity("onDragEnd", 0, 0);
        }
        if (!this.mDeferRemoveExtraEmptyScreen) {
            removeExtraEmptyScreen(true, this.mDragSourceInternal != null);
        }
        this.mIsDragOccuring = false;
        updateChildrenLayersEnabled(false);
        this.mLauncher.unlockScreenOrientation(false);
        InstallShortcutReceiver.disableAndFlushInstallQueue(getContext());
        UninstallShortcutReceiver.disableAndFlushUninstallQueue(getContext());
        this.mDragSourceInternal = null;
        this.mLauncher.onInteractionEnd();
        this.mOutlineProvider = null;
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragEnter(DropTarget.DragObject dragObject) {
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enforceDragParity("onDragEnter", 1, 1);
        }
        this.mCreateUserFolderOnDrop = false;
        this.mAddToExistingFolderOnDrop = false;
        this.mDropToLayout = null;
        CellLayout currentDropLayout = getCurrentDropLayout();
        setCurrentDropLayout(currentDropLayout);
        setCurrentDragOverlappingLayout(currentDropLayout);
        if (workspaceInModalState() || dragObject.isDesktopLocked) {
            return;
        }
        this.mLauncher.getDragLayer().showPageHints();
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragExit(DropTarget.DragObject dragObject) {
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enforceDragParity("onDragExit", -1, 0);
        }
        if (!this.mInScrollArea) {
            this.mDropToLayout = this.mDragTargetLayout;
        } else if (isPageMoving()) {
            this.mDropToLayout = (CellLayout) getPageAt(getNextPage());
        } else {
            this.mDropToLayout = this.mDragOverlappingLayout;
        }
        int i = this.mDragMode;
        if (i == 1) {
            this.mCreateUserFolderOnDrop = true;
        } else if (i == 2) {
            this.mAddToExistingFolderOnDrop = true;
        }
        onResetScrollArea();
        setCurrentDropLayout(null);
        setCurrentDragOverlappingLayout(null);
        this.mSpringLoadedDragController.cancel();
        if (!this.mIsPageMoving) {
            hideOutlines();
        }
        this.mLauncher.getDragLayer().hidePageHints();
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onDragOver(DropTarget.DragObject dragObject) {
        int i;
        int i2;
        int i3;
        int i4;
        CellLayout cellLayout;
        if (this.mInScrollArea || !transitionStateShouldAllowDrop() || dragObject.isDesktopLocked) {
            return;
        }
        Rect rect = new Rect();
        ItemInfo itemInfo = dragObject.dragInfo;
        if (itemInfo == null) {
            if (LauncherAppState.isDogfoodBuild()) {
                throw new NullPointerException("DragObject has null info");
            }
            return;
        }
        if (itemInfo.spanX < 0 || itemInfo.spanY < 0) {
            throw new RuntimeException("Improper spans found");
        }
        this.mDragViewVisualCenter = getDragViewVisualCenter(dragObject.x, dragObject.y, dragObject.xOffset, dragObject.yOffset, dragObject.dragView, this.mDragViewVisualCenter);
        CellLayout.CellInfo cellInfo = this.mDragInfo;
        View view = cellInfo == null ? null : cellInfo.cell;
        if (workspaceInModalState()) {
            CellLayout layout = (this.mLauncher.getHotseat() == null || isExternalDragWidget(dragObject) || !isPointInSelfOverHotseat(dragObject.x, dragObject.y, rect)) ? null : this.mLauncher.getHotseat().getLayout();
            if (layout == null) {
                layout = findMatchingPageForDragOver(dragObject.dragView, dragObject.x, dragObject.y, false);
            }
            if (layout != this.mDragTargetLayout) {
                setCurrentDropLayout(layout);
                setCurrentDragOverlappingLayout(layout);
                if (this.mState == State.SPRING_LOADED) {
                    if (this.mLauncher.isHotseatLayout(layout)) {
                        this.mSpringLoadedDragController.cancel();
                    } else {
                        this.mSpringLoadedDragController.setAlarm(this.mDragTargetLayout);
                    }
                }
            }
        } else {
            CellLayout layout2 = (this.mLauncher.getHotseat() == null || isDragWidget(dragObject) || !isPointInSelfOverHotseat(dragObject.x, dragObject.y, rect)) ? null : this.mLauncher.getHotseat().getLayout();
            if (layout2 == null) {
                layout2 = getCurrentDropLayout();
            }
            if (layout2 != this.mDragTargetLayout) {
                setCurrentDropLayout(layout2);
                setCurrentDragOverlappingLayout(layout2);
            }
        }
        CellLayout cellLayout2 = this.mDragTargetLayout;
        if (cellLayout2 != null) {
            if (this.mLauncher.isHotseatLayout(cellLayout2)) {
                mapPointFromSelfToHotseatLayout(this.mLauncher.getHotseat(), this.mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(this.mDragTargetLayout, this.mDragViewVisualCenter, null);
            }
            ItemInfo itemInfo2 = dragObject.dragInfo;
            int i5 = itemInfo.spanX;
            int i6 = itemInfo.spanY;
            if (itemInfo.minSpanX <= 0 || itemInfo.minSpanY <= 0) {
                i = i5;
                i2 = i6;
            } else {
                i = itemInfo.minSpanX;
                i2 = itemInfo.minSpanY;
            }
            float[] fArr = this.mDragViewVisualCenter;
            this.mTargetCell = findNearestArea((int) fArr[0], (int) fArr[1], i, i2, this.mDragTargetLayout, this.mTargetCell);
            int[] iArr = this.mTargetCell;
            int i7 = iArr[0];
            int i8 = iArr[1];
            setCurrentDropOverCell(iArr[0], iArr[1]);
            CellLayout cellLayout3 = this.mDragTargetLayout;
            float[] fArr2 = this.mDragViewVisualCenter;
            float distanceFromCell = cellLayout3.getDistanceFromCell(fArr2[0], fArr2[1], this.mTargetCell);
            CellLayout cellLayout4 = this.mDragTargetLayout;
            int[] iArr2 = this.mTargetCell;
            manageFolderFeedback(itemInfo2, this.mDragTargetLayout, this.mTargetCell, distanceFromCell, cellLayout4.getChildAt(iArr2[0], iArr2[1]));
            CellLayout cellLayout5 = this.mDragTargetLayout;
            float[] fArr3 = this.mDragViewVisualCenter;
            boolean zIsNearestDropLocationOccupied = cellLayout5.isNearestDropLocationOccupied((int) fArr3[0], (int) fArr3[1], itemInfo.spanX, itemInfo.spanY, view, this.mTargetCell);
            if (zIsNearestDropLocationOccupied) {
                int i9 = this.mDragMode;
                if ((i9 != 0 && i9 != 3) || this.mReorderAlarm.alarmPending() || (this.mLastReorderX == i7 && this.mLastReorderY == i8)) {
                    i3 = 1;
                    i4 = 2;
                } else {
                    CellLayout cellLayout6 = this.mDragTargetLayout;
                    float[] fArr4 = this.mDragViewVisualCenter;
                    cellLayout6.performReorder((int) fArr4[0], (int) fArr4[1], i, i2, itemInfo.spanX, itemInfo.spanY, view, this.mTargetCell, new int[2], 0);
                    i4 = 2;
                    i3 = 1;
                    this.mReorderAlarm.setOnAlarmListener(new ReorderAlarmListener(this.mDragViewVisualCenter, i, i2, itemInfo.spanX, itemInfo.spanY, dragObject.dragView, view));
                    this.mReorderAlarm.setAlarm(350L);
                }
            } else {
                CellLayout cellLayout7 = this.mDragTargetLayout;
                Bitmap bitmap = this.mOutlineProvider.generatedDragOutline;
                float[] fArr5 = this.mDragViewVisualCenter;
                int i10 = (int) fArr5[0];
                int i11 = (int) fArr5[1];
                int[] iArr3 = this.mTargetCell;
                cellLayout7.visualizeDropLocation(view, bitmap, i10, i11, iArr3[0], iArr3[1], itemInfo.spanX, itemInfo.spanY, false, dragObject.dragView.getDragVisualizeOffset(), dragObject.dragView.getDragRegion());
                i3 = 1;
                i4 = 2;
            }
            int i12 = this.mDragMode;
            if ((i12 == i3 || i12 == i4 || !zIsNearestDropLocationOccupied) && (cellLayout = this.mDragTargetLayout) != null) {
                cellLayout.revertTempState();
            }
        }
    }

    @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
    public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
        if (ENFORCE_DRAG_EVENT_ORDER) {
            enforceDragParity("onDragStart", 0, 0);
        }
        DragPreviewProvider dragPreviewProvider = this.mOutlineProvider;
        if (dragPreviewProvider != null) {
            dragPreviewProvider.generateDragOutline(this.mCanvas);
        }
        this.mIsDragOccuring = true;
        updateChildrenLayersEnabled(false);
        this.mLauncher.lockScreenOrientation();
        this.mLauncher.onInteractionBegin();
        setChildrenBackgroundAlphaMultipliers(1.0f);
        InstallShortcutReceiver.enableInstallQueue();
        UninstallShortcutReceiver.enableUninstallQueue();
        this.mDeferRemoveExtraEmptyScreen = false;
        addExtraEmptyScreenOnDrag();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        post(this.mBindPages);
    }

    /* JADX WARN: Code duplicated, block: B:72:0x019a  */
    /* JADX WARN: Code duplicated, block: B:74:0x019e  */
    /* JADX WARN: Code duplicated, block: B:76:0x01a7  */
    /* JADX WARN: Code duplicated, block: B:78:0x01ad  */
    /* JADX WARN: Code duplicated, block: B:79:0x01b1  */
    /* JADX WARN: Code duplicated, block: B:82:0x01e1  */
    /* JADX WARN: Code duplicated, block: B:84:0x01e9  */
    /* JADX WARN: Code duplicated, block: B:92:0x022f  */
    /* JADX WARN: Code duplicated, block: B:94:0x024d  */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v1 */
    /* JADX WARN: Type inference failed for: r12v13 */
    /* JADX WARN: Type inference failed for: r12v14 */
    /* JADX WARN: Type inference failed for: r12v2, types: [android.view.View] */
    /* JADX WARN: Type inference failed for: r15v1 */
    /* JADX WARN: Type inference failed for: r15v2, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r15v4 */
    /* JADX WARN: Type inference failed for: r15v9 */
    /* JADX WARN: Type inference failed for: r1v7, types: [com.voxel.launcher3.dragndrop.DragLayer] */
    /* JADX WARN: Type inference failed for: r2v3, types: [android.view.View] */
    /* JADX WARN: Type inference failed for: r43v0, types: [android.view.View, com.voxel.launcher3.Workspace] */
    /* JADX WARN: Type inference failed for: r6v1, types: [android.view.View] */
    /* JADX WARN: Type inference failed for: r8v2, types: [com.voxel.launcher3.CellLayout] */
    @Override // com.voxel.launcher3.DropTarget
    public void onDrop(DropTarget.DragObject dragObject) {
        /* REWORK: jadx left the register locals blank; initialize them so every
         * dex-possible path has a definite value, and snapshot the captures
         * (the reorder-frame runnable and the item being moved) as single assignments. */
        View r12 = null;
        boolean z = false;
        int r15 = 0;
        Runnable runnable = null;
        int i = -1;
        boolean z2 = false;
        char c = 0;
        int i2 = 0;
        View view = null;
        View view2 = null;
        ItemInfo itemInfo = null;
        boolean z3 = false;
        CellLayout parentCellLayoutForView = null;
        View view3 = null;
        this.mDragViewVisualCenter = getDragViewVisualCenter(dragObject.x, dragObject.y, dragObject.xOffset, dragObject.yOffset, dragObject.dragView, this.mDragViewVisualCenter);
        final CellLayout cellLayout = this.mDropToLayout;
        if (cellLayout != null) {
            if (this.mLauncher.isHotseatLayout(cellLayout)) {
                mapPointFromSelfToHotseatLayout(this.mLauncher.getHotseat(), this.mDragViewVisualCenter);
            } else {
                mapPointFromSelfToChild(cellLayout, this.mDragViewVisualCenter, null);
            }
        }
        if (dragObject.dragSource != this) {
            float[] fArr = this.mDragViewVisualCenter;
            onDropExternal(new int[]{(int) fArr[0], (int) fArr[1]}, dragObject.dragInfo, cellLayout, false, dragObject);
            return;
        }
        CellLayout.CellInfo cellInfo = this.mDragInfo;
        if (cellInfo != null) {
            View view4 = cellInfo.cell;
            if (cellLayout == null || dragObject.cancelled) {
                r12 = view4;
                z = true;
                r15 = 0;
                runnable = null;
                i = -1;
                z2 = false;
            } else {
                boolean z4 = getParentCellLayoutForView(view4) != cellLayout;
                boolean zIsHotseatLayout = this.mLauncher.isHotseatLayout(cellLayout);
                long j = zIsHotseatLayout ? -101L : -100L;
                long idForScreen = this.mTargetCell[0] < 0 ? this.mDragInfo.screenId : getIdForScreen(cellLayout);
                CellLayout.CellInfo cellInfo2 = this.mDragInfo;
                int i3 = cellInfo2 != null ? cellInfo2.spanX : 1;
                CellLayout.CellInfo cellInfo3 = this.mDragInfo;
                int i4 = cellInfo3 != null ? cellInfo3.spanY : 1;
                float[] fArr2 = this.mDragViewVisualCenter;
                long j2 = idForScreen;
                this.mTargetCell = findNearestArea((int) fArr2[0], (int) fArr2[1], i3, i4, cellLayout, this.mTargetCell);
                float[] fArr3 = this.mDragViewVisualCenter;
                float distanceFromCell = cellLayout.getDistanceFromCell(fArr3[0], fArr3[1], this.mTargetCell);
                if (this.mInScrollArea) {
                    c = 0;
                } else {
                    c = 0;
                    if (createUserFolderIfNecessary(view4, j, cellLayout, this.mTargetCell, distanceFromCell, false, dragObject.dragView, null)) {
                        return;
                    }
                }
                if (addToExistingFolderIfNecessary(view4, cellLayout, this.mTargetCell, distanceFromCell, dragObject, false)) {
                    return;
                }
                ItemInfo itemInfo2 = dragObject.dragInfo;
                int i5 = itemInfo2.spanX;
                int i6 = itemInfo2.spanY;
                if (itemInfo2.minSpanX <= 0 || itemInfo2.minSpanY <= 0) {
                    i2 = i6;
                } else {
                    i5 = itemInfo2.minSpanX;
                    i2 = itemInfo2.minSpanY;
                }
                int[] iArr = new int[2];
                float[] fArr4 = this.mDragViewVisualCenter;
                this.mTargetCell = cellLayout.performReorder((int) fArr4[c], (int) fArr4[1], i5, i2, i3, i4, view4, this.mTargetCell, iArr, 2);
                int[] iArr2 = this.mTargetCell;
                boolean z5 = iArr2[0] >= 0 && iArr2[1] >= 0;
                if (z5) {
                    view3 = view4;
                    view2 = view4;
                    if ((view3 instanceof AppWidgetHostView) && (iArr[0] != itemInfo2.spanX || iArr[1] != itemInfo2.spanY)) {
                        itemInfo2.spanX = iArr[0];
                        itemInfo2.spanY = iArr[1];
                        AppWidgetResizeFrame.updateWidgetSizeRanges((AppWidgetHostView) view3, this.mLauncher, iArr[0], iArr[1]);
                        z2 = true;
                    }
                } else {
                    view2 = view4;
                }
                if (getScreenIdForPageIndex(this.mCurrentPage) != j2 || zIsHotseatLayout) {
                    i = -1;
                } else {
                    int pageIndexForScreenId = getPageIndexForScreenId(j2);
                    snapToPage(pageIndexForScreenId);
                    i = pageIndexForScreenId;
                }
                if (z5) {
                    itemInfo = (ItemInfo) view2.getTag();
                    if (z4) {
                        parentCellLayoutForView = getParentCellLayoutForView(view2);
                        if (parentCellLayoutForView != null) {
                            parentCellLayoutForView.removeView(view2);
                        } else if (LauncherAppState.isDogfoodBuild()) {
                            throw new NullPointerException("mDragInfo.cell has null parent");
                        }
                        int[] iArr3 = this.mTargetCell;
                        z = true;
                        z3 = false;
                        addInScreen(view2, j, j2, iArr3[0], iArr3[1], itemInfo.spanX, itemInfo.spanY);
                    } else {
                        z = true;
                        z3 = false;
                    }
                    CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) view2.getLayoutParams();
                    int[] iArr4 = this.mTargetCell;
                    int i7 = iArr4[z3 ? 1 : 0];
                    layoutParams.tmpCellX = i7;
                    layoutParams.cellX = i7;
                    int i8 = iArr4[z ? 1 : 0];
                    layoutParams.tmpCellY = i8;
                    layoutParams.cellY = i8;
                    layoutParams.cellHSpan = itemInfo2.spanX;
                    layoutParams.cellVSpan = itemInfo2.spanY;
                    layoutParams.isLockedToGrid = z;
                    if (j == -101 && (view2 instanceof LauncherAppWidgetHostView)) {
                        final LauncherAppWidgetHostView launcherAppWidgetHostView = (LauncherAppWidgetHostView) view2;
                        if (launcherAppWidgetHostView.getAppWidgetInfo() != null) {
                            final ItemInfo itemInfoCapture = itemInfo;
                            final Runnable runnable2 = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$Yzg-EHYHNKtY2aLNe087JrtxgAQ
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Workspace.this.mLauncher.getDragLayer().addResizeFrame(itemInfoCapture, launcherAppWidgetHostView, cellLayout);
                                }
                            };
                            runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$WuXbCNZfUrKJJfPciR70Di3_o_g
                                @Override // java.lang.Runnable
                                public final void run() {
                                    Workspace.this.lambda$onDrop$5( runnable2);
                                }
                            };
                        } else {
                            runnable = null;
                        }
                    } else {
                        runnable = null;
                    }
                    LauncherModel.modifyItemInDatabase(this.mLauncher, itemInfo, j, j2, layoutParams.cellX, layoutParams.cellY, itemInfo2.spanX, itemInfo2.spanY);
                    r12 = view2;
                    r15 = 0;
                } else {
                    z = true;
                    r15 = 0;
                    CellLayout.LayoutParams layoutParams2 = (CellLayout.LayoutParams) view2.getLayoutParams();
                    this.mTargetCell[0] = layoutParams2.cellX;
                    this.mTargetCell[1] = layoutParams2.cellY;
                    ((CellLayout) view2.getParent().getParent()).markCellsAsOccupiedForView(view2);
                    runnable = null;
                    r12 = view2;
                }
            }
            CellLayout r8 = (CellLayout) r12.getParent().getParent();
            final Runnable runnableCapture = runnable;
            Runnable runnable3 = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$3eYfUmlyxMvpIfZKDWtWROEMXtE
                @Override // java.lang.Runnable
                public final void run() {
                    Workspace.this.lambda$onDrop$6( runnableCapture);
                }
            };
            this.mAnimatingViewIntoPlace = z;
            if (dragObject.dragView.hasDrawn()) {
                ItemInfo itemInfo3 = (ItemInfo) r12.getTag();
                if (itemInfo3.itemType == 4) {
                    animateWidgetDrop(itemInfo3, r8, dragObject.dragView, runnable3, z2 ? 2 : 0, r12, false);
                } else {
                    this.mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, r12, i < 0 ? -1 : 300, runnable3, this);
                }
            } else {
                dragObject.deferDragViewCleanupPostAnimation = r15 != 0;  /* REWORK: r15 was mangled to int by jadx */
                r12.setVisibility(r15);
            }
            r8.onDropChild(r12);
        }
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onDropCompleted(final View view, DragObjectAbstraction dragObjectAbstraction, final boolean z, final boolean z2) {
        CellLayout.CellInfo cellInfo;
        final DropTarget.DragObject dragObject = (DropTarget.DragObject) dragObjectAbstraction;
        if (view != null) {
            boolean z3 = view instanceof RemoveDropTarget;
        }
        if (this.mDeferDropAfterUninstall) {
            this.mDeferredAction = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$pL1aVQeWZXsebrXcp3xx2Dl3GM4
                @Override // java.lang.Runnable
                public final void run() {
                    Workspace.this.lambda$onDropCompleted$10( view, dragObject, z, z2);
                }
            };
            return;
        }
        boolean z4 = this.mDeferredAction != null;
        if (!z2 || (z4 && !this.mUninstallSuccessful)) {
            CellLayout.CellInfo cellInfo2 = this.mDragInfo;
            if (cellInfo2 != null) {
                final CellLayout layout = cellInfo2.container == -101 ? this.mLauncher.getHotseat().getLayout() : getScreenWithId(this.mDragInfo.screenId);
                if (layout == null && LauncherAppState.isDogfoodBuild()) {
                    throw new RuntimeException("Invalid state: cellLayout == null in Workspace#onDropCompleted. Please file a bug. ");
                }
                if (layout != null) {
                    final View view2 = this.mDragInfo.cell != null ? this.mDragInfo.cell : null;
                    if (view2 != null) {
                        this.mLauncher.getDragLayer().animateViewIntoPosition(dragObject.dragView, view2, new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$bF7UBqx2qrKRj8bDM1WWR7M5Sic
                            @Override // java.lang.Runnable
                            public final void run() {
                                Workspace.lambda$onDropCompleted$11(view2, layout);
                            }
                        }, this);
                    } else {
                        dragObject.deferDragViewCleanupPostAnimation = false;
                    }
                }
            }
        } else if (view != this && (cellInfo = this.mDragInfo) != null) {
            CellLayout parentCellLayoutForView = getParentCellLayoutForView(cellInfo.cell);
            if (parentCellLayoutForView != null) {
                parentCellLayoutForView.removeView(this.mDragInfo.cell);
            } else if (LauncherAppState.isDogfoodBuild()) {
                throw new NullPointerException("mDragInfo.cell has null parent");
            }
            if (this.mDragInfo.cell instanceof DropTarget) {
                this.mDragController.removeDropTarget((DropTarget) this.mDragInfo.cell);
            }
        }
        if ((dragObject.cancelled || (z4 && !this.mUninstallSuccessful)) && this.mDragInfo.cell != null) {
            this.mDragInfo.cell.setVisibility(0);
        }
        this.mOutlineProvider = null;
        this.mDragInfo = null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.voxel.launcher3.PagedView
    public void onEndReordering() {
        super.onEndReordering();
        if (this.mLauncher.isWorkspaceLoading()) {
            return;
        }
        hideOutlines();
        this.mScreenOrder.clear();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mScreenOrder.add(Long.valueOf(getIdForScreen((CellLayout) getChildAt(i))));
        }
        this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        enableLayoutTransitions();
    }

    @Override // com.voxel.launcher3.dragndrop.DragScroller
    public boolean onEnterScrollArea(int i, int i2, int i3) {
        boolean z = !LauncherAppState.isScreenLandscape(getContext());
        if (this.mLauncher.getHotseat() != null && z) {
            Rect rect = new Rect();
            this.mLauncher.getHotseat().getHitRect(rect);
            if (rect.contains(i, i2)) {
                return false;
            }
        }
        if (!workspaceInModalState() && !this.mIsSwitchingState && this.mLauncher.getOpenFolder() == null) {
            this.mInScrollArea = true;
            int nextPage = getNextPage() + (i3 == 0 ? -1 : 1);
            setCurrentDropLayout(null);
            if (nextPage < 0 || nextPage >= getChildCount() || getScreenIdForPageIndex(nextPage) == -301) {
                return false;
            }
            setCurrentDragOverlappingLayout((CellLayout) getChildAt(nextPage));
            invalidate();
            return true;
        }
        return false;
    }

    @Override // com.voxel.launcher3.dragndrop.DragScroller
    public boolean onExitScrollArea() {
        if (!this.mInScrollArea) {
            return false;
        }
        invalidate();
        CellLayout currentDropLayout = getCurrentDropLayout();
        setCurrentDropLayout(currentDropLayout);
        setCurrentDragOverlappingLayout(currentDropLayout);
        this.mInScrollArea = false;
        return true;
    }

    @Override // com.voxel.launcher3.DropTarget
    public void onFlingToDelete(DropTarget.DragObject dragObject, PointF pointF) {
    }

    @Override // com.evie.common.dragndrop.DragSource
    public void onFlingToDeleteCompleted() {
    }

    @Override // com.voxel.launcher3.PagedView, android.view.View
    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        Launcher.CustomContentCallbacks customContentCallbacks;
        if (getScreenIdForPageIndex(getCurrentPage()) != -301 || (customContentCallbacks = this.mCustomContentCallbacks) == null || customContentCallbacks.isScrollingAllowed()) {
            return super.onGenericMotionEvent(motionEvent);
        }
        return false;
    }

    /* JADX WARN: Code duplicated, block: B:7:0x0020  */
    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        CellLayout cellLayout;
        int action = motionEvent.getAction() & 255;
        if (action != 6) {
            switch (action) {
                case 0:
                    this.mXDown = motionEvent.getX();
                    this.mYDown = motionEvent.getY();
                    this.mTouchDownTime = System.currentTimeMillis();
                    break;
                case 1:
                    if (this.mTouchState == 0 && (cellLayout = (CellLayout) getChildAt(this.mCurrentPage)) != null && !cellLayout.lastDownOnOccupiedCell()) {
                        onWallpaperTap(motionEvent);
                    }
                    break;
            }
        } else if (this.mTouchState == 0) {
            onWallpaperTap(motionEvent);
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    @Override // com.voxel.launcher3.LauncherTransitionable
    public void onLauncherTransitionEnd(Launcher launcher, boolean z, boolean z2) {
        onTransitionEnd();
    }

    @Override // com.voxel.launcher3.LauncherTransitionable
    public void onLauncherTransitionPrepare(Launcher launcher, boolean z, boolean z2) {
        onTransitionPrepare();
    }

    @Override // com.voxel.launcher3.LauncherTransitionable
    public void onLauncherTransitionStart(Launcher launcher, boolean z, boolean z2) {
    }

    @Override // com.voxel.launcher3.LauncherTransitionable
    public void onLauncherTransitionStep(Launcher launcher, float f) {
        this.mTransitionProgress = f;
    }

    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mFirstLayout && this.mCurrentPage >= 0 && this.mCurrentPage < getChildCount()) {
            this.mWallpaperOffset.syncWithScroll();
            this.mWallpaperOffset.jumpToFinal();
        }
        super.onLayout(z, i, i2, i3, i4);
    }

    @Override // com.voxel.launcher3.PagedView
    protected void onPageBeginMoving() {
        super.onPageBeginMoving();
        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else if (this.mNextPage != -1) {
            enableChildrenCache(this.mCurrentPage, this.mNextPage);
        } else {
            enableChildrenCache(this.mCurrentPage - 1, this.mCurrentPage + 1);
        }
    }

    @Override // com.voxel.launcher3.PagedView
    protected void onPageEndMoving() {
        super.onPageEndMoving();
        if (isHardwareAccelerated()) {
            updateChildrenLayersEnabled(false);
        } else {
            clearChildrenCache();
        }
        if (this.mDragController.isDragging() && workspaceInModalState()) {
            this.mDragController.forceTouchMove();
        }
        Runnable runnable = this.mDelayedResizeRunnable;
        if (runnable != null) {
            runnable.run();
            this.mDelayedResizeRunnable = null;
        }
        Runnable runnable2 = this.mDelayedSnapToPageRunnable;
        if (runnable2 != null) {
            runnable2.run();
            this.mDelayedSnapToPageRunnable = null;
        }
        if (this.mStripScreensOnPageStopMoving) {
            stripEmptyScreens();
            this.mStripScreensOnPageStopMoving = false;
        }
    }

    @Override // com.voxel.launcher3.PagedView, android.view.ViewGroup
    protected boolean onRequestFocusInDescendants(int i, Rect rect) {
        if (this.mLauncher.isAllAppsVisible()) {
            return false;
        }
        Folder openFolder = this.mLauncher.getOpenFolder();
        return openFolder != null ? openFolder.requestFocus(i, rect) : super.onRequestFocusInDescendants(i, rect);
    }

    @Override // android.view.View
    protected void onRestoreInstanceState(Parcelable parcelable) {
        super.onRestoreInstanceState(parcelable);
        Launcher.setScreen(this.mCurrentPage);
    }

    protected void onResume() {
        View.OnClickListener pageIndicatorClickListener;
        if (getPageIndicator() != null && (pageIndicatorClickListener = getPageIndicatorClickListener()) != null) {
            getPageIndicator().setOnClickListener(pageIndicatorClickListener);
        }
        sAccessibilityEnabled = ((AccessibilityManager) getContext().getSystemService("accessibility")).isEnabled();
        if (LauncherAppState.getInstance().hasWallpaperChangedSinceLastCheck()) {
            setWallpaperDimension();
        }
        this.mWallpaperIsLiveWallpaper = !this.mLauncher.canAccessWallpaper();
        this.mLastSetWallpaperOffsetSteps = 0.0f;
    }

    @Override // com.voxel.launcher3.PagedView
    protected void onStartReordering() {
        super.onStartReordering();
        showOutlines();
        disableLayoutTransitions();
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return (!workspaceInModalState() && isFinishedSwitchingState() && (workspaceInModalState() || indexOfChild(view) == this.mCurrentPage)) ? false : true;
    }

    public void onTransitionEnd() {
        this.mIsSwitchingState = false;
        updateChildrenLayersEnabled(false);
        showCustomContentIfNecessary();
    }

    public void onTransitionPrepare() {
        this.mIsSwitchingState = true;
        invalidate();
        updateChildrenLayersEnabled(false);
        hideCustomContentIfNecessary();
    }

    protected void onWallpaperTap(MotionEvent motionEvent) {
        int[] iArr = this.mTempCell;
        getLocationOnScreen(iArr);
        int actionIndex = motionEvent.getActionIndex();
        iArr[0] = iArr[0] + ((int) motionEvent.getX(actionIndex));
        iArr[1] = iArr[1] + ((int) motionEvent.getY(actionIndex));
        this.mWallpaperManager.sendWallpaperCommand(getWindowToken(), motionEvent.getAction() == 1 ? "android.wallpaper.tap" : "android.wallpaper.secondaryTap", iArr[0], iArr[1], 0, null);
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        this.mLauncher.onWindowVisibilityChanged(i);
    }

    @Override // com.voxel.launcher3.PagedView
    protected void overScroll(float f) {
        if (!((f < 0.0f && (!hasCustomContent() || this.mIsRtl)) || (f > 0.0f && !(hasCustomContent() && this.mIsRtl)))) {
            this.mOverScrollEffect = 0.0f;
        } else {
            dampedOverScroll(f);
            this.mOverScrollEffect = acceleratedOverFactor(f);
        }
    }

    public void prepareDragWithProvider(DragPreviewProvider dragPreviewProvider) {
        this.mOutlineProvider = dragPreviewProvider;
    }

    protected void reinflateWidgetsIfNecessary() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            CellLayout cellLayout = (CellLayout) getChildAt(i);
            ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
            int childCount2 = shortcutsAndWidgets.getChildCount();
            for (int i2 = 0; i2 < childCount2; i2++) {
                View childAt = shortcutsAndWidgets.getChildAt(i2);
                if (childAt != null && (childAt.getTag() instanceof LauncherAppWidgetInfo)) {
                    LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) childAt.getTag();
                    LauncherAppWidgetHostView launcherAppWidgetHostView = (LauncherAppWidgetHostView) launcherAppWidgetInfo.hostView;
                    if (launcherAppWidgetHostView != null && launcherAppWidgetHostView.isReinflateRequired()) {
                        this.mLauncher.removeAppWidget(launcherAppWidgetInfo);
                        cellLayout.removeView(launcherAppWidgetHostView);
                        this.mLauncher.bindAppWidget(launcherAppWidgetInfo);
                    }
                }
            }
        }
    }

    public void removeAbandonedPromise(String str, UserHandleCompat userHandleCompat) {
        HashSet<String> hashSet = new HashSet<>(1);
        hashSet.add(str);
        LauncherModel.deletePackageFromDatabase(this.mLauncher, str, userHandleCompat);
        removeItemsByPackageName(hashSet, userHandleCompat);
    }

    public void removeAllWorkspaceScreens() {
        disableLayoutTransitions();
        if (hasCustomContent()) {
            removeCustomContentPage();
        }
        removeAllViews();
        this.mScreenOrder.clear();
        this.mWorkspaceScreens.clear();
        this.mFirstLayout = true;
        enableLayoutTransitions();
    }

    public void removeCustomContentPage() {
        CellLayout screenWithId = getScreenWithId(-301L);
        if (screenWithId == null) {
            throw new RuntimeException("Expected custom content screen to exist");
        }
        this.mWorkspaceScreens.remove(-301L);
        this.mScreenOrder.remove((Object) (-301L));
        removeView(screenWithId);
        Launcher.CustomContentCallbacks customContentCallbacks = this.mCustomContentCallbacks;
        if (customContentCallbacks != null) {
            customContentCallbacks.onCustomContentScrollProgressChanged(0.0f, 0.0f);
            this.mCustomContentCallbacks.onHide();
        }
        this.mCustomContentCallbacks = null;
        this.mDefaultPage = this.mOriginalDefaultPage;
        if (this.mRestorePage != -1001) {
            this.mRestorePage--;
        } else {
            setCurrentPage(getCurrentPage() - 1);
        }
    }

    public void removeExtraEmptyScreen(boolean z, boolean z2) {
        removeExtraEmptyScreenDelayed(z, null, 0, z2);
    }

    public void removeExtraEmptyScreenDelayed(final boolean z, final Runnable runnable, int i, final boolean z2) {
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - removeExtraEmptyScreen()", true);
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
            return;
        }
        if (i > 0) {
            postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$yQT6_4m2v72zDFLqIPEiZILn-bk
                @Override // java.lang.Runnable
                public final void run() {
                    Workspace.this.removeExtraEmptyScreenDelayed(z, runnable, 0, z2);
                }
            }, i);
            return;
        }
        convertFinalScreenToEmptyScreenIfNecessary();
        if (!hasExtraEmptyScreen()) {
            if (z2) {
                stripEmptyScreens();
            }
            if (runnable != null) {
                runnable.run();
                return;
            }
            return;
        }
        if (getNextPage() != this.mScreenOrder.indexOf(-201L)) {
            fadeAndRemoveEmptyScreen(0, 150, runnable, z2);
        } else {
            snapToPage(getNextPage() - 1, 400);
            fadeAndRemoveEmptyScreen(400, 150, runnable, z2);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Deprecated
    void removeItemsByComponentName(final HashSet<ComponentName> hashSet, final UserHandleCompat userHandleCompat) {
        for (CellLayout cellLayout : getWorkspaceAndHotseatCellLayouts()) {
            ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
            final HashMap map = new HashMap();
            for (int i = 0; i < shortcutsAndWidgets.getChildCount(); i++) {
                View childAt = shortcutsAndWidgets.getChildAt(i);
                map.put((ItemInfo) childAt.getTag(), childAt);
            }
            final ArrayList<View> arrayList = new ArrayList();
            final HashMap<FolderInfo, ArrayList<ShortcutInfo>> map2 = new HashMap<>(); /* REWORK: jadx raw HashMap */
            LauncherModel.filterItemInfos(map.keySet(), new LauncherModel.ItemInfoFilter() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$lRUFBgTLJmx6HjIRF_UzDKa1VcI
                @Override // com.voxel.launcher3.LauncherModel.ItemInfoFilter
                public final boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
                    return Workspace.lambda$removeItemsByComponentName$19(hashSet, userHandleCompat, map2, arrayList, map, itemInfo, itemInfo2, componentName);
                }
            });
            for (FolderInfo folderInfo : map2.keySet()) {
                Iterator it = ((ArrayList) map2.get(folderInfo)).iterator();
                while (it.hasNext()) {
                    folderInfo.remove((ShortcutInfo) it.next());
                }
            }
            for (View view : arrayList) {
                cellLayout.removeViewInLayout(view);
                if (view instanceof DropTarget) {
                    this.mDragController.removeDropTarget((DropTarget) view);
                }
            }
            if (arrayList.size() > 0) {
                shortcutsAndWidgets.requestLayout();
                shortcutsAndWidgets.invalidate();
            }
        }
        stripEmptyScreens();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void removeItemsByMatcher(final ItemInfoMatcher itemInfoMatcher) {
        for (CellLayout cellLayout : getWorkspaceAndHotseatCellLayouts()) {
            ShortcutAndWidgetContainer shortcutsAndWidgets = cellLayout.getShortcutsAndWidgets();
            final HashMap map = new HashMap();
            for (int i = 0; i < shortcutsAndWidgets.getChildCount(); i++) {
                View childAt = shortcutsAndWidgets.getChildAt(i);
                map.put((ItemInfo) childAt.getTag(), childAt);
            }
            final ArrayList<View> arrayList = new ArrayList();
            final HashMap<FolderInfo, ArrayList<ShortcutInfo>> map2 = new HashMap<>(); /* REWORK: jadx raw HashMap */
            LauncherModel.filterItemInfos(map.keySet(), new LauncherModel.ItemInfoFilter() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$BFa00BaVFGg_oLPeVI1OwRFwi-0
                @Override // com.voxel.launcher3.LauncherModel.ItemInfoFilter
                public final boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
                    return Workspace.lambda$removeItemsByMatcher$17(itemInfoMatcher, map2, arrayList, map, itemInfo, itemInfo2, componentName);
                }
            });
            for (FolderInfo folderInfo : map2.keySet()) {
                Iterator it = ((ArrayList) map2.get(folderInfo)).iterator();
                while (it.hasNext()) {
                    folderInfo.remove((ShortcutInfo) it.next());
                }
            }
            for (View view : arrayList) {
                cellLayout.removeViewInLayout(view);
                if (view instanceof DropTarget) {
                    this.mDragController.removeDropTarget((DropTarget) view);
                }
            }
            if (arrayList.size() > 0) {
                shortcutsAndWidgets.requestLayout();
                shortcutsAndWidgets.invalidate();
            }
        }
        stripEmptyScreens();
    }

    @Deprecated
    void removeItemsByPackageName(HashSet<String> hashSet, final UserHandleCompat userHandleCompat) {
        final HashSet hashSet2 = new HashSet(hashSet);
        HashSet hashSet3 = new HashSet();
        final HashSet<ComponentName> hashSet4 = new HashSet<>();
        Iterator<CellLayout> it = getWorkspaceAndHotseatCellLayouts().iterator();
        while (it.hasNext()) {
            ShortcutAndWidgetContainer shortcutsAndWidgets = it.next().getShortcutsAndWidgets();
            int childCount = shortcutsAndWidgets.getChildCount();
            for (int i = 0; i < childCount; i++) {
                hashSet3.add((ItemInfo) shortcutsAndWidgets.getChildAt(i).getTag());
            }
        }
        LauncherModel.filterItemInfos(hashSet3, new LauncherModel.ItemInfoFilter() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$A6bLPOl7ElOjP3VxfcvG9yaCSds
            @Override // com.voxel.launcher3.LauncherModel.ItemInfoFilter
            public final boolean filterItem(ItemInfo itemInfo, ItemInfo itemInfo2, ComponentName componentName) {
                return Workspace.lambda$removeItemsByPackageName$18(hashSet2, userHandleCompat, hashSet4, itemInfo, itemInfo2, componentName);
            }
        });
        removeItemsByComponentName(hashSet4, userHandleCompat);
    }

    public void resetTransitionTransform(CellLayout cellLayout) {
        if (isSwitchingState()) {
            setScaleX(this.mCurrentScale);
            setScaleY(this.mCurrentScale);
        }
    }

    public void restoreInstanceStateForChild(int i) {
        if (this.mSavedStates != null) {
            this.mRestoredPages.add(Integer.valueOf(i));
            CellLayout cellLayout = (CellLayout) getChildAt(i);
            if (cellLayout != null) {
                cellLayout.restoreInstanceState(this.mSavedStates);
            }
        }
    }

    public void restoreInstanceStateForRemainingPages() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (!this.mRestoredPages.contains(Integer.valueOf(i))) {
                restoreInstanceStateForChild(i);
            }
        }
        this.mRestoredPages.clear();
        this.mSavedStates = null;
    }

    @Override // com.voxel.launcher3.PagedView
    protected void screenScrolled(int i) {
        boolean z = this.mIsRtl;
        super.screenScrolled(i);
        updatePageAlphaValues(i);
        updateStateForCustomContent(i);
        enableHwLayersOnVisiblePages();
        if (!(this.mOverScrollX < 0 || this.mOverScrollX > this.mMaxScrollX)) {
            if (!this.mOverscrollEffectSet || getChildCount() <= 0) {
                return;
            }
            this.mOverscrollEffectSet = false;
            ((CellLayout) getChildAt(0)).setOverScrollAmount(0.0f, false);
            ((CellLayout) getChildAt(getChildCount() - 1)).setOverScrollAmount(0.0f, false);
            return;
        }
        int childCount = getChildCount() - 1;
        boolean z2 = this.mOverScrollX < 0;
        if ((!z && z2) || (z && !z2)) {
            childCount = 0;
        }
        ((CellLayout) getChildAt(childCount)).setOverScrollAmount(Math.abs(Math.abs(this.mOverScrollEffect)), z2);
        this.mOverscrollEffectSet = true;
    }

    @Override // com.voxel.launcher3.PagedView, com.voxel.launcher3.dragndrop.DragScroller
    public void scrollLeft() {
        if (!workspaceInModalState() && !this.mIsSwitchingState) {
            super.scrollLeft();
        }
        Folder openFolder = this.mLauncher.getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override // com.voxel.launcher3.PagedView, com.voxel.launcher3.dragndrop.DragScroller
    public void scrollRight() {
        if (!workspaceInModalState() && !this.mIsSwitchingState) {
            super.scrollRight();
        }
        Folder openFolder = this.mLauncher.getOpenFolder();
        if (openFolder != null) {
            openFolder.completeDragExit();
        }
    }

    @Override // android.view.View
    public void setAlpha(float f) {
        for (int i = 0; i < getChildCount(); i++) {
            ((CellLayout) getChildAt(i)).setAlpha(f);
        }
    }

    public void setChildrenOutlineAlpha(float f) {
        this.mChildrenOutlineAlpha = f;
        for (int i = 0; i < getChildCount(); i++) {
            ((CellLayout) getChildAt(i)).setBackgroundAlpha(f);
        }
    }

    void setCurrentDragOverlappingLayout(CellLayout cellLayout) {
        CellLayout cellLayout2 = this.mDragOverlappingLayout;
        if (cellLayout2 != null) {
            cellLayout2.setIsDragOverlapping(false);
        }
        this.mDragOverlappingLayout = cellLayout;
        CellLayout cellLayout3 = this.mDragOverlappingLayout;
        if (cellLayout3 != null) {
            cellLayout3.setIsDragOverlapping(true);
        }
        invalidate();
    }

    void setCurrentDropLayout(CellLayout cellLayout) {
        CellLayout cellLayout2 = this.mDragTargetLayout;
        if (cellLayout2 != null) {
            cellLayout2.revertTempState();
            this.mDragTargetLayout.onDragExit();
        }
        this.mDragTargetLayout = cellLayout;
        CellLayout cellLayout3 = this.mDragTargetLayout;
        if (cellLayout3 != null) {
            cellLayout3.onDragEnter();
        }
        cleanupReorder(true);
        cleanupFolderCreation();
        setCurrentDropOverCell(-1, -1);
    }

    void setCurrentDropOverCell(int i, int i2) {
        if (i == this.mDragOverX && i2 == this.mDragOverY) {
            return;
        }
        this.mDragOverX = i;
        this.mDragOverY = i2;
        setDragMode(0);
    }

    void setDragMode(int i) {
        if (i != this.mDragMode) {
            if (i == 0) {
                cleanupAddToFolder();
                cleanupReorder(false);
                cleanupFolderCreation();
            } else if (i == 2) {
                cleanupReorder(true);
                cleanupFolderCreation();
            } else if (i == 1) {
                cleanupAddToFolder();
                cleanupReorder(true);
            } else if (i == 3) {
                cleanupAddToFolder();
                cleanupFolderCreation();
            }
            this.mDragMode = i;
        }
    }

    public void setFinalScrollForPageChange(int i) {
        CellLayout cellLayout = (CellLayout) getChildAt(i);
        if (cellLayout != null) {
            this.mSavedScrollX = getScrollX();
            this.mSavedTranslationX = cellLayout.getTranslationX();
            this.mSavedRotationY = cellLayout.getRotationY();
            setScrollX(getScrollForPage(i));
            cellLayout.setTranslationX(0.0f);
            cellLayout.setRotationY(0.0f);
        }
    }

    public void setFinalTransitionTransform(CellLayout cellLayout) {
        if (isSwitchingState()) {
            this.mCurrentScale = getScaleX();
            setScaleX(this.mNewScale);
            setScaleY(this.mNewScale);
        }
    }

    @Override // com.voxel.launcher3.Insettable
    public void setInsets(Rect rect) {
        if (getFitsSystemWindows()) {
            this.mInsets.set(rect);
            CellLayout screenWithId = getScreenWithId(-301L);
            if (screenWithId != null) {
                KeyEvent.Callback childAt = screenWithId.getShortcutsAndWidgets().getChildAt(0);
                if (childAt instanceof Insettable) {
                    ((Insettable) childAt).setInsets(this.mInsets);
                }
            }
        }
    }

    @Override // com.voxel.launcher3.Insettable
    public void setInsets(WindowInsets windowInsets) {
        setInsets(new Rect(windowInsets.getSystemWindowInsetLeft(), windowInsets.getSystemWindowInsetTop(), windowInsets.getSystemWindowInsetRight(), windowInsets.getSystemWindowInsetBottom()));
    }

    @Override // android.view.View
    public void setLayerType(int i, Paint paint) {
    }

    @Override // com.voxel.launcher3.PagedView
    protected void setPageHoveringOverDeleteDropTarget(int i, boolean z, boolean z2) {
        super.setPageHoveringOverDeleteDropTarget(i, z, z2);
        ((CellLayout) getChildAt(i)).setColor(z ? getDeleteDropTarget().getHoverColor() : 0);
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [com.voxel.launcher3.Workspace$3] */
    protected void setWallpaperDimension() {
        new AsyncTask<Void, Void, Void>() { // from class: com.voxel.launcher3.Workspace.3
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) {
                WallpaperUtils.suggestWallpaperDimension(Workspace.this.mLauncher.getResources(), Workspace.this.mLauncher.getSharedPreferences("com.voxel.launcher3.WallpaperCropActivity", 4), Workspace.this.mLauncher.getWindowManager(), Workspace.this.mWallpaperManager, Workspace.this.mLauncher.overrideWallpaperDimensions());
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
    }

    void setup(DragController dragController) {
        this.mSpringLoadedDragController = new SpringLoadedDragController(this.mLauncher);
        this.mDragController = dragController;
        if (getWindowToken() != null) {
            this.mDragController.setWindowToken(getWindowToken());
        }
        updateChildrenLayersEnabled(false);
    }

    @Override // com.voxel.launcher3.PagedView
    protected boolean shouldDrawChild(View view) {
        CellLayout cellLayout = (CellLayout) view;
        return super.shouldDrawChild(view) && (this.mIsSwitchingState || cellLayout.getShortcutsAndWidgets().getAlpha() > 0.0f || cellLayout.getBackgroundAlpha() > 0.0f);
    }

    boolean shouldVoiceButtonProxyBeVisible() {
        return !isOnOrMovingToCustomContent() && this.mState == State.NORMAL;
    }

    void showCustomContentIfNecessary() {
        if ((this.mState == State.NORMAL) && hasCustomContent()) {
            this.mWorkspaceScreens.get(-301L).setVisibility(0);
        }
    }

    void showOutlines() {
        if (workspaceInModalState() || this.mIsSwitchingState) {
            return;
        }
        ObjectAnimator objectAnimator = this.mChildrenOutlineFadeOutAnimation;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        ObjectAnimator objectAnimator2 = this.mChildrenOutlineFadeInAnimation;
        if (objectAnimator2 != null) {
            objectAnimator2.cancel();
        }
        this.mChildrenOutlineFadeInAnimation = LauncherAnimUtils.ofFloat(this, "childrenOutlineAlpha", 1.0f);
        this.mChildrenOutlineFadeInAnimation.setDuration(100L);
        this.mChildrenOutlineFadeInAnimation.start();
    }

    public void showOutlinesTemporarily() {
        if (this.mIsPageMoving || isTouchActive()) {
            return;
        }
        snapToPage(this.mCurrentPage);
    }

    protected void snapToPage(int i, int i2, Runnable runnable) {
        Runnable runnable2 = this.mDelayedSnapToPageRunnable;
        if (runnable2 != null) {
            runnable2.run();
        }
        this.mDelayedSnapToPageRunnable = runnable;
        snapToPage(i, i2);
    }

    protected void snapToPage(int i, Runnable runnable) {
        snapToPage(i, 950, runnable);
    }

    public void snapToScreenId(long j) {
        snapToScreenId(j, null);
    }

    protected void snapToScreenId(long j, Runnable runnable) {
        snapToPage(getPageIndexForScreenId(j), runnable);
    }

    void startDragFromWorkspace(CellLayout.CellInfo cellInfo, DragOptions dragOptions) {
        View view = cellInfo.cell;
        if (view.isInTouchMode()) {
            this.mDragInfo = cellInfo;
            view.setVisibility(4);
            ((CellLayout) view.getParent().getParent()).prepareChildForDrag(view);
            beginDragShared(view, this, dragOptions);
        }
    }

    public void stripEmptyScreens() {
        Launcher.addDumpLog("Launcher.Workspace", "11683562 - stripEmptyScreens()", true);
        if (this.mLauncher.isWorkspaceLoading()) {
            Launcher.addDumpLog("Launcher.Workspace", "    - workspace loading, skip", true);
            return;
        }
        if (isPageMoving()) {
            this.mStripScreensOnPageStopMoving = true;
            return;
        }
        int nextPage = getNextPage();
        ArrayList<Long> arrayList = new ArrayList();
        for (Long l : this.mWorkspaceScreens.keySet()) {
            CellLayout cellLayout = this.mWorkspaceScreens.get(l);
            if (l.longValue() >= 0 && cellLayout.getShortcutsAndWidgets().getChildCount() == 0) {
                arrayList.add(l);
            }
        }
        int iNumCustomPages = numCustomPages() + 1;
        int i = 0;
        for (Long l2 : arrayList) {
            Launcher.addDumpLog("Launcher.Workspace", "11683562 -   removing id: " + l2, true);
            CellLayout cellLayout2 = this.mWorkspaceScreens.get(l2);
            this.mWorkspaceScreens.remove(l2);
            this.mScreenOrder.remove(l2);
            if (getChildCount() > iNumCustomPages) {
                if (indexOfChild(cellLayout2) < nextPage) {
                    i++;
                }
                removeView(cellLayout2);
            } else {
                this.mRemoveEmptyScreenRunnable = null;
                this.mWorkspaceScreens.put(-201L, cellLayout2);
                this.mScreenOrder.add(-201L);
            }
        }
        if (!arrayList.isEmpty()) {
            this.mLauncher.getModel().updateWorkspaceScreenOrder(this.mLauncher, this.mScreenOrder);
        }
        if (i >= 0) {
            setCurrentPage(nextPage - i);
        }
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsFlingToDelete() {
        return true;
    }

    @Override // com.evie.common.dragndrop.DragSource
    public boolean supportsRemoveDropTarget() {
        return true;
    }

    public boolean transitionStateShouldAllowDrop() {
        return (!isSwitchingState() || this.mTransitionProgress > 0.5f) && (this.mState == State.NORMAL || this.mState == State.SPRING_LOADED);
    }

    void updateCustomContentVisibility() {
        int i = this.mState == State.NORMAL ? 0 : 4;
        if (hasCustomContent()) {
            this.mWorkspaceScreens.get(-301L).setVisibility(i);
        }
    }

    public void updateInteractionForState() {
        if (this.mState != State.NORMAL) {
            this.mLauncher.onInteractionBegin();
        } else {
            this.mLauncher.onInteractionEnd();
        }
    }

    public void updatePackageBadge(final String str, final UserHandleCompat userHandleCompat) {
        mapOverItems(true, new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$HMp2dJ5eU-DEPHYWm6oT5_s5zPk
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Workspace.this.lambda$updatePackageBadge$22( userHandleCompat, str, itemInfo, view, view2);
            }
        });
    }

    public void updatePackageState(ArrayList<PackageInstallerCompat.PackageInstallInfo> arrayList) {
        HashSet hashSet = new HashSet();
        for (final PackageInstallerCompat.PackageInstallInfo packageInstallInfo : arrayList) {
            mapOverItems(true, new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$_7BvzxrHdAlnkCy5mEuk802VOEk
                @Override // com.voxel.launcher3.Workspace.ItemOperator
                public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                    return Workspace.lambda$updatePackageState$23(packageInstallInfo, itemInfo, view, view2);
                }
            });
            if (packageInstallInfo.state == 0) {
                hashSet.add(packageInstallInfo.packageName);
            }
        }
        if (hashSet.isEmpty()) {
            return;
        }
        restorePendingWidgets(hashSet);
    }

    void updateShortcuts(ArrayList<ShortcutInfo> arrayList) {
        final HashSet hashSet = new HashSet(arrayList);
        mapOverItems(true, new ItemOperator() { // from class: com.voxel.launcher3.-$$Lambda$Workspace$8-iZmxi-dI1bR1CXww_-LXNgzvA
            @Override // com.voxel.launcher3.Workspace.ItemOperator
            public final boolean evaluate(ItemInfo itemInfo, View view, View view2) {
                return Workspace.this.lambda$updateShortcuts$20( hashSet, itemInfo, view, view2);
            }
        });
    }

    void updateShortcutsAndWidgets(ArrayList<AppInfo> arrayList) {
        /* REWORK: jadx raw HashMap */
        HashMap<UserHandleCompat, ArrayList<AppInfo>> map = new HashMap<>();
        for (AppInfo appInfo : arrayList) {
            ArrayList<AppInfo> arrayList2 = map.get(appInfo.user);
            if (arrayList2 == null) {
                arrayList2 = new ArrayList<>();
                map.put(appInfo.user, arrayList2);
            }
            arrayList2.add(appInfo);
        }
        for (Map.Entry<UserHandleCompat, ArrayList<AppInfo>> entry : map.entrySet()) {
            updateShortcutsAndWidgetsPerUser(entry.getValue(), entry.getKey());
        }
    }

    public void widgetsRestored(ArrayList<LauncherAppWidgetInfo> arrayList) {
        if (arrayList.isEmpty()) {
            return;
        }
        DeferredWidgetRefresh deferredWidgetRefresh = new DeferredWidgetRefresh(arrayList, this.mLauncher.getAppWidgetHost());
        LauncherAppWidgetInfo launcherAppWidgetInfo = arrayList.get(0);
        if ((launcherAppWidgetInfo.hasRestoreFlag(1) ? AppWidgetManagerCompat.getInstance(this.mLauncher).findProvider(launcherAppWidgetInfo.providerName, launcherAppWidgetInfo.user) : AppWidgetManagerCompat.getInstance(this.mLauncher).getAppWidgetInfo(launcherAppWidgetInfo.appWidgetId)) != null) {
            deferredWidgetRefresh.run();
            return;
        }
        for (LauncherAppWidgetInfo launcherAppWidgetInfo2 : arrayList) {
            if (launcherAppWidgetInfo2.hostView instanceof PendingAppWidgetHostView) {
                launcherAppWidgetInfo2.installProgress = 100;
                ((PendingAppWidgetHostView) launcherAppWidgetInfo2.hostView).applyState();
            }
        }
    }

    boolean willAddToExistingUserFolder(Object obj, CellLayout cellLayout, int[] iArr, float f) {
        if (f > calculateMaxDistanceForFolderCreation()) {
            return false;
        }
        View childAt = cellLayout.getChildAt(iArr[0], iArr[1]);
        if (childAt != null) {
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) childAt.getLayoutParams();
            if (layoutParams.useTmpCoords && (layoutParams.tmpCellX != layoutParams.cellX || layoutParams.tmpCellY != layoutParams.tmpCellY)) {
                return false;
            }
        }
        if (childAt instanceof FolderIcon) {
            return ((FolderIcon) childAt).acceptDrop(obj);
        }
        return false;
    }

    boolean willCreateUserFolder(ItemInfo itemInfo, CellLayout cellLayout, int[] iArr, float f, boolean z) {
        if (f > calculateMaxDistanceForFolderCreation()) {
            return false;
        }
        View childAt = cellLayout.getChildAt(iArr[0], iArr[1]);
        if (childAt != null) {
            CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) childAt.getLayoutParams();
            if (layoutParams.useTmpCoords && (layoutParams.tmpCellX != layoutParams.cellX || layoutParams.tmpCellY != layoutParams.tmpCellY)) {
                return false;
            }
        }
        CellLayout.CellInfo cellInfo = this.mDragInfo;
        boolean z2 = cellInfo != null && childAt == cellInfo.cell;
        if (childAt == null || z2 || (z && !this.mCreateUserFolderOnDrop)) {
            return false;
        }
        return (childAt.getTag() instanceof ShortcutInfo) && (itemInfo.itemType == 0 || itemInfo.itemType == 1 || itemInfo.itemType == 6);
    }

    public boolean workspaceInModalState() {
        return this.mState != State.NORMAL;
    }
}
