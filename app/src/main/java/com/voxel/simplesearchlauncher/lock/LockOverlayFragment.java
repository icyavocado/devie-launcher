package com.voxel.simplesearchlauncher.lock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.fragment.app.FragmentActivity;
import com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes2.dex */
public class LockOverlayFragment extends LauncherStackableFragment {
    private static final TimeInterpolator INTERPOLATOR = PathInterpolatorCompat.create(0.35f, 0.77f, 0.19f, 1.0f);
    private static final TimeInterpolator REVERSE_INTERPOLATOR = PathInterpolatorCompat.create(0.81f, 0.0f, 0.65f, 0.23f);
    private AnimatorSet mAnimatorSet;

    View mBackground;
    private CancellationSignal mCancellationSignal;

    View mControlGroup;
    private int mControlHeight;

    ImageView mFingerprintIcon;

    View mGroupContainer;
    Handler mHandler;
    private boolean mIsClosing;

    TextView mText;

    ImageView mUnlockIcon;
    View mView;
    private Runnable mCloseRunnable = new Runnable() { // from class: com.voxel.simplesearchlauncher.lock.-$$Lambda$LockOverlayFragment$V0bJJ8DPxVfBFmesOX8xQycXZIs
        @Override // java.lang.Runnable
        public final void run() {
            LockOverlayFragment.this.runExitAnimation();
        }
    };
    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() { // from class: com.voxel.simplesearchlauncher.lock.-$$Lambda$LockOverlayFragment$Dtg6mx2_ItdaG1IUn3AJsrR1OUM
        @Override // android.view.View.OnLongClickListener
        public final boolean onLongClick(View view) {
            return LockOverlayFragment.this.lambda$new$4( view);
        }
    };
    private FingerprintManagerCompat.AuthenticationCallback mAuthenticationCallback = new FingerprintManagerCompat.AuthenticationCallback() { // from class: com.voxel.simplesearchlauncher.lock.LockOverlayFragment.1
        @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
        public void onAuthenticationError(int i, CharSequence charSequence) {
            LockOverlayFragment.this.runExitAnimation();
        }

        @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
        }

