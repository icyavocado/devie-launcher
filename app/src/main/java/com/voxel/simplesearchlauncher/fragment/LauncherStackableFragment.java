package com.voxel.simplesearchlauncher.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.voxel.launcher3.LauncherApplication;

/* JADX INFO: loaded from: classes2.dex */
public abstract class LauncherStackableFragment extends Fragment {
    private OnFragmentInteractionListener fragmentInteractionListener;
    public static final String PREFIX = LauncherStackableFragment.class.getCanonicalName() + ".";
    public static final String BACKSTACK_INDEX_KEY = PREFIX + "backstack_index";
    public static final String REQUEST_CODE_KEY = PREFIX + "request_code";
    public static final String REQUEST_INTENT_KEY = PREFIX + "request_intent";
    public static final String RESULT_CODE_KEY = PREFIX + "result_code";
    public static final String RESULT_INTENT_KEY = PREFIX + "result_intent";

    public interface OnFragmentInteractionListener {
        void onStackableFragmentRequestClose(LauncherStackableFragment launcherStackableFragment);

        void onStackableFragmentWillRequestClose(LauncherStackableFragment launcherStackableFragment);

        void popStackFragment(LauncherStackableFragment launcherStackableFragment, boolean z);

        boolean providesTransitionAnimation();

        void pushStackFragment(LauncherStackableFragment launcherStackableFragment);
    }

    public boolean canSaveInstanceState() {
        return true;
    }

    public abstract void focusFragment(boolean z);

    public final int getBackStackIndex() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return -1;
        }
        return arguments.getInt(BACKSTACK_INDEX_KEY, -1);
    }

    public final int getRequestCode() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return -1;
        }
        return arguments.getInt(REQUEST_CODE_KEY, -1);
    }

    public final int getResultCode() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return 0;
        }
        return arguments.getInt(RESULT_CODE_KEY, 0);
    }

    public final Intent getResultIntent() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        return (Intent) arguments.getParcelable(RESULT_INTENT_KEY);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void notifyFragmentRequestClose() {
        OnFragmentInteractionListener onFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onStackableFragmentRequestClose(this);
            onFragmentDidClose();
        }
    }

    protected final void notifyFragmentWillRequestClose() {
        OnFragmentInteractionListener onFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onStackableFragmentWillRequestClose(this);
            onFragmentWillClose();
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    @Override // androidx.fragment.app.Fragment
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.fragmentInteractionListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException unused) {
            throw new ClassCastException(activity.toString() + " must implement " + OnFragmentInteractionListener.class.getCanonicalName());
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
    }

    protected void onFragmentDidClose() {
    }

    protected void onFragmentWillClose() {
    }

    public abstract void onPopLauncherStackedAppearance(FragmentActivity fragmentActivity, boolean z);

    public abstract void onPushLauncherStackedAppearance(OnFragmentInteractionListener onFragmentInteractionListener);

    public void onStackableFragmentResult(int i, int i2, Intent intent) {
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void popStackFragment(LauncherStackableFragment launcherStackableFragment, boolean z) {
        OnFragmentInteractionListener onFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.popStackFragment(launcherStackableFragment, z);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final void pushStackFragment(LauncherStackableFragment launcherStackableFragment) {
        OnFragmentInteractionListener onFragmentInteractionListener = this.fragmentInteractionListener;
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.pushStackFragment(launcherStackableFragment);
        }
    }

    public boolean requiresFullScreen() {
        return false;
    }

    public abstract void runExitAnimation();

    public final void setBackStackIndex(int i) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            setArguments(arguments);
        }
        arguments.putInt(BACKSTACK_INDEX_KEY, i);
    }

    public final void setResultCode(int i) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            setArguments(arguments);
        }
        arguments.putInt(RESULT_CODE_KEY, i);
    }

    public final void setResultIntent(Intent intent) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            setArguments(arguments);
        }
        arguments.putParcelable(RESULT_INTENT_KEY, intent);
    }

    public abstract void unfocusFragment(boolean z);
}
