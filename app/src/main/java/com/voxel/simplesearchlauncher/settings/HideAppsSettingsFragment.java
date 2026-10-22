package com.voxel.simplesearchlauncher.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.voxel.launcher3.LauncherAppState;
import com.voxel.launcher3.LauncherModel;
import com.voxel.launcher3.compat.UserHandleCompat;
import com.voxel.launcher3.fresco.HybridNetworkFetcher;
import com.voxel.simplesearchlauncher.hideapps.HideAppsModel;
import com.voxel.simplesearchlauncher.model.AppsInfoDataHelper;
import dagger.android.support.AndroidSupportInjection;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import com.ovacoda.devie.R;
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
    private AppListSection mAppListSection;
    Context mApplicationContext;
    private Set<AppsInfoDataHelper.AppInfo> mChosenApps;
    private ConfigSection mConfigSection;
    HideAppsModel mHideAppsModel;
    private RecyclerView mRecyclerView;
    private SectionedRecyclerViewAdapter mSectionedAdapter;
    private boolean mSkipOnDestroy;

    class AppListSection extends StatelessSection {
        private View.OnClickListener mItemClickListener;

        class HeaderViewHolder extends RecyclerView.ViewHolder {

            TextView mName;

            TextView mSubtext;

            SwitchCompat mSwitch;

            public HeaderViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        /* REWORK: must be static — non-static nested binding carries an implicit
           enclosing instance that breaks ButterKnife's (target, View) reflection. */
        public static class HeaderViewHolder_ViewBinding implements Unbinder {
            private HeaderViewHolder target;

            public HeaderViewHolder_ViewBinding(HeaderViewHolder headerViewHolder, View view) {
                this.target = headerViewHolder;
                headerViewHolder.mName = (TextView) Utils.findRequiredViewAsType(view, R.id.name, "field 'mName'", TextView.class);
                headerViewHolder.mSubtext = (TextView) Utils.findRequiredViewAsType(view, R.id.subtext, "field 'mSubtext'", TextView.class);
                headerViewHolder.mSwitch = (SwitchCompat) Utils.findRequiredViewAsType(view, R.id.item_switch, "field 'mSwitch'", SwitchCompat.class);
            }

            @Override // butterknife.Unbinder
            public void unbind() {
                HeaderViewHolder headerViewHolder = this.target;
                if (headerViewHolder == null) {
                    throw new IllegalStateException("Bindings already cleared.");
                }
                this.target = null;
                headerViewHolder.mName = null;
                headerViewHolder.mSubtext = null;
                headerViewHolder.mSwitch = null;
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            SimpleDraweeView mApplicationIcon;

            CheckBox mCheckBox;

            TextView mLabel;

            public ItemViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        /* REWORK: must be static — see HeaderViewHolder_ViewBinding. */
        public static class ItemViewHolder_ViewBinding implements Unbinder {
            private ItemViewHolder target;

            public ItemViewHolder_ViewBinding(ItemViewHolder itemViewHolder, View view) {
                this.target = itemViewHolder;
                itemViewHolder.mCheckBox = (CheckBox) Utils.findRequiredViewAsType(view, R.id.checkbox, "field 'mCheckBox'", CheckBox.class);
                itemViewHolder.mApplicationIcon = (SimpleDraweeView) Utils.findRequiredViewAsType(view, R.id.application_icon, "field 'mApplicationIcon'", SimpleDraweeView.class);
                itemViewHolder.mLabel = (TextView) Utils.findRequiredViewAsType(view, R.id.label, "field 'mLabel'", TextView.class);
            }

            @Override // butterknife.Unbinder
            public void unbind() {
                ItemViewHolder itemViewHolder = this.target;
                if (itemViewHolder == null) {
                    throw new IllegalStateException("Bindings already cleared.");
                }
                this.target = null;
                itemViewHolder.mCheckBox = null;
                itemViewHolder.mApplicationIcon = null;
                itemViewHolder.mLabel = null;
            }
        }

        public AppListSection() {
            super(R.layout.settings_activity_notification_section_header, R.layout.hide_app_picker_list_item);
            this.mItemClickListener = new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$HideAppsSettingsFragment$AppListSection$13MXngTTj5OD6SobU6OKxF-lcUU
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    AppListSection.this.lambda$new$0( view);
                }
            };
        }

        public void lambda$new$0(View view) {
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

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public int getContentItemsTotal() {
            return HideAppsSettingsFragment.this.mAppInfos.size();
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            ItemViewHolder itemViewHolder = new ItemViewHolder(view);
            itemViewHolder.itemView.setOnClickListener(this.mItemClickListener);
            itemViewHolder.mCheckBox.setClickable(false);
            return itemViewHolder;
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
            headerViewHolder.mName.setText(R.string.app_settings_hide_apps);
            headerViewHolder.mSubtext.setVisibility(8);
            headerViewHolder.mSwitch.setVisibility(8);
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            AppsInfoDataHelper.AppInfo appInfo = (AppsInfoDataHelper.AppInfo) HideAppsSettingsFragment.this.mAppInfos.get(i);
            boolean zContains = HideAppsSettingsFragment.this.mChosenApps.contains(appInfo);
            viewHolder.itemView.setTag(appInfo);
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            itemViewHolder.mCheckBox.setChecked(zContains);
            itemViewHolder.mLabel.setText(appInfo.appLabel);
            itemViewHolder.mApplicationIcon.setImageURI((Uri) null);
            itemViewHolder.mApplicationIcon.setImageURI(HybridNetworkFetcher.getMagicResourceUri(appInfo.packageName, appInfo.activityName, appInfo.appVersion));
        }
    }

    class ConfigSection extends StatelessSection {

        class ConfigViewHolder extends RecyclerView.ViewHolder {
            SettingsSwitchViewHolder mHideFromSearch;

            public ConfigViewHolder(View view) {
                super(view);
                this.mHideFromSearch = new SettingsSwitchViewHolder(view.findViewById(R.id.hide_from_search));
            }
        }

        public ConfigSection() {
            super(R.layout.settings_activity_hide_apps_config, R.layout.settings_activity_icon_switch_item);
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public int getContentItemsTotal() {
            return 0;
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new ConfigViewHolder(view);
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new RecyclerView.ViewHolder(view) { // from class: com.voxel.simplesearchlauncher.settings.HideAppsSettingsFragment.ConfigSection.1
            };
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {
            ConfigViewHolder configViewHolder = (ConfigViewHolder) viewHolder;
            configViewHolder.mHideFromSearch.mLabel.setText(R.string.app_settings_hide_apps_hide_from_search);
            configViewHolder.mHideFromSearch.mSubLabel.setVisibility(0);
            configViewHolder.mHideFromSearch.mSubLabel.setText(R.string.app_settings_hide_apps_hide_from_search_subtext);
            configViewHolder.mHideFromSearch.mSwitch.setChecked(!HideAppsSettingsFragment.this.mHideAppsModel.isHiddenSearchable());
            configViewHolder.mHideFromSearch.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$HideAppsSettingsFragment$ConfigSection$drkk_IJYBErGysoQmbYo1HHCp2I
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    HideAppsSettingsFragment.this.mHideAppsModel.setHiddenSearchable(!z);
                }
            });
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
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

        @Override // androidx.fragment.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), 5);
            builder.setMessage(getResources().getString(R.string.app_settings_hide_apps_dialog_confirm));
            builder.setPositiveButton(R.string.app_settings_hide_apps_dialog_confirm_keep, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$HideAppsSettingsFragment$ConfirmHideAppDialog$jyNknLyt_RvZ9UgXZ01ecjCg4uM
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    ConfirmHideAppDialog.this.lambda$onCreateDialog$0( dialogInterface, i);
                }
            });
            builder.setNegativeButton(R.string.app_settings_hide_apps_dialog_confirm_remove, new DialogInterface.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$HideAppsSettingsFragment$ConfirmHideAppDialog$I2AkpzWXOkP6s6_04EP8DBaXd0I
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    ConfirmHideAppDialog.this.lambda$onCreateDialog$1( dialogInterface, i);
                }
            });
            return builder.create();
        }

        @Override // androidx.fragment.app.DialogFragment, androidx.fragment.app.Fragment
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

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1000) {
            if (i2 == 1) {
                saveChangesImpl(false);
            } else if (i2 == 2) {
                saveChangesImpl(true);
            }
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override // com.voxel.simplesearchlauncher.settings.SettingsActivity.OnSettingsBackButtonPressedListener
    public void onBackPressed() {
        Fragment fragmentFindFragmentByTag = getFragmentManager().findFragmentByTag(DIALOG_TAG);
        if (fragmentFindFragmentByTag != null && (fragmentFindFragmentByTag instanceof ConfirmHideAppDialog)) {
            ((ConfirmHideAppDialog) fragmentFindFragmentByTag).dismiss();
        }
        saveChanges();
        this.mSkipOnDestroy = true;
    }

    @Override // androidx.fragment.app.Fragment
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
        Collections.sort(this.mAppInfos, new Comparator<AppsInfoDataHelper.AppInfo>() { // from class: com.voxel.simplesearchlauncher.settings.HideAppsSettingsFragment.1
            final Collator mCollator = Collator.getInstance(Locale.getDefault());

            {
                this.mCollator.setStrength(0);
            }

            @Override // java.util.Comparator
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

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_hide_apps_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        if (!this.mSkipOnDestroy) {
            saveChangesImpl(false);
        }
        super.onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        this.mRecyclerView = (RecyclerView) view;
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.mAppListSection = new AppListSection();
        this.mConfigSection = new ConfigSection();
        this.mSectionedAdapter = new SectionedRecyclerViewAdapter();
        this.mSectionedAdapter.addSection(this.mConfigSection);
        this.mSectionedAdapter.addSection(this.mAppListSection);
        this.mRecyclerView.setAdapter(this.mSectionedAdapter);
    }
}
