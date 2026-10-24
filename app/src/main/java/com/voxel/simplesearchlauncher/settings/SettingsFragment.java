package com.voxel.simplesearchlauncher.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.voxel.simplesearchlauncher.activity.OnboardingActivity;
import com.voxel.simplesearchlauncher.activity.ResetLauncherActivity;
import com.voxel.simplesearchlauncher.activity.SafeActivityStarter;
import com.voxel.simplesearchlauncher.activity.onboarding.PermissionsSlide;
import com.voxel.simplesearchlauncher.analytics.AnalyticsHandler;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import com.ovacoda.devie.BuildConfig;
import com.ovacoda.devie.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* JADX INFO: loaded from: classes2.dex */
public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    SectionedRecyclerViewAdapter mAdapter;
    RecyclerView mRecyclerView;
    Section[] mSections;

    private enum SettingsGroup {
        ALERT(0, false),
        CUSTOMIZATION(R.string.app_settings_item_customization_header, true),
        GENERAL(R.string.app_settings_item_general_header, true),
        SWITCH(0, true);

        int headerTextResId;
        boolean showHeader;

        SettingsGroup(int i, boolean z) {
            this.headerTextResId = i;
            this.showHeader = z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    enum SettingsItemType {
        PERMISSIONS_REQUIRED(SettingsGroup.ALERT, 0, R.string.app_settings_item_permission_required),
        DESKTOP_SETTINGS(SettingsGroup.CUSTOMIZATION, R.drawable.ic_phone_android_white_24_px, R.string.app_settings_item_desktop_title),
        DOCK_SETTINGS(SettingsGroup.CUSTOMIZATION, R.drawable.ic_dock_white_24_px_2, R.string.app_settings_item_dock_title),
        CHANGE_ICON_PACK(SettingsGroup.CUSTOMIZATION, R.drawable.ic_apps_white_24_px, R.string.app_settings_item_icon_pack_title),
        FOLDER_SETTINGS(SettingsGroup.CUSTOMIZATION, R.drawable.ic_folders, R.string.app_settings_item_folder_title),
        BADGES(SettingsGroup.CUSTOMIZATION, R.drawable.group_5, R.string.app_settings_item_badges_title),
        GESTURES(SettingsGroup.CUSTOMIZATION, R.drawable.ic_gesture_white_24_px, R.string.app_settings_item_gestures_title),
        APP_DRAWER(SettingsGroup.CUSTOMIZATION, R.drawable.drawer, R.string.app_settings_item_app_drawer_title),
        SEARCH(SettingsGroup.CUSTOMIZATION, R.drawable.ic_search_white_24_px, R.string.app_settings_item_search_title),
        HIDE_APPS(SettingsGroup.CUSTOMIZATION, R.drawable.ic_visibility_off_white_24_px, R.string.app_settings_hide_apps),
        BACKUP_AND_RESTORE(SettingsGroup.GENERAL, R.drawable.ic_import_export_white_24_px_2, R.string.app_settings_item_backup_restore_title),
        FEEDBACK(SettingsGroup.GENERAL, R.drawable.ic_feedback_white_24_px, R.string.app_settings_item_feedback_title),
        /* REWORK: privacy-policy web page cut; the row now shows the no-tracking copy. */
        PRIVACY_POLICY(SettingsGroup.GENERAL, R.drawable.ic_info_white_24_px, R.string.app_settings_item_no_tracking_title),
        SWITCH_LAUNCHER(SettingsGroup.SWITCH, R.drawable.ic_cached_white_24_px, R.string.app_settings_item_switch_launcher);

        SettingsGroup group;
        int iconResId;
        int mainTextResId;

        SettingsItemType(SettingsGroup settingsGroup, int i, int i2) {
            this.group = settingsGroup;
            this.iconResId = i;
            this.mainTextResId = i2;
        }
    }

    class SettingsSection extends Section {
        int mHeaderResId;
        List<SettingsItemType> mItems;

        class HeaderViewHolder extends RecyclerView.ViewHolder {

            TextView mTextView;

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
                headerViewHolder.mTextView = (TextView) Utils.findRequiredViewAsType(view, R.id.text, "field 'mTextView'", TextView.class);
            }

            @Override // butterknife.Unbinder
            public void unbind() {
                HeaderViewHolder headerViewHolder = this.target;
                if (headerViewHolder == null) {
                    throw new IllegalStateException("Bindings already cleared.");
                }
                this.target = null;
                headerViewHolder.mTextView = null;
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            View mDivider;

            ImageView mImageView;

            TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        /* REWORK: must be static — see HeaderViewHolder_ViewBinding. */
        public static class ViewHolder_ViewBinding implements Unbinder {
            private ViewHolder target;

            public ViewHolder_ViewBinding(ViewHolder viewHolder, View view) {
                this.target = viewHolder;
                viewHolder.mImageView = (ImageView) Utils.findRequiredViewAsType(view, R.id.list_item_icon, "field 'mImageView'", ImageView.class);
                viewHolder.mTextView = (TextView) Utils.findRequiredViewAsType(view, R.id.list_item_label, "field 'mTextView'", TextView.class);
                viewHolder.mDivider = Utils.findRequiredView(view, R.id.list_divider, "field 'mDivider'");
            }

            @Override // butterknife.Unbinder
            public void unbind() {
                ViewHolder viewHolder = this.target;
                if (viewHolder == null) {
                    throw new IllegalStateException("Bindings already cleared.");
                }
                this.target = null;
                viewHolder.mImageView = null;
                viewHolder.mTextView = null;
                viewHolder.mDivider = null;
            }
        }

        SettingsSection(int i, List<SettingsItemType> list) {
            super(R.layout.settings_activity_top_level_header, R.layout.settings_activity_top_level_list_item, 0, 0);
            this.mItems = new ArrayList();
            this.mHeaderResId = i;
            this.mItems = list;
        }

        public void lambda$onBindItemViewHolder$0(View view) {
            Intent intent = new Intent(SettingsFragment.this.getContext(), (Class<?>) OnboardingActivity.class);
            intent.putExtra("permission", true);
            try {
                SettingsFragment.this.startActivity(intent);
            } catch (Exception unused) {
            }
        }

        public void lambda$onBindItemViewHolder$13(View view) {
            ResetLauncherActivity.resetHome(SettingsFragment.this.getActivity(), true, false);
            AnalyticsHandler.getInstance().logEvent("ev_settings_switch_launcher", null);
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public int getContentItemsTotal() {
            return this.mItems.size();
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        public SettingsItemType getItem(int i) {
            return this.mItems.get(i);
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ViewHolder(view);
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
            if (this.mHeaderResId > 0) {
                headerViewHolder.mTextView.setText(this.mHeaderResId);
                headerViewHolder.mTextView.setVisibility(0);
            } else {
                headerViewHolder.mTextView.setText((CharSequence) null);
                headerViewHolder.mTextView.setVisibility(8);
            }
        }

        @Override // io.github.luizgrp.sectionedrecyclerviewadapter.Section
        public void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            ViewHolder viewHolder2 = (ViewHolder) viewHolder;
            final SettingsItemType item = getItem(i);
            viewHolder2.mTextView.setText(item.mainTextResId);
            if (item.iconResId > 0) {
                viewHolder2.mImageView.setImageResource(item.iconResId);
            } else {
                viewHolder2.mImageView.setImageDrawable(null);
            }
            if (i == getContentItemsTotal() - 1) {
                viewHolder2.mDivider.setVisibility(4);
            } else {
                viewHolder2.mDivider.setVisibility(0);
            }
            switch (item) {
                case PERMISSIONS_REQUIRED:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$vgsm0_gXVl74WzXgW2F-AeqpYm4
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsSection.this.lambda$onBindItemViewHolder$0( view);
                        }
                    });
                    break;
                case DESKTOP_SETTINGS:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$N5XCNzS4DQ4VGtEraeN2AbJ3x8k
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.startWallpaperFragmentActivity(DesktopSettingsFragment.class.getName(), item.mainTextResId);
                        }
                    });
                    break;
                case DOCK_SETTINGS:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$xDAD9EIT78CGYaLq3Yw09EpGd-Y
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.startWallpaperFragmentActivity(DockSettingsFragment.class.getName(), item.mainTextResId);
                        }
                    });
                    break;
                case FOLDER_SETTINGS:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$Tz3JwSpT8T_laLzwxkoM78ksvdg
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.startWallpaperFragmentActivity(FolderSettingsFragment.class.getName(), item.mainTextResId);
                        }
                    });
                    break;
                case APP_DRAWER:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$HB2N1472itB_aixG3mY8xZ4Buj0
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.startWallpaperFragmentActivity(AppDrawerSettingsFragment.class.getName(), item.mainTextResId);
                        }
                    });
                    break;
                case GESTURES:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$XiY0c2qgxnIeXRUgc4gIFmKaWxk
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.pushFragment(new CustomGestureSettingsFragment(), item.mainTextResId);
                        }
                    });
                    break;
                case CHANGE_ICON_PACK:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$PAat3OgY92nls-0psSdb2vbUovs
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.startWallpaperFragmentActivity(IconPackSettingsFragment.class.getName(), item.mainTextResId);
                        }
                    });
                    break;
                case BADGES:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$mNEXaEQieKgzf53DlKk9iUVr-vc
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.startWallpaperFragmentActivity(NotificationSettingsFragment.class.getName(), item.mainTextResId);
                        }
                    });
                    break;
                case SEARCH:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$lCP24Pp9bETkrwHQUczYkEIce2w
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.pushFragment(new SearchSettingsFragment(), item.mainTextResId);
                        }
                    });
                    break;
                case HIDE_APPS:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$KQHW7tSY03w9eAsvgWxbFymzPe0
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.pushFragment(new HideAppsSettingsFragment(), item.mainTextResId);
                        }
                    });
                    break;
                case BACKUP_AND_RESTORE:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$m3KlLzLJb9bw4SFO_bBtVXOvvI4
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.pushFragment(new BackupRestoreSettingsFragment(), item.mainTextResId);
                        }
                    });
                    break;
                case FEEDBACK:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$WJcLw86tMAXB1BZl9ajR8mpjafA
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsFragment.this.pushFragment(new FeedbackFragment(), item.mainTextResId);
                        }
                    });
                    break;
                case PRIVACY_POLICY:
                    /* REWORK: no web page — a local no-tracking statement. */
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$YdhQNetwkwc3mpjcdtZgWU1yTG0
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            new androidx.appcompat.app.AlertDialog.Builder(SettingsFragment.this.getActivity())
                                    .setTitle(R.string.app_settings_item_no_tracking_title)
                                    .setMessage(R.string.app_settings_no_tracking_blurb)
                                    .setPositiveButton(R.string.app_settings_item_backup_restore_backup_success_dialog_positive, null)
                                    .show();
                        }
                    });
                    break;
                case SWITCH_LAUNCHER:
                    viewHolder2.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.voxel.simplesearchlauncher.settings.-$$Lambda$SettingsFragment$SettingsSection$k-9H2UD87oR4UIGIJQKd57OhZ7c
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            SettingsSection.this.lambda$onBindItemViewHolder$13( view);
                        }
                    });
                    break;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void pushFragment(Fragment fragment, int i) {
        FragmentTransaction fragmentTransactionBeginTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransactionBeginTransaction.setCustomAnimations(R.anim.fade_in_short, R.anim.fade_out_short, R.anim.fade_in_short, R.anim.fade_out_short);
        fragmentTransactionBeginTransaction.replace(R.id.container, fragment);
        fragmentTransactionBeginTransaction.setBreadCrumbTitle(i);
        fragmentTransactionBeginTransaction.addToBackStack(null);
        fragmentTransactionBeginTransaction.commit();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public void startWallpaperFragmentActivity(String str, int i) {
        Intent intent = new Intent(getActivity(), (Class<?>) TransparentSettingsActivity.class);
        intent.putExtra("show_title", i);
        intent.putExtra("show_fragment", str);
        FragmentActivity activity = getActivity();
        if (activity instanceof SafeActivityStarter) {
            ((SafeActivityStarter) activity).startActivitySafely(intent);
        } else {
            activity.startActivity(intent);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.settings_activity_top_level_fragment, viewGroup, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        if (PermissionsSlide.needsPermissions(getActivity())) {
            this.mSections[SettingsGroup.ALERT.ordinal()].setVisible(true);
            this.mRecyclerView.getAdapter().notifyDataSetChanged();
        } else {
            this.mSections[SettingsGroup.ALERT.ordinal()].setVisible(false);
            this.mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle bundle) {
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        /* REWORK: version footer under the switch-launcher row. */
        TextView versionFooter = (TextView) view.findViewById(R.id.app_version_footer);
        versionFooter.setText(getString(R.string.app_settings_version_footer, BuildConfig.VERSION_NAME, BuildConfig.GIT_SHA));
        this.mAdapter = new SectionedRecyclerViewAdapter();
        this.mSections = new Section[SettingsGroup.values().length];
        for (int i = 0; i < this.mSections.length; i++) {
            SettingsGroup settingsGroup = SettingsGroup.values()[i];
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < SettingsItemType.values().length; i2++) {
                SettingsItemType settingsItemType = SettingsItemType.values()[i2];
                if (settingsItemType.group == settingsGroup) {
                    arrayList.add(settingsItemType);
                }
            }
            this.mSections[i] = new SettingsSection(settingsGroup.headerTextResId, arrayList);
            this.mSections[i].setHasHeader(settingsGroup.showHeader);
            this.mAdapter.addSection(this.mSections[i]);
        }
        SettingsSection settingsSection = new SettingsSection(0, Collections.emptyList());
        settingsSection.setHasHeader(true);
        this.mAdapter.addSection(settingsSection);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.mRecyclerView.setAdapter(this.mAdapter);
    }
}
