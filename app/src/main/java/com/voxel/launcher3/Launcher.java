package com.voxel.launcher3;

/* REWORK: minSdk-safe receiver registration (API 34+ flag rule) */
import androidx.core.content.ContextCompat;
import timber.log.Timber;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.os.SystemClock;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Advanceable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import butterknife.ButterKnife;
import com.evie.common.dragndrop.DragSink;
import com.evie.common.dragndrop.DragSource;
import com.evie.common.dragndrop.ItemInfoCommon;
import com.evie.common.services.links.Link;
import com.evie.models.frequentlyused.FrequentlyUsedModel;
import com.evie.search.SearchPresenter;
import com.evie.search.local.LocalSearchManager;
import com.facebook.rebound.SpringConfig;
import com.voxel.launcher3.compat.AppWidgetManagerCompat;
import com.voxel.launcher3.compat.LauncherAppsCompat;
import com.voxel.launcher3.compat.PackageInstallerCompat;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.compat.UserManagerCompat;
import com.voxel.launcher3.dragndrop.DragController;
import com.voxel.launcher3.dragndrop.DragLayer;
import com.voxel.launcher3.dragndrop.DragOptions;
import com.voxel.launcher3.dragndrop.DragView;
import com.ovacoda.devie.images.IconLoader;
import com.ovacoda.devie.images.IconCacheCleaner;
import com.voxel.launcher3.shortcuts.DeepShortcutManager;
import com.voxel.launcher3.shortcuts.ShortcutKey;
import com.voxel.launcher3.util.ItemInfoMatcher;
import com.voxel.launcher3.util.LongArrayMap;
import com.voxel.launcher3.widget.PendingItemPreviewProvider;
import com.voxel.launcher3.widget.WidgetHostViewLoader;
import com.voxel.recyclerview.widget.DefaultItemAnimator;
import com.voxel.recyclerview.widget.LinearLayoutManager;
import com.voxel.simplesearchlauncher.activity.PermissionHelper;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.allapps.AllAppsFragment;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment;

import com.voxel.simplesearchlauncher.fragment.FolderFragment;
import com.voxel.simplesearchlauncher.fragment.LauncherRestoredFragment;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.voxel.simplesearchlauncher.fragment.ResetLauncherFailureFragment;
import com.voxel.simplesearchlauncher.fragment.ResetLauncherPromptFragment;
import com.voxel.simplesearchlauncher.gestures.CustomGestureListener;
import com.voxel.simplesearchlauncher.gestures.CustomGestureManager;
import com.voxel.simplesearchlauncher.gestures.GestureType;
import com.voxel.simplesearchlauncher.gestures.action.CustomGestureAction;
import com.voxel.simplesearchlauncher.hideapps.HideAppsModel;
import com.voxel.simplesearchlauncher.lock.LockOverlayFragment;
import com.voxel.simplesearchlauncher.model.itemdata.LocalAppData;
import com.voxel.simplesearchlauncher.model.managers.BackupRestoreManager;
import com.voxel.simplesearchlauncher.model.managers.LocalAppsManager;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.model.managers.UserProfileManager;
import com.voxel.simplesearchlauncher.notification.NotificationController;
import com.voxel.simplesearchlauncher.popup.PopupFragment;
import com.voxel.simplesearchlauncher.search.SearchUiHelper;
import com.voxel.simplesearchlauncher.touch.SpringyMotionTracker;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.voxel.simplesearchlauncher.utils.Blur;
import com.voxel.simplesearchlauncher.utils.CombinedFactorHelper;
import com.voxel.simplesearchlauncher.utils.FirstFrameAnimatorHelper;
import com.voxel.simplesearchlauncher.utils.LaunchUtil;
import com.voxel.simplesearchlauncher.utils.UpgradeUtil;
import com.voxel.simplesearchlauncher.utils.alpha.AlphaSet;
import com.voxel.simplesearchlauncher.utils.alpha.AlphaSetAnimator;
import com.voxel.simplesearchlauncher.view.CenterCropOffsetImageView;
import com.voxel.simplesearchlauncher.view.HotseatImageView;
import com.voxel.simplesearchlauncher.view.NoItemClickRecyclerView;
import com.voxel.simplesearchlauncher.view.PullDownRecyclerView;
import com.voxel.simplesearchlauncher.view.SpringySearchView;
import com.voxel.simplesearchlauncher.view.WindowInsetPolicyFrameLayout;
import com.voxel.simplesearchlauncher.view.launcher.IndicatorContainer;
import com.voxel.simplesearchlauncher.widget.LauncherAppWidgetProviderInfo;
import com.voxel.simplesearchlauncher.widget.PendingAddWidgetInfo;
import com.voxel.simplesearchlauncher.widget.WidgetsFragment;
import dagger.android.AndroidInjection;
import com.ovacoda.devie.BuildConfig;
import com.ovacoda.devie.R;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/* JADX INFO: loaded from: classes.dex */
public class Launcher extends FragmentActivity implements DialogInterface.OnDismissListener, DialogInterface.OnShowListener, View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, LauncherModel.Callbacks, PagedView.PageSwitchListener, SafeActivityStarter, AllAppsFragment.OnFragmentInteractionListener, AppOptionsFragment.OnFragmentInteractionListener, FolderFragment.OnFolderFragmentInteractionListener, LauncherStackableFragment.OnFragmentInteractionListener, WidgetsFragment.OnFragmentInteractionListener {
    private static PendingAddArguments sPendingAddItem;
    private boolean isShowingOnboard;
    LocalAppsManager localAppsManager;

    CenterCropOffsetImageView mAllAppsBackground;
    private View mAllAppsButton;

    View mAllAppsContainer;
    private AllAppsFragment mAllAppsFragment;
    private SpringyMotionTracker mAllAppsMotionTracker;

    ImageView mAllAppsUpChevron;
    AnalyticsHandler mAnalyticsHandler;
    private LauncherAppWidgetHost mAppWidgetHost;
    private AppWidgetManagerCompat mAppWidgetManager;
    private long mAutoAdvanceSentTime;
    private BackgroundFolderAnimationHelper mBackgroundFolderAnimationHelper;
    private GestureDetectorCompat mBlurGestureDetector;

    CenterCropOffsetImageView mBlurImageView;

    FrameLayout mBlurLayoutView;
    private View.OnTouchListener mBlurOnTouchListener;
    private Handler mCleanupGoogleNowBackgroundHandler;
    private GestureDetectorCompat mCustomGestureDetector;
    private CustomGestureListener mCustomGestureListener;
    private DevicePolicyManager mDPM;
    DeepShortcutManager mDeepShortcutManager;
    private ComponentName mDeviceAdminComponentName;
    private DragController mDragController;

    DragLayer mDragLayer;

    FocusIndicatorView mFocusHandler;
    private FolderInfo mFolderInfo;
    private FragmentHelper mFragmentHelper;
    FrequentlyUsedModel mFrequentlyUsedModel;

    FrameLayout mGoogleNowAnimBackground;
    private int mGoogleNowAnimFinalIconSize;

    ImageView mGoogleNowAnimIcon;
    private SpringyMotionTracker mGoogleNowLaunchMotionTracker;
    private boolean mGoogleNowLaunchTriggered;
    private View.OnTouchListener mHapticFeedbackTouchListener;
    HideAppsModel mHideAppsModel;

    Hotseat mHotseat;
    private AlphaSet mHotseatAlpha;

    HotseatImageView mHotseatBg;
    private IconCache mIconCache;
    private AlphaSet mIndicatorAlpha;

    IndicatorContainer mIndicatorContainer;
    private LayoutInflater mInflater;
    private boolean mIsGoogleNowAnimLocked;
    private boolean mIsLiveWallpaper;
    private boolean mIsSafeModeEnabled;

    CoordinatorLayout mLauncherView;
    private LocalSearchManager mLocalSearchManager;

    WindowInsetPolicyFrameLayout mMainLayer;

    CenterCropOffsetImageView mMainLayerBackground;

    ImageView mMainLayerDarken;
    private AlphaSet mMainLayerDarkenAlpha;
    private MainLayerGestureListener mMainLayerGestureListener;
    private View.OnLayoutChangeListener mMainLayerMiddlePivotLayoutChangeListener;
    private LauncherModel mModel;
    private boolean mOnResumeNeedsLoad;

    ViewGroup mOverviewPanel;
    private AlphaSet mOverviewPanelAlpha;

    View mPageIndicators;
    private LauncherAppWidgetProviderInfo mPendingAddWidgetInfo;
    private UserProfileManager.LauncherDefaultState mPreviousDefaultState;

    PullDownRecyclerView mPullDownRecyclerView;

    View mQsb;

    SpringySearchView mQsbSearchView;

    EditText mQsbSearchViewEditText;
    private GestureDetectorCompat mQsbSearchViewGestureDetector;
    private CombinedFactorHelper mQsbSearchViewScroller;
    private CombinedFactorHelper mQsbSearchViewScrollerModifier;
    private AsyncTask mRefreshBlurredImageTask;
    private boolean mRestoring;
    private Bundle mSavedState;
    private ScaleGestureDetector mScaleCustomGestureDetector;
    private int mScreenHeight;

    SearchDropTargetBar mSearchDropTargetBar;
    private AlphaSet mSearchDropTargetBarAlpha;
    private SearchItemShortcutHandler mSearchItemShortcutHandler;

    View mSearchLayout;
    private SearchPresenter mSearchPresenter;
    private GestureDetectorCompat mSearchResultListCloseKeyboardGestureDetector;
    private SearchUiHelper mSearchUiHelper;
    private SettingsStorage mSettingsStorage;
    private SharedPreferences mSharedPrefs;
    private AnimatorSet mStateAnimation;
    private boolean mWaitingForResult;
    private BubbleTextView mWaitingForResume;
    private BroadcastReceiver mWallpaperBroadcastReceiver;

    Workspace mWorkspace;
    private AlphaSet mWorkspaceAlpha;

