package com.voxel.simplesearchlauncher.fragment;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import androidx.fragment.app.FragmentActivity;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.voxel.simplesearchlauncher.utils.AlphaUtil;
import com.voxel.simplesearchlauncher.utils.Choreographer;
import com.voxel.simplesearchlauncher.utils.TraceUtil;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public abstract class SpringCardFragment extends LauncherStackableFragment {
    private Drawable mBackgroundWashDrawable;
    private FrameLayout mContainer;
    private View mInsetContainer;
    private View mNavBarBackground;
    private View mRootView;
    private final Spring mSpringScale;
    private int mTotalSpringDistanceOffset;
    private static final String PREFIX = SpringCardFragment.class.getCanonicalName();
    private static final String SPRING_SCALE_KEY = PREFIX + ".spring_scale_key";
    protected static final SpringConfig RUBBERBANDING_CONFIG = new SpringConfig(500.0d, 50.0d);
    protected static final SpringConfig FASTER_RUBBERBANDING_CONFIG = new SpringConfig(500.0d, 40.0d);
    private int mTotalSpringDistance = 0;
    private Choreographer mChoreographer = Choreographer.getInstance();
    private final SpringListener mEntranceSpringListener = new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.fragment.SpringCardFragment.1
        @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
        public void onSpringAtRest(Spring spring) {
            SpringCardFragment.this.onFinishedEnterTransition();
            spring.removeListener(this);
            SpringCardFragment.this.mChoreographer.animationEnded();
        }
    };
    protected final SpringSystem mSpringSystem = SpringSystem.create();
    protected final Spring mSpringY = this.mSpringSystem.createSpring();

    /* JADX INFO: renamed from: com.voxel.simplesearchlauncher.fragment.SpringCardFragment$4, reason: invalid class name */
    class AnonymousClass4 implements ViewTreeObserver.OnGlobalLayoutListener {
        final /* synthetic */ Bundle val$savedInstanceState;

        AnonymousClass4(Bundle bundle) {
            this.val$savedInstanceState = bundle;
        }

        public void lambda$onGlobalLayout$1() {
            LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener = (LauncherStackableFragment.OnFragmentInteractionListener) SpringCardFragment.this.getActivity();
            if (onFragmentInteractionListener == null || !onFragmentInteractionListener.providesTransitionAnimation()) {
                SpringCardFragment.this.mContainer.setTranslationY(SpringCardFragment.this.mTotalSpringDistance);
                SpringCardFragment.this.mSpringY.setCurrentValue(SpringCardFragment.this.mTotalSpringDistance);
            } else {
                SpringCardFragment.this.mContainer.setTranslationY(0.0f);
                SpringCardFragment.this.mSpringY.setCurrentValue(0.0d);
                Executors.getMainThreadExecutor().post(new Runnable() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$SpringCardFragment$4$QI3TVxBw5x4ZScO42u-WtRppRcQ
                    @Override // java.lang.Runnable
                    public final void run() {
                        SpringCardFragment.AnonymousClass4 anonymousClass5 = AnonymousClass4.this;
                        SpringCardFragment.this.mEntranceSpringListener.onSpringAtRest(SpringCardFragment.this.mSpringY);
                    }
                });
            }
            SpringCardFragment.this.mSpringY.addListener(SpringCardFragment.this.mEntranceSpringListener);
            SpringCardFragment.this.mSpringY.setEndValue(0.0d);
            SpringCardFragment.this.onStartedEnterTransition();
        }

        @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
        public void onGlobalLayout() {
            SpringCardFragment.this.mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            SpringCardFragment springCardFragment = SpringCardFragment.this;
            springCardFragment.mTotalSpringDistanceOffset = springCardFragment.getTotalSpringDistanceOffset();
            SpringCardFragment springCardFragment2 = SpringCardFragment.this;
            springCardFragment2.mTotalSpringDistance = springCardFragment2.mContainer.getHeight() + SpringCardFragment.this.mContainer.getPaddingBottom() + SpringCardFragment.this.mTotalSpringDistanceOffset;
            if (this.val$savedInstanceState != null) {
                SpringCardFragment.this.invalidateBackgroundWashAlpha();
            } else {
                SpringCardFragment.this.mChoreographer.postAnimationStart(new Runnable() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$SpringCardFragment$4$x82Jv8TNPV3KbdvchS77pFoEs5Y
                    @Override // java.lang.Runnable
                    public final void run() {
                        AnonymousClass4.this.lambda$onGlobalLayout$1();
                    }
                });
            }
        }
    }

    public SpringCardFragment() {
        this.mSpringY.setSpringConfig(RUBBERBANDING_CONFIG);
        this.mSpringY.setOvershootClampingEnabled(true);
        this.mSpringY.setRestDisplacementThreshold(1.0d);
        this.mSpringY.setRestSpeedThreshold(1.0d);
        this.mSpringY.addListener(new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.fragment.SpringCardFragment.2
            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                TraceUtil.beginSection("SpringCardFragment onSpringUpdate");
                if (SpringCardFragment.this.mContainer != null) {
                    SpringCardFragment.this.mContainer.setTranslationY((int) spring.getCurrentValue());
                }
                if (SpringCardFragment.this.mBackgroundWashDrawable != null && SpringCardFragment.this.mTotalSpringDistance != 0) {
                    SpringCardFragment.this.invalidateBackgroundWashAlpha();
                }
                if (spring.getCurrentDisplacementDistance() < 1.0d) {
                    spring.setAtRest();
                }
                TraceUtil.endSection();
            }
        });
        this.mSpringScale = this.mSpringSystem.createSpring();
        this.mSpringScale.setCurrentValue(1.0d);
        this.mSpringScale.setSpringConfig(RUBBERBANDING_CONFIG);
        this.mSpringScale.addListener(new SimpleSpringListener() { // from class: com.voxel.simplesearchlauncher.fragment.SpringCardFragment.3
            @Override // com.facebook.rebound.SimpleSpringListener, com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                if (SpringCardFragment.this.mContainer != null) {
                    SpringCardFragment.this.mContainer.setScaleY((float) spring.getCurrentValue());
                    SpringCardFragment.this.mContainer.setScaleX((float) spring.getCurrentValue());
                }
            }
        });
    }

    private int getNavBarHeight() {
        return this.mInsetContainer.getPaddingBottom();
    }

    public void lambda$onCreateView$0(View view) {
        if (this.shouldCloseOnBackgroundClick()) {
            this.runExitAnimation();
        }
    }

    static /* synthetic */ void lambda$onCreateView$1(View view) {
    }

    public void lambda$onCreateView$2() {
        int navBarHeight = this.getNavBarHeight();
        if (navBarHeight == 0) {
            return;
        }
        this.mNavBarBackground.setVisibility(0);
        ViewGroup.LayoutParams layoutParams = this.mNavBarBackground.getLayoutParams();
        layoutParams.height = navBarHeight;
        this.mNavBarBackground.setLayoutParams(layoutParams);
    }

    public void lambda$runExitAnimation$4() {
        this.mSpringY.setSpringConfig(FASTER_RUBBERBANDING_CONFIG);
        this.mSpringY.removeListener(this.mEntranceSpringListener);
        final SpringListener springListener = new SpringListener() { // from class: com.voxel.simplesearchlauncher.fragment.SpringCardFragment.5
            @Override // com.facebook.rebound.SpringListener
            public void onSpringActivate(Spring spring) {
            }

            @Override // com.facebook.rebound.SpringListener
            public void onSpringAtRest(Spring spring) {
                SpringCardFragment.this.notifyFragmentRequestClose();
                SpringCardFragment.this.mChoreographer.animationEnded();
            }

            @Override // com.facebook.rebound.SpringListener
            public void onSpringEndStateChange(Spring spring) {
            }

            @Override // com.facebook.rebound.SpringListener
            public void onSpringUpdate(Spring spring) {
                float currentDisplacementDistance = (float) (spring.getCurrentDisplacementDistance() / spring.getEndValue());
                SpringCardFragment.this.mNavBarBackground.setAlpha(2.0f * currentDisplacementDistance);
                SpringCardFragment.this.mContainer.setAlpha(currentDisplacementDistance);
            }
        };
        this.mSpringY.addListener(springListener);
        LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener = (LauncherStackableFragment.OnFragmentInteractionListener) this.getActivity();
        if (onFragmentInteractionListener != null && onFragmentInteractionListener.providesTransitionAnimation()) {
            Executors.getMainThreadExecutor().post(new Runnable() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$SpringCardFragment$sBA8VhxI1l8hMLTqXk-cWwPBWFU
                @Override // java.lang.Runnable
                public final void run() {
                    springListener.onSpringAtRest(SpringCardFragment.this.mSpringY);
                }
            });
            return;
        }
        this.mTotalSpringDistance -= this.mTotalSpringDistanceOffset;
        this.mTotalSpringDistanceOffset = this.getTotalSpringDistanceOffset();
        this.mTotalSpringDistance += this.mTotalSpringDistanceOffset;
        this.mSpringY.setEndValue(this.mTotalSpringDistance);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void focusFragment(boolean z) {
        if (z) {
            this.mSpringScale.setEndValue(1.0d);
        } else {
            this.mSpringScale.setCurrentValue(1.0d);
            this.mSpringScale.setAtRest();
        }
    }

    protected float getBackgroundWashAlphaFactor() {
        return 1.0f;
    }

    protected float getMaxBackgroundWashAlpha() {
        return 0.5f;
    }

    protected int getTotalSpringDistanceOffset() {
        return 0;
    }

    protected void invalidateBackgroundWashAlpha() {
        double currentValue = this.mSpringY.getCurrentValue();
        double d = this.mTotalSpringDistance;
        Double.isNaN(d);
        this.mBackgroundWashDrawable.setAlpha(AlphaUtil.floatToIntAlpha(getMaxBackgroundWashAlpha() * AlphaUtil.limitAlpha(AlphaUtil.limitAlpha(1.0f - ((float) (currentValue / d))) * getBackgroundWashAlphaFactor())));
    }

    public final boolean isTransitioning() {
        return !this.mSpringY.isAtRest();
    }

    protected abstract View onCreateChildContentView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle);

    @Override // androidx.fragment.app.Fragment
    public final View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        TraceUtil.beginSection("SpringCardFragment onCreateView");
        this.mRootView = layoutInflater.inflate(R.layout.fragment_spring_card, viewGroup, false);
        this.mRootView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$SpringCardFragment$7CzlyWZMjYxx9nHctTU1ay6aCOc
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpringCardFragment.this.lambda$onCreateView$0( view);
            }
        });
        this.mBackgroundWashDrawable = new ColorDrawable(Color.argb(255, 0, 0, 0));
        this.mBackgroundWashDrawable.setAlpha(0);
        this.mRootView.findViewById(R.id.background_wash).setBackground(this.mBackgroundWashDrawable);
        this.mInsetContainer = this.mRootView.findViewById(R.id.inset_container);
        this.mContainer = (FrameLayout) this.mRootView.findViewById(R.id.container);
        this.mContainer.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$SpringCardFragment$w9Q6f356IACBxhQgCddbfZqFUXQ
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                SpringCardFragment.lambda$onCreateView$1(view);
            }
        });
        View viewOnCreateChildContentView = onCreateChildContentView(layoutInflater, this.mContainer, bundle);
        if (viewOnCreateChildContentView != null) {
            this.mContainer.addView(viewOnCreateChildContentView);
        }
        getResources().getDisplayMetrics();
        this.mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new AnonymousClass4(bundle));
        this.mNavBarBackground = this.mRootView.findViewById(R.id.nav_bar_background);
        this.mNavBarBackground.post(new Runnable() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$SpringCardFragment$i05v3KBndpZ1vfB80ijTrnCQg3M
            @Override // java.lang.Runnable
            public final void run() {
                SpringCardFragment.this.lambda$onCreateView$2();
            }
        });
        TraceUtil.endSection();
        return this.mRootView;
    }

    protected void onFinishedEnterTransition() {
        if (this.mContainer == null) {
            return;
        }
        LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener = (LauncherStackableFragment.OnFragmentInteractionListener) getActivity();
        if (onFragmentInteractionListener == null || !onFragmentInteractionListener.providesTransitionAnimation()) {
            this.mContainer.setLayerType(0, null);
        }
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z) {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPushLauncherStackedAppearance(LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
    }

    @Override // androidx.fragment.app.Fragment
    public void onSaveInstanceState(Bundle bundle) {
        Spring spring;
        if (bundle == null || (spring = this.mSpringScale) == null) {
            return;
        }
        bundle.putDouble(SPRING_SCALE_KEY, spring.getEndValue());
    }

    protected void onStartedEnterTransition() {
        LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener = (LauncherStackableFragment.OnFragmentInteractionListener) getActivity();
        if (onFragmentInteractionListener == null || this.mContainer == null || onFragmentInteractionListener.providesTransitionAnimation()) {
            return;
        }
        this.mContainer.setLayerType(2, null);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewStateRestored(Bundle bundle) {
        Spring spring;
        super.onViewStateRestored(bundle);
        if (bundle == null || (spring = this.mSpringScale) == null) {
            return;
        }
        spring.setCurrentValue(bundle.getDouble(SPRING_SCALE_KEY, 1.0d));
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public final void runExitAnimation() {
        notifyFragmentWillRequestClose();
        this.mRootView.setOnClickListener(null);
        this.mChoreographer.postAnimationStart(new Runnable() { // from class: com.voxel.simplesearchlauncher.fragment.-$$Lambda$SpringCardFragment$Y9QkSzdLJ-z2yLP4TUnvhKNsM6s
            @Override // java.lang.Runnable
            public final void run() {
                SpringCardFragment.this.lambda$runExitAnimation$4();
            }
        });
    }

    public boolean shouldCloseOnBackgroundClick() {
        return true;
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void unfocusFragment(boolean z) {
        if (z) {
            this.mSpringScale.setEndValue(0.949999988079071d);
        } else {
            this.mSpringScale.setCurrentValue(0.949999988079071d);
            this.mSpringScale.setAtRest();
        }
    }
}