        @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            LockOverlayFragment.this.scheduleClose();
        }

        @Override // androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback
        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult authenticationResult) {
            LockOverlayFragment.this.onUnlockSuccess();
        }
    };

    public boolean lambda$new$4(View view) {
        this.onUnlockSuccess();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onUnlockSuccess() {
        SettingsStorage.getInstance().setDesktopUnlockedTemporarily(true);
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, R.string.desktop_unlocked_message, 0).show();
        }
        runExitAnimation();
    }

    void animateOpen() {
        this.mGroupContainer.measure(0, 0);
        this.mView.measure(0, 0);
        this.mControlHeight = this.mGroupContainer.getMeasuredHeight();
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat.setInterpolator(INTERPOLATOR);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.lock.-$$Lambda$LockOverlayFragment$bGDVt5SGtgHDfk376_sJwt4PYQ8
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockOverlayFragment.this.mBackground.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimatorOfFloat2.setInterpolator(INTERPOLATOR);
        valueAnimatorOfFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.lock.-$$Lambda$LockOverlayFragment$wu20FlRqrur2vFB4eP54u94CC7k
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockOverlayFragment lockOverlayFragment = LockOverlayFragment.this;
                lockOverlayFragment.mControlGroup.setTranslationY(lockOverlayFragment.mControlHeight * (1.0f - ((Float) valueAnimator.getAnimatedValue()).floatValue()));
            }
        });
        this.mControlGroup.setTranslationY(this.mControlHeight);
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        this.mAnimatorSet = new AnimatorSet();
        this.mAnimatorSet.play(valueAnimatorOfFloat);
        this.mAnimatorSet.play(valueAnimatorOfFloat2).after(100L);
        this.mAnimatorSet.start();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void focusFragment(boolean z) {
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.lock_overlay_fragment, (ViewGroup) null);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    protected void onFragmentWillClose() {
        super.onFragmentWillClose();
        CancellationSignal cancellationSignal = this.mCancellationSignal;
        if (cancellationSignal != null) {
            cancellationSignal.cancel();
        }
        this.mHandler.removeCallbacks(this.mCloseRunnable);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z) {
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void onPushLauncherStackedAppearance(LauncherStackableFragment.OnFragmentInteractionListener onFragmentInteractionListener) {
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mBackground = view.findViewById(R.id.background);
        this.mGroupContainer = view.findViewById(R.id.container);
        this.mControlGroup = view.findViewById(R.id.control_group);
        this.mFingerprintIcon = (ImageView) view.findViewById(R.id.fingerprint_icon);
        this.mUnlockIcon = (ImageView) view.findViewById(R.id.unlock_icon);
        this.mText = (TextView) view.findViewById(R.id.text);
        this.mView = view;
        this.mHandler = new Handler();
        this.mView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.lock.-$$Lambda$LockOverlayFragment$Nc6ea21zOP9qDFt-DDp6tdsWVcc
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                LockOverlayFragment.this.runExitAnimation();
            }
        });
        FingerprintManagerCompat fingerprintManagerCompatFrom = FingerprintManagerCompat.from(getContext());
        if (fingerprintManagerCompatFrom.isHardwareDetected() && fingerprintManagerCompatFrom.hasEnrolledFingerprints()) {
            setupFingerprints(fingerprintManagerCompatFrom);
        } else {
            setupLongpress();
        }
        scheduleClose();
        animateOpen();
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void runExitAnimation() {
        if (this.mIsClosing) {
            return;
        }
        this.mIsClosing = true;
        notifyFragmentWillRequestClose();
        ValueAnimator valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimatorOfFloat.setInterpolator(REVERSE_INTERPOLATOR);
        valueAnimatorOfFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.lock.-$$Lambda$LockOverlayFragment$mevStcq_kdNPBg4W5TUurlZYet8
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockOverlayFragment.this.mBackground.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ValueAnimator valueAnimatorOfFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
        valueAnimatorOfFloat2.setInterpolator(REVERSE_INTERPOLATOR);
        valueAnimatorOfFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.voxel.simplesearchlauncher.lock.-$$Lambda$LockOverlayFragment$jJH-6rqrYCAoYT7V94rk0JOpn7Y
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                LockOverlayFragment lockOverlayFragment = LockOverlayFragment.this;
                lockOverlayFragment.mControlGroup.setTranslationY(lockOverlayFragment.mControlHeight * (1.0f - ((Float) valueAnimator.getAnimatedValue()).floatValue()));
            }
        });
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        this.mAnimatorSet = new AnimatorSet();
        this.mAnimatorSet.play(valueAnimatorOfFloat);
        this.mAnimatorSet.play(valueAnimatorOfFloat2);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() { // from class: com.voxel.simplesearchlauncher.lock.LockOverlayFragment.2
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                LockOverlayFragment.this.notifyFragmentRequestClose();
            }
        });
        this.mAnimatorSet.start();
    }

    void scheduleClose() {
        this.mHandler.removeCallbacks(this.mCloseRunnable);
        this.mHandler.postDelayed(this.mCloseRunnable, 3000L);
    }

    void setupFingerprints(FingerprintManagerCompat fingerprintManagerCompat) {
        this.mFingerprintIcon.setVisibility(0);
        this.mText.setText(R.string.desktop_unlock_fingerprint);
        this.mCancellationSignal = new CancellationSignal();
        fingerprintManagerCompat.authenticate(null, 0, this.mCancellationSignal, this.mAuthenticationCallback, null);
    }

    void setupLongpress() {
        this.mUnlockIcon.setVisibility(0);
        this.mText.setText(R.string.desktop_unlock_long_press);
        this.mUnlockIcon.setLongClickable(true);
        this.mUnlockIcon.setOnLongClickListener(this.mLongClickListener);
    }

    @Override // com.voxel.simplesearchlauncher.fragment.LauncherStackableFragment
    public void unfocusFragment(boolean z) {
    }
}
