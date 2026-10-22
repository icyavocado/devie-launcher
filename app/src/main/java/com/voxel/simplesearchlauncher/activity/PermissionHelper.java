package com.voxel.simplesearchlauncher.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.ovacoda.devie.R;

/* JADX INFO: loaded from: classes.dex */
public class PermissionHelper {
    private final int mCallRequestCode;
    private Intent mPendingCallIntent;

    public static class CallPermissionDialogFragment extends DialogFragment {
        private static final String PREFIX = CallPermissionDialogFragment.class.getCanonicalName();
        private static final String REQUEST_CODE_KEY = PREFIX + ".request_code_key";

        public void lambda$onCreateDialog$0(int i, Dialog dialog, View view) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{"android.permission.CALL_PHONE"}, i);
            dialog.dismiss();
        }

        public static CallPermissionDialogFragment newInstance(int i) {
            Bundle bundle = new Bundle();
            bundle.putInt(REQUEST_CODE_KEY, i);
            CallPermissionDialogFragment callPermissionDialogFragment = new CallPermissionDialogFragment();
            callPermissionDialogFragment.setArguments(bundle);
            return callPermissionDialogFragment;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            final int i = getArguments().getInt(REQUEST_CODE_KEY, 0);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View viewInflate = getActivity().getLayoutInflater().inflate(R.layout.dialog_permission_call, (ViewGroup) null);
            Button button = (Button) viewInflate.findViewById(R.id.positive_button);
            Button button2 = (Button) viewInflate.findViewById(R.id.negative_button);
            View viewFindViewById = viewInflate.findViewById(R.id.close_button);
            button.setText(R.string.onboarding_permission_grant_access);
            button2.setText(R.string.onboarding_permission_deny_access);
            builder.setView(viewInflate);
            final AlertDialog alertDialogCreate = builder.create();
            alertDialogCreate.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.activity.-$$Lambda$PermissionHelper$CallPermissionDialogFragment$IbcQZMl9o9lgGthdqvZVNv-ZeOo
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    CallPermissionDialogFragment.this.lambda$onCreateDialog$0( i, alertDialogCreate, view);
                }
            });
            button2.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.activity.-$$Lambda$PermissionHelper$CallPermissionDialogFragment$KrolzV54IHoHxupT1t0kPVqQF4E
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    alertDialogCreate.cancel();
                }
            });
            viewFindViewById.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.activity.-$$Lambda$PermissionHelper$CallPermissionDialogFragment$T_hQABFR4sfKlPdRYbGcr7rB-rc
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    alertDialogCreate.cancel();
                }
            });
            return alertDialogCreate;
        }
    }

    public PermissionHelper(int i) {
        this.mCallRequestCode = i;
    }

    public boolean checkIntentPermission(Activity activity, Intent intent) {
        if (intent.getComponent() != null || !"android.intent.action.CALL".equals(intent.getAction()) || ContextCompat.checkSelfPermission(activity, "android.permission.CALL_PHONE") == 0) {
            return true;
        }
        CallPermissionDialogFragment.newInstance(this.mCallRequestCode).show(activity.getFragmentManager(), "dialog");
        this.mPendingCallIntent = intent;
        return false;
    }

    public boolean handleRequestPermissionsResult(Activity activity, int i, String[] strArr, int[] iArr) {
        Intent intent = this.mPendingCallIntent;
        this.mPendingCallIntent = null;
        if (i != this.mCallRequestCode) {
            return false;
        }
        if (iArr.length <= 0 || iArr[0] != 0) {
            return true;
        }
        activity.startActivity(intent);
        return true;
    }
}
