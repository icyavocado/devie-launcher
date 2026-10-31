package com.voxel.simplesearchlauncher.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.evie.common.HybridNetworkFetcherCommon;
import com.ovacoda.devie.R;
import com.ovacoda.devie.images.IconLoader;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.simplesearchlauncher.hideapps.HideAppsModel;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import dagger.android.support.AndroidSupportInjection;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/* JADX INFO: loaded from: classes2.dex */
public class HideAppsSettingsFragment extends Fragment implements SettingsActivity.OnSettingsBackButtonPressedListener {
    private static final String DIALOG_TAG = HideAppsSettingsFragment.class.getCanonicalName() + ".dialog";

    private List<AppsInfoDataHelper.AppInfo> mAppInfos;
    Context mApplicationContext;
    private Set<AppsInfoDataHelper.AppInfo> mChosenApps;
    HideAppsModel mHideAppsModel;
    private LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;
    private boolean mSkipOnDestroy;

    private class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final View.OnClickListener mItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppListAdapter.this.lambda$new$1(view);
            }
        };

        public void lambda$new$1(View view) {
            AppsInfoDataHelper.AppInfo appInfo = (AppsInfoDataHelper.AppInfo) view.getTag();
            if (appInfo == null) {
                return;
            }
            boolean zContains = HideAppsSettingsFragment.this.mChosenApps.contains(appInfo);
            ItemViewHolder itemViewHolder = (ItemViewHolder) HideAppsSettingsFragment.this.mRecyclerView.getChildViewHolder(view);
            if (zContains) {
                HideAppsSettingsFragment.this.mChosenApps.remove(appInfo);
            } else {
                HideAppsSettingsFragment.this.mChosenApps.add(appInfo);
            }
            if (itemViewHolder != null) {
                itemViewHolder.mCheckBox.setChecked(!zContains);
            }
        }

        @Override
        public int getItemCount() {
            return HideAppsSettingsFragment.this.mAppInfos.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ItemViewHolder itemViewHolder = new ItemViewHolder(HideAppsSettingsFragment.this.mLayoutInflater.inflate(R.layout.hide_app_picker_list_item, parent, false));
            itemViewHolder.itemView.setOnClickListener(this.mItemClickListener);
            itemViewHolder.mCheckBox.setClickable(false);
            return itemViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            bindItem((ItemViewHolder) viewHolder, position);
        }

        private void bindItem(ItemViewHolder itemViewHolder, int position) {
            AppsInfoDataHelper.AppInfo appInfo = (AppsInfoDataHelper.AppInfo) HideAppsSettingsFragment.this.mAppInfos.get(position);
            boolean zContains = HideAppsSettingsFragment.this.mChosenApps.contains(appInfo);
            itemViewHolder.itemView.setTag(appInfo);
            itemViewHolder.mCheckBox.setChecked(zContains);
            itemViewHolder.mLabel.setText(appInfo.appLabel);
            itemViewHolder.mApplicationIcon.setImageDrawable(null);
            IconLoader.getInstance().loadInto(itemViewHolder.mApplicationIcon, HybridNetworkFetcherCommon.getMagicResourceUri(appInfo.packageName, appInfo.activityName, appInfo.appVersion));
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView mApplicationIcon;

        CheckBox mCheckBox;

        TextView mLabel;

        public ItemViewHolder(View view) {
            super(view);
            this.mApplicationIcon = (ImageView) view.findViewById(R.id.application_icon);
            this.mCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
            this.mLabel = (TextView) view.findViewById(R.id.label);
        }
    }

    public static class ConfirmHideAppDialog extends DialogFragment {
        public void lambda$onCreateDialog$0(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            if (this.getTargetFragment() != null) {
                this.getTargetFragment().onActivityResult(this.getTargetRequestCode(), 1, null);
            }
        }

        public void lambda$onCreateDialog$1(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
            if (this.getTargetFragment() != null) {
                this.getTargetFragment().onActivityResult(this.getTargetRequestCode(), 2, null);
            }
        }

        public static ConfirmHideAppDialog newInstance() {
            return new ConfirmHideAppDialog();
        }

        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), 5);
            builder.setMessage(getResources().getString(R.string.app_settings_hide_apps_dialog_confirm));
            builder.setPositiveButton(R.string.app_settings_hide_apps_dialog_confirm_keep, new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i) {
                    ConfirmHideAppDialog.this.lambda$onCreateDialog$0( dialogInterface, i);
                }
            });
            builder.setNegativeButton(R.string.app_settings_hide_apps_dialog_confirm_remove, new DialogInterface.OnClickListener() {
                @Override
                public final void onClick(DialogInterface dialogInterface, int i) {
                    ConfirmHideAppDialog.this.lambda$onCreateDialog$1( dialogInterface, i);
                }
            });
            return builder.create();
        }

        @Override
        public void onSaveInstanceState(Bundle bundle) {
            if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), 1, null);
            }
            setTargetFragment(null, 0);
            super.onSaveInstanceState(bundle);
        }
    }

    private boolean saveChanges() {
        boolean z;
        Iterator<AppsInfoDataHelper.AppInfo> it = this.mChosenApps.iterator();
        while (true) {
            if (!it.hasNext()) {
                z = false;
                break;
            }
            AppsInfoDataHelper.AppInfo next = it.next();
            if (LauncherModel.getItemInfoForComponentName(new ComponentName(next.packageName, next.activityName), UserHandleCompat.myUserHandle()).size() != 0) {
                z = true;
                break;
            }
        }
        if (!z) {
            saveChangesImpl(false);
            return true;
        }
        ConfirmHideAppDialog confirmHideAppDialogNewInstance = ConfirmHideAppDialog.newInstance();
        confirmHideAppDialogNewInstance.setTargetFragment(this, 1000);
        confirmHideAppDialogNewInstance.show(getFragmentManager(), DIALOG_TAG);
        return false;
    }

    private void saveChangesImpl(boolean z) {
        for (AppsInfoDataHelper.AppInfo appInfo : this.mAppInfos) {
            boolean z2 = appInfo.hidden;
            boolean zContains = this.mChosenApps.contains(appInfo);
            if (z2 != zContains) {
                appInfo.hidden = zContains;
                this.mHideAppsModel.setHidden(appInfo.packageName, appInfo.activityName, zContains);
            }
        }
        if (z) {
            new ArrayList();
            new ArrayList();
            for (AppsInfoDataHelper.AppInfo appInfo2 : this.mChosenApps) {
                LauncherModel.deleteComponentNameFromDatabase(this.mApplicationContext, new ComponentName(appInfo2.packageName, appInfo2.activityName), UserHandleCompat.myUserHandle());
            }
            LauncherAppState.getInstance().getModel().forceReload();
        }
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1000) {
            if (i2 == 1) {
                saveChangesImpl(false);
            } else if (i2 == 2) {
                saveChangesImpl(true);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onBackPressed() {
        Fragment fragmentFindFragmentByTag = getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (fragmentFindFragmentByTag != null && (fragmentFindFragmentByTag instanceof ConfirmHideAppDialog)) {
            ((ConfirmHideAppDialog) fragmentFindFragmentByTag).dismiss();
        }
        saveChanges();
        this.mSkipOnDestroy = true;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mApplicationContext = getContext().getApplicationContext();
        this.mAppInfos = this.mHideAppsModel.getAllApps();
        this.mChosenApps = new HashSet();
        for (AppsInfoDataHelper.AppInfo appInfo : this.mAppInfos) {
            if (appInfo.hidden) {
                this.mChosenApps.add(appInfo);
            }
        }
        Collections.sort(this.mAppInfos, new Comparator<AppsInfoDataHelper.AppInfo>() {
            final Collator mCollator = Collator.getInstance(Locale.getDefault());

            {
                this.mCollator.setStrength(0);
            }

            @Override
            public int compare(AppsInfoDataHelper.AppInfo appInfo2, AppsInfoDataHelper.AppInfo appInfo3) {
                boolean zContains = HideAppsSettingsFragment.this.mChosenApps.contains(appInfo2);
                boolean zContains2 = HideAppsSettingsFragment.this.mChosenApps.contains(appInfo3);
                if (zContains && !zContains2) {
                    return -1;
                }
                if (zContains || !zContains2) {
                    return this.mCollator.compare(appInfo2.appLabel, appInfo3.appLabel);
                }
                return 1;
            }
        });
        this.mSkipOnDestroy = false;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_hide_apps_fragment, viewGroup, false);
    }

    @Override
    public void onDestroy() {
        if (!this.mSkipOnDestroy) {
            saveChangesImpl(false);
        }
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        this.mLayoutInflater = LayoutInflater.from(view.getContext());
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.mRecyclerView.addItemDecoration(new SettingsListDividerDecoration(view.getContext()));
        this.mRecyclerView.setAdapter(new AppListAdapter());
        SettingsSwitchViewHolder hideFromSearch = new SettingsSwitchViewHolder(view.findViewById(R.id.hide_from_search));
        hideFromSearch.mLabel.setText(R.string.app_settings_hide_apps_hide_from_search);
        hideFromSearch.mSubLabel.setVisibility(0);
        hideFromSearch.mSubLabel.setText(R.string.app_settings_hide_apps_hide_from_search_subtext);
        hideFromSearch.mSwitch.setChecked(!this.mHideAppsModel.isHiddenSearchable());
        hideFromSearch.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                HideAppsSettingsFragment.this.mHideAppsModel.setHiddenSearchable(!z);
            }
        });
    }
}