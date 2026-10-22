package com.voxel.simplesearchlauncher.settings.lock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.voxel.simplesearchlauncher.gestures.GestureType;
import com.voxel.simplesearchlauncher.gestures.action.CustomGestureAction;
import com.voxel.simplesearchlauncher.model.managers.SettingsStorage;
import com.voxel.simplesearchlauncher.receiver.DeviceAdminReceiverSub;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class ChooseLockMethodDialogFragment extends DialogFragment {
    private static final String TAG = "ChooseLockMethodDialogFragment";
    private boolean dismissOnResume;
    private ComponentName mDeviceAdminComponentName;
    private GestureType mGestureType;
    private SettingsStorage mSettingsStorage;

    /* JADX INFO: Access modifiers changed from: private */
    class LockMethodDialogAdapter extends ArrayAdapter<LockMethodDialogItem> {
        public LockMethodDialogAdapter(Context context, List<LockMethodDialogItem> list) {
            super(context, 0, list);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public long getItemId(int i) {
            return getItem(i).id;
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_lock_method_radiobutton_item, viewGroup, false);
            }
            LockMethodDialogItem item = getItem(i);
            if (item != null) {
                TextView textView = (TextView) view.findViewById(R.id.double_tap_to_lock_method_name);
                TextView textView2 = (TextView) view.findViewById(R.id.double_tap_to_lock_method_description);
                textView.setText(item.methodName);
                textView2.setText(item.methodDescription);
            }
            return view;
        }
    }

    private class LockMethodDialogItem {
        int id;
        int methodDescription;
        int methodName;

        LockMethodDialogItem(int i, int i2, int i3) {
            this.id = i;
            this.methodName = i2;
            this.methodDescription = i3;
        }
    }

    public void lambda$onCreateDialog$0(LockMethodDialogAdapter lockMethodDialogAdapter, AdapterView adapterView, View view, int i, long j) {
        try {
            if (j == 0) {
                if (((DevicePolicyManager) this.getActivity().getSystemService("device_policy")).isAdminActive(this.mDeviceAdminComponentName)) {
                    this.mSettingsStorage.setActionForGestureType(this.mGestureType, CustomGestureAction.SCREEN_LOCK_DEVICE_ADMIN);
                    this.dismiss();
                } else {
                    Intent intent = new Intent("android.app.action.ADD_DEVICE_ADMIN");
                    intent.putExtra("android.app.extra.DEVICE_ADMIN", this.mDeviceAdminComponentName);
                    try {
                        this.dismissOnResume = true;
                        this.startActivityForResult(intent, 1);
                    } catch (ActivityNotFoundException unused) {
                        Toast.makeText(this.getActivity(), R.string.activity_not_found, 0).show();
                    } catch (SecurityException e) {
                        Toast.makeText(this.getActivity(), R.string.msg_no_generic_permission, 0).show();
                        Log.e(TAG, "Launcher does not have the permission to launch " + intent + ". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity.  intent=" + intent, e);
                    }
                }
            } else if (j == 1) {
                if (Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(this.getContext())) {
                    this.mSettingsStorage.setActionForGestureType(this.mGestureType, CustomGestureAction.SCREEN_LOCK_WRITE_SETTINGS);
                    this.dismiss();
                } else {
                    Intent intent2 = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
                    intent2.setData(Uri.parse("package:" + this.getContext().getPackageName()));
                    try {
                        this.dismissOnResume = true;
                        this.startActivityForResult(intent2, 2);
                    } catch (ActivityNotFoundException unused2) {
                        Toast.makeText(this.getActivity(), R.string.activity_not_found, 0).show();
                    } catch (SecurityException e2) {
                        Toast.makeText(this.getActivity(), R.string.msg_no_generic_permission, 0).show();
                        Log.e(TAG, "Launcher does not have the permission to launch " + intent2 + ". Make sure to create a MAIN intent-filter for the corresponding activity or use the exported attribute for this activity.  intent=" + intent2, e2);
                    }
                }
            }
        } catch (Exception unused3) {
        }
        lockMethodDialogAdapter.notifyDataSetChanged();
    }

    public static ChooseLockMethodDialogFragment newInstance(GestureType gestureType) {
        ChooseLockMethodDialogFragment chooseLockMethodDialogFragment = new ChooseLockMethodDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("gesture", gestureType.name());
        chooseLockMethodDialogFragment.setArguments(bundle);
        return chooseLockMethodDialogFragment;
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1) {
            this.dismissOnResume = true;
            if (i2 == -1) {
                this.mSettingsStorage.setActionForGestureType(this.mGestureType, CustomGestureAction.SCREEN_LOCK_DEVICE_ADMIN);
                return;
            }
            return;
        }
        if (i == 2) {
            this.dismissOnResume = true;
            if (Build.VERSION.SDK_INT >= 23 ? Settings.System.canWrite(getContext()) : true) {
                this.mSettingsStorage.setActionForGestureType(this.mGestureType, CustomGestureAction.SCREEN_LOCK_WRITE_SETTINGS);
            }
        }
    }

    @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String string = arguments.getString("gesture");
            if (!TextUtils.isEmpty(string)) {
                this.mGestureType = GestureType.valueOf(string);
            }
        }
        if (this.mGestureType == null) {
            dismiss();
        }
    }

    @Override // androidx.fragment.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        this.mSettingsStorage = SettingsStorage.getInstance();
        this.mDeviceAdminComponentName = new ComponentName(getActivity(), (Class<?>) DeviceAdminReceiverSub.class);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View viewInflate = getActivity().getLayoutInflater().inflate(R.layout.dialog_list, (ViewGroup) null);
        ArrayList arrayList = new ArrayList();
        arrayList.add(new LockMethodDialogItem(0, R.string.app_settings_gestures_double_tap_lock_type_device_admin_title, R.string.app_settings_alert_dialog_double_tap_msg));
        arrayList.add(new LockMethodDialogItem(1, R.string.app_settings_gestures_double_tap_lock_type_timeout_title, R.string.app_settings_gestures_double_tap_lock_type_timeout_description));
        final LockMethodDialogAdapter lockMethodDialogAdapter = new LockMethodDialogAdapter(getActivity(), arrayList);
        ListView listView = (ListView) viewInflate.findViewById(R.id.list);
        listView.setAdapter((ListAdapter) lockMethodDialogAdapter);
        listView.setChoiceMode(1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.voxel.simplesearchlauncher.settings.lock.-$$Lambda$ChooseLockMethodDialogFragment$UnjA1DAifX3_AWjZHN0cLR-2RSk
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                ChooseLockMethodDialogFragment.this.lambda$onCreateDialog$0( lockMethodDialogAdapter, adapterView, view, i, j);
            }
        });
        builder.setTitle(R.string.app_settings_gestures_dialog_lock_type).setView(viewInflate).setCancelable(true).setNegativeButton(R.string.button_label_cancel, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.lock.-$$Lambda$ChooseLockMethodDialogFragment$p2kheynMaE5Y8Y0WgzB-qyo-bVo
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    @Override // androidx.fragment.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        if (getParentFragment() != null) {
            getParentFragment().onActivityResult(getTargetRequestCode(), -1, null);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        if (this.dismissOnResume) {
            dismiss();
        }
    }
}
