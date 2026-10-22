package com.evie.screen;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class TermsAndConditionsActivity_ViewBinding implements Unbinder {
    private TermsAndConditionsActivity target;

    public TermsAndConditionsActivity_ViewBinding(TermsAndConditionsActivity termsAndConditionsActivity, View view) {
        this.target = termsAndConditionsActivity;
        termsAndConditionsActivity.mTextView = (TextView) Utils.findRequiredViewAsType(view, R.id.text, "field 'mTextView'", TextView.class);
        termsAndConditionsActivity.mProgress = (ProgressBar) Utils.findRequiredViewAsType(view, R.id.progress, "field 'mProgress'", ProgressBar.class);
        termsAndConditionsActivity.mDialogLayout = (FrameLayout) Utils.findRequiredViewAsType(view, R.id.dialog, "field 'mDialogLayout'", FrameLayout.class);
    }

    @Override // butterknife.Unbinder
    public void unbind() {
        TermsAndConditionsActivity termsAndConditionsActivity = this.target;
        if (termsAndConditionsActivity == null) {
            throw new IllegalStateException("Bindings already cleared.");
        }
        this.target = null;
        termsAndConditionsActivity.mTextView = null;
        termsAndConditionsActivity.mProgress = null;
        termsAndConditionsActivity.mDialogLayout = null;
    }
}