    Drawable mWorkspaceBackgroundDrawable;
    private boolean sendHomeAnalyticsEventOnResume;
    private static final Object sLock = new Object();
    private static int sScreen = 2;
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
    private static LocaleConfiguration sLocaleConfiguration = null;
    private static LongArrayMap<FolderInfo> sFolders = new LongArrayMap<>();
    static final ArrayList<String> sDumpLogs = new ArrayList<>();
    static Date sDateStamp = new Date();
    static DateFormat sDateFormat = DateFormat.getDateTimeInstance(3, 3);
    static long sRunStart = System.currentTimeMillis();
    private static ArrayList<ComponentName> mIntentsOnWorkspaceFromUpgradePath = null;
    private static PermissionHelper sPermissionHelper = new PermissionHelper(13);
    public static boolean sForceEnableRotation = isPropertyEnabled("launcher_force_rotate");
    private State mState = State.WORKSPACE;
    private HashMap<Integer, Integer> mItemIdToViewId = new HashMap<>();
    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
    private final ContentObserver mWidgetObserver = new AppWidgetResetObserver();
    private ItemInfo mPendingAddInfo = new ItemInfo();
    private int mPendingAddWidgetId = -1;
    private int[] mTmpAddItemCellCoordinates = new int[2];
    private boolean mAutoAdvanceRunning = false;
    private List<DialogInterface> mShownDialogs = new ArrayList();
    private List<FolderFragment> mFolderFragments = new ArrayList();
    private boolean isStopped = true;
    private CombinedFactorHelper mBlurImageAlphaHelper = new CombinedFactorHelper();
    private State mOnResumeState = State.NONE;
    private SpannableStringBuilder mDefaultKeySsb = null;
    private boolean mWorkspaceLoading = true;
    private boolean mPaused = true;
    private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<>();
    private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<>();
    private boolean mUserPresent = true;
    private boolean mVisible = false;
    private boolean mHasFocus = false;
    private boolean mAttached = false;
    private final int ADVANCE_MSG = 1;
    private final int mAdvanceInterval = 20000;
    private final int mAdvanceStagger = 250;
    private long mAutoAdvanceTimeLeft = -1;
    private HashMap<View, AppWidgetProviderInfo> mWidgetsToAdvance = new HashMap<>();
    private final ArrayList<Integer> mSynchronouslyBoundPages = new ArrayList<>();
    private Rect mRectForFolderAnimation = new Rect();
    private Runnable mBuildLayersRunnable = new Runnable() { // from class: com.voxel.launcher3.Launcher.1
        @Override // java.lang.Runnable
        public void run() {
            if (Launcher.this.mWorkspace != null) {
                Launcher.this.mWorkspace.buildPageHardwareLayers();
            }
        }
    };
    private Runnable mForgetWallpaperRunnable = new Runnable() { // from class: com.voxel.launcher3.Launcher.2
        @Override // java.lang.Runnable
        public void run() {
            if (Launcher.this.mMainLayerBackground != null) {
                Launcher.this.mMainLayerBackground.setImageDrawable(null);
            }
            WallpaperManager.getInstance(Launcher.this).forgetLoadedWallpaper();
        }
    };
    private boolean isLaunchingVoiceAssist = false;
    private SpringyMotionTracker.OnFactorChangeListener mAllAppsFactorChangeListener = new SpringyMotionTracker.OnFactorChangeListener() { // from class: com.voxel.launcher3.Launcher.16
        @Override // com.voxel.simplesearchlauncher.touch.SpringyMotionTracker.OnFactorChangeListener
        public void onFactorBeginChanging() {
            Launcher.this.mAllAppsContainer.setLayerType(2, null);
            if (Launcher.this.mIsLiveWallpaper) {
                Launcher.this.mWorkspace.onTransitionPrepare();
                Launcher.this.mHotseatAlpha.setLayerType(2, null);
                Launcher.this.mSearchDropTargetBarAlpha.setLayerType(2, null);
                Launcher.this.mIndicatorAlpha.setLayerType(2, null);
            }
        }

        @Override // com.voxel.simplesearchlauncher.touch.SpringyMotionTracker.OnFactorChangeListener
        public void onFactorChanged(float f) {
            Launcher.this.mAllAppsContainer.setY(Launcher.this.mAllAppsMotionTracker.getFactorDistance() * f);
            float f2 = 1.0f - f;
            Launcher.this.mAllAppsContainer.setAlpha(AlphaUtil.limitAlpha(f2));
            float fLimitAlpha = AlphaUtil.limitAlpha(f2 * 1.5f);
            float f3 = Launcher.this.mIsLiveWallpaper ? 1.0f - fLimitAlpha : 1.0f;
            if (Launcher.this.mWorkspaceAlpha != null) {
                Launcher.this.mWorkspaceAlpha.putValue(this, f3);
            }
            if (Launcher.this.mHotseatAlpha != null) {
                Launcher.this.mHotseatAlpha.putValue(this, f3);
            }
            if (Launcher.this.mIndicatorAlpha != null) {
                Launcher.this.mIndicatorAlpha.putValue(this, f3);
            }
            if (Launcher.this.mSearchDropTargetBarAlpha != null) {
                Launcher.this.mSearchDropTargetBarAlpha.putValue(this, f3);
            }
            float factorDistance = (f - 1.0f) * Launcher.this.mAllAppsMotionTracker.getFactorDistance() * 0.25f;
            if (Launcher.this.mWorkspace != null) {
                Launcher.this.mWorkspace.setTranslationY(factorDistance);
            }
            if (Launcher.this.mHotseat != null) {
                Launcher.this.mHotseat.setTranslationY(factorDistance);
            }
            if (Launcher.this.mHotseatBg != null) {
                Launcher.this.mHotseatBg.setTranslationY(factorDistance);
            }
            if (Launcher.this.mIndicatorContainer != null) {
                Launcher.this.mIndicatorContainer.setTranslationY(factorDistance);
            }
            if (Launcher.this.mQsbSearchView != null) {
                Launcher.this.mQsbSearchViewScroller.put(this, factorDistance);
            }
            int i = fLimitAlpha >= 1.0f ? 4 : 0;
            if (Launcher.this.mWorkspace != null) {
                Launcher.this.mWorkspace.setVisibility(i);
            }
            if (Launcher.this.mHotseat != null) {
                Launcher.this.mHotseat.setVisibility(i);
            }
            if (Launcher.this.mHotseatBg != null) {
                Launcher.this.mHotseatBg.setVisibility(i);
            }
            if (Launcher.this.mIndicatorContainer != null) {
                Launcher.this.mIndicatorContainer.setVisibility(i);
            }
            if (Launcher.this.mSearchDropTargetBar != null) {
                Launcher.this.mSearchDropTargetBar.setVisibility(i);
            }
            if (Launcher.this.mAllAppsBackground != null) {
                Launcher.this.mAllAppsBackground.setImageAlpha(AlphaUtil.floatToIntAlpha(fLimitAlpha));
            }
            if (Launcher.this.mAllAppsFragment != null) {
                if (f == 0.0f) {
                    Launcher.this.mAllAppsFragment.onFocusChanged(Launcher.this.mHandler, true);
                } else if (f == 1.0f) {
                    Launcher.this.mAllAppsFragment.onFocusChanged(Launcher.this.mHandler, false);
                }
            }
        }

        @Override // com.voxel.simplesearchlauncher.touch.SpringyMotionTracker.OnFactorChangeListener
        public void onFactorEndChanging() {
            if (Launcher.this.mAllAppsContainer != null) {
                Launcher.this.mAllAppsContainer.setLayerType(0, null);
            }
            if (Launcher.this.mWorkspace != null) {
                Launcher.this.mWorkspace.onTransitionEnd();
            }
            if (Launcher.this.mHotseatAlpha != null) {
                Launcher.this.mHotseatAlpha.setLayerType(0, null);
            }
            if (Launcher.this.mSearchDropTargetBarAlpha != null) {
                Launcher.this.mSearchDropTargetBarAlpha.setLayerType(0, null);
            }
            if (Launcher.this.mIndicatorAlpha != null) {
                Launcher.this.mIndicatorAlpha.setLayerType(0, null);
            }
        }
    };
    private SpringyMotionTracker.OnFactorChangeListener mGoogleNowLaunchFactorChangeListener = new SpringyMotionTracker.OnFactorChangeListener() { // from class: com.voxel.launcher3.Launcher.17
        @Override // com.voxel.simplesearchlauncher.touch.SpringyMotionTracker.OnFactorChangeListener
        public void onFactorBeginChanging() {
            Launcher.this.setupGoogleNowLaunchBackground();
        }

        @Override // com.voxel.simplesearchlauncher.touch.SpringyMotionTracker.OnFactorChangeListener
        public void onFactorChanged(float f) {
            if (Launcher.this.mIsGoogleNowAnimLocked) {
                return;
            }
            Launcher.this.mGoogleNowAnimBackground.setY(Launcher.this.mGoogleNowLaunchMotionTracker.getFactorDistance() * f);
            Launcher.this.updateGoogleNowBackgroundIcon();
            Launcher.this.setGoogleBlurAlpha(AlphaUtil.limitAlpha((1.0f - f) * 3.0f));
            Float endValueIfAvailable = Launcher.this.mGoogleNowLaunchMotionTracker.getEndValueIfAvailable();
            if (!Launcher.this.mGoogleNowLaunchTriggered && endValueIfAvailable != null && endValueIfAvailable.floatValue() == 0.0d) {
                Launcher.this.launchGoogleNowActivity(true);
                Launcher.this.mGoogleNowLaunchTriggered = true;
            }
            Launcher.this.mGoogleNowAnimBackground.setVisibility(0);
        }

        @Override // com.voxel.simplesearchlauncher.touch.SpringyMotionTracker.OnFactorChangeListener
        public void onFactorEndChanging() {
            if (Launcher.this.mIsGoogleNowAnimLocked) {
                return;
            }
            Launcher.this.mIsGoogleNowAnimLocked = true;
            Launcher.this.mGoogleNowLaunchTriggered = false;
            if (Launcher.this.mGoogleNowLaunchMotionTracker.getCurrentFactor() == 0.0f) {
                Launcher.this.scheduleGoogleNowBackgroundCleanup(3000L);
            } else {
                Launcher.this.scheduleGoogleNowBackgroundCleanup(0L);
            }
        }
    };
    private Runnable mCleanupGoogleNowBackgroundRunnable = new Runnable() { // from class: com.voxel.launcher3.Launcher.18
        @Override // java.lang.Runnable
        public void run() {
            if (Launcher.this.mGoogleNowAnimBackground != null) {
                Launcher.this.cleanupGoogleNowLaunchBackground();
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() { // from class: com.voxel.launcher3.Launcher.21
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                Launcher.this.mUserPresent = false;
                Launcher.this.mDragLayer.clearAllResizeFrames();
                Launcher.this.updateRunning();
                if (Launcher.this.mPendingAddInfo.container == -1) {
                    Launcher.this.changeAllAppsVisibility(false, false);
                    Launcher.this.showWorkspace(false);
                }
                Launcher.this.mSettingsStorage.setDesktopUnlockedTemporarily(false);
                return;
            }
            if ("android.intent.action.USER_PRESENT".equals(action)) {
                Launcher.this.mUserPresent = true;
                Launcher.this.updateRunning();
            } else if ("android.intent.action.MANAGED_PROFILE_ADDED".equals(action) || "android.intent.action.MANAGED_PROFILE_REMOVED".equals(action)) {
                Launcher.this.getModel().forceReload();
            }
        }
    };
    private final Handler mHandler = new Handler() { // from class: com.voxel.launcher3.Launcher.23
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message.what == 1) {
                int i = 0;
                for (View view : Launcher.this.mWidgetsToAdvance.keySet()) {
                    KeyEvent.Callback callbackFindViewById = view.findViewById(((AppWidgetProviderInfo) Launcher.this.mWidgetsToAdvance.get(view)).autoAdvanceViewId);
                    int i2 = i * 250;
                    if (callbackFindViewById instanceof Advanceable) {
                        final Advanceable advanceable = (Advanceable) callbackFindViewById;
                        advanceable.getClass();
                        postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$McLEzzcuDpNLGPEbi7yalJwL7eo
                            @Override // java.lang.Runnable
                            public final void run() {
                                advanceable.advance();
                            }
                        }, i2);
                    }
                    i++;
                }
                Launcher.this.sendAdvanceMessage(20000L);
            }
        }
    };

    /* JADX INFO: renamed from: com.voxel.launcher3.Launcher$1BlurredImageTaskResult, reason: invalid class name */
    class C1BlurredImageTaskResult {
        Bitmap bitmap;
        boolean isLiveWallpaper;

        C1BlurredImageTaskResult() {
        }
    }

    /* JADX INFO: renamed from: com.voxel.launcher3.Launcher$22, reason: invalid class name */
    class AnonymousClass22 implements ViewTreeObserver.OnDrawListener {
        private boolean mStarted = false;

        AnonymousClass22() {
        }

        public void lambda$onDraw$0() {
            if (Launcher.this.mWorkspace == null || Launcher.this.mWorkspace.getViewTreeObserver() == null) {
                return;
            }
            // REWORK: the dex lambda captured only `this`; it removes THIS listener.
            Launcher.this.mWorkspace.getViewTreeObserver().removeOnDrawListener(this);
        }

        @Override // android.view.ViewTreeObserver.OnDrawListener
        public void onDraw() {
            if (this.mStarted) {
                return;
            }
            this.mStarted = true;
            Launcher.this.mWorkspace.postDelayed(Launcher.this.mBuildLayersRunnable, 500L);
            Launcher.this.mWorkspace.post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$22$R6qAcwXG0PhQC5Pcsh1tRNjSz_0
                @Override // java.lang.Runnable
                public final void run() {
                    AnonymousClass22.this.lambda$onDraw$0();
                }
            });
        }
    }

    /* JADX INFO: renamed from: com.voxel.launcher3.Launcher$26, reason: invalid class name */
    class AnonymousClass26 implements Animator.AnimatorListener {
        AnonymousClass26() {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animator) {
            if (Launcher.this.mLauncherView != null) {
                Launcher.this.mLauncherView.postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$26$73DZX3zfBztDdSjSwHVFAMXGKs8
                    @Override // java.lang.Runnable
                    public final void run() {
                        Launcher.this.showFirstRunClings();
                    }
                }, 700L);
            }
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animator) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animator) {
        }
    }

    private class AppWidgetResetObserver extends ContentObserver {
        public AppWidgetResetObserver() {
            super(new Handler());
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z) {
            Launcher.this.onAppWidgetReset();
        }
    }

    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
        private CloseSystemDialogsIntentReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            Launcher.this.closeSystemDialogs();
        }
    }

    public interface CustomContentCallbacks {
        boolean isScrollingAllowed();

        void onCustomContentScrollProgressChanged(float f, float f2);

        void onHide();

        void onShow(boolean z);

        boolean shouldIncludeInWallpaperScroll();
    }

    private static class LocaleConfiguration {
        public String locale;
        public int mcc;
        public int mnc;

        private LocaleConfiguration() {
            this.mcc = -1;
            this.mnc = -1;
        }
    }

    private class MainLayerGestureListener extends GestureDetector.SimpleOnGestureListener implements GestureDetector.OnDoubleTapListener {
        boolean allAppsLocked;
        boolean disallowTouches;
        int edgeSlop;
        private boolean isDoubleTapping;
        boolean searchLocked;
        boolean touchDownAllAppsScrollBar;
        boolean touchDownScrollingWidget;
        boolean intercepting = false;
        boolean hasCalculatedSlope = false;
        boolean horizontal = false;
        boolean swipeUp = false;
        int[] tmpXy = new int[2];

        MainLayerGestureListener(Context context) {
            this.edgeSlop = ViewConfiguration.get(context).getScaledEdgeSlop();
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnDoubleTapListener
        public boolean onDoubleTap(MotionEvent motionEvent) {
            if (Launcher.this.mMainLayerGestureListener.searchLocked || Launcher.this.mMainLayerGestureListener.allAppsLocked || Launcher.this.mMainLayerGestureListener.disallowTouches || Launcher.this.isOnCustomContent() || !(Launcher.this.mState == State.WORKSPACE || Launcher.this.mWorkspace.getState() == Workspace.State.NORMAL)) {
                return false;
            }
            this.isDoubleTapping = true;
            Launcher.this.mCustomGestureListener.onDoubleTap(motionEvent);
            return false;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onDown(MotionEvent motionEvent) {
            this.hasCalculatedSlope = false;
            this.horizontal = false;
            this.swipeUp = false;
            this.disallowTouches = false;
            this.intercepting = false;
            this.touchDownScrollingWidget = false;
            this.searchLocked = Launcher.this.mQsbSearchView.getCurrentFactor() <= 0.0f || (Launcher.this.mQsbSearchView.getEndValueIfAvailable() != null && Launcher.this.mQsbSearchView.getEndValueIfAvailable().floatValue() <= 0.0f);
            this.allAppsLocked = Launcher.this.mAllAppsMotionTracker.getCurrentFactor() <= 0.0f || (Launcher.this.mAllAppsMotionTracker.getEndValueIfAvailable() != null && Launcher.this.mAllAppsMotionTracker.getEndValueIfAvailable().floatValue() <= 0.0f);
            if (this.isDoubleTapping) {
                this.disallowTouches = true;
            }
            this.isDoubleTapping = false;
            if (this.allAppsLocked) {
                int[] iArr = this.tmpXy;
                iArr[0] = 0;
                iArr[1] = 0;
                Utilities.getDescendantCoordRelativeToParent(Launcher.this.mAllAppsFragment.getRecyclerView(), Launcher.this.mLauncherView, this.tmpXy, false);
                this.touchDownAllAppsScrollBar = Launcher.this.mAllAppsFragment.isNearIndexBar(((int) motionEvent.getX()) - this.tmpXy[0], ((int) motionEvent.getY()) - this.tmpXy[1]);
            } else {
                this.touchDownAllAppsScrollBar = false;
            }
            Float endValueIfAvailable = Launcher.this.mQsbSearchView.getEndValueIfAvailable();
            if (Launcher.this.mQsbSearchView.getCurrentFactor() <= 0.0f || (endValueIfAvailable != null && endValueIfAvailable.floatValue() <= 0.0f)) {
                if (Launcher.this.mSearchUiHelper == null || Launcher.this.mSearchUiHelper.getSearchText().isEmpty()) {
                    this.horizontal = false;
                    this.hasCalculatedSlope = true;
                } else {
                    this.disallowTouches = true;
                }
            }
            if (Launcher.this.mWorkspace.isInOverviewMode()) {
                this.disallowTouches = true;
            }
            return false;
        }

        @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (Launcher.this.mWorkspace.isInOverviewMode()) {
                this.disallowTouches = true;
            }
            if (this.disallowTouches) {
                return false;
            }
            if (this.intercepting) {
                return true;
            }
            if (Launcher.this.mLauncherView != null) {
                if (!this.hasCalculatedSlope) {
                    float x = motionEvent2.getX() - motionEvent.getX();
                    float y = motionEvent2.getY() - motionEvent.getY();
                    if (Math.abs(x == 0.0f ? Float.MAX_VALUE : y / x) <= 1.0f && motionEvent2.getPointerCount() <= 1) {
                        this.horizontal = true;
                    } else {
                        this.horizontal = false;
                    }
                    if (y < 0.0f) {
                        this.swipeUp = true;
                    }
                    this.hasCalculatedSlope = true;
                }
                if (!this.horizontal) {
                    if (this.touchDownScrollingWidget || this.touchDownAllAppsScrollBar || Launcher.this.isOnCustomContent()) {
                        this.intercepting = false;
                        this.disallowTouches = true;
                    } else {
                        this.intercepting = true;
                        if (this.allAppsLocked) {
                            if (this.swipeUp) {
                                this.intercepting = false;
                            } else if (Launcher.this.mAllAppsFragment != null && Launcher.this.mAllAppsFragment.getRecyclerView() != null && Launcher.this.mAllAppsFragment.getRecyclerView().computeVerticalScrollOffset() > 0) {
                                this.intercepting = false;
                            }
                        }
                    }
                }
            }
            return this.intercepting;
        }
    }

    private static class PendingAddArguments {
        int appWidgetId;
        int cellX;
        int cellY;
        long container;
        Intent intent;
        int requestCode;
        long screenId;

        private PendingAddArguments() {
        }
    }

    public enum SearchInteractionMethod {
        PULL_DOWN,
        HOME,
        TAP,
        ASSIST,
        BACK,
        NEG_SPACE
    }

    private enum State {
        NONE,
        WORKSPACE,
        APPS_CUSTOMIZE,
        APPS_CUSTOMIZE_SPRING_LOADED
    }

    private boolean acceptFilter() {
        return !((InputMethodManager) getSystemService("input_method")).isFullscreenMode();
    }

    public static void addDumpLog(String str, String str2, Exception exc, boolean z) {
        if (z) {
            if (exc != null) {
                Log.d(str, str2, exc);
            } else {
                Log.d(str, str2);
            }
        }
    }

    public static void addDumpLog(String str, String str2, boolean z) {
        addDumpLog(str, str2, null, z);
    }

    private boolean canRunNewAppsAnimation() {
        return System.currentTimeMillis() - this.mDragController.getLastGestureUpTime() > 5000;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void changeSearchMode(boolean z, boolean z2) {
        SpringySearchView springySearchView = this.mQsbSearchView;
        if (springySearchView == null) {
            return;
        }
        if (!z) {
            if (springySearchView.hasFocus()) {
                this.mQsbSearchView.clearFocus();
            }
            if (z2) {
                this.mQsbSearchView.setEndValue(1.0f);
            } else {
                this.mQsbSearchView.applyFactor(1.0f, false, 0.0f);
                setSearchBlurAlpha(0.0f);
            }
            this.mQsbSearchView.setQuery("", false);
            this.mBlurLayoutView.setOnTouchListener(null);
            return;
        }
        if (z2) {
            springySearchView.setEndValue(0.0f);
        } else {
            springySearchView.applyFactor(0.0f, false, 0.0f);
            setSearchBlurAlpha(1.0f);
        }
        if (!this.mQsbSearchView.hasFocus()) {
            this.mQsbSearchView.requestFocus();
        }
        this.mBlurLayoutView.setOnTouchListener(this.mBlurOnTouchListener);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService("input_method");
        inputMethodManager.viewClicked(this.mQsbSearchViewEditText);
        inputMethodManager.showSoftInput(this.mQsbSearchViewEditText, 1);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v7, types: [com.voxel.launcher3.Launcher$6] */
    /* JADX WARN: Type inference failed for: r2v6, types: [com.voxel.launcher3.Launcher$7] */
    public void checkForLocaleChange() {
        if (sLocaleConfiguration == null) {
            new AsyncTask<Void, Void, LocaleConfiguration>() { // from class: com.voxel.launcher3.Launcher.6
                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public LocaleConfiguration doInBackground(Void... voidArr) {
                    LocaleConfiguration localeConfiguration = new LocaleConfiguration();
                    Launcher.readConfiguration(Launcher.this, localeConfiguration);
                    return localeConfiguration;
                }

                /* JADX INFO: Access modifiers changed from: protected */
                @Override // android.os.AsyncTask
                public void onPostExecute(LocaleConfiguration localeConfiguration) {
                    LocaleConfiguration unused = Launcher.sLocaleConfiguration = localeConfiguration;
                    Launcher.this.checkForLocaleChange();
                }
            }.execute(new Void[0]);
            return;
        }
        Configuration configuration = getResources().getConfiguration();
        String str = sLocaleConfiguration.locale;
        String string = configuration.locale.toString();
        int i = sLocaleConfiguration.mcc;
        int i2 = configuration.mcc;
        int i3 = sLocaleConfiguration.mnc;
        int i4 = configuration.mnc;
        if ((string.equals(str) && i2 == i && i4 == i3) ? false : true) {
            LocaleConfiguration localeConfiguration = sLocaleConfiguration;
            localeConfiguration.locale = string;
            localeConfiguration.mcc = i2;
            localeConfiguration.mnc = i4;
            this.mIconCache.flush();
            final LocaleConfiguration localeConfiguration2 = sLocaleConfiguration;
            new AsyncTask<Void, Void, Void>() { // from class: com.voxel.launcher3.Launcher.7
                @Override // android.os.AsyncTask
                public Void doInBackground(Void... voidArr) {
                    Launcher.writeConfiguration(Launcher.this, localeConfiguration2);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }
    }


    /* JADX INFO: Access modifiers changed from: private */
    public void cleanupGoogleNowLaunchBackground() {
        this.mIsGoogleNowAnimLocked = false;
        Handler handler = this.mCleanupGoogleNowBackgroundHandler;
        if (handler != null) {
            handler.removeCallbacks(this.mCleanupGoogleNowBackgroundRunnable);
        }
        if (this.mGoogleNowAnimBackground != null) {
            this.mGoogleNowLaunchMotionTracker.setFactor(1.0f);
        }
        ImageView imageView = this.mGoogleNowAnimIcon;
        if (imageView != null) {
            imageView.setImageDrawable(null);
        }
    }

    private void clearTypedText() {
        SpannableStringBuilder spannableStringBuilder = this.mDefaultKeySsb;
        if (spannableStringBuilder == null) {
            return;
        }
        spannableStringBuilder.clear();
        this.mDefaultKeySsb.clearSpans();
        Selection.setSelection(this.mDefaultKeySsb, 0);
    }

    private long completeAdd(PendingAddArguments pendingAddArguments) {
        long jEnsurePendingDropLayoutExists = pendingAddArguments.screenId;
        if (pendingAddArguments.container == -100) {
            jEnsurePendingDropLayoutExists = ensurePendingDropLayoutExists(pendingAddArguments.screenId);
        }
        int i = pendingAddArguments.requestCode;
        if (i == 1) {
            completeAddShortcut(pendingAddArguments.intent, pendingAddArguments.container, jEnsurePendingDropLayoutExists, pendingAddArguments.cellX, pendingAddArguments.cellY);
        } else if (i == 5) {
            completeAddAppWidget(pendingAddArguments.appWidgetId, pendingAddArguments.container, jEnsurePendingDropLayoutExists, null, null);
        } else if (i == 12 || i == 111) {
            completeRestoreAppWidget(pendingAddArguments.appWidgetId);
        }
        resetAddInfo();
        return jEnsurePendingDropLayoutExists;
    }

    /* JADX WARN: Type inference failed for: r1v14, types: [com.voxel.launcher3.Launcher$20] */
    private void completeAddAppWidget(final int i, long j, long j2, AppWidgetHostView appWidgetHostView, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        int i2;
        boolean zFindCellForSpan;
        LauncherAppWidgetInfo launcherAppWidgetInfo;
        LauncherAppWidgetProviderInfo launcherAppWidgetInfo2 = launcherAppWidgetProviderInfo == null ? this.mAppWidgetManager.getLauncherAppWidgetInfo(i) : launcherAppWidgetProviderInfo;
        CellLayout cellLayout = getCellLayout(j, j2);
        int[] minSpanForWidget = getMinSpanForWidget(this, launcherAppWidgetInfo2);
        int[] spanForWidget = getSpanForWidget(this, launcherAppWidgetInfo2);
        int[] iArr = this.mTmpAddItemCellCoordinates;
        int[] iArr2 = this.mPendingAddInfo.dropPos;
        int[] iArr3 = new int[2];
        if (this.mPendingAddInfo.cellX >= 0 && this.mPendingAddInfo.cellY >= 0) {
            iArr[0] = this.mPendingAddInfo.cellX;
            iArr[1] = this.mPendingAddInfo.cellY;
            spanForWidget[0] = this.mPendingAddInfo.spanX;
            spanForWidget[1] = this.mPendingAddInfo.spanY;
            zFindCellForSpan = true;
            i2 = 1;
        } else if (cellLayout == null) {
            i2 = 1;
            zFindCellForSpan = false;
        } else if (iArr2 != null) {
            int[] iArrFindNearestVacantArea = cellLayout.findNearestVacantArea(iArr2[0], iArr2[1], minSpanForWidget[0], minSpanForWidget[1], spanForWidget[0], spanForWidget[1], iArr, iArr3);
            spanForWidget[0] = iArr3[0];
            i2 = 1;
            spanForWidget[1] = iArr3[1];
            zFindCellForSpan = iArrFindNearestVacantArea != null;
        } else {
            i2 = 1;
            zFindCellForSpan = cellLayout.findCellForSpan(iArr, minSpanForWidget[0], minSpanForWidget[1]);
        }
        if (!zFindCellForSpan) {
            if (i != -1) {
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() { // from class: com.voxel.launcher3.Launcher.20
                    @Override // android.os.AsyncTask
                    public Void doInBackground(Void... voidArr) {
                        Launcher.this.mAppWidgetHost.deleteAppWidgetId(i);
                        return null;
                    }
                };
                Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;
                Void[] voidArr = new Void[i2];
                voidArr[0] = (Void) null;
                asyncTask.executeOnExecutor(executor, voidArr);
            }
            showOutOfSpaceMessage(isHotseatLayout(cellLayout));
            return;
        }
        LauncherAppWidgetInfo launcherAppWidgetInfo3 = new LauncherAppWidgetInfo(i, launcherAppWidgetInfo2.provider);
        launcherAppWidgetInfo3.spanX = spanForWidget[0];
        launcherAppWidgetInfo3.spanY = spanForWidget[1];
        launcherAppWidgetInfo3.minSpanX = this.mPendingAddInfo.minSpanX;
        launcherAppWidgetInfo3.minSpanY = this.mPendingAddInfo.minSpanY;
        launcherAppWidgetInfo3.user = this.mAppWidgetManager.getUser(launcherAppWidgetInfo2);
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo2 = launcherAppWidgetInfo2;
        LauncherModel.addItemToDatabase(this, launcherAppWidgetInfo3, j, j2, iArr[0], iArr[1], false);
        if (!this.mRestoring) {
            if (appWidgetHostView == null) {
                launcherAppWidgetInfo = launcherAppWidgetInfo3;
                launcherAppWidgetInfo.hostView = this.mAppWidgetHost.createView(this, i, launcherAppWidgetProviderInfo2);
                launcherAppWidgetInfo.hostView.setAppWidget(i, launcherAppWidgetProviderInfo2);
            } else {
                launcherAppWidgetInfo = launcherAppWidgetInfo3;
                launcherAppWidgetInfo.hostView = appWidgetHostView;
            }
            launcherAppWidgetInfo.hostView.setTag(launcherAppWidgetInfo);
            launcherAppWidgetInfo.hostView.setVisibility(0);
            launcherAppWidgetInfo.notifyWidgetSizeChanged(this);
            this.mWorkspace.addInScreen(launcherAppWidgetInfo.hostView, j, j2, iArr[0], iArr[1], launcherAppWidgetInfo.spanX, launcherAppWidgetInfo.spanY, isWorkspaceLocked());
            addWidgetToAutoAdvanceIfNeeded(launcherAppWidgetInfo.hostView, launcherAppWidgetProviderInfo2);
        }
        resetAddInfo();
    }

    private void completeAddShortcut(Intent intent, long j, long j2, int i, int i2) {
        ShortcutInfo shortcutInfo;
        CellLayout cellLayout;
        char c;
        boolean zFindCellForSpan;
        int[] iArr = this.mTmpAddItemCellCoordinates;
        int[] iArr2 = this.mPendingAddInfo.dropPos;
        CellLayout cellLayout2 = getCellLayout(j, j2);
        ShortcutInfo shortcutInfoInfoFromShortcutIntent = this.mModel.infoFromShortcutIntent(this, intent);
        if (shortcutInfoInfoFromShortcutIntent == null) {
            return;
        }
        View viewCreateShortcut = createShortcut(shortcutInfoInfoFromShortcutIntent);
        if (i < 0 || i2 < 0) {
            shortcutInfo = shortcutInfoInfoFromShortcutIntent;
            cellLayout = cellLayout2;
            c = 1;
            if (iArr2 != null) {
                zFindCellForSpan = cellLayout.findNearestVacantArea(iArr2[0], iArr2[1], 1, 1, iArr) != null;
            } else {
                zFindCellForSpan = cellLayout.findCellForSpan(iArr, 1, 1);
            }
        } else {
            iArr[0] = i;
            iArr[1] = i2;
            if (this.mWorkspace.createUserFolderIfNecessary(viewCreateShortcut, j, cellLayout2, iArr, 0.0f, true, null, null)) {
                return;
            }
            DropTarget.DragObject dragObject = new DropTarget.DragObject();
            dragObject.dragInfo = shortcutInfoInfoFromShortcutIntent;
            c = 1;
            shortcutInfo = shortcutInfoInfoFromShortcutIntent;
            cellLayout = cellLayout2;
            if (this.mWorkspace.addToExistingFolderIfNecessary(viewCreateShortcut, cellLayout2, iArr, 0.0f, dragObject, true)) {
                return;
            } else {
                zFindCellForSpan = true;
            }
        }
        if (!zFindCellForSpan) {
            showOutOfSpaceMessage(isHotseatLayout(cellLayout));
            return;
        }
        LauncherModel.addItemToDatabase(this, shortcutInfo, j, j2, iArr[0], iArr[c], false);
        if (this.mRestoring) {
            return;
        }
        this.mWorkspace.addInScreen(viewCreateShortcut, j, j2, iArr[0], iArr[c], 1, 1, isWorkspaceLocked());
    }

    private void completeRestoreAppWidget(int i) {
        LauncherAppWidgetHostView widgetForAppWidgetId = this.mWorkspace.getWidgetForAppWidgetId(i);
        if (widgetForAppWidgetId == null || !(widgetForAppWidgetId instanceof PendingAppWidgetHostView)) {
            Log.e("Launcher", "Widget update called, when the widget no longer exists.");
            return;
        }
        LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) widgetForAppWidgetId.getTag();
        launcherAppWidgetInfo.restoreStatus = 0;
        this.mWorkspace.reinflateWidgetsIfNecessary();
        LauncherModel.updateItemInDatabase(this, launcherAppWidgetInfo);
    }

    private void completeTwoStageWidgetDrop(final int i, final int i2) {
        Runnable runnable;
        AppWidgetHostView appWidgetHostView;
        int i3;
        CellLayout screenWithId = this.mWorkspace.getScreenWithId(this.mPendingAddInfo.screenId);
        if (i == -1) {
            final AppWidgetHostView appWidgetHostViewCreateView = this.mAppWidgetHost.createView(this, i2, this.mPendingAddWidgetInfo);
            appWidgetHostView = appWidgetHostViewCreateView;
            runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$IK52mSPAKBArZeKBu_mgVNUmLmk
                @Override // java.lang.Runnable
                public final void run() {
                    Launcher.lambda$completeTwoStageWidgetDrop$3(Launcher.this, i2, appWidgetHostViewCreateView, i);
                }
            };
            i3 = 3;
        } else if (i == 0) {
            Log.d("Launcher", "================= Widget deleted " + i2);
            this.mAppWidgetHost.deleteAppWidgetId(i2);
            runnable = null;
            appWidgetHostView = null;
            i3 = 4;
        } else {
            runnable = null;
            appWidgetHostView = null;
            i3 = 0;
        }
        if (screenWithId != null) {
            if (this.mDragLayer.getAnimatedView() != null) {
                this.mWorkspace.animateWidgetDrop(this.mPendingAddInfo, screenWithId, (DragView) this.mDragLayer.getAnimatedView(), runnable, i3, appWidgetHostView, true);
                return;
            } else {
                if (runnable != null) {
                    runnable.run();
                    return;
                }
                return;
            }
        }
        /* REWORK: fabric removed — the unreachable error-log branch is gone. */
        this.mDragLayer.clearAnimatedView();
        if (runnable != null) {
            runnable.run();
        }
    }

    private ValueAnimator createNewAppBounceAnimation(View view, int i) {
        ObjectAnimator objectAnimatorOfPropertyValuesHolder = LauncherAnimUtils.ofPropertyValuesHolder(view, PropertyValuesHolder.ofFloat("alpha", 1.0f), PropertyValuesHolder.ofFloat("scaleX", 1.0f), PropertyValuesHolder.ofFloat("scaleY", 1.0f));
        objectAnimatorOfPropertyValuesHolder.setDuration(450L);
        objectAnimatorOfPropertyValuesHolder.setStartDelay(i * 85);
        objectAnimatorOfPropertyValuesHolder.setInterpolator(new OvershootInterpolator(1.3f));
        return objectAnimatorOfPropertyValuesHolder;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deactivateSearch(SearchInteractionMethod searchInteractionMethod, boolean z) {
        changeSearchMode(false, z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public void dispatchOnLauncherTransitionEnd(View view, boolean z, boolean z2) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionEnd(this, z, z2);
        }
        dispatchOnLauncherTransitionStep(view, 1.0f);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void dispatchOnLauncherTransitionPrepare(View view, boolean z, boolean z2) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionPrepare(this, z, z2);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void dispatchOnLauncherTransitionStart(View view, boolean z, boolean z2) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionStart(this, z, z2);
        }
        dispatchOnLauncherTransitionStep(view, 0.0f);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void dispatchOnLauncherTransitionStep(View view, float f) {
        if (view instanceof LauncherTransitionable) {
            ((LauncherTransitionable) view).onLauncherTransitionStep(this, f);
        }
    }

    private long ensurePendingDropLayoutExists(long j) {
        if (this.mWorkspace.getScreenWithId(j) != null) {
            return j;
        }
        this.mWorkspace.addExtraEmptyScreen();
        return this.mWorkspace.commitExtraEmptyScreen();
    }

    public static int generateViewId() {
        int i;
        int i2;
        if (Build.VERSION.SDK_INT >= 17) {
            return View.generateViewId();
        }
        do {
            i = sNextGeneratedId.get();
            i2 = i + 1;
            if (i2 > 16777215) {
                i2 = 1;
            }
        } while (!sNextGeneratedId.compareAndSet(i, i2));
        return i;
    }

    private boolean getFirstRunDidAutoImport() {
        return this.mSharedPrefs.getBoolean("launcher.first_run_did_auto_import", false);
    }

    public static Launcher getLauncher(Context context) {
        return context instanceof Launcher ? (Launcher) context : (Launcher) ((ContextWrapper) context).getBaseContext();
    }

    public static int[] getMinSpanForWidget(Context context, AppWidgetProviderInfo appWidgetProviderInfo) {
        return getSpanForWidget(context, appWidgetProviderInfo.provider, appWidgetProviderInfo.minResizeWidth, appWidgetProviderInfo.minResizeHeight);
    }

    public static int[] getSpanForWidget(Context context, AppWidgetProviderInfo appWidgetProviderInfo) {
        return getSpanForWidget(context, appWidgetProviderInfo.provider, appWidgetProviderInfo.minWidth, appWidgetProviderInfo.minHeight);
    }

    public static int[] getSpanForWidget(Context context, ComponentName componentName, int i, int i2) {
        Rect defaultPaddingForWidget = AppWidgetHostView.getDefaultPaddingForWidget(context, componentName, null);
        return CellLayout.rectToCell(i + defaultPaddingForWidget.left + defaultPaddingForWidget.right, i2 + defaultPaddingForWidget.top + defaultPaddingForWidget.bottom, null);
    }

    private String getTypedText() {
        return this.mDefaultKeySsb.toString();
    }

    private void handleHomeFirstTimeEvent(boolean z) {
        Workspace workspace = this.mWorkspace;
        this.mAnalyticsHandler.logEvent("ev_home_first_time", new AnalyticsHandler.EventProp().add("is_default", z).add("num_homescreens", workspace != null ? workspace.getNumScreens() : 1));
        setHasLaunchedHome();
    }

    private boolean hasCompletedFirstLoad() {
        SharedPreferences sharedPreferences = this.mSharedPrefs;
        return sharedPreferences != null && sharedPreferences.getBoolean("launcher.first_load_complete", false);
    }

    private boolean hasLaunchedHome() {
        SharedPreferences sharedPreferences = this.mSharedPrefs;
        return sharedPreferences != null && sharedPreferences.getBoolean("launcher.has_launhced_home", false);
    }

    private void hideAppsCustomizeHelper(Workspace.State state, final boolean z, boolean z2, final Runnable runnable) {
        AnimatorSet animatorSet = this.mStateAnimation;
        Animator changeStateAnimation = null;
        if (animatorSet != null) {
            animatorSet.setDuration(0L);
            this.mStateAnimation.cancel();
            this.mStateAnimation = null;
        }
        Utilities.isLmpOrAbove();
        Resources resources = getResources();
        resources.getInteger(R.integer.config_appsCustomizeZoomOutTime);
        resources.getInteger(R.integer.config_appsCustomizeFadeOutTime);
        resources.getInteger(R.integer.config_appsCustomizeConcealTime);
        resources.getInteger(R.integer.config_appsCustomizeItemsAlphaStagger);
        resources.getInteger(R.integer.config_appsCustomizeZoomScaleFactor);
        final Workspace workspace = this.mWorkspace;
        final ArrayList<View> arrayList = new ArrayList<>();
        if (state == Workspace.State.NORMAL || state == Workspace.State.SPRING_LOADED || state == Workspace.State.OVERVIEW) {
            changeStateAnimation = this.mWorkspace.getChangeStateAnimation(state, z, arrayList);
        }
        boolean z3 = !this.mWorkspaceLoading;
        if (!z || !z3) {
            dispatchOnLauncherTransitionPrepare(workspace, z, true);
            dispatchOnLauncherTransitionStart(workspace, z, true);
            dispatchOnLauncherTransitionEnd(workspace, z, true);
        } else {
            this.mStateAnimation = LauncherAnimUtils.createAnimatorSet();
            if (changeStateAnimation != null) {
                this.mStateAnimation.play(changeStateAnimation);
            }
            this.mStateAnimation.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.launcher3.Launcher.25
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    Launcher.this.dispatchOnLauncherTransitionEnd(workspace, z, true);
                    Runnable runnable2 = runnable;
                    if (runnable2 != null) {
                        runnable2.run();
                    }
                }
            });
            final AnimatorSet animatorSet2 = this.mStateAnimation;
            workspace.post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$SujQgTF21Z-kDddFetTxrYh1uM8
                @Override // java.lang.Runnable
                public final void run() {
                    Launcher.lambda$hideAppsCustomizeHelper$31(Launcher.this, animatorSet2, workspace, z, arrayList);
                }
            });
        }
    }

    private static State intToState(int i) {
        State state = State.WORKSPACE;
        for (State state2 : State.values()) {
            if (state2.ordinal() == i) {
                return state2;
            }
        }
        return state;
    }

    private boolean isAllAppsVisibleOrMovingToVisible() {
        return false;
    }

    private boolean isInstrumentedRun() {
        try {
            getApplication().getClassLoader().loadClass("com.voxel.simplesearchlauncher.RemoteAppLaunchTest");
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    static boolean isPropertyEnabled(String str) {
        return Log.isLoggable(str, 2);
    }

    public static boolean isWallpaperDrawableValid(Drawable drawable) {
        if (drawable == null) {
            return false;
        }
        if (!(drawable instanceof BitmapDrawable)) {
            return true;
        }
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        return (bitmapDrawable.getBitmap() == null || bitmapDrawable.getBitmap().isRecycled()) ? false : true;
    }

    public static /* synthetic */ void lambda$bindItems$35(Launcher launcher, AnimatorSet animatorSet, Collection collection, int i, ArrayList arrayList) {
        animatorSet.playTogether((Collection<Animator>) collection);
        if (i >= arrayList.size()) {
            animatorSet.addListener(launcher.new AnonymousClass26());
        }
        animatorSet.start();
    }

    public static /* synthetic */ void lambda$bindItems$36(Launcher launcher, int i, Runnable runnable) {
        Workspace workspace = launcher.mWorkspace;
        if (workspace != null) {
            workspace.snapToPage(i);
            launcher.mWorkspace.postDelayed(runnable, 500L);
        }
    }

    public static /* synthetic */ void lambda$completeTwoStageWidgetDrop$3(Launcher launcher, int i, AppWidgetHostView appWidgetHostView, int i2) {
        launcher.completeAddAppWidget(i, launcher.mPendingAddInfo.container, launcher.mPendingAddInfo.screenId, appWidgetHostView, null);
        launcher.exitSpringLoadedDragModeDelayed(i2 != 0, 300, null);
    }

    public static /* synthetic */ void lambda$exitSpringLoadedDragModeDelayed$32(Launcher launcher, boolean z, Runnable runnable) {
        if (z) {
            launcher.showWorkspace(true, runnable);
        } else {
            launcher.exitSpringLoadedDragMode();
        }
    }

    static /* synthetic */ boolean lambda$getHapticFeedbackTouchListener$27(View view, MotionEvent motionEvent) {
        if ((motionEvent.getAction() & 255) != 0) {
            return false;
        }
        view.performHapticFeedback(1);
        return false;
    }

    public static /* synthetic */ void lambda$hideAppsCustomizeHelper$31(Launcher launcher, AnimatorSet animatorSet, View view, boolean z, ArrayList arrayList) {
        if (launcher.mStateAnimation != animatorSet) {
            return;
        }
        launcher.dispatchOnLauncherTransitionStart(view, z, false);
        if (Utilities.isLmpOrAbove()) {
            for (int i = 0; i < arrayList.size(); i++) {
                View view2 = (View) arrayList.get(i);
                if (view2 != null) {
                    if (Build.VERSION.SDK_INT >= 19 ? view2.isAttachedToWindow() : true) {
                        view2.buildLayer();
                    }
                }
            }
        }
        launcher.mStateAnimation.start();
    }

    public static /* synthetic */ void lambda$onActivityResult$2(Launcher launcher, int i, int i2, CellLayout cellLayout) {
        launcher.completeTwoStageWidgetDrop(i, i2);
        cellLayout.setDropPending(false);
    }

    public static /* synthetic */ void lambda$onResume$4(Launcher launcher, View view, ItemInfoCommon itemInfoCommon, DragSource dragSource) {
        ItemInfo itemInfoFromItemInfoCommon = launcher.mSearchItemShortcutHandler.fromItemInfoCommon(launcher.getApplicationContext(), itemInfoCommon);
        if (itemInfoFromItemInfoCommon != null) {
            launcher.startDrag(view, itemInfoFromItemInfoCommon, dragSource);
        }
    }

    public static /* synthetic */ void lambda$openFolder$28(Launcher launcher, FolderIcon folderIcon) {
        int[] iArr = new int[2];
        float descendantCoordRelativeToParent = Utilities.getDescendantCoordRelativeToParent(folderIcon, launcher.mMainLayer, iArr, false);
        int measuredWidth = (int) (iArr[0] + ((folderIcon.getMeasuredWidth() * descendantCoordRelativeToParent) / 2.0f));
        int measuredHeight = (int) (iArr[1] + ((folderIcon.getMeasuredHeight() * descendantCoordRelativeToParent) / 2.0f));
        launcher.mMainLayer.setPivotX(measuredWidth);
        launcher.mMainLayer.setPivotY(measuredHeight);
        launcher.mMainLayer.removeOnLayoutChangeListener(launcher.mMainLayerMiddlePivotLayoutChangeListener);
        launcher.mMainLayerBackground.setVisibility(0);
        launcher.mBackgroundFolderAnimationHelper.animateOpenFolder();
    }

    public static /* synthetic */ void lambda$setupViews$10(Launcher launcher, View view) {
        if (launcher.mWorkspace.isSwitchingState()) {
            return;
        }
        launcher.onClickWallpaperPicker(view);
    }

    public static /* synthetic */ void lambda$setupViews$11(Launcher launcher, View view) {
        if (launcher.mWorkspace.isSwitchingState()) {
            return;
        }
        launcher.onClickSettingsButton(view);
    }

    public static /* synthetic */ void lambda$setupViews$12(Launcher launcher, View view, boolean z) {
        SearchView searchView = (SearchView) view;
        if (!z) {
            if (launcher.getCurrentStackFragment() == null && searchView.getQuery().length() == 0) {
                launcher.changeSearchMode(false, !launcher.isStopped);
                return;
            }
            return;
        }
        Float endValueIfAvailable = launcher.mQsbSearchView.getEndValueIfAvailable();
        if (endValueIfAvailable == null || endValueIfAvailable.floatValue() == 1.0f) {
            launcher.activateSearch(SearchInteractionMethod.TAP, !launcher.isStopped);
        }
    }

    public static /* synthetic */ boolean lambda$setupViews$13(Launcher launcher) {
        return launcher.getStackFragmentCount() == 0;
    }

    public static /* synthetic */ void lambda$setupViews$14(Launcher launcher, View view) {
        Log.d("Launcher", "Voice RecognitionListener - Starting Google Now Assist Listener");
        launcher.startActivitySafely(view, new Intent("android.intent.action.VOICE_ASSIST"), null, R.string.voice_no_assistant);
        launcher.isLaunchingVoiceAssist = true;
    }

    public static /* synthetic */ void lambda$setupViews$16(Launcher launcher) {
        launcher.deactivateSearch(SearchInteractionMethod.NEG_SPACE, true);
        ((InputMethodManager) launcher.getSystemService("input_method")).hideSoftInputFromWindow(launcher.mQsbSearchView.getWindowToken(), 0);
    }

    public static /* synthetic */ boolean lambda$setupViews$17(Launcher launcher, View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() != 2 && motionEvent.getAction() != 0) {
            launcher.mQsbSearchView.sendMotionEvent(motionEvent);
        }
        launcher.mBlurGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    public static /* synthetic */ void lambda$setupViews$20(Launcher launcher, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        launcher.mMainLayer.setPivotX((i + i3) / 2);
        launcher.mMainLayer.setPivotY((i2 + i4) / 2);
    }

    public static /* synthetic */ void lambda$setupViews$8(Launcher launcher, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i == i5 && i3 == i7 && i2 == i6 && i4 == i8) {
            return;
        }
        launcher.refreshBlurredImage();
        launcher.mQsbSearchViewScroller.put(launcher, SettingsStorage.getInstance().getSearchBarDesktopEnabled() ? 0.0f : launcher.getSearchBarHiddenHeight());
    }

    public static /* synthetic */ void lambda$setupViews$9(Launcher launcher, View view) {
        if (launcher.mWorkspace.isSwitchingState()) {
            return;
        }
        launcher.onClickAddWidgetButton(view);
    }

    private int mapConfigurationOriActivityInfoOri(int i) {
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        switch (defaultDisplay.getRotation()) {
            case 0:
            case 2:
                break;
            case 1:
            case 3:
                i = i != 2 ? 2 : 1;
                break;
            default:
                i = 2;
                break;
        }
        return new int[]{1, 0, 9, 8}[(defaultDisplay.getRotation() + (i != 2 ? 0 : 1)) % 4];
    }

    private void markFirstRunClingShown() {
        SharedPreferences.Editor editorEdit = this.mSharedPrefs.edit();
        editorEdit.putBoolean("launcher.first_run_cling_displayed", true);
        editorEdit.apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onAppWidgetReset() {
        LauncherAppWidgetHost launcherAppWidgetHost = this.mAppWidgetHost;
        if (launcherAppWidgetHost != null) {
            launcherAppWidgetHost.startListening();
        }
    }

    private PendingAddArguments preparePendingAddArgs(int i, Intent intent, int i2, ItemInfo itemInfo) {
        PendingAddArguments pendingAddArguments = new PendingAddArguments();
        pendingAddArguments.requestCode = i;
        pendingAddArguments.intent = intent;
        pendingAddArguments.container = itemInfo.container;
        pendingAddArguments.screenId = itemInfo.screenId;
        pendingAddArguments.cellX = itemInfo.cellX;
        pendingAddArguments.cellY = itemInfo.cellY;
        pendingAddArguments.appWidgetId = i2;
        return pendingAddArguments;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void readConfiguration(Context context, LocaleConfiguration localeConfiguration) {
        /* REWORK: the dex method swallowed everything after closing the stream; the
         * decompile's rethrow tangle does not survive javac. Best-effort IO. */
        DataInputStream dataInputStream = null;
        try {
            dataInputStream = new DataInputStream(context.openFileInput("launcher.preferences"));
            localeConfiguration.locale = dataInputStream.readUTF();
            localeConfiguration.mcc = dataInputStream.readInt();
            localeConfiguration.mnc = dataInputStream.readInt();
        } catch (FileNotFoundException unused) {
        } catch (IOException unused2) {
            // fall back to device defaults
        } catch (RuntimeException unused3) {
            // fall back to device defaults
        } finally {
            try {
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
            } catch (IOException unused4) {
            }
        }
    }

    private void reconfigureWidget(PendingAppWidgetHostView pendingAppWidgetHostView) {
        LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) pendingAppWidgetHostView.getTag();
        int i = launcherAppWidgetInfo.appWidgetId;
        LauncherAppWidgetProviderInfo launcherAppWidgetInfo2 = this.mAppWidgetManager.getLauncherAppWidgetInfo(i);
        if (launcherAppWidgetInfo2 != null) {
            this.mPendingAddWidgetInfo = launcherAppWidgetInfo2;
            this.mPendingAddInfo.copyFrom(launcherAppWidgetInfo);
            this.mPendingAddWidgetId = i;
            AppWidgetManagerCompat.getInstance(this).startConfigActivity(launcherAppWidgetInfo2, launcherAppWidgetInfo.appWidgetId, this, this.mAppWidgetHost, 12);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.voxel.launcher3.Launcher$5] */
    @SuppressLint({"StaticFieldLeak"})
    public void refreshBlurredImage() {
        AsyncTask asyncTask = this.mRefreshBlurredImageTask;
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        this.mRefreshBlurredImageTask = new AsyncTask<Void, Void, C1BlurredImageTaskResult>() { // from class: com.voxel.launcher3.Launcher.5
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public C1BlurredImageTaskResult doInBackground(Void[] voidArr) {
                Drawable drawable;
                float f;
                float f2;
                if (Launcher.this.mLauncherView == null) {
                    return null;
                }
                C1BlurredImageTaskResult c1BlurredImageTaskResult = Launcher.this.new C1BlurredImageTaskResult();
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(Launcher.this);
                c1BlurredImageTaskResult.isLiveWallpaper = !Launcher.this.canAccessWallpaper();
                try {
                    drawable = wallpaperManager.getDrawable();
                } catch (RuntimeException unused) {
                    drawable = null;
                }
                if (!Launcher.isWallpaperDrawableValid(drawable)) {
                    c1BlurredImageTaskResult.isLiveWallpaper = true;
                    drawable = null;
                }
                if (c1BlurredImageTaskResult.isLiveWallpaper) {
                    Bitmap bitmapCreateBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmapCreateBitmap);
                    int color = Launcher.this.getResources().getColor(R.color.blur_darken_live_wallpaper);
                    canvas.drawARGB(Color.alpha(color), Color.red(color), Color.blue(color), Color.green(color));
                    c1BlurredImageTaskResult.bitmap = bitmapCreateBitmap;
                } else {
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    int iCeil = (int) Math.ceil(drawable.getIntrinsicWidth() * 0.25f);
                    int iCeil2 = (int) Math.ceil(drawable.getIntrinsicHeight() * 0.25f);
                    if (iCeil <= 0 || iCeil2 <= 0) {
                        return null;
                    }
                    Bitmap bitmapCreateBitmap2 = Bitmap.createBitmap(iCeil, iCeil2, Bitmap.Config.ARGB_8888);
                    Canvas canvas2 = new Canvas(bitmapCreateBitmap2);
                    Matrix matrix = new Matrix();
                    int intrinsicWidth = drawable.getIntrinsicWidth();
                    int intrinsicHeight = drawable.getIntrinsicHeight();
                    float f3 = 0.0f;
                    if (intrinsicWidth * iCeil2 > iCeil * intrinsicHeight) {
                        f = iCeil2 / intrinsicHeight;
                        f2 = (iCeil - (intrinsicWidth * f)) * 0.5f;
                    } else {
                        float f4 = iCeil / intrinsicWidth;
                        f3 = (iCeil2 - (intrinsicHeight * f4)) * 0.5f;
                        f = f4;
                        f2 = 0.0f;
                    }
                    matrix.setScale(f, f);
                    matrix.postTranslate((int) (f2 + 0.5f), (int) (f3 + 0.5f));
                    canvas2.save();
                    canvas2.concat(matrix);
                    drawable.draw(canvas2);
                    canvas2.restore();
                    int color2 = Launcher.this.getResources().getColor(R.color.blur_darken_opaque_wallpaper);
                    canvas2.save();
                    canvas2.drawARGB(Color.alpha(color2), Color.red(color2), Color.blue(color2), Color.green(color2));
                    canvas2.restore();
                    c1BlurredImageTaskResult.bitmap = Blur.fastBlur(Launcher.this, bitmapCreateBitmap2, 25, 0.7f);
                }
                if (Launcher.this.mQsbSearchView != null) {
                    Launcher.this.mQsbSearchView.refreshSubtleBackgroundDrawable(drawable, Launcher.this.canAccessWallpaper());
                }
                return c1BlurredImageTaskResult;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(C1BlurredImageTaskResult c1BlurredImageTaskResult) {
                if (c1BlurredImageTaskResult == null || Launcher.this.mBlurImageView == null) {
                    return;
                }
                Launcher.this.mBlurImageView.setImageBitmap(c1BlurredImageTaskResult.bitmap);
                Launcher.this.mAllAppsBackground.setImageBitmap(c1BlurredImageTaskResult.bitmap);
                Launcher.this.mIsLiveWallpaper = c1BlurredImageTaskResult.isLiveWallpaper;
                Launcher.this.mQsbSearchView.setVisibility(0);
                Resources resources = Launcher.this.getResources();
                Launcher.this.mHotseatBg.enableBlurredBackground(Launcher.this.mHotseat.getCellParams().backgroundBlur);
                Launcher.this.mHotseatBg.setImageDrawables(resources.getDrawable(R.drawable.hotseat_bg), new BitmapDrawable(resources, c1BlurredImageTaskResult.bitmap));
                Launcher.this.mRefreshBlurredImageTask = null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private void registerContentObservers() {
        getContentResolver().registerContentObserver(LauncherProvider.CONTENT_APPWIDGET_RESET_URI, true, this.mWidgetObserver);
    }

    private void resetAddInfo() {
        ItemInfo itemInfo = this.mPendingAddInfo;
        itemInfo.container = -1L;
        itemInfo.screenId = -1L;
        itemInfo.cellY = -1;
        itemInfo.cellX = -1;
        itemInfo.spanY = -1;
        itemInfo.spanX = -1;
        itemInfo.minSpanY = -1;
        itemInfo.minSpanX = -1;
        itemInfo.dropPos = null;
    }

    private void restoreState(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        if (intToState(bundle.getInt("launcher.state", State.WORKSPACE.ordinal())) == State.APPS_CUSTOMIZE) {
            this.mOnResumeState = State.APPS_CUSTOMIZE;
        }
        int i = bundle.getInt("launcher.current_screen", -1001);
        if (i != -1001) {
            this.mWorkspace.setRestorePage(i);
        }
        long j = bundle.getLong("launcher.add_container", -1L);
        long j2 = bundle.getLong("launcher.add_screen", -1L);
        if (j != -1 && j2 > -1) {
            ItemInfo itemInfo = this.mPendingAddInfo;
            itemInfo.container = j;
            itemInfo.screenId = j2;
            itemInfo.cellX = bundle.getInt("launcher.add_cell_x");
            this.mPendingAddInfo.cellY = bundle.getInt("launcher.add_cell_y");
            this.mPendingAddInfo.spanX = bundle.getInt("launcher.add_span_x");
            this.mPendingAddInfo.spanY = bundle.getInt("launcher.add_span_y");
            this.mPendingAddWidgetInfo = (LauncherAppWidgetProviderInfo) bundle.getParcelable("launcher.add_widget_info");
            this.mPendingAddWidgetId = bundle.getInt("launcher.add_widget_id");
            setWaitingForResult(true);
            this.mRestoring = true;
        }
        if (bundle.getBoolean("launcher.rename_folder", false)) {
            this.mFolderInfo = this.mModel.getFolderById(this, sFolders, bundle.getLong("launcher.rename_folder_id"));
            this.mRestoring = true;
        }
        this.mItemIdToViewId = (HashMap) bundle.getSerializable("launcher.view_ids");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleGoogleNowBackgroundCleanup(long j) {
        if (this.mCleanupGoogleNowBackgroundHandler == null) {
            this.mCleanupGoogleNowBackgroundHandler = new Handler();
        }
        this.mCleanupGoogleNowBackgroundHandler.removeCallbacks(this.mCleanupGoogleNowBackgroundRunnable);
        if (j > 0) {
            this.mCleanupGoogleNowBackgroundHandler.postDelayed(this.mCleanupGoogleNowBackgroundRunnable, j);
        } else {
            cleanupGoogleNowLaunchBackground();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void sendAdvanceMessage(long j) {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), j);
        this.mAutoAdvanceSentTime = System.currentTimeMillis();
    }

    private void sendLoadingCompleteBroadcastIfNecessary() {
        if (hasCompletedFirstLoad()) {
            return;
        }
        sendBroadcast(new Intent("com.voxel.launcher3.action.FIRST_LOAD_COMPLETE"), getResources().getString(R.string.receive_first_load_broadcast_permission));
        SharedPreferences.Editor editorEdit = this.mSharedPrefs.edit();
        editorEdit.putBoolean("launcher.first_load_complete", true);
        editorEdit.apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setFragmentBlurAlpha(float f) {
        this.mBlurImageAlphaHelper.put("fragment", f);
        updateBlurImageViewAlpha();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setGoogleBlurAlpha(float f) {
        this.mBlurImageAlphaHelper.put("google", f);
        updateBlurImageViewAlpha();
    }

    private void setHasLaunchedHome() {
        SharedPreferences sharedPreferences = this.mSharedPrefs;
        if (sharedPreferences != null) {
            SharedPreferences.Editor editorEdit = sharedPreferences.edit();
            editorEdit.putBoolean("launcher.has_launhced_home", true);
            editorEdit.apply();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setPullDownListViewEnabled(boolean z) {
        if (z) {
            this.mPullDownRecyclerView.setEnabled(true);
            this.mPullDownRecyclerView.setClickable(true);
            this.mPullDownRecyclerView.setLongClickable(true);
        } else {
            this.mPullDownRecyclerView.setEnabled(false);
            this.mPullDownRecyclerView.setClickable(false);
            this.mPullDownRecyclerView.setLongClickable(false);
        }
    }

    static void setScreen(int i) {
        synchronized (sLock) {
            sScreen = i;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setSearchBlurAlpha(float f) {
        this.mBlurImageAlphaHelper.put("search", f);
        updateBlurImageViewAlpha();
    }

    private void setUpSearchHelper() {
        this.mSearchItemShortcutHandler = new SearchItemShortcutHandler(this, this.localAppsManager);
        this.mSearchUiHelper = new SearchUiHelper(this, this.mQsbSearchView, this.mPullDownRecyclerView, this.mFrequentlyUsedModel, this.mHideAppsModel, new SearchUiHelper.ScaleState() { // from class: com.voxel.launcher3.Launcher.15
            @Override // com.voxel.simplesearchlauncher.search.SearchUiHelper.ScaleState
            public float getScale() {
                if (Launcher.this.mBackgroundFolderAnimationHelper != null) {
                    return 1.0f * Launcher.this.mBackgroundFolderAnimationHelper.getAnimatedValue();
                }
                return 1.0f;
            }

            @Override // com.voxel.simplesearchlauncher.search.SearchUiHelper.ScaleState
            public View getScaledView() {
                return Launcher.this.mMainLayer;
            }
        });
        this.mSearchPresenter = this.mSearchUiHelper.getSearchPresenter();
        this.mSearchPresenter.setWebSearchAllowed(true);
        this.mFragmentHelper = new FragmentHelper(this);
    }

    private void setWaitingForResult(boolean z) {
        boolean zIsWorkspaceLocked = isWorkspaceLocked();
        this.mWaitingForResult = z;
        if (zIsWorkspaceLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    private void setWorkspaceBackground(boolean z) {
    }

    private void setWorkspaceLoading(boolean z) {
        boolean zIsWorkspaceLocked = isWorkspaceLocked();
        this.mWorkspaceLoading = z;
        if (zIsWorkspaceLocked != isWorkspaceLocked()) {
            onWorkspaceLockedChanged();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setupGoogleNowLaunchBackground() {
        if (this.mGoogleNowAnimBackground != null) {
            this.mGoogleNowAnimIcon.setImageResource(R.drawable.google);
            this.mGoogleNowAnimFinalIconSize = (int) (this.mGoogleNowAnimBackground.getWidth() * 0.45f);
            this.mIsGoogleNowAnimLocked = false;
        }
    }

    private void setupViews() {
        DragController dragController = this.mDragController;
        ButterKnife.bind(this);
        this.mAllAppsFragment = (AllAppsFragment) getSupportFragmentManager().findFragmentById(R.id.all_apps_fragment);
        this.mWorkspaceAlpha = new AlphaSet(this.mWorkspace);
        this.mHotseatAlpha = new AlphaSet(this.mHotseat, this.mHotseatBg);
        this.mIndicatorAlpha = new AlphaSet(this.mIndicatorContainer);
        this.mSearchDropTargetBarAlpha = new AlphaSet(this.mSearchDropTargetBar);
        this.mOverviewPanelAlpha = new AlphaSet(this.mOverviewPanel);
        this.mMainLayerDarkenAlpha = new AlphaSet(this.mMainLayerDarken);
        this.mOverviewPanelAlpha.putValue(this.mWorkspace, 0.0f);
        this.mOverviewPanelAlpha.putValue(this, 1.0f);
        this.mQsbSearchViewScroller = new CombinedFactorHelper();
        this.mQsbSearchViewScroller.addObserver(new Observer() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$BorPgMNdJmJQ5azuSVWpGl1-m64
            @Override // java.util.Observer
            public final void update(Observable observable, Object obj) {
                Launcher launcher = Launcher.this;
                launcher.mQsbSearchView.setTranslationY(launcher.mQsbSearchViewScroller.getCombinedFactor(CombinedFactorHelper.ADD_COMBINER));
            }
        });
        this.mQsbSearchViewScrollerModifier = new CombinedFactorHelper();
        this.mQsbSearchViewScrollerModifier.addObserver(new Observer() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$zhU500aZo-d4VaukXY5jX3eAGdI
            @Override // java.util.Observer
            public final void update(Observable observable, Object obj) {
                Launcher launcher = Launcher.this;
                launcher.mQsbSearchViewScroller.putModifier(launcher, launcher.mQsbSearchViewScrollerModifier.getCombinedFactor(CombinedFactorHelper.MULTIPLY_COMBINER));
            }
        });
        setUpSearchHelper();
        this.mLauncherView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$GvpUv7DXjVS5KkPiSaYqD2n_5R4
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                Launcher.lambda$setupViews$8(Launcher.this, view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mLauncherView.setFocusableInTouchMode(true);
        this.mLauncherView.setSystemUiVisibility(1792);
        this.mDragLayer.setup(this, dragController);
        Hotseat hotseat = this.mHotseat;
        if (hotseat != null) {
            hotseat.setup(this);
            this.mHotseat.setOnLongClickListener(this);
        }
        this.mHotseat.setBackgroundView(this.mHotseatBg);
        View viewFindViewById = findViewById(R.id.widget_button);
        viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$WiCWsKhJ7G7upckPy8pCsLSkGz0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Launcher.lambda$setupViews$9(Launcher.this, view);
            }
        });
        viewFindViewById.setOnTouchListener(getHapticFeedbackTouchListener());
        View viewFindViewById2 = findViewById(R.id.wallpaper_button);
        viewFindViewById2.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$p1VwbHHZQ3ZHHqC9_lswaPZD6jg
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Launcher.lambda$setupViews$10(Launcher.this, view);
            }
        });
        viewFindViewById2.setOnTouchListener(getHapticFeedbackTouchListener());
        View viewFindViewById3 = findViewById(R.id.settings_button);
        if (hasSettings()) {
            viewFindViewById3.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$8bpeOA_SsnRycNsXHgq63uNOkC0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    Launcher.lambda$setupViews$11(Launcher.this, view);
                }
            });
            viewFindViewById3.setOnTouchListener(getHapticFeedbackTouchListener());
        } else {
            viewFindViewById3.setVisibility(8);
            ((FrameLayout.LayoutParams) viewFindViewById.getLayoutParams()).gravity = 8388661;
            viewFindViewById.requestLayout();
        }
        this.mWorkspace.setPageSwitchListener(this);
        this.mWorkspace.setDeleteDropTarget((RemoveDropTarget) this.mDragLayer.findViewById(R.id.workspace_delete_drop_target));
        this.mWorkspace.setHapticFeedbackEnabled(false);
        this.mWorkspace.setOnLongClickListener(this);
        this.mWorkspace.setup(dragController);
        dragController.addDragListener(this.mWorkspace);
        this.mWorkspace.setPageInteractionListener(this.mIndicatorContainer);
        this.mDragController.addDragListener(this.mIndicatorContainer);
        this.mDragController.addDragListener(new DragController.DragListener() { // from class: com.voxel.launcher3.Launcher.9
            boolean needsReshow = false;
            boolean showLockOverlay = false;

            @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
            public void onDragEnd() {
                if (this.needsReshow) {
                    Launcher.this.mSearchDropTargetBar.showSearchBar(true);
                    this.needsReshow = false;
                }
                if (this.showLockOverlay) {
                    Launcher.this.pushStackFragment(new LockOverlayFragment());
                    this.showLockOverlay = false;
                }
            }

            @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
            public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
                Launcher.this.changeSearchMode(false, true);
                Launcher.this.mPullDownRecyclerView.clearFocus();
                Launcher.this.changeAllAppsVisibility(false, true);
                this.showLockOverlay = dragOptions.isDesktopLocked;
                if (dragObject.dragSource instanceof AllAppsFragment) {
                    Launcher.this.mSearchDropTargetBar.hideSearchBar(false);
                    this.needsReshow = true;
                }
            }
        });
        this.mQsbSearchView.setFactorDistance(getResources().getDimensionPixelSize(R.dimen.search_pull_down_distance));
        if (SettingsStorage.getInstance().getSearchBarStyle() == 1) {
            this.mQsbSearchView.setUseSubtleBackground(true);
            this.mQsbSearchView.setVisibility(4);
        }
        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$QrXdSFEB58XT5q0HVObpCAfkYw0
            @Override // android.view.View.OnFocusChangeListener
            public final void onFocusChange(View view, boolean z) {
                Launcher.lambda$setupViews$12(Launcher.this, view, z);
            }
        };
        this.mQsbSearchView.setOnFocusChangeListener(onFocusChangeListener);
        this.mQsbSearchView.setOnQueryTextFocusChangeListener(onFocusChangeListener);
        this.mQsbSearchView.setOnFactorChangeListener(new SpringySearchView.OnFactorChangeListener() { // from class: com.voxel.launcher3.Launcher.10
            boolean wasFullyExpanded = false;

            @Override // com.voxel.simplesearchlauncher.view.SpringySearchView.OnFactorChangeListener
            public void onFactorBeginChanging() {
                if (Launcher.this.mIsLiveWallpaper) {
                    Launcher.this.mWorkspace.onTransitionPrepare();
                    Launcher.this.mHotseatAlpha.setLayerType(2, null);
                    Launcher.this.mIndicatorAlpha.setLayerType(2, null);
                }
            }

            @Override // com.voxel.simplesearchlauncher.view.SpringySearchView.OnFactorChangeListener
            public void onFactorChanged(float f) {
                if (Launcher.this.mWorkspace == null || Launcher.this.mMainLayerBackground == null) {
                    return;
                }
                float f2 = 1.0f - f;
                float fLimitAlpha = AlphaUtil.limitAlpha(f2 / (1.0f - Launcher.this.mQsbSearchView.getCrossOverFactor()));
                Launcher.this.setSearchBlurAlpha(fLimitAlpha);
                float f3 = Launcher.this.mIsLiveWallpaper ? 1.0f - fLimitAlpha : 1.0f;
                Launcher.this.mWorkspaceAlpha.putValue(Launcher.this.mQsbSearchView, f3);
                Launcher.this.mHotseatAlpha.putValue(Launcher.this.mQsbSearchView, f3);
                Launcher.this.mIndicatorAlpha.putValue(Launcher.this.mQsbSearchView, f3);
                if (fLimitAlpha >= 1.0f) {
                    Launcher.this.mWorkspace.setVisibility(4);
                    Launcher.this.mHotseat.setVisibility(4);
                    Launcher.this.mMainLayerBackground.setVisibility(4);
                    Launcher.this.mHotseatBg.setVisibility(4);
                    Launcher.this.mIndicatorContainer.setVisibility(4);
                } else {
                    Launcher.this.mWorkspace.setVisibility(0);
                    Launcher.this.mHotseat.setVisibility(0);
                    Launcher.this.mHotseatBg.setVisibility(0);
                    Launcher.this.mIndicatorContainer.setVisibility(0);
                }
                Launcher.this.mPullDownRecyclerView.setCurrentFactor(f2);
                Launcher.this.mQsbSearchViewScrollerModifier.put(this, f);
                if (f <= 0.0f) {
                    if (!Launcher.this.mSharedPrefs.getBoolean("launcher.has_engaged_search", false)) {
                        Launcher.this.mSharedPrefs.edit().putBoolean("launcher.has_engaged_search", true).apply();
                    }
                    if (Launcher.this.mQsbSearchView.getAlternateHintTextModeTarget() != 0.0f) {
                        Launcher.this.mQsbSearchView.hideAlternateHintTextMode(false);
                    }
                    this.wasFullyExpanded = true;
                } else if (f >= 1.0f) {
                    this.wasFullyExpanded = false;
                }
                Float endValueIfAvailable = Launcher.this.mQsbSearchView.getEndValueIfAvailable();
                if (f <= 0.0f || (endValueIfAvailable != null && endValueIfAvailable.floatValue() <= 0.0f)) {
                    Launcher.this.setPullDownListViewEnabled(true);
                } else if (f >= 1.0f || ((endValueIfAvailable != null && endValueIfAvailable.floatValue() >= 0.0f) || this.wasFullyExpanded)) {
                    Launcher.this.setPullDownListViewEnabled(false);
                } else {
                    Launcher.this.setPullDownListViewEnabled(true);
                }
                if (Launcher.this.mSearchUiHelper != null && TextUtils.isEmpty(Launcher.this.mQsbSearchView.getQuery()) && !TextUtils.isEmpty(Launcher.this.mSearchUiHelper.getSearchText())) {
                    Launcher.this.mQsbSearchView.setQuery(Launcher.this.mSearchUiHelper.getSearchText(), false);
                }
                if (f < 1.0f) {
                    Launcher.this.mPullDownRecyclerView.setVisibility(0);
                    return;
                }
                Launcher.this.mQsbSearchView.setQuery("", false);
                if (!Launcher.this.mSearchUiHelper.getSearchText().isEmpty()) {
                    Launcher.this.mSearchUiHelper.updateSearchInput("");
                }
                Launcher.this.mPullDownRecyclerView.getLayoutManager().scrollToPosition(0);
                Launcher.this.mPullDownRecyclerView.setVisibility(4);
            }

            @Override // com.voxel.simplesearchlauncher.view.SpringySearchView.OnFactorChangeListener
            public void onFactorEndChanging() {
                if (Launcher.this.mWorkspace != null) {
                    Launcher.this.mWorkspace.onTransitionEnd();
                }
                if (Launcher.this.mHotseatAlpha != null) {
                    Launcher.this.mHotseatAlpha.setLayerType(0, null);
                }
                if (Launcher.this.mIndicatorAlpha != null) {
                    Launcher.this.mIndicatorAlpha.setLayerType(0, null);
                }
            }
        });
        this.mQsbSearchView.setSearchableInfo(((SearchManager) getSystemService("search")).getSearchableInfo(getComponentName()));
        this.mQsbSearchView.setKeyboardRulesHandler(new SpringySearchView.KeyboardRulesHandler() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$njqBCaBNIeD1LVn-zUNPErn514k
            @Override // com.voxel.simplesearchlauncher.view.SpringySearchView.KeyboardRulesHandler
            public final boolean canDisplayKeyboard() {
                return Launcher.lambda$setupViews$13(Launcher.this);
            }
        });
        this.mQsbSearchView.findViewById(R.id.search_voice_btn).setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$F2iZ-Jl6bceIfx4T2ns2fSk53LM
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Launcher.lambda$setupViews$14(Launcher.this, view);
            }
        });
        EditText editText = this.mQsbSearchViewEditText;
        if (editText != null) {
            editText.setInputType(524289);
        }
        this.mQsbSearchViewGestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() { // from class: com.voxel.launcher3.Launcher.11
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
                if ((!Launcher.this.mWorkspace.isTouchActive() || Launcher.this.mWorkspace.getPageCount() == 1 || !Launcher.this.mWorkspace.isAllowOverScroll()) && Launcher.this.mQsbSearchView != null) {
                    Launcher.this.mQsbSearchView.sendMotionEvent(motionEvent2);
                    return true;
                }
                if (Launcher.this.mQsbSearchView == null) {
                    return false;
                }
                Launcher.this.mQsbSearchView.cancelMotion();
                return false;
            }
        });
        this.mQsbSearchView.clearFocus();
        this.mLauncherView.requestFocus();
        this.mPullDownRecyclerView.setItemAnimator(new DefaultItemAnimator());
        this.mPullDownRecyclerView.getItemAnimator().setRemoveDuration(500L);
        this.mPullDownRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.mPullDownRecyclerView.setItemViewCacheSize(10);
        this.mSearchResultListCloseKeyboardGestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() { // from class: com.voxel.launcher3.Launcher.12
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
                ((InputMethodManager) Launcher.this.getSystemService("input_method")).hideSoftInputFromWindow(Launcher.this.mQsbSearchView.getWindowToken(), 0);
                Launcher.this.mQsbSearchView.clearFocus();
                return false;
            }
        });
        this.mPullDownRecyclerView.setOnTouchListener(new View.OnTouchListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$t9Kd_rILOsUISEGlYeTwrgWw0KU
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return Launcher.this.mSearchResultListCloseKeyboardGestureDetector.onTouchEvent(motionEvent);
            }
        });
        this.mPullDownRecyclerView.setOnNoItemClickListener(new NoItemClickRecyclerView.OnNoItemClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$STKe9PGn9CFluPJLz2v395i2HLY
            @Override // com.voxel.simplesearchlauncher.view.NoItemClickRecyclerView.OnNoItemClickListener
            public final void onNoItemClicked() {
                Launcher.lambda$setupViews$16(Launcher.this);
            }
        });
        setPullDownListViewEnabled(false);
        this.mQsbSearchView.setOnQueryTextListener(this.mSearchUiHelper.getQueryTextListener());
        setSearchBlurAlpha(0.0f);
        this.mBlurGestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() { // from class: com.voxel.launcher3.Launcher.13
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
                Launcher.this.mQsbSearchView.sendMotionEvent(motionEvent2);
                return true;
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                Launcher.this.deactivateSearch(SearchInteractionMethod.NEG_SPACE, true);
                return true;
            }
        });
        this.mBlurOnTouchListener = new View.OnTouchListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$mffmvBKS6p_M7HV28GMoJVkvaqw
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return Launcher.lambda$setupViews$17(Launcher.this, view, motionEvent);
            }
        };
        this.mAllAppsUpChevron.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$gBzU-G4zs5jCY-s4acexIkaeJ74
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                Launcher.this.changeAllAppsVisibility(true, true);
            }
        });
        this.mAllAppsBackground.setImageAlpha(0);
        this.mAllAppsContainer.setTranslationY(getResources().getDisplayMetrics().heightPixels);
        this.mAllAppsContainer.setAlpha(0.0f);
        this.mAllAppsMotionTracker = new SpringyMotionTracker(this);
        this.mAllAppsMotionTracker.setSpringConfig(new SpringConfig(360.0d, 40.0d));
        this.mAllAppsMotionTracker.setDirection(SpringyMotionTracker.Direction.VERTICAL);
        this.mAllAppsMotionTracker.setFactorDistance(getResources().getDisplayMetrics().heightPixels);
        this.mAllAppsMotionTracker.setOnFactorChangeListener(this.mAllAppsFactorChangeListener);
        this.mAllAppsMotionTracker.setFactor(1.0f);
        this.mGoogleNowLaunchMotionTracker = new SpringyMotionTracker(this);
        this.mGoogleNowLaunchMotionTracker.setFactor(1.0f);
        this.mGoogleNowLaunchMotionTracker.setSpringConfig(new SpringConfig(500.0d, 50.0d));
        this.mGoogleNowLaunchMotionTracker.setFactorDistance(this.mScreenHeight);
        this.mGoogleNowLaunchMotionTracker.setDirection(SpringyMotionTracker.Direction.VERTICAL);
        this.mGoogleNowLaunchMotionTracker.setOnFactorChangeListener(this.mGoogleNowLaunchFactorChangeListener);
        dragController.setDragScoller(this.mWorkspace);
        dragController.setScrollView(this.mDragLayer);
        dragController.setMoveTarget(this.mWorkspace);
        dragController.addDropTarget(this.mWorkspace);
        SearchDropTargetBar searchDropTargetBar = this.mSearchDropTargetBar;
        if (searchDropTargetBar != null) {
            searchDropTargetBar.setup(this, dragController);
        }
        /* REWORK: WeightWatcher/memory debug overlay cut (debug-only, never shown). */
        this.mCustomGestureListener = new CustomGestureListener(this, new CustomGestureManager(this, SettingsStorage.getInstance()));
        this.mCustomGestureDetector = new GestureDetectorCompat(this, this.mCustomGestureListener);
        this.mScaleCustomGestureDetector = new ScaleGestureDetector(this, this.mCustomGestureListener);
        this.mMainLayerGestureListener = new MainLayerGestureListener(this);
        final GestureDetectorCompat gestureDetectorCompat = new GestureDetectorCompat(this, this.mMainLayerGestureListener);
        this.mMainLayer.setOnInterceptTouchListener(new View.OnTouchListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$ajMcfR8mNeR3-PIGktfzBKgWqes
            @Override // android.view.View.OnTouchListener
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetectorCompat.onTouchEvent(motionEvent);
            }
        });
        this.mMainLayer.setOnTouchListener(new View.OnTouchListener() { // from class: com.voxel.launcher3.Launcher.14
            boolean mEventStreamStart = true;
            boolean mSingleTouchSwipeUpAllApps = false;
            boolean mSingleTouchSwipeDownSearch = false;
            boolean mCustomDetectorActive = false;
            boolean mSingleTouchSwipeUpActive = false;
            boolean mSingleTouchSwipeDownActive = false;
            boolean mMultiTouchSwipeUpActive = false;
            boolean mMultiTouchSwipeDownActive = false;
            boolean mMultiTouchSwipeUpCancelSent = false;
            boolean mMultiTouchSwipeDownCancelSent = false;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean zOnTouchEvent = gestureDetectorCompat.onTouchEvent(motionEvent);
                if (!Launcher.this.mMainLayerGestureListener.disallowTouches) {
                    Launcher.this.mScaleCustomGestureDetector.onTouchEvent(motionEvent);
                }
                if (this.mEventStreamStart) {
                    this.mEventStreamStart = false;
                    int action = motionEvent.getAction();
                    motionEvent.setAction(3);
                    Launcher.this.mScaleCustomGestureDetector.onTouchEvent(motionEvent);
                    Launcher.this.mCustomGestureDetector.onTouchEvent(motionEvent);
                    motionEvent.setAction(0);
                    Launcher.this.mScaleCustomGestureDetector.onTouchEvent(motionEvent);
                    Launcher.this.mCustomGestureDetector.onTouchEvent(motionEvent);
                    motionEvent.setAction(action);
                    Launcher.this.mCustomGestureListener.reset();
                    this.mSingleTouchSwipeUpAllApps = CustomGestureAction.APP_DRAWER.equals(SettingsStorage.getInstance().getActionForGestureType(GestureType.SWIPE_UP));
                    this.mSingleTouchSwipeDownSearch = CustomGestureAction.EVIE_SEARCH.equals(SettingsStorage.getInstance().getActionForGestureType(GestureType.SWIPE_DOWN));
                }
                if (!Launcher.this.mCustomGestureListener.isGestureHandled() && ((zOnTouchEvent || (motionEvent.getAction() != 2 && motionEvent.getAction() != 0)) && !Launcher.this.mMainLayerGestureListener.horizontal)) {
                    if (Launcher.this.mMainLayerGestureListener.searchLocked) {
                        Launcher.this.mQsbSearchView.sendMotionEvent(motionEvent);
                    } else if (Launcher.this.mMainLayerGestureListener.allAppsLocked) {
                        Launcher.this.mAllAppsMotionTracker.sendMotionEvent(motionEvent);
                    } else if (Launcher.this.mMainLayerGestureListener.swipeUp) {
                        if (this.mSingleTouchSwipeUpAllApps && motionEvent.getPointerCount() == 1) {
                            Launcher.this.mAllAppsMotionTracker.sendMotionEvent(motionEvent);
                            this.mSingleTouchSwipeUpActive = true;
                        } else {
                            if (this.mSingleTouchSwipeUpActive) {
                                int action2 = motionEvent.getAction();
                                motionEvent.setAction(3);
                                Launcher.this.mAllAppsMotionTracker.sendMotionEvent(motionEvent);
                                motionEvent.setAction(action2);
                                this.mSingleTouchSwipeUpActive = false;
                            }
                            Launcher.this.mCustomGestureDetector.onTouchEvent(motionEvent);
                            this.mCustomDetectorActive = true;
                        }
                    } else if (this.mSingleTouchSwipeDownSearch && motionEvent.getPointerCount() == 1) {
                        Launcher.this.mQsbSearchView.sendMotionEvent(motionEvent);
                        this.mSingleTouchSwipeDownActive = true;
                    } else {
                        if (this.mSingleTouchSwipeDownActive) {
                            int action3 = motionEvent.getAction();
                            motionEvent.setAction(3);
                            Launcher.this.mQsbSearchView.sendMotionEvent(motionEvent);
                            motionEvent.setAction(action3);
                            this.mSingleTouchSwipeDownActive = false;
                        }
                        Launcher.this.mCustomGestureDetector.onTouchEvent(motionEvent);
                        this.mCustomDetectorActive = true;
                    }
                }
                if (motionEvent.getAction() == 1 || motionEvent.getAction() == 3) {
                    this.mEventStreamStart = true;
                    this.mSingleTouchSwipeDownActive = false;
                    this.mSingleTouchSwipeUpActive = false;
                    this.mMultiTouchSwipeDownActive = false;
                    this.mMultiTouchSwipeUpActive = false;
                }
                return true;
            }
        });
        this.mMainLayerMiddlePivotLayoutChangeListener = new View.OnLayoutChangeListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$i2CCMuykmISgl6teQaKt5sB1adw
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                Launcher.lambda$setupViews$20(Launcher.this, view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        };
        this.mMainLayer.addOnLayoutChangeListener(this.mMainLayerMiddlePivotLayoutChangeListener);
    }

    private boolean shouldRunFirstRunActivity() {
        /* REWORK: keep the onboarding flow on emulators too — dropping the
           isRunningInTestHarness clause so the flow is verifiable on our AVD. */
        return isInstrumentedRun() || this.mSharedPrefs.getBoolean("launcher.first_run_activity_displayed", false) ? false : true;
    }

    private boolean shouldRunFirstRunClings() {
        /* REWORK: emulator-visible like shouldRunFirstRunActivity. */
        return isInstrumentedRun() || this.mSharedPrefs.getBoolean("launcher.first_run_cling_displayed", false) ? false : true;
    }


    private void showBrokenAppInstallDialog(final String str, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_DeviceDefault /* REWORK: android-35 R flattens nested style classes */)).setTitle(R.string.abandoned_promises_title).setMessage(R.string.abandoned_promise_explanation).setPositiveButton(R.string.abandoned_search, onClickListener).setNeutralButton(R.string.abandoned_clean_this, new DialogInterface.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$J1M51etEzYVkQ2xk1sr_TZFd7eA
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                Launcher.this.mWorkspace.removeAbandonedPromise(str, UserHandleCompat.myUserHandle());
            }
        }).create().show();
    }


    /* JADX WARN: Code duplicated, block: B:12:0x0021  */
    private boolean startActivity(View view, Intent intent, Object obj) {
        boolean z;
        Bundle bundle;
        ActivityOptions activityOptionsMakeCustomAnimation;
        int iWidth;
        int iWidth2;
        int paddingTop;
        Drawable textViewIcon;
        intent.addFlags(268435456);
        if (!sPermissionHelper.checkIntentPermission(this, intent)) {
            return false;
        }
        if (view != null) {
            try {
                if (intent.hasExtra("com.voxel.launcher3.intent.extra.shortcut.INGORE_LAUNCH_ANIMATION")) {
                    z = false;
                } else {
                    z = true;
                }
            } catch (SecurityException e) {
                if (Build.VERSION.SDK_INT >= 23 && (obj instanceof ItemInfo) && intent.getComponent() == null && "android.intent.action.CALL".equals(intent.getAction()) && checkSelfPermission("android.permission.CALL_PHONE") != 0) {
                    sPendingAddItem = preparePendingAddArgs(13, intent, 0, (ItemInfo) obj);
                    requestPermissions(new String[]{"android.permission.CALL_PHONE"}, 13);
                    return false;
                }
                Toast.makeText(this, R.string.msg_no_generic_permission, 0).show();
                Log.e("Launcher", "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity. tag=" + obj + " intent=" + intent, e);
                return false;
            }
        } else {
            z = false;
        }
        LauncherAppsCompat launcherAppsCompat = LauncherAppsCompat.getInstance(this);
        UserHandleCompat userForSerialNumber = intent.hasExtra("profile") ? UserManagerCompat.getInstance(this).getUserForSerialNumber(intent.getLongExtra("profile", -1L)) : null;
        if (z) {
            if (Build.VERSION.SDK_INT >= 23) {
                int measuredWidth = view.getMeasuredWidth();
                int measuredHeight = view.getMeasuredHeight();
                if (!(view instanceof TextView) || (textViewIcon = Workspace.getTextViewIcon((TextView) view)) == null) {
                    iWidth = measuredWidth;
                    iWidth2 = 0;
                    paddingTop = 0;
                } else {
                    Rect bounds = textViewIcon.getBounds();
                    iWidth2 = (measuredWidth - bounds.width()) / 2;
                    paddingTop = view.getPaddingTop();
                    iWidth = bounds.width();
                    measuredHeight = bounds.height();
                }
                activityOptionsMakeCustomAnimation = ActivityOptions.makeClipRevealAnimation(view, iWidth2, paddingTop, iWidth, measuredHeight);
            } else if (Build.VERSION.SDK_INT < 21) {
                activityOptionsMakeCustomAnimation = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            } else {
                activityOptionsMakeCustomAnimation = Build.VERSION.SDK_INT >= 22 ? ActivityOptions.makeCustomAnimation(this, R.anim.task_open_enter, R.anim.no_anim) : null;
            }
            bundle = activityOptionsMakeCustomAnimation != null ? activityOptionsMakeCustomAnimation.toBundle() : null;
        } else {
            bundle = null;
        }
        ItemInfo itemInfo = obj instanceof ItemInfo ? (ItemInfo) obj : null;
        if (itemInfo != null && ((itemInfo.itemType == 1 || itemInfo.itemType == 6) && ((ShortcutInfo) itemInfo).promisedIntent == null)) {
            startShortcutIntent(intent, bundle, itemInfo);
        } else if (userForSerialNumber == null || userForSerialNumber.equals(UserHandleCompat.myUserHandle())) {
            try {
                startActivity(intent, bundle);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this, R.string.activity_not_found, 0).show();
                Log.e("Launcher", "Unable to launch. tag=" + obj + ". Activity Not Found. intent=" + intent, e2);
            } catch (Exception e3) {
                Log.e("Launcher", "Unable to launch. tag=" + obj + " intent=" + intent, e3);
            }
        } else {
            launcherAppsCompat.startActivityForProfile(intent.getComponent(), userForSerialNumber, intent.getSourceBounds(), bundle);
        }
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startAppShortcutOrInfoActivity(View view) {
        ShortcutInfo shortcutInfo;
        Intent intent;
        Object tag = view.getTag();
        if (tag instanceof ShortcutInfo) {
            shortcutInfo = (ShortcutInfo) tag;
            intent = shortcutInfo.intent;
            int[] iArr = new int[2];
            view.getLocationOnScreen(iArr);
            intent.setSourceBounds(new Rect(iArr[0], iArr[1], iArr[0] + view.getWidth(), iArr[1] + view.getHeight()));
        } else {
            if (!(tag instanceof AppInfo)) {
                throw new IllegalArgumentException("Input must be a Shortcut or AppInfo");
            }
            shortcutInfo = null;
            intent = ((AppInfo) tag).intent;
        }
        boolean zStartActivitySafely = this.mSearchItemShortcutHandler.handleIntent(intent) ? true : startActivitySafely(view, intent, tag);
        this.mAnalyticsHandler.recordAppLaunch(this, intent, "homescreen");
        ComponentName component = intent.getComponent();
        if (component != null) {
            String packageName = component.getPackageName();
            String className = component.getClassName();
            if (packageName != null && className != null) {
                new LocalAppData(packageName, className);
            }
        }
        if (zStartActivitySafely && (view instanceof BubbleTextView)) {
            this.mWaitingForResume = (BubbleTextView) view;
            this.mWaitingForResume.setStayPressed(true);
        }
    }

    private void startGlobalSearch(String str, boolean z, Bundle bundle, Rect rect) {
        ComponentName globalSearchActivity = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            Log.w("Launcher", "No global search activity found.");
            return;
        }
        Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
        intent.addFlags(268435456);
        intent.setComponent(globalSearchActivity);
        Bundle bundle2 = bundle == null ? new Bundle() : new Bundle(bundle);
        if (!bundle2.containsKey("source")) {
            bundle2.putString("source", getPackageName());
        }
        intent.putExtra("app_data", bundle2);
        if (!TextUtils.isEmpty(str)) {
            intent.putExtra("query", str);
        }
        if (z) {
            intent.putExtra("select_query", z);
        }
        intent.setSourceBounds(rect);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Log.e("Launcher", "Global search activity not found: " + globalSearchActivity);
        }
    }

    private void startShortcutIntent(Intent intent, Bundle bundle, ItemInfo itemInfo) {
        StrictMode.VmPolicy vmPolicy = StrictMode.getVmPolicy();
        try {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
            if (itemInfo.itemType == 6) {
                String deepShortcutId = ((ShortcutInfo) itemInfo).getDeepShortcutId();
                this.mDeepShortcutManager.startShortcut(intent.getPackage(), deepShortcutId, intent.getSourceBounds(), bundle, itemInfo.user);
            } else {
                startActivity(intent, bundle);
            }
        } finally {
            StrictMode.setVmPolicy(vmPolicy);
        }
    }

    private void updateBlurImageViewAlpha() {
        int iFloatToIntAlpha = AlphaUtil.floatToIntAlpha(AlphaUtil.limitAlpha(this.mBlurImageAlphaHelper.getCombinedFactor(CombinedFactorHelper.ADD_COMBINER)));
        CenterCropOffsetImageView centerCropOffsetImageView = this.mBlurImageView;
        if (centerCropOffsetImageView == null) {
            return;
        }
        centerCropOffsetImageView.setImageAlpha(iFloatToIntAlpha);
        if (iFloatToIntAlpha == 0) {
            this.mBlurLayoutView.setVisibility(4);
        } else {
            this.mBlurLayoutView.setVisibility(0);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateGoogleNowBackgroundIcon() {
        FrameLayout frameLayout = this.mGoogleNowAnimBackground;
        if (frameLayout == null) {
            return;
        }
        float y = frameLayout.getY();
        float f = this.mScreenHeight;
        float f2 = f - y;
        float f3 = this.mGoogleNowAnimFinalIconSize * (f2 / f);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mGoogleNowAnimIcon.getLayoutParams();
        int i = (int) f3;
        layoutParams.width = i;
        layoutParams.height = i;
        this.mGoogleNowAnimIcon.setLayoutParams(layoutParams);
        this.mGoogleNowAnimIcon.setY((f2 / 2.0f) - (f3 / 2.0f));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMainLayerScale() {
        this.mSearchUiHelper.updateMainLayerScale();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRunning() {
        boolean z = this.mVisible && this.mUserPresent && !this.mWidgetsToAdvance.isEmpty();
        if (z != this.mAutoAdvanceRunning) {
            this.mAutoAdvanceRunning = z;
            if (z) {
                long j = this.mAutoAdvanceTimeLeft;
                if (j == -1) {
                    j = 20000;
                }
                sendAdvanceMessage(j);
                return;
            }
            if (!this.mWidgetsToAdvance.isEmpty()) {
                this.mAutoAdvanceTimeLeft = Math.max(0L, 20000 - (System.currentTimeMillis() - this.mAutoAdvanceSentTime));
            }
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(0);
        }
    }

    private boolean waitUntilResume(Runnable runnable) {
        return waitUntilResume(runnable, false);
    }

    private boolean waitUntilResume(Runnable runnable, boolean z) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void writeConfiguration(Context context, LocaleConfiguration localeConfiguration) {
        /* REWORK: see readConfiguration; best-effort write and delete on failure. */
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(context.openFileOutput("launcher.preferences", 0));
            dataOutputStream.writeUTF(localeConfiguration.locale);
            dataOutputStream.writeInt(localeConfiguration.mcc);
            dataOutputStream.writeInt(localeConfiguration.mnc);
            dataOutputStream.flush();
        } catch (FileNotFoundException e) {
            /* REWORK: file open failures drop the write; harmless for a locale cache. */
        } catch (IOException e2) {
            try {
                context.getFileStreamPath("launcher.preferences").delete();
            } catch (RuntimeException unused) {
            }
        } catch (RuntimeException e3) {
            // drop
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
            } catch (IOException unused2) {
            }
        }
    }

    public void activateSearch(SearchInteractionMethod searchInteractionMethod, boolean z) {
        new AnalyticsHandler.EventProp().add("activate_method", searchInteractionMethod.toString());
        changeSearchMode(true, z);
    }

    void addAppWidgetFromDrop(PendingAddWidgetInfo pendingAddWidgetInfo, long j, long j2, int[] iArr, int[] iArr2, int[] iArr3) {
        resetAddInfo();
        ItemInfo itemInfo = this.mPendingAddInfo;
        pendingAddWidgetInfo.container = j;
        itemInfo.container = j;
        pendingAddWidgetInfo.screenId = j2;
        itemInfo.screenId = j2;
        itemInfo.dropPos = iArr3;
        itemInfo.minSpanX = pendingAddWidgetInfo.minSpanX;
        this.mPendingAddInfo.minSpanY = pendingAddWidgetInfo.minSpanY;
        if (iArr != null) {
            ItemInfo itemInfo2 = this.mPendingAddInfo;
            itemInfo2.cellX = iArr[0];
            itemInfo2.cellY = iArr[1];
        }
        if (iArr2 != null) {
            ItemInfo itemInfo3 = this.mPendingAddInfo;
            itemInfo3.spanX = iArr2[0];
            itemInfo3.spanY = iArr2[1];
        }
        AppWidgetHostView appWidgetHostView = pendingAddWidgetInfo.boundWidget;
        if (appWidgetHostView != null) {
            getDragLayer().removeView(appWidgetHostView);
            addAppWidgetImpl(appWidgetHostView.getAppWidgetId(), pendingAddWidgetInfo, appWidgetHostView, pendingAddWidgetInfo.info);
            pendingAddWidgetInfo.boundWidget = null;
            return;
        }
        int iAllocateAppWidgetId = getAppWidgetHost().allocateAppWidgetId();
        if (this.mAppWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, pendingAddWidgetInfo.info, pendingAddWidgetInfo.bindOptions)) {
            addAppWidgetImpl(iAllocateAppWidgetId, pendingAddWidgetInfo, null, pendingAddWidgetInfo.info);
            return;
        }
        this.mPendingAddWidgetInfo = pendingAddWidgetInfo.info;
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
        intent.putExtra("appWidgetId", iAllocateAppWidgetId);
        intent.putExtra("appWidgetProvider", pendingAddWidgetInfo.componentName);
        this.mAppWidgetManager.getUser(this.mPendingAddWidgetInfo).addToIntent(intent, "appWidgetProviderProfile");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 11);
            return;
        }
        Log.d("Launcher", "Cannot fetch activity for intent: " + intent.toString());
        Toast.makeText(this, getString(R.string.widget_could_not_be_added_error), 1).show();
    }

    void addAppWidgetImpl(int i, ItemInfo itemInfo, AppWidgetHostView appWidgetHostView, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo) {
        addAppWidgetImpl(i, itemInfo, appWidgetHostView, launcherAppWidgetProviderInfo, 0);
    }

    void addAppWidgetImpl(int i, ItemInfo itemInfo, AppWidgetHostView appWidgetHostView, LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo, int i2) {
        if (launcherAppWidgetProviderInfo.configure != null) {
            this.mPendingAddWidgetInfo = launcherAppWidgetProviderInfo;
            this.mPendingAddWidgetId = i;
            this.mAppWidgetManager.startConfigActivity(launcherAppWidgetProviderInfo, i, this, this.mAppWidgetHost, 5);
        } else {
            Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$cb4bKeVbHe8xQ9iO2NOBihYGnoY
                @Override // java.lang.Runnable
                public final void run() {
                    Launcher.this.exitSpringLoadedDragModeDelayed(true, 300, null);
                }
            };
            completeAddAppWidget(i, itemInfo.container, itemInfo.screenId, appWidgetHostView, launcherAppWidgetProviderInfo);
            this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, i2, false);
        }
    }

    FolderIcon addFolder(CellLayout cellLayout, long j, long j2, int i, int i2) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.title = getText(R.string.folder_name);
        LauncherModel.addItemToDatabase(this, folderInfo, j, j2, i, i2, false);
        sFolders.put(folderInfo.id, folderInfo);
        FolderIcon folderIconFromXml = FolderIcon.fromXml(R.layout.folder_icon, this, cellLayout, folderInfo, this.mIconCache);
        this.mWorkspace.addInScreen(folderIconFromXml, j, j2, i, i2, 1, 1, isWorkspaceLocked());
        this.mWorkspace.getParentCellLayoutForView(folderIconFromXml).getShortcutsAndWidgets().measureChild(folderIconFromXml);
        return folderIconFromXml;
    }

    public void addOnResumeCallback(Runnable runnable) {
        this.mOnResumeCallbacks.add(runnable);
    }

    public void addToCustomContentPage(View view, CustomContentCallbacks customContentCallbacks, String str) {
        this.mWorkspace.addToCustomContentPage(view, customContentCallbacks, str);
    }

    void addWidgetToAutoAdvanceIfNeeded(View view, AppWidgetProviderInfo appWidgetProviderInfo) {
        if (appWidgetProviderInfo == null || appWidgetProviderInfo.autoAdvanceViewId == -1) {
            return;
        }
        KeyEvent.Callback callbackFindViewById = view.findViewById(appWidgetProviderInfo.autoAdvanceViewId);
        if (callbackFindViewById instanceof Advanceable) {
            this.mWidgetsToAdvance.put(view, appWidgetProviderInfo);
            ((Advanceable) callbackFindViewById).fyiWillBeAdvancedByHostKThx();
            updateRunning();
        }
    }

    public void bindAddScreens(ArrayList<Long> arrayList) {
        addDumpLog("Launcher", "11683562 - bindAddScreens()", true);
        addDumpLog("Launcher", "11683562 -   orderedScreenIds: " + TextUtils.join(", ", arrayList), true);
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            this.mWorkspace.insertNewWorkspaceScreenBeforeEmptyScreen(arrayList.get(i).longValue());
        }
     }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindAllApplications(ArrayList<AppInfo> arrayList) {
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindAppWidget(final LauncherAppWidgetInfo launcherAppWidgetInfo) {
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfo;
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$LvPFL9-LtwmcXBoRs9H-TYPxcqs
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindAppWidget(launcherAppWidgetInfo);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from bindAppWidget()");
            return;
        }
        Workspace workspace = this.mWorkspace;
        if ((launcherAppWidgetInfo.restoreStatus & 2) == 0 && (launcherAppWidgetInfo.restoreStatus & 1) != 0) {
            LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfoFindProvider = this.mAppWidgetManager.findProvider(launcherAppWidgetInfo.providerName, launcherAppWidgetInfo.user);
            if (launcherAppWidgetProviderInfoFindProvider == null) {
                LauncherModel.deleteItemFromDatabase(this, launcherAppWidgetInfo);
                return;
            }
            PendingAddWidgetInfo pendingAddWidgetInfo = new PendingAddWidgetInfo(this, launcherAppWidgetProviderInfoFindProvider, null);
            pendingAddWidgetInfo.spanX = launcherAppWidgetInfo.spanX;
            pendingAddWidgetInfo.spanY = launcherAppWidgetInfo.spanY;
            pendingAddWidgetInfo.minSpanX = launcherAppWidgetInfo.minSpanX;
            pendingAddWidgetInfo.minSpanY = launcherAppWidgetInfo.minSpanY;
            Bundle defaultOptionsForWidget = WidgetHostViewLoader.getDefaultOptionsForWidget(this, pendingAddWidgetInfo);
            int iAllocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
            if (this.mAppWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, launcherAppWidgetProviderInfoFindProvider, defaultOptionsForWidget)) {
                launcherAppWidgetInfo.appWidgetId = iAllocateAppWidgetId;
                launcherAppWidgetInfo.restoreStatus = launcherAppWidgetProviderInfoFindProvider.configure == null ? 0 : 4;
                LauncherModel.updateItemInDatabase(this, launcherAppWidgetInfo);
            }
        }
        if (launcherAppWidgetInfo.restoreStatus == 0) {
            int i = launcherAppWidgetInfo.appWidgetId;
            LauncherAppWidgetProviderInfo launcherAppWidgetInfo2 = this.mAppWidgetManager.getLauncherAppWidgetInfo(i);
            launcherAppWidgetInfo.hostView = this.mAppWidgetHost.createView(this, i, launcherAppWidgetInfo2);
            launcherAppWidgetProviderInfo = launcherAppWidgetInfo2;
        } else {
            PendingAppWidgetHostView pendingAppWidgetHostView = new PendingAppWidgetHostView(this, launcherAppWidgetInfo);
            pendingAppWidgetHostView.updateIcon(this.mIconCache);
            launcherAppWidgetInfo.hostView = pendingAppWidgetHostView;
            launcherAppWidgetInfo.hostView.updateAppWidget(null);
            launcherAppWidgetInfo.hostView.setOnClickListener(this);
            launcherAppWidgetProviderInfo = null;
        }
        launcherAppWidgetInfo.hostView.setTag(launcherAppWidgetInfo);
        launcherAppWidgetInfo.onBindAppWidget(this);
        workspace.addInScreen(launcherAppWidgetInfo.hostView, launcherAppWidgetInfo.container, launcherAppWidgetInfo.screenId, launcherAppWidgetInfo.cellX, launcherAppWidgetInfo.cellY, launcherAppWidgetInfo.spanX, launcherAppWidgetInfo.spanY, false);
        addWidgetToAutoAdvanceIfNeeded(launcherAppWidgetInfo.hostView, launcherAppWidgetProviderInfo);
        workspace.requestLayout();
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindAppsAdded(final ArrayList<Long> arrayList, final ArrayList<ItemInfo> arrayList2, final ArrayList<ItemInfo> arrayList3, final ArrayList<AppInfo> arrayList4) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$BtYbm6PgdaT8jKfl7Z4KA956E2A
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindAppsAdded(arrayList, arrayList2, arrayList3, arrayList4);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from bindAppsAdded()");
            return;
        }
        if (arrayList != null) {
            bindAddScreens(arrayList);
        }
        if (arrayList2 != null && !arrayList2.isEmpty()) {
            bindItems(arrayList2, 0, arrayList2.size(), false, false);
        }
        if (arrayList3 != null && !arrayList3.isEmpty()) {
            bindItems(arrayList3, 0, arrayList3.size(), true, false);
        }
        this.mWorkspace.removeExtraEmptyScreen(false, false);
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindAppsRestored(final ArrayList<AppInfo> arrayList) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$dHD7PiccOJEIGfibOQ6bazWozco
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindAppsRestored(arrayList);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from bindAppsRestored()");
            return;
        }
        Workspace workspace = this.mWorkspace;
        if (workspace != null) {
            workspace.updateShortcutsAndWidgets(arrayList);
        }
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindAppsUpdated(final ArrayList<AppInfo> arrayList) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$8Klc9yS0vZkXsjWSkV-p4bknX6w
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindAppsUpdated(arrayList);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from bindAppsUpdated()");
            return;
        }
        Workspace workspace = this.mWorkspace;
        if (workspace != null) {
            workspace.updateShortcutsAndWidgets(arrayList);
        }
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindComponentsRemoved(final HashSet<String> hashSet, final HashSet<ComponentName> hashSet2, final UserHandleCompat userHandleCompat) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$nxm1DpGP9ASuLXtvALwRAQ4GBJU
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindComponentsRemoved(hashSet, hashSet2, userHandleCompat);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from bindComponentsRemoved()");
            return;
        }
        if (!hashSet.isEmpty()) {
            ItemInfoMatcher itemInfoMatcherOfPackages = ItemInfoMatcher.ofPackages(hashSet, userHandleCompat);
            this.mWorkspace.removeItemsByMatcher(itemInfoMatcherOfPackages);
            this.mDragController.onAppsRemoved(itemInfoMatcherOfPackages);
        }
        if (hashSet2.isEmpty()) {
            return;
        }
        ItemInfoMatcher itemInfoMatcherOfComponents = ItemInfoMatcher.ofComponents(hashSet2, userHandleCompat);
        this.mWorkspace.removeItemsByMatcher(itemInfoMatcherOfComponents);
        this.mDragController.onAppsRemoved(itemInfoMatcherOfComponents);
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindFolders(final LongArrayMap<FolderInfo> longArrayMap) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$8Fq9BnDbfIA-zasvKcJ3-zptmng
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindFolders(longArrayMap);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from bindFolders()");
        } else {
            sFolders.clear();
            sFolders = longArrayMap.clone();
        }
    }

    /* JADX WARN: Code duplicated, block: B:28:0x00ce  */
    /* JADX WARN: Code duplicated, block: B:36:0x0120  */
    /* JADX WARN: Code duplicated, block: B:41:0x014d  */
    /* JADX WARN: Code duplicated, block: B:59:0x0126 A[SYNTHETIC] */
    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindItems(final ArrayList<ItemInfo> arrayList, final int i, final int i2, final boolean z, boolean z2) {
        Workspace workspace;
        View viewCreateShortcut;
        Workspace workspace2;
        AnimatorSet animatorSet;
        CellLayout screenWithId;
        String str;
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$tCud5zZ9GSAFuLr9pyNfu03Wz-A
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindItems(arrayList, i, i2, z, false);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from bindItems()");
            return;
        }
        AnimatorSet animatorSetCreateAnimatorSet = LauncherAnimUtils.createAnimatorSet();
        final ArrayList arrayList2 = new ArrayList();
        boolean z3 = z && canRunNewAppsAnimation();
        Workspace workspace3 = this.mWorkspace;
        int i3 = i;
        long j = -1;
        while (i3 < i2) {
            ItemInfo itemInfo = arrayList.get(i3);
            if (itemInfo.container == -101 && this.mHotseat == null) {
                animatorSet = animatorSetCreateAnimatorSet;
                workspace2 = workspace3;
            } else {
                int i4 = itemInfo.itemType;
                if (i4 != 6) {
                    switch (i4) {
                        case 0:
                        case 1:
                            viewCreateShortcut = createShortcut((ShortcutInfo) itemInfo);
                            if (itemInfo.container == -100 && (screenWithId = this.mWorkspace.getScreenWithId(itemInfo.screenId)) != null && screenWithId.isOccupied(itemInfo.cellX, itemInfo.cellY)) {
                                str = "Collision while binding workspace item: " + itemInfo + ". Collides with " + screenWithId.getChildAt(itemInfo.cellX, itemInfo.cellY).getTag();
                                if (!LauncherAppState.isDogfoodBuild()) {
                                    throw new RuntimeException(str);
                                }
                                Log.d("Launcher", str);
                            }
                            workspace2 = workspace3;
                            animatorSet = animatorSetCreateAnimatorSet;
                            workspace3.addInScreenFromBind(viewCreateShortcut, itemInfo.container, itemInfo.screenId, itemInfo.cellX, itemInfo.cellY, 1, 1);
                            if (z3) {
                                viewCreateShortcut.setAlpha(0.0f);
                                viewCreateShortcut.setScaleX(0.0f);
                                viewCreateShortcut.setScaleY(0.0f);
                                arrayList2.add(createNewAppBounceAnimation(viewCreateShortcut, i3));
                                j = itemInfo.screenId;
                            }
                            break;
                        case 2:
                            FolderIcon folderIconFromXml = FolderIcon.fromXml(R.layout.folder_icon, this, (ViewGroup) workspace3.getChildAt(workspace3.getCurrentPage()), (FolderInfo) itemInfo, this.mIconCache);
                            workspace3.addInScreenFromBind(folderIconFromXml, itemInfo.container, itemInfo.screenId, itemInfo.cellX, itemInfo.cellY, 1, 1);
                            if (!z3) {
                                animatorSet = animatorSetCreateAnimatorSet;
                                workspace2 = workspace3;
                            } else {
                                folderIconFromXml.setAlpha(0.0f);
                                folderIconFromXml.setScaleX(0.0f);
                                folderIconFromXml.setScaleY(0.0f);
                                arrayList2.add(createNewAppBounceAnimation(folderIconFromXml, i3));
                                animatorSet = animatorSetCreateAnimatorSet;
                                workspace2 = workspace3;
                                j = itemInfo.screenId;
                            }
                            break;
                        default:
                            throw new RuntimeException("Invalid Item Type");
                    }
                } else {
                    viewCreateShortcut = createShortcut((ShortcutInfo) itemInfo);
                    if (itemInfo.container == -100) {
                        /* REWORK: the register var `screenWithId` was assigned in an earlier
                         * pass of this loop; requery rather than read a potentially
                         * uninitialized register. */
                        str = "Collision while binding workspace item: " + itemInfo + ". Collides with " + this.mWorkspace.getScreenWithId(itemInfo.screenId).getChildAt(itemInfo.cellX, itemInfo.cellY).getTag();
                        if (!LauncherAppState.isDogfoodBuild()) {
                            throw new RuntimeException(str);
                        }
                        Log.d("Launcher", str);
                    }
                    workspace2 = workspace3;
                    animatorSet = animatorSetCreateAnimatorSet;
                    workspace3.addInScreenFromBind(viewCreateShortcut, itemInfo.container, itemInfo.screenId, itemInfo.cellX, itemInfo.cellY, 1, 1);
                    if (z3) {
                        viewCreateShortcut.setAlpha(0.0f);
                        viewCreateShortcut.setScaleX(0.0f);
                        viewCreateShortcut.setScaleY(0.0f);
                        arrayList2.add(createNewAppBounceAnimation(viewCreateShortcut, i3));
                        j = itemInfo.screenId;
                    }
                }
                /* REWORK: jadx duplicated the loop increment at two edges; the dex has
                   a single add-int at the common join (:goto_4). Double-increment
                   skipped every other item (dock slots 1,3,5... never bound). */
                animatorSetCreateAnimatorSet = animatorSet;
                workspace3 = workspace2;
            }
            j = j;
            i3++;
            animatorSetCreateAnimatorSet = animatorSet;
            workspace3 = workspace2;
        }
        final AnimatorSet animatorSet2 = animatorSetCreateAnimatorSet;
        Workspace workspace4 = workspace3;
        long j2 = j;
        if (!z3 || j2 <= -1) {
            workspace = workspace4;
        } else {
            Workspace workspace5 = this.mWorkspace;
            long screenIdForPageIndex = workspace5.getScreenIdForPageIndex(workspace5.getNextPage());
            final int pageIndexForScreenId = this.mWorkspace.getPageIndexForScreenId(j2);
            workspace = workspace4;
            final Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$XvhTZaM44pmLCcv7X5G38guhK-E
                @Override // java.lang.Runnable
                public final void run() {
                    Launcher.lambda$bindItems$35(Launcher.this, animatorSet2, arrayList2, i2, arrayList);
                }
            };
            if (j2 == screenIdForPageIndex || z2) {
                this.mWorkspace.postDelayed(runnable, 500L);
            } else {
                this.mWorkspace.postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$puxwcDWYjiGFu7G3cLJj_ytq11o
                    @Override // java.lang.Runnable
                    public final void run() {
                        Launcher.lambda$bindItems$36(Launcher.this, pageIndexForScreenId, runnable);
                    }
                }, 500L);
            }
        }
        workspace.requestLayout();
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindScreens(ArrayList<Long> arrayList) {
        bindAddScreens(arrayList);
        if (arrayList.size() == 0) {
            this.mWorkspace.addExtraEmptyScreen();
        }
        /* REWORK: side-screen feed removed — no custom left page. */
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindShortcutsChanged(final ArrayList<ShortcutInfo> arrayList, final ArrayList<ShortcutInfo> arrayList2, final UserHandleCompat userHandleCompat) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$LinNY5Dj8nt7rfvK9_WPAnLqIhw
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindShortcutsChanged(arrayList, arrayList2, userHandleCompat);
            }
        })) {
            return;
        }
        if (!arrayList.isEmpty()) {
            this.mWorkspace.updateShortcuts(arrayList);
        }
        if (arrayList2.isEmpty()) {
            return;
        }
        HashSet hashSet = new HashSet();
        HashSet hashSet2 = new HashSet();
        for (ShortcutInfo shortcutInfo : arrayList2) {
            if (shortcutInfo.itemType == 6) {
                hashSet2.add(ShortcutKey.fromShortcutInfo(shortcutInfo));
            } else {
                hashSet.add(shortcutInfo.getTargetComponent());
            }
        }
        if (!hashSet.isEmpty()) {
            ItemInfoMatcher itemInfoMatcherOfComponents = ItemInfoMatcher.ofComponents(hashSet, userHandleCompat);
            this.mWorkspace.removeItemsByMatcher(itemInfoMatcherOfComponents);
            this.mDragController.onAppsRemoved(itemInfoMatcherOfComponents);
        }
        if (hashSet2.isEmpty()) {
            return;
        }
        ItemInfoMatcher itemInfoMatcherOfShortcutKeys = ItemInfoMatcher.ofShortcutKeys(hashSet2);
        this.mWorkspace.removeItemsByMatcher(itemInfoMatcherOfShortcutKeys);
        this.mDragController.onAppsRemoved(itemInfoMatcherOfShortcutKeys);
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void bindWidgetsRestored(final ArrayList<LauncherAppWidgetInfo> arrayList) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$PNqk4Bzu9PJ7EGRJQGOVIgKrUOE
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.bindWidgetsRestored(arrayList);
            }
        })) {
            return;
        }
        this.mWorkspace.widgetsRestored(arrayList);
    }

    public boolean canAccessWallpaper() {
        boolean z = WallpaperManager.getInstance(this).getWallpaperInfo() != null;
        boolean z2 = !Utilities.isAtLeast81() || ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == 0;
        return z2 && !z;
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsFragment.OnFragmentInteractionListener
    public boolean canShowAllAppsSetDefaultPrompt() {
        return true;
    }

    public void changeAllAppsVisibility(boolean z, boolean z2) {
        if (this.mAllAppsFragment == null) {
            this.mAllAppsMotionTracker.setFactor(1.0f);
            return;
        }
        if (z) {
            if (z2) {
                this.mAllAppsMotionTracker.setEndValue(0.0f);
                return;
            } else {
                this.mAllAppsMotionTracker.setFactor(0.0f);
                return;
            }
        }
        if (z2) {
            this.mAllAppsMotionTracker.setEndValue(1.0f);
        } else {
            this.mAllAppsMotionTracker.setFactor(1.0f);
        }
    }


    void closeFolder(Folder folder) {
        if (folder == null) {
            return;
        }
        folder.getInfo().opened = false;
        this.mDragController.removeDropTarget(folder);
        for (FolderFragment folderFragment : new ArrayList<>(this.mFolderFragments)) {
            if (folderFragment.getFolder() == folder) {
                folderFragment.runExitAnimation();
            }
        }
        getDragLayer().sendAccessibilityEvent(32);
    }

    public boolean closeFolder() {
        Folder openFolder = this.mDragLayer != null ? getOpenFolder() : null;
        if (openFolder != null) {
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
            }
            closeFolder(openFolder);
        }
        return openFolder != null;
    }

    public void closeSystemDialogs() {
        getWindow().closeAllPanels();
        setWaitingForResult(false);
    }

    View createShortcut(int i, CellLayout cellLayout, ShortcutInfo shortcutInfo) {
        BubbleTextView bubbleTextView = (BubbleTextView) this.mInflater.inflate(i, (ViewGroup) cellLayout, false);
        if (cellLayout != null) {
            bubbleTextView.setCellParams(cellLayout.getCellParams());
        }
        bubbleTextView.applyFromShortcutInfo(shortcutInfo, this.mIconCache);
        bubbleTextView.setOnClickListener(this);
        bubbleTextView.setOnFocusChangeListener(this.mFocusHandler);
        return bubbleTextView;
    }

    View createShortcut(ShortcutInfo shortcutInfo) {
        Workspace workspace = this.mWorkspace;
        return createShortcut(R.layout.application, (CellLayout) workspace.getChildAt(workspace.getCurrentPage()), shortcutInfo);
    }

    @Override // androidx.core.app.ComponentActivity, android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getAction() == 0) {
            int keyCode = keyEvent.getKeyCode();
            if (keyCode == 3) {
                return true;
            }
            if (keyCode == 25 && isPropertyEnabled("launcher_dump_state")) {
                dumpState();
                return true;
            }
        } else if (keyEvent.getAction() == 1 && keyEvent.getKeyCode() == 3) {
            return true;
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        boolean zDispatchPopulateAccessibilityEvent = super.dispatchPopulateAccessibilityEvent(accessibilityEvent);
        List<CharSequence> text = accessibilityEvent.getText();
        text.clear();
        if (this.mState != State.APPS_CUSTOMIZE) {
            text.add(getString(R.string.all_apps_home_button_label));
        }
        return zDispatchPopulateAccessibilityEvent;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(str, fileDescriptor, printWriter, strArr);
        synchronized (sDumpLogs) {
            printWriter.println(" ");
            printWriter.println("Debug logs: ");
            for (int i = 0; i < sDumpLogs.size(); i++) {
                printWriter.println("  " + sDumpLogs.get(i));
            }
        }
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void dumpLogsToLocalData() {
    }

    public void dumpState() {
        Log.d("Launcher", "BEGIN launcher3 dump state for launcher " + this);
        Log.d("Launcher", "mSavedState=" + this.mSavedState);
        Log.d("Launcher", "mWorkspaceLoading=" + this.mWorkspaceLoading);
        Log.d("Launcher", "mRestoring=" + this.mRestoring);
        Log.d("Launcher", "mWaitingForResult=" + this.mWaitingForResult);
        Log.d("Launcher", "sFolders.size=" + sFolders.size());
        this.mModel.dumpState();
        Log.d("Launcher", "END launcher3 dump state");
    }

    void enterSpringLoadedDragMode() {
        if (isAllAppsVisible()) {
            hideAppsCustomizeHelper(Workspace.State.SPRING_LOADED, true, true, null);
            this.mState = State.APPS_CUSTOMIZE_SPRING_LOADED;
        }
    }

    boolean exitSpringLoadedDragMode() {
        if (this.mState != State.APPS_CUSTOMIZE_SPRING_LOADED) {
            return false;
        }
        showWorkspace(true, null);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void exitSpringLoadedDragModeDelayed(final boolean z, int i, final Runnable runnable) {
        if (this.mState != State.APPS_CUSTOMIZE_SPRING_LOADED) {
            return;
        }
        this.mHandler.postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$haSKoLK4vpeWjxWPd2UweqcQrlg
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.lambda$exitSpringLoadedDragModeDelayed$32(Launcher.this, z, runnable);
            }
        }, i);
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void finishBindingItems(final boolean z) {
        if (waitUntilResume(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$WvNwIP9V35gLyoTXUaj81yF5uAo
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.finishBindingItems(z);
            }
        })) {
            Timber.tag("DEBUG-HOME-DELAY").d("++ waitUntilResume - task added from finishBindingItems()");
            return;
        }
        if (this.mSavedState != null) {
            if (!this.mWorkspace.hasFocus()) {
                Workspace workspace = this.mWorkspace;
                workspace.getChildAt(workspace.getCurrentPage()).requestFocus();
            }
            this.mSavedState = null;
        }
        this.mWorkspace.restoreInstanceStateForRemainingPages();
        setWorkspaceLoading(false);
        sendLoadingCompleteBroadcastIfNecessary();
        PendingAddArguments pendingAddArguments = sPendingAddItem;
        if (pendingAddArguments != null) {
            final long jCompleteAdd = completeAdd(pendingAddArguments);
            this.mWorkspace.post(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$vk1ZNPDriORgDSYhwqpa2Jtc914
                @Override // java.lang.Runnable
                public final void run() {
                    Launcher.this.mWorkspace.snapToScreenId(jCompleteAdd);
                }
            });
            sPendingAddItem = null;
        }
        if (z) {
            this.mWorkspace.getUniqueComponents(true, null);
            mIntentsOnWorkspaceFromUpgradePath = this.mWorkspace.getUniqueComponents(true, null);
        }
        PackageInstallerCompat.getInstance(this).onFinishBind();
        this.mModel.recheckRestoredItems(this);
        this.localAppsManager.finishStartup();
        this.mDeepShortcutManager.finishStartup();
    }

    public void forceCloseFolders() {
        ArrayList<FolderFragment> arrayList = new ArrayList(this.mFolderFragments);
        boolean z = arrayList.size() > 0;
        for (FolderFragment folderFragment : arrayList) {
            if (folderFragment.getFolder() != null && folderFragment.getFolder().getInfo() != null) {
                folderFragment.getFolder().getInfo().opened = false;
            }
            onFolderFragmentWillExit(folderFragment);
            onFolderFragmentExit(folderFragment);
        }
        this.mFolderFragments.clear();
        this.mBackgroundFolderAnimationHelper.setAnimatedValue(1.0f);
        if (z) {
            getDragLayer().sendAccessibilityEvent(32);
        }
    }

    public LauncherAppWidgetHost getAppWidgetHost() {
        return this.mAppWidgetHost;
    }

    CellLayout getCellLayout(long j, long j2) {
        if (j != -101) {
            return this.mWorkspace.getScreenWithId(j2);
        }
        Hotseat hotseat = this.mHotseat;
        if (hotseat != null) {
            return hotseat.getLayout();
        }
        return null;
    }

    protected Fragment getCurrentStackFragment() {
        return this.mFragmentHelper.getCurrentStackFragment();
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public int getCurrentWorkspaceScreen() {
        Workspace workspace = this.mWorkspace;
        if (workspace != null) {
            return workspace.getCurrentPage();
        }
        return 2;
    }

    public DragController getDragController() {
        return this.mDragController;
    }

    public DragLayer getDragLayer() {
        return this.mDragLayer;
    }

    protected Intent getFirstRunActivity() {
        Intent intent = new Intent();
        intent.setClassName(this, "com.voxel.simplesearchlauncher.activity.OnboardingActivity");
        return intent;
    }

    public View.OnTouchListener getHapticFeedbackTouchListener() {
        if (this.mHapticFeedbackTouchListener == null) {
            this.mHapticFeedbackTouchListener = new View.OnTouchListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$WGjgd4TKg9qXNRsG2jiVaz90yb4
                @Override // android.view.View.OnTouchListener
                public final boolean onTouch(View view, MotionEvent motionEvent) {
                    return Launcher.lambda$getHapticFeedbackTouchListener$27(view, motionEvent);
                }
            };
        }
        return this.mHapticFeedbackTouchListener;
    }

    public Hotseat getHotseat() {
        return this.mHotseat;
    }

    public AlphaSet getHotseatAlpha() {
        return this.mHotseatAlpha;
    }

    public AlphaSet getIndicatorAlpha() {
        return this.mIndicatorAlpha;
    }



    public AlphaSet getMainLayerDarkenAlpha() {
        return this.mMainLayerDarkenAlpha;
    }

    public LauncherModel getModel() {
        return this.mModel;
    }

    public Folder getOpenFolder() {
        Iterator it = new ArrayList(this.mFolderFragments).iterator();
        while (it.hasNext()) {
            Folder folder = ((FolderFragment) it.next()).getFolder();
            if (folder != null && folder.getInfo().opened) {
                return folder;
            }
        }
        return null;
    }

    public ViewGroup getOverviewPanel() {
        return this.mOverviewPanel;
    }

    public AlphaSet getOverviewPanelAlpha() {
        return this.mOverviewPanelAlpha;
    }

    public PullDownRecyclerView getPullDownRecyclerView() {
        return this.mPullDownRecyclerView;
    }

    public View getQsbBar() {
        return this.mQsb;
    }




    public SearchDropTargetBar getSearchBar() {
        return this.mSearchDropTargetBar;
    }

    public int getSearchBarHiddenHeight() {
        return Math.round(((-((View) this.mQsb.getParent()).getPaddingTop()) - ((View) this.mQsb.getParent().getParent()).getPaddingTop()) - (this.mQsbSearchView.getHeight() * 1.05f));
    }


    public LauncherStackableFragment getStackFragmentAt(int i) {
        return this.mFragmentHelper.getStackFragmentAt(i);
    }

    public int getStackFragmentCount() {
        return this.mFragmentHelper.getStackFragmentCount();
    }

    public int getViewIdForItem(ItemInfo itemInfo) {
        int i = (int) itemInfo.id;
        if (this.mItemIdToViewId.containsKey(Integer.valueOf(i))) {
            return this.mItemIdToViewId.get(Integer.valueOf(i)).intValue();
        }
        int iGenerateViewId = generateViewId();
        this.mItemIdToViewId.put(Integer.valueOf(i), Integer.valueOf(iGenerateViewId));
        return iGenerateViewId;
    }

    protected ComponentName getWallpaperPickerComponent() {
        return new ComponentName(getPackageName(), LauncherWallpaperPickerActivity.class.getName());
    }

    public Workspace getWorkspace() {
        return this.mWorkspace;
    }



    protected boolean hasFirstRunActivity() {
        return true;
    }

    protected boolean hasSettings() {
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment.OnFragmentInteractionListener
    public void hideBlurImage() {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$bRk2eSxa1UDcCxrPMQfXdUUPTFo
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                Launcher.this.setFragmentBlurAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        animatorSet.play(valueAnimatorOfFloat);
        float f = 1;
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mSearchDropTargetBarAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mIndicatorAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mWorkspaceAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mHotseatAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mOverviewPanelAlpha, this, f));
        animatorSet.start();
    }

    public void hideSpecialFragments() {
        if (this.mAllAppsFragment != null && this.mAllAppsMotionTracker.getCurrentFactor() != 1.0f) {
            this.mAllAppsFragment.setVisible(false);
        }
        Iterator<FolderFragment> it = this.mFolderFragments.iterator();
        while (it.hasNext()) {
            it.next().setVisible(false);
        }
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public boolean isAllAppsButtonRank(int i) {
        return false;
    }

    public boolean isAllAppsVisible() {
        return this.mState == State.APPS_CUSTOMIZE || this.mOnResumeState == State.APPS_CUSTOMIZE;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.OnFolderFragmentInteractionListener
    public boolean isDragging() {
        return this.mDragController.isDragging();
    }

    public boolean isDraggingEnabled() {
        return !this.mModel.isLoadingWorkspace();
    }

    boolean isHotseatLayout(View view) {
        Hotseat hotseat = this.mHotseat;
        return hotseat != null && view != null && (view instanceof CellLayout) && view == hotseat.getLayout();
    }

    public boolean isLauncherDefault() {
        ArrayList arrayList = new ArrayList();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");
        intentFilter.addCategory("android.intent.category.HOME");
        arrayList.add(intentFilter);
        String packageName = getPackageName();
        PackageManager packageManager = getPackageManager();
        ArrayList arrayList2 = new ArrayList();
        packageManager.getPreferredActivities(arrayList, arrayList2, packageName);
        Iterator<ComponentName> it = arrayList2.iterator();
        while (it.hasNext()) {
            if (it.next().getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnCustomContent() {
        return this.mWorkspace.isOnOrMovingToCustomContent();
    }

    public boolean isRotationEnabled() {
        return sForceEnableRotation || getResources().getBoolean(R.bool.allow_rotation);
    }

    public boolean isWorkspaceLoading() {
        return this.mWorkspaceLoading;
    }

    public boolean isWorkspaceLocked() {
        return this.mWorkspaceLoading || this.mWaitingForResult;
    }

    public void launchGoogleNowActivity(boolean z) {
        ComponentName globalSearchActivity = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
        if (globalSearchActivity == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.ASSIST");
        intent.addFlags(268468224 | (z ? 65536 : 0));
        intent.setComponent(globalSearchActivity);
        LaunchUtil.startActivitySafely(getApplicationContext(), intent);
        if (z) {
            overridePendingTransition(android.R.anim.fade_in, R.anim.no_anim);
        }
    }

    public void lockScreenOrientation() {
        if (isRotationEnabled()) {
            setRequestedOrientation(mapConfigurationOriActivityInfoOri(getResources().getConfiguration().orientation));
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int i, final int i2, Intent intent) {
        setWaitingForResult(false);
        if (i == 14) {
            return;
        }
        final int i3 = this.mPendingAddWidgetId;
        this.mPendingAddWidgetId = -1;
        Runnable runnable = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$ScNnRdmDAXxHvT8hK51OmWraQl4
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.this.exitSpringLoadedDragModeDelayed(i2 != 0, 300, null);
            }
        };
        boolean zIsWorkspaceLocked = isWorkspaceLocked();
        if (i == 11) {
            int intExtra = intent != null ? intent.getIntExtra("appWidgetId", -1) : -1;
            if (i2 == 0) {
                completeTwoStageWidgetDrop(0, intExtra);
                this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, 500, false);
                return;
            } else {
                if (i2 == -1) {
                    addAppWidgetImpl(intExtra, this.mPendingAddInfo, null, this.mPendingAddWidgetInfo, 500);
                    return;
                }
                return;
            }
        }
        if (i == 111) {
            if (i2 == -1) {
                LauncherAppWidgetHostView widgetForAppWidgetId = this.mWorkspace.getWidgetForAppWidgetId(intent != null ? intent.getIntExtra("appWidgetId", -1) : -1);
                if (widgetForAppWidgetId == null || !(widgetForAppWidgetId instanceof PendingAppWidgetHostView)) {
                    Log.e("Launcher", "Widget update called, when the widget no longer exists.");
                    return;
                }
                LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) widgetForAppWidgetId.getTag();
                launcherAppWidgetInfo.restoreStatus = this.mPendingAddWidgetInfo.configure != null ? 4 : 0;
                LauncherModel.updateItemInDatabase(this, launcherAppWidgetInfo);
                if (this.mPendingAddWidgetInfo.configure != null) {
                    reconfigureWidget((PendingAppWidgetHostView) widgetForAppWidgetId);
                    return;
                }
                PendingAddArguments pendingAddArgumentsPreparePendingAddArgs = preparePendingAddArgs(i, intent, i3, this.mPendingAddInfo);
                if (zIsWorkspaceLocked) {
                    sPendingAddItem = pendingAddArgumentsPreparePendingAddArgs;
                    return;
                } else {
                    completeAdd(pendingAddArgumentsPreparePendingAddArgs);
                    return;
                }
            }
            return;
        }
        if (i == 10) {
            if (i2 == -1 && this.mWorkspace.isInOverviewMode()) {
                this.mWorkspace.exitOverviewMode(false);
                return;
            }
            return;
        }
        if (!(i == 9 || i == 5)) {
            if (i == 12) {
                if (i2 == -1) {
                    PendingAddArguments pendingAddArgumentsPreparePendingAddArgs2 = preparePendingAddArgs(i, intent, i3, this.mPendingAddInfo);
                    if (zIsWorkspaceLocked) {
                        sPendingAddItem = pendingAddArgumentsPreparePendingAddArgs2;
                        return;
                    } else {
                        completeAdd(pendingAddArgumentsPreparePendingAddArgs2);
                        return;
                    }
                }
                return;
            }
            if (i2 == -1 && this.mPendingAddInfo.container != -1) {
                PendingAddArguments pendingAddArgumentsPreparePendingAddArgs3 = preparePendingAddArgs(i, intent, -1, this.mPendingAddInfo);
                if (isWorkspaceLocked()) {
                    sPendingAddItem = pendingAddArgumentsPreparePendingAddArgs3;
                } else {
                    completeAdd(pendingAddArgumentsPreparePendingAddArgs3);
                    this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, 500, false);
                }
            } else if (i2 == 0) {
                this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable, 500, false);
            }
            this.mDragLayer.clearAnimatedView();
            return;
        }
        int widgetId = i3; // REWORK: jadx merged this with the effectively-final lambda capture above.
        int intExtra2 = intent != null ? intent.getIntExtra("appWidgetId", -1) : -1;
        if (intExtra2 >= 0) {
            widgetId = intExtra2;
        }
        if (widgetId < 0 || i2 == 0) {
            Log.e("Launcher", "Error: appWidgetId (EXTRA_APPWIDGET_ID) was not returned from the widget configuration activity.");
            completeTwoStageWidgetDrop(0, widgetId);
            Runnable runnable2 = new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$L-jRKWzPvvf7CTPfJnpHeu7B2jE
                @Override // java.lang.Runnable
                public final void run() {
                    Launcher.this.exitSpringLoadedDragModeDelayed(false, 0, null);
                }
            };
            if (zIsWorkspaceLocked) {
                this.mWorkspace.postDelayed(runnable2, 500L);
                return;
            } else {
                this.mWorkspace.removeExtraEmptyScreenDelayed(true, runnable2, 500, false);
                return;
            }
        }
        if (zIsWorkspaceLocked) {
            sPendingAddItem = preparePendingAddArgs(i, intent, widgetId, this.mPendingAddInfo);
            return;
        }
        if (this.mPendingAddInfo.container == -100) {
            ItemInfo itemInfo = this.mPendingAddInfo;
            itemInfo.screenId = ensurePendingDropLayoutExists(itemInfo.screenId);
        }
        final CellLayout screenWithId = this.mWorkspace.getScreenWithId(this.mPendingAddInfo.screenId);
        screenWithId.setDropPending(true);
        this.mWorkspace.removeExtraEmptyScreenDelayed(true, new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$uxuwNko07SMmckGOLLuPJhg3HCA
            @Override // java.lang.Runnable
            public final void run() {
                Launcher.lambda$onActivityResult$2(Launcher.this, i2, i3, screenWithId);
            }
        }, 500, false);
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsFragment.OnFragmentInteractionListener
    public boolean onAllAppsDropCompleted(AllAppsFragment allAppsFragment, boolean z) {
        exitSpringLoadedDragModeDelayed(true, 300, null);
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.allapps.AllAppsFragment.OnFragmentInteractionListener
    public boolean onAllAppsLongPress(AllAppsFragment allAppsFragment, View view, LocalAppData localAppData) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setClassName(localAppData.getPackageName(), localAppData.getActivityName());
        intent.setFlags(270532608);
        View viewFindViewById = view.findViewById(R.id.list_item_icon);
        ItemInfo itemInfoCreateAppDragInfo = this.mSearchItemShortcutHandler.createAppDragInfo(this, intent);
        if (itemInfoCreateAppDragInfo == null) {
            return false;
        }
        viewFindViewById.setTag(itemInfoCreateAppDragInfo);
        startDrag(viewFindViewById, itemInfoCreateAppDragInfo, allAppsFragment);
        return true;
    }

    /* JADX WARN: Code duplicated, block: B:17:0x0039  */
    @Override // com.voxel.simplesearchlauncher.widget.WidgetsFragment.OnFragmentInteractionListener
    public boolean onAllWidgetsDropCompleted(WidgetsFragment widgetsFragment, View view, DropTarget.DragObject dragObject, boolean z, boolean z2) {
        boolean z3;
        if (z || !z2 || (view != getWorkspace() && !(view instanceof Folder))) {
            exitSpringLoadedDragModeDelayed(true, 300, null);
        }
        unlockScreenOrientation(false);
        if (!z2) {
            if (view instanceof Workspace) {
                CellLayout cellLayout = (CellLayout) ((Workspace) view).getChildAt(getCurrentWorkspaceScreen());
                ItemInfo itemInfo = dragObject.dragInfo;
                if (cellLayout != null) {
                    z3 = !cellLayout.findCellForSpan(null, itemInfo.spanX, itemInfo.spanY);
                } else {
                    z3 = false;
                }
            } else {
                z3 = false;
            }
            if (z3) {
                showOutOfSpaceMessage(false);
            }
            dragObject.deferDragViewCleanupPostAnimation = false;
        }
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.widget.WidgetsFragment.OnFragmentInteractionListener
    public boolean onAllWidgetsLongPress(WidgetsFragment widgetsFragment, View view, PendingAddItemInfo pendingAddItemInfo, Bitmap bitmap, Bitmap bitmap2, boolean z) {
        DragOptions dragOptions = new DragOptions();
        getWorkspace().prepareDragWithProvider(new PendingItemPreviewProvider(view, pendingAddItemInfo, bitmap2));
        this.mDragController.startDragTouchCentered(bitmap2, widgetsFragment, pendingAddItemInfo, null, null, 0.8f, dragOptions);
        if (getDragController().isDragging()) {
            this.mState = State.APPS_CUSTOMIZE;
            enterSpringLoadedDragMode();
            popStackFragment(widgetsFragment, true);
        }
        return true;
    }

    protected void onAssistIntent(boolean z, boolean z2) {
        activateSearch(SearchInteractionMethod.ASSIST, z);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        /* REWORK: API 34+ exported-state rule; filters are system broadcasts. */
        ContextCompat.registerReceiver(this, this.mReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        FirstFrameAnimatorHelper.initializeDrawListener(getWindow().getDecorView());
        Utilities.setupTransparentSystemBarsForLmp(this);
        this.mAttached = true;
        this.mVisible = true;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        LauncherStackableFragment launcherStackableFragment = (LauncherStackableFragment) getCurrentStackFragment();
        if (launcherStackableFragment != null) {
            launcherStackableFragment.runExitAnimation();
            return;
        }
        if (this.mAllAppsMotionTracker.getCurrentFactor() != 1.0f) {
            changeAllAppsVisibility(false, true);
            return;
        }
        if (isAllAppsVisible()) {
            showOverviewMode(true);
            return;
        }
        if (this.mWorkspace.isInOverviewMode()) {
            this.mWorkspace.exitOverviewMode(true);
            return;
        }
        SpringySearchView springySearchView = this.mQsbSearchView;
        if (springySearchView != null && springySearchView.getCurrentFactor() == 0.0f) {
            deactivateSearch(SearchInteractionMethod.BACK, !this.isStopped);
            return;
        }
        Folder openFolder = getOpenFolder();
        if (openFolder != null) {
            if (openFolder.isEditingName()) {
                openFolder.dismissEditingName();
                return;
            } else {
                closeFolder();
                return;
            }
        }
        if (!this.mWorkspace.isOnOrMovingToCustomContent()) {
            this.mWorkspace.exitWidgetResizeMode();
            this.mWorkspace.showOutlinesTemporarily();
        } else {
            this.mWorkspace.moveToDefaultScreen(true);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getWindowToken() == null) {
            return;
        }
        SpringySearchView springySearchView = this.mQsbSearchView;
        if ((springySearchView == null || springySearchView.getCurrentFactor() <= 0.0f || this.mQsbSearchView.getCurrentFactor() >= 1.0f) && this.mWorkspace.isFinishedSwitchingState()) {
            if (view instanceof Workspace) {
                if (this.mWorkspace.isInOverviewMode()) {
                    this.mWorkspace.exitOverviewMode(true);
                    return;
                }
                return;
            }
            if ((view instanceof CellLayout) && this.mWorkspace.isInOverviewMode()) {
                Workspace workspace = this.mWorkspace;
                workspace.exitOverviewMode(workspace.indexOfChild(view), true);
            }
            Object tag = view.getTag();
            if (tag instanceof ShortcutInfo) {
                onClickAppShortcut(view);
                return;
            }
            if (tag instanceof FolderInfo) {
                if (view instanceof FolderIcon) {
                    onClickFolderIcon(view);
                }
            } else {
                if (view == this.mAllAppsButton) {
                    return;
                }
                if (tag instanceof AppInfo) {
                    startAppShortcutOrInfoActivity(view);
                } else if ((tag instanceof LauncherAppWidgetInfo) && (view instanceof PendingAppWidgetHostView)) {
                    onClickPendingWidget((PendingAppWidgetHostView) view);
                }
            }
        }
    }

    protected void onClickAddWidgetButton(View view) {
        if (this.mSettingsStorage.isDesktopLockedCurrently()) {
            pushStackFragment(new LockOverlayFragment());
        } else {
            pushStackFragment(WidgetsFragment.newInstance());
        }
    }

    protected void onClickAppShortcut(final View view) {
        Object tag = view.getTag();
        if (!(tag instanceof ShortcutInfo)) {
            throw new IllegalArgumentException("Input must be a Shortcut");
        }
        ShortcutInfo shortcutInfo = (ShortcutInfo) tag;
        Intent intent = shortcutInfo.intent;
        if ("android.intent.action.CALL_PRIVILEGED".equals(intent.getAction())) {
            intent.setAction("android.intent.action.CALL");
        }
        if (shortcutInfo.isDisabled != 0 && (shortcutInfo.isDisabled & (-5) & (-9)) != 0) {
            if (!TextUtils.isEmpty(shortcutInfo.disabledMessage)) {
                Toast.makeText(this, shortcutInfo.disabledMessage, 0).show();
                return;
            }
            int i = R.string.activity_not_found;
            if ((shortcutInfo.isDisabled & 1) != 0) {
                i = R.string.safemode_shortcut_error;
            } else if ((shortcutInfo.isDisabled & 16) != 0 || (shortcutInfo.isDisabled & 32) != 0) {
                i = R.string.shortcut_not_available;
            }
            Toast.makeText(this, i, 0).show();
            return;
        }
        /* REWORK: MemoryDumpActivity / ToggleWeightWatcher debug shortcuts removed. */
        if ((view instanceof BubbleTextView) && shortcutInfo.isPromise() && !shortcutInfo.hasStatusFlag(4)) {
            showBrokenAppInstallDialog(shortcutInfo.getTargetComponent().getPackageName(), new DialogInterface.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$JL9YE_-t4w1nwULsQukRbUYlwcs
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i2) {
                    Launcher.this.startAppShortcutOrInfoActivity(view);
                }
            });
        } else {
            startAppShortcutOrInfoActivity(view);
        }
    }

    protected void onClickFolderIcon(View view) {
        if (!(view instanceof FolderIcon)) {
            throw new IllegalArgumentException("Input must be a FolderIcon");
        }
        FolderIcon folderIcon = (FolderIcon) view;
        FolderInfo folderInfo = folderIcon.getFolderInfo();
        Folder openFolder = getOpenFolder();
        if (folderInfo.opened && openFolder == null) {
            Log.d("Launcher", "Folder info marked as open, but associated folder is not open. Screen: " + folderInfo.screenId + " (" + folderInfo.cellX + ", " + folderInfo.cellY + ")");
            folderInfo.opened = false;
        }
        if (folderInfo.opened && openFolder != null && openFolder.getInfo() == folderInfo) {
            return;
        }
        if (!folderInfo.opened && !folderIcon.getFolder().isDestroyed()) {
            closeFolder();
            openFolder(folderIcon);
        } else if (openFolder != null) {
            int pageForView = this.mWorkspace.getPageForView(openFolder);
            closeFolder(openFolder);
            if (pageForView != this.mWorkspace.getCurrentPage()) {
                closeFolder();
                openFolder(folderIcon);
            }
        }
    }


    public void onClickPendingWidget(final PendingAppWidgetHostView pendingAppWidgetHostView) {
        final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) pendingAppWidgetHostView.getTag();
        if ((launcherAppWidgetInfo.restoreStatus & 2) != 0 || (launcherAppWidgetInfo.restoreStatus & 1) == 0) {
            if (pendingAppWidgetHostView.isReadyForClickSetup()) {
                reconfigureWidget(pendingAppWidgetHostView);
                return;
            } else if (launcherAppWidgetInfo.installProgress >= 0) {
                startActivitySafely(pendingAppWidgetHostView, LauncherModel.getMarketIntent(launcherAppWidgetInfo.providerName.getPackageName()), launcherAppWidgetInfo);
                return;
            } else {
                final String packageName = launcherAppWidgetInfo.providerName.getPackageName();
                showBrokenAppInstallDialog(packageName, new DialogInterface.OnClickListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$-p6La6hTpXv01UtHvHx5czN1Et0
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        Launcher.this.startActivitySafely(pendingAppWidgetHostView, LauncherModel.getMarketIntent(packageName), launcherAppWidgetInfo);
                    }
                });
                return;
            }
        }
        LauncherAppWidgetProviderInfo launcherAppWidgetProviderInfoFindProvider = AppWidgetManagerCompat.getInstance(this).findProvider(launcherAppWidgetInfo.providerName, launcherAppWidgetInfo.user);
        PendingAddWidgetInfo pendingAddWidgetInfo = new PendingAddWidgetInfo(this, launcherAppWidgetProviderInfoFindProvider, null);
        pendingAddWidgetInfo.spanX = launcherAppWidgetInfo.spanX;
        pendingAddWidgetInfo.spanY = launcherAppWidgetInfo.spanY;
        pendingAddWidgetInfo.minSpanX = launcherAppWidgetInfo.minSpanX;
        pendingAddWidgetInfo.minSpanY = launcherAppWidgetInfo.minSpanY;
        Bundle defaultOptionsForWidget = WidgetHostViewLoader.getDefaultOptionsForWidget(this, pendingAddWidgetInfo);
        int iAllocateAppWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        launcherAppWidgetInfo.appWidgetId = iAllocateAppWidgetId;
        boolean zBindAppWidgetIdIfAllowed = this.mAppWidgetManager.bindAppWidgetIdIfAllowed(iAllocateAppWidgetId, launcherAppWidgetProviderInfoFindProvider, defaultOptionsForWidget);
        if (zBindAppWidgetIdIfAllowed) {
            launcherAppWidgetInfo.restoreStatus = launcherAppWidgetProviderInfoFindProvider.configure == null ? 0 : 4;
            LauncherModel.updateItemInDatabase(this, launcherAppWidgetInfo);
            if (launcherAppWidgetProviderInfoFindProvider.configure != null) {
                reconfigureWidget(pendingAppWidgetHostView);
            } else {
                completeAdd(preparePendingAddArgs(111, null, iAllocateAppWidgetId, launcherAppWidgetInfo));
            }
        }
        if (zBindAppWidgetIdIfAllowed) {
            return;
        }
        this.mPendingAddWidgetInfo = launcherAppWidgetProviderInfoFindProvider;
        Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
        intent.putExtra("appWidgetId", iAllocateAppWidgetId);
        intent.putExtra("appWidgetProvider", launcherAppWidgetInfo.providerName);
        this.mAppWidgetManager.getUser(this.mPendingAddWidgetInfo).addToIntent(intent, "appWidgetProviderProfile");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 111);
            return;
        }
        Log.d("Launcher", "Cannot fetch activity for intent: " + intent.toString());
        Toast.makeText(this, getString(R.string.widget_could_not_be_added_error), 1).show();
    }


    public void onClickSettingsButton(View view) {
        this.mOnResumeState = State.WORKSPACE;
        Intent intent = new Intent();
        intent.setClassName(this, "com.voxel.simplesearchlauncher.settings.SettingsActivity");
        intent.addFlags(268468224);
        startActivity(intent);
    }

    public void onClickVoiceButton(View view) {
        view.performHapticFeedback(1);
        startVoice();
    }

    protected void onClickWallpaperPicker(View view) {
        Intent intent = new Intent("android.intent.action.SET_WALLPAPER");
        intent.setComponent(getWallpaperPickerComponent());
        startActivityForResult(intent, 10);
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        this.mLocalSearchManager = LocalSearchManager.getInstance(getApplicationContext(), true);
        this.mSettingsStorage = SettingsStorage.getInstance();
        Timber.tag("DEBUG-HOME-DELAY").d("**** Launcher.onCreate() Launcer activity starting ****");
        AndroidInjection.inject(this);
        super.onCreate(bundle);
        LauncherAppState.setApplicationContext(getApplicationContext());
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        Point point = new Point();
        Point point2 = new Point();
        Point point3 = new Point();
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        defaultDisplay.getCurrentSizeRange(point, point2);
        defaultDisplay.getRealSize(point3);
        defaultDisplay.getMetrics(new DisplayMetrics());
        this.mScreenHeight = point3.y;
        DeviceProfile deviceProfileInitDynamicGrid = launcherAppState.initDynamicGrid(this);
        this.mSharedPrefs = getSharedPreferences(LauncherAppState.getSharedPreferencesKey(), 0);
        UpgradeUtil.processUpgrade(this.mSharedPrefs, null, 0);
        this.mIsSafeModeEnabled = getPackageManager().isSafeMode();
        this.mModel = launcherAppState.setLauncher(this);
        this.mIconCache = launcherAppState.getIconCache();
        this.mIconCache.flushInvalidIcons(deviceProfileInitDynamicGrid);
        this.mDragController = new DragController(this);
        this.mInflater = getLayoutInflater();
        this.mAppWidgetManager = AppWidgetManagerCompat.getInstance(this);
        this.mAppWidgetHost = new LauncherAppWidgetHost(this, 1024);
        this.mAppWidgetHost.startListening();
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        this.mDeviceAdminComponentName = new ComponentName(this, "com.voxel.simplesearchlauncher.receiver.DeviceAdminReceiverSub");
        this.mPaused = false;
        checkForLocaleChange();
        setContentView(R.layout.launcher);
        setupViews();
        this.mBackgroundFolderAnimationHelper = new BackgroundFolderAnimationHelper();
        this.mBackgroundFolderAnimationHelper.setListener(new BackgroundFolderAnimationHelper.AnimationListener() { // from class: com.voxel.launcher3.Launcher.3
            @Override // com.voxel.launcher3.BackgroundFolderAnimationHelper.AnimationListener
            public void onAnimationEnd() {
                if (Launcher.this.isDestroyed()) {
                    return;
                }
                Launcher.this.mMainLayerBackground.setLayerType(0, null);
                for (int i = 0; i < Launcher.this.mWorkspace.getChildCount(); i++) {
                    Launcher.this.mWorkspace.getChildAt(i).setLayerType(0, null);
                }
                Launcher.this.mSearchDropTargetBar.setLayerType(0, null);
                if (Launcher.this.getOpenFolder() == null) {
                    Launcher.this.mMainLayer.setPivotX(Launcher.this.mMainLayer.getWidth() / 2.0f);
                    Launcher.this.mMainLayer.setPivotY(Launcher.this.mMainLayer.getHeight() / 2.0f);
                    Launcher.this.mMainLayer.addOnLayoutChangeListener(Launcher.this.mMainLayerMiddlePivotLayoutChangeListener);
                }
            }

            @Override // com.voxel.launcher3.BackgroundFolderAnimationHelper.AnimationListener
            public void onAnimationStart() {
                if (Launcher.this.isDestroyed()) {
                    return;
                }
                Launcher.this.mMainLayerBackground.clearAnimation();
                Launcher.this.mMainLayerBackground.setLayerType(2, null);
                Launcher.this.mMainLayerBackground.buildLayer();
                for (int i = 0; i < Launcher.this.mWorkspace.getChildCount(); i++) {
                    Launcher.this.mWorkspace.getChildAt(i).setLayerType(2, null);
                    Launcher.this.mWorkspace.getChildAt(i).buildLayer();
                }
                Launcher.this.mSearchDropTargetBar.setLayerType(2, null);
                Launcher.this.mSearchDropTargetBar.buildLayer();
            }

            @Override // com.voxel.launcher3.BackgroundFolderAnimationHelper.AnimationListener
            public void onAnimationUpdate() {
                if (Launcher.this.isDestroyed()) {
                    return;
                }
                Launcher.this.updateMainLayerScale();
                float animatedRatio = Launcher.this.mBackgroundFolderAnimationHelper.getAnimatedRatio();
                Launcher.this.mWorkspaceAlpha.putValue(this, animatedRatio);
                Launcher.this.mHotseatAlpha.putValue(this, animatedRatio);
                Launcher.this.mIndicatorAlpha.putValue(this, animatedRatio);
                Launcher.this.mSearchDropTargetBarAlpha.putValue(this, animatedRatio);
            }
        });
        deviceProfileInitDynamicGrid.layout(this);
        registerContentObservers();
        this.mSavedState = bundle;
        restoreState(this.mSavedState);
        if (!this.mRestoring) {
            this.mModel.startLoader(true, -1001);
        }
        this.mDefaultKeySsb = new SpannableStringBuilder();
        Selection.setSelection(this.mDefaultKeySsb, 0);
        WallpaperManager.getInstance(this);
        this.mIsLiveWallpaper = !canAccessWallpaper();
        this.mWallpaperBroadcastReceiver = new BroadcastReceiver() { // from class: com.voxel.launcher3.Launcher.4
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(Launcher.this);
                Launcher launcher = Launcher.this;
                launcher.mIsLiveWallpaper = !launcher.canAccessWallpaper();
                if (Launcher.this.mIsLiveWallpaper) {
                    Launcher.this.mMainLayerBackground.setImageDrawable(null);
                } else {
                    try {
                        Drawable drawable = wallpaperManager.getDrawable();
                        if (!Launcher.isWallpaperDrawableValid(drawable)) {
                            Launcher.this.mIsLiveWallpaper = true;
                            drawable = null;
                        }
                        Launcher.this.mMainLayerBackground.setImageDrawable(drawable);
                    } catch (RuntimeException unused) {
                        Launcher.this.mMainLayerBackground.setImageDrawable(null);
                        Launcher.this.mIsLiveWallpaper = true;
                    }
                }
                Launcher.this.refreshBlurredImage();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.WALLPAPER_CHANGED");
        ContextCompat.registerReceiver(this, this.mWallpaperBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(this, this.mCloseSystemDialogsReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"), ContextCompat.RECEIVER_NOT_EXPORTED);
        unlockScreenOrientation(true);
        this.mPreviousDefaultState = UserProfileManager.getInstance().getLauncherDefaultState();
        getApplication().registerActivityLifecycleCallbacks(new IconCacheCleaner(this.mLocalSearchManager));
        Timber.tag("DEBUG-HOME-DELAY").d("**** Launcher.onCreate() DONE ****");
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        IconLoader.getInstance().clearMemory();
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(0);
        this.mWorkspace.removeCallbacks(this.mBuildLayersRunnable);
        LauncherAnimUtils.onDestroyActivity();
        LauncherAppState launcherAppState = LauncherAppState.getInstance();
        if (this.mModel.isCurrentCallbacks(this)) {
            this.mModel.stopLoader();
            launcherAppState.setLauncher(null);
        }
        try {
            this.mAppWidgetHost.stopListening();
        } catch (NullPointerException e) {
            Log.w("Launcher", "problem while stopping AppWidgetHost during Launcher destruction", e);
        }
        this.mAppWidgetHost = null;
        this.mWidgetsToAdvance.clear();
        TextKeyListener.getInstance().release();
        getContentResolver().unregisterContentObserver(this.mWidgetObserver);
        BroadcastReceiver broadcastReceiver = this.mWallpaperBroadcastReceiver;
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            this.mWallpaperBroadcastReceiver = null;
        }
        unregisterReceiver(this.mCloseSystemDialogsReceiver);
        AsyncTask asyncTask = this.mRefreshBlurredImageTask;
        if (asyncTask != null) {
            asyncTask.cancel(true);
            this.mRefreshBlurredImageTask = null;
        }
        this.mDragLayer.clearAllResizeFrames();
        this.mLauncherView.removeAllViews();
        this.mWorkspace.removeAllWorkspaceScreens();
        this.mLauncherView = null;
        this.mWorkspace = null;
        this.mDragController = null;
        PackageInstallerCompat.getInstance(this).onStop();
        this.mSearchPresenter.clearPresenters();
        this.mSearchUiHelper.cleanup();
        CenterCropOffsetImageView centerCropOffsetImageView = this.mMainLayerBackground;
        if (centerCropOffsetImageView != null) {
            centerCropOffsetImageView.setImageDrawable(null);
            this.mMainLayerBackground = null;
        }
        CenterCropOffsetImageView centerCropOffsetImageView2 = this.mBlurImageView;
        if (centerCropOffsetImageView2 != null) {
            centerCropOffsetImageView2.setImageDrawable(null);
            this.mBlurImageView = null;
        }
        this.mLocalSearchManager = null;
        NotificationController.getInstance(this).stop();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mVisible = false;
        if (this.mAttached) {
            unregisterReceiver(this.mReceiver);
            this.mAttached = false;
        }
        updateRunning();
    }

    @Override // android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        this.mShownDialogs.remove(dialogInterface);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.OnFolderFragmentInteractionListener
    public void onFolderFragmentExit(FolderFragment folderFragment) {
        this.mFolderFragments.remove(folderFragment);
        if (folderFragment.getFolderIcon() != null) {
            folderFragment.getFolderIcon().setIconsVisible(true);
            folderFragment.setFolderIcon(null);
        }
        folderFragment.setFolder(null);
        FragmentTransaction fragmentTransactionBeginTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransactionBeginTransaction.remove(folderFragment);
        fragmentTransactionBeginTransaction.commitAllowingStateLoss();
        this.mMainLayerBackground.setVisibility(4);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.OnFolderFragmentInteractionListener
    public void onFolderFragmentRequestClose(FolderFragment folderFragment) {
        closeFolder(folderFragment.getFolder());
    }

    @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.OnFolderFragmentInteractionListener
    public void onFolderFragmentWillExit(FolderFragment folderFragment) {
        this.mDragController.removeDropTarget(folderFragment.getFolder());
        this.mBackgroundFolderAnimationHelper.animateCloseFolder();
    }

    protected void onHomeIntent(boolean z, boolean z2) {
        Timber.tag("DEBUG-HOME-DELAY").d("----------- onHomeIntent ------------");
        if (z2) {
            deactivateSearch(SearchInteractionMethod.HOME, z);
        } else if (this.mQsbSearchView.getCurrentFactor() == 1.0f && z) {
            this.mCustomGestureListener.onHomeButtonPressed();
        } else {
            deactivateSearch(SearchInteractionMethod.HOME, z);
        }
    }

    protected void onInteractionBegin() {
    }

    protected void onInteractionEnd() {
    }

    @Override // android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        SpannableStringBuilder spannableStringBuilder;
        int unicodeChar = keyEvent.getUnicodeChar();
        boolean zOnKeyDown = super.onKeyDown(i, keyEvent);
        boolean z = unicodeChar > 0 && !Character.isWhitespace(unicodeChar);
        if (!zOnKeyDown && acceptFilter() && z && TextKeyListener.getInstance().onKeyDown(this.mWorkspace, this.mDefaultKeySsb, i, keyEvent) && (spannableStringBuilder = this.mDefaultKeySsb) != null && spannableStringBuilder.length() > 0) {
            return onSearchRequested();
        }
        if (i == 82 && keyEvent.isLongPress()) {
            return true;
        }
        return zOnKeyDown;
    }

    @Override // android.view.View.OnLongClickListener
    public boolean onLongClick(View view) {
        View view2;
        CellLayout.CellInfo cellInfo;
        if (!isDraggingEnabled() || isWorkspaceLocked() || this.mState != State.WORKSPACE) {
            return false;
        }
        if (view instanceof Workspace) {
            if (this.mWorkspace.isInOverviewMode() || !this.mWorkspace.enterOverviewMode()) {
                return false;
            }
            this.mWorkspace.performHapticFeedback(0, 1);
            return true;
        }
        if (view.getTag() instanceof ItemInfo) {
            cellInfo = new CellLayout.CellInfo(view, (ItemInfo) view.getTag());
            view2 = cellInfo.cell;
            resetAddInfo();
        } else {
            view2 = null;
            cellInfo = null;
        }
        boolean zIsHotseatLayout = isHotseatLayout(view);
        if (!this.mDragController.isDragging()) {
            if (view2 == null) {
                this.mWorkspace.performHapticFeedback(0, 1);
                if (!this.mWorkspace.isInOverviewMode()) {
                    this.mWorkspace.enterOverviewMode();
                } else if (this.mSettingsStorage.isDesktopLockedCurrently()) {
                    pushStackFragment(new LockOverlayFragment());
                } else {
                    this.mWorkspace.startReordering(view);
                }
                this.mWorkspace.performHapticFeedback(0, 1);
            } else {
                boolean z = zIsHotseatLayout && isAllAppsButtonRank(this.mHotseat.getOrderInHotseat(cellInfo.cellX, cellInfo.cellY));
                if (!(view2 instanceof Folder) && !z) {
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    DragOptions dragOptions = new DragOptions();
                    dragOptions.isDesktopLocked = this.mSettingsStorage.isDesktopLockedCurrently();
                    if (PopupFragment.canProperlyShow((ItemInfo) view2.getTag())) {
                        PopupFragment popupFragment = new PopupFragment(view2, (ItemInfo) view2.getTag(), this.mWorkspace);
                        pushStackFragment(popupFragment);
                        dragOptions.deferDragCondition = popupFragment.createDeferDragCondition(null);
                    }
                    this.mWorkspace.startDragFromWorkspace(cellInfo, dragOptions);
                }
            }
        }
        return true;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, android.content.ComponentCallbacks
    public void onLowMemory() {
        super.onLowMemory();
        IconCache iconCache = this.mIconCache;
        if (iconCache != null) {
            iconCache.flush();
        }
        IconLoader.getInstance().clearMemory();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onNewIntent(Intent intent) {
        boolean z;
        super.onNewIntent(intent);
        if ("android.intent.action.MAIN".equals(intent.getAction()) || "android.intent.action.ASSIST".equals(intent.getAction())) {
            Iterator<DialogInterface> it = this.mShownDialogs.iterator();
            boolean z2 = false;
            while (it.hasNext()) {
                it.next().dismiss();
                z2 |= true;
            }
            this.mShownDialogs.clear();
            closeSystemDialogs();
            boolean z3 = (!this.mHasFocus || this.isLaunchingVoiceAssist || (intent.getFlags() & 4194304) == 4194304 || intent.getBooleanExtra(ResetLauncherActivity.RESET_EXTRA_KEY, false)) ? false : true;
            if (intent.getBooleanExtra(BackupRestoreManager.KEY_LAUNCHER_RESTORED, false)) {
                addOnResumeCallback(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$XHFBWYbBofTN9dVJDPR4rpH1MHI
                    @Override // java.lang.Runnable
                    public final void run() {
                        Launcher.this.pushStackFragment(new LauncherRestoredFragment());
                    }
                });
            }
            if (intent.getBooleanExtra(ResetLauncherActivity.VERIFY_EXTRA_KEY, false) && !ResetLauncherActivity.isLauncherDefault(this)) {
                addOnResumeCallback(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$Dh6rPjEzyDWM8tPhfVyQd0kRtiU
                    @Override // java.lang.Runnable
                    public final void run() {
                        Launcher.this.pushStackFragment(new ResetLauncherFailureFragment());
                    }
                });
                return;
            }
            if (this.mWorkspace == null) {
                return;
            }
            Folder openFolder = getOpenFolder();
            boolean zExitWidgetResizeMode = z2 | this.mWorkspace.exitWidgetResizeMode();
            if (z3 && this.mState == State.WORKSPACE && !this.mWorkspace.isTouchActive() && openFolder == null && shouldMoveToDefaultScreenOnHomeIntent()) {
                zExitWidgetResizeMode |= this.mWorkspace.moveToDefaultScreen(true);
            }
            boolean zCloseFolder = zExitWidgetResizeMode | closeFolder() | exitSpringLoadedDragMode();
            int stackFragmentCount = getStackFragmentCount();
            if (stackFragmentCount > 0) {
                int i = stackFragmentCount - 1;
                /* REWORK: upgrade/What's-New popup removed. */
                z = false;
                if (!z) {
                    if (z3) {
                        while (i >= 0) {
                            getStackFragmentAt(i).runExitAnimation();
                            i--;
                        }
                    } else {
                        this.mFragmentHelper.closeAllFragments();
                    }
                }
                changeSearchMode(false, z3);
                zCloseFolder |= true;
            } else {
                z = false;
            }
            if (this.mAllAppsFragment != null && this.mAllAppsMotionTracker.getCurrentFactor() != 1.0f) {
                changeAllAppsVisibility(false, z3);
                zCloseFolder |= true;
            }
            if (z3) {
                zCloseFolder |= this.mState != State.WORKSPACE;
                showWorkspace(true);
            } else {
                this.mOnResumeState = State.WORKSPACE;
            }
            View viewPeekDecorView = getWindow().peekDecorView();
            if (viewPeekDecorView != null && viewPeekDecorView.getWindowToken() != null) {
                ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(viewPeekDecorView.getWindowToken(), 0);
            }
            if (z) {
                return;
            }
            if ("android.intent.action.MAIN".equals(intent.getAction())) {
                onHomeIntent(z3, zCloseFolder);
            } else if ("android.intent.action.ASSIST".equals(intent.getAction())) {
                onAssistIntent(z3, zCloseFolder);
            }
        }
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void onPageBoundSynchronously(int i) {
        this.mSynchronouslyBoundPages.add(Integer.valueOf(i));
    }

    @Override // com.voxel.launcher3.PagedView.PageSwitchListener
    public void onPageSwitch(View view, int i) {
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        InstallShortcutReceiver.enableInstallQueue();
        PackageInstallerCompat.getInstance(this).onPause();
        super.onPause();
        this.mPaused = true;
        this.mDragController.cancelDrag();
        this.mDragController.resetLastGestureUpTime();
        if (this.mWorkspace.getCustomContentCallbacks() != null) {
            this.mWorkspace.getCustomContentCallbacks().onHide();
        }
        NotificationController.getInstance(this).pause();
        MainThreadExecutor.getInstance().execute(this.mForgetWallpaperRunnable, 1000L);
        this.mSearchPresenter.clearDragSink();
    }

    @Override // android.app.Activity
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (getOpenFolder() != null) {
            return false;
        }
        changeSearchMode(false, true);
        changeAllAppsVisibility(!isAllAppsVisibleOrMovingToVisible(), true);
        return false;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        if (sPermissionHelper.handleRequestPermissionsResult(this, i, strArr, iArr)) {
            return;
        }
        if (i != 13) {
            if (i == 15 && iArr.length > 0 && iArr[0] == 0) {
                this.mWallpaperBroadcastReceiver.onReceive(this, null);
                return;
            }
            return;
        }
        PendingAddArguments pendingAddArguments = sPendingAddItem;
        if (pendingAddArguments == null || pendingAddArguments.requestCode != 13) {
            return;
        }
        CellLayout cellLayout = getCellLayout(sPendingAddItem.container, sPendingAddItem.screenId);
        View childAt = cellLayout != null ? cellLayout.getChildAt(sPendingAddItem.cellX, sPendingAddItem.cellY) : null;
        Intent intent = sPendingAddItem.intent;
        sPendingAddItem = null;
        if (iArr.length <= 0 || iArr[0] != 0) {
            Toast.makeText(this, getString(R.string.msg_no_phone_permission, new Object[]{getString(R.string.app_name)}), 0).show();
        } else {
            startActivity(childAt, intent, null);
        }
    }

    @Override // android.app.Activity
    public void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        Iterator<Integer> it = this.mSynchronouslyBoundPages.iterator();
        while (it.hasNext()) {
            this.mWorkspace.restoreInstanceStateForChild(it.next().intValue());
        }
    }

    /* JADX WARN: Type inference failed for: r1v52, types: [com.voxel.launcher3.Launcher$8] */

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        System.currentTimeMillis();
        super.onResume();
        MainThreadExecutor.getInstance().removeCallback(this.mForgetWallpaperRunnable);
        this.mQsbSearchView.setUseSubtleBackground(SettingsStorage.getInstance().getSearchBarStyle() == 1);
        this.mQsbSearchViewScroller.put(this, SettingsStorage.getInstance().getSearchBarDesktopEnabled() ? 0.0f : getSearchBarHiddenHeight());
        this.mAllAppsUpChevron.setVisibility(SettingsStorage.getInstance().isAppDrawerHintEnabled() ? 0 : 4);
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        boolean z = !canAccessWallpaper();
        if (z != this.mIsLiveWallpaper) {
            this.mIsLiveWallpaper = z;
            refreshBlurredImage();
            if (this.mIsLiveWallpaper) {
                this.mMainLayerBackground.setImageDrawable(null);
            }
        }
        if (!this.mIsLiveWallpaper) {
            new AsyncTask<Void, Void, BitmapDrawable>() { // from class: com.voxel.launcher3.Launcher.8
                @Override // android.os.AsyncTask
                public BitmapDrawable doInBackground(Void... voidArr) {
                    BitmapDrawable bitmapDrawable = null;
                    try {
                        BitmapDrawable bitmapDrawable2 = (BitmapDrawable) wallpaperManager.getDrawable();
                        try {
                            if (Launcher.isWallpaperDrawableValid(bitmapDrawable2)) {
                                return bitmapDrawable2;
                            }
                            Launcher.this.mIsLiveWallpaper = true;
                            return null;
                        } catch (RuntimeException unused) {
                            bitmapDrawable = bitmapDrawable2;
                        }
                    } catch (RuntimeException unused2) {
                    }
                    Launcher.this.mIsLiveWallpaper = true;
                    return bitmapDrawable;
                }

                @Override // android.os.AsyncTask
                public void onPostExecute(BitmapDrawable bitmapDrawable) {
                    if (Launcher.this.mMainLayerBackground != null) {
                        Launcher.this.mMainLayerBackground.setImageDrawable(bitmapDrawable);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
        cleanupGoogleNowLaunchBackground();
        this.mAppWidgetHost.startListening();
        this.isLaunchingVoiceAssist = false;
        Timber.tag("DEBUG-HOME-DELAY").d("Launcher.onResume()");
        this.mQsbSearchView.setImeOptions(6);
        this.mLocalSearchManager.initContactsIfNeeded(this);
        this.mLocalSearchManager.updateRecents();
        this.mSearchPresenter.setDragSink(new DragSink() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$WNdnB1SSSbgSkfr6tBGGVKORK8Y
            @Override // com.evie.common.dragndrop.DragSink
            public final void startDrag(View view, ItemInfoCommon itemInfoCommon, DragSource dragSource) {
                Launcher.lambda$onResume$4(Launcher.this, view, itemInfoCommon, dragSource);
            }
        });
        if (this.mOnResumeState == State.WORKSPACE) {
            showWorkspace(false);
        } else if (this.mOnResumeState == State.APPS_CUSTOMIZE) {
            changeAllAppsVisibility(true, true);
        }
        this.mOnResumeState = State.NONE;
        setWorkspaceBackground(this.mState == State.WORKSPACE);
        this.mPaused = false;
        if (!this.mModel.isCurrentCallbacks(this)) {
            this.mModel.stopLoader();
            LauncherAppState.getInstance().setLauncher(this);
            this.mModel.startLoader(true, -1001);
        } else if (this.mRestoring || this.mOnResumeNeedsLoad) {
            setWorkspaceLoading(true);
            this.mModel.startLoader(true, -1001);
            this.mRestoring = false;
            this.mOnResumeNeedsLoad = false;
        } else if (!this.isShowingOnboard && shouldRunFirstRunClings()) {
            setWorkspaceLoading(true);
            this.mModel.resetLoadedState(false, true);
            this.mModel.startLoader(true, -1001, 12);
            this.mRestoring = false;
            this.mOnResumeNeedsLoad = false;
        }
        if (this.mBindOnResumeCallbacks.size() > 0) {
            Timber.tag("DEBUG-HOME-DELAY").d("--> Running bindOnResumeCallbacks: " + this.mBindOnResumeCallbacks.size());
            for (int i = 0; i < this.mBindOnResumeCallbacks.size(); i++) {
                this.mBindOnResumeCallbacks.get(i).run();
            }
            this.mBindOnResumeCallbacks.clear();
            Timber.tag("DEBUG-HOME-DELAY").d("--> bindOnResumeCallbacks DONE");
        }
        if (this.mOnResumeCallbacks.size() > 0) {
            Timber.tag("DEBUG-HOME-DELAY").d("--> Running onResumeCallbacks: " + this.mOnResumeCallbacks.size());
            for (int i2 = 0; i2 < this.mOnResumeCallbacks.size(); i2++) {
                this.mOnResumeCallbacks.get(i2).run();
            }
            this.mOnResumeCallbacks.clear();
            Timber.tag("DEBUG-HOME-DELAY").d("--> Running mOnResumeCallbacks DONE");
        }
        BubbleTextView bubbleTextView = this.mWaitingForResume;
        if (bubbleTextView != null) {
            bubbleTextView.setStayPressed(false);
        }
        getWorkspace().reinflateWidgetsIfNecessary();
        if (InstallShortcutReceiver.disableAndFlushInstallQueue(this) != 0) {
            closeFolder();
            changeAllAppsVisibility(false, false);
            changeSearchMode(false, false);
        }
        /* REWORK: the rate-us survey was cut (no Play Store); only the
         * set-default search prompt remains in this chain. */
        if (!getResources().getBoolean(R.bool.widget_enabled)) {
            this.mSearchUiHelper.showSetLauncherSearchPromptIfNeeded();
        }
        updateVoiceButtonProxyVisible(false);
        if (this.mWorkspace.getCustomContentCallbacks() != null && this.mWorkspace.isOnOrMovingToCustomContent()) {
            this.mWorkspace.getCustomContentCallbacks().onShow(true);
        }
        this.mWorkspace.updateInteractionForState();
        this.mWorkspace.onResume();
        if (!this.isShowingOnboard && this.sendHomeAnalyticsEventOnResume && !shouldRunFirstRunClings()) {
            boolean zIsLauncherDefault = isLauncherDefault();
            if (hasLaunchedHome()) {
                this.mAnalyticsHandler.logEvent("ev_launcher_home", new AnalyticsHandler.EventProp().add("is_default", zIsLauncherDefault));
            } else {
                handleHomeFirstTimeEvent(zIsLauncherDefault);
            }
            this.sendHomeAnalyticsEventOnResume = false;
            if (this.mPreviousDefaultState == UserProfileManager.LauncherDefaultState.UNKNOWN) {
                this.mPreviousDefaultState = zIsLauncherDefault ? UserProfileManager.LauncherDefaultState.DEFAULT : UserProfileManager.LauncherDefaultState.NOT_DEFAULT;
                UserProfileManager.getInstance().setLauncherDefaultState(this.mPreviousDefaultState);
            } else if (this.mPreviousDefaultState == UserProfileManager.LauncherDefaultState.DEFAULT && !zIsLauncherDefault) {
                this.mPreviousDefaultState = UserProfileManager.LauncherDefaultState.NOT_DEFAULT;
                UserProfileManager.getInstance().setLauncherDefaultState(this.mPreviousDefaultState);
                this.mAnalyticsHandler.logEvent("ev_switch_to_not_default", null);
            } else if (this.mPreviousDefaultState == UserProfileManager.LauncherDefaultState.NOT_DEFAULT && zIsLauncherDefault) {
                this.mPreviousDefaultState = UserProfileManager.LauncherDefaultState.DEFAULT;
                UserProfileManager.getInstance().setLauncherDefaultState(this.mPreviousDefaultState);
                this.mAnalyticsHandler.logEvent("ev_switch_to_default", null);
            }
        }
        PackageInstallerCompat.getInstance(this).onResume();
        NotificationController.getInstance(this).resume();
        LauncherAppState.getInstance().getIconCache().onResume();
        this.mDeepShortcutManager.refreshShortcutsIfRequired();
    }

    @Override // androidx.fragment.app.FragmentActivity
    protected void onResumeFragments() {
        super.onResumeFragments();
        this.mFragmentHelper.onResume();
    }

    @Override // androidx.fragment.app.FragmentActivity
    public Object onRetainCustomNonConfigurationInstance() {
        if (this.mModel.isCurrentCallbacks(this)) {
            this.mModel.stopLoader();
        }
        return Boolean.TRUE;
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        this.mFragmentHelper.onSaveInstanceState(bundle);
        if (this.mWorkspace.getChildCount() > 0) {
            bundle.putInt("launcher.current_screen", this.mWorkspace.getCurrentPageOffsetFromCustomContent());
        }
        forceCloseFolders();
        super.onSaveInstanceState(bundle);
        bundle.putInt("launcher.state", this.mState.ordinal());
        if (this.mPendingAddInfo.container != -1 && this.mPendingAddInfo.screenId > -1 && this.mWaitingForResult) {
            bundle.putLong("launcher.add_container", this.mPendingAddInfo.container);
            bundle.putLong("launcher.add_screen", this.mPendingAddInfo.screenId);
            bundle.putInt("launcher.add_cell_x", this.mPendingAddInfo.cellX);
            bundle.putInt("launcher.add_cell_y", this.mPendingAddInfo.cellY);
            bundle.putInt("launcher.add_span_x", this.mPendingAddInfo.spanX);
            bundle.putInt("launcher.add_span_y", this.mPendingAddInfo.spanY);
            bundle.putParcelable("launcher.add_widget_info", this.mPendingAddWidgetInfo);
            bundle.putInt("launcher.add_widget_id", this.mPendingAddWidgetId);
        }
        if (this.mFolderInfo != null && this.mWaitingForResult) {
            bundle.putBoolean("launcher.rename_folder", true);
            bundle.putLong("launcher.rename_folder_id", this.mFolderInfo.id);
        }
        bundle.putSerializable("launcher.view_ids", this.mItemIdToViewId);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean onSearchRequested() {
        startSearch((String) null, false, (Bundle) null, true);
        return true;
    }

    @Override // android.content.DialogInterface.OnShowListener
    public void onShow(DialogInterface dialogInterface) {
        this.mShownDialogs.add(dialogInterface);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void onStackableFragmentRequestClose(LauncherStackableFragment launcherStackableFragment) {
        this.mFragmentHelper.onStackableFragmentRequestClose(launcherStackableFragment);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void onStackableFragmentWillRequestClose(LauncherStackableFragment launcherStackableFragment) {
        this.mFragmentHelper.onStackableFragmentWillRequestClose(launcherStackableFragment);
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStart() {
        super.onStart();
        this.isStopped = false;
        this.mFragmentHelper.onStart();
        FirstFrameAnimatorHelper.setIsVisible(true);
        Timber.tag("DEBUG-HOME-DELAY").d("Launcher.onStart()");
        Timber.tag("DEBUG-HOME-DELAY").d("Launcher.onStart() DONE");
        this.sendHomeAnalyticsEventOnResume = true;
        NotificationController.getInstance(this).start();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        this.isStopped = true;
        Iterator<DialogInterface> it = this.mShownDialogs.iterator();
        while (it.hasNext()) {
            it.next().dismiss();
        }
        this.mShownDialogs.clear();
        boolean zIsLauncherDefault = isLauncherDefault();
        this.mQsbSearchView.hideAlternateHintTextMode(false);
        FirstFrameAnimatorHelper.setIsVisible(false);
        if (!this.isShowingOnboard && !shouldRunFirstRunClings() && !hasLaunchedHome()) {
            handleHomeFirstTimeEvent(zIsLauncherDefault);
        }
        if (this.mPreviousDefaultState == UserProfileManager.LauncherDefaultState.DEFAULT && !zIsLauncherDefault) {
            this.mPreviousDefaultState = UserProfileManager.LauncherDefaultState.NOT_DEFAULT;
            UserProfileManager.getInstance().setLauncherDefaultState(this.mPreviousDefaultState);
            this.mAnalyticsHandler.logEvent("ev_switch_to_not_default", null);
        } else if (this.mPreviousDefaultState == UserProfileManager.LauncherDefaultState.NOT_DEFAULT && zIsLauncherDefault) {
            this.mPreviousDefaultState = UserProfileManager.LauncherDefaultState.DEFAULT;
            UserProfileManager.getInstance().setLauncherDefaultState(this.mPreviousDefaultState);
            this.mAnalyticsHandler.logEvent("ev_switch_to_default", null);
        }
    }

    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }


    public void onTouchDownScrollingWidget(LauncherAppWidgetHostView launcherAppWidgetHostView) {
        MainLayerGestureListener mainLayerGestureListener = this.mMainLayerGestureListener;
        if (mainLayerGestureListener != null) {
            mainLayerGestureListener.touchDownScrollingWidget = launcherAppWidgetHostView.checkForAdapterView();
        }
    }

    @Override // android.app.Activity
    public boolean onTouchEvent(MotionEvent motionEvent) {
        SpringySearchView springySearchView;
        if (this.mDragController.isDragging() || this.mQsb.getAlpha() != 1.0f || this.mWorkspace.isInOverviewMode() || !this.mWorkspace.isFinishedSwitchingState()) {
            return false;
        }
        if (motionEvent.getAction() != 2 && motionEvent.getAction() != 0 && (springySearchView = this.mQsbSearchView) != null) {
            springySearchView.sendMotionEvent(motionEvent);
        }
        return this.mQsbSearchViewGestureDetector.onTouchEvent(motionEvent) || super.onTouchEvent(motionEvent);
    }

    /* REWORK(fresco): the MemoryTrimType registry was fresco-owned; our
     * IconCache flush + LRU eviction is all that remains. */
    @Override // android.app.Activity, android.content.ComponentCallbacks2
    public void onTrimMemory(int i) {
        super.onTrimMemory(i);
        IconCache iconCache;
        if (i == 15 || i == 80) {
            iconCache = this.mIconCache;
            if (iconCache != null) {
                iconCache.flush();
            }
        }
        IconLoader.getInstance().clearMemory();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        this.mHasFocus = z;
    }

    public void onWindowVisibilityChanged(int i) {
        this.mVisible = i == 0;
        updateRunning();
        if (this.mVisible) {
            if (!this.mWorkspaceLoading) {
                this.mWorkspace.getViewTreeObserver().addOnDrawListener(new AnonymousClass22());
            }
            clearTypedText();
        }
    }

    protected void onWorkspaceLockedChanged() {
    }

    public void onWorkspaceShown(boolean z) {
    }

    public void openFolder(final FolderIcon folderIcon) {
        Folder folder = folderIcon.getFolder();
        folder.mInfo.opened = true;
        FragmentTransaction fragmentTransactionBeginTransaction = getSupportFragmentManager().beginTransaction();
        FolderFragment folderFragment = new FolderFragment();
        folderFragment.setFolderIcon(folderIcon);
        folderFragment.setFolder(folder);
        fragmentTransactionBeginTransaction.add(R.id.folder_fragment_target, folderFragment, null);
        fragmentTransactionBeginTransaction.commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();
        this.mFolderFragments.add(folderFragment);
        this.mDragController.addDropTarget(folder);
        folderFragment.setListener(new FolderFragment.FolderFragmentListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$LKLY7MD3Fc71w_x4Fz3GFw9qDJk
            @Override // com.voxel.simplesearchlauncher.fragment.FolderFragment.FolderFragmentListener
            public final void onAnimationStarted() {
                Launcher.lambda$openFolder$28(Launcher.this, folderIcon);
            }
        });
        folder.sendAccessibilityEvent(32);
        getDragLayer().sendAccessibilityEvent(2048);
    }

    protected boolean overrideWallpaperDimensions() {
        return true;
    }


    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void popStackFragment(LauncherStackableFragment launcherStackableFragment, boolean z) {
        this.mFragmentHelper.popStackFragment(launcherStackableFragment, z);
    }


    void processShortcut(Intent intent) {
        Utilities.startActivityForResultSafely(this, intent, 1);
    }

    void processShortcutFromDrop(ComponentName componentName, long j, long j2, int[] iArr, int[] iArr2) {
        resetAddInfo();
        ItemInfo itemInfo = this.mPendingAddInfo;
        itemInfo.container = j;
        itemInfo.screenId = j2;
        itemInfo.dropPos = iArr2;
        if (iArr != null) {
            itemInfo.cellX = iArr[0];
            itemInfo.cellY = iArr[1];
        }
        Intent intent = new Intent("android.intent.action.CREATE_SHORTCUT");
        intent.setComponent(componentName);
        processShortcut(intent);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public boolean providesTransitionAnimation() {
        return false;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment.OnFragmentInteractionListener
    public void pushStackFragment(LauncherStackableFragment launcherStackableFragment) {
        this.mFragmentHelper.pushStackFragment(R.id.drag_layer, launcherStackableFragment);
        this.mQsb.clearFocus();
        if (launcherStackableFragment.requiresFullScreen()) {
            changeSearchMode(false, true);
        }
    }

    public void removeAppWidget(LauncherAppWidgetInfo launcherAppWidgetInfo) {
        removeWidgetToAutoAdvance(launcherAppWidgetInfo.hostView);
        launcherAppWidgetInfo.hostView = null;
    }

    public void removeFolder(FolderInfo folderInfo) {
        sFolders.remove(folderInfo.id);
    }

    public void removeViewForItemInfo(final ItemInfo itemInfo) {
        this.mWorkspace.removeItemsByMatcher(new ItemInfoMatcher() { // from class: com.voxel.launcher3.Launcher.24
            @Override // com.voxel.launcher3.util.ItemInfoMatcher
            public boolean matches(ItemInfo itemInfo2, ComponentName componentName) {
                return itemInfo == itemInfo2;
            }
        });
    }

    void removeWidgetToAutoAdvance(View view) {
        if (this.mWidgetsToAdvance.containsKey(view)) {
            this.mWidgetsToAdvance.remove(view);
            updateRunning();
        }
    }

    public void resetQSBScroll() {
        this.mSearchDropTargetBar.animate().translationY(0.0f).start();
        getQsbBar().animate().translationY(0.0f).start();
    }


    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public boolean setLoadOnResume() {
        return false;
    }

    public void setWallpaperOffsets(float f, float f2) {
        CenterCropOffsetImageView centerCropOffsetImageView = this.mMainLayerBackground;
        if (centerCropOffsetImageView != null) {
            centerCropOffsetImageView.setOffsets(f, f2);
        }
        CenterCropOffsetImageView centerCropOffsetImageView2 = this.mBlurImageView;
        if (centerCropOffsetImageView2 != null) {
            centerCropOffsetImageView2.setOffsets(f, f2);
        }
        CenterCropOffsetImageView centerCropOffsetImageView3 = this.mAllAppsBackground;
        if (centerCropOffsetImageView3 != null) {
            centerCropOffsetImageView3.setOffsets(f, f2);
        }
        SpringySearchView springySearchView = this.mQsbSearchView;
        if (springySearchView != null && springySearchView.mSubtleBackground != null) {
            this.mQsbSearchView.setSubtleOffsets(f, f2);
        }
        HotseatImageView hotseatImageView = this.mHotseatBg;
        if (hotseatImageView != null) {
            hotseatImageView.setOffsets(f, f2);
        }
    }

    protected boolean shouldMoveToDefaultScreenOnHomeIntent() {
        EditText editText;
        SpringySearchView springySearchView = this.mQsbSearchView;
        return (springySearchView == null || !springySearchView.hasFocus()) && ((editText = this.mQsbSearchViewEditText) == null || !editText.hasFocus());
    }

    @Override // com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment.OnFragmentInteractionListener
    public void showBlurImage() {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$jeEDl4ovx6w1uKQuDIGOdCemTJM
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                Launcher.this.setFragmentBlurAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        animatorSet.play(valueAnimatorOfFloat);
        float f = 0;
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mSearchDropTargetBarAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mIndicatorAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mWorkspaceAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mHotseatAlpha, this, f));
        animatorSet.play(AlphaSetAnimator.ofFloat(this.mOverviewPanelAlpha, this, f));
        animatorSet.start();
    }

    public boolean showFirstRunActivity() {
        Intent firstRunActivity;
        if (!shouldRunFirstRunActivity() || !hasFirstRunActivity() || (firstRunActivity = getFirstRunActivity()) == null) {
            return false;
        }
        startActivityForResult(firstRunActivity, 14);
        this.mAnalyticsHandler.logEvent("ev_onboard_initiated", null);
        return true;
    }

    public boolean showFirstRunClings() {
        if (isFinishing() || isDestroyed() || this.mPaused) {
            return false;
        }
        Fragment currentStackFragment = getCurrentStackFragment();
        if ((currentStackFragment != null && (currentStackFragment instanceof ResetLauncherPromptFragment)) || !shouldRunFirstRunClings()) {
            return false;
        }
        pushStackFragment(ResetLauncherPromptFragment.getInstance(getFirstRunDidAutoImport()));
        markFirstRunClingShown();
        return true;
    }


    void showOutOfSpaceMessage(boolean z) {
        Toast.makeText(this, getString(z ? R.string.hotseat_out_of_space : R.string.out_of_space), 0).show();
    }

    void showOverviewMode(boolean z) {
        this.mWorkspace.setVisibility(0);
        hideAppsCustomizeHelper(Workspace.State.OVERVIEW, z, false, null);
        this.mState = State.WORKSPACE;
        onWorkspaceShown(z);
    }

    public void showSpecialFragments() {
        AllAppsFragment allAppsFragment = this.mAllAppsFragment;
        if (allAppsFragment != null) {
            allAppsFragment.setVisible(true);
        }
        Iterator<FolderFragment> it = this.mFolderFragments.iterator();
        while (it.hasNext()) {
            it.next().setVisible(true);
        }
    }

    public void showWorkspace(boolean z) {
        showWorkspace(z, null);
    }

    void showWorkspace(boolean z, Runnable runnable) {
        if (this.mWorkspace == null) {
            return;
        }
        if (this.mState != State.WORKSPACE || this.mWorkspace.getState() != Workspace.State.NORMAL) {
            boolean z2 = false;
            boolean z3 = this.mState != State.WORKSPACE;
            this.mWorkspace.setVisibility(0);
            hideAppsCustomizeHelper(Workspace.State.NORMAL, z, false, runnable);
            SearchDropTargetBar searchDropTargetBar = this.mSearchDropTargetBar;
            if (searchDropTargetBar != null) {
                if (z && z3) {
                    z2 = true;
                }
                searchDropTargetBar.showSearchBar(z2);
            }
            View view = this.mAllAppsButton;
            if (view != null) {
                view.requestFocus();
            }
        }
        this.mState = State.WORKSPACE;
        this.mUserPresent = true;
        updateRunning();
        getWindow().getDecorView().sendAccessibilityEvent(32);
        onWorkspaceShown(z);
    }

    void showWorkspaceIsLoadingMessage() {
        Toast.makeText(this, R.string.toast_error_workspace_still_loading, 0).show();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void startActivityForResult(Intent intent, int i) {
        if (i >= 0) {
            setWaitingForResult(true);
        }
        super.startActivityForResult(intent, i);
    }

    @Override // com.voxel.simplesearchlauncher.activity.SafeActivityStarter
    public boolean startActivitySafely(Intent intent) {
        return startActivitySafely(null, intent, null);
    }

    public boolean startActivitySafely(View view, Intent intent, Object obj) {
        return startActivitySafely(view, intent, obj, R.string.activity_not_found);
    }

    boolean startActivitySafely(View view, Intent intent, Object obj, int i) {
        if (this.mIsSafeModeEnabled && !Utilities.isSystemApp(this, intent)) {
            Toast.makeText(this, R.string.safemode_shortcut_error, 0).show();
            return false;
        }
        try {
            return startActivity(view, intent, obj);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, i, 0).show();
            Log.e("Launcher", "Unable to launch. tag=" + obj + " intent=" + intent, e);
            return false;
        }
    }

    @Override // com.voxel.simplesearchlauncher.appoptions.AppOptionsFragment.OnFragmentInteractionListener
    public boolean startApplicationUninstallActivity(ComponentName componentName, UserHandleCompat userHandleCompat) {
        boolean z;
        try {
            z = (getPackageManager().getApplicationInfo(componentName.getPackageName(), 0).flags & 1) == 0;
        } catch (PackageManager.NameNotFoundException unused) {
            z = false;
        }
        if (!z) {
            Toast.makeText(this, R.string.uninstall_system_app_text, 0).show();
            return false;
        }
        Intent intent = new Intent("android.intent.action.DELETE", Uri.fromParts("package", componentName.getPackageName(), componentName.getClassName()));
        intent.setFlags(276824064);
        if (userHandleCompat != null) {
            userHandleCompat.addToIntent(intent, "android.intent.extra.USER");
        }
        startActivitySafely(intent);
        return true;
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void startBinding() {
        setWorkspaceLoading(true);
        this.mBindOnResumeCallbacks.clear();
        this.mDragController.cancelDrag();
        this.mWorkspace.clearDropTargets();
        this.mWorkspace.removeAllWorkspaceScreens();
        forceCloseFolders();
        this.mWidgetsToAdvance.clear();
        Hotseat hotseat = this.mHotseat;
        if (hotseat != null) {
            hotseat.refreshLayout();
        }
        LauncherAppState.getInstance().getDynamicGrid().getDeviceProfile().layout(this);
    }

    public void startDrag(final View view, ItemInfo itemInfo, DragSource dragSource) {
        view.setTag(itemInfo);
        this.mDragController.addDragListener(new DragController.DragListener() { // from class: com.voxel.launcher3.Launcher.19
            @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
            public void onDragEnd() {
                view.setVisibility(0);
                Launcher.this.mDragController.removeDragListener(this);
            }

            @Override // com.voxel.launcher3.dragndrop.DragController.DragListener
            public void onDragStart(DropTarget.DragObject dragObject, DragOptions dragOptions) {
                int dragRegionHeight = 0;
                int dragRegionWidth = (dragObject.xOffset > dragObject.dragView.getDragRegionLeft() + dragObject.dragView.getDragRegionWidth() || dragObject.xOffset < dragObject.dragView.getDragRegionLeft()) ? (-dragObject.xOffset) + (dragObject.dragView.getDragRegionWidth() / 2) : 0;
                if (dragObject.yOffset > dragObject.dragView.getDragRegionTop() + dragObject.dragView.getDragRegionHeight() || dragObject.yOffset < dragObject.dragView.getDragRegionTop()) {
                    dragRegionHeight = (dragObject.dragView.getDragRegionHeight() / 2) + (-dragObject.yOffset);
                }
                view.setVisibility(4);
                if (dragRegionWidth == 0 && dragRegionHeight == 0) {
                    return;
                }
                dragObject.dragView.animateShiftTo(dragRegionWidth, dragRegionHeight);
                dragObject.xOffset += dragRegionWidth;
                dragObject.yOffset += dragRegionHeight;
            }
        });
        DragOptions dragOptions = new DragOptions();
        dragOptions.isDesktopLocked = this.mSettingsStorage.isDesktopLockedCurrently();
        if (PopupFragment.canProperlyShow(itemInfo)) {
            PopupFragment popupFragment = new PopupFragment(view, itemInfo, dragSource);
            pushStackFragment(popupFragment);
            dragOptions.deferDragCondition = popupFragment.createDeferDragCondition(null);
        }
        this.mWorkspace.beginDragShared(view, dragSource, dragOptions);
    }

    @Override // android.app.Activity
    public void startSearch(String str, boolean z, Bundle bundle, boolean z2) {
        showWorkspace(true);
        if (str == null) {
            str = getTypedText();
        }
        if (bundle == null) {
            bundle = new Bundle();
            bundle.putString("source", "launcher-search");
        }
        Rect rect = new Rect();
        SearchDropTargetBar searchDropTargetBar = this.mSearchDropTargetBar;
        if (searchDropTargetBar != null) {
            rect = searchDropTargetBar.getSearchBarBounds();
        }
        if (startSearch(str, z, bundle, rect)) {
            clearTypedText();
        }
    }

    public boolean startSearch(String str, boolean z, Bundle bundle, Rect rect) {
        startGlobalSearch(str, z, bundle, rect);
        return false;
    }

    public void startVoice() {
        try {
            ComponentName globalSearchActivity = ((SearchManager) getSystemService("search")).getGlobalSearchActivity();
            Intent intent = new Intent("android.speech.action.WEB_SEARCH");
            intent.setFlags(268435456);
            if (globalSearchActivity != null) {
                intent.setPackage(globalSearchActivity.getPackageName());
            }
            startActivity(null, intent, "onClickVoiceButton");
        } catch (ActivityNotFoundException unused) {
            Intent intent2 = new Intent("android.speech.action.WEB_SEARCH");
            intent2.setFlags(268435456);
            startActivitySafely(null, intent2, "onClickVoiceButton");
        }
    }

    public void unlockScreenOrientation(boolean z) {
        if (isRotationEnabled()) {
            if (z) {
                setRequestedOrientation(-1);
            } else {
                this.mHandler.postDelayed(new Runnable() { // from class: com.voxel.launcher3.-$$Lambda$Launcher$d-1czdx1XY2BjLd8E-nl1V-LG08
                    @Override // java.lang.Runnable
                    public final void run() {
                        Launcher.this.setRequestedOrientation(-1);
                    }
                }, 500L);
            }
        }
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void updatePackageBadge(String str) {
        Workspace workspace = this.mWorkspace;
        if (workspace != null) {
            workspace.updatePackageBadge(str, UserHandleCompat.myUserHandle());
        }
    }

    @Override // com.voxel.launcher3.LauncherModel.Callbacks
    public void updatePackageState(ArrayList<PackageInstallerCompat.PackageInstallInfo> arrayList) {
        Workspace workspace = this.mWorkspace;
        if (workspace != null) {
            workspace.updatePackageState(arrayList);
        }
    }

    public void updateVoiceButtonProxyVisible(boolean z) {
        View viewFindViewById = findViewById(R.id.voice_button_proxy);
        if (viewFindViewById != null) {
            viewFindViewById.setVisibility(!z && this.mWorkspace.shouldVoiceButtonProxyBeVisible() ? 0 : 8);
            viewFindViewById.bringToFront();
        }
    }
}
